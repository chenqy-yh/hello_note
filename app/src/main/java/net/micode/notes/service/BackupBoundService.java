package net.micode.notes.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class BackupBoundService extends Service {

    //tag
    private static final String TAG = "chenqy";
    public static final String SERVICE_NAME = "BackupIntentService";
    public static final String BACKUP_NOTE_ACTION = "net.micode.notes.BACKUP_NOTE";

    private final IBinder binder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void backupNotes(String noteData) {
        Log.e(TAG, "backupNotes: " + noteData);
    }


    public class LocalBinder extends Binder {
        public BackupBoundService getService() {
            return BackupBoundService.this;
        }
    }
}
