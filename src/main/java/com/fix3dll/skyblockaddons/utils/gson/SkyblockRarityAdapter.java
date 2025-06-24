package com.fix3dll.skyblockaddons.utils.gson;

import com.fix3dll.skyblockaddons.core.SkyblockRarity;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class SkyblockRarityAdapter extends TypeAdapter<SkyblockRarity> {

    @Override
    public void write(JsonWriter out, SkyblockRarity value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.value(value.getLoreName());
    }

    @Override
    public SkyblockRarity read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        String rarityString = in.nextString();
        return SkyblockRarity.getByLoreName(rarityString);
    }
}