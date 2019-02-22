package cat.bcn.vincles.mobile.Client.Services;

import cat.bcn.vincles.mobile.Client.Model.CircleUsers;
import cat.bcn.vincles.mobile.Client.Model.UserCircles;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Path;


public interface CirclesService {

    @GET("/t/vincles-bcn.cat/vincles-services/1.0/circles/mine/users")
    public Call<CircleUsers> getCircleUser();

    @GET("/t/vincles-bcn.cat/vincles-services/1.0/users/me/circles")
    public Call<UserCircles> getUserCircle();

    @DELETE("/t/vincles-bcn.cat/vincles-services/1.0/circles/mine/users/{idUserToUnlink}")
    public Call<ResponseBody> deleteCircle(@Path("idUserToUnlink") String idUserToUnlink);

    @DELETE("/t/vincles-bcn.cat/vincles-services/1.0/circles/{idUserToUnlink}/user/me")
    public Call<ResponseBody> exitCircle(@Path("idUserToUnlink") String idUserToUnlink);


}
