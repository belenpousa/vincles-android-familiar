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

import cat.bcn.vincles.mobile.Client.Model.GetUser;
import cat.bcn.vincles.mobile.Client.Model.GroupUserList;
import cat.bcn.vincles.mobile.Client.Model.UserGroup;
import cat.bcn.vincles.mobile.Client.Model.UserGroups;

public class GroupUserListDeserializer implements JsonDeserializer<GroupUserList> {
    @Override
    public GroupUserList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        List<GetUser> usersList = new ArrayList<>();

        JsonArray getUserGroupsJsonArr = json.getAsJsonArray();
        for ( JsonElement jsonElement : getUserGroupsJsonArr) {

            JsonObject jsonObject = jsonElement.getAsJsonObject();

            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            GetUser user = gson.fromJson(jsonObject, GetUser.class);
            usersList.add(user);

        }
        return new GroupUserList(usersList);
    }
}
