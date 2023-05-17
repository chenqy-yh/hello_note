package net.micode.notes.service;

import android.app.Activity;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import net.micode.notes.callback.NoteCallback;
import net.micode.notes.data.Auth;
import net.micode.notes.data.Notes;
import net.micode.notes.model.Note;
import net.micode.notes.tool.NoteHttpServer;
import net.micode.notes.tool.NoteRemoteConfig;
import net.micode.notes.tool.UIUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class BackupBoundService extends Service {

    //tag
    private static final String TAG = "chenqy";
    public static final String SERVICE_NAME = "BackupIntentService";
    public static final String BACKUP_NOTE_ACTION = "net.micode.notes.BACKUP_NOTE";

    private final IBinder binder = new LocalBinder();

    private final static String[] PROJECTION_NOTE = new String[]{
            Notes.NoteColumns.VERSION,
    };
    private final static String[] PROJECTION_DATA = new String[]{
            Notes.DataColumns.CONTENT,
            Notes.DataColumns.DATA5,
    };

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    public void backupNotes(List<Long> noteData) throws JSONException {
        NoteHttpServer noteHttpServer = new NoteHttpServer();
        String phone = Auth.getAuthToken(this, Auth.AUTH_PHONE_KEY);
        HttpUrl url = HttpUrl.parse(NoteRemoteConfig.generateUrl("/note/syncnote"));

        for (Long noteId : noteData) {
            JSONObject body = new JSONObject();

            // Get version number from Notes.CONTENT_NOTE_URI
            Cursor cursorNote = getContentResolver().query(
                    Notes.CONTENT_NOTE_URI,
                    PROJECTION_NOTE,
                    Notes.NoteColumns.ID + " = ?",
                    new String[]{noteId.toString()},
                    null
            );
            if (cursorNote != null && cursorNote.moveToFirst()) {
                int version = cursorNote.getInt(0);
                cursorNote.close();
                body.put("version", version);
            }

            // Get content and note token from Notes.CONTENT_DATA_URI
            Cursor cursorData = getContentResolver().query(
                    Notes.CONTENT_DATA_URI,
                    PROJECTION_DATA,
                    Notes.DataColumns.NOTE_ID + " = ?",
                    new String[]{noteId.toString()},
                    null
            );
            if (cursorData != null && cursorData.moveToFirst()) {
                String content = cursorData.getString(0);
                String noteToken = cursorData.getString(1);
                cursorData.close();
                body.put("content", content);
                body.put("note_token", noteToken);
            }

            // Send data to server
            noteHttpServer.sendAsyncPostRequest(url, body.toString(), NoteHttpServer.BodyType.JSON, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Log.e(TAG, "Backup failed: " + e.getMessage());
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    String resStr = response.body().string();
                    Log.e(TAG, "Backup response: " + resStr);

                    try {
                        JSONObject result = new JSONObject(resStr);
                        String noteToken = (String) result.get("data");

                        if (noteToken == null) {
                            Log.e(TAG, "Backup failed");
                        } else {
                            // Update note_token in local database
                            ContentValues values = new ContentValues();
                            values.put(Notes.DataColumns.DATA5, noteToken);
                            getContentResolver().update(
                                    Notes.CONTENT_DATA_URI,
                                    values,
                                    Notes.DataColumns.NOTE_ID + " = ?",
                                    new String[]{noteId.toString()}
                            );
                            Log.e(TAG, "Backup successful");
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Backup failed: " + e.getMessage());
                    }
                }
            });
        }
    }



    public class LocalBinder extends Binder {
        public BackupBoundService getService() {
            return BackupBoundService.this;
        }
    }
}
