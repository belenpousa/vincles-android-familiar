package cat.bcn.vincles.mobile.Client.Model;

import com.google.gson.annotations.JsonAdapter;

import java.util.List;

import cat.bcn.vincles.mobile.Client.Model.Serializers.ChatMessageRestListDeserializer;

@JsonAdapter(ChatMessageRestListDeserializer.class)
public class ChatMessageRestList {

    List<ChatMessageRest> chatMessageRestList;

    public ChatMessageRestList(List<ChatMessageRest> chatMessageRestList) {
        this.chatMessageRestList = chatMessageRestList;
    }

    public List<ChatMessageRest> getChatMessageRestList() {
        return chatMessageRestList;
    }

    public void setChatMessageRestList(List<ChatMessageRest> chatMessageRestList) {
        this.chatMessageRestList = chatMessageRestList;
    }
}
