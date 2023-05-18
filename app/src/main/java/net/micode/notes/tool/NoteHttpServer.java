package net.micode.notes.tool;

import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;

public class NoteHttpServer {

    private final OkHttpClient client = new OkHttpClient();
    public static final String FORM_DATA = "formdata";
    public static final String JSON = "json";

    public String sendSyncGetRequest(HttpUrl url) throws IOException {
        Request req = new Request.Builder()
                .url(url)
                .build();
        try (Response res = client.newCall(req).execute()) {
            return res.body().string();
        }
    }

    public void sendAsyncGetRequest(HttpUrl url, Callback callback) {
        Request req = new Request.Builder()
                .url(url)
                .build();
        client.newCall(req).enqueue(callback);
    }

    public String sendSyncPostRequest(HttpUrl url, String body, BodyType bodyType) throws IOException {
        RequestBody requestBody = createRequestBody(body, bodyType);

        if (requestBody == null) {
            return null;
        }

        Request req = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        try (Response res = client.newCall(req).execute()) {
            return res.body().string();
        }
    }

    public void sendAsyncPostRequest(HttpUrl url, String body, BodyType bodyType, Callback callback) {
        RequestBody requestBody = createRequestBody(body, bodyType);

        if (requestBody == null) {
            return;
        }

        Request req = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        client.newCall(req).enqueue(callback);
    }

    private RequestBody createRequestBody(String body, BodyType bodyType) {
        RequestBody requestBody;
        switch (bodyType) {
            case JSON:
                requestBody = RequestBody.create(MediaType.parse("application/json"), body);
                break;
            case FORM_DATA:
                FormBody.Builder formBuilder = new FormBody.Builder();
                try {
                    JSONObject jsonObject = new JSONObject(body);
                    Iterator<String> keys = jsonObject.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        String value = jsonObject.getString(key);
                        formBuilder.add(key, value);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                }
                requestBody = formBuilder.build();
                break;
            default:
                return null;
        }
        return requestBody;
    }

    public enum BodyType {
        JSON, FORM_DATA
    }
}
