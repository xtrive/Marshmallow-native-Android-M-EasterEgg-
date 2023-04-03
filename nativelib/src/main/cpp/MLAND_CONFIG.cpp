#include <jni.h>

//
// Created by droid on 2023-03-26.
//

const float m_obstacle_spacing = 380.0f;
const float m_translation_per_sec = 100.0f;
const float m_boost_dv = 550.0f;
const float m_player_hit_size = 40.0f;
const float m_player_size = 40.0f;
const float m_obstacle_width = 130.0f;
const float m_obstacle_stem_width = 8.0f;
const float m_obstacle_gap = 140.0f;
const float m_obstacle_height_min = 48.0f;
const float m_building_width_min = 50.0f;
const float m_building_width_max = 250.0f;
const float m_building_height_min = 20.0f;
const float m_cloud_size_min = 10.0f;
const float m_cloud_size_max = 100.0f;
/*const float m_sun_size = 45.0f;
const float m_moon_size = 30.0f;*/
const float m_star_size_min = 3.0f;
const float m_star_size_max = 5.0f;
const float m_G = 30.0f;
const float m_max_v = 1000.0f;
/*const float m_scenery_z = 6.0f;*/
const float m_obstacle_z = 18.0f;
const float m_player_z = 18.0f;
const float m_player_z_boost = 20.0f;
/*const float m_hud_z = 10.0f; // #res/values/mland_config.xml m_hud_z*/

void setFValue(JNIEnv *env, jobject thiz, jclass jclazz, const char *fieldName, float value);

void setIValue(JNIEnv *env, jobject thiz, jclass jclazz, const char *fieldName, float value);

extern "C"
JNIEXPORT void JNICALL
Java_com_example_marshmallow_Params_initParamClass(JNIEnv *env, jobject thiz, jfloat density) {
    jclass jclazz = env->GetObjectClass(thiz);

    setFValue(env, thiz, jclazz, "TRANSLATION_PER_SEC", m_translation_per_sec * density);
    
    // setIValue(env, thiz, jclazz, "OBSTACLE_SPACING", m_obstacle_spacing * density);
    setIValue(env, thiz, jclazz, "OBSTACLE_PERIOD", (m_obstacle_spacing / m_translation_per_sec));
    setIValue(env, thiz, jclazz, "BOOST_DV", m_boost_dv * density);
    setIValue(env, thiz, jclazz, "PLAYER_HIT_SIZE", m_player_hit_size * density);
    setIValue(env, thiz, jclazz, "PLAYER_SIZE", m_player_size * density);
    setIValue(env, thiz, jclazz, "OBSTACLE_WIDTH", m_obstacle_width * density);
    setIValue(env, thiz, jclazz, "OBSTACLE_STEM_WIDTH", m_obstacle_stem_width * density);
    setIValue(env, thiz, jclazz, "OBSTACLE_GAP", m_obstacle_gap * density);
    setIValue(env, thiz, jclazz, "OBSTACLE_MIN", m_obstacle_height_min * density);
    setIValue(env, thiz, jclazz, "BUILDING_HEIGHT_MIN", m_building_height_min * density);
    setIValue(env, thiz, jclazz, "BUILDING_WIDTH_MIN", m_building_width_min * density);
    setIValue(env, thiz, jclazz, "BUILDING_WIDTH_MAX", m_building_width_max * density);
    setIValue(env, thiz, jclazz, "CLOUD_SIZE_MIN", m_cloud_size_min * density);
    setIValue(env, thiz, jclazz, "CLOUD_SIZE_MAX", m_cloud_size_max * density);
    setIValue(env, thiz, jclazz, "STAR_SIZE_MIN", m_star_size_min * density);
    setIValue(env, thiz, jclazz, "STAR_SIZE_MAX", m_star_size_max * density);
    setIValue(env, thiz, jclazz, "G", m_G * density);
    setIValue(env, thiz, jclazz, "MAX_V", m_max_v * density);

    // setFValue(env, thiz, jclazz, "SCENERY_Z", m_scenery_z * density);
    setFValue(env, thiz, jclazz, "OBSTACLE_Z", m_obstacle_z * density);
    setFValue(env, thiz, jclazz, "PLAYER_Z", m_player_z * density);
    setFValue(env, thiz, jclazz, "PLAYER_Z_BOOST", m_player_z_boost * density);
    // setFValue(env, thiz, jclazz, "HUD_Z", m_hud_z * density);

    // Sanity checking
    if (m_obstacle_height_min <= m_obstacle_width / 2) {
        setIValue(env, thiz, jclazz, "OBSTACLE_MIN", m_obstacle_height_min * density / 2 + 1);
    }

}

void setFValue(JNIEnv *env, jobject thiz, jclass jclazz, const char* fieldName, float value){
    jfieldID filedId = env->GetFieldID(jclazz, fieldName, "F");
    env->SetFloatField(thiz, filedId, value);
}

void setIValue(JNIEnv *env, jobject thiz, jclass jclazz, const char* fieldName, float value){
    jfieldID filedId = env->GetFieldID(jclazz, fieldName, "I");
    env->SetIntField(thiz, filedId, (int)value);
}

