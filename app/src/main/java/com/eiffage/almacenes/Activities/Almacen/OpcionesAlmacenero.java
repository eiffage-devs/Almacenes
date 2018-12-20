package com.eiffage.almacenes.Activities.Almacen;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.eiffage.almacenes.R;

public class OpcionesAlmacenero extends AppCompatActivity {

    RadioGroup radioGroup;

    //Método para usar flecha de atrás en Action Bar
    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tipo_movimiento);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Movimientos de material");

        radioGroup = findViewById(R.id.radiogroup);
    }

    public void siguiente(View view){
        int index = -1;
        String dest = "";
        switch (radioGroup.getCheckedRadioButtonId()){
            case R.id.rbEntrada:
                index = 0;
                dest = "com.eiffage.almacenes.Activities.Almacen.TiposRegistro.Entrada";
                break;
            case R.id.rbDevEndesa:
                index = 1;
                break;
            case R.id.rbSalidaAlmacen:
                index = 2;
                dest = "com.eiffage.almacenes.Activities.Almacen.TiposRegistro.SalidaAlmacen";
                break;
            case R.id.rbSalidaObra:
                index = 3;
                dest = "com.eiffage.almacenes.Activities.General.OTOIncidencia";
                break;
            case R.id.rbDevObra:
                index = 4;
                dest = "com.eiffage.almacenes.Activities.General.OTOIncidencia";
                break;
        }

        if(index != -1){
            if(index == 1){
                Intent intent = new Intent(OpcionesAlmacenero.this, CreaLineas.class);
                intent.putExtra("tipoRegistro", "DEVOLUCIÓN A ENDESA");
                startActivity(intent);
            }
            else {
                RadioButton radioButton = (RadioButton) radioGroup.getChildAt(index);
                String seleccion = radioButton.getText().toString();
                try {
                    Intent intent = new Intent(OpcionesAlmacenero.this, Class.forName(dest));
                    intent.putExtra("Seleccion", seleccion);
                    startActivity(intent);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        else {
            muestraAlert("Elige una opción", "Es necesario para poder continuar");
        }
    }

    public void muestraAlert(String titulo, String mensaje){

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(titulo)
                .setMessage(mensaje)
                .setCancelable(true)
                . setPositiveButton("Entendido", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.show();
    }
}
