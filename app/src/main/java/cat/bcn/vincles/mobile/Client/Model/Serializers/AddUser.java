package cat.bcn.vincles.mobile.Client.Model.Serializers;


import com.google.gson.annotations.JsonAdapter;

import cat.bcn.vincles.mobile.Client.Model.GetUser;

@JsonAdapter(AddUserDeserializer.class)
public class AddUser {

    String relationship;

    GetUser userVincles;

    public AddUser(String relationship, GetUser getUser) {
        this.relationship = relationship;
        this.userVincles = getUser;
    }

    public String getRelationship() {
        return relationship;
    }

    public GetUser getUserVincles() {
        return userVincles;
    }
}
