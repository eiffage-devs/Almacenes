package com.eiffage.almacenes.Activities.JefeObra;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;

import com.eiffage.almacenes.Activities.General.OTOIncidencia;
import com.eiffage.almacenes.R;

public class OpcionesJefeObra extends AppCompatActivity {

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
        setContentView(R.layout.activity_opciones_jefe_obra);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Movimientos de certificación");

        radioGroup = findViewById(R.id.radiogroup);
    }

    public void siguiente(View view){
        Intent i = new Intent(OpcionesJefeObra.this, OTOIncidencia.class);
        switch (radioGroup.getCheckedRadioButtonId()){
            case R.id.rbCertificacion:
                i.putExtra("objetivo", "certificacion");
                break;
            case R.id.rbInforme:
                i.putExtra("objetivo", "informe");
                break;

        }

        startActivity(i);
    }
}
