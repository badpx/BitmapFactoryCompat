
#ifndef __SKBITMAP_HELPER_H__
#define __SKBITMAP_HELPER_H__

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jint JNICALL Java_com_badpx_BitmapFactoryCompat_BitmapHelper_nativeReconfigure(
        JNIEnv* env, jobject, jobject javaBitmap, jint width, jint height);

#ifdef __cplusplus
}
#endif

#endif
