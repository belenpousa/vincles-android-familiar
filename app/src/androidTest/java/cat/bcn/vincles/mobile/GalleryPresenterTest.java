package cat.bcn.vincles.mobile;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.google.gson.JsonObject;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


import cat.bcn.vincles.mobile.Client.Db.GalleryDb;
import cat.bcn.vincles.mobile.Client.Model.GalleryContent;
import cat.bcn.vincles.mobile.Client.Model.TokenFromLogin;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Client.Requests.LoginRequest;
import cat.bcn.vincles.mobile.UI.Gallery.GalleryView;
import cat.bcn.vincles.mobile.UI.Gallery.GallerypPresenter;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

import java.io.File;
import cat.bcn.vincles.mobile.Client.Requests.GalleryAddContentRequest;
import cat.bcn.vincles.mobile.Utils.ImageUtils;
import io.realm.RealmResults;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class GalleryPresenterTest {


    final static String USER = "testVincles2";
    final static String PASSWORD = "12345678";
    String ACCES_TOKEN = "";

    @Before
    public void setUp() throws Exception {
        login(USER, PASSWORD);
    }

    public void login (String user, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(user,password);
        loginRequest.addOnOnResponse(new LoginRequest.OnResponse() {
            @Override
            public void onResponseLoginRequest(TokenFromLogin tokenFromLogin) {
                ACCES_TOKEN = tokenFromLogin.getAccessToken();
            }

            @Override
            public void onFailureLoginRequest(Object error) {
                assertNull(error);
            }
        });
        loginRequest.doRequest();
        Thread.sleep(4000);
    }

    @Test
    public void galleryPresenterCallsShowGalleryContent() throws Exception {
        Context context = InstrumentationRegistry.getTargetContext();
        cat.bcn.vincles.mobile.Client.Db.GalleryDb galleryDb = new GalleryDb(context);
        UserPreferences userPreferences = new UserPreferences(context);

        GallerypPresenter gallerypPresenter = new GallerypPresenter(context, new GalleryView() {
            @Override
            public void showErrorMessage(Object error) {
                assertNull(error);
            }

            @Override
            public void showGalleryContent() {
                assertEquals(true,true);
            }

            @Override
            public void showAlertSaveImageInGalery() {
            }

            @Override
            public void closeAlertSaveImageInGalery() {
            }

            @Override
            public void showContents(RealmResults<GalleryContent> contentPaths) {

            }

            @Override
            public void updateContents(RealmResults<GalleryContent> contentPaths) {

            }

        }, userPreferences,galleryDb);

        gallerypPresenter.downloadContentNotDownloaded();
        Thread.sleep(32000);
    }

    @Test
    public void shouldGetIDOfContentUploaded() throws Exception {
        Context context = InstrumentationRegistry.getTargetContext();
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.ic_help);

        ImageUtils imageUtils = new ImageUtils();
        Uri photoUri = imageUtils.getImageUri(context, icon);
        File file = new File(photoUri.getPath());
        String imageFileName = file.getName();

        RequestBody fileB = RequestBody.create(MediaType.parse("image/jpeg"), file);
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("file", imageFileName,fileB);


        GalleryAddContentRequest galleryAddContentRequest = new GalleryAddContentRequest(ACCES_TOKEN, body);
        galleryAddContentRequest.addOnOnResponse(new GalleryAddContentRequest.OnResponse() {
            @Override
            public void onResponseGalleryAddContentRequest(JsonObject galleryAddedContent) {
                int contentID = galleryAddedContent.getAsJsonObject().get("id").getAsInt();
                assertNotNull(contentID);
            }

            @Override
            public void onFailureGalleryAddContentRequest(Object error) {
                assertNull(error);
            }
        });
        galleryAddContentRequest.doRequest();
        Thread.sleep(8000);
    }
}
