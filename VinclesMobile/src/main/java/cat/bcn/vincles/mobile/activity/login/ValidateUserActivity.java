/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.activity.login;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.activity.MainActivity;
import cat.bcn.vincles.mobile.activity.TourActivity;

public class ValidateUserActivity extends MainActivity {
    protected final String TAG = this.getClass().getSimpleName();
    protected EditText ediVerify;
    protected View validateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_validate);

        ediVerify = (EditText)findViewById(R.id.ediValidate);
        validateButton = findViewById(R.id.validate_button);
    }


    public void validateUser(View view) {
        String validateCode = ediVerify.getText().toString();
        if (validateCode.length() == 0) {
            ediVerify.setError(getString(R.string.error_empty_code));
            mainModel.showSimpleError(findViewById(R.id.main_content), getString(R.string.error_empty_code), Snackbar.LENGTH_LONG);
            return;
        }

        if (validateCode.toLowerCase().charAt(0) != 'v') {
            ediVerify.setError(getString(R.string.error_1300));
            mainModel.showSimpleError(findViewById(R.id.main_content), getString(R.string.error_1300), Snackbar.LENGTH_LONG);
            return;
        }

        String realValidationCode = validateCode.substring(1);
        performValidation(realValidationCode);
    }

    public void performValidation(String realValidationCode) {
        validateButton.setEnabled(false);
        mainModel.validateUser(new AsyncResponse() {
                                   @Override
                                   public void onSuccess(Object result) {
//                                       validateButton.setEnabled(true);
                                       mainModel.updateUserPhoto(mainModel.currentUser.imageName);
                                       Intent intent = new Intent(ValidateUserActivity.this, TourActivity.class);
                                       intent.putExtra(MainActivity.IS_ROOT_ACTIVITY, "1");
                                       finishAffinity();
                                       startActivity(intent);
                                       finish();
                                   }

                                   @Override
                                   public void onFailure(Object error) {
                                       validateButton.setEnabled(true);
                                       Log.d(null, "LOGIN FAILED!");
                                       mainModel.showSimpleError(findViewById(R.id.main_content), mainModel.getErrorByCode(error), Snackbar.LENGTH_LONG);
                                   }
                               },
                realValidationCode);
    }

    public void resendEmail(View v) {
        mainModel.registerUser(new AsyncResponse() {
            @Override public void onSuccess(Object result) {
                mainModel.showSimpleError(findViewById(R.id.main_content), getString(R.string.email_sent), Snackbar.LENGTH_LONG);
            }

            @Override
            public void onFailure(Object error) {
                Log.i(TAG, "error: " + error);
                mainModel.showSimpleError(findViewById(R.id.main_content), mainModel.getErrorByCode(error), Snackbar.LENGTH_LONG);
            }
        }, mainModel.currentUser
        , mainModel.getPassword(mainModel.currentUser));
    }

    public void goBack(View v) {
        new AlertDialog.Builder(ValidateUserActivity.this)
                .setTitle(R.string.reset_register_process_title)
                .setMessage(R.string.reset_register_process_info)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mainModel.wipeout(ValidateUserActivity.this);
                        Intent intent = new Intent(ValidateUserActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        finishAffinity();
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(false)
                .show();
    }
}