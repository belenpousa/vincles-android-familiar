package cat.bcn.vincles.mobile.Client.Model.Serializers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import cat.bcn.vincles.mobile.Client.Model.Circle;
import cat.bcn.vincles.mobile.Client.Model.CircleUser;
import cat.bcn.vincles.mobile.Client.Model.GetUser;
import cat.bcn.vincles.mobile.Client.Model.UserCircle;

public class UserCircleDeserializer implements JsonDeserializer<UserCircle> {
    @Override
    public UserCircle deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        String relationship = jsonObject.get("relationship").getAsString();
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        Circle circle = gson.fromJson(jsonObject.get("circle"), Circle.class);

        return new UserCircle(relationship, circle);
    }
}
