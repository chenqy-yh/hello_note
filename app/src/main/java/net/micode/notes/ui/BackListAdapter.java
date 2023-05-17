package net.micode.notes.ui;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.TextView;
import net.micode.notes.R;
import net.micode.notes.data.Notes;

public class BackListAdapter extends CursorAdapter {
    public BackListAdapter(Context context,Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return  LayoutInflater.from(context).inflate(R.layout.note_backup_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tv = (TextView)view.findViewById(R.id.note_backup_item_tv);
        tv.setText(cursor.getString(cursor.getColumnIndex(Notes.NoteColumns.SNIPPET)));
        CheckBox cb = (CheckBox) view.findViewById(R.id.note_backup_item_cb);
        cb.setSelected(false);
    }
}
