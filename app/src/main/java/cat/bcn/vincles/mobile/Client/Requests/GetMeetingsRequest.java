package cat.bcn.vincles.mobile.Client.Requests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Model.GroupMessageRestList;
import cat.bcn.vincles.mobile.Client.Model.MeetingRestList;
import cat.bcn.vincles.mobile.Client.Services.ChatService;
import cat.bcn.vincles.mobile.Client.Services.MeetingsService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetMeetingsRequest extends BaseRequest implements Callback<MeetingRestList> {

    private static String FROM = "from";
    private static String TO = "to";

    MeetingsService meetingsService;
    List<OnResponse> onResponses = new ArrayList<>();
    Map<String, String> params;

    public GetMeetingsRequest(RenewTokenFailed listener, long timeStampStart, long timeStampEnd) {
        super(listener, BaseRequest.AUTHENTICATED_REQUEST);
        this.params = new HashMap<>();
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
        meetingsService = retrofit.create(MeetingsService.class);
        Call<MeetingRestList> call = meetingsService.getMeetings(params);
        call.enqueue(this);
    }

    public void addOnOnResponse(OnResponse onResponse) {
        onResponses.add(onResponse);
    }

    @Override
    public void onResponse(Call<MeetingRestList> call, Response<MeetingRestList> response) {
        if (!shouldRenewToken(this, response)) {
            for (OnResponse r : onResponses) {
                if (response.isSuccessful()) {
                    r.onResponseGetMeetingsRequest(response.body());
                } else {
                    String errorCode = ErrorHandler.parseError(response).getCode();
                    r.onFailureGetMeetingsRequest(errorCode);
                }
            }
        }
    }

    @Override
    public void onFailure(Call<MeetingRestList> call, Throwable t) {
        for (OnResponse r : onResponses) {
            r.onFailureGetMeetingsRequest(new Exception(t));
        }
    }

    public interface OnResponse {
        void onResponseGetMeetingsRequest(MeetingRestList meetingRestList);
        void onFailureGetMeetingsRequest(Object error);
    }
}
