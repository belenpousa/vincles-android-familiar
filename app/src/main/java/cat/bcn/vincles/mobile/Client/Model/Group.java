package cat.bcn.vincles.mobile.Client.Model;

import com.google.gson.annotations.JsonAdapter;

import cat.bcn.vincles.mobile.Client.Model.Serializers.GroupDeserializer;
import io.realm.RealmList;

@JsonAdapter(GroupDeserializer.class)
public class Group {

    private int id;
    private String name;
    private String topic;
    private String description;
    private String photo;
    private Dynamizer dynamizer;
    private int idChat;

    public Group() {

    }

    public Group(int id, String name, String topic, String description, String photo, Dynamizer dynamizer, int idChat) {
        this.id = id;
        this.name = name;
        this.topic = topic;
        this.description = description;
        this.photo = photo;
        this.dynamizer = dynamizer;
        this.idChat = idChat;
    }

    public int getIdGroup() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getPhoto() {
        return photo;
    }

    public Dynamizer getDynamizer() {
        return dynamizer;
    }

    public int getIdChat() {
        return idChat;
    }

    public void setIdGroup(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public void setDynamizer(Dynamizer dynamizer) {
        this.dynamizer = dynamizer;
    }

    public void setIdChat(int idChat) {
        this.idChat = idChat;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Group{" +
                "id=" + id+
                ", name='" + name  +
                ", description='" + description  +
                ", dynamizer='" + dynamizer  +
                ", idChat='" + idChat  +
                '}';
    }

}
