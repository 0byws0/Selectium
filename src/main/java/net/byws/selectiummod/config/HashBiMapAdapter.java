package net.byws.selectiummod.config;
import com.google.common.collect.HashBiMap;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Map;

// Custom TypeAdapter for HashBiMap
public class HashBiMapAdapter<K, V> implements JsonSerializer<HashBiMap<K, V>>, JsonDeserializer<HashBiMap<K, V>> {

    @Override
    public JsonElement serialize(HashBiMap<K, V> src, Type typeOfSrc, JsonSerializationContext context) {
        // Convert BiMap to standard Map for JSON representation
        return context.serialize(src, Map.class);
    }

    @Override
    public HashBiMap<K, V> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        // Deserialize back to a standard Map and convert to HashBiMap
        Map<K, V> map = context.deserialize(json, new TypeToken<Map<K, V>>(){}.getType());
        return HashBiMap.create(map);
    }
}
