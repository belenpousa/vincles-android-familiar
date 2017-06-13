/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.activity.message;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.activity.MainActivity;
import cat.bcn.vincles.mobile.model.MessageModel;

public class MessageTemplateActivity extends MainActivity {
    private static final String TAG = "MessageActivity";
    protected MessageModel messageModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        messageModel = MessageModel.getInstance();
        setContentView(R.layout.activity_message);
    }
}
