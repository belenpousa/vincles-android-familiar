package cat.bcn.vincles.mobile.Client.Model;

import com.google.gson.annotations.JsonAdapter;

import java.util.List;

import cat.bcn.vincles.mobile.Client.Model.Serializers.CircleUsersDeserializer;

@JsonAdapter(CircleUsersDeserializer.class)
public class CircleUsers {
    private List<CircleUser> circleUsers;

    public CircleUsers(List<CircleUser> circleUsers) {
        this.circleUsers = circleUsers;
    }

    public List<CircleUser> getCircleUsers() {
        return circleUsers;
    }

    public void setCircleUsers(List<CircleUser> circleUsers) {
        this.circleUsers = circleUsers;
    }
}
