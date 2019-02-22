package cat.bcn.vincles.mobile.Client.Requests;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Model.GroupUserList;
import cat.bcn.vincles.mobile.Client.Model.UserGroups;
import cat.bcn.vincles.mobile.Client.Services.GroupsService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetGroupUserListRequest extends BaseRequest implements Callback<GroupUserList> {

    GroupsService groupsService;
    List<OnResponse> onResponses = new ArrayList<>();
    String groupID;

    public GetGroupUserListRequest(RenewTokenFailed listener, String accessToken, String groupID) {
        super(listener, BaseRequest.AUTHENTICATED_REQUEST, accessToken);
        groupsService = retrofit.create(GroupsService.class);
        this.groupID = groupID;
    }

    public void doRequest() {
        Call<GroupUserList> call = groupsService.getGroupUserList(groupID);
        call.enqueue(this);
    }

    public void addOnOnResponse(OnResponse onResponse) {
        onResponses.add(onResponse);
    }

    @Override
    public void onResponse(Call<GroupUserList> call, Response<GroupUserList> response) {
        if (!shouldRenewToken(this, response)) {
            for (OnResponse r : onResponses) {
                if (response.isSuccessful()) {
                    r.onResponseGetGroupUserListRequest(response.body(), groupID);
                } else {
                    String errorCode = ErrorHandler.parseError(response).getCode();
                    r.onFailureGetGroupUserListRequest(errorCode);
                }
            }
        }
    }

    @Override
    public void onFailure(Call<GroupUserList> call, Throwable t) {
        for (OnResponse r : onResponses) {
            r.onFailureGetGroupUserListRequest(new Exception(t));
        }
    }

    @Override
    public void doRequest(String token) {

    }


    public interface OnResponse {
        void onResponseGetGroupUserListRequest(GroupUserList userList, String groupID);
        void onFailureGetGroupUserListRequest(Object error);
    }
}
