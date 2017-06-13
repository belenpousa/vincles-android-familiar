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
import cat.bcn.vincles.mobile.activity.home.HomeActivity;

public class RecoverPassActivity extends MainActivity {
    private final String TAG = this.getClass().getSimpleName();
    private EditText ediEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recoverpass);

        ediEmail = (EditText)findViewById(R.id.ediEmail);
    }


    public void recoverPass(View view) {
        if (ediEmail.getText().length() == 0) {
            ediEmail.setError(getString(R.string.error_mandatory_field));
            mainModel.showSimpleError(findViewById(R.id.main_content), getString(R.string.error_mandatory_field), Snackbar.LENGTH_LONG);
            return;
        }

        mainModel.recoverPassword(new AsyncResponse() {
                                      @Override
                                      public void onSuccess(Object result) {
                                          Log.d(null, "RECOVER PASSWORD SUCCESS!");
                                          new AlertDialog.Builder(RecoverPassActivity.this)
                                                  .setTitle(R.string.recover_pass_title)
                                                  .setMessage(R.string.recover_pass_success)
                                                  .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                                      public void onClick(DialogInterface dialog, int which) {
                                                          finish();
                                                      }
                                                  })
                                                  .setIcon(android.R.drawable.ic_dialog_info)
                                                  .setCancelable(false)
                                                  .show();
                                      }

                                      @Override
                                      public void onFailure(Object error) {
                                          Log.d(null, "RECOVER PASSWORD FAILED!");
                                          mainModel.showSimpleError(findViewById(R.id.main_content), mainModel.getErrorByCode(error), Snackbar.LENGTH_LONG);
                                      }
                                  },
                ediEmail.getText().toString());
    }
}