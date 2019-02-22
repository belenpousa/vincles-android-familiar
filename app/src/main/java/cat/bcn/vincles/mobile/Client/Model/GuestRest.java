package cat.bcn.vincles.mobile.Client.Model;

import com.google.gson.annotations.JsonAdapter;

import cat.bcn.vincles.mobile.Client.Model.Serializers.GuestRestDeserializer;
import cat.bcn.vincles.mobile.Client.Model.Serializers.MeetingUserInfoDeserializer;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

@JsonAdapter(GuestRestDeserializer.class)
public class GuestRest {

    private GetUser user;
    private String state;

    public GuestRest() {
    }

    public GuestRest(GetUser user, String state) {
        this.user = user;
        this.state = state;
    }

    public GetUser getUser() {
        return user;
    }

    public void setUser(GetUser user) {
        this.user = user;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
