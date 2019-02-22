package cat.bcn.vincles.mobile.UI.Gallery;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.gson.JsonObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import cat.bcn.vincles.mobile.Client.Business.Media;
import cat.bcn.vincles.mobile.Client.Db.GalleryDb;
import cat.bcn.vincles.mobile.Client.Db.Model.GalleryContentRealm;
import cat.bcn.vincles.mobile.Client.Db.UsersDb;
import cat.bcn.vincles.mobile.Client.Model.GalleryContent;
import cat.bcn.vincles.mobile.Client.Model.GalleryContents;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Client.Requests.AddContentInTheGallery;
import cat.bcn.vincles.mobile.Client.Requests.BaseRequest;
import cat.bcn.vincles.mobile.Client.Requests.DeleteGalleryContentRequest;
import cat.bcn.vincles.mobile.Client.Requests.GalleryAddContentRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetGalleryContentRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetGalleryContentsRequest;
import cat.bcn.vincles.mobile.Utils.ImageUtils;
import cat.bcn.vincles.mobile.Utils.MyApplication;
import cat.bcn.vincles.mobile.Utils.OtherUtils;
import io.realm.RealmResults;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class GallerypPresenter implements GalleryPresenterContract,
        GetGalleryContentsRequest.OnResponse, GetGalleryContentRequest.OnResponse,
        GalleryAddContentRequest.OnResponse, AddContentInTheGallery.OnResponse,
        DeleteGalleryContentRequest.OnResponse {

    UserPreferences userPreferences;
    GalleryDb galleryDb;
    UsersDb usersDb;
    GalleryView galleryView;
    int numberContentToDownload = 0 ;
    int numberContentToDownloaded = 0 ;
    Context context;
    ImageUtils imageUtils;
    private int idPhoto;
    private File imageFile;
    private ArrayList<Integer> itemsSelected;
    int numberItemsSelectedResponseCorrect = 0;
    private List<Integer> itemsSelectedResponse;
    private boolean isInSelectionMode = false;
    private List<GalleryContentRealm> galleryContentRealmList;
    private List<Integer> contentRequested;

    private String filterKind = FILTER_ALL_FILES;
    boolean addedAnyNewPicutre = false;
    BaseRequest.RenewTokenFailed listener;

    public GallerypPresenter(BaseRequest.RenewTokenFailed listener, Context context, GalleryView galleryView,
                             UserPreferences userPreferences, GalleryDb galleryDb, UsersDb usersDb,
                             @Nullable ArrayList<Integer> itemsSelected, String filterKind,
                             boolean isInSelectionMode) {
        Log.d("filtfa","onCreate presenter");
        this.context = context;
        this.listener = listener;
        if (filterKind != null) this.filterKind = filterKind;
        this.galleryView = galleryView;
        this.userPreferences = userPreferences;
        this.galleryDb = galleryDb;
        this.usersDb = usersDb;
        imageUtils = new ImageUtils();
        if (itemsSelected == null) {
            this.itemsSelected = new ArrayList<>();
        } else {
            this.itemsSelected = itemsSelected;
        }
        this.isInSelectionMode = isInSelectionMode;
        itemsSelectedResponse = new ArrayList<>();
        contentRequested = new ArrayList<>();
        galleryContentRealmList = galleryDb.findAll();


    }

    @Override
    public void onCreateView() {
        if (isInSelectionMode && this.itemsSelected.size() == 1) {
            Log.d("glerysel","create presenter, enabled true");
            galleryView.updateEnabledButtons(true);
        } else if (isInSelectionMode && this.itemsSelected.size() == 0) {
            Log.d("glerysel","create presenter, enabled false");
            galleryView.updateEnabledButtons(false);
        }
    }

    @Override
    public void getContent(long to) {
        String accessToken = userPreferences.getAccessToken();
        GetGalleryContentsRequest getContentRequest = new GetGalleryContentsRequest(listener, to);
        getContentRequest.addOnOnResponse(this);
        getContentRequest.doRequest(accessToken);
    }

    @Override
    public void pushImageToAPI(Object picture, boolean isUri) {
        File file;
        if (isUri) {
            file = new File(((Uri)picture).getPath());
        } else {
            /*Bitmap photo = (Bitmap) picture;
            Uri photoUri = imageUtils.getImageUri(context, photo);
            galleryView.savingFilePictureIsUri(photoUri);
            file = new File(photoUri.getPath());*/
            file = new File((String)picture);
        }
        addContentToGallery(file, "image/jpeg");
    }

    @Override
    public void pushVideoToAPI(Uri fileUri) {
        File file = new File(fileUri.getPath());
        Log.d("vidrc","pushVideoToAPI exists?"+file.exists()+", filePath:"+fileUri.getPath());
        addContentToGallery(file, "video/mp4");
    }

    @Override
    public void saveImage(int id) {
        int userID = userPreferences.getUserID();
        String mimeType = ImageUtils.getMimeType(imageFile.getPath());

        long inclusionTime;
        if (mimeType.contains("video")) {
            inclusionTime = System.currentTimeMillis();
        } else {
            String imageName = imageFile.getName();
            String name = imageName.split("_")[0];
            inclusionTime = Long.valueOf(name.replace(".jpg",""));
        }

        if (new UserPreferences().getIsCopyPhotos()){
            ImageUtils.saveMediaExternalMemory(this.context,imageFile.getName(),imageFile.getPath());
        }

        GalleryContentRealm galleryContentRealm = new GalleryContentRealm(id, idPhoto,
                mimeType, userID,inclusionTime);
        String absolutePath = imageFile.getAbsolutePath();
        galleryContentRealm.setPath(absolutePath);
        galleryDb.insertContent(galleryContentRealm);
        galleryView.onFileAdded();
        galleryView.closeAlertSavingImage();
    }

    private void addContentToGallery (File file, String mediaType) {
        this.imageFile = file;
        String imageFileName = imageFile.getName();

        RequestBody fileB = RequestBody.create(MediaType.parse(mediaType), imageFile);
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("file", imageFileName, fileB);

        Log.d("vidrc","addContentToGallery request:"+body);
        String accessToken = userPreferences.getAccessToken();
        GalleryAddContentRequest galleryAddContentRequest = new GalleryAddContentRequest(listener, body);
        galleryAddContentRequest.addOnOnResponse(this);
        galleryAddContentRequest.doRequest(accessToken);
    }

    public void filterMedia(String filterKind) {
        Log.d("filtfa","filterMedia presenter");
        this.filterKind = filterKind;
        galleryView.showGalleryContent(getFilteredMedia(filterKind));
    }

    public RealmResults<GalleryContentRealm> getFilteredMedia(String filterKind) {
        Log.d("filtfa","getFilteredMedia presenter");
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
    public void itemSelected(int contentID, int index) {
        Log.d("glery"," item selected list before:"+itemsSelected.toString());
        boolean added = true;
        if (itemsSelected.contains(contentID)) {
            itemsSelected.removeAll(Arrays.asList(contentID));
            added = false;
        } else {
            itemsSelected.add(contentID);
        }
        Log.d("glerysel","item selected, added:"+added+" size:"+this.itemsSelected.size());
        if (isInSelectionMode && added && this.itemsSelected.size() == 1) {
            galleryView.updateEnabledButtons(true);
        } else if (isInSelectionMode && !added && this.itemsSelected.size() == 0) {
            galleryView.updateEnabledButtons(false);
        }
        Log.d("glery"," item selected list after:"+itemsSelected.toString());
    }

    public boolean isInSelectionMode() {
        return isInSelectionMode;
    }

    @Override
    public void setInSelectionMode(boolean inSelectionMode) {
        isInSelectionMode = inSelectionMode;
    }

    @Override
    public void onResponseGetGalleryContentsRequest(GalleryContents galleryContents) {

        /**boolean isLastContentInGallery = true;
        List<GalleryContent> galleryContentList = galleryContents.getGalleryContents();
        if (galleryContentList.size() > 0) {
            int lasGalleryContentId = galleryContentList.get(galleryContentList.size()-1).getId();
            isLastContentInGallery = galleryDb.existsContentById(lasGalleryContentId);

            for (GalleryContent galleryContent :galleryContentList) {
                if (!galleryDb.existsContentById(galleryContent.getId())) {
                    galleryDb.insertContent(galleryContent);
                    addedAnyNewPicutre = true;//TODO guardar al rotar
                }
                if (galleryContent.getPath()== null || galleryContent.getPath().equals("")) {
                    missingPhoto = true;
                }
            }
        }
        Log.d("glry","onResponseGetGalleryContentsRequest");

        //Server returns results in groups of 10. Several requests need to be done
        if (!isLastContentInGallery) {
            long InclusionTime = galleryContentList.get(galleryContentList.size()-1).getInclusionTime();
            String accessToken = userPreferences.getAccessToken();
            GetGalleryContentsRequest getContentRequest = new GetGalleryContentsRequest(listener, InclusionTime);
            getContentRequest.addOnOnResponse(this);
            getContentRequest.doRequest(accessToken);
        } else { //Finally, all info obtained
            downloadContentNotDownloaded();
            if (addedAnyNewPicutre) {
                addedAnyNewPicutre = false;
                galleryView.updateContents(getFilteredMedia(filterKind));
            }
        }
        Log.d("glry","fotos request, addedNew?"+addedAnyNewPicutre); **/
        boolean isLastContentInGallery = true;
        if (galleryContents != null && galleryContents.getGalleryContents() != null && galleryContents.getGalleryContents().size() > 0) {
            int lasGalleryContentId = galleryContents.getGalleryContents().get(galleryContents.getGalleryContents().size() - 1).getId();
            isLastContentInGallery = galleryDb.existsContentById(lasGalleryContentId);
        }

        if (galleryContentRealmList == null) {
            galleryContentRealmList = new ArrayList<>();
        }
        for (GalleryContent galleryContent : galleryContents.getGalleryContents()) {
            GalleryContentRealm galleryContentRealm = new GalleryContentRealm(
                    galleryContent.getId(), galleryContent.getIdContent(),
                    galleryContent.getMimeType(), galleryContent.getUser().getId(),
                    galleryContent.getInclusionTime());
            galleryContentRealmList.add(galleryContentRealm);
            galleryDb.insertContent(galleryContentRealm);
        }
        for (GalleryContentRealm galleryContentRealm : galleryContentRealmList) {
            galleryContentRealm.setPath(galleryDb.getPathToFile(galleryContentRealm.getId()));
        }

        galleryView.showGalleryContent(galleryContentRealmList);


    }

    @Override
    public void getGalleryPathByContentId(int id, int contentID) {
        Log.d("glpag","get Content, is<4? "+(contentRequested.size() < 4));
        if (!contentRequested.contains(Integer.valueOf(contentID))
                && "".equals(galleryDb.getPathToFile(id))
                && contentRequested.size() < 5) {

            String path = galleryDb.getPathFromIdContent(contentID);
            if (path == null) {
                contentRequested.add(contentID);
                GalleryContentRealm galleryContentRealm = galleryDb.getContentById(id);
                String mimeType = galleryContentRealm.getMimeType();
                Log.d("galchng","GetGalleryContentRequest, contentID:"+galleryContentRealm.getIdContent());
                GetGalleryContentRequest getGalleryContentRequest = new GetGalleryContentRequest(listener, context, String.valueOf(contentID), mimeType);
                getGalleryContentRequest.addOnOnResponse(this);
                getGalleryContentRequest.doRequest(userPreferences.getAccessToken());
            } else {
                //Another gallery item links to the same file so we reuse it instead of downloading
                galleryDb.setPathFromIdContent(contentID, path);
                galleryView.onFileAdded();
            }

        }
    }

    @Override
    public void onFailureGetGalleryContentsRequest(Object error) {
        galleryView.showErrorMessage(error);
    }

    @Override
    public void onResponseGetGalleryContentRequest(String contentID, String filePath) {
        Log.d("galchng","onResponseGetGalleryContentRequest, contentID:"+contentID);
        contentRequested.remove((Integer)Integer.parseInt(contentID));
        galleryDb.setPathFromIdContent(Integer.valueOf(contentID), filePath);


        //store on external SD if user wants to
        if (userPreferences.getIsCopyPhotos() && galleryView.checkWriteExternalStoragePermission()) {
            ImageUtils.safeCopyFileToExternalStorage(filePath, contentID);
        }

        /**for (GalleryContentRealm galleryContentRealm : galleryContentRealmList) {
            if (String.valueOf(galleryContentRealm.getId()).equals(contentID)) {
                galleryContentRealm.setPath(filePath);
            }
        }**/
        /*if (numberContentToDownload == numberContentToDownloaded) {
            //galleryView.showGalleryContent();
            galleryView.updateContents(getFilteredMedia(filterKind));
        }*/

        //galleryView.updateContents(getFilteredMedia(filterKind));

        //galleryView.showGalleryContent(galleryContentRealmList);
        galleryView.onFileAdded();
    }

    @Override
    public void onFailureGetGalleryContentRequest(Object error) {
        galleryView.showErrorMessage(error);
    }

    @Override
    public void onResponseGalleryAddContentRequest(JsonObject galleryAddedContent) {
        idPhoto = galleryAddedContent.getAsJsonObject().get("id").getAsInt();

        Log.d("vidrc","onResponseGalleryAddContentRequest request:"+galleryAddedContent.toString());

        AddContentInTheGallery addContentInTheGallery = new AddContentInTheGallery(listener, idPhoto);
        addContentInTheGallery.addOnOnResponse(this);
        addContentInTheGallery.doRequest(userPreferences.getAccessToken());
    }

    @Override
    public void deleteSelectedContent() {
        Log.d("glry","deleteSelectedContent");
        itemsSelectedResponse.clear();
        String accessToken = userPreferences.getAccessToken();
        for (int i = 0; i < itemsSelected.size(); i++) {
            Log.d("glry","deleteSelectedContent item "+i+" id:"+itemsSelected.get(i));
            DeleteGalleryContentRequest deleteGalleryContentRequest =
                    new DeleteGalleryContentRequest(listener, itemsSelected.get(i));
            deleteGalleryContentRequest.addOnOnResponse(this);
            deleteGalleryContentRequest.doRequest(accessToken);
        }
    }

    @Override
    public void onResponseDeleteGalleryContentRequest(final int contentID) {
        numberItemsSelectedResponseCorrect++;
        Log.d("glry","remove responseOK id:"+contentID);
        itemsSelected.removeAll(Arrays.asList(contentID));

        //Delete from device
        new Thread(new Runnable() {
            @Override
            public void run() {
                String path = galleryDb.getContentById(contentID).getPath();
                galleryDb.deleteContentGalleryByID(contentID);
                Media.deleteFile(path);
            }
        }).start();

        onNewDeleteResponse();
    }

    @Override
    public void onFailureDeleteGalleryContentRequest(Object error, int contentID) {
        Log.d("glry","remove responseFAIL id:"+contentID);
        itemsSelectedResponse.add(contentID);
        onNewDeleteResponse();
    }

    synchronized private void onNewDeleteResponse() {
        Log.d("glry","onNewDeleteResponse Number:"+numberItemsSelectedResponseCorrect+" size:"+itemsSelectedResponse.size()
        +" size2:"+itemsSelected.size());
        if (itemsSelectedResponse.size() == itemsSelected.size()) {
            if (itemsSelected.size() == 0) {
                isInSelectionMode = false;
                galleryView.onUpdateIsInSelectionMode();
                galleryView.onDeleteResults(GalleryView.DELETE_OK);
            } else if (numberItemsSelectedResponseCorrect == 0) {
                galleryView.onDeleteResults(GalleryView.DELETE_NOT_OK);
            } else {
                galleryView.onDeleteResults(GalleryView.DELETE_PARTIALLY_OK);
            }
            itemsSelectedResponse.clear();
            numberItemsSelectedResponseCorrect = 0;
        }
    }

    @Override
    public void onFailureGalleryAddContentRequest(Object error) {
        Log.d("vidrc","onFailureGalleryAddContentRequest request result:"+error);
        galleryView.closeAlertSavingImage();
        galleryView.showErrorSavingFile(error);
    }

    @Override
    public void onResponseAddContentInTheGallery(int id) {
        saveImage(id);
        galleryView.savingFileOk();
    }

    @Override
    public void onFailureAddContentInTheGallery(Object error) {
        Log.d("vidrc","onFailureAddContentInTheGallery request result:"+error);
        galleryView.closeAlertSavingImage();
        galleryView.showErrorSavingFile(error);
    }


    public ArrayList<Integer> getItemsSelected() {
        return itemsSelected;
    }

    public String getFilterKind() {
        return filterKind;
    }

    public void onShareSelectionModeClicked() {

        ArrayList<Integer> items = getItemsSelected();
        ArrayList<String> paths = new ArrayList<>();
        ArrayList<String> metadatas = new ArrayList<>();

        for (Integer id : items) {
            for (GalleryContentRealm galleryContentRealm : galleryContentRealmList) {
                if (galleryContentRealm.getId() == id) {
                    paths.add(galleryContentRealm.getPath());
                    metadatas.add(galleryContentRealm.getMimeType());
                    break;
                }
            }
        }

        galleryView.onShareContentSelectionMode(items, paths, metadatas);
    }

}
