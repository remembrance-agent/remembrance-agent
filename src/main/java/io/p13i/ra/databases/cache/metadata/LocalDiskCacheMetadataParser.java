package io.p13i.ra.databases.cache.metadata;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import io.p13i.ra.utils.DateUtils;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

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
//                            fileObj.addProperty("created", DateUtils.formatTimestamp(entry.getValue().created));
//                            fileObj.addProperty("lastModified", DateUtils.formatTimestamp(entry.getValue().lastModified));
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


    public static void main(String[] args) {
        LocalDiskCacheMetadata metadata = new LocalDiskCacheMetadata(new HashMap<String, LocalDiskCacheDocumentMetadata>() {{
            put("asdf.txt", new LocalDiskCacheDocumentMetadata() {{
                fileName = "asdf.txt";
                subject = "ASDF";
//                    lastModified = DateUtils.now();
//                    created = DateUtils.now();
            }});
        }});

        String json = asString(metadata);
        LocalDiskCacheMetadata metadata1 = fromString(json);
        json = asString(metadata1);

        System.out.println(json);
    }
}
