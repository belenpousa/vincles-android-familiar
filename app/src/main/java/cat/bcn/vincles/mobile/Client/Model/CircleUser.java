package cat.bcn.vincles.mobile.Client.Model;

import com.google.gson.annotations.JsonAdapter;

import cat.bcn.vincles.mobile.Client.Model.Serializers.CircleUserDeserializer;


@JsonAdapter(CircleUserDeserializer.class)
public class CircleUser {

    private String relationship;
    private GetUser user;

    public CircleUser() {

    }

    public CircleUser(String relationship, GetUser user) {
        this.relationship = relationship;
        this.user = user;
    }

    public String getRelationship() {
        return relationship;
    }

    public GetUser getUser() {
        return user;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public void setUser(GetUser user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "CircleUser{" +
                "relationship=" + relationship +
                ", user='" + user +
                '}';
    }

}
