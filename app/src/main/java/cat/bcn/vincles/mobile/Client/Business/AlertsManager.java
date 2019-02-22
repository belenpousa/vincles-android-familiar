package cat.bcn.vincles.mobile.Client.Business;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;

import java.util.List;
import java.util.Locale;

import cat.bcn.vincles.mobile.Client.Db.MeetingsDb;
import cat.bcn.vincles.mobile.Client.Db.NotificationsDb;
import cat.bcn.vincles.mobile.Client.Db.UsersDb;
import cat.bcn.vincles.mobile.Client.Model.GetUser;
import cat.bcn.vincles.mobile.Client.Model.MeetingRealm;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.UI.Alert.AlertMessage;
import cat.bcn.vincles.mobile.Utils.DateUtils;
import cat.bcn.vincles.mobile.Utils.MyApplication;
import cat.bcn.vincles.mobile.Utils.OtherUtils;
import io.realm.RealmList;


public class AlertsManager {

    private final static String CONNECTIVITY_CHANGE_INTENT_FILTER = "android.net.conn.CONNECTIVITY_CHANGE";

    private final static int BATTERY_STATE_OK = 0;
    private final static int BATTERY_STATE_20 = 1;
    private final static int BATTERY_STATE_10 = 2;
    private final static int BATTERY_STATE_05 = 3;

    private final static int REPEATED_CHECKS_PERIOD = 30; //seconds
    public static final String SAVE_INSTANCE_LANGUAGE = "save_instance_language";

    private Handler handler;
    private Activity activity;
    private Intent batteryStatus;
    private MeetingsDb meetingsDb;

    private int batteryState;
    private boolean batteryAlertShowing;
    private int meetingShowingId = -1;
    private AlertMessage batteryAlert, networkAlert, meetingAlert;
    private MeetingRealm alertMeetingRealm = null;
    private boolean isNetworkConnected = true;
    private NetworkChangeReceiver networkBroadcastReceiver;


    public AlertsManager(Activity activity, Bundle savedInstanceState) {
        this.activity = activity;
        handler = new Handler();
        meetingsDb = new MeetingsDb(MyApplication.getAppContext());

        /**
         * Sometimes changing language requires recreating activity an extra time. This leads to
         * saveInstanceState being lost.
         * SAVE_INSTANCE_LANGUAGE is used to verify if saveInstanceState has been lost
         */
        if (savedInstanceState != null && savedInstanceState.getInt(SAVE_INSTANCE_LANGUAGE) == -1) {
            batteryAlertShowing = savedInstanceState.getBoolean("batteryAlertShowing");
            meetingShowingId = savedInstanceState.getInt("meetingShowingId");
            batteryState = savedInstanceState.getInt("batteryState");
        }


        batteryAlert = new AlertMessage(new AlertMessage.AlertMessageInterface() {
            @Override
            public void onOkAlertMessage(AlertMessage alertMessage, String type) {
                batteryAlertShowing = false;
                alertMessage.dismissSafely();
            }
        }, AlertMessage.TITTLE_SYSTEM);

        networkAlert = new AlertMessage(new AlertMessage.AlertMessageInterface() {
            @Override
            public void onOkAlertMessage(AlertMessage alertMessage, String type) {
                alertMessage.dismissSafely();
            }
        }, AlertMessage.TITTLE_SYSTEM);

        meetingAlert = new AlertMessage(new AlertMessage.AlertMessageInterface() {
            @Override
            public void onOkAlertMessage(AlertMessage alertMessage, String type) {
                meetingShowingId = -1;
                alertMessage.dismissSafely();
            }
        }, AlertMessage.TITTLE_REMINDER);
        meetingAlert.setCancelMessageInterface(new AlertMessage.CancelMessageInterface() {
            @Override
            public void onCancelAlertMessage() {
                meetingShowingId = -1;
            }
        });
    }

    public void start() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryStatus = activity.registerReceiver(null, ifilter);

        if (meetingShowingId != -1) {
            meetingAlert.showMessage(activity, getMeetingText(meetingsDb
                    .findMeeting(meetingShowingId)), "");
            createMeetingNotification(meetingShowingId);
        } else if (batteryAlertShowing) {
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            float batteryPercentage = level / (float)scale;
            batteryAlert.showMessage(activity, activity.getResources().getString(
                    R.string.battery_alert_text, formatBatteryPercentage(batteryPercentage)), "");
        }

        handler.post(batteryRunnable);
        handler.post(meetingRunnable);

        networkBroadcastReceiver = new NetworkChangeReceiver();
        IntentFilter intentFilter = new IntentFilter(CONNECTIVITY_CHANGE_INTENT_FILTER);
        activity.registerReceiver(networkBroadcastReceiver, intentFilter);
        isNetworkConnected = isNetworkAvailable(activity);
        if (!isNetworkConnected) showNetworkAlert();
    }

    public void stop() {
        handler.removeCallbacks(batteryRunnable);
        handler.removeCallbacks(meetingRunnable);
        activity.unregisterReceiver(networkBroadcastReceiver);
        networkAlert.dismissSafely();
        meetingAlert.dismissSafely();
        batteryAlert.dismissSafely();
        alertMeetingRealm = null;
    }

    public void restartMeetingRunnable() {
        handler.removeCallbacks(meetingRunnable);
        meetingShowingId = -1;
        alertMeetingRealm = null;
        handler.post(meetingRunnable);
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("batteryAlertShowing", batteryAlertShowing);
        outState.putInt("meetingShowingId", meetingShowingId);
        outState.putInt("batteryState", batteryState);
        outState.putInt(SAVE_INSTANCE_LANGUAGE, -1);
    }

    private Runnable batteryRunnable = new Runnable() {
        @Override
        public void run() {
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            float batteryPercentage = level / (float)scale;
            boolean showAlert = false;

            if (batteryState == BATTERY_STATE_OK && batteryPercentage <= 0.2 ||
                    batteryState == BATTERY_STATE_20 && batteryPercentage <= 0.1 ||
                    batteryState == BATTERY_STATE_10 && batteryPercentage <= 0.05) {
                showAlert = true;
            }
            setBatteryState(batteryPercentage);

            if (showAlert) {
                batteryAlertShowing = true;
                batteryAlert.showMessage(activity, activity.getResources().getString(
                        R.string.battery_alert_text, formatBatteryPercentage(batteryPercentage)), "");
            }

            handler.postDelayed(batteryRunnable, REPEATED_CHECKS_PERIOD * 1000);
        }
    };

    private void setBatteryState(float percentage) {
        if (0.2 < percentage) {
            batteryState = BATTERY_STATE_OK;
        } else if (0.1 < percentage && percentage <= 0.2) {
            batteryState = BATTERY_STATE_20;
        } else if (0.05 < percentage && percentage <= 0.1) {
            batteryState = BATTERY_STATE_10;
        } else {
            batteryState = BATTERY_STATE_05;
        }
    }

    private Runnable meetingRunnable = new Runnable() {

        @Override
        public void run() {
            Log.d("alrtmanme","meetingRunnable run");
            if (alertMeetingRealm != null) {
                Log.d("alrtmanme","meetingRunnable show alert");
                meetingShowingId = alertMeetingRealm.getId();
                meetingAlert.showMessage(activity, getMeetingText(alertMeetingRealm), "");
                createMeetingNotification(meetingShowingId);
            }
            alertMeetingRealm = meetingsDb.findFirstMeetingAfterTime(
                    System.currentTimeMillis()+60*60*1000);
            if (alertMeetingRealm != null) {
                Log.d("alrtmanme","NextAlert time:"+(alertMeetingRealm.getDate()));
                Log.d("alrtmanme","NextAlert:"+(alertMeetingRealm.getDate() - 60*60*1000 - System.currentTimeMillis()));
                handler.postDelayed(meetingRunnable, alertMeetingRealm.getDate()
                        - 60*60*1000 - System.currentTimeMillis());
            }
        }
    };

    private void createMeetingNotification(int meetingId) {
        new NotificationsDb(activity.getApplicationContext())
                .saveNotificationCloseMeetingAlert(meetingsDb.findMeeting(meetingId));
    }

    private String getMeetingText(MeetingRealm meetingRealm) {
        Locale locale = activity.getResources().getConfiguration().locale;
        RealmList<Integer> guestIds = meetingRealm.getGuestIDs();
        return meetingRealm.getDescription() + "\n"
                + DateUtils.getAlertFormatedTime(meetingRealm.getDate(), locale) + "\n"
                + activity.getResources().getString(R.string.calendar_date_length_alert) + " "
                + OtherUtils.getDuration(meetingRealm.getDuration(), activity.getResources()) + "\n"
                + getListOfUsers(guestIds, getUserList(guestIds),activity.getResources());
    }

    private String getListOfUsers(List<Integer> userIds, SparseArray<GetUser> usersList, Resources resources) {
        if (userIds.size() == 0) return "";
        int userMe = new UserPreferences(activity).getUserID();
        StringBuilder usersString = new StringBuilder();
        boolean putComma = false;
        for (Integer userId : userIds) {
            if (putComma) {
                usersString.append(", ");
            } else {
                putComma = true;
            }
            if (userMe == userId) {
                usersString.append(resources.getString(R.string.chat_username_you));
            } else {
                usersString.append(usersList.get(userId).getName());
            }
        }

        if (usersString.length() > 0) return resources.getString(R.string.calendar_date_guests,
                (usersString/* + "."*/));
        return "";
    }

    private SparseArray<GetUser> getUserList(List<Integer> userIds) {
        UsersDb usersDb = new UsersDb(activity.getApplicationContext());
        SparseArray<GetUser> list = new SparseArray<>();
        for (int id : userIds) {
            list.put(id, usersDb.findUser(id));
        }
        return list;
    }

    private String formatBatteryPercentage(float decimal) {
        return String.valueOf((int)(decimal*100)) + "%";
    }

    private void onNetworkStateChange(boolean isConnected) {
        if (isConnected != isNetworkConnected) {
            isNetworkConnected = isConnected;
            if (!isConnected) showNetworkAlert();
        }
    }

    private void showNetworkAlert() {
        networkAlert.showMessage(activity, activity.getResources().getString(
                R.string.network_alert_text), "");
    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    public class NetworkChangeReceiver extends BroadcastReceiver {


        @Override
        public void onReceive(final Context context, final Intent intent) {

            if (CONNECTIVITY_CHANGE_INTENT_FILTER.equals(intent.getAction())) {
                onNetworkStateChange(isNetworkAvailable(context));
            }
        }

    }

}
