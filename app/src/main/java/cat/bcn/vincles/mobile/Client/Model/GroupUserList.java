package cat.bcn.vincles.mobile.Client.Model;

import com.google.gson.annotations.JsonAdapter;

import java.util.List;

import cat.bcn.vincles.mobile.Client.Model.Serializers.GroupUserListDeserializer;
import cat.bcn.vincles.mobile.Client.Model.Serializers.UserGroupsDeserializer;

@JsonAdapter(GroupUserListDeserializer.class)
public class GroupUserList {
    private List<GetUser> users;

    public GroupUserList(List<GetUser> users) {
        this.users = users;
    }

    public List<GetUser> getGroupUserList() {
        return users;
    }

    public void setUserGroups(List<GetUser> users) {
        this.users = users;
    }
}
