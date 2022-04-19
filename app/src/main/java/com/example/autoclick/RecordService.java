package com.example.autoclick;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.Multipart;

public class RecordService extends Service {

    private Retrofit retrofit = ClientApi.getClientApi();
    private RetrofitInterface retrofitInterface = retrofit.create(RetrofitInterface.class);

    private static View tv;											//항상 보이게 할 뷰

    WindowManager wm;

    BroadcastReceiver br = new MyBroadcastReceiver();

    public ImageView temp_img;

    private static final String TAG = "Always_record";

    private String someMember;

    private ArrayList<String> touchList;
    private ArrayList<File> filePathList;
    private ArrayList<String> fileName;

    Bitmap mBitmap;


    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            record_end();

        }
    }

    static Context con;

    String tc_name = "TC_iot";
    int i = 0;

    int xx [] = {300, 700, 700,  300 };
    int yy [] = {300, 300, 1000, 1000};

    int move_xx  [] = {400, 0, -400, 0};
    int move_yy [] = {0, 700, 0, -700};

    int x;
    int y;

    long time_back = 0;

    AutoServiceNew autoService = null;


    public RecordService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "시작");

        super.onCreate();

        someMember = "Some Member";
//        binder = new Binder();

        con = this;

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        tv = inflater.inflate(R.layout.capture_view, null);


//        //최상위 윈도우에 넣기 위한 설정
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                //주석시작
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.TYPE_PHONE,
                //주석끝
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE // 막으면 키 이벤트 받을수 있음 ex) backkey
//                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.LEFT | Gravity.TOP;
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        wm.addView(tv, params);        //최상위 윈도우에 뷰 넣기. *중요 : 여기에 permission을 미리 설정해 두어야 한다. 매니페스트에

        tv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {


                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    Log.d(TAG,"onTouch =================> action up");
                    x = (int) motionEvent.getX();
                    y = (int) motionEvent.getY();

                    long temp_time = System.currentTimeMillis();
                    long temp_gap = temp_time - time_back;
                    Log.d(TAG,"TOUCH Info ================> "+ x + "," + y + "," + temp_gap);
                    touchList.add(x + "," + y + "," + temp_gap);
                    time_back = temp_time;
                    ev_handle.sendMessageDelayed(ev_handle.obtainMessage(1), 10);
                }
                return false;
            }
        });



        time_back = System.currentTimeMillis();
        touchList = new ArrayList<>();
        filePathList = new ArrayList<>();
        fileName = new ArrayList<>();

        IntentFilter filter = new IntentFilter("com.pin.record_end");
        registerReceiver(br, filter);

        Intent intent = new Intent(getApplicationContext(), AutoServiceNew.class);
        startService(intent);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();

        autoService.StopAService(); //꼭 서비스 stop을 해준다.
//        autoService = null;

        if(tv != null)		//서비스 종료시 뷰 제거. *중요 : 뷰를 꼭 제거 해야함.
        {
            ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(tv);
            tv = null;
        }
        unregisterReceiver(br);
    }
    private Handler ev_handle = new Handler(){
        public void handleMessage(Message msg) {

            try {

                System.out.println("JJJ ev_handle :" + msg.what );

                if( wm == null){
                    return;
                }

                switch (msg.what){
                    case 1 :{
                        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                                150,150,
                                0,0,
                                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                                PixelFormat.TRANSLUCENT);
                        params.gravity = Gravity.LEFT | Gravity.TOP;
                        wm.removeView(tv);
                        wm.addView(tv,params);
                        ev_handle.sendMessageDelayed(ev_handle.obtainMessage(2), 10);

                    }
                    break;
                    case 2 :{
                        if(autoService == null) {
                            autoService = AutoServiceNew.getInstance();
                        }
                        autoService.tapEvent(x, y);
        //                autoService.dragEvent(160, 500, 160+1500, 500);
                        ev_handle.sendMessageDelayed(ev_handle.obtainMessage(3), 500);
                    }
                    break;
                    case 3 :{
                        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                                WindowManager.LayoutParams.TYPE_PHONE,
                                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                        |WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                                        |WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                                PixelFormat.TRANSLUCENT);
                        params.gravity = Gravity.LEFT | Gravity.TOP;
                        wm.removeView(tv);
                        wm.addView(tv,params);

                        //record Btn 을 최상위로 올려준다.
                        Intent intent = new Intent("com.pin.goFront");
                        sendBroadcast(intent);
                    }
                    break;
                    case 4 :{

                    }
                    break;
                }


            }catch (Exception e){
                Log.d(TAG, "handleMessage: EEEE : " + e);
            }
        }
    };

    //텍스트내용을 경로의 텍스트 파일에 쓰기
    public void WriteTextFile(String contents, String file_name){

        Log.d(TAG, "WriteTextFile: "+contents);
        try{

            File file = new File(Environment.getExternalStorageDirectory() + "/" + file_name +".json");
            //파일 output stream 생성
            FileWriter fw = null ;

            fw = new FileWriter(file) ;

            //파일쓰기
            BufferedWriter writer = new BufferedWriter(fw);
            writer.write(contents);
            writer.flush();

            writer.close();
            fw.close() ;

            filePathList.add(file);
            fileName.add(file_name +".json");

        }catch (IOException e){
            e.printStackTrace();
            Log.d(TAG, "WriteTextFile: ");
        }
    }

    public void save(){

//       Date date = new Date(System.currentTimeMillis());
//       String file_name = tc_name+new SimpleDateFormat("HHmm", Locale.KOREA).format(date);

        filePathList.clear();
        fileName.clear();
        SharedPreferences pref = getSharedPreferences("auto", Context.MODE_PRIVATE);

        int count = pref.getInt("count", 0);

        String s = String.format("%03d", count);

        String file_name = tc_name+s;

        Bitmap image = BitmapFactory.decodeFile("/sdcard/"+tc_name+".png");

        Bitmap newBmp = rotateImage(image, 270 );

//       Bitmap newBmp = rotateImage(image, 0 );

        File file = new File("/sdcard/"+file_name+".png");
        FileOutputStream filestream = null;
        try {
            filestream = new FileOutputStream(file);
            newBmp.compress(Bitmap.CompressFormat.PNG, 0, filestream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        filePathList.add(file);
        fileName.add(file_name +".png");
        image.recycle();
        newBmp.recycle();


        ArrayList<TouchEvent> temp_touchList = new ArrayList<>();

        for(String event : touchList){

            TouchEvent temp = new TouchEvent();

            String[] name_split = event.split(",");
            temp.setPosition_x(name_split[0]);
            temp.setPosition_y(name_split[1]);
            temp.setDelay_time(name_split[2]);

            temp_touchList.add(temp);
        }

        TouchData touchData = new TouchData();

        touchData.setTest_name("TC_iot");
        touchData.setTouchList(temp_touchList);

        WriteTextFile(  new Gson().toJson(touchData), file_name);

        SharedPreferences.Editor editor = pref.edit();

        editor.putInt("count", count+1);
        editor.commit();

//        StartService.getevent(1, file_name); 
        
        //파일 업로드 진행 retropit2 사용
        multiImage();
    }


    // 이미지 회전 함수
    public Bitmap rotateImage(Bitmap src, float degree) {

        // Matrix 객체 생성
        Matrix matrix = new Matrix();
        // 회전 각도 셋팅
        matrix.postRotate(degree);
        // 이미지와 Matrix 를 셋팅해서 Bitmap 객체 생성
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(),
                src.getHeight(), matrix, true);
    }


    public void record_end(){
        Log.d(TAG, "recod_end: ~~~~~~~~~~~~~~~~~~~~~~~~~");
//                Environment.getExternalStorageDirectory()
        if(tv != null)		//서비스 종료시 뷰 제거. *중요 : 뷰를 꼭 제거 해야함.
        {
            ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(tv);
            tv = null;
        }
//        boolean aa =  setProp("screencap -p /sdcard/"+tc_name+".png" );
        Log.d("R.id.capture","R.id.capture" );
        Intent intent = new Intent(getApplicationContext(), screenCapture.class); //signal, slot 같은 개념이네
        intent.putExtra("action", "/sdcard/"+tc_name+".png");
        startActivity(intent);
//        Log.d(TAG, "onClick: screencap ::" + aa);
        wm = null;
        ev_handle.removeMessages(-1, null);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(1), 5000);
    }

    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            save();
            stopSelf();
        }
    };

    private void multiImage() {
        try{
//            File filesDir = getApplicationContext().getFilesDir();
//            File file = new File(filesDir, "image" + ".png");
//
//            ByteArrayOutputStream bos = new ByteArrayOutputStream();
//            mBitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
//            byte[] bitmapdata = bos.toByteArray();
//
//
//            FileOutputStream fos = new FileOutputStream(file);
//            fos.write(bitmapdata);
//            fos.flush();
//            fos.close();

            ArrayList<MultipartBody.Part> files = new ArrayList<>();
            for (int i = 0; i < filePathList.size(); ++i) {
                // Uri 타입의 파일경로를 가지는 RequestBody 객체 생성
                RequestBody fileBody = RequestBody.create(MediaType.parse("image/*"), filePathList.get(i));

                // 사진 파일 이름
                //String filename = fileName.get(i);
                // RequestBody로 Multipart.Part 객체 생성
                MultipartBody.Part filePart = MultipartBody.Part.createFormData("uploadFile", fileName.get(i), fileBody);

                // 추가
                files.add(filePart);
            }
//            RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
//            MultipartBody.Part body = MultipartBody.Part.createFormData("images", file.getName(), reqFile);
//            RequestBody name = RequestBody.create(MediaType.parse("text/plain"), "images");

            Call<ResponseBody> req = retrofitInterface.multiImage2(files);
            req.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Toast.makeText(getApplicationContext(), response.code() + " ", Toast.LENGTH_SHORT).show();

                    for (int i = 0; i < filePathList.size(); ++i)
                        filePathList.get(i).delete();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(getApplicationContext(), "응답 실패", Toast.LENGTH_SHORT).show();
                    t.printStackTrace();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}