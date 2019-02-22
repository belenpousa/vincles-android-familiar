package cat.bcn.vincles.mobile.UI.Chats;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Db.GroupMessageDb;
import cat.bcn.vincles.mobile.Client.Db.Model.GroupRealm;
import cat.bcn.vincles.mobile.Client.Db.UserGroupsDb;
import cat.bcn.vincles.mobile.Client.Db.UserMessageDb;
import cat.bcn.vincles.mobile.Client.Db.UsersDb;
import cat.bcn.vincles.mobile.Client.Model.ChatMessageRepositoryModel;
import cat.bcn.vincles.mobile.Client.Model.ChatMessageRest;
import cat.bcn.vincles.mobile.Client.Model.ChatMessageSentResponse;
import cat.bcn.vincles.mobile.Client.Model.Dynamizer;
import cat.bcn.vincles.mobile.Client.Model.GetUser;
import cat.bcn.vincles.mobile.Client.Model.GroupMessageRest;
import cat.bcn.vincles.mobile.Client.Model.GroupMessageRestList;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Client.Requests.AddContentInTheGallery;
import cat.bcn.vincles.mobile.Client.Requests.BaseRequest;
import cat.bcn.vincles.mobile.Client.Requests.GalleryAddContentRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetGalleryContentRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetGroupMessageFileRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetGroupMessageRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetGroupMessagesRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetUserPhotoRequest;
import cat.bcn.vincles.mobile.Client.Requests.PutGroupLastAccessRequest;
import cat.bcn.vincles.mobile.Client.Requests.SendGroupMessageRequest;
import cat.bcn.vincles.mobile.UI.Chats.Model.ChatMessage;
import cat.bcn.vincles.mobile.UI.FragmentManager.MainFragmentManagerActivity;
import cat.bcn.vincles.mobile.Utils.MyApplication;
import cat.bcn.vincles.mobile.Utils.OtherUtils;
import io.realm.DynamicRealm;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmModel;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class ChatRepositoryGroup extends ChatRepository implements
        GalleryAddContentRequest.OnResponse, AddContentInTheGallery.OnResponse,
        GetGroupMessagesRequest.OnResponse, GetGroupMessageRequest.OnResponse,
        SendGroupMessageRequest.OnResponse, GetUserPhotoRequest.OnResponse, GetGroupMessageFileRequest.OnResponse {

    private int fileId;
    private String mediaType;
    ArrayList<GetUser> chatUserList;
    RealmChangeListener<GetUser> chatUserChangeListener;
    ArrayList<GetUser> userChangedCounter = new ArrayList<>();

    ArrayList<String> sendingPaths;
    ArrayList<String> sendingMetadataList;

    GroupRealm group;
    Dynamizer dynamizer;
    String dynamizerPhoto;
    int dynamizerID;
    boolean isDynamizer;

    boolean logoutOcurred = false;

    public ChatRepositoryGroup() {
        initChangeListener();
    }

    @SuppressLint("ValidFragment")
    public ChatRepositoryGroup(final Callback listener, BaseRequest.RenewTokenFailed renewTokenListener,
                               int userId, final int chatId, boolean isDynamizer) {
        super(listener, renewTokenListener, userId, chatId);
        this.isDynamizer = isDynamizer;

        initChangeListener();
    }

    private void initChangeListener() {
        chatUserChangeListener = new RealmChangeListener<GetUser>() {
          @Override
          public void onChange(@NonNull GetUser chatUser) {
            try{
            Log.d("tpqne","ChatRepositoryUser on Realm Change");
            if (listener != null) {
              //GetUser chatUser = chatUserList.get(0);
              if (chatUser.isValid()) {
                        /*listener.onChatInfoUpdated(chatUser.getName(), chatUser.getLastname(),
                                chatUser.getPhoto());*/
                if (userChangedCounter.contains(chatUser))
                  userChangedCounter.remove(chatUser);

                notifyDataIfReady();
              }
            }
          }catch (Exception e) {
            System.out.println("Error " + e.getMessage());
          }
          }
        };
    }

    @Override
    public void onLogout() {
        logoutOcurred = true;
    }



    @Override
    protected void notifyDataIfReady() {
        if (listener != null && chatUserList != null) {
            //GetUser chatUser = chatUserList.get(0);
            if (group != null && group.isValid()) {
                listener.onChatInfoUpdated(group.getName(), null,
                        group.getPhoto());
            } else if (dynamizer != null && dynamizer.isValid()) {
                listener.onChatInfoUpdated(dynamizer.getName(), null,
                        dynamizer.getPhoto());
            }
            if (dynamizerPhoto != null && dynamizerPhoto.length() > 0) {
                listener.onChatDynamizerUpdated(dynamizerID, dynamizerPhoto);
            }
            if (userChangedCounter.size() == 0) {
                listener.onUserListUpdated();
            }
        }
    }


    @Override
    public ArrayList<GetUser> getUserListData() {
        if (chatUserList != null) return chatUserList;

        UserGroupsDb userGroupsDb = new UserGroupsDb(MyApplication.getAppContext());
        if (!isDynamizer) {
            group = userGroupsDb.getGroupFromIdChat(chatId);
            dynamizerID = group.getIdDynamizer();
            dynamizerPhoto = userGroupsDb.getGroupDynamizerAvatarPath(dynamizerID);
            dynamizer = userGroupsDb.findDynamizer(dynamizerID);
        } else {
            group = null;
            dynamizer = userGroupsDb.findDynamizerFromChatId(chatId);
            dynamizerID = dynamizer.getId();
            dynamizerPhoto = dynamizer.getPhoto();
        }

        if (listener != null) {
            if (!isDynamizer) {
                listener.onChatInfoUpdated(group.getName(), null,
                        group.getPhoto());
            } else {
                listener.onChatInfoUpdated(dynamizer.getName(), null,
                        dynamizer.getPhoto());
            }
            if (dynamizerPhoto != null && dynamizerPhoto.length() > 0) {
                listener.onChatDynamizerUpdated(dynamizerID, dynamizerPhoto);
            } else {
                GetUserPhotoRequest getUserPhotoRequest = new GetUserPhotoRequest(
                        renewTokenListener, String.valueOf(group.getIdDynamizer()));
                getUserPhotoRequest.addOnOnResponse(this);
                getUserPhotoRequest.doRequest(new UserPreferences(MyApplication.getAppContext()).getAccessToken());
            }
        }

        if (group != null) {
            group.addChangeListener(new RealmChangeListener<RealmModel>() {
                @Override
                public void onChange(RealmModel realmModel) {
                    if (!logoutOcurred && listener != null && group != null && group.isValid()) {
                        listener.onChatInfoUpdated(group.getName(), null,
                                group.getPhoto());
                    }
                }
            });
        }

        if (isDynamizer && dynamizer != null) {
            dynamizer.addChangeListener(new RealmChangeListener<RealmModel>() {
                @Override
                public void onChange(RealmModel realmModel) {
                    if (!logoutOcurred && listener != null && dynamizer != null && dynamizer.isValid()) {
                        listener.onChatInfoUpdated(dynamizer.getName(), null,
                                dynamizer.getPhoto());
                    }
                }
            });
        }


        RealmList<Integer> realmList = null;
        if (group != null) realmList = group.getUsers();
        ArrayList<Integer> userIDs;
        if (realmList == null) userIDs = new ArrayList<>();
        else userIDs = new ArrayList<>(realmList);
        chatUserList = new ArrayList<>();
        for (int userId : userIDs) {
            UsersDb usersDb = new UsersDb(MyApplication.getAppContext());
            GetUser chatUser = usersDb.findUserAsync(userId);
            chatUserList.add(chatUser);
            userChangedCounter.add(chatUser);
            chatUser.addChangeListener(chatUserChangeListener);
        }

        return chatUserList;
    }

    @Override
    protected Dynamizer getDynamizer() {
        return dynamizer;
    }

    @Override
    protected int getDynamizerId() {
        if (group != null) {
            UserGroupsDb userGroupsDb = new UserGroupsDb(MyApplication.getAppContext());
            Dynamizer dynamizer = userGroupsDb.findDynamizer(group.getIdDynamizer());
            if (dynamizer != null) return dynamizer.getIdChat();
        }
        if (dynamizer != null) return dynamizer.getIdChat();
        return -1;
    }

    public void getMessages(long timeStampStart, long timeStampEnd) {
        Log.d("asd","getMessages");
        if (timeStampStart == 0 && timeStampEnd == 0) {
            GroupMessageDb groupMessageDb = new GroupMessageDb(MyApplication.getAppContext());
            ArrayList<GroupMessageRest> messages = groupMessageDb.findAllMessagesForGroup(chatId);
            if (messages != null && messages.size() > 0) {
                Log.d("asd","getMessages handleObtainedUserMessages");
                handleObtainedUserMessages(messages);
            } else {
                Log.d("asd","getMessages download from server");
                GetGroupMessagesRequest getGroupMessagesRequest= new GetGroupMessagesRequest(renewTokenListener, String.valueOf(chatId), timeStampStart, timeStampEnd);
                getGroupMessagesRequest.addOnOnResponse(this);
                getGroupMessagesRequest.doRequest(new UserPreferences().getAccessToken());
            }
        } else {
            GetGroupMessagesRequest getGroupMessagesRequest = new GetGroupMessagesRequest(renewTokenListener, String.valueOf(chatId), timeStampStart, timeStampEnd);
            getGroupMessagesRequest.addOnOnResponse(this);
            getGroupMessagesRequest.doRequest(new UserPreferences().getAccessToken());
        }

    }

    public void getMessage(String idMessage) {
        GetGroupMessageRequest getGroupMessageRequest= new GetGroupMessageRequest(renewTokenListener,
                String.valueOf(chatId), idMessage);
        getGroupMessageRequest.addOnOnResponse(this);
        getGroupMessageRequest.doRequest(new UserPreferences().getAccessToken());
    }

    public void getLocalMessage(Long idMessage) {
        GroupMessageDb groupMessageDb = new GroupMessageDb(MyApplication.getAppContext());
        GroupMessageRest groupMessageRest = groupMessageDb.findMessage(idMessage);
        onLocalMessageReady(new ChatMessageRepositoryModel(groupMessageRest));
    }

    public void getMediaFiles(long messageId, int[] mediaId) {
        String accessToken = new UserPreferences().getAccessToken();
        for (int i = 0; i < mediaId.length; i++) {
            GetGroupMessageFileRequest getGroupMessageFileRequest = new GetGroupMessageFileRequest(
                    renewTokenListener, MyApplication.getAppContext(), String.valueOf(mediaId[i]),
                    chatId, (int) messageId);
            getGroupMessageFileRequest.addOnOnResponse(this);
            getGroupMessageFileRequest.doRequest(accessToken);
        }
    }

    public void getMediaFile(long messageId, int position) {
        GroupMessageDb groupMessageDb = new GroupMessageDb(MyApplication.getAppContext());
        GroupMessageRest messageRest = groupMessageDb.findMessage(messageId);
        String fileId = String.valueOf(messageRest.getIdContent());

        String accessToken = new UserPreferences().getAccessToken();
        GetGroupMessageFileRequest getGroupMessageFileRequest = new GetGroupMessageFileRequest(
                renewTokenListener, MyApplication.getAppContext(), fileId, chatId, (int) messageId);
        getGroupMessageFileRequest.addOnOnResponse(this);
        getGroupMessageFileRequest.doRequest(accessToken);
    }

    @Override
    public void sendMessage(ChatMessageRepositoryModel message) {
        GroupMessageRest groupMessageRest = new GroupMessageRest();
        groupMessageRest.setIdChat(message.getIdChat());
        groupMessageRest.setIdUserSender(message.getIdSender());
        groupMessageRest.setText(message.getText());
        groupMessageRest.setIdContent((message.getIdAdjuntContents() != null
                && message.getIdAdjuntContents().size() > 0) ? message.getIdAdjuntContents().get(0)
                : null);
        groupMessageRest.setMetadataTipus(message.getMetadataTipus());
        sendingPaths = message.getPathsAdjuntContents();
        sendingMetadataList = message.getMetadataAdjuntContents();

        SendGroupMessageRequest sendGroupMessageRequest = new SendGroupMessageRequest(renewTokenListener,
                groupMessageRest, String.valueOf(chatId));
        sendGroupMessageRequest.addOnOnResponse(this);
        sendGroupMessageRequest.doRequest(new UserPreferences().getAccessToken());
    }

    @Override
    public void saveFileToGallery(File file, String mediaType) {
        this.mediaType = mediaType;
        String imageFileName = file.getName();

        RequestBody fileB = RequestBody.create(MediaType.parse(mediaType), file);
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("file", imageFileName, fileB);

        String accessToken = new UserPreferences().getAccessToken();
        GalleryAddContentRequest galleryAddContentRequest = new GalleryAddContentRequest(
                renewTokenListener, body);
        galleryAddContentRequest.addOnOnResponse(this);
        galleryAddContentRequest.doRequest(accessToken);
    }

    @Override
    public void setMessageAsWatched(long messageId) {
        PutGroupLastAccessRequest putGroupLastAccessRequest = new PutGroupLastAccessRequest(
                renewTokenListener, String.valueOf(chatId));
        putGroupLastAccessRequest.doRequest(new UserPreferences(MyApplication.getAppContext())
                .getAccessToken());

        GroupMessageDb groupMessageDb = new GroupMessageDb(MyApplication.getAppContext());
        groupMessageDb.setGroupMessageListWatchedTrue(chatId);

        OtherUtils.updateGroupOrDynChatInfo(chatId);
    }

    private void handleObtainedUserMessages(List<GroupMessageRest> messages) {
        if (listener != null) {
            ArrayList<ChatMessageRepositoryModel> chatMessageList = new ArrayList<>();
            for (GroupMessageRest groupMessageRest : messages) {
                chatMessageList.add(new ChatMessageRepositoryModel(groupMessageRest));
            }
            listener.messagesReceived(chatMessageList);
        }
    }

    @Override
    public void onFailureGetUserPhotoRequest(Object error, String userID, int viewID, int contactType) {
        if (listener != null) listener.onFileError(error);
    }

    @Override
    public void onGetGroupMessageFileRequestResponse(int messageId, String filePath,
                                                     String contentID, String mimeType) {
        GroupMessageDb groupMessageDb = new GroupMessageDb(MyApplication.getAppContext());
        groupMessageDb.setMessageFile(Integer.parseInt(contentID), filePath, messageId, mimeType);
        onFileReceived(messageId, Integer.parseInt(contentID), filePath, mimeType);
    }

    @Override
    public void onResponseGalleryAddContentRequest(JsonObject galleryAddedContent) {
        fileId = galleryAddedContent.getAsJsonObject().get("id").getAsInt();


        AddContentInTheGallery addContentInTheGallery = new AddContentInTheGallery(
                renewTokenListener, fileId);
        addContentInTheGallery.addOnOnResponse(this);
        addContentInTheGallery.doRequest(new UserPreferences().getAccessToken());
    }

    @Override
    public void onFailureGalleryAddContentRequest(Object error) {
        listener.onFileSaveToGalleryError();
    }

    @Override
    public void onResponseAddContentInTheGallery(int contentId) {
        onFileSaved(contentId, fileId, mediaType);
    }

    @Override
    public void onFailureAddContentInTheGallery(Object error) {
        listener.onFileSaveToGalleryError();
    }

    @Override
    public void onResponseGetGroupMessagesRequest(GroupMessageRestList groupMessageRestList,
                                                  String idChat) {
        List<GroupMessageRest> messages = groupMessageRestList.getChatMessageRestList();
        if (messages != null && messages.size() > 0) {
            GroupMessageDb groupMessageDb = new GroupMessageDb(MyApplication.getAppContext());
            groupMessageDb.saveGroupMessageRestList(messages);

            GroupRealm group = new UserGroupsDb(MyApplication.getAppContext()).getGroupFromIdChat(
                    Integer.parseInt(idChat));
            Long lastAccess;
            if (group != null) lastAccess = group.getLastAccess();
            else lastAccess = new UserGroupsDb(MyApplication.getAppContext()).findDynamizerFromChatId(
                    Integer.parseInt(idChat)).getLastAccess();
            groupMessageDb.setGroupMessageListWatched(Integer.parseInt(idChat), lastAccess,
                    messages.get(0).getSendTime());

            OtherUtils.updateGroupOrDynChatInfo(chatId);

        }
        handleObtainedUserMessages(messages);

    }

    @Override
    public void onFailureGetGroupMessagesRequest(Object error) {

    }

    @Override
    public void onResponseSendGroupMessageRequest(ChatMessageSentResponse responseBody) {
        onMessageSent(responseBody.getId());
    }

    @Override
    public void onFailureSendGroupMessageRequest(Object error) {
        onMessageError();
    }

    @Override
    public void onResponseGetGroupMessageRequest(GroupMessageRest groupMessageRest) {
        GroupMessageDb groupMessageDb = new GroupMessageDb(MyApplication.getAppContext());
        groupMessageRest.setPathContent(sendingPaths != null ? sendingPaths.get(0) : null);
        groupMessageRest.setMetadataContent(sendingMetadataList != null ? sendingMetadataList.get(0)
        : null);
        groupMessageDb.saveGroupMessageRest(groupMessageRest);
        onMessageReceived(new ChatMessageRepositoryModel(groupMessageRest));
    }

    @Override
    public void onFailureGetGroupMessageRequest(Object error) {

    }

    @Override
    public void onResponseGetUserPhotoRequest(Uri photo, String userID, int viewID, int contactType) {
        if (Integer.parseInt(userID) == dynamizerID) {
            UserGroupsDb userGroupsDb = new UserGroupsDb(MyApplication.getAppContext());
            dynamizerPhoto = photo.getPath();
            userGroupsDb.setGroupDynamizerAvatarPath(dynamizerID, dynamizerPhoto);
            if (listener != null) {
                listener.onChatDynamizerUpdated(dynamizerID, dynamizerPhoto);
            }
        }
    }

    @Override
    public void onGetGroupMessageFileRequestFailure(Object error) {

    }
}
