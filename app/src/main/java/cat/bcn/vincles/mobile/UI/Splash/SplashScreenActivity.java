package cat.bcn.vincles.mobile.UI.Splash;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.ndk.CrashlyticsNdk;
import com.tempos21.versioncontrol.service.AlertMessageService;

import cat.bcn.vincles.mobile.Client.Enviroment.Environment;
import cat.bcn.vincles.mobile.Client.Migration.Fase1SQLiteHelper;
import cat.bcn.vincles.mobile.Client.Model.UserRegister;
import cat.bcn.vincles.mobile.UI.Alert.AlertMessage;
import cat.bcn.vincles.mobile.UI.Login.LoginActivity;
import cat.bcn.vincles.mobile.Utils.MyApplication;
import cat.bcn.vincles.mobile.Utils.OtherUtils;
import io.fabric.sdk.android.Fabric;
import java.util.Timer;
import java.util.TimerTask;

import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.UI.FragmentManager.MainFragmentManagerActivity;
import cat.bcn.vincles.mobile.UI.TermsAndConditions.TermsAndConditionsActivity;


public class SplashScreenActivity extends AppCompatActivity {

    private static final long SPLASH_SCREEN_DELAY = 2000;

    Context context;

    @Override
    public void onStart() {
        super.onStart();
        OtherUtils.sendAnalyticsView(this, getResources().getString(R.string.tracking_terms_splash));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        Fabric.with(this, new Crashlytics(), new CrashlyticsNdk());
        setContentView(R.layout.activity_splash_screen);
        startTimer();
    }

    private void startTimer() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                UserPreferences userPreferences = new UserPreferences(getApplication());
                int userID = userPreferences.getUserID();
                boolean isLogged = userPreferences.getLoginDataDownloaded();

                //old preferences for migration
                SharedPreferences preferences = getSharedPreferences(
                        "cat.bcn.vincles.mobile.app-preferences", Context.MODE_PRIVATE);
                final long fase1UserId = preferences.getLong("cat.bcn.vincles.mobile.user-id", 0L);

                if (fase1UserId != 0) {
                    Log.d("migrt","fase 1 user id:"+fase1UserId);
                    Fase1SQLiteHelper sqLiteHelper = new Fase1SQLiteHelper(context);
                    String[] userPwd = sqLiteHelper.getUserPassword((int) fase1UserId);
                    Log.d("migrt","fase 1 username:"+userPwd[0]+" pwd:"+userPwd[1]);

                    Intent intent = new Intent().setClass(
                            SplashScreenActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    Bundle bundle = new Bundle();
                    bundle.putString("username", userPwd[0]);
                    bundle.putString("password", userPwd[1]);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    overridePendingTransition(0, 0);

                } else if (userID != 0 && isLogged) {
                    Intent intent = new Intent().setClass(
                            SplashScreenActivity.this, MainFragmentManagerActivity.class);
                    startActivity(intent);
                } else if (userID != 0) {
                    Intent intent = new Intent().setClass(
                            SplashScreenActivity.this, LoginActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent().setClass(
                            SplashScreenActivity.this, TermsAndConditionsActivity.class);
                    startActivity(intent);
                }
            }};

        Timer timer = new Timer();
        timer.schedule(task, SPLASH_SCREEN_DELAY);
    }
}
