package net.micode.notes.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.RelativeLayout;
import net.micode.notes.R;

//public class NoteMenuButton extends androidx.appcompat.widget.AppCompatButton implements View.OnClickListener {
//    private static final String TAG = "chenqy";
//    private RelativeLayout note_menu;
//    private FragmentManager fm;
//    private Context context;
//    private NoteMenuMainFragment noteMenuMainFragment;
//    private OnClickNoteMenuButton callback;
//    private View maks_view;
//
//    public NoteMenuButton(Context context) {
//        super(context);
//        this.context = context;
//    }
//
//    public NoteMenuButton(Context context, AttributeSet attrs) {
//        super(context, attrs);
//        this.context = context;
//    }
//
//    public NoteMenuButton(Context context, AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//        this.context = context;
//    }
//
//    @Override
//    protected void onAttachedToWindow() {
//        super.onAttachedToWindow();
//        init();
//    }
//
//    private void init() {
//        fm = ((Activity) context).getFragmentManager();
//        note_menu = ((Activity)context).findViewById(R.id.note_menu);
//        noteMenuMainFragment = new NoteMenuMainFragment();
//        maks_view = ((Activity)context).findViewById(R.id.mask_view);
//        setOnClickListener(this);
//        maks_view.setOnClickListener(v1 -> {
//            Log.e(TAG, " maskView.setOnClickListener" );
//            hideMenuWithAnimation();
//        });
//    }
//
//    @Override
//    public void onClick(View v) {
//        Log.e(TAG, "note menu btn onClick" );
////        View maskView = ((Activity) context).findViewById(R.id.mask_view);
//        maks_view.setVisibility(View.VISIBLE);
//
//
//        AnimatorSet animatorSet = new AnimatorSet();
//        ObjectAnimator rotationAnimator = ObjectAnimator.ofFloat(this, "rotation", 0f, 90f);
//        animatorSet.playTogether(rotationAnimator);
//        animatorSet.setDuration(200);
//        animatorSet.start();
//
//        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
//        anim.setDuration(200);
//        note_menu.startAnimation(anim);
//        note_menu.setVisibility(View.VISIBLE);
//        note_menu.findViewById(R.id.note_menu_container).setVisibility(View.VISIBLE);
////        fm.beginTransaction().replace(R.id.note_menu_container, noteMenuMainFragment).commit();
//        //执行OnClickNoteMenuButton
//        if (callback != null) callback.execute();
//    }
//
//    private void hideMenuWithAnimation() {
//
//        AlphaAnimation anim = new AlphaAnimation(1.0f, 0.0f);
//        anim.setDuration(200);
//        anim.setFillAfter(true); // 设置动画结束后保持最后的状态
//        anim.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                note_menu.setVisibility(View.GONE);
//                note_menu.findViewById(R.id.note_menu_container).setVisibility(View.GONE);
//
//                View maskView = ((Activity) context).findViewById(R.id.mask_view);
//                maskView.setVisibility(View.GONE);
//
//                AnimatorSet animatorSet = new AnimatorSet();
//                ObjectAnimator rotationAnimator = ObjectAnimator.ofFloat(NoteMenuButton.this, "rotation", 90f, 0f);
//                animatorSet.playTogether(rotationAnimator);
//                animatorSet.setDuration(200);
//                animatorSet.start();
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//            }
//        });
//        note_menu.startAnimation(anim);
//    }
//
//    public  void hiddenMenu() {
//        hideMenuWithAnimation();
//    }
//
//    public void setOnClickNoteMenuButton(OnClickNoteMenuButton callback) {
//        this.callback = callback;
//    }
//
//    interface OnClickNoteMenuButton {
//        void execute();
//    }
//
//}
//



public class NoteMenuButton extends androidx.appcompat.widget.AppCompatButton implements View.OnClickListener {
    private static final String TAG = "chenqy";
    private RelativeLayout note_menu;
    private FragmentManager fm;
    private Context context;
    private NoteMenuMainFragment noteMenuMainFragment;
    private OnClickNoteMenuButton callback;
    private View mask_view;

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
        note_menu = ((Activity)context).findViewById(R.id.note_menu);
        noteMenuMainFragment = new NoteMenuMainFragment();
        mask_view = ((Activity)context).findViewById(R.id.mask_view);
        setOnClickListener(this);
        note_menu.setOnClickListener(v -> {
            // 处理 note_menu 的点击事件
            // 执行OnClickNoteMenuButton
            if (callback != null) {
                callback.execute();
            }
        });
    }

    @Override
    public void onClick(View v) {
        Log.e(TAG, "note menu btn onClick" );
        mask_view.setVisibility(View.VISIBLE);
        mask_view.setOnClickListener(v1 -> {
            Log.e(TAG, " maskView.setOnClickListener" );
            hideMenuWithAnimation();
        });

        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator rotationAnimator = ObjectAnimator.ofFloat(this, "rotation", 0f, 90f);
        animatorSet.playTogether(rotationAnimator);
        animatorSet.setDuration(200);
        animatorSet.start();

        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(200);
        note_menu.startAnimation(anim);
        note_menu.setVisibility(View.VISIBLE);
        note_menu.findViewById(R.id.note_menu_container).setVisibility(View.VISIBLE);
        fm.beginTransaction().replace(R.id.note_menu_container, noteMenuMainFragment).commit();

    }

    private void hideMenuWithAnimation() {
        AlphaAnimation anim = new AlphaAnimation(1.0f, 0.0f);
        anim.setDuration(200);
        anim.setFillAfter(true);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                note_menu.setVisibility(View.GONE);
                note_menu.findViewById(R.id.note_menu_container).setVisibility(View.GONE);

                mask_view.setVisibility(View.GONE);

                AnimatorSet animatorSet = new AnimatorSet();
                ObjectAnimator rotationAnimator = ObjectAnimator.ofFloat(NoteMenuButton.this, "rotation", 90f, 0f);
                animatorSet.playTogether(rotationAnimator);
                animatorSet.setDuration(200);
                animatorSet.start();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        note_menu.startAnimation(anim);
    }

    public void hiddenMenu() {
        hideMenuWithAnimation();
    }

    public void setOnClickNoteMenuButton(OnClickNoteMenuButton callback) {
        this.callback = callback;
    }

    interface OnClickNoteMenuButton {
        void execute();
    }
}

