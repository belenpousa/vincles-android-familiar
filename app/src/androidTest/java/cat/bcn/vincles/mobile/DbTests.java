package cat.bcn.vincles.mobile;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.junit.Test;

import cat.bcn.vincles.mobile.Client.Db.GalleryDb;
import cat.bcn.vincles.mobile.Client.Model.GalleryContent;
import cat.bcn.vincles.mobile.Client.Model.GetUser;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import io.realm.RealmResults;

import static org.junit.Assert.assertEquals;

public class DbTests {

    private GetUser getUser() {
        return new GetUser(11111,"fulano","de tal","pepito","MALE",
                111,2222,"photo","photoMimeType",true,
        123,123,132,123,"username",1046511791,"a@a.com",
        "123456",true);
    }

    @Test
    public void insertContentInGalleryDbGetBackAllTheContentInTheGalleryThatShouldContain() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        GalleryDb galleryDb = new GalleryDb(appContext);

        GetUser user = getUser();
        GalleryContent galleryContent = new GalleryContent(1,"image/jpeg",user,1519897391);
        String galleryContentPath = "fakepath1";
        galleryContent.setPath(galleryContentPath);

        galleryDb.insertContent(galleryContent);

        Boolean result = false;
        RealmResults<GalleryContent> allGalleryContentsPath = galleryDb.findAll();
        for (int i = 0; i < allGalleryContentsPath.size() && !result; i++) {
            if (allGalleryContentsPath.get(i).equals(galleryContentPath)) {
                result = true;
            }
        }
        assertEquals(true,result);
    }

    @Test
    public void insertContentInGalleryAndGetThatSpecificContent () throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        GalleryDb galleryDb = new GalleryDb(appContext);

        GetUser user = getUser();
        int gallertContetnID = 123;
        GalleryContent galleryContent = new GalleryContent(gallertContetnID,"image/jpeg",user,1519897391);
        String galleryContentPath = "fakepath2";
        galleryContent.setPath(galleryContentPath);

        galleryDb.insertContent(galleryContent);

        boolean result = galleryDb.existsContentById(gallertContetnID);
        assertEquals(true,result);
    }

    @Test
    public void insertContentInGalleryAsNotDownloadedAndGetItBackAsQueryAkingForAllContentsNotDownloaded () throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        GalleryDb galleryDb = new GalleryDb(appContext);

        GetUser user = getUser();
        int gallertContetnID = 123;
        GalleryContent galleryContent = new GalleryContent(gallertContetnID,"image/jpeg",user,1519897391);

        galleryDb.insertContent(galleryContent);

        RealmResults<GalleryContent> contentNotDownloaded = galleryDb.findContentNotDownloaded();
        boolean result = false;
        for (int i = 0; i < contentNotDownloaded.size() && !result; i++) {
            if (gallertContetnID == contentNotDownloaded.get(i).getId()) {
                result = true;
            }
        }
        assertEquals(true,result);
    }

    @Test
    public void setPathToAContentAlreadySavedAndCheckThatItsBeenSettingProperly () throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        GalleryDb galleryDb = new GalleryDb(appContext);

        GetUser user = getUser();
        int gallertContetnID = 123;
        GalleryContent galleryContent = new GalleryContent(gallertContetnID,"image/jpeg",user,1519897391);

        galleryDb.insertContent(galleryContent);
        String fakePath = "fakePath3";
        galleryDb.setPathToFile(gallertContetnID,fakePath);
        RealmResults<GalleryContent> contentPaht = galleryDb.getContentsPathByUserID();

        boolean result = false;
        for (int i = 0; i < contentPaht.size() && !result; i++) {
            if (fakePath.equals(contentPaht.get(i).getPath())) {
                result = true;
            }
        }
        assertEquals(true,result);
    }

    @Test
    public void insertContentInThaGalleryAsItWereSentByOtherUserAnGetThatContentBack () throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        int userId = 1;
        int otherUerId = 2;
        UserPreferences userPreferences = new UserPreferences(appContext);
        userPreferences.setUserID(userId);

        GetUser user = getUser();
        user.setId(otherUerId);

        GalleryDb galleryDb = new GalleryDb(appContext);
        GalleryContent galleryContent = new GalleryContent(123,"image/jpeg",user,1519897391);
        String fakePath = "fakePath4";
        galleryContent.setPath(fakePath);

        galleryDb.insertContent(galleryContent);

        RealmResults<GalleryContent> recivedContents = galleryDb.getRecivedContentsPath();
        boolean result = false;
        for (int i = 0; i < recivedContents.size() && !result; i++) {
            if (fakePath.equals(recivedContents.get(i).getPath())) {
                result = true;
            }
        }
        assertEquals(true,result);
    }

    @Test
    public void insertContentInThaGalleryAsItWereInsertByTheUserAndDontGetThatContentBackWhenDoingQueryForRecivedContetns () throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        int userId = 1;
        int otherUerId = 2;
        UserPreferences userPreferences = new UserPreferences(appContext);
        userPreferences.setUserID(userId);

        GetUser user = getUser();
        user.setId(otherUerId);

        GalleryDb galleryDb = new GalleryDb(appContext);
        GalleryContent galleryContent = new GalleryContent(123,"image/jpeg",user,1519897391);
        String fakePath = "fakePath5";

        galleryDb.insertContent(galleryContent);
        galleryDb.getRecivedContentsPath();

        galleryDb.getRecivedContentsPath();
        RealmResults<GalleryContent> recivedContents = galleryDb.getRecivedContentsPath();
        boolean result = false;
        for (int i = 0; i < recivedContents.size() && !result; i++) {
            if (fakePath.equals(recivedContents.get(i))) {
                result = true;
            }
        }
        assertEquals(false,result);
    }

}
