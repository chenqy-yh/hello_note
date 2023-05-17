package net.micode.notes.tool;

import android.app.Activity;
import android.content.Context;
import android.nfc.Tag;
import android.util.Log;
import android.widget.Toast;
import net.micode.notes.callback.NoteCallback;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class SyncNoteUtils {

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
                UIUtils.runInUI((Activity) context, () -> Toast.makeText(context, "同步失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                UIUtils.runInUI((Activity) context, () -> Toast.makeText(context, "同步成功", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private static NoteHttpServer getServerIns(){
        if(noteHttpServer == null){
            noteHttpServer = new NoteHttpServer();
        }
        return noteHttpServer;
    }

    public static class SyncNoteItemData{
        private long note_id;
        private long version;
        private String content;
        private String note_token;

        //cons
        public SyncNoteItemData(long note_id, long version, String content, String note_token) {
            this.note_id = note_id;
            this.version = version;
            this.content = content;
            this.note_token = note_token;
        }

    }


}
