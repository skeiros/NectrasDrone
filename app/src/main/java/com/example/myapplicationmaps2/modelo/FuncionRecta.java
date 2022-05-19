package com.example.myapplicationmaps2.modelo;

import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;

public class FuncionRecta {

    //Función de la recta y=ax+b
    double a;//Pendiente
    double b;//Módulo independiente
    double x_max, x_min;
    double y_max, y_min;

    //en una función recta, también puedo expresar:
    // Ax + By + C = 0
    double termino_A;
    double termino_B;
    double termino_C;

    ArrayList<Waypoint>wakepointsPasada;

    public FuncionRecta() {
        wakepointsPasada=new ArrayList<>();
    }

    public double getA() {
        return a;
    }

    public void setA(double a) {
        this.a = a;
    }

    public double getB() {
        return b;
    }

    public double getX_max() {
        return x_max;
    }

    public double getX_min() {
        return x_min;
    }

    public double getY_max() {
        return y_max;
    }

    public double getY_min() {
        return y_min;
    }

    public void calcularTerminosFuncion(LatLng point1, LatLng point2){
        a=(point2.latitude-point1.latitude)/(point2.longitude-point1.longitude);
        b=(-1*point1.longitude*(point2.latitude-point1.latitude)/(point2.longitude-point1.longitude))+point1.latitude;
        calcularMaxMin(point1,point2);
    }
    private void calcularMaxMin(LatLng point1, LatLng point2) {
        if(point1.longitude<point2.longitude){
            x_min=point1.longitude;
            x_max=point2.longitude;
        }else{
            x_min=point2.longitude;
            x_max=point1.longitude;
        }
        if(point1.latitude<point2.latitude){
            y_min=point1.latitude;
            y_max=point2.latitude;
        }else{
            y_min=point2.latitude;
            y_max=point1.latitude;
        }
    }

    public void calcularBdeParalela(LatLng point){
        //y=ax+b
        //y-ax=b
        b=point.latitude-(a*point.longitude);
    }

    @Override
    public String toString() {
        return "y=" + a +"x + " + b +"\n";
               // termino_A+"x + "+termino_B+"y + "+termino_C+" = 0";
    }

    public void addWakepointPasada(Waypoint point, int indice){
        wakepointsPasada.add(indice, point);
    }
    public void addWakepointPasada(Waypoint point){
        wakepointsPasada.add(point);
    }

    public ArrayList<Waypoint> getWakepointsPasada(){
        return wakepointsPasada;
    }

    /* public void calcularABC() {
        //Tengo la distancia , tengo la pendiente y el termino independiente.
        //Calculo A, B y C para expresar la formula diferente.
        termino_A=1;
        termino_B=1/getA();
        termino_C=getB()/getA();
    }

    public void calcularCconDistancia(double distancia, double B_original, double C_original, boolean suma) {
        //Teniendo A, B y C de la paralela principal, obtengo C de una paralela a la distancia correspondiente
        termino_A=1;
        termino_B=B_original;
        //Partiendo de la formula de distancia: d=C-C'/RaizCuadrada(A^2+B^2)
        //Obtengo mi C': C'= C - (d * RaizCuadrada(A^2+B^2))
        if(suma) {
            termino_C = C_original + (distancia * Math.sqrt(1 + (termino_B * termino_B)));
        }else{
            termino_C = C_original - (distancia * Math.sqrt(1 + (termino_B * termino_B)));
        }
    }

    public void calcularTerminoIndependienteSegunABC(){
        // Ax + By + C = 0
        // Ax + C = -By
        // (Ax + C)/-B = y
        // A/-B x + C/-B =y
        // a=A/-B; b=C/-B;
        // a=-1*termino_A/termino_B;Como la pendiente es la misma siempre, la seteo igual a la de la paralela principal desde funciones
        b=(termino_C/termino_B);
        Log.d("CALCULAR ", "TerminoIndependiente segun ABC:  Termino independiente="+termino_C+"/-"+termino_B+"="+b);
    }

   public double  nuevaPendienteFuncionRotada(int angulo){
        double anguloAnterior= Math.atan(*//*Math.toRadians(a)*//*a);
        double nuevoAngulo= Math.toRadians(angulo)+anguloAnterior;
        Log.d("ANGULOS","ANGULO BASE: "+Math.toDegrees(anguloAnterior)+", ANGULO NUEVO: "+Math.toDegrees(nuevoAngulo)+", PENDIENTE NUEVA: "+a);
        return Math.tan(nuevoAngulo);
    }*/
}
