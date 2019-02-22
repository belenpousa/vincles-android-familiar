package cat.bcn.vincles.mobile.Client.Db.Model;

import io.realm.RealmObject;

public class GalleryContentRealm extends RealmObject {

    private int id;
    private int idContent;
    private String mimeType;
    private int userId;
    private long inclusionTime;
    private String path = "";

    public GalleryContentRealm() {

    }

    public GalleryContentRealm(int id, int idContent, String mimeType, int userId, long inclusionTime) {
        this.id = id;
        this.idContent = idContent;
        this.mimeType = mimeType;
        this.userId = userId;
        this.inclusionTime = inclusionTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public long getInclusionTime() {
        return inclusionTime;
    }

    public void setInclusionTime(long inclusionTime) {
        this.inclusionTime = inclusionTime;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getIdContent() {
        return idContent;
    }

    public void setIdContent(int idContent) {
        this.idContent = idContent;
    }
}
