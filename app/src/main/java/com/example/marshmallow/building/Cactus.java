package com.example.marshmallow.building;

import static com.example.marshmallow.MLand.PARAMS;
import static com.example.marshmallow.MLand.irand;
import static com.example.marshmallow.MLand.pick;

import android.content.Context;

import com.example.marshmallow.R;

public class Cactus extends Building {

    static final int[] CACTI = { R.drawable.m_cactus1, R.drawable.m_cactus2, R.drawable.m_cactus3};

    public Cactus(Context context) {
        super(context);

        setBackgroundResource(pick(CACTI));
        w = h = irand(PARAMS.BUILDING_WIDTH_MAX / 4, PARAMS.BUILDING_WIDTH_MAX / 2);
    }
}
