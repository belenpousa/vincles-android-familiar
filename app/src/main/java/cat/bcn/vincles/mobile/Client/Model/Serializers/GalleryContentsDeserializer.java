package cat.bcn.vincles.mobile.Client.Model.Serializers;


import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Model.GalleryContent;
import cat.bcn.vincles.mobile.Client.Model.GalleryContents;
import cat.bcn.vincles.mobile.Client.Model.GetUser;

public class GalleryContentsDeserializer implements JsonDeserializer<GalleryContents> {
    @Override
    public GalleryContents deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        List<GalleryContent> galleryContentsList = new ArrayList<>();

        JsonArray gallerContentsJsonArr = json.getAsJsonArray();
        for ( JsonElement jsonElement : gallerContentsJsonArr) {

            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            GalleryContent galleryContent = gson.fromJson(jsonObject, GalleryContent.class);
            galleryContentsList.add(galleryContent);

        }
        return new GalleryContents(galleryContentsList);
    }
}
