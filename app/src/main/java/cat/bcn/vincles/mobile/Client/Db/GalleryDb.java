package cat.bcn.vincles.mobile.Client.Db;


import android.content.Context;
import android.util.Log;

import cat.bcn.vincles.mobile.Client.Db.Model.GalleryContentRealm;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class GalleryDb extends BaseDb{

    Context context;

    public GalleryDb (Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public void dropTable() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<GalleryContentRealm> content = findAll();
        realm.beginTransaction();
        content.deleteAllFromRealm();
        realm.commitTransaction();

    }

    public void insertContent(GalleryContentRealm galleryContentRealm) {
        Realm realm = Realm.getDefaultInstance();
        if (!existsContentById(galleryContentRealm.getId())) {
            realm.beginTransaction();
            realm.copyToRealm(galleryContentRealm);
            realm.commitTransaction();
        }
    }

    public boolean existsContentById(int id) {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<GalleryContentRealm> galleryContentRealmsList = realm.where(GalleryContentRealm.class)
                .equalTo("id", id)
                .findAll();

        return galleryContentRealmsList.size() > 0;
    }

    public GalleryContentRealm getContentById(int id) {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<GalleryContentRealm> galleryContentRealmsList = realm.where(GalleryContentRealm.class)
                .equalTo("id", id)
                .findAll();

        return galleryContentRealmsList.first();
    }

    public int getIdContentFromId(int id) {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(GalleryContentRealm.class)
                .equalTo("id", id)
                .findFirst().getIdContent();
    }

    public int getIdFromIdContent(int idContent) {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(GalleryContentRealm.class)
                .equalTo("idContent", idContent)
                .findFirst().getId();
    }

    public String getPathFromIdContent(int idContent) {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<GalleryContentRealm> results = realm.where(GalleryContentRealm.class)
                .equalTo("idContent", idContent)
                .findAll();
        for (GalleryContentRealm contentRealm : results) {
            if (contentRealm.getPath() != null && contentRealm.getPath().length() > 0) {
                return contentRealm.getPath();
            }
        }

        return null;
    }

    public void setPathFromIdContent(int idContent, String path) {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<GalleryContentRealm> results = realm.where(GalleryContentRealm.class)
                .equalTo("idContent", idContent)
                .findAll();
        for (GalleryContentRealm contentRealm : results) {
            realm.beginTransaction();
            contentRealm.setPath(path);
            realm.commitTransaction();
        }
    }

    public RealmResults<GalleryContentRealm> findContentNotDownloaded() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<GalleryContentRealm> galleryContentRealmsList = realm.where(GalleryContentRealm.class)
                .equalTo("path", "")
                .findAll();

        return galleryContentRealmsList;
    }

    public String[] findAllPaths() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<GalleryContentRealm> galleryContentRealmsList = realm.where(GalleryContentRealm.class)
                .sort("inclusionTime", Sort.DESCENDING)
                .findAll();

        String[] contentsPath =  new String[galleryContentRealmsList.size()];
        for (int i = 0; i < galleryContentRealmsList.size(); i++) {
            contentsPath[i] = galleryContentRealmsList.get(i).getPath();
        }

        return contentsPath;
    }

    public RealmResults<GalleryContentRealm> findAll() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<GalleryContentRealm> galleryContentRealmsList = realm.where(GalleryContentRealm.class)
                .sort("inclusionTime", Sort.DESCENDING)
                .findAll();

        return galleryContentRealmsList;
    }

    public void setPathToFile(int id, String path) {
        Realm realm = Realm.getDefaultInstance();
        GalleryContentRealm galleryContentRealm = realm.where(GalleryContentRealm.class)
                .equalTo("id", id)
                .findFirst();
        if (galleryContentRealm != null) {
            realm.beginTransaction();
            galleryContentRealm.setPath(path);
            realm.commitTransaction();
        }
    }

    public String getPathToFile(int id) {
        Realm realm = Realm.getDefaultInstance();
        String path = "";
        GalleryContentRealm galleryContentRealm = realm.where(GalleryContentRealm.class)
                .equalTo("id", id)
                .findFirst();
        if (galleryContentRealm != null) {
            realm.beginTransaction();
            path = galleryContentRealm.getPath();
            realm.commitTransaction();
        }
        return path;
    }

    public void deleteContentGalleryByID(int contentID) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmResults<GalleryContentRealm> galleryContentRealm = realm.where(GalleryContentRealm.class)
                .equalTo("id", contentID)
                .findAll();
        galleryContentRealm.deleteAllFromRealm();
        realm.commitTransaction();

    }

    public RealmResults<GalleryContentRealm> getContentsPathByUserID () {
        UserPreferences userPreferences = new UserPreferences(context);
        int id = userPreferences.getUserID();
        Log.d("flt","getContentsPathByUserID, userID:"+id);
        Realm realm = Realm.getDefaultInstance();
        RealmResults<GalleryContentRealm> galleryContentRealm = realm.where(GalleryContentRealm.class)
                .sort("inclusionTime", Sort.DESCENDING)
                .equalTo("userId",id)
                .findAll();

        return  galleryContentRealm;
    }

    public RealmResults<GalleryContentRealm> getRecivedContentsPath () {
        UserPreferences userPreferences = new UserPreferences(context);
        int logedUserid = userPreferences.getUserID();
        Log.d("flt","getRecivedContentsPath, userID:"+logedUserid);
        Realm realm = Realm.getDefaultInstance();
        RealmResults<GalleryContentRealm> galleryContentRealm = realm.where(GalleryContentRealm.class)
                .sort("inclusionTime", Sort.DESCENDING)
                .notEqualTo("userId",logedUserid)
                .findAll();

        return galleryContentRealm;
    }
}
