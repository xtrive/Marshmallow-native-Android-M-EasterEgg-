//
// Created by droid on 2023-03-29.
//

#ifndef MARSHMALLOW_PLAYER_H
#define MARSHMALLOW_PLAYER_H

#include "jni.h"
#include "View.h"

class Player : View{
public:
    float dv;
    int color;
    bool mBoosting;
    float mTouchX = -1, mTouchY = -1;
    bool mAlive;
    int mScore;
    float y;
    jobject jPlayer;

    static jlong initPlayer(JNIEnv *env, jobject thiz);
    static void releasePlayer(JNIEnv *env, jobject thiz, jlong ptr);

    static jfloat getDv(JNIEnv *env, jobject thiz);
    static void setDv(JNIEnv *env, jobject thiz, jfloat val);

    static jint getColor(JNIEnv *env, jobject thiz);
    static void setColor(JNIEnv *env, jobject thiz, jint color);

    static jboolean isBoosting(JNIEnv *env, jobject thiz);
    static void setBoosting(JNIEnv *env, jobject thiz, jboolean boosting);

    static jfloat getTouchX(JNIEnv *env, jobject thiz);
    static void setTouchX(JNIEnv *env, jobject thiz, jfloat touchX);

    static jfloat getTouchY(JNIEnv *env, jobject thiz);
    static void setTouchY(JNIEnv *env, jobject thiz, jfloat touchY);

    static jboolean isAlive(JNIEnv *env, jobject thiz);
    static void setAlive(JNIEnv *env, jobject thiz, jboolean alive);

    static jint getScore(JNIEnv *env, jobject thiz);
    static void setScore(JNIEnv *env, jobject thiz, jint score);

    static void saveY(JNIEnv *env, jobject thiz, jfloat y);

};

#endif //MARSHMALLOW_PLAYER_H

