package net.micode.notes.tool;

import android.app.Activity;
import android.widget.Toast;
import net.micode.notes.callback.NoteCallback;

public class UIUtils {

    public static void runInUI(Activity activity, NoteCallback callback) {
        activity.runOnUiThread(callback::execute);
    }

    public static void sendMsg(Activity activity, String msg) {
        runInUI(activity, () -> Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show());
    }
}


