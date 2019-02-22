package cat.bcn.vincles.mobile.UI.Common;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;

import java.util.Locale;

import cat.bcn.vincles.mobile.Client.Model.UserRegister;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(updateBaseContextLocale(base));
    }

    private Context updateBaseContextLocale(Context context) {
        String language = new UserPreferences(context).getUserLanguage();
        Log.d("lng","set base activity, language: "+language);
        Resources res = context.getResources();
        Configuration conf = res.getConfiguration();
        if (language.equals(UserRegister.LANGUAGE_NOT_SET) && getLocale(conf).contains("es")) {
            language = "es";
        } else if (language.equals(UserRegister.LANGUAGE_NOT_SET)) {
            language = "ca";
        } else {
            language = language.equals(UserRegister.ESP) ? "es" : "ca";
        }

        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        float fontScaleSystem = Settings.System.getFloat(context.getContentResolver(),
                Settings.System.FONT_SCALE, 1.0f);
        Log.d("fntsz","Font scale system:"+fontScaleSystem +" fontScaleBeforeChange:"+conf.fontScale);

        UserPreferences userPreferences = new UserPreferences();
        conf.fontScale = fontScaleSystem;
        if (userPreferences.getFontSize() == UserPreferences.FONT_SIZE_SMALL) {
            conf.fontScale = conf.fontScale - 0.15f;
        } else if (userPreferences.getFontSize() == UserPreferences.FONT_SIZE_BIG) {
            conf.fontScale = conf.fontScale + 0.15f;
        }
        Log.d("fntsz","Font scale:"+conf.fontScale);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return updateResourcesLocale(context, locale);
        }

        return updateResourcesLocaleLegacy(context, locale);
    }

    private String getLocale(Configuration conf) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return conf.getLocales().get(0).toString();
        } else {
            return conf.locale.toString();
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    private Context updateResourcesLocale(Context context, Locale locale) {
        Configuration configuration = context.getResources().getConfiguration();
        configuration.setLocale(locale);
        return context.createConfigurationContext(configuration);
    }

    @SuppressWarnings("deprecation")
    private Context updateResourcesLocaleLegacy(Context context, Locale locale) {
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        return context;
    }


}
