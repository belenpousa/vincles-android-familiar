package cat.bcn.vincles.mobile.Client.Model.Serializers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import cat.bcn.vincles.mobile.Client.Model.GetUser;
import cat.bcn.vincles.mobile.Client.Model.GuestRest;
import cat.bcn.vincles.mobile.Client.Model.MeetingUserInfoRest;

public class GuestRestDeserializer implements JsonDeserializer<GuestRest> {

    @Override
    public GuestRest deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        JsonObject jsonObject = json.getAsJsonObject();

        String state = !jsonObject.get("state").equals(JsonNull.INSTANCE) ? jsonObject.get("state").getAsString() : null;
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        GetUser user = gson.fromJson(jsonObject.get("userInfo"), GetUser.class);

        return new GuestRest(user, state);
    }

}
