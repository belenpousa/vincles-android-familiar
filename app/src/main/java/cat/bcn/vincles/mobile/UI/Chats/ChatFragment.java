package cat.bcn.vincles.mobile.UI.Chats;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Model.GetUser;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Client.Requests.BaseRequest;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.UI.Alert.AlertMessage;
import cat.bcn.vincles.mobile.UI.Alert.AlertNonDismissable;
import cat.bcn.vincles.mobile.UI.Alert.AlertRetry;
import cat.bcn.vincles.mobile.UI.Calls.CallsActivity;
import cat.bcn.vincles.mobile.UI.Calls.CallsActivityView;
import cat.bcn.vincles.mobile.UI.Chats.Model.ChatElement;
import cat.bcn.vincles.mobile.UI.Common.BaseFragment;
import cat.bcn.vincles.mobile.UI.Compound.ActionCompoundView;
import cat.bcn.vincles.mobile.UI.Contacts.Contact;
import cat.bcn.vincles.mobile.UI.Gallery.ZoomContentActivity;
import cat.bcn.vincles.mobile.Utils.ImageUtils;
import cat.bcn.vincles.mobile.Utils.OtherUtils;

import static android.app.Activity.RESULT_OK;
import static cat.bcn.vincles.mobile.UI.Chats.ChatPresenter.MEDIA_PHOTO;
import static cat.bcn.vincles.mobile.UI.Chats.ChatPresenter.MEDIA_VIDEO;

public class ChatFragment extends BaseFragment implements ChatFragmentView, View.OnClickListener, ChatAdapter.ChatAdapterListener, AlertRetry.AlertSaveImageInGalleryInterface {

    private static final int NORMAL_BOTTOM_BAR = 0;
    private static final int TEXT_BOTTOM_BAR = 1;
    private static final int AUDIO_BOTTOM_BAR = 2;

    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_SEND_VIDEO = 0;
    private static final int MY_PERMISSIONS_REQUEST_AUDIO_RECORDING = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_SEND_FILE = 2;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA_PHOTO = 3;

    public static final String SHARE_MEDIA_IDS = "share_media_ids";
    public static final String SHARE_MEDIA_PATHS = "share_media_paths";
    public static final String SHARE_MEDIA_METADATAS = "share_media_metadatas";

    public static final String AUDIO_RECORDER_FRAGMENT_TAG = "audio_recorder_fragment_tag";
    public static final String REPOSITORY_FRAGMENT_TAG = "repository_fragment_tag";

    private static final int REQUEST_IMAGE_OR_VIDEO = 754;


    ChatPresenter presenter;
    View rootView;
    ViewGroup bottomBar;
    EditText messageET;
    ActionCompoundView actionCompoundView;
    ChatAdapter chatAdapter;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private String idChat;
    private boolean isGroupChat;
    private boolean isDynamizer;
    private boolean isImageErrorDialog = false;
    private String newMediaFile;

    AlertRetry alertRetry;
    AlertNonDismissable alertNonDismissable;
    private OnFragmentInteractionListener mListener;

    Bundle savedInstanceState;

    ProgressBar audioProgressbar;
    TextView audioTime;

    private int bottomBarState;

    public ChatFragment() {
        // Required empty public constructor
    }

    public static ChatFragment newInstance(FragmentResumed listener, String idUserSender,
                                           boolean isGroupChat, boolean isDynamizer) {
        ChatFragment fragment = new ChatFragment();
        fragment.setListener(listener, FragmentResumed.FRAGMENT_CONTACTS);
        Bundle arguments = new Bundle();
        if (isDynamizer) isGroupChat = true;
        arguments.putString("idChat", idUserSender);
        arguments.putBoolean("isGroupChat", isGroupChat);
        arguments.putBoolean("isDynamizer", isDynamizer);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getContext() != null)
            OtherUtils.sendAnalyticsView(getContext(),
                    getResources().getString(R.string.tracking_chat));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        alertNonDismissable = new AlertNonDismissable(getResources().getString(R.string.login_sending_data), true);
        idChat = getArguments().getString("idChat");
        isGroupChat = getArguments().getBoolean("isGroupChat");
        isDynamizer = getArguments().getBoolean("isDynamizer");
        if(savedInstanceState != null ) isImageErrorDialog = savedInstanceState.getBoolean("isImageErrorDialog");
        presenter = new ChatPresenter((BaseRequest.RenewTokenFailed) getActivity(),this,
                savedInstanceState, idChat, new UserPreferences(getContext()), isGroupChat, isDynamizer);
        this.savedInstanceState = savedInstanceState;

        ChatRepository chatRepository;
        Fragment repo = getFragmentManager().findFragmentByTag(REPOSITORY_FRAGMENT_TAG);
        if (repo instanceof ChatRepository) {
            chatRepository = (ChatRepository) repo;
        } else {
            chatRepository = null;
            getFragmentManager().beginTransaction().remove(repo);
        }

        if (chatRepository != null && savedInstanceState == null) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.remove(chatRepository);
            chatRepository = null;
        }
        if (chatRepository == null) {
            chatRepository = ChatRepository.newInstance(presenter,
                    (BaseRequest.RenewTokenFailed) getActivity(), new UserPreferences().getUserID(),
                    Integer.parseInt(idChat), isGroupChat, isDynamizer);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(chatRepository, REPOSITORY_FRAGMENT_TAG).commit();
        } else {
            chatRepository.setListeners(presenter, (BaseRequest.RenewTokenFailed) getActivity());
        }
        presenter.setChatRepository(chatRepository);

        ChatAudioRecorderFragment audioRecorderFragment = (ChatAudioRecorderFragment)
                getFragmentManager().findFragmentByTag(AUDIO_RECORDER_FRAGMENT_TAG);
        if (audioRecorderFragment == null) {
            audioRecorderFragment = new ChatAudioRecorderFragment();
            audioRecorderFragment.setPresenterAudio(presenter, this);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(audioRecorderFragment, AUDIO_RECORDER_FRAGMENT_TAG).commitAllowingStateLoss();
            presenter.setAudioRecorderFragment(audioRecorderFragment);
        } else {
            audioRecorderFragment.setPresenterAudio(presenter, this);
            presenter.setAudioRecorderFragment(audioRecorderFragment);
        }
    }

    @Override
    public void onDetach() {
      super.onDetach();
      chatAdapter.stopPlayingAudio();
    }

  @Override
    public void onDestroy() {
      super.onDestroy();
      chatAdapter.stopPlayingAudio();
    }

    @Override
    public void onStop() {
        super.onStop();
        InputMethodManager inputMethodManager =
                (InputMethodManager) getActivity().getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                getActivity().getCurrentFocus().getWindowToken(), 0);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        presenter.onSaveInstanceState(outState);
        outState.putBoolean("isImageErrorDialog", isShowingImageErrorDialog());
        if (chatAdapter != null) chatAdapter.onSaveInstanceState(outState);
        outState.putString("newMediaFile", this.newMediaFile);

    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_chat, container, false);

        initRecyclerView();

        bottomBar = rootView.findViewById(R.id.bottom_bar);
        actionCompoundView = rootView.findViewById(R.id.action);
        if (actionCompoundView != null) actionCompoundView.setOnClickListener(this);
        View backButton = rootView.findViewById(R.id.back);
        if (backButton != null) backButton.setOnClickListener(this);
        if (isGroupChat) {
            rootView.findViewById(R.id.avatar).setOnClickListener(this);
            rootView.findViewById(R.id.chat_title).setOnClickListener(this);
        }
        if(isShowingImageErrorDialog()) {
            showImageErrorDialog();
        }
        presenter.onCreateView();
        presenter.loadData();

        if (savedInstanceState != null){
            this.newMediaFile = savedInstanceState.getString("newMediaFile");
        }

        return rootView;
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

    private void initRecyclerView() {
        recyclerView = rootView.findViewById(R.id.recyclerView);

        mLayoutManager = new LinearLayoutManager(getContext());
        ((LinearLayoutManager)mLayoutManager).setReverseLayout(true);
        //((LinearLayoutManager)mLayoutManager).setStackFromEnd(true);

        recyclerView.setLayoutManager(mLayoutManager);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (chatAdapter != null && newState == RecyclerView.SCROLL_STATE_IDLE
                        && ((LinearLayoutManager) mLayoutManager)
                        .findLastCompletelyVisibleItemPosition() == chatAdapter.getItemCount()-1) {
                    presenter.onScrolledToTop();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_SEND_VIDEO && grantResults.length > 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            OtherUtils.sendVideoIntent(this);
        } else if (requestCode == MY_PERMISSIONS_REQUEST_AUDIO_RECORDING && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            presenter.onClickAudio();
        } else if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_SEND_FILE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            presenter.onClickFileShare();
        } else if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA_PHOTO && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            newMediaFile = OtherUtils.sendPhotoIntent(this);
        }

    }

    @Override
    public boolean isLanguageCatalan() {
        Locale current = getResources().getConfiguration().locale;
        return current.getLanguage().contains("ca");
    }

    @Override
    public void showMessages(final List<ChatElement> elementsList, final SparseArray<Contact> users) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (chatAdapter == null || recyclerView.getAdapter() == null) {
                        Log.d("qwer","chatfrag users size:"+((users==null) ? "0" : users.size()));
                        chatAdapter = new ChatAdapter(getContext(), elementsList,
                                ChatFragment.this, users, savedInstanceState, presenter);
                        recyclerView.setAdapter(chatAdapter);
                    } else {
                        chatAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }

    @Override
    public void reloadMessagesAdapter() {
        if (getActivity() != null && chatAdapter != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    chatAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public void setChatInfo(String name, String photo) {
        if (name != null) {
            TextView title = rootView.findViewById(R.id.chat_title);
            title.setText(name);
        }

        if (photo != null && photo.length()>0 && !photo.equals("placeholder")) {
            ImageView avatar = rootView.findViewById(R.id.avatar);
            Glide.with(avatar.getContext())
                    .load(new File(photo))
                    .apply(new RequestOptions().overrideOf(128, 128)
                            .centerCrop().diskCacheStrategy(DiskCacheStrategy.ALL))
                    .into(avatar);
        }
    }

    @Override
    public void setChatDynamizer(int id, String photo) {
        if (photo != null && photo.length()>0 && !photo.equals("placeholder")) {
            actionCompoundView.setImagePath(photo);
        }
    }

    @Override
    public void showLoadingMessages() {

    }

    @Override
    public void hideLoadingMessages() {

    }

    @Override
    public void showWaitDialog() {
        alertNonDismissable.showMessage(getActivity());
    }

    @Override
    public boolean isShowingImageErrorDialog() {
        return isImageErrorDialog;
    }

    @Override
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

    @Override
    public boolean isShowingWaitDialog() {
        return alertNonDismissable != null && alertNonDismissable.alert != null && alertNonDismissable.alert.isShowing();
    }

    @Override
    public void hideWaitDialog() {
        if (alertNonDismissable != null && getActivity() != null && isAdded()) {
            alertNonDismissable.dismissSafely();
        }
    }

    @Override
    public void showRetryDialog() {
        alertRetry = new AlertRetry(getActivity(), this);
        alertRetry.showMessage(getString(R.string.chat_send_message_error));
    }

    @Override
    public void hideSendAgainDialog() {
        if (alertRetry != null) alertRetry.dismissSafely();
    }

    @Override
    public void onRetryAccept(AlertRetry alertRetry) {
        presenter.retrySendMessage();
    }

    @Override
    public void onRetryCancel(AlertRetry alertRetry) {
        presenter.cancelRetrySendMessage();
    }

    @Override
    public void showBottomBar() {
        bottomBarState = NORMAL_BOTTOM_BAR;

        if (messageET != null && getContext() != null) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(messageET.getWindowToken(), 0);
            }
        }
        bottomBar.removeAllViews();
        try {
          LayoutInflater inflater = LayoutInflater.from(getContext());
          inflater.inflate(R.layout.fragment_chat_bottom_bar, bottomBar);
        } catch (Exception e) {
          System.out.println("Error " + e.getMessage());
        }

        View textButton = bottomBar.findViewById(R.id.text);
        View cameraButton = bottomBar.findViewById(R.id.camera);
        View videoButton = bottomBar.findViewById(R.id.video);
        View audioButton = bottomBar.findViewById(R.id.audio);
        View fileButton = bottomBar.findViewById(R.id.file);

        if (textButton != null) textButton.setOnClickListener(this);
        if (cameraButton != null) cameraButton.setOnClickListener(this);
        if (videoButton != null) videoButton.setOnClickListener(this);
        if (audioButton != null) audioButton.setOnClickListener(this);
        if (fileButton != null) fileButton.setOnClickListener(this);
    }

    @Override
    public void showWritingBottomBar() {
        bottomBarState = TEXT_BOTTOM_BAR;

        bottomBar.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.fragment_chat_write_bottom_bar, bottomBar);

        View cancelButton = bottomBar.findViewById(R.id.cancel);
        messageET = bottomBar.findViewById(R.id.message_et);
        View sendButton = bottomBar.findViewById(R.id.send);
        messageET.requestFocus();
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(messageET, InputMethodManager.SHOW_IMPLICIT);

        if (cancelButton != null) cancelButton.setOnClickListener(this);
        if (sendButton != null) sendButton.setOnClickListener(this);
    }

    @Override
    public void showAudioBottomBar() {
        bottomBarState = AUDIO_BOTTOM_BAR;

        bottomBar.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.fragment_chat_audio_bottom_bar, bottomBar);

        View cancelButton = bottomBar.findViewById(R.id.cancel);
        View sendButton = bottomBar.findViewById(R.id.send_audio);
        audioProgressbar = bottomBar.findViewById(R.id.progressbar);
        audioTime = bottomBar.findViewById(R.id.time);
        audioProgressbar.setMax(60*1000);
        if (cancelButton != null) cancelButton.setOnClickListener(this);
        if (sendButton != null) sendButton.setOnClickListener(this);
    }

    @Override
    public void setAudioProgress(int progress, String time) {
        if (audioProgressbar != null) {
            audioProgressbar.setProgress(progress);
        }
        if (audioTime != null) {
            audioTime.setText(time);
        }
    }

    @Override
    public void setAction(Drawable drawable) {
        if (isGroupChat) {
            if (isDynamizer) actionCompoundView.setVisibility(View.GONE);
            else {
                actionCompoundView.setText(getResources().getString(R.string.chat_button_dinamizer));
                if (drawable == null) {
                    actionCompoundView.setImageDrawable(getResources().getDrawable(R.drawable.user));
                } else {
                    actionCompoundView.setImageDrawable(drawable);
                }
            }
        } else {
            actionCompoundView.setText(getString(R.string.chat_button_call));
        }
    }

    public void onExitScreen() {
        Log.d("groupwtch","onBackPressed");
        if (mListener != null)
            if (isGroupChat) mListener.onSetGroupMessagesAsWatched(idChat);
            else mListener.onSetUserMessagesAsWatched(presenter.getUnwatchedReceivedMessages());

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                OtherUtils.hideKeyboard(getActivity());
                Log.d("groupwtch","back click");
                onExitScreen();
                getFragmentManager().popBackStack();
                break;
            case R.id.text:
                presenter.onClickText();
                break;
            case R.id.camera:
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    newMediaFile = OtherUtils.sendPhotoIntent(this);
                } else {
                    requestPermissions(new String[]{Manifest.permission.CAMERA},
                            MY_PERMISSIONS_REQUEST_CAMERA_PHOTO);
                }
                break;
            case R.id.video:
                if (ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(getActivity(),
                                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    OtherUtils.sendVideoIntent(this);
                } else {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.CAMERA},
                            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_SEND_VIDEO);
                }
                break;
            case R.id.audio:
                if (ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.RECORD_AUDIO)
                        == PackageManager.PERMISSION_GRANTED) {
                    presenter.onClickAudio();
                } else {
                    requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},
                            MY_PERMISSIONS_REQUEST_AUDIO_RECORDING);
                }
                break;
            case R.id.file:
                if (ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    presenter.onClickFileShare();
                } else {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_SEND_FILE);
                }
                break;
            case R.id.cancel:
                presenter.onClickCancelMessage();
                break;
            case R.id.send:
                presenter.onClickSendMessage(String.valueOf(messageET.getText()));
                break;
            case R.id.send_audio:
                presenter.onClickSendAudio();
                break;
            case R.id.action:
                if (isGroupChat) {
                    int chatId = presenter.getDynamizerChatId();
                    if (mListener != null && chatId != -1) {
                        mListener.onContactSelected(String.valueOf(chatId), false, true);
                    }
                } else {
                    GetUser otherUser = presenter.getOtherUserInfoIfNotGroup();
                    if (otherUser != null) {
                        Intent intent = new Intent(getContext(), CallsActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putInt(CallsActivity.EXTRAS_CALL_MODE, CallsActivityView.MAKING_CALL);
                        bundle.putInt(CallsActivity.EXTRAS_USER_ID, otherUser.getId());
                        bundle.putString(CallsActivity.EXTRAS_USER_NAME, otherUser.getName());
                        bundle.putString(CallsActivity.EXTRAS_USER_LASTNAME, otherUser.getLastname());
                        bundle.putBoolean(CallsActivity.EXTRAS_USER_IS_VINCLES,
                                otherUser.getIdCircle() != null && otherUser.getIdCircle() != -1);
                        bundle.putString(CallsActivity.EXTRAS_USER_AVATAR_PATH, otherUser.getPhoto());
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                }
                break;
            case R.id.avatar:
            case R.id.chat_title:
                if (isGroupChat) {
                    if(!isDynamizer) {
                        mListener.onGroupTitleClicked(Integer.parseInt(idChat));
                    }
                }
                break;

        }

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("vidimg","activity result");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OtherUtils.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            onSaveMedia(MEDIA_PHOTO);

        } else if (requestCode == OtherUtils.REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Uri _uri = data.getData();
            if (_uri != null && "content".equals(_uri.getScheme())) {
                Cursor cursor = getActivity().getContentResolver().query(_uri, new String[] { android.provider.MediaStore.Images.ImageColumns.DATA }, null, null, null);
                cursor.moveToFirst();
                newMediaFile = Uri.parse(cursor.getString(0)).getPath();
                cursor.close();
            } else {
                newMediaFile = _uri.getPath();
            }

            //check if it is not too big
            if (OtherUtils.isFileTooBigForServer(newMediaFile)) {
                Toast.makeText(getContext(), getResources().getString(R.string.file_too_big), Toast.LENGTH_LONG).show();
                return;
            }

            onSaveMedia(MEDIA_VIDEO);
            //picture = data.getData();

        } else if (requestCode == REQUEST_IMAGE_OR_VIDEO && resultCode == RESULT_OK) {
            Log.d("vidimg","activity result ok img or vid");
            Uri uri = data.getData();
            //String mimeType = getActivity().getContentResolver().getType(uri);
            String mimeType = "image/jpeg";
            if (uri.toString().contains("video")) {
                mimeType = "video/mp4";
            }
            String path;
            try {
                path = ImageUtils.decodeFile(ImageUtils.getRealPathFromURI(uri,
                        getActivity()));
                presenter.onSendSystemFile(path, mimeType);
            } catch (IOException e) {
                Log.d("vidimg", "activity result catch:" + e);
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                showImageErrorDialog();
            }
            /*if (uri.toString().contains("image")) {
                metadata =
            } else  if (uri.toString().contains("video")) {
                //handle video
            }*/
        }
    }

    private void onSaveMedia(int type) {
        presenter.onSaveMediaFile(newMediaFile, type);
    }


    @Override
    public void onChatElementMediaClicked(String path, String mimeType) {
        if (mimeType.toLowerCase().contains("audio")) {

        } else {
            Intent intent = new Intent(getContext(), ZoomContentActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("filePath", path);
            bundle.putString("mimeType", mimeType);
            intent.putExtras(bundle);
            startActivity(intent);
        }

    }

    @Override
    protected void processPendingChanges(Bundle bundle){
        Log.d("notman2","chat processPendingChanges");
        int changesType = bundle.getInt(BaseFragment.CHANGES_TYPE);
        if (changesType == BaseFragment.CHANGES_SHARE_MEDIA) {
            ArrayList<Integer> ids = bundle.getIntegerArrayList(SHARE_MEDIA_IDS);
            ArrayList<String> paths = bundle.getStringArrayList(SHARE_MEDIA_PATHS);
            ArrayList<String> metadatas = bundle.getStringArrayList(SHARE_MEDIA_METADATAS);
            presenter.sendFileMessage(OtherUtils.convertIntegers(ids), paths, metadatas, false);
        } else if (changesType == BaseFragment.CHANGES_NEW_MESSAGE
                || changesType == BaseFragment.CHANGES_NEW_GROUP_MESSAGE) {
            Log.d("notman2","chat processPendingChanges new message id:"+bundle.getLong("chatId")+", idChat:"+idChat);
            if (bundle.getLong("chatId") == Long.parseLong(idChat)) {
                Log.d("notman2","chat processPendingChanges new message reload messages");
                presenter.onNewMessageReceived(bundle.getLong("messageId"));
            }
        }
        pendingChangeProcessed();
    }

    @Override
    public void launchGalleryInSelectMode() {
        if (mListener != null) {
            mListener.onShareFileClicked();
        }
    }

    @Override
    public void launchPickVideoOrPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //intent.setType("image/* video/*"); //any video or image
        intent.setType("image/jpeg video/mp4");
        startActivityForResult(intent, REQUEST_IMAGE_OR_VIDEO);
    }

    public void onLogout() {
        if (presenter != null) presenter.onLogout();
    }

    /**
     *
     * Methods for showing help guide
     *
     */
    @Override
    protected boolean shouldShowMenu() {
        return true;
    }

    @Override
    protected String getTextForPage(int page) {
        switch (bottomBarState) {
            case NORMAL_BOTTOM_BAR:
                switch (page) {
                    case 0:
                        return getString(R.string.context_help_chat_text);
                    case 1:
                        return getString(R.string.context_help_chat_photo);
                    case 2:
                        return getString(R.string.context_help_chat_video);
                    case 3:
                        return getString(R.string.context_help_chat_audio);
                    case 4:
                        return getString(R.string.context_help_chat_gallery);
                    case 5:
                        if ( this.isGroupChat ){
                            return getString(R.string.context_help_chat_profile);
                        }else{
                            return getString(R.string.context_help_chat_call);
                        }
                    default:
                        break;
                }
            case TEXT_BOTTOM_BAR:
                switch (page) {
                    case 0:
                        return getString(R.string.context_help_chat_send_text);
                    default:
                        break;
                }
            case AUDIO_BOTTOM_BAR:
                switch (page) {
                    case 0:
                        return getString(R.string.context_help_chat_record);
                    default:
                        break;
                }
        }
        return null;
    }

    @Override
    protected View getViewForPage(int page) {

        switch (bottomBarState) {
            case NORMAL_BOTTOM_BAR:
                switch (page) {
                    case 0:
                        return rootView.findViewById(R.id.text);
                    case 1:
                        return rootView.findViewById(R.id.camera);
                    case 2:
                        return rootView.findViewById(R.id.video);
                    case 3:
                        return rootView.findViewById(R.id.audio);
                    case 4:
                        return rootView.findViewById(R.id.file);
                    case 5:
                        return rootView.findViewById(R.id.action);
                    default:
                        break;
                }
            case TEXT_BOTTOM_BAR:
                switch (page) {
                    case 0:
                        return rootView.findViewById(R.id.bottom_bar);
                    default:
                        break;
                }
            case AUDIO_BOTTOM_BAR:
                switch (page) {
                    case 0:
                        return rootView.findViewById(R.id.bottom_bar);
                    default:
                        break;
                }
        }
        return null;
    }

    public interface OnFragmentInteractionListener {
        void onShareFileClicked();
        void onSetUserMessagesAsWatched(ArrayList<Long> ids);
        void onSetGroupMessagesAsWatched(String idChat);
        void onGroupTitleClicked(int chatId);
        void onContactSelected(String idUserSender, boolean isGroupChat, boolean isDynamizer);
    }
}
