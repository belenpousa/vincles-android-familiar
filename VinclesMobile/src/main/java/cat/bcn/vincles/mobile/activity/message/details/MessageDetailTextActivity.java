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
import java.util.Date;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.vo.Resource;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.activity.message.MessageDetailTemplateActivity;

public class MessageDetailTextActivity extends MessageDetailTemplateActivity {
    private static final String TAG = "MessageAudioActivity";
    private VideoView video;
    private ProgressBar progressBar;
    private View imgPlay, imgPause, fraPlay;
    TextView titleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        layout = R.layout.content_message_detail_video;
        super.onCreate(savedInstanceState);

        titleText = (TextView)findViewById(R.id.newmessage_title);
        imgPlay = findViewById(R.id.imgPlay);
        imgPause = findViewById(R.id.imgPause);
        video = (VideoView) findViewById(R.id.video);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        fraPlay = findViewById(R.id.fraPlay);
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
        mainModel.showBusy("Please wait...", "Loading video");
        if (messageModel.currentMessage.getCurrentResource() != null) {
            if (messageModel.currentMessage.getCurrentResource().filename == ""
                    || messageModel.currentMessage.getCurrentResource().filename == null) {
                try {
                    messageModel.getServerResourceData(new AsyncResponse() {
                        @Override
                        public void onSuccess(Object result) {
                            // Update resource
                            Resource item = messageModel.currentMessage.getCurrentResource();
                            item.filename = VinclesConstants.VIDEO_PREFIX + new Date().getTime() + VinclesConstants.VIDEO_EXTENSION;
                            messageModel.saveResource(item);
                            VinclesConstants.saveVideo((byte[]) result, item.filename);
                            prepareVideo();
                        }

                        @Override
                        public void onFailure(Object error) {
                            String errorMessage = mainModel.getErrorByCode(error);
                            mainModel.showSimpleError(findViewById(R.id.main_content), getString(R.string.message_detail_loading_error), Snackbar.LENGTH_LONG);
                        }
                    }, messageModel.currentMessage.getCurrentResource().getId());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                prepareVideo();
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
            imgPause.setVisibility(View.GONE);
        }
        mainModel.hideBusy();
    }

    private void playVideo() {
        if (isReady) {
            if (video.isPlaying()) {
                video.pause();
                imgPlay.setVisibility(View.VISIBLE);
            } else {
                imgPlay.setVisibility(View.GONE);
                video.start();
                showProgress();
            }
        }
    }

    private void showProgress() {
        new Thread(new Runnable() {
            public void run() {
                int current = 0;
                while (video.isPlaying() && current < 100) {
                    current = ((video.getCurrentPosition() * 100) / video.getDuration()) + 1;
                    progressBar.setProgress(current);
                }
                progressBar.setProgress(current);
            }
        }).start();
    }
}
