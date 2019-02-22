package cat.bcn.vincles.mobile.Client.Requests;


import android.content.Context;
import android.util.Log;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Business.Media;
import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Services.ChatService;
import cat.bcn.vincles.mobile.Utils.ImageUtils;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetGroupContentRequest extends BaseRequest implements Callback<ResponseBody> {

    ChatService chatService;
    List<OnResponse> onResponses = new ArrayList<>();
    String mimeType;
    String idChat, idMessage;
    ImageUtils imageUtils;
    Calendar c = Calendar.getInstance();
    Context context;

    public GetGroupContentRequest(RenewTokenFailed listener, Context context, String idChat,
                                  String idMessage, String mimeType) {
        super(listener, BaseRequest.AUTHENTICATED_REQUEST);
        this.idChat = idChat;
        this.idMessage = idMessage;
        this.mimeType = mimeType;
        imageUtils = new ImageUtils();
        this.context = context;
    }

    @Override
    public void doRequest(String accessToken) {
        authenticatedRequest(accessToken);
        chatService = retrofit.create(ChatService.class);
        Call<ResponseBody> call = chatService.getGroupContent(idChat, idMessage);
        call.enqueue(this);
    }

    public void addOnOnResponse(OnResponse onResponse) {
        onResponses.add(onResponse);
    }

    @Override
    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        if (!shouldRenewToken(this, response)) {
            for (OnResponse r : onResponses) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        InputStream is = response.body().byteStream();

                        long now = c.getTimeInMillis();
                        String imagePaht = "";
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
                        imagePaht = Media.saveFileImage(context,is,fileName);
                        Log.e("FILENAME: ", fileName);
                        r.onResponseGetGroupContentRequest(idChat, idMessage, imagePaht);
                    }
                } else {
                    String errorCode = ErrorHandler.parseError(response).getCode();
                    r.onFailureGetGroupContentRequest(errorCode);
                }
            }
        }
    }

    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {

    }


    public interface OnResponse {
        void onResponseGetGroupContentRequest(String idChat, String idMessage, String filePath);
        void onFailureGetGroupContentRequest(Object error);
    }
}
