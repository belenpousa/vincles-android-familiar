package cat.bcn.vincles.mobile.Client.Requests;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Model.CircleUsers;
import cat.bcn.vincles.mobile.Client.Model.UserGroups;
import cat.bcn.vincles.mobile.Client.Services.CirclesService;
import cat.bcn.vincles.mobile.Client.Services.GroupsService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetUserGroupsRequest extends BaseRequest implements Callback<UserGroups> {

    GroupsService groupsService;
    List<OnResponse> onResponses = new ArrayList<>();

    public GetUserGroupsRequest(RenewTokenFailed listener, String accessToken) {
        super(listener, BaseRequest.AUTHENTICATED_REQUEST, accessToken);
        groupsService = retrofit.create(GroupsService.class);
    }

    public void doRequest() {
        Call<UserGroups> call = groupsService.getUserGroupList();
        call.enqueue(this);
    }

    public void addOnOnResponse(OnResponse onResponse) {
        onResponses.add(onResponse);
    }

    @Override
    public void onResponse(Call<UserGroups> call, Response<UserGroups> response) {
        if (!shouldRenewToken(this, response)) {
            for (OnResponse r : onResponses) {
                if (response.isSuccessful()) {
                    r.onResponseGetUserGroupsRequest(response.body());
                } else {
                    String errorCode = ErrorHandler.parseError(response).getCode();
                    r.onFailureGetUserGroupsRequest(errorCode);
                }
            }
        }
    }

    @Override
    public void onFailure(Call<UserGroups> call, Throwable t) {
        for (OnResponse r : onResponses) {
            r.onFailureGetUserGroupsRequest(new Exception(t));
        }
    }

    @Override
    public void doRequest(String token) {

    }

    public interface OnResponse {
        void onResponseGetUserGroupsRequest(UserGroups userGroups);
        void onFailureGetUserGroupsRequest(Object error);
    }
}
