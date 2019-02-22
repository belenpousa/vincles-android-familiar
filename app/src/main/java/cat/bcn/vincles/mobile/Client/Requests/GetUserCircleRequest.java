package cat.bcn.vincles.mobile.Client.Requests;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Model.CircleUsers;
import cat.bcn.vincles.mobile.Client.Model.UserCircles;
import cat.bcn.vincles.mobile.Client.Services.CirclesService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetUserCircleRequest extends BaseRequest implements Callback<UserCircles> {

    CirclesService circlesService;
    List<OnResponse> onResponses = new ArrayList<>();

    public GetUserCircleRequest(RenewTokenFailed listener, String accessToken) {
        super(listener, BaseRequest.AUTHENTICATED_REQUEST, accessToken);
        circlesService = retrofit.create(CirclesService.class);
    }

    public void doRequest() {
        Call<UserCircles> call = circlesService.getUserCircle();
        call.enqueue(this);
    }

    public void addOnOnResponse(OnResponse onResponse) {
        onResponses.add(onResponse);
    }

    @Override
    public void onResponse(Call<UserCircles> call, Response<UserCircles> response) {
        if (!shouldRenewToken(this, response)) {
            for (OnResponse r : onResponses) {
                if (response.isSuccessful()) {
                    r.onResponseGetUserCircleRequest(response.body());
                } else {
                    String errorCode = ErrorHandler.parseError(response).getCode();
                    r.onFailureGetUserCircleRequest(errorCode);
                }
            }
        }
    }

    @Override
    public void onFailure(Call<UserCircles> call, Throwable t) {
        for (OnResponse r : onResponses) {
            r.onFailureGetUserCircleRequest(new Exception(t));
        }
    }

    @Override
    public void doRequest(String token) {

    }

    public interface OnResponse {
        void onResponseGetUserCircleRequest(UserCircles userCircles);
        void onFailureGetUserCircleRequest(Object error);
    }
}
