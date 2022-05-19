package com.example.myapplicationmaps2.modelo;
import com.google.android.gms.maps.model.LatLng;

public class Waypoint implements Comparable<Waypoint>{
    LatLng ubicacion;
    double altitud;
    String tipo="nav"; //home, nav (navegación), obs (obstáculo), pi (perimetro interno), cr (corte de ruta)
    boolean pulverizar; // es true si tiene que abrir en ese wp o false si tiene que cerrar
    int command, param1, param2;

    public Waypoint() {

    }

    public Waypoint(LatLng ubicacion, double altitud) {
        this.ubicacion = ubicacion;
        this.altitud = altitud;
    }

    public LatLng getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(LatLng ubicacion) {
        this.ubicacion = ubicacion;
    }

    public double getAltitud() {
        return altitud;
    }

    public void setAltitud(double altitud) {
        this.altitud = altitud;
    }

    public String getTipo() {
        return tipo;
    }

    public boolean isPulverizar() {
        return pulverizar;
    }

    public void setPulverizar(boolean pulverizar) {
        this.pulverizar = pulverizar;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getCommand() {
        return command;
    }

    public void setCommand(int command) {
        this.command = command;
    }

    public int getParam1() {
        return param1;
    }

    public void setParam1(int param1) {
        this.param1 = param1;
    }

    public int getParam2() {
        return param2;
    }

    public void setParam2(int param2) {
        this.param2 = param2;
    }

    @Override
    public String toString() {
        return "Wakepoint{" +
                "ubicacion=" + ubicacion.toString() +
                ", altitud=" + altitud +
                '}';
    }


    @Override
    public int compareTo(Waypoint o) {
        double a, b;
        a = this.getUbicacion().longitude;
        b = o.getUbicacion().longitude;
        if(a<b){
            return -1;
        }else{
            return 1;
        }
    }


}
