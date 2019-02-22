package cat.bcn.vincles.mobile.Client.Model.Serializers;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import cat.bcn.vincles.mobile.Client.Model.Dynamizer;
import cat.bcn.vincles.mobile.Client.Model.Group;
import cat.bcn.vincles.mobile.Client.Model.UserGroup;

public class GroupDeserializer implements JsonDeserializer<Group> {
    @Override
    public Group deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Log.d("contRepo","GroupDeserializer");
        JsonObject jsonObject = json.getAsJsonObject();

        int idGroup = jsonObject.get("id").getAsInt();
        Log.d("contRepo","GroupDeserializer idGroup OK");
        String name= jsonObject.get("name").getAsString();
        String topic= jsonObject.get("topic").toString().equals("null") ? "" :
                jsonObject.get("topic").getAsString();
        String description= jsonObject.get("description").toString().equals("null") ? "" :
                jsonObject.get("description").getAsString();
        int idChat = jsonObject.get("idChat").getAsInt();
        Log.d("contRepo","GroupDeserializer idChat OK");
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        Dynamizer dynamizer = gson.fromJson(jsonObject.get("dynamizer"), Dynamizer.class);

        return new Group(idGroup, name, topic, description, "", dynamizer, idChat);
    }
}
