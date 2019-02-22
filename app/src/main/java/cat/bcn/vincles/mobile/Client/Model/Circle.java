package cat.bcn.vincles.mobile.Client.Model;


import com.google.gson.annotations.JsonAdapter;

import cat.bcn.vincles.mobile.Client.Model.Serializers.CircleDeserializer;


@JsonAdapter(CircleDeserializer.class)
public class Circle {

    private int id;
    private GetUser user;

    public Circle() {

    }

    public Circle(int id, GetUser user) {
        this.id = id;
        this.user = user;
    }

    public int getId() {
        return id;
    }

    public GetUser getUser() {
        return user;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUser(GetUser user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Circle{" +
                "id=" + id+
                ", user='" + user  +
                '}';
    }

}
