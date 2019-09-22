package io.p13i.ra.databases.cache.metadata;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Maps a file name to metadata information
 */
public class LocalDiskCacheMetadata implements Serializable {
    public Map<String, LocalDiskCacheDocumentMetadata> fileNamesToMetadata;

    /**
     * @param fileNamesToMetadata a mapping of a file name to the file's metadata
     */
    public LocalDiskCacheMetadata(Map<String, LocalDiskCacheDocumentMetadata> fileNamesToMetadata) {
        this.fileNamesToMetadata = fileNamesToMetadata;
    }

    /**
     * @return a GSON object that can read and append metadata
     */
    private static Gson getMetadataGson() {
        return new GsonBuilder()
                .setPrettyPrinting()
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

    /**
     * Gets a metadata object from a string representation
     *
     * @param string the JSON string of metadata
     * @return a {@code LocalDiskCacheMetadata} object
     */
    public static LocalDiskCacheMetadata fromJSONString(String string) {
        return getMetadataGson().fromJson(string, new TypeToken<LocalDiskCacheMetadata>() {
        }.getType());
    }

    /**
     * Converts a metadata object to a string
     *
     * @return a JSON string
     */
    public String asJSONString() {
        return getMetadataGson().toJson(this, new TypeToken<LocalDiskCacheMetadata>() {
        }.getType());
    }
}
