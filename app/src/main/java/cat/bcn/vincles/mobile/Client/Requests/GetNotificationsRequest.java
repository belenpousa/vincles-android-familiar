package cat.bcn.vincles.mobile.Client.Requests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cat.bcn.vincles.mobile.Client.Business.Firebase.Installation;
import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Model.NotificationsRestList;
import cat.bcn.vincles.mobile.Client.Services.ChatService;
import cat.bcn.vincles.mobile.Client.Services.UserService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetNotificationsRequest extends BaseRequest implements Callback<NotificationsRestList> {

    private static String PLATFORM_VERSION = "platform_version";
    private static String FROM = "from";
    private static String TO = "to";

    UserService userService;
    List<OnResponse> onResponses = new ArrayList<>();
    Map<String, String> params;

    public GetNotificationsRequest(RenewTokenFailed listener, long timeStampStart, long timeStampEnd) {
        super(listener, BaseRequest.AUTHENTICATED_REQUEST);
        this.params = new HashMap<>();
        params.put(PLATFORM_VERSION, String.valueOf(Installation.INSTALLATION_PLATFORM_VERSION));
        if (timeStampStart != 0) {
            params.put(FROM, String.valueOf(timeStampStart));
        }
        if (timeStampEnd != 0) {
            params.put(TO, String.valueOf(timeStampEnd));
        }
    }

    @Override
    public void doRequest(String accessToken) {
        authenticatedRequest(accessToken);
        userService = retrofit.create(UserService.class);
        Call<NotificationsRestList> call = userService.getNotifications(params);
        call.enqueue(this);
    }

    public void addOnOnResponse(OnResponse onResponse) {
        onResponses.add(onResponse);
    }

    @Override
    public void onResponse(Call<NotificationsRestList> call, Response<NotificationsRestList> response) {
        if (!shouldRenewToken(this, response)) {
            for (OnResponse r : onResponses) {
                if (response.isSuccessful()) {
                    r.onResponseGetNotificationsRequest(response.body());
                } else {
                    String errorCode = ErrorHandler.parseError(response).getCode();
                    r.onFailureGetNotificationsRequest(errorCode);
                }
            }
        }
    }

    @Override
    public void onFailure(Call<NotificationsRestList> call, Throwable t) {
        for (OnResponse r : onResponses) {
            r.onFailureGetNotificationsRequest(new Exception(t));
        }
    }

    public interface OnResponse {
        void onResponseGetNotificationsRequest(NotificationsRestList notificationsRestList);
        void onFailureGetNotificationsRequest(Object error);
    }
}
