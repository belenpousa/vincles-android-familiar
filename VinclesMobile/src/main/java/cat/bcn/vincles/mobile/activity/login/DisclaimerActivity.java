/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.activity.login;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.activity.MainActivity;
import cat.bcn.vincles.mobile.activity.login.LoginActivity;
import cat.bcn.vincles.mobile.util.VinclesMobileConstants;

public class DisclaimerActivity extends MainActivity {
    private final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_disclaimer);

        String htmlAsString = getString(R.string.termsandconditions);
        WebView webView = (WebView) findViewById(R.id.webView);
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.loadDataWithBaseURL(null, htmlAsString, "text/html", "utf-8", null);
    }

    public void acceptDisclaimer(View view) {
        mainModel.savePreferences(VinclesMobileConstants.APP_DISCLAIMER_ACCEPTED, true, VinclesConstants.PREFERENCES_TYPE_BOOLEAN);
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void rejectDisclaimer(View view) {
        finish();
    }
}