package cat.bcn.vincles.mobile.UI.Contacts;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Client.Requests.BaseRequest;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.UI.Alert.AlertMessage;
import cat.bcn.vincles.mobile.UI.Alert.AlertNonDismissable;
import cat.bcn.vincles.mobile.UI.Alert.ContactAddedAlert;
import cat.bcn.vincles.mobile.Utils.OtherUtils;

public class AddContactFragment extends Fragment implements View.OnClickListener, AddContactView, AlertMessage.AlertMessageInterface, ContactAddedAlert.AddContactDialogCallback {

    private AddContactPresenterContract presenter;
    private EditText codeEditText;
    private TextView codeTextView;
    private AlertNonDismissable alertNonDismissable;
    private LinearLayout seeCodeLayout;
    private ContactAddedAlert contactAddedAlert;
    private View cancel;
    private Spinner relationshipSpinner;

    private String code = null;

    public AddContactFragment() {
        // Required empty public constructor
    }

    public static AddContactFragment newInstance() {
        return new AddContactFragment();
    }

    public static AddContactFragment newInstance(String code) {
        AddContactFragment fragment = new AddContactFragment();
        Bundle arguments = new Bundle();
        arguments.putString("code", code);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getContext() != null)
            OtherUtils.sendAnalyticsView(getContext(),
                    getResources().getString(R.string.tracking_add_contact));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) code = getArguments().getString("code");

        alertNonDismissable = new AlertNonDismissable(getResources().getString(R.string.login_sending_data), true);
        presenter = new AddContactPresenter((BaseRequest.RenewTokenFailed) getActivity(), this,
                new UserPreferences(getContext()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View v;
        if (new UserPreferences().getIsUserSenior()) {
            v = inflater.inflate(R.layout.add_contact_layout, container, false);
            v.findViewById(R.id.see_code_button).setOnClickListener(this);
        } else {
            v = inflater.inflate(R.layout.add_contact_layout_no_code, container, false);
        }
        v.findViewById(R.id.back).setOnClickListener(this);
        v.findViewById(R.id.add_contact_button).setOnClickListener(this);

        this.relationshipSpinner = v.findViewById(R.id.relationship_spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getActivity(), R.array.add_contact_relationships, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.relationshipSpinner.setAdapter(adapter);

        cancel = v.findViewById(R.id.cancel);
        cancel.setOnClickListener(this);
        codeEditText = v.findViewById(R.id.add_contact_code_et);
        codeTextView = v.findViewById(R.id.see_code_textview);
        seeCodeLayout = v.findViewById(R.id.see_code_ll);

        if (code != null) codeEditText.setText(code);

        if (getActivity() != null) {
            KeyboardVisibilityEvent.setEventListener(
                    getActivity(),
                    new KeyboardVisibilityEventListener() {
                        @Override
                        public void onVisibilityChanged(boolean isOpen) {
                            if (getActivity() != null && isAdded()) {
                                DisplayMetrics dm = getResources().getDisplayMetrics();
                                boolean isLandscape = dm.widthPixels > dm.heightPixels;
                                boolean isTablet = getResources().getBoolean(R.bool.isTablet);

                                // some code depending on keyboard visiblity status 
                                if (seeCodeLayout != null) seeCodeLayout.setVisibility(isOpen && (isTablet
                                        || !isLandscape) ? View.GONE : View.VISIBLE);
                                if (cancel != null) {
                                    cancel.setVisibility(isOpen && isLandscape ? View.GONE : View.VISIBLE);
                                }

                                View addContactButton = v.findViewById(R.id.add_contact_button);
                                if (addContactButton != null) addContactButton.setVisibility(isOpen
                                        && isTablet && isLandscape ? View.GONE : View.VISIBLE);

                                View addContactTV = v.findViewById(R.id.add_contact_tv);
                                if (addContactTV != null) addContactTV.setVisibility(isOpen
                                        && isTablet && isLandscape ? View.GONE : View.VISIBLE);

                                View cancelButton = v.findViewById(R.id.cancel);
                                if (cancelButton != null) cancelButton.setVisibility(isOpen
                                        && !isTablet ? View.GONE : View.VISIBLE);
                            }
                        }
                    });
        }

        return v;
    }

    @Override
    public void onClick(View view) {
        Log.d("GenerateUserCode","on click");
        InputMethodManager inputMethodManager =
                (InputMethodManager) getActivity().getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                getActivity().getCurrentFocus().getWindowToken(), 0);
        switch (view.getId()) {
            case R.id.back:
            case R.id.cancel:
                getFragmentManager().popBackStack();
                break;
            case R.id.see_code_button:
                Log.d("GenerateUserCode","see Code click listener");
                presenter.onSeeCodeClicked();
                break;
            case R.id.add_contact_button:
                String[] relationships = {"PARTNER", "CHILD", "GRANDCHILD", "FRIEND", "VOLUNTEER", "CAREGIVER", "SIBLING", "NEPHEW", "OTHER"};
                presenter.onAddContactClicked(codeEditText.getText().toString(), relationships[this.relationshipSpinner.getSelectedItemPosition()]);
                break;
        }
    }


    @Override
    public void showCode(String code) {
        codeTextView.setText(code);
        codeTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public void showWaitDialog() {
        alertNonDismissable.showMessage(getActivity());
    }

    @Override
    public void hideWaitDialog() {
        if (alertNonDismissable != null) alertNonDismissable.dismissSafely();
    }

    @Override
    public void codeShouldNotBeEmpty() {
        codeEditText.requestFocus();
        codeEditText.setError(getString(R.string.add_contacts_add_code_error));
    }

    @Override
    public void showContactAdded(final Contact contact) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                contactAddedAlert = new ContactAddedAlert(getActivity(),AddContactFragment.this);
                contactAddedAlert.showMessage(getResources().getString(R.string.add_contact_ok_message)+ ": "+contact.getName() + " " + contact.getLastname(), contact.getPath());
            }
        });

    }

    @Override
    public void showErrorMessage(Object error) {
        ErrorHandler errorHandler = new ErrorHandler();
        AlertMessage alertMessage = new AlertMessage(this, AlertMessage.TITTLE_ERROR);
        String errorMsg = errorHandler.getErrorByCode(getContext(), error);
        alertMessage.showMessage(getActivity(),errorMsg, "");
    }

    @Override
    public void onOkAlertMessage(AlertMessage alertMessage, String type) {
        if (alertMessage != null) alertMessage.dismissSafely();
    }

    @Override
    public void onAcceptButtonClicked() {
        if (contactAddedAlert != null) {
            contactAddedAlert.close();
            contactAddedAlert = null;
            getFragmentManager().popBackStack();
        }
    }
}
