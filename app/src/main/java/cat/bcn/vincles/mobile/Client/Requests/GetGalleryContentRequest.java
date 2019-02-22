package cat.bcn.vincles.mobile.Client.Requests;


import android.content.Context;
import android.util.Log;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Business.Media;
import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Services.GalleryService;
import cat.bcn.vincles.mobile.Utils.ImageUtils;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetGalleryContentRequest extends BaseRequest implements Callback<ResponseBody> {

    GalleryService galleryService;
    List<OnResponseBase> onResponses = new ArrayList<>();
    String contentID;
    String mimeType;
    ImageUtils imageUtils;
    Calendar c = Calendar.getInstance();
    Context context;
    long messageID = -1;

    public GetGalleryContentRequest(RenewTokenFailed listener, Context context, String contentID, String mimeType) {
        //super(BaseRequest.AUTHENTICATED_REQUEST, accesToken);
        super(listener, BaseRequest.AUTHENTICATED_REQUEST);
        this.contentID = contentID;
        this.mimeType = mimeType;
        //galleryService = retrofit.create(GalleryService.class);
        imageUtils = new ImageUtils();
        this.context = context;
    }

    @Override
    public void doRequest(String accessToken) {
        authenticatedRequest(accessToken);
        galleryService = retrofit.create(GalleryService.class);
        Call<ResponseBody> call = galleryService.getContent(contentID);
        call.enqueue(this);
    }

    public void setMessageID(long messageID) {
        this.messageID = messageID;
    }

    public void addOnOnResponse(OnResponseBase onResponse) {
        onResponses.add(onResponse);
    }

    @Override
    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        if (!shouldRenewToken(this, response)) {
            for (OnResponseBase r : onResponses) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        InputStream is = response.body().byteStream();

                        c = Calendar.getInstance();
                        long now = c.getTimeInMillis();
                        String imagePath = "";
                        String fileName = "";
                        if (mimeType == null || mimeType.equals("")) {
                            mimeType = response.headers().get("Content-Type");
                        }
                        switch (mimeType) {
                            case "image/jpeg":
                                fileName = String.valueOf(now) + ".jpeg";
                                break;
                            case "image/png":
                                fileName = String.valueOf(now) + ".png";
                                break;
                            case "video/mp4" :
                                fileName = String.valueOf(now) + ".mp4";
                                break;
                            case "audio/aac":
                                fileName = String.valueOf(now) + ".aac";
                                break;
                        }
                        imagePath = Media.saveFileImage(context,is,fileName);
                        Log.d("galchng","request, saveFile, imagePath:"+imagePath);
                        Log.e("FILENAME: ", fileName);
                        if (r instanceof OnResponse) {
                            ((OnResponse) r).onResponseGetGalleryContentRequest(contentID, imagePath);
                        } else {
                            ((OnResponseMessage)r).onResponseGetGalleryContentRequest(contentID,
                                    imagePath, messageID, mimeType);
                        }
                    }
                } else {
                    String errorCode = ErrorHandler.parseError(response).getCode();
                    r.onFailureGetGalleryContentRequest(errorCode);
                }
            }
        }
    }

    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {

    }


    public interface OnResponseBase {
        void onFailureGetGalleryContentRequest(Object error);
    }

    public interface OnResponse extends OnResponseBase {
        void onResponseGetGalleryContentRequest(String contentID, String filePath);
    }

    public interface OnResponseMessage extends OnResponseBase {
        void onResponseGetGalleryContentRequest(String contentID, String filePath, long messageId,
                                                String mimeType);
    }
}
