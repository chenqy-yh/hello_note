package net.micode.notes.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import net.micode.notes.R;
import net.micode.notes.tool.SyncNoteUtils;

import java.util.ArrayList;
import java.util.List;

public class SyncListAdapter extends BaseAdapter {
    private List<SyncNoteUtils.SyncNoteItemData> mData;
    private Context mContext;
    private List<SyncNoteUtils.SyncNoteItemData> mSelectedList;

    public SyncListAdapter(List<SyncNoteUtils.SyncNoteItemData> data, Context context) {
        mData = data;
        mContext = context;
        mSelectedList = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public SyncNoteUtils.SyncNoteItemData getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.test_list_item, parent, false);
            vh = new ViewHolder();
            vh.tv_note_id = convertView.findViewById(R.id.numberTextView);
            vh.tv_note_snippet = convertView.findViewById(R.id.textTextView);
            vh.checkBox = convertView.findViewById(R.id.checkBox);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }

        SyncNoteUtils.SyncNoteItemData item = mData.get(position);
        vh.tv_note_id.setText(String.valueOf(position + 1) + ".");
        vh.tv_note_snippet.setText(item.getContent());
        vh.checkBox.setTag(item);

        convertView.setOnClickListener(v -> {
            if (vh.checkBox.isChecked()) {
                vh.checkBox.setChecked(false);
                mSelectedList.remove(item);
            } else {
                vh.checkBox.setChecked(true);
                mSelectedList.add(item);
            }
        });

        return convertView;
    }

    public List<SyncNoteUtils.SyncNoteItemData> getSelectedList() {
        return mSelectedList;
    }

    static class ViewHolder {
        private TextView tv_note_id;
        private TextView tv_note_snippet;
        private CheckBox checkBox;
    }
}
