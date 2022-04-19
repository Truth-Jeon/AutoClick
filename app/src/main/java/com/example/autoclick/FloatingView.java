package com.example.autoclick;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.example.autoclick.R;


public class FloatingView extends Service implements View.OnClickListener {
    private WindowManager mWindowManager;
    private View myFloatingView = null;
//    AutoServiceNew autoService = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        //getting the widget layout from xml using layout inflater
        myFloatingView = LayoutInflater.from(this).inflate(R.layout.floating_view, null);

        int layout_parms;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            layout_parms = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        else {
            layout_parms = WindowManager.LayoutParams.TYPE_PHONE;
        }

        //setting the layout parameters
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layout_parms,
                 WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        //getting windows services and adding the floating view to it
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(myFloatingView, params);

        //adding an touchlistener to make drag movement of the floating widget
        myFloatingView.findViewById(R.id.thisIsAnID).setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("TOUCH","THIS IS TOUCHED");
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_UP:

                        return true;

                    case MotionEvent.ACTION_MOVE:
                        //this code is helping the widget to move around the screen with fingers
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        mWindowManager.updateViewLayout(myFloatingView, params);
                        return true;
                }
                return false;
            }
        });

        Button startButton = (Button) myFloatingView.findViewById(R.id.start);
        startButton.setOnClickListener(this);
        Button stopButton = (Button) myFloatingView.findViewById(R.id.stop);
        stopButton.setOnClickListener(this);
        Button recordButton = (Button) myFloatingView.findViewById(R.id.record);
        recordButton.setOnClickListener(this);
        Button captureButton = (Button) myFloatingView.findViewById(R.id.capture);
        captureButton.setOnClickListener(this);


//        Intent intent = new Intent(getApplicationContext(), AutoServiceNew.class);
//        startService(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onClick(View v) {
        //Log.d("onClick","THIS IS CLICKED");
//        Intent intent = new Intent(getApplicationContext(), AutoService.class);
        switch (v.getId()) {
            case R.id.start:
                //Log.d("START","THIS IS STARTED");
//                int[] location = new int[2];
//                myFloatingView.getLocationOnScreen(location);
//                intent.putExtra("action", "play");
//                intent.putExtra("x", location[0] - 1);
//                intent.putExtra("y", location[1] - 1);


                int[] location = new int[2];
                myFloatingView.getLocationOnScreen(location);

                Log.d("location","X : " + location[0] + ", Y : " + location[1] );
//                if(autoService == null) {
//                    autoService = AutoServiceNew.getInstance();
//                }
////                autoService.tapEvent(160, 500);
//                autoService.dragEvent(160, 500, 160+1500, 500);
                break;
            case R.id.stop:
//                intent.putExtra("action", "stop");

//                Intent intent = new Intent(getApplicationContext(), AutoServiceNew.class);
//                stopService(intent);


//                autoService.StopAService();
//                autoService = null;


                if (myFloatingView != null)
                    mWindowManager.removeView(myFloatingView);
                Intent appMain = new Intent(getApplicationContext(), MainActivity.class);

                getApplication().startActivity(appMain);

                stopSelf();
                break;
            case R.id.record:
                Log.d("R.id.record","R.id.record");

                startService(new Intent(this, RecordService.class));
                startService(new Intent(this, EndBtn_Service.class));
                break;
            case R.id.capture:

                Log.d("R.id.capture","R.id.capture" );
                Intent intent = new Intent(getApplicationContext(), screenCapture.class); //signal, slot 같은 개념이네
                intent.putExtra("action", "/sdcard/play.png");
                startActivity(intent);
                break;
                //requires the FLAG_ACTIVITY_NEW_TASK flag
    }
//        getApplication().startService(intent);
    }

}