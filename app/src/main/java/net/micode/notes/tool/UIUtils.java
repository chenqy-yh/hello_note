package net.micode.notes.tool;

import android.app.Activity;
import android.widget.Toast;
import net.micode.notes.callback.NoteCallback;
import okhttp3.Callback;

public class UIUtils {
    public static void runInUI(Activity activity, NoteCallback callback) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                callback.execute();
            }
        });
    }

    public static void sendMsg(Activity activity, String msg) {
        runInUI(activity, () -> Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show());
    }

}


