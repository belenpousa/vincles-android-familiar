package cat.bcn.vincles.mobile.Client.Model.Serializers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Model.ChatMessageRest;
import cat.bcn.vincles.mobile.Client.Model.ChatMessageRestList;
import cat.bcn.vincles.mobile.Client.Model.MeetingRest;
import cat.bcn.vincles.mobile.Client.Model.MeetingRestList;

public class MeetingRestListDeserializer implements JsonDeserializer<MeetingRestList> {

    @Override
    public MeetingRestList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        List<MeetingRest> meetingRestList = new ArrayList<>();

        JsonArray getUsersJsonArr = json.getAsJsonArray();
        for ( JsonElement jsonElement : getUsersJsonArr) {

            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            MeetingRest meetingRest = gson.fromJson(jsonObject, MeetingRest.class);
            meetingRestList.add(meetingRest);

        }

        return new MeetingRestList(meetingRestList);
    }
}
