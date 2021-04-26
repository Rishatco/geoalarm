package android.rishat.myapplication;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.shitij.goyal.slidebutton.SwipeButton;

public class Aweking_activity extends AppCompatActivity {
   private Button awaking_button;
   private Uri ringtone_uri;
   MyBackgroundServices myBackgroundService=null;
   private Ringtone ringtone;
   private boolean rising_sound;
   private boolean vibration;
   private boolean flashlight;
   boolean mBound;
   private boolean isWork =true;
    private Camera camera;
    private boolean isFlashOn;
    private boolean hasFlash;
    Camera.Parameters params;
    private Thread TurchThread = new Thread(new Runnable() {
        @Override
        public void run() {
            getCamera();
            while(isWork){
                try {

                        turnOnFlash();
                        Thread.sleep(500);
                        turnOffFlash();
                        Thread.sleep(500);


                }catch (InterruptedException ex){
                    Log.d("Turch_log",ex.getMessage());
                }
            }
        }
    });
    String cameraId = null; // Usually back camera is at 0 position.
    private void getCamera() {
        if (camera == null) {
            try {
                camera = Camera.open();
                params = camera.getParameters();
            } catch (RuntimeException e) {
                Log.e("Ошибка", e.getMessage());
            }
        }
    }
   private   Vibrator vibsys;
   private Thread rising_tread= new Thread(new Runnable() {
       @RequiresApi(api = Build.VERSION_CODES.P)
       @Override
       public void run() {
           if(isWork)
           for(int i=10; i<100;i+=10){
               ringtone.setVolume((float)i/1000);
               try {
                   Thread.sleep(1000);
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }
           }
       }
   });
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyBackgroundServices.LocalBinder binder =(MyBackgroundServices.LocalBinder)service;
            myBackgroundService =binder.getService();
            mBound=true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            myBackgroundService=null;
            mBound=false;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aweking_activity);
        awaking_button=(Button)findViewById(R.id.awake_button);

        ringtone_uri=(Uri)getIntent().getParcelableExtra("ringtone_uri");
        if(ringtone_uri==null){
           ringtone_uri=RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM);
        }
        rising_sound=getIntent().getBooleanExtra("rising_sound", false);
        flashlight=getIntent().getBooleanExtra("flashlight", false);
        vibration=getIntent().getBooleanExtra("vibration", false);
        ringtone= RingtoneManager.getRingtone(this, ringtone_uri);
        if(rising_sound) rising_tread.start();
        long[] pattern = {0, 100, 1000};
        if(vibration){
          vibsys = (Vibrator)this.getSystemService(Context.VIBRATOR_SERVICE);
            vibsys.vibrate(pattern, 0);
        }





        ringtone.play();
        if(flashlight) TurchThread.start();
        Toast.makeText(getApplicationContext(), "Wake Up", Toast.LENGTH_LONG).show();
        awaking_button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ringtone.stop();
                if(vibsys.hasVibrator())
                    vibsys.cancel();
                if(rising_tread.isAlive())
                    rising_tread.stop();
                if(TurchThread.isAlive())
                    isWork=false;

                finish();
                return false;
            }
        });
    }
    private void turnOnFlash() {
        if (!isFlashOn) {
            if (camera == null || params == null) {
                return;
            }
            params = camera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(params);
            camera.startPreview();
            isFlashOn = true;

        }
    }
    private void turnOffFlash() {
        if (isFlashOn) {
            if (camera == null || params == null) {
                return;
            }
            params = camera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(params);
            camera.stopPreview();
            isFlashOn = false;


        }
    }
}
