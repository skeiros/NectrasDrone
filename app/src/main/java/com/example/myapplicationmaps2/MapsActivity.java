package com.example.myapplicationmaps2;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import com.example.myapplicationmaps2.controladores.RadioDialogFragment;
import com.example.myapplicationmaps2.databinding.ActivityMapsBinding;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, RadioDialogFragment.NoticeDialogListener {

    Context mContext;

    private GoogleMap mMap;
    private Location lastKnownLocation;
    LocationRequest locationRequest ;
    private LocationCallback locationCallback;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;
    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient fusedLocationClient;
    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 17;
    Button  btn_next, btn_reset_map, btn_add_home, btn_add_maker, btn_add_obstaculo,btn_perimetro_interno;
    ImageButton btn_delete_last_point;
    private LatLng home_point;
    private Marker home_point_marker;
    private double altitud_home_point;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    //PERIMETRO VALUES
    public ArrayList<LatLng> perimeterPoints = new ArrayList<>();
    public ArrayList<LatLng> perimetroInternoPoints = new ArrayList<>();
    private final ArrayList<Double>altitud_perimeterPoints = new ArrayList<>();
    private final ArrayList<Double>altitud_perimetroInternoPoints = new ArrayList<>();
    private final ArrayList<Marker> perimetroMarkers = new ArrayList<>();
    public ArrayList<Marker> perimetroInternoMarkers = new ArrayList<>();
    private final ArrayList<LatLng>ubicacion_obstaculos=new ArrayList<>();
    private final ArrayList<Integer>radio_obstaculos=new ArrayList<>();
    private final ArrayList<Double>altitud_obstaculos=new ArrayList<>();
    private Polyline LineasPerimetro, LineasPerimetroInterno;
    int periColor = Color.parseColor("#00547e");
    int periInternoColor = Color.parseColor("#D7F7ED");
    ImageButton btn_config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext=this;

        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            //cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        com.example.myapplicationmaps2.databinding.ActivityMapsBinding binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Construct a FusedLocationProviderClient.
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        iniciarLocationRequest();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        btn_add_home = findViewById(R.id.button_add_home);
        btn_add_maker = findViewById(R.id.button_add_maker);
        btn_next = findViewById(R.id.button_next);
        btn_reset_map = findViewById(R.id.btn_reset_map);
        btn_delete_last_point = findViewById(R.id.btn_delete_last_point);
        btn_config = findViewById(R.id.imageButton_config);
        btn_add_obstaculo = findViewById(R.id.button_add_obstaculo);
        btn_perimetro_interno = findViewById(R.id.btn_perimetro_interno);

        btn_add_home.setOnClickListener(v -> marcarHome());
        btn_add_maker.setOnClickListener(v -> marcarPunto());
        btn_perimetro_interno.setOnClickListener(v -> marcarPuntoPerimetroInterno());
        btn_config.setOnClickListener(v -> {
            Intent miIntent = new Intent(MapsActivity.this, Configuracion.class);
            startActivity(miIntent);
        });
        btn_next.setOnClickListener(v -> {
            if(home_point!=null && perimeterPoints.size()>2){
                Intent miIntent = new Intent(MapsActivity.this, MapsActivityRutaVuelo.class);
                miIntent.putExtra("puntosPerimetro", perimeterPoints);
                miIntent.putExtra("altitudPuntosPerimetro",altitud_perimeterPoints);
                miIntent.putExtra("uobstaculos", ubicacion_obstaculos);
                miIntent.putExtra("robstaculos",radio_obstaculos);
                miIntent.putExtra("homePoint_lat", home_point.latitude);
                miIntent.putExtra("homePoint_long", home_point.longitude);
                miIntent.putExtra("homePoint_altitud",altitud_home_point);
                miIntent.putExtra("aobstaculos", altitud_obstaculos);
                miIntent.putExtra("perimetroInterno", perimetroInternoPoints);
                miIntent.putExtra("altitudPerimetroInterno", altitud_perimetroInternoPoints);
                startActivity(miIntent);
            }else{
                Toast t=Toast.makeText(this,"Debe ingresar un home point y al menos tres puntos de perímetro.",Toast.LENGTH_LONG);
                t.show();
            }
        });
        btn_reset_map.setOnClickListener(v -> clearPointsPerimeter());
        btn_delete_last_point.setOnClickListener(v -> deleteLastPoint());
        btn_add_obstaculo.setOnClickListener(v -> mostrarDialogoRadio());

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //updateLocation(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
            /* se actualizará cada minuto y 50 metros de cambio en la localización
            mientras más pequeños sean estos valores más frecuentes serán las actualizaciones */
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 1, this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    //Log.e("LOG_PRUEBA", "LLAMADA AL CALLBACK DESDE ONCREATE, LOCATION  " + location.toString());
                    // Muevo la cámara a mi posición
                    CameraUpdate cam = CameraUpdateFactory.newLatLngZoom(
                            new LatLng(location.getLatitude(),
                                    location.getLongitude()), DEFAULT_ZOOM);
                    mMap.moveCamera(cam);
                    onResume();
                }
            }
        };
    }

    private void iniciarLocationRequest() {
        //Este objeto se crea para evaluar si el gps está desactivado, y en caso de que lo este,
        // mostrar el cuadro de diálogo para activarlo.
        locationRequest = LocationRequest.create();
        //locationRequest.setInterval(10000);
        //locationRequest.setFastestInterval(5000);
        //locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);;
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MapsActivity.this,REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }

    /*private void updateLocation(Location location) {
        try{
            Log.e("TAG_PRUEBA", "Update Location: " + location.toString());
        }catch(java.lang.NullPointerException ex){
            Log.e("TAG_PRUEBA", "Update Location: location null");
        }
    }*/

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        // Prompt the user for permission.
        getLocationPermission();
        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();
        // Get the current location of the device and set the position of the map.
        getDeviceLocation();
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker arg0) {
            }

            @Override
            public void onMarkerDragEnd(Marker arg0) {
                createPerimterLines();/// ACTUALIZAMOS EL PERIMETRO
                mMap.animateCamera(CameraUpdateFactory.newLatLng(arg0.getPosition()));
            }

            @Override
            public void onMarkerDrag(Marker arg0) {
            }
        });
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        if(perimeterPoints.size()>2) createPerimterLines(); // VEMOS SI TENEMOS CREADO UN PERIMETRO,
    }



    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, lastKnownLocation);
        }
        super.onSaveInstanceState(outState);
    }


    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationClient.getLastLocation();
                locationResult.addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.getResult();
                        if (lastKnownLocation != null) {
                            Log.d("TAG_PRUEBA", "Current location is: " + lastKnownLocation.toString());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(lastKnownLocation.getLatitude(),
                                            lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            //mMap.animateCamera(CameraUpdateFactory.newLatLng(arg0.getPosition()));
                        }
                    } else {
                        Log.e("TAG_PRUEBA", "Exception: %s", task.getException());
                        mMap.moveCamera(CameraUpdateFactory
                                .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                        mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationPermissionGranted = false;
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            }
        }
        updateLocationUI();
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private BitmapDescriptor bitmapDescriptorFromVector(String tipo) {
        Drawable vectorDrawable;
        switch (tipo) {
            case "home":
                vectorDrawable = ContextCompat.getDrawable(this, R.drawable.ic_point_home);
                break;
            case "point":
                vectorDrawable = ContextCompat.getDrawable(this, R.drawable.ic_point);
                break;
            case "obstaculo":
                vectorDrawable = ContextCompat.getDrawable(this, R.drawable.ic_point_obstaculo);
                break;
            default:
                vectorDrawable = ContextCompat.getDrawable(this, R.drawable.ic_point);
                break;
        }
        assert vectorDrawable != null;
        vectorDrawable.setBounds(0, 0, 150, 150);
        Bitmap bitmap = Bitmap.createBitmap(150, 150, Bitmap.Config.ARGB_8888);//vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void marcarHome() {
        BitmapDescriptor icon = bitmapDescriptorFromVector("home");
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationClient.getLastLocation();
                locationResult.addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.getResult();
                        if (lastKnownLocation != null) {
                            Log.d("TAG_PRUEBA", "Current location is: " + lastKnownLocation.toString());
                            try{
                                //Primero tengo que borrar el home anterior, reinicio el mapa
                                home_point_marker.remove();
                            }catch (java.lang.NullPointerException ex){

                            }
                            //Ahora genero el nuevo
                            home_point = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                            home_point_marker=mMap.addMarker(new MarkerOptions().position(home_point).title("Punto home").icon(icon));
                            altitud_home_point=lastKnownLocation.getAltitude();
                        }
                    } else {
                        Log.d("TAG_PRUEBA", "Current location is null. Using defaults.");
                        Log.e("TAG_PRUEBA", "Exception: %s", task.getException());
                        mMap.moveCamera(CameraUpdateFactory
                                .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                        mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }


    private void marcarPunto() {
        BitmapDescriptor icon = bitmapDescriptorFromVector("point");
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationClient.getLastLocation();
                locationResult.addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.getResult();
                        if (lastKnownLocation != null) {
                            //Log.d("TAG_PRUEBA", "Current location is: " + lastKnownLocation.toString());
                            LatLng point = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                            perimeterPoints.add(point);
                            altitud_perimeterPoints.add(lastKnownLocation.getAltitude());
                            //mMap.addMarker(new MarkerOptions().position(point).title("Punto campo").icon(icon));
                            Toast t=Toast.makeText(this,"POINT: "+point.toString(),Toast.LENGTH_LONG);
                            t.show();
                            MarkerOptions m=new MarkerOptions();
                            m.draggable(true);
                            m.position(point);
                            m.title("Vertice "+perimetroMarkers.size());
                            m.icon(icon);
                            Marker mm=mMap.addMarker(m);
                            perimetroMarkers.add(mm);
                            createPerimterLines();
                        }
                    } else {
                        Log.d("TAG_PRUEBA", "Current location is null. Using defaults.");
                        Log.e("TAG_PRUEBA", "Exception: %s", task.getException());
                        mMap.moveCamera(CameraUpdateFactory
                                .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                        mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    private void marcarPuntoPerimetroInterno(){
        BitmapDescriptor icon = bitmapDescriptorFromVector("point");
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationClient.getLastLocation();
                locationResult.addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.getResult();
                        if (lastKnownLocation != null) {
                            //Log.d("TAG_PRUEBA", "Current location is: " + lastKnownLocation.toString());
                            LatLng point = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                           perimetroInternoPoints.add(point);
                           altitud_perimetroInternoPoints.add(lastKnownLocation.getAltitude());
                            //mMap.addMarker(new MarkerOptions().position(point).title("Punto campo").icon(icon));
                            Toast t=Toast.makeText(this,"POINT: "+point.toString(),Toast.LENGTH_LONG);
                            t.show();
                            MarkerOptions m=new MarkerOptions();
                            m.draggable(true);
                            m.position(point);
                            m.title("Vertice "+perimetroInternoMarkers.size());
                            m.icon(icon);
                            Marker mm=mMap.addMarker(m);
                            perimetroInternoMarkers.add(mm);
                            createPerimetroInternoLines();
                        }
                    } else {
                        Log.d("TAG_PRUEBA", "Current location is null. Using defaults.");
                        Log.e("TAG_PRUEBA", "Exception: %s", task.getException());
                        mMap.moveCamera(CameraUpdateFactory
                                .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                        mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    //MARCAMOS PERIMETRO CON LINEAS CON MAKERS
    public void createPerimterLines(){
        ArrayList <LatLng> points2=new ArrayList<>();
        if(perimetroMarkers.size()>2){
            for(int i=0;i<perimetroMarkers.size();i++){
                points2.add(perimetroMarkers.get(i).getPosition());
            }
            points2.add(perimetroMarkers.get(0).getPosition());
            if(LineasPerimetro==null){
                LineasPerimetro = mMap.addPolyline(new PolylineOptions()
                        .clickable(false)
                        .color(periColor)
                        .width(10)
                );
            }
            LineasPerimetro.setPoints(points2);
        }else{
            if(LineasPerimetro!=null){
                LineasPerimetro.remove();
                LineasPerimetro=null;
            }
        }
    }

    public void createPerimetroInternoLines(){
        ArrayList <LatLng> points2=new ArrayList<>();
        if(perimetroInternoMarkers.size()>2){
            for(int i=0;i<perimetroInternoMarkers.size();i++){
                points2.add(perimetroInternoMarkers.get(i).getPosition());
            }
            points2.add(perimetroInternoMarkers.get(0).getPosition());
            if(LineasPerimetroInterno==null){
                LineasPerimetroInterno = mMap.addPolyline(new PolylineOptions()
                        .clickable(false)
                        .color(periInternoColor)
                        .width(10)
                );
            }
            LineasPerimetroInterno.setPoints(points2);
        }else{
            if(LineasPerimetroInterno!=null){
                LineasPerimetroInterno.remove();
                LineasPerimetroInterno=null;
            }
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        mMap.moveCamera(CameraUpdateFactory
                .newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()), DEFAULT_ZOOM));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(perimeterPoints.size()>2) createPerimterLines(); // VEMOS SI TENEMOS CREADO UN PERIMETRO,
        startLocationUpdates();

    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        LocationRequest locationRequest=new LocationRequest();
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    //Elimina el último punto del mapa
    public  void  deleteLastPoint() {
        try{
            if(perimetroMarkers.size()>0) {
                //Obtengo el índice del último punto
                int index = perimetroMarkers.size() - 1;
                //Crea el marker a partir del índice correspondiente en relación al mapa
                Marker m = perimetroMarkers.get(index);
                //Borra el marker del mapa
                m.remove();
                //Borra el marker del ArrayList de puntos
                perimetroMarkers.remove(index);
                perimeterPoints.remove(index);
                //Vuelve a dibujar el perimetro con los puntos como quedaron
                createPerimterLines();
            }
        }catch(ArrayIndexOutOfBoundsException ex){
            ex.printStackTrace();
        }
    }

    // ELIMINA EL PERIMETRO.
    public  void  clearPointsPerimeter(){
        try{
            perimetroMarkers.clear();
            perimetroInternoMarkers.clear();
        }catch (java.lang.NullPointerException ignored){

        }
        perimeterPoints.clear();
        perimetroInternoPoints.clear();
        LineasPerimetro=null;
        LineasPerimetroInterno=null;
        ubicacion_obstaculos.clear();
        radio_obstaculos.clear();
        mMap.clear();
    }

    public void mostrarDialogoRadio(){
        DialogFragment newFragment = new RadioDialogFragment();
        newFragment.show(getSupportFragmentManager(), "missiles");

    }

    public void marcarObstaculo(int radio){

        BitmapDescriptor icon = bitmapDescriptorFromVector("obstaculo");
        int myColor = getResources().getColor(R.color.gray);
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationClient.getLastLocation();
                locationResult.addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.getResult();
                        if (lastKnownLocation != null) {
                            Log.d("TAG_PRUEBA", "Current location obstaculo is: " + lastKnownLocation.toString());
                            LatLng ubicacion_obstaculo = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                            mMap.addMarker(new MarkerOptions().position(ubicacion_obstaculo).title("Obstáculo").icon(icon));
                            mMap.addCircle(new CircleOptions()
                                    .center(ubicacion_obstaculo)
                                    .radius(radio)
                                    .fillColor(myColor));
                            ubicacion_obstaculos.add(ubicacion_obstaculo);
                            radio_obstaculos.add(radio);
                            altitud_obstaculos.add(lastKnownLocation.getAltitude());
                        }
                    } else {
                        Log.d("TAG_PRUEBA", "Current location is null. Using defaults.");
                        Log.e("TAG_PRUEBA", "Exception: %s", task.getException());
                        mMap.moveCamera(CameraUpdateFactory
                                .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                        mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }

    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    @Override
    public void onDialogPositiveClick(DialogFragment dialog, int radio) {
        // User touched the dialog's positive button
        Log.d("DIALOG-ACTIVITY", "PRESIONÓ FIRE, el radio ingresado es "+radio);
        marcarObstaculo(radio);
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button
        Log.d("DIALOG-ACTIVITY", "PRESIONÓ CANCEL");
    }
}