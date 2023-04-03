package com.example.marshmallow.building;

import static com.example.marshmallow.MLand.PARAMS;

import android.content.Context;
import android.util.Log;
import android.widget.FrameLayout;

import com.example.marshmallow.GameView;

public class Scenery extends FrameLayout implements GameView {
    private static final String TAG = "Scenery";

    public float z;
    public float v;
    public int h, w;
    public Scenery(Context context) {
        super(context);
    }

    @Override
    public void step(long t_ms, long dt_ms, float t, float dt) {
        setTranslationX(getTranslationX() - PARAMS.TRANSLATION_PER_SEC * dt * v);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d(TAG, "onDetachedFromWindow: ");

    }


}
