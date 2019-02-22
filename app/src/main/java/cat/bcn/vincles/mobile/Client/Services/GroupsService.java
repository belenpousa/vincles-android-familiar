package cat.bcn.vincles.mobile.Client.Services;

import com.google.gson.JsonArray;

import cat.bcn.vincles.mobile.Client.Model.GroupUserList;
import cat.bcn.vincles.mobile.Client.Model.UserGroups;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;


public interface GroupsService {

    @GET("/t/vincles-bcn.cat/vincles-services/1.0/users/me/groups")
    public Call<UserGroups> getUserGroupList();

    @GET("/t/vincles-bcn.cat/vincles-services/1.0/groups/{groupId}/photo")
    public Call<ResponseBody> getGroupPhoto(@Path("groupId") String groupId);

    @GET("/t/vincles-bcn.cat/vincles-services/1.0/groups/{groupId}/users")
    public Call<GroupUserList> getGroupUserList(@Path("groupId") String groupId);

    @POST("/t/vincles-bcn.cat/vincles-services/1.0/groups/{groupId}/users/{userId}/invite")
    public Call<ResponseBody> groupInviteUserToCircle(@Path("groupId") int groupId,
                                                      @Path("userId") int userId);

    @GET("/t/vincles-bcn.cat/vincles-services/1.0/chats/{idChat}/messages/{idMessage}/content")
    public Call<ResponseBody> getMessageFile(@Path("idChat") int idChat,
                                         @Path("idMessage") int idMessage);
}
