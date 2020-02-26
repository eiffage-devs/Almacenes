package com.eiffage.almacenes.Activities.Almacen.TiposRegistro;

import android.content.DialogInterface;
import android.content.Intent;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;

import com.eiffage.almacenes.Activities.Almacen.CreaLineas;
import com.eiffage.almacenes.R;

public class Entrada extends AppCompatActivity {

    EditText albaran, observaciones;
    Switch etiquetas;
    String provieneDe, etiqueta;

    //Método para usar flecha de atrás en Action Bar
    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrada);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Parámetros generales");

        albaran = findViewById(R.id.albaranEntrada);
        observaciones = findViewById(R.id.observacionesEntrada);
        etiquetas = findViewById(R.id.etiquetas);

        etiquetas.setChecked(true);
    }

    public void siguiente(View view){
        String alb = albaran.getText().toString();
        String obs = observaciones.getText().toString();

        if(alb.equals("") && obs.equals("")){
            muestraAlert("Falta información", "Si el material viene de Endesa, introduce el nº de albarán.\n\nSi viene de otro proveedor, indícalo en las observaciones.");
            return;
        }
        else if(alb.equals("")){
            provieneDe = obs;
        }
        else {
            provieneDe = alb;
        }

        if(etiquetas.isChecked())
            etiqueta = "SI";
        else etiqueta = "NO";


        Intent i = new Intent(Entrada.this, CreaLineas.class);
        i.putExtra("tipoRegistro", "ENTRADA");
        i.putExtra("albaran", provieneDe);
        i.putExtra("etiqueta", etiqueta);
        startActivity(i);
    }

    public void muestraAlert(String titulo, String mensaje){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(titulo)
                .setMessage(mensaje)
                .setCancelable(true)
                . setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.show();
    }
}
