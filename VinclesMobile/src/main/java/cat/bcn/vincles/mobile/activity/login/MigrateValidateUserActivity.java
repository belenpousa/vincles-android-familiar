/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.activity.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import cat.bcn.vincles.lib.dao.UserDAOImpl;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.vo.User;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.activity.MainActivity;
import cat.bcn.vincles.mobile.activity.home.HomeActivity;
import cat.bcn.vincles.mobile.util.VinclesMobileConstants;

public class MigrateValidateUserActivity extends ValidateUserActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void performValidation(String realValidationCode) {
        validateButton.setEnabled(false);
        mainModel.migrateValidateUser(new AsyncResponse() {
                                   @Override
                                   public void onSuccess(Object result) {
                                       Intent intent = new Intent(MigrateValidateUserActivity.this, HomeActivity.class);
                                       intent.putExtra(MainActivity.IS_ROOT_ACTIVITY, "1");
                                       startActivity(intent);
                                       finish();
                                   }

                                   @Override
                                   public void onFailure(Object error) {
                                       validateButton.setEnabled(true);
                                       Log.d(null, "USER MIGRATION FAILED!");
                                       mainModel.showSimpleError(findViewById(R.id.main_content), mainModel.getErrorByCode(error), Snackbar.LENGTH_LONG);
                                   }
                               },
                realValidationCode);
    }

    @Override
    public void resendEmail(View v) {
        Long tempId = mainModel.preferences.getLong(VinclesMobileConstants.MIGRATION_USER_ID, 0l);
        User migrationTempUser = new UserDAOImpl().get(tempId);

        mainModel.migrateUser(new AsyncResponse() {
                                  @Override
                                  public void onSuccess(Object result) {
                                      mainModel.showSimpleError(findViewById(R.id.main_content), getString(R.string.email_sent), Snackbar.LENGTH_LONG);
                                  }

                                  @Override
                                  public void onFailure(Object error) {
                                      Log.d(null, "USER MIGRATION FAILED!");
                                      mainModel.showSimpleError(findViewById(R.id.main_content), mainModel.getErrorByCode(error), Snackbar.LENGTH_LONG);
                                  }
                              },
                migrationTempUser.email,
                mainModel.getPassword(migrationTempUser));
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MigrateUserActivity.class));
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            startActivity(new Intent(this, MigrateUserActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }
}