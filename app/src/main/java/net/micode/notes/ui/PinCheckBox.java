package net.micode.notes.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import net.micode.notes.R;

public class PinCheckBox extends androidx.appcompat.widget.AppCompatCheckBox {

    public PinCheckBox(Context context) {
        super(context);
        init();
    }

    public PinCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PinCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        Drawable pinDrawable = getResources().getDrawable(R.drawable.pin);
        Drawable pinActiveDrawable = getResources().getDrawable(R.drawable.pin_active);

        Bitmap pinBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pin);
        Bitmap resizedPinBitmap = Bitmap.createScaledBitmap(pinBitmap, dpToPx(20), dpToPx(20), false);
        Drawable resizedPinDrawable = new BitmapDrawable(getResources(), resizedPinBitmap);

        Bitmap pinActiveBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pin_active);
        Bitmap resizedPinActiveBitmap = Bitmap.createScaledBitmap(pinActiveBitmap, dpToPx(20), dpToPx(20), false);
        Drawable resizedPinActiveDrawable = new BitmapDrawable(getResources(), resizedPinActiveBitmap);

        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_checked}, resizedPinActiveDrawable);
        stateListDrawable.addState(new int[]{}, resizedPinDrawable);

        setButtonDrawable(stateListDrawable);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
