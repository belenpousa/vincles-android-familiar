package cat.bcn.vincles.mobile.UI.Chats;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.CallSuper;
import android.support.design.widget.TabLayout;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.UI.Chats.Model.ChatElement;
import cat.bcn.vincles.mobile.UI.Chats.Model.ChatMessage;
import cat.bcn.vincles.mobile.UI.Chats.Model.ChatMessageMedia;
import cat.bcn.vincles.mobile.UI.Contacts.Contact;
import cat.bcn.vincles.mobile.Utils.DateUtils;

import static cat.bcn.vincles.mobile.UI.Chats.Model.ChatElement.TYPE_ME_AUDIO_FIRST;
import static cat.bcn.vincles.mobile.UI.Chats.Model.ChatElement.TYPE_ME_TEXT;
import static cat.bcn.vincles.mobile.UI.Chats.Model.ChatElement.TYPE_USER_AUDIO_FIRST;
import static cat.bcn.vincles.mobile.UI.Chats.Model.ChatElement.TYPE_USER_TEXT;
import static java.security.AccessController.getContext;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.BaseViewHolder> implements ChatImagePagerAdapter.DownloadRequest {

    public static final int USER_ME = -1;

    FileRequest fileRequestListener;

    boolean isAutodownload;
    Context context;
    List<ChatElement> elementsList;
    ChatAdapterListener listener;
    private boolean deleteVisibility;
    SparseArray<Contact> users;

    HashMap<Integer, Integer> audioPlayPositions;
    HashMap<Integer, Integer> audioPlayDurations;
    int audioPlayingId = -1;
    String audioPath;
    int audioDuration;
    MediaPlayer mediaPlayer;
    AudioViewHolder playingAudioVh;
    CountDownTimer audioTimer;



    public class BaseViewHolder extends RecyclerView.ViewHolder {

        TextView message;


        public BaseViewHolder(View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.message);
        }

        @CallSuper
        public void fillInfo(ChatElement chatElement) {
            message.setText(chatElement.getText());
        }

    }

    public class CallAlertViewHolder extends BaseViewHolder {

        ImageView avatar;


        public CallAlertViewHolder(View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
        }

        @CallSuper
        public void fillInfo(ChatElement chatElement) {
            super.fillInfo(chatElement);
            setUserAvatarForId(avatar, ((ChatMessage) chatElement).getIdUserFrom());
        }

    }

    public class MessageViewHolder extends BaseViewHolder {
        ImageView avatar;
        TextView contactName;
        TextView time;
        View bubble;


        public MessageViewHolder(View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            contactName = itemView.findViewById(R.id.contact_name);
            time = itemView.findViewById(R.id.time);
            bubble = itemView.findViewById(R.id.bubble);
        }

        @Override
        public void fillInfo(ChatElement chatElement) {
            super.fillInfo(chatElement);
            ChatMessage chatMessage = (ChatMessage) chatElement;

            int id = chatMessage.getType() >= TYPE_ME_TEXT ? USER_ME : chatMessage.getIdUserFrom();
            contactName.setText(getUserNameForId(id, chatMessage.getFullNameUserSender()));
            setUserAvatarForId(avatar, id);


            time.setText(DateUtils.getFormattedHourMinutesFromMillis(time.getContext(), chatElement.getSendTime()));
            if (chatElement.getType() >= TYPE_ME_TEXT && chatElement.getType() <= TYPE_ME_AUDIO_FIRST) {
                Drawable background = bubble.getBackground();
                Drawable wrapDrawable = DrawableCompat.wrap(background);
                DrawableCompat.setTint(wrapDrawable, bubble.getContext().getResources()
                        .getColor(R.color.chat_bubble_grey_dark));
                bubble.setBackground(wrapDrawable);

                message.setTextColor(message.getContext().getResources().getColor(R.color.colorWhite));
            } else if (!((ChatMessage) chatElement).isWatched()
                    && chatElement.getType() >= TYPE_USER_TEXT
                    && chatElement.getType() <= TYPE_USER_AUDIO_FIRST) {
                Drawable background = bubble.getBackground();
                Drawable wrapDrawable = DrawableCompat.wrap(background);
                DrawableCompat.setTint(wrapDrawable, bubble.getContext().getResources()
                        .getColor(R.color.chat_bubble_pink));
                bubble.setBackground(wrapDrawable);
            } else {
                Drawable background = bubble.getBackground();
                Drawable wrapDrawable = DrawableCompat.wrap(background);
                DrawableCompat.setTint(wrapDrawable, bubble.getContext().getResources()
                        .getColor(R.color.chat_bubble_grey_light));
                bubble.setBackground(wrapDrawable);
            }
        }

    }

    public class ImageViewHolder extends MessageViewHolder implements ChatImagePagerAdapter.Callback {
        ViewPager viewPager;
        TabLayout tabLayout;


        public ImageViewHolder(View itemView) {
            super(itemView);
            viewPager = itemView.findViewById(R.id.pager);
            tabLayout = itemView.findViewById(R.id.tablayout);
        }

        @Override
        public void fillInfo(ChatElement chatElement) {
            super.fillInfo(chatElement);
            if (chatElement.getText() == null || chatElement.getText().length()<=0) {
                message.setVisibility(View.GONE);
            }

            ChatMessageMedia message = (ChatMessageMedia) chatElement;
            ChatImagePagerAdapter adapter = new ChatImagePagerAdapter(message.getMediaFiles(),
                    message.getIsVideo(), viewPager.getContext(), this,
                    (int) message.getId(), ChatAdapter.this);
            viewPager.setAdapter(adapter);
            if (adapter.getCount() < 2) {
                tabLayout.setVisibility(View.GONE);
            } else {
                tabLayout.setVisibility(View.VISIBLE);
                tabLayout.setupWithViewPager(viewPager, true);
            }
        }

        @Override
        public void onMediaClicked(String path, boolean isVideo) {
            listener.onChatElementMediaClicked(path, isVideo ? "video" : "image");
        }
    }

    public class AudioViewHolder extends MessageViewHolder {
        ImageView audioButton;
        ImageView download;
        ProgressBar progressBar;
        SeekBar seekBar;
        TextView time;
        int duration;


        public AudioViewHolder(View itemView) {
            super(itemView);
            audioButton = itemView.findViewById(R.id.play_iv);
            progressBar = itemView.findViewById(R.id.progressbar);
            seekBar = itemView.findViewById(R.id.seekbar);
            time = itemView.findViewById(R.id.proggress_time);
            download = itemView.findViewById(R.id.download);

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (seekBar.getTag() != null) {
                        int id = (int)seekBar.getTag();
                        audioPlayPositions.put(id, progress);
                        if (id == audioPlayingId && fromUser) {
                            mediaPlayer.seekTo(progress);
                            if(audioTimer!=null) {
                              audioTimer.cancel();
                            } else {
                              Toast.makeText(context, R.string.error_1001, Toast.LENGTH_SHORT).show();
                            }
                            createCountdownTimer(progress);
                        } else if (fromUser) {
                            if (audioPlayingId != -1) stopPlayingAudio();
                            if (!audioPlayDurations.containsKey(id)) {
                                for (ChatElement element : elementsList) {
                                    if (element instanceof ChatMessageMedia &&
                                            ((ChatMessageMedia) element).getId() == id) {
                                        String path = ((ChatMessageMedia) element).getMediaFiles().get(0);
                                        MediaPlayer mediaPlayer = new MediaPlayer();
                                        try {
                                            mediaPlayer.setDataSource(path);
                                            mediaPlayer.prepare();
                                            int duration = mediaPlayer.getDuration();
                                            audioPlayDurations.put(id, duration);
                                            setDuration(duration);
                                            setPlayPosition(progress*duration/100);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                          Toast.makeText(context, R.string.error_1001, Toast.LENGTH_SHORT).show();
                                        }
                                        break;
                                    }
                                }
                            } else {
                                setPlayPosition(progress);
                            }
                        }
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }

        public void setPlayPosition(int position) {
            seekBar.setProgress(position);
            time.setText(DateUtils.getFormatedTimeFromMillis(position));
        }

        public void setPausedState() {
            audioButton.setImageDrawable(audioButton.getContext().getResources()
                    .getDrawable(R.drawable.play));
        }

        public int getDuration() {
            return duration;
        }

        public void setDuration(int duration) {
            this.duration = duration;
            seekBar.setMax(duration);
        }

        @Override
        public void fillInfo(final ChatElement chatElement) {
            super.fillInfo(chatElement);
            if (chatElement.getText() == null || chatElement.getText().length()<=0) {
                message.setVisibility(View.GONE);
            }
            if (chatElement instanceof ChatMessageMedia) {
                final ChatMessageMedia chatMessageMedia = (ChatMessageMedia) chatElement;
                seekBar.setTag((int)chatMessageMedia.getId());
                int playPosition = 0;
                if (audioPlayPositions.containsKey((int)chatMessageMedia.getId())) {
                    playPosition = audioPlayPositions.get((int)chatMessageMedia.getId());
                }
                final String fileName = chatMessageMedia.getMediaFiles().get(0);
                if (fileName == null || fileName.length() == 0) {
                    if (isAutodownload) {
                        progressBar.setVisibility(View.VISIBLE);
                        audioButton.setVisibility(View.GONE);
                        download.setVisibility(View.GONE);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        audioButton.setVisibility(View.GONE);
                        download.setVisibility(View.VISIBLE);
                    }
                } else {
                    download.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                    audioButton.setVisibility(View.VISIBLE);
                    //setPlayPosition(playPosition);
                    Log.d("audch","cha id:"+chatMessageMedia.getId()+", audioPlaID:"+audioPlayingId+ "savedPosition:"+audioPlayPositions);
                    if (audioPlayPositions.containsKey((int)chatMessageMedia.getId()) &&
                            audioPlayDurations.containsKey((int)chatMessageMedia.getId())) {
                        setDuration(audioPlayDurations.get((int)chatMessageMedia.getId()));
                        Log.d("audch","setPos, pos:"+audioPlayPositions.get((int)chatMessageMedia.getId())+", dura:"+audioPlayDurations.get((int)chatMessageMedia.getId()));
                        setPlayPosition(playPosition);
                    }
                    if (chatMessageMedia.getId() == audioPlayingId) {
                        audioButton.setImageDrawable(audioButton.getContext().getResources()
                                .getDrawable(R.drawable.exo_controls_pause));
                        playingAudioVh = this;
                    } else {
                        audioButton.setImageDrawable(audioButton.getContext().getResources()
                                .getDrawable(R.drawable.play));
                    }

                    if (audioPlayingId == (int) chatMessageMedia.getId() && mediaPlayer == null) {
                        playingAudioVh = AudioViewHolder.this;
                        audioPath = chatMessageMedia.getMediaFiles().get(0);
                        playAudio();
                    }

                    audioButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            //TODO anything else to do?
                            if (chatMessageMedia.getId() == audioPlayingId) {
                                stopPlayingAudio();
                                //audioButton.setImageDrawable(v.getContext().getResources().getDrawable(R.drawable.play));
                            } else {
                                stopPlayingAudio(); //check if -1
                                playingAudioVh = AudioViewHolder.this;
                                audioButton.setImageDrawable(v.getContext().getResources().getDrawable(R.drawable.exo_controls_pause));
                                audioPlayingId = (int) chatMessageMedia.getId();
                                if (!audioPlayPositions.containsKey(audioPlayingId)) audioPlayPositions.put(audioPlayingId, 0);
                                audioPath = chatMessageMedia.getMediaFiles().get(0);
                                playAudio();
                            }
                        }
                    });
                }
                download.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        download.setVisibility(View.GONE);
                        progressBar.setVisibility(View.VISIBLE);
                        if (fileRequestListener != null) {
                            fileRequestListener.onFileRequest((int) chatMessageMedia.getId(), 0);
                        }
                    }
                });
            }
        }
    }

    public void stopPlayingAudio() {
        if (audioTimer != null) audioTimer.cancel();
        if (mediaPlayer != null) mediaPlayer.release();
        mediaPlayer = null;
        if (playingAudioVh != null) playingAudioVh.setPausedState();
        playingAudioVh = null;
        audioPlayingId = -1;
    }

    private void playAudio() {
        mediaPlayer = new MediaPlayer();
        try {
            File file = new File(audioPath);

            String extension = FilenameUtils.getExtension(audioPath);

            if(file.exists()==false || !extension.equals("aac")){
              Toast.makeText(context, R.string.error_audio_format, Toast.LENGTH_SHORT).show();
            }

            mediaPlayer.setDataSource(audioPath);
            mediaPlayer.prepare();
            audioPlayDurations.put(audioPlayingId, mediaPlayer.getDuration());
            if (playingAudioVh != null) {
                playingAudioVh.setDuration(mediaPlayer.getDuration());
            }
            int currentProgress = 0;
            if (audioPlayPositions.containsKey(audioPlayingId)) {
                currentProgress = audioPlayPositions.get(audioPlayingId);
            }
            mediaPlayer.seekTo(currentProgress);
            mediaPlayer.start();
            createCountdownTimer(currentProgress);

        } catch (IOException e) {
            Log.e("media_player", "prepare() failed");
        }
    }

    private void createCountdownTimer(int currentProgress) {
        audioTimer = new CountDownTimer(mediaPlayer.getDuration() - currentProgress,
                16) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (playingAudioVh != null) {
                    playingAudioVh.setPlayPosition((int) (playingAudioVh.getDuration()
                            - millisUntilFinished));
                }
            }

            @Override
            public void onFinish() {
                if (playingAudioVh != null) playingAudioVh.setPlayPosition(0);
                stopPlayingAudio();
                if (audioPlayPositions.containsKey(audioPlayingId)) {
                    audioPlayPositions.remove(audioPlayingId);
                }
            }
        }.start();
    }


    ChatAdapter(Context context, List<ChatElement> elementsList, ChatAdapterListener listener,
                SparseArray<Contact> users, Bundle savedState, FileRequest fileRequestListener){
        this.context = context;
        this.elementsList = elementsList;
        this.listener = listener;
        this.users = users;
        this.fileRequestListener = fileRequestListener;
        isAutodownload = new UserPreferences(context).getIsAutodownload();
        audioPlayPositions = new HashMap<>();
        audioPlayDurations = new HashMap<>();
        loadSavedState(savedState);
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("audioPlayingId", audioPlayingId);
        outState.putSerializable("audioPlayPositions", audioPlayPositions);
        outState.putSerializable("audioPlayDurations", audioPlayDurations);
        outState.putString("audioPath", audioPath);

        if (audioTimer != null) audioTimer.cancel();
        if (mediaPlayer != null) mediaPlayer.release();
        mediaPlayer = null;
    }

    private void loadSavedState(Bundle state) {
        if (state != null) {
            audioPlayingId = state.getInt("audioPlayingId");
            audioPlayPositions = (HashMap<Integer, Integer>) state.getSerializable("audioPlayPositions");
            audioPlayDurations = (HashMap<Integer, Integer>) state.getSerializable("audioPlayDurations");
            if (audioPlayPositions == null) audioPlayPositions = new HashMap<>();
            if (audioPlayDurations == null) audioPlayDurations = new HashMap<>();
            audioPath = state.getString("audioPath");
        }
    }

    @Override
    public int getItemViewType(int position) {
        return elementsList.get(position).getType();
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(context).inflate(getLayoutId(viewType), parent, false);

        switch (viewType) {
            case ChatElement.TYPE_ALERT_DATE: default:
                return new BaseViewHolder(v);
            case ChatElement.TYPE_ALERT_MISSED_CALL:
                return new CallAlertViewHolder(v);
            case ChatElement.TYPE_USER_TEXT:
            case ChatElement.TYPE_USER_TEXT_FIRST:
            case ChatElement.TYPE_ME_TEXT:
            case ChatElement.TYPE_ME_TEXT_FIRST:
                return new MessageViewHolder(v);
            case ChatElement.TYPE_USER_AUDIO:
            case ChatElement.TYPE_USER_AUDIO_FIRST:
            case ChatElement.TYPE_ME_AUDIO:
            case ChatElement.TYPE_ME_AUDIO_FIRST:
                return new AudioViewHolder(v);
            case ChatElement.TYPE_USER_IMAGE:
            case ChatElement.TYPE_USER_IMAGE_FIRST:
            case ChatElement.TYPE_ME_IMAGE:
            case ChatElement.TYPE_ME_IMAGE_FIRST:
                return new ImageViewHolder(v);
        }
    }

    private int getLayoutId(int viewType) {
        switch (viewType) {
            case ChatElement.TYPE_ALERT_DATE: default:
                return R.layout.chat_element_alert;
            case ChatElement.TYPE_ALERT_MISSED_CALL:
                return R.layout.chat_element_call_alert;
            case ChatElement.TYPE_USER_TEXT_FIRST:
                return R.layout.chat_element_user_text_first;
            case ChatElement.TYPE_USER_TEXT:
                return R.layout.chat_element_user_text;
            case ChatElement.TYPE_USER_AUDIO_FIRST:
                return R.layout.chat_element_user_audio_first;
            case ChatElement.TYPE_USER_AUDIO:
                return R.layout.chat_element_user_audio;
            case ChatElement.TYPE_ME_IMAGE:
                return R.layout.chat_element_me_image;
            case ChatElement.TYPE_ME_IMAGE_FIRST:
                return R.layout.chat_element_me_image_first;
            case ChatElement.TYPE_ME_TEXT_FIRST:
                return R.layout.chat_element_me_text_first;
            case ChatElement.TYPE_ME_TEXT:
                return R.layout.chat_element_me_text;
            case ChatElement.TYPE_ME_AUDIO_FIRST:
                return R.layout.chat_element_me_audio_first;
            case ChatElement.TYPE_ME_AUDIO:
                return R.layout.chat_element_me_audio;
            case ChatElement.TYPE_USER_IMAGE:
                return R.layout.chat_element_user_image;
            case ChatElement.TYPE_USER_IMAGE_FIRST:
                return R.layout.chat_element_user_image_first;
        }
    }

    @Override
    public void onBindViewHolder(final BaseViewHolder holder, int position) {
        holder.fillInfo(elementsList.get(position));
    }


    @Override
    public int getItemCount() {
        return elementsList.size();
    }

    public interface ChatAdapterListener {
        void onChatElementMediaClicked(String path, String mimeType);
    }

    private String getUserNameForId(int id, String fullname) {
        if (id == USER_ME) return context.getResources()
                .getString(R.string.chat_username_you);
        if (users != null) {
            Contact user = users.get(id);
            if (user != null) {
                return user.getName()+" "+user.getLastname();
            }
        }
        if (fullname != null && !fullname.isEmpty()) return fullname;

        return ""+id;
    }

    private void setUserAvatarForId(ImageView avatar, int id) {
        boolean avatarSet = false;
        if (users != null) {

            Contact user = users.get(id);
            if (user != null) {
                String path = user.getPath();
                Log.d("qwe","setAvatar, id:"+id+" path:"+path);
                if (path != null && path.length()>0 && !path.equals("placeholder")) {
                    path = path.replace("file://","");
                    Log.d("qwe","entra glide");
                    avatarSet = true;
                    Glide.with(avatar.getContext())
                            .load(new File(path))
                            .apply(new RequestOptions().overrideOf(128, 128)
                                    .centerCrop().diskCacheStrategy(DiskCacheStrategy.ALL))
                            .into(avatar);
                }
            }
        }
        if (!avatarSet) avatar.setImageDrawable(context.getResources().getDrawable(R.drawable.user));
    }

    public void setUsers(SparseArray<Contact> users) {
        this.users = users;
    }

    @Override
    public void onDownloadRequest(int messageId, int position) {
        if (fileRequestListener != null) fileRequestListener.onFileRequest(messageId, position);
    }

    interface FileRequest {
        void onFileRequest(int messageId, int position);
    }
}
