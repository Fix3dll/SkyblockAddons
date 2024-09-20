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
                "Failed to fetch \"{}\" data from the server. The local copy will be used instead.",
                DataUtils.getFileNameFromUrlString(urlString)
        );
        logger.error(ex.getMessage());
        DataUtils.handleOnlineFileLoadException(urlString, ex, isEssential);
    }

    @Override
    public void cancelled() {
        logger.info("Cancelled fetching {}", urlString);
    }
}
