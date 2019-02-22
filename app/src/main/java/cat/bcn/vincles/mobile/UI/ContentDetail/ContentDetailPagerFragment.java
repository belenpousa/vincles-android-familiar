package cat.bcn.vincles.mobile.UI.ContentDetail;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;

import java.io.File;

import cat.bcn.vincles.mobile.Client.Model.GalleryContent;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.UI.Gallery.ZoomContentActivity;
import cat.bcn.vincles.mobile.Utils.OtherUtils;

public class ContentDetailPagerFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = ContentDetailPagerFragment.class.getName();

    boolean isAutoDownload;
    String filePath, mimeType;
    ProgressBar progressBar;
    ImageView videoHint;
    ImageView downloadIV;
    ImageView imageView;
    private int position;
    private DownloadRequest listener;

    public static ContentDetailPagerFragment newInstance(String filePath, String mimeType, int position) {
        ContentDetailPagerFragment fragment = new ContentDetailPagerFragment();
        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putString("filePath", filePath);
        args.putString("mimeType", mimeType);
        args.putInt("position", position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getContext() != null)
            OtherUtils.sendAnalyticsView(getContext(),
                    getResources().getString(R.string.tracking_gallery_detail));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle args = getArguments();
        filePath = args.getString("filePath");
        mimeType = args.getString("mimeType");
        position = args.getInt("position");
        isAutoDownload = new UserPreferences(getContext()).getIsAutodownload();

        boolean isVideo = (mimeType != null && mimeType.startsWith("video"));
        View rootView = inflater.inflate(R.layout.fragment_content_detail_pager_picture, container, false);

        imageView = rootView.findViewById(R.id.imageView);
        progressBar = rootView.findViewById(R.id.progressbar);
        downloadIV = rootView.findViewById(R.id.download);
        downloadIV.setOnClickListener(this);
        if (filePath != null && !"".equals(filePath)) {
            refreshPicture(filePath);
        } else {
            if (isAutoDownload) {
                progressBar.setVisibility(View.VISIBLE);
                downloadIV.setVisibility(View.GONE);
            } else {
                progressBar.setVisibility(View.GONE);
                downloadIV.setVisibility(View.VISIBLE);
            }
        }
        if (isVideo) {
            videoHint = rootView.findViewById(R.id.video_hint);
            videoHint.setVisibility(View.VISIBLE);
        }


        return rootView;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageView:
            case R.id.videoView:
                Intent intent = new Intent(getContext(), ZoomContentActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("filePath", filePath);
                bundle.putString("mimeType", mimeType);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case R.id.download:
                downloadIV.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                if (listener != null) listener.onDownloadRequest(position);
                break;
        }
    }

    public void refreshPicture(String filePath) {
        Glide.with(getContext())
                .load(new File(filePath))
                .into(imageView);
        imageView.setOnClickListener(this);
    }

    public void setListener(DownloadRequest listener) {
        this.listener = listener;
    }

    interface DownloadRequest {
        void onDownloadRequest(int position);
    }


}
