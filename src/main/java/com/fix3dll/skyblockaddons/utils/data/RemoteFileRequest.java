package com.fix3dll.skyblockaddons.utils.data;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.utils.Utils;
import com.google.gson.Gson;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.zip.GZIPInputStream;

public class RemoteFileRequest<T> {

    private static final Gson GSON = SkyblockAddons.getGson();
    protected static final String NO_DATA_RECEIVED_ERROR = "No data received for get request to \"%s\"";
    private static final int MAX_RETRY_COUNT = 1;

    @Getter private final boolean essential;
    private final DataFetchCallback<T> callback;
    private final Type type;

    private String requestUrl;
    @Getter private CompletableFuture<T> futureTask;

    public RemoteFileRequest(@NonNull String requestPath, @NonNull Type type,
                             DataFetchCallback<T> dataFetchCallback) {
        this(requestPath, type, dataFetchCallback, false);
    }

    public RemoteFileRequest(@NonNull String requestPath, @NonNull Type type,
                             DataFetchCallback<T> dataFetchCallback, boolean essential) {
        this(requestPath, type, dataFetchCallback, essential, false);
    }

    public RemoteFileRequest(@NonNull String requestPath, @NonNull Type type,
                             DataFetchCallback<T> dataFetchCallback, boolean essential, boolean usingCustomUrl) {
        this.requestUrl = usingCustomUrl ? requestPath : DataConstants.CDN_BASE_URL + requestPath;
        this.callback = dataFetchCallback;
        this.essential = essential;
        this.type = type;
        this.futureTask = null;
    }

    public void execute(@NonNull HttpClient client, @NonNull ExecutorService executor) {
        // Request is starting, increment the counter.
        DataUtils.onRequestStart();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(requestUrl))
                .timeout(Duration.ofSeconds(120))
                .header("User-Agent", Utils.USER_AGENT)
                .GET()
                .build();

        CompletableFuture<T> mainOperation = sendRequestWithRetry(client, request, MAX_RETRY_COUNT)
                .thenApply(bytes -> isGzipUrl()
                        ? parseGzip(bytes)
                        : GSON.fromJson(new String(bytes, StandardCharsets.UTF_8), type)
                );
        this.futureTask = mainOperation;
        mainOperation.whenCompleteAsync((result, ex) -> {
            try {
                if (ex != null) {
                    Throwable cause = ex instanceof CompletionException ? ex.getCause() : ex;
                    if (cause instanceof CancellationException) {
                        callback.cancelled();
                    } else {
                        DataUtils.failedUris.add(requestUrl);
                        callback.failed(cause);
                    }
                } else {
                    callback.completed(result);
                }
            } finally {
                // Whether successful or not, the counter should decrease.
                DataUtils.onRequestFinish();
            }
        }, executor);
    }

    private CompletableFuture<byte[]> sendRequestWithRetry(HttpClient client, HttpRequest request, int retries) {
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .thenCompose(response -> {
                    if (response.statusCode() == 200) {
                        return CompletableFuture.completedFuture(response.body());
                    } else {
                        return CompletableFuture.failedFuture(new RuntimeException("HTTP Error Code: " + response.statusCode()));
                    }
                })
                .exceptionallyCompose(ex -> {
                    if (retries > 0) {
                        return sendRequestWithRetry(client, request, retries - 1);
                    }
                    return CompletableFuture.failedFuture(ex);
                });
    }

    public void cancel() {
        if (this.futureTask != null && !this.futureTask.isDone()) {
            this.futureTask.cancel(true);
        }
    }

    public String getURL() {
        return requestUrl;
    }

    public void setFallbackCDN() {
        this.requestUrl = requestUrl.replace(DataConstants.CDN_BASE_URL, DataConstants.FALLBACK_CDN_BASE_URL);
    }

    protected boolean isDone() {
        return futureTask != null && futureTask.isDone();
    }

    @SneakyThrows
    private T parseGzip(byte[] bytes) {
        try (var gzip = new GZIPInputStream(new ByteArrayInputStream(bytes));
             var reader = new BufferedReader(new InputStreamReader(gzip, StandardCharsets.UTF_8))) {
            return GSON.fromJson(reader, type);
        }
    }

    private boolean isGzipUrl() {
        String url = getURL();
        return url.endsWith(".gz") || url.endsWith(".gzip");
    }

}