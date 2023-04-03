package com.example.marshmallow.building;

import static com.example.marshmallow.MLand.PARAMS;
import static com.example.marshmallow.MLand.irand;

import android.content.Context;

public class Building extends Scenery {
    public Building(Context context) {
        super(context);

        w = irand(PARAMS.BUILDING_WIDTH_MIN, PARAMS.BUILDING_WIDTH_MAX);
        h = 0; // will be setup later, along with z
    }
}
