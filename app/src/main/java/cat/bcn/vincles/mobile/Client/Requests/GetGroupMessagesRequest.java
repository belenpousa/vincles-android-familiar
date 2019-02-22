package cat.bcn.vincles.mobile.Client.Requests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Model.ChatMessageRestList;
import cat.bcn.vincles.mobile.Client.Model.GroupMessageRestList;
import cat.bcn.vincles.mobile.Client.Services.ChatService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetGroupMessagesRequest extends BaseRequest implements Callback<GroupMessageRestList> {

    private static String FROM = "from";
    private static String TO = "to";

    ChatService chatService;
    List<OnResponse> onResponses = new ArrayList<>();
    String idChat;
    Map<String, String> params;

    public GetGroupMessagesRequest(RenewTokenFailed listener, String idChat, long timeStampStart, long timeStampEnd) {
        super(listener, BaseRequest.AUTHENTICATED_REQUEST);
        this.params = new HashMap<>();
        if (timeStampStart != 0) {
            params.put(FROM, String.valueOf(timeStampStart));
        }
        if (timeStampEnd != 0) {
            params.put(TO, String.valueOf(timeStampEnd));
        }
        this.idChat = idChat;
    }

    @Override
    public void doRequest(String accessToken) {
        authenticatedRequest(accessToken);
        chatService = retrofit.create(ChatService.class);
        Call<GroupMessageRestList> call = chatService.getGroupMessages(idChat, params);
        call.enqueue(this);
    }

    public void addOnOnResponse(OnResponse onResponse) {
        onResponses.add(onResponse);
    }

    @Override
    public void onResponse(Call<GroupMessageRestList> call, Response<GroupMessageRestList> response) {
        if (!shouldRenewToken(this, response)) {
            for (OnResponse r : onResponses) {
                if (response.isSuccessful()) {
                    r.onResponseGetGroupMessagesRequest(response.body(), idChat);
                } else {
                    String errorCode = ErrorHandler.parseError(response).getCode();
                    r.onFailureGetGroupMessagesRequest(errorCode);
                }
            }
        }
    }

    @Override
    public void onFailure(Call<GroupMessageRestList> call, Throwable t) {
        for (OnResponse r : onResponses) {
            r.onFailureGetGroupMessagesRequest(new Exception(t));
        }
    }

    public interface OnResponse {
        void onResponseGetGroupMessagesRequest(GroupMessageRestList groupMessageRestList, String idChat);
        void onFailureGetGroupMessagesRequest(Object error);
    }
}
