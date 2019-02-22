package cat.bcn.vincles.mobile.UI.About;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.text.method.LinkMovementMethod;
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

import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.Locale;

import cat.bcn.vincles.mobile.BuildConfig;
import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Model.UserRegister;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Client.Requests.UpdateUserRequest;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.UI.Alert.AlertMessage;
import cat.bcn.vincles.mobile.UI.Alert.AlertPickOrTakePhoto;
import cat.bcn.vincles.mobile.UI.Common.BaseFragment;
import cat.bcn.vincles.mobile.Utils.CircleTransform;
import cat.bcn.vincles.mobile.Utils.ImageUtils;
import cat.bcn.vincles.mobile.Utils.OtherUtils;

import static android.app.Activity.RESULT_OK;

public class AboutFragment extends BaseFragment {



    public AboutFragment() {
        // Required empty public constructor
    }

    public static AboutFragment newInstance(FragmentResumed listener) {
        AboutFragment fragment = new AboutFragment();
        fragment.setListener(listener, FragmentResumed.FRAGMENT_ABOUT);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getContext() != null)
            OtherUtils.sendAnalyticsView(getContext(),
                    getResources().getString(R.string.tracking_about));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_about, container, false);
        TextView versionTV = v.findViewById(R.id.app_version_tv);
        versionTV.setText(getString(R.string.about_app_version, BuildConfig.VERSION_NAME) + " (" + BuildConfig.VERSION_CODE +")" );

        TextView bodyText = v.findViewById(R.id.about_tv);
        bodyText.setMovementMethod(LinkMovementMethod.getInstance());


        //((TextView)v.findViewById(R.id.about_tv)).setMovementMethod(LinkMovementMethod.getInstance());

        v.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        return v;
    }


}
