/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.model;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cat.bcn.vincles.lib.business.ServiceGenerator;
import cat.bcn.vincles.lib.business.UserService;
import cat.bcn.vincles.lib.dao.NetworkDAO;
import cat.bcn.vincles.lib.dao.NetworkDAOImpl;
import cat.bcn.vincles.lib.dao.UserDAO;
import cat.bcn.vincles.lib.dao.UserDAOImpl;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.util.ErrorHandler;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.vo.Network;
import cat.bcn.vincles.lib.vo.User;
import cat.bcn.vincles.mobile.util.VinclesMobileConstants;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NetworkModel {
    private static final String TAG = "NetworkModel";
    protected MainModel mainModel;
    private boolean initialized;
    private static NetworkModel instance;
    private NetworkDAO networkDAO;
    private UserDAO userDAO;
    public String view;
    public List<Network> networkList;
    public Network current;
    public static final String NETWORK_JOIN = "networkJoin";
    public static final String NETWORK_SUCCESS = "networkSuccess";

    public static NetworkModel getInstance() {
        if (instance == null) {
            instance = new NetworkModel();
            instance.initialize();
        }
        return instance;
    }

    private NetworkModel() {
    }

    public void initialize() {
        if (!initialized) {
            initialized = true;
            view = "";
            networkDAO = new NetworkDAOImpl();
            userDAO = new UserDAOImpl();
            mainModel = MainModel.getInstance();
        }
    }

    public List<Network> getNetworkList() {
        Log.i(TAG, "getNetworkList()");
        List<Network> items = networkDAO.getAll();

        // Mark current network
        for (Network it : items) {
            if (mainModel.currentNetwork != null &&
                    it.getId().longValue() == mainModel.currentNetwork.getId().longValue()) {
                it.selected = true;
                current = it;
            }
        }

        return items;
    }

    public Network getNetwork(Long id) {
        return networkDAO.get(id);
    }

    public void getNetworkServerList(final AsyncResponse response) {
        if (MainModel.avoidServerCalls) return;
        Log.i(TAG, "getUserServerList()");

        UserService client = ServiceGenerator.createService(UserService.class, mainModel.accessToken);
        Call<JsonArray> call = client.getUserMobileNetworkList();
        call.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> result) {
                if (result.isSuccessful()) {
                    JsonArray jsonArray = result.body();
                    List<Network> items = new ArrayList<Network>();
                    for (JsonElement item : jsonArray) {
                        JsonObject circle = item.getAsJsonObject().get("circle").getAsJsonObject();
                        JsonObject userVincles = circle.get("userVincles").getAsJsonObject();
                        User it = User.fromJSON(userVincles);
                        Network network = new Network();
                        network.relationship = item.getAsJsonObject().get("relationship").getAsString();
                        network.setId(it.getId());
                        network.userVincles = it;
                        items.add(network);
                    }

                    // Sync Compare, saver or delete local 'User' list
                    saveOrUpdateNetworkList(items);

                    response.onSuccess(networkList);
                } else {
                    String errorCode = ErrorHandler.parseError(result).getCode();
                    response.onFailure(errorCode);
                }
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                Log.i(TAG, "getUserNetworkList() - error: " + t.getMessage());
                response.onFailure(t);
            }
        });
    }

    private void saveOrUpdateNetworkList(List<Network> items) {
        if (items == null || items.size() == 0) return;
        Iterator<Network> i = items.iterator();
        while (i.hasNext()) {
            Network it = i.next();
            boolean hasNetwork = false;
            for (Network item : items) {
                if (it.getId().longValue() == item.getId().longValue()) {
                    hasNetwork = true;
                    // CAUTION: Save image reference!!!
                    String imageName = it.userVincles.imageName;

                    it.userVincles = item.userVincles;

                    // CAUTION: Restore image reference!!!
                    it.userVincles.imageName = imageName;
                }
                saveNetwork(item);
            }

            // Synchronize deletion list
            if (!hasNetwork) {
                // Network server deleted. Delete local network
                userDAO.delete(it.userVincles);
                networkDAO.delete(it);
                i.remove();
            }
        }
    }

    public void saveNetwork(Network item) {
        // Save first UserVincles (must be persisted separated by the ORM)
        userDAO.save(item.userVincles);
        // Last save Network
        networkDAO.save(item);
    }

    public void changeNetwork(Network item) {
        if (item == null) return;
        if (current == null || current.getId().longValue() != item.getId().longValue()) {
            item.selected = true;
            if (current != null) {
                current.selected = false;
            }

            // CAUTION: exchange object last of all!!!
            current = item;
        }
        mainModel.savePreferences(VinclesMobileConstants.NETWORK_CODE, item.getId(), VinclesConstants.PREFERENCES_TYPE_LONG);
        mainModel.currentNetwork = item;
    }

}