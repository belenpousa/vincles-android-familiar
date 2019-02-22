package cat.bcn.vincles.mobile.Client.Model.Serializers;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import cat.bcn.vincles.mobile.Client.Model.GetUser;

public class AddUserDeserializer implements JsonDeserializer<AddUser> {

    @Override
    public AddUser deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        String relationship = jsonObject.get("relationship").getAsString();
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        GetUser user = gson.fromJson(jsonObject.get("userVincles"), GetUser.class);

        return new AddUser(relationship, user);
    }
}
