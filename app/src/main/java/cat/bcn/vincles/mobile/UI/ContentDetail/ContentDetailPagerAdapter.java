package cat.bcn.vincles.mobile.UI.ContentDetail;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import cat.bcn.vincles.mobile.Client.Db.Model.GalleryContentRealm;
import io.realm.RealmResults;

public class ContentDetailPagerAdapter extends FragmentStatePagerAdapter implements ContentDetailPagerFragment.DownloadRequest {

    private RealmResults<GalleryContentRealm> galleryContentsRealm;
    ContentDetailPagerFragment contentDetailPagerFragment;
    private DownloadRequest listener;

    public ContentDetailPagerAdapter(FragmentManager fragmentManager,
                                     RealmResults<GalleryContentRealm> galleryContentsRealm,
                                     DownloadRequest listener) {
        super(fragmentManager);
        this.galleryContentsRealm = galleryContentsRealm;
        this.listener = listener;
    }


    @Override
    public int getCount() {
        return galleryContentsRealm.size();
    }


    @Override
    public Fragment getItem(int position) {
        GalleryContentRealm galleryContent = galleryContentsRealm.get(position);
        contentDetailPagerFragment = ContentDetailPagerFragment.newInstance(galleryContent.getPath(),
                galleryContent.getMimeType(), position);
        contentDetailPagerFragment.setListener(this);
        return contentDetailPagerFragment;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;

    }

    @Override
    public void onDownloadRequest(int position) {
        if (listener != null) listener.onDownloadRequest(position);
    }

    interface DownloadRequest {
        void onDownloadRequest(int position);
    }
}
