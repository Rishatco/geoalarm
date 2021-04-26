package android.rishat.myapplication.ui.home;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.rishat.myapplication.Common;
import android.rishat.myapplication.Properties;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.app.BundleCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.rishat.myapplication.R;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;
import java.util.List;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static androidx.constraintlayout.widget.Constraints.TAG;
import android.content.SharedPreferences;
public class HomeFragment extends SupportMapFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String APP_PREFERENCES = "mysettings";
    public static final String  LATITUDE_PREFERENCES="latitude";
    public static final String  LONGITUDE_PREFERENCES="longitude";
    private static final int ACCESS_COARSE_LOCATION =1 ;

    private String LATITUDE_KEY="android.rishat.myapplication.ui.home-latitude";
    private String LONGITUDE_KEY="android.rishat.myapplication.ui.home-longitude";

    private GoogleMap mMap;
    private final String MAP_PREF="map.preferense";
    private SharedPreferences mySharedPreferences;
    private MarkerOptions mMarkerOptions;
    private   LatLng latLng;
    private GoogleApiClient mClient;
    private int permissionStatus;
    private int access_coarse;
    private int internet;
    private static  int REQUEST_ERROR=0;
    private final static  int REQUEST_CODE_PERMISSION_READ_CONTACTS=1;
    private  static  int AUTOCOMPLETE_REQUEST_CODE = 1;
    private Properties mProperties;
    private  boolean map_ready=false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProperties= Properties.get(getContext());
        final GoogleApiAvailability apiAvailability= GoogleApiAvailability.getInstance();
        final int errorCode=apiAvailability.isGooglePlayServicesAvailable(getContext());
        if(mProperties.sLatLng!=null){
            latLng=mProperties.sLatLng;
        }
        Places.initialize(getActivity().getBaseContext(), "AIzaSyD4LVYDQGAfpI3gNMbAzGftG-gin_2n26g");
        PlacesClient placesClient = Places.createClient(getActivity().getBaseContext());
        setHasOptionsMenu(true);
        mClient= new GoogleApiClient.Builder(getActivity()).addApi(LocationServices.API).addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {
                Log.d("Geo", "YES");
            }

            @Override
            public void onConnectionSuspended(int i) {

            }
        }).build();
        mySharedPreferences =getContext().getSharedPreferences(MAP_PREF, Context.MODE_PRIVATE);
        mMarkerOptions = new MarkerOptions();
        permissionStatus = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        access_coarse= ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION);
        internet=  ContextCompat.checkSelfPermission(getContext(), Manifest.permission.INTERNET);
//        if (permissionStatus != PackageManager.PERMISSION_GRANTED || access_coarse != PackageManager.PERMISSION_GRANTED || internet != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions( new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET},
//                    REQUEST_CODE_PERMISSION_READ_CONTACTS );
//
//        }





       getMapAsync(new OnMapReadyCallback() {
           @Override
           public void onMapReady(GoogleMap googleMap) {
               mMap=googleMap;
               map_ready=true;
               if (permissionStatus != PackageManager.PERMISSION_GRANTED || access_coarse != PackageManager.PERMISSION_GRANTED || internet != PackageManager.PERMISSION_GRANTED) {
                   requestPermissions( new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET},
                           REQUEST_CODE_PERMISSION_READ_CONTACTS );

               }
               else{
                   mMap.getUiSettings().setScrollGesturesEnabled(true);
                   mMap.getUiSettings().setAllGesturesEnabled(true);
                   mMap.getUiSettings().setZoomGesturesEnabled(true);
                   mMap.setMyLocationEnabled(true);

                   mMap.getUiSettings().setMyLocationButtonEnabled(true);
               }


               if(latLng!=null){
                    final ProgressDialog mProgressDialog;
                   mProgressDialog = new ProgressDialog(getContext());
                   mProgressDialog.setMessage("Loading");
                   mProgressDialog.show();
                   mMarkerOptions.position(latLng);
                   mMap.addMarker(mMarkerOptions);
                   CircleOptions circleOptions=new CircleOptions().radius(mProperties.radius).center(mMarkerOptions.getPosition()).fillColor( 0x223f51b5).strokeWidth(5).strokeColor( 0x803f51b5);
                   mMap.addCircle(circleOptions);
                   mMap.animateCamera(CameraUpdateFactory.newCameraPosition(mProperties.current_camera_position), new GoogleMap.CancelableCallback() {
                       @Override
                       public void onFinish() {
                           mProgressDialog.dismiss();
                       }

                       @Override
                       public void onCancel() {

                       }
                   });

               }
               mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                   @Override
                   public void onMapClick(LatLng latLng) {
                            update_LatLng(latLng);
                   }
               });

                


           }
       });

    }
    @Override
    public void onPause() {
        super.onPause();
        Log.d("DEBUG","OnPause");

        // Release the Camera because we don't need it when paused
        // and other activities might need to use it.

    }
    @Override
    public  void onStop(){
        super.onStop();
        mClient.disconnect();

    }

    @Override
    public  void onStart(){
        super.onStart();
        if(mMarkerOptions.getPosition()!=null)
            mMap.addMarker(mMarkerOptions);
        mClient.connect();
    }
    @Override
    public  void onDestroy(){
        if(mMarkerOptions.getPosition()!=null){
            mProperties.sLatLng=mMarkerOptions.getPosition();

        }
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search_button, menu);
    }
    private void update_LatLng(final LatLng latLng){
        if(mMarkerOptions.getPosition()==null){
            LocationRequest request = LocationRequest.create();
            request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            request.setNumUpdates(1);
            request.setInterval(1);


            LocationServices.FusedLocationApi.requestLocationUpdates(mClient, request, new LocationListener() {
                @Override
                public void onLocationChanged(Location location1) {
                    Log.d("GEO", location1.getLongitude()+" "+location1.getLatitude());
                    mMarkerOptions.position(latLng);
                    LatLngBounds latLngBounds= new LatLngBounds.Builder().include(mMarkerOptions.getPosition()).include(new LatLng(location1.getLatitude(), location1.getLongitude())).build();

                    Location location = new Location("Point b");
                    location.setLongitude(mMarkerOptions.getPosition().longitude);
                    location.setLatitude(mMarkerOptions.getPosition().latitude);
                    if( check_distance(location.distanceTo(location1))){
                        mMap.clear();

                        mMap.addMarker(mMarkerOptions);
                        CircleOptions options=new CircleOptions().radius(mProperties.radius).center(mMarkerOptions.getPosition()).fillColor( 0x223f51b5).strokeWidth(5).strokeColor( 0x803f51b5);
                        mMap.addCircle(options);
                        CameraUpdate update= CameraUpdateFactory.newLatLngBounds(latLngBounds, 100);
                        mMap.animateCamera(update);
                        mProperties.sLatLng=latLng;
                        mProperties.current_camera_position= mMap.getCameraPosition();

                    }
                    else mMarkerOptions.position(null);

                }
            });

        }
        else {
            final AlertDialog.Builder builder= new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.warning);
            builder.setMessage("Do you want change place?\n");
            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    LocationRequest request = LocationRequest.create();

                    request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    request.setNumUpdates(1);
                    request.setInterval(1);


                    LocationServices.FusedLocationApi.requestLocationUpdates(mClient, request, new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location1) {
                            mMarkerOptions.position(latLng);
                            LatLngBounds latLngBounds= new LatLngBounds.Builder().include(mMarkerOptions.getPosition()).include( new LatLng(location1.getLatitude(), location1.getLongitude())).build();
                            //LatLngBounds latLngBounds= new LatLngBounds.Builder().include()
                           Location location = new Location("Point b");
                            location.setLongitude(mMarkerOptions.getPosition().longitude);
                            location.setLatitude(mMarkerOptions.getPosition().latitude);
                          if( check_distance(location.distanceTo(location1))){
                              mMap.clear();

                              mMap.addMarker(mMarkerOptions);
                              CircleOptions options=new CircleOptions().radius(mProperties.radius).center(mMarkerOptions.getPosition()).fillColor( 0x223f51b5).strokeWidth(5).strokeColor( 0x803f51b5);
                              mMap.addCircle(options);
                              CameraUpdate update= CameraUpdateFactory.newLatLngBounds(latLngBounds, 100);
                              mMap.animateCamera(update);
                              mProperties.sLatLng=latLng;
                              mProperties.current_camera_position= mMap.getCameraPosition();
                          }
                          else mMarkerOptions.position(null);

                        }
                    });

                }
            });
            builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            AlertDialog dialog=builder.create();
            dialog.show();

        }
    }

    @Override
    public boolean  onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.search_button:
                find_pos();

            default:
              return   super.onOptionsItemSelected(item);
        }


    }

    private void find_pos() {



        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);


        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, fields)
                .build(getContext());
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);



    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode ==  AUTOCOMPLETE_REQUEST_CODE ) {
            if (resultCode == RESULT_OK) {
                Log.i("Places", "OK");
                Place place = Autocomplete.getPlaceFromIntent(data);
                Log.i("Places", "Place: " + place.getName() + ", " + place.getLatLng());
                //Log.i("Places", place.getLatLng().toString() );
               update_LatLng(place.getLatLng());
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.e("Places", status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    @Override
    public  void onResume(){
        super.onResume();
        GoogleApiAvailability apiAvailability= GoogleApiAvailability.getInstance();
        int errorCode= apiAvailability.isGooglePlayServicesAvailable(getContext());
        if(errorCode!= ConnectionResult.SUCCESS){
            Dialog errorDialog =apiAvailability.getErrorDialog(getActivity(),errorCode, REQUEST_ERROR,new DialogInterface.OnCancelListener(){

                @Override
                public void onCancel(DialogInterface dialog) {
                    getActivity().finish();
                }
            });
            errorDialog.show();
        }
    }

    private boolean check_distance(float distance ){

        if (distance< mProperties.radius){
            AlertDialog.Builder builder= new AlertDialog.Builder(getActivity());
            builder.setTitle("Error");
            builder.setMessage(R.string.error_message);
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.show();
            return false;
        }
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions , int[] grantResults){
        switch (requestCode){
            case ACCESS_COARSE_LOCATION:
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            default:
                super.onRequestPermissionsResult(requestCode,permissions,grantResults);
                mMap.getUiSettings().setScrollGesturesEnabled(true);
                mMap.getUiSettings().setAllGesturesEnabled(true);
                mMap.getUiSettings().setZoomGesturesEnabled(true);
                mMap.setMyLocationEnabled(true);

                mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }

    }
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(Common.KEY_REQUESTING_LOCATIONS_UPDATES)){



        }
    }
}