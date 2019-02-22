package cat.bcn.vincles.mobile.Client.Model;

import com.google.gson.JsonObject;
import com.google.gson.annotations.JsonAdapter;

import cat.bcn.vincles.mobile.Client.Model.Serializers.GetUserDeserializer;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

@JsonAdapter(GetUserDeserializer.class)
public class GetUser extends RealmObject {

    @PrimaryKey
    int id;
    String name;
    String lastname;
    String alias;
    String gender;
    Integer idContentPhoto;
    Integer idContent;
    String photo;
    String photoMimeType;
    Boolean active;
    Integer idInstallation;
    Integer idCircle;
    Integer idLibrary;
    Integer idCalendar;
    String username;
    long birthdate;
    String email;
    String phone;
    Boolean liveInBarcelona;

    private int numberUnreadMessages;
    private long lastInteraction;

    public  GetUser () {

    }

    public GetUser(Integer id, String name, String lastname, String alias, String gender,
                   Integer idContentPhoto, Integer idContent, String photo, String photoMimeType,
                   Boolean active, Integer idInstallation, Integer idCircle, Integer idLibrary, Integer idCalendar,
                   String username, long birthdate, String email, String phone, Boolean liveInBarcelona) {
        this.id = id;
        this.name = name;
        this.lastname = lastname;
        this.alias = alias;
        this.gender = gender;
        this.idContentPhoto = idContentPhoto;
        this.idContent = idContent;
        this.photo = photo;
        this.photoMimeType = photoMimeType;
        this.active = active;
        this.idInstallation = idInstallation;
        this.idCircle = idCircle;
        this.idLibrary = idLibrary;
        this.idCalendar = idCalendar;
        this.username = username;
        this.birthdate = birthdate;
        this.email = email;
        this.phone = phone;
        this.liveInBarcelona = liveInBarcelona;
    }

    public GetUser(Integer id, String name, String lastname, String alias, String gender, Integer idContentPhoto, Integer idContent, String photo, String photoMimeType, Boolean active) {
        this.id = id;
        this.name = name;
        this.lastname = lastname;
        this.alias = alias;
        this.gender = gender;
        this.idContentPhoto = idContentPhoto;
        this.idContent = idContent;
        this.photo = photo;
        this.photoMimeType = photoMimeType;
        this.active = active;
    }

    public GetUser(Integer id, String name, String lastname, Integer idContentPhoto) {
        this.id = id;
        this.name = name;
        this.lastname = lastname;
        this.idContentPhoto = idContentPhoto;
    }

    public GetUser(int id, String name, String lastname, String alias, String gender) {
        this.id = id;
        this.name = name;
        this.lastname = lastname;
        this.alias = alias;
        this.gender = gender;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getIdContentPhoto() {
        return idContentPhoto;
    }

    public void setIdContentPhoto(Integer idContentPhoto) {
        this.idContentPhoto = idContentPhoto;
    }

    public Integer getIdContent() {
        return idContent;
    }

    public void setIdContent(Integer idContent) {
        this.idContent = idContent;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getPhotoMimeType() {
        return photoMimeType;
    }

    public void setPhotoMimeType(String photoMimeType) {
        this.photoMimeType = photoMimeType;
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Integer getIdInstallation() {
        return idInstallation;
    }

    public void setIdInstallation(Integer idInstallation) {
        this.idInstallation = idInstallation;
    }

    public Integer getIdCircle() {
        return idCircle;
    }

    public void setIdCircle(Integer idCircle) {
        this.idCircle = idCircle;
    }

    public Integer getIdLibrary() {
        return idLibrary;
    }

    public void setIdLibrary(Integer idLibrary) {
        this.idLibrary = idLibrary;
    }

    public Integer getIdCalendar() {
        return idCalendar;
    }

    public void setIdCalendar(Integer idCalendar) {
        this.idCalendar = idCalendar;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(long birthdate) {
        this.birthdate = birthdate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Boolean getLiveInBarcelona() {
        return liveInBarcelona;
    }

    public void setLiveInBarcelona(Boolean liveInBarcelona) {
        this.liveInBarcelona = liveInBarcelona;
    }

    public int getNumberUnreadMessages() {
        return numberUnreadMessages;
    }

    public void setNumberUnreadMessages(int numberUnreadMessages) {
        this.numberUnreadMessages = numberUnreadMessages;
    }

    public long getLastInteraction() {
        return lastInteraction;
    }

    public void setLastInteraction(long lastInteraction) {
        this.lastInteraction = lastInteraction;
    }

    @Override
    public String toString() {
        return "GetUser{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", lastname='" + lastname + '\'' +
                ", alias='" + alias + '\'' +
                ", gender='" + gender + '\'' +
                ", idContentPhoto=" + idContentPhoto +
                ", idContent=" + idContent +
                ", photo='" + photo + '\'' +
                ", photoMimeType='" + photoMimeType + '\'' +
                ", active=" + active +
                ", idInstallation=" + idInstallation +
                ", idCircle=" + idCircle +
                ", idLibrary=" + idLibrary +
                ", idCalendar=" + idCalendar +
                ", username='" + username + '\'' +
                ", birthdate=" + birthdate +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", liveInBarcelona=" + liveInBarcelona +
                '}';
    }

    public JsonObject toJSON() {
        com.google.gson.JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("name", name);
        json.addProperty("lastname", lastname);
        json.addProperty("alias", alias);
        json.addProperty("gender", gender);
        json.addProperty("idContentPhoto", idContentPhoto);
        json.addProperty("idContent", idContent);

        JsonObject photoJson = new JsonObject();
        photoJson.addProperty("idContent",idContent);
        photoJson.addProperty("photo",photo);
        photoJson.addProperty("photoMimeType",photoMimeType);
        json.add("photo", photoJson);

        json.addProperty("active", active);
        json.addProperty("idInstallation", idInstallation);
        json.addProperty("idCircle", idCircle);
        json.addProperty("idCalendar", idCalendar);
        json.addProperty("username",username);
        json.addProperty("birthdate", birthdate);
        json.addProperty("email", email);
        json.addProperty("phone", phone);
        json.addProperty("liveInBarcelona", liveInBarcelona);

        return json;
    }
}
