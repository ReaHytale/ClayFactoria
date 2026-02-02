package com.clayfactoria.codecs;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.codecs.EnumCodec;

import java.util.function.Supplier;

public enum PathType implements Supplier<String> {
    ONCE("Path once and no more"),
    LOOP("Loop between all paths");

    public static final Codec<PathType> CODEC = new EnumCodec<>(PathType.class);

    private final String description;
    PathType(String description) {
        this.description = description;
    }
    public String get() {
        return this.description;
    }
}