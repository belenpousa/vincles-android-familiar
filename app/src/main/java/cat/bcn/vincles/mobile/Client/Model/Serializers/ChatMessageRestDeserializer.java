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

public class ChatMessageRestDeserializer implements JsonDeserializer<ChatMessageRest> {

    @Override
    public ChatMessageRest deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        JsonObject jsonObject = json.getAsJsonObject();

        long id = jsonObject.get("id").getAsLong();
        int idUserFrom = jsonObject.get("idUserFrom").getAsInt();
        int idUserTo = jsonObject.get("idUserTo").getAsInt();
        long sendTime = jsonObject.get("sendTime").getAsLong();
        boolean watched = jsonObject.get("watched").getAsBoolean();
        String text = !jsonObject.get("text").equals(JsonNull.INSTANCE) ? jsonObject.get("text").getAsString() : "";
        String metadataTipus = !jsonObject.get("metadataTipus").equals(JsonNull.INSTANCE) ? jsonObject.get("metadataTipus").getAsString() : "";
        JsonArray idAdjuntContentsArray = !jsonObject.get("idAdjuntContents").equals(JsonNull.INSTANCE) ? jsonObject.get("idAdjuntContents").getAsJsonArray() : new JsonArray();
        int[] idAdjuntContents = new int[idAdjuntContentsArray.size()];
        for (int i = 0; i < idAdjuntContentsArray.size(); i++) {
            idAdjuntContents[i] = idAdjuntContentsArray.get(i).getAsInt();
        }

        return new ChatMessageRest(id, idUserFrom, idUserTo, sendTime, watched, text, idAdjuntContents, metadataTipus);
    }

}
