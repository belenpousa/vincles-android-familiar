package cat.bcn.vincles.mobile.Client.Services;

import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.Map;

import cat.bcn.vincles.mobile.Client.Model.ChatMessageRest;
import cat.bcn.vincles.mobile.Client.Model.ChatMessageSentResponse;
import cat.bcn.vincles.mobile.Client.Model.GetUser;
import cat.bcn.vincles.mobile.Client.Model.NotificationRest;
import cat.bcn.vincles.mobile.Client.Model.NotificationsRestList;
import cat.bcn.vincles.mobile.Client.Model.Serializers.AddUser;
import cat.bcn.vincles.mobile.Client.Model.TokenFromLogin;
import cat.bcn.vincles.mobile.Client.Model.UserRegister;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;


public interface CallService {

    @POST("/t/vincles-bcn.cat/vincles-services/1.0/videoconference/start")
    Call<ResponseBody> startVideoconference(@Body JsonObject callInfo);

    @POST("/t/vincles-bcn.cat/vincles-services/1.0/videoconference/error")
    Call<ResponseBody> errorVideoconference(@Body JsonObject callInfo);

}
