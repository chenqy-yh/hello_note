package net.micode.notes.data;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.webkit.HttpAuthHandler;
import net.micode.notes.callback.NoteCallback;
import net.micode.notes.tool.NoteHttpServer;
import net.micode.notes.tool.NoteRemoteConfig;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.logging.Handler;

public class Auth {


    //tag
    private static final String TAG = "chenqy";
    public static final String AUTHORITY = "micode_notes";
    public static final Uri CONTENT_NOTE_URI = Uri.parse("content://" + AUTHORITY + "/auth");
    public static final String END_POINT = "/auth/verifyauthtoken";
    public static final String AUTH_TOKEN_KEY = "auth_token";
    public static final String AUTH_PHONE_KEY = "auth_phone";
    public static final String AUTH_SHARED_NAME ="auth";
    private static NoteHttpServer server;

    public static void syncToken(Context context,String token_key,String auth_token){
        SharedPreferences sharedPreferences = context.getSharedPreferences(AUTH_SHARED_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(token_key, auth_token);
        editor.apply();
    }

    public static void removeToken(Context context,String token_key){
        SharedPreferences sp = context.getSharedPreferences(AUTH_SHARED_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(token_key);
    }

    public static String getAuthToken(Context context, String token_key){
        SharedPreferences sharedPreferences = context.getSharedPreferences(AUTH_SHARED_NAME, Context.MODE_PRIVATE);
        return  sharedPreferences.getString(token_key, null);
    }

    public static void checkAuthToken(Context context, Callback callback) throws IOException, JSONException {
        String auth_token = getAuthToken(context, AUTH_TOKEN_KEY);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("token", auth_token);
        HttpUrl url = HttpUrl.parse(NoteRemoteConfig.generateUrl(END_POINT));
        getServerInstance().sendAsyncPostRequest(url, jsonObject.toString(), NoteHttpServer.BodyType.FORM_DATA, callback);
    }

    public static NoteHttpServer getServerInstance(){
        if (server == null) {
            server = new NoteHttpServer();
        }
        return server;
    }

}
