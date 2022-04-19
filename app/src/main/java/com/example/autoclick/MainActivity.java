package com.example.autoclick;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.Toast;
import android.os.Bundle;

import java.util.List;

import static android.content.ContentValues.TAG;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    //FrameLayout mLayout;
    private static final int SYSTEM_ALERT_WINDOW_PERMISSION = 2084;
    Button startButton;
    Button playButton;
    Button accessibilityButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(this);
        startButton.setEnabled(false); //startButton 뷰 비활성화. (setEnabled(false)가 뷰를 비활성화한다는 뜻임.)
        playButton = (Button) findViewById(R.id.playButton);
        playButton.setOnClickListener(this);
        playButton.setEnabled(false);
        accessibilityButton = (Button) findViewById(R.id.accessibilityButton);
        accessibilityButton.setOnClickListener(this);
        accessibilityButton.setEnabled(false);


        testPermission();

        // 접근성 권한이 없으면 접근성 권한 설정하는 다이얼로그 띄워주는 부분
        if(!checkAccessibilityPermissions()) {
            //setAccessibilityPermissions();
            accessibilityButton.setEnabled(true);
        }
        else
        {
            startButton.setEnabled(true);
            playButton.setEnabled(true);
        }

//        findViewById(R.id.startButton).setOnClickListener(this);
    }

    // 접근성 권한이 있는지 없는지 확인하는 부분
    // 있으면 true, 없으면 false
    public boolean checkAccessibilityPermissions() {
        AccessibilityManager accessibilityManager = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        //AccessibilityManager : 우리말로 번역 시 "접근성 관리자" 라는 뜻으로, 접근성 서비스에 등록된 서비스들은 이 클래스의 영향을 받는다.

        // getEnabledAccessibilityServiceList는 현재 접근성 권한을 가진 리스트를 가져오게 된다. 지정된 피드백 유형에 대해 활성화된 접근성 서비스의 AccessibilityServiceInfo를 반환한다.
        List<AccessibilityServiceInfo> list = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.DEFAULT);

        for (int i = 0; i < list.size(); i++) {
            //접근성 권한을 가지게 되면 동작하게 되는 부분, 여기서 연결과 이벤트 발생시 콜백을 받을 수 있다.
            AccessibilityServiceInfo info = list.get(i);

            // 접근성 권한을 가진 앱의 패키지 네임과 패키지 네임이 같으면 현재앱이 접근성 권한을 가지고 있다고 판단함
            if (info.getResolveInfo().serviceInfo.packageName.equals(getApplication().getPackageName())) {
                return true;
            }
        }
        return false;
//        return true;
    }

    // 접근성 설정화면으로 넘겨주는 부분
    public void setAccessibilityPermissions() {
        //사용자의 전체 화면을 가리지 않으면서 사용자의 응답이나 추가 정보를 입력하도록 하는 작은 창을 의미한다.
        // AlertDialog.Builder 객체를 생성하여 AlertDialog의 다양한 디자인을 구축할 수 있다.
        AlertDialog.Builder gsDialog = new AlertDialog.Builder(this);
        gsDialog.setTitle("접근성 권한 설정");//Text title(내용)을 바꾸는 메소드.
        gsDialog.setMessage("접근성 권한을 필요로 합니다");
        gsDialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // 설정화면으로 보내는 부분
//                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));

                startActivityForResult(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS), 112);
                // startActivityForResult() : 새 액티비티를 열어줌 + 결과 값 전달 (쌍방향)

                try {
                    if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        Intent i = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS); //Intent는 데이터를 주고받기 위한 용도로 쓰인다.
                        startActivityForResult(i, 112);
                    }
                } catch(ActivityNotFoundException e){
                    Toast.makeText(getApplicationContext(), "미지원 기기입니다.", Toast.LENGTH_LONG).show();
                    //getApplicationContext()는 Application Context를 가리킴. 반대로 this는 Activity Context를 가리킨다. this는 getBaseContext()와 같은 기능을 한다.
                }
                return;
            }
        }).create().show();
    }

    private String[] permission_list = {
            Manifest.permission.INTERNET,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.SYSTEM_ALERT_WINDOW
    };
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==0)
        {
            for(int i=0; i<grantResults.length; i++)
            {
                //허용됬다면
                if(grantResults[i]== PackageManager.PERMISSION_GRANTED){ //PERMISSION_GRANTED : 퍼미션 허용 상태.
                }
                else {
                    //권한을 하나라도 허용하지 않는다면 앱 종료
                    Toast.makeText(getApplicationContext(),"앱권한설정하세요", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
    }
//    private void askPermission() {
//        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
//                Uri.parse("package:" + getPackageName()));
//        startActivityForResult(intent, SYSTEM_ALERT_WINDOW_PERMISSION);
//    }
    public void testPermission() {
        Log.d(TAG, "recod_end: 1");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d(TAG, "recod_end: 2");
            if (!Settings.canDrawOverlays(this)) {

                Log.d(TAG, "recod_end: 3");
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 111);
            }
        }
    }
    //
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 111) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {

                    //저장 권한 부여
                    for(String permission : permission_list){
                        int chk = checkCallingOrSelfPermission(permission);

                        if(chk == PackageManager.PERMISSION_DENIED){
                            //권한 허용을여부를 확인하는 창을 띄운다
                            requestPermissions(permission_list,0);
                        }
                    }
                }
            }
        }
        else if (requestCode == 112) {
            if(!checkAccessibilityPermissions()) {
                setAccessibilityPermissions();
            }
            else
            {
                startButton.setEnabled(true); //startButton 뷰 활성화 (setEnabled(true)가 뷰를 활성화한다는 뜻임.)
                playButton.setEnabled(true);
                accessibilityButton.setEnabled(false);
                Intent intent = new Intent(getApplicationContext(), screenCapture.class); //signal, slot 같은 개념이네
                intent.putExtra("action", "/sdcard/temp.png");
                startActivity(intent);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startButton:
                Log.d("R.id.record","R.id.record");

                startService(new Intent(this, RecordService.class)); //startService : 시작타입의 서비스는 호출한 곳에 결과 값을 반환하지 않고 계속해서 서비스한다.
                startService(new Intent(this, EndBtn_Service.class));

                finish();
                break;
            case R.id.playButton:
                break;
            case R.id.accessibilityButton:
                setAccessibilityPermissions();
                break;

        }

    }

//    @Override
//    //Full 모드
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//        if (hasFocus) {
//            getWindow().getDecorView().setSystemUiVisibility(
//                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
//        }
//    }
}
