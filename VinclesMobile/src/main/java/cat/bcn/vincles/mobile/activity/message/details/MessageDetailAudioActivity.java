/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.activity.message.details;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.vo.Resource;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.activity.message.MessageDetailTemplateActivity;

public class MessageDetailAudioActivity extends MessageDetailTemplateActivity {
    private static final String TAG = "MessageAudioActivity";
    private MediaPlayer mediaPlayer;
    private ProgressBar progressBar;
    private View imgPlay, imgPause, imgDownload, audioImage, audioImageVoid, fraPlay;
    TextView titleTextProgress;
    private Boolean exit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        layout = R.layout.content_message_detail_audio;
        super.onCreate(savedInstanceState);

        titleTextProgress = (TextView)findViewById(R.id.newmessage_title_progress);
        imgPlay = findViewById(R.id.imgPlay);
        imgPause = findViewById(R.id.imgPause);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        audioImage = findViewById(R.id.audio_image);
        audioImageVoid = findViewById(R.id.audio_image_void);
        fraPlay = findViewById(R.id.fraPlay);
        imgDownload = findViewById(R.id.audio_download);
        imgDownload.setVisibility(View.GONE);
        isReady = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mediaPlayer = new MediaPlayer();
        imgPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAudio();
            }
        });
        imgPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAudio();
            }
        });
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                isReady = true;
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                resetAudioControl();
            }
        });

        // Load audio from loca/server
        if (messageModel.currentMessage.getCurrentResource() != null) {
            if (checkResourceAlreadyDownloaded()) {
                prepareAudio();
            } else {
                if (mainModel.downloads) downloadAudio(null);
                else {
                    fraPlay.setVisibility(View.GONE);
                    audioImageVoid.setVisibility(View.GONE);
                    imgDownload.setVisibility(View.VISIBLE);
                }
            }
        }

        resetAudioControl();
    }

    private void prepareAudio() {
        audioImage.setVisibility(View.VISIBLE);
        audioImageVoid.setVisibility(View.GONE);

        String path = VinclesConstants.getAudioPath() + messageModel.currentMessage.getCurrentResource().filename;
        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
        } catch (IOException e) {
            Log.e(TAG, "mediaPlayer.start() - error: " + e);
        }
        mainModel.hideBusy();
    }

    private void playAudio() {
        if (isReady) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                imgPause.setVisibility(View.GONE);
                imgPlay.setVisibility(View.VISIBLE);
            } else {
                imgPlay.setVisibility(View.GONE);
                imgPause.setVisibility(View.VISIBLE);
                mediaPlayer.start();
                showProgress();
            }
        }
    }

    @Override
    protected void onPause() {
        stopAudio();
        super.onPause();
    }

    private void stopAudio() {
        exit = true;
        mediaPlayer.stop();
        resetAudioControl();
    }

    private void resetAudioControl() {
        imgPlay.setVisibility(View.VISIBLE);
        imgPause.setVisibility(View.GONE);
        progressBar.setProgress(0);
        titleTextProgress.setText("00:00");
    }

    private void showProgress() {
        exit = false;
        new Thread(new Runnable() {
            public void run() {
                int current = 0;
                while (!exit && mediaPlayer.isPlaying() && current < 100) {
                    current = ((mediaPlayer.getCurrentPosition() * 100) / mediaPlayer.getDuration()) + 1;
                    setDurationToTextView(titleTextProgress, mediaPlayer.getCurrentPosition());
                    progressBar.setProgress(current);
                }
                if (!exit) {
                    progressBar.setProgress(current);
                } else {
                    progressBar.setProgress(0);
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

    public void downloadAudio(View v) {
        if (checkResourceAlreadyDownloaded()) return;
        try {
            showLoadingDialog();
            messageModel.getServerResourceData(new AsyncResponse() {
                @Override
                public void onSuccess(Object result) {
                    // Update resource
                    Resource item = messageModel.currentMessage.getCurrentResource();
                    item.filename = VinclesConstants.AUDIO_PREFIX + new Date().getTime() + VinclesConstants.AUDIO_EXTENSION;
                    messageModel.saveResource(item);
                    VinclesConstants.saveAudio((byte[]) result, item.filename);
                    prepareAudio();
                    fraPlay.setVisibility(View.VISIBLE);
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
