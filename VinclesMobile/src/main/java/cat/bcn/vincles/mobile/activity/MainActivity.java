/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.activity;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;

import java.util.List;

import cat.bcn.vincles.lib.VinclesApp;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.util.FontCache;
import cat.bcn.vincles.lib.util.ValidationResponse;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.widget.CustomTypefaceSpan;
import cat.bcn.vincles.mobile.BuildConfig;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.activity.about.AboutActivity;
import cat.bcn.vincles.mobile.activity.config.ConfigurationActivity;
import cat.bcn.vincles.mobile.activity.diary.DiaryActivity;
import cat.bcn.vincles.mobile.activity.home.HomeActivity;
import cat.bcn.vincles.mobile.activity.login.LoginActivity;
import cat.bcn.vincles.mobile.activity.message.MessageListActivity;
import cat.bcn.vincles.mobile.activity.network.NetworkActivity;
import cat.bcn.vincles.mobile.activity.notes.NotesActivity;
import cat.bcn.vincles.mobile.activity.videocall.VideoCallIntroActivity;
import cat.bcn.vincles.mobile.activity.videocall.VideoConferenceCallActivity;
import cat.bcn.vincles.mobile.model.MainModel;
import cat.bcn.vincles.mobile.model.NetworkModel;
import cat.bcn.vincles.mobile.util.VinclesMobileConstants;

public class MainActivity extends VinclesActivity
        implements NavigationView.OnNavigationItemSelectedListener, Validator.ValidationListener {
    public static final String IS_ROOT_ACTIVITY = "IS_ROOT_ACTIVITY";
    public static final String CHANGE_APP_NETWORK = "CHANGE_APP_NETWORK";

    public Validator validator;
//    private TextView texXarxa;
    private LinearLayout layout_xarxa;
    private View callUserActionView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // AUTOMATION FOR CHANGE NETWORK
        if (mainModel.currentNetwork != null) {
            Long netId = getIntent().getLongExtra(
                    CHANGE_APP_NETWORK, mainModel.currentNetwork.getId());
            if (getIntent().hasExtra(CHANGE_APP_NETWORK)
                    && netId != mainModel.currentNetwork.getId()) {

                NetworkModel.getInstance().changeNetwork(
                        NetworkModel.getInstance().getNetwork(netId)
                );
                refreshCallActionBar();
            }
        }

        // ALERT EVERY WEEK
        if (mainModel.lastRateGooglePlayCheck <
                (System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000)) {
            rateGooglePlayDialog();
        }

        validator = new Validator(this);
        mainModel.context = this;
    }

    @Override
    protected void onResume() {
        if (callUserActionView != null) {
            callUserActionView.setEnabled(true);
        }
        super.onResume();
        refreshCallActionBar();
    }

    private void applyFontToMenuItem(MenuItem mi) {
        Typeface font = FontCache.get(VinclesConstants.TYPEFACE.REGULAR, this);
        SpannableString mNewTitle = new SpannableString(mi.getTitle());
        mNewTitle.setSpan(new CustomTypefaceSpan("" , font), 0 , mNewTitle.length(),  Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        mi.setTitle(mNewTitle);
    }

    public void createEnvironment(int menuItemNumber) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        toggle.setDrawerIndicatorEnabled(false);
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.header_vincles, getTheme());
        toggle.setHomeAsUpIndicator(drawable);
        toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawer.isDrawerVisible(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    drawer.openDrawer(GravityCompat.START);
                    fillNavigationDrawer();
                }
            }
        });
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
            if (menuItemNumber >= 0)
                navigationView.getMenu().getItem(menuItemNumber).setChecked(true);
        }
    }

    protected void fillNavigationDrawer() {
        final ImageView imgUser = (ImageView) findViewById(R.id.imgUser);
        TextView texUser = (TextView) findViewById(R.id.texUser);
        if (mainModel.currentNetwork != null
                && mainModel.currentNetwork.userVincles != null) {
            if (imgUser != null && !this.isFinishing()
                    && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && !this.isDestroyed()
                    || Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1)){
                if (!isFinishing())
                    Glide.with(this)
                        .load(mainModel.getUserPhotoUrlFromUserWithAction(mainModel.currentNetwork.userVincles,
                                new AsyncResponse() {
                                    @Override
                                    public void onSuccess(Object result) {
                                        if (!isFinishing())
                                            Glide.with(MainActivity.this)
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
            }
            if (texUser != null)
                texUser.setText(mainModel.currentNetwork.userVincles.alias);
        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu men = navigationView.getMenu();
        MenuItem navVersion = men.findItem(R.id.nav_version);
        MenuItem navVersionName = men.findItem(R.id.nav_versionname);
        MenuItem navFlavour = men.findItem(R.id.nav_flavour);
        navVersion.setTitle(BuildConfig.VERSION_NAME);

        if (BuildConfig.DEBUG) {
            navVersionName.setTitle(String.valueOf(BuildConfig.VERSION_CODE));
            navFlavour.setTitle(BuildConfig.FLAVOR + "-" + VinclesApp.getVinclesApp().getAppFlavour());
        } else {
            navVersionName.setVisible(false);
            navFlavour.setVisible(false);
            navVersion.setVisible(false);
        }

        layout_xarxa = (LinearLayout) findViewById(R.id.layout_xarxa);
        if (layout_xarxa != null)
            layout_xarxa.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finishAffinity(); // This will clear activity stack and finish with the infinite back bug
                    Intent intent = new Intent(MainActivity.this, NetworkActivity.class);
                    intent.putExtra(MainActivity.IS_ROOT_ACTIVITY, "1");
                    startActivity(intent);

                    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                    drawer.closeDrawer(GravityCompat.START);
                }

            });


        // change font
        for (int i=0;i<men.size();i++) {
            MenuItem mi = men.getItem(i);

            //for aapplying a font to subMenu ...
            SubMenu subMenu = mi.getSubMenu();
            if (subMenu != null && subMenu.size() > 0) {
                for (int j=0; j <subMenu.size();j++) {
                    MenuItem subMenuItem = subMenu.getItem(j);
                    applyFontToMenuItem(subMenuItem);
                }
            }

            //the method we have create in activity
            applyFontToMenuItem(mi);
        }
    }

    protected void fillMenu() {
        ImageView actionUser = (ImageView)callUserActionView.findViewById(R.id.callUserActionImg);
        if (mainModel.currentNetwork != null
                && mainModel.currentNetwork.userVincles != null) {

            if (!this.isFinishing()
                    && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && !this.isDestroyed()
                    || Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1))

                if (!isFinishing())
                    Glide.with(this)
                        .load(mainModel.getUserPhotoUrlFromUser(mainModel.currentNetwork.userVincles))
                        .error(R.drawable.user).placeholder(R.color.superlightgray)
                        .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                        .into(actionUser);
        }
    }

    @Override
    public void onValidationSucceeded() {
        mainModel.showSimpleError(findViewById(R.id.main_content), "Yay! we got it right!", Snackbar.LENGTH_LONG);
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(this);

            // Display error messages ;)
            if (view instanceof EditText) {
                ((EditText) view).setError(message);
            } else {
                mainModel.showSimpleError(findViewById(R.id.main_content), message, Snackbar.LENGTH_LONG);
            }
        }
    }

    public void validate(final ValidationResponse response) {
        validator.setValidationListener(new Validator.ValidationListener() {
            @Override
            public void onValidationSucceeded() {
                response.onSuccess();
            }

            @Override
            public void onValidationFailed(List<ValidationError> errors) {
                response.onFailure(errors);
            }
        });
        validator.validate();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (this instanceof HomeActivity) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.app_close_dialog_title)
                        .setMessage(R.string.app_close_dialog_msg)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                finishAffinity();
                                finish();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // DO NOTHING
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            } else {
                if (getIntent().hasExtra(MainActivity.IS_ROOT_ACTIVITY)) {
                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                }
                else super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        MenuItem item = menu.findItem(R.id.action_call);
        callUserActionView = View.inflate(this, R.layout.item_toolbar_call, null);
        MenuItemCompat.setActionView(item, callUserActionView);
        callUserActionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                callUserActionView.setEnabled(false);
                VideoConferenceCallActivity.startActivityForOutgoingCall(MainActivity.this, mainModel);
            }
        });

        refreshCallActionBar();
        fillNavigationDrawer();
        return true;
    }

    protected void refreshCallActionBar() {
        if (callUserActionView != null) {
            final ImageView actionUser = (ImageView) callUserActionView.findViewById(R.id.callUserActionImg);
            if (mainModel.currentNetwork != null
                    && mainModel.currentNetwork.userVincles != null) {

                if (!this.isFinishing()
                        && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && !this.isDestroyed()
                        || Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1))

                    Glide.with(this)
                            .load(mainModel.getUserPhotoUrlFromUserWithAction(mainModel.currentNetwork.userVincles,
                                    new AsyncResponse() {
                                        @Override
                                        public void onSuccess(Object result) {
                                            Glide.with(MainActivity.this)
                                                    .load(mainModel.getUserPhotoUrlFromUser(mainModel.currentNetwork.userVincles))
                                                    .error(R.drawable.user).placeholder(R.color.superlightgray)
                                                    .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                                                    .into(actionUser);
                                        }

                                        @Override
                                        public void onFailure(Object error) { }
                                    }))
                            .error(R.drawable.user).placeholder(R.color.superlightgray)
                            .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                            .into(actionUser);
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent intent = new Intent(this, HomeActivity.class);

        if (id == R.id.nav_home) {
            intent = new Intent(this, HomeActivity.class);
        } else if (id == R.id.nav_videocall) {
            intent = new Intent(this, VideoCallIntroActivity.class);
        } else if (id == R.id.nav_messages) {
            intent = new Intent(this, MessageListActivity.class);
        } else if (id == R.id.nav_diary) {
            intent = new Intent(this, DiaryActivity.class);
        } else if (id == R.id.nav_notes) {
            intent = new Intent(this, NotesActivity.class);
        } else if (id == R.id.nav_configuration) {
            intent = new Intent(this, ConfigurationActivity.class);
        } else if (id == R.id.nav_about) {
            intent = new Intent(this, AboutActivity.class);
        } else if (id == R.id.nav_logout) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.signout_title)
                    .setMessage(R.string.signout_info)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            mainModel.wipeout(MainActivity.this);
                            finishAffinity();
                            Intent i = new Intent(MainActivity.this, LoginActivity.class);
                            startActivity(i);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setCancelable(false)
                    .show();
            return true;
        } else if (id == R.id.nav_version || id == R.id.nav_versionname || id == R.id.nav_flavour) {
            mainModel.view = MainModel.REGISTER_DISCLAIMER;
            intent = getIntent();
        }

        finishAffinity(); // This will clear activity stack and finish with the infinite back bug
        intent.putExtra(MainActivity.IS_ROOT_ACTIVITY, 1);
        startActivity(intent);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public static final int REQUEST_CAMERA_RESULT = 100;
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode){
            case REQUEST_CAMERA_RESULT:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    mainModel.showSimpleError(findViewById(R.id.main_content), getString(R.string.permission_camera), Snackbar.LENGTH_LONG);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    public void rateGooglePlayDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.rate_google_play_title)
                .setMessage(R.string.rate_google_play_content)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        rateApp();
                        // SET INFINITE DATE TO DO NOT  TO TRUE AGAIN
                        mainModel.lastRateGooglePlayCheck = Long.MAX_VALUE;
                        mainModel.savePreferences(VinclesMobileConstants.APP_LASTRATEGOOGLEPLAYCHECK, mainModel.lastRateGooglePlayCheck, VinclesConstants.PREFERENCES_TYPE_LONG);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mainModel.lastRateGooglePlayCheck = System.currentTimeMillis();
                        mainModel.savePreferences(VinclesMobileConstants.APP_LASTRATEGOOGLEPLAYCHECK, mainModel.lastRateGooglePlayCheck, VinclesConstants.PREFERENCES_TYPE_LONG);
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();

        mainModel.lastRateGooglePlayCheck = System.currentTimeMillis();
        mainModel.savePreferences(VinclesMobileConstants.APP_LASTRATEGOOGLEPLAYCHECK, mainModel.lastRateGooglePlayCheck, VinclesConstants.PREFERENCES_TYPE_LONG);
    }

    public void rateApp()
    {
        try
        {
            Intent rateIntent = rateIntentForUrl("market://details");
            startActivity(rateIntent);
        }
        catch (ActivityNotFoundException e)
        {
            Intent rateIntent = rateIntentForUrl("http://play.google.com/store/apps/details");
            startActivity(rateIntent);
        }
    }
    private Intent rateIntentForUrl(String url)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("%s?id=%s", url, getPackageName())));
        int flags = Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
        if (Build.VERSION.SDK_INT >= 21)
        {
            flags |= Intent.FLAG_ACTIVITY_NEW_DOCUMENT;
        }
        else
        {
            //noinspection deprecation
            flags |= Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET;
        }
        intent.addFlags(flags);
        return intent;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // CAUTION: restore current language (camera override it with device language default)
        mainModel.updateLocale(mainModel.language, mainModel.country);
    }
}
