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

import cat.bcn.vincles.mobile.Client.Model.ChatMessageRest;
import cat.bcn.vincles.mobile.Client.Model.ChatMessageRestList;
import cat.bcn.vincles.mobile.Client.Model.GroupMessageRest;
import cat.bcn.vincles.mobile.Client.Model.GroupMessageRestList;

public class GroupMessageRestListDeserializer implements JsonDeserializer<GroupMessageRestList> {

    @Override
    public GroupMessageRestList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        List<GroupMessageRest> groupMessageRests = new ArrayList<>();

        JsonArray getUsersJsonArr = json.getAsJsonArray();
        for ( JsonElement jsonElement : getUsersJsonArr) {

            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            GroupMessageRest groupMessageRest = gson.fromJson(jsonObject, GroupMessageRest.class);
            groupMessageRests.add(groupMessageRest);

        }

        return new GroupMessageRestList(groupMessageRests);
    }
}
