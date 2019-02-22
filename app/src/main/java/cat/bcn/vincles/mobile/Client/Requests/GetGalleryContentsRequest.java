package cat.bcn.vincles.mobile.Client.Requests;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Model.GalleryContents;
import cat.bcn.vincles.mobile.Client.Services.GalleryService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetGalleryContentsRequest extends BaseRequest implements Callback<GalleryContents> {

    private static final String MIMETYPES = "image/jpeg,image/jpg,video/mp4";

    GalleryService galleryService;
    List<OnResponse> onResponses = new ArrayList<>();
    long to;

    public GetGalleryContentsRequest(RenewTokenFailed listener, long to) {
        super(listener, BaseRequest.AUTHENTICATED_REQUEST);
        //super(BaseRequest.AUTHENTICATED_REQUEST, accessToken);
        //galleryService = retrofit.create(GalleryService.class);
        this.to = to;
    }

    @Override
    public void doRequest(String accessToken) {
        authenticatedRequest(accessToken);
        galleryService = retrofit.create(GalleryService.class);
        Call<GalleryContents> call = galleryService.getMineContents(to, MIMETYPES);
        call.enqueue(this);
    }

    public void addOnOnResponse(OnResponse onResponse) {
        onResponses.add(onResponse);
    }

    @Override
    public void onResponse(Call<GalleryContents> call, Response<GalleryContents> response) {
        if (!shouldRenewToken(this, response)) {
            for (OnResponse r : onResponses) {
                if (response.isSuccessful()) {
                    r.onResponseGetGalleryContentsRequest(response.body());
                } else {
                    String errorCode = ErrorHandler.parseError(response).getCode();
                    r.onFailureGetGalleryContentsRequest(errorCode);
                }
            }
        }
    }

    @Override
    public void onFailure(Call<GalleryContents> call, Throwable t) {
        for (OnResponse r : onResponses) {
            r.onFailureGetGalleryContentsRequest(new Exception(t));
        }
    }


    public interface OnResponse {
        void onResponseGetGalleryContentsRequest(GalleryContents galleryContent);
        void onFailureGetGalleryContentsRequest(Object error);
    }
}
