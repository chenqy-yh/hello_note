package net.micode.notes.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import net.micode.notes.R;
import net.micode.notes.data.Auth;
import net.micode.notes.tool.NoteHttpServer;
import net.micode.notes.tool.NoteRemoteConfig;
import net.micode.notes.tool.UIUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;

public class NoteMenuMainFragment extends Fragment {

    //tag
    private static final String TAG = "chenqy";
    private Button btn_note_backup;
    private Button btn_note_sync;
    private Button btn_sign_out;
    private Context context;


    public NoteMenuMainFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.context = getActivity();
        View view = inflater.inflate(R.layout.note_pop_menu_main, container, false);
        bindView(view);
        return view;
    }


    private void bindView(View view) {
        btn_note_backup = view.findViewById(R.id.btn_note_backup);
        btn_note_sync = view.findViewById(R.id.btn_note_sync);
        btn_sign_out = view.findViewById(R.id.btn_note_signout);

        this.btn_note_backup.setOnClickListener(new NoteMenuMainFragment.NoteMenuBackUpButtonClickListener(context));
        this.btn_note_sync.setOnClickListener(new NoteMenuSyncButtonClickListener(context));
        this.btn_sign_out.setOnClickListener(new NoteMenuSignOutButtonClickListener(context, phone -> {
            Intent it = new Intent(context, NoteLoginActivity.class);
            Bundle args = new Bundle();
            args.putString("phone", phone);
            it.putExtras(args);
            startActivity(it);
            ((Activity) context).finish();
        }));
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

    static class NoteMenuSignOutButtonClickListener implements View.OnClickListener {

        private final Context context;
        private AfterSignOutListener afterSignOutListener;

        NoteMenuSignOutButtonClickListener(Context context,AfterSignOutListener afterSignOutListener) {
            this.context = context;
            this.afterSignOutListener = afterSignOutListener;
        }

        @Override
        public void onClick(View v) {
            Log.e(TAG, "onClick signout");
            NoteHttpServer server = new NoteHttpServer();
            JSONObject body = new JSONObject();
            String phone = Auth.getAuthToken(context, Auth.AUTH_PHONE_KEY);
            String token = Auth.getAuthToken(context, Auth.AUTH_TOKEN_KEY);
            HttpUrl url = HttpUrl.parse(NoteRemoteConfig.generateUrl("/auth/signout"));
            try {
                body.put("phone", phone);
                body.put("verifycode", token);

                server.sendAsyncPostRequest(url, body.toString(), NoteHttpServer.BodyType.JSON, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        sendSignOutMessage(context);
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) {
                        sendSignOutMessage(context);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "signout:"+e.getMessage() );
                UIUtils.sendMsg((Activity) v.getContext(), "网络异常");
            }
        }


        private void sendSignOutMessage(Context context) {
            String phone = Auth.getAuthToken(context, Auth.AUTH_PHONE_KEY);
            Auth.removeToken(context, Auth.AUTH_PHONE_KEY);
            Auth.removeToken(context, Auth.AUTH_TOKEN_KEY);
            afterSignOutListener.afterSignOut(phone);
        }
    }

    interface AfterSignOutListener {
        void afterSignOut(String phone);
    }

}
