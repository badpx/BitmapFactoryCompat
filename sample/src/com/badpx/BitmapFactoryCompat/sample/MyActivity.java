package com.badpx.BitmapFactoryCompat.sample;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;

public class MyActivity extends Activity implements View.OnClickListener {

    private int mClickCount;
    private ImageView mImageView;
    private Bitmap mReuseBitmap;
    public static final String[] IMAGE_FILES = new String[]{"food.jpg", "ruby.jpg", "image.png"};
    private BitmapFactory.Options mOptions = new BitmapFactory.Options();

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mImageView = (ImageView) findViewById(R.id.imageView);
        findViewById(R.id.button).setOnClickListener(this);


        try {
            Bitmap image = mReuseBitmap = getBitmapFromAsset(this, "food.jpg");
            mImageView.setImageBitmap(image);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        String file = IMAGE_FILES[++mClickCount % 3]; // display next image file.

        try {
            Bitmap image = getBitmapFromAsset(this, file);
            Log.d("MyActivity", String.format("Reuse Bitmap %s", image == mReuseBitmap ? "Success!" : "Failed!"));
            mImageView.setImageBitmap(image);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // create Bitmap from assets image file, and reuse inBitmap as can as possible.
    public Bitmap getBitmapFromAsset(Context context, String filePath) {
        AssetManager assetManager = context.getAssets();

        InputStream is;
        Bitmap bitmap = null;
        mOptions.inBitmap = mReuseBitmap;
        mOptions.inMutable = true;
        try {
            is = assetManager.open(filePath);
            bitmap = com.badpx.BitmapFactoryCompat.BitmapFactory.decodeStream(is, null, mOptions);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }
}
