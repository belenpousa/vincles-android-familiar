package cat.bcn.vincles.mobile.Client.Model;

import com.google.gson.annotations.JsonAdapter;

import cat.bcn.vincles.mobile.Client.Model.Serializers.GalleryContentDeserializer;
import cat.bcn.vincles.mobile.Client.Model.Serializers.GalleryContentsDeserializer;

@JsonAdapter(GalleryContentDeserializer.class)
public class GalleryContent {

    public static final String TAG_IMAGES = "IMAGES";

    int id;
    int idContent;
    String mimeType;
    GetUser getUser;
    long inclusionTime;
    String path = "";
    String tag;

    public GalleryContent () {

    }

    public GalleryContent(int id, int idContent, String mimeType, GetUser getUser,
                          long inclusionTime, String tag) {
        this.id = id;
        this.idContent = idContent;
        this.mimeType = mimeType;
        this.getUser = getUser;
        this.inclusionTime = inclusionTime;
        this.tag = tag;
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

    public GetUser getUser() {
        return getUser;
    }

    public void setGetUser(GetUser getUser) {
        this.getUser = getUser;
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
