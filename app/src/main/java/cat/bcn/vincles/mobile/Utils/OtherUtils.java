package cat.bcn.vincles.mobile.Utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cat.bcn.vincles.mobile.Client.Db.GroupMessageDb;
import cat.bcn.vincles.mobile.Client.Db.Model.GroupRealm;
import cat.bcn.vincles.mobile.Client.Db.UserGroupsDb;
import cat.bcn.vincles.mobile.Client.Model.UserRegister;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.R;
import io.realm.RealmList;

public class OtherUtils {

    public static boolean activityCannotShowDialog(Activity activity) {
        return activity == null || activity.isFinishing()
                || ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
                        && activity.isDestroyed());
    }

    public static int[] convertIntegers(List<Integer> integers)
    {
        int[] ret = new int[integers.size()];
        for (int i=0; i < ret.length; i++)
        {
            ret[i] = integers.get(i).intValue();
        }
        return ret;
    }

    public static ArrayList<Integer> convertIntegers(int[] integers) {
        ArrayList<Integer> list = new ArrayList<>();
        for (int i=0; i < integers.length; i++) {
            list.add(integers[i]);
        }
        return list;
    }

    public static RealmList<Integer> convertIntegersToRealmList(int[] integers) {
        RealmList<Integer> list = new RealmList<>();
        for (int i=0; i < integers.length; i++) {
            list.add(integers[i]);
        }
        return list;
    }

    public static RealmList<Integer> convertIntegersToRealmList(ArrayList<Integer> integers) {
        RealmList<Integer> list = new RealmList<>();
        if (integers != null) {
            for (int i=0; i < integers.size(); i++) {
                list.add(integers.get(i));
            }
        }
        return list;
    }

    public static RealmList<String> convertStringsToRealmList(ArrayList<String> strings) {
        RealmList<String> list = new RealmList<>();
        if (strings != null)
            list.addAll(strings);
        return list;
    }


    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int REQUEST_VIDEO_CAPTURE = 2;

    public static String sendPhotoIntent(Fragment fragment) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(fragment.getContext().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = ImageUtils.createImageFile(fragment.getContext());
                Uri photoURI = FileProvider.getUriForFile(fragment.getContext(),
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                fragment.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                return photoFile.getAbsolutePath();
            } catch (IOException ex) {
                // Error occurred while creating the File
                //TODO handle error
            }
        }
        return null;
    }

    public static void sendVideoIntent(Fragment fragment) {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT,120); // 2 minutes
        takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY,0); // low quality
        long maxVideoSize = 10*1024*1024; // 10 MB
        takeVideoIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, maxVideoSize);
        if (takeVideoIntent.resolveActivity(fragment.getContext().getPackageManager()) != null) {
            fragment.startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    public static void hideKeyboard(Activity activity) {
        if (activity != null) {
            InputMethodManager inputMethodManager =
                    (InputMethodManager) activity.getSystemService(
                            Activity.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null && activity.getCurrentFocus() != null) {
                inputMethodManager.hideSoftInputFromWindow(
                        activity.getCurrentFocus().getWindowToken(), 0);
            }
        }
    }

    public static boolean arraysEqual(ArrayList<Integer> a1, RealmList<Integer> a2) {
        if (a1.size() != a2.size()) return false;
        for (int val : a1) {
            if (!a2.contains(val)) return false;
        }
        return true;
    }

    public static String getMeetingInvitationState(String state, Resources resources) {
        switch (state) {
            case "PENDING": default:
                return resources.getString(R.string.calendar_invited_state);
            case "ACCEPTED":
                return resources.getString(R.string.calendar_accepted_state);
            case "REJECTED":
                return resources.getString(R.string.calendar_rejected_state);

        }
    }

    public static float getTextSizeNumberBullet(Resources resources, int sizePx) {
        float density = resources.getDisplayMetrics().density;
        int size = (int) (sizePx / density);
        Log.d("bullt","getTextSizeNumberBullet size: "+size);
        if (size < 18) {
            return resources.getDimension(R.dimen.contacts_icon_number_text_size_very_small);
        } else if (size < 20) {
            return resources.getDimension(R.dimen.contacts_icon_number_text_size_small);
        } else if (size < 23) {
            return resources.getDimension(R.dimen.contacts_icon_number_text_size_normal);
        } else if (size < 25) {
            return resources.getDimension(R.dimen.contacts_icon_number_text_size_big);
        } else if (size < 28) {
            return resources.getDimension(R.dimen.contacts_icon_number_text_size_very_big);
        } else if (size < 32) {
            return resources.getDimension(R.dimen.contacts_icon_number_text_size_huge);
        } else {
            return resources.getDimension(R.dimen.contacts_icon_number_text_size_huge_x);
        }

    }

    public static String getDuration(int lengthDate, Resources resources) {
        switch (lengthDate) {
            case 30:
                return resources.getString(R.string.calendar_meeting_duration_30);
            case 60:
                return resources.getString(R.string.calendar_meeting_duration_60);
            case 90:
                return resources.getString(R.string.calendar_meeting_duration_90);
            case 120:
                return resources.getString(R.string.calendar_meeting_duration_120);
        }
        return "";
    }

    public static String getArticleBeforeName(Locale locale, String name, String gender) {
        if (!DateUtils.isCatalan(locale)) return "";
        if ("aeiou".indexOf(Character.toLowerCase(name.charAt(0))) != -1) {
            return "L'";
        }
        if (gender.equals(UserRegister.MALE)) return "El ";
        return "La ";
    }

    public static void sendAnalyticsView(Context context, String name) {
        String tracker = context.getResources().getString(R.string.analytic_key);
        Tracker t = GoogleAnalytics.getInstance(context.getApplicationContext()).newTracker(tracker);
        t.setScreenName(name);
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public static void clearFragmentsBackstack(FragmentManager fm) {
        for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStack();
        }
    }

    public static void updateGroupOrDynChatInfo(int idChat) {
        Context context = MyApplication.getAppContext();
        int idMe = new UserPreferences(context).getUserID();
        GroupMessageDb groupMessageDb = new GroupMessageDb(context);
        UserGroupsDb userGroupsDb = new UserGroupsDb(MyApplication.getAppContext());
        GroupRealm groupRealm = userGroupsDb.getGroupFromIdChat(idChat);
        if (groupRealm != null) {
            new UserGroupsDb(context).setMessagesInfo(idChat, groupMessageDb
                            .getNumberUnreadMessagesReceived(idMe, idChat),
                    groupMessageDb.getTotalNumberMessages(idChat));
        } else { //its dynamizer
            new UserGroupsDb(context).setDynamizerMessagesInfo(idChat, groupMessageDb
                            .getNumberUnreadMessagesReceived(idMe, idChat),
                    groupMessageDb.getTotalNumberMessages(idChat));
        }
    }

    public static void saveAccount(String username, String password, AccountManager accountManager) {
        Account[] accounts = null;
        try {
            accounts = accountManager.getAccountsByType("cat.bcn.vincles.mobile");
        } catch (Exception ignored) {

        }
        boolean saveNewAccount = false;
        if (accounts != null && accounts.length > 0) {
            if (accounts[0].name.equals(username)) {
                accountManager.setPassword(accounts[0], password);
            } else {
                saveNewAccount = true;
                removeAccount(accounts[0], accountManager);
            }
        } else {
            saveNewAccount = true;
        }
        if (saveNewAccount) {
            Account account = new Account(username, "cat.bcn.vincles.mobile");
            accountManager.addAccountExplicitly(account, password, null);
        }
    }

    public static void updateAccountPassword(String username, String password, AccountManager accountManager) {
        Account[] accounts = null;
        try {
            accounts = accountManager.getAccountsByType("cat.bcn.vincles.mobile");
        } catch (Exception ignored) {

        }
        if (accounts != null && accounts.length > 0) {
            if (accounts[0].name.equals(username)) {
                accountManager.setPassword(accounts[0], password);
            }
        }
    }

    private static void removeAccount(Account account, AccountManager accountManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            accountManager.removeAccountExplicitly(account);
        } else {
            accountManager.removeAccount(account, null, null);
        }
    }

    public static void deleteAccountIfExisting(AccountManager accountManager) {
        Account[] accounts = null;
        try {
            accounts = accountManager.getAccountsByType("cat.bcn.vincles.mobile");
        } catch (Exception ignored) {

        }
        if (accounts != null && accounts.length > 0) {
            removeAccount(accounts[0], accountManager);
        }
    }

    public static boolean isFileTooBigForServer(String filePath) {
        File file = new File(filePath);
        int file_size = Integer.parseInt(String.valueOf(file.length()/1024)); //size in KB
        Log.d("flsze","file too big? size:"+file_size);
        return 10*1024 < file_size;
    }

}
