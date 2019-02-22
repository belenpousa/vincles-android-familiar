package cat.bcn.vincles.mobile.Client.Requests;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Model.CircleUsers;
import cat.bcn.vincles.mobile.Client.Services.CirclesService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetCircleUserRequest extends BaseRequest implements Callback<CircleUsers> {

    CirclesService circlesService;
    List<OnResponse> onResponses = new ArrayList<>();

    public GetCircleUserRequest(RenewTokenFailed listener) {
        //super(BaseRequest.AUTHENTICATED_REQUEST, accessToken);
        //circlesService = retrofit.create(CirclesService.class);
        super(listener, BaseRequest.AUTHENTICATED_REQUEST);
    }

    @Override
    public void doRequest(String accessToken) {
        authenticatedRequest(accessToken);
        circlesService = retrofit.create(CirclesService.class);
        Call<CircleUsers> call = circlesService.getCircleUser();
        call.enqueue(this);
    }

    public void addOnOnResponse(OnResponse onResponse) {
        onResponses.add(onResponse);
    }

    @Override
    public void onResponse(Call<CircleUsers> call, Response<CircleUsers> response) {
        if (!shouldRenewToken(this, response)) {
            for (OnResponse r : onResponses) {
                if (response.isSuccessful()) {
                    r.onResponseGetCircleUserRequest(response.body());
                } else {
                    String errorCode = ErrorHandler.parseError(response).getCode();
                    r.onFailureGetCircleUserRequest(errorCode);
                }
            }
        }
    }

    @Override
    public void onFailure(Call<CircleUsers> call, Throwable t) {
        for (OnResponse r : onResponses) {
            r.onFailureGetCircleUserRequest(new Exception(t));
        }
    }

    public interface OnResponse {
        void onResponseGetCircleUserRequest(CircleUsers circleUsers);
        void onFailureGetCircleUserRequest(Object error);
    }
}
