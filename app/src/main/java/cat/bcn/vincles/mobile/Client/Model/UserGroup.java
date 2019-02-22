package cat.bcn.vincles.mobile.Client.Model;

import com.google.gson.annotations.JsonAdapter;

import cat.bcn.vincles.mobile.Client.Model.Serializers.UserCircleDeserializer;
import cat.bcn.vincles.mobile.Client.Model.Serializers.UserGroupDeserializer;

@JsonAdapter(UserGroupDeserializer.class)
public class UserGroup {

    private int idDynamizerChat;
    private Group group;

    public UserGroup() {

    }

    public UserGroup(int idDynamizerChat, Group group) {
        this.idDynamizerChat = idDynamizerChat;
        this.group = group;
    }

    public int getIdDynamizerChat() {
        return idDynamizerChat;
    }

    public Group getGroup() {
        return group;
    }


    public void setIdDynamizerChat(int idDynamizerChat) {
        this.idDynamizerChat = idDynamizerChat;
    }

    public void setGroup(Group group) {
        this.group = group;
    }


    @Override
    public String toString() {
        return "CircleUser{" +
                "idDynamizerChat=" + idDynamizerChat+
                ", group='" + group  +
                '}';
    }

}
