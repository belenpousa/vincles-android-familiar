package cat.bcn.vincles.mobile.Client.Model.Serializers;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Model.ChatMessageSentResponse;
import cat.bcn.vincles.mobile.Client.Model.ChatMessagesSentResponse;
import cat.bcn.vincles.mobile.Utils.OtherUtils;

public class ChatMessageSentResponseDeserializer implements JsonDeserializer<ChatMessageSentResponse> {

    @Override
    public ChatMessageSentResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        JsonObject jsonObject = json.getAsJsonObject();
        return new ChatMessageSentResponse(jsonObject.get("id").getAsInt());

    }
}
