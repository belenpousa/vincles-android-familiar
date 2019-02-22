package cat.bcn.vincles.mobile.Client.Model;

import com.google.gson.annotations.JsonAdapter;

import java.util.ArrayList;

import cat.bcn.vincles.mobile.Client.Model.Serializers.MeetingRestDeserializer;
import io.realm.annotations.PrimaryKey;

@JsonAdapter(MeetingRestDeserializer.class)
public class MeetingRest {

    private int id;

    private long date;
    private int duration;
    private String description;
    private GetUser host;
    private ArrayList<GuestRest> guests;


    public MeetingRest() {
    }

    public MeetingRest(int id, long date, int duration, String description, GetUser host, ArrayList<GuestRest> guests) {
        this.id = id;
        this.date = date;
        this.duration = duration;
        this.description = description;
        this.host = host;
        this.guests = guests;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public GetUser getHost() {
        return host;
    }

    public void setHost(GetUser host) {
        this.host = host;
    }

    public ArrayList<GuestRest> getGuests() {
        return guests;
    }

    public void setGuests(ArrayList<GuestRest> guests) {
        this.guests = guests;
    }
}
