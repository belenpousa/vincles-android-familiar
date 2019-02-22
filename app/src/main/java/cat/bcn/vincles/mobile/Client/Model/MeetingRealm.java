package cat.bcn.vincles.mobile.Client.Model;

import android.util.Log;

import com.google.gson.annotations.JsonAdapter;

import cat.bcn.vincles.mobile.Client.Db.MeetingsDb;
import cat.bcn.vincles.mobile.Client.Model.Serializers.MeetingRestDeserializer;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Utils.MyApplication;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

@JsonAdapter(MeetingRestDeserializer.class)
public class MeetingRealm extends RealmObject {

    @PrimaryKey
    private int id;

    private long date;
    private int duration;
    private String description;
    private int hostId;
    private RealmList<Integer> guestIDs;
    private RealmList<String> guestStates;
    private boolean shouldShow;
    private long androidCalendarEventId;


    public MeetingRealm() {
    }

    public MeetingRealm(int id, long date, int duration, String description, int hostId,
                        RealmList<Integer> guestIDs, RealmList<String> guestStates, boolean shouldShow) {
        this.id = id;
        this.date = date;
        this.duration = duration;
        this.description = description;
        this.hostId = hostId;
        this.guestIDs = guestIDs;
        this.shouldShow = shouldShow;
        this.guestStates = guestStates;
    }

    public MeetingRealm(MeetingRest meetingRest) {
        UserPreferences userPreferences = new UserPreferences(MyApplication.getAppContext());

        this.id = meetingRest.getId();
        date = meetingRest.getDate();
        duration = meetingRest.getDuration();
        description = meetingRest.getDescription();
        hostId = meetingRest.getHost().getId();
        guestIDs = new RealmList<>();
        guestStates = new RealmList<>();
        shouldShow = true;
        for (GuestRest guestRest : meetingRest.getGuests()) {
            guestIDs.add(guestRest.getUser().getId());
            guestStates.add(guestRest.getState());
            if (guestRest.getUser().getId() == userPreferences.getUserID()
                    && guestRest.getState().equals(MeetingsDb.USER_REJECTED)) {
                shouldShow = false;
            }
        }
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

    public int getHostId() {
        return hostId;
    }

    public void setHostId(int hostId) {
        this.hostId = hostId;
    }

    public RealmList<Integer> getGuestIDs() {
        return guestIDs;
    }

    public void setGuestIDs(RealmList<Integer> guestIDs) {
        this.guestIDs = guestIDs;
    }

    public RealmList<String> getGuestStates() {
        return guestStates;
    }

    public void setGuestStates(RealmList<String> guestStates) {
        this.guestStates = guestStates;
    }

    public boolean isShouldShow() {
        return shouldShow;
    }

    public void setShouldShow(boolean shouldShow) {
        this.shouldShow = shouldShow;
    }

    public long getAndroidCalendarEventId() {
        return androidCalendarEventId;
    }

    public void setAndroidCalendarEventId(long androidCalendarEventId) {
        this.androidCalendarEventId = androidCalendarEventId;
    }
}
