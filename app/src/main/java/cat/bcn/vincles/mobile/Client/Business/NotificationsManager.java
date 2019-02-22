package cat.bcn.vincles.mobile.Client.Business;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.HashMap;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Business.Firebase.FirebaseListenerService;
import cat.bcn.vincles.mobile.Client.Db.NotificationsDb;
import cat.bcn.vincles.mobile.Client.Model.NotificationRest;
import cat.bcn.vincles.mobile.Client.Model.NotificationsRestList;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Client.Requests.BaseRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetNotificationsRequest;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.UI.FragmentManager.MainFragmentManagerActivity;
import cat.bcn.vincles.mobile.Utils.MyApplication;

public class NotificationsManager extends Service implements GetNotificationsRequest.OnResponse,
        NotificationProcessor.NotificationProcessed {


    public static final String NOTIFICATION_PROCESSED_BROADCAST = "NOTIFICATION_PROCESSED_BROADCAST";

    private NotificationProcessor notificationProcessor;

    private boolean stopProcessing = false;

    UserPreferences preferences;
    NotificationsDb notificationsDb;
    long lastTime;
    long lastStoredTime;

    List<NotificationRest> unprocessedNotifications;
    int currentNotificationProcessing = 0;
    Resources resources;

    boolean isFirstStart = true;
    boolean shouldRequestNotificationsAgain = false;

    public NotificationsManager(){}


    @Override
    public void onCreate() {
        // initialize Environment class with Application context
        // necessary to process notifications after killing the app
        new cat.bcn.vincles.mobile.Client.Enviroment.Environment(this.getApplicationContext());
        
        preferences = new UserPreferences();
        this.notificationsDb = new NotificationsDb(this);
        this.resources = getResources();
        LocalBroadcastManager.getInstance(this).registerReceiver(logoutBroadcastReceiver,
                new IntentFilter(MainFragmentManagerActivity.LOGOUT_BROADCAST));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance =  NotificationManager.IMPORTANCE_MIN;
            String channelId = "CHANNEL_ID_NOTIFICATIONS";
            NotificationChannel channel = new NotificationChannel(channelId,
                    resources.getString(R.string.notifications_channel_notifications_name), importance);
            channel.setDescription(resources.getString(R.string.notifications_channel_notifications_description));
            NotificationManager notificationManager = MyApplication.getAppContext()
                    .getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            Intent intent = new Intent(MyApplication.getAppContext(), MainFragmentManagerActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(MyApplication.getAppContext(),
                    3456773, intent, 0);

            String text = resources.getString(R.string.notifications_processing_notifications);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                    MyApplication.getAppContext(), channelId)
                    .setSmallIcon(R.drawable.ic_notification_small)
                    .setContentText(text)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                    .setColor(resources.getColor(R.color.colorPrimary))
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent);
            startForeground(1, mBuilder.build());
        }
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (isFirstStart) {
            isFirstStart = false;
            shouldRequestNotificationsAgain = true;
            requestNotifications();
        } else {
            shouldRequestNotificationsAgain = true;
            requestNotifications();
        }
        return Service.START_NOT_STICKY;
    }

    public void requestNotifications() {
        lastStoredTime = preferences.getLastDownloadedNotification();
        GetNotificationsRequest getNotificationsRequest = new GetNotificationsRequest(null,
                lastStoredTime+1, 0);
        getNotificationsRequest.addOnOnResponse(this);
        getNotificationsRequest.doRequest(preferences.getAccessToken());
        Log.d("notman","notif manager, req not");
    }

    @Override
    public void onResponseGetNotificationsRequest(NotificationsRestList notificationsRestList) {
        boolean done = true;
        if (notificationsRestList != null) {
            List<NotificationRest> notificationsList = notificationsRestList.getNotificationsList();
            if (notificationsList != null && notificationsList.size() > 0) {
                Log.d("firebaseToken", "Notification manager, list not 0");
                notificationsDb.saveNotificationRestList(notificationsRestList.getNotificationsList());
                lastTime = notificationsList.get(notificationsList.size()-1).getCreationTime();

                /*String a = "";
                for (NotificationRest notificationRest : notificationsList) {
                    a = a+notificationRest.getCreationTime()+", ";
                }*/
                //Log.d("notman","notif id:"+notificationsList.get(0).getId()+" times:"+a);
                Log.d("notman","notif id:"+notificationsList.get(0).getId()+" size:"+notificationsList.size());
                if (notificationsList.size() >= 10) {
                    GetNotificationsRequest getNotificationsRequest = new GetNotificationsRequest(null,
                            lastStoredTime+1, lastTime);
                    getNotificationsRequest.addOnOnResponse(this);
                    getNotificationsRequest.doRequest(preferences.getAccessToken());
                    done = false;
                }
            }
        }
        if (done) onDoneDownloadingNotifications();
    }

    @Override
    public void onFailureGetNotificationsRequest(Object error) {
        Log.d("callvid", "onFailureGetNotificationsRequest:"+error);
        Log.d("notman","notif error:"+error);
        if (shouldRequestNotificationsAgain) {
            shouldRequestNotificationsAgain = false;
            requestNotifications();
        } else {
            stopSelf();
        }
    }

    private void onDoneDownloadingNotifications() {
        long lastNotificationTime = notificationsDb.getLastNotificationTime();
        if (lastNotificationTime != 0) preferences.setLastDownloadedNotification(lastNotificationTime);

        unprocessedNotifications = notificationsDb.findAllUnProcessedNotifications();
        Log.d("msgnot","onDoneDownloadingNotifications, size:"+unprocessedNotifications.size());

        if (!stopProcessing && (unprocessedNotifications.size() != 0 && currentNotificationProcessing < unprocessedNotifications.size())) {
            processNotification();
        } else {
            unprocessedNotifications.clear();
            currentNotificationProcessing = 0;
            onDoneProcessingNotifications();
        }


    }

    private void onDoneProcessingNotifications() {
        if (shouldRequestNotificationsAgain) {
            shouldRequestNotificationsAgain = false;
            if (!stopProcessing) requestNotifications();
            stopSelf();
        }
    }

    private void processNotification() {
        Log.d("gdp","notifmanager processNotification");
        if (stopProcessing || unprocessedNotifications.size() == 0 ||
                currentNotificationProcessing >= unprocessedNotifications.size()) {
            unprocessedNotifications.clear();
            currentNotificationProcessing = 0;

            onDoneProcessingNotifications();
            return; //todo notify finished?
        }

        NotificationRest notificationRest = unprocessedNotifications.get(currentNotificationProcessing);
        String type = notificationRest.getType();
        Log.d("callvid", "processNotification type:"+type);

        Bundle data = null;
        switch (type) {
            case "NEW_MESSAGE":
                data = new Bundle();
                data.putInt("idMessage", notificationRest.getIdMessage());
                notificationProcessor = new NotificationProcessor(notificationRest.getId(), type,
                        data, notificationsDb, null, this, resources);
                notificationProcessor.processNotification();
                break;
            case "NEW_CHAT_MESSAGE":
                data = new Bundle();
                data.putInt("idMessage", notificationRest.getIdChatMessage());
                data.putInt("chatId", notificationRest.getIdChat());
                notificationProcessor = new NotificationProcessor(notificationRest.getId(), type,
                        data, notificationsDb, null, this, resources);
                notificationProcessor.processNotification();
                break;
            case "USER_UPDATED":
            case "USER_LINKED":
            case "USER_UNLINKED":
            case "USER_LEFT_CIRCLE":
                data = new Bundle();
                data.putInt("idUser", notificationRest.getIdUser());
                notificationProcessor = new NotificationProcessor(notificationRest.getId(), type,
                        data, notificationsDb, null, this, resources);
                notificationProcessor.processNotification();
                break;
            case "ADDED_TO_GROUP":
            case "GROUP_UPDATED":
            case "REMOVED_FROM_GROUP":
                data = new Bundle();
                data.putInt("idGroup", notificationRest.getIdGroup());
                notificationProcessor = new NotificationProcessor(notificationRest.getId(), type,
                        data, notificationsDb, null, this, resources);
                notificationProcessor.processNotification();
                break;
            case "NEW_USER_GROUP":
            case "REMOVED_USER_GROUP":
                data = new Bundle();
                data.putInt("idUser", notificationRest.getIdUser());
                data.putInt("idGroup", notificationRest.getIdGroup());
                notificationProcessor = new NotificationProcessor(notificationRest.getId(), type,
                        data, notificationsDb, null, this, resources);
                notificationProcessor.processNotification();
                break;
            case "REMEMBER_MEETING_INVITATION_EVENT":
            case "INVITATION_SENDED":
                notificationsDb.setNotificationToProcessedShown(notificationRest.getId(), false);
                break;

            case "MEETING_ACCEPTED_EVENT":
            case "MEETING_REJECTED_EVENT":
            case "MEETING_INVITATION_DELETED_EVENT":
                data = new Bundle();
                data.putInt("idUser", notificationRest.getIdUser());
            case "MEETING_INVITATION_EVENT":
            case "MEETING_CHANGED_EVENT":
            case "MEETING_INVITATION_ADDED_EVENT":
            case "MEETING_INVITATION_REVOKE_EVENT":
            case "MEETING_DELETED_EVENT":
                if (data == null) data = new Bundle();
                data.putInt("idMeeting", notificationRest.getIdMeeting());
                notificationProcessor = new NotificationProcessor(notificationRest.getId(), type,
                        data, notificationsDb, null, this, resources);
                notificationProcessor.processNotification();
                break;
            case "INCOMING_CALL":
                Log.d("callvid", "notif Manager INCOMING_CALL");
                data = new Bundle();
                data.putInt("idUser", notificationRest.getIdUser());
                data.putString("idRoom", notificationRest.getIdRoom());
                data.putLong("notificationTime", notificationRest.getCreationTime());
                notificationProcessor = new NotificationProcessor(notificationRest.getId(), type,
                        data, notificationsDb, null, this, resources);
                notificationProcessor.processNotification();
                break;
            case "ERROR_IN_CALL":
                Log.d("cllerr", "notifManager error in call");
                data = new Bundle();
                data.putInt("idUser", notificationRest.getIdUser());
                data.putString("idRoom", notificationRest.getIdRoom());
                data.putLong("notificationTime", notificationRest.getCreationTime());
                notificationProcessor = new NotificationProcessor(notificationRest.getId(), type,
                        data, notificationsDb, null, this, resources);
                notificationProcessor.processNotification();
                break;
            case "GROUP_USER_INVITATION_CIRCLE":
                data = new Bundle();
                data.putString("code", notificationRest.getCode());
                data.putInt("idUser", notificationRest.getIdUser());
                Log.d("gdp","notifmanager GROUP_USER_INVITATION_CIRCLE");
                notificationProcessor = new NotificationProcessor(notificationRest.getId(), type,
                        data, notificationsDb, null, this, resources);
                notificationProcessor.processNotification();
                break;
            case "CONTENT_ADDED_TO_GALLERY":
                data = new Bundle();
                data.putInt("idGalleryContent", notificationRest.getIdGalleryContent());
                notificationProcessor = new NotificationProcessor(notificationRest.getId(), type,
                        data, notificationsDb, null, this, resources);
                notificationProcessor.processNotification();
                break;
            default:
                currentNotificationProcessing++;
                processNotification();
                break;
        }
    }


    @Override
    public void onNotificationProcessed(Bundle bundle) {
        if (!stopProcessing) {

            if (bundle != null) {
                Intent intent = new Intent(NOTIFICATION_PROCESSED_BROADCAST);
                intent.putExtra("bundle", bundle);
                LocalBroadcastManager.getInstance(MyApplication.getAppContext()).sendBroadcast(intent);
            }

            currentNotificationProcessing++;
        }

        processNotification();
    }

    @Override
    public void onNotificationFailure() {
        //stop processing. On next notification it will be tried again
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    /*public interface NotificationProcessed {
        public void onNotificationProcessed(@NonNull Bundle bundle);
    }*/

    private BroadcastReceiver logoutBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            stopProcessing = true;
            if (notificationProcessor != null) {
                notificationProcessor.cancelProcessing();
            }
        }
    };

}
