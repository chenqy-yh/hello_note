package net.micode.notes.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import net.micode.notes.R;
import net.micode.notes.data.Notes;

import java.util.ArrayList;
import java.util.LinkedList;

public class NoteMenuListFragment extends Fragment implements View.OnClickListener {


    //tag
    private static final String TAG = "chenqy";
    private ListView lv_note_list;
    private Button btn_back;
    private Button btn_close;
    private Button btn_backup;
    private BackupListAdapter adapter;
    private FragmentManager fm;
    private Switch note_pop_menu_list_switch;


    public static final String SELECTED_ID_LIST_KEY = "selected_backup_list";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.note_pop_menu_list, container, false);
        bindView(view);
        return view;
    }

    private void bindView(View container) {
        lv_note_list = container.findViewById(R.id.note_pop_menu_lv);
        btn_back = container.findViewById(R.id.note_pop_menu_list_btn_back);
        btn_close = container.findViewById(R.id.note_pop_menu_list_btn_close);
        btn_backup = container.findViewById(R.id.note_pop_menu_list_btn_backup);
        note_pop_menu_list_switch = container.findViewById(R.id.note_pop_menu_list_switch);
        note_pop_menu_list_switch.setOnCheckedChangeListener((buttonView, isChecked) -> adapter.changeAll(isChecked));
        btn_back.setOnClickListener(this);
        btn_close.setOnClickListener(this);
        btn_backup.setOnClickListener(this);
        fm = getActivity().getFragmentManager();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //TODO 需要备份的note清单列表
        lv_note_list = getActivity().findViewById(R.id.note_pop_menu_lv);
        //Cursor cursor = getActivity().getContentResolver().query(Notes.CONTENT_NOTE_URI, null, null, null, null);
        Cursor cursor = getActivity().getContentResolver().query(Notes.CONTENT_NOTE_URI, null, Notes.NoteColumns.ID + " > ?", new String[]{"0"}, null);
        //遍历cursor 将id 放入list
//        ArrayList<Long> id_list = new ArrayList<>();

        adapter = new BackupListAdapter(getActivity(), cursor, isChecked -> note_pop_menu_list_switch.setChecked(isChecked));
        lv_note_list.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.note_pop_menu_list_btn_backup:
                //TODO 备份笔记
                //TODO 启动 backupService 将笔记备份到云端
                Intent it = new Intent(NotesListActivity.BACKUP_ACTION);
                //从lv_note_list 获得勾选的笔记的id
                ArrayList<Long> selectedIndex = adapter.getmSelectedIndex();
                Bundle args = new Bundle();
                long[] arg_list;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    args.putLongArray(SELECTED_ID_LIST_KEY, selectedIndex.stream().mapToLong(Long::longValue).toArray());
                } else {
                    arg_list = new long[selectedIndex.size()];
                    for (int i = 0; i < selectedIndex.size(); i++) {
                        arg_list[i] = selectedIndex.get(i);
                    }
                    args.putLongArray(SELECTED_ID_LIST_KEY, arg_list);
                }
                it.putExtras(args);
                getActivity().sendBroadcast(it);
                break;
            case R.id.note_pop_menu_list_btn_back:
                //TODO 回退到上一级
                FragmentTransaction ft = fm.beginTransaction();
                ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out);
                ft.replace(R.id.ffffkkkkkk, new NoteMenuMainFragment()).commit();
                break;
            case R.id.note_pop_menu_list_btn_close:
                //TODO 关闭当前页面
//                getActivity().findViewById(R.)
                View note_menu = getActivity().findViewById(R.id.xxxxxss);
                AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
                alphaAnimation.setDuration(200);
                note_menu.startAnimation(alphaAnimation);
                note_menu.setVisibility(View.GONE);
                break;
        }
    }
}
