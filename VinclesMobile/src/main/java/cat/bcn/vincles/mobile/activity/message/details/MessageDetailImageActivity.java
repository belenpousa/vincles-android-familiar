/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.activity.message.details;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.Date;

import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.util.ImageUtils;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.vo.Resource;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.activity.message.MessageDetailTemplateActivity;

public class MessageDetailImageActivity extends MessageDetailTemplateActivity {
    private static final String TAG = "MessageAudioActivity";
    private ImageView imgTypeImage;
    private Bitmap bitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        layout = R.layout.content_message_detail_image;
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        imgTypeImage = (ImageView) findViewById(R.id.imgTypeImage);
        imgTypeImage.setVisibility(View.INVISIBLE);

        if (checkResourceAlreadyDownloaded()) {
            imgTypeImage.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(VinclesConstants.getImageDirectory() + "/" + messageModel.currentMessage.getCurrentResource().filename)
                    .error(R.drawable.user).placeholder(R.color.superlightgray)
                    .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                    .into(imgTypeImage);
        } else {
            if (mainModel.downloads)
                downloadImage(null);
            else
                imgTypeImage.setVisibility(View.VISIBLE);
        }
    }

    public void downloadImage(View v) {
        if (checkResourceAlreadyDownloaded()) return;
        try {
            showLoadingDialog();
            messageModel.getServerResourceData(new AsyncResponse() {
                @Override
                public void onSuccess(Object result) {
                    byte[] data = (byte[]) result;
                    imgTypeImage.setVisibility(View.VISIBLE);

                    // Update resource
                    Resource item = messageModel.currentMessage.getCurrentResource();
                    item.filename = VinclesConstants.IMAGE_PREFIX + new Date().getTime() + VinclesConstants.IMAGE_EXTENSION;
                    messageModel.saveResource(item);

                    // Save locally image or data
                    VinclesConstants.saveImage(data, item.filename);
                    stopDialog();

                    // Load image once has been saved
                    Glide.with(getApplicationContext())
                            .load(VinclesConstants.getImageDirectory() + "/" + item.filename)
                            .into(imgTypeImage);
                }

                @Override
                public void onFailure(Object error) {
                    imgTypeImage.setVisibility(View.VISIBLE);
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
