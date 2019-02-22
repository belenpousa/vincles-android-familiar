package cat.bcn.vincles.mobile.Client.Business.Firebase;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.List;

import cat.bcn.vincles.mobile.Client.Business.NotificationsManager;

public class FirebaseListenerService extends FirebaseMessagingService {

    public final static String FIREBASE_PUSH_INTENT = "firebase_push_intent";

    @Override
    public void onMessageReceived(final RemoteMessage message){
        Log.d("firebase","onMessageReceived, isBackgroundRunning:"+ isBackgroundRunning(this));

        Intent serviceIntent = new Intent(this, NotificationsManager.class);
        ContextCompat.startForegroundService(this, serviceIntent);
    }


    public static boolean isBackgroundRunning(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
        if (runningProcesses == null) return true;
        for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                for (String activeProcess : processInfo.pkgList) {
                    if (activeProcess.equals(context.getPackageName())) {
                        //If your app is the process in foreground, then it's not in running in background
                        return false;
                    }
                }
            }
        }


        return true;
    }
}
