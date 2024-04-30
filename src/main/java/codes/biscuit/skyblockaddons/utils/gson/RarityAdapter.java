package codes.biscuit.skyblockaddons.utils.gson;

import codes.biscuit.skyblockaddons.core.Rarity;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class RarityAdapter extends TypeAdapter<Rarity> {

    @Override
    public void write(JsonWriter out, Rarity value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.value(value.getLoreName());
    }

    @Override
    public Rarity read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        String rarityString = in.nextString();
        return Rarity.getByLoreName(rarityString);
    }
}