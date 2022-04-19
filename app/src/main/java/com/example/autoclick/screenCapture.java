package com.example.autoclick;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PersistableBundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class screenCapture  extends AppCompatActivity {

    private static final String TAG = "ScreenTestCapture";
    private static final int REQUEST_CODE = 100;
    private static String STORE_DIRECTORY;
    private static int IMAGES_PRODUCED;
    private static final String SCREENCAP_NAME = "screencap";
    private static final int VIRTUAL_DISPLAY_FLAGS = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
    private static MediaProjection sMediaProjection;

    private MediaProjectionManager mProjectionManager;
    private ImageReader mImageReader;
    private Handler mHandler;
    private Display mDisplay;
    private VirtualDisplay mVirtualDisplay;
    private int mDensity;
    private int mWidth;
    private int mHeight;
    private int mRotation;
    private OrientationChangeCallback mOrientationChangeCallback;
    private String action;
//    private WindowManager mWindowManager;
//    private static screenCapture mInstance;
//    private Context mContext;


//    public static screenCapture getInstance() {
//        return mInstance;
//    }
//
//
////    @Override
//    protected void screenCapture() {
////        mContext = context;
//        mProjectionManager = (MediaProjectionManager)mContext.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
////        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
//        mInstance = this;
//    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.capture_view);
        mProjectionManager = (MediaProjectionManager)getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        //MediaProjectionManager는 getSystemService를 통해 service를 생성하고, 사용자에게 권한을 요구하게 됨.
        //시스템의 Projection Service를 획득.
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        action = bundle.getString("action");

        IMAGES_PRODUCED = 0; //캡쳐를 한장만 하기위해
        startProjection();
    }


    public void startProjection() {
//사용자 허가 요청!
        startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
    }

    public void stopProjection() {
        if (sMediaProjection != null) {
            sMediaProjection.stop();
            finish();
        }
    }

//    public WindowManager getWindowManager() {
//        return mWindowManager;
//    }
//
//    public void setWindowManager(WindowManager windowManager) {
//        this.mWindowManager = windowManager;
//    }

    private class ImageAvailableListener implements ImageReader.OnImageAvailableListener {
        @Override
        public void onImageAvailable(ImageReader imageReader) {
            Image image = null;
            FileOutputStream fos = null;
            Bitmap bitmap = null;

            try {
                image = mImageReader.acquireLatestImage();
                if (image != null) {


                    if( IMAGES_PRODUCED == 0 ) {
                        Image.Plane[] planes = image.getPlanes();
                        ByteBuffer buffer = planes[0].getBuffer();
                        int pixelStride = planes[0].getPixelStride();
                        int rowStride = planes[0].getRowStride();
                        int rowPadding = rowStride - pixelStride * mWidth;

                        //쨋든 createBitmap으로 bitmap파일 만들고 위의 이미지 buffer로 이미지를 가져옵니다.
                        bitmap = Bitmap.createBitmap(mWidth + rowPadding / pixelStride, mHeight, Bitmap.Config.ARGB_8888);
                        bitmap.copyPixelsFromBuffer(buffer);

                        //그 다음 저장하는 부분
//                        fos = new FileOutputStream(STORE_DIRECTORY + "/" + action);
                        fos = new FileOutputStream(action);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos); //압축

                        Log.d(TAG, "ImageAvailableListener   =======> in " + IMAGES_PRODUCED);
                        IMAGES_PRODUCED++;
                    }
                    else if( IMAGES_PRODUCED == 1 ) {

                        //한장만 캡쳐를 하기위함
                        stopProjection();
                        IMAGES_PRODUCED++;
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }

                if (bitmap != null) {
                    bitmap.recycle();
                }

                if (image != null) {
                    image.close();
                }
            }
        }
    }

    private class OrientationChangeCallback extends OrientationEventListener {
        //생성자가 필히 요구 됩니다.
        public OrientationChangeCallback(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            synchronized (this) {
                final int rotation = mDisplay.getRotation();

                if (rotation != mRotation) {
                    mRotation = rotation;
                    try {

                        if (mVirtualDisplay != null) mVirtualDisplay.release();
                        if (mImageReader != null) mImageReader.setOnImageAvailableListener(null, null);

                        createVirtualDisplay();

                        Log.d(TAG, "onOrientationChanged   =======> in");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private class MediaProjectionStopCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
            if (mVirtualDisplay != null) mVirtualDisplay.release();
            if (mImageReader != null) mImageReader.setOnImageAvailableListener(null, null);
            if (mOrientationChangeCallback != null) mOrientationChangeCallback.disable();
            sMediaProjection.unregisterCallback(MediaProjectionStopCallback.this);
//                }
//            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
/*
권한 요청하고 REQUEST_CODE는 임의 코드 입니다.
*/
        Log.d(TAG, "onActivityResult   =======>");
        if (requestCode == REQUEST_CODE) {
            sMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);

//            if (sMediaProjection != null) {
//                STORE_DIRECTORY = Environment.getExternalStorageDirectory() + "/capturetest/";
//                File storeDirectory = new File(STORE_DIRECTORY);
//                if (!storeDirectory.exists()) {
//                    boolean success = storeDirectory.mkdirs();
//                    if (!success) {
//                        Log.d(TAG, "onActivityResult   =======>  !success" );
//                        return;
//                    }
//                }
//            }

/*
현재 디스플레이의 density dpi 가져 옵니다.
*/
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            mDensity = metrics.densityDpi;
            mDisplay = getWindowManager().getDefaultDisplay();

/*
그리고 나서 createVirtualDisplay() 호출해서 virtualdisplay를 만듭니다.
*/

            createVirtualDisplay();


/*
여건 orientation callback 등록 부분.
감지 할 수 있ㅇ면, enable()로 이제 감지하게끔 해주는가 봅니다.
*/

            mOrientationChangeCallback = new OrientationChangeCallback(this);
            if (mOrientationChangeCallback.canDetectOrientation()) {
                mOrientationChangeCallback.enable();
            }
            Log.d(TAG, "mOrientationChangeCallback   =======>  end" );

//getMdiaProjection으로 가져온 object에 이제register 등록을 해줍니다. 핸들러와 함께
//흠... mHandler를 여기서등록 시켜주면, 흠..null값을 줘도 된다고 developer에 나와있습니다.
//looper를 호출 할 필요가 있다면 핸들러를 넣으려는데 아직도 handler를 쓴 이유를 잘 모르겠네요.
            Log.d(TAG, "sMediaProjection   =======>  start" );
            sMediaProjection.registerCallback(new MediaProjectionStopCallback(), null);
            Log.d(TAG, "sMediaProjection   =======>  end" );
        }

        super.onActivityResult(requestCode, resultCode, data);

    }

    //가상 디스플레이를 만듭니다.
    private void createVirtualDisplay() {

        Log.d(TAG, "createVirtualDisplay   =======>  in" );
//가로,세로 고려 사이즈는 다시 설정하고
        Point size = new Point();
        mDisplay.getSize(size);
        mWidth = size.x;
        mHeight = size.y;

//ImageReader 새로운 사이즈의 인스턴스 만들고, createVirtualDisplay로 생ㅇ성 합니다.
//하고 이미지 처리할 ImageAvailableListener를 등록 해줍니다.
        mImageReader = ImageReader.newInstance(mWidth, mHeight, 1, 2);
        mVirtualDisplay = sMediaProjection.createVirtualDisplay(SCREENCAP_NAME, mWidth, mHeight, mDensity, VIRTUAL_DISPLAY_FLAGS, mImageReader.getSurface(), null, null);
        mImageReader.setOnImageAvailableListener(new ImageAvailableListener(), null);
    }
}
