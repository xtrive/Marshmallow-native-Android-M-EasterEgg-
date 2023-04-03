//
// Created by droid on 2023-03-30.
//

#ifndef MARSHMALLOW_OBSTACLE_H
#define MARSHMALLOW_OBSTACLE_H

#include "jni.h"
#include "View.h"

class Obstacle : View {
public:
    int left, top, right, bottom;
    float h;
    jobject jObstacle;

    static jlong initObstacleNative(JNIEnv *env, jobject thiz, jfloat h);
    static void releaseObstacleNative(JNIEnv *env, jobject thiz, jlong nPtr);

    static jfloat getObstacleH(JNIEnv *env, jobject thiz);

    static void saveHitRect(JNIEnv *env, jobject thiz, int left, int top, int right, int bottom);

};

#endif //MARSHMALLOW_OBSTACLE_H

