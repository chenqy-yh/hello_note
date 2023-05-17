package net.micode.notes.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ListView;
import net.micode.notes.R;
import net.micode.notes.data.Notes;

import java.util.ArrayList;

public class NoteMenuListFragment extends Fragment implements View.OnClickListener {

    private ListView lv_note_list;
    private Button btn_back;
    private Button btn_close;
    private Button btn_backup;
    private FragmentManager fm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.note_pop_menu_list, container, false);
        bindView(view);
        return view;
    }

    private void bindView(View container){
        lv_note_list = container.findViewById(R.id.note_pop_menu_lv);
        btn_back = container.findViewById(R.id.note_pop_menu_list_btn_back);
        btn_close = container.findViewById(R.id.note_pop_menu_list_btn_close);
        btn_backup = container.findViewById(R.id.note_pop_menu_list_btn_backup);
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

        BackListAdapter adapter = new BackListAdapter(getActivity(),cursor);
        lv_note_list.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id){
            case R.id.note_pop_menu_list_btn_backup:
                //TODO 备份笔记
                //TODO 启动 backupService 将笔记备份到云端
                Intent it = new Intent(NotesListActivity.BACKUP_ACTION);
                //从lv_note_list 获得勾选的笔记的id



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
