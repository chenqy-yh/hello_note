package net.micode.notes.ui;

import android.animation.Animator;
import android.app.Activity;
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
import net.micode.notes.callback.NoteCallback;
import net.micode.notes.tool.NoteHttpServer;
import net.micode.notes.tool.NoteRemoteConfig;
import net.micode.notes.tool.UIUtils;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class NoteLoginActivity extends Activity {

    //tag
    private static final String TAG = "chenqy";
    private NoteHttpServer server;
    private NbButton btn_login;
    private Button btn_verification;
    private EditText note_login_phone_num;
    private EditText note_verification_code;
    private RelativeLayout rlContent;
    private Handler handler;
    private Animator animator;
    private long lastClickTime = 0;
    private static final String VERIFICATION_CODE_URL = "/verify/verifycode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_login);
        bindViews();
        initResources();
    }


    private void bindViews() {
        btn_login = (NbButton) findViewById(R.id.btn_login);
        note_verification_code = (EditText) findViewById(R.id.note_verification_code);
        note_login_phone_num = (EditText) findViewById(R.id.note_login_phone_num);
        rlContent = (RelativeLayout) findViewById(R.id.btn_login_area);
        rlContent.getBackground().setAlpha(0);
        handler = new Handler();


    }

    private void initResources() {
        server = new NoteHttpServer();
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check verifycode是否合法 合法就跳转
                try {
                    checkVerifyCode();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
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
        String status = "";
        server.sendAsyncPostRequest(url, jsonObject.toString(), NoteHttpServer.BodyType.FORM_DATA, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                UIUtils.runInUI(NoteLoginActivity.this, new NoteCallback() {
                    @Override
                    public void execute() {
                        Toast.makeText(getApplicationContext(), "网络错误", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                JSONObject responseJson = null;
                try {
                    responseJson = new JSONObject(response.body().string());
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                int code = -1;
                try {
                    code = responseJson.getInt("code");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                if(code == NoteRemoteConfig.RESPONSE_SUCCESS){
                    UIUtils.runInUI(NoteLoginActivity.this, () -> {
                        btn_login.startAnim();
                        handler.postDelayed(() -> {
                            //跳转
                            gotoNew();
                        }, 500);
                    });
                }else{
                    UIUtils.runInUI(NoteLoginActivity.this, () -> {
                        Toast.makeText(getApplicationContext(), "验证码错误", Toast.LENGTH_SHORT).show();
                    });
                }


            }
        });
    }

    private void gotoNew() {
        btn_login.gotoNew();
        int xc = (btn_login.getLeft() + btn_login.getRight()) / 2;
        int yc = (btn_login.getTop() + btn_login.getBottom()) / 2;
        float startRadius = 0;
        float endRadus = 1111;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animator = ViewAnimationUtils.createCircularReveal(rlContent, xc, yc, 0, 1111);
        }
        animator.setDuration(300);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
//                        startActivity(intent);
                        finish();
                        overridePendingTransition(R.anim.anim_in, R.anim.anim_out);

                    }
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

    @Override
    protected void onStop() {
        super.onStop();
        animator.cancel();
        rlContent.getBackground().setAlpha(0);
        btn_login.regainBackground();
    }

//    @Override
//    public void onBackPressed() {
//        if(System.currentTimeMillis() - lastClickTime < 2000){
//            finish();
//        }else{
//            lastClickTime = System.currentTimeMillis();
//            Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
//        }
//
//        super.onBackPressed();
//    }
}



