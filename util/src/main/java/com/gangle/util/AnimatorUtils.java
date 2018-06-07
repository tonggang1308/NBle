package com.gangle.util;

import android.animation.ObjectAnimator;

public class AnimatorUtils {

    public static ObjectAnimator getAlphaAnimator(Object target, float... values) {
        return ObjectAnimator.ofFloat(target, "alpha", values);
    }

    public static ObjectAnimator getScaleXAnimator(Object target, float... values) {
        return ObjectAnimator.ofFloat(target, "scaleX", values);
    }

    public static ObjectAnimator getScaleYAnimator(Object target, float... values) {
        return ObjectAnimator.ofFloat(target, "scaleY", values);
    }

    public static ObjectAnimator getRotationAnimator(Object target, float... values) {
        return ObjectAnimator.ofFloat(target, "rotation", values);
    }

}
