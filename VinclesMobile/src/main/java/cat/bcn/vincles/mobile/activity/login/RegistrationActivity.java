/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.activity.login;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import com.bumptech.glide.Glide;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.activity.MainActivity;
import cat.bcn.vincles.mobile.model.MainModel;
import cat.bcn.vincles.mobile.util.VinclesMobileConstants;
import de.hdodenhof.circleimageview.CircleImageView;

public class RegistrationActivity extends MainActivity {
    private final String TAG = this.getClass().getSimpleName();
    public static final String VINCLES_ERROR_CODE = "VINCLES_ERROR_CODE";
    private Switch swiLocale;
    private CircleImageView imgPhoto;
    private EditText ediFirstName;
    private EditText ediLastName;
    private Spinner spiYear;
    private Spinner spiMonth;
    private Spinner spiDay;
    private LinearLayout spiYearBg;
    private LinearLayout spiMonthBg;
    private LinearLayout spiDayBg;
    private EditText ediPhone;
    private Switch swiGender;
    private Switch swiHome;
    private String currentFilename;
    private String currentImagePath;

    private EditText ediEmail;
    private EditText ediPassword;
    private EditText ediPasswordRepeat;

    private final int YEAR_END = 1900;
    private static int REGISTRATION_DATA_FIELDS_NUM = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mainModel.view.equals(MainModel.REGISTER_BLOCKED)) {
            setContentView(R.layout.activity_registration_blocked);
        } else if (mainModel.view.equals(MainModel.REGISTER_DISCLAIMER)) {
            setContentView(R.layout.activity_registration_disclaimer);
            String htmlAsString = getString(R.string.termsandconditions);
            WebView webView = (WebView) findViewById(R.id.webView);
            webView.setBackgroundColor(Color.TRANSPARENT);
            webView.loadDataWithBaseURL(null, htmlAsString, "text/html", "utf-8", null);
        } else {
            setContentView(R.layout.activity_registration);

            setViews();
            updateViews();
            addListeners();
        }
    }

    private void setViews() {
        imgPhoto          = (CircleImageView) findViewById(R.id.imgPhoto);
        ediFirstName      = (EditText) findViewById(R.id.ediFirstName);
        ediLastName       = (EditText) findViewById(R.id.ediLastname);
        ediPhone          = (EditText) findViewById(R.id.ediPhone);
        swiGender         = (Switch) findViewById(R.id.swiGender);
        swiHome           = (Switch) findViewById(R.id.swiHome);
        swiLocale         = (Switch) findViewById(R.id.swiLocale);
        spiYear           = (Spinner) findViewById(R.id.spiYear);
        spiMonth          = (Spinner) findViewById(R.id.spiMonth);
        spiDay            = (Spinner) findViewById(R.id.spiDay);
        spiYearBg         = (LinearLayout) findViewById(R.id.spiYearBg);
        spiMonthBg        = (LinearLayout) findViewById(R.id.spiMonthBg);
        spiDayBg          = (LinearLayout) findViewById(R.id.spiDayBg);
        ediEmail          = (EditText) findViewById(R.id.ediEmail);
        ediPassword       = (EditText) findViewById(R.id.ediPassword);
        ediPasswordRepeat = (EditText) findViewById(R.id.ediPasswordRepeat);
    }

    private void updateViews() {
        if (!isFinishing())
            Glide.with(this)
                .load(mainModel.getUserPhotoUrlFromUser(mainModel.currentUser))
                .error(R.drawable.user).placeholder(R.color.superlightgray)
                .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                .into(imgPhoto);

        ediFirstName.setText(mainModel.currentUser.name.toString());
        ediLastName.setText(mainModel.currentUser.lastname.toString());
        ediEmail.setText(mainModel.currentUser.email.toString());
        if (mainModel.currentUser.phone != null && mainModel.currentUser.phone.length() > 0) {
            ediPhone.setText("" + mainModel.currentUser.phone);
        }

        swiGender.setChecked(mainModel.currentUser.gender);
        if (mainModel.currentUser.liveInBarcelona) {
            swiHome.setChecked(true);
        } else {
            swiHome.setChecked(false);
        }
        if (mainModel.language.equals("ca")) {
            swiLocale.setChecked(false);
        } else {
            swiLocale.setChecked(true);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        getBaseContext().getResources().updateConfiguration(newConfig, getBaseContext().getResources().getDisplayMetrics());
        setContentView(R.layout.activity_registration);

        setViews();
        updateViews();
        addListeners();
    }

    private void addListeners() {

        swiLocale.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG,"onCheckedChanged:" + isChecked);
                Configuration newConfig = null;
                if (isChecked) {
                    newConfig = mainModel.updateLocale("es", "ES");
                } else {
                    newConfig = mainModel.updateLocale("ca", "ES");
                }

                // Save current data!
                int year = 1950;
                int month = 0;
                int day = 1;
                try {
                    day = Integer.parseInt(spiDay.getSelectedItem().toString());// datBirthday.getDayOfMonth();
                    month = spiMonth.getSelectedItemPosition() - 1;//datBirthday.getMonth();
                    year = Integer.parseInt(spiYear.getSelectedItem().toString());//datBirthday.getYear();
                } catch (NumberFormatException e) {
                    Log.e(TAG, e.toString());
                }
                Date today = new Date();
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, month);
                cal.set(Calendar.DAY_OF_MONTH, day);
                cal.set(Calendar.HOUR, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                Date selectedDate = cal.getTime();
                savePreferencesData(selectedDate);

                mainModel.savePreferences(VinclesMobileConstants.APP_LANGUAGE, mainModel.language, VinclesConstants.PREFERENCES_TYPE_STRING);
                mainModel.savePreferences(VinclesMobileConstants.APP_COUNTRY, mainModel.country, VinclesConstants.PREFERENCES_TYPE_STRING);

                onConfigurationChanged(newConfig);
            }
        });

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayList<String> yearList = new ArrayList<String>();
        yearList.add(getResources().getString(R.string.year));
        int year = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = year; i >= YEAR_END; i--) {
            yearList.add(Integer.toString(i));
        }
        // Specify the layout to use when the list of choices appears
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, yearList);
        // Apply the adapter to the spinner
        spiYear.setAdapter(yearAdapter);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource(this,   R.array.monthList, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spiMonth.setAdapter(monthAdapter);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayList<String> dayList = new ArrayList<String>();
        dayList.add(getResources().getString(R.string.day));
        for (int i = 1; i <= 31; i++) {
            dayList.add(Integer.toString(i));
        }
        // Specify the layout to use when the list of choices appears
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, dayList);
        // Apply the adapter to the spinner
        spiDay.setAdapter(dayAdapter);

        if (mainModel.currentUser.birthdate != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(mainModel.currentUser.birthdate);
            int selectedYear = yearList.size() - (cal.get(Calendar.YEAR) - YEAR_END + 1);
            int selectedMonth = cal.get(Calendar.MONTH) + 1;
            int selectedDay = cal.get(Calendar.DATE);
            spiDay.setSelection(selectedDay);
            spiMonth.setSelection(selectedMonth);
            spiYear.setSelection(selectedYear);
        }
    }

    public void updateLocale(View view) {
        boolean result = ((Switch) view).isChecked();
        if (result) {
            mainModel.updateLocale("es", "ES");
        } else {
            mainModel.updateLocale("ca", "ES");
        }

        Log.i(TAG, "updateLocale() - result: " + result);
    }

    public void register(View view) {
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

        if (ediEmail.getText().toString().length() > 0 && !VinclesConstants.isEmailValid(ediEmail.getText().toString())
                || ediEmail.getText().toString().length() == 0) {
            if (message == null) {
                message = getString(R.string.error_invalid_email);
            }
            ediEmail.setError(message,null);
            ok = false;
            num++;
        }

        if (!ediPassword.getText().toString().equals(ediPasswordRepeat.getText().toString())
                || ediPassword.getText().toString().length() < 8
                || ediPassword.getText().toString().length() > 16
                ) {
            if (message == null) {
                message = getString(R.string.error_passwords_repeat);
            }
            ediPassword.setError(message,null);
            ediPasswordRepeat.setError(message,null);
            ok = false;
            num++;
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

        if (spiYear.getSelectedItemPosition() == 0) {
            spiYearBg.setBackground(getResources().getDrawable(R.drawable.edittext_background_red));
            ok = false;
            num++;
            message = getString(R.string.error_invalid_date);
        }

        if (spiMonth.getSelectedItemPosition() == 0) {
            spiMonthBg.setBackground(getResources().getDrawable(R.drawable.edittext_background_red));
            ok = false;
            num++;
            message = getString(R.string.error_invalid_date);
        }

        if (spiDay.getSelectedItemPosition() == 0) {
            spiDayBg.setBackground(getResources().getDrawable(R.drawable.edittext_background_red));
            ok = false;
            num++;
            message = getString(R.string.error_invalid_date);
        }

        if (mainModel.currentUser.imageName == null || mainModel.currentUser.imageName.equals("")) {
            if (message == null) {
                message = getString(R.string.error_empty_photo);
            }
            imgPhoto.setBorderColor(getResources().getColor(R.color.red));
            ok = false;
            num++;
        }

        if (ok) {
            onRegister();
        } else {
            if( num >= REGISTRATION_DATA_FIELDS_NUM) {
                message = getString(R.string.error_update);
            }
            final Snackbar snackbar = Snackbar.make(findViewById(R.id.main_content),
                    message, Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction(R.string.close, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    snackbar.dismiss();
                }
            });
            snackbar.show();
        }
    }

    private void onRegister() {
        // TODO: DatePicker datBirthday = (DatePicker) findViewById(R.id.datBirthday);
        int year = 1950;
        int month = 0;
        int day = 1;
        try {
            day = Integer.parseInt(spiDay.getSelectedItem().toString());// datBirthday.getDayOfMonth();
            month = spiMonth.getSelectedItemPosition() - 1;//datBirthday.getMonth();
            year = Integer.parseInt(spiYear.getSelectedItem().toString());//datBirthday.getYear();
        } catch (NumberFormatException e) {
            Log.e(TAG, e.toString());
        }

        Date today = new Date();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date selectedDate = cal.getTime();

        long days = VinclesConstants.getDayInterval(today, selectedDate);

        // Check date is over 14 years
        int LEGAL_AGE = 14;
        if (days < 365 * LEGAL_AGE) {
            mainModel.view = MainModel.REGISTER_BLOCKED;
            startActivity(getIntent());
            finish();
        } else {
            savePreferencesData(selectedDate);
            mainModel.view = MainModel.REGISTER_DISCLAIMER;
            registerUser();
        }
    }

    private void savePreferencesData(Date date) {
        // Fill data to save
        mainModel.currentUser.name = ediFirstName.getText().toString();
        mainModel.currentUser.lastname = ediLastName.getText().toString();
        if (swiLocale.isChecked()) {
            mainModel.updateLocale("es", "ES");
        } else {
            mainModel.updateLocale("ca", "ES");
        }
        mainModel.currentUser.birthdate = date;
        mainModel.currentUser.email = ediEmail.getText().toString();
        mainModel.currentUser.phone = ediPhone.getText().toString();
        mainModel.currentUser.gender = swiGender.isChecked();
        mainModel.currentUser.liveInBarcelona = swiHome.isChecked();

        // Save data to preferences
        mainModel.savePreferences(VinclesMobileConstants.APP_LANGUAGE, mainModel.language, VinclesConstants.PREFERENCES_TYPE_STRING);
        mainModel.savePreferences(VinclesMobileConstants.APP_COUNTRY, mainModel.country, VinclesConstants.PREFERENCES_TYPE_STRING);
        // CAUTION: Don't save User here until get ID from server
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
                imgPhoto = (CircleImageView) this.findViewById(R.id.imgPhoto);
                if (!isFinishing())
                    Glide.with(this)
                        .load(VinclesConstants.getImageDirectory() + "/" + currentFilename)
                        .error(R.drawable.user).placeholder(R.color.superlightgray)
                        .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                        .into(imgPhoto);

                imgPhoto.setBorderColor(Color.BLACK);
                mainModel.currentUser.imageName = currentFilename;
            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the image capture
            } else {
                // Image capture failed, advise currentUser
            }
        }
    }

    public void quitVincles(View view) {
        mainModel.view = "";

        startActivity(getIntent());
        finish();
    }

    private void registerUser() {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.main_content), getString(R.string.registration_process), Snackbar.LENGTH_INDEFINITE);
        ((ImageView)snackbar.getView().findViewById(R.id.snackbar_icon)).setImageResource(R.drawable.icon_network_white);
        snackbar.getView().findViewById(R.id.snackbar_icon).setVisibility(View.VISIBLE);
        snackbar.show();

        mainModel.registerUser(new AsyncResponse() {
            @Override
            public void onSuccess(Object result) {
                Intent intent = new Intent(RegistrationActivity.this, ValidateUserActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                finishAffinity();
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Object error) {
                    Log.i(TAG, "error: " + error);
                    mainModel.showSimpleError(findViewById(R.id.main_content), mainModel.getErrorByCode(error), Snackbar.LENGTH_LONG);
            }
        }, mainModel.currentUser
        , ediPassword.getText().toString());
    }

}