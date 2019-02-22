package cat.bcn.vincles.mobile.Client.Requests;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Services.ChatService;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeleteMessageRequest extends BaseRequest implements Callback<ResponseBody> {

    ChatService chatService;
    List<OnResponse> onResponses = new ArrayList<>();
    private String idMessage;

    public DeleteMessageRequest(RenewTokenFailed listener, String idMessage) {
        super(listener, BaseRequest.AUTHENTICATED_REQUEST);
        this.idMessage = idMessage;
    }

    @Override
    public void doRequest(String accessToken) {
        authenticatedRequest(accessToken);
        chatService = retrofit.create(ChatService.class);
        Call<ResponseBody> call = chatService.deleteMessage(idMessage);
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
                    r.onResponseDeleteMessageRequest(response.body());
                } else {
                    String errorCode = ErrorHandler.parseError(response).getCode();
                    r.onFailureDeleteMessageRequest(errorCode);
                }
            }
        }
    }

    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {
        for (OnResponse r : onResponses) {
            r.onFailureDeleteMessageRequest(new Exception(t));
        }
    }

    public interface OnResponse {
        void onResponseDeleteMessageRequest(ResponseBody chatMessageRestList);
        void onFailureDeleteMessageRequest(Object error);
    }
}