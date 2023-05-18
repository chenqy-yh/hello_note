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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Switch;
import androidx.appcompat.app.AlertDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NoteMenuListFragment extends Fragment implements View.OnClickListener {

    //tag
    private static final String TAG = "chenqy";
    private ListView lv_note_list;
    private Button btn_back;
    private Button btn_close;
    //备份按钮
    private Button btn_backup;
    //同步按钮
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
        now_list_type = (int) getArguments().get(SHOW_LIST_KEY);
        View view = null;
        switch (now_list_type){
            case SHOW_BACKUP_LIST:
                //TODO 需要备份的note清单列表
                view = inflater.inflate(R.layout.note_pop_menu_backup_list, container, false);
                break;
            case SHOW_SYNC_LIST:
                //TODO 需要同步的note清单列表
                view = inflater.inflate(R.layout.note_pop_menu_sync_list, container, false);
                break;
        }
        bindView(view);
        return view;
    }

    private void bindView(View container) {
        switch (now_list_type){
            case SHOW_BACKUP_LIST:
                //TODO 需要备份的note清单列表
                btn_backup = container.findViewById(R.id.note_pop_menu_list_btn_backup);
                note_pop_menu_list_switch = container.findViewById(R.id.note_pop_menu_list_switch);
                note_pop_menu_list_switch.setOnCheckedChangeListener((buttonView, isChecked) -> backupListAdapter.changeAll(isChecked));
                btn_backup.setOnClickListener(this);
                break;
            case SHOW_SYNC_LIST:
                //TODO 需要同步的note清单列表
                btn_sync = container.findViewById(R.id.note_pop_menu_list_btn_sync);
                btn_sync.setOnClickListener(this);
                break;
            default:
                UIUtils.sendMsg(getActivity(), "bindView: now_list_type error");
                break;
        }

        lv_note_list = container.findViewById(R.id.note_pop_menu_lv);
        btn_back = container.findViewById(R.id.note_pop_menu_list_btn_back);
        btn_close = container.findViewById(R.id.note_pop_menu_list_btn_close);
        fm = getActivity().getFragmentManager();
        btn_back.setOnClickListener(this);
        btn_close.setOnClickListener(this);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        switch (now_list_type){
            case SHOW_BACKUP_LIST:
                //TODO 需要备份的note清单列表
                lv_note_list = getActivity().findViewById(R.id.note_pop_menu_lv);
                //Cursor cursor = getActivity().getContentResolver().query(Notes.CONTENT_NOTE_URI, null, null, null, null);
                Cursor cursor = getActivity().getContentResolver().query(Notes.CONTENT_NOTE_URI, null, Notes.NoteColumns.ID + " > ?", new String[]{"0"}, null);
                //遍历cursor 将id 放入list
                backupListAdapter = new BackupListAdapter(getActivity(), cursor, isChecked -> note_pop_menu_list_switch.setChecked(isChecked));
                lv_note_list.setAdapter(backupListAdapter);
                break;
            case SHOW_SYNC_LIST:
                //TODO sync
                //从sp获取phone
                SharedPreferences sp = getActivity().getSharedPreferences(Auth.AUTH_SHARED_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
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
                        try {
                            List<SyncNoteUtils.SyncNoteItemData> noteDtos = SyncNoteUtils.extractNoteData(resStr);
                            syncListAdapter = new SyncListAdapter(noteDtos,getActivity());
                            UIUtils.runInUI(getActivity(), () -> lv_note_list.setAdapter(syncListAdapter));
                        } catch (JSONException e) {
                            UIUtils.sendMsg(getActivity(), "网络错误，获取同步列表失败");
                        }

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
                //TODO 启动 backupService 将笔记备份到云端
                Intent it = new Intent(NotesListActivity.BACKUP_ACTION);
                //从lv_note_list 获得勾选的笔记的id
                ArrayList<Long> selectedIndex = backupListAdapter.getmSelectedIndex();
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
                ft.replace(R.id.note_menu_container, new NoteMenuMainFragment()).commit();
                break;
            case R.id.note_pop_menu_list_btn_close:
                //TODO 关闭当前页面
//                getActivity().findViewById(R.)
                View note_menu = getActivity().findViewById(R.id.note_menu);
                AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
                alphaAnimation.setDuration(200);
                note_menu.startAnimation(alphaAnimation);
                note_menu.setVisibility(View.GONE);
                break;
            case R.id.note_pop_menu_list_btn_sync:
                //TODO sync
                UIUtils.sendMsg(getActivity(), "同步中...");
                List<SyncNoteUtils.SyncNoteItemData> selectedList = syncListAdapter.getSelectedList();
                int count = 0;
                for (SyncNoteUtils.SyncNoteItemData itemData : selectedList) {
                    count++;
                    //根据SyncNoteItemData中的token 查找数据库中是否存在对应的笔记
                    Cursor cursor = getActivity().getContentResolver().query(Notes.CONTENT_DATA_URI, null, Notes.DataColumns.DATA5 + " = ?", new String[]{itemData.getNote_token()}, null);
                    //如果存在
                    if(cursor.moveToNext()){
                        //获取这个笔记的id
                        long noteId = cursor.getLong(cursor.getColumnIndex(Notes.DataColumns.NOTE_ID));
                        //触发alert提示用户 同步 这个笔记会覆盖原来的笔记
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("同步提示");
                        builder.setMessage("同步第"+ count +"条笔记会覆盖原来的笔记，是否继续？");
                        builder.setPositiveButton("继续", (dialog, which) -> {
                            //TODO 继续同步
                            //将这个笔记的内容更新到数据库
                            ContentValues values = new ContentValues();
                            values.put(Notes.DataColumns.CONTENT, itemData.getContent());
                            getActivity().getContentResolver().update(Notes.CONTENT_DATA_URI, values, Notes.DataColumns.DATA5 + " = ?", new String[]{itemData.getNote_token()});
                            values.clear();
                            values.put(Notes.NoteColumns.VERSION, itemData.getVersion());
                            getActivity().getContentResolver().update(Notes.CONTENT_NOTE_URI, values, Notes.NoteColumns.ID + " = ?", new String[]{String.valueOf(noteId)});
                            UIUtils.sendMsg(getActivity(), "同步成功");
                            dialog.cancel();
                        });
                        builder.setNegativeButton("取消", (dialog, which) -> {
                            //TODO 取消同步
                            dialog.cancel();
                        });
                        builder.show();
                    }else{
                        // 创建一个新的 WorkingNote 对象，并将读取到的文本内容设置为其正文
                        WorkingNote note = WorkingNote.createEmptyNote(getActivity(), Notes.ID_ROOT_FOLDER,
                                AppWidgetManager.INVALID_APPWIDGET_ID, Notes.TYPE_WIDGET_INVALIDE,
                                ResourceParser.YELLOW);
                        note.setWorkingText(itemData.getContent());
                        note.saveNote();
                        //更新这条笔记的token和version
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
