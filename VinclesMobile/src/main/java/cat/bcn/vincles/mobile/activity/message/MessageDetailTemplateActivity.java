/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.activity.message;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.Locale;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.vo.Message;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.activity.MainActivity;
import cat.bcn.vincles.mobile.activity.videocall.VideoCallIntroActivity;
import cat.bcn.vincles.mobile.model.MessageModel;

public class MessageDetailTemplateActivity extends MainActivity {
    private static final String TAG = "MessageDetailActivity";
    TextView titleTextDuration, titleTextDate;
    protected MessageModel messageModel;
    private Snackbar snackbar;
    protected boolean isReady = false;

    protected int layout = R.layout.content_message_detail_audio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_detail);

        ViewGroup containerCanvas = (ViewGroup)findViewById(R.id.linMessageDetail);
        containerCanvas.removeAllViews();
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(layout, containerCanvas);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        messageModel = MessageModel.getInstance();
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey("GCM_MESSAGE_ID")) {
            MessageModel messageModel = MessageModel.getInstance();
            messageModel.currentMessage.getResources();
            messageModel.currentMessage = Message.findById(Message.class, extras.getLong("GCM_MESSAGE_ID"));
            messageModel.view = MessageModel.MESSAGE_DETAIL;
        }

        titleTextDate = (TextView)findViewById(R.id.newmessage_title_day);
        titleTextDuration = (TextView)findViewById(R.id.newmessage_title_duration);

        // Mark message as watched
        if (messageModel.currentMessage.watched != true) {
            messageModel.markMessageAsWatched(messageModel.currentMessage);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        String dateString = VinclesConstants.getDateString(messageModel.currentMessage.sendTime, getResources().getString(R.string.dateSmallformat),
                new Locale(getResources().getString(R.string.locale_language), getResources().getString(R.string.locale_country)));

        if (DateUtils.isToday(messageModel.currentMessage.sendTime.getTime()))
            dateString = getString(R.string.task_today);

        dateString = dateString.substring(0, 1).toUpperCase() + dateString.substring(1);

        titleTextDate.setText(dateString);
        titleTextDuration.setText(VinclesConstants.getDateString(messageModel.currentMessage.sendTime, getResources().getString(R.string.timeformat),
                new Locale(getResources().getString(R.string.locale_language), getResources().getString(R.string.locale_country))));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                startActivity(new Intent(this, MessageListActivity.class));
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void stopDialog() {
        // Close previous dialog if exist
        if (snackbar != null && snackbar.isShownOrQueued()) {
            snackbar.dismiss();
        }
    }

    public void showLoadingDialog() {
        stopDialog();
        snackbar = Snackbar.make(findViewById(R.id.main_content), R.string.message_loading_data, Snackbar.LENGTH_INDEFINITE);
        snackbar.show();
    }

    protected boolean checkResourceAlreadyDownloaded() {
        return messageModel.currentMessage.getCurrentResource().filename != ""
                && messageModel.currentMessage.getCurrentResource().filename != null;
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MessageListActivity.class));
    }
}
