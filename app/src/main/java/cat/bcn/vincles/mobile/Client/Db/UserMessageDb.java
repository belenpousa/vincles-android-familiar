package cat.bcn.vincles.mobile.Client.Db;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Db.Model.CircleRealm;
import cat.bcn.vincles.mobile.Client.Db.Model.CircleUserRealm;
import cat.bcn.vincles.mobile.Client.Db.Model.UserCircleRealm;
import cat.bcn.vincles.mobile.Client.Model.ChatMessageRest;
import cat.bcn.vincles.mobile.Client.Model.CircleUser;
import cat.bcn.vincles.mobile.Client.Model.ChatMessageRest;
import cat.bcn.vincles.mobile.Client.Model.UserCircle;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;

public class UserMessageDb extends BaseDb {

    public UserMessageDb(Context context) {
        super(context);
    }

    @Override
    public void dropTable() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.where(ChatMessageRest.class).findAll().deleteAllFromRealm();
        realm.commitTransaction();
    }

    public ChatMessageRest findMessage(long id) {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(ChatMessageRest.class).equalTo("id", id).findFirst();
    }


    public ArrayList<ChatMessageRest> findAllChatMessageRest() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<ChatMessageRest> messagesList = realm.where(ChatMessageRest.class)
                .findAll();
        return new ArrayList<>(messagesList);
    }

    public ArrayList<ChatMessageRest> findAllMessagesBetween(int senderId, int receiverId) {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<ChatMessageRest> messagesList = realm.where(ChatMessageRest.class)
                .beginGroup()
                    .equalTo("idUserFrom",senderId)
                    .equalTo("idUserTo",receiverId)
                .endGroup()
                .or()
                .beginGroup()
                    .equalTo("idUserFrom",receiverId)
                    .equalTo("idUserTo",senderId)
                .endGroup()
                .sort("sendTime", Sort.DESCENDING)
                .findAll();
        return new ArrayList<>(messagesList);
    }

    public ArrayList<ChatMessageRest> getUnreadMessagesReceived(int idMe, int idOther) {
        Realm realm = Realm.getDefaultInstance();
        return new ArrayList<>(realm.where(ChatMessageRest.class)
                .equalTo("idUserFrom", idOther)
                .equalTo("idUserTo", idMe)
                .equalTo("watched", false)
                .sort("sendTime", Sort.DESCENDING)
                .findAll());
    }

    public int getNumberUnreadMessagesReceived(int idMe, int idOther) {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(ChatMessageRest.class)
                .equalTo("idUserFrom", idOther)
                .equalTo("idUserTo", idMe)
                .equalTo("watched", false)
                .findAll().size();
    }

    public int getTotalNumberMessages(int idMe, int idOther) {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(ChatMessageRest.class)
                .beginGroup()
                .equalTo("idUserFrom",idMe)
                .equalTo("idUserTo",idOther)
                .endGroup()
                .or()
                .beginGroup()
                .equalTo("idUserFrom",idOther)
                .equalTo("idUserTo",idMe)
                .endGroup()
                .findAll().size();
    }

    public long getLastMessage(int idMe, int idOther) {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<ChatMessageRest> messageRests = realm.where(ChatMessageRest.class)
                .beginGroup()
                .equalTo("idUserFrom",idMe)
                .equalTo("idUserTo",idOther)
                .endGroup()
                .or()
                .beginGroup()
                .equalTo("idUserFrom",idOther)
                .equalTo("idUserTo",idMe)
                .endGroup()
                .sort("sendTime", Sort.DESCENDING)
                .findAll();
        ChatMessageRest chatMessageRest = messageRests.size() > 0 ? messageRests.get(0) : null;
        return chatMessageRest == null ? 0 : chatMessageRest.getSendTime();
    }


    public void saveChatMessageRest(ChatMessageRest chatMessageRest) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        realm.copyToRealmOrUpdate(chatMessageRest);
        realm.commitTransaction();
    }

    public void saveChatMessageRestList(List<ChatMessageRest> messages) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        for (ChatMessageRest messageRest : messages) {
            realm.copyToRealmOrUpdate(messageRest);
        }
        realm.commitTransaction();


    }

    public void setMessageFile(int contentID, String path, long messageID, String metadata) {
        ChatMessageRest message = findMessage(messageID);
        if (message != null) {
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            RealmList<Integer> contentIDs = message.getIdAdjuntContents();
            for (int i = 0; i<contentIDs.size(); i++) {
                if (contentIDs.get(i) == contentID) {
                    RealmList<String> paths = message.getPathsAdjuntContents();
                    if (paths.size() > 0) paths.remove(i);
                    paths.add(i, path);
                    RealmList<String> metadatas = message.getMetadataAdjuntContents();
                    if (metadatas.size() > 0) metadatas.remove(i);
                    metadatas.add(i, metadata);
                    break;
                }
            }
            realm.copyToRealmOrUpdate(message);
            realm.commitTransaction();
        }
    }


    public void setMessageWatched(long id) {
        Realm realm = Realm.getDefaultInstance();
        ChatMessageRest chatMessageRest = realm.where(ChatMessageRest.class)
                .equalTo("id", id).findFirst();
        if (chatMessageRest != null) {
            realm.beginTransaction();
            chatMessageRest.setWatched(true);
            realm.commitTransaction();
        }
    }



    /*public String ChatMessageRestAvatarPath(int userID) {
        Realm realm = Realm.getDefaultInstance();
        ChatMessageRest user = realm.where(ChatMessageRest.class)
                .equalTo("id", userID)
                .findFirst();

        if (user == null) return null;
        return user.getPhoto();
    }*/

}
