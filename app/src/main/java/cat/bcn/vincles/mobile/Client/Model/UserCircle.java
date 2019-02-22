package cat.bcn.vincles.mobile.Client.Model;

import com.google.gson.annotations.JsonAdapter;

import cat.bcn.vincles.mobile.Client.Model.Serializers.UserCircleDeserializer;

@JsonAdapter(UserCircleDeserializer.class)
public class UserCircle {

    private String relationship;
    private Circle circle;

    public UserCircle() {

    }

    public UserCircle(String relationship, Circle circle) {
        this.relationship = relationship;
        this.circle = circle;
    }

    public String getRelationship() {
        return relationship;
    }

    public Circle getCircle() {
        return circle;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public void setCircle(Circle circle) {
        this.circle = circle;
    }

    @Override
    public String toString() {
        return "UserCircle{" +
                "circle=" + circle+
                ", relationship='" + relationship  +
                '}';
    }

}
