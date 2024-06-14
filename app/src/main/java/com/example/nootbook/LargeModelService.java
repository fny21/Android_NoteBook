package com.example.nootbook;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LargeModelService {
    public static final String API_KEY = "LPJUF3L4G2HRz1NWmr4W1gjD";
    public static final String SECRET_KEY = "7iByCdz0P37SpIYdnJVPCUuiVIQkBep0";

    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder().build();
    private Gson gson;

    public LargeModelService() {
        gson = new Gson();
    }

    public void generateSummary(String input, LargeModelCallback callback) {
        new Thread(() -> {
            try {
                String summary = getSummaryFromModel(input);
                new Handler(Looper.getMainLooper()).post(() -> callback.onSummaryGenerated(summary));
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> callback.onError(e));
            }
        }).start();
    }

    private String getSummaryFromModel(String user_msg) throws IOException, JSONException {
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, String.format(
                "{\"messages\":[{\"role\":\"user\",\"content\":\"%s\"}],\"system\":\"你是一位帮助用户生成内容摘要的助手，你只说英语\",\"disable_search\":false,\"enable_citation\":false,\"response_format\":\"text\"}",
                user_msg
        ));

        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions?access_token=" + getAccessToken())
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();

        Response response = HTTP_CLIENT.newCall(request).execute();
        JSONObject json_feedback = new JSONObject(response.body().string());
        String result = json_feedback.getString("result");

        return result;
    }

    private String getAccessToken() throws IOException, JSONException {
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "grant_type=client_credentials&client_id=" + API_KEY
                + "&client_secret=" + SECRET_KEY);
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/oauth/2.0/token")
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        return new JSONObject(response.body().string()).getString("access_token");
    }
}
