package com.example.myapplicationmaps2.modelo;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class Poligono {

    public ArrayList<Waypoint> perimeterPoints;
    public ArrayList<FuncionRecta> funcionesRectasPerimetro;
    public ArrayList<Double> distancia_entre_puntos;
    double RADIO_TIERRA = 6371000;

    public Poligono (){
        perimeterPoints=new ArrayList<>();
        funcionesRectasPerimetro=new ArrayList<>();
        distancia_entre_puntos=new ArrayList<>();
    }

    public ArrayList<Waypoint> getPerimeterPoints() {
        return perimeterPoints;
    }

    public void setPerimeterPoints(ArrayList<Waypoint> perimeterPoints) {
        this.perimeterPoints = perimeterPoints;
    }

    public ArrayList<FuncionRecta> getFuncionesRectasPerimetro() {
        return funcionesRectasPerimetro;
    }

    public ArrayList<Double> getDistancia_entre_puntos() {
        return distancia_entre_puntos;
    }

    public void addPerimeterPoint(Waypoint w){
        this.perimeterPoints.add(w);
    }

    public void cargarFuncionesRectas(){
        FuncionRecta fx;
        if(perimeterPoints.size()>2){
            perimeterPoints.add(perimeterPoints.get(0));
            for(int i=1; i<perimeterPoints.size();i++){
                fx = new FuncionRecta();
                fx.calcularTerminosFuncion(perimeterPoints.get(i-1).getUbicacion(),
                        perimeterPoints.get(i).getUbicacion());
                funcionesRectasPerimetro.add(fx);
                Log.d("FX POLIGONO", fx.toString());
                distancia_entre_puntos.add(calculardistancia(perimeterPoints.get(i-1).getUbicacion(),perimeterPoints.get(i).getUbicacion()));
            }
        }
    }

    public double calculardistancia(LatLng pointa, LatLng pointb) {
        //Multiplico por pi y divido por 180 para convertir a radianes
        double lat1=(pointa.latitude)*Math.PI/180;
        double lat2=(pointb.latitude)*Math.PI/180;
        double lon1=(pointa.longitude)*Math.PI/180;
        double lon2=(pointb.longitude)*Math.PI/180;
        double parteA = (Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1)) + (Math.sin(lat1) * Math.sin(lat2));
        //Radio de la tierra en km, en los polos=6356,8, en el ecuador=6378,1, promedio=6371
        double distancia = (RADIO_TIERRA * Math.acos(parteA));
        if (Double.isNaN(distancia)) {
            distancia = 0;
        }
        //Log.d("dist", "distancia"+distancia);
        return distancia;
    }
}
