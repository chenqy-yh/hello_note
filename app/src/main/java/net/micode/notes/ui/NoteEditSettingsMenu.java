package net.micode.notes.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;
import net.micode.notes.R;

import java.util.ArrayList;
import java.util.List;

public class NoteEditSettingsMenu extends Fragment {

    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.context = getActivity();
        View view = inflater.inflate(R.layout.note_edit_main_settings_menu, container, false);
        bindView(view);
        return view;
    }

    private void bindView(View convertView){
        List<NoteGridDataItem> mData = new ArrayList<>();
        mData.add(new NoteGridDataItem(R.drawable.pic_1, "字体设置"));
        mData.add(new NoteGridDataItem(R.drawable.more, ""));
        NoteGridAdapter adapter = new NoteGridAdapter(context, mData);
        GridView gridView = convertView.findViewById(R.id.gridview);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            //判断是不是最后一个元素
            if (position == parent.getCount() - 1) {
                Toast.makeText(context, "更多功能,请别期待!", Toast.LENGTH_SHORT).show();
                return;
            }
            FragmentManager fm = ((Activity) context).getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out);
            NoteEditFontSettingsFragment noteEditFontSettingsFragment = new NoteEditFontSettingsFragment();
            ft.replace(R.id.note_edit_settings_container, noteEditFontSettingsFragment).commit();
        });
    }

}
