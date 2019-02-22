package cat.bcn.vincles.mobile.Client.Db;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Db.Model.CircleRealm;
import cat.bcn.vincles.mobile.Client.Db.Model.CircleUserRealm;
import cat.bcn.vincles.mobile.Client.Db.Model.UserCircleRealm;
import cat.bcn.vincles.mobile.Client.Model.CircleUser;
import cat.bcn.vincles.mobile.Client.Model.GetUser;
import cat.bcn.vincles.mobile.Client.Model.Serializers.AddUser;
import cat.bcn.vincles.mobile.Client.Model.UserCircle;
import io.realm.Realm;
import io.realm.RealmResults;

public class UsersDb extends BaseDb {

    public UsersDb(Context context) {
        super(context);
    }

    @Override
    public void dropTable() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.where(GetUser.class).findAll().deleteAllFromRealm();
        realm.where(CircleUserRealm.class).findAll().deleteAllFromRealm();
        realm.where(UserCircleRealm.class).findAll().deleteAllFromRealm();
        realm.where(CircleRealm.class).findAll().deleteAllFromRealm();
        realm.commitTransaction();
    }

    public boolean userExists(int id) {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(GetUser.class).equalTo("id", id).findFirst() != null;
    }

    public GetUser findUser(int id) {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(GetUser.class).equalTo("id", id).findFirst();
    }

    public GetUser findUserAsync(int id) {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(GetUser.class).equalTo("id", id).findFirstAsync();
    }

    public CircleUserRealm findCircleUser(int id) { //user is vincles
        Realm realm = Realm.getDefaultInstance();
        return realm.where(CircleUserRealm.class).equalTo("userId", id).findFirst();
    }

    public UserCircleRealm findUserCircle(int id) {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(UserCircleRealm.class).equalTo("circleRealm.userId", id).findFirst();
    }

    public boolean deleteUserCircle(int id) {
        Realm realm = Realm.getDefaultInstance();
        UserCircleRealm userCircleRealm = realm.where(UserCircleRealm.class).equalTo("circleRealm.id", id).findFirst();
        if (userCircleRealm == null) return false;

        realm.beginTransaction();
        userCircleRealm.deleteFromRealm();
        realm.commitTransaction();
        return true;
    }

    public boolean deleteCircleUser(int id) {
        Realm realm = Realm.getDefaultInstance();
        CircleUserRealm circleUserRealm = realm.where(CircleUserRealm.class).equalTo("userId", id).findFirst();
        if (circleUserRealm == null) return false;

        realm.beginTransaction();
        circleUserRealm.deleteFromRealm();
        realm.commitTransaction();
        return true;
    }


    public ArrayList<GetUser> findAllGetUser() {
        Realm realm = Realm.getDefaultInstance();
        ArrayList<GetUser> getUserArrayList = new ArrayList<>();
        RealmResults<GetUser> userList = realm.where(GetUser.class)
                .findAll();
        for (GetUser getUser : userList) {
            getUserArrayList.add(getUser);
        }
        return getUserArrayList;
    }

    public void deleteUserCircleIfExists(final int id) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                RealmResults<UserCircleRealm> userCircle = realm.where(UserCircleRealm.class)
                        .equalTo( "circleRealm.userId", id).findAll();
                userCircle.deleteAllFromRealm();

                RealmResults<CircleUserRealm> circleUser = realm.where(CircleUserRealm.class)
                        .equalTo( "userId", id).findAll();
                circleUser.deleteAllFromRealm();
            }
        });
    }

    public ArrayList<GetUser> findAllCircleUser() { //user is vincles
        Realm realm = Realm.getDefaultInstance();
        ArrayList<GetUser> getUserArrayList = new ArrayList<>();
        RealmResults<CircleUserRealm> circleUserRealmResults = realm.where(CircleUserRealm.class)
                .findAll();
        GetUser getUser;
        for (CircleUserRealm circleUserRealm : circleUserRealmResults) {
            getUser = realm.where(GetUser.class).equalTo("id", circleUserRealm.getUserId()).findFirst();
            safeAddUserToList(getUserArrayList, getUser);
        }
        return getUserArrayList;
    }

    public ArrayList<GetUser> findAllUserCircle() {
        Realm realm = Realm.getDefaultInstance();
        ArrayList<GetUser> getUserArrayList = new ArrayList<>();
        RealmResults<UserCircleRealm> userCircleRealmRealmResults = realm.where(UserCircleRealm.class)
                .findAll();
        GetUser getUser;
        for (UserCircleRealm userCircleRealm : userCircleRealmRealmResults) {
            getUser = realm.where(GetUser.class).equalTo("id", userCircleRealm.getCircleRealm().getUserId()).findFirst();
            safeAddUserToList(getUserArrayList, getUser);
        }
        return getUserArrayList;
    }

    private void safeAddUserToList(ArrayList<GetUser> list, GetUser user) {
        if (list != null && user != null && !list.contains(user))
            list.add(user);
    }

    public CircleRealm getCircleRealm(int id) {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(CircleRealm.class).equalTo("id", id).findFirst();
    }

    public void saveGetUserIfNotExists(GetUser getUser) {
        if (!userExists(getUser.getId())) saveGetUser(getUser, true);
    }

    public void saveGetUser(GetUser getUser, boolean beginTransaction) {
        Realm realm = Realm.getDefaultInstance();
        if (beginTransaction) {
            realm.beginTransaction();
        }
        GetUser getUserRealm = findUser(getUser.getId());
        if (getUserRealm != null/* && getUserRealm.getIdContentPhoto() == getUser.getIdContentPhoto()*/) {
            getUser.setPhoto(getUserRealm.getPhoto());
            getUser.setNumberUnreadMessages(getUserRealm.getNumberUnreadMessages());
            getUser.setLastInteraction(getUserRealm.getLastInteraction());
        }
        realm.copyToRealmOrUpdate(getUser);
        if (beginTransaction) {
            realm.commitTransaction();
        }
    }

    public void updateUserName(int userId, String userName, String lastname) {
        Realm realm = Realm.getDefaultInstance();
        GetUser user = findUser(userId);
        if (user != null) {
            realm.beginTransaction();
            user.setName(userName);
            user.setLastname(lastname);
            realm.copyToRealmOrUpdate(user);
            realm.commitTransaction();
        }
    }

    public void addUser(AddUser addUser) {
        CircleUserRealm circleUser = new CircleUserRealm(addUser.getRelationship(),
                addUser.getUserVincles().getId());
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealm(circleUser);
        realm.copyToRealmOrUpdate(addUser.getUserVincles());
        realm.commitTransaction();
    }

    public void saveCircleUsers(List<CircleUser> circleUserList) {
        Realm realm = Realm.getDefaultInstance();
        CircleUserRealm circleUserRealm = null;
        for (CircleUser circleUser : circleUserList) {
            realm.beginTransaction();

            circleUserRealm = findCircleUser(circleUser.getUser().getId());
            if (circleUserRealm != null) {
                circleUserRealm.deleteFromRealm();
            }
            circleUserRealm = new CircleUserRealm(circleUser.getRelationship(), circleUser.getUser().getId());
            realm.copyToRealm(circleUserRealm);
            saveGetUser(circleUser.getUser(), false);

            realm.commitTransaction();
        }
    }

    public void saveUserCircles(List<UserCircle> userCircleList) {
        Realm realm = Realm.getDefaultInstance();
        UserCircleRealm userCircleRealm = null;
        CircleRealm circleRealm = null;
        for (UserCircle userCircle : userCircleList) {
            realm.beginTransaction();

            circleRealm = new CircleRealm(userCircle.getCircle().getId(), userCircle.getCircle().getUser().getId());
            userCircleRealm = findUserCircle(circleRealm.getId());
            if (userCircleRealm != null) {
                userCircleRealm.deleteFromRealm();
            }
            userCircleRealm = new UserCircleRealm(userCircle.getRelationship(), circleRealm);
            realm.copyToRealm(userCircleRealm);
            saveGetUser(userCircle.getCircle().getUser(), false);

            realm.commitTransaction();
        }
    }

    public void setPathAvatarToUser(int userID, String path) {
        Realm realm = Realm.getDefaultInstance();
        GetUser user = realm.where(GetUser.class)
                .equalTo("id", userID)
                .findFirst();
        if (user != null) {
            realm.beginTransaction();
            user.setPhoto(path);
            realm.commitTransaction();
        }
    }

    public String getUserAvatarPath(int userID) {
        Realm realm = Realm.getDefaultInstance();
        GetUser user = realm.where(GetUser.class)
                .equalTo("id", userID)
                .findFirst();

        if (user == null) return null;
        return user.getPhoto();
    }

    public void setMessagesInfo(int userId, int unreadMessages, int unreadMissedCalls, long lastInteraction) {
        Log.d("grpwatched","setMessagesInfo");
        Log.d("unrd","setMessageInfo uneMes:"+unreadMessages+", calls:"+unreadMissedCalls);
        Realm realm = Realm.getDefaultInstance();
        GetUser user = realm.where(GetUser.class)
                .equalTo("id", userId)
                .findFirst();
        if (user != null) {
            Log.d("unrd","setMessageInfo userNotNull, user:"+userId+" totalNum:"
                    +lastInteraction);
            realm.beginTransaction();
            user.setNumberUnreadMessages(unreadMessages+unreadMissedCalls);
            user.setLastInteraction(lastInteraction);
            realm.commitTransaction();
        } else {
            Log.d("unrd","setMessageInfo USER NULL, user:"+userId+" totalNum:"
                    +lastInteraction);
        }
    }

}
