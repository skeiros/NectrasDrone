package com.example.myapplicationmaps2.modelo;

import com.google.android.gms.maps.model.LatLng;



public class FuncionCircunferencia{
    double x2;
    double y2;
    double A;
    double B;
    double C;
    double radio_grados;
    double radio_metros;
    LatLng centro;
    double altitud;

    FuncionRecta paralelaPasadaCentroCircunfefencia;

    public FuncionCircunferencia(double radio_metros) {
        this.radio_metros=radio_metros;
    }

    public void calcularTerminos(LatLng centro, double radio){
        this.centro=centro;
        this.radio_grados =radio;
        x2=1;
        y2=1;
        A=-2*centro.longitude;
        B=-2*centro.latitude;
        C=(centro.longitude*centro.longitude)+(centro.latitude*centro.latitude)-(radio*radio);
    }

    public void cargarRectaQuePasaPorElCentro(FuncionRecta pasada){
        paralelaPasadaCentroCircunfefencia=new FuncionRecta();
        paralelaPasadaCentroCircunfefencia.setA(pasada.getA());
        paralelaPasadaCentroCircunfefencia.calcularBdeParalela(centro);
    }

    public double getX2() {
        return x2;
    }

    public void setX2(double x2) {
        this.x2 = x2;
    }

    public double getY2() {
        return y2;
    }

    public void setY2(double y2) {
        this.y2 = y2;
    }

    public double getA() {
        return A;
    }

    public void setA(double a) {
        A = a;
    }

    public double getB() {
        return B;
    }

    public void setB(double b) {
        B = b;
    }

    public double getC() {
        return C;
    }

    public void setC(double c) {
        C = c;
    }

    public double getRadio_grados() {
        return radio_grados;
    }

    public void setRadio_grados(double radio_grados) {
        this.radio_grados = radio_grados;
    }

    public LatLng getCentro() {
        return centro;
    }

    public void setCentro(LatLng centro) {
        this.centro = centro;
    }

    public FuncionRecta getParalelaPasadaCentroCircunfefencia() {
        return paralelaPasadaCentroCircunfefencia;
    }

    public void setParalelaPasadaCentroCircunfefencia(FuncionRecta paralelaPasadaCentroCircunfefencia) {
        this.paralelaPasadaCentroCircunfefencia = paralelaPasadaCentroCircunfefencia;
    }

    public double getRadio_metros() {
        return radio_metros;
    }

    public void setRadio_metros(double radio_metros) {
        this.radio_metros = radio_metros;
    }

    public double getAltitud() {
        return altitud;
    }

    public void setAltitud(double altitud) {
        this.altitud = altitud;
    }

    @Override
    public String toString() {
        return "FuncionCircunferencia{" +
                x2 +"x^2 + " +
                y2 +"y^2 + " +
                A + "x + " +
                B + "y + " +
                C + "=0" +
                "\n radio=" + radio_grados +
                ", centro=" + centro +'}';
                //", paralelaPasadaCentroCircunfefencia=" + paralelaPasadaCentroCircunfefencia.toString() +

    }


}
