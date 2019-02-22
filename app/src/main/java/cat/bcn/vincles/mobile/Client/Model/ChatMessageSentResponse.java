package cat.bcn.vincles.mobile.Client.Model;


import com.google.gson.annotations.JsonAdapter;

import cat.bcn.vincles.mobile.Client.Model.Serializers.ChatMessageSentResponseDeserializer;
import cat.bcn.vincles.mobile.Client.Model.Serializers.ChatMessagesSentResponseDeserializer;

@JsonAdapter(ChatMessageSentResponseDeserializer.class)
public class ChatMessageSentResponse {

    int id;

    public ChatMessageSentResponse(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
