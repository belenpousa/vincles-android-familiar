/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.activity.network;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.util.ImageUtils;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.model.NetworkModel;
import cat.bcn.vincles.mobile.model.TaskModel;

public class NetworkSuccessActivity extends NetworkTemplateActivity {
    private static final String TAG = "NetworkActivity";
    private int maxTextusername = 20;
    private ProgressDialog progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        View linNetworkList = findViewById(R.id.linNetworkList);
        View linNetworkJoin = findViewById(R.id.linNetworkJoin);
        View linNetworkSuccess = findViewById(R.id.linJoinSuccess);

        linNetworkList.setVisibility(View.GONE);
        linNetworkJoin.setVisibility(View.GONE);
        linNetworkSuccess.setVisibility(View.VISIBLE);

        ImageView imgUser = (ImageView) findViewById(R.id.imgJoinUser);
        Glide.with(this)
                .load(mainModel.getUserPhotoUrlFromUser(mainModel.currentNetwork.userVincles))
                .error(R.drawable.user).placeholder(R.color.superlightgray)
                .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                .into(imgUser);
        TextView texJoinUser = (TextView) findViewById(R.id.texJoinUser);

        String friendname = mainModel.currentNetwork.userVincles.alias;

        String name = mainModel.currentUser.toString();
        if (name.length() > maxTextusername) name = name.substring(0, maxTextusername) + "..";

        texJoinUser.setText(friendname);
        TextView texJoinSuccess = (TextView) findViewById(R.id.texJoinSuccess);
        texJoinSuccess.setText(getResources().getString(R.string.join_message_success_title, name, mainModel.currentNetwork.userVincles.alias));

        fillUserData();
    }

    private void fillUserData() {
        progressBar = new ProgressDialog(this/*,R.style.DialogCustomTheme*/);
        progressBar.setMessage(getString(R.string.first_launch_configuration));
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setInverseBackgroundForced(true);
        progressBar.show();

        progressBar.setMessage(getString(R.string.first_launch_configuration_step_2));
        final boolean oldAvoidServerCalls = mainModel.avoidServerCalls;
        mainModel.avoidServerCalls = false;
        if (mainModel.currentNetwork != null) {
            TaskModel.getInstance().getAllTaskServer(new AsyncResponse() {
                @Override
                public void onSuccess(Object result) {
                    mainModel.avoidServerCalls = oldAvoidServerCalls;
                    progressBar.dismiss();
                }

                @Override
                public void onFailure(Object error) {
                    mainModel.avoidServerCalls = oldAvoidServerCalls;
                    progressBar.dismiss();
                    mainModel.showSimpleError(findViewById(R.id.main_content), mainModel.getErrorByCode(error), Snackbar.LENGTH_LONG);
                }
            }, mainModel.currentNetwork, 0l);
        }
    }
}
