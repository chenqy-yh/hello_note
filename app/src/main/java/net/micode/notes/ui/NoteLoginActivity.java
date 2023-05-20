package net.micode.notes.ui;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class NoteLoginActivity extends Activity {

    //tag
    private static final String TAG = "chenqy";
    private NoteHttpServer server;
    private NbButton btn_login;
    private EditText note_login_phone_num;
    private EditText note_verification_code;
    private RelativeLayout rlContent;
    private Handler handler;
    private Animator animator;
    private Context context;
    private long mExitTime = 0;
    private boolean login_btn_isClickable = true;

    private static final String VERIFICATION_CODE_URL = "/verify/verifycode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.note_login);
        bindViews();
        initResources();
        try {
            checkLogin();
        } catch (JSONException | IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void bindViews() {
        btn_login = findViewById(R.id.btn_login);
        note_verification_code = findViewById(R.id.note_verification_code);
        note_login_phone_num = findViewById(R.id.note_login_phone_num);
        rlContent = findViewById(R.id.btn_login_area);
        rlContent.getBackground().setAlpha(0);
        handler = new Handler();

    }

    private void initResources() {
        context = this;
        server = new NoteHttpServer();

        btn_login.setOnClickListener(v -> {
            if (!login_btn_isClickable){
                return;
            }
            login_btn_isClickable = false;
            new Handler().postDelayed(() -> login_btn_isClickable = true, 2000);
            //check verifycode是否合法 合法就跳转
            try {
                checkVerifyCode();
            } catch (IOException | JSONException e) {
                UIUtils.sendMsg(NoteLoginActivity.this, "验证码错误");
            }
        });
        //如果携带了phone信息
        Intent intent = getIntent();
        String phone = intent.getStringExtra("phone");
        if (phone != null) {
            note_login_phone_num.setText(phone);
        }
    }

    //checkIsLogin
    private void checkLogin() throws JSONException, IOException {
        //TODO  通过验证本地存储的token与服务器进行匹配判断是否登陆
        Auth.checkAuthToken(this, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                //删除本地token
                Auth.removeToken(NoteLoginActivity.this, Auth.AUTH_TOKEN_KEY);
                //删除本地用户信息
                Auth.removeToken(NoteLoginActivity.this, Auth.AUTH_PHONE_KEY);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String resStr = response.body().string();
                JSONObject resJson;
                try {
                    resJson = new JSONObject(resStr);
                } catch (JSONException e) {
                    Log.e(TAG, "NoteListActivity checkLogin JSONObject转化失败");
                    return;
                }
                int code;
                try {
                    code = resJson.getInt("code");
                } catch (JSONException e) {
                    Log.e(TAG, "NoteListActivity checkLogin JSONObject 提取code失败");
                    return;
                }

                if (code != 200) {
                    Log.e(TAG, "NoteListActivity checkLogin JSONObject code != 200");
                } else {
                    //登陆成功
                    goNoteList();
                }
            }
        });
    }


    private void checkVerifyCode() throws IOException, JSONException {
        HttpUrl url = HttpUrl.parse(NoteRemoteConfig.generateUrl(VERIFICATION_CODE_URL));
        String phone_num = note_login_phone_num.getText().toString();
        String verify_code = note_verification_code.getText().toString();
        Log.e(TAG, "checkVerifyCode: " + phone_num + " " + verify_code);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("phone", phone_num);
        jsonObject.put("verifycode", verify_code);
        server.sendAsyncPostRequest(url, jsonObject.toString(), NoteHttpServer.BodyType.FORM_DATA, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                UIUtils.sendMsg(NoteLoginActivity.this, "网络错误");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                JSONObject responseJson;
                try {
                    responseJson = new JSONObject(response.body().string());
                } catch (JSONException e) {
                    Log.e(TAG, "登陆"+e.getMessage());
                    UIUtils.sendMsg(NoteLoginActivity.this, "网络异常");
                    return;
                }
                int code;
                try {
                    code = responseJson.getInt("code");
                } catch (JSONException e) {
                    Log.e(TAG, "登陆"+e.getMessage());
                    UIUtils.sendMsg(NoteLoginActivity.this, "网络异常");
                    return;
                }
                if (code == NoteRemoteConfig.RESPONSE_SUCCESS) {
                    String token;
                    try {
                        token = responseJson.getString("data");
                    } catch (JSONException e) {
                        Log.e(TAG, "登陆"+e.getMessage());
                        UIUtils.sendMsg(NoteLoginActivity.this, "网络异常");
                        return;
                    }

                    //保存token
                    Auth.syncToken(context, Auth.AUTH_TOKEN_KEY, token);
                    //保存验证的手机号
                    Auth.syncToken(context, Auth.AUTH_PHONE_KEY, phone_num);

                    UIUtils.runInUI(NoteLoginActivity.this, () -> {
                        btn_login.startAnim();
                        handler.postDelayed(() -> {
                            //跳转
                            gotoNew();
                        }, 500);
                    });
                } else {
                    UIUtils.sendMsg(NoteLoginActivity.this, "验证码错误");
                }
            }
        });
    }

    private void gotoNew() {
        btn_login.gotoNew();
        int xc = (btn_login.getLeft() + btn_login.getRight()) / 2;
        int yc = (btn_login.getTop() + btn_login.getBottom()) / 2;
        animator = ViewAnimationUtils.createCircularReveal(rlContent, xc, yc, 0, 1111);

        animator.setDuration(300);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                handler.postDelayed(() -> {
                    goNoteList();
                    overridePendingTransition(R.anim.anim_in, R.anim.anim_out);

                }, 200);
            }


            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
        rlContent.getBackground().setAlpha(255);
    }

    private void goNoteList() {
        Intent it = new Intent(NoteLoginActivity.this, NotesListActivity.class);
        it.setAction(Intent.ACTION_VIEW);
        startActivity(it);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (animator != null) animator.cancel();
        rlContent.getBackground().setAlpha(0);
        btn_login.regainBackground();
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - mExitTime > 2000) {
            Toast.makeText(this, R.string.press_again_exit, Toast.LENGTH_SHORT).show();
            mExitTime = System.currentTimeMillis();
        } else {
            super.onBackPressed();
        }
    }


}



