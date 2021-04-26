package android.rishat.myapplication;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.greenrobot.eventbus.EventBus;




    public class MyBackgroundServices extends Service {

        private static final String CHANNEL_ID="my_channel";
        private static final String EXTRA_STARTED_FROM_NOTIFICATION ="com.example.backgroudnlocation"+
                ".started_from_notofication";

        private final IBinder mBinder = new LocalBinder();
        private static final long UPDATE_INTERVAL_IN_MIL=10000;
        private static final long FASTEST_UPDATE_INTERVAL_IN_MIL=UPDATE_INTERVAL_IN_MIL/2;
        private static final int NOTI_ID=1223;
        private boolean mChangingConfiguration=false;
        private NotificationManager mNotificationManager;

        private LocationRequest locationRequest;
        private FusedLocationProviderClient fusedLocationProviderClient;
        private LocationCallback locationCallback;
        private Handler mServiceHandler;
        private Location mLocation;
        private Properties properties;
        private Location finish_point;
        private Uri ringtone;
        private int radius;
        private boolean isAwake=false;
        private boolean rising_sound;
        private boolean vibration;
        private boolean flashlight;


        public  MyBackgroundServices(){






        }


        @Override
        public void onConfigurationChanged(Configuration newConfig) {
            super.onConfigurationChanged(newConfig);
            mChangingConfiguration=true;
        }

        @Override
        public void onCreate() {
            fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this);
            locationCallback = new LocationCallback(){

                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    onNewLocation(locationResult.getLastLocation());
                }
            };
            properties=Properties.get(this);

            createLocationRequest();
            getLastLocation();

            HandlerThread handlerThread= new HandlerThread("EDMTDev");
            handlerThread.start();
            mServiceHandler = new Handler(handlerThread.getLooper());
            mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
                NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID,getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);

                mNotificationManager.createNotificationChannel(mChannel);

            }

        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {

            boolean startFromNotification = intent.getBooleanExtra(EXTRA_STARTED_FROM_NOTIFICATION,false);
            if(startFromNotification){
                removeLocationUpdates();
                stopSelf();
            }
            return START_NOT_STICKY ;
        }

        public void removeLocationUpdates() {
            try {

                fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                Common.setRequestingLocationUpdates(this, false);
                stopSelf();
            }   catch (SecurityException ex){
                Common.setRequestingLocationUpdates(this, true);
                Log.e("EDMT_DEV",  "Lost location permission.Cloud not remove Updates. "+ ex);
            }
        }

        private void getLastLocation() {
            try
            {
                fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if(task.isSuccessful() && task.getResult()!=null)
                            mLocation=task.getResult();
                        else
                            Log.e("EDMT_DEV", "Failed to get location");
                    }
                });
            }catch (SecurityException ex){
                Log.e("EDMT_DEV", "Lost location permission. "+ex);
            }
        }

        private void createLocationRequest() {
            locationRequest= new LocationRequest();
            locationRequest.setInterval(UPDATE_INTERVAL_IN_MIL);
            locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MIL);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        }

        private void onNewLocation(Location lastLocation) {
            mLocation=lastLocation;
            EventBus.getDefault().postSticky(new SendLocationToActivity(mLocation));
            if(mLocation.distanceTo(finish_point)<radius && !isAwake){



                Intent intent1= new Intent( this, Aweking_activity.class);
                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                // intent1.setFlags(Intent.);
                intent1.putExtra("ringtone_uri", ringtone);
                intent1.putExtra("vibration", vibration);
                intent1.putExtra("rising_sound", rising_sound);
                intent1.putExtra("flashlight", flashlight);
                intent1.setAction(Intent.ACTION_VIEW);
                intent1.addCategory(Intent.CATEGORY_HOME);
                Log.d("TEST", "i am cheking my position");
                //removeLocationUpdates();
                startActivity(intent1);
                isAwake=true;


            }
            if(isAwake)
                removeLocationUpdates();
            //Update notification content   if running as a foreground service
            if(serviceIsRunnigInForeGround(this))
                mNotificationManager.notify(NOTI_ID,getNotification());
        }

        private Notification getNotification() {
            Intent  intent =new Intent(this,MyBackgroundServices.class);

            String text;
            text = Common.getLocationText(mLocation);
            if(mLocation.distanceTo(finish_point)>radius){
                text="еще не приехали";
            }else{
                text="приехали";
//                Intent intent1= new Intent( this, Aweking_activity.class);
//                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//               // intent1.setFlags(Intent.);
//                intent.putExtra("ringtone_uri", ringtone);
//                intent1.setAction(Intent.ACTION_VIEW);
//                intent1.addCategory(Intent.CATEGORY_HOME);
//                //removeLocationUpdates();
//                startActivity(intent1);

            }
            intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true);
            PendingIntent servicePendingIntent=PendingIntent.getService(this,0,intent, PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent activityPendingIntent=PendingIntent.getActivity(this,0, new Intent(this, MainActivity.class),0);

            NotificationCompat.Builder builder=new NotificationCompat.Builder(this).addAction(R.drawable.ic_launch_black_24dp, "Launch", activityPendingIntent).
                    addAction(R.drawable.ic_cancel_black_24dp, "Remove", servicePendingIntent).setContentText(text).setContentTitle(Common.getLocationTitle(this)).setOngoing(true)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setTicker(text)
                    .setWhen(System.currentTimeMillis());
            //set the channel id for Android
            if(Build.VERSION.SDK_INT> Build.VERSION_CODES.O){

                builder.setChannelId(CHANNEL_ID);

            }
            return builder.build();
        }

        private boolean serviceIsRunnigInForeGround(Context context) {
            ActivityManager manager= (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
            for(ActivityManager.RunningServiceInfo service:manager.getRunningServices(Integer.MAX_VALUE))
                if(getClass().getName().equals(service.service.getClassName()))
                    if(service.foreground)
                        return true;
            return false;
        }

        public void requestLocationUpdates() {
            radius=properties.radius;
            finish_point=new Location("point b");
            finish_point.setLatitude(properties.sLatLng.latitude);
            finish_point.setLongitude(properties.sLatLng.longitude);
            ringtone=properties.choosen_rington;
            flashlight=properties.flashlight;
            rising_sound=properties.rising_sound;
            vibration=properties.vibration;
            Common.setRequestingLocationUpdates(this,true);
            startService(new Intent(getApplicationContext(),MyBackgroundServices.class));
            try{
                fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());
            }catch (SecurityException ex){
                Log.e("EDMT_DEV", "Lost Location permission. Could not request it "+ex);
            }
        }

        public class LocalBinder extends Binder {
            public MyBackgroundServices getService(){ return  MyBackgroundServices.this ;}
        }



        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            stopForeground(true);
            mChangingConfiguration=false;
            return mBinder;
        }

        @Override
        public void onRebind(Intent intent) {
            super.onRebind(intent);
        }

        @Override
        public boolean onUnbind(Intent intent) {
            if(!mChangingConfiguration && Common.requestingLocationUpdates(this))
                startForeground(NOTI_ID, getNotification());

            return true;
        }

        @Override
        public void onDestroy() {
            mServiceHandler.removeCallbacks(null);
            super.onDestroy();
        }
    }


