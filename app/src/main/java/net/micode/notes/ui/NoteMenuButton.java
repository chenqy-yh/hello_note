package net.micode.notes.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.RelativeLayout;
import net.micode.notes.tool.Validator;


public class NoteMenuButton extends androidx.appcompat.widget.AppCompatButton implements View.OnClickListener {
    private static final String TAG = "chenqy";
    private RelativeLayout note_menu;
    private FragmentManager fm;
    private Context context;
    private Fragment menuFragment;
    private OnClickNoteMenuButton callback;
    private View mask_view;
    private ToggleMneuComponentListener toggleMneuComponentListener;

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
//        note_menu = ((Activity) context).findViewById(R.id.note_menu);
//        noteMenuMainFragment = new NoteMenuMainFragment();
//        mask_view = ((Activity) context).findViewById(R.id.mask_view);
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
        Log.e(TAG, "note menu btn onClick");
        mask_view.setVisibility(View.VISIBLE);
        mask_view.setOnClickListener(v1 -> {
            Log.e(TAG, " maskView.setOnClickListener");
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
        toggleMneuComponentListener.show(context);
//        note_menu.findViewById(R.id.note_menu_container).setVisibility(View.VISIBLE);
        toggleMneuComponentListener.replace(context, menuFragment);
//        fm.beginTransaction().replace(R.id.note_menu_container, menuFragment).commit();

    }

    private void hideMenuWithAnimation() {
        AlphaAnimation anim = new AlphaAnimation(1.0f, 0.0f);
        anim.setDuration(200);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                note_menu.setVisibility(View.GONE);
                toggleMneuComponentListener.hide(context);
//                note_menu.findViewById(R.id.note_menu_container).setVisibility(View.GONE);

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


    public void setMaskView(int resourceId) {
        if (Validator.isValidResource(context, resourceId)) {
            Log.e(TAG, "setMaskView: 传入mask view ID不合法");
            return;
        }
        this.mask_view = ((Activity) context).findViewById(resourceId);
    }

    public void setNoteMenu(int resourceId) {
        if (Validator.isValidResource(context, resourceId)) {
            Log.e(TAG, "setNoteMenu: 传入菜单ID不合法");
            return;
        }
        this.note_menu = ((Activity) context).findViewById(resourceId);
    }

    public void setNoteMenuMainFragment(Fragment menuFragment) {
        this.menuFragment = menuFragment;
    }

    public void setToggleMneuComponentListener(ToggleMneuComponentListener toggleMneuComponentListener) {
        this.toggleMneuComponentListener = toggleMneuComponentListener;
    }

    public  interface ToggleMneuComponentListener {
        void show(Context context);
        void hide(Context context);
        void replace(Context context, Fragment menuFragment);
    }

}
