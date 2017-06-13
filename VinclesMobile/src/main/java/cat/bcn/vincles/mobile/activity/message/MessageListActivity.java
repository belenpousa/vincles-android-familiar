/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.activity.message;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import java.util.List;
import cat.bcn.vincles.lib.push.CommonVinclesGcmHelper;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.vo.Message;
import cat.bcn.vincles.lib.vo.PushMessage;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.activity.MainActivity;
import cat.bcn.vincles.mobile.activity.message.adapter.MessageRVAdapter;
import cat.bcn.vincles.mobile.activity.network.NetworkActivity;
import cat.bcn.vincles.mobile.component.swipetoremove.SwipeToRemoveTouchCallback;
import cat.bcn.vincles.mobile.model.MainModel;
import cat.bcn.vincles.mobile.push.AppFCMDefaultListenerImpl;

public class MessageListActivity extends MessageTemplateActivity {
    private static final String TAG = "MessageActivity";
    private RecyclerView lisMessage;
    private List<Message> items;
    private MessageRVAdapter adapter;
    private View textErrorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lisMessage = (RecyclerView) findViewById(R.id.lisMessage);
        textErrorLayout = findViewById(R.id.texError);
        setTitle(getString(R.string.title_activity_message));

        SwipeToRemoveTouchCallback simpleItemTouchCallback = new SwipeToRemoveTouchCallback(lisMessage, this);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(lisMessage);

        lisMessage.setLayoutManager(new LinearLayoutManager(this));
        super.createEnvironment(2);
    }

    @Override
    protected void onResume() {
        // HARDCODE ROOT ACTIVITY (WORKFLOW BUG: BACK BUTTON FROM DETAILMESSAGE)
        getIntent().putExtra(MainActivity.IS_ROOT_ACTIVITY, 1);
        super.onResume();

        // Get last message which is first record!
        items = messageModel.getMessageList();
        String fromDate = "";
        Message lastMessage = null;
        if (items.size() > 0) {
            lastMessage = items.get(0);
            if (lastMessage != null && lastMessage.sendTime != null)
                fromDate = String.valueOf(lastMessage.sendTime.getTime() + 1);
        }

        // IT DOES NOTHING IF avoidServerCalls IS TRUE (in case you are questioning.. it is)
        messageModel.getMessageServerList(new AsyncResponse() {
            @Override
            public void onSuccess(Object result) {
                Log.i(TAG, "getMessageServerList() - result");
                refreshList();
            }

            @Override
            public void onFailure(Object error) {
                Log.e(TAG, "getMessageServerList() - error: " + error);
                mainModel.showSimpleError(findViewById(R.id.main_content), getResources().getString(R.string.error_messsage_load_list), Snackbar.LENGTH_LONG);
            }
        }, fromDate, "");

        // REACTIVE LIST ON NEW MESSAGE
        CommonVinclesGcmHelper.setPushListener(new AppFCMDefaultListenerImpl(this) {
            @Override
            public void onPushMessageReceived(PushMessage pushMessage) {
                super.onPushMessageReceived(pushMessage);
                try {
                    if (pushMessage.getType().equalsIgnoreCase(PushMessage.TYPE_NEW_MESSAGE))
                        refreshList();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onPushMessageError(long idPush, Throwable t) {
                super.onPushMessageError(idPush, t);
            }
        });

        refreshList();
    }

    @Override
    public void onPause() {
        super.onPause();
        CommonVinclesGcmHelper.setPushListener(MainModel.getInstance().getPushListener());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MessageActivityNew.REQUEST_CREATE_NEW_VINCLES_MESSAGE) {
            if (resultCode == RESULT_OK) {
                Snackbar snackbar = Snackbar.make(findViewById(R.id.main_content), R.string.message_snackbar_sendok, Snackbar.LENGTH_LONG);
                snackbar.getView().findViewById(R.id.snackbar_icon).setVisibility(View.VISIBLE);
                snackbar.show();
            } else if (resultCode == RESULT_CANCELED) {

            }
        }
    }

    public void refreshList() {
        items = messageModel.getMessageList();
        if (items.size() == 0) textErrorLayout.setVisibility(View.VISIBLE);
        else textErrorLayout.setVisibility(View.GONE);
        adapter = new MessageRVAdapter(this, items);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                lisMessage.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        });

    }

    public void addMessage(View view) {
        messageModel.view = "";
        if (mainModel.currentUser != null && mainModel.currentNetwork != null) {
            startActivityForResult(new Intent(this, MessageActivityNew.class), MessageActivityNew.REQUEST_CREATE_NEW_VINCLES_MESSAGE);
        }
        else {
            mainModel.showCustomError(findViewById(R.id.main_content),
                    getString(R.string.error_no_vincles_user), Snackbar.LENGTH_LONG,
                    R.drawable.icon_user_block, getString(R.string.add_new_user),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(MessageListActivity.this, NetworkActivity.class);
                            startActivity(intent);
                        }
                    });
        }

    }
}
