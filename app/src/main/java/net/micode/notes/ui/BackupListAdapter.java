package net.micode.notes.ui;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import net.micode.notes.R;
import net.micode.notes.data.Notes;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class BackupListAdapter extends CursorAdapter {

    //tag
    private static final String TAG = "chenqy";
    private ArrayList<Long> mSelectedIndex;
    private OnAllCheckedListener mCallback;

    public BackupListAdapter(Context context, Cursor c, OnAllCheckedListener callback) {
        super(context, c, 0);
        mSelectedIndex = new ArrayList<>();
        this.mCallback = callback;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.note_backup_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tv = (TextView) view.findViewById(R.id.note_backup_item_tv);
        tv.setText(cursor.getString(cursor.getColumnIndex(Notes.NoteColumns.SNIPPET)));
        NoteCheckBox cb = (NoteCheckBox) view.findViewById(R.id.note_backup_item_cb);
        cb.setNoteId(cursor.getLong(cursor.getColumnIndex(Notes.NoteColumns.ID)));
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "getCount" + getCount());
                if (cb.isChecked()) {
                    mSelectedIndex.remove(cb.getNoteId());
                    cb.setChecked(false);
                } else {
                    mSelectedIndex.add(cb.getNoteId());
                    cb.setChecked(true);
                }
                if (mSelectedIndex.size() == getCount()) {
                    mCallback.onAllChecked(true);
                }else{
                    mCallback.onAllChecked(false);
                }

            }
        });
        cb.setChecked(mSelectedIndex.contains(cb.getNoteId()));
    }

    public ArrayList<Long> getmSelectedIndex() {
        return mSelectedIndex;
    }

    public void changeAll(boolean isChecked) {
        Log.e(TAG, "changeAll");
        mSelectedIndex.clear();
        if (isChecked) {
            Cursor cursor = getCursor();
            if (cursor != null) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    mSelectedIndex.add(cursor.getLong(cursor.getColumnIndex(Notes.NoteColumns.ID)));
                    cursor.moveToNext();
                }
            }
        }
        Log.e(TAG, "mselectedList" + mSelectedIndex.toString());
        notifyDataSetChanged();
    }

    interface OnAllCheckedListener {
        void onAllChecked(boolean isChecked);
    }

}

