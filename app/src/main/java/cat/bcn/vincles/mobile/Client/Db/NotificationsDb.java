package cat.bcn.vincles.mobile.Client.Db;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Model.ChatMessageRest;
import cat.bcn.vincles.mobile.Client.Model.MeetingRealm;
import cat.bcn.vincles.mobile.Client.Model.NotificationRest;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import io.realm.NotificationRestRealmProxy;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;

public class NotificationsDb extends BaseDb {

    public final static String MEETING_REMINDER_NOTIFICATION_TYPE = "MEETING_REMINDER_NOTIFICATION_TYPE";
    public final static String MISSED_CALL_NOTIFICATION_TYPE = "MISSED_CALL_NOTIFICATION_TYPE";

    public NotificationsDb(Context context) {
        super(context);
    }

    @Override
    public void dropTable() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.where(NotificationRest.class).findAll().deleteAllFromRealm();
        realm.commitTransaction();
    }

    public NotificationRest findNotification(long id) {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(NotificationRest.class).equalTo("id", id).findFirst();
    }

    public ArrayList<NotificationRest> findAllNotifications() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<NotificationRest> notificationsList = realm.where(NotificationRest.class)
                .findAll();
        return new ArrayList<>(notificationsList);
    }

    public ArrayList<NotificationRest> findAllUnProcessedNotifications() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<NotificationRest> notificationsList = realm.where(NotificationRest.class)
                .equalTo("processed",false)
                .sort("creationTime", Sort.ASCENDING)
                .findAll();
        return new ArrayList<>(notificationsList);
    }

    public RealmResults<NotificationRest> findShownNotificationsAsync() {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(NotificationRest.class)
                .equalTo("processed",true)
                .equalTo("shouldBeShown",true)
                .sort("creationTime", Sort.DESCENDING)
                //.findAll();
                .findAllAsync();
    }

    public int findUnwatchedNotificationsNumber() {
        return Realm.getDefaultInstance()
                .where(NotificationRest.class)
                .equalTo("processed",true)
                .equalTo("shouldBeShown",true)
                .equalTo("watched",false)
                .findAll().size();
    }

    public void saveNotificationRestList(List<NotificationRest> notifications) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        for (NotificationRest notificationRest : notifications) {
            realm.copyToRealmOrUpdate(notificationRest);
        }
        realm.commitTransaction();
    }

    public void saveNotification(NotificationRest notificationRest) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(notificationRest);
        realm.commitTransaction();
    }

    public void saveMissedCallNotification(long notificationTime, int idUser) {
        NotificationRest notificationRest = new NotificationRest(
                new UserPreferences().notificationUpperIdGetAndSubtract(),
                NotificationsDb.MISSED_CALL_NOTIFICATION_TYPE, notificationTime,
                true, idUser, -1, -1,
                -1, -1, -1, "","", -1);
        notificationRest.setShouldBeShown(true);
        notificationRest.setWatched(false);

        saveNotification(notificationRest);
    }

    public int getNumberUnreadMissedCallNotifications(int userId) {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(NotificationRest.class)
                .equalTo("type", MISSED_CALL_NOTIFICATION_TYPE)
                .equalTo("idUser", userId)
                .equalTo("watched", false)
                .findAll().size();
    }

    public RealmResults<NotificationRest> getUnreadMissedCallNotifications() {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(NotificationRest.class)
                .equalTo("type", MISSED_CALL_NOTIFICATION_TYPE)
                .equalTo("watched", false)
                .findAll();
    }

    public RealmResults<NotificationRest> getMissedCallNotifications(int userId) {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(NotificationRest.class)
                .equalTo("type", MISSED_CALL_NOTIFICATION_TYPE)
                .equalTo("idUser", userId)
                .findAll();
    }

    public long getLastMissedCallTime(int userId) {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<NotificationRest> calls = realm.where(NotificationRest.class)
                .equalTo("type", MISSED_CALL_NOTIFICATION_TYPE)
                .equalTo("idUser", userId)
                .sort("creationTime", Sort.DESCENDING)
                .findAll();
        return calls.size() == 0 ? 0 : calls.get(0).getCreationTime();
    }

    public void saveNotificationCloseMeetingAlert(MeetingRealm meetingRealm) {
        NotificationRest notificationRest = new NotificationRest(
                new UserPreferences().notificationUpperIdGetAndSubtract(),
                MEETING_REMINDER_NOTIFICATION_TYPE, meetingRealm.getDate() - 60*60*1000,
                true, meetingRealm.getHostId(), -1, -1,
                -1, -1, meetingRealm.getId(), "","", -1);
        notificationRest.setShouldBeShown(true);
        notificationRest.setWatched(false);

        saveNotification(notificationRest);
    }

    public void setNotificationToProcessedShown(int notificationId, boolean setShown) {
        NotificationRest notification = findNotification(notificationId);
        if (notification != null) {
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            notification.setProcessed(true);
            notification.setShouldBeShown(setShown);
            realm.copyToRealmOrUpdate(notification);
            realm.commitTransaction();
        }
    }

    public void setNotificationUserName(int notificationId, String name) {
        NotificationRest notification = findNotification(notificationId);
        if (notification != null) {
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            notification.setUserName(name);
            realm.copyToRealmOrUpdate(notification);
            realm.commitTransaction();
        }
    }

    public void setNotificationShouldBeShown(int notificationId, boolean shouldBeShown) {
        NotificationRest notification = findNotification(notificationId);
        if (notification != null) {
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            notification.setShouldBeShown(shouldBeShown);
            realm.copyToRealmOrUpdate(notification);
            realm.commitTransaction();
        }
    }

    public void setNotificationUserUserName(int notificationId, int idUser, String userName) {
        NotificationRest notification = findNotification(notificationId);
        if (notification != null) {
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            notification.setIdUser(idUser);
            notification.setUserName(userName);
            realm.copyToRealmOrUpdate(notification);
            realm.commitTransaction();
        }
    }

    public void setNotificationUserId(int notificationId, int idUser) {
        NotificationRest notification = findNotification(notificationId);
        if (notification != null) {
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            notification.setIdUser(idUser);
            realm.copyToRealmOrUpdate(notification);
            realm.commitTransaction();
        }
    }

    public void setNotificationChatInfo(int notificationId, int idChat, String chatName) {
        NotificationRest notification = findNotification(notificationId);
        if (notification != null) {
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            notification.setIdChat(idChat);
            notification.setUserName(chatName);
            realm.copyToRealmOrUpdate(notification);
            realm.commitTransaction();
        }
    }

    public void setNotificationGroupInfo(int notificationId, String name, String photo) {
        NotificationRest notification = findNotification(notificationId);
        if (notification != null) {
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            notification.setDeletedGroupName(name);
            notification.setDeletedGroupPhoto(photo);
            realm.copyToRealmOrUpdate(notification);
            realm.commitTransaction();
        }
    }

    /**
     * Of all notifications of new message from a user, only the last one has to be shown.
     * Method to set all newMessage notifications to hidden except the one with id:notificationId
     *
     * @param notificationId    notification that will be set to shown
     * @param idChat            user sender of the message
     */
    public void setMessageNotificationsNotShownExceptId(String type, int notificationId, int idChat) {
        Realm realm = Realm.getDefaultInstance();
        String idChatField = type.equals("NEW_MESSAGE") ? "idUser" : "idChat";
        RealmResults<NotificationRest> notifications = realm.where(NotificationRest.class)
                .equalTo("type", type)
                .equalTo(idChatField, idChat)
                .findAll();

        if (notifications != null && notifications.size() > 0) {
            realm.beginTransaction();
            for (NotificationRest notification : notifications) {
                if (notification.getId() == notificationId) {
                    notification.setShouldBeShown(true);
                } else {
                    notification.setShouldBeShown(false);
                }
            }
            realm.commitTransaction();
        }
    }

    public long getLastNotificationTime() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<NotificationRest> notificationsList = realm.where(NotificationRest.class)
                .sort("creationTime", Sort.DESCENDING)
                .findAll();
        return (notificationsList == null || notificationsList.size() == 0) ? 0 :
                notificationsList.get(0).getCreationTime();
    }

    public void markAllAsRead() {
        ArrayList<NotificationRest> notifications = findAllNotifications();
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        for (NotificationRest notificationRest : notifications) {
            notificationRest.setWatched(true);
        }
        realm.commitTransaction();
    }


}
