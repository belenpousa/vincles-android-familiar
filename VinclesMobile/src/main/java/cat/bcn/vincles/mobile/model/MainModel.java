/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.model;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.gson.JsonObject;
import com.orm.SchemaGenerator;
import com.orm.SugarContext;
import com.orm.SugarDb;
import com.orm.util.NamingHelper;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import cat.bcn.vincles.lib.business.ServiceGenerator;
import cat.bcn.vincles.lib.business.UserService;
import cat.bcn.vincles.lib.dao.UserDAO;
import cat.bcn.vincles.lib.dao.UserDAOImpl;
import cat.bcn.vincles.lib.push.CommonVinclesGcmHelper;
import cat.bcn.vincles.lib.push.VinclesPushListener;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.util.ErrorHandler;
import cat.bcn.vincles.lib.util.ImageUtils;
import cat.bcn.vincles.lib.util.Model;
import cat.bcn.vincles.lib.util.TokenAuthenticator;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.util.VinclesError;
import cat.bcn.vincles.lib.vo.Installation;
import cat.bcn.vincles.lib.vo.Network;
import cat.bcn.vincles.lib.vo.User;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.activity.VinclesActivity;
import cat.bcn.vincles.mobile.monitors.SignalStrengthMonitor;
import cat.bcn.vincles.mobile.push.AppFCMDefaultListenerImpl;
import cat.bcn.vincles.mobile.push.VinclesInstanceIDListenerService;
import cat.bcn.vincles.mobile.util.VinclesMobileConstants;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainModel implements Model {
    private static final String TAG = "MainModel";
    public static boolean avoidServerCalls = true;
    private boolean initialized;
    private NetworkModel networkModel;
    private UserDAO userDAO;
    private static MainModel instance;
    public ProgressDialog busy;
    public String accessToken;
    private VinclesPushListener vinclesPushListener;
    public long lastNotificationCheck, lastRateGooglePlayCheck;
    private boolean checkingNotificationsWorking = false;
    private ConnectivityManager connectivityManager;
    private AudioManager audioManager;
    public NetworkInfo networkInfo;
    public TelephonyManager telephonyManager;
    public SignalStrengthMonitor phoneListener;
    public boolean isLowConnection;

    public static MainModel getInstance() {
        if (instance == null) {
            instance = new MainModel();
        }
        return instance;
    }

    public String language;
    public String country;
    public User currentUser;
    public Network currentNetwork;
    public SharedPreferences preferences;
    public Context context;
    public String view;
    public boolean tour;
    public boolean notifications;
    public boolean downloads;
    public boolean synchronizations;
    public Locale locale;

    public static final String REGISTER_DISCLAIMER = "registerDisclaimer";
    public static final String REGISTER_BLOCKED = "registerBlocked";
    public static final String JOIN_SUCCESS = "joinSuccess";
    public static final String JOIN_ERROR = "joinError";
    public static final String JOIN_ERROR_CODI = "joinErrorCodi";
    public static final String CONFIGURATION_DATA = "configurationData";
    public static final String VIDEO_CALLING = "videoCalling";

    private MainModel() {
    }

    public void initialize(Context context) {
        initialize(context, false);
    }

    public void initialize(Context context, boolean forceUpdate) {
        if (!initialized || forceUpdate) {
            initialized = true;
            this.context = context;

            networkModel = NetworkModel.getInstance();
            userDAO = new UserDAOImpl();

            initData();
        }
    }

    private void initData() {
        audioManager = (AudioManager) context.getSystemService(context.AUDIO_SERVICE);
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        // Read preferences
        view = "";
        preferences = context.getSharedPreferences(VinclesMobileConstants.APP_PREFERENCES, Context.MODE_PRIVATE);

        long userId = preferences.getLong(VinclesMobileConstants.USER_ID, 0l);
        currentUser = getUser(userId);
        if (currentUser == null) {
            currentUser = new User();
        } else {
            // Update TokenAuthenticator values for re-login
            TokenAuthenticator.username = currentUser.username;
            TokenAuthenticator.password = currentUser.cipher;
        }
        TokenAuthenticator.model = this;

        lastNotificationCheck = preferences.getLong(VinclesMobileConstants.APP_LASTNOTIFICATIONCHECK, System.currentTimeMillis());
        lastRateGooglePlayCheck = preferences.getLong(VinclesMobileConstants.APP_LASTRATEGOOGLEPLAYCHECK, System.currentTimeMillis());

        notifications = preferences.getBoolean(VinclesMobileConstants.APP_NOTIFICATIONS, true);
        downloads = preferences.getBoolean(VinclesMobileConstants.APP_DOWNLOADS, true);
        synchronizations = preferences.getBoolean(VinclesMobileConstants.APP_SYNCHRONIZATIONS, false);

        language = preferences.getString(VinclesMobileConstants.APP_LANGUAGE, Locale.getDefault().getLanguage());
        country = preferences.getString(VinclesMobileConstants.APP_COUNTRY, Locale.getDefault().getCountry());
        tour = preferences.getBoolean(VinclesMobileConstants.TOUR, false);

        // Select current network
        currentNetwork = networkModel.getNetwork(preferences.getLong(VinclesMobileConstants.NETWORK_CODE, 0l));
        if (currentNetwork != null) {
            currentNetwork.selected = true;
        }

        // Recover status
        updateLocale(language, country);
    }

    public void login(final AsyncResponse response, final String username, final String password) {
        Log.i(TAG, "login()");
        UserService client = ServiceGenerator.createLoginService(UserService.class);
        Call<JsonObject> call = client.login(username + VinclesConstants.LOGIN_SUFFIX, password);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> result) {
                Log.i(TAG, "result: " + result.body());

                if (result.isSuccessful()) {
                    // Set authToken globally for further request
                    JsonObject json = result.body();
                    accessToken = json.get("access_token").getAsString();

                    currentUser.username = username;
                    currentUser.active = true;
                    currentUser.save();
                    setPassword(currentUser, password);

                    updateTokenAuthenticator(username, currentUser.cipher);

                    response.onSuccess(true);
                } else {
                    String errorCode = ErrorHandler.parseError(result).getCode();
                    response.onFailure(errorCode);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.i(TAG, "login() - error: " + t.getMessage());
                response.onFailure(t);
            }
        });
    }

    private void updateTokenAuthenticator(String username, byte[] pass) {
        // Update TokenAuthenticator values for re-login
        TokenAuthenticator.username = username;
        TokenAuthenticator.password = pass;
    }

    public void recoverPassword(final AsyncResponse response, final String mail) {
        Log.i(TAG, "recoverPassword()");
        JsonObject json = new JsonObject();
        json.addProperty("username", mail);

        UserService client = ServiceGenerator.createService(UserService.class, accessToken);
        Call<ResponseBody> call = client.recoverPassword(json);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> result) {
                Log.i(TAG, "recoverPassword() - result: " + result.body());

                if (result.isSuccessful()) {
                    response.onSuccess(true);

                } else {
                    String errorCode = ErrorHandler.parseError(result).getCode();
                    response.onFailure(errorCode);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.i(TAG, "recoverPassword() - error: " + t.getMessage());
                response.onFailure(t);
            }
        });
    }

    public void changeUserPassword(final AsyncResponse response, final String newPassword) {
        Log.i(TAG, "changeUserPassword()");
        JsonObject json = new JsonObject();
        json.addProperty("currentPassword", getPassword(currentUser));
        json.addProperty("newPassword", newPassword);

        UserService client = ServiceGenerator.createService(UserService.class, accessToken);
        Call<JsonObject> call = client.changePassword(json);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> result) {
                Log.i(TAG, "changeUserPassword() - result: " + result.body());

                if (result.isSuccessful()) {
                    try {
                        JsonObject userJSON = result.body();
                        accessToken = userJSON.getAsJsonObject("signInInfo")
                                .get("access_token").getAsString();

                        setPassword(currentUser, newPassword);
                        saveUser(currentUser);
                        response.onSuccess(true);
                    } catch (Exception e) {
                        response.onFailure(
                                context.getResources().getString(R.string.error_default)
                        );
                    }

                } else {
                    String errorCode = ErrorHandler.parseError(result).getCode();
                    response.onFailure(errorCode);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.i(TAG, "changeUserPassword() - error: " + t.getMessage());
                response.onFailure(t);
            }
        });
    }

    public void migrateUser(final AsyncResponse response, final String email, final String newPassword) {
        Log.i(TAG, "migrateUser()");
        JsonObject json = new JsonObject();
        json.addProperty("email", email);
        json.addProperty("password", newPassword);

        UserService client = ServiceGenerator.createService(UserService.class, accessToken);
        Call<ResponseBody> call = client.migrateUser(json);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> result) {
                Log.i(TAG, "changeUserPassword() - result: " + result.body());

                if (result.isSuccessful()) {
                    // CREATE OR USE A EXISTING TEMPORAL CLONE TO USE DURING THE PROCESS
                    User temp;
                    Long tempId;
                    if (preferences.getLong(VinclesMobileConstants.MIGRATION_USER_ID, 0l) != 0l ) {
                        tempId = preferences.getLong(VinclesMobileConstants.MIGRATION_USER_ID, 0l);
                        temp = userDAO.get(tempId);
                    } else {
                        tempId = preferences.getLong(VinclesMobileConstants.USER_ID, 0l);
                        temp = new User().fromJSON(new UserDAOImpl().get(tempId).toJSON());
                        temp.idContentPhoto = currentUser.idContentPhoto;
                        temp.imageName = currentUser.imageName;
                        temp.setId(null);
                    }
                    temp.email = email;
                    temp.username = email;
                    temp.active = false;
                    tempId = saveUser(temp);

                    setPassword(temp, newPassword);
//                    updateTokenAuthenticator(temp.email, temp.cipher);

                    //SAVE MIGRATE TEMP USER
                    savePreferences(VinclesMobileConstants.MIGRATION_USER_ID, tempId, VinclesConstants.PREFERENCES_TYPE_LONG);
                    response.onSuccess(true);

                } else {
                    String errorCode = ErrorHandler.parseError(result).getCode();
                    response.onFailure(errorCode);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.i(TAG, "migrateUser() - error: " + t.getMessage());
                response.onFailure(t);
            }
        });
    }

    public void updateLocalUser(final AsyncResponse response) {
        Log.i(TAG, "updateLocalUser()");
        UserService client = ServiceGenerator.createService(UserService.class, accessToken);
        Call<JsonObject> call = client.getMyUserInfo();
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> result) {
                Log.i(TAG, "updateLocalUser() - result: " + result.body());

                if (result.isSuccessful()) {
                    JsonObject userJSON = result.body();
                    User cloudInfo = User.fromJSON(userJSON);

                    if (currentUser == null)
                        currentUser = new User();

                    currentUser.name = cloudInfo.name;
                    currentUser.lastname = cloudInfo.lastname;
                    currentUser.alias = cloudInfo.alias;
                    currentUser.birthdate = cloudInfo.birthdate;
                    currentUser.email = cloudInfo.email;
                    currentUser.username = cloudInfo.username;
                    currentUser.liveInBarcelona = cloudInfo.liveInBarcelona;
                    currentUser.phone = cloudInfo.phone;
                    currentUser.gender = cloudInfo.gender;
                    currentUser.save();

                    // UPDATE ID ALSO:
                    String pass = getPassword(currentUser);
                    User.executeQuery(
                            "UPDATE " + NamingHelper.toSQLName(User.class) + " SET id = ? WHERE id = ?",
                            new String[] {"" + cloudInfo.getId(), "" + currentUser.getId()});
                    currentUser.setId(cloudInfo.getId());
                    setPassword(currentUser, pass);
                    savePreferences(VinclesMobileConstants.USER_ID, cloudInfo.getId(), VinclesConstants.PREFERENCES_TYPE_LONG);
                    pass = null;

                    // Update photo at server
                    getUserPhoto(new AsyncResponse() {
                        @Override
                        public void onSuccess(Object result) {
                            saveUser(currentUser);
                            response.onSuccess(true);
                        }

                        @Override
                        public void onFailure(Object error) {
                            response.onFailure("UPDATE_PHOTO_ERROR");
                        }
                    }, currentUser);
                } else {
                    String errorCode = ErrorHandler.parseError(result).getCode();
                    response.onFailure(errorCode);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.i(TAG, "updateLocalUser() - error: " + t.getMessage());
                response.onFailure(t);
            }
        });
    }

    public boolean isConnected() {
        Log.i(TAG, "isConnected()");
        boolean result = false;
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                result = true;
            }
        }
        return result;
    }

    public int getConnectionType() {
//        Log.i(TAG, "getConnectionType()");
        int result = 0;
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                result = networkInfo.getType();
            }
        }
        return result;
    }

    private void synchronize() {
        Log.i(TAG, "synchronize()");
        busy.dismiss();
    }

    public Configuration updateLocale(String language, String country) {
        this.language = language;
        this.country = country;

        locale = new Locale(language, country);
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = locale;
        res.updateConfiguration(conf, dm);
        return conf;
    }

    public void savePreferences(String key, Object value, String type) {
        SharedPreferences.Editor editor = preferences.edit();
        switch (type) {
            case VinclesConstants.PREFERENCES_TYPE_STRING:
                editor.putString(key, (String) value);
                break;
            case VinclesConstants.PREFERENCES_TYPE_FLOAT:
                editor.putFloat(key, (Float) value);
                break;
            case VinclesConstants.PREFERENCES_TYPE_BOOLEAN:
                editor.putBoolean(key, (boolean) value);
                break;
            case VinclesConstants.PREFERENCES_TYPE_INT:
                editor.putInt(key, (int) value);
                break;
            case VinclesConstants.PREFERENCES_TYPE_LIST:
                editor.remove(key);
                editor.putStringSet(key, (Set<String>) value);
                break;
            case VinclesConstants.PREFERENCES_TYPE_LONG:
                editor.putLong(key, (long) value);
                break;
        }
        editor.commit();
    }

    public void clearPreferences() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
        tour = false;

        initData();
    }

    public long saveUser(User item) {
        return userDAO.save(item);
    }

    public User getUser(Long id) {
        return userDAO.get(id);
    }

    public Bitmap getLowImage(String filename) {
        Bitmap result = getLowBitmapFromPath(VinclesConstants.getImageDirectory() + "/" + filename);
        return result;
    }

    public Bitmap getLowBitmapFromPath(String path) {
        Bitmap result = ImageUtils.decodeSampledBitmapFromFile(path, VinclesConstants.IMAGE_MIN_WIDTH, VinclesConstants.IMAGE_MIN_HEIGHT);
        return result;
    }

    public void registerUser(final AsyncResponse response, final User user, final String password) {
        Log.i(TAG, "registerUser()");
        UserService client = ServiceGenerator.createService(UserService.class);
        Call<JsonObject> call = client.register(user.toJSON(true, password));
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> result) {
                Log.i(TAG, "registerUser() - result: " + result.body());

                if (result.isSuccessful()) {
                    JsonObject json = result.body();
                    Log.d(null, json.toString());

                    // Create User for the first time
                    currentUser = user;
                    currentUser.username = currentUser.email;

                    // Restore properties
                    // Save properties to restore
                    String restoreImagePath = currentUser.imageName;
                    currentUser.imageName = restoreImagePath;

//                    // DO NOT UPDATE FOTO YET CAUSE USER IS NOT VALIDATED
//                    updateUserPhoto(currentUser.imageName);

                    saveUser(currentUser);
                    setPassword(currentUser, password);
                    savePreferences(VinclesMobileConstants.USER_ID, currentUser.getId(), VinclesConstants.PREFERENCES_TYPE_LONG);

                    // Update TokenAuthenticator values for re-login
                    updateTokenAuthenticator(currentUser.email, currentUser.cipher);

                    response.onSuccess(json);
                } else {
                    String errorCode = ErrorHandler.parseError(result).getCode();
                    response.onFailure(errorCode);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.i(TAG, "associateAnonymous() - error: " + t.getMessage());
                response.onFailure(t);
            }
        });
    }

    public void validateUser(final AsyncResponse response, String verificationCode) {
        Log.i(TAG, "validateUser()");
        JsonObject json = new JsonObject();
        json.addProperty("email", currentUser.email);
        json.addProperty("code", verificationCode);

        UserService client = ServiceGenerator.createService(UserService.class);
        Call<JsonObject> call = client.validateUser(json);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> result) {
                Log.i(TAG, "validateUser() - result: " + result.body());

                if (result.isSuccessful()) {
                    // THE REAL ID
                    JsonObject json = result.body();

                    // RE-SET User ID To real one
                    Long newID = json.get("id").getAsLong();
                    User.executeQuery(
                            "UPDATE " + NamingHelper.toSQLName(User.class) + " SET id = ? WHERE id = ?",
                            new String[] {newID.toString(), currentUser.getId().toString()});
                    String pass = getPassword(currentUser);
                    currentUser = userDAO.get(newID);

                    currentUser.active = true;
                    saveUser(currentUser);
                    setPassword(currentUser, pass);
                    savePreferences(VinclesMobileConstants.USER_ID, newID, VinclesConstants.PREFERENCES_TYPE_LONG);
                    pass = null;

                    updateTokenAuthenticator(currentUser.email, currentUser.cipher);

                    response.onSuccess(true);

                } else {
                    String errorCode = ErrorHandler.parseError(result).getCode();
                    response.onFailure(errorCode);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.i(TAG, "validateUser() - error: " + t.getMessage());
                response.onFailure(t);
            }
        });
    }

    public void migrateValidateUser(final AsyncResponse response, String verificationCode) {
        Log.i(TAG, "migrateValidateUser()");
        JsonObject json = new JsonObject();
        json.addProperty("code", verificationCode);

        UserService client = ServiceGenerator.createService(UserService.class);
        Call<ResponseBody> call = client.migrateValidateUser(json);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> result) {
                Log.i(TAG, "migrateValidateUser() - result: " + result.body());

                if (result.isSuccessful()) {
                    // RECOVER MIGRATION TEMPORAL USER
                    Long tempId = preferences.getLong(VinclesMobileConstants.MIGRATION_USER_ID, 0l);
                    User temp = userDAO.get(tempId);

                    currentUser.active = true;
                    currentUser.username = temp.email;
                    currentUser.email = temp.email;
                    saveUser(currentUser);
                    setPassword(currentUser, getPassword(temp));
                    temp.delete();

//                    savePreferences(VinclesMobileConstants.USER_ID, newID, VinclesConstants.PREFERENCES_TYPE_LONG);
                    updateTokenAuthenticator(currentUser.email, currentUser.cipher);
                    response.onSuccess(true);

                } else {
                    String errorCode = ErrorHandler.parseError(result).getCode();
                    response.onFailure(errorCode);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.i(TAG, "migrateValidateUser() - error: " + t.getMessage());
                response.onFailure(t);
            }
        });
    }

    public void associateRegistered(final AsyncResponse response, JsonObject association) {
        Log.i(TAG, "associateRegistered()");
        UserService client = ServiceGenerator.createService(UserService.class, accessToken);
        Call<JsonObject> call = client.associateRegistered(association);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> result) {
                Log.i(TAG, "associateRegistered() - result: " + result.body());

                if (result.isSuccessful()) {
                    JsonObject json = result.body();
                    response.onSuccess(json);
                } else {
                    String errorCode = ErrorHandler.parseError(result).getCode();
                    response.onFailure(errorCode);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.i(TAG, "associateRegistered() - error: " + t.getMessage());
                response.onFailure(t);
            }
        });
    }

    public String getErrorByCode(Object error) {
        String result = context.getResources().getString(R.string.error_default);
        if (error instanceof Exception) {
            result = ((Exception) error).getMessage();
            if (error instanceof SocketTimeoutException) {
                result = context.getResources().getString(R.string.error_connection);
            } else if (error instanceof ConnectException) {
                result = context.getResources().getString(R.string.error_server);
            } else if (error instanceof IOException) {
                if (VinclesError.ERROR_LOGIN.equals(((IOException) error).getMessage())) {
                    result = context.getResources().getString(R.string.error_login);
                }
            }
        } else if (error instanceof Error) {
            result = ((Error) error).getMessage();
        } else {
            String code = "";
            if (error instanceof VinclesError) {
                code = ((VinclesError)error).getCode();
            } else if (error != null) {
                code = (String) error;
            }
            switch (code) {
                // CODI INCORRECTE
                case "2803":
                case "2701":
                    result = context.getResources().getString(R.string.error_2701);
                    break;
                case "1110":
                    result = context.getResources().getString(R.string.error_1110);
                    break;
                case "1908":
                    result = context.getResources().getString(R.string.error_1908);
                    break;
                case "1606":
                    result = context.getResources().getString(R.string.error_1606);
                    break;
                case "1321":
                    result = context.getResources().getString(R.string.error_1321);
                    break;
                case VinclesError.ERROR_CODE:
                    result = context.getResources().getString(R.string.error_1300);
                    break;
                case "1": // VinclesError.ERROR_CONNECTION
                    result = context.getResources().getString(R.string.error_connection);
                    break;
                case "invalid_grant": // VinclesError.ERROR_LOGIN
                    result = context.getResources().getString(R.string.error_login);
                    break;
                case VinclesError.ERROR_CANCEL: // VinclesError.ERROR_CANCEL
                    result = context.getResources().getString(R.string.error_cancel);
                    break;
                case VinclesError.ERROR_FILE_NOT_FOUND: // VinclesError.ERROR_FILE_NOT_FOUND:
                    result = context.getResources().getString(R.string.error_no_file);
                    break;
                case "1501":
                    // Content not found in library
                    result = context.getResources().getString(R.string.error_1501);
                    break;
                default: // VinclesError.ERROR_DEFAULT
                    result = context.getResources().getString(R.string.error_default);
                    break;
            }
        }

        return result;
    }

    public void showSimpleError(View view, String text, int time) {
        try {
            Log.e(TAG, text);
            Snackbar.make(view, text, time).show();
        } catch (Exception e) {
            Log.e(TAG, text);
        }
    }

    public void showCustomError(View view, String text, int time,
            int id_icon, String buttonString, View.OnClickListener buttonListener) {
        try {
            Log.e(TAG, text);

            Snackbar snackbar = Snackbar.make(view, text, time);
            if (id_icon != -1) {
                snackbar.getView().findViewById(R.id.snackbar_icon).setVisibility(View.VISIBLE);
                ((ImageView) snackbar.getView().findViewById(R.id.snackbar_icon))
                        .setImageResource(R.drawable.icon_user_block);
            }
            snackbar.setAction(buttonString, buttonListener);
            snackbar.show();
        } catch (Exception e) {
            Log.e(TAG, text);
        }
    }

    @Override
    public void updateAccessToken(String token) {
        accessToken = token;
    }

    @Override
    public String getAccessToken() {
        return accessToken;
    }

    @Override
    public Long getCurrentUserId() {
        return currentUser.getId();
    }

    public void showBusy(String title, String msg) {
        Log.i(TAG, "showBusy()");
        busy = ProgressDialog.show(context, title, msg, true, true);
        busy.show();
    }

    public void hideBusy() {
        Log.i(TAG, "hideBusy()");
        if (busy != null && busy.isShowing()) {
            busy.dismiss();
        }
    }

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    public void startGCM(Activity activity) {
        if (Installation.findById(Installation.class, 1) == null) {
//            if (checkPlayServices(activity)) {
            VinclesInstanceIDListenerService.forceRefreshToken(activity);
//            }
        }

        // SET MESSAGE LISTENER HERE
        if (vinclesPushListener == null)
            CommonVinclesGcmHelper.setPushListener(getPushListener());
    }

    public VinclesPushListener getPushListener() {
        if (vinclesPushListener == null) {
            vinclesPushListener = new AppFCMDefaultListenerImpl(context);
        }
        return vinclesPushListener;
    }

    private boolean checkPlayServices(Activity activity) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(activity, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                activity.finish();
            }
            return false;
        }
        return true;
    }

    public String getRealVideoPathFromURI(Uri uri) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, null, null, null, null);
            cursor.moveToFirst();
            int idx = cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DATA);
            return cursor.getString(idx);
        } catch (Exception e) {
            return uri.getPath();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public String getRealImagePathFromURI(Uri uri) throws Exception {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, null, null, null, null);
            cursor.moveToFirst();
            int idx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            return cursor.getString(idx);
        } catch (Exception e) {
            Cursor cursor2 = null;
            try {
                String[] projection = {MediaStore.MediaColumns.DATA};

                cursor2 = context.getContentResolver().query(uri, projection, null, null, null);
                if (cursor2 != null) {
                    int column_index = cursor2.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                    cursor2.moveToFirst();
                    return cursor2.getString(column_index);
                }
            } catch (Exception exc){
                throw exc;
            } finally {
                if (cursor2 != null) {
                    cursor2.close();
                }
            }
            return uri.getPath();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void updateUserServer(final User user) {
        Log.i(TAG, "updateUserServer()");
        UserService client = ServiceGenerator.createService(UserService.class, accessToken);
        Call<ResponseBody> call = client.updateUser(user.toJSON());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> result) {
                Log.i(TAG, "result: " + result.body());
                // Nothing to do!!!
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.i(TAG, "updateUserServer() - error: " + t.getMessage());
                String errorMessage = getErrorByCode(t);
                MainModel.getInstance().showSimpleError(VinclesActivity.instance.findViewById(R.id.main_content),
                        errorMessage,
                        Snackbar.LENGTH_LONG);
            }
        });
    }

    public void getFullUserInfo(final AsyncResponse response, final long userId) {
        Log.i(TAG, "getFullUserInfo()");
        UserService client = ServiceGenerator.createService(UserService.class, accessToken);
        Call<JsonObject> call = client.getFullUserInfo(userId);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> result) {
                if (result.isSuccessful()) {
                    JsonObject json = result.body();
                    final User user = User.fromJSON(json);
                    if (checkPhotoAndSaveUser(user))
                        // Get user photo
                        getUserPhoto(new AsyncResponse() {
                            @Override
                            public void onSuccess(Object result) {
                                response.onSuccess(user);
                            }

                            @Override
                            public void onFailure(Object error) {
                                String errorMessage = getErrorByCode(error);
                                Toast toast = Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT);
                                toast.show();
                                Log.e(TAG, "getUserPhoto - error: " + errorMessage);
                                response.onFailure(error);
                            }
                        }, user);
                    response.onSuccess(user);
                } else {
                    VinclesError error = ErrorHandler.parseError(result);
                    String errorMessage = getErrorByCode(error);
                    Toast toast = Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT);
                    toast.show();
                    Log.e(TAG, "error: " + errorMessage);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.i(TAG, "getFullUserInfo() - error: " + t.getMessage());
                String errorMessage = getErrorByCode(t);
                Toast toast = Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    public boolean checkPhotoAndSaveUser(User item) {
        User tempUser = userDAO.get(item.getId());
        userDAO.save(item);
        if (tempUser != null)
            return tempUser.idContentPhoto != item.idContentPhoto;
        else return true;
    }

    public void getUserPhoto(final AsyncResponse response, final User user) {
        UserService client = ServiceGenerator.createService(UserService.class, accessToken);
        Call<ResponseBody> call = client.getUserPhoto(user.getId());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> result) {
                if (result.isSuccessful()) {
                    byte[] data = null;
                    try {
                        data = IOUtils.toByteArray(result.body().byteStream());
                        String imageName = VinclesConstants.IMAGE_USER_PREFIX + new Date().getTime() + VinclesConstants.IMAGE_EXTENSION;
                        VinclesConstants.saveImage(data, imageName);

                        // Update reference to user image file
                        user.imageName = imageName;
                        saveUser(user);
                        response.onSuccess(imageName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    VinclesError error = ErrorHandler.parseError(result);
                    response.onFailure(error);
                    Log.e(TAG, "error: " + error.getMessage());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.i(TAG, "getUserPhoto() - error: " + t.getMessage());
                response.onFailure(t);
            }
        });
    }

    public void updateUserPhoto(String nameFile) {
        Log.i(TAG, "updateUserPhoto()");
        File imageFile = new File(VinclesConstants.getImagePath(), nameFile);
        RequestBody file = RequestBody.create(MediaType.parse("image/jpeg"), imageFile);
        MultipartBody.Part data = MultipartBody.Part.createFormData("file", nameFile, file);

        UserService client = ServiceGenerator.createService(UserService.class, accessToken);
        Call<JsonObject> call = client.updateUserPhoto(data);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call2, Response<JsonObject> result) {
                if (result.isSuccessful()) {
                    // Nothing to do!!!
                    Log.i(TAG, "result: " + result.body());
                } else {
                    VinclesError error = ErrorHandler.parseError(result);
                    String errorMessage = getErrorByCode(error);
                    Toast toast = Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT);
                    toast.show();
                    Log.e(TAG, "error: " + errorMessage);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call2, Throwable t) {
                Log.i(TAG, "login() - error: " + t.getMessage());
                String errorMessage = getErrorByCode(t);
                Toast toast = Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    public void checkNewNotifications() {
        if (checkingNotificationsWorking) return;
        checkingNotificationsWorking = true;
        // SHOULD NOT WORK ON MAIN THREAD
        new Thread() {
            @Override
            public void run()
            {
                try {
                    long result =
                            CommonVinclesGcmHelper.checkNewNotifications(lastNotificationCheck+1, accessToken);
                    if (result != -1 && result != lastNotificationCheck) {
                        lastNotificationCheck = result;
                        savePreferences(VinclesMobileConstants.APP_LASTNOTIFICATIONCHECK, lastNotificationCheck, VinclesConstants.PREFERENCES_TYPE_LONG);
                    }
                } catch (Exception e) {} finally {
                    checkingNotificationsWorking = false;
                }
            }
        }.start();
    }

    public String getUserPhotoUrlFromUser(User user) {
        return getUserPhotoUrlFromUserWithAction(user, null);
    }

    public String getUserPhotoUrlFromUserWithAction(User user, final AsyncResponse response) {
        if (user == null) return null;
        if (!new File(VinclesConstants.getImageDirectory() + "/" + user.imageName).exists()) {
            getUserPhoto(new AsyncResponse() {
                @Override
                public void onSuccess(Object result) {
                    if (response != null) response.onSuccess(result);
                }

                @Override
                public void onFailure(Object error) {
                    if (response != null) response.onFailure(error);
                }
            }, user);
        }
        return (VinclesConstants.getImageDirectory() + "/" + user.imageName);
    }

    public GlideUrl getUserPhotoUrlFromUserId(Long userId) {
        GlideUrl glideUrl = new GlideUrl(
                ServiceGenerator.getApiBaseUrl() + "API_URL_USERS" + userId + "/photo", new LazyHeaders.Builder()
                .addHeader("Authorization", "Bearer " + MainModel.getInstance().getAccessToken())
                .build());
        return glideUrl;
    }

    public void wipeout(Context context) {
        currentUser = null;
        accessToken = null;
        SugarContext.terminate();
        SchemaGenerator schemaGenerator = new SchemaGenerator(context);
        schemaGenerator.deleteTables(new SugarDb(context).getDB());
        SugarContext.init(context);
        schemaGenerator.createDatabase(new SugarDb(context).getDB());
        preferences.edit().remove(VinclesMobileConstants.MIGRATION_USER_ID).commit();
        preferences.edit().remove(VinclesMobileConstants.USER_ID).commit();
        initialized = false;
    }

    public String getPassword(User user) {
        return userDAO.getPassword(user);
    }

    public void setPassword(User user, String password) {
        userDAO.setPassword(user, password);
    }
}