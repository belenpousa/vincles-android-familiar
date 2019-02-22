package cat.bcn.vincles.mobile.Client.Model;

import com.google.gson.annotations.JsonAdapter;

import java.util.List;

import cat.bcn.vincles.mobile.Client.Model.Serializers.ChatMessageRestListDeserializer;
import cat.bcn.vincles.mobile.Client.Model.Serializers.GroupMessageRestListDeserializer;

@JsonAdapter(GroupMessageRestListDeserializer.class)
public class GroupMessageRestList {

    List<GroupMessageRest> groupMessageRestList;

    public GroupMessageRestList(List<GroupMessageRest> groupMessageRestList) {
        this.groupMessageRestList = groupMessageRestList;
    }

    public List<GroupMessageRest> getChatMessageRestList() {
        return groupMessageRestList;
    }

    public void setChatMessageRestList(List<GroupMessageRest> groupMessageRestList) {
        this.groupMessageRestList = groupMessageRestList;
    }
}
