/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.activity.about;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.widget.TextView;
import cat.bcn.vincles.mobile.BuildConfig;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.activity.MainActivity;
import cat.bcn.vincles.mobile.util.VinclesMobileConstants;

public class AboutActivity extends MainActivity {
    private static final String TAG = "AboutActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setTitle(getString(R.string.title_activity_about));

        TextView aboutTextView = (TextView) findViewById(R.id.about_text);
        TextView versionTextView = (TextView) findViewById(R.id.version_text);
        aboutTextView.setMovementMethod(LinkMovementMethod.getInstance());
        versionTextView.setText(getString(R.string.app_version) + BuildConfig.VERSION_NAME);
        super.createEnvironment(6);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }
}
