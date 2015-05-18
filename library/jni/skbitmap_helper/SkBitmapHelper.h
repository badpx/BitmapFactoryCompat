
#ifndef __SKBITMAPHELPER_H__
#define __SKBITMAPHELPER_H__

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jboolean JNICALL Java_com_badpx_BitmapFactoryCompat_BitmapHelper_nativeChangeBitmapSize(
        JNIEnv* env, jobject, jobject javaBitmap, jint width, jint height);

#ifdef __cplusplus
}
#endif

#endif
