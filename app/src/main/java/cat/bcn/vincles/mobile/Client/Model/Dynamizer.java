package cat.bcn.vincles.mobile.Client.Model;

import com.google.gson.annotations.JsonAdapter;

import cat.bcn.vincles.mobile.Client.Model.Serializers.DynamizerDeserializer;
import cat.bcn.vincles.mobile.Client.Model.Serializers.GroupDeserializer;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

@JsonAdapter(DynamizerDeserializer.class)
public class Dynamizer extends RealmObject {

    @PrimaryKey
    private int id;
    private String name;
    private String lastname;
    private String alias;
    private String gender;
    private int idContentPhoto;
    private String photo;
    private int numberUnreadMessages;
    private long numberInteractions;
    private int idChat;
    private long lastAccess;
    private boolean shouldShow = true;

    public Dynamizer() {

    }

    public Dynamizer(int id, String name, String lastname, String alias, String gender,
                     int idContentPhoto, String photo) {
        this.id = id;
        this.name = name;
        this.lastname = lastname;
        this.alias = alias;
        this.gender = gender;
        this.idContentPhoto = idContentPhoto;
        this.photo = photo;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLastname() {
        return lastname;
    }

    public String getAlias() {
        return alias;
    }

    public String getGender() {
        return gender;
    }

    public int getIdContentPhoto() {
        return idContentPhoto;
    }

    public String getPhoto() {
        return photo;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setIdContentPhoto(int idContentPhoto) {
        this.idContentPhoto = idContentPhoto;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public int getNumberUnreadMessages() {
        return numberUnreadMessages;
    }

    public void setNumberUnreadMessages(int numberUnreadMessages) {
        this.numberUnreadMessages = numberUnreadMessages;
    }

    public long getNumberInteractions() {
        return numberInteractions;
    }

    public void setNumberInteractions(long numberInteractions) {
        this.numberInteractions = numberInteractions;
    }

    public int getIdChat() {
        return idChat;
    }

    public void setIdChat(int idChat) {
        this.idChat = idChat;
    }

    public long getLastAccess() {
        return lastAccess;
    }

    public void setLastAccess(long lastAccess) {
        this.lastAccess = lastAccess;
    }

    public boolean isShouldShow() {
        return shouldShow;
    }

    public void setShouldShow(boolean shouldShow) {
        this.shouldShow = shouldShow;
    }

    @Override
    public String toString() {
        return "Dynamizer{" +
                "id=" + id+
                ", name='" + name  +
                ", lastname='" + lastname  +
                ", alias='" + alias  +
                ", gender='" + gender  +
                ", idContentPhoto='" + idContentPhoto  +
                '}';
    }
}
