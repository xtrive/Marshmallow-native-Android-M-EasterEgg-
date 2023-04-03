//
// Created by droid on 2023-03-29.
//

#include "Player.h"
#include <jni.h>

jlong Player::initPlayer(JNIEnv *env, jobject thiz) {
    auto player = new Player();
    return (jlong)player;
}
void Player::releasePlayer(JNIEnv *env, jobject thiz, jlong ptr) {
    auto player = (Player*)ptr;
    delete player;
}

// ------------------------------------------------------------------------------------------

Player* getPlayer(JNIEnv *env, jobject thiz){
    auto clazz = env->GetObjectClass(thiz);
    auto ptrId = env->GetFieldID(clazz, "nPtr", "J");
    auto ptr = env->GetLongField(thiz, ptrId);
    return (Player*)ptr;
}

// ------------------------------------------------------------------------------------------

jfloat Player::getDv(JNIEnv *env, jobject thiz){
    return getPlayer(env, thiz)->dv;
}
void Player::setDv(JNIEnv *env, jobject thiz, jfloat val){
    getPlayer(env, thiz)->dv = val;
}

// ------------------------------------------------------------------------------------------

jint Player::getColor(JNIEnv *env, jobject thiz){
    return getPlayer(env, thiz)->color;
}
void Player::setColor(JNIEnv *env, jobject thiz, jint color){
    getPlayer(env, thiz)->color = color;
}

// ------------------------------------------------------------------------------------------

jboolean Player::isBoosting(JNIEnv *env, jobject thiz){
    return getPlayer(env, thiz)->mBoosting;
}
void Player::setBoosting(JNIEnv *env, jobject thiz, jboolean boosting){
    getPlayer(env, thiz)->mBoosting = boosting;
}

// ------------------------------------------------------------------------------------------

jfloat Player::getTouchX(JNIEnv *env, jobject thiz){
    return getPlayer(env, thiz)->mTouchX;
}
void Player::setTouchX(JNIEnv *env, jobject thiz, jfloat touchX){
    getPlayer(env, thiz)->mTouchX = touchX;
}

// ------------------------------------------------------------------------------------------

jfloat Player::getTouchY(JNIEnv *env, jobject thiz){
    return getPlayer(env, thiz)->mTouchY;
}
void Player::setTouchY(JNIEnv *env, jobject thiz, jfloat touchY){
    getPlayer(env, thiz)->mTouchY = touchY;
}

// ------------------------------------------------------------------------------------------

jboolean Player::isAlive(JNIEnv *env, jobject thiz){
    return getPlayer(env, thiz)->mAlive;
}
void Player::setAlive(JNIEnv *env, jobject thiz, jboolean alive){
    getPlayer(env, thiz)->mAlive = alive;
}

// ------------------------------------------------------------------------------------------

jint Player::getScore(JNIEnv *env, jobject thiz){
    return getPlayer(env, thiz)->mScore;
}
void Player::setScore(JNIEnv *env, jobject thiz, jint score){
    getPlayer(env, thiz)->mScore = score;
}


// ------------------------------------------------------------------------------------------

void Player::saveY(JNIEnv *env, jobject thiz, jfloat y){
    getPlayer(env, thiz)->y = y;
}


