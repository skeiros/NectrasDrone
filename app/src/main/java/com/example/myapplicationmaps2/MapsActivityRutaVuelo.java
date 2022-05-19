package com.example.myapplicationmaps2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;

import com.example.myapplicationmaps2.controladores.Funciones;
import com.example.myapplicationmaps2.controladores.PolygonUtils;
import com.example.myapplicationmaps2.databinding.ActivityMapsRutaVueloBinding;
import com.example.myapplicationmaps2.modelo.Poligono;
import com.example.myapplicationmaps2.modelo.Waypoint;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MapsActivityRutaVuelo extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Funciones f;
    private int /*altura_vuelo,*/ ancho_botalon;
    private LatLng home_point;
    public ArrayList<LatLng> perimeterPoints, perimetroInternoPoints ;
    public ArrayList<Double> altitud_perimeterPoints, altitud_perimetroInternoPoints;
    private ArrayList<LatLng> ubicacion_obstaculos;
    private ArrayList<Integer> radio_obstaculos;
    private ArrayList<Waypoint> waypoints;
    private Poligono perimetroInterno;
    private Polyline lineasRuta, lineasPerimetroInterno, lineasRutaSinPulverizar;
    private Polyline LineasPerimetro;
    private boolean esquivar_perimetro_interno;
    Button btn_volver, btn_exportar, btn_girar;
    EditText editText_angulo;
    String textoRuta="";

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.example.myapplicationmaps2.databinding.ActivityMapsRutaVueloBinding binding = ActivityMapsRutaVueloBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        btn_exportar=findViewById(R.id.btn_exportar);
        btn_volver=findViewById(R.id.btn_back);
        btn_girar=findViewById(R.id.btn_girar);
        editText_angulo=findViewById(R.id.editTextNumber);
        btn_exportar.setOnClickListener(v -> {
            textoRuta=f.generarTxt();
            guardarConSelectorDeArchivos();
            f.calcularDistanciaTotalRecorrido();
        });
        btn_volver.setOnClickListener(v -> finish());
        btn_girar.setOnClickListener(v -> {
            int angulo = Integer.parseInt(editText_angulo.getText().toString());
            clearMap();
            f.girarFunciones(angulo);
            waypoints =f.getWakepoints();
            onResume();
        });

        SharedPreferences pref = getSharedPreferences("config_dron", Context.MODE_PRIVATE);
        ancho_botalon = pref.getInt("ancho_botalon", 3);
        esquivar_perimetro_interno=pref.getBoolean("esquivar_pi",false);
       // altura_vuelo = pref.getInt("altura_vuelo", 5);



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);


        //onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        perimeterPoints = new ArrayList<>();
        ubicacion_obstaculos = new ArrayList<>();
        radio_obstaculos = new ArrayList<>();
        perimeterPoints = (ArrayList<LatLng>) getIntent().getSerializableExtra("puntosPerimetro");
        altitud_perimeterPoints=(ArrayList<Double>)getIntent().getSerializableExtra("altitudPuntosPerimetro");

        perimetroInternoPoints = (ArrayList<LatLng>) getIntent().getSerializableExtra("perimetroInterno");
        altitud_perimetroInternoPoints = (ArrayList<Double>)getIntent().getSerializableExtra("altitudPerimetroInterno");

        Log.e("AREA POLIGONO", "El area del poligono delimitado es "+ PolygonUtils.computeArea(perimeterPoints)+"m2");
        ubicacion_obstaculos = (ArrayList<LatLng>)getIntent().getSerializableExtra("uobstaculos");
        radio_obstaculos=(ArrayList<Integer>)getIntent().getSerializableExtra("robstaculos");
        home_point = new LatLng(getIntent().getDoubleExtra("homePoint_lat",-1), getIntent().getDoubleExtra("homePoint_long",-1));
        double altitud_home=getIntent().getDoubleExtra("homePoint_altitud",0);
        ArrayList<Double>altitud_obstaculos=(ArrayList<Double>)getIntent().getSerializableExtra("aobstaculos");
        Log.d("NECTRAS DRON", "Home point:"+home_point.toString());
        Log.d("NECTRAS DRON", "Bienvenidos a la app para generar rutas de vuelo. Ancho botalon:"+ancho_botalon+"cantidad puntos perimetro="+perimeterPoints.size()+"Esquivar perimetro interno es "+esquivar_perimetro_interno);

        f = new Funciones(perimeterPoints,altitud_perimeterPoints, ancho_botalon, home_point,altitud_home,ubicacion_obstaculos,radio_obstaculos,altitud_obstaculos, perimetroInternoPoints, altitud_perimetroInternoPoints, esquivar_perimetro_interno);
        waypoints =f.getWakepoints();
        perimetroInterno = f.getPerimetroInterno();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.moveCamera(CameraUpdateFactory
                .newLatLngZoom(home_point, 15));
        mMap.getUiSettings().setZoomControlsEnabled(true);

        marcarObstaculos();
        drawPolilinesPoligonoInterno();
        createPerimterLines();
        marcarRuta();
        drawPolilinesRuta();
        //adaptarZoomListaDeSensores ();
    }

    private void drawPolilinesPoligonoInterno() {
        try {
            if(perimetroInterno.getPerimeterPoints().size()>2){
                ArrayList<LatLng> points2 = new ArrayList<>();
                for(int i = 0; i< perimetroInterno.getPerimeterPoints().size(); i++){
                    points2.add(perimetroInterno.getPerimeterPoints().get(i).getUbicacion());
                }
                points2.add(perimetroInterno.getPerimeterPoints().get(0).getUbicacion());
                if(lineasPerimetroInterno==null){

                    lineasPerimetroInterno = mMap.addPolyline(new PolylineOptions()
                            .clickable(false)
                            .color(Color.DKGRAY)
                            .width(10)
                    );

                }
                lineasPerimetroInterno.setPoints(points2);
            }else{
                if(lineasPerimetroInterno!=null){
                    lineasPerimetroInterno.remove();
                    lineasPerimetroInterno=null;
                }
            }
        }catch (java.lang.NullPointerException ignored){}
    }

    private void marcarObstaculos() {
        int myColor = getResources().getColor(R.color.gray);
        for(int i=0; i<ubicacion_obstaculos.size();i++){
            //mMap.addMarker(new MarkerOptions().position(obstaculo).title("Obstáculo").icon(icon));
            mMap.addCircle(new CircleOptions()
                    .center(ubicacion_obstaculos.get(i))
                    .radius(radio_obstaculos.get(i))
                    .fillColor(myColor));
        }
    }

    public void drawPolilinesRuta(){
        Log.e("NECTRASLOG", "CREANDO ROUTE POINTS "+ waypoints.size());
        try {
        if(waypoints.size()>2){
            ArrayList<LatLng> points2 = new ArrayList<>();
            ArrayList<LatLng> points3 = new ArrayList<>();
            for(int i = 0; i< waypoints.size(); i++){
                if(waypoints.get(i).getTipo().equals("nopulv")){
                    points3.add(waypoints.get(i).getUbicacion());
                }else{
                    points2.add(waypoints.get(i).getUbicacion());
                }

            }
            points2.add(waypoints.get(0).getUbicacion());
            if(lineasRuta==null){

                    lineasRuta = mMap.addPolyline(new PolylineOptions()
                            .clickable(false)
                            .color(Color.RED)
                            .width(10)
                    );
                    lineasRutaSinPulverizar= mMap.addPolyline(new PolylineOptions()
                            .clickable(false)
                            .color(Color.YELLOW)
                            .width(10)
                    );

            }
            lineasRuta.setPoints(points2);
            lineasRutaSinPulverizar.setPoints(points3);
        }else{
            if(lineasRuta!=null){
                lineasRuta.remove();
                lineasRuta=null;
                lineasRutaSinPulverizar.remove();
                lineasRutaSinPulverizar=null;
            }
        }
        }catch (java.lang.NullPointerException ignored){}
    }

    public void marcarRuta(){
        Log.e("NECTRASLOG", "ENTRE A DIBUJAR WAKEPOINTS ");
        try{
            for(int i = 1; i< waypoints.size(); i++){

                MarkerOptions m=new MarkerOptions();
                m.position(waypoints.get(i).getUbicacion());
                m.draggable(false);
                m.title("Punto "+i);
                mMap.addMarker(m);
            }
            //adaptarZoomListaDeSensores();
        }catch (java.lang.NullPointerException ex){
            Log.e("NECTRASLOG", "no pude cargar el mapar");

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try{
            drawPolilinesPoligonoInterno();
        }catch(java.lang.NullPointerException ignored){

        }
        createPerimterLines();
        marcarRuta();
        drawPolilinesRuta();
        try{
            marcarObstaculos();
        }catch (java.lang.NullPointerException ignored){

        }
    }

    private void clearMap(){
        waypoints.clear();
        waypoints =new ArrayList<>();
        lineasRuta=null;
        mMap.clear();
    }

    // Request code for creating a PDF document.
    private static final int PICKFILE_RESULT_CODE = 1;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void guardarConSelectorDeArchivos(){
        Format formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String nombre = "Mission "+formatter.format(new Date())+".txt";
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/txt");
        intent.putExtra(Intent.EXTRA_TITLE, nombre);
        startActivityForResult(intent, PICKFILE_RESULT_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // First we need to check if the requestCode matches the one we used.
        if(requestCode == PICKFILE_RESULT_CODE) {
            if(resultCode == Activity.RESULT_OK) {
                Uri uri = data.getData();
                String filePath = uri.getPath();
                ParcelFileDescriptor pfd;
                try {
                    pfd = this.getContentResolver().openFileDescriptor(uri, "w");
                    FileOutputStream fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());
                    fileOutputStream.write(textoRuta.getBytes());
                    // Let the document provider know you're done by closing the stream.
                    fileOutputStream.close();
                    pfd.close();
                    Toast.makeText(this, "Archivo editado y almacenado en"+filePath,Toast.LENGTH_LONG).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                Toast.makeText(this, "ERROR AL SELECCIONAR UBICACION",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    public void createPerimterLines() {
        try{
        ArrayList<LatLng> point2 = new ArrayList<>();
        if (perimeterPoints.size() > 2) {
            point2.addAll(perimeterPoints);
            point2.add(perimeterPoints.get(0));
            if (LineasPerimetro == null) {
                LineasPerimetro = mMap.addPolyline(new PolylineOptions()
                        .clickable(false)
                        .color(Color.GREEN)
                        .width(5)
                );
            }
            LineasPerimetro.setPoints(point2);
        } else {
            if (LineasPerimetro != null) {
                LineasPerimetro.remove();
                LineasPerimetro = null;
            }
        }
        }catch (java.lang.NullPointerException ignored){}
    }

}