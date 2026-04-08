package com.fix3dll.skyblockaddons.features.backpacks;

import com.fix3dll.skyblockaddons.utils.gson.GsonInitializable;

import java.util.StringJoiner;

public class CompressedStorage implements GsonInitializable {

    /**
     * The gson serialized string. This is used to reduce the number of lines in the persistent config.
     * We use gson's pretty printing, so serializing a long byte array means a lot of line breaks and a very long file...
     */
    private String storage = "[]";

    /**
     * We immediately convert the string to byte[] form after deserialization using the {@link #gsonInit()} function.
     * This is cached internally so we don't have to parse out the string every time we want to get the storage.
     */
    private transient byte[] transientStorage = new byte[0];

    public CompressedStorage() {}

    public CompressedStorage(byte[] compressedStorage) {
        setStorage(compressedStorage);
    }

    /**
     * Gets the cached value of the storage to prevent string parsing every time.
     * @return the cached storage
     */
    public byte[] getStorage() {
        return transientStorage;
    }

    /**
     * Special setter.
     * Sets the cached value and also updates the serializable string.
     * @param storageBytes the bytes to store
     */
    public void setStorage(byte[] storageBytes) {
        if (storageBytes == null || storageBytes.length == 0) {
            this.transientStorage = new byte[0];
            this.storage = "[]";
            return;
        }
        this.transientStorage = storageBytes;
        this.storage = convertByteArrayToString(storageBytes);
    }

    /**
     * Converts a byte array into a string enclosed in brackets, with each byte separated by commas.
     * @param byteArray a byte array
     * @return the equivalent string of the form {@code \[(([0-9]+,)*[0-9]+)?\]}
     */
    private String convertByteArrayToString(byte[] byteArray) {
        StringJoiner joiner = new StringJoiner(",", "[", "]");
        for (byte b : byteArray) {
            joiner.add(Byte.toString(b));
        }
        return joiner.toString();
    }

    /**
     * Converts a string of bytes separated by commas and enclosed in brackets
     * into a byte array with each string of numbers forming the array.
     * @param formattedString a string of the form {@code \[(([0-9]+,)*[0-9]+)?\]}
     * @return the equivalent byte array
     */
    private byte[] convertStringToByteArray(String formattedString) {
        if (formattedString == null || formattedString.length() <= 2) {
            return new byte[0];
        }
        String[] parts = formattedString.substring(1, formattedString.length() - 1).split(",");
        byte[] result = new byte[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Byte.parseByte(parts[i]);
        }
        return result;
    }

    /**
     * Called immediately after deserialization by SBA's GSON parser.
     * Converts the deserialized string into a byte array for caching purposes.
     */
    @Override
    public void gsonInit() {
        this.transientStorage = convertStringToByteArray(storage);
    }

}