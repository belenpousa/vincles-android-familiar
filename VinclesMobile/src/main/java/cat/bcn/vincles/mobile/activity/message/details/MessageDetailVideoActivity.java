/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.activity.message.details;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.vo.Resource;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.activity.message.MessageDetailTemplateActivity;

public class MessageDetailVideoActivity extends MessageDetailTemplateActivity {
    private static final String TAG = "MessageAudioActivity";
    private VideoView video;
    private ProgressBar progressBar;
    private View imgPlay, imgPause, fraPlay, imgDownload;
    TextView titleTextProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        layout = R.layout.content_message_detail_video;
        super.onCreate(savedInstanceState);

        titleTextProgress = (TextView)findViewById(R.id.newmessage_title_progress);
        imgPlay = findViewById(R.id.imgPlay);
        imgPause = findViewById(R.id.imgPause);
        video = (VideoView) findViewById(R.id.video);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        fraPlay = findViewById(R.id.fraPlay);
        imgDownload = findViewById(R.id.imgDownload);
        imgDownload.setVisibility(View.GONE);
        isReady = false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        imgPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playVideo();
            }
        });
        imgPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playVideo();
            }
        });
        video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if (video.canSeekForward()) {
                    // Force video to show first still
                    video.start();
                    video.pause();
                    video.seekTo(0);
                    video.setBackground(null);
                    isReady = true;
                }
            }
        });
        video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                imgPlay.setVisibility(View.VISIBLE);
                fraPlay.setVisibility(View.GONE);
            }
        });
        video.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                playVideo();
                return false;
            }
        });

        // Load video from loca/server
        if (messageModel.currentMessage.getCurrentResource() != null) {
            if (checkResourceAlreadyDownloaded())
                prepareVideo();
            else {
                if (mainModel.downloads) downloadVideo(null);
                else {
                    imgPlay.setVisibility(View.GONE);
                    fraPlay.setVisibility(View.GONE);
                    imgDownload.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void prepareVideo() {
        String path = VinclesConstants.getVideoPath() + messageModel.currentMessage.getCurrentResource().filename;
        File file = new File(path);
        if (file.exists()) {
            video.setVideoPath(path);
            video.setVisibility(View.VISIBLE);
            fraPlay.setVisibility(View.VISIBLE);
            imgPlay.setVisibility(View.VISIBLE);
            fraPlay.setVisibility(View.GONE);
        }
        mainModel.hideBusy();
    }

    private void playVideo() {
        if (isReady) {
            if (video.isPlaying()) {
                video.pause();
                imgPlay.setVisibility(View.VISIBLE);
                fraPlay.setVisibility(View.GONE);
            } else {
                imgPlay.setVisibility(View.GONE);
                fraPlay.setVisibility(View.VISIBLE);
                video.start();
                showProgress();
            }
        }
    }

    private void showProgress() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    int current = 0;
                    while (video.isPlaying() && current < 100) {
                        int duration = video.getDuration();
                        if (duration > 0) {
                            current = ((video.getCurrentPosition() * 100) / duration) + 1;
                        } else {
                            current = 1;
                        }
                        setDurationToTextView(titleTextProgress, video.getCurrentPosition());
                        progressBar.setProgress(current);
                    }
                    progressBar.setProgress(current);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void setDurationToTextView(final TextView tv, final long duration) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0);
                cal.add(Calendar.SECOND, (int)(duration / 1000));
                tv.setText(VinclesConstants.getDateString(cal.getTime(), "mm:ss",
                        new Locale(getResources().getString(R.string.locale_language), getResources().getString(R.string.locale_country)))
                );
            }
        });

    }

    public void downloadVideo(View v) {
        if (checkResourceAlreadyDownloaded()) return;
        try {
            showLoadingDialog();
            messageModel.getServerResourceData(new AsyncResponse() {
                @Override
                public void onSuccess(Object result) {
                    // Update resource
                    Resource item = messageModel.currentMessage.getCurrentResource();
                    item.filename = VinclesConstants.VIDEO_PREFIX + new Date().getTime() + VinclesConstants.VIDEO_EXTENSION;
                    messageModel.saveResource(item);
                    VinclesConstants.saveVideo((byte[]) result, item.filename);
                    prepareVideo();
                    imgDownload.setVisibility(View.GONE);
                    stopDialog();
                }

                @Override
                public void onFailure(Object error) {
                    String errorMessage = mainModel.getErrorByCode(error);
                    stopDialog();
                    mainModel.showSimpleError(findViewById(R.id.main_content), getString(R.string.message_detail_loading_error), Snackbar.LENGTH_LONG);
                }
            }, messageModel.currentMessage.getCurrentResource().getId());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
