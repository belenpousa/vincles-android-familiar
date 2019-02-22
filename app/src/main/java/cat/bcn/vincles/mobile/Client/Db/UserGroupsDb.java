package cat.bcn.vincles.mobile.Client.Db;


import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Db.Model.GroupRealm;
import cat.bcn.vincles.mobile.Client.Db.Model.UserGroupRealm;
import cat.bcn.vincles.mobile.Client.Model.Dynamizer;
import cat.bcn.vincles.mobile.Client.Model.GetUser;
import cat.bcn.vincles.mobile.Client.Model.UserGroup;
import cat.bcn.vincles.mobile.Utils.OtherUtils;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class UserGroupsDb extends BaseDb {

    Context context;

    public UserGroupsDb(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public void dropTable() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.where(UserGroupRealm.class).findAll().deleteAllFromRealm();
        realm.where(GroupRealm.class).findAll().deleteAllFromRealm();
        realm.where(Dynamizer.class).findAll().deleteAllFromRealm();
        realm.commitTransaction();
    }

    public boolean checkIfDynamizerShouldBeShown(int dynamizerId) {
        ArrayList<GroupRealm> groups = findAllGroupRealm();
        for (GroupRealm group : groups) {
            if (group.getIdDynamizer() == dynamizerId) return true;
        }
        return false;
    }

    public void deleteGroup(final int groupID) {
        setShouldShowGroup(groupID, false);
        /*Realm realm = Realm.getDefaultInstance();

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                RealmResults<GroupRealm> result = realm.where(GroupRealm.class)
                        .equalTo( "id", groupID).findAll();
                result.deleteAllFromRealm();
            }
        });*/
    }

    public void setShouldShowGroup(final int groupId, final boolean shouldShow) {
        Realm realm = Realm.getDefaultInstance();
        GroupRealm groupRealm = getGroup(groupId);
        realm.beginTransaction();
        groupRealm.setShouldShow(shouldShow);
        realm.commitTransaction();
    }

    public void setGroupLastAccess(final int chatId, final long lastAccess) {
        Realm realm = Realm.getDefaultInstance();
        GroupRealm groupRealm = getGroupFromIdChat(chatId);
        if (groupRealm != null) {
            realm.beginTransaction();
            groupRealm.setLastAccess(lastAccess);
            realm.commitTransaction();
        } else {
            Dynamizer dynamizer = findDynamizerFromChatId(chatId);
            realm.beginTransaction();
            dynamizer.setLastAccess(lastAccess);
            realm.commitTransaction();
        }
    }

    public void saveCurrentUsersGroups(List<UserGroup> userGroupList) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        for (UserGroup userGroup : userGroupList) {
            UserGroupRealm userGroupRealm = realm.where(UserGroupRealm.class).equalTo("idDynamizerChat", userGroup.getIdDynamizerChat()).findFirst();
            if (userGroupRealm != null) {
                userGroupRealm.deleteFromRealm();
            }
            userGroupRealm = new UserGroupRealm(userGroup.getIdDynamizerChat(), userGroup.getGroup().getIdGroup());
            realm.copyToRealm(userGroupRealm);


            GroupRealm groupRealm = realm.where(GroupRealm.class).equalTo("id", userGroup.getGroup().getIdGroup()).findFirst();
            String groupPhoto =  groupRealm != null && groupRealm.getPhoto() != null ? groupRealm.getPhoto() : "";
            if (groupRealm == null) {
                groupRealm = new GroupRealm(userGroup.getGroup().getIdGroup(), userGroup.getGroup().getName(), userGroup.getGroup().getTopic(), userGroup.getGroup().getDescription(),
                        groupPhoto, userGroup.getGroup().getDynamizer().getId(), userGroup.getGroup().getIdChat());
            } else {
                groupRealm.setName(userGroup.getGroup().getName());
                groupRealm.setDescription(userGroup.getGroup().getDescription());
                groupRealm.setIdDynamizer(userGroup.getGroup().getDynamizer().getId());
                groupRealm.setIdChat(userGroup.getGroup().getIdChat());
            }

            realm.copyToRealmOrUpdate(groupRealm);

            Dynamizer dynamizerRealm = realm.where(Dynamizer.class).equalTo("id", userGroup.getGroup().getDynamizer().getId()).findFirst();
            Dynamizer dynamizer = userGroup.getGroup().getDynamizer();
            if (dynamizerRealm != null && dynamizerRealm.getIdContentPhoto() == dynamizer.getIdContentPhoto()) {
                dynamizer.setPhoto(dynamizerRealm.getPhoto());
            }
            /*if (dynamizerRealm != null) {
                dynamizerRealm.deleteFromRealm();
            }*/
            realm.copyToRealmOrUpdate(dynamizer);
        }
        realm.commitTransaction();

    }

    public void addOrUpdateUserGroup(UserGroup userGroup) {
        Realm realm = Realm.getDefaultInstance();

        //add or modify userGroup
        UserGroupRealm userGroupRealm = realm.where(UserGroupRealm.class).equalTo(
                "idGroup", userGroup.getGroup().getId()).findFirst();
        if (userGroupRealm != null) {
            if (userGroupRealm.getIdDynamizerChat() != userGroup.getIdDynamizerChat()) {
                realm.beginTransaction();
                userGroupRealm.setIdDynamizerChat(userGroup.getIdDynamizerChat());
                realm.commitTransaction();
            }
        } else {
            userGroupRealm = new UserGroupRealm(userGroup.getIdDynamizerChat(), userGroup.getGroup().getIdGroup());
            realm.beginTransaction();
            realm.copyToRealm(userGroupRealm);
            realm.commitTransaction();
        }

        //add or modify group
        GroupRealm groupRealm = realm.where(GroupRealm.class).equalTo("id", userGroup.getGroup().getIdGroup()).findFirst();
        String groupPhoto =  groupRealm != null && groupRealm.getPhoto() != null ? groupRealm.getPhoto() : "";
        RealmList<Integer> userIds =  groupRealm != null ? groupRealm.getUsers() : null;

        groupRealm = new GroupRealm(userGroup.getGroup().getIdGroup(), userGroup.getGroup().getName(), userGroup.getGroup().getTopic(), userGroup.getGroup().getDescription(),
                groupPhoto, userGroup.getGroup().getDynamizer().getId(), userGroup.getGroup().getIdChat());
        groupRealm.setUsers(userIds);
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(groupRealm);
        realm.commitTransaction();

        Dynamizer dynamizerRealm = realm.where(Dynamizer.class).equalTo("id", userGroup.getGroup().getDynamizer().getId()).findFirst();
        Dynamizer dynamizer = userGroup.getGroup().getDynamizer();
        if (dynamizerRealm != null && dynamizerRealm.getIdContentPhoto() == dynamizer.getIdContentPhoto()) {
            dynamizer.setPhoto(dynamizerRealm.getPhoto());
        }
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(dynamizer);
        realm.commitTransaction();
    }

    public void setShouldShowDynamizer(final int dynamizerId, final boolean shouldShow) {
        Realm realm = Realm.getDefaultInstance();
        Dynamizer dynamizer = findDynamizer(dynamizerId);
        realm.beginTransaction();
        dynamizer.setShouldShow(shouldShow);
        realm.commitTransaction();
    }


    public ArrayList<Dynamizer> findAllDynamizer() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Dynamizer> dynamizerRealmResults = realm.where(Dynamizer.class)
                .equalTo("shouldShow", true)
                .findAll();
        return new ArrayList<>(dynamizerRealmResults);
    }

    public Dynamizer findDynamizer(int id) {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(Dynamizer.class).equalTo("id", id).findFirst();
    }

    public Dynamizer findDynamizerFromChatId(int chatId) {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(Dynamizer.class).equalTo("idChat", chatId).findFirst();
    }

    public ArrayList<UserGroupRealm> findAllUserGroupRealm() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<UserGroupRealm> userGroupRealmRealmResults = realm.where(UserGroupRealm.class)
                .findAll();
        ArrayList<UserGroupRealm> userGroupRealmList = new ArrayList<>();
        for (UserGroupRealm userGroupRealm : userGroupRealmList) {
            userGroupRealmList.add(userGroupRealm);
        }
        return userGroupRealmList;
    }

    public ArrayList<GroupRealm> findAllGroupRealm() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<GroupRealm> groupRealmRealmResults = realm.where(GroupRealm.class)
                .equalTo("shouldShow", true)
                .findAll();
        return new ArrayList<>(groupRealmRealmResults);
    }

    public void setUserGroupAvatarPath (int groupID, String path) {
        Realm realm = Realm.getDefaultInstance();
        GroupRealm groupRealm = realm.where(GroupRealm.class)
                .equalTo("id", groupID)
                .findFirst();
        if (groupRealm != null) {
            realm.beginTransaction();
            groupRealm.setPhoto(path);
            realm.commitTransaction();
        }
    }

    public void setUserGroupUsersList(int groupID, ArrayList<Integer> userIDs) {
        Realm realm = Realm.getDefaultInstance();
        GroupRealm groupRealm = realm.where(GroupRealm.class)
                .equalTo("id", groupID)
                .findFirst();
        if (groupRealm != null) {
            realm.beginTransaction();
            groupRealm.setUsers(OtherUtils.convertIntegersToRealmList(userIDs));
            realm.commitTransaction();
        }
    }

    public void addUserToGroupList(int groupID, int userId) {
        Realm realm = Realm.getDefaultInstance();
        GroupRealm groupRealm = realm.where(GroupRealm.class)
                .equalTo("id", groupID)
                .findFirst();
        if (groupRealm != null) {
            ArrayList<Integer> userIDs = new ArrayList<>(groupRealm.getUsers());
            if (!userIDs.contains(userId)) {
                userIDs.add(userId);
                realm.beginTransaction();
                groupRealm.setUsers(OtherUtils.convertIntegersToRealmList(userIDs));
                realm.commitTransaction();
            }
        }
    }

    /**
     * Remove user and return whether it existed
     *
     * @param groupID
     * @param userId
     * @return          whether it existed
     */
    public boolean removeUserFromGroupList(int groupID, int userId) {
        Realm realm = Realm.getDefaultInstance();
        GroupRealm groupRealm = realm.where(GroupRealm.class)
                .equalTo("id", groupID)
                .findFirst();
        if (groupRealm != null) {
            RealmList<Integer> userIDs = groupRealm.getUsers();
            if (userIDs.contains(userId)) {
                realm.beginTransaction();
                userIDs.remove((Integer)userId);
                realm.commitTransaction();
            }
            return true;
        }
        return false;
    }

    public RealmList<Integer> getGroupUserListFromIdChat(int idChat) {
        Realm realm = Realm.getDefaultInstance();
        GroupRealm groupRealm = realm.where(GroupRealm.class)
                .equalTo("idChat", idChat)
                .findFirst();

        if (groupRealm == null) return null;
        return groupRealm.getUsers();
    }

    public GroupRealm getGroupFromIdChat(int idChat) {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(GroupRealm.class)
                .equalTo("idChat", idChat)
                .findFirst();
    }

    public GroupRealm getGroup(int idGroup) {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(GroupRealm.class)
                .equalTo("id", idGroup)
                .findFirst();
    }


    public String getUserGroupAvatarPath (int groupID) {
        Realm realm = Realm.getDefaultInstance();
        GroupRealm groupRealm = realm.where(GroupRealm.class)
                .equalTo("id", groupID)
                .findFirst();

        if (groupRealm == null) return null;
        return groupRealm.getPhoto();
    }

    public void setGroupDynamizerAvatarPath (int dynID, String path) {
        Realm realm = Realm.getDefaultInstance();
        Dynamizer dynamizer = realm.where(Dynamizer.class)
                .equalTo("id", dynID)
                .findFirst();
        if (dynamizer != null) {
            realm.beginTransaction();
            dynamizer.setPhoto(path);
            realm.commitTransaction();
        }
    }

    public String getGroupDynamizerAvatarPath (int dynID) {
        Realm realm = Realm.getDefaultInstance();
        Dynamizer dynamizer = realm.where(Dynamizer.class)
                .equalTo("id", dynID)
                .findFirst();

        if (dynamizer == null) return null;
        return dynamizer.getPhoto();
    }


    public void setMessagesInfo(int chatId, int unreadMessages, int interactions) {
        Realm realm = Realm.getDefaultInstance();
        GroupRealm groupRealm = getGroupFromIdChat(chatId);
        if (groupRealm != null) {
            realm.beginTransaction();
            groupRealm.setNumberUnreadMessages(unreadMessages);
            groupRealm.setNumberInteractions(interactions);
            realm.commitTransaction();
        }
    }

    public void setDynamizerMessagesInfo(int chatId, int unreadMessages, int interactions) {
        Realm realm = Realm.getDefaultInstance();
        Dynamizer dynamizer = realm.where(Dynamizer.class)
                .equalTo("idChat", chatId)
                .findFirst();
        if (dynamizer != null) {
            realm.beginTransaction();
            dynamizer.setNumberUnreadMessages(unreadMessages);
            dynamizer.setNumberInteractions(interactions);
            realm.commitTransaction();
        }
    }
}
