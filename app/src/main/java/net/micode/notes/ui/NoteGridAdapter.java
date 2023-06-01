package net.micode.notes.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import net.micode.notes.R;

import java.util.List;

public class NoteGridAdapter extends BaseAdapter {
    private Context context;
    private List<NoteGridDataItem> dataList;

    public NoteGridAdapter(Context context, List<NoteGridDataItem> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.note_grid_item, null);
        } else {
            view = convertView;
        }

        // 设置每个格子的内容
        TextView textView = view.findViewById(R.id.grid_item_text);
        ImageView imageView = view.findViewById(R.id.grid_item_image);
        textView.setText(dataList.get(position).getDesc());
        imageView.setBackgroundResource(dataList.get(position).getPicId());
        return view;
    }
}
