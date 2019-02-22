package cat.bcn.vincles.mobile.Client.Model;

import com.google.gson.annotations.JsonAdapter;

import java.util.List;

import cat.bcn.vincles.mobile.Client.Model.Serializers.ChatMessageRestListDeserializer;
import cat.bcn.vincles.mobile.Client.Model.Serializers.NotificationsListDeserializer;

@JsonAdapter(NotificationsListDeserializer.class)
public class NotificationsRestList {

    List<NotificationRest> notificationsList;

    public NotificationsRestList(List<NotificationRest> notificationsList) {
        this.notificationsList = notificationsList;
    }

    public List<NotificationRest> getNotificationsList() {
        return notificationsList;
    }

    public void setNotificationsList(List<NotificationRest> notificationsList) {
        this.notificationsList = notificationsList;
    }
}
