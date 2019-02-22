package cat.bcn.vincles.mobile.Client.Db;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Business.CalendarSyncManager;
import cat.bcn.vincles.mobile.Client.Model.GetUser;
import cat.bcn.vincles.mobile.Client.Model.MeetingRealm;
import cat.bcn.vincles.mobile.Client.Model.MeetingRest;
import cat.bcn.vincles.mobile.Client.Model.MeetingUserInfoRest;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Utils.MyApplication;
import cat.bcn.vincles.mobile.Utils.OtherUtils;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;

public class MeetingsDb extends BaseDb {

    public static final String USER_DELETED = "USER_DELETED";
    public static final String USER_ACCEPTED = "ACCEPTED";
    public static final String USER_REJECTED = "REJECTED";

    public MeetingsDb(Context context) {
        super(context);
    }

    @Override
    public void dropTable() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.where(MeetingRealm.class).findAll().deleteAllFromRealm();
        realm.where(MeetingUserInfoRest.class).findAll().deleteAllFromRealm();
        realm.commitTransaction();
    }


    public void updateMeeting(int meetingId, String description, long date, int duration,
                              int[] guests, long androidCalendarId) {
        MeetingRealm meetingRealm = findMeeting(meetingId);
        RealmList<Integer> ids = meetingRealm.getGuestIDs();
        ArrayList<String> states = new ArrayList<>(meetingRealm.getGuestStates());
        for (int i = 0; i < guests.length; i++) {
            if (!ids.contains(guests[i])) {
                states.add(i, "PENDING");
            }
        }

        Realm realm = Realm.getDefaultInstance();

        realm.beginTransaction();
        meetingRealm.setDescription(description);
        meetingRealm.setDate(date);
        meetingRealm.setDuration(duration);
        meetingRealm.setGuestIDs(OtherUtils.convertIntegersToRealmList(guests));
        meetingRealm.setGuestStates(OtherUtils.convertStringsToRealmList(states));
        realm.commitTransaction();

        if (androidCalendarId != -1) {
            new CalendarSyncManager().updateEvent(androidCalendarId,
                    meetingRealm.getAndroidCalendarEventId(), meetingRealm);
        }
    }

    public void setMeetingAndroidCalendarId(int meetingId, long androidCalendarId) {
        MeetingRealm meetingRealm = findMeeting(meetingId);

        Realm realm = Realm.getDefaultInstance();

        realm.beginTransaction();
        meetingRealm.setAndroidCalendarEventId(androidCalendarId);
        realm.commitTransaction();
    }

    public void setShouldShowMeeting(int meetingId, boolean shouldShow, long androidCalendarId) {
        Realm realm = Realm.getDefaultInstance();
        MeetingRealm meetingRealm = findMeeting(meetingId);
        if (androidCalendarId != -1) {
            new CalendarSyncManager().deleteEvent(androidCalendarId,
                    meetingRealm.getAndroidCalendarEventId());
        }

        realm.beginTransaction();
        meetingRealm.setShouldShow(shouldShow);
        realm.commitTransaction();
    }

    public void deleteMeeting(int meetingId, long androidCalendarId) {
        setShouldShowMeeting(meetingId, false, androidCalendarId);
        /*MeetingRealm meetingRealm = findMeeting(meetingId);
        if (androidCalendarId != -1) {
            new CalendarSyncManager().deleteEvent(androidCalendarId,
                    meetingRealm.getAndroidCalendarEventId());
        }

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        meetingRealm.deleteFromRealm();
        realm.commitTransaction();*/
    }

    public void setMeetingToAccepted(int meetingId, int guestId) {
        Realm realm = Realm.getDefaultInstance();
        MeetingRealm meetingRealm = findMeeting(meetingId);

        int position;
        for (position = 0; position < meetingRealm.getGuestIDs().size(); position++) {
            if (meetingRealm.getGuestIDs().get(position) == guestId) break;
        }
        realm.beginTransaction();
        meetingRealm.getGuestStates().set(position, MeetingUserInfoRest.ACCEPTED);
        realm.commitTransaction();
    }

    public void saveMeeting(int meetingId, int hostId, String description, long date, int duration,
                            int[] guests, long androidCalendarId) {
        RealmList<String> guestStates = new RealmList<>();
        for (int i = 0; i < guests.length; i++) {
            guestStates.add("PENDING");
        }

        MeetingRealm meetingRealm = new MeetingRealm(meetingId, date, duration, description, hostId,
                OtherUtils.convertIntegersToRealmList(guests), guestStates, true);
        if (androidCalendarId != -1) {
            meetingRealm.setAndroidCalendarEventId(new CalendarSyncManager()
                    .addEvent(androidCalendarId, meetingRealm));
        }

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(meetingRealm);
        realm.commitTransaction();
    }

    public void saveMeetingRest(MeetingRest meetingRest, long androidCalendarId) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        MeetingRealm meetingRealm = new MeetingRealm(meetingRest);
        if (androidCalendarId != -1) {
            MeetingRealm oldMeeting = findMeeting(meetingRest.getId());
            if (oldMeeting != null) {
                new CalendarSyncManager().updateEvent(androidCalendarId,
                        oldMeeting.getAndroidCalendarEventId(), meetingRealm);
                meetingRealm.setAndroidCalendarEventId(oldMeeting.getAndroidCalendarEventId());
            } else {
                meetingRealm.setAndroidCalendarEventId(new CalendarSyncManager()
                        .addEvent(androidCalendarId, meetingRealm));
            }
        }

        realm.copyToRealmOrUpdate(meetingRealm);
        realm.commitTransaction();
    }

    public void saveMeetingsRestList(List<MeetingRest> meetings, long androidCalendarId) {
        Realm realm = Realm.getDefaultInstance();

        for (MeetingRest messageRest : meetings) {
            MeetingRealm meetingRealm = new MeetingRealm(messageRest);
            if (androidCalendarId != -1) {
                meetingRealm.setAndroidCalendarEventId(new CalendarSyncManager()
                        .addEvent(androidCalendarId, meetingRealm));
            }
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(meetingRealm);
            realm.commitTransaction();
        }
    }

    public RealmResults<MeetingRealm> findAllShownMeetings() {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(MeetingRealm.class)
                .sort("date", Sort.ASCENDING)
                .equalTo("shouldShow", true)
                .findAll();
    }

    public RealmResults<MeetingRealm> findAllShownMeetingsAsync() {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(MeetingRealm.class)
                .sort("date", Sort.ASCENDING)
                .equalTo("shouldShow", true)
                .findAllAsync();
    }

    public MeetingRealm findMeeting(int id) {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(MeetingRealm.class)
                .equalTo("id", id)
                .findFirst();
    }

    public boolean setMeetingUserState(int meetingId, int userId, String state) {
        Realm realm = Realm.getDefaultInstance();
        MeetingRealm meetingRealm = findMeeting(meetingId);
        if (meetingRealm == null) return false;
        RealmList<Integer> ids = meetingRealm.getGuestIDs();
        for (int i = 0; i < ids.size(); i++) {
            if (ids.get(i) == userId) {
                realm.beginTransaction();
                if (state.equals(USER_DELETED)) {
                    meetingRealm.getGuestIDs().remove(i);
                    meetingRealm.getGuestStates().remove(i);
                } else {
                    meetingRealm.getGuestStates().set(i, state);
                }

                realm.commitTransaction();
                return true;
            }
        }
        return false;
    }

    public int getNumberOfMeetingsPending() {
        int id = new UserPreferences(MyApplication.getAppContext()).getUserID();
        int res = 0;

        RealmResults<MeetingRealm> results = findAllShownMeetings();
        for (MeetingRealm meetingRealm : results) {
            RealmList<Integer> guestIds = meetingRealm.getGuestIDs();
            RealmList<String> guestStates = meetingRealm.getGuestStates();
            for (int i = 0; i < guestIds.size(); i++) {
                if (guestIds.get(i) == id) {
                    if (guestStates.get(i).equalsIgnoreCase(MeetingUserInfoRest.PENDING)) {
                        res++;
                    }
                    break;

                }
            }
        }
        return res;
    }

    public MeetingRealm findFirstMeetingAfterTime(long time) {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<MeetingRealm> meetingRealms = realm.where(MeetingRealm.class)
                .sort("date", Sort.ASCENDING)
                .equalTo("shouldShow", true)
                .greaterThan("date", time)
                .findAll();
        return meetingRealms.size() > 0 ? meetingRealms.first() : null;
    }
}