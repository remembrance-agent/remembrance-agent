package io.p13i.ra.databases.cache.metadata;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Helper class for parsing metadata to an object and serializing it to a string
 */
public class LocalDiskCacheMetadataParser {

    private static Gson getMetadataGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDiskCacheMetadata.class, new JsonSerializer<LocalDiskCacheMetadata>() {
                    @Override
                    public JsonElement serialize(LocalDiskCacheMetadata src, Type typeOfSrc, JsonSerializationContext context) {
                        JsonObject obj = new JsonObject();
                        obj.add("fileNamesToMetadata", new JsonObject());
                        for (Map.Entry<String, LocalDiskCacheDocumentMetadata> entry : src.fileNamesToMetadata.entrySet()) {
                            JsonObject fileObj = new JsonObject();
                            fileObj.addProperty("fileName", entry.getValue().fileName);
                            fileObj.addProperty("subject", entry.getValue().subject);
                            fileObj.addProperty("url", entry.getValue().url);
                            obj.getAsJsonObject("fileNamesToMetadata").add(entry.getKey(), fileObj);
                        }
                        return obj;
                    }
                })
                .create();
    }

    public static LocalDiskCacheMetadata fromString(String string) {
        return getMetadataGson().fromJson(string, new TypeToken<LocalDiskCacheMetadata>(){}.getType());
    }

    public static String asString(LocalDiskCacheMetadata metadata) {
        return getMetadataGson().toJson(metadata, new TypeToken<LocalDiskCacheMetadata>(){}.getType());
    }
}
