package cat.bcn.vincles.mobile.Client.Requests;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Model.TokenFromLogin;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Client.Services.UserService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RenewTokenRequest extends BaseRequest implements Callback<TokenFromLogin> {

    UserService userService;
    private static final String USER_PREFIX = "@vincles-bcn.cat";
    BaseRequest pendingRequest;


    public RenewTokenRequest(RenewTokenFailed listener, BaseRequest pendingRequest) {
        super(listener, BaseRequest.LOGIN_LOGOUT_REQUEST);
        userService = retrofit.create(UserService.class);
        this.pendingRequest = pendingRequest;
    }

    public void doRequest(String refreshToken) {
        Call<TokenFromLogin> call = userService.renewToken("refresh_token",refreshToken);
        call.enqueue(this);
    }



    @Override
    public void onResponse(Call<TokenFromLogin> call, Response<TokenFromLogin> response) {
        if (response.isSuccessful()) {
            TokenFromLogin tokenFromLogin = response.body();
            UserPreferences userPreferences = new UserPreferences();
            userPreferences.setAccessToken(tokenFromLogin.getAccessToken());
            userPreferences.setExpiresIn(tokenFromLogin.getExpiresIn());
            userPreferences.setTokenType(tokenFromLogin.getTokenType());
            userPreferences.setRefreshToken(tokenFromLogin.getRefreshToken());

            pendingRequest.doRequest(tokenFromLogin.getAccessToken());
        } else {
            onRenewTokenFailed();
        }

    }

    @Override
    public void onFailure(Call<TokenFromLogin> call, Throwable t) {
        onRenewTokenFailed();
    }


}
