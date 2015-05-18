package com.badpx.BitmapFactoryCompat;

import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.Field;

/**
 * Created by kanedong on 15-5-18.
 */
public class BitmapHelper {
    private static final String TAG = "BitmapHelper";

    static {
        System.loadLibrary("skbitmap_helper");
    }

    /**
     * Returns the size of the allocated memory used to store this bitmap's pixels.
     *
     * <p>This can be larger than the result of Bitmap.getByteCount() if a bitmap is reused to
     * decode other bitmaps of smaller size.</p>
     *
     * <p>This value will not change over the lifetime of a Bitmap.</p>
     *
     */
    public static int getAllocationByteCount(Bitmap bitmap) {
        if (null != bitmap) {
            Field mBufferField = null;
            try {
                mBufferField = Bitmap.class.getDeclaredField("mBuffer");
                Object buffer = mBufferField.get(bitmap);
                if (null == buffer || !buffer.getClass().isArray()) {
                    // native backed bitmaps don't support reconfiguration,
                    // so alloc size is always content size
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
                        return bitmap.getByteCount();
                    } else {
                        return bitmap.getRowBytes() * bitmap.getHeight();
                    }
                }
                return ((byte[])buffer).length;
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public static boolean reconfigure(Bitmap bmp, int width, int height) {
        if (null != bmp) {
            int bpp = bmp.getRowBytes() / bmp.getWidth();
            if (bpp * width * height <= getAllocationByteCount(bmp)) {
                return nativeReconfigure(bmp, width, height);
            } else {
                Log.d(TAG, "Bitmap not large enough to support new configuration!");
            }
        }
        return false;
    }

    public static native boolean nativeReconfigure(Bitmap bmp, int width, int height);
}
