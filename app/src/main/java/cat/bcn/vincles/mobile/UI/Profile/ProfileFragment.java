package cat.bcn.vincles.mobile.UI.Profile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;

import cat.bcn.vincles.mobile.Client.Business.CalendarSyncManager;
import cat.bcn.vincles.mobile.Client.Db.MeetingsDb;
import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Model.UserRegister;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Client.Requests.BaseRequest;
import cat.bcn.vincles.mobile.Client.Requests.UpdateUserRequest;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.UI.Alert.AlertMessage;
import cat.bcn.vincles.mobile.UI.Alert.AlertNonDismissable;
import cat.bcn.vincles.mobile.UI.Alert.AlertPickOrTakePhoto;
import cat.bcn.vincles.mobile.UI.Common.BaseFragment;
import cat.bcn.vincles.mobile.Utils.ImageUtils;
import cat.bcn.vincles.mobile.Utils.MyApplication;
import cat.bcn.vincles.mobile.Utils.OtherUtils;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends BaseFragment implements View.OnClickListener, AlertPickOrTakePhoto.AlertPickOrTakePhotoInterface, UpdateUserRequest.OnResponse, AlertMessage.AlertMessageInterface, RadioGroup.OnCheckedChangeListener, AlertPickOrTakePhoto.AlertPickOrTakePhotoClosed {

    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL = 0;
    private static final int CALENDAR_PERMISSION_REQUEST = 1;

    private static final int REQUEST_CAMERA = 1;
    private static final int SELECT_FILE = 2;

    private OnFragmentInteractionListener mListener;
    ImageView userAvatarView;
    ImageButton editData;
    TextView dataTV;
    Uri selectedImageUri = null;
    String pathAvatar;
    AlertPickOrTakePhoto alertPickOrTakePhoto;
    UserPreferences userPreferences;
    AlertMessage alertMessage;
    AlertNonDismissable alertNonDismissable;
    AlertNonDismissable alertWaitCalendarSync;
    RadioGroup languageRadioGroup, textSizeRadioGroup, autodownloadRadioGroup,
            syncCalendarRadioGroup, copyPhotosGalleryRadioGroup;
    RadioButton languageCat, languageCast;

    boolean repeatingCalendarPermissionsRequest = false;

    boolean alertPhotoShowing;

    boolean disableSyncClick = false;
    private boolean isImageErrorDialog = false;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance(FragmentResumed listener) {
        ProfileFragment fragment = new ProfileFragment();
        fragment.setListener(listener, FragmentResumed.FRAGMENT_CONFIGURATION);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("alertPhotoShowing", alertPhotoShowing);
        outState.putBoolean("isImageErrorDialog", isShowingImageErrorDialog());
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getContext() != null)
            OtherUtils.sendAnalyticsView(getContext(),
                    getResources().getString(R.string.tracking_config));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userPreferences = new UserPreferences(getContext());
        if(savedInstanceState != null ) isImageErrorDialog = savedInstanceState.getBoolean("isImageErrorDialog");
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_profile, container, false);
        editData = v.findViewById(R.id.personal_data_button);
        userAvatarView = v.findViewById(R.id.userAvatar);
        dataTV = v.findViewById(R.id.personal_data);
        languageRadioGroup = v.findViewById(R.id.language);
        autodownloadRadioGroup = v.findViewById(R.id.auto_download);
        copyPhotosGalleryRadioGroup = v.findViewById(R.id.copy_photos_gallery);
        syncCalendarRadioGroup = v.findViewById(R.id.sync_calendar);
        textSizeRadioGroup = v.findViewById(R.id.text_size);
        languageCat = v.findViewById(R.id.catalan);
        languageCast = v.findViewById(R.id.spanish);
        View backButton = v.findViewById(R.id.back);

        alertPickOrTakePhoto = new AlertPickOrTakePhoto(getActivity(), this);
        alertPickOrTakePhoto.setAlertPickOrTakePhotoClosed(this);

        alertNonDismissable = new AlertNonDismissable(getResources().getString(
                R.string.login_sending_data),true);
        alertWaitCalendarSync = new AlertNonDismissable(getResources().getString(
                R.string.processing),true);

        if (backButton != null) backButton.setOnClickListener(this);
        editData.setOnClickListener(this);
        userAvatarView.setOnClickListener(this);

        String email = userPreferences.getEmail() == null ? "" : userPreferences.getEmail();
        dataTV.setText(userPreferences.getName() + "\n" +
                userPreferences.getLastName() + "\n" +
                userPreferences.getUsername() + "\n" +
                email + "\n" +
                userPreferences.getPhone() +
                (userPreferences.livesInBarcelona() ?
                        "\n" + getResources().getString(R.string.configuration_lives_barcelona) :
                ""));

        Log.d("lng","Profile fragment, set language:"+userPreferences.getUserLanguage());
        String language = userPreferences.getUserLanguage();
        Configuration conf = getResources().getConfiguration();
        if (userPreferences.getUserLanguage().equals(UserRegister.ESP) ||
                (language.equals(UserRegister.LANGUAGE_NOT_SET) && getLocale(conf).contains("es"))) {
            languageRadioGroup.check(R.id.spanish);
        } else {
            languageRadioGroup.check(R.id.catalan);
        }
        languageRadioGroup.setOnCheckedChangeListener(this);

        if (userPreferences.getFontSize() == UserPreferences.FONT_SIZE_SMALL) {
            textSizeRadioGroup.check(R.id.small);
        } else if (userPreferences.getFontSize() == UserPreferences.FONT_SIZE_BIG) {
            textSizeRadioGroup.check(R.id.big);
        } else {
            textSizeRadioGroup.check(R.id.medium);
        }
        textSizeRadioGroup.setOnCheckedChangeListener(this);

        if(userPreferences.getIsAutodownload()) {
            autodownloadRadioGroup.check(R.id.auto_download_yes);
        } else {
            autodownloadRadioGroup.check(R.id.auto_download_no);
        }
        autodownloadRadioGroup.setOnCheckedChangeListener(this);

        if(userPreferences.getIsCopyPhotos()) {
            copyPhotosGalleryRadioGroup.check(R.id.copy_photos_gallery_yes);
        } else {
            copyPhotosGalleryRadioGroup.check(R.id.copy_photos_gallery_no);
        }
        copyPhotosGalleryRadioGroup.setOnCheckedChangeListener(this);

        if(userPreferences.getIsSyncCalendars()) {
            syncCalendarRadioGroup.check(R.id.sync_calendar_yes);
        } else {
            syncCalendarRadioGroup.check(R.id.sync_calendar_no);
        }
        syncCalendarRadioGroup.setOnCheckedChangeListener(this);

        pathAvatar = userPreferences.getUserAvatar();
        if (!pathAvatar.equals("")) {
            Uri avatarUri = Uri.parse(pathAvatar);
            userAvatarView.setImageURI(avatarUri);
        }

        if (savedInstanceState != null) {
            alertPhotoShowing = savedInstanceState.getBoolean("alertPhotoShowing", alertPhotoShowing);
        }
        if (alertPhotoShowing) {
            alertPickOrTakePhoto.showMessage();
            alertPhotoShowing = true;
            if (!pathAvatar.equals("")) {
                Uri myUri = Uri.parse(pathAvatar);
                alertPickOrTakePhoto.setImage(myUri);
            }
        }
        if(isShowingImageErrorDialog()) {
            showImageErrorDialog();
        }

        return v;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    private String getLocale(Configuration conf) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return conf.getLocales().get(0).toString();
        } else {
            return conf.locale.toString();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.userAvatar:
                alertPickOrTakePhoto.showMessage();
                alertPhotoShowing = true;
                if (!pathAvatar.equals("")) {
                    Uri myUri = Uri.parse(pathAvatar);
                    alertPickOrTakePhoto.setImage(myUri);
                }
                break;
            case R.id.personal_data_button:
                if (mListener != null) mListener.onProfileEditClicked();
                break;
            case R.id.back:
                getFragmentManager().popBackStack();
                break;
        }
    }

    private void sendPhoto() {
        String alias = userPreferences.getAlias();
        String email = userPreferences.getEmail();
        String name = userPreferences.getName();
        String lastname = userPreferences.getLastName();
        long birthdate = userPreferences.getBirthdate();
        String phone = userPreferences.getPhone();
        String gender = userPreferences.getGender();
        boolean livesInBarcelona = userPreferences.livesInBarcelona();

        String photo = ImageUtils.getImage64(pathAvatar);
        String photoMimeType = ImageUtils.getMimeType(pathAvatar);

        String accessToken = userPreferences.getAccessToken();
        UserRegister userRegister = new UserRegister( alias, name, lastname, birthdate, email,
                phone, gender, livesInBarcelona, photo, photoMimeType);

        UpdateUserRequest updateUserRequest = new UpdateUserRequest((BaseRequest.RenewTokenFailed) getActivity(), userRegister);
        updateUserRequest.addOnOnResponse(this);
        updateUserRequest.doRequest(accessToken);
    }

    @Override
    public void onTakePhoto(AlertPickOrTakePhoto alertPickOrTakePhoto) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    @Override
    public void onPickPhoto(AlertPickOrTakePhoto alertPickOrTakePhoto) {
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            doPickPhotoAction();
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            doPickPhotoAction();
        } else if (requestCode == CALENDAR_PERMISSION_REQUEST && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            doCalendarSync();
        } else if (requestCode == CALENDAR_PERMISSION_REQUEST) {
            repeatingCalendarPermissionsRequest = true;
            syncCalendarRadioGroup.check(R.id.sync_calendar_no);
        }
    }

    private void doPickPhotoAction() {
        Intent intent = new Intent(
               // Intent.ACTION_GET_CONTENT)
                 //       .setType("image/png");

        Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, SELECT_FILE);
    }

    @Override
    public void onAcceptPhoto(AlertPickOrTakePhoto alertPickOrTakePhoto) {
        alertPickOrTakePhoto.close();
        alertPhotoShowing = false;
        sendPhoto();
    }

    @Override
    public void onCancelPhoto(AlertPickOrTakePhoto alertPickOrTakePhoto) {
        alertPickOrTakePhoto.hideAccpetOrCancelBtns();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAMERA) {
            if (resultCode == RESULT_OK) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                if (photo == null) {
                    showImageErrorDialog();
                } else {
                    selectedImageUri = ImageUtils.getImageUri(getContext(), photo);
                    pathAvatar = selectedImageUri.getPath();
                    alertPhotoShowing = false;
                    alertPickOrTakePhoto.close();
                    alertNonDismissable.showMessage(getActivity());
                    sendPhoto();
                }
            }
        } else if (requestCode == SELECT_FILE) {
            if (resultCode == RESULT_OK) {
                selectedImageUri = data.getData();
                Log.d("asd","profile frag onActivityREsult");
                //pathAvatar = selectedImageUri.getPath();
                try {
                    Log.d("asd","profile frag onActivityREsult TRY");
                    pathAvatar = ImageUtils.decodeFile(ImageUtils.getRealPathFromURI(selectedImageUri,
                            getActivity()));
                    if (pathAvatar.endsWith("gif")){
                        showImageErrorDialog();
                    } else {
                        alertNonDismissable.showMessage(getActivity());
                        sendPhoto();
                    }

                } catch (IOException e) {
                    Log.d("asd","profile frag onActivityREsult CATCH");
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    showImageErrorDialog();
                }
                alertPhotoShowing = false;
                alertPickOrTakePhoto.close();

            }
        }
    }

    @Override
    public void onResponseUpdateUserRequest(JSONObject userRegister) {
        userPreferences.setUserAvatar(pathAvatar);
        if (mListener != null) mListener.onUpdateAvatar();
        userAvatarView.setImageURI(selectedImageUri);
        if (alertNonDismissable != null && getActivity() != null && isAdded()) {
            alertNonDismissable.dismissSafely();
        }
    }

    @Override
    public void onFailureUpdateUserRequest(Object error) {
        if (alertNonDismissable != null && getActivity() != null && isAdded()) {
            alertNonDismissable.dismissSafely();
        }
        showError(error);
    }

    @Override
    public void onOkAlertMessage(AlertMessage alertMessage, String type) {
        alertMessage.alert.cancel();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (group == languageRadioGroup) {
            if(languageRadioGroup.getCheckedRadioButtonId() == R.id.catalan){
                setLocale("ca");
                userPreferences.setUserLanguage(UserRegister.CAT);
            } else {
                setLocale("es");
                userPreferences.setUserLanguage(UserRegister.ESP);
            }
        } else if (group == autodownloadRadioGroup) {
            userPreferences.setIsAutodownload(checkedId == R.id.auto_download_yes);
        } else if (group == syncCalendarRadioGroup) {
            onSyncCalendarChanged(checkedId == R.id.sync_calendar_yes
                    && !repeatingCalendarPermissionsRequest);
        } else if (group == textSizeRadioGroup) {
            if (textSizeRadioGroup.getCheckedRadioButtonId() == R.id.small) {
                userPreferences.setFontSize(UserPreferences.FONT_SIZE_SMALL);
            } else if (textSizeRadioGroup.getCheckedRadioButtonId() == R.id.big) {
                userPreferences.setFontSize(UserPreferences.FONT_SIZE_BIG);
            } else {
                Log.d("fntsz","size medium");
                userPreferences.setFontSize(UserPreferences.FONT_SIZE_MEDIUM);
            }
            Log.d("fntsz","recreate act");
            if (getActivity() != null) getActivity().recreate();
        } else if (group == copyPhotosGalleryRadioGroup) {
            userPreferences.setIsCopyPhotos(checkedId == R.id.copy_photos_gallery_yes);
        }
    }

    private void onSyncCalendarChanged(boolean sync) {
        if (!disableSyncClick) {
            disableSyncClick = true;
            userPreferences.setIsSyncCalendars(sync);
            if (sync) {
                if (CalendarSyncManager.hasCalendarPermissions(getContext())) {
                    doCalendarSync();
                } else {
                    requestPermissions( new String[]{Manifest.permission.WRITE_CALENDAR,
                                    Manifest.permission.READ_CALENDAR},
                            CALENDAR_PERMISSION_REQUEST);
                }
            } else {
                startCalendarSyncAlert();
                repeatingCalendarPermissionsRequest = false;
                CalendarSyncManager calendarSyncManager = new CalendarSyncManager();
                calendarSyncManager.deleteCalendar();
            }
        } else {
            syncCalendarRadioGroup.setOnCheckedChangeListener(null);
            syncCalendarRadioGroup.check(sync ? R.id.sync_calendar_no : R.id.sync_calendar_yes);
        }

    }

    private void startCalendarSyncAlert() {
        alertWaitCalendarSync.showMessage(getActivity());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                disableSyncClick = false;
                syncCalendarRadioGroup.setOnCheckedChangeListener(ProfileFragment.this);
                if (alertWaitCalendarSync != null) alertWaitCalendarSync.dismissSafely();
            }
        }, 1200);
    }

    private void doCalendarSync() {
        startCalendarSyncAlert();
        new Thread(new Runnable() {
            @Override
            public void run() {
                userPreferences.setIsSyncCalendars(true);
                CalendarSyncManager calendarSyncManager = new CalendarSyncManager();
                Long calendarId = calendarSyncManager.addCalendar();
                userPreferences.setCalendarId(calendarId);
                calendarSyncManager.addAllMeetings(calendarId, new MeetingsDb(
                        MyApplication.getAppContext()).findAllShownMeetings());
            }
        }).start();

    }

    public void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = locale;
        res.updateConfiguration(conf, dm);
        if (getActivity() != null) getActivity().recreate();
    }

    @Override
    public void onClosed() {
        alertPhotoShowing = false;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
        void onUpdateAvatar();
        void onProfileEditClicked();
    }

    public void showError(Object error) {
        ErrorHandler errorHandler = new ErrorHandler();
        alertMessage = new AlertMessage(this, AlertMessage.TITTLE_ERROR);
        String errorMsg = errorHandler.getErrorByCode(getContext(), error);
        alertMessage.showMessage(getActivity(),errorMsg, "");
    }

    public boolean isShowingImageErrorDialog() {
        return isImageErrorDialog;
    }

    public void showImageErrorDialog() {
        isImageErrorDialog = true;
        AlertMessage alertMessage = new AlertMessage(new AlertMessage.AlertMessageInterface() {
            @Override
            public void onOkAlertMessage(AlertMessage alertMessage, String type) {
                alertMessage.dismissSafely();
                isImageErrorDialog = false;
            }
        }, AlertMessage.TITTLE_ERROR);
        alertMessage.setDismissMessageInterface(new AlertMessage.DismissMessageInterface() {
            @Override
            public void onDismissAlertMessage() {
                isImageErrorDialog = false;
            }
        });
        String errorMsg = getString(R.string.error_opening_image);
        alertMessage.showMessage(getActivity(),errorMsg, "");
    }


}
