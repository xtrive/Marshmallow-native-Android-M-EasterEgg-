package com.example.marshmallow.obstacle;

import android.content.Context;
import android.view.View;

@SuppressWarnings("all") // @SuppressWarnings("JniMissingFunction") ide bug...
abstract class BaseObstacle extends View {

    private final long nPtr;

    public native float getH();
    protected native long initNative(float h);
    protected native void releaseNative(long nPtr);
    protected native void setNativeHitRect(int left, int top, int right, int bottom);

    public native float getTranslateX();
    public native void nativeSetTranslateX(float translateX);

    public native float getTranslateY();
    public native void nativeSetTranslateY(float translateY);

    @Override
    public void setTranslationX(float translationX) {
        super.setTranslationX(translationX);
        nativeSetTranslateX(translationX);
    }

    @Override
    public void setTranslationY(float translationY) {
        super.setTranslationY(translationY);
        nativeSetTranslateY(translationY);
    }

    @Override
    public float getTranslationX() {
        return getTranslateX();
    }

    @Override
    public float getTranslationY() {
        return getTranslateY();
    }

    public BaseObstacle(Context context, float h) {
        super(context);
        nPtr = initNative(h);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        releaseNative(nPtr);
    }

}

