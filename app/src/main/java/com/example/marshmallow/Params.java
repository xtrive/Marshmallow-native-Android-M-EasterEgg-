package com.example.marshmallow;

import android.content.res.Resources;

public class Params {

    public final float TRANSLATION_PER_SEC = 0f;

    /* val OBSTACLE_SPACING = 0*/

    public final int OBSTACLE_PERIOD = 0;

    public final int BOOST_DV = 0;

    public final int PLAYER_HIT_SIZE = 0;

    public final int PLAYER_SIZE = 0;

    public final int OBSTACLE_WIDTH = 0;

    public final int OBSTACLE_STEM_WIDTH = 0;

    public final int OBSTACLE_GAP = 0;

    public final int OBSTACLE_MIN = 0;

    public final int  BUILDING_WIDTH_MIN = 0;

    public final int  BUILDING_WIDTH_MAX = 0;

    public final int  BUILDING_HEIGHT_MIN = 0;

    public final int CLOUD_SIZE_MIN = 0;

    public final int CLOUD_SIZE_MAX = 0;

    public final int STAR_SIZE_MIN = 0;

    public final int STAR_SIZE_MAX = 0;

    public final int G = 0;

    public final int MAX_V = 0;

    /*
    private val SCENERY_Z = 0f*/

    public final float OBSTACLE_Z = 0f;


    public final float PLAYER_Z = 0f;


    public final float PLAYER_Z_BOOST = 0f;

    /*
    private val HUD_Z = 0f*/


    public Params(Resources resources) {
        initParamClass(resources.getDisplayMetrics().density);
    }

    native void initParamClass(float f);

}
