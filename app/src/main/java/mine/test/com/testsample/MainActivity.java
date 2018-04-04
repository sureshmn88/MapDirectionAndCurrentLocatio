package mine.test.com.testsample;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ahmadrosid.lib.drawroutemap.DrawMarker;
import com.ahmadrosid.lib.drawroutemap.DrawRouteMaps;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import mine.test.com.testsample.SampleMine.Constants;
import mine.test.com.testsample.SampleMine.GeocodeAddressIntentService;
import mine.test.com.testsample.SampleMine.HttpConnection;
import mine.test.com.testsample.SampleMine.PathJSONParser;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, GoogleMap.InfoWindowAdapter, GoogleMap.OnInfoWindowClickListener,
        LocationListener {

    private static final String TAG = "MapLocation";
    private static final int REQUEST_SOURCE_LOCATION = 459;

    public MapView mMapView;
    public GoogleMap mGoogleMap;
    LatLng mCenterLatLng;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mCurrentLocation;
    private Boolean mRequestingLocationUpdates;
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    private final static String LOCATION_KEY = "location-key";

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    boolean isGPSEnabled = false;
    boolean isGetLocationInfo = false;
    private boolean Storelocation = false;

    private double fusedLatitude = 0.0;
    private double fusedLongitude = 0.0;

    MarkerOptions mp1;

    boolean isCurrentLocation = false;

    EditText etLoc;
    Button btnSubmit;
    TextView tvLocFirst,tvLocSec,tvDistance;
    ImageButton ibFirst,ibSec;
    int locType=0;
    LatLng locFirst,locSec;

    ProgressDialog mProgressDialog;
    AddressResultReceiver mResultReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        InitView();

        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();

        MapsInitializer.initialize(MainActivity.this);
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                setMap(googleMap);
            }
        });

        mResultReceiver = new AddressResultReceiver(null);

    }

    private void setMap(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                String msg = latLng.latitude + ", " + latLng.longitude;
                Log.d(TAG, "Location Info:" + msg);
                //sharjah = new LatLngBounds(new LatLng(25.2, 55.51), new LatLng(24.6, 55.61));
                /*LatLngBounds ADELAIDE = new LatLngBounds(
                        new LatLng(-35.0, 138.58), new LatLng(-34.9, 138.61));
                mGoogleMap.setLatLngBoundsForCameraTarget(ADELAIDE);*/

            }
        });

        mGoogleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                Log.d(TAG, "Location Info 1:" + marker.getPosition());
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                Log.d(TAG, "Location Info 1:" + marker.getPosition());
                return null;
            }
        });

        mGoogleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {

                LatLng latLng = cameraPosition.target;
                if (mCenterLatLng != null) {
                    if (latLng.latitude == mCenterLatLng.latitude && latLng.longitude == mCenterLatLng.longitude) {
                        return;
                    }
                }

                mCenterLatLng = cameraPosition.target;

                Location mLocation = new Location("");
                mLocation.setLatitude(mCenterLatLng.latitude);
                mLocation.setLongitude(mCenterLatLng.longitude);

                fusedLatitude = mCenterLatLng.latitude;
                fusedLongitude = mCenterLatLng.longitude;

                Log.d(TAG, "Display LatLng:" + fusedLatitude + "-" + fusedLongitude);

            }
        });

        mGoogleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                Log.d(TAG, "Display LatLng:" + fusedLatitude + "-" + fusedLongitude);
                //Toast.makeText(MainActivity.this,etLoc.getText().toString().trim(),Toast.LENGTH_LONG).show();
                return null;
            }
        });

        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.d(TAG, "Marker Click:" + fusedLatitude + "-" + fusedLongitude);
                //Toast.makeText(MainActivity.this,etLoc.getText().toString().trim(),Toast.LENGTH_LONG).show();
                //showDialog(etLoc.getText().toString().trim());
                return false;
            }
        });

        mGoogleMap.setInfoWindowAdapter(this);
        mGoogleMap.setOnInfoWindowClickListener(this);

    }

    void showDialog(String message) {

        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("Location");
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();

    }

    void InitView() {

        mMapView = (MapView) findViewById(R.id.map);

        etLoc= (EditText) findViewById(R.id.loc_address);
        btnSubmit= (Button) findViewById(R.id.loc_submit);
        tvLocFirst= (TextView) findViewById(R.id.loc_first);
        tvLocSec= (TextView) findViewById(R.id.loc_sec);
        tvDistance= (TextView) findViewById(R.id.loc_distance);
        ibFirst= (ImageButton) findViewById(R.id.ibFirst);
        ibSec= (ImageButton) findViewById(R.id.ibSec);

        mRequestingLocationUpdates = false;

        buildGoogleApiClient();

        startUpdatesButtonHandler();

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!etLoc.getText().toString().trim().isEmpty()) {
                    mProgressDialog = new ProgressDialog(MainActivity.this);
                    mProgressDialog.setTitle("Loading");
                    mProgressDialog.setMessage("Please Wait..");
                    mProgressDialog.setCancelable(true);
                    mProgressDialog.show();

                    String address = etLoc.getText().toString();

                    GeocodingLocation locationAddress = new GeocodingLocation();
                    locationAddress.getAddressFromLocation(address,
                            getApplicationContext(), new GeocoderHandler());
                } else {
                    Toast.makeText(MainActivity.this,"Enter Location",Toast.LENGTH_SHORT).show();
                }

            }
        });

        tvLocFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locType=0;
                openPlaceScreen();
            }
        });
        tvLocSec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locType=1;
                openPlaceScreen();
            }
        });

        ibFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvLocFirst.setText("");
                mGoogleMap.clear();
                if (locSec != null) {
                    mp1 = new MarkerOptions();
                    mp1.position(locSec);

                    mp1.draggable(true);
                    mp1.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_loc_sec));
                    mGoogleMap.addMarker(mp1);

                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locSec, 15));
                }
            }
        });

        ibSec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvLocSec.setText("");
                mGoogleMap.clear();
                if (locFirst!= null) {
                    mp1 = new MarkerOptions();
                    mp1.position(locFirst);

                    mp1.draggable(true);
                    mp1.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_loc_first));
                    mGoogleMap.addMarker(mp1);

                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locFirst, 15));
                }
            }
        });

        tvDistance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    void openPlaceScreen() {

        Intent intentpick = null;
        try {
            intentpick = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).setBoundsBias(new LatLngBounds(
                    new LatLng(9.859464, 78.048662),
                    new LatLng(9.987299, 78.174319))).build(this);
            startActivityForResult(intentpick, REQUEST_SOURCE_LOCATION);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SOURCE_LOCATION) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(MainActivity.this, data);
                if (locType == 0) {
                    locFirst=place.getLatLng();
                    tvLocFirst.setText(place.getAddress() + "");
                    setLocationMarker();
                } else {
                    locSec=place.getLatLng();
                    tvLocSec.setText(place.getAddress() + "");
                    setLocationMarker();
                }
            }
        }
    }

    void setLocationMarker() {

        /*mGoogleMap.clear();

        if (locFirst!=null) {

            mp1 = new MarkerOptions();
            mp1.position(locFirst);

            mp1.draggable(true);
            mp1.icon(BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
            mGoogleMap.addMarker(mp1);
        }

        if (locSec != null) {

            mp1 = new MarkerOptions();
            mp1.position(locSec);

            mp1.draggable(true);
            mp1.icon(BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            mGoogleMap.addMarker(mp1);

            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locSec, 15));

            drawLines();

        } else {
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locFirst, 15));
        }*/

        if (locFirst != null && locSec != null) {
            drawLines();
        }

    }

    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void startUpdatesButtonHandler() {

        if (!isPlayServicesAvailable(this)) return;
        if (!mRequestingLocationUpdates) {
            mRequestingLocationUpdates = true;
        } else {
            return;
        }

        if (Build.VERSION.SDK_INT < 23) {
            //setButtonsEnabledState();
            //startLocationUpdates();
            checkLocationEnable();
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //setButtonsEnabledState();
            checkLocationEnable();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                showRationaleDialog();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        }
    }

    public void stopUpdatesButtonHandler() {
        if (mRequestingLocationUpdates) {
            mRequestingLocationUpdates = false;
            //setButtonsEnabledState();
            stopLocationUpdates();
        }
    }

    private void startLocationUpdates() {

        Log.i(TAG, "startLocationUpdates");

        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        } else {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, MainActivity.this);
            }
        }

    }

    private void updateUI() {

        if (mCurrentLocation == null) return;

        fusedLatitude = mCurrentLocation.getLatitude();
        fusedLongitude = mCurrentLocation.getLongitude();

        Log.d(TAG, "fLat:" + fusedLatitude);
        Log.d(TAG, "fLng:" + fusedLongitude);

        locFirst=new LatLng(fusedLatitude,fusedLongitude);

        setMarker();
        if (locFirst != null) {
            getAddresssFromLatLng();
        }

        Storelocation = true;
        stopUpdatesButtonHandler();

    }

    void setMarker() {

        mGoogleMap.clear();

        mp1 = new MarkerOptions();
        mp1.position(locFirst);

        mp1.draggable(true);
        mp1.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_loc_first));
        mGoogleMap.addMarker(mp1);

        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locFirst, 15));

    }

    protected void stopLocationUpdates() {
        Log.i(TAG, "stopLocationUpdates");
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        if (mGoogleApiClient.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //setButtonsEnabledState();
                    //startLocationUpdates();
                    checkLocationEnable();
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                        mRequestingLocationUpdates = false;
                        Toast.makeText(MainActivity.this, "To enable the function of this application please enable location permission of the application from the setting screen of the terminal.", Toast.LENGTH_SHORT).show();
                    } else {
                        showRationaleDialog();
                    }
                }
                break;
            }
        }
    }

    private void showRationaleDialog() {
        new AlertDialog.Builder(this)
                .setPositiveButton("To give permission", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                    }
                })
                .setNegativeButton("do not do", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, "Position information permission was not allowed.", Toast.LENGTH_SHORT).show();
                        mRequestingLocationUpdates = false;
                    }
                })
                .setCancelable(false)
                .setMessage("This application needs to allow use of location information.")
                .show();
    }

    public static boolean isPlayServicesAvailable(Context context) {
        // Google Play Service APKが有効かどうかチェックする
        int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            GoogleApiAvailability.getInstance().getErrorDialog((Activity) context, resultCode, 2).show();
            return false;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        isPlayServicesAvailable(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        Storelocation = false;
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        stopLocationUpdates();
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "onConnected");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            //updateUI();
        }

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
            checkLocationEnable();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "onLocationChanged");
        mCurrentLocation = location;
        updateUI();
        //Toast.makeText(this, "Location Updated", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        super.onSaveInstanceState(savedInstanceState);
    }

    void checkLocationEnable() {

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!isGPSEnabled) {
            isGPSEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!isGPSEnabled) {
                showSettingsAlert(this);
                isGetLocationInfo = false;
            } else {
                startLocationUpdates();
            }
        } else {
            startLocationUpdates();
        }

    }

    public void showSettingsAlert(final Activity activity) {

            android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(
                    activity);
            alertDialog.setTitle("SETTINGS");
            alertDialog.setCancelable(false);
            alertDialog.setMessage("Enable Location Provider! Go to settings menu?");
            alertDialog.setPositiveButton("Settings",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(
                                    Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            activity.startActivity(intent);
                        }
                    });
            alertDialog.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            alertDialog.show();

    }


    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String locationAddress;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    break;
                default:
                    locationAddress = null;
            }
            List<String> locationList= Arrays.asList(locationAddress.split(","));
            fusedLatitude=Double.parseDouble(locationList.get(0));
            fusedLongitude=Double.parseDouble(locationList.get(1));

            setMarker();

            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }
        }
    }

    void drawLines() {

        if(mGoogleMap!=null)
            mGoogleMap.clear();

        DrawRouteMaps.getInstance(this)
                .draw(locFirst, locSec, mGoogleMap);
        DrawMarker.getInstance(this).draw(mGoogleMap, locFirst, R.drawable.ic_loc_first, "Origin Location");
        DrawMarker.getInstance(this).draw(mGoogleMap, locSec, R.drawable.ic_loc_sec, "Destination Location");

        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(locFirst)
                .include(locSec).build();
        Point displaySize = new Point();
        getWindowManager().getDefaultDisplay().getSize(displaySize);
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, displaySize.x, 250, 30));

        Double distance=getDistance(locFirst.latitude,locFirst.longitude,locSec.latitude,locSec.longitude,"K");
        tvDistance.setText(String.format("%.2f", distance)+" KM");

        /*
        MarkerOptions options = new MarkerOptions();


        options.position(locFirst);
        options.position(locSec);
        mGoogleMap.addMarker(options);
        String url = getMapsApiDirectionsUrl();
        ReadTask downloadTask = new ReadTask();
        downloadTask.execute(url);

        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locSec,13));

        if (mGoogleMap != null) {
            mGoogleMap.addMarker(new MarkerOptions().position(locFirst)
                    .title("First Point"));
            mGoogleMap.addMarker(new MarkerOptions().position(locSec)
                    .title("Second Point"));
        }
        */

    }

    void getAddresssFromLatLng() {

        Intent intent = new Intent(this, GeocodeAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.FETCH_TYPE_EXTRA, Constants.USE_ADDRESS_LOCATION);

        if (locFirst.latitude == 0.0 || locFirst.longitude== 0.0) {
            Toast.makeText(this,
                    "Please enter both latitude and longitude",
                    Toast.LENGTH_LONG).show();
            return;
        }

        intent.putExtra(Constants.LOCATION_LATITUDE_DATA_EXTRA,locFirst.latitude);
        intent.putExtra(Constants.LOCATION_LONGITUDE_DATA_EXTRA,locFirst.longitude);

        Log.e(TAG, "Starting Service");
        startService(intent);
    }

    /*
    * Get Address From LatLng
    * */

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, final Bundle resultData) {
            if (resultCode == Constants.SUCCESS_RESULT) {
                final Address address = resultData.getParcelable(Constants.RESULT_ADDRESS);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        /*tvLocFirst.setText("Latitude: " + address.getLatitude() + "\n" +
                                "Longitude: " + address.getLongitude() + "\n" +
                                "Address: " + resultData.getString(Constants.RESULT_DATA_KEY));*/
                        tvLocFirst.setText(resultData.getString(Constants.RESULT_DATA_KEY));

                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvLocFirst.setText(resultData.getString(Constants.RESULT_DATA_KEY));
                    }
                });
            }
        }
    }

    /*
    * Map Direction
    * */

    private String getMapsApiDirectionsUrl() {
        /*String waypoints = "waypoints=optimize:true|"
                + locFirst.latitude + "," + locFirst.longitude
                + "|" + locSec.latitude + ","
                + locSec.longitude;*/

        String waypoints = "waypoints=optimize:true|"
                + locFirst.latitude + "," + locFirst.longitude
                + "|" + "|" + locSec.latitude + ","
                + locSec.longitude + "|" + locFirst.latitude + ","
                + locFirst.longitude;

        String sensor = "sensor=false";
        String params = waypoints + "&" + sensor;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + params;
        return url;
    }

    private class ReadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                HttpConnection http = new HttpConnection();
                data = http.readUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new ParserTask().execute(result);
        }
    }

    private class ParserTask extends
            AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                PathJSONParser parser = new PathJSONParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
            ArrayList<LatLng> points = null;
            PolylineOptions polyLineOptions = null;

            // traversing through routes
            for (int i = 0; i < routes.size(); i++) {
                points = new ArrayList<LatLng>();
                polyLineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = routes.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                polyLineOptions.addAll(points);
                polyLineOptions.width(2);
                polyLineOptions.color(Color.BLUE);
            }

            mGoogleMap.addPolyline(polyLineOptions);
        }
    }

    /*
    * Getting Distance between Two LatLng
    * */

    /*private double getDistance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }*/

    private static double getDistance(double lat1, double lon1, double lat2, double lon2, String unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == "K") {
            dist = dist * 1.609344;
        } else if (unit == "N") {
            dist = dist * 0.8684;
        }

        return (dist);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::	This function converts decimal degrees to radians						 :*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::	This function converts radians to decimal degrees						 :*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }


}
