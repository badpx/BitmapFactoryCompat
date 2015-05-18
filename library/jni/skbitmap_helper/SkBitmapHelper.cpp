#include <cstdio>
#include <jni.h>
#include <android/log.h>
#include "SkBitmapHelper.h"

#define  LOG_TAG    "NativeBitmapHelper"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
 
bool findAndSetSize(int bitmap, int rowBytes, int origWidth, int origHeight, int dstWidth, int dstHeight);

jfieldID gBitmap_nativeBitmapFieldID;
jfieldID gBitmap_widthFieldID;
jfieldID gBitmap_heightFieldID;
jmethodID  gBitmap_getWidthMethodID;
jmethodID  gBitmap_getHeightMethodID;
jmethodID  gBitmap_getRowBytesMethodID;
jmethodID  gBitmap_isMutableMethodID;

JNIEXPORT jboolean JNICALL Java_com_badpx_BitmapFactoryCompat_BitmapHelper_nativeChangeBitmapSize(
        JNIEnv* env, jobject, jobject javaBitmap, jint width, jint height) {
    if (NULL == javaBitmap || width <= 0 || height <= 0) {
        LOGD("Illegal Arguments(Bitmap=%p, width=%d, height = %d) in nativeChangeBitmapSize!",
                javaBitmap, width, height);
        return false;
    }

    if (NULL == gBitmap_nativeBitmapFieldID) {
        jclass bitmap_class = env->FindClass("android/graphics/Bitmap");

        gBitmap_nativeBitmapFieldID = env->GetFieldID(bitmap_class, "mNativeBitmap", "I");
        gBitmap_widthFieldID = env->GetFieldID(bitmap_class, "mWidth", "I");
        gBitmap_heightFieldID = env->GetFieldID(bitmap_class, "mHeight", "I");
        gBitmap_getWidthMethodID = env->GetMethodID(bitmap_class, "getWidth", "()I");
        gBitmap_getHeightMethodID = env->GetMethodID(bitmap_class, "getHeight", "()I");
        gBitmap_getRowBytesMethodID = env->GetMethodID(bitmap_class, "getRowBytes", "()I");
        gBitmap_isMutableMethodID = env->GetMethodID(bitmap_class, "isMutable", "()Z");
    }

    if (NULL == gBitmap_isMutableMethodID || !env->CallBooleanMethod(javaBitmap, gBitmap_isMutableMethodID)) {
        LOGD("Immutable bitmap can't be reused!");
        return false;
    }

    if (NULL == gBitmap_nativeBitmapFieldID) {
        return false;
    }

    int bitmap = env->GetIntField(javaBitmap, gBitmap_nativeBitmapFieldID);

    if (NULL != gBitmap_widthFieldID && 
            NULL != gBitmap_heightFieldID &&
            NULL != gBitmap_getWidthMethodID &&
            NULL != gBitmap_getHeightMethodID &&
            NULL != gBitmap_getRowBytesMethodID) {
        int rowBytes = env->CallIntMethod(javaBitmap, gBitmap_getRowBytesMethodID);
        int origWidth = env->CallIntMethod(javaBitmap, gBitmap_getWidthMethodID);
        int origHeight = env->CallIntMethod(javaBitmap, gBitmap_getHeightMethodID);

        LOGD("Attempt change size of Bitmap@%d(with rowBytes %d): (%d, %d) to (%d, %d)", 
                bitmap, rowBytes, origWidth, origHeight, width, height);

        env->SetIntField(javaBitmap, gBitmap_widthFieldID, width);
        env->SetIntField(javaBitmap, gBitmap_heightFieldID, height);

        if (findAndSetSize(bitmap, rowBytes, origWidth, origHeight, width, height)) {
            return true;
        } else {
            // Restore Java Bitmap object size when native size was changed failed.
            env->SetIntField(javaBitmap, gBitmap_widthFieldID, origWidth);
            env->SetIntField(javaBitmap, gBitmap_heightFieldID, origHeight);
        }
    }
    return false;
}

bool findAndSetSize(int bitmap, int rowBytes, int origWidth, int origHeight, int dstWidth, int dstHeight) {
    uint32_t* ptr = (uint32_t*)bitmap;
    for (int i = 0; i < 32; ++i) {
        if (ptr[i] == rowBytes) {
            if (ptr[i + 1] == origWidth && ptr[i + 2] == origHeight) {
                int bpp = rowBytes / origWidth; // Calc bytes per pixel.
                ptr[i] = bpp * dstWidth;
                ptr[i + 1] = dstWidth;
                ptr[i + 2] = dstHeight;
                LOGD("Change size success!");
                return true;
            }
        }
    }

    LOGD("Change size failed!");
    return false;
}
