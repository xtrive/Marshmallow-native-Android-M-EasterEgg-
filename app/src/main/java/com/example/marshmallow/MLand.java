/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.marshmallow;

import android.animation.LayoutTransition;
import android.animation.TimeAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.core.util.Consumer;

import com.example.marshmallow.building.Building;
import com.example.marshmallow.building.Cactus;
import com.example.marshmallow.building.Cloud;
import com.example.marshmallow.building.Mountain;
import com.example.marshmallow.building.Scenery;
import com.example.marshmallow.building.Star;
import com.example.marshmallow.obstacle.Obstacle;
import com.example.marshmallow.obstacle.Pop;
import com.example.marshmallow.obstacle.Stem;
import com.example.marshmallow.player.Player;

import java.util.ArrayList;

// It's like LLand, but "M"ultiplayer.
public class MLand extends FrameLayout {
    public static final String TAG = "MLand";

    static{
        System.loadLibrary("nativelib");
    }

    public static final boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG); // setprop log.tag.MLand D
    public static final boolean DEBUG_DRAW = false; // DEBUG

    public static final boolean SHOW_TOUCHES = true;

    public static void L(String s, Object ... objects) {
        if (DEBUG) {
            Log.d(TAG, objects.length == 0 ? s : String.format(s, objects));
        }
    }

    public static final float PI_2 = (float) (Math.PI/2);

    public static final boolean AUTOSTART = true;
    public static final boolean HAVE_STARS = true;

    public static final float DEBUG_SPEED_MULTIPLIER = 0.5f; // only if DEBUG
    public static final boolean DEBUG_IDDQD = false; // Log.isLoggable(TAG + ".iddqd", Log.DEBUG);

    public static final int DEFAULT_PLAYERS = 1;
    public static final int MIN_PLAYERS = 1;
    public static final int MAX_PLAYERS = 6;

    static final float CONTROLLER_VIBRATION_MULTIPLIER = 2f;

    private TimeAnimator mAnim;
    private final Vibrator mVibrator;
    private final AudioManager mAudioManager;
    private final AudioAttributes mAudioAttrs = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME).build();

    private View mSplash;
    private ViewGroup mScoreFields;

    native void playersForEach(Consumer<Player> callback);
    native int playersSize();
    native Player playersGet(int index);
    native boolean playersRemove(Player player);
    native void playersAdd(Player player);

    native void obstaclesForEach(Consumer<Obstacle> callback);
    native int obstaclesSize();
    native Obstacle obstaclesGet(int index);
    native void obstaclesRemove(Obstacle obstacle);
    native void obstaclesClear();
    native void obstaclesAdd(Obstacle obstacle);

    private float t, dt; // dt: lastTimeStep

    private float mLastPipeTime; // in sec
    public static int mCurrentPipeId; // basically, equivalent to the current score
    int mWidth;
    public int mHeight;
    private boolean mAnimating, mPlaying;
    private boolean mFrozen; // after death, a short backoff
    private int mCountdown = 0;
    private boolean mFlipped;

    private int mTimeOfDay;
    private static final int DAY = 0, NIGHT = 1, TWILIGHT = 2, SUNSET = 3;
    private static final int[][] SKIES = {
            { 0xFFc0c0FF, 0xFFa0a0FF }, // DAY
            { 0xFF000010, 0xFF000000 }, // NIGHT
            { 0xFF000040, 0xFF000010 }, // TWILIGHT
            { 0xFFa08020, 0xFF204080 }, // SUNSET
    };

    private int mScene;
    private static final int SCENE_CITY = 0, SCENE_TX = 1, SCENE_ZRH = 2;
    private static final int SCENE_COUNT = 3;

    public static Params PARAMS;

    private static float dp = 1f;

    private final Paint mTouchPaint;
    private final Paint mPlayerTracePaint;

    private final ArrayList<Integer> mGameControllers = new ArrayList<>();

    public MLand(Context context) {
        this(context, null);
    }

    public MLand(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MLand(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        setFocusable(true);

        PARAMS = new Params(getResources());

        mTimeOfDay = irand(0, SKIES.length - 1);
        mScene = irand(0, SCENE_COUNT);

        mTouchPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTouchPaint.setColor(0x80FFFFFF);
        mTouchPaint.setStyle(Paint.Style.FILL);

        mPlayerTracePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPlayerTracePaint.setColor(0x80FFFFFF);
        mPlayerTracePaint.setStyle(Paint.Style.STROKE);
        mPlayerTracePaint.setStrokeWidth(2 * dp);

        // we assume everything will be laid out left|top
        setLayoutDirection(LAYOUT_DIRECTION_LTR);

        setupPlayers(DEFAULT_PLAYERS);

    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        dp = getResources().getDisplayMetrics().density;

        reset();
        if (AUTOSTART) {
            start(false);
        }
    }

    @Override
    public boolean willNotDraw() {
        return !DEBUG;
    }

    public float getGameTime() { return t; }

    public void setScoreFieldHolder(ViewGroup vg) {
        mScoreFields = vg;
        if (vg != null) {
            final LayoutTransition lt = new LayoutTransition();
            lt.setDuration(250);
            mScoreFields.setLayoutTransition(lt);
        }

        playersForEach(p -> {
            mScoreFields.addView(p.getScoreField(),
                    new MarginLayoutParams(
                            MarginLayoutParams.WRAP_CONTENT,
                            MarginLayoutParams.MATCH_PARENT));
        });

    }

    public void setSplash(View v) {
        mSplash = v;
    }



    public int getControllerPlayer(int id) {
        final int player = mGameControllers.indexOf(id);
        if (player < 0 || player >= playersSize()) return 0;
        return player;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        dp = getResources().getDisplayMetrics().density;

        stop();

        reset();
        if (AUTOSTART) {
            start(false);
        }
    }


    public Player getPlayer(int i) {
        return i > -1 && i < playersSize() ? playersGet(i) : null;
    }

    private void addPlayerInternal(Player p) {
        playersAdd(p);
        realignPlayers();
        TextView scoreField = (TextView)
            LayoutInflater.from(getContext()).inflate(R.layout.m_mland_scorefield, null);
        if (mScoreFields != null) {
            mScoreFields.addView(scoreField,
                new MarginLayoutParams(
                        MarginLayoutParams.WRAP_CONTENT,
                        MarginLayoutParams.MATCH_PARENT));
        }
        p.setScoreField(scoreField);
    }

    private void removePlayerInternal(Player p) {
        if (playersRemove(p)) {
            removeView(p);
            if(mScoreFields != null){
                mScoreFields.removeView(p.getScoreField());
            }
            realignPlayers();
        }
    }

    private void realignPlayers() {
        final int N = playersSize();
        float x = (mWidth - (N-1) * PARAMS.PLAYER_SIZE) / 2f;
        for (int i=0; i<N; i++) {
            final Player p = playersGet(i);
            p.setX(x);
            x += PARAMS.PLAYER_SIZE;
        }
    }

    private void clearPlayers() {
        while (playersSize() > 0) {
            removePlayerInternal(playersGet(0));
        }
    }

    public void setupPlayers(int num) {
        clearPlayers();
        for (int i=0; i<num; i++) {
            addPlayerInternal(Player.create(this));
        }
    }

    public void addPlayer() {
        if (getNumPlayers() == MAX_PLAYERS) return;
        addPlayerInternal(Player.create(this));
    }

    public int getNumPlayers() {
        return playersSize();
    }

    public void removePlayer() {
        if (getNumPlayers() == MIN_PLAYERS) return;
        removePlayerInternal(playersGet(playersSize() - 1));
    }

    private void thump(int playerIndex, long ms) {
        if (mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
            // No interruptions. Not even game haptics.
            return;
        }
        if (playerIndex < mGameControllers.size()) {
            int controllerId = mGameControllers.get(playerIndex);
            InputDevice dev = InputDevice.getDevice(controllerId);
            if (dev != null && dev.getVibrator().hasVibrator()) {
                dev.getVibrator().vibrate(
                        (long) (ms * CONTROLLER_VIBRATION_MULTIPLIER),
                        mAudioAttrs);
                return;
            }
        }
        mVibrator.vibrate(ms, mAudioAttrs);
    }

    public void reset() {
        L("reset");
        final Drawable sky = new GradientDrawable(
                GradientDrawable.Orientation.BOTTOM_TOP,
                SKIES[mTimeOfDay]
        );
        sky.setDither(true);
        setBackground(sky);

        mFlipped = frand() > 0.5f;
        setScaleX(mFlipped ? -1 : 1);

        int i = getChildCount();
        while (i-->0) {
            final View v = getChildAt(i);
            if (v instanceof GameView) {
                removeViewAt(i);
            }
        }

        obstaclesClear();
        mCurrentPipeId = 0;

        mWidth = getWidth();
        mHeight = getHeight();

        boolean showingSun = (mTimeOfDay == DAY || mTimeOfDay == SUNSET) && frand() > 0.25;
        if (showingSun) {
            final Star sun = new Star(getContext());
            sun.setBackgroundResource(R.drawable.m_sun);
            final int w = getResources().getDimensionPixelSize(R.dimen.m_sun_size);
            sun.setTranslationX(frand(w, mWidth-w));
            if (mTimeOfDay == DAY) {
                sun.setTranslationY(frand(w, (mHeight * 0.66f)));
                sun.getBackground().setTint(0);
            } else {
                sun.setTranslationY(frand(mHeight * 0.66f, mHeight - w));
                sun.getBackground().setTintMode(PorterDuff.Mode.SRC_ATOP);
                sun.getBackground().setTint(0xC0FF8000);

            }
            addView(sun, new LayoutParams(w, w));
        }
        if (!showingSun) {
            final boolean dark = mTimeOfDay == NIGHT || mTimeOfDay == TWILIGHT;
            final float ff = frand();
            if ((dark && ff < 0.75f) || ff < 0.5f) {
                final Star moon = new Star(getContext());
                moon.setBackgroundResource(R.drawable.m_moon);
                moon.getBackground().setAlpha(dark ? 255 : 128);
                moon.setScaleX(frand() > 0.5 ? -1 : 1);
                moon.setRotation(moon.getScaleX() * frand(5, 30));
                final int w = getResources().getDimensionPixelSize(R.dimen.m_sun_size);
                moon.setTranslationX(frand(w, mWidth - w));
                moon.setTranslationY(frand(w, mHeight - w));
                addView(moon, new LayoutParams(w, w));
            }
        }

        final int mh = mHeight / 6;
        final boolean cloudless = frand() < 0.25;
        final int N = 20;
        for (i=0; i<N; i++) {
            final float r1 = frand();
            final Scenery s;
            if (HAVE_STARS && r1 < 0.3 && mTimeOfDay != DAY) {
                s = new Star(getContext());
            } else if (r1 < 0.6 && !cloudless) {
                s = new Cloud(getContext());
            } else {
                switch (mScene) {
                    case SCENE_ZRH:
                        s = new Mountain(getContext());
                        break;
                    case SCENE_TX:
                        s = new Cactus(getContext());
                        break;
                    case SCENE_CITY:
                    default:
                        s = new Building(getContext());
                        break;
                }
                s.z = (float) i / N;
                // no more shadows for these things
                //s.setTranslationZ(PARAMS.SCENERY_Z * (1+s.z));
                s.v = 0.85f * s.z; // buildings move proportional to their distance
                if (mScene == SCENE_CITY) {
                    s.setBackgroundColor(Color.GRAY);
                    s.h = irand(PARAMS.BUILDING_HEIGHT_MIN, mh);
                }
                final int c = (int)(255f*s.z);
                final Drawable bg = s.getBackground();
                if (bg != null) bg.setColorFilter(Color.rgb(c,c,c), PorterDuff.Mode.MULTIPLY);
            }
            final LayoutParams lp = new LayoutParams(s.w, s.h);
            if (s instanceof Building) {
                lp.gravity = Gravity.BOTTOM;
            } else {
                lp.gravity = Gravity.TOP;
                final float r = frand();
                if (s instanceof Star) {
                    lp.topMargin = (int) (r * r * mHeight);
                } else {
                    lp.topMargin = (int) (1 - r*r * mHeight/2) + mHeight/2;
                }
            }


            addView(s, lp);
            s.setTranslationX(frand(-lp.width, mWidth + lp.width));
        }

        playersForEach(p -> {
            ViewParent parent = p.getParent();
            if(parent == null){
                addView(p);
            }
            p.reset();
        });

        realignPlayers();

        if (mAnim != null) {
            mAnim.cancel();
        }
        mAnim = new TimeAnimator();
        mAnim.setTimeListener((timeAnimator, t, dt) -> step(t, dt));
    }

    public void start(boolean startPlaying) {
        L("start(startPlaying=%s)", startPlaying ? "true" : "false");
        if (startPlaying && mCountdown <= 0) {
            showSplash();

            mSplash.findViewById(R.id.play_button).setEnabled(false);

            final View playImage = mSplash.findViewById(R.id.play_button_image);
            final TextView playText = (TextView) mSplash.findViewById(R.id.play_button_text);

            playImage.animate().alpha(0f);
            playText.animate().alpha(1f);

            mCountdown = 3;
            post(new Runnable() {
                @Override
                public void run() {
                    if (mCountdown == 0) {
                        startPlaying();
                    } else {
                        postDelayed(this, 500);
                    }
                    playText.setText(String.valueOf(mCountdown));
                    mCountdown--;
                }
            });
        }

        playersForEach(p -> {
            p.setVisibility(View.INVISIBLE);
        });

        if (!mAnimating) {
            mAnim.start();
            mAnimating = true;
        }
    }

    public void hideSplash() {
        if (mSplash != null && mSplash.getVisibility() == View.VISIBLE) {
            mSplash.setClickable(false);
            mSplash.animate().alpha(0).translationZ(0).setDuration(300).withEndAction(
                    () -> mSplash.setVisibility(View.GONE)
            );
        }
    }

    public void showSplash() {
        if (mSplash != null && mSplash.getVisibility() != View.VISIBLE) {
            mSplash.setClickable(true);
            mSplash.setAlpha(0f);
            mSplash.setVisibility(View.VISIBLE);
            mSplash.animate().alpha(1f).setDuration(1000);
            mSplash.findViewById(R.id.play_button_image).setAlpha(1f);
            mSplash.findViewById(R.id.play_button_text).setAlpha(0f);
            mSplash.findViewById(R.id.play_button).setEnabled(true);
            mSplash.findViewById(R.id.play_button).requestFocus();
        }
    }

    public void startPlaying() {
        mPlaying = true;

        t = 0;
        // there's a sucker born every OBSTACLE_PERIOD
        mLastPipeTime = getGameTime() - PARAMS.OBSTACLE_PERIOD;

        hideSplash();

        realignPlayers();

        final int N = playersSize();
        for (int i=0; i<N; i++) {
            final Player p = playersGet(i);
            p.setVisibility(View.VISIBLE);
            Log.d(TAG, "startPlaying: i = " + i + ",backgroundTintList = " + p.getDrawable());
            p.reset();
            p.start();
            p.boost(-1, -1); // start you off flying!
            p.unboost(); // not forever, though
        }
    }

    public void stop() {
        if (mAnimating) {
            mAnim.cancel();
            mAnim = null;
            mAnimating = false;
            mPlaying = false;
            mTimeOfDay = irand(0, SKIES.length - 1); // for next reset
            mScene = irand(0, SCENE_COUNT);
            mFrozen = true;
            playersForEach(Player::die);
            postDelayed(() -> mFrozen = false, 250);
        }
    }

    public static float lerp(float x, float a, float b) {
        return (b - a) * x + a;
    }

    public static float rlerp(float v, float a, float b) {
        return (v - a) / (b - a);
    }

    public static float clamp(float f) {
        return f < 0f ? 0f : f > 1f ? 1f : f;
    }

    public static float frand() {
        return (float) Math.random();
    }

    public static float frand(float a, float b) {
        return lerp(frand(), a, b);
    }

    public static int irand(int a, int b) {
        return Math.round(frand((float) a, (float) b));
    }

    public static int pick(int[] l) {
        return l[irand(0, l.length-1)];
    }

    private void step(long t_ms, long dt_ms) {
        // Log.d(TAG, "step: t_ms = " + t_ms + ", dt_ms = " + dt_ms);
        // totalTime:动画开始以来的总时间,以毫秒为单位
        // deltaTime:从前一帧到现在的运行时间,以毫秒为单位


        t = t_ms / 1000f; // seconds
        dt = dt_ms / 1000f;

        if (DEBUG) {
            t *= DEBUG_SPEED_MULTIPLIER;
            dt *= DEBUG_SPEED_MULTIPLIER;
        }

        // 1. Move all objects and update bounds
        final int N = getChildCount();
        int i = 0;
        for (; i<N; i++) {
            final View v = getChildAt(i);
            if (v instanceof GameView) {
                ((GameView) v).step(t_ms, dt_ms, t, dt);
            }
        }

        if (mPlaying) {
            int livingPlayers = 0;
            for (i = 0; i < playersSize(); i++) {
                final Player p = getPlayer(i);

                if (p.isAlive()) {
                    // 2. Check for altitude
                    if (p.below(mHeight)) {
                        if (DEBUG_IDDQD) {
                            poke(i);
                            unpoke(i);
                        } else {
                            L("player %d hit the floor", i);
                            thump(i, 80);
                            p.die();
                        }
                    }

                    // 3. Check for obstacles
                    int maxPassedStem = 0;
                    for (int j = obstaclesSize(); j-- > 0; ) {
                        final Obstacle ob = obstaclesGet(j);
                        if (ob.intersects(p) && !DEBUG_IDDQD) {
                            L("player hit an obstacle");
                            thump(i, 80);
                            p.die();
                        } else if (ob.cleared(p)) {
                            if (ob instanceof Stem) {
                                maxPassedStem = Math.max(maxPassedStem, ((Stem) ob).getPipeId());
                            }
                        }
                    }

                    if (maxPassedStem > p.getScore()) {
                        p.addScore(1);
                    }
                }

                if (p.isAlive()) livingPlayers++;
            }

            if (livingPlayers == 0) {
                stop();

            }
        }

        i = N;
        // todo bug
        //  https://github.com/aosp-mirror/platform_frameworks_base/blob/marshmallow-mr3-release/packages/SystemUI/src/com/android/systemui/egg/MLand.java

        // 4. Handle edge of screen
        // Walk backwards to make sure removal is safe
        while (i-->0) {
            final View v = getChildAt(i);
            if (v instanceof Obstacle) {
                if (v.getTranslationX() + v.getWidth() < 0) {
                    Log.d(TAG, "step: remove obstacle, i = " + i);
                    removeViewAt(i);
                    obstaclesRemove((Obstacle)v);
                }
            } else if (v instanceof Scenery) {
                final Scenery s = (Scenery) v;
                if (v.getTranslationX() + s.w < 0) {
                    v.setTranslationX(getWidth());
                }
            }
        }

        // 3. Time for more obstacles!
        if (mPlaying && (t - mLastPipeTime) > PARAMS.OBSTACLE_PERIOD) {
            mLastPipeTime = t;
            mCurrentPipeId ++;
            final int obstacley =
                    (int)(frand() * (mHeight - 2*PARAMS.OBSTACLE_MIN - PARAMS.OBSTACLE_GAP)) +
                    PARAMS.OBSTACLE_MIN;

            final int inset = (PARAMS.OBSTACLE_WIDTH - PARAMS.OBSTACLE_STEM_WIDTH) / 2;
            final int yinset = PARAMS.OBSTACLE_WIDTH/2;

            final int d1 = irand(0,250);
            final Obstacle s1 = new Stem(getContext(), obstacley - yinset, false);
            addView(s1, new LayoutParams(
                    PARAMS.OBSTACLE_STEM_WIDTH,
                    (int) s1.getH(),
                    Gravity.TOP|Gravity.LEFT));
            s1.setTranslationX(mWidth+inset);
            s1.setTranslationY(-s1.getH()-yinset);
            s1.setTranslationZ(PARAMS.OBSTACLE_Z*0.75f);
            s1.animate()
                    .translationY(0)
                    .setStartDelay(d1)
                    .setDuration(250);
            obstaclesAdd(s1);

            final Obstacle p1 = new Pop(getContext(), PARAMS.OBSTACLE_WIDTH);
            addView(p1, new LayoutParams(
                    PARAMS.OBSTACLE_WIDTH,
                    PARAMS.OBSTACLE_WIDTH,
                    Gravity.TOP|Gravity.LEFT));
            p1.setTranslationX(mWidth);
            p1.setTranslationY(-PARAMS.OBSTACLE_WIDTH);
            p1.setTranslationZ(PARAMS.OBSTACLE_Z);
            p1.setScaleX(0.25f);
            p1.setScaleY(-0.25f);
            p1.animate()
                    .translationY(s1.getH()-inset)
                    .scaleX(1f)
                    .scaleY(-1f)
                    .setStartDelay(d1)
                    .setDuration(250);
            obstaclesAdd(p1);

            final int d2 = irand(0,250);
            final Obstacle s2 = new Stem(getContext(),
                    mHeight - obstacley - PARAMS.OBSTACLE_GAP - yinset,
                    true);
            addView(s2, new LayoutParams(
                    PARAMS.OBSTACLE_STEM_WIDTH,
                    (int) s2.getH(),
                    Gravity.TOP|Gravity.LEFT));
            s2.setTranslationX(mWidth+inset);
            s2.setTranslationY(mHeight+yinset);
            s2.setTranslationZ(PARAMS.OBSTACLE_Z*0.75f);
            s2.animate()
                    .translationY(mHeight-s2.getH())
                    .setStartDelay(d2)
                    .setDuration(400);
            obstaclesAdd(s2);

            final Obstacle p2 = new Pop(getContext(), PARAMS.OBSTACLE_WIDTH);
            addView(p2, new LayoutParams(
                    PARAMS.OBSTACLE_WIDTH,
                    PARAMS.OBSTACLE_WIDTH,
                    Gravity.TOP|Gravity.LEFT));
            p2.setTranslationX(mWidth);
            p2.setTranslationY(mHeight);
            p2.setTranslationZ(PARAMS.OBSTACLE_Z);
            p2.setScaleX(0.25f);
            p2.setScaleY(0.25f);
            p2.animate()
                    .translationY(mHeight-s2.getH()-yinset)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setStartDelay(d2)
                    .setDuration(400);
            obstaclesAdd(p2);
        }

        if (SHOW_TOUCHES || DEBUG_DRAW) invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        L("touch: %s", ev);
        final int actionIndex = ev.getActionIndex();
        final float x = ev.getX(actionIndex);
        final float y = ev.getY(actionIndex);
        int playerIndex = (int) (getNumPlayers() * (x / getWidth()));
        if (mFlipped) playerIndex = getNumPlayers() - 1 - playerIndex;
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                poke(playerIndex, x, y);
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                unpoke(playerIndex);
                return true;
        }
        return false;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        L("trackball: %s", ev);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                poke(0);
                return true;
            case MotionEvent.ACTION_UP:
                unpoke(0);
                return true;
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent ev) {
        L("keyDown: %d", keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_SPACE:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_BUTTON_A:
                int player = getControllerPlayer(ev.getDeviceId());
                poke(player);
                return true;
        }
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent ev) {
        L("keyDown: %d", keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_SPACE:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_BUTTON_A:
                int player = getControllerPlayer(ev.getDeviceId());
                unpoke(player);
                return true;
        }
        return false;
    }

    @Override
    public boolean onGenericMotionEvent (MotionEvent ev) {
        L("generic: %s", ev);
        return false;
    }

    private void poke(int playerIndex) {
        poke(playerIndex, -1, -1);
    }

    private void poke(int playerIndex, float x, float y) {
        L("poke(%d)", playerIndex);
        if (mFrozen) return;
        if (!mAnimating) {
            reset();
        }
        if (!mPlaying) {
            start(true);
        } else {
            final Player p = getPlayer(playerIndex);
            if (p == null) return; // no player for this controller
            p.boost(x, y);
            if (DEBUG) {
                p.setDv(p.getDv() * DEBUG_SPEED_MULTIPLIER);
                p.animate().setDuration((long) (200 / DEBUG_SPEED_MULTIPLIER));
            }
        }
    }

    private void unpoke(int playerIndex) {
        L("unboost(%d)", playerIndex);
        if (mFrozen || !mAnimating || !mPlaying) return;
        final Player p = getPlayer(playerIndex);
        if (p == null) return; // no player for this controller
        p.unboost();
    }

    final Paint pt = new Paint(Paint.ANTI_ALIAS_FLAG);

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);

        if (SHOW_TOUCHES) {
            playersForEach(p -> {
                if (p.getTouchX() > 0) {
                    mTouchPaint.setColor(0x80FFFFFF & p.getColor());
                    mPlayerTracePaint.setColor(0x80FFFFFF & p.getColor());
                    float x1 = p.getTouchX();
                    float y1 = p.getTouchY();
                    c.drawCircle(x1, y1, 100, mTouchPaint);
                    float x2 = p.getX() + p.getPivotX();
                    float y2 = p.getY() + p.getPivotY();
                    float angle = PI_2 - (float) Math.atan2(x2-x1, y2-y1);
                    x1 += 100*Math.cos(angle);
                    y1 += 100*Math.sin(angle);
                    c.drawLine(x1, y1, x2, y2, mPlayerTracePaint);
                }
            });
        }

        if (!DEBUG_DRAW) return;

        pt.setColor(0xFFFFFFFF);
        playersForEach(p->{
            final int L = p.corners.length;
            final int N = L / 2;
            for (int i = 0; i < N; i++) {
                final int x = (int) p.corners[i * 2];
                final int y = (int) p.corners[i * 2 + 1];
                c.drawCircle(x, y, 4, pt);
                c.drawLine(x, y,
                        p.corners[(i * 2 + 2) % L],
                        p.corners[(i * 2 + 3) % L],
                        pt);
            }
        });

        pt.setStyle(Paint.Style.STROKE);
        pt.setStrokeWidth(getResources().getDisplayMetrics().density);

        final int M = getChildCount();
        pt.setColor(0x8000FF00);
        for (int i=0; i<M; i++) {
            final View v = getChildAt(i);
            if (v instanceof Player) continue;
            if (!(v instanceof GameView)) continue;
            if (v instanceof Pop) {
                final Pop pop = (Pop) v;
                c.drawCircle(pop.cx, pop.cy, pop.r, pt);
            } else {
                final Rect r = new Rect();
                v.getHitRect(r);
                c.drawRect(r, pt);
            }
        }

        pt.setColor(Color.YELLOW);
        final StringBuilder sb = new StringBuilder("obstacles: ");
        sb.append(obstaclesSize());
        sb.append("(");
        sb.append(getChildCount());
        sb.append(")");
        obstaclesForEach(ob->{
            sb.append(ob.hitRect.toShortString());
            sb.append(" ");
        });

        pt.setTextSize(20f);
        c.drawText(sb.toString(), 20, 100, pt);
    }

    public static boolean isGamePad(InputDevice dev) {
        int sources = dev.getSources();

        // Verify that the device has gamepad buttons, control sticks, or both.
        return (((sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD)
                || ((sources & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK));
    }

    public ArrayList<Integer> getGameControllers() {
        mGameControllers.clear();
        int[] deviceIds = InputDevice.getDeviceIds();
        for (int deviceId : deviceIds) {
            InputDevice dev = InputDevice.getDevice(deviceId);
            if (isGamePad(dev)) {
                if (!mGameControllers.contains(deviceId)) {
                    mGameControllers.add(deviceId);
                }
            }
        }
        Log.d(TAG, "getGameControllers: mGameControllers = " + mGameControllers);
        return mGameControllers;
    }

    public static float luma(int color) {
        return    0.2126f * (float) (color & 0xFF0000) / 0xFF0000
                + 0.7152f * (float) (color & 0xFF00) / 0xFF00
                + 0.0722f * (float) (color & 0xFF) / 0xFF;
    }



}
