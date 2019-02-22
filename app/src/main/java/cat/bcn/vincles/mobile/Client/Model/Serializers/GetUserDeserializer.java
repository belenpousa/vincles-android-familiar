package cat.bcn.vincles.mobile.Client.Model.Serializers;

import android.util.Log;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import cat.bcn.vincles.mobile.Client.Model.GetUser;

public class GetUserDeserializer implements JsonDeserializer<GetUser> {
    @Override
    public GetUser deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        JsonObject jsonObject = json.getAsJsonObject();

        int id;
        if (jsonObject.get("id") == null) {
            id = jsonObject.get("userId").getAsInt();
        } else {
            id = jsonObject.get("id").getAsInt();
        }
        String name = jsonObject.get("name").getAsString();
        String lastname = jsonObject.get("lastname").getAsString();
        String alias = jsonObject.get("alias")==null || jsonObject.get("alias").equals(JsonNull.INSTANCE) ? null : jsonObject.get("alias").getAsString();
        String gender = jsonObject.get("gender")==null || jsonObject.get("gender").equals(JsonNull.INSTANCE) ? null : jsonObject.get("gender").getAsString();
        int idContentPhoto = jsonObject.get("idContentPhoto") == null || jsonObject.get("idContentPhoto").equals(JsonNull.INSTANCE) ? 0
                : jsonObject.get("idContentPhoto").getAsInt();

        JsonObject photoJson = jsonObject.getAsJsonObject("photo");
        Integer idContent = (photoJson == null || photoJson.get("idContent")==null
                || photoJson.get("idContent").equals(JsonNull.INSTANCE))
            ? null : photoJson.get("idContent").getAsInt();
        String photo = photoJson == null || photoJson.get("photo") == null || photoJson.get("photo").toString().equals("null") ? "": photoJson.get("photo").toString();
        String photoMimeType = photoJson == null || photoJson.get("photoMimeType")==null || photoJson.get("photoMimeType").toString().equals("null") ? "": photoJson.get("photoMimeType").toString();
        Boolean active = (jsonObject.get("active") == null || jsonObject.get("active").equals(JsonNull.INSTANCE)) ? null : jsonObject.get("active").getAsBoolean();

        int idInstallation = (jsonObject.get("idInstallation")==null ||
                jsonObject.get("idInstallation").equals(JsonNull.INSTANCE)) ? 0
                : jsonObject.get("idInstallation").getAsInt();
        int idCircle = (jsonObject.get("idCircle")==null ||
                jsonObject.get("idCircle").equals(JsonNull.INSTANCE)) ? 0
                : jsonObject.get("idCircle").getAsInt();
        int idLibrary  = (jsonObject.get("idLibrary")==null ||
                jsonObject.get("idLibrary").equals(JsonNull.INSTANCE)) ? 0
            : jsonObject.get("idLibrary").getAsInt();
        int idCalendar = (jsonObject.get("idCalendar")==null ||
                jsonObject.get("idCalendar").equals(JsonNull.INSTANCE)) ? 0
            : jsonObject.get("idCalendar").getAsInt();
        String username = (jsonObject.get("username")==null ||
                jsonObject.get("username").equals(JsonNull.INSTANCE)) ? null
            : jsonObject.get("username").getAsString();
        long birthdate = (jsonObject.get("birthdate")==null ||
                jsonObject.get("birthdate").equals(JsonNull.INSTANCE)) ? 0
            : jsonObject.get("birthdate").getAsLong();
        String email = (jsonObject.get("email")==null ||
                jsonObject.get("email").toString().equals("null")) ? null : jsonObject.get("email").getAsString();
        String phone = (jsonObject.get("phone")==null
                || jsonObject.get("phone").equals(JsonNull.INSTANCE)) ? null
            : jsonObject.get("phone").getAsString();
        Boolean liveInBarcelona = (jsonObject.get("liveInBarcelona") == null
                || jsonObject.get("liveInBarcelona").equals(JsonNull.INSTANCE)) ? null
            : jsonObject.get("liveInBarcelona").getAsBoolean();
        Log.d("meetfa","deserialize OK id:"+id);

        return new GetUser(id,name,lastname,alias,gender,idContentPhoto,idContent,photo,photoMimeType,active,idInstallation,idCircle,idLibrary,idCalendar,username,birthdate,email,phone,liveInBarcelona);
    }
}
