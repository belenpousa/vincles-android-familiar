/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.activity.login;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.vo.User;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.activity.MainActivity;
import cat.bcn.vincles.mobile.activity.home.HomeActivity;
import cat.bcn.vincles.mobile.util.VinclesMobileConstants;

public class LoginActivity extends MainActivity  {
    private final String TAG = this.getClass().getSimpleName();
    private EditText ediEmail;
    private EditText ediPassword;
    private View loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ediEmail = (EditText)findViewById(R.id.ediEmail);
        ediPassword = (EditText)findViewById(R.id.ediPassword);
        loginButton = findViewById(R.id.login_button);
    }


    public void recoverPasswordClick(View view) {
        Intent intent = new Intent(this, RecoverPassActivity.class);
        startActivity(intent);
    }

    public void loginClick(View view) {
        if (ediEmail.getText().length() == 0) {
            ediEmail.setError(getString(R.string.error_mandatory_field));
            mainModel.showSimpleError(findViewById(R.id.main_content), getString(R.string.error_mandatory_field), Snackbar.LENGTH_LONG);
            return;
        }

        if (ediPassword.getText().length() == 0) {
            ediPassword.setError(getString(R.string.error_mandatory_field));
            mainModel.showSimpleError(findViewById(R.id.main_content), getString(R.string.error_mandatory_field), Snackbar.LENGTH_LONG);
            return;
        }

        loginButton.setEnabled(false);
        mainModel.login(new AsyncResponse() {
            @Override
            public void onSuccess(Object result) {
                Log.d(null, "LOGGED IN OK!");
                mainModel.tour = false;

                SharedPreferences.Editor editor = mainModel.preferences.edit();
                editor.remove(VinclesMobileConstants.MIGRATION_USER_ID);
                editor.putBoolean(VinclesMobileConstants.TOUR, false);
                if (mainModel.language.equalsIgnoreCase("es")) {
                    editor.putString(VinclesMobileConstants.APP_LANGUAGE, "es");
                    editor.putString(VinclesMobileConstants.APP_COUNTRY, "ES");
                } else {
                    editor.putString(VinclesMobileConstants.APP_LANGUAGE, "ca");
                    editor.putString(VinclesMobileConstants.APP_COUNTRY, "ES");
                }
                editor.commit();

                mainModel.updateLocalUser(new AsyncResponse() {
                    @Override
                    public void onSuccess(Object result) {
                        loginSuccess();
                    }

                    @Override
                    public void onFailure(Object error) {
                        if (((String)error).equalsIgnoreCase("UPDATE_PHOTO_ERROR")) {
                            new AlertDialog.Builder(LoginActivity.this)
                                    .setTitle(R.string.error_photo_update_title)
                                    .setMessage(R.string.error_photo_update_info)
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            loginSuccess();
                                        }
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setCancelable(false)
                                    .show();
                        } else {
                            loginButton.setEnabled(true);
                            Log.d(null, "UPDATE LOCAL USER FAILED!");
                            mainModel.showSimpleError(findViewById(R.id.main_content), mainModel.getErrorByCode(error), Snackbar.LENGTH_LONG);
                        }
                    }
                });
            }

            @Override
            public void onFailure(Object error) {
                Log.d(null, "LOGIN FAILED!");
                loginButton.setEnabled(true);
                mainModel.showSimpleError(findViewById(R.id.main_content), mainModel.getErrorByCode(error), Snackbar.LENGTH_LONG);
            }
        },
        ediEmail.getText().toString(),
        ediPassword.getText().toString());
    }


    public void newUserClick(View view) {
        Intent intent = new Intent(this, RegistrationActivity.class);
        startActivity(intent);
    }

    private void loginSuccess() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        finishAffinity();
        startActivity(intent);
        finish();
    }
}