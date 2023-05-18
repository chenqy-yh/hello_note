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
        mSelectedList= new ArrayList<>();
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
        ViewHolder vh = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.test_list_item, null, false);
            vh = new ViewHolder();
            vh.tv_note_id = convertView.findViewById(R.id.numberTextView);
            vh.tv_note_snippet = convertView.findViewById(R.id.textTextView);
            vh.checkBox = convertView.findViewById(R.id.checkBox);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }
        vh.tv_note_id.setText(String.valueOf(position+1)+".");
        vh.tv_note_snippet.setText((CharSequence) mData.get(position).getContent());
        vh.checkBox.setTag( mData.get(position));
        ViewHolder finalVh = vh;
        convertView.setOnClickListener(v -> {
            if (finalVh.checkBox.isChecked()) {
                finalVh.checkBox.setChecked(false);
                mSelectedList.remove(mData.get(position));
            } else {
                finalVh.checkBox.setChecked(true);
                mSelectedList.add(mData.get(position));
            }
        });
        return convertView;
    }

    public List<SyncNoteUtils.SyncNoteItemData> getSelectedList() {
        return mSelectedList;
    }

    static class ViewHolder {
        //TODO
        private TextView tv_note_id;
        private TextView tv_note_snippet;
        private CheckBox checkBox;

    }


}
