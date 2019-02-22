package cat.bcn.vincles.mobile.Client.Model;

import com.google.gson.annotations.JsonAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Model.Serializers.ChatMessageRestDeserializer;
import cat.bcn.vincles.mobile.Utils.OtherUtils;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

@JsonAdapter(ChatMessageRestDeserializer.class)
public class ChatMessageRest extends RealmObject {

    @PrimaryKey
    private long id;

    private int idUserFrom;

    private int idUserTo;

    private long sendTime;

    private boolean watched;

    private String text;

    private RealmList<Integer> idAdjuntContents;

    private RealmList<String> pathsAdjuntContents;

    private RealmList<String> metadataAdjuntContents;

    private String metadataTipus;

    public ChatMessageRest() {
    }

    public ChatMessageRest(long id, int idUserFrom, int idUserTo, long sendTime, boolean watched, String text, int[] idAdjuntContents, String metadataTipus) {
        this.id = id;
        this.idUserFrom = idUserFrom;
        this.idUserTo = idUserTo;
        this.sendTime = sendTime;
        this.watched = watched;
        this.text = text;
        this.idAdjuntContents = OtherUtils.convertIntegersToRealmList(idAdjuntContents);
        this.metadataTipus = metadataTipus;
        pathsAdjuntContents = new RealmList<>();
        metadataAdjuntContents = new RealmList<>();
        for (int contentID : idAdjuntContents) {
            pathsAdjuntContents.add("");
            metadataAdjuntContents.add("");
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getIdUserFrom() {
        return idUserFrom;
    }

    public void setIdUserFrom(int idUserFrom) {
        this.idUserFrom = idUserFrom;
    }

    public int getIdUserTo() {
        return idUserTo;
    }

    public void setIdUserTo(int idUserTo) {
        this.idUserTo = idUserTo;
    }

    public long getSendTime() {
        return sendTime;
    }

    public void setSendTime(long sendTime) {
        this.sendTime = sendTime;
    }

    public boolean isWatched() {
        return watched;
    }

    public void setWatched(boolean watched) {
        this.watched = watched;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public RealmList<Integer> getIdAdjuntContents() {
        return idAdjuntContents;
    }

    public void setIdAdjuntContents(RealmList<Integer> idAdjuntContents) {
        this.idAdjuntContents = idAdjuntContents;
    }

    public String getMetadataTipus() {
        return metadataTipus;
    }

    public void setMetadataTipus(String metadataTipus) {
        this.metadataTipus = metadataTipus;
    }

    public RealmList<String> getPathsAdjuntContents() {
        return pathsAdjuntContents;
    }

    public void setPathsAdjuntContents(ArrayList<String> pathsAdjuntContents) {
        this.pathsAdjuntContents = OtherUtils.convertStringsToRealmList(pathsAdjuntContents);
    }

    public RealmList<String> getMetadataAdjuntContents() {
        return metadataAdjuntContents;
    }

    public void setMetadataAdjuntContents(ArrayList<String> metadataAdjuntContents) {
        this.metadataAdjuntContents = OtherUtils.convertStringsToRealmList(metadataAdjuntContents);
    }
}
