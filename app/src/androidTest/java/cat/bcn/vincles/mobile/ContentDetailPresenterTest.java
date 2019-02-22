package cat.bcn.vincles.mobile;


import android.content.Context;
import android.graphics.Bitmap;
import android.support.test.InstrumentationRegistry;

import org.junit.Test;

import cat.bcn.vincles.mobile.Client.Db.GalleryDb;
import cat.bcn.vincles.mobile.Client.Db.UsersDb;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.UI.ContentDetail.ContentDetailPresenter;
import cat.bcn.vincles.mobile.UI.ContentDetail.ContentDetailView;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ContentDetailPresenterTest {

    @Test
    public void onCallPickFilePathShouldCallLoadDetailImage() throws Exception {
        Context context = InstrumentationRegistry.getTargetContext();
        GalleryDb galleryDb =  new GalleryDb(context);
        UsersDb usersDb = new UsersDb(context);
        UserPreferences userPreferences = new UserPreferences(context);

        ContentDetailView contentDetailView = new MockContentDetailView();
        ContentDetailPresenter contentDetailPresenter = new ContentDetailPresenter(contentDetailView, galleryDb, usersDb, userPreferences);

        contentDetailPresenter.pickFilePath(0);
        assertEquals(true,((MockContentDetailView) contentDetailView).isLoadDetailImageOrErrorOpeneing);
    }

    @Test
    public void onCallDeleteContentShouldCallRemovedContentOrShowErrorRemovingContent() throws Exception {
        Context context = InstrumentationRegistry.getTargetContext();
        GalleryDb galleryDb =  new GalleryDb(context);
        UsersDb usersDb = new UsersDb(context);
        UserPreferences userPreferences = new UserPreferences(context);

        ContentDetailView contentDetailView = new MockContentDetailView();

        ContentDetailPresenter contentDetailPresenter = new ContentDetailPresenter(contentDetailView, galleryDb,usersDb,userPreferences);
        int fakeContentId = 0;
        contentDetailPresenter.deleteContent(fakeContentId);
        assertEquals(((MockContentDetailView) contentDetailView).isRemovedContentOrShowErrorRemovingContent, true);
    }

    class MockContentDetailView implements ContentDetailView {

        public boolean isRemovedContentOrShowErrorRemovingContent = false;
        public boolean isLoadDetailImageOrErrorOpeneing = false;

        @Override
        public void loadDetailImage(String path) {
            isLoadDetailImageOrErrorOpeneing = true;
        }

        @Override
        public void setOwnerName(String ownerName) {

        }

        @Override
        public void setDate(int day, int month, int year, int hour, int minute) {

        }

        @Override
        public void showAvatar(Bitmap bm) {

        }

        @Override
        public void showAvatar(String path) {

        }

        @Override
        public void showError(Object error) {

        }

        @Override
        public void showConfirmationRemoveContent() {

        }

        @Override
        public void removedContent() {
            isRemovedContentOrShowErrorRemovingContent = true;
        }

        @Override
        public void showErrorRemovingContent() {
            isRemovedContentOrShowErrorRemovingContent = true;
        }

        @Override
        public void showErrorOpeningImage() {
            isLoadDetailImageOrErrorOpeneing = true;
        }
    }

}
