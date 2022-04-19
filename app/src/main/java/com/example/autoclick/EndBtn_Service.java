package com.example.autoclick;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

public class EndBtn_Service extends Service {
    private static final String TAG = "Always_btn";

    private String someMember;
//    private Binder binder;

    private ImageView _dragView;
    private WindowManager _windowManager;
    private WindowManager.LayoutParams _windowParams;
    private Point _touchPosition = new Point(0, 0);
    private Point _touchPositionOffset = new Point(0, 0);

    private int initialX = 0;
    private int initialY = 0;
    private Float initialTouchX = 0f;
    private Float initialTouchY = 0f;

    boolean isDrag = false;
    static Context con;
    BroadcastReceiver br = new MyBroadcastReceiver();
    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            //view를 최상위로 올린다.
            Log.d(TAG, "front ======================>");
            //((EndBtn_Service)con)._dragView.requestFocus();

            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    ((EndBtn_Service)con)._windowParams.width, ((EndBtn_Service)con)._windowParams.height,
                    ((EndBtn_Service)con)._windowParams.x, ((EndBtn_Service)con)._windowParams.y,
//                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
//                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                            |WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                            |WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                    PixelFormat.TRANSLUCENT);


            params.gravity = Gravity.LEFT | Gravity.TOP;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.width = WindowManager.LayoutParams.WRAP_CONTENT;
            params.format = PixelFormat.TRANSLUCENT;
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE ;

            ((EndBtn_Service)con)._windowManager.removeView(((EndBtn_Service)con)._dragView);
            ((EndBtn_Service)con)._windowManager.addView(((EndBtn_Service)con)._dragView,params);
        }
    }
    public EndBtn_Service() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
//        binder = new Binder();
        con = this;

//        _windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);

        _windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);	//윈도우 매니저 불러옴.

//        _windowParams = new WindowManager.LayoutParams();

        _windowParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
//                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
//                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
//                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                        |WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        |WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);


        _windowParams.gravity = Gravity.LEFT | Gravity.TOP;
        _windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        _windowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        _windowParams.format = PixelFormat.TRANSLUCENT;
        _windowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE ;


        doMakeDragview();

        _dragView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
//                Log.d("JJJJ", "toutch");

                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN: {
                        isDrag = false;
                        initialX = _windowParams.x;
                        initialY = _windowParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return false;
                    }
                    case MotionEvent.ACTION_MOVE: {

                        if (!isDrag && isDragging(event)) {
                            isDrag = true;
                        }
                        if (!isDrag) return false;

                        _windowParams.x = (int) (initialX + (event.getRawX() - initialTouchX));
                        _windowParams.y = (int) (initialY + (event.getRawY() - initialTouchY));


                        doDragView((int) event.getX(), (int) event.getY());
                    }
                    break;

                    case MotionEvent.ACTION_UP: {
                        if (!isDrag) {

                            Intent intent = new Intent("com.pin.record_end");
                            sendBroadcast(intent);
                            Log.d("setOnTouchListener", "onTouch: click");
                            stopSelf();
                        }


                    }
                    break;
                }

                return false;
            }
        });

//        _dragView.bringToFront();

        IntentFilter filter = new IntentFilter("com.pin.goFront");
        registerReceiver(br, filter);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();

//        mtime_Handler.removeMessages(0);

        if(_dragView != null)		//서비스 종료시 뷰 제거. *중요 : 뷰를 꼭 제거 해야함.
        {
            ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(_dragView);
            _dragView = null;
        }
        unregisterReceiver(br);
    }

    public boolean isDragging(MotionEvent event){
        Boolean  aa =
                ((Math.pow((event.getRawX() - initialTouchX), 2.0)
                        + Math.pow((event.getRawY() - initialTouchY), 2.0))
                        > 10 * 10);
        return  aa;
    }

    // 드래그 하는 동안 보일 view
    private void doMakeDragview()
    {
//        View item = (View) getChildAt(_selectItemIndex - getFirstVisiblePosition()); //  getFirstVisiblePosition() 이 없으면 스크롤 됐을 때 문제 생김
//        item.setDrawingCacheEnabled(true);
//        Bitmap bitmap = Bitmap.createBitmap(item.getDrawingCache());

        ImageView image = new ImageView(con);
        image.setBackgroundColor(Color.parseColor("#00000000"));
        image.setImageResource(R.drawable.record);

//        _touchPositionOffset.x = (int) (item.getWidth() * 0.5);
//        _touchPositionOffset.y = (int) (item.getHeight() * 0.5);

        _touchPositionOffset.x = (int) (0);
        _touchPositionOffset.y = (int) (0);

        _windowParams.x = _touchPosition.x - _touchPositionOffset.x;
        _windowParams.y = _touchPosition.y - _touchPositionOffset.y;
        _windowManager.addView(image, _windowParams);
        _dragView = image;

    }
    // 드래그하기
    private void doDragView(int $x, int $y)
    {
        if (_dragView == null)
            return;

//        _windowParams.x = $x - _touchPositionOffset.x;
//        _windowParams.y = $y - _touchPositionOffset.y;
        _windowManager.updateViewLayout(_dragView, _windowParams);

//        _dragView.setX(_windowParams.x);
//        _dragView.setY(_windowParams.y);

    }
}