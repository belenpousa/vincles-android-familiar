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

import cat.bcn.vincles.mobile.Client.Model.UserCircle;
import cat.bcn.vincles.mobile.Client.Model.UserCircles;
import cat.bcn.vincles.mobile.Client.Model.UserGroup;
import cat.bcn.vincles.mobile.Client.Model.UserGroups;

public class UserGroupsDeserializer implements JsonDeserializer<UserGroups> {
    @Override
    public UserGroups deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        List<UserGroup> userGroupsList = new ArrayList<>();

        JsonArray getUserGroupsJsonArr = json.getAsJsonArray();
        for ( JsonElement jsonElement : getUserGroupsJsonArr) {

            JsonObject jsonObject = jsonElement.getAsJsonObject();

            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            UserGroup userGroup = gson.fromJson(jsonObject, UserGroup.class);
            userGroupsList.add(userGroup);

        }
        return new UserGroups(userGroupsList);
    }
}
