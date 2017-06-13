/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.activity.network;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.vo.Network;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.adapter.NetworkAdapter;

public class NetworkActivity extends NetworkTemplateActivity {
    private static final String TAG = "NetworkActivity";
    private ListView lisNetwork;
    private NetworkAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.title_activity_network));

        View linNetworkList = findViewById(R.id.linNetworkList);
        View linNetworkJoin = findViewById(R.id.linNetworkJoin);
        View linNetworkSuccess = findViewById(R.id.linJoinSuccess);

        linNetworkList.setVisibility(View.VISIBLE);
        linNetworkJoin.setVisibility(View.GONE);
        linNetworkSuccess.setVisibility(View.GONE);

        lisNetwork = (ListView) findViewById(R.id.lisNetwork);
        adapter = new NetworkAdapter(getApplicationContext(), 0, new ArrayList<Network>());
        // ListView Item Click Listener
        lisNetwork.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // ListView Clicked item index
                int itemPosition = position;
                // Set default network
                Network item = (Network) lisNetwork.getItemAtPosition(position);
                changeNetwork(item);
                adapter.notifyDataSetChanged();
            }
        });

        // Assign adapter to ListView
        lisNetwork.setAdapter(adapter);

        super.createEnvironment(-1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Use first local data
        adapter.clear();
        networkModel.networkList = networkModel.getNetworkList();
        addToNetworkList(networkModel.networkList);
        adapter.addAll(networkModel.networkList);

        // Get from server
        networkModel.getNetworkServerList(new AsyncResponse() {
            @Override
            public void onSuccess(Object result) {
                Log.i(TAG, "getUserServerList() - result");
                networkModel.networkList = networkModel.getNetworkList();
                adapter.clear();
                adapter.addAll(networkModel.networkList);

                // Update menu photo
                if (mainModel.currentNetwork != null
                        && mainModel.currentNetwork.userVincles != null) {
                    if (!NetworkActivity.this.isFinishing()) {
                        // Update user vincles photo
                        final ImageView imgUser = (ImageView) findViewById(R.id.imgUser);
                        Glide.with(NetworkActivity.this)
                                .load(mainModel.getUserPhotoUrlFromUserWithAction(mainModel.currentNetwork.userVincles,
                                        new AsyncResponse() {
                                            @Override
                                            public void onSuccess(Object result) {
                                                Glide.with(NetworkActivity.this)
                                                        .load(mainModel.getUserPhotoUrlFromUser(mainModel.currentNetwork.userVincles))
                                                        .error(R.drawable.user).placeholder(R.color.superlightgray)
                                                        .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                                                        .into(imgUser);
                                            }

                                            @Override
                                            public void onFailure(Object error) { }
                                        }))
                                .error(R.drawable.user).placeholder(R.color.superlightgray)
                                .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                                .into(imgUser);

                        // Update NavigationDrawer photo
                        ImageView drawerUser = (ImageView) findViewById(R.id.imgUser);
                        Glide.with(NetworkActivity.this)
                                .load(mainModel.getUserPhotoUrlFromUser(mainModel.currentNetwork.userVincles))
                                .error(R.drawable.user).placeholder(R.color.superlightgray)
                                .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                                .into(drawerUser);

                        // Update action photo
                        ImageView actionUser = (ImageView) findViewById(R.id.callUserActionImg);
                        Glide.with(NetworkActivity.this)
                                .load(mainModel.getUserPhotoUrlFromUser(mainModel.currentNetwork.userVincles))
                                .error(R.drawable.user).placeholder(R.color.superlightgray)
                                .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                                .into(actionUser);
                    }
                }
            }

            @Override
            public void onFailure(Object error) {
                Log.e(TAG, "getUserServerList() - error: " + error);
                String errorMessage = mainModel.getErrorByCode(error);
                mainModel.showSimpleError(findViewById(R.id.main_content), errorMessage, Snackbar.LENGTH_LONG);
            }
        });
    }
}
