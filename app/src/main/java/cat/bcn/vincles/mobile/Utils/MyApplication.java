package cat.bcn.vincles.mobile.Utils;

import android.app.Application;
import android.content.Context;


public class MyApplication extends Application {
    private static Context context;

    /**
     *
     * When a notification requires the update of the UI, a broadcast is used.
     *
     * When there is a missed call due to timeout, sending a broadcast to the MainActivity does
     * not work because it is not running (the current activity is CallsActivity.
     *
     * This boolean is used to let the activity know that there is a missed call that requires
     * updating the UI
     *
     */
    private static boolean pendingMissedCallBroadcast = false;

    public void onCreate() {
        super.onCreate();
        MyApplication.context = getApplicationContext();



    }

    public static Context getAppContext() {
        return MyApplication.context;
    }

    public static boolean isPendingMissedCallBroadcast() {
        return pendingMissedCallBroadcast;
    }

    public static void setPendingMissedCallBroadcast(boolean pendingMissedCallBroadcast) {
        MyApplication.pendingMissedCallBroadcast = pendingMissedCallBroadcast;
    }
}
