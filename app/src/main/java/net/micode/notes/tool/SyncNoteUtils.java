package net.micode.notes.tool;

import android.app.Activity;
import android.content.Context;
import com.google.gson.Gson;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SyncNoteUtils {


    private static Gson gson = new Gson();
    //tag
    private static final String TAG = "chenqy";
    private static NoteHttpServer noteHttpServer;
    public static void SyncNote(Context context, SyncNoteItemData syncNoteItemData) throws JSONException {
        HttpUrl url = HttpUrl.parse(NoteRemoteConfig.generateUrl("/note/sync"));
        JSONObject body = new JSONObject();
        body.put("note_id", syncNoteItemData.note_id);
        body.put("version", syncNoteItemData.version);
        body.put("content", syncNoteItemData.content);
        body.put("note_token", syncNoteItemData.note_token);
        getServerIns().sendAsyncPostRequest(url, body.toString(), NoteHttpServer.BodyType.JSON, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                UIUtils.sendMsg((Activity) context, "同步失败");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                UIUtils.sendMsg((Activity) context, "同步成功");
            }
        });
    }

    private static NoteHttpServer getServerIns(){
        if(noteHttpServer == null){
            noteHttpServer = new NoteHttpServer();
        }
        return noteHttpServer;
    }


    public static ArrayList<SyncNoteItemData> extractNoteData(String jsonString) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonString);
        JSONArray dataArray = jsonObject.getJSONArray("data");
        ArrayList<SyncNoteItemData> noteDtos = new ArrayList<>();
        for(int i = 0; i < dataArray.length(); i++){
            JSONObject noteData = dataArray.getJSONObject(i);
            SyncNoteItemData noteDto = new SyncNoteItemData(
                    noteData.getInt("note_id"),
                    noteData.getString("user_id"),
                    noteData.getString("content"),
                    noteData.getInt("version"),
                    noteData.getString("note_token"),
                    noteData.getString("created_at"),
                    noteData.getString("updated_at")
            );
            noteDtos.add(noteDto);
        }
        return noteDtos;
    }




    public static class SyncNoteItemData{
        private long note_id;
        private String user_id;//phone
        private String content;
        private long version;
        private String note_token;
        private String created_at;
        private String updated_at;

        //cons
        public SyncNoteItemData(int note_id, String user_id, String content, int version, String note_token, String created_at, String updated_at) {
            this.note_id = note_id;
            this.user_id = user_id;
            this.content = content;
            this.version = version;
            this.note_token = note_token;
            this.created_at = created_at;
            this.updated_at = updated_at;
        }

        // get content
        public String getContent() {
            return content;
        }
        public long getNote_id() {
            return note_id;
        }
        //get token
        public String getNote_token() {
            return note_token;
        }
        //get version
        public long getVersion() {
            return version;
        }
    }


}
