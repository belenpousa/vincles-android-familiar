package cat.bcn.vincles.mobile.UI.ContentDetail;


import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.util.Calendar;

import cat.bcn.vincles.mobile.Client.Business.Media;
import cat.bcn.vincles.mobile.Client.Db.GalleryDb;
import cat.bcn.vincles.mobile.Client.Db.Model.GalleryContentRealm;
import cat.bcn.vincles.mobile.Client.Db.UsersDb;
import cat.bcn.vincles.mobile.Client.Model.GetUser;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Client.Requests.BaseRequest;
import cat.bcn.vincles.mobile.Client.Requests.DeleteGalleryContentRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetUserPhotoRequest;
import cat.bcn.vincles.mobile.UI.Gallery.GallerypPresenter;
import cat.bcn.vincles.mobile.Utils.DateUtils;
import io.realm.RealmResults;

public class ContentDetailPresenter implements ContentDetailPresenterContract, GetUserPhotoRequest.OnResponse, DeleteGalleryContentRequest.OnResponse {

    ContentDetailView contentDetailView;
    GalleryDb galleryDb;
    UsersDb usersDb;
    RealmResults<GalleryContentRealm> galleryContentsRealm;
    UserPreferences userPreferences;
    GalleryContentRealm contentToBeDeleted;
    int userId = -1;
    String filterKind;
    BaseRequest.RenewTokenFailed listener;
    boolean isAvatarWanted = false;
    String avatarPath;


    public ContentDetailPresenter(BaseRequest.RenewTokenFailed listener, ContentDetailView contentDetailView, GalleryDb galleryDb,
                                  UsersDb usersDb, UserPreferences userPreferences, String filterKind) {
        this.listener = listener;
        this.contentDetailView = contentDetailView;
        this.galleryDb = galleryDb;
        this.usersDb = usersDb;
        this.filterKind = filterKind;
        this.userPreferences = userPreferences;
        galleryContentsRealm = getFilteredMedia();

    }

    @Override
    public void loadOwnerName (int position) {
        userId = galleryContentsRealm.get(position).getUserId();
        setOwnerName();
    }

    private void setOwnerName() {
        if (contentDetailView == null) return;

        String name = null;
        String lastName = null;

        GetUser user = usersDb.findUser(userId);
        if(user != null) {
            name = user.getName();
            lastName = user.getLastname();
        }

        if(name==null && lastName==null) {
            contentDetailView.setOwnerName(userPreferences.getName() + " " + userPreferences.getLastName());
        } else {
            contentDetailView.setOwnerName(name + " " + lastName);
        }
    }

    public void onUserUpdated(int userId) {
        if (this.userId == userId) {
            setOwnerName();
            if (isAvatarWanted) {
                GetUser user = usersDb.findUser(userId);
                if (!avatarPath.equals(user.getPhoto())) {
                    avatarPath = user.getPhoto();
                    setAvatarPath();
                }
            }
        }
    }

    @Override
    public void updateUserID (int position) {
        if (galleryContentsRealm.get(position) != null
                && usersDb.findUser(galleryContentsRealm.get(position).getUserId()) != null) {
            userId = usersDb.findUser(galleryContentsRealm.get(position).getUserId()).getId();
        }
    }

    @Override
    public void loadDate (Context context, int position) {
        long timpeStamp = galleryContentsRealm.get(position).getInclusionTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timpeStamp);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        String formatedTime = DateUtils.getFormattedHourMinutesFromMillis(context, timpeStamp);

        contentDetailView.setDate(dayOfMonth,month,year,formatedTime);
    }

    @Override
    public void loadAvatar(int position) {
        isAvatarWanted = true;
        userId = usersDb.findUser(galleryContentsRealm.get(position).getUserId()).getId();
        GetUser user = usersDb.findUser(userId);
        avatarPath = user.getPhoto();
        setAvatarPath();
    }

    private void setAvatarPath() {
        if (avatarPath != null && !avatarPath.equals("")) {
            contentDetailView.showAvatar(avatarPath);
        } else {
            contentDetailView.showAvatar("placeholder");
            String accessToken = userPreferences.getAccessToken();
            GetUserPhotoRequest getUserPhotoRequest = new GetUserPhotoRequest(listener, String.valueOf(userId));
            getUserPhotoRequest.addOnOnResponse(this);
            getUserPhotoRequest.doRequest(accessToken);
        }
    }

    @Override
    public void saveAvatarPath(int position, String filePath) {
        int userID = usersDb.findUser(galleryContentsRealm.get(position).getUserId()).getId();
        usersDb.setPathAvatarToUser(userID,filePath);
        galleryContentsRealm = getFilteredMedia();
    }

    public RealmResults<GalleryContentRealm> getFilteredMedia() {
        Log.d("flt","cont det filter:"+filterKind);
        switch (filterKind) {
            default:
            case GallerypPresenter.FILTER_ALL_FILES:
                return galleryDb.findAll();
            case  GallerypPresenter.FILTER_ALL_MY_FILES:
                return galleryDb.getContentsPathByUserID();
            case GallerypPresenter.FILTER_RECIVED_FILES:
                return galleryDb.getRecivedContentsPath();
        }
    }

    @Override
    public void deleteContent (int position) {
        Log.d("glry","deleteContent, pos:"+position);
        if (position >= 0 && galleryContentsRealm.size() > 0) {
            contentToBeDeleted = galleryContentsRealm.get(position);
            int contentID = contentToBeDeleted.getId();
            String accesToken = userPreferences.getAccessToken();
            DeleteGalleryContentRequest deleteGalleryContentRequest = new DeleteGalleryContentRequest(listener, contentID);
            deleteGalleryContentRequest.addOnOnResponse(this);
            deleteGalleryContentRequest.doRequest(accesToken);
        } else {
            contentDetailView.showErrorRemovingContent();
        }
    }

    @Override
    public void onResponseGetUserPhotoRequest(Uri photo, String userID, int viewID, int contactType) {
        usersDb.setPathAvatarToUser(Integer.valueOf(userID), photo.getPath());
        if (this.userId == Integer.valueOf(userID) && contentDetailView.getAvatarPath().equals("placeholder")) {
            contentDetailView.showAvatar(photo.getPath());
        }
    }

    @Override
    public void onFailureGetUserPhotoRequest(Object error, String userID, int viewID, int contactType) {
        contentDetailView.showError(error);
    }

    @Override
    public void onResponseDeleteGalleryContentRequest(int contentID) {
        String path = contentToBeDeleted.getPath();
        galleryDb.deleteContentGalleryByID(contentID);

        boolean isRemoveSucced = Media.deleteFile(path);
        Log.d("glry","onResponseDeleteGalleryContentRequest, isSucceed:"+isRemoveSucced);
        if (isRemoveSucced) {
            contentDetailView.removedContent();
        } else {
            contentDetailView.showErrorRemovingContent();
        }
    }

    @Override
    public void onFailureDeleteGalleryContentRequest(Object error, int contentID) {
        Log.d("glry","onFailureDeleteGalleryContentRequest,:");
        contentDetailView.showError(error);
    }
}
