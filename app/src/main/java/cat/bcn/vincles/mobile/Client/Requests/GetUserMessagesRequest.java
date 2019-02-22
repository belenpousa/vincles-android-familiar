package cat.bcn.vincles.mobile.Client.Requests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Model.ChatMessageRestList;
import cat.bcn.vincles.mobile.Client.Services.ChatService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetUserMessagesRequest extends BaseRequest implements Callback<ChatMessageRestList> {

    private static String FROM = "from";
    private static String TO = "to";

    ChatService chatService;
    List<OnResponse> onResponses = new ArrayList<>();
    String idUserSender;
    Map<String, String> params;

    public GetUserMessagesRequest(RenewTokenFailed listener, String idUserSender, long timeStampStart, long timeStampEnd) {
        super(listener, BaseRequest.AUTHENTICATED_REQUEST);
        this.params = new HashMap<>();
        if (timeStampStart != 0) {
            params.put(FROM, String.valueOf(timeStampStart));
        }
        if (timeStampEnd != 0) {
            params.put(TO, String.valueOf(timeStampEnd));
        }
        this.idUserSender = idUserSender;
    }

    @Override
    public void doRequest(String accessToken) {
        authenticatedRequest(accessToken);
        chatService = retrofit.create(ChatService.class);
        Call<ChatMessageRestList> call = chatService.getUserMessages(idUserSender, params);
        call.enqueue(this);
    }

    public void addOnOnResponse(OnResponse onResponse) {
        onResponses.add(onResponse);
    }

    @Override
    public void onResponse(Call<ChatMessageRestList> call, Response<ChatMessageRestList> response) {
        if (!shouldRenewToken(this, response)) {
            for (OnResponse r : onResponses) {
                if (response.isSuccessful()) {
                    r.onResponseGetUserMessagesRequest(response.body(), idUserSender);
                } else {
                    String errorCode = ErrorHandler.parseError(response).getCode();
                    r.onFailureGetUserMessagesRequest(errorCode, idUserSender);
                }
            }
        }
    }

    @Override
    public void onFailure(Call<ChatMessageRestList> call, Throwable t) {
        for (OnResponse r : onResponses) {
            r.onFailureGetUserMessagesRequest(new Exception(t), idUserSender);
        }
    }

    public interface OnResponse {
        void onResponseGetUserMessagesRequest(ChatMessageRestList chatMessageRestList,
                                              String idUserSender);
        void onFailureGetUserMessagesRequest(Object error, String idUserSender);
    }
}
