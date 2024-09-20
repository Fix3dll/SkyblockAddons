package codes.biscuit.skyblockaddons.utils.data;

import lombok.Getter;
import lombok.NonNull;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.client.FutureRequestExecutionService;
import org.apache.http.impl.client.HttpRequestFutureTask;

public class RemoteFileRequest<T> {
    protected static final String NO_DATA_RECEIVED_ERROR = "No data received for get request to \"%s\"";
    private final String REQUEST_URL;
    private final ResponseHandler<T> RESPONSE_HANDLER;
    private final FutureCallback<T> FETCH_CALLBACK;
    private final boolean ESSENTIAL;

    @Getter
    private HttpRequestFutureTask<T> futureTask;

    public RemoteFileRequest(@NonNull String requestPath, @NonNull ResponseHandler<T> responseHandler,
                             DataFetchCallback<T> dataFetchCallback) {
        this(requestPath, responseHandler, dataFetchCallback, false);
    }

    public RemoteFileRequest(@NonNull String requestPath, @NonNull ResponseHandler<T> responseHandler,
                             DataFetchCallback<T> dataFetchCallback, boolean essential) {
        this(requestPath, responseHandler, dataFetchCallback, essential, false);
    }

    public RemoteFileRequest(@NonNull String requestPath, @NonNull ResponseHandler<T> responseHandler,
                             DataFetchCallback<T> dataFetchCallback, boolean essential, boolean usingCustomUrl) {
        REQUEST_URL = usingCustomUrl ? requestPath : getCDNBaseURL() + requestPath;
        RESPONSE_HANDLER = responseHandler;
        FETCH_CALLBACK = dataFetchCallback;
        ESSENTIAL = essential;
        futureTask = null;
    }

    public void execute(@NonNull FutureRequestExecutionService executionService) {
        futureTask = executionService.execute(new HttpGet(REQUEST_URL), null, RESPONSE_HANDLER, FETCH_CALLBACK);
    }

    public String getURL() {
        return REQUEST_URL;
    }

    public boolean isEssential() {
        return ESSENTIAL;
    }

    protected boolean isDone() {
        return futureTask.isDone();
    }

    protected static String getCDNBaseURL() {
        return DataUtils.useFallbackCDN ? DataConstants.FALLBACK_CDN_BASE_URL : DataConstants.CDN_BASE_URL;
    }
}
