package net.micode.notes.tool;

import okhttp3.*;

import java.io.IOException;

public class NoteHttpServer {
    private final OkHttpClient client = new OkHttpClient();


    /**
    * sync get
    * @param url the url to send the request to
    * @return the response body as a string
    * @throws IOException if the request fails
    *
    * */

    public String sendSyncGetRequest(String url) throws IOException {
        Request req = new Request.Builder()
                .url(url)
                .build();
        try(Response res = client.newCall(req).execute()){
            return res.body().string();
        }
    }

    /**
    *
    * @param url the url to send the request to
    * @param callback the callback to be executed *when the request is complete
     * @return the response body as a string
    *
    * */
    public void sendAsyncGetRequest(String url, Callback callback){
        Request req = new Request.Builder()
                .url(url)
                .build();
        client.newCall(req).enqueue(callback);
    }


    /**
    * sync post
    *
    * @param url the url to send the request to
    * @param jsonBody the json body to send with the request
    * @return the response body as a string
    *
    * */
    public String sendSyncPostRequest(String url,String jsonBody)throws IOException{
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonBody);

        Request req = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        try(Response res = client.newCall(req).execute()){
            return res.body().string();
        }
    }

    public void sendAsyncPostRequest(String url,String jsonBody,Callback callback){
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonBody);

        Request req = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        client.newCall(req).enqueue(callback);
    }

}
