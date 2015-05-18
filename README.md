# BitmapFactoryCompat

---

Since Andorid API Level 11, BitmapFactory providing the reuse mechanism when decode bitmap: If set BitmapFactory.Options.inBitmap, then decode methods that take the Options object will attempt to reuse this bitmap when loading content. 
As of KITKAT, any mutable bitmap can be reused by BitmapFactory to decode any other bitmaps as long as the resulting byte count of the decoded bitmap is less than or equal to the allocated byte count of the reused bitmap, but prior to KITKAT additional constraints apply: The image being decoded (whether as a resource or as a stream) must be in jpeg or png format. **Only equal sized bitmaps are supported!** For resolved this restrict, here provides a BitmapFactory compatibility library.

## Get Started

### AndroidStudio/IntelliJ IDEA
First at all, import this library directory as a module into your Android project which created by AndroidStudio or IntelliJ IDEA, make sure add dependencies to your main module.

### Gradle Projects
TODO.

### Code sample
Just like use BitmapFactory of android framework, we set BitmapFactory.Options.inBitmap to a exists bitmap and BitmapFactory.Options.inSampleSize to 1(another restrict prior to KITKAT) for reuse the inBitmap:

```
    BitmapFactory.Options opts = new BitmapFactory.Options();
    opts.inMutable = true; // The current implementation necessitates that the reused bitmap be mutable
    mReuseBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image, opts);

    // Now reuse above bitmap object to decode another picture:
    opts.inBitmap = mReuseBitmap;
    opts.inSampleSize = 1;  // Cannot reuse bitmap with sampleSize != 1
    try {
        Bitmap image = com.badpx.BitmapFactoryCompat.BitmapFactory.decodeResource(
                    getResources(), R.drawable.ic_launcher, opts);
    } catch (IllegalArgumentException e) {
        e.printStackTrace();
    }
```
