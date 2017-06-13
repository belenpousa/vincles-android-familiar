/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.activity.message.newfragments;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import java.io.File;
import java.util.Date;
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

public class MessageNewPhotoFragment extends Fragment implements IFragmentNewMessage {
    private MainModel mainModel;
    private MessageModel messageModel;
    private View rootView;

    private View layoutButtonsNew, layoutButtonsPhoto, photoRemoveWidget;
    private ImageView imgPhoto;
    private String currentFilename;
    private String currentImagePath;
    private boolean isFromGallery;

    public static MessageNewPhotoFragment instance;

    public MessageNewPhotoFragment() {
        mainModel = MainModel.getInstance();
        messageModel = MessageModel.getInstance();
    }

    public static MessageNewPhotoFragment newInstance() {
        if (instance == null)
            instance = new MessageNewPhotoFragment();
        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_messagenew_photo, container, false);

        layoutButtonsPhoto = rootView.findViewById(R.id.newButtonsLayoutPhoto);
        layoutButtonsNew = rootView.findViewById(R.id.newButtonsLayout);
        photoRemoveWidget =rootView.findViewById(R.id.imgPhotoRemove);
        imgPhoto = (ImageView) rootView.findViewById(R.id.imgPhoto);

        rootView.findViewById(R.id.btnGallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeGalleryPhoto();
            }
        });
        rootView.findViewById(R.id.btnTakePhoto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });
        rootView.findViewById(R.id.btnDiscardImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                discardImage();
            }
        });
        rootView.findViewById(R.id.btnSendImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send();
            }
        });
        photoRemoveWidget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                discardImage();
            }
        });
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshImage();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VinclesConstants.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == getActivity().RESULT_OK) {
                isFromGallery = false;

                refreshImage();
            } else if (resultCode == getActivity().RESULT_CANCELED) {
                // User cancelled the image capture
                discardImage();
            } else {
                // Image capture failed, advise currentUser
            }
        } else if (requestCode == MessageActivityNew.RESULT_LOAD_IMG) {
            if (resultCode == getActivity().RESULT_OK) {
                if (data != null) {
                    try {
                        currentImagePath = mainModel.getRealImagePathFromURI(data.getData());
                        currentFilename = currentImagePath;
                        isFromGallery = true;

                        refreshImage();
                    } catch (Exception e) {
                        mainModel.showSimpleError(rootView, getString(R.string.message_error_attach_image), Snackbar.LENGTH_LONG);
                        e.printStackTrace();
                    }
                }
            } else if (resultCode == getActivity().RESULT_CANCELED) {
                // User cancelled the image capture
                discardImage();
            } else {
                // Image capture failed, advise currentUser
            }
        }

        // CAUTION: restore current language (camera override it with device language default)
        mainModel.updateLocale(mainModel.language, mainModel.country);
    }

    private void refreshImage() {
        if (currentFilename != null && !currentFilename.equals("")) {
            layoutButtonsNew.setVisibility(View.GONE);
            layoutButtonsPhoto.setVisibility(View.VISIBLE);
            photoRemoveWidget.setVisibility(View.VISIBLE);

            // Load from glide
            String path = VinclesConstants.getImageDirectory() + "/" + currentFilename;
            if (isFromGallery) {
                path = currentImagePath;
            }
            Glide.with(this)
                    .load(path)
                    .into(imgPhoto);
        }
        else {
            layoutButtonsNew.setVisibility(View.VISIBLE);
            layoutButtonsPhoto.setVisibility(View.GONE);
            photoRemoveWidget.setVisibility(View.GONE);
            imgPhoto.setImageResource(android.R.color.transparent);
        }
    }

    public void takePhoto() {
        if(ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // Indicate file uri to save
            currentFilename = VinclesConstants.IMAGE_PREFIX + new Date().getTime() + VinclesConstants.IMAGE_EXTENSION;
            File currentImageFile = new File(VinclesConstants.getImagePath(), currentFilename);
            currentImagePath = currentImageFile.getAbsolutePath();
            Uri currentImageUri = Uri.fromFile(currentImageFile);

            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, currentImageUri);
            startActivityForResult(intent, VinclesConstants.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA}, MessageActivityNew.REQUEST_CAMERA_RESULT);
            }
        }
    }

    public void takeGalleryPhoto() {
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, MessageActivityNew.RESULT_LOAD_IMG);
    }

    public void discardImage() {
        Log.i(null, "onDiscardImage()");
        currentFilename = null;
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
        if (currentFilename == null || currentFilename.equals("")) {
            mainModel.showSimpleError(rootView, getString(R.string.message_error_image), Snackbar.LENGTH_LONG);
            return;
        }

        if (getActivity() != null)
            ((MessageActivityNew)getActivity()).showSendingDialog();

        messageModel.currentMessage.idUserFrom = mainModel.currentUser.getId();
        messageModel.currentMessage.idUserTo = mainModel.currentNetwork.userVincles.getId();
        messageModel.currentMessage.metadataTipus = VinclesConstants.RESOURCE_TYPE.IMAGES_MESSAGE;

        File imageFile = new File(currentImagePath);
        RequestBody file = RequestBody.create(MediaType.parse("image/jpeg"), imageFile);
        Resource resource = new Resource();
        resource.filename = VinclesConstants.IMAGE_PREFIX + "_" + new Date().getTime();

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

                if ((MessageActivityNew)getActivity() != null) {
                    ((MessageActivityNew) getActivity()).stopDialog();
                    ((MessageActivityNew) getActivity()).finishWithOk();
                }
            }

            @Override
            public void onFailure(Object error) {
                Log.e(null, "sendMessage() - error: " + error);
                ((MessageActivityNew)getActivity()).showResendDialog(error);
            }
        }, messageModel.currentMessage);
    }
}