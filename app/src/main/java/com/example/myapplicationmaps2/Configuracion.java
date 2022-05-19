package com.example.myapplicationmaps2;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

public class Configuracion extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);

        SharedPreferences sharedPref =getSharedPreferences("config_dron",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        Button btn_save = findViewById(R.id.btn_guardar);
        Button btn_back = findViewById(R.id.btn_volver);
        EditText et_ancho_botalon = findViewById(R.id.et_ancho_botalon);
        EditText et_altura_de_vuelo = findViewById(R.id.et_altura_vuelo);
        Switch sw_esquivar_perimetro_interno = findViewById(R.id.switch1);

        int defaultValue = -1;
        int ancho_botalon = sharedPref.getInt("ancho_botalon", defaultValue);
        int altura_vuelo = sharedPref.getInt("altura_vuelo", defaultValue);

        sw_esquivar_perimetro_interno.setChecked(sharedPref.getBoolean("esquivar_pi",false));
        String string_et;
        if(ancho_botalon!=-1){
            string_et=""+ancho_botalon;
            et_ancho_botalon.setText(string_et);
        }
        if(altura_vuelo!=-1){
            string_et=""+altura_vuelo;
            et_altura_de_vuelo.setText(string_et);
        }

        btn_save.setOnClickListener(v -> {
            //Guardo la configuracion con shared_pref
            editor.putInt("ancho_botalon",Integer.parseInt(et_ancho_botalon.getText().toString()) );
            editor.putInt("altura_vuelo", Integer.parseInt(et_altura_de_vuelo.getText().toString()));
            editor.putBoolean("esquivar_pi",sw_esquivar_perimetro_interno.isChecked());
            editor.apply();
            finish();
        });
        btn_back.setOnClickListener(v -> finish());
    }
}