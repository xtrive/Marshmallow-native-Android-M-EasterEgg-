package com.example.marshmallow.obstacle;

import static com.example.marshmallow.MLand.PARAMS;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;

import com.example.marshmallow.GameView;
import com.example.marshmallow.player.Player;

@SuppressLint("ViewConstructor")
public class Obstacle extends BaseObstacle implements GameView {

    public final Rect hitRect = new Rect();

    public Obstacle(Context context, float h) {
        super(context, h);
        setBackgroundColor(0xFFFF0000);

    }

    public boolean intersects(Player p) {
        final int N = p.corners.length/2;
        for (int i=0; i<N; i++) {
            final int x = (int) p.corners[i*2];
            final int y = (int) p.corners[i*2+1];
            if (hitRect.contains(x, y)) return true;
        }
        return false;
    }

    public boolean cleared(Player p) {
        final int N = p.corners.length/2;
        for (int i=0; i<N; i++) {
            final int x = (int) p.corners[i*2];
            if (hitRect.right >= x) return false;
        }
        return true;
    }

    @Override
    public void step(long t_ms, long dt_ms, float t, float dt) {
        setTranslationX(getTranslationX()-PARAMS.TRANSLATION_PER_SEC*dt);
        getHitRect(hitRect);
        setNativeHitRect(hitRect.left, hitRect.top, hitRect.right, hitRect.bottom);
    }



}


