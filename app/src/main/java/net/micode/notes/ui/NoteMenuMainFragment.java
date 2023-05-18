package net.micode.notes.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import net.micode.notes.R;

public class NoteMenuMainFragment extends Fragment {

    private Button btn_note_backup;
    private Button btn_note_sync;
    private Button btn_close;
    private Context context;
    private OnMenuCloseListener onMenuCloseListener;


    public NoteMenuMainFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.context = getActivity();
        View view = inflater.inflate(R.layout.note_pop_menu_main, container, false);
        bindView(view);
        return view;
    }

    void setCloseListener(OnMenuCloseListener callback) {
        this.onMenuCloseListener = callback;
    }

    private void bindView(View view) {
        btn_note_backup = view.findViewById(R.id.btn_note_backup);
        btn_note_sync = view.findViewById(R.id.btn_note_sync);
        btn_close = view.findViewById(R.id.btn_close);
        btn_close.setOnClickListener(v -> {
            View note_menu = ((Activity) context).findViewById(R.id.note_menu);
            AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
            alphaAnimation.setDuration(200);
            note_menu.startAnimation(alphaAnimation);
            note_menu.setVisibility(View.GONE);
        });
        this.btn_note_backup.setOnClickListener(new NoteMenuMainFragment.NoteMenuBackUpButtonClickListener(context));
        this.btn_note_sync.setOnClickListener(new NoteMenuSyncButtonClickListener(context));
    }

    // 点击事件: 将笔记备份到云端
    private static class NoteMenuBackUpButtonClickListener implements View.OnClickListener {
        private final Context context;

        NoteMenuBackUpButtonClickListener(Context context) {
            this.context = context;
        }

        @Override
        public void onClick(View v) {
            //切换到到备份笔记列表
            FragmentManager fm = ((Activity) context).getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out);
            Bundle args = new Bundle();
            args.putInt(NoteMenuListFragment.SHOW_LIST_KEY, NoteMenuListFragment.SHOW_BACKUP_LIST);
            NoteMenuListFragment noteMenuListFragment = new NoteMenuListFragment();
            noteMenuListFragment.setArguments(args);
            ft.replace(R.id.note_menu_container, noteMenuListFragment).commit();

        }
    }

    private static class NoteMenuSyncButtonClickListener implements View.OnClickListener {
        private final Context context;
        NoteMenuSyncButtonClickListener(Context context) {
            this.context = context;
        }
        @Override
        public void onClick(View v) {
            //切换到到备份笔记列表
            FragmentManager fm = ((Activity) context).getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out);
            Bundle args = new Bundle();
            args.putInt(NoteMenuListFragment.SHOW_LIST_KEY, NoteMenuListFragment.SHOW_SYNC_LIST);
            NoteMenuListFragment noteMenuListFragment = new NoteMenuListFragment();
            noteMenuListFragment.setArguments(args);
            ft.replace(R.id.note_menu_container, noteMenuListFragment).commit();

        }
    }

    interface OnMenuCloseListener {
        void onClose();
    }

}
