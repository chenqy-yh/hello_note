package net.micode.notes.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class NoteTypefaceAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<ItemData> dataList;

    public NoteTypefaceAdapter(Context context, int resource, ArrayList<ItemData> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public ItemData getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, null);
            holder = new ViewHolder();
            holder.typeface_name = convertView.findViewById(android.R.id.text1);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        holder.typeface_name.setText(dataList.get(position).typeface_name);
        holder.typeface_name.setTypeface(dataList.get(position).typeface);
        return convertView;
    }


    static class ItemData{
        private String typeface_name;
        private Typeface typeface;

        public ItemData(String typeface_name, Typeface typeface){
            this.typeface_name = typeface_name;
            this.typeface = typeface;
        }

        //getter
        public String getTypeface_name(){
            return typeface_name;
        }

        public Typeface getTypeface(){
            return typeface;
        }
    }
    private static class ViewHolder{
        private TextView typeface_name;
    }
}
