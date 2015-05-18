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

    public final static int getAllocationByteCount(Bitmap bitmap) {
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

    public static boolean changeBitmapSize(Bitmap bmp, int width, int height) {
        if (null != bmp) {
            int bpp = bmp.getRowBytes() / bmp.getWidth();
            if (bpp * width * height <= getAllocationByteCount(bmp)) {
                return nativeChangeBitmapSize(bmp, width, height);
            } else {
                Log.d(TAG, "ByteCount of bitmap is smaller than expect size so can't changed!");
            }
        }
        return false;
    }

    public static native boolean nativeChangeBitmapSize(Bitmap bmp, int width, int height);
}
