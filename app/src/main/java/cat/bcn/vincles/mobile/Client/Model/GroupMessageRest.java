package cat.bcn.vincles.mobile.Client.Model;

import com.google.gson.annotations.JsonAdapter;

import java.util.ArrayList;

import cat.bcn.vincles.mobile.Client.Model.Serializers.ChatMessageRestDeserializer;
import cat.bcn.vincles.mobile.Client.Model.Serializers.GroupMessageRestDeserializer;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

@JsonAdapter(GroupMessageRestDeserializer.class)
public class GroupMessageRest extends RealmObject {

    @PrimaryKey
    private int id;

    private String text;

    private long sendTime;

    private String metadataTipus;

    private int idUserSender;

    private String fullNameUserSender;

    private Integer idContent;

    private int idChat;

    String pathContent;

    String metadataContent;

    private boolean watched;

    public GroupMessageRest() {
    }

    public GroupMessageRest(int id, String text, long sendTime, String metadataTipus,
                            int idUserSender, String fullNameUserSender, Integer idContent, int idChat) {
        this.id = id;
        this.text = text;
        this.sendTime = sendTime;
        this.metadataTipus = metadataTipus;
        this.idUserSender = idUserSender;
        this.fullNameUserSender = fullNameUserSender;
        this.idContent = idContent;
        this.idChat = idChat;
        pathContent = "";
        metadataContent = "";

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getSendTime() {
        return sendTime;
    }

    public void setSendTime(long sendTime) {
        this.sendTime = sendTime;
    }

    public String getMetadataTipus() {
        return metadataTipus;
    }

    public void setMetadataTipus(String metadataTipus) {
        this.metadataTipus = metadataTipus;
    }

    public int getIdUserSender() {
        return idUserSender;
    }

    public void setIdUserSender(int idUserSender) {
        this.idUserSender = idUserSender;
    }

    public Integer getIdContent() {
        return idContent;
    }

    public void setIdContent(Integer idContent) {
        this.idContent = idContent;
    }

    public int getIdChat() {
        return idChat;
    }

    public void setIdChat(int idChat) {
        this.idChat = idChat;
    }

    public String getPathContent() {
        return pathContent;
    }

    public void setPathContent(String pathContent) {
        this.pathContent = pathContent;
    }

    public String getMetadataContent() {
        return metadataContent;
    }

    public void setMetadataContent(String metadataContent) {
        this.metadataContent = metadataContent;
    }

    public String getFullNameUserSender() {
        return fullNameUserSender;
    }

    public void setFullNameUserSender(String fullNameUserSender) {
        this.fullNameUserSender = fullNameUserSender;
    }

    public boolean isWatched() {
        return watched;
    }

    public void setWatched(boolean watched) {
        this.watched = watched;
    }
}
