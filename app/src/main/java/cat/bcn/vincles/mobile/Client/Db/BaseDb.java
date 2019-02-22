package cat.bcn.vincles.mobile.Client.Db;

import android.content.Context;

import cat.bcn.vincles.mobile.Client.Model.GalleryContent;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.Sort;

public abstract class BaseDb {

    public BaseDb(Context context) {
        Realm.init(context);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder().schemaVersion(1).build();
        Realm.setDefaultConfiguration(realmConfiguration);
    }

    public abstract void dropTable();

}
