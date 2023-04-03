#include <jni.h>
#include <string>
#include <vector>
#include <android/log.h>
#include "Player.h"
#include "Obstacle.h"

#define LOGV(msg...) __android_log_print(ANDROID_LOG_VERBOSE, "Native_MARSHMALLOW", msg)
#define LOGW(msg...) __android_log_print(ANDROID_LOG_WARN, "Native_MARSHMALLOW", msg)
#define LOGE(msg...) __android_log_print(ANDROID_LOG_ERROR, "Native_MARSHMALLOW", msg)

template <class T>
int getArrayLen(T &array){
    return sizeof(array) / sizeof(array[0]);
}

using namespace std;

class Game{
public:
    vector<Player*> *mPlayers = new vector<Player*>();
    vector<Obstacle*> *mObstaclesInPlay = new vector<Obstacle*>();

};

static Game* gameInstance;


// ===============================================================================================

void playersForEach(JNIEnv *env, jobject thiz, jobject consumerObj){
    auto jclass = env->FindClass("androidx/core/util/Consumer");
    auto methodID = env->GetMethodID(jclass, "accept", "(Ljava/lang/Object;)V");

    auto *args = new jvalue[1];
    for(auto &mPlayer : *gameInstance->mPlayers){
        args[0].l = mPlayer->jPlayer;
        env->CallVoidMethodA(consumerObj, methodID, args);
    }
    delete[] args;
}

jint playersSize(JNIEnv *env, jobject thiz){
    return (jint)gameInstance->mPlayers->size();
}

jobject playersGet(JNIEnv *env, jobject thiz, jint index){
     return gameInstance->mPlayers->at(index)->jPlayer;
}

void playersAdd(JNIEnv *env, jobject thiz, jobject player){
    auto playerClazz = env->FindClass("com/example/marshmallow/player/Player");
    auto ptrId = env->GetFieldID(playerClazz, "nPtr", "J");
    auto ptrVal = env->GetLongField(player, ptrId);

    auto playerPtr = (Player*)ptrVal;
    playerPtr->jPlayer = env->NewGlobalRef(player);
    gameInstance->mPlayers->push_back(playerPtr);
}

jboolean playersRemove(JNIEnv *env, jobject thiz, jobject player){
    vector<Player*>::iterator iterator;
    for(iterator = gameInstance->mPlayers->begin() ; iterator != gameInstance->mPlayers->end() ;){
        if(env->IsSameObject((*iterator)->jPlayer, player)){
            env->DeleteGlobalRef((*iterator)->jPlayer);
            gameInstance->mPlayers->erase(iterator);
            return true;
        }
        iterator++;
    }
    return false;
}

// ===============================================================================================

jint obstaclesSize(JNIEnv *env, jobject thiz){
    return (jint)gameInstance->mObstaclesInPlay->size();
}
void obstaclesForEach(JNIEnv *env, jobject thiz, jobject consumerObj){
    auto jclass = env->FindClass("androidx/core/util/Consumer");
    auto methodID = env->GetMethodID(jclass, "accept", "(Ljava/lang/Object;)V");

    LOGV("obstaclesForEach. %d", (int)obstaclesSize(env, thiz));

    auto *args = new jvalue[1];
    for(auto &obstacle : *gameInstance->mObstaclesInPlay){
        args[0].l = obstacle->jObstacle;
        env->CallVoidMethodA(consumerObj, methodID, args);
    }
    delete[] args;
}
jobject obstaclesGet(JNIEnv *env, jobject thiz, jint index){
    return gameInstance->mObstaclesInPlay->at(index)->jObstacle;
}
void obstaclesRemove(JNIEnv *env, jobject thiz, jobject obstacle){
    vector<Obstacle*>::iterator iterator;
    for(iterator = gameInstance->mObstaclesInPlay->begin() ; iterator != gameInstance->mObstaclesInPlay->end() ;){
        if(env->IsSameObject((*iterator)->jObstacle, obstacle)){
            LOGV("obstaclesRemove");
            env->DeleteGlobalRef((*iterator)->jObstacle);
            gameInstance->mObstaclesInPlay->erase(iterator);
            return;
        }
        iterator++;
    }
}
void obstaclesClear(JNIEnv *env, jobject thiz){
    vector<Obstacle*>::iterator iterator;
    for(iterator = gameInstance->mObstaclesInPlay->begin() ; iterator != gameInstance->mObstaclesInPlay->end() ;){
        env->DeleteGlobalRef((*iterator)->jObstacle);
        gameInstance->mObstaclesInPlay->erase(iterator);
    }
}
void obstaclesAdd(JNIEnv *env, jobject thiz, jobject obstacle){
    auto obstacleClazz = env->FindClass("com/example/marshmallow/obstacle/Obstacle");
    auto ptrId = env->GetFieldID(obstacleClazz, "nPtr", "J");
    auto ptrVal = env->GetLongField(obstacle, ptrId);

    auto obstaclePtr = (Obstacle*)ptrVal;
    obstaclePtr->jObstacle = env->NewGlobalRef(obstacle);
    gameInstance->mObstaclesInPlay->push_back(obstaclePtr);

    LOGV("obstaclesAdd");

}



// ===============================================================================================

static JNINativeMethod nativeMethodsMLand[] = {
        {"playersForEach", "(Landroidx/core/util/Consumer;)V", (void *) playersForEach},
        {"playersSize", "()I", (void *) playersSize},
        {"playersGet", "(I)Lcom/example/marshmallow/player/Player;", (void *) playersGet},
        {"playersAdd", "(Lcom/example/marshmallow/player/Player;)V", (void *) playersAdd},
        {"playersRemove", "(Lcom/example/marshmallow/player/Player;)Z", (void *) playersRemove},

        {"obstaclesForEach", "(Landroidx/core/util/Consumer;)V", (void *) obstaclesForEach},
        {"obstaclesSize", "()I", (void *) obstaclesSize},
        {"obstaclesGet", "(I)Lcom/example/marshmallow/obstacle/Obstacle;", (void *) obstaclesGet},
        {"obstaclesRemove", "(Lcom/example/marshmallow/obstacle/Obstacle;)V", (void *) obstaclesRemove},
        {"obstaclesClear", "()V", (void *) obstaclesClear},
        {"obstaclesAdd", "(Lcom/example/marshmallow/obstacle/Obstacle;)V", (void *) obstaclesAdd},

};


static JNINativeMethod nativeMethodsPlayer[] = {
        {"initPlayer", "()J", (void *) Player::initPlayer},
        {"releasePlayer", "(J)V", (void *) Player::releasePlayer},

        {"getDv", "()F", (void *) Player::getDv},
        {"getColor", "()I", (void *) Player::getColor},
        {"isBoosting", "()Z", (void *) Player::isBoosting},
        {"getTouchX", "()F", (void *) Player::getTouchX},
        {"getTouchY", "()F", (void *) Player::getTouchY},
        {"isAlive", "()Z", (void *) Player::isAlive},
        {"getScore", "()I", (void *) Player::getScore},
        {"getTranslateX", "()F", (void *) View::getTranslateX},
        {"getTranslateY", "()F", (void *) View::getTranslateY},

        {"setDv", "(F)V", (void *) Player::setDv},
        {"setColor", "(I)V", (void *) Player::setColor},
        {"setBoosting", "(Z)V", (void *) Player::setBoosting},
        {"setTouchX", "(F)V", (void *) Player::setTouchX},
        {"setTouchY", "(F)V", (void *) Player::setTouchY},
        {"setAlive", "(Z)V", (void *) Player::setAlive},
        {"nativeSetScore", "(I)V", (void *) Player::setScore},
        {"nativeSetTranslateX", "(F)V", (void *) View::setTranslateX},
        {"nativeSetTranslateY", "(F)V", (void *) View::setTranslateY},
        {"saveY", "(F)V", (void *) Player::saveY},

};

static JNINativeMethod nativeMethodsObstacle[] = {
        {"initNative", "(F)J", (void *) Obstacle::initObstacleNative},
        {"releaseNative", "(J)V", (void *) Obstacle::releaseObstacleNative},
        {"setNativeHitRect", "(IIII)V", (void *) Obstacle::saveHitRect},
        {"getH", "()F", (void *) Obstacle::getObstacleH},

        {"getTranslateX", "()F", (void *) View::getTranslateX},
        {"getTranslateY", "()F", (void *) View::getTranslateY},
        {"nativeSetTranslateX", "(F)V", (void *) View::setTranslateX},
        {"nativeSetTranslateY", "(F)V", (void *) View::setTranslateY},


};

jint JNI_OnLoad(JavaVM* vm, void* reserved){
    JNIEnv* env = nullptr;
    if(vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK){
        LOGE("Error: GetEnv failed in JNI_OnLoad");
        return -1;
    }

    gameInstance = new Game();

    auto clazzMLand = env->FindClass("com/example/marshmallow/MLand");
    if(clazzMLand == nullptr){
        LOGE("Native registration unable to find class '%s'", "com/example/marshmallow/MLand");
        return -1;
    }
    env->RegisterNatives(clazzMLand, nativeMethodsMLand, getArrayLen(nativeMethodsMLand));


    auto clazzPlayer = env->FindClass("com/example/marshmallow/player/BasePlayer");
    if(clazzPlayer == nullptr){
        LOGE("Native registration unable to find class '%s'", "com/example/marshmallow/BasePlayer");
        return -1;
    }
    env->RegisterNatives(clazzPlayer, nativeMethodsPlayer, getArrayLen(nativeMethodsPlayer));


    auto clazzObstacle = env->FindClass("com/example/marshmallow/obstacle/BaseObstacle");
    if(clazzObstacle == nullptr){
        LOGE("Native registration unable to find class '%s'", "com/example/marshmallow/obstacle/BaseObstacle");
        return -1;
    }
    env->RegisterNatives(clazzObstacle, nativeMethodsObstacle, getArrayLen(nativeMethodsObstacle));

    return JNI_VERSION_1_6;
}


