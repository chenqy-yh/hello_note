package net.micode.notes.tool;

import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;

public class NoteHttpServer {

    public enum BodyType {
        JSON, FORM_DATA
    }

    private final OkHttpClient client = new OkHttpClient();
    public static final String FROM_DATA = "formdata";
    public static final String JSON = "json";

    /**
     * sync get
     *
     * @param url the url to send the request to
     * @return the response body as a string
     * @throws IOException if the request fails
     */

    public String sendSyncGetRequest(HttpUrl url) throws IOException {
        Request req = new Request.Builder()
                .url(url)
                .build();
        try (Response res = client.newCall(req).execute()) {
            return res.body().string();
        }
    }

    /**
     * @param url      the url to send the request to
     * @param callback the callback to be executed *when the request is complete
     * @return the response body as a string
     */
    public void sendAsyncGetRequest(HttpUrl url, Callback callback) {
        Request req = new Request.Builder()
                .url(url)
                .build();
        client.newCall(req).enqueue(callback);
    }


    /**
     * sync post
     *
     * @param url      the url to send the request to
     * @param body     the body of the request
     * @param bodyType the type of the body
     * @return the response body as a string
     */
    public String sendSyncPostRequest(HttpUrl url, String body, BodyType bodyType) throws IOException {
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
                    // Handle exception
                    return null;
                }
                requestBody = formBuilder.build();
            default:
                // Invalid body type
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

    /**
     * @param url      the url to send the request to
     * @param body     the body of the request
     * @param bodyType the type of the body
     */
    public void sendAsyncPostRequest(HttpUrl url, String body, BodyType bodyType, Callback callback) throws JSONException, IllegalArgumentException {
        RequestBody requestBody;
        switch (bodyType) {
            case JSON:
                requestBody = RequestBody.create(MediaType.parse("application/json"), body);
                break;
            case FORM_DATA:
                FormBody.Builder formBuilder = new FormBody.Builder();
                JSONObject jsonObject = new JSONObject(body);
                Iterator<String> keysIterator = jsonObject.keys();
                while (keysIterator.hasNext()) {
                    String key = keysIterator.next();
                    String value = jsonObject.getString(key);
                    formBuilder.add(key, value);
                }
                requestBody = formBuilder.build();
                break;
            default:
                throw new IllegalArgumentException("Invalid content type: " + bodyType);
        }

        Request req = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        client.newCall(req).enqueue(callback);
    }


}
