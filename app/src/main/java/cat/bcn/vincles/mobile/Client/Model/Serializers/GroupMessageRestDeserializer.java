package cat.bcn.vincles.mobile.Client.Model.Serializers;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import cat.bcn.vincles.mobile.Client.Model.ChatMessageRest;
import cat.bcn.vincles.mobile.Client.Model.GroupMessageRest;

public class GroupMessageRestDeserializer implements JsonDeserializer<GroupMessageRest> {

    @Override
    public GroupMessageRest deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        JsonObject jsonObject = json.getAsJsonObject();

        int id = jsonObject.get("id").getAsInt();
        String text = !jsonObject.get("text").equals(JsonNull.INSTANCE) ? jsonObject.get("text").getAsString() : "";
        long sendTime = jsonObject.get("sendTime").getAsLong();
        String metadataTipus = !jsonObject.get("metadataTipus").equals(JsonNull.INSTANCE) ? jsonObject.get("metadataTipus").getAsString() : "";
        int idUserSender = jsonObject.get("idUserSender").getAsInt();
        String  fullNameUserSender = jsonObject.get("fullNameUserSender").equals(JsonNull.INSTANCE)
                ? "" : jsonObject.get("fullNameUserSender").getAsString();
        Integer idContent = jsonObject.get("idContent").equals(JsonNull.INSTANCE) ? null : jsonObject.get("idContent").getAsInt();
        int idChat = jsonObject.get("idChat").getAsInt();

        return new GroupMessageRest(id, text, sendTime, metadataTipus, idUserSender,
                fullNameUserSender, idContent, idChat);
    }

}
