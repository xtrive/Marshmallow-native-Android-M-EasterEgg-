package com.example.marshmallow.player;

import static com.example.marshmallow.MLand.PARAMS;
import static com.example.marshmallow.MLand.clamp;
import static com.example.marshmallow.MLand.lerp;
import static com.example.marshmallow.MLand.luma;
import static com.example.marshmallow.MLand.rlerp;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Matrix;
import android.graphics.Outline;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.marshmallow.GameView;
import com.example.marshmallow.MLand;
import com.example.marshmallow.R;

public class Player extends BasePlayer implements GameView {
    private static final String TAG = "Player";

    private final int[] sColors = new int[] {
            // 0xFF78C557,
            0xFFDB4437,
            0xFF3B78E7,
            0xFFF4B400,
            0xFF0F9D58,
            0xFF7B1880,
            0xFF9E9E9E,
    };
    static int sNextColor = 0;

    private final float[] sHull = new float[] {
            0.3f,  0f,    // left antenna
            0.7f,  0f,    // right antenna
            0.92f, 0.33f, // off the right shoulder of Orion
            0.92f, 0.75f, // right hand (our right, not his right)
            0.6f,  1f,    // right foot
            0.4f,  1f,    // left foot BLUE!
            0.08f, 0.75f, // sinistram
            0.08f, 0.33f, // cold shoulder
    };
    public final float[] corners = new float[sHull.length];

    private MLand mLand;

    private TextView mScoreField;

    private void setScore(int score) {
        nativeSetScore(score);
        if (mScoreField != null) {
            // mScoreField.setText(DEBUG_IDDQD ? "??" : String.valueOf(score));
            mScoreField.setText(String.valueOf(score));
        }
    }

    public static Player create(MLand land) {
        final Player p = new Player(land.getContext());
        p.mLand = land;
        p.reset();
        p.setVisibility(View.INVISIBLE);
        land.addView(p, new FrameLayout.LayoutParams(PARAMS.PLAYER_SIZE, PARAMS.PLAYER_SIZE));
        return p;
    }

    public TextView getScoreField() {
        return mScoreField;
    }

    public void addScore(int incr) {
        setScore(getScore() + incr);
    }

    public void setScoreField(TextView tv) {
        Log.d(TAG, "setScoreField: tv = " + tv);
        mScoreField = tv;
        if (tv != null) {
            setScore(getScore()); // reapply
//            mScoreField.setBackgroundResource(R.drawable.scorecard);
            mScoreField.getBackground().setColorFilter(getColor(), PorterDuff.Mode.SRC_ATOP);
            mScoreField.setTextColor(luma(getColor()) > 0.7f ? 0xFF000000 : 0xFFFFFFFF);
        }
    }

    public void reset() {
        // setX(mLand.mWidth / 2);
        float y = mLand.mHeight / 2f + (int)(Math.random() * PARAMS.PLAYER_SIZE) - PARAMS.PLAYER_SIZE / 2f;
        setY(y);
        saveY(y);
        setScore(0);
        setScoreField(mScoreField); // refresh color
        setBoosting(false);
        setDv(0);
    }

    private Player(Context context) {
        super(context);

        setBackgroundResource(R.drawable.m_android);
        getBackground().setTintMode(PorterDuff.Mode.SRC_ATOP);
        int color = sColors[(sNextColor++ % sColors.length)];
        setColor(color);
        // getBackground().setTint(color);
        setBackgroundTintList(ColorStateList.valueOf(color)); //or getBackground().mutate().setTint(color);


        setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                final int w = view.getWidth();
                final int h = view.getHeight();
                final int ix = (int) (w * 0.3f);
                final int iy = (int) (h * 0.2f);
                outline.setRect(ix, iy, w - ix, h - iy);
            }
        });
    }

    public void prepareCheckIntersections() {
        final int inset = (PARAMS.PLAYER_SIZE - PARAMS.PLAYER_HIT_SIZE)/2;
        final int scale = PARAMS.PLAYER_HIT_SIZE;
        final int N = sHull.length/2;
        for (int i=0; i<N; i++) {
            corners[i*2]   = scale * sHull[i*2]   + inset;
            corners[i*2+1] = scale * sHull[i*2+1] + inset;
        }
        final Matrix m = getMatrix();
        m.mapPoints(corners);
    }

    public boolean below(int h) {
        final int N = corners.length/2;
        for (int i=0; i<N; i++) {
            final int y = (int) corners[i*2+1];
            if (y >= h) return true;
        }
        return false;
    }

    public void setTranslationY(float f){
        nativeSetTranslateY(f);
        super.setTranslationY(f);
    }

    public void setTranslationX(float f){
        nativeSetTranslateX(f);
        super.setTranslationX(f);
    }

    public void step(long t_ms, long dt_ms, float t, float dt) {
        if (!isAlive()) {
            // float away with the garbage
            setTranslationX(getTranslateX()-PARAMS.TRANSLATION_PER_SEC*dt);
            return;
        }

        if (isBoosting()) {
            setDv(-PARAMS.BOOST_DV);
        } else {
            setDv(getDv()+PARAMS.G);
        }

        float dv = getDv();

        if (dv < -PARAMS.MAX_V) setDv(-PARAMS.MAX_V);
        else if (dv > PARAMS.MAX_V) setDv(PARAMS.MAX_V);

        final float y = getTranslateY() + dv * dt;
        setTranslationY(y < 0 ? 0 : y);
        setRotation(
                90 + lerp(clamp(rlerp(dv, PARAMS.MAX_V, -1 * PARAMS.MAX_V)), 90, -90));

        prepareCheckIntersections();
    }

    public void boost(float x, float y) {
        setTouchX(x);
        setTouchY(y);
        boost();
    }

    public void boost() {
        setBoosting(true);
        setDv(-PARAMS.BOOST_DV);

        animate().cancel();
        animate()
                .scaleX(1.25f)
                .scaleY(1.25f)
                .translationZ(PARAMS.PLAYER_Z_BOOST)
                .setDuration(100);
        setScaleX(1.25f);
        setScaleY(1.25f);
    }

    public void unboost() {
        setBoosting(false);
        setTouchX(-1);
        setTouchY(-1);

        animate().cancel();
        animate()
                .scaleX(1f)
                .scaleY(1f)
                .translationZ(PARAMS.PLAYER_Z)
                .setDuration(200);
    }

    public void die() {
        setAlive(false);
    }

    public void start() {
        setAlive(true);
    }

}

