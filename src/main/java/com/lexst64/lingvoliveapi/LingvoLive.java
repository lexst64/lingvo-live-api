package com.lexst64.lingvoliveapi;

import com.google.gson.Gson;
import com.lexst64.lingvoliveapi.request.BaseRequest;
import com.lexst64.lingvoliveapi.response.BaseResponse;
import com.lexst64.lingvoliveapi.request.GetSuggests;
import com.lexst64.lingvoliveapi.request.GetWordForms;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.Request;
import okhttp3.FormBody;
import okhttp3.Call;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LingvoLive {

    private static final String API_V1_URL = "https://developers.lingvolive.com/api/v1/";
    private static final String API_V1_1_URL = "https://developers.lingvolive.com/api/v1.1/";

    private final String apiKey;
    private final OkHttpClient client;
    private final Gson gson;

    private String bearerToken;

    public LingvoLive(@NotNull String apiKey) throws AuthenticationException {
        this.apiKey = apiKey;
        client = new OkHttpClient();
        gson = new Gson();
        bearerToken = authenticate();
    }

    public <T extends BaseRequest<T, R>, R extends BaseResponse> R execute(BaseRequest<T, R> request) {
        R response = send(request);
        if (response.code() == 401) {
            bearerToken = authenticate();
            return send(request);
        }
        return response;
    }

    public <T extends BaseRequest<T, R>, R extends BaseResponse> void execute(T request, Callback<T, R> callback) {
        Callback<T, R> callback0 = new Callback<>() {
            @Override
            public void onResponse(T request, R response) {
                if (response.code() == 401) {
                    bearerToken = authenticate();
                    send(request, callback);
                } else {
                    callback.onResponse(request, response);
                }
            }

            @Override
            public void onFailure(T request, IOException e) {
                callback.onFailure(request, e);
            }
        };
        send(request, callback0);
    }

    private <T extends BaseRequest<T, R>, R extends BaseResponse> void send(final T request, final Callback<T, R> callback) {
        client.newCall(createRequest(request)).enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                R result = null;
                Exception exception = null;

                try {
                    result = createResponse(request, response);
                } catch (Exception e) {
                    exception = e;
                }

                if (result != null) {
                    callback.onResponse(request, result);
                } else if (exception != null) {
                    callback.onFailure(request, exception instanceof IOException ? (IOException) exception : new IOException(exception));
                } else {
                    callback.onFailure(request, new IOException("response json is null or empty"));
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                callback.onFailure(request, e);
            }
        });
    }

    private <T extends BaseRequest<T, R>, R extends BaseResponse> R send(final BaseRequest<T, R> request) {
        try (Response response = client.newCall(createRequest(request)).execute()) {
            return createResponse(request, response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private <T extends BaseRequest<T, R>, R extends BaseResponse> R createResponse(BaseRequest<T, R> request, Response response) throws IOException {
        String json = response.body().string();

        if (request instanceof GetSuggests) json = "{suggests: " + json + "}";
        if (request instanceof GetWordForms) json = "{lexemModels: " + json + "}";

        if (response.code() < 200 || response.code() >= 300) {
            json = createJsonOnError(response.code(), response.message(), json);
        }
        return gson.fromJson(json, request.getResponseType());
    }

    private String createJsonOnError(int code, String message, String errorDescription) {
        return String.format(
                "{\"isOk\": false, \"code\": %d, \"message\": \"%s\", \"errorDescription\": %s}",
                code, message, errorDescription
        );
    }

    private Request createRequest(BaseRequest<?, ?> request) {
        return new Request.Builder()
                .header("Authorization", "Bearer " + bearerToken)
                .url(API_V1_URL + request.getMethod() + createQueryString(request))
                .build();
    }

    private String createQueryString(BaseRequest<?, ?> request) {
        List<String> queries = new ArrayList<>();
        for (Map.Entry<String, Object> query : request.getQueries().entrySet()) {
            queries.add(query.getKey() + "=" + query.getValue());
        }
        return "?" + String.join("&", queries);
    }

    private String authenticate() throws AuthenticationException {
        Request request = new Request.Builder()
                .post(new FormBody.Builder().build())
                .header("Authorization", "Basic " + apiKey)
                .url(API_V1_1_URL + "authenticate")
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.code() == 401) {
                throw new AuthenticationException("invalid api key");
            }
            return response.body().string();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
