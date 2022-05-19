package com.example.myapplicationmaps2.controladores;

import android.util.Log;
import com.example.myapplicationmaps2.modelo.FuncionCircunferencia;
import com.example.myapplicationmaps2.modelo.FuncionRecta;
import com.example.myapplicationmaps2.modelo.Poligono;
import com.example.myapplicationmaps2.modelo.Waypoint;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collections;

public class Funciones {

    ArrayList<Waypoint> puntosPoligonoCampo;
    ArrayList<FuncionRecta> funcionesRectasPoligonos;
    ArrayList<Double>distancia_puntos_poligono;
    FuncionRecta paralelaBaseEnLimite;
    LatLng limiteParalelaBase;
    double anchoBotalonMetros;
    ArrayList<FuncionRecta> funcionesParalelas;
    ArrayList<FuncionCircunferencia> funcionesObstaculos;
    ArrayList<Waypoint> wayPoints, wayPointsOrder, wayPointsFinal;
    Poligono perimetroInterno;
    double maxLat, minLat, maxLon, minLon;
    boolean maxmax, haciaArriba; //esta variable es true si las paralelas van hacia la izquierda abajo(tengo que sumar), y false si van a la derecha arriba (tengo que restar)
    double RADIO_TIERRA = 6371000;
    double angulo, angulo_perpendicular, angulo_perp_rad,angulo0_360;
    boolean estoyGirando=false;
    Waypoint homePoint;
    private boolean de_izquierda_a_derecha;
    private final boolean esquivar_perimetro_interno;
    private final int DISTANCIA_CORTE_RUTA_COMBUSTIBLE =1000;//1000 metros
    //private final int DISTANCIA_CORTE_RUTA_LIQUIDO_PULV =500;//500 metros
    boolean copiarInvertido=false;



    public Funciones(ArrayList<LatLng> puntosPerimetro, ArrayList<Double>altitud_puntosPerimetro,
                     int ancho_botalon, LatLng home, double altitud_home,
                     ArrayList<LatLng> ubicacion_obstaculos, ArrayList<Integer>radio_obstaculos,
                     ArrayList<Double>altitud_obstaculos,
                     ArrayList<LatLng> puntosPerimetroInterno, ArrayList<Double>altitud_puntosPerimetroInterno,
                     boolean esquivar_pi) {
        Log.d("INICIO FUNCIONES", "Inicializo puntos poligono campo con los puntos marcados");
        esquivar_perimetro_interno=esquivar_pi;
        homePoint=new Waypoint(home, altitud_home);//Guardo el punto que corresponde al home para usar mas adelante.
        homePoint.setTipo("home");
        generarWakepointPuntosPerimetro(puntosPerimetro,altitud_puntosPerimetro);
        anchoBotalonMetros=ancho_botalon;//20;
        crearCircunferenciasObstaculos(ubicacion_obstaculos,radio_obstaculos, altitud_obstaculos);
        cargarPerimetroInterno(puntosPerimetroInterno, altitud_puntosPerimetroInterno);
        inicializar();
    }

    private void generarWakepointPuntosPerimetro(ArrayList<LatLng> puntosPerimetro, ArrayList<Double> altitud_puntosPerimetro) {
        puntosPoligonoCampo=new ArrayList<>();
        for (int i=0; i<puntosPerimetro.size();i++) {
            puntosPoligonoCampo.add(new Waypoint(puntosPerimetro.get(i),altitud_puntosPerimetro.get(i) ));
        }
    }

    private void crearCircunferenciasObstaculos(ArrayList<LatLng> ubicacion_obstaculos, ArrayList<Integer>radio_obstaculos, ArrayList<Double>altitud_obstaculos) {
        FuncionCircunferencia fc;
        funcionesObstaculos = new ArrayList<>();
        for(int i=0; i<ubicacion_obstaculos.size();i++){
            fc=new FuncionCircunferencia(radio_obstaculos.get(i));
            fc.calcularTerminos(ubicacion_obstaculos.get(i),give_grades_from_meters_lng(fc.getRadio_metros(),ubicacion_obstaculos.get(i).latitude));
            fc.setAltitud(altitud_obstaculos.get(i));
            funcionesObstaculos.add(fc);
        }
    }

    private void cargarPerimetroInterno(ArrayList<LatLng> points, ArrayList<Double> altitud){
        perimetroInterno= new Poligono();
        Waypoint w;
        for (int i=0; i<points.size();i++){
            w=new Waypoint(points.get(i),altitud.get(i));
            perimetroInterno.addPerimeterPoint(w);
        }
        perimetroInterno.cargarFuncionesRectas();
    }

    public void inicializar(){
        Log.d("NECTRAS DRON", "Inicialización de variables de prueba");
        calcularFuncionesPoligono();
        calcularLimites();
        calcularAngulos();
        calcularParalelaBaseEnLimite();
        calcularParalelas();
    }

    public void calcularLimites(){
        maxLat=puntosPoligonoCampo.get(0).getUbicacion().latitude;
        minLat=puntosPoligonoCampo.get(0).getUbicacion().latitude;
        maxLon=puntosPoligonoCampo.get(0).getUbicacion().longitude;
        minLon=puntosPoligonoCampo.get(0).getUbicacion().longitude;
        double lat, lon;
        for(int i=1; i<puntosPoligonoCampo.size();i++){
            lat = puntosPoligonoCampo.get(i).getUbicacion().latitude;
            lon = puntosPoligonoCampo.get(i).getUbicacion().longitude;
            if(lat>maxLat){
                maxLat=lat;
            }else{
                if(lat<minLat){
                    minLat=lat;
                }
            }
            if(lon>maxLon){
                maxLon=lon;
            }else{
                if(lon<minLon){
                    minLon=lon;
                }
            }
        }
        Log.d("LIMITES LON", "LON MAX="+maxLon+", LON MIN="+minLon);
        Log.d("LIMITES LAT", "LAT MAX="+maxLat+", LAT MIN="+minLat);
    }

    public void calcularAngulos(){
        //double difLat=puntosPoligonoCampo.get(0).getUbicacion().latitude-puntosPoligonoCampo.get(1).getUbicacion().latitude;
        //double difLong=puntosPoligonoCampo.get(0).getUbicacion().longitude-puntosPoligonoCampo.get(1).getUbicacion().longitude;
        //angulo=(Math.toDegrees(Math.atan2(difLat,difLong)));
        angulo=Math.toDegrees(Math.atan(funcionesRectasPoligonos.get(0).getA()));//*180/Math.PI);
        Log.d("ANGULO", ""+angulo);
        if(angulo<0){
            angulo0_360=360+angulo;
        }
        angulo_perpendicular=angulo+90;
        /*if(angulo_perpendicular>360){
            angulo_perpendicular-=360;
        }*/
        angulo_perp_rad=Math.toRadians(angulo_perpendicular);
        Log.d("ANGULO", "ANGULO: "+angulo+", ANGULO PERPENDICULAR: "+angulo_perpendicular+", Angulo perpendicular en radianes: "+angulo_perp_rad);
    }

    public void calcularParalelaBaseEnLimite(){
        paralelaBaseEnLimite=new FuncionRecta();
        if(estoyGirando){
            paralelaBaseEnLimite.setA(Math.tan(Math.toRadians(angulo)));
            estoyGirando=false;
        }else{
            paralelaBaseEnLimite.setA(funcionesRectasPoligonos.get(0).getA());
        }
        if(angulo0_360<90){
            //angulo entre 0 y 90°
            limiteParalelaBase=new LatLng(maxLat, minLon);
            maxmax=false;
        }else{
            if(angulo0_360<180){
                //angulo entre 90 y 180°
                limiteParalelaBase=new LatLng(maxLat, maxLon);
                maxmax=true;
            }else{
                if(angulo0_360<270){
                    //angulo entre 180 y 270°
                    limiteParalelaBase=new LatLng(maxLat, minLon);
                    maxmax=false;
                }else{
                    //ángulo entre 270 y 360°
                    limiteParalelaBase=new LatLng(maxLat, maxLon);
                    maxmax=true;
                }
            }
        }
        if(maxmax){
            //si la recta es tirando a horizontal, me fijo si va de dcha a izda. o viceversa
            de_izquierda_a_derecha =(puntosPoligonoCampo.get(0).getUbicacion().longitude < puntosPoligonoCampo.get(1).getUbicacion().longitude);
        }else{
            //si la recta es semi vertical, me fijo si va hacia abajo o hacia arriba la 1° pasada
            haciaArriba= !(puntosPoligonoCampo.get(0).getUbicacion().latitude > puntosPoligonoCampo.get(1).getUbicacion().latitude);
        }

        paralelaBaseEnLimite.calcularBdeParalela(limiteParalelaBase);
        Log.d("LIMITE DONDE CORRERSE", (maxmax)?"max max":"min lat, max long");
        Log.d("PARALELA BASE LIMITE", "LIMITE: "+limiteParalelaBase.toString()+"\n PARALELA BASE CORRIDA AL LIMITE: "+paralelaBaseEnLimite.toString());
    }

    public void calcularFuncionesPoligono(){
        funcionesRectasPoligonos = new ArrayList<>();
        distancia_puntos_poligono = new ArrayList<>();
        FuncionRecta f1;
        puntosPoligonoCampo.add(puntosPoligonoCampo.get(0));

        Log.d("PUNTO POLÍGONO", "POINT "+0+": Punto({"+puntosPoligonoCampo.get(0).getUbicacion().longitude+","+puntosPoligonoCampo.get(0).getUbicacion().latitude+"})");
        for(int i=0; i<puntosPoligonoCampo.size()-1;i++){
            Log.d("PUNTO POLÍGONO", "POINT "+(i+1)+": Punto({"+puntosPoligonoCampo.get(i+1).getUbicacion().longitude+","+puntosPoligonoCampo.get(i+1).getUbicacion().latitude+"})");
            f1 = new FuncionRecta();
            f1.calcularTerminosFuncion(puntosPoligonoCampo.get(i).getUbicacion(), puntosPoligonoCampo.get(i+1).getUbicacion());
            funcionesRectasPoligonos.add(f1);
            distancia_puntos_poligono.add(calculardistancia(puntosPoligonoCampo.get(i).getUbicacion(),puntosPoligonoCampo.get(i+1).getUbicacion()));
            Log.d("FUNCION POLIGONO", " F"+i+": "+f1.toString());
        }
    }

    public void calcularParalelas(){
        funcionesParalelas = new ArrayList<>();
        wayPoints =new ArrayList<>();
        FuncionRecta fx;
        double lat2,lon2;
        LatLng point2;
        boolean hayInterseccion=true, darVuelta=false;
        int contador=1;
        double inc;
        double incLong;

        while (hayInterseccion){
            inc= (anchoBotalonMetros*contador*360)/(2*Math.PI*RADIO_TIERRA);
            incLong=inc/(Math.cos(Math.toRadians(limiteParalelaBase.latitude)));
            Log.d("INC", "INC="+inc+", IncLong="+incLong);
            fx=new FuncionRecta();
            //Le seteo la misma pendiente que la función del polígono de los puntos AB
            fx.setA(paralelaBaseEnLimite.getA());

            //Calculo el punto de latitud corrido en el ancho de botalón, en dirección a la perpendicular d la paralela base
            if(darVuelta){
                lat2=(maxmax)?limiteParalelaBase.latitude+inc*Math.sin(angulo_perp_rad):limiteParalelaBase.latitude-inc*Math.sin(angulo_perp_rad);
                lon2=(maxmax)?limiteParalelaBase.longitude+incLong*Math.cos(angulo_perp_rad):limiteParalelaBase.longitude-incLong*Math.cos(angulo_perp_rad);
            }else{
                lat2=(maxmax)?limiteParalelaBase.latitude-inc*Math.sin(angulo_perp_rad):limiteParalelaBase.latitude+inc*Math.sin(angulo_perp_rad);
                lon2=(maxmax)?limiteParalelaBase.longitude-incLong*Math.cos(angulo_perp_rad):limiteParalelaBase.longitude+incLong*Math.cos(angulo_perp_rad);
            }
            point2 = new LatLng(lat2,lon2);
            Log.d("PUNTO LAT CORRIDO", "Point original: "+limiteParalelaBase.toString()+", Point corrido: "+point2.toString());
            calculardistancia(puntosPoligonoCampo.get(0).getUbicacion(),point2);

            //Calculo la paralela a AB que pasa por point (lat2, long2)
            fx.calcularBdeParalela(point2);
            funcionesParalelas.add(fx);
            int indice_fx=funcionesParalelas.size()-1;
            Log.d("FUNCION PARALELA", " PARALELA F"+contador+": "+fx.toString());
            contador++;
            hayInterseccion=comprobarIntersecciones(indice_fx);
            if(hayInterseccion){
            //Me fijo sit hay algún obstáculo que interseque con esta paralela
                comprobarObsaculos(indice_fx);
            //Me fijo si hay algún perímetro interno que interseque con esta pasada
                //comprobarPerimetrosInternos(indice_fx);
            }else{
                //si no hay interseccion elimino la funcion de las pasadas
                funcionesParalelas.remove(fx);
                if(!darVuelta){
                    //Calculo el punto de latitud corrido en el ancho de botalón, en direccion opuesta a lo que lo calcule arriba
                    lat2=(maxmax)?limiteParalelaBase.latitude+inc*Math.sin(angulo_perp_rad):limiteParalelaBase.latitude-inc*Math.sin(angulo_perp_rad);
                    lon2=(maxmax)?limiteParalelaBase.longitude+incLong*Math.cos(angulo_perp_rad):limiteParalelaBase.longitude-incLong*Math.cos(angulo_perp_rad);
                    point2 = new LatLng(lat2,lon2);
                    Log.d("PUNTO LAT CORRIDO", "Point original: "+limiteParalelaBase.toString()+", Point corrido inv: "+point2.toString());
                    //Calculo la paralela a AB que pasa por point (lat2, long2)
                    fx.calcularBdeParalela(point2);
                    Log.d("FUNCION PARALELA", " PARALELA F"+contador+": "+fx.toString());
                    funcionesParalelas.add(fx);
                    hayInterseccion=comprobarIntersecciones(indice_fx);
                    if(hayInterseccion){
                        //CAMBIAR CALCULO PUNTO CORRIDO
                        darVuelta=true;
                        //Me fijo sit hay algún obstáculo que interseque con esta paralela
                        comprobarObsaculos(indice_fx);
                    }else {
                        if (contador < 16) {
                            hayInterseccion = true;
                        }
                    }
                }
                if (contador < 16) {
                    hayInterseccion = true;
                }
            }
        }
        ordenarPuntos();
    }

    private boolean comprobarIntersecciones(int indice_fx) {
        boolean resultado=false;
        double longitudInterseccion, latitudInterseccion;
        FuncionRecta fPoligono;
        FuncionRecta fParalela=funcionesParalelas.get(indice_fx);
        for(int j=0; j<funcionesRectasPoligonos.size();j++){
            Log.e("FUNCION POLIGONO","Función "+j+"***********************");
            //Comparo cada recta del polígono con la paralela en cuestión
            fPoligono=funcionesRectasPoligonos.get(j);
            longitudInterseccion=(fPoligono.getB()-fParalela.getB())/(fParalela.getA()-fPoligono.getA());
            if(longitudInterseccion<=fPoligono.getX_max()&&longitudInterseccion>=fPoligono.getX_min()) {
                //El punto de intersección está dentro de los límites x del polígono, calculo y
                latitudInterseccion = fParalela.getA() * longitudInterseccion + fParalela.getB();
                if (latitudInterseccion <= fPoligono.getY_max() && latitudInterseccion >= fPoligono.getY_min()) {
                    //El punto está dentro de los límites y del polígono , lo guardo
                    Waypoint w = new Waypoint();
                    w.setUbicacion(new LatLng(latitudInterseccion, longitudInterseccion));
                    //CALCULO LA ALTITUD DEL WAKEPOINT CORRESPONDIENTE A LA INTERSECCIÓN
                    double dist_punto_poligono_punto_interseccion=calculardistancia(puntosPoligonoCampo.get(j).getUbicacion(), w.getUbicacion());
                    double porcentaje_respecto_dist_total=dist_punto_poligono_punto_interseccion*100/distancia_puntos_poligono.get(j);
                    double delta_h=puntosPoligonoCampo.get(j+1).getAltitud()-puntosPoligonoCampo.get(j).getAltitud();
                    double altitud_point=puntosPoligonoCampo.get(j).getAltitud()+(porcentaje_respecto_dist_total*delta_h/100);
                    w.setAltitud(altitud_point);
                    funcionesParalelas.get(indice_fx).addWakepointPasada(w);
                    resultado = true;
                    Log.d("NECTRAS INTERSECCIONES", "INTERSECA CON FUNCION POLIGONO " + j + " EN: " + latitudInterseccion + " ; " + longitudInterseccion);
                }
            }
        }
        return resultado;
    }

    private void comprobarObsaculos(int indice_fx) {
        Log.d("COMPROBAR OBST", "ENTRE A COMPROBAR OBSTACULOS, CON "+funcionesObstaculos.size()+" funciones de obstaculos cargadas");
        double parte_a, parte_b, parte_c, x1, x2, y1, y2, sqrt;
        LatLng intersec1, intersec2 = null;
        FuncionRecta fx = funcionesParalelas.get(indice_fx);

        //Para buscar la intersección, tengo que buscar la solución al sistema de ecuaciones
        //   | ax + b = y
        //   | x^2 + y^2 + Ax + By + C = 0
        for(int i=0; i<funcionesObstaculos.size();i++){
            FuncionCircunferencia fo=funcionesObstaculos.get(i);
            Log.d("FUNCION CIRCUNF", fo.toString());
            //1° obtengo una cuadrática remplazando y en la ecuacion de la circunferencia
            parte_a=1+(fx.getA()*fx.getA());
            parte_b=(2*fx.getA()*fx.getB())+fo.getA()+(fo.getB()* fx.getA());
            parte_c=(fx.getB()*fx.getB())+(fo.getB()*fx.getB())+fo.getC();
            //2° resuelvo la cuadratica y obtengo los dos x de interseccion
            //formula de la cuadrática:
            //(-b+-RaizCuadrada(b^2-4*a*c))/2*a
            sqrt = Math.sqrt(Math.pow(parte_b, 2) - 4 * parte_a * parte_c);
            x1=(-parte_b+ sqrt)/(2*parte_a);
            x2=(-parte_b- sqrt)/(2*parte_a);

            if(x1<maxLon&&x1>minLon){      
                //3°, si los x están dnetro del limite obtengo y1 reemplazando en la función lineal
                y1=fx.getA()*x1+fx.getB();
                intersec1=new LatLng(y1,x1);                
                Log.d("OBSTACULO INTERSEC","INTERSECCION CIRCUNFERENCIA - LINEAL: I1="+intersec1.toString());

                if(x2<maxLon&&x2>minLon){
                    //si los x2 estan dentro de los límites, obtengo y2
                    y2=fx.getA()*x2+fx.getB();
                    intersec2=new LatLng(y2,x2);

                    //calculo el angulo de los puntos que intersecan 
                    double angulo_1= getAnguloPuntoInterseccion(intersec1, fo);
                    double angulo_2= getAnguloPuntoInterseccion(intersec2, fo);

                   // boolean interseccion_invertidos=false;
                    double angulo,diferencia, incremento,diferencia_por_el_otro_lado;

                    Log.d("INDICES", "CANTIDAD WAKEPOINT PASADA="+fx.getWakepointsPasada().size());
                    int indice= fx.getWakepointsPasada().size()-1;
                    Log.e("ANGULOSCIRCUNFERENCIA2", "Angulo 1="+angulo_1+", Angulo 2="+angulo_2);

                    if(angulo_1>angulo_2){
                        diferencia=angulo_1-angulo_2;
                        diferencia_por_el_otro_lado=360-diferencia;
                        if(diferencia<diferencia_por_el_otro_lado){
                            Log.e("ANGULOSCIRCUNFERENCIA3", "Voy de "+angulo_2+" a "+angulo_1);

                            incremento=diferencia/5;
                            angulo=angulo_2+incremento;
                        }else{
                            Log.e("ANGULOSCIRCUNFERENCIA3", "Voy de "+angulo_1+" a "+angulo_2);

                            incremento=diferencia_por_el_otro_lado/5;
                            angulo=angulo_1+incremento;
                            if(angulo>360){
                                angulo=angulo-360;
                            }
                        }
                        Waypoint w1 = new Waypoint(intersec2,fo.getAltitud());
                        w1.setTipo("obs");
                        funcionesParalelas.get(indice_fx).addWakepointPasada(w1, indice);
                        indice++;
                        Waypoint w2 = new Waypoint(intersec1,fo.getAltitud());
                        w2.setTipo("obs");
                        funcionesParalelas.get(indice_fx).addWakepointPasada(w2, indice);
                    }else{
                        diferencia=angulo_2-angulo_1;
                        diferencia_por_el_otro_lado=360-diferencia;
                        if(diferencia<diferencia_por_el_otro_lado){
                            Log.e("ANGULOSCIRCUNFERENCIA3", "Voy de "+angulo_2+" a "+angulo_1);

                            incremento=diferencia/5;
                            angulo=angulo_1+incremento;
                        }else{
                            Log.e("ANGULOSCIRCUNFERENCIA3", "Voy de "+angulo_1+" a "+angulo_2);

                            incremento=diferencia_por_el_otro_lado/5;
                            angulo=angulo_2+incremento;
                            if(angulo>360){
                                angulo=angulo-360;
                            }
                        }
                        Waypoint w1 = new Waypoint(intersec1,fo.getAltitud());
                        funcionesParalelas.get(indice_fx).addWakepointPasada(w1, indice);
                        w1.setTipo("obs");
                        indice++;
                        Waypoint w2 = new Waypoint(intersec2,fo.getAltitud());
                        w2.setTipo("obs");
                        funcionesParalelas.get(indice_fx).addWakepointPasada(w2, indice);
                    }
                    for (int k=0; k<4;k++){
                        //agrego wakepoints a la ruta siguiendo la formula de la circunferencia
                        funcionesParalelas.get(indice_fx).addWakepointPasada(generarWakepoint(angulo, fo),indice);
                        angulo+=incremento;
                        indice++;
                    }
                }
                assert intersec2 != null;
                Log.d("OBSTACULO INTERSEC","INTERSECCION CIRCUNFERENCIA - LINEAL: I2="+intersec2.toString());
                }
            }
        }

        private void comprobarPerimetrosInternos(int indice_fx){
        Log.d("Controlo PI", "Llamo a controlar Perimetros Internos para la pasada "+indice_fx);
            FuncionRecta fx_pasada = funcionesParalelas.get(indice_fx);
            double longitudInterseccion, latitudInterseccion;
            ArrayList<Integer>rectasInterseccion=new ArrayList<>();
            ArrayList<Waypoint>matriz_puntos_pi_int=new ArrayList<>();

            if(esquivar_perimetro_interno){
                //tengo que ver como genero los wakepoint para esquivar el perímetro en lugar de solo cortar la pulverización
                ArrayList <Waypoint> alWakepointsPi=new ArrayList<>();
                for(int i=0; i<perimetroInterno.getFuncionesRectasPerimetro().size();i++){
                    Log.d("INTERSEC PERI INTERNO", "Compruebo si hay interseccion entre la pasada y las rectas del perimetro interno");
                    FuncionRecta fx_perInterno=perimetroInterno.getFuncionesRectasPerimetro().get(i);
                    longitudInterseccion=(fx_perInterno.getB()-fx_pasada.getB())/(fx_pasada.getA()-fx_perInterno.getA());
                    latitudInterseccion = fx_pasada.getA() * longitudInterseccion + fx_pasada.getB();
                    Log.d("INTERSEC PERI INTERNO", "Max x funcion peri interno:"+fx_perInterno.getX_max()+", Min x: "+fx_perInterno.getX_min()+", INTERSEC= ("+longitudInterseccion+","+latitudInterseccion+")");
                    if(longitudInterseccion<=fx_perInterno.getX_max()&&longitudInterseccion>=fx_perInterno.getX_min()) {
                        //El punto de intersección está dentro de los límites x del polígono, calculo y
                        latitudInterseccion = fx_pasada.getA() * longitudInterseccion + fx_pasada.getB();
                        if (latitudInterseccion <= fx_perInterno.getY_max() && latitudInterseccion >= fx_perInterno.getY_min()) {
                            //El punto está dentro de los límites y del polígono , lo guardo
                            Waypoint w = new Waypoint();
                            w.setUbicacion(new LatLng(latitudInterseccion, longitudInterseccion));
                            w.setTipo("pi");
                            rectasInterseccion.add(i);
                            //CALCULO LA ALTITUD DEL WAKEPOINT CORRESPONDIENTE A LA INTERSECCIÓN
                            double dist_punto_poligono_punto_interseccion=calculardistancia(perimetroInterno.getPerimeterPoints().get(i).getUbicacion(), w.getUbicacion());
                            double porcentaje_respecto_dist_total=dist_punto_poligono_punto_interseccion*100/
                                    perimetroInterno.getDistancia_entre_puntos().get(i);
                            double delta_h=perimetroInterno.getPerimeterPoints().get(i+1).getAltitud()-perimetroInterno.getPerimeterPoints().get(i).getAltitud();
                            double altitud_point=porcentaje_respecto_dist_total*delta_h/100;
                            w.setAltitud(altitud_point);
                            w.setTipo("pi");
                            alWakepointsPi.add(w);//en este al guardo las dos intersecciones (0int y 1int)
                            Log.d("INTERSEC PERI INTERNO", "X="+longitudInterseccion+", Y="+latitudInterseccion);
                        }
                    }
                }
                if(perimetroInterno.getPerimeterPoints().size()>=3){
                    //con el siguente if me aseguro tener 2 puntos que intersecan la pasada
                    if(alWakepointsPi.size()==2){
                        //si dos puntos intersecan, son dos rectas diferentes las que intersecan la pasada, me fijo cuales
                        int a = rectasInterseccion.get(0);
                        int b = rectasInterseccion.get(1);
                        int cant_puntos = perimetroInterno.perimeterPoints.size()-1;
                        Log.d("PerimetroInternoSize", "a="+a+", b="+b+", Cant puntos perimetro interno: "+cant_puntos);
                        int cant_elementos_matriz=cant_puntos+2;
                        int contador=0,contador_ppi=a;
                        matriz_puntos_pi_int.add(perimetroInterno.getPerimeterPoints().get(a));
                        contador++;
                        contador_ppi++;
                        matriz_puntos_pi_int.add(alWakepointsPi.get(0));
                        contador++;
                        for (int i=a+1; i<=b; i++){
                            matriz_puntos_pi_int.add(perimetroInterno.getPerimeterPoints().get(i));
                            contador++;
                            contador_ppi++;
                        }
                        matriz_puntos_pi_int.add(alWakepointsPi.get(1));
                        contador++;
                        for(int j=0; j<(cant_elementos_matriz-contador); j++ ){
                            if(contador_ppi>=cant_puntos){
                                contador_ppi=0;
                            }
                            matriz_puntos_pi_int.add(perimetroInterno.getPerimeterPoints().get(contador_ppi));
                            contador_ppi++;
                        }
                        Log.d("DRON","Matriz ordenada de puntos pasada " + indice_fx + ": ");
                        for(int i=0; i<matriz_puntos_pi_int.size();i++){
                            Log.d("Matriz ","Punto "+i+": "+matriz_puntos_pi_int.get(i).toString());
                        }
                        //Ahora me fijo si es mas corto recorrer esos puntos de derecha a izquierda o de izquierda a derecha
                        double distancia_1_esquivar_pi=0, distancia_2_esquivar_pi=0;
                        int posicion_punto_int_2=b+2-a;
                        for(int i=2; i<=posicion_punto_int_2;i++){
                            distancia_1_esquivar_pi+=calculardistancia(
                                    matriz_puntos_pi_int.get(i-1).getUbicacion(),
                                    matriz_puntos_pi_int.get(i).getUbicacion());
                        }
                        distancia_2_esquivar_pi=calculardistancia(
                                matriz_puntos_pi_int.get(1).getUbicacion(),
                                matriz_puntos_pi_int.get(0).getUbicacion());
                        distancia_2_esquivar_pi+=calculardistancia(
                                matriz_puntos_pi_int.get(0).getUbicacion(),
                                matriz_puntos_pi_int.get(matriz_puntos_pi_int.size()-1).getUbicacion());
                        for(int i=matriz_puntos_pi_int.size()-1; i>=posicion_punto_int_2;i--){
                            distancia_2_esquivar_pi+=calculardistancia(
                                    matriz_puntos_pi_int.get(i).getUbicacion(),
                                    matriz_puntos_pi_int.get(i-1).getUbicacion());
                        }
                        int indice_wp_order= wayPointsOrder.size()-1;
                        if(distancia_1_esquivar_pi<distancia_2_esquivar_pi){
                            Log.d("CAMINO MAS CORTO", "Distancia 1 menor a distancia 2");
                            if(copiarInvertido){
                                for(int i=posicion_punto_int_2; i>0;i--){
                                    wayPointsOrder.add(indice_wp_order,matriz_puntos_pi_int.get(i));
                                    indice_wp_order++;
                                }
                            }else{
                                for(int i=1; i<=posicion_punto_int_2;i++){
                                    wayPointsOrder.add(indice_wp_order,matriz_puntos_pi_int.get(i));
                                    indice_wp_order++;
                                }
                            }
                        }else{
                            Log.d("CAMINO MAS CORTO", "Distancia 2 menor a distancia 1");
                            if(copiarInvertido){
                                for(int i=posicion_punto_int_2; i<matriz_puntos_pi_int.size();i++){
                                    wayPointsOrder.add(indice_wp_order,matriz_puntos_pi_int.get(i));
                                    indice_wp_order++;
                                }
                                wayPointsOrder.add(indice_wp_order,matriz_puntos_pi_int.get(0));
                                indice_wp_order++;
                                wayPointsOrder.add(indice_wp_order,matriz_puntos_pi_int.get(1));
                            }else{
                                wayPointsOrder.add(indice_wp_order,matriz_puntos_pi_int.get(1));
                                indice_wp_order++;
                                wayPointsOrder.add(indice_wp_order,matriz_puntos_pi_int.get(0));
                                for(int i=matriz_puntos_pi_int.size()-1; i>=posicion_punto_int_2;i--){
                                    indice_wp_order++;
                                    wayPointsOrder.add(indice_wp_order,matriz_puntos_pi_int.get(i));
                                }
                            }

                        }
                    }
                }
            }else{
                for(int i=0; i<perimetroInterno.getFuncionesRectasPerimetro().size();i++){
                    Log.d("INTERSEC PERI INTERNO", "Compruebo si hay interseccion entre la pasada y las rectas del perimetro interno");
                    FuncionRecta fx_perInterno=perimetroInterno.getFuncionesRectasPerimetro().get(i);
                    longitudInterseccion=(fx_perInterno.getB()-fx_pasada.getB())/(fx_pasada.getA()-fx_perInterno.getA());
                    latitudInterseccion = fx_pasada.getA() * longitudInterseccion + fx_pasada.getB();
                    Log.d("INTERSEC PERI INTERNO", "Max x funcion peri interno:"+fx_perInterno.getX_max()+", Min x: "+fx_perInterno.getX_min()+", INTERSEC= ("+longitudInterseccion+","+latitudInterseccion+")");
                    if(longitudInterseccion<=fx_perInterno.getX_max()&&longitudInterseccion>=fx_perInterno.getX_min()) {
                        //El punto de intersección está dentro de los límites x del polígono, calculo y
                        latitudInterseccion = fx_pasada.getA() * longitudInterseccion + fx_pasada.getB();
                        if (latitudInterseccion <= fx_perInterno.getY_max() && latitudInterseccion >= fx_perInterno.getY_min()) {
                            //El punto está dentro de los límites y del polígono , lo guardo
                            Waypoint w = new Waypoint();
                            w.setUbicacion(new LatLng(latitudInterseccion, longitudInterseccion));
                            w.setTipo("pi");
                            rectasInterseccion.add(i);
                            //CALCULO LA ALTITUD DEL WAKEPOINT CORRESPONDIENTE A LA INTERSECCIÓN
                            double dist_punto_poligono_punto_interseccion=calculardistancia(perimetroInterno.getPerimeterPoints().get(i).getUbicacion(), w.getUbicacion());
                            double porcentaje_respecto_dist_total=dist_punto_poligono_punto_interseccion*100/
                                    perimetroInterno.getDistancia_entre_puntos().get(i);
                            double delta_h=perimetroInterno.getPerimeterPoints().get(i+1).getAltitud()-perimetroInterno.getPerimeterPoints().get(i).getAltitud();
                            double altitud_point=porcentaje_respecto_dist_total*delta_h/100;
                            w.setAltitud(altitud_point);
                            w.setTipo("pi");
                            funcionesParalelas.get(indice_fx).addWakepointPasada(w);
                            Log.d("INTERSEC PERI INTERNO", "X="+longitudInterseccion+", Y="+latitudInterseccion);
                        }
                    }
                }
            }
    }

    /// DE METROS A GRADOS EN LONGITUD
    private double give_grades_from_meters_lng(double metros, double lat){
        double rlat=Math.toRadians(lat); // Convert degrees to radians
        double R = RADIO_TIERRA*Math.cos(rlat); // Radius of the Earth en metros
        double P=2*Math.PI*R; //PERIMETRO DE LA TIERRA
        return 360*metros/P;
    }

    public ArrayList<Waypoint> getWakepoints() {
        return this.wayPointsOrder;
    }

    public void copiarIgual(FuncionRecta fx, double distancia){
        copiarInvertido=false;
        Waypoint point1=homePoint;
        Waypoint point2;
        double distanciaExtra;
        for(int j=0; j<fx.getWakepointsPasada().size();j++){
            point2= fx.getWakepointsPasada().get(j);
            distancia+=calculardistancia(point1.getUbicacion(),point2.getUbicacion());
            if(distancia< DISTANCIA_CORTE_RUTA_COMBUSTIBLE){
                wayPointsOrder.add(fx.getWakepointsPasada().get(j));
            }else{
                distanciaExtra=distancia- DISTANCIA_CORTE_RUTA_COMBUSTIBLE;
                Waypoint w = new Waypoint();
                w.setUbicacion(generarCorteDeRuta(point1.getUbicacion(),distanciaExtra));
                w.setAltitud(point1.getAltitud());
                w.setTipo("cr");
                w.setPulverizar(false);
                wayPointsOrder.add(w);
                wayPointsOrder.add(homePoint);
                wayPointsOrder.add(w);
                distancia=calculardistancia(homePoint.getUbicacion(),w.getUbicacion());
                wayPointsOrder.add(point2);
                distancia+=calculardistancia(w.getUbicacion(),point2.getUbicacion());
            }
            point1=point2;
        }
    }

    public void copiarInvertido(FuncionRecta fx, double distancia){
        copiarInvertido=true;
        Waypoint point1=homePoint;
        Waypoint point2;
        double distanciaExtra;
        for(int j=fx.getWakepointsPasada().size()-1;j>=0;j--){
            point2= fx.getWakepointsPasada().get(j);
            distancia+=calculardistancia(point1.getUbicacion(),point2.getUbicacion());
            if(distancia< DISTANCIA_CORTE_RUTA_COMBUSTIBLE) {
                wayPointsOrder.add(fx.getWakepointsPasada().get(j));
            }else{
                distanciaExtra=distancia- DISTANCIA_CORTE_RUTA_COMBUSTIBLE;
                Waypoint w = new Waypoint();
                w.setUbicacion(generarCorteDeRuta(point1.getUbicacion(),distanciaExtra));
                w.setAltitud(point1.getAltitud());
                w.setTipo("cr");
                w.setPulverizar(false);
                wayPointsOrder.add(w);
                wayPointsOrder.add(homePoint);
                wayPointsOrder.add(w);
                distancia=calculardistancia(homePoint.getUbicacion(),w.getUbicacion());
                wayPointsOrder.add(point2);
                distancia+=calculardistancia(w.getUbicacion(),point2.getUbicacion());
            }
            point1=point2;
        }
    }

    public void ordenarPuntos(){
        wayPointsOrder = new ArrayList<>();
        wayPointsOrder.add(homePoint);
        double distancia =0;
        for(int i=0; i<funcionesParalelas.size(); i++){
            FuncionRecta fx = funcionesParalelas.get(i);
            //PRIMERO ME FIJO SI SON RECTAS HORIZONTALES O VERTICALES
            if(maxmax){
                //SON HORIZONTALES,Ordeno el arrayList de puntos de la pasada según la longitud
                Collections.sort(fx.getWakepointsPasada(), (w1, w2) -> {
                    if(w1.getUbicacion().longitude<w2.getUbicacion().longitude){return -1;}else{return 1;}
                });
                // ME FIJO SI VA DE IZQ. A DCHA O AL REVÉS
                if(de_izquierda_a_derecha){
                    //PRIMERA PASADA DE IZQUIERDA A DERECHA
                    //ME FIJO SI ES PAR O IMPAR ESTA PASADA PARA ORDENAR
                    if(i%2==0){
                        //PAR ->IZQ A DER. guardo igual
                        copiarIgual(fx,distancia);
                    }else {
                        //IMPAR -> DER A IZQ. guardo invertido
                        copiarInvertido(fx,distancia);
                    }
                }else{
                    //PRIMERA PASADAS DE DERECHA A IZQUIERDA
                    //ME FIJO SI ES PAR O IMPAR ESTA PASADA PARA ORDENAR
                    if(i%2==0){
                        //PAR -> DER A IZQ
                        copiarInvertido(fx,distancia);
                    }else {
                        //IMPAR -> IZQ A DER
                        copiarIgual(fx,distancia);
                    }
                }
            }else{
                //Son verticales, ordeno el array de puntos de la pasada según la latitud
                Collections.sort(fx.getWakepointsPasada(), (w1, w2) -> {
                    if(w1.getUbicacion().latitude<w2.getUbicacion().latitude){return -1;}else{return 1;}
                });
                //SI SON VERTICALES, ME FIJO SI VAN DE ABAJO HACIA ARRIBA O AL REVÉS.
                if(haciaArriba){
                    if(i%2==0){
                        //PAR, HACIA ARRIBA: copio igual
                        copiarIgual(fx,distancia);
                    }else{
                       //IMPAR, HACIA ABAJO: copio invertido
                        copiarInvertido(fx,distancia);
                    }
                }else{
                    if(i%2==0){
                        //PAR, HACIA ABAJO: copio invertido
                        copiarInvertido(fx,distancia);
                    }else{
                        //IMPAR, HACIA ARRIBA: copio igual
                        copiarIgual(fx,distancia);
                    }
                }
            }
            //Me fijo si hay algún perímetro interno que interseque con esta pasada
            comprobarPerimetrosInternos(i);
        }

        wayPointsOrder.add(homePoint);
        agregarPulverizacionAbiertaCerrada();
        agregar16aComandosServo();
    }

    private LatLng generarCorteDeRuta(LatLng wu, double distancia ){
        LatLng point;
        double inc = (distancia*360)/(2*Math.PI*RADIO_TIERRA);
        double incLong = inc/(Math.cos(Math.toRadians(wu.latitude)));
        double lat2= wu.latitude+inc*Math.sin(Math.toRadians(angulo));
        double long2=wu.longitude+incLong*Math.cos(Math.toRadians(angulo));
        point=new LatLng(lat2,long2);
        return point;
    }

    private void agregarPulverizacionAbiertaCerrada() {
        //El 1 siempre es home, por defecto
        Waypoint w1 = wayPointsOrder.get(0);
        w1.setCommand(21);
        w1.setParam1(0);
        w1.setParam2(1);
        Waypoint w2;
        String tipo1, tipo2, combinacion;
        boolean nava = true, pia=true;
        for(int i = 1; i< wayPointsOrder.size(); i++){
            switch (wayPointsOrder.get(i).getTipo()){
                case "nav":
                    wayPointsOrder.get(i).setPulverizar(nava);
                    nava=!nava;
                    break;
                case "obs":
                    wayPointsOrder.get(i).setPulverizar(false);
                    break;
                case "pi":
                    wayPointsOrder.get(i).setPulverizar(pia);
                    pia=!pia;
                    break;
                case "home":
                    wayPointsOrder.get(i).setPulverizar(false);
                    break;
            }
        }
        for(int i = 1; i< wayPointsOrder.size(); i++){
            tipo1=w1.getTipo();
            w2= wayPointsOrder.get(i);
            tipo2=w2.getTipo();
            combinacion=tipo1+"-"+tipo2;
            Log.d("TIPO WP", "Combinación tipos wp1-wp2: "+combinacion);
            if((tipo1.equals("nav")||tipo1.equals("obs")||tipo1.equals("pi"))&&w1.isPulverizar()){
                if(tipo1.equals("obs")&&(tipo2.equals("obs")&&w2.isPulverizar())){
                    //de obsa(uno interno sería) a obsa
                    w2.setCommand(16);
                    w2.setParam1(0);
                    w2.setParam2(1);
                }else{
                    w2.setCommand(183);
                    w2.setParam1(1);
                    w2.setParam2(1000);
                }
            }else{
                if(tipo1.equals("obs")&&w1.isPulverizar()){
                    w2.setCommand(16);
                    w2.setParam1(0);
                    w2.setParam2(1);
                }else{
                    switch (combinacion) {
                        case"home-nav":
                        case "cr-nav":
                            if(w2.isPulverizar()){
                                //h a nava
                                //cr a nava
                                w2.setCommand(183);
                                w2.setParam1(1);
                                w2.setParam2(1900);
                            }else{
                                if (tipo1.equals("cr")){
                                    //cr a navc
                                    w2.setCommand(16);
                                    w2.setParam1(0);
                                    w2.setParam2(1);
                                }
                            }
                            break;
                        case "home-cr":
                            w2.setCommand(16);
                            w2.setParam1(0);
                            w2.setParam2(1);
                            break;
                        case "nav-nav":
                        case "pi-pi":
                            if(!w1.isPulverizar()&&w2.isPulverizar()){
                                //navc a nava
                                //pic a pia
                                w2.setCommand(183);
                                w2.setParam1(1);
                                w2.setParam2(1900);
                            }
                            break;
                        case "nav-home":
                            if(!w1.isPulverizar()){
                                //navc a h
                                w2.setCommand(21);
                                w2.setParam1(0);
                                w2.setParam2(1);
                            }
                            break;
                        case "nav-cr":
                        case "pi-cr":
                            if(!w1.isPulverizar()){
                                //navc a cr
                                //pic a cr
                                w2.setCommand(16);
                                w2.setParam1(0);
                                w2.setParam2(1);
                            }
                            break;
                        case "cr-home":
                            w2.setCommand(21);
                            w2.setParam1(0);
                            w2.setParam2(1);
                            break;
                        case "cr-pi":
                        case "cr-obs":
                            if(w2.isPulverizar()){
                                //cr a pia
                                //cr a obsa
                                w2.setCommand(183);
                                w2.setParam1(1);
                                w2.setParam2(1900);
                            }else{
                                //cr a pic
                                //cr a obsc
                                w2.setCommand(16);
                                w2.setParam1(0);
                                w2.setParam2(1);
                            }
                            break;
                        /*default:
                            w2.setCommand(16);
                            w2.setParam1(0);
                            w2.setParam2(1);
                            break;*/
                    }
                }
            }
            Log.d("SET COMMAND", "Seteo al wakepoint el comando: "+w2.getCommand());
            w1=w2;
        }
    }

    public void agregar16aComandosServo(){
        Waypoint w_agregar;
        wayPointsFinal=new ArrayList<>();
        for(int i = 0; i< wayPointsOrder.size(); i++){
            Waypoint w_actual=wayPointsOrder.get(i);
            //si es un 183 agrego antes un 16 igual, para que valla hasta ahí y le siga el parametro de set servo
            if(w_actual.getCommand()==183){
                w_agregar=new Waypoint();
                w_agregar.setUbicacion(w_actual.getUbicacion());
                w_agregar.setAltitud(w_actual.getAltitud());
                w_agregar.setCommand(16);
                w_agregar.setParam1(0);
                w_agregar.setParam2(3);
                wayPointsFinal.add(w_agregar);
            }
            wayPointsFinal.add(w_actual);
        }
    }

    /*public String generarTxt(){
        Log.d("ALTITUD HOME","ALTITUD PUNTO HOME="+homePoint.getAltitud());
        StringBuilder texto= new StringBuilder("QGC WPL 110\n0\t1\t3\t16\t0\t3\t0\t0\t"
                + wayPointsOrder.get(0).getUbicacion().latitude
                + "\t" + wayPointsOrder.get(0).getUbicacion().longitude + "\t0\t1\n");

        for (int i = 1; i< wayPointsOrder.size()-1; i++){
            Waypoint w= wayPointsOrder.get(i);
            Log.d("WAKEPOINT ORDER ","wakepoint "+i+", comando: "+w.getCommand());
            //esHome=wakePointsOrder.get(i).equals(homePoint);
            //if (wakePointsOrder.get(i).getTipo().equals("home")) {
                //texto.append("\t0\t3\t21\t0\t3\t0\t0\t");
            //} else {
              //  texto.append("\t0\t3\t16\t0\t3\t0\t0\t");
            //}
            texto.append(i)
                    .append("\t0\t3\t").append(w.getCommand()).append("\t")
                    .append(w.getParam1()).append("\t")
                    .append(w.getParam2()).append("\t0\t0\t")
                    .append(wayPointsOrder.get(i).getUbicacion().latitude)
                    .append("\t").append(wayPointsOrder.get(i).getUbicacion().longitude)
                    .append("\t").append(wayPointsOrder.get(i).getAltitud()-homePoint.getAltitud()).append("\t1\n");
        }
        int last_index = wayPointsOrder.size()-1;
        texto.append(last_index).append("\t0\t3\t21\t0\t3\t0\t0\t")
                .append(wayPointsOrder.get(last_index).getUbicacion().latitude)
                .append("\t").append(wayPointsOrder.get(last_index).getUbicacion().longitude)
                .append("\t").append(wayPointsOrder.get(last_index).getAltitud()-homePoint.getAltitud()).append("\t1\n");
        Log.d("texto archivo", texto.toString());
        //guardarArchivoMem(texto, context);
        return texto.toString();
    }*/
    public String generarTxt(){
        Log.d("ALTITUD HOME","ALTITUD PUNTO HOME="+homePoint.getAltitud());
        StringBuilder texto= new StringBuilder("QGC WPL 110\n0\t1\t3\t16\t0\t3\t0\t0\t"
                + wayPointsFinal.get(0).getUbicacion().latitude
                + "\t" + wayPointsFinal.get(0).getUbicacion().longitude + "\t0\t1\n");

        for (int i = 1; i< wayPointsFinal.size()-1; i++){
            Waypoint w= wayPointsFinal.get(i);
            Log.d("WAYPOINT FINAL ","waypoint "+i+", comando: "+w.getCommand());
            //esHome=wakePointsOrder.get(i).equals(homePoint);
            //if (wakePointsOrder.get(i).getTipo().equals("home")) {
            //texto.append("\t0\t3\t21\t0\t3\t0\t0\t");
            //} else {
            //  texto.append("\t0\t3\t16\t0\t3\t0\t0\t");
            //}
            texto.append(i)
                    .append("\t0\t3\t").append(w.getCommand()).append("\t")
                    .append(w.getParam1()).append("\t")
                    .append(w.getParam2()).append("\t0\t0\t")
                    .append(wayPointsFinal.get(i).getUbicacion().latitude)
                    .append("\t").append(wayPointsFinal.get(i).getUbicacion().longitude)
                    .append("\t").append(wayPointsFinal.get(i).getAltitud()-homePoint.getAltitud()).append("\t1\n");
        }
        int last_index = wayPointsFinal.size()-1;
        texto.append(last_index).append("\t0\t3\t21\t0\t3\t0\t0\t")
                .append(wayPointsFinal.get(last_index).getUbicacion().latitude)
                .append("\t").append(wayPointsFinal.get(last_index).getUbicacion().longitude)
                .append("\t").append(wayPointsFinal.get(last_index).getAltitud()-homePoint.getAltitud()).append("\t1\n");
        Log.d("texto archivo", texto.toString());
        //guardarArchivoMem(texto, context);
        return texto.toString();
    }


    public void girarFunciones(int incAngulo){
        Log.d("prueba", "cantidad de puntos del poligono campo: "+puntosPoligonoCampo.size());
        estoyGirando=true;
        double angulo_nuevo=this.angulo+incAngulo;
        if(angulo_nuevo>360){
            angulo0_360=angulo_nuevo-360;
        }else{
            if(angulo_nuevo<0){
                angulo0_360=360+angulo_nuevo;
            }else{
                angulo0_360=angulo_nuevo;
            }

        }
        this.angulo=angulo_nuevo;
        angulo_perpendicular=angulo_nuevo+90;
        //if(angulo_perpendicular>360)angulo_perpendicular-=360;
        angulo_perp_rad=Math.toRadians(angulo_perpendicular);
        Log.d("ANGULO", "ANGULO DE GIRO: "+incAngulo+", ANGULO PARALELA BASE GIRADO: "+angulo_nuevo+", ANGULO PERPENDICULAR: "+angulo_perpendicular+", Angulo perpendicular en radianes: "+angulo_perp_rad);
        calcularParalelaBaseEnLimite();
        calcularParalelas();
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

    public double calcularDistanciaTotalRecorrido(){
        double distancia=0;
        for (int i = 0; i< wayPointsOrder.size()-1; i++){
            LatLng point1= wayPointsOrder.get(i).getUbicacion();
            LatLng point2= wayPointsOrder.get(i+1).getUbicacion();
            distancia+=calculardistancia(point1,point2);
        }
        Log.e("DISTANCIA TOTAL", ""+distancia);
        return distancia;
    }

    public double getAnguloPuntoInterseccion(LatLng point_interseccion, FuncionCircunferencia fc){
        double alpha_final;
        double radianes=(point_interseccion.latitude-fc.getCentro().latitude)/fc.getRadio_grados();
        double alpha=Math.toDegrees(Math.asin(radianes));
        //Log.d("CALCULO ANGULO","ANGULO RAD=asin("+point_interseccion.latitude+" - ("+fc.getCentro().latitude+")/)"+fc.getRadio_grados()+"=asin("+radianes+")="+alpha+"°");
        double radianes2=(point_interseccion.longitude-fc.getCentro().longitude)/fc.getRadio_grados();
        double alpha2=Math.toDegrees(Math.acos(radianes2));
        //Log.d("CALCULO ANGULO","ANGULO RAD=acos("+point_interseccion.longitude+" - ("+fc.getCentro().longitude+")/)"+fc.getRadio_grados()+"=acos("+radianes2+")="+alpha2+"°");
        alpha_final = Math.max(alpha, alpha2);
        double distancia1=calculardistancia(point_interseccion,generarWakepoint(alpha_final,fc).getUbicacion());
        double alpha_aux=360-alpha_final;
        double distancia2=calculardistancia(point_interseccion,generarWakepoint(alpha_aux,fc).getUbicacion());
        if(distancia2<distancia1){
            //Log.d("CALCULO ANGULO","ANGULO =360-"+alpha_final+"="+alpha_aux);
            alpha_final=alpha_aux;
        }
        return alpha_final;
    }

    public Waypoint generarWakepoint(double angulo, FuncionCircunferencia fc){
        LatLng point;
        Waypoint w=new Waypoint();
        double sin=Math.sin(Math.toRadians(angulo));
        double cos=Math.cos(Math.toRadians(angulo));
        double lat=fc.getRadio_grados()*sin+fc.getCentro().latitude;
        double lon=fc.getRadio_grados()*cos+fc.getCentro().longitude;
        point=new LatLng(lat,lon);
        w.setUbicacion(point);
        w.setAltitud(fc.getAltitud());
        Log.d("WAKEPOINT CIRC", "Ángulo: "+angulo+", "+point.toString());
        return w;
    }

    public Poligono getPerimetroInterno() {
        return perimetroInterno;
    }
}
