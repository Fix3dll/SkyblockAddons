package com.fix3dll.skyblockaddons.utils.gson;

import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;

import java.io.IOException;

public class String2DoubleMapAdapter extends TypeAdapter<Object2DoubleMap<String>> {

    @Override
    public void write(JsonWriter out, Object2DoubleMap<String> map) throws IOException {
        if (map == null) {
            out.nullValue();
            return;
        }

        out.beginObject();
        for (Object2DoubleMap.Entry<String> entry : map.object2DoubleEntrySet()) {
            out.name(entry.getKey());
            out.value(entry.getDoubleValue());
        }
        out.endObject();
    }

    @Override
    public Object2DoubleMap<String> read(JsonReader in) throws IOException {
        JsonToken token = in.peek();
        if (token == JsonToken.NULL) {
            in.nextNull();
            return null;
        }

        Object2DoubleOpenHashMap<String> map = new Object2DoubleOpenHashMap<>();
        in.beginObject();
        while (in.hasNext()) {
            String key = in.nextName();

            if (in.peek() == JsonToken.NUMBER) {
                map.put(key, in.nextDouble());
            } else {
                throw new JsonParseException("Expected double but was " + in.peek() + " at path " + in.getPath());
            }
        }
        in.endObject();

        // Trims backing arrays to the exact number of parsed elements to minimize RAM usage
        map.trim();
        return map;
    }
}