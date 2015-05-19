#include <cstdio>
#include <jni.h>
#include <android/log.h>
#include "SkBitmapHelper.h"

#define  LOG_TAG    "NativeBitmapHelper"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)

#define TRAVERSAL_TIMES     (16)
#define RECONFIGURE_SUCCESS (0)
#define INIT_FAILED         (-1)
#define ILLEGAL_ARGS        (-2)
#define IMMUTABLE_BMP       (-3)
#define TRAVERSAL_FAILED    (-4)
 
bool traversalAndReconfigure(int bitmap, int rowBytes, int origWidth, int origHeight, int dstWidth, int dstHeight);

int gInitFlag;
jfieldID gBitmap_nativeBitmapFieldID;
jfieldID gBitmap_widthFieldID;
jfieldID gBitmap_heightFieldID;
jmethodID  gBitmap_getWidthMethodID;
jmethodID  gBitmap_getHeightMethodID;
jmethodID  gBitmap_getRowBytesMethodID;
jmethodID  gBitmap_isMutableMethodID;

JNIEXPORT jint JNICALL Java_com_badpx_BitmapFactoryCompat_BitmapHelper_nativeReconfigure(
        JNIEnv* env, jobject, jobject javaBitmap, jint width, jint height) {
    if (NULL == javaBitmap || width <= 0 || height <= 0) {
        LOGD("Illegal Arguments(Bitmap=%p, width=%d, height = %d) in nativeChangeBitmapSize!",
                javaBitmap, width, height);
        return false;
    }

    if (0 == gInitFlag) {
        LOGD("NativeBitmapHelper initialize started");
        gInitFlag = -1;

        jclass bitmap_class = env->FindClass("android/graphics/Bitmap");
        if (NULL == bitmap_class) return INIT_FAILED;
        gBitmap_nativeBitmapFieldID = env->GetFieldID(bitmap_class, "mNativeBitmap", "I");
        if (NULL == gBitmap_nativeBitmapFieldID) return INIT_FAILED;
        gBitmap_widthFieldID = env->GetFieldID(bitmap_class, "mWidth", "I");
        if (NULL == gBitmap_widthFieldID) return INIT_FAILED;
        gBitmap_heightFieldID = env->GetFieldID(bitmap_class, "mHeight", "I");
        if (NULL == gBitmap_heightFieldID) return INIT_FAILED;
        gBitmap_getWidthMethodID = env->GetMethodID(bitmap_class, "getWidth", "()I");
        if (NULL == gBitmap_getWidthMethodID) return INIT_FAILED;
        gBitmap_getHeightMethodID = env->GetMethodID(bitmap_class, "getHeight", "()I");
        if (NULL == gBitmap_getHeightMethodID) return INIT_FAILED;
        gBitmap_getRowBytesMethodID = env->GetMethodID(bitmap_class, "getRowBytes", "()I");
        if (NULL == gBitmap_getRowBytesMethodID) return INIT_FAILED;
        gBitmap_isMutableMethodID = env->GetMethodID(bitmap_class, "isMutable", "()Z");
        if (NULL == gBitmap_isMutableMethodID) return INIT_FAILED;

        gInitFlag = 1;  // Initialize success
        LOGD("NativeBitmapHelper initialize finished");
    } else if (-1 == gInitFlag) {
        // Initialize failed, may be can't take some java fields or methods by reflection.
        LOGD("NativeBitmapHelper initialize failed!");
        return INIT_FAILED;
    }

    if (!env->CallBooleanMethod(javaBitmap, gBitmap_isMutableMethodID)) {
        LOGD("Immutable bitmap can't be reused!");
        return IMMUTABLE_BMP;
    }

    int bitmap = env->GetIntField(javaBitmap, gBitmap_nativeBitmapFieldID);
    int rowBytes = env->CallIntMethod(javaBitmap, gBitmap_getRowBytesMethodID);
    int origWidth = env->CallIntMethod(javaBitmap, gBitmap_getWidthMethodID);
    int origHeight = env->CallIntMethod(javaBitmap, gBitmap_getHeightMethodID);

    LOGD("Attempt reconfigure Bitmap@%d(with rowBytes %d) from (%dx%d) to (%dx%d)", 
            bitmap, rowBytes, origWidth, origHeight, width, height);

    env->SetIntField(javaBitmap, gBitmap_widthFieldID, width);
    env->SetIntField(javaBitmap, gBitmap_heightFieldID, height);

    if (traversalAndReconfigure(bitmap, rowBytes, origWidth, origHeight, width, height)) {
        return RECONFIGURE_SUCCESS;
    } else {
        // Restore Java Bitmap object size when native reconfigure was failed.
        env->SetIntField(javaBitmap, gBitmap_widthFieldID, origWidth);
        env->SetIntField(javaBitmap, gBitmap_heightFieldID, origHeight);
    }

    return TRAVERSAL_FAILED;
}

bool traversalAndReconfigure(int bitmap, int rowBytes, int origWidth, int origHeight, int dstWidth, int dstHeight) {
    uint32_t* ptr = (uint32_t*)bitmap;
    if (NULL != ptr) {
        for (int i = 0; i < TRAVERSAL_TIMES; ++i) {
            // Assuming the rowBytes/width/height of SkBitmap are continuous in memory model
            if (ptr[i] == rowBytes && ptr[i + 1] == origWidth && ptr[i + 2] == origHeight) {
                int bpp = rowBytes / origWidth; // Calc bytes per pixel.
                ptr[i] = bpp * dstWidth;
                ptr[i + 1] = dstWidth;
                ptr[i + 2] = dstHeight;
                LOGD("Native reconfigure success(%d)", i);
                return true;
            }
        }
    }

    LOGD("Native reconfigure failed!");
    return false;
}
