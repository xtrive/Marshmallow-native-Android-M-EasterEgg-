package com.example.marshmallow.player;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;

@SuppressWarnings("all") // @SuppressWarnings("JniMissingFunction") ide bug...
abstract class BasePlayer extends AppCompatImageView {
    private final long nPtr;

    public BasePlayer(@NonNull Context context) {
        super(context);
        nPtr = initPlayer();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        releasePlayer(nPtr);
    }

    protected native long initPlayer();
    protected native void releasePlayer(long ptr);

    public native float getDv();
    public native void setDv(float dv);

    public native int getColor();
    public native void setColor(int color);

    public native boolean isBoosting();
    public native void setBoosting(boolean boosting);

    public native float getTouchX();
    public native void setTouchX(float touchX);

    public native float getTouchY();
    public native void setTouchY(float touchY);

    public native boolean isAlive();
    public native void setAlive(boolean alive);

    public native int getScore();
    public native void nativeSetScore(int score);

    public native float getTranslateX();
    public native void nativeSetTranslateX(float translateX);

    public native float getTranslateY();
    public native void nativeSetTranslateY(float translateY);

    public native void saveY(float y);
    
}
