package com.fix3dll.skyblockaddons.utils.gson;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.impl.util.version.VersionParser;

import java.io.IOException;

public class SemanticVersionAdapter extends TypeAdapter<SemanticVersion> {

    @Override
    public void write(JsonWriter out, SemanticVersion value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.value(value.toString());
    }

    @Override
    public SemanticVersion read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        String versionString = in.nextString();
        try {
            return VersionParser.parseSemantic(versionString);
        } catch (VersionParsingException e) {
            SkyblockAddons.getLogger().error("Failed to parse semantic version string: " + versionString, e);
            return null;
        }
    }
}
