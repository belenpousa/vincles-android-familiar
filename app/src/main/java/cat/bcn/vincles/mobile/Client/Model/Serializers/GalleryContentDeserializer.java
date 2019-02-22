package cat.bcn.vincles.mobile.Client.Model.Serializers;


import android.util.Log;

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

public class GalleryContentDeserializer implements JsonDeserializer<GalleryContent> {
    @Override
    public GalleryContent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        JsonObject jsonObject = json.getAsJsonObject();

        int id = jsonObject.get("id").equals(JsonNull.INSTANCE) ? -1 : jsonObject.get("id").getAsInt();
        int idContentGallery = jsonObject.get("idContent").equals(JsonNull.INSTANCE) ? -1 : jsonObject.get("idContent").getAsInt();
        if (id == -1 || idContentGallery == -1) {
            Log.d("logdown"," deserialize gallery contrent, id:"+id+", idContentGallery:"+idContentGallery);
        }
        String mimeType = jsonObject.get("mimeType").getAsString();
        String tag = jsonObject.get("tag").equals(JsonNull.INSTANCE) ? null : jsonObject.get("tag").getAsString();
        long inclusionTime = jsonObject.get("inclusionTime").getAsLong();
        JsonObject userCreatorJson = jsonObject.get("userCreator").getAsJsonObject();

        int idUser = userCreatorJson.get("id").getAsInt();
        String name = userCreatorJson.get("name").getAsString();
        String lastname = userCreatorJson.get("lastname").getAsString();
        String alias = userCreatorJson.get("alias").getAsString();
        String gender = userCreatorJson.get("gender").getAsString();

        GetUser getUser = new GetUser(idUser, name, lastname, alias, gender/*, idContentPhoto, idContent, photo, photoMimeType, active*/);

        return new GalleryContent(id, idContentGallery, mimeType, getUser,
                inclusionTime, tag);
    }
}
