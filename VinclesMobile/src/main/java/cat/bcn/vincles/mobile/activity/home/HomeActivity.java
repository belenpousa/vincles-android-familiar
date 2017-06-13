/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.activity.home;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import cat.bcn.vincles.lib.push.CommonVinclesGcmHelper;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.util.Security;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.vo.FeedItem;
import cat.bcn.vincles.lib.vo.Message;
import cat.bcn.vincles.lib.vo.Network;
import cat.bcn.vincles.lib.vo.PushMessage;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.activity.MainActivity;
import cat.bcn.vincles.mobile.activity.home.adapter.FeedRVAdapter;
import cat.bcn.vincles.mobile.activity.network.NetworkActivity;
import cat.bcn.vincles.mobile.component.swipetoremove.SwipeToRemoveTouchCallback;
import cat.bcn.vincles.mobile.model.FeedModel;
import cat.bcn.vincles.mobile.model.MainModel;
import cat.bcn.vincles.mobile.model.MessageModel;
import cat.bcn.vincles.mobile.model.NetworkModel;
import cat.bcn.vincles.mobile.model.TaskModel;
import cat.bcn.vincles.mobile.push.AppFCMDefaultListenerImpl;
import cat.bcn.vincles.mobile.util.VinclesMobileConstants;

public class HomeActivity extends MainActivity {
    private static final String TAG = "HomeActivity";
    private ProgressDialog progressBar;
    private RecyclerView lisFeed;
    private List<FeedItem> items;
    private FeedRVAdapter adapter;
    private FeedModel feedModel;

    private int step = 0;
    private boolean oldAvoidServerCalls;
    private RelativeLayout relTour;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setTitle(getString(R.string.title_activity_home));

        super.createEnvironment(0);

        relTour = (RelativeLayout) findViewById(R.id.relTour);
        lisFeed = (RecyclerView) findViewById(R.id.lisFeed);
        initializeTour();

        SwipeToRemoveTouchCallback simpleItemTouchCallback = new SwipeToRemoveTouchCallback(lisFeed, this);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(lisFeed);

        feedModel = FeedModel.getInstance();
        lisFeed.setLayoutManager(new LinearLayoutManager(this));

        // CHECK NOTIFICATIONS ONLY IN HOME ACTIVITY
        mainModel.checkNewNotifications();

        oldAvoidServerCalls = MainModel.avoidServerCalls;
    }

    @Override
    protected void onResume() {
        super.onResume();

        isNoNetworkUsers();

        // REACTIVE LIST ON NEW MESSAGE
        CommonVinclesGcmHelper.setPushListener(new AppFCMDefaultListenerImpl(this) {
            @Override
            public void onPushMessageReceived(PushMessage pushMessage) {
                super.onPushMessageReceived(pushMessage);
                try {
                    // REFRESH ON EVERY PUSH (DB CHANGE)
                    // NO DISCRIMINATION HERE ATM
                    refreshList();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onPushMessageError(long idPush, Throwable t) {
                super.onPushMessageError(idPush, t);
            }
        });

        refreshList();
    }

    @Override
    public void onPause() {
        super.onPause();
        CommonVinclesGcmHelper.setPushListener(MainModel.getInstance().getPushListener());
    }

    public void refreshList() {
        if (isNoNetworkUsers()) return;

        items = feedModel.getList(true);
        adapter = new FeedRVAdapter(this, items);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                lisFeed.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private boolean isNoNetworkUsers() {
        // CHECK NO USERS
        View layoutNoUsers = findViewById(R.id.layoutNoUsers);
        if (mainModel.currentUser != null && mainModel.currentNetwork != null) {
            layoutNoUsers.setVisibility(View.GONE);
            return false;
        } else {
            layoutNoUsers.setVisibility(View.VISIBLE);
            return true;
        }
    }

    public void initializeTour() {
        if (!mainModel.tour) {
            relTour.setVisibility(View.VISIBLE);
            finishConfiguration();
        } else {
            relTour.setVisibility(View.GONE);
        }
    }

    public void closeTour(View view) {
        mainModel.tour = true;
        mainModel.savePreferences(VinclesMobileConstants.TOUR, mainModel.tour, VinclesConstants.PREFERENCES_TYPE_BOOLEAN);
        // Remove tour-layout becauseof problems with clickable events
        ViewGroup vg = (ViewGroup) relTour.getParent();
        vg.removeView(relTour);
    }

    private void finishConfiguration() {
        // FIST LAUNCH
        progressBar = new ProgressDialog(this/*,R.style.DialogCustomTheme*/);
        progressBar.setMessage(getString(R.string.first_launch_configuration));
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setInverseBackgroundForced(true);
        progressBar.show();


        // SLEEP TIME ENOUGH TO SEE MESSAGE AND DO NOT DISTURB
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startAutoConfigurationStep();
            }
        }, 1000);
    }

    private void startAutoConfigurationStep() {
        step++;
        progressBar.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                MainModel.avoidServerCalls = oldAvoidServerCalls;
            }
        });
        MainModel.avoidServerCalls = false;
        Log.i(TAG, "Auto Configuration Step " + (step));
        AsyncResponse response = new AsyncResponse() {
            @Override
            public void onSuccess(Object result) {
                Log.i(TAG, "Auto Configuration Step " + (step) + " OK!");
                startAutoConfigurationStep();
            }

            @Override
            public void onFailure(Object error) {
                Log.e(TAG, "getUserServerList() - error: " + error);
                progressBar.dismiss();
                startAutoConfigurationStep();
            }
        };

        Calendar calFrom = VinclesConstants.getCalendarWithoutTime(Calendar.getInstance());
        String dateFrom = String.valueOf(calFrom.getTime().getTime());

        switch (step) {
            case 1:
                final AsyncResponse networkResponse = response;
                progressBar.setMessage(getString(R.string.first_launch_configuration_step_1));
                NetworkModel.getInstance().getNetworkServerList(new AsyncResponse() {
                    @Override
                    public void onSuccess(Object result) {
                        NetworkModel networkModel = NetworkModel.getInstance();
                        networkModel.networkList = networkModel.getNetworkList();
                        if (networkModel.networkList.size() > 0) {
                            networkModel.changeNetwork(networkModel.networkList.get(0));
                            for (Network network: networkModel.networkList) {
                                FeedModel.getInstance().addItem(new FeedItem()
                                        .setType(FeedItem.FEED_TYPE_USER_LINKED)
                                        .setIdData(network.userVincles.getId())
                                        .setInfo(network.userVincles.alias)
                                        .setExtraId(network.userVincles.getId()));
                            }

                            fillNavigationDrawer();
                            refreshCallActionBar();
                            refreshList();
                        }
                        networkResponse.onSuccess(result);
                    }

                    @Override
                    public void onFailure(Object error) {
                        networkResponse.onFailure(error);
                    }
                });
                break;
            case 2:
                progressBar.setMessage(getString(R.string.first_launch_configuration_step_2));
                if (mainModel.currentNetwork != null) {
                    NetworkModel networkModel = NetworkModel.getInstance();
                    AsyncResponse reponseNull = new AsyncResponse() {
                        @Override public void onSuccess(Object result) {}
                        @Override public void onFailure(Object error) {}
                    };

                    for (int i = 0; i < networkModel.networkList.size(); i++) {
                        // ONLY THE LAST CALL RESPONSE WILL CONTINUE THE PROCESS
                        if (i == networkModel.networkList.size()-1)
                            reponseNull = response;

                        TaskModel.getInstance().getAllTaskServer(reponseNull, networkModel.networkList.get(i), 0l);
                    }
                }
                else startAutoConfigurationStep();
                break;
            case 3:
                progressBar.setMessage(getString(R.string.first_launch_configuration_step_3));
                if (mainModel.currentNetwork != null && mainModel.currentNetwork.userVincles != null) {
                    MessageModel.getInstance().getAllMessagesServer(response, 0l);
                }
                else startAutoConfigurationStep();
                break;
            default:
                if (progressBar != null) {
                    progressBar.dismiss();
                }
                return;
        }
    }

    public void addNewUser(View view) {
        Intent intent = new Intent(HomeActivity.this, NetworkActivity.class);
        startActivity(intent);
    }
}