package cat.bcn.vincles.mobile.Client.Model;

import com.google.gson.annotations.JsonAdapter;

import java.util.List;

import cat.bcn.vincles.mobile.Client.Model.Serializers.ChatMessageRestListDeserializer;
import cat.bcn.vincles.mobile.Client.Model.Serializers.MeetingRestListDeserializer;

@JsonAdapter(MeetingRestListDeserializer.class)
public class MeetingRestList {

    List<MeetingRest> meetingRestList;

    public MeetingRestList(List<MeetingRest> meetingRestList) {
        this.meetingRestList = meetingRestList;
    }

    public List<MeetingRest> getChatMessageRestList() {
        return meetingRestList;
    }

    public void setChatMessageRestList(List<MeetingRest> meetingRestList) {
        this.meetingRestList = meetingRestList;
    }
}
