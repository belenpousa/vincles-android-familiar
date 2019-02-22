package cat.bcn.vincles.mobile.UI.Contacts;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daasuu.bl.BubbleLayout;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Client.Requests.BaseRequest;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.UI.Alert.AlertConfirmOrCancel;
import cat.bcn.vincles.mobile.UI.Alert.AlertMessage;
import cat.bcn.vincles.mobile.UI.Alert.AlertNonDismissable;
import cat.bcn.vincles.mobile.UI.Common.BaseFragment;
import cat.bcn.vincles.mobile.UI.Gallery.GridSpacingItemDecoration;
import cat.bcn.vincles.mobile.Utils.OtherUtils;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class ContactsFragment extends BaseFragment implements ContactsFragmentContract, View.OnClickListener, ContactsAdapter.ContactsAdapterListener,
        AlertConfirmOrCancel.AlertConfirmOrCancelInterface, AlertMessage.AlertMessageInterface, ContactsAdapter.OnItemClicked {

    private static final int ALERT_NONE_SHOWING = -1;
    private static final int ALERT_VOLS_ELIMINAR = 0;
    private static final int ALERT_ELIMINAR_OK = 1;
    private static final int ALERT_ELIMINAR_KO = 2;
    private static final int ALERT_SHARED_MEDIA_OK = 3;
    private static final int ALERT_SHARED_MEDIA_KO = 4;

    private View v;
    private ViewGroup bottomBar;
    private OnFragmentInteractionListener mListener;
    private PopupWindow mPopupWindow;
    private View popWindowMenu;
    private boolean shouldShowFilter = false;
    private View filterBtn, removeBtn, addBtn;
    private ContactsPresenter contactsPresenter;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.ItemDecoration itemDecoration;
    private ContactsAdapter contactsAdapter;
    private boolean showDeleteLayout;
    private AlertConfirmOrCancel alertConfirmOrCancel;
    private int idUserToUnlink;
    private  RecyclerView recyclerView;
    private boolean isUserSenior;
    private LayoutInflater layoutInflater;
    private TextView noContactsError;
    private String contactNameToDelete;
    private AlertNonDismissable alertNonDismissable;
    private ArrayList<Integer> shareContents;
    private TextView titleTextView;
    private int alertShowingMode = ALERT_NONE_SHOWING;
    private boolean isShowingAlert;
    private boolean isShareDate;

    public ContactsFragment() {
        // Required empty public constructor
    }

    public static ContactsFragment newInstance(FragmentResumed listener, int filter, ArrayList<Integer> shareContents) {
        Log.d("shre","contacts frag new instance filter:"+filter);
        ContactsFragment fragment = new ContactsFragment();
        fragment.setListener(listener, FragmentResumed.FRAGMENT_CONTACTS);
        Bundle arguments = new Bundle();
        arguments.putInt("filterKind", filter);
        arguments.putIntegerArrayList("shareContents", shareContents);
        fragment.setArguments(arguments);
        return fragment;
    }

    public static ContactsFragment newInstance(FragmentResumed listener, int filter,
                                               boolean isShareDate) {
        ContactsFragment fragment = new ContactsFragment();
        fragment.setListener(listener, FragmentResumed.FRAGMENT_CONTACTS);
        Bundle arguments = new Bundle();
        arguments.putInt("filterKind", filter);
        arguments.putIntegerArrayList("shareContents", null);
        arguments.putBoolean("isShareDate", isShareDate);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getContext() != null)
            OtherUtils.sendAnalyticsView(getContext(),
                    getResources().getString(R.string.tracking_contacts));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isUserSenior = new UserPreferences(getContext()).getIsUserSenior();
        int filterKind = getArguments().getInt("filterKind");
        isShareDate = getArguments().getBoolean("isShareDate");
        shareContents = getArguments().getIntegerArrayList("shareContents");
        if (savedInstanceState != null) {
            shareContents = savedInstanceState.getIntegerArrayList("shareContents");
            filterKind = savedInstanceState.getInt("filterKind", ContactsPresenterContract.FILTER_NOT_INIT);
            shouldShowFilter = savedInstanceState.getBoolean("showingFilter");

            alertShowingMode = savedInstanceState.getInt("alertShowingMode");
            idUserToUnlink = savedInstanceState.getInt("idUserToUnlink");
            contactNameToDelete = savedInstanceState.getString("contactNameToDelete");
            isShowingAlert = savedInstanceState.getBoolean("isShowingAlert");
            alertShowingMode = savedInstanceState.getInt("alertShowingMode", -1);
            showDeleteLayout = savedInstanceState.getBoolean("showDeleteLayout");


        }
        alertNonDismissable = new AlertNonDismissable(getResources().getString(R.string.login_sending_data), true);
        /*if (shareContents != null && shareContents.size() > 0 || isShareDate) {
            filterKind = ContactsPresenterContract.FILTER_FAMILY;
        }*/
        contactsPresenter = new ContactsPresenter((BaseRequest.RenewTokenFailed) getActivity(),this, filterKind);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("filterKind", contactsPresenter.getFilterKind());
        outState.putIntegerArrayList("shareContents", shareContents);
        outState.putBoolean("showingFilter", mPopupWindow != null && mPopupWindow.isShowing());
        outState.putBoolean("isShareDate", isShareDate);

        outState.putInt("alertShowingMode",alertShowingMode);
        outState.putInt("idUserToUnlink",idUserToUnlink);
        outState.putString("contactNameToDelete",contactNameToDelete);
        outState.putBoolean("isShowingAlert", isShowingAlert);
        outState.putBoolean("showDeleteLayout", showDeleteLayout);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_contacts, container, false);
        noContactsError = v.findViewById(R.id.no_contacts_error);
        initRecyclerView();
        View backBtn = v.findViewById(R.id.back);
        if (backBtn != null) backBtn.setOnClickListener(this);
        this.layoutInflater = inflater;
        bottomBar = v.findViewById(R.id.bottom_bar);
        titleTextView = v.findViewById(R.id.title);

        setupBottomBar(layoutInflater, showDeleteLayout);
        contactsPresenter.getContacts();

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

        if (shareContents != null && shareContents.size() > 0) {
            //titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            titleTextView.setText(R.string.contacts_share_title);
        } else if (isShareDate) {
            titleTextView.setText(R.string.calendar_invite_contacts_title);
        } else {
            setTitle(contactsPresenter.getFilterKind());
        }

        showAlert();
        return v;
    }

    private void initRecyclerView() {
        recyclerView = v.findViewById(R.id.recyclerView);

        int numberOfColumns = getResources().getInteger(R.integer.contacts_number_of_columns);
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
    protected void processPendingChanges(Bundle bundle){
        Log.d("notman2","chat processPendingChanges");
        if (bundle.getInt(BaseFragment.CHANGES_TYPE) == BaseFragment.CHANGES_OTHER_NOTIFICATION) {
            contactsPresenter.notificationToProcess(bundle);
        }
        pendingChangeProcessed();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back:
                if (shareContents != null) {
                    shareContents.clear();
                }
                getFragmentManager().popBackStack();
                break;
            case R.id.filterLayout:
                showFilterPopup();
                break;
            case R.id.addLayout:
                mListener.onAddContactPressed();
                break;
            case R.id.removeLayout:
                showDeleteLayout = !showDeleteLayout;
                setupBottomBar(layoutInflater, showDeleteLayout);
                showRemoveLayout(showDeleteLayout);
                break;
            case R.id.all_contacts:
                checkPopMenuItem(0);
                if (removeBtn != null) {
                    removeBtn.setEnabled(true);
                    removeBtn.findViewById(R.id.removeImageView).setEnabled(true);
                    TextView tv = removeBtn.findViewById(R.id.removeTextView);
                    if (tv != null) tv.setEnabled(true);
                }
                setTitle(ContactsPresenter.FILTER_ALL_CONTACTS);
                mPopupWindow.dismiss();
                contactsPresenter.onFilterClicked(ContactsPresenter.FILTER_ALL_CONTACTS);
                break;
            case R.id.family:
                checkPopMenuItem(1);
                setTitle(ContactsPresenter.FILTER_FAMILY);
                if (removeBtn != null) {
                    removeBtn.setEnabled(true);
                    removeBtn.findViewById(R.id.removeImageView).setEnabled(true);
                    TextView tv = removeBtn.findViewById(R.id.removeTextView);
                    if (tv != null) tv.setEnabled(true);
                }
                mPopupWindow.dismiss();
                contactsPresenter.onFilterClicked(ContactsPresenter.FILTER_FAMILY);
                break;
            case R.id.groups:
                checkPopMenuItem(2);
                setTitle(ContactsPresenter.FILTER_GROUPS);
                if (removeBtn != null) {
                    removeBtn.setEnabled(false);
                    removeBtn.findViewById(R.id.removeImageView).setEnabled(false);
                    TextView tv = removeBtn.findViewById(R.id.removeTextView);
                    if (tv != null) tv.setEnabled(false);
                }
                mPopupWindow.dismiss();
                contactsPresenter.onFilterClicked(ContactsPresenter.FILTER_GROUPS);
                break;
            case R.id.dynam:
                checkPopMenuItem(3);
                setTitle(ContactsPresenter.FILTER_DYNAM);
                if (removeBtn != null) {
                    removeBtn.setEnabled(false);
                    removeBtn.findViewById(R.id.removeImageView).setEnabled(false);
                    TextView tv = removeBtn.findViewById(R.id.removeTextView);
                    if (tv != null) tv.setEnabled(false);
                }
                mPopupWindow.dismiss();
                contactsPresenter.onFilterClicked(ContactsPresenter.FILTER_DYNAM);
                break;
            //case R.id.delete:
            case R.id.delete:
                shareContents.clear();
                getFragmentManager().popBackStack();
                break;
            case R.id.share:
                contactsPresenter.shareMedia(shareContents);
                break;
            case R.id.invite:
                ArrayList<Integer> selectedContacts = contactsPresenter.getSelectedContacts();
                Log.d("slcon","activity contactsFragment on click:"+selectedContacts);
                if (selectedContacts != null && selectedContacts.size() > 0) {
                    if (mListener != null) mListener.onContactsSelected(selectedContacts);
                } else {
                    isShareDate = false;
                }
                getFragmentManager().popBackStack();
                break;
            case R.id.no_invite:
                isShareDate = false;
                getFragmentManager().popBackStack();
                break;
        }


    }

    private void setTitle(int which) {
        switch (which) {
            case ContactsPresenter.FILTER_ALL_CONTACTS:
                titleTextView.setText(getResources().getString(R.string.contacts_filter_all_title));
                break;
            case ContactsPresenter.FILTER_FAMILY:
                titleTextView.setText(getResources().getString(R.string.contacts_filter_family_friends_title));
                break;
            case ContactsPresenter.FILTER_GROUPS:
                titleTextView.setText(getResources().getString(R.string.contacts_filter_groups_title));
                break;
            case ContactsPresenter.FILTER_DYNAM:
                titleTextView.setText(getResources().getString(R.string.contacts_filter_dynam_title));
                break;
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

    private void setupBottomBar(LayoutInflater inflater, boolean removeAction) {
        bottomBar.removeAllViews();
        if (shareContents != null && shareContents.size() > 0) {
            inflater.inflate(R.layout.fragment_share_select_bottom_bar, bottomBar);
            //inflater.inflate(R.layout.fragment_content_detail_bottom_bar, bottomBar);
            View shareButton = bottomBar.findViewById(R.id.share);
            //ImageView deleteImageView = bottomBar.findViewById(R.id.delete);
            View deleteButton = bottomBar.findViewById(R.id.delete);
            if (deleteButton == null) {
                deleteButton = bottomBar.findViewById(R.id.delete);
            }
            TextView deleteTextView = bottomBar.findViewById(R.id.delete_tv);
            if (deleteTextView != null) {
                deleteTextView.setText(getResources().getString(R.string.gallery_no_share));
            }
            deleteButton.setOnClickListener(this);
            shareButton.setOnClickListener(this);
        } else if (isShareDate) {
            inflater.inflate(R.layout.fragment_invite_contacts_bottom_bar, bottomBar);
            bottomBar.findViewById(R.id.invite).setOnClickListener(this);
            bottomBar.findViewById(R.id.no_invite).setOnClickListener(this);
        } else {
            if (!removeAction) {
                inflater.inflate(R.layout.fragment_contacts_bottom_bar, bottomBar);
                filterBtn = bottomBar.findViewById(R.id.filterLayout);
                addBtn = bottomBar.findViewById(R.id.addLayout);
                removeBtn = bottomBar.findViewById(R.id.removeLayout);
                if (contactsPresenter.getFilterKind() == ContactsPresenterContract.FILTER_DYNAM
                        || contactsPresenter.getFilterKind() == ContactsPresenterContract.FILTER_GROUPS) {
                    removeBtn.setEnabled(false);
                    removeBtn.findViewById(R.id.removeImageView).setEnabled(false);
                    TextView tv = removeBtn.findViewById(R.id.removeTextView);
                    if (tv != null) tv.setEnabled(false);
                } else {
                    removeBtn.setEnabled(true);
                    removeBtn.findViewById(R.id.removeImageView).setEnabled(true);
                    TextView tv = removeBtn.findViewById(R.id.removeTextView);
                    if (tv != null) tv.setEnabled(true);
                }
                if (filterBtn != null && !isUserSenior) {
                    filterBtn.setVisibility(View.GONE);
                } else if (filterBtn != null) {
                    filterBtn.setOnClickListener(this);
                }
                if (addBtn != null) addBtn.setOnClickListener(this);
                if (removeBtn != null) removeBtn.setOnClickListener(this);
            } else {
                inflater.inflate(R.layout.remove_layout, bottomBar);
                LinearLayout removeLayout = bottomBar.findViewById(R.id.removeLayout);
                removeLayout.setOnClickListener(this);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPopupWindow != null) {
            mPopupWindow.dismiss();
        }
    }

    @Override
    public void loadContacts(final List<Contact> contactList) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (contactsAdapter == null || recyclerView.getAdapter() == null) {
                        contactsAdapter = new ContactsAdapter(getContext(), contactList, ContactsFragment.this);
                        contactsAdapter.setDeleteVisibility(showDeleteLayout);
                        if (isShareDate) contactsAdapter.setShowNotificationsNumber(false);
                        if (shareContents != null && shareContents.size() > 0) {
                            contactsAdapter.setContactSelectionEnabled(true);
                            contactsAdapter.setShowNotificationsNumber(false);
                            contactsAdapter.addItemClickedListeners(ContactsFragment.this);
                            if (titleTextView != null) {
                                titleTextView.setText(getResources().getString(R.string.contacts_share_title));
                            }
                        } else if (isShareDate) {
                            contactsAdapter.setContactSelectionEnabled(true);
                            contactsAdapter.addItemClickedListeners(ContactsFragment.this);
                        } else {
                            contactsAdapter.setContactSelectionEnabled(false);
                        }
                        recyclerView.setAdapter(contactsAdapter);
                    } else {
                        contactsAdapter.notifyDataSetChanged();
                    }
                }
            });
        }

    }

    private void showRemoveLayout(boolean show) {
        if (contactsAdapter != null) {
            contactsAdapter.setDeleteVisibility(show);
            contactsAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void reloadContactAdapter() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                contactsAdapter.notifyDataSetChanged();
            }
        });

    }

    @Override
    public void showNoContactsError() {
        if (noContactsError!= null) noContactsError.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideNoContactsError() {
        if (noContactsError!= null) noContactsError.setVisibility(View.GONE);
    }

    private void showAlert() {
        isShowingAlert = true;
        switch (alertShowingMode) {
            case ALERT_VOLS_ELIMINAR:
                alertConfirmOrCancel = new AlertConfirmOrCancel(getActivity(),this);
                alertConfirmOrCancel.setButtonsText(getString(R.string.delete_contact_yes), getString(R.string.delete_contact_no));
                alertConfirmOrCancel.showMessage(getString(R.string.delete_contact_explanation, contactNameToDelete), "Eliminar", AlertConfirmOrCancel.BUTTONS_VERTICAL);
                break;
            case ALERT_ELIMINAR_OK:
                AlertMessage alertMessage = new AlertMessage(this, AlertMessage.TITTLE_INFO);
                alertMessage.showMessage(getActivity(), getString(R.string.delete_contact_success, this.contactNameToDelete), AlertMessage.TITTLE_INFO);
                break;
            case ALERT_ELIMINAR_KO:
                AlertConfirmOrCancel alertConfirmOrCancel = new AlertConfirmOrCancel(getActivity(), this);
                alertConfirmOrCancel.setButtonsText(getString(R.string.retry), getString(R.string.cancel));
                alertConfirmOrCancel.showMessage(getString(R.string.delete_contact_error), getString(R.string.error), AlertConfirmOrCancel.BUTTONS_HORIZNTAL);
                break;
            case ALERT_SHARED_MEDIA_OK:
                AlertMessage alertMessageSharedMedia = new AlertMessage(this, AlertMessage.TITTLE_INFO);
                alertMessageSharedMedia.showMessage(getActivity(), getString(R.string.media_shared_ok), AlertMessage.TITTLE_INFO);
                break;
            case ALERT_SHARED_MEDIA_KO:
                AlertConfirmOrCancel alertConfirmOrCancelShareMedia = new AlertConfirmOrCancel(getActivity(), this);
                alertConfirmOrCancelShareMedia.setButtonsText(getString(R.string.retry), getString(R.string.cancel));
                alertConfirmOrCancelShareMedia.showMessage(getString(R.string.media_shared_ko), getString(R.string.error), AlertConfirmOrCancel.BUTTONS_HORIZNTAL);
                break;
        }
    }


    @Override
    public void showContactDeleteAlert(boolean ok) {
        if (!ok) {
            alertShowingMode = ALERT_ELIMINAR_KO;
            showAlert();
        } else {
            alertShowingMode = ALERT_ELIMINAR_OK;
            showAlert();
        }
    }

    @Override
    public void showSharedMediaAlert(boolean ok) {
        if (ok) {
        alertShowingMode = ALERT_SHARED_MEDIA_OK;
            showAlert();
        } else {
            alertShowingMode = ALERT_SHARED_MEDIA_KO;
            showAlert();
        }
    }

    @Override
    public void needContactPicturePath(int contactId, int contactType) {
        contactsPresenter.getContactPicture(contactId, contactType);
    }

    @Override
    public void deleteCircle(int idUserToUnlink, String contactName) {
        this.idUserToUnlink = idUserToUnlink;
        this.contactNameToDelete = contactName;
        alertShowingMode = ALERT_VOLS_ELIMINAR;
        showAlert();
    }

    @Override
    public void clickedCircle(String idUserToUnlink, boolean isGroupChat, boolean isDynamizer) {
        mListener.onContactSelected(idUserToUnlink, isGroupChat, isDynamizer);
    }

    @Override
    public void onAccept(AlertConfirmOrCancel alertConfirmOrCancel) {
        isShowingAlert = false;
        if (alertShowingMode == ALERT_SHARED_MEDIA_KO) {
            contactsPresenter.shareMedia(shareContents);
        } else {
            contactsPresenter.deleteCircle(idUserToUnlink);
        }
        alertShowingMode = ALERT_NONE_SHOWING;
        alertConfirmOrCancel.resetAlert();
        alertConfirmOrCancel.alert.dismiss();
    }

    @Override
    public void onCancel(AlertConfirmOrCancel alertConfirmOrCancel) {
        isShowingAlert = false;
        alertShowingMode = ALERT_NONE_SHOWING;
        alertConfirmOrCancel.resetAlert();
        alertConfirmOrCancel.alert.dismiss();
    }

    @Override
    public void onOkAlertMessage(AlertMessage alertMessage, String type) {
        isShowingAlert = false;
        alertMessage.alert.dismiss();
        if (alertShowingMode == ALERT_SHARED_MEDIA_OK) {
            getFragmentManager().popBackStack();
        } else {
            alertShowingMode = ALERT_NONE_SHOWING;
            contactsPresenter.refreshContactList();
        }
    }

    private void showFilterPopup() {
        if (popWindowMenu == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            popWindowMenu = inflater.inflate(R.layout.popupwindow_filter_contacts, null);
            mPopupWindow = new PopupWindow(
                    popWindowMenu,
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
            mPopupWindow.setFocusable(true);
            mPopupWindow.setOutsideTouchable(true);

            TextView allContacts = popWindowMenu.findViewById(R.id.all_contacts);
            TextView family = popWindowMenu.findViewById(R.id.family);
            TextView groups = popWindowMenu.findViewById(R.id.groups);
            TextView dynam = popWindowMenu.findViewById(R.id.dynam);

            allContacts.setOnClickListener(this);
            family.setOnClickListener(this);
            groups.setOnClickListener(this);
            dynam.setOnClickListener(this);

            if (contactsPresenter.getFilterKind() == (ContactsPresenter.FILTER_FAMILY)) {
                checkPopMenuItem(1);
            } else if (contactsPresenter.getFilterKind() == (ContactsPresenter.FILTER_GROUPS)) {
                checkPopMenuItem(2);
            } else if (contactsPresenter.getFilterKind() == (ContactsPresenter.FILTER_DYNAM)) {
                checkPopMenuItem(3);
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
        for (int i = 0; i < 4; i++) {
            TextView textView = popWindowMenu.findViewById(getPopupTextViewID(i));
            textView.setTextColor(getResources().getColor(R.color.colorBlack));
            ImageView imageView = popWindowMenu.findViewById(getPopupImageViewID(i));
            imageView.setVisibility(View.INVISIBLE);
        }
    }
    private int getPopupTextViewID(int index) {
        switch (index) {
            case 0: default: return R.id.all_contacts;
            case 1: return R.id.family;
            case 2: return R.id.groups;
            case 3: return R.id.dynam;
        }
    }
    private int getPopupImageViewID(int index) {
        switch (index) {
            case 0: default: return R.id.all_contacts_iv;
            case 1: return R.id.family_iv;
            case 2: return R.id.groups_iv;
            case 3: return R.id.dynam_iv;
        }
    }

    @Override
    public void showWaitDialog() {
        alertNonDismissable.showMessage(getActivity());
    }

    @Override
    public void hideWaitDialog() {
        if (alertNonDismissable != null) alertNonDismissable.dismissSafely();
    }

    /**
     *
     * Methods for showing help guide
     *
     */
    @Override
    protected boolean shouldShowMenu() {
        return !(shareContents != null && shareContents.size() > 0) && !isShareDate;
    }

    @Override
    protected String getTextForPage(int page) {
        boolean isSelectMode = (shareContents != null && shareContents.size() > 0) || isShareDate
                || showDeleteLayout;
        if (isSelectMode) {
            switch (page) {
                case 0:
                    return getString(R.string.context_help_contacts_delete_button);
                default:
                    return null;
            }
        } else if (isUserSenior) {
            switch (page) {
                case 0:
                    return getString(R.string.context_help_contacts_filter);
                case 1:
                    return getString(R.string.context_help_contacts_add);
                case 2:
                    return getString(R.string.context_help_contacts_delete);
                default:
                    return null;
            }
        } else {
            switch (page) {
                case 0:
                    return getString(R.string.context_help_contacts_add);
                case 1:
                    return getString(R.string.context_help_contacts_delete);
                default:
                    return null;
            }
        }

    }

    @Override
    protected View getViewForPage(int page) {
        boolean isSelectMode = (shareContents != null && shareContents.size() > 0) || isShareDate
                || showDeleteLayout;
        if (isSelectMode) {
            switch (page) {
                case 0:
                    return v.findViewById(R.id.removeLayout);
                default:
                    return null;
            }
        }  else if (isUserSenior) {
            switch (page) {
                case 0:
                    return v.findViewById(R.id.filterLayout);
                case 1:
                    return v.findViewById(R.id.addLayout);
                case 2:
                    return v.findViewById(R.id.removeLayout);
                default:
                    return null;
            }
        } else {
            switch (page) {
                case 0:
                    return v.findViewById(R.id.addLayout);
                case 1:
                    return v.findViewById(R.id.removeLayout);
                default:
                    return null;
            }
        }


    }


    public interface OnFragmentInteractionListener {
        void onAddContactPressed();
        void onContactSelected(String idUserSender, boolean isGroupChat, boolean isDynamizer);
        void onContactsSelected(ArrayList<Integer> contactIds);
    }


    @Override
    public void onSelectItem(Contact contact, int index) {
        contactsPresenter.itemSelected(contact, index);
    }


}
