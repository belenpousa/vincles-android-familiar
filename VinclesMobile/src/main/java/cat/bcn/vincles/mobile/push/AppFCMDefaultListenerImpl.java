/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.push;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.List;
import java.util.Locale;

import cat.bcn.vincles.lib.dao.NetworkDAOImpl;
import cat.bcn.vincles.lib.dao.TaskDAOImpl;
import cat.bcn.vincles.lib.push.VinclesPushListener;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.vo.FeedItem;
import cat.bcn.vincles.lib.vo.Message;
import cat.bcn.vincles.lib.vo.Network;
import cat.bcn.vincles.lib.vo.PushMessage;
import cat.bcn.vincles.lib.vo.Task;
import cat.bcn.vincles.lib.vo.User;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.activity.MainActivity;
import cat.bcn.vincles.mobile.activity.VinclesActivity;
import cat.bcn.vincles.mobile.activity.diary.DiaryDayDetailActivity;
import cat.bcn.vincles.mobile.activity.message.details.MessageDetailAudioActivity;
import cat.bcn.vincles.mobile.activity.message.details.MessageDetailImageActivity;
import cat.bcn.vincles.mobile.activity.message.details.MessageDetailVideoActivity;
import cat.bcn.vincles.mobile.activity.network.NetworkActivity;
import cat.bcn.vincles.mobile.activity.videocall.VideoCallIntroActivity;
import cat.bcn.vincles.mobile.model.AndroidCalendarModel;
import cat.bcn.vincles.mobile.model.FeedModel;
import cat.bcn.vincles.mobile.model.MainModel;
import cat.bcn.vincles.mobile.model.MessageModel;
import cat.bcn.vincles.mobile.model.NetworkModel;

public class AppFCMDefaultListenerImpl implements VinclesPushListener {
    Context context;
    FeedModel feedModel;
    MainModel mainModel;
    AndroidCalendarModel androidCalendarModel;
    private VinclesActivity actualActivity;

    public AppFCMDefaultListenerImpl(Context ctx) {
        context = ctx;
        mainModel = MainModel.getInstance();
        feedModel = FeedModel.getInstance();
        androidCalendarModel = AndroidCalendarModel.getInstance();
    }

    @Override
    public void onPushMessageReceived(final PushMessage pushMessage) {
        // DO WHATEVER NEED TO DO YOUR APP HERE
        Log.d(null, "GCM: LLEGAN A LA APP!!");

        Long id = pushMessage.getId();
        int mNotificationId = 0;
        if (id != null) {
            mNotificationId = id.intValue();
        }
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.vincles_app_name))
                .setAutoCancel(true);
        Intent resultIntent;
        PendingIntent resultPendingIntent;

        Task taskTemp; Message messageTemp;
        Log.d(null, "GCM PROCESS CONTENT:" + pushMessage.getRawDataJson());
        JsonObject json = null;
        if (pushMessage.getRawDataJson() != null)
            json = new JsonParser().parse(pushMessage.getRawDataJson()).getAsJsonObject();

        FeedItem feedItem = new FeedItem().fromPushMessage(pushMessage);

        switch (pushMessage.getType()) {
            case PushMessage.TYPE_USER_UPDATED:
                MainModel.getInstance().getFullUserInfo(new AsyncResponse() {
                    @Override
                    public void onSuccess(Object result) {
                        if (MainModel.getInstance().currentNetwork != null)
                            MainModel.getInstance().currentNetwork.userVincles = (User)result;
                    }

                    @Override
                    public void onFailure(Object error) {
                        String errorMessage = MainModel.getInstance().getErrorByCode(error);
                            Toast toast = Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }, pushMessage.getIdData());
                break;
            case PushMessage.TYPE_USER_UNLINKED:
                NetworkDAOImpl networkDAO = new NetworkDAOImpl();
                Network net = networkDAO.findByUserId(pushMessage.getIdData());

                feedModel.addItem(feedItem.setInfo(net.userVincles.alias));

                Intent i = new Intent(context, NetworkActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                resultIntent = i;
                resultPendingIntent =
                        PendingIntent.getActivity(
                                context,
                                0,
                                resultIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );

                if (mainModel.notifications) {
                    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
                        mBuilder =
                                new NotificationCompat.Builder(context)
                                        .setSmallIcon(R.drawable.icon_user)
                                        .setContentTitle(context.getString(R.string.notification_unlinked_title))
                                        .setContentText(context.getString(R.string.notification_unlinked_text, net.userVincles.alias))
                                        .setAutoCancel(true);
                    } else {
                        mBuilder =
                                new NotificationCompat.Builder(context)
                                        .setSmallIcon(R.drawable.icon_user)
                                        .setContentTitle(context.getString(R.string.notification_unlinked_title))
                                        .setContentText(context.getString(R.string.notification_unlinked_text, net.userVincles.alias))
                                        .setAutoCancel(true);
                    }
                    mBuilder.setContentIntent(resultPendingIntent);
                    mNotifyMgr.notify(mNotificationId, mBuilder.build());
                }

                // AUTO SELECT ANOTHER NETWORK OR NULL IF NONE
                if (MainModel.getInstance().currentNetwork.getId().longValue() == net.getId().longValue()) {
                    List<Network> networkList = NetworkModel.getInstance().getNetworkList();
                    MainModel.getInstance().currentNetwork = null;
                    for (Network netTemp: networkList) {
                        if (net.getId().longValue() != netTemp.getId().longValue()) {
                            NetworkModel.getInstance().changeNetwork(netTemp);
                            break;
                        }
                    }
                }

                Network.deleteAllUserRelatedInfo(net.userVincles.getId());
                networkDAO.removeNetwork(net);
                break;
            case PushMessage.TYPE_NEW_MESSAGE:
                messageTemp = Message.findById(Message.class, pushMessage.getIdData());
                resultIntent = new Intent(context, MessageDetailVideoActivity.class);
                resultIntent.putExtra("GCM_MESSAGE_ID", messageTemp.getId());
                resultIntent.putExtra(MainActivity.CHANGE_APP_NETWORK,
                        messageTemp.idUserFrom);   // NETWORKID == USERID

                mBuilder.setSmallIcon(R.drawable.icon_mensajes);

                // TEXT CONTENT
                String smallContent = "";
                String largeContent = VinclesConstants.getDateString(messageTemp.sendTime, context.getResources().getString(R.string.dateSmallformat),
                        new Locale(context.getResources().getString(R.string.locale_language), context.getResources().getString(R.string.locale_country)));

                if (DateUtils.isToday(messageTemp.sendTime.getTime()))
                    largeContent = context.getString(R.string.task_today);

                largeContent = largeContent.substring(0, 1).toUpperCase() + largeContent.substring(1) +  "\r\n";
                feedItem.setExtraId(messageTemp.idUserFrom);
                switch (messageTemp.metadataTipus) {
                    case VinclesConstants.RESOURCE_TYPE.AUDIO_MESSAGE:
                        smallContent += context.getString(R.string.message_receive_audio);
                        resultIntent.setClass(context, MessageDetailAudioActivity.class);
                        feedModel.addItem(feedItem.setType(FeedItem.FEED_TYPE_NEW_AUDIO_MESSAGE));
                        break;
                    case VinclesConstants.RESOURCE_TYPE.VIDEO_MESSAGE:
                        smallContent += context.getString(R.string.message_receive_video);
                        resultIntent.setClass(context, MessageDetailVideoActivity.class);
                        feedModel.addItem(feedItem.setType(FeedItem.FEED_TYPE_NEW_VIDEO_MESSAGE));
                        break;
                    case VinclesConstants.RESOURCE_TYPE.IMAGES_MESSAGE:
                        smallContent += context.getString(R.string.message_receive_image);
                        resultIntent.setClass(context, MessageDetailImageActivity.class);
                        feedModel.addItem(feedItem.setType(FeedItem.FEED_TYPE_NEW_IMAGE_MESSAGE));
                        break;
                    default:
                        return;
                }

                // SAVE MESSAGE ( IT IS HERE TO NOT STORE UNRECOGNIZED MESSAGES )
                MessageModel.getInstance().saveMessage(Message.fromJSON(json));

                if (mainModel.notifications) {
                    largeContent = largeContent + smallContent;
                    mBuilder.setContentText(smallContent);
                    mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(largeContent));
                    resultPendingIntent =
                            PendingIntent.getActivity(
                                    context,
                                    0,
                                    resultIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            );
                    mBuilder.setContentIntent(resultPendingIntent);

                    Notification notification = mBuilder.build();
                    mNotifyMgr.notify(mNotificationId, notification);
                }
                break;

            case PushMessage.TYPE_NEW_EVENT:
                taskTemp = new TaskDAOImpl().get(pushMessage.getIdData());

                // DON'T SHOW OTHERS NOTIFICATIONS
                if (taskTemp == null || taskTemp.owner == null) {
                    Log.d(getClass().getSimpleName(), "GCM NOTIFICATION FILTERED CAUSE IT IS NOT MINE");
                    return;
                }
                else if (mainModel.synchronizations)
                    androidCalendarModel.addOrUpdateAndroidCalendarEvent(taskTemp, context);

                resultIntent = new Intent(context, DiaryDayDetailActivity.class);
                resultIntent.putExtra("date", taskTemp.getDate().getTime());
                resultIntent.putExtra(MainActivity.CHANGE_APP_NETWORK,
                        taskTemp.network.userVincles.getId());   // NETWORKID == USERID

                resultPendingIntent =
                        PendingIntent.getActivity(
                                context,
                                0,
                                resultIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );

                if (mainModel.notifications) {
                    mBuilder =
                            new NotificationCompat.Builder(context)
                                    .setSmallIcon(R.drawable.icon_agenda)
                                    .setContentTitle(context.getString(R.string.task_new))
//                                .setContentText("blabla")
                                    .setAutoCancel(true);
                    mBuilder.setContentIntent(resultPendingIntent);

                    mNotifyMgr.notify(mNotificationId, mBuilder.build());
                }
                break;

            case PushMessage.TYPE_DELETED_EVENT:
                taskTemp = Task.fromJSON(json);
                // DON'T SHOW OTHERS NOTIFICATIONS
                if (taskTemp == null || taskTemp.owner == null) {
                    Log.d(getClass().getSimpleName(), "GCM NOTIFICATION FILTERED CAUSE IT IS NOT MINE");
                    return;
                }
                else if (mainModel.synchronizations)
                    androidCalendarModel.deleteAndroidCalendarEvent(taskTemp, context);

                feedModel.addItem(feedItem
                        .setFixedData(null, taskTemp.description, taskTemp.getDate().getTime())
                        .setExtraId(taskTemp.network.userVincles.getId()));

                resultIntent = new Intent(context, DiaryDayDetailActivity.class);
                resultIntent.putExtra("date", taskTemp.getDate().getTime());
                resultIntent.putExtra(MainActivity.CHANGE_APP_NETWORK,
                        taskTemp.network.userVincles.getId());   // NETWORKID == USERID

                resultPendingIntent =
                        PendingIntent.getActivity(
                                context,
                                0,
                                resultIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );

                if (mainModel.notifications) {
                    mBuilder =
                            new NotificationCompat.Builder(context)
                                    .setSmallIcon(R.drawable.icon_agenda)
                                    .setContentTitle(context.getString(R.string.task_deleted))
//                                .setContentText("blabla")
                                    .setAutoCancel(true);
                    mBuilder.setContentIntent(resultPendingIntent);

                    mNotifyMgr.notify(mNotificationId, mBuilder.build());
                }
                break;

            case PushMessage.TYPE_EVENT_UPDATED:
                taskTemp = new TaskDAOImpl().get(pushMessage.getIdData());
                // DON'T SHOW OTHERS NOTIFICATIONS
                if (taskTemp == null || taskTemp.owner == null) {
                    Log.d(getClass().getSimpleName(), "GCM NOTIFICATION FILTERED CAUSE IT IS NOT MINE");
                    return;
                }
                else if (mainModel.synchronizations)
                    androidCalendarModel.addOrUpdateAndroidCalendarEvent(taskTemp, context);

                feedModel.addItem(feedItem
                        .setFixedData(null, taskTemp.description, taskTemp.getDate().getTime())
                        .setExtraId(taskTemp.network.userVincles.getId()));

                resultIntent = new Intent(context, DiaryDayDetailActivity.class);
                resultIntent.putExtra("date", taskTemp.getDate().getTime());
                resultIntent.putExtra(MainActivity.CHANGE_APP_NETWORK,
                        taskTemp.network.userVincles.getId());   // NETWORKID == USERID

                resultPendingIntent =
                        PendingIntent.getActivity(
                                context,
                                0,
                                resultIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );

                if (mainModel.notifications) {
                    mBuilder =
                            new NotificationCompat.Builder(context)
                                    .setSmallIcon(R.drawable.icon_agenda)
                                    .setContentTitle(context.getString(R.string.task_updated))
//                                .setContentText("blabla")
                                    .setAutoCancel(true);
                    mBuilder.setContentIntent(resultPendingIntent);

                    mNotifyMgr.notify(mNotificationId, mBuilder.build());
                }
                break;

            case PushMessage.TYPE_EVENT_ACCEPTED:
                taskTemp = new TaskDAOImpl().get(pushMessage.getIdData());
                // DON'T SHOW OTHERS NOTIFICATIONS
                if (taskTemp == null || taskTemp.owner == null) {
                    Log.d(getClass().getSimpleName(), "GCM NOTIFICATION FILTERED CAUSE IT IS NOT MINE");
                    return;
                }
                else if (mainModel.synchronizations)
                    androidCalendarModel.addOrUpdateAndroidCalendarEvent(taskTemp, context);

                feedModel.addItem(feedItem
                        .setFixedData(null, taskTemp.description, taskTemp.getDate().getTime())
                        .setExtraId(taskTemp.network.userVincles.getId()));

                resultIntent = new Intent(context, DiaryDayDetailActivity.class);
                resultIntent.putExtra("date", taskTemp.getDate().getTime());
                resultIntent.putExtra(MainActivity.CHANGE_APP_NETWORK,
                        taskTemp.network.userVincles.getId());   // NETWORKID == USERID

                resultPendingIntent =
                        PendingIntent.getActivity(
                                context,
                                0,
                                resultIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );

                if (mainModel.notifications) {
                    mBuilder =
                            new NotificationCompat.Builder(context)
                                    .setSmallIcon(R.drawable.icon_agenda)
                                    .setContentTitle(context.getString(R.string.task_updated))
                                    .setContentText(context.getString(R.string.task_accepted))
                                    .setAutoCancel(true);
                    ;
                    mBuilder.setContentIntent(resultPendingIntent);

                    mNotifyMgr.notify(mNotificationId, mBuilder.build());
                }
                break;

            case PushMessage.TYPE_EVENT_REJECTED:
                taskTemp = new TaskDAOImpl().get(pushMessage.getIdData());
                // DON'T SHOW OTHERS NOTIFICATIONS
                if (taskTemp == null || taskTemp.owner == null) {
                    Log.d(getClass().getSimpleName(), "GCM NOTIFICATION FILTERED CAUSE IT IS NOT MINE");
                    return;
                }
                else if (mainModel.synchronizations)
//                    androidCalendarModel.deleteAndroidCalendarEvent(taskTemp, context);
                    androidCalendarModel.addOrUpdateAndroidCalendarEvent(taskTemp, context);

                feedModel.addItem(feedItem
                        .setFixedData(null, taskTemp.description, taskTemp.getDate().getTime())
                        .setExtraId(taskTemp.network.userVincles.getId()));

                resultIntent = new Intent(context, DiaryDayDetailActivity.class);
                resultIntent.putExtra("date", taskTemp.getDate().getTime());
                resultIntent.putExtra(MainActivity.CHANGE_APP_NETWORK,
                        taskTemp.network.userVincles.getId());   // NETWORKID == USERID

                resultPendingIntent =
                        PendingIntent.getActivity(
                                context,
                                0,
                                resultIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );

                if (mainModel.notifications) {
                    mBuilder =
                            new NotificationCompat.Builder(context)
                                    .setSmallIcon(R.drawable.icon_agenda)
                                    .setContentTitle(context.getString(R.string.task_updated))
                                    .setContentText(context.getString(R.string.task_rejected))
                                    .setAutoCancel(true);
                    ;
                    mBuilder.setContentIntent(resultPendingIntent);

                    mNotifyMgr.notify(mNotificationId, mBuilder.build());
                }
                break;
            case PushMessage.TYPE_STRENGTH_CONNECTION_LOW:
                if (actualActivity != null && actualActivity instanceof VideoCallIntroActivity) {
                    actualActivity.checkStrengthSignalStatus();
                }
                break;
            case PushMessage.TYPE_STRENGTH_CONNECTION_OK:
                if (actualActivity != null) {
                    actualActivity.removeStrengthSignalStatus();
                }
                break;
        }
    }

    @Override
    public void onPushMessageError(long idPush, Throwable t) {
        Log.d(null, "GCM: ERROR TRYING TO ACCESS PUSHID: " + idPush);
    }


    private Bitmap getCircleBitmap(Bitmap bitmap) {
        int side = bitmap.getWidth();
        if (side > bitmap.getHeight()) side = bitmap.getHeight();
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = Color.RED;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, side, side);
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        bitmap.recycle();

        return output;
    }

    public void setActualActivity(VinclesActivity activity) {
        actualActivity = activity;
    }
}
