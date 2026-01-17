package com.fix3dll.skyblockaddons.utils.data;

import org.apache.logging.log4j.Logger;

import java.net.URI;

/**
 * This is a simple class to log the result of a request for debugging.
 * @param <T> the type of the result, unused
 */
public abstract class DataFetchCallback<T> {

    private final Logger logger;
    private final String urlString;
    private final boolean isEssential;
    private Throwable firstFail = null;

    public DataFetchCallback(Logger logger, URI url) {
        this(logger, url, false);
    }

    public DataFetchCallback(Logger logger, URI url, boolean isEssential) {
        this.logger = logger;
        this.urlString = url.toString();
        this.isEssential = isEssential;
    }

    public void completed(T result) {
        logger.debug("Successfully fetched {}", urlString);
    }

    public void failed(Throwable ex) {
        logger.error(
                "Failed to fetch \"{}\" data from the server. The local copy will be used instead.\n{}",
                DataUtils.getFileNameFromUrlString(urlString), ex.getMessage()
        );

        // If both the main and fallback CDNs fail, log both.
        if (DataUtils.failedUris.contains(urlString) && firstFail != null) {
            String fallbackAddress = urlString.replace(DataConstants.CDN_BASE_URL, DataConstants.FALLBACK_CDN_BASE_URL);
            DataUtils.handleOnlineFileLoadException(urlString, firstFail, isEssential);
            DataUtils.handleOnlineFileLoadException(fallbackAddress, ex, isEssential);
        } else if (!urlString.contains(DataConstants.CDN_BASE_URL)) {
            DataUtils.handleOnlineFileLoadException(urlString, ex, isEssential);
        }

        if (firstFail == null) firstFail = ex;
    }

    public void cancelled() {
        logger.info("Cancelled fetching {}", urlString);
    }

}