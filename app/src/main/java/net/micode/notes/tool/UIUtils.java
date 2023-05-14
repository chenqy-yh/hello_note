package net.micode.notes.tool;

import android.app.Activity;
import net.micode.notes.callback.NoteCallback;

public class UIUtils {
    public static void runInUI(Activity activity, NoteCallback callback){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                callback.execute();
            }
        });
    }

}

