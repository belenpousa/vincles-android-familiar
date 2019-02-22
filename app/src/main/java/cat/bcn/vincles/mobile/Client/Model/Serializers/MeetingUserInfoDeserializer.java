package cat.bcn.vincles.mobile.Client.Model.Serializers;

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

import cat.bcn.vincles.mobile.Client.Model.ChatMessageRest;
import cat.bcn.vincles.mobile.Client.Model.MeetingRest;
import cat.bcn.vincles.mobile.Client.Model.MeetingUserInfoRest;

public class MeetingUserInfoDeserializer implements JsonDeserializer<MeetingUserInfoRest> {

    @Override
    public MeetingUserInfoRest deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        JsonObject jsonObject = json.getAsJsonObject();

        int id = jsonObject.get("id").getAsInt();
        String name = !jsonObject.get("name").equals(JsonNull.INSTANCE) ? jsonObject.get("text").getAsString() : "";
        String lastname = !jsonObject.get("lastname").equals(JsonNull.INSTANCE) ? jsonObject.get("text").getAsString() : "";
        int idContentPhoto = jsonObject.get("idContentPhoto").getAsInt();
        String state = !jsonObject.get("state").equals(JsonNull.INSTANCE) ? jsonObject.get("text").getAsString() : "";

        return new MeetingUserInfoRest(id, name, lastname, idContentPhoto, state);
    }

}
