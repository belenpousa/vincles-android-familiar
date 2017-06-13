/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.activity.videocall;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.activity.message.MessageActivityNew;
import cat.bcn.vincles.mobile.activity.message.MessageListActivity;

public class VideoCallLostCallActivity extends VideoCallIntroActivity {
    private static final String TAG = "VideoCallActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        layout = R.layout.activity_videocall_lostcall;
        setTitle(getString(R.string.title_activity_video_call));
        super.onCreate(savedInstanceState);

        checkIfCallWasConnected(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        checkIfCallWasConnected(intent);
    }

    private void checkIfCallWasConnected(Intent intent) {
        boolean wasCallConnected = intent.getBooleanExtra("wasCallConnected", false);
        Log.d(TAG, "wasCallConnected? " + wasCallConnected);
        // TODO change text depending on whether there was an actual connected call before it was lost
    }

    @Override
    protected void onResume() {
        super.onResume();
        userText.setText(getString(R.string.task_videocall_lostcall, mainModel.currentNetwork.userVincles.alias));
    }

    public void onMessage(View v) {
        startActivity(new Intent(this, MessageActivityNew.class));
    }
}