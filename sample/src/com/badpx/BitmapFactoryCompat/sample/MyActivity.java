package com.badpx.BitmapFactoryCompat.sample;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class MyActivity extends Activity implements View.OnClickListener {

    private int mClickCount;
    private ImageView mImageView;
    private Bitmap mReuseBitmap;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mImageView = (ImageView) findViewById(R.id.imageView);
        mImageView.setOnClickListener(this);

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inMutable = true;
        mReuseBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.map, opts);

        opts.inBitmap = mReuseBitmap;
        opts.inSampleSize = 1;  // Cannot reuse bitmap with sampleSize != 1
        try {
            Bitmap image = com.badpx.BitmapFactoryCompat.BitmapFactory.decodeResource(
                    getResources(), R.drawable.image, opts);
            mImageView.setImageBitmap(image);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inBitmap = mReuseBitmap;
        opts.inSampleSize = 1;
        try {
            Bitmap image = com.badpx.BitmapFactoryCompat.BitmapFactory.decodeResource(getResources(),
                    0 == (mClickCount % 2) ? R.drawable.ruby : R.drawable.map, opts);
            mImageView.setImageBitmap(image);
            mClickCount++;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
