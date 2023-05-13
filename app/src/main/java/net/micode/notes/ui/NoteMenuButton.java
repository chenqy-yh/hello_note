package net.micode.notes.ui;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import net.micode.notes.R;

public class NoteMenuButton extends Button implements View.OnClickListener {


    //tag
    private static  final String TAG = "chenqy";

    //TODO 定制menu
    private PopupWindow popupWindow;
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

    private void init(){
        setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Log.e(TAG, "onClick");
        initPopMenu(v);
    }

    private void initPopMenu(View v) {
        //这一步是为了获取屏幕的宽高，便于用来设置PopupWindow的大小
        DisplayMetrics dm = getResources().getDisplayMetrics();
        //新建popupwindow依靠的view
        View view = View.inflate(context, R.layout.note_pop_menu,null);
        Button btn_close = view.findViewById(R.id.btn_close);

        //创建pop（视图，宽，高
        popupWindow =  new PopupWindow(view,dm.widthPixels/11*8,dm.heightPixels/11*8);
        popupWindow.setAnimationStyle(R.style.popwin_anim_style);
        //展示pop
        //设置关闭菜单
        popupWindow.setTouchable(true);
        popupWindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
                // 这里如果返回true的话，touch事件将被拦截
                // 拦截后 PopupWindow的onTouchEvent不被调用，这样点击外部区域无法dismiss
            }
        });

        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        popupWindow.showAtLocation(((Activity)context).getWindow().getDecorView(), Gravity.CENTER,0,0);
    }

}
