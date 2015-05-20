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

    interface DecodeWorker {
        void decode(android.graphics.BitmapFactory.Options opts);
    }

    public static Bitmap decodeFile(final String pathName,
                                    final android.graphics.BitmapFactory.Options opts) {
        if (compatMode && null != opts) {
            reconfigureBitmapForReuse(opts, new DecodeWorker() {
                @Override
                public void decode(android.graphics.BitmapFactory.Options opts) {
                    android.graphics.BitmapFactory.decodeFile(pathName, opts);
                }
            });
        }
        return android.graphics.BitmapFactory.decodeFile(pathName, opts);
    }

    public static Bitmap decodeFile(String pathName) {
        return android.graphics.BitmapFactory.decodeFile(pathName);
    }

    public static Bitmap decodeResourceStream(final Resources res, final TypedValue value,
                                              final InputStream is, final Rect pad,
                                              final android.graphics.BitmapFactory.Options opts) {
        if (compatMode && null != opts) {
            reconfigureBitmapForReuse(opts, new DecodeWorker() {
                @Override
                public void decode(android.graphics.BitmapFactory.Options opts) {
                    android.graphics.BitmapFactory.decodeResourceStream(res, value, is, pad, opts);
                }
            });
        }
        return android.graphics.BitmapFactory.decodeResourceStream(res, value, is, pad, opts);
    }

    public static Bitmap decodeResource(Resources res, int id) {
        return android.graphics.BitmapFactory.decodeResource(res, id);
    }

    public static Bitmap decodeByteArray(final byte[] data, final int offset,
                                         final int length, final android.graphics.BitmapFactory.Options opts) {
        if (compatMode && null != opts) {
            reconfigureBitmapForReuse(opts, new DecodeWorker() {
                @Override
                public void decode(android.graphics.BitmapFactory.Options opts) {
                    android.graphics.BitmapFactory.decodeByteArray(data, offset, length, opts);
                }
            });
        }
        return android.graphics.BitmapFactory.decodeByteArray(data, offset, length, opts);
    }

    public static Bitmap decodeByteArray(byte[] data, int offset, int length) {
        return android.graphics.BitmapFactory.decodeByteArray(data, offset, length);
    }

    public static Bitmap decodeStream(final InputStream is, final Rect outPadding,
                                      final android.graphics.BitmapFactory.Options opts) {
        if (compatMode && null != opts) {
            reconfigureBitmapForReuse(opts, new DecodeWorker() {
                @Override
                public void decode(android.graphics.BitmapFactory.Options opts) {
                    android.graphics.BitmapFactory.decodeStream(is, outPadding, opts);
                }
            });
        }
        return android.graphics.BitmapFactory.decodeStream(is, outPadding, opts);
    }

    public static Bitmap decodeStream(InputStream is) {
        return android.graphics.BitmapFactory.decodeStream(is);
    }

    public static Bitmap decodeFileDescriptor(final FileDescriptor fd, final Rect outPadding,
                                              final android.graphics.BitmapFactory.Options opts) {
        if (compatMode && null != opts) {
            reconfigureBitmapForReuse(opts, new DecodeWorker() {
                @Override
                public void decode(android.graphics.BitmapFactory.Options opts) {
                    android.graphics.BitmapFactory.decodeFileDescriptor(fd, outPadding, opts);
                }
            });
        }
        return android.graphics.BitmapFactory.decodeFileDescriptor(fd, outPadding, opts);
    }

    public static Bitmap decodeFileDescriptor(FileDescriptor fd) {
        return android.graphics.BitmapFactory.decodeFileDescriptor(fd);
    }

    public static Bitmap decodeResource(final Resources res, final int id,
                                        android.graphics.BitmapFactory.Options opts) {
        if (compatMode && null != opts) {
            reconfigureBitmapForReuse(opts, new DecodeWorker() {
                @Override
                public void decode(android.graphics.BitmapFactory.Options opts) {
                    android.graphics.BitmapFactory.decodeResource(res, id, opts);
                }
            });
        }

        return android.graphics.BitmapFactory.decodeResource(res, id, opts);
    }

    private static void reconfigureBitmapForReuse(android.graphics.BitmapFactory.Options opts,
                                                  DecodeWorker decodeWorker) {
        if (null != decodeWorker && null != opts.inBitmap &&
                !opts.inJustDecodeBounds && opts.inSampleSize <= 1) {
            Bitmap inBitmap = opts.inBitmap;
            opts.inBitmap = null;
            opts.inJustDecodeBounds = true;
            opts.inSampleSize = 1;  // Ensure sample size to 1 for reuse success.

            // Just decode bitmap bounds:
            decodeWorker.decode(opts);

            int width = opts.outWidth;
            int height = opts.outHeight;
            if (!(width == inBitmap.getWidth() &&
                    height == inBitmap.getHeight())) {
                Log.d("BitmapFactory", "Dimension of Options.inBitmap mismatched, need reconfigure...");
                BitmapHelper.reconfigure(inBitmap, width, height);
            }

            opts.inJustDecodeBounds = false;
            opts.inBitmap = inBitmap;
        }
    }
}
