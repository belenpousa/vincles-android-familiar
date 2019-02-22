package cat.bcn.vincles.mobile.Client.Model.Serializers;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;

import cat.bcn.vincles.mobile.Client.Model.ChatMessageRest;
import cat.bcn.vincles.mobile.Client.Model.GetUser;
import cat.bcn.vincles.mobile.Client.Model.GuestRest;
import cat.bcn.vincles.mobile.Client.Model.MeetingRest;
import cat.bcn.vincles.mobile.Client.Model.MeetingUserInfoRest;
import io.realm.RealmList;

public class MeetingRestDeserializer implements JsonDeserializer<MeetingRest> {

    @Override
    public MeetingRest deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        if (json==null) {
            Log.d("meetfa","json meet NUUUULL!!");
        }

        Log.d("meetfam","deserializeM");
        JsonObject jsonObject = json.getAsJsonObject();
        if (jsonObject==null) {
            Log.d("meetfa","jsonObject meet NUUUULL!!");
        }

        Log.d("meetints","meeting bef1");
        int id = jsonObject.get("id").getAsInt();
        Log.d("meetfam","deserializeM id:"+id);
        long date = jsonObject.get("date").getAsLong();
        Log.d("meetints","meeting bef2, id:"+id);
        int duration = jsonObject.get("duration").getAsInt();
        Log.d("meetints","meeting after2");
        String description = !jsonObject.get("description").equals(JsonNull.INSTANCE) ? jsonObject.get("description").getAsString() : "";

        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        GetUser host = gson.fromJson(jsonObject.get("hostInfo"), GetUser.class);
        Log.d("meetints","meeting after host");

        ArrayList<GuestRest> guests = new ArrayList<>();
        JsonArray guestsArray = jsonObject.get("guests") != null &&
                !jsonObject.get("guests").equals(JsonNull.INSTANCE) ?
                jsonObject.get("guests").getAsJsonArray() : new JsonArray();

        Log.d("meetints","meeting before for, length:"+guestsArray.size());
        int i = 0;
        for (JsonElement jsonElement : guestsArray) {
            JsonObject guestJsonObject = jsonElement.getAsJsonObject();
            GuestRest guestRest = gson.fromJson(guestJsonObject, GuestRest.class);
            guests.add(guestRest);
            Log.d("meetints","meeting after guest i:"+i);
            i++;
        }
        Log.d("meetints","meeting after guests");

        Log.d("meetfam","deserializeM OK id:"+id);

        return new MeetingRest(id, date, duration, description, host, guests);
    }

}
