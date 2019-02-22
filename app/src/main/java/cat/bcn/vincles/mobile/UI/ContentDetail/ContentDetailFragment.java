package cat.bcn.vincles.mobile.UI.ContentDetail;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.Locale;

import cat.bcn.vincles.mobile.Client.Db.GalleryDb;
import cat.bcn.vincles.mobile.Client.Db.Model.GalleryContentRealm;
import cat.bcn.vincles.mobile.Client.Db.UsersDb;
import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Client.Requests.BaseRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetGalleryContentRequest;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.UI.Alert.AlertConfirmOrCancel;
import cat.bcn.vincles.mobile.UI.Alert.AlertMessage;
import cat.bcn.vincles.mobile.UI.Common.BaseFragment;
import cat.bcn.vincles.mobile.Utils.DateUtils;
import cat.bcn.vincles.mobile.Utils.ImageUtils;
import io.realm.RealmResults;

public class ContentDetailFragment extends BaseFragment implements ContentDetailView, AlertMessage.AlertMessageInterface, View.OnClickListener,
        AlertConfirmOrCancel.AlertConfirmOrCancelInterface, ViewPager.OnPageChangeListener, GetGalleryContentRequest.OnResponse, ContentDetailPagerAdapter.DownloadRequest {

    private OnFragmentInteractionListener mListener;
    UserPreferences userPreferences;
    GalleryDb galleryDb;
    UsersDb usersDb;
    ImageView avatar;
    View deleteBtn;
    View shareButton;
    TextView ownerNameTv, dateTv, hourTv;
    String pathImage = "";
    int index = 0;
    ContentDetailPresenter contentDetailPresenter;
    AlertMessage alertMessageErrorRemovinContent;
    AlertMessage alertMessageOpeningImage;
    AlertConfirmOrCancel alertConfirmOrCancel;
    ContentDetailPagerAdapter pagerAdapter;
    ViewPager viewPager;
    Button nextButton, previousButton;
    TextView nextTV, previousTV;
    int currentPage;
    String filterKind;
    private String avatarPath;
    private RealmResults<GalleryContentRealm> galleryContentRealmRealmResults;
    private ContentDetailListener listener;
    private boolean sharedMedia;

    public ContentDetailFragment() {
        // Required empty public constructor
    }

    public static ContentDetailFragment newInstance(BaseFragment.FragmentResumed listener, String path, int index, String filterKind) {
        ContentDetailFragment fragment = new ContentDetailFragment();
        Bundle args = new Bundle();
        args.putString("path", path);
        args.putInt("index", index);
        args.putString("filterKind", filterKind);
        fragment.setArguments(args);
        fragment.setListener(listener, BaseFragment.FragmentResumed.FRAGMENT_GALLERY);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.pathImage = getArguments().getString("path", "");
        this.index = getArguments().getInt("index", 0);
        this.filterKind = getArguments().getString("filterKind");
        galleryDb = new GalleryDb(getContext());
        usersDb = new UsersDb(getContext());
        userPreferences = new UserPreferences(getContext());
        contentDetailPresenter = new ContentDetailPresenter((BaseRequest.RenewTokenFailed) getActivity(),this, galleryDb,usersDb,
                userPreferences, filterKind);
        galleryContentRealmRealmResults = contentDetailPresenter.getFilteredMedia();
        pagerAdapter = new ContentDetailPagerAdapter(getActivity().getSupportFragmentManager(),
                galleryContentRealmRealmResults, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = null;
        if (sharedMedia) {
            sharedMedia = false;
            getFragmentManager().popBackStack();
        } else {
            v = inflater.inflate(R.layout.fragment_content_detail, container, false);
            ownerNameTv = v.findViewById(R.id.chat_title);
            dateTv = v.findViewById(R.id.date);
            hourTv = v.findViewById(R.id.hour);
            deleteBtn = v.findViewById(R.id.delete);
            if (deleteBtn == null) {
                deleteBtn = v.findViewById(R.id.delete);
            }
            shareButton = v.findViewById(R.id.share);
            nextButton = v.findViewById(R.id.next);
            previousButton = v.findViewById(R.id.before);
            nextTV = v.findViewById(R.id.next_text);
            previousTV = v.findViewById(R.id.beforeText);

            deleteBtn.setOnClickListener(this);
            v.findViewById(R.id.back).setOnClickListener(this);
            if (nextButton != null) nextButton.setOnClickListener(this);
            if (previousButton != null) previousButton.setOnClickListener(this);
            if (shareButton != null) shareButton.setOnClickListener(this);

            contentDetailPresenter.loadOwnerName(index);
            contentDetailPresenter.loadDate(getContext(), index);

            avatar = v.findViewById(R.id.avatar);
            if (avatar != null) {
                contentDetailPresenter.loadAvatar(index);
            }

            viewPager = v.findViewById(R.id.pager);
            viewPager.setAdapter(pagerAdapter);
            viewPager.setCurrentItem(index);
            currentPage = index;
            viewPager.addOnPageChangeListener(this);
            setButtonsForPosition(index);

        }


        return v;
    }

    @Override
    protected void processPendingChanges(Bundle bundle){
        Log.d("notman2","chat processPendingChanges");
        if (bundle.getInt(BaseFragment.CHANGES_TYPE) == BaseFragment.CHANGES_OTHER_NOTIFICATION) {
            String type = bundle.getString("type");
            switch (type) {
                case "USER_UPDATED":
                    contentDetailPresenter.onUserUpdated(bundle.getInt("idUser"));
                    break;
            }
        }
        pendingChangeProcessed();
    }


    @Override
    public void setOwnerName (String ownerName) {
        ownerNameTv.setText(ownerName);
        //In constraint layout 1.0.2 there is a bug with wrap when changing the text (size) of the
        //textview. This forces it to recalculate
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) ownerNameTv.getLayoutParams();
        params.width = 0;
        ownerNameTv.setLayoutParams(params);
    }



    @Override
    public void setDate(int day, int month, int year, String formatedTime) {
        Locale current = getResources().getConfiguration().locale;
        String lang = current.getLanguage();
        String date = DateUtils.getFormatedDate(lang.equals("ca"),day,month,year);
        dateTv.setText(date);
        hourTv.setText(formatedTime);
    }

    @Override
    public void showAvatar (final String path) {
        avatarPath = path;
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Glide.with(getContext()).load(path.equals("placeholder") ?
                            getResources().getDrawable(R.drawable.user)
                            : new File(path))
                            .apply(RequestOptions.overrideOf(200, 200))
                            .into(avatar);
                }
            });
        }
    }

    @Override
    public void showError(Object error) {
        ErrorHandler errorHandler = new ErrorHandler();
        AlertMessage alertMessage = new AlertMessage(this, AlertMessage.TITTLE_ERROR);
        String errorMsg = errorHandler.getErrorByCode(getContext(), error);
        alertMessage.showMessage(getActivity(),errorMsg, "");

        if (alertConfirmOrCancel != null) alertConfirmOrCancel.alert.cancel();
    }

    @Override
    public void showConfirmationRemoveContent() {
        alertConfirmOrCancel = new AlertConfirmOrCancel(getActivity(),this);
        alertConfirmOrCancel.showMessage(getString(R.string.remove_item_info), "Eliminar", AlertConfirmOrCancel.BUTTONS_HORIZNTAL);
    }

    @Override
    public void removedContent() {
        if (alertConfirmOrCancel != null && alertConfirmOrCancel.alert != null
                && alertConfirmOrCancel.alert.isShowing()) {
            alertConfirmOrCancel.alert.dismiss();
        }
        getFragmentManager().popBackStack();
    }

    @Override
    public void showErrorRemovingContent() {
        alertMessageErrorRemovinContent = new AlertMessage(this,AlertMessage.TITTLE_ERROR);
        alertMessageErrorRemovinContent.showMessage(getActivity(),getResources().getString(R.string.error_removing_content), "");
    }

    @Override
    public void showErrorOpeningImage() {
        alertMessageOpeningImage = new AlertMessage(new AlertMessage.AlertMessageInterface() {
            @Override
            public void onOkAlertMessage(AlertMessage alertMessage, String type) {
                alertMessage.alert.dismiss();
                getFragmentManager().popBackStack();
            }
        },getResources().getString(R.string.error));
        alertMessageOpeningImage.showMessage(getActivity(),getString(R.string.error_opening_image), "");
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ContentDetailListener) {
            listener = (ContentDetailListener) context;
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
    public void onOkAlertMessage(AlertMessage alertMessage, String type) {
        alertMessage.alert.cancel();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.back) {
            getFragmentManager().popBackStack();
        } else if (view.getId() == R.id.delete || view.getId() == R.id.delete) {
            showConfirmationRemoveContent();
        } else if (view.getId() == R.id.next) {
            viewPager.setCurrentItem(viewPager.getCurrentItem()+1);
        } else if (view.getId() == R.id.before) {
            viewPager.setCurrentItem(viewPager.getCurrentItem()-1);
        } else if (view.getId() == R.id.share) {
            if (listener != null) {
                sharedMedia = true;
                GalleryContentRealm content = galleryContentRealmRealmResults.get(index);
                listener.onGalleryShareButtonClicked(content.getId(), content.getPath(),
                        content.getMimeType());
            }
        }
    }

    private void setButtonsForPosition(int position) {
        if (previousButton==null || nextButton==null) return;
        if (position == 0) {
            previousButton.setVisibility(View.INVISIBLE);
            previousTV.setVisibility(View.INVISIBLE);
            if(pagerAdapter.getCount()>1) {
                nextButton.setVisibility(View.VISIBLE);
                nextTV.setVisibility(View.VISIBLE);
            } else{
                nextButton.setVisibility(View.INVISIBLE);
                nextTV.setVisibility(View.INVISIBLE);
            }
        } else if (position == pagerAdapter.getCount()-1) {
            nextButton.setVisibility(View.INVISIBLE);
            nextTV.setVisibility(View.INVISIBLE);
            previousButton.setVisibility(View.VISIBLE);
            previousTV.setVisibility(View.VISIBLE);
        } else {
            previousButton.setVisibility(View.VISIBLE);
            previousTV.setVisibility(View.VISIBLE);
            nextButton.setVisibility(View.VISIBLE);
            nextTV.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onAccept(AlertConfirmOrCancel alertConfirmOrCancel) {
        if (alertConfirmOrCancel != null && alertConfirmOrCancel.alert != null
                && alertConfirmOrCancel.alert.isShowing()) {
            alertConfirmOrCancel.alert.dismiss();
        }
        contentDetailPresenter.deleteContent(viewPager.getCurrentItem());
    }

    @Override
    public void onCancel(AlertConfirmOrCancel alertConfirmOrCancel) {
        alertConfirmOrCancel.alert.dismiss();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        setButtonsForPosition(position);
        currentPage = position;
        contentDetailPresenter.updateUserID(currentPage);
        contentDetailPresenter.loadOwnerName(currentPage);
        contentDetailPresenter.loadDate(getContext(), currentPage);
        if (avatar != null) {
            contentDetailPresenter.loadAvatar(currentPage);
        }
        if ("".equals(galleryContentRealmRealmResults.get(position).getPath())) {
            if (userPreferences.getIsAutodownload()) {
                getGalleryPathByContentId(galleryContentRealmRealmResults.get(position).getId(),
                        galleryContentRealmRealmResults.get(position).getIdContent());
            }
        }
    }

    @Override
    public int getCurrentPage() {
        return currentPage;
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onDownloadRequest(int position) {
        getGalleryPathByContentId(galleryContentRealmRealmResults.get(position).getId(),
                galleryContentRealmRealmResults.get(position).getIdContent());
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public String getAvatarPath() {
        return avatarPath;
    }

    public void getGalleryPathByContentId(int id, int contentID) {
        String path = galleryDb.getPathFromIdContent(contentID);
        if (path == null) {
            GalleryContentRealm galleryContentRealm = galleryDb.getContentById(id);
            String mimeType = galleryContentRealm.getMimeType();
            GetGalleryContentRequest getGalleryContentRequest = new GetGalleryContentRequest(
                    (BaseRequest.RenewTokenFailed) getActivity(), getContext(), String.valueOf(
                    contentID), mimeType);
            getGalleryContentRequest.addOnOnResponse(this);
            getGalleryContentRequest.doRequest(userPreferences.getAccessToken());
        } else {
            galleryDb.setPathFromIdContent(contentID, path);
            galleryContentRealmRealmResults = contentDetailPresenter.getFilteredMedia();
            viewPager.getAdapter().notifyDataSetChanged();
        }


    }


    @Override
    public void onResponseGetGalleryContentRequest(String contentID, String filePath) {
        galleryDb.setPathToFile(Integer.valueOf(contentID),filePath);
        galleryContentRealmRealmResults = contentDetailPresenter.getFilteredMedia();
        viewPager.getAdapter().notifyDataSetChanged();

        if (userPreferences.getIsCopyPhotos() && getContext() != null &&
                ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            ImageUtils.safeCopyFileToExternalStorage(filePath, contentID);
        }
    }

    @Override
    public void onFailureGetGalleryContentRequest(Object error) {
        //getActivity().showErrorMessage(error);
    }

    public interface ContentDetailListener {
        void onGalleryShareButtonClicked(int idContent, String path, String metadata);
    }

}
