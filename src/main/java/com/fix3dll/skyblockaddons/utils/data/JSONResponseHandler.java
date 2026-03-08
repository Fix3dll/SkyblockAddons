package com.fix3dll.skyblockaddons.utils.data;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

/**
 * A {@link ResponseHandler} that deserializes a JSON response body to the given type if the
 * response status is 200. Supports gzip-compressed responses.
 * If the response has any other status code, {@link HttpResponseException} is thrown.
 * @param <T> the type to deserialize the JSON to
 */
public class JSONResponseHandler<T> implements ResponseHandler<T> {

    private static final Gson GSON = SkyblockAddons.getGson();
    private final Type type;
    private final boolean gzip;

    /**
     * Creates a new {@code JSONResponseHandler} with the given type. Response body is treated as plain JSON.
     * @param type the type to deserialize the response to
     */
    public JSONResponseHandler(Type type) {
        this(type, false);
    }

    /**
     * Creates a new {@code JSONResponseHandler} with the given type.
     * @param type the type to deserialize the response to
     * @param gzip whether the response body is gzip-compressed
     */
    public JSONResponseHandler(Type type, boolean gzip) {
        this.type = type;
        this.gzip = gzip;
    }

    @Override
    public T handleResponse(HttpResponse response) throws IOException {
        int status = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();

        if (status == 200) {
            if (entity == null) return null;

            if (gzip) {
                byte[] bytes = EntityUtils.toByteArray(entity);
                try (var gzipStream = new GZIPInputStream(new ByteArrayInputStream(bytes));
                     var reader = new BufferedReader(new InputStreamReader(gzipStream, StandardCharsets.UTF_8))) {
                    return GSON.fromJson(reader, type);
                }
            }

            return GSON.fromJson(EntityUtils.toString(entity, StandardCharsets.UTF_8), type);
        } else {
            EntityUtils.consume(entity);
            throw new HttpResponseException(status, "Unexpected response status: " + status);
        }
    }

}