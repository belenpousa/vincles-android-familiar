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

import cat.bcn.vincles.mobile.Client.Model.GetUser;
import cat.bcn.vincles.mobile.Client.Model.Group;
import cat.bcn.vincles.mobile.Client.Model.UserCircle;
import cat.bcn.vincles.mobile.Client.Model.UserGroup;

public class UserGroupDeserializer implements JsonDeserializer<UserGroup> {
    @Override
    public UserGroup deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Log.d("contRepo","UserGroupDeserializer");
        JsonObject jsonObject = json.getAsJsonObject();

        int idDynamizerChat = jsonObject.get("idDynamizerSharedChat").getAsInt();
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        Log.d("contRepo","UserGroupDeserializer is group null? "+(jsonObject.get("group")==null));
        Group group = gson.fromJson(jsonObject.get("group"), Group.class);

        Log.d("dynfa","deserialize groupId:"+group.getId()+"dynChat:"+idDynamizerChat);
        group.getDynamizer().setIdChat(idDynamizerChat);
        return new UserGroup(idDynamizerChat, group);
    }
}
