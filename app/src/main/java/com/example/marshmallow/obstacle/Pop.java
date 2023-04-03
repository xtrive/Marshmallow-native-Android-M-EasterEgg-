package com.example.marshmallow.obstacle;

import static com.example.marshmallow.MLand.frand;
import static com.example.marshmallow.MLand.pick;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewOutlineProvider;

import com.example.marshmallow.player.Player;
import com.example.marshmallow.R;

@SuppressLint("ViewConstructor")
public class Pop extends Obstacle {
    static final int[] ANTENNAE = new int[] {R.drawable.m_mm_antennae, R.drawable.m_mm_antennae2};
    static final int[] EYES = new int[] {R.drawable.m_mm_eyes, R.drawable.m_mm_eyes2};
    static final int[] MOUTHS = new int[] {R.drawable.m_mm_mouth1, R.drawable.m_mm_mouth2,
            R.drawable.m_mm_mouth3, R.drawable.m_mm_mouth4};

    int mRotate;
    public int cx, cy, r;
    // The marshmallow illustration and hitbox is 2/3 the size of its container.
    Drawable antenna, eyes, mouth;


    public Pop(Context context, float h) {
        super(context, h);
        setBackgroundResource(R.drawable.m_mm_head);
        antenna = context.getDrawable(pick(ANTENNAE));
        if (frand() > 0.5f) {
            eyes = context.getDrawable(pick(EYES));
            if (frand() > 0.8f) {
                mouth = context.getDrawable(pick(MOUTHS));
            }
        }
        setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                final int pad = (int) (getWidth() * 1f/6);
                outline.setOval(pad, pad, getWidth()-pad, getHeight()-pad);
            }
        });
    }

    public boolean intersects(Player p) {
        final int N = p.corners.length/2;
        for (int i=0; i<N; i++) {
            final int x = (int) p.corners[i*2];
            final int y = (int) p.corners[i*2+1];
            if (Math.hypot(x-cx, y-cy) <= r) return true;
        }
        return false;
    }

    @Override
    public void step(long t_ms, long dt_ms, float t, float dt) {
        super.step(t_ms, dt_ms, t, dt);
        if (mRotate != 0) {
            setRotation(getRotation() + dt * 45 * mRotate);
        }

        cx = (hitRect.left + hitRect.right)/2;
        cy = (hitRect.top + hitRect.bottom)/2;
        r = getWidth() / 3; // see above re 2/3 container size
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);
        if (antenna != null) {
            antenna.setBounds(0, 0, c.getWidth(), c.getHeight());
            antenna.draw(c);
        }
        if (eyes != null) {
            eyes.setBounds(0, 0, c.getWidth(), c.getHeight());
            eyes.draw(c);
        }
        if (mouth != null) {
            mouth.setBounds(0, 0, c.getWidth(), c.getHeight());
            mouth.draw(c);
        }
    }
}
