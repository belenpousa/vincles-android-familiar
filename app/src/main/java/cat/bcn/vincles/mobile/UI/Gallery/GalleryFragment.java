package cat.bcn.vincles.mobile.UI.Gallery;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daasuu.bl.BubbleLayout;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Db.GalleryDb;
import cat.bcn.vincles.mobile.Client.Db.Model.GalleryContentRealm;
import cat.bcn.vincles.mobile.Client.Db.UsersDb;
import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Client.Requests.BaseRequest;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.UI.Alert.AlertConfirmOrCancel;
import cat.bcn.vincles.mobile.UI.Alert.AlertMessage;
import cat.bcn.vincles.mobile.UI.Alert.AlertNonDismissable;
import cat.bcn.vincles.mobile.UI.Alert.AlertRetry;
import cat.bcn.vincles.mobile.UI.Common.BaseFragment;
import cat.bcn.vincles.mobile.Utils.ImageUtils;
import cat.bcn.vincles.mobile.Utils.OtherUtils;
import io.realm.RealmResults;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static cat.bcn.vincles.mobile.Utils.OtherUtils.REQUEST_IMAGE_CAPTURE;
import static cat.bcn.vincles.mobile.Utils.OtherUtils.REQUEST_VIDEO_CAPTURE;

public class GalleryFragment extends BaseFragment implements GalleryView, View.OnClickListener, AlertMessage.AlertMessageInterface,
        GalleryAdapter.OnItemClicked, AlertConfirmOrCancel.AlertConfirmOrCancelInterface, AlertRetry.AlertSaveImageInGalleryInterface,
        GalleryAdapter.GalleryAdapterListener {


    private static final int STATE_DEFAULT = 0;
    private static final int STATE_ASKING_DELETE = 1;
    private static final int STATE_DELETING = 2;
    private static final int STATE_SHOWING_RESULT_OK = 3;
    private static final int STATE_SHOWING_RESULT_PARTIALLY_OK = 4;
    private static final int STATE_SHOWING_RESULT_NOT_OK = 5;


    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_SEND_VIDEO = 0;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA_PHOTO = 3;
    private static final int CALL_PERMISSIONS_REQUEST = 0;

    private OnFragmentInteractionListener mListener;

    AlertMessage alertMessage;
    private GalleryAdapter adapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.ItemDecoration itemDecoration;
    GalleryDb galleryDb;
    UsersDb usersDb;
    View v;
    ViewGroup bottomBar;
    UserPreferences userPreferences;
    AlertRetry alertRetry;
    PopupWindow mPopupWindow;
    boolean shouldShowFilter = false;
    View filterBtn;
    GallerypPresenter gallerypPresenter;
    View popWindowMenu;
    Object picture;
    boolean savingIsPhoto, isSavingFile = false, isPictureUri = false;
    AlertNonDismissable alertNonDismissable;
    int state = STATE_DEFAULT;
    View shareButton;
    View cancelButton;
    View cameraButton;
    View videoButton;
    View deleteButton;
    String pathPhotoTaken;
    private RecyclerView recyclerView;
    private boolean hasZeroFiles;
    private boolean sharedMedia;
    private int index;
    private GalleryContentRealm galleryContentRealm;
    private boolean selectMode;
    private boolean fromChat;

    private boolean viewCreated = false;

    public GalleryFragment() {
        // Required empty public constructor
    }

    public static GalleryFragment newInstance(FragmentResumed listener, boolean selectMode) {
        GalleryFragment fragment = new GalleryFragment();
        fragment.setListener(listener, FragmentResumed.FRAGMENT_GALLERY);
        Bundle arguments = new Bundle();
        arguments.putBoolean("selectMode", selectMode);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getContext() != null)
            OtherUtils.sendAnalyticsView(getContext(),
                    getResources().getString(R.string.tracking_gallery));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("filtfa","onCreate frag");
        super.onCreate(savedInstanceState);
        alertNonDismissable = new AlertNonDismissable(getResources().getString(R.string.login_sending_data), true);
        ArrayList<Integer> itemsSelected = null;
        selectMode = getArguments().getBoolean("selectMode");
        index = -1;
        boolean isInSelectionMode = false;
        if (savedInstanceState != null) {
            itemsSelected = savedInstanceState.getIntegerArrayList("itemsSelected");
            isInSelectionMode = savedInstanceState.getBoolean("isInSelectionMode", itemsSelected.size() != 0);
            isSavingFile = savedInstanceState.getBoolean("isSavingFile");
            sharedMedia = savedInstanceState.getBoolean("sharedMedia");
            if (isSavingFile) {
                savingIsPhoto = savedInstanceState.getBoolean("savingIsPhoto");
                isPictureUri = savedInstanceState.getBoolean("isPictureUri");
                if (isPictureUri) picture = savedInstanceState.getParcelable("picture");
                else picture = savedInstanceState.getString("picture");
            }
        }
        loadContent(itemsSelected, savedInstanceState, isInSelectionMode);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("pathPhotoTaken",this.pathPhotoTaken);
        outState.putIntegerArrayList("itemsSelected", gallerypPresenter.getItemsSelected());
        outState.putBoolean("isInSelectionMode", gallerypPresenter.isInSelectionMode());
        outState.putString("filterKind", gallerypPresenter.getFilterKind());
        outState.putBoolean("showingFilter", mPopupWindow != null && mPopupWindow.isShowing());
        outState.putBoolean("isSavingFile", isSavingFile);
        outState.putBoolean("sharedMedia", sharedMedia);
        if (isSavingFile) {
            outState.putBoolean("savingIsPhoto", savingIsPhoto);
            outState.putBoolean("isPictureUri", isPictureUri);
            if (isPictureUri) outState.putParcelable("picture", (Uri) picture);
            else outState.putString("picture", (String) picture);
        }
        Log.d("filtfa","onSaveInstanceState filt:"+gallerypPresenter.getFilterKind());
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("filtfa","onResume frag");
        if (recyclerView != null) {
            Log.d("filtfa","onResume filtKind:"+gallerypPresenter.getFilterKind());
            showGalleryContent(gallerypPresenter.getFilteredMedia(gallerypPresenter.getFilterKind()));
            if (selectMode) {
                onUpdateIsInSelectionMode();
            } else {
                if (sharedMedia) {
                    sharedMedia = false;
                    gallerypPresenter.setInSelectionMode(false);
                    adapter.emptyItemSelecteds();
                    onUpdateIsInSelectionMode();
                }/* else if (index != -1) {
                    onViewItem(galleryContentRealm, index);
                    index = -1;
                }*/
            }
        }
    }

    public void loadContent (ArrayList<Integer> itemsSelected, Bundle savedInstanceState, boolean isInSelectionMode) {
        String filterKind = null;
        if (savedInstanceState != null) {
            filterKind = savedInstanceState.getString("filterKind");
            shouldShowFilter = savedInstanceState.getBoolean("showingFilter");
        }
        Log.d("filtfa","frag loadContent intanceNiull?"+(savedInstanceState== null)+ " filt:"+filterKind);
        userPreferences = new UserPreferences(getContext());
        galleryDb = new GalleryDb(getContext());
        usersDb = new UsersDb(getContext());
        gallerypPresenter = new GallerypPresenter((BaseRequest.RenewTokenFailed) getActivity(), getContext(),this,userPreferences,
                galleryDb, usersDb, itemsSelected, filterKind, isInSelectionMode);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            alertNonDismissable.showMessage(getActivity(), getResources().getString(R.string.saving_file));
            //picture = data.getExtras().get("data");
            savingIsPhoto = true;
            isPictureUri = false;
            picture = pathPhotoTaken;
            onSavePhoto();

        } else if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            alertNonDismissable.showMessage(getActivity(), getResources().getString(R.string.saving_file));
            Log.d("vidrc","video result, data:"+data.getData().toString());
            Uri _uri = data.getData();
            if (_uri != null && "content".equals(_uri.getScheme())) {
                Cursor cursor = getActivity().getContentResolver().query(_uri, new String[] { android.provider.MediaStore.Images.ImageColumns.DATA }, null, null, null);
                cursor.moveToFirst();
                picture = Uri.parse(cursor.getString(0));
                cursor.close();
            } else {
                picture = _uri.getPath();
            }
            savingIsPhoto = false;

            if (OtherUtils.isFileTooBigForServer(picture.toString())) {
                alertNonDismissable.dismissSafely();
                Toast.makeText(getContext(), getResources().getString(R.string.file_too_big), Toast.LENGTH_LONG).show();
                return;
            }

            onSaveVideo();
            //picture = data.getData();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.fragment_gallery, container, false);
        View backBtn = v.findViewById(R.id.back);
        if (backBtn != null) backBtn.setOnClickListener(this);
        Log.d("bck","back is nul?"+(backBtn==null));

        bottomBar = v.findViewById(R.id.bottom_bar);
        setupBottomBar(inflater);

        if (shouldShowFilter) {
            shouldShowFilter = false;
            //Posting delayed so that views have been drawn. Should do viewTree observer but this is
            //easier, and gives it "an animation look" as it shows the filter later
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    showFilterPopup();
                }
            }, 300);
        }

        TextView titleLine = v.findViewById(R.id.title);
        if (gallerypPresenter.getFilterKind().equals(GallerypPresenter.FILTER_ALL_MY_FILES)) {
            titleLine.setText(getResources().getString(R.string.my_files));
        } else if (gallerypPresenter.getFilterKind().equals(GallerypPresenter.FILTER_RECIVED_FILES)) {
            titleLine.setText(getResources().getString(R.string.received_files));
        }

        if (isSavingFile) {
            showErrorSavingFile(null);
        }
        configureRecyclerView();
        if (savedInstanceState == null) {
            Log.d("filtfa","frag onCreateActivity, RESET FILTER");
            gallerypPresenter.filterMedia(gallerypPresenter.getFilterKind());
        } else  {
            this.pathPhotoTaken = savedInstanceState.getString("pathPhotoTaken");
        }

        gallerypPresenter.onCreateView();

        viewCreated = true;
        return v;
    }

    public void onButtonPressed(Uri uri) {
    }

    private void setupBottomBar(LayoutInflater inflater) {
        bottomBar.removeAllViews();

        inflater.inflate(gallerypPresenter.isInSelectionMode() || selectMode ?
                        R.layout.fragment_contacts_select_bottom_bar: R.layout.fragment_gallery_bottom_bar,
                bottomBar);

        shareButton = bottomBar.findViewById(R.id.share);
        cancelButton = bottomBar.findViewById(R.id.cancel);
        cameraButton = bottomBar.findViewById(R.id.camera);
        videoButton = bottomBar.findViewById(R.id.video);
        deleteButton = bottomBar.findViewById(R.id.delete);
        if (deleteButton == null) {
            deleteButton = bottomBar.findViewById(R.id.delete);
        }
        if (cancelButton == null) {
            cancelButton = bottomBar.findViewById(R.id.cancel);
        }
        TextView shareTextView = bottomBar.findViewById(R.id.share_tv);
        if (shareTextView != null) {
            if (gallerypPresenter.isInSelectionMode()) {
                shareTextView.setText(getResources().getString(R.string.gallery_button_share_selection));
            } else {
                shareTextView.setText(getResources().getString(R.string.gallery_button_share));
            }
        }
        if (selectMode) {
            deleteButton.setVisibility(View.GONE);
        }
        filterBtn = bottomBar.findViewById(R.id.filter);

        if (cameraButton != null) cameraButton.setOnClickListener(this);
        if (videoButton != null) videoButton.setOnClickListener(this);
        if (shareButton != null) shareButton.setOnClickListener(this);
        if (cancelButton != null) cancelButton.setOnClickListener(this);
        if (deleteButton != null) deleteButton.setOnClickListener(this);
        if (filterBtn != null) filterBtn.setOnClickListener(this);
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
    }

    /*@Override
    public void onPause() {
        super.onPause();
        if (mPopupWindow != null) {
            mPopupWindow.dismiss();
        }
    }*/

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPopupWindow != null) {
            mPopupWindow.dismiss();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_SEND_VIDEO && grantResults.length > 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            OtherUtils.sendVideoIntent(this);
        } else if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA_PHOTO && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pathPhotoTaken = OtherUtils.sendPhotoIntent(this);
        }
    }

    @Override
    public void onClick(View view) {
        Log.d("bck","gallery click");
        if (view.getId() == R.id.back) {
            Log.d("bck","back click");
            getFragmentManager().popBackStack();
        } else if (view.getId() == R.id.camera) {
            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                pathPhotoTaken = OtherUtils.sendPhotoIntent(this);
            } else {
                requestPermissions(new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA_PHOTO);
            }

        } else if (view.getId() == R.id.video) {
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

        } else if (view.getId() == R.id.filter) {
            /*filterBtn.setBackground(getResources().getDrawable(R.drawable.shape_all_corners_round_red));
            filterBtn.setImageResource(R.drawable.filter_hover);*/

            showFilterPopup();

        } else if (view.getId() == R.id.delete || view.getId() == R.id.delete) {
            if (gallerypPresenter.getItemsSelected() != null && gallerypPresenter.getItemsSelected().size() > 0) {
                showConfirmationRemoveContent();
            }
        } else if (view.getId() == R.id.cancel || view.getId() == R.id.cancel) {
            gallerypPresenter.setInSelectionMode(false);
            onUpdateIsInSelectionMode();
        } else if (view.getId() == R.id.see_all_files) {
            checkPopMenuItem(0);

            TextView titleLine = v.findViewById(R.id.title);
            titleLine.setText(getResources().getString(R.string.all_files));

            mPopupWindow.dismiss();
            gallerypPresenter.filterMedia(GallerypPresenter.FILTER_ALL_FILES);
        } else if (view.getId() == R.id.see_my_files) {
            checkPopMenuItem(1);

            TextView titleLine = v.findViewById(R.id.title);
            titleLine.setText(getResources().getString(R.string.my_files));

            mPopupWindow.dismiss();
            gallerypPresenter.filterMedia(GallerypPresenter.FILTER_ALL_MY_FILES);
        } else if (view.getId() == R.id.see_recived_files) {
            checkPopMenuItem(2);

            TextView titleLine = v.findViewById(R.id.title);
            titleLine.setText(getResources().getString(R.string.received_files));

            mPopupWindow.dismiss();
            gallerypPresenter.filterMedia(GallerypPresenter.FILTER_RECIVED_FILES);
        } else if (view.getId() == R.id.share) {
            if (!gallerypPresenter.isInSelectionMode()) {
                gallerypPresenter.setInSelectionMode(!gallerypPresenter.isInSelectionMode());
                TextView titleLine = v.findViewById(R.id.title);
                titleLine.setText(getResources().getString(R.string.gallery_share_title));
                onUpdateIsInSelectionMode();
            } else {
                if (gallerypPresenter.getItemsSelected() != null && gallerypPresenter.getItemsSelected().size() > 0) {
                    sharedMedia = true;
                    gallerypPresenter.onShareSelectionModeClicked();
                }
            }
        }
    }

    @Override
    public void onShareContentSelectionMode(ArrayList<Integer> itemIDs, ArrayList<String> paths,
                                            ArrayList<String> metadata) {
        Log.d("shre","onShareContentSelectionMode");
        mListener.onGalleryShareButtonClicked(gallerypPresenter.getItemsSelected(), paths, metadata,
                selectMode);
    }

    private void showFilterPopup() {
        if (popWindowMenu == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            popWindowMenu = inflater.inflate(R.layout.popupwindow_filter_gallery, null);
            mPopupWindow = new PopupWindow(
                    popWindowMenu,
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
            mPopupWindow.setFocusable(true);
            mPopupWindow.setOutsideTouchable(true);

            TextView seeAllFiles = popWindowMenu.findViewById(R.id.see_all_files);
            TextView seeMyFiles = popWindowMenu.findViewById(R.id.see_my_files);
            TextView seeRecivedFiles = popWindowMenu.findViewById(R.id.see_recived_files);

            seeAllFiles.setOnClickListener(this);
            seeMyFiles.setOnClickListener(this);
            seeRecivedFiles.setOnClickListener(this);

            if (gallerypPresenter.getFilterKind().equals(GallerypPresenter.FILTER_ALL_MY_FILES)) {
                checkPopMenuItem(1);
            } else if (gallerypPresenter.getFilterKind().equals(GallerypPresenter.FILTER_RECIVED_FILES)) {
                checkPopMenuItem(2);
            } else {
                checkPopMenuItem(0);
            }
        }
        if (filterBtn != null) {
            popWindowMenu.measure(RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            mPopupWindow.showAsDropDown(filterBtn,0,-popWindowMenu.getMeasuredHeight()-filterBtn.getHeight());
            BubbleLayout bubbleLayout = ((BubbleLayout)popWindowMenu.findViewById(R.id.bubblelayout));
            bubbleLayout.setArrowPosition(filterBtn.getWidth()/2-bubbleLayout.getArrowWidth()/2);
        }
    }

    public void showConfirmationRemoveContent() {
        state = STATE_ASKING_DELETE;

        if(gallerypPresenter.getItemsSelected().size() > 1){
            new AlertConfirmOrCancel(getActivity(),this).showMessage(getString(R.string.remove_items_info), "Eliminar", AlertConfirmOrCancel.BUTTONS_HORIZNTAL);
        }else{
            new AlertConfirmOrCancel(getActivity(),this).showMessage(getString(R.string.remove_item_info), "Eliminar", AlertConfirmOrCancel.BUTTONS_HORIZNTAL);
        }

    }

    @Override
    public void onAccept(AlertConfirmOrCancel alertConfirmOrCancel) {
        alertConfirmOrCancel.alert.dismiss();
        state = STATE_DELETING;
        alertNonDismissable.showMessage(getActivity(), getResources().getString(gallerypPresenter
                .getItemsSelected().size() > 1 ? R.string.deleting_files : R.string.deleting_file));
        gallerypPresenter.deleteSelectedContent();
    }

    @Override
    public void onCancel(AlertConfirmOrCancel alertConfirmOrCancel) {
        alertConfirmOrCancel.alert.dismiss();
        state = STATE_DEFAULT;
    }

    @Override
    public void onDeleteResults(int results) {
        if (alertNonDismissable != null && alertNonDismissable.alert.isShowing()) {
            alertNonDismissable.alert.dismiss();
        }
        int messageID = 0;
        switch (results) {
            case GalleryView.DELETE_OK:
                state = STATE_DEFAULT;
                adapter.notifyDataSetChanged();
                break;
            case GalleryView.DELETE_PARTIALLY_OK:
                state = STATE_SHOWING_RESULT_PARTIALLY_OK;
                messageID = R.string.deleting_files_partially_ok;
                break;
            case GalleryView.DELETE_NOT_OK:
                messageID = R.string.deleting_files_not_ok_retry;
                state = STATE_SHOWING_RESULT_NOT_OK;
                break;
        }
        if (messageID != 0) {
            new AlertConfirmOrCancel(getActivity(),this).showMessage(getString(messageID), "Eliminar", AlertConfirmOrCancel.BUTTONS_HORIZNTAL);
        }

    }


    private int getPopupTextViewID(int index) {
        switch (index) {
            case 0: default: return R.id.see_all_files;
            case 1: return R.id.see_my_files;
            case 2: return R.id.see_recived_files;
        }
    }
    private int getPopupImageViewID(int index) {
        switch (index) {
            case 0: default: return R.id.see_all_files_iv;
            case 1: return R.id.see_my_files_iv;
            case 2: return R.id.see_recived_files_iv;
        }
    }

    public void checkPopMenuItem (int index) {
        unCheckAllPopMenuItem();
        int textViewID = getPopupTextViewID(index);
        ((TextView)popWindowMenu.findViewById(textViewID))
                .setTextColor(getResources().getColor(R.color.colorPrimary));
        int imageViewID = getPopupImageViewID(index);
        popWindowMenu.findViewById(imageViewID).setVisibility(View.VISIBLE);
        //textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.check_filter, 0);
    }

    public void unCheckAllPopMenuItem () {
        for (int i = 0; i < 3; i++) {
            TextView textView = popWindowMenu.findViewById(getPopupTextViewID(i));
            textView.setTextColor(getResources().getColor(R.color.colorBlack));
            ImageView imageView = popWindowMenu.findViewById(getPopupImageViewID(i));
            imageView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void showErrorMessage(Object error) {
        ErrorHandler errorHandler = new ErrorHandler();
        alertMessage = new AlertMessage(this, AlertMessage.TITTLE_ERROR);
        String errorMsg = errorHandler.getErrorByCode(getContext(), error);
        alertMessage.showMessage(getActivity(),errorMsg, "");
    }

    @Override
    public void savingFileOk() {
        isSavingFile = false;
    }

    @Override
    public void savingFilePictureIsUri(Uri uri) {
        isPictureUri = true;
        picture = uri;
    }

    @Override
    public void showErrorSavingFile(Object error) {
        isSavingFile = true;
        alertRetry = new AlertRetry(getActivity(), this);
        alertRetry.showMessage(null);
    }

    @Override
    public void onRetryAccept(AlertRetry alertRetry) {
        if (alertRetry != null && alertRetry.alert != null
                && alertRetry.alert.isShowing()) {
            alertRetry.alert.dismiss();
        }
        alertNonDismissable.showMessage(getActivity(), getResources().getString(R.string.saving_file));
        if (savingIsPhoto) {
            onSavePhoto();
        } else {
            onSaveVideo();
        }
    }

    @Override
    public void onRetryCancel(AlertRetry alertRetry) {
        isSavingFile = false;
        if (alertRetry != null && alertRetry.alert != null
                && alertRetry.alert.isShowing()) {
            alertRetry.alert.dismiss();
        }
    }

    @Override
    public void closeAlertSavingImage() {
        if (alertNonDismissable != null && alertNonDismissable.alert != null
                && alertNonDismissable.alert.isShowing() && getActivity() != null && isAdded()) {
            alertNonDismissable.alert.dismiss();
        }
    }

    public void configureRecyclerView() {
        if (!isAdded()) return;

        recyclerView = v.findViewById(R.id.recyclerView);

        int numberOfColumns = getResources().getInteger(R.integer.gallery_number_of_columns);
        mLayoutManager = new GridLayoutManager(getContext(), numberOfColumns);
        if (itemDecoration == null) {
            int spacing = getResources().getDimensionPixelSize(R.dimen.adapter_image_spacing);
            itemDecoration = new GridSpacingItemDecoration(numberOfColumns, spacing, false);
        }
        recyclerView.removeItemDecoration(itemDecoration);
        recyclerView.addItemDecoration(itemDecoration);

        recyclerView.setLayoutManager(mLayoutManager);


    }

    @Override
    public void showGalleryContent(final List<GalleryContentRealm> galleryContentList) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (adapter == null || recyclerView.getAdapter() == null || hasZeroFiles) {
                        adapter = new GalleryAdapter(getContext(), galleryContentList, gallerypPresenter.getItemsSelected(),
                                gallerypPresenter.isInSelectionMode(), GalleryFragment.this);
                        adapter.addItemClickedListeners(GalleryFragment.this);
                        recyclerView.setAdapter(adapter);
                        hasZeroFiles = false;
                    } else {
                        if (galleryContentList == null || galleryContentList.size() == 0) {
                            adapter = new GalleryAdapter(getContext(), new ArrayList<GalleryContentRealm>(), gallerypPresenter.getItemsSelected(),
                                    gallerypPresenter.isInSelectionMode(), GalleryFragment.this);
                            recyclerView.setAdapter(adapter);
                            hasZeroFiles = true;
                        } else {
                            adapter.setGalleryContents(galleryContentList);
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
            });
        }
    }

    @Override
    public void updateContents(RealmResults<GalleryContentRealm> contentPaths) {
        adapter.notifyDataSetChanged();
        v.findViewById(R.id.progressbar).setVisibility(View.GONE);
    }

    @Override
    public void onFileAdded() {
        if (viewCreated) adapter.notifyDataSetChanged();
    }

    @Override
    public boolean checkWriteExternalStoragePermission() {
        if (getActivity() == null) return false;
        return ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void updateEnabledButtons(boolean enabled) {
        if (shareButton != null && deleteButton != null) {
            shareButton.setEnabled(enabled);
            deleteButton.setEnabled(enabled);
            ImageView iv = shareButton.findViewById(R.id.share_iv);
            if (iv != null) iv.setEnabled(enabled);
            TextView tv = shareButton.findViewById(R.id.share_tv);
            if (tv != null) tv.setEnabled(enabled);
            iv = deleteButton.findViewById(R.id.delete_iv);
            if (iv != null) iv.setEnabled(enabled);
            tv = deleteButton.findViewById(R.id.delete_tv);
            if (tv != null) tv.setEnabled(enabled);
        }
    }

    @Override
    public void onUpdateIsInSelectionMode() {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
        setupBottomBar(inflater);
        if (selectMode) {
            adapter.setInSelectedMode(true);
        } else {
            adapter.setInSelectedMode(gallerypPresenter.isInSelectionMode());
            if (gallerypPresenter.isInSelectionMode() && gallerypPresenter.getItemsSelected() != null && gallerypPresenter.getItemsSelected().size() == 0) {
                if (shareButton != null && deleteButton != null) {
                    shareButton.setEnabled(false);
                    deleteButton.setEnabled(false);
                    ImageView iv = shareButton.findViewById(R.id.share_iv);
                    if (iv != null) iv.setEnabled(false);
                    TextView tv = shareButton.findViewById(R.id.share_tv);
                    if (tv != null) tv.setEnabled(false);
                    iv = deleteButton.findViewById(R.id.delete_iv);
                    if (iv != null) iv.setEnabled(false);
                    tv = deleteButton.findViewById(R.id.delete_tv);
                    if (tv != null) tv.setEnabled(false);
                }
            } else {
                if (shareButton != null && deleteButton != null) {
                    shareButton.setEnabled(true);
                    deleteButton.setEnabled(true);
                    ImageView iv = shareButton.findViewById(R.id.share_iv);
                    if (iv != null) iv.setEnabled(true);
                    TextView tv = shareButton.findViewById(R.id.share_tv);
                    if (tv != null) tv.setEnabled(true);
                    iv = deleteButton.findViewById(R.id.delete_iv);
                    if (iv != null) iv.setEnabled(true);
                    tv = deleteButton.findViewById(R.id.delete_tv);
                    if (tv != null) tv.setEnabled(true);
                }
            }
        }
    }


    @Override
    public void onOkAlertMessage(AlertMessage alertMessage, String type) {
        alertMessage.alert.cancel();
    }

    public void onViewItem(GalleryContentRealm galleryContentRealm, int index) {
        this.index = index;
        this.galleryContentRealm = galleryContentRealm;
        Log.d("filtfa","onGalleryItemPicked filt:"+gallerypPresenter.getFilterKind());
        mListener.onGalleryItemPicked(galleryContentRealm, index, gallerypPresenter.getFilterKind());
    }

    @Override
    public void onSelectItem(GalleryContentRealm galleryContentRealm, int index) {
        gallerypPresenter.itemSelected(galleryContentRealm.getId(),index);
    }

    @Override
    public void needGalleryContent(int id, int idContent) {
        gallerypPresenter.getGalleryPathByContentId(id, idContent);
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
        if (gallerypPresenter.isInSelectionMode() || selectMode) {
            switch (page) {
                case 0:
                    return getString(R.string.context_help_gallery_back);
                case 1:
                    return getString(R.string.context_help_gallery_delete);
                case 2:
                    return getString(R.string.context_help_gallery_share_contacts);
                default:
                    return null;
            }
        } else {
            switch (page) {
                case 0:
                    return getString(R.string.context_help_gallery_filter);
                case 1:
                    return getString(R.string.context_help_gallery_share);
                case 2:
                    return getString(R.string.context_help_gallery_new_photo);
                case 3:
                    return getString(R.string.context_help_gallery_new_video);
                default:
                    return null;
            }
        }
    }

    @Override
    protected View getViewForPage(int page) {
        if (gallerypPresenter.isInSelectionMode() || selectMode) {
            switch (page) {
                case 0:
                    return bottomBar.findViewById(R.id.cancel);
                case 1:
                    return bottomBar.findViewById(R.id.delete);
                case 2:
                    return bottomBar.findViewById(R.id.share);
                default:
                    return null;
            }
        } else {
            switch (page) {
                case 0:
                    return bottomBar.findViewById(R.id.filter);
                case 1:
                    return bottomBar.findViewById(R.id.share);
                case 2:
                    return bottomBar.findViewById(R.id.camera);
                case 3:
                    return bottomBar.findViewById(R.id.video);
                default:
                    return null;
            }
        }
    }

    public interface OnFragmentInteractionListener {
        void onGalleryItemPicked(GalleryContentRealm galleryContent, int index, String filterKind);
        void onGalleryShareButtonClicked(ArrayList<Integer> idContentList, ArrayList<String> paths,
                                         ArrayList<String> metadata, boolean fromChat);
    }

    public void onSavePhoto() {
        gallerypPresenter.pushImageToAPI(picture, isPictureUri);
    }
    public void onSaveVideo() {
        gallerypPresenter.pushVideoToAPI((Uri)picture);
    }
}
