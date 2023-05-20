package net.micode.notes.ui;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Path;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import androidx.annotation.Nullable;
import net.micode.notes.R;


public class TestActivity extends Activity {

    private Button btn_main;
    private Path path;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);
//        bindView();
    }

    private void bindView() {
        btn_main = findViewById(R.id.btn);
        btn_main.setOnClickListener(v -> {
          //创建贝塞尔曲线
            path = new Path();
            float x = btn_main.getX();
            float y = btn_main.getY();
            float endX = x + convertDpToPx(100);
            float endY = y - convertDpToPx(30);
            path.moveTo(x, y);
            path.quadTo(endX, y , endX, endY);
            ObjectAnimator animator = ObjectAnimator.ofFloat(btn_main, btn_main.X, btn_main.Y, path);
            animator.setDuration(200);
            animator.start();
        });
    }

    private float convertDpToPx(float dp) {
        float scale = getResources().getDisplayMetrics().density;
        return dp * scale + 0.5f;
    }


}
