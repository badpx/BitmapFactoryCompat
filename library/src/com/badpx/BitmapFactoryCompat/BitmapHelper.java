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
    private static final int RECONFIG_SUCCESS = 0;
    private static final int RECONFIG_INIT_FAILED = -1;
    private static final int RECONFIG_ILLEGAL_ARGS = -2;
    private static final int RECONFIG_IMMUTABLE_BITMAP = -3;
    private static final int RECONFIG_TRAVERSAL_FAILED = -4;

    static {
        System.loadLibrary("skbitmap_helper");
    }

    private static volatile Field mBufferField;

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
            Object buffer = null;
            try {
                if (null == mBufferField) {
                    mBufferField = Bitmap.class.getDeclaredField("mBuffer");
                    if (!mBufferField.isAccessible()) {
                        mBufferField.setAccessible(true);
                    }
                }

                buffer = mBufferField.get(bitmap);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            if (null == buffer || !buffer.getClass().isArray()) {
                // native backed bitmaps don't support reconfiguration,
                // so alloc size is always content size
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
                    return bitmap.getByteCount();
                } else {
                    return bitmap.getRowBytes() * bitmap.getHeight();
                }
            }
            return ((byte[]) buffer).length;
        }
        return -1;
    }

    public static boolean reconfigure(Bitmap bmp, int width, int height) {
        if (null != bmp) {
            int bpp = getBytesPerPixel(bmp.getConfig());
            if (bpp * width * height <= getAllocationByteCount(bmp)) {
                return (RECONFIG_SUCCESS == nativeReconfigure(bmp, width, height));
            } else {
                Log.d(TAG, "Bitmap not large enough to support new configuration!");
            }
        }
        return false;
    }

    static int getBytesPerPixel(Bitmap.Config config) {
        if (config == Bitmap.Config.ARGB_8888) {
            return 4;
        } else if (config == Bitmap.Config.RGB_565) {
            return 2;
        } else if (config == Bitmap.Config.ARGB_4444) {
            return 2;
        } else if (config == Bitmap.Config.ALPHA_8) {
            return 1;
        }
        return 1;
    }

    public static synchronized native int nativeReconfigure(Bitmap bmp, int width, int height);
}
