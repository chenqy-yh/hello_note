package net.micode.notes.ui;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

public class NoteCommonButton extends androidx.appcompat.widget.AppCompatButton {

    private String release_color = "#eeeeee";
    private String pressed_color = "#f43a68";
    private int duration = 300;
    private float cornerRadius;


    public NoteCommonButton(@NonNull Context context) {
        super(context);
        init();
    }

    public NoteCommonButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NoteCommonButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init() {
        ViewTreeObserver vto = getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int buttonHeight = getHeight();
                cornerRadius = buttonHeight / 2f; // 获取按钮高度的一半作为边框半径

                GradientDrawable shape = new GradientDrawable();
                shape.setShape(GradientDrawable.RECTANGLE);
                shape.setCornerRadius(cornerRadius);
                shape.setColor(Color.parseColor(release_color));
                setBackground(shape);
            }
        });

        setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    animateButtonColorTransition(this, Color.parseColor(release_color), Color.parseColor(pressed_color), duration);
                    break;
                case MotionEvent.ACTION_UP:
                    animateButtonColorTransition(this, Color.parseColor(pressed_color), Color.parseColor(release_color), duration);
                    break;
            }
            return false;
        });
    }

    private void animateButtonColorTransition(View view, int startColor, int endColor, int duration) {
        ValueAnimator colorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), startColor, endColor);
        colorAnimator.setDuration(duration);
        colorAnimator.addUpdateListener(animation -> {
            int animatedValue = (int) animation.getAnimatedValue();
            GradientDrawable shape = (GradientDrawable) view.getBackground();
            shape.setColor(animatedValue);
            setTextColor(getContrastColor(animatedValue));
        });
        colorAnimator.start();
    }

    private int getContrastColor(int color) {
        double luminance = ColorUtils.calculateLuminance(color);
        return luminance >= 0.8 ? Color.BLACK : Color.WHITE;
    }

    public void setRelease_color(String release_color) {
        this.release_color = release_color;
        GradientDrawable shape = (GradientDrawable) getBackground();
        shape.setColor(Color.parseColor(release_color));
        setTextColor(getContrastColor(Color.parseColor(release_color)));
    }

    public void setPressed_color(String pressed_color) {
        this.pressed_color = pressed_color;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }


}
