package com.example.marshmallow.building;

import static com.example.marshmallow.MLand.PARAMS;
import static com.example.marshmallow.MLand.frand;
import static com.example.marshmallow.MLand.irand;

import android.content.Context;

import com.example.marshmallow.R;

public class Cloud extends Scenery {
    public Cloud(Context context) {
        super(context);
        setBackgroundResource(frand() < 0.01f ? R.drawable.m_cloud_off : R.drawable.m_cloud);
        getBackground().setAlpha(0x40);
        w = h = irand(PARAMS.CLOUD_SIZE_MIN, PARAMS.CLOUD_SIZE_MAX);
        z = 0;
        v = frand(0.15f,0.5f);
    }
}
