package cat.bcn.vincles.mobile.Client.Db;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Model.ChatMessageRest;
import cat.bcn.vincles.mobile.Client.Model.GroupMessageRest;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;

public class GroupMessageDb extends BaseDb {

    public GroupMessageDb(Context context) {
        super(context);
    }

    @Override
    public void dropTable() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.where(GroupMessageRest.class).findAll().deleteAllFromRealm();
        realm.where(ChatMessageRest.class).findAll().deleteAllFromRealm();
        realm.commitTransaction();
    }

    public GroupMessageRest findMessage(long id) {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(GroupMessageRest.class).equalTo("id", id).findFirst();
    }


    public ArrayList<GroupMessageRest> findAllGroupMessagesRest() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<GroupMessageRest> messagesList = realm.where(GroupMessageRest.class)
                .findAll();
        return new ArrayList<>(messagesList);
    }

    public ArrayList<GroupMessageRest> findAllMessagesForGroup(int groupId) {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<GroupMessageRest> messagesList = realm.where(GroupMessageRest.class)
                .equalTo("idChat",groupId)
                .sort("sendTime", Sort.DESCENDING)
                .findAll();
        return new ArrayList<>(messagesList);
    }

    public ArrayList<GroupMessageRest> findAllMessagesForGroupOlderThan(int groupId, long date) {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<GroupMessageRest> messagesList = realm.where(GroupMessageRest.class)
                .equalTo("idChat",groupId)
                .lessThanOrEqualTo("sendTime", date)
                .findAll();
        return new ArrayList<>(messagesList);
    }


    public void saveGroupMessageRest(GroupMessageRest groupMessageRest) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        realm.copyToRealmOrUpdate(groupMessageRest);
        realm.commitTransaction();
    }

    public void saveGroupMessageRestList(List<GroupMessageRest> messages) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        for (GroupMessageRest messageRest : messages) {
            realm.copyToRealmOrUpdate(messageRest);
        }
        realm.commitTransaction();
    }

    public void setGroupMessageListWatched(int idChat, long date, long dateOfLastMessage) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        ArrayList<GroupMessageRest> messages = dateOfLastMessage != -1 ?
                findAllMessagesForGroupOlderThan(idChat, dateOfLastMessage)
                : findAllMessagesForGroup(idChat);
        for (GroupMessageRest messageRest : messages) {
            Log.d("grpwatched","setGroupMessageListWatched, sendTime:"+messageRest.getSendTime()
                    +", Date:"+date);
            messageRest.setWatched(messageRest.getSendTime() < date);
        }
        realm.commitTransaction();
    }

    public void setGroupMessageListWatchedTrue(int idChat) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        ArrayList<GroupMessageRest> messages = findAllMessagesForGroup(idChat);
        for (GroupMessageRest messageRest : messages) {
            messageRest.setWatched(true);
        }
        realm.commitTransaction();
    }

    public void setMessageFile(int contentID, String path, long messageID, String metadata) {
        GroupMessageRest message = findMessage(messageID);
        if (message != null) {
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            message.setPathContent(path);
            message.setMetadataContent(metadata);
            realm.copyToRealmOrUpdate(message);
            realm.commitTransaction();
        }
    }

    public int getNumberUnreadMessagesReceived(int idMe, int idOther) {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(GroupMessageRest.class)
                .equalTo("idChat", idOther)
                .equalTo("watched", false)
                .notEqualTo("idUserSender", idMe)
                .findAll().size();
    }

    public int getTotalNumberMessages(int idChat) {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(GroupMessageRest.class)
                .equalTo("idChat",idChat)
                .findAll().size();
    }

}
