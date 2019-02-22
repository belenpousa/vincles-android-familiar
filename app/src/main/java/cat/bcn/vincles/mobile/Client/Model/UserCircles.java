package cat.bcn.vincles.mobile.Client.Model;

import com.google.gson.annotations.JsonAdapter;

import java.util.List;

import cat.bcn.vincles.mobile.Client.Model.Serializers.CircleUsersDeserializer;
import cat.bcn.vincles.mobile.Client.Model.Serializers.UserCirclesDeserializer;

@JsonAdapter(UserCirclesDeserializer.class)
public class UserCircles {
    private List<UserCircle> userCircles;

    public UserCircles(List<UserCircle> userCircles) {
        this.userCircles = userCircles;
    }

    public List<UserCircle> getUserCircles() {
        return userCircles;
    }

    public void setUserCircles(List<UserCircle> userCircles) {
        this.userCircles = userCircles;
    }
}
