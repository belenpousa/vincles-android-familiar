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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.util.Security;
import cat.bcn.vincles.lib.util.TokenAuthenticator;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.activity.MainActivity;
import cat.bcn.vincles.mobile.activity.home.HomeActivity;

public class MigrateUserActivity extends MainActivity {
    private final String TAG = this.getClass().getSimpleName();
    private static int REGISTRATION_DATA_FIELDS_NUM = 2;
    private EditText ediEmail, ediEmailRepeat, ediPassword, ediPasswordRepeat;
    private View migrateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_migrate_old_user);

        ediEmail            = (EditText) findViewById(R.id.ediEmail);
        ediEmailRepeat      = (EditText) findViewById(R.id.ediEmailRepeat);
        ediPassword         = (EditText) findViewById(R.id.ediPassword);
        ediPasswordRepeat   = (EditText) findViewById(R.id.ediPasswordRepeat);
        migrateButton       = findViewById(R.id.migrate_button);

        migrateInfo(null);
        findViewById(R.id.migrate_hint).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                migrateInfo(null);
            }
        });
    }

    public boolean validate() {
        boolean ok = true;
        String message = null;
        int num = 0;

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
                || ediPassword.getText().toString().length() < 8) {
            if (message == null) {
                message = getString(R.string.error_passwords_repeat);
            }
            ediPassword.setError(message,null);
            ediPasswordRepeat.setError(message,null);
            ok = false;
            num++;
        }

        if (!ediEmail.getText().toString().equals(ediEmailRepeat.getText().toString())) {
            if (message == null) {
                message = getString(R.string.error_email_repeat);
            }
            ediEmail.setError(message,null);
            ediEmailRepeat.setError(message,null);
            ok = false;
            num++;
        }

        if (!ok) {
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
        return ok;
    }

    public void migrateInfo(View v) {
        new AlertDialog.Builder(MigrateUserActivity.this)
                .setTitle(R.string.migrate_user)
                .setMessage(R.string.migrate_info)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .setCancelable(false)
                .show();
    }

    public void migrateUser(View view) {
        if (validate()) {
            migrateButton.setEnabled(false);
            // HACK TOKEN AUTHENTICATOR TO ACCESS NEW TOKEN BEFORE SAVE USER
            mainModel.getPassword(mainModel.currentUser);
            TokenAuthenticator.password = mainModel.currentUser.cipher;

            mainModel.migrateUser(new AsyncResponse() {
                @Override
                public void onSuccess(Object result) {
                    Log.d(TAG, "USER MIGRATION SUCCEDED");
                    migrateButton.setEnabled(true);
                    new AlertDialog.Builder(MigrateUserActivity.this)
                            .setTitle(R.string.migrate_ok)
                            .setMessage(R.string.migrate_success)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(MigrateUserActivity.this, MigrateValidateUserActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setCancelable(false)
                            .show();
                }

                @Override
                public void onFailure(Object error) {
                    Log.d(null, "USER MIGRATION FAILED!");
                    migrateButton.setEnabled(true);
                    mainModel.showSimpleError(findViewById(R.id.main_content), mainModel.getErrorByCode(error), Snackbar.LENGTH_LONG);
                }
            },
            ediEmail.getText().toString(),
            ediPassword.getText().toString());
        }
    }
}