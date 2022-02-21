/*
 * Copyright (C) 2018, 2019 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying 
 * and/or commercial use of this code in source and/or binary form,
 * without author's written permission, are strongly prohibited. 
 */

/*************************************
 * Created on 07.01.2019. *
 *************************************/

#include <jni.h>
#include <string>
#include <stdint.h>
#include <android/log.h>
#include "RootOperations.h"

std::string jstring2stdString(JNIEnv *env, jstring s);

extern "C"
JNIEXPORT jstring
JNICALL Java__MFile_ls(JNIEnv *env, jobject instance, jstring path)
{
    return env->NewStringUTF(RootOperations::ls(jstring2stdString(env, path)).c_str());
}

extern "C"
JNIEXPORT jboolean
JNICALL Java__MFile_isDir(JNIEnv *env, jobject instance, jstring file)
{
    return (jboolean) RootOperations::isDirectory(jstring2stdString(env, file));
}

extern "C"
JNIEXPORT jboolean
JNICALL Java__MFile_fileExists(JNIEnv *env, jobject instance, jstring path)
{
    return (jboolean) RootOperations::fileExists(jstring2stdString(env, path));
}

extern "C"
JNIEXPORT jbyteArray
JNICALL Java__TextEditor_rootRead(JNIEnv *env, jobject instance, jstring file)
{
//    RootOperations::byte_arr out = RootOperations::readFile(jstring2stdString(env, file));
//
//    jbyteArray arr = env->NewByteArray(RootOperations::byte_arrSize);
//    env->SetByteArrayRegion(arr, 0, RootOperations::byte_arrSize, out);
//
//    delete[] out;

//    out.deleteVal(*[](void *v1, void *v2) -> void
//                  {
//                      delete[] (RootOperations::byte_arr) v1;
//                  });

//    try
//    {
        std::string a = std::string(RootOperations::readFile(jstring2stdString(env, file)));
        jbyteArray arr = env->NewByteArray((int) a.length());
        env->SetByteArrayRegion(arr, 0, (int) a.length(), reinterpret_cast<const jbyte *>(a.c_str()));
//    }
//    catch (...)
//    {
//        return nullptr;
//    }

    return arr;
}

extern "C"
JNIEXPORT void
JNICALL Java__TextEditor_rootWrite(JNIEnv *env,
                                                                jobject instance,
                                                                jstring file,
                                                                jstring text)
{
    RootOperations::writeFile(jstring2stdString(env, file), jstring2stdString(env, text));
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

namespace ChmodTranslate
{

    typedef struct _JNI_CHBTRANSLATED
    {
        jclass clazz;
        jmethodID constructorID;
        jfieldID ownerID;
        jfieldID groupID;
        jfieldID permissionsID;
        jfieldID fileID;
    } JNI_CHBTRANSLATED;

    JNI_CHBTRANSLATED *jni_chbtranslated = nullptr;

    void loadIDs(JNIEnv *env);
    void fillJObject(JNIEnv *env, jobject chb, RootOperations::chb _chb);
    RootOperations::chb fillStruct(JNIEnv *env, jobject _chb);

    void loadIDs(JNIEnv *env)
    {
        if (jni_chbtranslated != nullptr)
            jni_chbtranslated = nullptr;
        jni_chbtranslated = new JNI_CHBTRANSLATED;
        jni_chbtranslated->clazz = env->FindClass("/ChBTranslated");
        jni_chbtranslated->constructorID = env->GetMethodID(jni_chbtranslated->clazz, "<init>",
                "(Ljava/lang/String;Ljava/lang/String;[ILjava/lang/String;)V");
        jni_chbtranslated->ownerID = env->GetFieldID(jni_chbtranslated->clazz, "owner", "Ljava/lang/String;");
        jni_chbtranslated->groupID = env->GetFieldID(jni_chbtranslated->clazz, "group", "Ljava/lang/String;");
        jni_chbtranslated->permissionsID = env->GetFieldID(jni_chbtranslated->clazz, "permissions", "[I");
        jni_chbtranslated->fileID = env->GetFieldID(jni_chbtranslated->clazz, "file", "Ljava/lang/String;");
    }

    void fillJObject(JNIEnv *env, jobject chb, RootOperations::chb _chb)
    {
//        __android_log_print(
//                ANDROID_LOG_DEBUG,
//                "testo",
//                "native fiillObj %s %s %s %s %s",
//                _chb.owner.c_str(),
//                _chb.group.c_str(),
//                _chb.permissions,
//                _chb.file.c_str(),
//                (chb == nullptr) ? "true" : "false");

        env->SetObjectField(chb, jni_chbtranslated->ownerID, env->NewStringUTF(_chb.owner.c_str()));
        env->SetObjectField(chb, jni_chbtranslated->groupID, env->NewStringUTF(_chb.group.c_str()));

        jintArray jarr = env->NewIntArray(CHB_ARR_SIZE);
        env->SetIntArrayRegion(jarr, 0, CHB_ARR_SIZE, _chb.permissions);
        env->SetObjectField(chb, jni_chbtranslated->permissionsID, jarr);

        env->SetObjectField(chb, jni_chbtranslated->fileID, env->NewStringUTF(_chb.file.c_str()));

        __android_log_print(ANDROID_LOG_DEBUG, "testo", "native fillObj return");
    }

    RootOperations::chb fillStruct(JNIEnv *env, jobject _chb)
    {
        jstring owner = static_cast<jstring>(env->GetObjectField(_chb, jni_chbtranslated->ownerID));
        jstring group = static_cast<jstring>(env->GetObjectField(_chb, jni_chbtranslated->groupID));

        jintArray permissions = static_cast<jintArray>(env->GetObjectField(_chb, jni_chbtranslated->permissionsID));
        int perms[CHB_ARR_SIZE];
        env->GetIntArrayRegion(permissions, 0, CHB_ARR_SIZE, perms);

        jstring file = static_cast<jstring>(env->GetObjectField(_chb, jni_chbtranslated->fileID));

        RootOperations::chb chb;

        chb.owner = jstring2stdString(env, owner);
        chb.group = jstring2stdString(env, group);
        //std::copy(std::begin(perms), std::end(perms), std::begin(chb.permissions));
        for (int i = 0; i < CHB_ARR_SIZE; i++)
        {
            chb.permissions[i] = perms[i];
            __android_log_print(ANDROID_LOG_DEBUG, "testo", "native fs %i", chb.permissions[i]);
        }
        chb.file = jstring2stdString(env, file);

        return chb;
    }
}

extern "C"
JNIEXPORT jboolean
JNICALL Java__Processing_chmod(JNIEnv *env,
                                                            jobject instance,
                                                            jstring file,
                                                            jobject attrs,
                                                            jboolean root)
{
    ChmodTranslate::loadIDs(env);
    return static_cast<jboolean>(RootOperations::chmod(
            jstring2stdString(env, file),
            ChmodTranslate::fillStruct(env, attrs),
            root));
}

extern "C"
JNIEXPORT jobject
JNICALL Java__Processing_getFilePermissions(JNIEnv *env,
                                                                         jobject instance,
                                                                         jstring file)
{
    //return env->NewStringUTF(RootOperations::getFilePermissions(jstring2stdString(env, file)).c_str());

    ChmodTranslate::loadIDs(env);
    jobject res = env->NewObject(
            ChmodTranslate::jni_chbtranslated->clazz,
            ChmodTranslate::jni_chbtranslated->constructorID,
            env->NewStringUTF("abc"),
            env->NewStringUTF("abc"),
            env->NewIntArray(CHB_ARR_SIZE),
            env->NewStringUTF("abc"));
    ChmodTranslate::fillJObject(env, res, RootOperations::getFilePermissions(jstring2stdString(env, file)));
    return res;
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

std::string jstring2stdString(JNIEnv *env, jstring s)
{
    if (!s)
        return "";

    const jclass stringCl = env->GetObjectClass(s);
    const jmethodID bytes = env->GetMethodID(stringCl, "getBytes", "(Ljava/lang/String;)[B");
    const jbyteArray stringBytes = (jbyteArray) env->CallObjectMethod(s, bytes, env->NewStringUTF("UTF-8"));

    size_t length = (size_t) env->GetArrayLength(stringBytes);
    jbyte *pBytes = env->GetByteArrayElements(stringBytes, nullptr);

    std::string ret = std::string((char *) pBytes, length);
    env->ReleaseByteArrayElements(stringBytes, pBytes, JNI_ABORT);

    env->DeleteLocalRef(stringBytes);
    env->DeleteLocalRef(stringCl);

    return ret;
}
