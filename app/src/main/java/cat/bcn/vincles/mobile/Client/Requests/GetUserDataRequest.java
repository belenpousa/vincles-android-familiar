package cat.bcn.vincles.mobile.Client.Requests;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Model.GetUser;
import cat.bcn.vincles.mobile.Client.Services.UserService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetUserDataRequest extends BaseRequest implements Callback<GetUser> {

    UserService userService;
    String userId;
    List<OnResponse> onResponses = new ArrayList<>();

    public GetUserDataRequest(String userId) {
        super(null, BaseRequest.AUTHENTICATED_REQUEST);

        this.userId = userId;
    }

    @Override
    public void doRequest(String accessToken) {
        authenticatedRequest(accessToken);
        userService = retrofit.create(UserService.class);
        Call<GetUser> call = userService.getUserInfo(userId);
        call.enqueue(this);
    }

    public void addOnOnResponse(OnResponse onResponse) {
        onResponses.add(onResponse);
    }

    @Override
    public void onResponse(Call<GetUser> call, Response<GetUser> response) {
        if (!shouldRenewToken(this, response)) {
            for (OnResponse r : onResponses) {
                if (response.isSuccessful()) {
                    r.onResponseGetAuthenticatedUserDataRequest(response.body());
                } else {
                    String errorCode = ErrorHandler.parseError(response).getCode();
                    r.onFailureGetAuthenticatedUserDataRequest(errorCode);
                }
            }
        }
    }

    @Override
    public void onFailure(Call<GetUser> call, Throwable t) {
        for (OnResponse r : onResponses) {
            r.onFailureGetAuthenticatedUserDataRequest(new Exception(t));
        }
    }


    public interface OnResponse {
        void onResponseGetAuthenticatedUserDataRequest(GetUser user);
        void onFailureGetAuthenticatedUserDataRequest(Object error);
    }

}