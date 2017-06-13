/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.activity.network;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import com.google.gson.JsonObject;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import java.net.SocketTimeoutException;
import java.util.List;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.util.VinclesError;
import cat.bcn.vincles.lib.vo.FeedItem;
import cat.bcn.vincles.lib.vo.Network;
import cat.bcn.vincles.lib.vo.User;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.model.FeedModel;
import cat.bcn.vincles.mobile.model.NetworkModel;

public class NetworkJoinActivity extends NetworkTemplateActivity {
    private static final String TAG = "NetworkActivity";
    @NotEmpty(messageResId = R.string.error_empty_field)
    private EditText ediCode;
    private Spinner spiRelationship;
    private View spiRelationshipBg;
    private TextView texMessage;
    private Network network;
    private Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.title_activity_network));

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        View linNetworkList = findViewById(R.id.linNetworkList);
        View linNetworkJoin = findViewById(R.id.linNetworkJoin);
        View linNetworkSuccess = findViewById(R.id.linJoinSuccess);

        linNetworkList.setVisibility(View.GONE);
        linNetworkSuccess.setVisibility(View.GONE);
        linNetworkJoin.setVisibility(View.VISIBLE);
        ediCode = (EditText) findViewById(R.id.ediCode);

        texMessage = (TextView) findViewById(R.id.texMessage);
        texMessage.setText(getString(R.string.join_message));

        spiRelationship = (Spinner) findViewById(R.id.spiRelationship);
        spiRelationshipBg = findViewById(R.id.spiRelationshipBg);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.join_relationship_pos, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spiRelationship.setAdapter(adapter);

        List<Network> netList = networkModel.getNetworkList();
        if (!TextUtils.isEmpty(mainModel.currentUser.username) && (netList == null || netList.size() == 0))
            texMessage.setText(getString(R.string.join_message_last));
    }

    public void joinNetwork(View view) {
        boolean ok = true;
        if (ediCode.getText().length() <= 0) {
            ediCode.setError(getString(R.string.error_codi_xarxa),null);
            ok = false;
        }
        if (spiRelationship.getSelectedItemPosition() == 0) {
            spiRelationshipBg.setBackground(getResources().getDrawable(R.drawable.edittext_background_red));
            ok = false;
        }

        if (ok) {
            onJoinNetwork();
        } else {
            if (snackbar != null) snackbar.dismiss();
            snackbar = Snackbar.make(findViewById(R.id.main_content), getString(R.string.error_codi_xarxa), Snackbar.LENGTH_LONG);
            snackbar.setAction(R.string.close, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    snackbar.dismiss();
                }
            });
            snackbar.show();
        }
    }

    private void onJoinNetwork() {
        Log.i(TAG, "onJoinVincles()");
        JsonObject association = new JsonObject();
        association.addProperty("registerCode", ediCode.getText().toString());
        association.addProperty("relationship", VinclesConstants.USER_TYPES[spiRelationship.getSelectedItemPosition()-1]);

        snackbar = Snackbar.make(findViewById(R.id.main_content), getString(R.string.registration_process), Snackbar.LENGTH_INDEFINITE);
        ((ImageView)snackbar.getView().findViewById(R.id.snackbar_icon)).setImageResource(R.drawable.icon_network_white);
        snackbar.getView().findViewById(R.id.snackbar_icon).setVisibility(View.VISIBLE);
        snackbar.show();
        snackbar.getView().getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                snackbar.getView().getViewTreeObserver().removeOnPreDrawListener(this);
                ((CoordinatorLayout.LayoutParams) snackbar.getView().getLayoutParams()).setBehavior(null);
                return true;
            }
        });

        mainModel.associateRegistered(new AsyncResponse() {
            @Override
            public void onSuccess(Object result) {
                JsonObject json = (JsonObject) result;

                // Update/Create User vincles
                JsonObject userVinclesJSON = json.getAsJsonObject("userVincles");
                User userVincles = User.fromJSON(userVinclesJSON);

                // Create local Network
                Long networkCode = userVincles.getId();//userCode; //userVinclesJSON.get("idCircle").getAsLong();
                network = networkModel.getNetwork(networkCode);
                if (network == null) {
                    network = new Network();
                    network.relationship = spiRelationship.getSelectedItem().toString();
                }
                network.setId(networkCode);
                network.userVincles = userVincles;
                networkModel.saveNetwork(network);

                networkModel.view = NetworkModel.NETWORK_SUCCESS;
                // Update current network in APP
                networkModel.changeNetwork(network);
                // Update menu information
                ImageView imgUser = (ImageView) findViewById(R.id.imgUser);
                TextView texUser = (TextView) findViewById(R.id.texUser);

                // Get 'userVincles' photo
                if (network.userVincles == null || network.userVincles.imageName == null || network.userVincles.imageName.equals("")) {
                    mainModel.getUserPhoto(new AsyncResponse() {
                        @Override
                        public void onSuccess(Object result) {
                            // Update 'UserVincles' with photo
                            String imageName = (String)result;
                            network.userVincles.imageName = imageName;
                            mainModel.saveUser(network.userVincles);

                            // Continue activity flow
                            startActivity(new Intent(NetworkJoinActivity.this, NetworkSuccessActivity.class));
                            FeedModel.getInstance().addItem(new FeedItem()
                                    .setType(FeedItem.FEED_TYPE_USER_LINKED)
                                    .setIdData(network.userVincles.getId())
                                    .setInfo(network.userVincles.alias)
                                    .setExtraId(network.userVincles.getId()));
                            finish();
                        }

                        @Override
                        public void onFailure(Object error) {
                            Log.e(TAG, "getUserPhoto() - error: " + error);
                            String errorMessage = mainModel.getErrorByCode(error);
                            if (snackbar != null) snackbar.dismiss();
                            mainModel.showSimpleError(findViewById(R.id.main_content), errorMessage, Snackbar.LENGTH_LONG);
                        }
                    }, network.userVincles);
                } else {
                    // Continue activity flow
                    startActivity(new Intent(NetworkJoinActivity.this, NetworkSuccessActivity.class));
                    finish();
                }
            }

            @Override
            public void onFailure(Object error) {
                Log.i(TAG, "error: " + error);
                String errorMessage;
                mainModel.currentUser.registerCode = null;
                if (snackbar != null) snackbar.dismiss();
                if (error instanceof SocketTimeoutException) {
                    errorMessage = mainModel.getErrorByCode(VinclesError.ERROR_CONNECTION);
                    mainModel.showSimpleError(findViewById(R.id.main_content), errorMessage, Snackbar.LENGTH_LONG);
                } else if (error instanceof java.net.ConnectException) {
                    errorMessage = mainModel.getErrorByCode(VinclesError.ERROR_CONNECTION);
                    mainModel.showSimpleError(findViewById(R.id.main_content), errorMessage, Snackbar.LENGTH_LONG);
                } else {
                    if (error != null && error instanceof String && ((String)error).equals(VinclesError.ERROR_CODE)) {
                        texMessage.setText(getString(R.string.join_message_error));
                        ediCode.setText("");
                        mainModel.showSimpleError(findViewById(R.id.main_content), getString(R.string.join_message_error), Snackbar.LENGTH_LONG);
                    } else {
                        errorMessage = mainModel.getErrorByCode(error);
                        mainModel.showSimpleError(findViewById(R.id.main_content), errorMessage, Snackbar.LENGTH_LONG);
                    }
                }
            }
        }, association);
    }
}
