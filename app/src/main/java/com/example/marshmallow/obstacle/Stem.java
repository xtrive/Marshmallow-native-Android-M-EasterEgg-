package com.example.marshmallow.obstacle;

import static com.example.marshmallow.MLand.PARAMS;
import static com.example.marshmallow.MLand.frand;
import static com.example.marshmallow.MLand.mCurrentPipeId;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewOutlineProvider;

public class Stem extends Obstacle {
    Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Path mShadow = new Path();
    GradientDrawable mGradient = new GradientDrawable();
    boolean mDrawShadow;
    Path mJandystripe;
    Paint mPaint2;
    int pipeId; // use this to track which pipes have been cleared

    public int getPipeId() {
        return pipeId;
    }

    public Stem(Context context, float h, boolean drawShadow) {
        super(context, h);
        pipeId = mCurrentPipeId;

        mDrawShadow = drawShadow;
        setBackground(null);
        mGradient.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
        mPaint.setColor(0xFF000000);
        mPaint.setColorFilter(new PorterDuffColorFilter(0x22000000, PorterDuff.Mode.MULTIPLY));

        if (frand() < 0.01f) {
            mGradient.setColors(new int[]{0xFFFFFFFF, 0xFFDDDDDD});
            mJandystripe = new Path();
            mPaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint2.setColor(0xFFFF0000);
            mPaint2.setColorFilter(new PorterDuffColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY));
        } else {
            //mPaint.setColor(0xFFA1887F);
            mGradient.setColors(new int[]{0xFFBCAAA4, 0xFFA1887F});
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setWillNotDraw(false);
        setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRect(0, 0, getWidth(), getHeight());
            }
        });
    }
    @Override
    public void onDraw(Canvas c) {
        final int w = c.getWidth();
        final int h = c.getHeight();
        mGradient.setGradientCenter(w * 0.75f, 0);
        mGradient.setBounds(0, 0, w, h);
        mGradient.draw(c);

        if (mJandystripe != null) {
            mJandystripe.reset();
            mJandystripe.moveTo(0, w);
            mJandystripe.lineTo(w, 0);
            mJandystripe.lineTo(w, 2 * w);
            mJandystripe.lineTo(0, 3 * w);
            mJandystripe.close();
            for (int y=0; y<h; y+=4*w) {
                c.drawPath(mJandystripe, mPaint2);
                mJandystripe.offset(0, 4 * w);
            }
        }

        if (!mDrawShadow) return;
        mShadow.reset();
        mShadow.moveTo(0, 0);
        mShadow.lineTo(w, 0);
        mShadow.lineTo(w, PARAMS.OBSTACLE_WIDTH * 0.4f + w*1.5f);
        mShadow.lineTo(0, PARAMS.OBSTACLE_WIDTH * 0.4f);
        mShadow.close();
        c.drawPath(mShadow, mPaint);
    }
}

