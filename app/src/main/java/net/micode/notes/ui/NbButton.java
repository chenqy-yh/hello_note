package net.micode.notes.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import net.micode.notes.R;

public class NbButton extends androidx.appcompat.widget.AppCompatButton {

    private int width;
    private int heigh;

    private GradientDrawable backDrawable;

    private boolean isMorphing;
    private int startAngle;

    private Paint paint;

    private ValueAnimator arcValueAnimator;

    public NbButton(Context context) {
        super(context);
        init();
    }

    public NbButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NbButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        isMorphing=false;

        backDrawable=new GradientDrawable();
        int colorDrawable= getResources().getColor(R.color.cutePink);
        backDrawable.setColor(colorDrawable);
        backDrawable.setCornerRadius(120);
        setBackgroundDrawable(backDrawable);

        paint=new Paint();
        paint.setColor(getResources().getColor(R.color.white));
        paint.setStrokeWidth(4);
        paint.setStyle(Paint.Style.STROKE);
        paint.setTextSize(2);
    }



    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode= View.MeasureSpec.getMode(widthMeasureSpec);
        int widthSize= View.MeasureSpec.getSize(widthMeasureSpec);
        int heighMode= View.MeasureSpec.getMode(heightMeasureSpec);
        int heighSize= View.MeasureSpec.getSize(heightMeasureSpec);
        if (widthMode== View.MeasureSpec.EXACTLY){
            width=widthSize;
        }
        if (heighMode== View.MeasureSpec.EXACTLY){
            heigh=heighSize;
        }
    }

    public void startAnim(){
        isMorphing=true;

        setText("");
        ValueAnimator valueAnimator=ValueAnimator.ofInt(width,heigh);

        valueAnimator.addUpdateListener(animation -> {
            int value= (int) animation.getAnimatedValue();
            int leftOffset=(width-value)/2;
            int rightOffset=width-leftOffset;

            backDrawable.setBounds(leftOffset,0,rightOffset,heigh);
        });
        ObjectAnimator objectAnimator=ObjectAnimator.ofFloat(backDrawable,"cornerRadius",120, (float) heigh /2);

        AnimatorSet animatorSet=new AnimatorSet();
        animatorSet.setDuration(500);
        animatorSet.playTogether(valueAnimator,objectAnimator);
        animatorSet.start();

        showArc();
    }
    public void gotoNew(){
        isMorphing=false;

        arcValueAnimator.cancel();
        setVisibility(GONE);

    }
    public void regainBackground(){
        setVisibility(VISIBLE);
        backDrawable.setBounds(0,0,width,heigh);
        backDrawable.setCornerRadius(24);
        setBackgroundDrawable(backDrawable);
        isMorphing=false;
    }

    private void showArc() {
        arcValueAnimator=ValueAnimator.ofInt(0,1080);
        arcValueAnimator.addUpdateListener(animation -> {
            startAngle= (int) animation.getAnimatedValue();
            invalidate();
        });
        arcValueAnimator.setInterpolator(new LinearInterpolator());
        arcValueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        arcValueAnimator.setDuration(3000);
        arcValueAnimator.start();


    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

       if (isMorphing){
           final RectF rectF=new RectF((float) (getWidth() * 5) /12, (float) getHeight() /7, (float) (getWidth() * 7) /12,getHeight()-getHeight()/7);
           canvas.drawArc(rectF,startAngle,270,false,paint);
       }
    }
}
