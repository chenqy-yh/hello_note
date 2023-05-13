package net.micode.notes.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import net.micode.notes.R;

public class NoteLoginButton extends Button implements View.OnClickListener {


    //tag
    private static final String TAG = "chenqy";
    private static final int ANIMATION_DURATION = 500;
    private static final int FADE_DURATION = 300;
    private static final int CROSS_LENGTH = 120;
    private static final int BTN_BG_COLOR = R.color.black;
    private Handler handler;
    private View btn_mask;


    public NoteLoginButton(Context context) {
        super(context);
    }

    public NoteLoginButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NoteLoginButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.e(TAG, "onDraw");
        // 在此处绘制按钮的图形
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setColor(getResources().getColor(BTN_BG_COLOR));
        paint.setAntiAlias(true);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, Math.min(getWidth(), getHeight()) / 2, paint);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(15);
        canvas.drawLine(getWidth() / 2, getHeight() / 2 - CROSS_LENGTH / 2, getWidth() / 2, getWidth() / 2 + CROSS_LENGTH / 2, paint);
        canvas.drawLine(getWidth() / 2 - CROSS_LENGTH / 2, getHeight() / 2, getWidth() / 2 + CROSS_LENGTH / 2, getHeight() / 2, paint);
    }

    private void init() {
        // 将 btn_cover 转换为 ViewGroup 类型
        ViewGroup parentLayout = (ViewGroup) getParent();
//        btn_mask = parentLayout.findViewById(R.id.btn_mask);
        handler = new Handler();
        setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        btn_mask.setVisibility(VISIBLE);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(btn_mask, "scaleX", 1f, 40f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(btn_mask, "scaleY", 1f, 40f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(btn_mask, "alpha", 1f, 0f);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(scaleX).with(scaleY);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animatorSet.play(alpha);
                Intent it = new Intent(getContext(), NoteLoginActivity.class);
                it.setAction(Intent.ACTION_VIEW);
                getContext().startActivity(it);
                ((Activity) getContext()).overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        btn_mask.setVisibility(GONE);
                        btn_mask.setScaleX(1f);
                        btn_mask.setScaleY(1f);
                        btn_mask.setAlpha(1f);
                    }
                }, 500);
            }
        });
        animatorSet.start();
    }
}
