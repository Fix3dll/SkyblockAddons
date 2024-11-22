package codes.biscuit.skyblockaddons.utils.data;

import org.apache.http.concurrent.FutureCallback;
import org.apache.logging.log4j.Logger;

import java.net.URI;

/**
 * This is a simple {@link FutureCallback} to log the result of a request for debugging.
 *
 * @param <T> the type of the result, unused
 */
public abstract class DataFetchCallback<T> implements FutureCallback<T> {
    private final Logger logger;
    private final String urlString;
    private final boolean isEssential;
    private Exception firstFail = null;

    public DataFetchCallback(Logger logger, URI url) {
        this(logger, url, false);
    }

    public DataFetchCallback(Logger logger, URI url, boolean isEssential) {
        this.logger = logger;
        this.urlString = url.toString();
        this.isEssential = isEssential;
    }

    @Override
    public void completed(T result) {
        logger.debug("Successfully fetched {}", urlString);
    }

    @Override
    public void failed(Exception ex) {
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

    @Override
    public void cancelled() {
        logger.info("Cancelled fetching {}", urlString);
    }
}
