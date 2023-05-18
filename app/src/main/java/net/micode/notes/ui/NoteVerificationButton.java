package net.micode.notes.ui;

import android.app.Activity;
import android.content.Context;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import net.micode.notes.R;
import net.micode.notes.callback.NoteCallback;
import net.micode.notes.tool.NoteHttpServer;
import net.micode.notes.tool.NoteRemoteConfig;
import net.micode.notes.tool.UIUtils;
import net.micode.notes.tool.Validator;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Locale;


public class NoteVerificationButton extends androidx.appcompat.widget.AppCompatButton implements View.OnClickListener {
    private static final String TAG = "chenqy";
    private NoteHttpServer server;
    private EditText note_login_phone_num;
    private EditText note_verification_code;

    private boolean isClickable = true; // flag to check if the button is clickable or not
    private CountDownTimer countDownTimer; // timer to count down

    private static final String VERIFICATION_ENDPOINT = "/verify/getverifycode";


    public NoteVerificationButton(Context context) {
        super(context);
    }

    public NoteVerificationButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NoteVerificationButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        init();
    }

    private void init() {
        note_login_phone_num = ((Activity) getContext()).findViewById(R.id.note_login_phone_num);
        note_verification_code = ((Activity) getContext()).findViewById(R.id.note_verification_code);
        server = new NoteHttpServer();
        this.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        Log.e(TAG, "onClick: ");
        if (isClickable) { // only execute if the button is clickable
            HttpUrl url = HttpUrl.parse(NoteRemoteConfig.generateUrl(VERIFICATION_ENDPOINT));
            String phone = note_login_phone_num.getText().toString();
            //验证手机号是否合法
            if (!Validator.isPhoneNum(phone)) {
                UIUtils.sendMsg((Activity) getContext(), "手机号不合法");
                return;
            }
            disableButton(); // disable the button and start the countdown timer

            //发送验证码
            sendVerificationCode(phone);
            //输入框聚焦在验证码输入框
            note_verification_code.requestFocus();
        }
    }

    private void sendVerificationCode(String phone) {
        HttpUrl url = HttpUrl.parse(NoteRemoteConfig.generateUrl(VERIFICATION_ENDPOINT));
        url = url.newBuilder().addQueryParameter("phone", phone).build();
        server.sendAsyncGetRequest(url, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                enableButton(); // enable the button if the request fails
                ((Activity) getContext()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "发送失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                enableButton(); // enable the button if the request succeeds
                ((Activity) getContext()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "发送成功", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void disableButton() {
        isClickable = false;
        countDownTimer = new CountDownTimer(5000, 1000) { // start a countdown timer for 60 seconds
            @Override
            public void onTick(long millisUntilFinished) {
                long remainingSeconds = (millisUntilFinished + 999) / 1000; // round up the remaining milliseconds to seconds
                long seconds = remainingSeconds % 60;
                UIUtils.runInUI((Activity) getContext(), () -> {
                    setText(String.format(Locale.getDefault(), "%ds", seconds)); // set the button text to show the remaining time
                });

            }

            @Override
            public void onFinish() {
                enableButton(); // enable the button when the timer finishes
            }
        }.start();
    }

    private void enableButton() {
        isClickable = true;
        countDownTimer.cancel(); // cancel the countdown timer if it's still running
        UIUtils.sendMsg((Activity) getContext(), "获取验证码");
    }
}
