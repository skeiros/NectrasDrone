package com.example.myapplicationmaps2.controladores;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.jetbrains.annotations.NotNull;

public class NectrasAppGPS {
    Activity con;
    //private Boolean isActive;
    Location lasLocation = null;
    // La clase FusedLocationProviderClient, se usa para leer la ubicación más reciente del usuario
    private FusedLocationProviderClient fusedLocationClient;

    // Estado del Settings de verificación de permisos del GPS
    private static final int REQUEST_CHECK_SETTINGS = 102;

    // La clase LocationRequest sirve para  para solicitar las actualizaciones de ubicación de FusedLocationProviderApi
    public LocationRequest mLocationRequest;

    public NectrasAppGPS(Activity c) {
        this.con = c;
        // Hago uso de FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(con);
        // Obtenemos actualizaciones de la ubicación del usuario
        mLocationRequest = createLocationRequest();

        // Construimos un LocationSettingsRequest
        // La clase LocationSettingsRequest.Builder extiende un Object y construye una LocationSettingsRequest.
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);


        // Verificamos la configuración de los permisos de ubicación
        checkLocationSetting(builder);

        //isActive = checkPermissions();
        //updatePosition();

    }


    /**********************Neri**********************/
    //FusedLocationProviderClient carga la ubicación más reciente del usuario en el mapa y realiza un zoom determinado a esta ubicación más reciente del usuario.
    //Para poder hacer uso de la FusedLocationProviderClient debemos agregar el paquete de servicios de ubicación de Google Play Services,
    // esto lo hacemos en el archivo build.gradle (Module: app) que se encuentra en Gradle Scripts > build.grade (Project: Module app)
    //Agregar lo siguiente en la zona de dependencias: implementation 'com.google.android.gms:play-services-location:17.0.0'

    //Con el método createLocationRequest() obtenemos actualizaciones de la ubicación del usuario
    protected LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(30000);
        mLocationRequest.setFastestInterval(10000);
        mLocationRequest.setSmallestDisplacement(30);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    private void checkLocationSetting(LocationSettingsRequest.Builder builder) {

        builder.setAlwaysShow(true);
        // Dentro de la variable 'cliente' iniciamos LocationServices, para los servicios de ubicación
        SettingsClient cliente = LocationServices.getSettingsClient(con);

        // Creamos una task o tarea para verificar la configuración de ubicación del usuario
        Task<LocationSettingsResponse> task = cliente.checkLocationSettings(builder.build());

        // Adjuntamos addOnCompleteListener a la task para gestionar si la tarea se realiza correctamente
        task.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {

            @Override
            public void onComplete(@NotNull Task<LocationSettingsResponse> task) {
                try {
                    /*LocationSettingsResponse response =*/
                    task.getResult(ApiException.class);
                    // En try podemos hacer 'algo', si la configuración de ubicación es correcta,

                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                            // La configuración de ubicación no está satisfecha.
                            // Le mostramos al usuario un diálogo de confirmación de uso de GPS.
                            try {
                                // Transmitimos a una excepción resoluble.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;

                                // Mostramos el diálogo llamando a startResolutionForResult()
                                // y es verificado el resultado en el método onActivityResult().
                                resolvable.startResolutionForResult(con, REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignora el error.
                            } catch (ClassCastException e) {
                                // Ignorar, aca podría ser un error imposible.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // La configuración de ubicación no está satisfecha podemos hacer algo.
                            break;
                    }
                }
            }
        });
    }

    private void dialogoSolicitarPermisoGPS() {
        if (ActivityCompat.checkSelfPermission(con, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(con, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(con, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 123);
        }
    }

    /************************************************/

    public void updatePosition() {
         if (ActivityCompat.checkSelfPermission( this.con, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                 ActivityCompat.checkSelfPermission(this.con, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
             dialogoSolicitarPermisoGPS();
            return;
        }
        fusedLocationClient = new FusedLocationProviderClient(con);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this.con, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            lasLocation = location;
                            SharedPreferences prefs = con.getApplicationContext().getSharedPreferences("LogData", Context.MODE_PRIVATE);
                            SharedPreferences.Editor edit = prefs.edit();
                            edit.putString("lat", String.valueOf(location.getLatitude()));
                            edit.putString("lon", String.valueOf(location.getLongitude()));
                            edit.apply();
                            //Log.e("NECTRASLOG","POSITION  "+location.toString());
                        }
                    }
                });
    }

    public LatLng getLasLocation() {
        LatLng loc=null;
        if(lasLocation!=null){
            loc=new LatLng(lasLocation.getLatitude(),lasLocation.getLongitude());
        }
        return loc;
    }
}
/*private boolean checkPermissions() {
        boolean hasPermission = (ContextCompat.checkSelfPermission(con,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);

        boolean hasPermission2 = (ContextCompat.checkSelfPermission(con,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            //ask permission
            ActivityCompat.requestPermissions(con,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
            return false;
        }
        if (!hasPermission2) {
            //ask permission
            ActivityCompat.requestPermissions(con,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
            return false;
        }
        return true;
    }*/
