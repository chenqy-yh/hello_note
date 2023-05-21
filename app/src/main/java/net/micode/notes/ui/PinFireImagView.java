package net.micode.notes.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.util.Log;
import androidx.annotation.Nullable;
import net.micode.notes.R;
import net.micode.notes.data.Notes;

public class PinFireImagView extends androidx.appcompat.widget.AppCompatImageView {
    //tag
    public static final String TAG = "chenqy";
    private Context context;
    private AnimationDrawable background;

    public PinFireImagView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public PinFireImagView(Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public PinFireImagView(Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }


    private void init() {
        this.setBackground(getResources().getDrawable(R.drawable.anim_fire));
        this.background = (AnimationDrawable) this.getBackground();
        this.setVisibility(GONE);
    }

    public void run() {
        if (this.getTag() == null) return;
        long note_id = (long) this.getTag();
        //find pin
        Cursor c = context.getContentResolver().query(Notes.CONTENT_NOTE_URI, new String[]{Notes.NoteColumns.PIN}, Notes.NoteColumns.ID + " = ?", new String[]{String.valueOf(note_id)}, null);
        if (c != null) {
            if(c.moveToFirst()){
                @SuppressLint("Range") int pin = c.getInt(c.getColumnIndex(Notes.NoteColumns.PIN));
                if (pin == 1) {
                    this.setVisibility(VISIBLE);
                    background.start();
                } else {
                    background.stop();
                    this.setVisibility(GONE);
                }
            }
            c.close();
        }
    }

}
