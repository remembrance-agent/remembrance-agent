package io.p13i.ra.utils;

import com.google.gson.*;

class JSONUtils {
    private static Gson getGson() {
        return new GsonBuilder()
                .create();
    }

    public static String toJson(Object src) {
        return getGson().toJson(src);
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        return getGson().fromJson(json, classOfT);
    }
}
