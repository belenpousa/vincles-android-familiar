/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.activity.message.newfragments;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.util.VinclesError;
import cat.bcn.vincles.lib.vo.Resource;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.activity.message.MessageActivityNew;
import cat.bcn.vincles.mobile.model.MainModel;
import cat.bcn.vincles.mobile.model.MessageModel;
import cat.bcn.vincles.mobile.util.VideoUtils;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class MessageNewVideoFragment extends Fragment implements IFragmentNewMessage {
    private MainModel mainModel;
    private MessageModel messageModel;
    private VideoView video;

    private View rootView;
    private View layoutButtonsNew, layoutButtonsPhoto, btnPlay, btnGallery, imgUserVideo;
    private Uri videoUri;
    private TextView titleDuration;

    public static MessageNewVideoFragment instance;

    public MessageNewVideoFragment() {
        mainModel = MainModel.getInstance();
        messageModel = MessageModel.getInstance();
    }

    public static MessageNewVideoFragment newInstance() {
        if (instance == null)
            instance = new MessageNewVideoFragment();
        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_messagenew_video, container, false);

        layoutButtonsPhoto = rootView.findViewById(R.id.newButtonsLayoutPhoto);
        layoutButtonsNew = rootView.findViewById(R.id.newButtonsLayout);
        titleDuration = (TextView)getActivity().findViewById(R.id.newmessage_title);
        btnGallery = rootView.findViewById(R.id.btnGallery);
        btnPlay = rootView.findViewById(R.id.btnPlay);
        video = (VideoView) rootView.findViewById(R.id.video);

        rootView.findViewById(R.id.togMessageRecord).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordVideo();
            }
        });
        rootView.findViewById(R.id.btnSendVideo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send();
            }
        });
        rootView.findViewById(R.id.btnDiscardVideo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                discardVideo();
            }
        });
        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getVideoGallery();
            }
        });
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (video != null) {
                    if (video.isPlaying()) {
                        btnPlay.setVisibility(View.VISIBLE);
                        video.stopPlayback();
                    } else {
                        btnPlay.setVisibility(View.GONE);
                        video.setVideoURI(videoUri);
                        video.start();
                    }
                }
            }
        });

        video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                btnPlay.setVisibility(View.VISIBLE);
            }
        });
        return rootView;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case MessageActivityNew.REQUEST_TAKE_GALLERY_VIDEO:
            case VinclesConstants.CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE:
                if (resultCode == getActivity().RESULT_OK) {
                    if (data != null) {
                        videoUri = data.getData();
                    }
                } else if (resultCode == getActivity().RESULT_CANCELED) {
                    // User cancelled the video capture
                    discardVideo();
                }
                refreshImage();
                break;
        }

        // CAUTION: restore current language (camera override it with device language default)
        mainModel.updateLocale(mainModel.language, mainModel.country);
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    private void refreshImage() {
        if (videoUri != null) {
            video.setVideoURI(videoUri);
            video.setOnPreparedListener(new MediaPlayer.OnPreparedListener()  {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    long duration = video.getDuration();
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0);
                    cal.add(Calendar.SECOND, (int)(duration / 1000));
                    titleDuration.setText(getString(R.string.duration) + " " + getString(R.string.message_new_title));
                    titleDuration.setText(VinclesConstants.getDateString(cal.getTime(), "mm:ss",
                            new Locale(getResources().getString(R.string.locale_language), getResources().getString(R.string.locale_country)))
                    );

                    // GOOD WAY TO PREVIEW AN IMAGE INTO VIDEVIEW
                    video.seekTo(10);

                }
            });
            layoutButtonsNew.setVisibility(View.GONE);
            layoutButtonsPhoto.setVisibility(View.VISIBLE);
            btnPlay.setVisibility(View.VISIBLE);
            video.setVisibility(View.VISIBLE);
        }
        else {
            titleDuration.setText(getString(R.string.message_new_title));
            layoutButtonsNew.setVisibility(View.VISIBLE);
            layoutButtonsPhoto.setVisibility(View.GONE);
            btnPlay.setVisibility(View.GONE);
            video.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshImage();
    }

    @Override
    public void send() {
        //run send in background for
        new Thread(new Runnable() {
            @Override
            public void run() {
                sendInternal();
            }
        }).start();
    }

    public void sendInternal() {
        Log.i(null, "sendVideo()");

        final MessageActivityNew messageActivityNew = (MessageActivityNew) getActivity();
        if (messageActivityNew != null) {
            messageActivityNew.showSendingDialog();
        }

        messageModel.currentMessage.idUserFrom = mainModel.currentUser.getId();
        messageModel.currentMessage.idUserTo = mainModel.currentNetwork.userVincles.getId();
        messageModel.currentMessage.metadataTipus = VinclesConstants.RESOURCE_TYPE.VIDEO_MESSAGE;

        // Load video from local system
        String videoPath = VideoUtils.getPath(getActivity(), videoUri);
        if (videoPath != null) {
            File videoFile = new File(videoPath);
            RequestBody file = RequestBody.create(MediaType.parse("video/mp4"), videoFile);
            Resource resource = new Resource();
            resource.filename = VinclesConstants.VIDEO_PREFIX + "_" + new Date().getTime();
            resource.data = MultipartBody.Part.createFormData("file", messageModel.currentMessage.getCurrentResource().filename, file);
            messageModel.currentMessage.resourceTempList.clear();
            messageModel.currentMessage.resourceTempList.add(resource);
            messageModel.currentMessage.sendTime = new Date();

            messageModel.sendMessage(new AsyncResponse() {
                @Override
                public void onSuccess(Object result) {
                    Log.e(null, "sendMessage() - result: " + result);

                    // First save new Message
                    messageModel.saveMessage(messageModel.currentMessage);

                    // Last save resource with message
                    for (Resource it : messageModel.currentMessage.resourceTempList) {
                        it.message = messageModel.currentMessage;
                        messageModel.saveResource(it);
                    }

                    if (messageActivityNew != null) {
                        messageActivityNew.stopDialog();
                        messageActivityNew.finishWithOk();
                    }
                }

                @Override
                public void onFailure(Object error) {
                    Log.e(null, "sendMessage() - error: " + error);
                    if (messageActivityNew != null) {
                        messageActivityNew.showResendDialog(error);
                    }
                }
            }, messageModel.currentMessage);
        } else {
            if (messageActivityNew != null) {
                messageActivityNew.showResendDialog(VinclesError.ERROR_FILE_NOT_FOUND);
            }
        }
    }

    public void discardVideo() {
        videoUri = null;
        refreshImage();
    }

    private void recordVideo() {
        Log.i(null, "recordVideo()");
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, VinclesConstants.VIDEO_QUALITY);
        intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, VinclesConstants.VIDEO_SIZE_LIMIT);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, VinclesConstants.VIDEO_DURATION_LIMIT);

        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(intent, VinclesConstants.CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);
        }
    }

    private void getVideoGallery() {
        Intent intent = new Intent();
        intent.setDataAndTypeAndNormalize(android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Video"), MessageActivityNew.REQUEST_TAKE_GALLERY_VIDEO);
    }

}