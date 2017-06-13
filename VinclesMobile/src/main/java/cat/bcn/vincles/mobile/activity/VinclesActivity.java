/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.activity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.model.MainModel;
import cat.bcn.vincles.mobile.push.AppFCMDefaultListenerImpl;

public class VinclesActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();
    protected MainModel mainModel;
    public static VinclesActivity instance;
    private Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mainModel = MainModel.getInstance();
        if (mainModel != null
            || mainModel.currentUser == null) //Current user should be at least a blank new User();
            mainModel.initialize(this);

        if (mainModel.currentUser != null && mainModel.currentUser.active == true)
            mainModel.startGCM(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        instance = this;

        if (mainModel.getPushListener() != null && mainModel.getPushListener() instanceof AppFCMDefaultListenerImpl) {
            ((AppFCMDefaultListenerImpl) mainModel.getPushListener()).setActualActivity(this);
        }
    }

    public void checkStrengthSignalStatus() {
        Log.i(TAG, "checkStrengthSignalStatus(): " + mainModel.isLowConnection);
        if (mainModel.isLowConnection) {
            snackbar = Snackbar.make(findViewById(R.id.main_content), getString(R.string.message_low_connectivity), Snackbar.LENGTH_INDEFINITE);
            ((ImageView)snackbar.getView().findViewById(R.id.snackbar_icon)).setImageResource(R.drawable.icon_calendar_white);
            snackbar.getView().findViewById(R.id.snackbar_icon).setVisibility(View.GONE);
            snackbar.show();
        }
        else {
            removeStrengthSignalStatus();
        }
    }

    public void removeStrengthSignalStatus() {
        if (snackbar != null) {
            snackbar.dismiss();
        }
    }
}
