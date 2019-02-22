package cat.bcn.vincles.mobile.UI.Chats;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.util.ArrayList;

import cat.bcn.vincles.mobile.Client.Db.MeetingsDb;
import cat.bcn.vincles.mobile.Client.Db.Model.GroupRealm;
import cat.bcn.vincles.mobile.Client.Db.UserGroupsDb;
import cat.bcn.vincles.mobile.Client.Db.UsersDb;
import cat.bcn.vincles.mobile.Client.Model.Dynamizer;
import cat.bcn.vincles.mobile.Client.Model.GetUser;
import cat.bcn.vincles.mobile.Client.Model.MeetingRealm;
import cat.bcn.vincles.mobile.Client.Model.MeetingRestSendModel;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Client.Requests.BaseRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetMeetingUserPhotoRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetUserPhotoRequest;
import cat.bcn.vincles.mobile.Client.Requests.GroupInviteUserToCircle;
import cat.bcn.vincles.mobile.UI.Contacts.Contact;
import cat.bcn.vincles.mobile.Utils.MyApplication;
import cat.bcn.vincles.mobile.Utils.OtherUtils;

public class GroupDetailPresenter extends Fragment implements
        GroupDetailPresenterContract, GetUserPhotoRequest.OnResponse, GetMeetingUserPhotoRequest.OnResponse, GroupInviteUserToCircle.OnResponse {


    private int groupId, chatId = -1;

    UsersDb usersDb;

    BaseRequest.RenewTokenFailed listener;
    GroupDetailFragmentView view;
    UserPreferences userPreferences;

    ArrayList<Integer> contactIds;
    ArrayList<Integer> circleIds;
    ArrayList<Contact> contacts;
    String groupName, groupPath, groupDescription;
    int dynamizerId;

    boolean showingAlertMessage = false;
    boolean showingNonDismissable = false;
    boolean showingError = false;

    Object error;

    boolean onCreateViewDone = false, infoLoaded = false, loadDataPending = false;

    public GroupDetailPresenter(){
        contactIds = new ArrayList<>();
        usersDb = new UsersDb(MyApplication.getAppContext());
        contactIds = new ArrayList<>();
        circleIds = new ArrayList<>();
        contacts = new ArrayList<>();
    }

    public static GroupDetailPresenter newInstance(BaseRequest.RenewTokenFailed listener,
                                                   GroupDetailFragmentView view,
                                                   Bundle savedInstanceState,
                                                   int chatId) {
        GroupDetailPresenter fragment = new GroupDetailPresenter();
        Bundle args = new Bundle();
        args.putInt("chatId", chatId);
        fragment.setArguments(args);
        fragment.setExternalVars(listener, view, savedInstanceState);

        return fragment;
    }


    public void setExternalVars(BaseRequest.RenewTokenFailed listener,
                                GroupDetailFragmentView view,
                                Bundle savedInstanceState) {
        this.listener = listener;
        this.view = view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userPreferences = new UserPreferences(MyApplication.getAppContext());
        chatId = getArguments().getInt("chatId");
        Log.d("gdp","onCreate chatId:"+chatId);
        setRetainInstance(true);
        if (loadDataPending) loadData();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
        view = null;
    }


    @Override
    public void onCreateView() {
        onCreateViewDone = true;
        if (infoLoaded) setViewInfo();

        if (showingAlertMessage) {
            view.showInvitationSent();
        } else if (showingNonDismissable) {
            view.showSendingData();
        } else if (showingError) {
            view.showError(error);
        }
    }

    private void setViewInfo() {
        if (view != null) {
            view.updateGroupName(groupName);
            view.updateDescription(groupDescription);
            view.updateAvatar(groupPath);
            view.setContacts(contacts);
        }
    }

    @Override
    public void loadData() {
        if (chatId == -1) {
            loadDataPending = true;
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    loadDataPending = false;
                    Log.d("gdp","load data chatId:"+chatId);
                    UserGroupsDb userGroupsDb = new UserGroupsDb(MyApplication.getAppContext());
                    GroupRealm group = userGroupsDb.getGroupFromIdChat(chatId);
                    groupId = group.getIdGroup();
                    groupDescription = group.getDescription();
                    groupName = group.getName();
                    groupPath = group.getPhoto();
                    dynamizerId = group.getIdDynamizer();
                    contactIds.clear();
                    contactIds.addAll(group.getUsers());
                    Log.d("gdp", "contact ids size:"+contactIds.size());
                    getCircleList();
                    getContactList();

                    infoLoaded = true;
                    if (onCreateViewDone) setViewInfo();
                }
            }).start();
        }
    }

    @Override
    public void loadContactPicture(int contactId) {
        GetUserPhotoRequest getUserPhotoRequest = new GetUserPhotoRequest(listener,
                String.valueOf(contactId));
        getUserPhotoRequest.addOnOnResponse(this);
        getUserPhotoRequest.doRequest(userPreferences.getAccessToken());
    }

    private void getCircleList() {
        ArrayList<GetUser> users = usersDb.findAllCircleUser();
        circleIds.clear();
        for (GetUser getUser : users) {
            circleIds.add(getUser.getId());
        }
    }

    private void getContactList() {
        usersDb = new UsersDb(MyApplication.getAppContext());
        contacts.clear();
        contacts.add(createContact(new UserGroupsDb(MyApplication.getAppContext())
                .findDynamizer(dynamizerId)));
        for (int id : contactIds) {
            Log.d("gdp","contact type:"+getGuestType(id)+" userid:"+id+" circleIds:"+circleIds);
            contacts.add(createContact(usersDb.findUser(id), getGuestType(id)));
        }
    }

    private int getGuestType(int id) {
        if (circleIds.contains(id)) return Contact.TYPE_CIRCLE_USER;
        return Contact.TYPE_GROUP;
    }

    private Contact createContact(Dynamizer dynamizer) {
        Contact contact = new Contact();
        contact.setId(dynamizer.getId());
        contact.setName(dynamizer.getName());
        contact.setLastname(dynamizer.getLastname());
        contact.setType(Contact.TYPE_DYNAMIZER);
        contact.setIdContentPhoto(dynamizer.getIdContentPhoto());
        contact.setPath(dynamizer.getPhoto());
        contact.setNumberNotifications(Integer.MAX_VALUE);
        return contact;
    }

    private Contact createContact(GetUser getUser, int type) {
        Contact contact = new Contact();
        contact.setId(getUser.getId());
        contact.setName(getUser.getName());
        contact.setLastname(getUser.getLastname());
        contact.setType(type);
        contact.setIdContentPhoto(getUser.getIdContentPhoto() == null ? 0
                : getUser.getIdContentPhoto());
        contact.setPath(getUser.getPhoto());
        return contact;
    }


    @Override
    public void stoppedShowingErrorDialog() {
        showingError = false;
    }

    @Override
    public void stoppedShowingMessageDialog() {
        showingAlertMessage = false;
    }

    @Override
    public void clickedInvite(int id, int type) {
        if (type == Contact.TYPE_GROUP) {
            GroupInviteUserToCircle groupInviteUserToCircle = new GroupInviteUserToCircle(listener,
                    userPreferences.getAccessToken(), groupId, id);
            groupInviteUserToCircle.addOnOnResponse(this);
            groupInviteUserToCircle.doRequest();
            showingNonDismissable = true;
            if (view != null) view.showSendingData();
        }
    }

    public ArrayList<Integer> getContactIds() {
        return contactIds;
    }

    @Override
    public void onResponseGetUserPhotoRequest(final Uri photo, String userID, final int viewID, int contactType) {
        new UsersDb(MyApplication.getAppContext()).setPathAvatarToUser(Integer.parseInt(userID), photo.getPath());
        for (Contact contact : contacts) {
            if (contact.getId() == Integer.parseInt(userID)) {
                contact.setPath(photo.getPath());
            }
        }
        if (view != null) view.notifyContactChange();
    }

    @Override
    public void onFailureGetUserPhotoRequest(Object error, String userID, int viewID, int contactType) {
        if (view != null) view.updateAvatar("placeholder");
    }

    @Override
    public void onResponseGetMeetingUserPhotoRequest(Uri photo, String userID) {
        new UsersDb(MyApplication.getAppContext()).setPathAvatarToUser(Integer.parseInt(userID), photo.getPath());
        for (Contact contact : contacts) {
            if (contact.getId() == Integer.parseInt(userID)) {
                contact.setPath(photo.getPath());
                if (view != null) view.notifyContactChange();
                break;
            }
        }
    }

    @Override
    public void onFailureGetMeetingUserPhotoRequest(Object error, String userID) {
        for (Contact contact : contacts) {
            if (contact.getId() == Integer.parseInt(userID)) {
                contact.setPath("placeholder");
                if (view != null) view.notifyContactChange();
                break;
            }
        }
    }

    @Override
    public void onResponseGroupInviteUserToCircle(int groupID, int userId) {
        showingNonDismissable = false;
        showingAlertMessage = true;
        if (view != null) view.hideSendingData();
        if (view != null) view.showInvitationSent();
    }

    @Override
    public void onFailureGroupInviteUserToCircle(Object error) {
        Log.d("gdp","error invite: "+error);
        showingNonDismissable = false;
        showingError = true;
        this.error = error;
        if (view != null) view.hideSendingData();
        if (view != null) view.showError(error);
    }
}
