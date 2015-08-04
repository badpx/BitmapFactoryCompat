#include <cstdio>
#include <jni.h>
#include <android/log.h>
#include <android/bitmap.h>
#include "SkBitmapHelper.h"

#define  LOG_TAG    "NativeBitmapHelper"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)

#define TRAVERSAL_TIMES     (16)
#define RECONFIGURE_SUCCESS (0)
#define INIT_FAILED         (-1)
#define ILLEGAL_ARGS        (-2)
#define IMMUTABLE_BMP       (-3)
#define TRAVERSAL_FAILED    (-4)
 
int traversalForFieldOffset(int bitmap, const AndroidBitmapInfo& bmpInfo);
bool reconfigure(int bitmap, const AndroidBitmapInfo& bmpInfo, int width, int height);
int computeBytesPerPixel(uint32_t config);

static int gInitFlag;
static int gFieldOffset;

jfieldID gBitmap_nativeBitmapFieldID;
jfieldID gBitmap_widthFieldID;
jfieldID gBitmap_heightFieldID;
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

    AndroidBitmapInfo bmpInfo;
    AndroidBitmap_getInfo(env, javaBitmap, &bmpInfo);
    LOGD("Attempt to reconfigure Bitmap@%d(with rowBytes %d) from (%dx%d) to (%dx%d)", 
            bitmap, bmpInfo.stride, bmpInfo.width, bmpInfo.height, width, height);

    if (reconfigure(bitmap, bmpInfo, width, height)) {
        // Update width/height fields of java object.
        env->SetIntField(javaBitmap, gBitmap_widthFieldID, width);
        env->SetIntField(javaBitmap, gBitmap_heightFieldID, height);

        return RECONFIGURE_SUCCESS;
    }

    return TRAVERSAL_FAILED;
}

JNIEXPORT jint JNICALL Java_com_badpx_BitmapFactoryCompat_BitmapHelper_nativeGetBytesPerPixel(
        JNIEnv* env, jobject, jobject javaBitmap) {
    if (NULL != javaBitmap) {
        AndroidBitmapInfo bmpInfo;
        AndroidBitmap_getInfo(env, javaBitmap, &bmpInfo);
        return computeBytesPerPixel(bmpInfo.format);
    }
    return 0;
}

bool reconfigure(int bitmap, const AndroidBitmapInfo& bmpInfo, int dstWidth, int dstHeight) {
    uint32_t* ptr = (uint32_t*)bitmap;
    if (NULL != ptr) {
        int fieldOffset = traversalForFieldOffset(bitmap, bmpInfo);

        if (fieldOffset > 0) {
            int bpp = computeBytesPerPixel(bmpInfo.format);
            LOGD("Native bitmap bpp = %d.", bpp);

            if (bpp > 0) {
                ptr[fieldOffset] = bpp * dstWidth;
                ptr[fieldOffset + 1] = dstWidth;
                ptr[fieldOffset + 2] = dstHeight;
                LOGD("Native reconfigure success!");
                return true;
            }
        }
    }

    LOGD("Native reconfigure failed!");
    return false;
}

int traversalForFieldOffset(int bitmap, const AndroidBitmapInfo& bmpInfo) {
    uint32_t* ptr = (uint32_t*)bitmap;
    if (NULL != ptr && 0 == gFieldOffset) {
        for (int i = 0; i < TRAVERSAL_TIMES; ++i) {
            // Assuming the rowBytes/width/height of SkBitmap are continuous in memory model
            if (ptr[i] == bmpInfo.stride && ptr[i + 1] == bmpInfo.width && ptr[i + 2] == bmpInfo.height) {
                gFieldOffset = i;
                LOGD("traversal for field offset success(offset=%d)", i);
                return gFieldOffset;
            }
        }
        gFieldOffset = -1;
        LOGD("traversal for field offset failed!");
    }
    return gFieldOffset;
}

int computeBytesPerPixel(uint32_t config) {
    int bpp;
    switch (config) {
        case ANDROID_BITMAP_FORMAT_NONE:
            bpp = 0;   // not applicable
            break;
        case ANDROID_BITMAP_FORMAT_A_8:
            bpp = 1;
            break;
        case ANDROID_BITMAP_FORMAT_RGB_565:
        case ANDROID_BITMAP_FORMAT_RGBA_4444:
            bpp = 2;
            break;
        case ANDROID_BITMAP_FORMAT_RGBA_8888:
            bpp = 4;
            break;
        default:
            bpp = 0;   // error
            break;
    }
    return bpp;
}

