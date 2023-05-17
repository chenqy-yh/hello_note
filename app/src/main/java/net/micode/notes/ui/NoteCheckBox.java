package net.micode.notes.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import net.micode.notes.R;

public class NoteCheckBox extends CheckBox implements CompoundButton.OnCheckedChangeListener {

    //tag
    private static final String TAG = "chenqy";
    private long note_id;

    private BitmapDrawable bitmapDrawable;


    public NoteCheckBox(Context context) {
        super(context);
    }

    public NoteCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NoteCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        init();
//        setEnabled(false);
        super.onAttachedToWindow();
    }

    private void init() {
        Drawable checkImg = this.getResources().getDrawable(R.drawable.backup_note_checked);
        Bitmap bitmap = Bitmap.createScaledBitmap(((BitmapDrawable) checkImg).getBitmap(), 25, 25, true);
        bitmapDrawable = new BitmapDrawable(getResources(), bitmap);
        this.setOnCheckedChangeListener(this);
    }

    public void setNoteId(long note_id) {
        this.note_id = note_id;
    }

    public long getNoteId() {
        return this.note_id;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.e(TAG, "onCheckedChanged ");
        if (isChecked) {
            Log.e(TAG, "onCheckedChanged: " + isChecked);
            this.setCompoundDrawablesWithIntrinsicBounds(null, null, bitmapDrawable, null);
        } else {
            Log.e(TAG, "onCheckedChanged: " + isChecked);
            this.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        }
    }
}
