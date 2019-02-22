package cat.bcn.vincles.mobile.Client.Model;

import com.google.gson.annotations.JsonAdapter;

import java.util.List;

import cat.bcn.vincles.mobile.Client.Model.Serializers.GalleryContentsDeserializer;

@JsonAdapter(GalleryContentsDeserializer.class)
public class GalleryContents {
    List<GalleryContent> galleryContents;

    public GalleryContents(List<GalleryContent> galleryContents) {
        this.galleryContents = galleryContents;
    }

    public List<GalleryContent> getGalleryContents() {
        return galleryContents;
    }

    public void setGalleryContents(List<GalleryContent> galleryContents) {
        this.galleryContents = galleryContents;
    }
}
