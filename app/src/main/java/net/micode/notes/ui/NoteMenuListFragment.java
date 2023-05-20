package net.micode.notes.ui;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.appwidget.AppWidgetManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Switch;
import androidx.appcompat.app.AlertDialog;
import net.micode.notes.R;
import net.micode.notes.data.Auth;
import net.micode.notes.data.Notes;
import net.micode.notes.model.WorkingNote;
import net.micode.notes.tool.*;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NoteMenuListFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "chenqy";
    private ListView lv_note_list;
    private Button btn_back;
    private Button btn_backup;
    private Button btn_sync;
    private BackupListAdapter backupListAdapter;
    private SyncListAdapter syncListAdapter;
    private FragmentManager fm;
    private Switch note_pop_menu_list_switch;
    private int now_list_type;

    public static final String SELECTED_ID_LIST_KEY = "selected_backup_list";
    public static final String SHOW_LIST_KEY = "show_list_key";
    public static final int SHOW_BACKUP_LIST = 0;
    public static final int SHOW_SYNC_LIST = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        now_list_type = getArguments().getInt(SHOW_LIST_KEY);
        View view = null;
        switch (now_list_type) {
            case SHOW_BACKUP_LIST:
                // TODO: Display backup note list
                view = inflater.inflate(R.layout.note_pop_menu_backup_list, container, false);
                break;
            case SHOW_SYNC_LIST:
                // TODO: Display sync note list
                view = inflater.inflate(R.layout.note_pop_menu_sync_list, container, false);
                break;
        }
        bindView(view);
        return view;
    }

    private void bindView(View container) {
        switch (now_list_type) {
            case SHOW_BACKUP_LIST:
                // TODO: Bind views for backup note list
                btn_backup = container.findViewById(R.id.note_pop_menu_list_btn_backup);
                note_pop_menu_list_switch = container.findViewById(R.id.note_pop_menu_list_switch);
                note_pop_menu_list_switch.setOnCheckedChangeListener((buttonView, isChecked) -> backupListAdapter.changeAll(isChecked));
                btn_backup.setOnClickListener(this);
                break;
            case SHOW_SYNC_LIST:
                // TODO: Bind views for sync note list
                btn_sync = container.findViewById(R.id.note_pop_menu_list_btn_sync);
                btn_sync.setOnClickListener(this);
                break;
            default:
                UIUtils.sendMsg(getActivity(), "bindView: now_list_type error");
                break;
        }

        lv_note_list = container.findViewById(R.id.note_pop_menu_lv);
        btn_back = container.findViewById(R.id.note_pop_menu_list_btn_back);
        fm = getActivity().getFragmentManager();
        btn_back.setOnClickListener(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        switch (now_list_type) {
            case SHOW_BACKUP_LIST:
                // TODO: Show backup note list
                lv_note_list = getActivity().findViewById(R.id.note_pop_menu_lv);
                Cursor cursor = getActivity().getContentResolver().query(Notes.CONTENT_NOTE_URI, null, Notes.NoteColumns.ID + " > ?", new String[]{"0"}, null);
                backupListAdapter = new BackupListAdapter(getActivity(), cursor, isChecked -> note_pop_menu_list_switch.setChecked(isChecked));
                lv_note_list.setAdapter(backupListAdapter);
                break;
            case SHOW_SYNC_LIST:
                // TODO: Show sync note list
                SharedPreferences sp = getActivity().getSharedPreferences(Auth.AUTH_SHARED_NAME, Context.MODE_PRIVATE);
//                SharedPreferences.Editor editor = sp.edit();
                String phone = sp.getString(Auth.AUTH_PHONE_KEY, "");
                HttpUrl url = HttpUrl.parse(NoteRemoteConfig.generateUrl("/note/getnotes?user_id=" + phone));
                NoteHttpServer server = new NoteHttpServer();
                server.sendAsyncGetRequest(url, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        UIUtils.sendMsg(getActivity(), "网络错误，获取同步列表失败");
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String resStr = response.body().string();
                        ArrayList<SyncNoteUtils.SyncNoteItemData> noteDtos;
                        try {
                            noteDtos = SyncNoteUtils.extractNoteData(resStr);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        syncListAdapter = new SyncListAdapter(noteDtos, getActivity());
                        UIUtils.runInUI(getActivity(), () -> lv_note_list.setAdapter(syncListAdapter));
                    }
                });
                break;
        }
    }

    @SuppressLint("Range")
    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.note_pop_menu_list_btn_backup:
                // TODO: Start backupService to backup notes to the cloud
                Intent it = new Intent(NotesListActivity.BACKUP_ACTION);
                ArrayList<Long> selectedIndex = backupListAdapter.getmSelectedIndex();
                Bundle args = new Bundle();
                args.putLongArray(SELECTED_ID_LIST_KEY, selectedIndex.stream().mapToLong(Long::longValue).toArray());
                it.putExtras(args);
                getActivity().sendBroadcast(it);
                break;
            case R.id.note_pop_menu_list_btn_back:
                // TODO: Navigate back to the previous fragment
                FragmentTransaction ft = fm.beginTransaction();
                ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out);
                ft.replace(R.id.note_menu_container, new NoteMenuMainFragment()).commit();
                break;

            case R.id.note_pop_menu_list_btn_sync:
                // TODO: Perform sync
                UIUtils.sendMsg(getActivity(), "同步中...");
                List<SyncNoteUtils.SyncNoteItemData> selectedList = syncListAdapter.getSelectedList();
                if(selectedList.isEmpty()){
                    UIUtils.sendMsg(getActivity(), "请选择要同步的笔记");
                    return;
                }
                int count = 0;
                for (SyncNoteUtils.SyncNoteItemData itemData : selectedList) {
                    count++;
                    Cursor cursor = getActivity().getContentResolver().query(Notes.CONTENT_DATA_URI, null, Notes.DataColumns.DATA5 + " = ?", new String[]{itemData.getNote_token()}, null);
                    if (cursor.moveToNext()) {
                        long noteId = cursor.getLong(cursor.getColumnIndex(Notes.DataColumns.NOTE_ID));
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("同步提示");
                        builder.setMessage("同步第" + count + "条笔记会覆盖原来的笔记，是否继续？");
                        builder.setPositiveButton("继续", (dialog, which) -> {
                            ContentValues values = new ContentValues();
                            values.put(Notes.DataColumns.CONTENT, itemData.getContent());
                            getActivity().getContentResolver().update(Notes.CONTENT_DATA_URI, values, Notes.DataColumns.DATA5 + " = ?", new String[]{itemData.getNote_token()});
                            values.clear();
                            values.put(Notes.NoteColumns.VERSION, itemData.getVersion());
                            getActivity().getContentResolver().update(Notes.CONTENT_NOTE_URI, values, Notes.NoteColumns.ID + " = ?", new String[]{String.valueOf(noteId)});
                            UIUtils.sendMsg(getActivity(), "同步成功");
                            dialog.cancel();
                        });
                        builder.setNegativeButton("取消", (dialog, which) -> dialog.cancel());
                        builder.show();
                    } else {
                        WorkingNote note = WorkingNote.createEmptyNote(getActivity(), Notes.ID_ROOT_FOLDER, AppWidgetManager.INVALID_APPWIDGET_ID, Notes.TYPE_WIDGET_INVALIDE, ResourceParser.YELLOW);
                        note.setWorkingText(itemData.getContent());
                        note.saveNote();
                        ContentValues values = new ContentValues();
                        values.put(Notes.DataColumns.DATA5, itemData.getNote_token());
                        getActivity().getContentResolver().update(Notes.CONTENT_DATA_URI, values, Notes.DataColumns.NOTE_ID + " = ?", new String[]{String.valueOf(note.getNoteId())});
                        values.clear();
                        values.put(Notes.NoteColumns.VERSION, itemData.getVersion());
                        getActivity().getContentResolver().update(Notes.CONTENT_NOTE_URI, values, Notes.NoteColumns.ID + " = ?", new String[]{String.valueOf(note.getNoteId())});
                        UIUtils.sendMsg(getActivity(), "同步成功");
                    }
                }
                break;
        }
    }
}

