package cat.bcn.vincles.mobile.UI.Chats;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.UI.Gallery.ZoomContentActivity;

public class ChatImagePagerAdapter extends PagerAdapter {

    boolean isAutoDownload;
    int messageId;
    private List<String> imagePathsList;
    private List<Boolean> isVideoList;
    Context context;
    Callback listener;
    DownloadRequest downloadRequestListener;

    public ChatImagePagerAdapter(List<String> imagePathsList, List<Boolean> isVideoList,
                                 Context context, Callback listener, int messageId,
                                 DownloadRequest downloadRequestListener) {
        super();
        this.imagePathsList = imagePathsList;
        this.isVideoList = isVideoList;
        this.context = context;
        this.listener = listener;
        this.messageId = messageId;
        this.downloadRequestListener = downloadRequestListener;
        isAutoDownload = new UserPreferences(context).getIsAutodownload();
    }


    @Override
    public int getCount() {
        return imagePathsList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup collection, final int position) {
        LayoutInflater inflater = LayoutInflater.from(context);
        final ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.chat_element_image_adapter_item, collection, false);
        collection.addView(layout);

        final String path = imagePathsList.get(position);
        final Boolean isVideo = isVideoList.get(position);
        Log.d("imgpag","instantiate item, path:"+path);
        ImageView imageView = layout.findViewById(R.id.imageview);
        final ImageView download = layout.findViewById(R.id.download);
        if (path != null && path.length()>0 && !path.equals("placeholder")) {
            layout.findViewById(R.id.progressbar).setVisibility(View.GONE);
            RequestOptions options = new RequestOptions();
            options.centerCrop();
            options.override(200,200);
            Glide.with(context)
                    .load(path)
                    .apply(options)
                    .into(imageView);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onMediaClicked(path, isVideo);
                }
            });
        } else if (path!= null && path.length()>0 && path.equals("placeholder")) {
            imageView.setImageDrawable(layout.getContext()
                    .getResources().getDrawable(R.drawable.user));
            layout.findViewById(R.id.progressbar).setVisibility(View.GONE);
            imageView.setOnClickListener(null);
        } else {
            if (isAutoDownload) {
                layout.findViewById(R.id.progressbar).setVisibility(View.VISIBLE);
                imageView.setOnClickListener(null);
                download.setVisibility(View.GONE);
            } else {
                layout.findViewById(R.id.progressbar).setVisibility(View.GONE);
                download.setVisibility(View.VISIBLE);
                download.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        layout.findViewById(R.id.progressbar).setVisibility(View.VISIBLE);
                        download.setVisibility(View.GONE);
                        if (downloadRequestListener != null) {
                            downloadRequestListener.onDownloadRequest(messageId, position);
                        }
                    }
                });
            }
        }

        ImageView videoHintIV = layout.findViewById(R.id.video_hint);
        videoHintIV.setVisibility(isVideoList.get(position) ? View.VISIBLE : View.GONE);

        return layout;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup collection, int position, @NonNull Object view) {
        collection.removeView((View) view);
    }

    public interface Callback {
        void onMediaClicked(String path, boolean isVideo);
    }

    interface DownloadRequest {
        void onDownloadRequest(int messageId, int position);
    }

}
