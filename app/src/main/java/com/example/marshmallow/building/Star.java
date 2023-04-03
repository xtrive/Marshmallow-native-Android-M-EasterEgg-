package com.example.marshmallow.building;

import static com.example.marshmallow.MLand.PARAMS;
import static com.example.marshmallow.MLand.irand;

import android.content.Context;

import com.example.marshmallow.R;

public class Star extends Scenery {
    public Star(Context context) {
        super(context);
        setBackgroundResource(R.drawable.m_star);
        w = h = irand(PARAMS.STAR_SIZE_MIN, PARAMS.STAR_SIZE_MAX);
        v = z = 0;
    }
}
