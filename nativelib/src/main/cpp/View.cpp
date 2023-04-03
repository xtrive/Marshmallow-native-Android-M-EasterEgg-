//
// Created by droid on 2023-04-01.
//

#include "View.h"

View* getInstence(JNIEnv *env, jobject thiz){
    auto clazz = env->GetObjectClass(thiz);
    auto ptrId = env->GetFieldID(clazz, "nPtr", "J");
    auto ptr = env->GetLongField(thiz, ptrId);
    return (View*)ptr;
}

jfloat View::getTranslateX(JNIEnv *env, jobject thiz){
    return getInstence(env, thiz)->translateX;
}
void View::setTranslateX(JNIEnv *env, jobject thiz, jfloat translateX) {
    getInstence(env, thiz)->translateX = translateX;
}
jfloat View::getTranslateY(JNIEnv *env, jobject thiz){
    return getInstence(env, thiz)->translateY;
}
void View::setTranslateY(JNIEnv *env, jobject thiz, jfloat translateY){
    getInstence(env, thiz)->translateY = translateY;
}

