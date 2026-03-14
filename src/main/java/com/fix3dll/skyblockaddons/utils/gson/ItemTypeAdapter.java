package com.fix3dll.skyblockaddons.utils.gson;

import com.fix3dll.skyblockaddons.core.ItemType;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class ItemTypeAdapter extends TypeAdapter<ItemType> {

    @Override
    public void write(JsonWriter out, ItemType value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.value(value.name());
    }

    @Override
    public ItemType read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        String itemTypeString = in.nextString();
        return ItemType.valueOf(itemTypeString);
    }
}