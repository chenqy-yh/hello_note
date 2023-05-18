package net.micode.notes.ui;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import net.micode.notes.R;

public class NoteMenuButton extends androidx.appcompat.widget.AppCompatButton implements View.OnClickListener {


    //tag
    private static  final String TAG = "chenqy";

    //TODO 定制menu
    private Context context;
    private boolean isClickable = true;
    private RelativeLayout note_menu;
    private FragmentManager fm ;





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

    private void init(){
        fm = ((Activity)context).getFragmentManager();
        note_menu = ((Activity)context).findViewById(R.id.note_menu);
        setOnClickListener(this);
    }

//    @Override
//    public void onClick(View v) {
//        if(!isClickable) return;
//        Log.e(TAG, "onClick");
//        initPopMenu(v);
//    }

    @Override
    public void onClick(View v) {
        if (!isClickable) return;
        isClickable = false;
        Log.e(TAG, "onClick");
        AlphaAnimation anim =  new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(200);
        note_menu.startAnimation(anim);
        note_menu.setVisibility(View.VISIBLE);
        View container = note_menu.findViewById(R.id.note_menu_container);
        NoteMenuMainFragment noteMenuMainFragment = new NoteMenuMainFragment();
        noteMenuMainFragment.setCloseListener(() -> {
            isClickable = true;
            addEndAnimation();
        });
        fm.beginTransaction().replace(R.id.note_menu_container, new NoteMenuMainFragment()).commit();
        // 添加渐变动画效果
        addStartAnimation();
    }



    public void addStartAnimation(){
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(1f, 0.8f);
        valueAnimator.setDuration(300);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float scale = (float) animation.getAnimatedValue();
                NoteMenuButton.this.setScaleX(scale);
                NoteMenuButton.this.setScaleY(scale);
            }
        });
        valueAnimator.start();
    }

    public void addEndAnimation(){
        // 添加渐变动画效果
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0.8f, 1f);
        valueAnimator.setDuration(300);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float scale = (float) animation.getAnimatedValue();
                NoteMenuButton.this.setScaleX(scale);
                NoteMenuButton.this.setScaleY(scale);
            }
        });
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isClickable = true;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        valueAnimator.start();
    }

}
