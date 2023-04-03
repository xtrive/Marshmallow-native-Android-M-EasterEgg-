package com.example.marshmallow.building;

import static com.example.marshmallow.MLand.PARAMS;
import static com.example.marshmallow.MLand.irand;
import static com.example.marshmallow.MLand.pick;

import android.content.Context;

import com.example.marshmallow.R;

public class Mountain extends Building {
    static final int[] MOUNTAINS = {
            R.drawable.m_mountain1, R.drawable.m_mountain2, R.drawable.m_mountain3};

    public Mountain(Context context) {
        super(context);

        setBackgroundResource(pick(MOUNTAINS));
        w = h = irand(PARAMS.BUILDING_WIDTH_MAX / 2, PARAMS.BUILDING_WIDTH_MAX);
        z = 0;
    }
}
