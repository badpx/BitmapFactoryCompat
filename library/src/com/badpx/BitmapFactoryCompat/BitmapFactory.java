package com.badpx.BitmapFactoryCompat;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;

import java.io.FileDescriptor;
import java.io.InputStream;

/**
 * Created by kanedong on 15-5-18.
 */
public class BitmapFactory {
    private static boolean compatMode = (Build.VERSION.SDK_INT >= 11 && Build.VERSION.SDK_INT < 19);

    public static Bitmap decodeFile(String pathName, android.graphics.BitmapFactory.Options opts) {
        if (compatMode) {
            if (null != opts && null != opts.inBitmap) {
                android.graphics.BitmapFactory.Options tmpOpts =
                        new android.graphics.BitmapFactory.Options();
                tmpOpts.inJustDecodeBounds = true;
                android.graphics.BitmapFactory.decodeFile(pathName, tmpOpts);
                changeBitmapSizeForReuse(opts.inBitmap, tmpOpts);
            }
        }
        return android.graphics.BitmapFactory.decodeFile(pathName, opts);
    }

    public static Bitmap decodeFile(String pathName) {
        return android.graphics.BitmapFactory.decodeFile(pathName);
    }

    public static Bitmap decodeResourceStream(Resources res, TypedValue value,
                                              InputStream is, Rect pad,
                                              android.graphics.BitmapFactory.Options opts) {
        if (compatMode) {
            if (null != opts && null != opts.inBitmap) {
                android.graphics.BitmapFactory.Options tmpOpts =
                        new android.graphics.BitmapFactory.Options();
                tmpOpts.inJustDecodeBounds = true;
                android.graphics.BitmapFactory.decodeResourceStream(res, value, is, pad, tmpOpts);
                changeBitmapSizeForReuse(opts.inBitmap, tmpOpts);
            }
        }
        return android.graphics.BitmapFactory.decodeResourceStream(res, value, is, pad, opts);
    }

    public static Bitmap decodeResource(Resources res, int id) {
        return android.graphics.BitmapFactory.decodeResource(res, id);
    }

    public static Bitmap decodeByteArray(byte[] data, int offset, int length, android.graphics.BitmapFactory.Options opts) {
        if (compatMode) {
            if (null != opts && null != opts.inBitmap) {
                android.graphics.BitmapFactory.Options tmpOpts =
                        new android.graphics.BitmapFactory.Options();
                tmpOpts.inJustDecodeBounds = true;
                android.graphics.BitmapFactory.decodeByteArray(data, offset, length, tmpOpts);
                changeBitmapSizeForReuse(opts.inBitmap, tmpOpts);
            }
        }
        return android.graphics.BitmapFactory.decodeByteArray(data, offset, length, opts);
    }

    public static Bitmap decodeByteArray(byte[] data, int offset, int length) {
        return android.graphics.BitmapFactory.decodeByteArray(data, offset, length);
    }

    public static Bitmap decodeStream(InputStream is, Rect outPadding, android.graphics.BitmapFactory.Options opts) {
        if (compatMode) {
            if (null != opts && null != opts.inBitmap) {
                android.graphics.BitmapFactory.Options tmpOpts =
                        new android.graphics.BitmapFactory.Options();
                tmpOpts.inJustDecodeBounds = true;
                android.graphics.BitmapFactory.decodeStream(is, outPadding, tmpOpts);
                changeBitmapSizeForReuse(opts.inBitmap, tmpOpts);
            }
        }
        return android.graphics.BitmapFactory.decodeStream(is, outPadding, opts);
    }

    public static Bitmap decodeStream(InputStream is) {
        return android.graphics.BitmapFactory.decodeStream(is);
    }

    public static Bitmap decodeFileDescriptor(FileDescriptor fd, Rect outPadding, android.graphics.BitmapFactory.Options opts) {
        if (compatMode) {
            if (null != opts && null != opts.inBitmap) {
                android.graphics.BitmapFactory.Options tmpOpts =
                        new android.graphics.BitmapFactory.Options();
                tmpOpts.inJustDecodeBounds = true;
                android.graphics.BitmapFactory.decodeFileDescriptor(fd, outPadding, tmpOpts);
                changeBitmapSizeForReuse(opts.inBitmap, tmpOpts);
            }
        }
        return android.graphics.BitmapFactory.decodeFileDescriptor(fd, outPadding, opts);
    }

    public static Bitmap decodeFileDescriptor(FileDescriptor fd) {
        return android.graphics.BitmapFactory.decodeFileDescriptor(fd);
    }

    public static Bitmap decodeResource(Resources res, int id, android.graphics.BitmapFactory.Options opts) {
        if (compatMode) {
            if (null != opts && null != opts.inBitmap) {
                android.graphics.BitmapFactory.Options tmpOpts =
                        new android.graphics.BitmapFactory.Options();
                tmpOpts.inJustDecodeBounds = true;
                android.graphics.BitmapFactory.decodeResource(res, id, tmpOpts);
                changeBitmapSizeForReuse(opts.inBitmap, tmpOpts);
            }
        }

        return android.graphics.BitmapFactory.decodeResource(res, id, opts);
    }

    private static void changeBitmapSizeForReuse(Bitmap inBitmap, android.graphics.BitmapFactory.Options tmpOpts) {
        if (!(tmpOpts.outWidth == inBitmap.getWidth() &&
                tmpOpts.outHeight == inBitmap.getHeight())) {
            Log.d("BitmapFactory", "inBitmap's dimension not matched, need change size...");
            BitmapHelper.changeBitmapSize(inBitmap, tmpOpts.outWidth, tmpOpts.outHeight);
        }
    }

}
