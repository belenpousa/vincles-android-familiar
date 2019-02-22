package cat.bcn.vincles.mobile.UI.Gallery;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Db.Model.GalleryContentRealm;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.Utils.ImageUtils;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder>{

    Context context;
    UserPreferences userPreferences;
    List<OnItemClicked> onItemClickedListeners = new ArrayList<>();
    private List<Integer> itemsSelected;
    private boolean isInSelectedMode;
    List<GalleryContentRealm> galleryContents;
    private GalleryAdapterListener listener;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        ImageView imageDetail;
        ImageView seletected;
        ImageView videoHint;
        ProgressBar progressBar;

        ViewHolder(View v){
            super(v);
            imageDetail = v.findViewById(R.id.image);
            seletected = v.findViewById(R.id.selected);
            progressBar = v.findViewById(R.id.progressbar);
            videoHint = v.findViewById(R.id.video_hint);

            imageDetail.setOnClickListener(this);
            imageDetail.setOnLongClickListener(this);

        }

        @Override
        public void onClick(View view) {
          try{
            if (view == videoHint && (view.getTag() != null
              && view.getTag() instanceof Boolean && !(boolean)view.getTag())) {
              listener.needGalleryContent(galleryContents.get(getAdapterPosition()).getId(),
                galleryContents.get(getAdapterPosition()).getIdContent());
              videoHint.setVisibility(View.GONE);
              progressBar.setVisibility(View.VISIBLE);
            } else {
              if (isInSelectedMode) {
                onSelectItem();
              } else {
                onViewItem();
              }
            }
          }catch(Exception e){
            System.out.println("Error " + e.getMessage());
          }
        }

        @Override
        public boolean onLongClick(View view) {
            onSelectItem();
            return true;
        }

        private void onViewItem() {
          try{
            for (int i = 0; i < onItemClickedListeners.size(); i++) {
              int position = getAdapterPosition();
              GalleryContentRealm galleryContentRealm = galleryContents.get(position);
              onItemClickedListeners.get(i).onViewItem(galleryContentRealm, position);
            }
          }catch(Exception e){
            System.out.println("Error " + e.getMessage());
          }
        }

        public void onSelectItem() {
          try{
            for (int i = 0; i < onItemClickedListeners.size(); i++) {
              int position = getAdapterPosition();
              GalleryContentRealm galleryContentRealm = galleryContents.get(position);
              onItemClickedListeners.get(i).onSelectItem(galleryContentRealm, position);
            }
            Drawable.ConstantState selectedImageState = context.getResources().getDrawable(R.drawable.imatge_selected).getConstantState();
            Drawable.ConstantState actualState = seletected.getDrawable().getConstantState();

            Drawable unselectedImage = context.getResources().getDrawable(R.drawable.image_unselected);
            Drawable selectedImage = context.getResources().getDrawable(R.drawable.imatge_selected);
            Drawable stateBackground = actualState.equals(selectedImageState) ? unselectedImage : selectedImage;

            seletected.setImageDrawable(stateBackground);
          }catch(Exception e){
            System.out.println("Error " + e.getMessage());
          }

        }

    }

    public void emptyItemSelecteds() {
        itemsSelected.clear();
    }

    GalleryAdapter(Context context, List<GalleryContentRealm> galleryContents,
                   List<Integer> itemsSelected, boolean isInSelectedMode, GalleryAdapterListener listener){
        this.context = context;
        this.galleryContents = galleryContents;
        this.itemsSelected = itemsSelected;
        this.isInSelectedMode = isInSelectedMode;
        this.listener = listener;
        this.userPreferences = new UserPreferences(context);
    }

    public void setGalleryContents(List<GalleryContentRealm> galleryContents) {
        this.galleryContents = galleryContents;
    }

    public void setInSelectedMode(boolean inSelectedMode) {
        isInSelectedMode = inSelectedMode;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.gallery_adapter_item,parent,false);
        ViewHolder vh = new ViewHolder(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String filePath = galleryContents.get(position).getPath();
        if (filePath != null && !"".equals(filePath)) {
            File file = new File(filePath);
            Uri imageUri = Uri.fromFile(file);
            if (!imageUri.toString().equals("file:///")) {
                holder.videoHint.setTag(true);
                holder.imageDetail.setVisibility(View.VISIBLE);
                holder.progressBar.setVisibility(View.GONE);

                String mimeType = galleryContents.get(position).getMimeType();
                if (mimeType != null && mimeType.startsWith("video")) {
                    holder.videoHint.setImageDrawable(holder.videoHint.getResources()
                            .getDrawable(R.drawable.video_hint));
                    holder.videoHint.setVisibility(View.VISIBLE);
                    holder.videoHint.setOnClickListener(holder);

                    Log.d("cdecsios","mimetype video, pos:"+position+" path:"+imageUri.getPath());
                    Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(imageUri.getPath(),
                            MediaStore.Images.Thumbnails.MINI_KIND);
                    Log.d("cdecsios","can read file nul?"+(new File(imageUri.getPath()).canRead()));
                    /*if (position == 0) {
                        ImageUtils.saveMediaExternalMemory(holder.imageDetail.getContext(),
                                "videoIos.mp4", imageUri.getPath());
                    } else if (position == 3) {
                        ImageUtils.saveMediaExternalMemory(holder.imageDetail.getContext(),
                                "videoAndroid.mp4", imageUri.getPath());
                    }*/

                    /*MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    retriever.setDataSource(imageUri.getPath());
                    Bitmap thumbnail = retriever.getFrameAtTime(100, MediaMetadataRetriever.OPTION_CLOSEST);*/

                    holder.imageDetail.setImageDrawable(new BitmapDrawable(holder.imageDetail.getResources(), thumbnail));
                } else {
                    Log.d("cdecsios","mimetype photo, pos:"+position);
                    holder.videoHint.setVisibility(View.GONE);
                    Glide.with(this.context)
                            .load(imageUri)
                            .apply(RequestOptions.overrideOf(200, 200))
                            .into(holder.imageDetail);
                }
            } else {
                holder.videoHint.setTag(false);
                if (userPreferences.getIsAutodownload()) {
                    holder.progressBar.setVisibility(View.VISIBLE);
                    holder.videoHint.setVisibility(View.GONE);
                } else {
                    holder.videoHint.setOnClickListener(holder);
                    holder.videoHint.setVisibility(View.VISIBLE);
                    holder.videoHint.setImageDrawable(holder.videoHint.getResources()
                            .getDrawable(R.drawable.download));
                }
            }
            Drawable selectedImage;
            if (itemsSelected.contains(galleryContents.get(position).getId())) {
                selectedImage = context.getResources().getDrawable(R.drawable.imatge_selected);
            } else {
                selectedImage = context.getResources().getDrawable(R.drawable.image_unselected);
            }
            holder.seletected.setImageDrawable(selectedImage);
            holder.seletected.setVisibility(isInSelectedMode ? View.VISIBLE : View.GONE);
        } else {
            holder.videoHint.setTag(false);
            holder.imageDetail.setImageDrawable(null);
            if (listener != null) {
                if (userPreferences.getIsAutodownload()) {
                    holder.progressBar.setVisibility(View.VISIBLE);
                    holder.imageDetail.setVisibility(View.INVISIBLE);
                    listener.needGalleryContent(galleryContents.get(position).getId(),
                            galleryContents.get(position).getIdContent());
                } else {
                    holder.videoHint.setOnClickListener(holder);
                    holder.videoHint.setVisibility(View.VISIBLE);
                    holder.videoHint.setImageDrawable(holder.videoHint.getResources()
                            .getDrawable(R.drawable.download));
                }

            }
        }
    }

    @Override
    public int getItemCount() {
        return galleryContents.size();
    }

    public interface GalleryAdapterListener {
        void needGalleryContent(int id, int idContent);
    }

    public void addItemClickedListeners(OnItemClicked onItemClicked) {
        onItemClickedListeners.add(onItemClicked);
    }

    public interface OnItemClicked {
        void onViewItem(GalleryContentRealm galleryContentRealm, int index);
        void onSelectItem(GalleryContentRealm galleryContentRealm, int index);
    }
}
