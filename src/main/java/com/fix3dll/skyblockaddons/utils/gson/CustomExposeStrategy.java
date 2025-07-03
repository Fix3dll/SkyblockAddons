package com.fix3dll.skyblockaddons.utils.gson;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.annotations.Expose;

public final class CustomExposeStrategy implements ExclusionStrategy {

    private final boolean serializing;

    public CustomExposeStrategy(boolean serializing) {
        this.serializing = serializing;
    }

    @Override
    public boolean shouldSkipField(FieldAttributes f) {
        Expose ex = f.getAnnotation(Expose.class);
        if (ex == null) return false; // default
        return serializing ? !ex.serialize() : !ex.deserialize();
    }

    @Override
    public boolean shouldSkipClass(Class<?> c) {
        Expose ex = c.getAnnotation(Expose.class);
        if (ex == null) return false; // default
        return serializing ? !ex.serialize() : !ex.deserialize();
    }

}