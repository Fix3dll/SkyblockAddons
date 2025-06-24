package com.fix3dll.skyblockaddons.exceptions;

import com.fix3dll.skyblockaddons.core.ColorCode;

/**
 * This exception is thrown when the mod fails to load a necessary data file during startup.
 */
public class DataLoadingException extends RuntimeException {
    private static final String ERROR_MESSAGE_FORMAT = "Failed to load file at\n" + ColorCode.DARK_RED + "%s";

    public DataLoadingException(String filePathString, Throwable cause) {
        super(String.format(ERROR_MESSAGE_FORMAT, filePathString), cause);
    }
}