//
// Created by droid on 2023-04-01.
//

#ifndef MARSHMALLOW_VIEW_H
#define MARSHMALLOW_VIEW_H

#include <jni.h>

class View {
private:
    float translateX, translateY;
public:
    static jfloat getTranslateX(JNIEnv *env, jobject thiz);
    static void setTranslateX(JNIEnv *env, jobject thiz, jfloat translateY);
    static jfloat getTranslateY(JNIEnv *env, jobject thiz);
    static void setTranslateY(JNIEnv *env, jobject thiz, jfloat translateY);
};


#endif //MARSHMALLOW_VIEW_H
