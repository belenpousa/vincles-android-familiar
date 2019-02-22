package cat.bcn.vincles.mobile.Client.Model.Serializers;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Model.CircleUser;
import cat.bcn.vincles.mobile.Client.Model.CircleUsers;

public class CircleUsersDeserializer implements JsonDeserializer<CircleUsers> {
    @Override
    public CircleUsers deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        List<CircleUser> circleUsersList = new ArrayList<>();

        JsonArray getUsersJsonArr = json.getAsJsonArray();
        for ( JsonElement jsonElement : getUsersJsonArr) {

            JsonObject jsonObject = jsonElement.getAsJsonObject();

            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            CircleUser circleUser = gson.fromJson(jsonObject, CircleUser.class);
            circleUsersList.add(circleUser);

        }
        return new CircleUsers(circleUsersList);
    }
}
