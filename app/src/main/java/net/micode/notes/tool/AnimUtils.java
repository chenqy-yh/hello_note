package net.micode.notes.tool;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;

public class AnimUtils {

    public static AnimatorSet playAnimations(int duration, AnimatorListenerAdapter listenerAdapter, Animator... animators){
        AnimatorSet set = new AnimatorSet();
        set.playTogether(animators);
        set.setDuration(duration);
        if (listenerAdapter != null){
            set.addListener(listenerAdapter);
        }
        return set;
    }

}
