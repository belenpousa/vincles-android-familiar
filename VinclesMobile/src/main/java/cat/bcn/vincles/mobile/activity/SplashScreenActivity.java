/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.tempos21.versioncontrol.service.AlertMessageService;

import java.util.Timer;
import java.util.TimerTask;

import cat.bcn.vincles.lib.business.ServiceGenerator;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.activity.home.HomeActivity;
import cat.bcn.vincles.mobile.activity.login.DisclaimerActivity;
import cat.bcn.vincles.mobile.activity.login.LoginActivity;
import cat.bcn.vincles.mobile.activity.login.MigrateUserActivity;
import cat.bcn.vincles.mobile.activity.login.MigrateValidateUserActivity;
import cat.bcn.vincles.mobile.activity.login.ValidateUserActivity;
import cat.bcn.vincles.mobile.monitors.SignalStrengthMonitor;
import cat.bcn.vincles.mobile.util.VinclesMobileConstants;

public class SplashScreenActivity extends VinclesActivity {
    private final String TAG = this.getClass().getSimpleName();
    private static final long SPLASH_SCREEN_DELAY = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        initSignalStrengthMonitor();
        String lang = mainModel.language;
        if (lang.equalsIgnoreCase("ca")) lang = "cat";

        // Control version
        AlertMessageService.showMessageDialog(this,
                ServiceGenerator.getModulesVersionUrl(),
                lang,
                new AlertMessageService.AlertDialogListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.i(TAG, "onFailure()");
                        start();
                    }

                    @Override
                    public void onSuccess(boolean b) {
                        Log.i(TAG, "onSuccess()");
                        if (!b)
                            start();
                    }

                    @Override
                    public void onAlertDialogDismissed() {
                        Log.i(TAG, "onAlertDialogDismissed()");
                        start();
                    }
                });
    }

    private void start() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {

                // Start the next activity
                Intent mainIntent = new Intent().setClass(
                        SplashScreenActivity.this, HomeActivity.class);
                if (mainModel.currentUser == null || !mainModel.currentUser.active) {
                    boolean disclaimerAccepted =
                            mainModel.preferences.getBoolean(VinclesMobileConstants.APP_DISCLAIMER_ACCEPTED, false);
                    if (!disclaimerAccepted)
                        mainIntent = new Intent(SplashScreenActivity.this, DisclaimerActivity.class);

                    // VALIDATE A MIGRATION USER IS NOT THE SAME CALL AS JUST VALIDATE USER
                    else if (mainModel.currentUser != null && !mainModel.currentUser.active && mainModel.currentUser.getId() != null) {
                        if (mainModel.preferences.contains(VinclesMobileConstants.MIGRATION_USER_ID))
                            mainIntent = new Intent(SplashScreenActivity.this, MigrateValidateUserActivity.class);
                        else
                            mainIntent = new Intent(SplashScreenActivity.this, ValidateUserActivity.class);
                    } else
                        mainIntent = new Intent(SplashScreenActivity.this, LoginActivity.class);

                }

                // OLD USERS NEED TO MIGRATE TO NEW USER/PASSWORD LOGIN SYSTEM
                else if (mainModel.currentUser != null && mainModel.currentUser.active
                        && mainModel.currentUser.email.length() == 0) {
                    if (mainModel.preferences.contains(VinclesMobileConstants.MIGRATION_USER_ID))
                        mainIntent = new Intent(SplashScreenActivity.this, MigrateValidateUserActivity.class);
                    else
                        mainIntent = new Intent(SplashScreenActivity.this, MigrateUserActivity.class);
                }

                startActivity(mainIntent);
                finish();
            }
        };

        // Simulate a long loading process on application startup.
        Timer timer = new Timer();
        timer.schedule(task, SPLASH_SCREEN_DELAY);
    }


    private void initSignalStrengthMonitor() {
        mainModel.phoneListener = new SignalStrengthMonitor();
        mainModel.telephonyManager.listen(mainModel.phoneListener, SignalStrengthMonitor.LISTEN_SIGNAL_STRENGTHS);
    }
}
