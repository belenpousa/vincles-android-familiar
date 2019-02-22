package cat.bcn.vincles.mobile.Client.Model;

import com.google.gson.annotations.JsonAdapter;

import cat.bcn.vincles.mobile.Client.Model.Serializers.MeetingUserInfoDeserializer;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

@JsonAdapter(MeetingUserInfoDeserializer.class)
public class MeetingUserInfoRest extends RealmObject {

    public static final String PENDING = "PENDING";
    public static final String ACCEPTED = "ACCEPTED";
    public static final String REJECTED = "REJECTED";

    @PrimaryKey
    private int id;
    private String name;
    private String lastName;
    private int idContentPhoto;
    private String state;


    public MeetingUserInfoRest() {
    }

    public MeetingUserInfoRest(int id, String name, String lastName, int idContentPhoto, String state) {
        this.id = id;
        this.name = name;
        this.lastName = lastName;
        this.idContentPhoto = idContentPhoto;
        this.state = state;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getIdContentPhoto() {
        return idContentPhoto;
    }

    public void setIdContentPhoto(int idContentPhoto) {
        this.idContentPhoto = idContentPhoto;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
