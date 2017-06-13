/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.activity.videocall;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.activity.MainActivity;

public class VideoCallIntroActivity extends MainActivity {
    private static final String TAG = "VideoCallActivity";
    protected ImageView userImage;
    protected TextView userText;
    private View callButtonLayout;
    protected int layout = R.layout.activity_videocall_intro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout);
        setTitle(getString(R.string.title_activity_video_call));

        super.createEnvironment(1);
        userImage = (ImageView) findViewById(R.id.user_image);
        userText = (TextView) findViewById(R.id.user_text);
        callButtonLayout = findViewById(R.id.call_button_layout);
    }


    @Override
    protected void onResume() {
        super.onResume();
        callButtonLayout.setEnabled(true);

        if (mainModel.currentNetwork != null) {
            userText.setText(mainModel.currentNetwork.userVincles.alias);
            Glide.with(this)
                    .load(mainModel.getUserPhotoUrlFromUser(mainModel.currentNetwork.userVincles))
                    .error(R.drawable.user).placeholder(R.color.superlightgray)
                    .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                    .into(userImage);
        }

        // Check strength connection
        checkStrengthSignalStatus();
    }

    public void onCall(final View v) {
        v.setEnabled(false);
        VideoConferenceCallActivity.startActivityForOutgoingCall(this, mainModel);
    }
}