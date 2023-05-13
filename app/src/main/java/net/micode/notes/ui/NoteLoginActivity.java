package net.micode.notes.ui;

import android.animation.Animator;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;
import net.micode.notes.R;

public class NoteLoginActivity extends Activity {

    private NbButton btn_login;
    private RelativeLayout rlContent;
    private Handler handler;
    private Animator animator;
    private long lastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_login);
        bindViews();
        initResources();
    }


    private void bindViews(){
        btn_login = (NbButton) findViewById(R.id.btn_login);
        rlContent = (RelativeLayout) findViewById(R.id.btn_login_area);

        rlContent.getBackground().setAlpha(0);
        handler=new Handler();
    }

    private void initResources(){
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_login.startAnim();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        //跳转
                        gotoNew();
                    }
                },500);

            }
        });
    }

    private void gotoNew() {
        btn_login.gotoNew();
        int xc=(btn_login.getLeft()+btn_login.getRight())/2;
        int yc=(btn_login.getTop()+btn_login.getBottom())/2;
        float startRadius = 0;
        float endRadus = 1111;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animator= ViewAnimationUtils.createCircularReveal(rlContent,xc,yc,0,1111);
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
                        overridePendingTransition(R.anim.anim_in,R.anim.anim_out);

                    }
                },200);
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



