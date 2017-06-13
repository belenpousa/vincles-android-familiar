/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.push;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import cat.bcn.vincles.lib.push.CommonVinclesGcmHelper;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.vo.FeedItem;
import cat.bcn.vincles.lib.vo.PushMessage;
import cat.bcn.vincles.mobile.activity.videocall.VideoConferenceCallActivity;
import cat.bcn.vincles.mobile.model.FeedModel;
import cat.bcn.vincles.mobile.model.MainModel;
import cat.bcn.vincles.mobile.util.VinclesMobileConstants;

public class VinclesFcmListenerService extends FirebaseMessagingService {
    private static final String TAG = "VinclesGcmListenerSvc";

    /**
     * Called when message is received.
     *
     * @param message Data Map containing message data as key/value pairs.
     */
    @Override
    public void onMessageReceived(final RemoteMessage message){
        Log.d(TAG, "GCM From: " + message.getFrom());
        final MainModel mainModel = MainModel.getInstance();
        try {
            String accessToken = mainModel.getAccessToken();
            if (accessToken == null) {
                mainModel.initialize(this, true);
                mainModel.login(new AsyncResponse() {
                    @Override
                    public void onSuccess(Object result) {
                        String accessToken = mainModel.getAccessToken();
                        try {
                            CommonVinclesGcmHelper.setPushListener(mainModel.getPushListener());
                            handleReceived(message.getData(), accessToken);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Object error) {

                    }
                }, mainModel.currentUser.username + VinclesConstants.LOGIN_SUFFIX,
                mainModel.getPassword(mainModel.currentUser));

            } else {
                handleReceived(message.getData(), accessToken);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleReceived(Map data, String accessToken) {
        String type = (String)data.get("push_notification_type");
        if (type != null) {
            if (type.equals(PushMessage.TYPE_INCOMING_CALL)) {
                long userId = Long.parseLong(String.valueOf(data.get("idUser")));
                // MORE THAN 10 SECONDS BETWEEN SEND AND RECEIVE NOTIFICATION MEANS LOST CALL
                if (System.currentTimeMillis() - Long.parseLong(String.valueOf(data.get("push_notification_time"))) > VinclesMobileConstants.VC_NOTIFICATION_TIMEOUT_MS) {
                    FeedModel.getInstance().addLostCall(this, userId);
                    // FAKE A PUSH TO REFRESH FEED IF LOST CALL
                    PushMessage pushMessage = new PushMessage();
                    pushMessage.setId(0L);              // Fake id required but will not be used
                    pushMessage.setType("REFRESH");
                    CommonVinclesGcmHelper.getPushListener().onPushMessageReceived(pushMessage);
                } else {
                    String idRoom = (String)data.get("idRoom");
                    VideoConferenceCallActivity.startActivityForIncomingCall(this, userId, idRoom);
                }
            }
        } else {
            try {
                MainModel.getInstance().checkNewNotifications();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}