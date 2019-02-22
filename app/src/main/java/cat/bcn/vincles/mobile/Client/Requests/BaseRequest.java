package cat.bcn.vincles.mobile.Client.Requests;

import android.app.Application;
import android.content.res.Resources;

import java.io.IOException;

import cat.bcn.vincles.mobile.BuildConfig;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import cat.bcn.vincles.mobile.Client.Enviroment.Environment;

public abstract class BaseRequest {

    static final int UNAUTHENTICATED_REQUEST = 1;
    static final int LOGIN_LOGOUT_REQUEST = 2;
    static final int AUTHENTICATED_REQUEST = 3;

    static final String BEARER_AUTH = "Bearer ";
    Retrofit retrofit;
    String refreshToken;
    RenewTokenFailed listener;

    //singleton retrofit for authenticated requests
    private static Retrofit authenticatedRetrofit = null;
    private static String authenticatedToken = null;

    public BaseRequest (RenewTokenFailed listener, int typeRequest){
        this.listener = listener;
        switch (typeRequest) {
            case UNAUTHENTICATED_REQUEST:
                unauthnticatedRequest();
                return;
            case LOGIN_LOGOUT_REQUEST:
                loginRequest();
                return;
        }
    }

    public BaseRequest (RenewTokenFailed listener, int typeRequest, String accessToken){
        this.listener = listener;
        switch (typeRequest) {
            case AUTHENTICATED_REQUEST:
                authenticatedRequest(accessToken);
                return;
        }
    }

    public abstract void doRequest(String token);

    public void onRenewTokenFailed() {
        if (listener!=null) listener.onRenewTokenFailed();
    }

    public void unauthnticatedRequest() {

        OkHttpClient okHttpClient = VinclesHttpClient.getOkHttpClient();
        retrofit = new Retrofit.Builder()
                .baseUrl(Environment.getApiBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();
    }

    public void loginRequest () {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            // set your desired log level
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
            clientBuilder.addInterceptor(logging);
        }
        clientBuilder.addInterceptor(new Interceptor() {
                                      @Override
                                      public Response intercept(Interceptor.Chain chain) throws IOException {
                                          Request original = chain.request();

                                          Request request = original.newBuilder()
                                                  .header("Authorization", "Basic " + Environment.getApiBasicAuth())
                                                  .header("Content-Type", "application/x-www-form-urlencoded")
                                                  .method(original.method(), original.body())
                                                  .build();

                                          return chain.proceed(request);
                                      }
                                  });

        OkHttpClient okHttpClient = VinclesHttpClient.getOkHttpClient(clientBuilder);
        retrofit = new Retrofit.Builder()
                    .baseUrl(Environment.getApiBaseUrl())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();
    }

    public void authenticatedRequest (final String accessToken) {
        retrofit = getAuthenticatedRetrofitInstance(accessToken);
    }

    public boolean shouldRenewToken(BaseRequest request, retrofit2.Response response) {
        if (response.code() == 401) {
            RenewTokenRequest renewTokenRequest = new RenewTokenRequest(listener, request);
            renewTokenRequest.doRequest(new UserPreferences().getRefreshToken());

            return true;
        }
        return false;
    }

    public interface RenewTokenFailed {
        public void onRenewTokenFailed();
    }

    private Retrofit getAuthenticatedRetrofitInstance(final String accessToken) {
        if (authenticatedToken == null || !authenticatedToken.equals(accessToken)) {

            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
            if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                // set your desired log level
                logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
                clientBuilder.addInterceptor(logging);
            }
            clientBuilder.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Interceptor.Chain chain) throws IOException {
                    Request original = chain.request();

                    Request request = original.newBuilder()
                            .header("Authorization", BEARER_AUTH + accessToken)
                            .method(original.method(), original.body())
                            .build();

                    return chain.proceed(request);
                }
            });


            OkHttpClient okHttpClient = VinclesHttpClient.getOkHttpClient(clientBuilder);
            authenticatedRetrofit = new Retrofit.Builder()
                    .baseUrl(Environment.getApiBaseUrl())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();

            authenticatedToken = accessToken;
        }

        return authenticatedRetrofit;
    }

}
