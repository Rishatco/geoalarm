package android.rishat.myapplication;

import android.content.Context;
import android.net.Uri;

import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

public class Properties {

    private static  Properties mpropirtes;
    public  int radius;
    public  LatLng sLatLng;
    public  int number_music;
    public boolean rising_sound;
    public boolean flashlight;
    public boolean vibration;
    public CameraPosition current_camera_position;
    public Uri choosen_rington;
    private Properties(Context context){
        radius=500;
        number_music=0;
        sLatLng=null;
        rising_sound=false;
        flashlight=false;
        vibration=true;
        current_camera_position=null;
        choosen_rington=null;

    }

    public static Properties get(Context context){
        if (mpropirtes==null){
            mpropirtes= new Properties(context);
        }
        return  mpropirtes;
    }
}
