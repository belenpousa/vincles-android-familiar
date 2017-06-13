/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.activity.config;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import java.io.File;
import java.util.Date;
import java.util.List;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.vo.Task;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.activity.MainActivity;
import cat.bcn.vincles.mobile.model.AndroidCalendarModel;
import cat.bcn.vincles.mobile.model.MainModel;
import cat.bcn.vincles.mobile.model.TaskModel;
import cat.bcn.vincles.mobile.util.VinclesMobileConstants;
import de.hdodenhof.circleimageview.CircleImageView;

public class ConfigurationActivity extends MainActivity {
    private static final String TAG = "ConfigurationActivity";
    TextView ediFirstName;
    TextView ediLastName;
    TextView ediEmail;
    TextView ediPhone;
    TextView ediPassword;
    TextView ediPasswordRepeat;
    TextView ediOldPassword;
    Switch swiHome;

    private ViewGroup layConfiguration;
    private ViewGroup layPersonalData;
    private TextView texBack;
    private TextView texPersonalData;
    private CircleImageView imgUserConfiguration;

    private Switch swiLocale;
    private Switch swiNotification;
    private Switch swiDownloads;
    private Switch swiSynchronization;
    private LinearLayout ll_aceptar, ll_error;
    private ScrollView scroll;

    private String currentFilename;
    private String currentImagePath;

    private AndroidCalendarModel androidCalendarModel;
    private static int CONFIGURATION_DATA_FIELDS_NUM = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);
        setTitle(getString(R.string.title_activity_configuration));

        setViews();
        super.createEnvironment(5);
    }

    @Override
    protected void onResume() {
        super.onResume();
        androidCalendarModel = AndroidCalendarModel.getInstance();
        androidCalendarModel.selectDefaultCalendar(this);

        updateViews();
        addListeners();
    }

    private void setViews(){
        layConfiguration = (ViewGroup) findViewById(R.id.layConfiguration);
        layPersonalData = (ViewGroup) findViewById(R.id.layPersonalData);
        texBack = (TextView) findViewById(R.id.texBack);
        imgUserConfiguration = (CircleImageView) findViewById(R.id.imgUserConfiguration);
        ediFirstName = (TextView) findViewById(R.id.ediFirstName);
        ediLastName = (TextView) findViewById(R.id.ediLastName);
        ediEmail = (TextView) findViewById(R.id.ediEmail);
        ediPhone = (TextView) findViewById(R.id.ediPhone);
        ediPassword = (TextView) findViewById(R.id.ediPassword);
        ediOldPassword = (TextView) findViewById(R.id.ediOldPassword);
        ediPasswordRepeat = (TextView) findViewById(R.id.ediPasswordRepeat);
        swiHome = (Switch) findViewById(R.id.swiHome);

        swiLocale = (Switch) findViewById(R.id.swiLocale);
        swiNotification = (Switch) findViewById(R.id.swiNotification);
        swiDownloads = (Switch) findViewById(R.id.swiDownloads);
        swiSynchronization = (Switch) findViewById(R.id.swiSynchronization);
        ll_aceptar = (LinearLayout) findViewById(R.id.ll_aceptar);
        ll_error   = (LinearLayout) findViewById(R.id.ll_error);
        scroll = (ScrollView)findViewById(R.id.scroll);

        if (mainModel.currentUser != null) {
            ediFirstName.setText(mainModel.currentUser.name);
            ediLastName.setText(mainModel.currentUser.lastname);
            ediPhone.setText("" + mainModel.currentUser.phone);
            ediEmail.setText(mainModel.currentUser.email);
        }

    }

    private void updateViews() {
        if (mainModel.view.equals(MainModel.CONFIGURATION_DATA)) {
            layConfiguration.setVisibility(View.GONE);
            layPersonalData.setVisibility(View.VISIBLE);
        } else {
            layConfiguration.setVisibility(View.VISIBLE);
            layPersonalData.setVisibility(View.GONE);
        }

        texBack.setText(getResources().getString(R.string.personal_data));
        texPersonalData = (TextView) findViewById(R.id.texPersonalData);

        String personalData = mainModel.currentUser.name
                + "\n" + mainModel.currentUser.lastname
                + "\n" + mainModel.currentUser.email
                + "\n" + mainModel.currentUser.phone;
        if (mainModel.currentUser.liveInBarcelona) {
            personalData += "\n" + getResources().getString(R.string.configuration_live_bcn);
        } else {
            personalData += "\n" + getResources().getString(R.string.configuration_live_bcn_no);
        }
        texPersonalData.setText(personalData);

        if (!isFinishing())
            Glide.with(this)
                .load(mainModel.getUserPhotoUrlFromUser(mainModel.currentUser))
                .error(R.drawable.user).placeholder(R.color.superlightgray)
                .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                .into(imgUserConfiguration);

        ediFirstName.setText(mainModel.currentUser.name);
        ediLastName.setText(mainModel.currentUser.lastname);
        ediEmail.setText(mainModel.currentUser.email);
        ediPhone.setText("" + mainModel.currentUser.phone);

        swiHome.setChecked(mainModel.currentUser.liveInBarcelona);
        swiLocale.setChecked(!mainModel.language.equals("ca"));
        swiNotification.setChecked(mainModel.notifications);
        swiDownloads.setChecked(mainModel.downloads);
        swiSynchronization.setChecked(mainModel.synchronizations);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        getBaseContext().getResources().updateConfiguration(newConfig, getBaseContext().getResources().getDisplayMetrics());
        setContentView(R.layout.activity_configuration);
        setTitle(getString(R.string.title_activity_configuration));

        // to force show bottom
        if (scroll != null)
            scroll.post(new Runnable() {
            @Override
            public void run() {
                scroll.fullScroll(View.FOCUS_UP);
            }
        });
        super.createEnvironment(5);
        setViews();
        updateViews();
        addListeners();
    }
    private void addListeners(){
        // Save preferences

        swiLocale.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "onCheckedChanged:" + isChecked);
                Configuration newConfig = null;
                if (isChecked) {
                    newConfig = mainModel.updateLocale("es", "ES");
                } else {
                    newConfig = mainModel.updateLocale("ca", "ES");
                }
                mainModel.savePreferences(VinclesMobileConstants.APP_LANGUAGE, mainModel.language, VinclesConstants.PREFERENCES_TYPE_STRING);
                mainModel.savePreferences(VinclesMobileConstants.APP_COUNTRY, mainModel.country, VinclesConstants.PREFERENCES_TYPE_STRING);

                onConfigurationChanged(newConfig);
            }
        });

        swiNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean result = ((Switch) v).isChecked();
                mainModel.notifications = result;
                mainModel.savePreferences(VinclesMobileConstants.APP_NOTIFICATIONS, mainModel.notifications, VinclesConstants.PREFERENCES_TYPE_BOOLEAN);
            }
        });

        swiDownloads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean result = ((Switch) v).isChecked();
                mainModel.downloads = result;
                mainModel.savePreferences(VinclesMobileConstants.APP_DOWNLOADS, mainModel.downloads, VinclesConstants.PREFERENCES_TYPE_BOOLEAN);
            }
        });

        swiSynchronization.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean result = ((Switch) v).isChecked();
                mainModel.synchronizations = result;
                mainModel.savePreferences(VinclesMobileConstants.APP_SYNCHRONIZATIONS, mainModel.synchronizations, VinclesConstants.PREFERENCES_TYPE_BOOLEAN);

                List<Task> list = TaskModel.getInstance().getTaskList();
                for (Task task : list) {
                    if (task.owner == null) continue;
                    if (result)
                        androidCalendarModel.addOrUpdateAndroidCalendarEvent(task, ConfigurationActivity.this);
                    else
                        androidCalendarModel.deleteAndroidCalendarEvent(task, ConfigurationActivity.this);
                }
            }
        });

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        Log.w(TAG, "onCreateOptionsMenu");
        return true;
    }

    public void editPersonalData(View view) {
        mainModel.view = MainModel.CONFIGURATION_DATA;
        startActivity(getIntent());
        finish();
    }

    public void goBack(View view) {
        mainModel.view = "";
        startActivity(getIntent());
        finish();
    }

    public void savePersonalDataError(View view) {
        ll_aceptar.setVisibility(View.VISIBLE);
        ll_error.setVisibility(View.GONE);
    }

    public void savePersonalData(View view) {
        boolean ok = true;
        String message = null;
        int num = 0;

        if (ediFirstName.getText().length() <= 0) {
            message = getString(R.string.error_empty_field);
            ediFirstName.setError(message, null);
            ok = false;
            num++;
        }

        if (ediLastName.getText().length() <= 0) {
            message = getString(R.string.error_empty_field);
            ediLastName.setError(message,null);
            ok = false;
            num++;
        }

        if (ediPassword.getText().length() > 0) {
            if (!ediPassword.getText().toString().equals(ediPasswordRepeat.getText().toString())
                    || ediPassword.getText().toString().length() < 8
                    || ediPassword.getText().toString().length() > 16
                    ) {
                if (message == null) {
                    message = getString(R.string.error_passwords_repeat);
                }
                ediPassword.setError(message, null);
                ediPasswordRepeat.setError(message, null);
                ok = false;
            }

            if (ediOldPassword.getText().length() <= 0
                    || !ediOldPassword.getText().toString().equals(mainModel.getPassword(mainModel.currentUser))) {
                if (message == null) {
                    message = getString(R.string.error_old_passwords);
                }
                ediOldPassword.setError(message, null);
                ok = false;
            }
        }

        if (ediPhone.getText().length() <= 8 || ediPhone.getText().length() > 9) {
            if (message == null) {
                message = getString(R.string.error_phone_numbers);
            }
            ediPhone.setError(message,null);
            ok = false;
            num++;
        } else if (!TextUtils.isDigitsOnly(ediPhone.getText())) {
            if (message == null) {
                message = getString(R.string.error_phone_numbers);
            }
            ediPhone.setError(message,null);
            ok = false;
            num++;
        }

        if (mainModel.currentUser.imageName == null || mainModel.currentUser.imageName.equals("")) {
            imgUserConfiguration.setBorderColor(getResources().getColor(R.color.red));
            ok = false;
            num++;
        }

        if (ok) {
            onSavePersonalData();
        } else {
            ll_aceptar.setVisibility(View.GONE);
            ll_error.setVisibility(View.VISIBLE);
            TextView textUserData = (TextView)findViewById(R.id.textUserData);
            if( num >= CONFIGURATION_DATA_FIELDS_NUM) {
                message = getString(R.string.error_update);
            }
            textUserData.setText(message);
            scroll.post(new Runnable() {
                @Override
                public void run() {
                    scroll.fullScroll(View.FOCUS_DOWN);
                }
            });
        }
    }

    public void onSavePersonalData() {
        mainModel.view = "";
        Log.i(TAG, "savePersonalData()");

        // Save personal data
        mainModel.currentUser.name = ediFirstName.getText().toString();
        mainModel.currentUser.lastname = ediLastName.getText().toString();
        mainModel.currentUser.phone = ediPhone.getText().toString();
        mainModel.currentUser.liveInBarcelona = swiHome.isChecked();
        mainModel.saveUser(mainModel.currentUser);

        // Update at server
        mainModel.updateUserServer(mainModel.currentUser);

        if (ediPassword.getText().length() > 0) {
            // CHANGE PASSWORD PROCESS HERE
            mainModel.changeUserPassword(new AsyncResponse() {
                @Override
                public void onSuccess(Object result) {
                    Log.d(TAG, "Change Password Success");
                    startActivity(getIntent());
                    finish();
                }

                @Override
                public void onFailure(Object error) {
                    Log.d(null, "CHANGE PASSWORD FAILED!");
                    mainModel.showSimpleError(findViewById(R.id.main_content), mainModel.getErrorByCode(error), Snackbar.LENGTH_LONG);
                }
            }, ediPassword.getText().toString());
        }
        else {
            startActivity(getIntent());
            finish();
        }
    }

    public void takePhoto(View view) {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // Indicate file uri to save
            currentFilename = VinclesConstants.IMAGE_PREFIX + new Date().getTime() + VinclesConstants.IMAGE_EXTENSION;
            File currentImageFile = new File(VinclesConstants.getImagePath(), currentFilename);
            currentImagePath = currentImageFile.getAbsolutePath();
            Uri currentImageUri = Uri.fromFile(currentImageFile);

            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, currentImageUri);
            intent.putExtra("android.intent.extras.CAMERA_FACING", 1);
            startActivityForResult(intent, VinclesConstants.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_RESULT);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VinclesConstants.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                imgUserConfiguration = (CircleImageView) this.findViewById(R.id.imgUserConfiguration);
                if (!isFinishing())
                    Glide.with(this)
                        .load(VinclesConstants.getImageDirectory() + "/" + currentFilename)
                        .error(R.drawable.user).placeholder(R.color.superlightgray)
                        .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                        .into(imgUserConfiguration);

                mainModel.currentUser.imageName = currentFilename;
                mainModel.saveUser(mainModel.currentUser);

                // Update photo at server
                mainModel.updateUserPhoto(currentFilename);
            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the image capture
            } else {
                // Image capture failed, advise currentUser
            }
        }
    }
}
