package cat.bcn.vincles.mobile.Client.Model;

import com.google.gson.annotations.JsonAdapter;

import java.util.List;

import cat.bcn.vincles.mobile.Client.Model.Serializers.CircleUsersDeserializer;
import cat.bcn.vincles.mobile.Client.Model.Serializers.UserGroupsDeserializer;

@JsonAdapter(UserGroupsDeserializer.class)
public class UserGroups {
    private List<UserGroup> userGroups;

    public UserGroups(List<UserGroup> userGroups) {
        this.userGroups = userGroups;
    }

    public List<UserGroup> getUserGroups() {
        return userGroups;
    }

    public void setUserGroups(List<UserGroup> userGroups) {
        this.userGroups = userGroups;
    }
}
