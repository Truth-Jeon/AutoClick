package com.example.autoclick;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.graphics.Path;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

public class AutoServiceNew extends AccessibilityService {
    private Handler mHandler;
    private static AutoServiceNew mAutoService;
    private boolean serviceRun = false;

    public static AutoServiceNew getInstance(){
        return mAutoService;
    }

    public void StopAService(){
        serviceRun = false;
        stopSelf();

    }

    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread handlerThread = new HandlerThread("auto-handler");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
        Log.d("Service","onCreate STARTED");
    }

    @Override
    public void onDestroy() {
        Log.d("Service","onDestroy STARTED");
        super.onDestroy();

    }

    @Override
    protected void onServiceConnected() {
        Log.d("Service","onServiceConnected STARTED");
        super.onServiceConnected();

        mAutoService = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Service","SERVICE STARTED");

        if (mRunnable == null) {
            mRunnable = new AutoServiceNew.IntervalRunnable();
        }
        serviceRun = true;
        mHandler.post(mRunnable);


//        if(intent!=null){
//            String action = intent.getStringExtra("action");
//            if (action.equals("play")) {
//                mX = intent.getIntExtra("x", 0);
//                Log.d("x_value",Integer.toString(mX));
//                mY = intent.getIntExtra("y", 0);
//                Log.d("y_value",Integer.toString(mY));
//                if (mRunnable == null) {
//                    mRunnable = new IntervalRunnable();
//                }
//                //playTap(mX,mY);
//                //mHandler.postDelayed(mRunnable, 1000);
//                mHandler.post(mRunnable);
//                Toast.makeText(getBaseContext(), "Started", Toast.LENGTH_SHORT).show();
//            }
//            else if(action.equals("stop")){
//                mHandler.removeCallbacksAndMessages(null);
//                Toast.makeText(getBaseContext(), "stopped", Toast.LENGTH_SHORT).show();
//            }
//        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void tapEvent(int x, int y) {

        Path swipePath = new Path();

        swipePath.moveTo(x, y);
//        swipePath.lineTo(x+1500, y);
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, 10));
        //dispatchGesture(gestureBuilder.build(), null, null);
        Log.d("point","X : " + x + ", Y : " + y);
        boolean result = dispatchGesture(gestureBuilder.build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                Log.d("Gesture Completed","Gesture Completed");
                super.onCompleted(gestureDescription);
                //mHandler.postDelayed(mRunnable, 1);
//                mHandler.post(mRunnable);
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                Log.d("Gesture Cancelled","Gesture Cancelled");
                super.onCancelled(gestureDescription);
            }
        }, null);
        Log.d("hi","hi? ===> " + result);
    }
    public void dragEvent(int x, int y, int x1, int y1) {

        Path swipePath = new Path();

        swipePath.moveTo(x, y);
        swipePath.lineTo(x1, y1);
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, 300));
        //dispatchGesture(gestureBuilder.build(), null, null);
        Log.d("point","X : " + x + ", Y : " + y);
        boolean result = dispatchGesture(gestureBuilder.build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                Log.d("Gesture Completed","Gesture Completed");
                super.onCompleted(gestureDescription);
                //mHandler.postDelayed(mRunnable, 1);
//                mHandler.post(mRunnable);
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                Log.d("Gesture Cancelled","Gesture Cancelled");
                super.onCancelled(gestureDescription);
            }
        }, null);
        Log.d("hi","hi? ===> " + result);
    }
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

//        Log.d("onAccessibilityEvent","onAccessibilityEvent Completed");
    }

    @Override
    public void onInterrupt() {
        Log.d("Service","onInterrupt STARTED");
    }


    private IntervalRunnable mRunnable = null;
    private int mCount = 0;
    private class IntervalRunnable implements Runnable {
        @Override
        public void run() {
            while(serviceRun){
                try {
                    mCount++;
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d("IntervalRunable","Count = " + mCount);
            }
            mCount = 0;
        }
    }
}
