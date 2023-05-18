package net.micode.notes.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;
import net.micode.notes.R;

public class NoteMenuButton extends androidx.appcompat.widget.AppCompatButton implements View.OnClickListener {
    private static final String TAG = "chenqy";

    private boolean isClickable = true;
    private RelativeLayout note_menu;
    private FragmentManager fm;
    private Context context;

    public NoteMenuButton(Context context) {
        super(context);
        this.context = context;
    }

    public NoteMenuButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public NoteMenuButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        init();
    }

    private void init() {
        fm = ((Activity) context).getFragmentManager();
        note_menu = ((Activity) context).findViewById(R.id.note_menu);
        setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (!isClickable) {
            return;
        }
        isClickable = false;
        Log.e(TAG, "onClick");

        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(200);
        note_menu.startAnimation(anim);
        note_menu.setVisibility(View.VISIBLE);

        NoteMenuMainFragment noteMenuMainFragment = new NoteMenuMainFragment();
        noteMenuMainFragment.setCloseListener(() -> {
            isClickable = true;
            addEndAnimation();
        });
        fm.beginTransaction().replace(R.id.note_menu_container, noteMenuMainFragment).commit();

        addStartAnimation();
    }

    private void addStartAnimation() {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(1f, 0.8f);
        valueAnimator.setDuration(300);
        valueAnimator.addUpdateListener(animation -> {
            float scale = (float) animation.getAnimatedValue();
            setScaleX(scale);
            setScaleY(scale);
        });
        valueAnimator.start();
    }

    private void addEndAnimation() {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0.8f, 1f);
        valueAnimator.setDuration(300);
        valueAnimator.addUpdateListener(animation -> {
            float scale = (float) animation.getAnimatedValue();
            setScaleX(scale);
            setScaleY(scale);
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isClickable = true;
            }
        });
        valueAnimator.start();
    }
}
