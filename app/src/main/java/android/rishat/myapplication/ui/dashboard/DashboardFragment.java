package android.rishat.myapplication.ui.dashboard;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;

import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.rishat.myapplication.Common;
import android.rishat.myapplication.MainActivity;
import android.rishat.myapplication.MyBackgroundServices;
import android.rishat.myapplication.Properties;
import android.rishat.myapplication.SendLocationToActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.rishat.myapplication.R;
import android.widget.Toast;

import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import android.content.Context;
import android.content.Intent;

public class DashboardFragment extends Fragment  implements SharedPreferences.OnSharedPreferenceChangeListener{

    private DashboardViewModel dashboardViewModel;
    private Properties mProperties;
    private Button requestLocation;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private SettingsClient mSettingsClient;
    private LocationSettingsRequest mLocationSettingsRequest;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private ActivityRecognitionClient mActivityRecognitionClient;
    private LocationCallback mLocationCallback;
    private Button removeLocation;
    private TextView choosen_view;
    public View root;
     MyBackgroundServices myBackgroundService=null;
    boolean mBound =false;

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


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, final Bundle savedInstanceState) {
        dashboardViewModel =
                ViewModelProviders.of(this).get(DashboardViewModel.class);
         root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        Button choose_button = (Button) root.findViewById(R.id.rington_choose_button);
        SeekBar bar = (SeekBar) root.findViewById(R.id.radius_seek_bar);
        Switch rising_switch = (Switch) root.findViewById(R.id.rising_sound_switch);
        Switch vibration = (Switch) root.findViewById(R.id.vibration_switch);
        final Switch flashlight = (Switch) root.findViewById(R.id.flashlight_switch);
        final TextView radius_v = (TextView) root.findViewById(R.id.radius_text_view);
        requestLocation = (Button) root.findViewById(R.id.starting_button);
        removeLocation =(Button)root.findViewById(R.id.stop);
        choosen_view=(TextView)root.findViewById(R.id.choosen_view);
        mProperties = Properties.get(getContext());
        if(mProperties.choosen_rington!=null){
            choosen_view.setText(RingtoneManager.getRingtone(getContext(),mProperties.choosen_rington).getTitle(getContext()));
        }else
            choosen_view.setText("");
        if(ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.SYSTEM_ALERT_WINDOW)
                != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[] {Manifest.permission.SYSTEM_ALERT_WINDOW},
                    5);

        }
        rising_switch.setChecked(mProperties.rising_sound);
        vibration.setChecked(mProperties.vibration);
        flashlight.setChecked(mProperties.flashlight);
        bar.setProgress(mProperties.radius-500);
        radius_v.setText(radius_v.getText()+"  "+mProperties.radius);
        bar.setPadding(0, 0, 5, 0);

        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mProperties.radius = progress+500;
                String str = (String) radius_v.getText();
                String[] parst = str.split(" ");
                str = parst[0];
                str += "  " + (progress + 500);
                radius_v.setText(str);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        flashlight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean hasFlash = getContext().getPackageManager()
                        .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

                if (!hasFlash) {
                    AlertDialog.Builder builder= new AlertDialog.Builder(getActivity());
                    builder.setTitle("Error");
                    builder.setMessage("You haven't flashlight");
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                                flashlight.setChecked(mProperties.flashlight);
                        }
                    });
                    builder.show();
                }
                if(hasFlash) {
                    int permissionStatus = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA);

                    if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                        mProperties.flashlight = isChecked;
                    } else {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA},
                                5);
                        mProperties.flashlight = isChecked;
                    }
                }

            }
        });
        vibration.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mProperties.vibration = isChecked;
            }
        });
        rising_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mProperties.rising_sound = isChecked;
            }
        });
        dashboardViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {

            }
        });
        requestLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mProperties.sLatLng!=null) {
                    myBackgroundService.requestLocationUpdates();
                    Toast.makeText(getContext(), "Поехали", Toast.LENGTH_LONG).show();
                }
                else{
                   AlertDialog.Builder builder= new AlertDialog.Builder(getActivity());
                    builder.setTitle("Error");
                    builder.setMessage("You don't picked finish point");
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    builder.show();
                }
            }
        });
        removeLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myBackgroundService.removeLocationUpdates();
            }
        });

        setButtonState(Common.requestingLocationUpdates(getContext()));
        getContext().bindService(new Intent(getContext(),MyBackgroundServices.class),mServiceConnection, Context.BIND_AUTO_CREATE);
        choose_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    choose_ringtone();

            }
        });

        return root;
    }

    private  void choose_ringtone(){
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
        startActivityForResult(intent, 5);

    }

    private void setButtonState(boolean isRequestEnable) {
        if(isRequestEnable){
            requestLocation.setEnabled(false);
            removeLocation.setEnabled(true);
        }
        else{
            requestLocation.setEnabled(true);
            removeLocation.setEnabled(false);
        }

    }
    @Subscribe(sticky =true,threadMode = ThreadMode.MAIN)
    public void onListenLocation(SendLocationToActivity event){
        if (event != null) {
//            String data = new  StringBuilder().append(event.getLocation().getLatitude())
//                    .append("/").append(event.getLocation().getLongitude())
//                    .toString();
//            Toast.makeText(myBackgroundService, data ,Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(Common.KEY_REQUESTING_LOCATIONS_UPDATES)){

            setButtonState(sharedPreferences.getBoolean(Common.KEY_REQUESTING_LOCATIONS_UPDATES, false));

        }
    }

    @Override
    public void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(this);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        if (mBound) {
            ContextWrapper wrapper= new ContextWrapper(getContext());
            wrapper.unbindService(mServiceConnection);
            mBound=false;
        }
        PreferenceManager.getDefaultSharedPreferences(getContext()) .unregisterOnSharedPreferenceChangeListener(this);
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent intent)
    {
        Log.e("TEST_MUSIC","I am getting ringtone id");
        if (resultCode == Activity.RESULT_OK && requestCode == 5)
        {
            Uri uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);

            if (uri != null)
            {
               mProperties.choosen_rington = uri;
               choosen_view.setText(RingtoneManager.getRingtone(getContext(),uri).getTitle(getContext()));

            }

        }
    }

}
