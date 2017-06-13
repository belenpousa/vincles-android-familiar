/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.activity.message.newfragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import java.io.File;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.vo.Resource;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.activity.message.MessageActivityNew;
import cat.bcn.vincles.mobile.model.MainModel;
import cat.bcn.vincles.mobile.model.MessageModel;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class MessageNewTextFragment extends Fragment implements IFragmentNewMessage {
    private MessageModel messageModel;
    private MainModel mainModel;
    private View rootView;
    private String currentImagePath;
    private MessageActivityNew activityNew;

    public static MessageNewTextFragment instance;

    public MessageNewTextFragment() {
        messageModel = MessageModel.getInstance();
        mainModel = MainModel.getInstance();
    }

    public static MessageNewTextFragment newInstance() {
        if (instance == null)
            instance = new MessageNewTextFragment();
        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_messagenew_text, container, false);
        rootView.findViewById(R.id.buttonAddPhoto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeGalleryTextPhoto();
            }
        });

        rootView.findViewById(R.id.buttonSendPhoto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send();
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh4ImageView();
    }


    public void takeGalleryTextPhoto() {
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, MessageActivityNew.RESULT_TEXT_LOAD_IMG);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MessageActivityNew.RESULT_TEXT_LOAD_IMG) {
            if (resultCode == getActivity().RESULT_OK) {
                if (messageModel.currentMessage.resourceTempList.size() >= 4) {
                    mainModel.showSimpleError(rootView, getString(R.string.message_error_maximage), Snackbar.LENGTH_LONG);
                } else {
                    if (data != null) {
                        try {
                            currentImagePath = mainModel.getRealImagePathFromURI(data.getData());
                            File imageFile = new File(currentImagePath);
                            RequestBody file = RequestBody.create(MediaType.parse("image/jpeg"), imageFile);

                            Resource resource = new Resource();
                            resource.filename = currentImagePath;
                            resource.data = MultipartBody.Part.createFormData("file", messageModel.currentMessage.getCurrentResource().filename, file);
                            messageModel.currentMessage.resourceTempList.add(resource);

                            refresh4ImageView();
                        } catch (Exception e) {
                            mainModel.showSimpleError(rootView, getString(R.string.message_error_attach_image), Snackbar.LENGTH_LONG);
                            e.printStackTrace();
                        }
                    }
                }
            } else if (resultCode == getActivity().RESULT_CANCELED) {
                // User cancelled the image capture
            } else {
                // Image capture failed, advise currentUser
            }
        }
    }

    @Override
    public void send() {
        Log.i(null, "sendText()");
        EditText ediMessage = (EditText) rootView.findViewById(R.id.ediMessage);
        String msg = ediMessage.getText().toString();
        if (!msg.isEmpty()) {
            messageModel.currentMessage.text = msg;
            messageModel.currentMessage.idUserFrom = mainModel.currentUser.getId();
            messageModel.currentMessage.idUserTo = mainModel.currentNetwork.userVincles.getId();
            messageModel.currentMessage.metadataTipus = VinclesConstants.RESOURCE_TYPE.TEXT_MESSAGE;

            activityNew = ((MessageActivityNew) getActivity());
            if (activityNew != null) {
                activityNew.showSendingDialog();
            }
            messageModel.sendMessage(new AsyncResponse() {
                @Override
                public void onSuccess(Object result) {
                    Log.i(null, "sendMessage() - result");
                    messageModel.saveMessage(messageModel.currentMessage);
                    if (activityNew != null) {
                        activityNew.stopDialog();
                        activityNew.finishWithOk();
                    }
                }

                @Override
                public void onFailure(Object error) {
                    Log.e(null, "sendMessage() - error: " + error);
                    if (getActivity() != null && activityNew != null) {
                        activityNew.showResendDialog(error);
                    }
                }
            }, messageModel.currentMessage);
        } else {
            mainModel.showSimpleError(rootView, getString(R.string.error_empty_field), Snackbar.LENGTH_LONG);
        }
    }

    private void refresh4ImageView() {
        messageModel.currentMessage.getResources();
        int layoutResId, imgResId, btnResId;
        for (int i = 1; i <= 4; i++) {
            layoutResId = getResources().getIdentifier("imageLayout" + i, "id", getActivity().getPackageName());
            imgResId = getResources().getIdentifier("imgMessage" + i, "id", getActivity().getPackageName());
            btnResId = getResources().getIdentifier("btnClose" + i, "id", getActivity().getPackageName());
            if (layoutResId > 0) {
                if (i <= messageModel.currentMessage.resourceTempList.size()) {
                    try {
                        rootView.findViewById(layoutResId).setVisibility(View.VISIBLE);
                        ImageView imgPhoto = (ImageView) rootView.findViewById(imgResId);
                        Glide.with(this)
                                .load(messageModel.currentMessage.resourceTempList.get(i - 1).filename)
                                .into(imgPhoto);

                        rootView.findViewById(btnResId).setTag(i - 1);
                        rootView.findViewById(btnResId).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                messageModel.currentMessage.resourceTempList.remove((int) v.getTag());
                                refresh4ImageView();
                            }
                        });
                    } catch (Exception e) {
                        Log.d(null, "LIST SIZE: " + messageModel.currentMessage.getResources().size());
                        Log.d(null, "I POSITION: " + i);
                        Log.d(null, "DIRECT MAPPED LIST: " + messageModel.currentMessage.resourceTempList);
                        Log.d(null, "DIRECT MAPPED LIST SIZE: " + messageModel.currentMessage.resourceTempList.size());
                        Log.d(null, "THROW AGAIN THE SAME ERROR HERE: " + messageModel.currentMessage.resourceTempList.get(i - 1));
                    }
                } else {
                    rootView.findViewById(layoutResId).setVisibility(View.GONE);
                }
            }
        }
    }
}