package com.example.nootbook;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LargeModelService {
    private static final String API_KEY = "YOUR_API_KEY";  // Replace with your OpenAI API key
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final OkHttpClient client;
    private final Gson gson = new GsonBuilder().create();

    public LargeModelService() {
        // 设置代理
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("183.172.199.245", 10809));

        // 初始化 OkHttpClient 并设置代理和超时
        client = new OkHttpClient.Builder()
                .proxy(proxy)
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request();
                        Response response = null;
                        boolean responseOK = false;
                        int tryCount = 0;
                        while (!responseOK && tryCount < 3) {
                            try {
                                response = chain.proceed(request);
                                responseOK = response.isSuccessful();
                            } catch (Exception e) {
                                System.out.println("Request is not successful - " + tryCount);
                                e.printStackTrace();
                            } finally {
                                tryCount++;
                            }
                        }
                        if (response == null) {
                            throw new IOException("Unexpected end of stream");
                        }
                        return response;
                    }
                })
                .build();
    }

    public void generateSummary(String messageContent, LargeModelCallback callback) {
        ChatGptRequest.Message message = new ChatGptRequest.Message("user", messageContent);
        ChatGptRequest requestPayload = new ChatGptRequest("gpt-3.5-turbo", Arrays.asList(message));

        RequestBody body = RequestBody.create(
                gson.toJson(requestPayload), JSON);

        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + API_KEY)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                new Handler(Looper.getMainLooper()).post(() -> callback.onError(e));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    new Handler(Looper.getMainLooper()).post(() -> callback.onError(new IOException("Unexpected code " + response)));
                    return;
                }

                String responseBody = response.body().string();
                ChatGptResponse chatGptResponse = gson.fromJson(responseBody, ChatGptResponse.class);
                String summary = chatGptResponse.getChoices().get(0).getMessage().getContent().trim();

                new Handler(Looper.getMainLooper()).post(() -> callback.onSummaryGenerated(summary));
            }
        });
    }
}
