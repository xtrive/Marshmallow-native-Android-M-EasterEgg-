//
// Created by droid on 2023-03-30.
//

#include "Obstacle.h"

jlong Obstacle::initObstacleNative(JNIEnv *env, jobject thiz, jfloat h){
    auto o = new Obstacle();
    o->h = h;
    return (jlong)o;
}

void Obstacle::releaseObstacleNative(JNIEnv *env, jobject thiz, jlong nPtr){
    auto o = (Obstacle*)nPtr;
    delete o;
}

Obstacle* getObstacle(JNIEnv *env, jobject thiz){
    auto clazz = env->GetObjectClass(thiz);
    auto ptrId = env->GetFieldID(clazz, "nPtr", "J");
    auto ptr = env->GetLongField(thiz, ptrId);
    return (Obstacle*)ptr;
}

jfloat Obstacle::getObstacleH(JNIEnv *env, jobject thiz){
    auto o = getObstacle(env, thiz);
    return o->h;
}

void Obstacle::saveHitRect(JNIEnv *env, jobject thiz, int left, int top, int right, int bottom){
    auto o = getObstacle(env, thiz);
    o->left = left;
    o->top = top;
    o->right = right;
    o->bottom = bottom;
}



