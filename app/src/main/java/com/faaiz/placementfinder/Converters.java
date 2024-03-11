package com.faaiz.placementfinder;

import androidx.room.TypeConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;

public class Converters {
    @TypeConverter
    public static String fromList(List<String> skills) {
        return new Gson().toJson(skills);
    }

    @TypeConverter
    public static List<String> toList(String skillsString) {
        Type listType = new TypeToken<List<String>>() {}.getType();
        return new Gson().fromJson(skillsString, listType);
    }
}
