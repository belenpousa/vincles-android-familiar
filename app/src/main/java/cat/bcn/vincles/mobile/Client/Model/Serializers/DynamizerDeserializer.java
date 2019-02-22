package cat.bcn.vincles.mobile.Client.Model.Serializers;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import cat.bcn.vincles.mobile.Client.Model.Dynamizer;
import cat.bcn.vincles.mobile.Client.Model.Group;

public class DynamizerDeserializer implements JsonDeserializer<Dynamizer> {
    @Override
    public Dynamizer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Log.d("contRepo","DynamizerDeserializer");
        JsonObject jsonObject = json.getAsJsonObject();

        int id = jsonObject.get("id").getAsInt();
        String name = jsonObject.get("name").getAsString();
        String lastname = jsonObject.get("lastname").getAsString();
        String alias = jsonObject.get("alias").getAsString();
        String gender = jsonObject.get("gender").getAsString();
        int idContentPhoto = jsonObject.get("idContentPhoto").getAsInt();

        return new Dynamizer(id, name, lastname, alias, gender, idContentPhoto, "");
    }
}
