package com.eiffage.almacenes.Activities.General;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.eiffage.almacenes.Activities.Almacen.CreaLineas;
import com.eiffage.almacenes.Activities.Almacen.TiposRegistro.SalidaObra;
import com.eiffage.almacenes.Activities.JefeObra.FiltroOT;
import com.eiffage.almacenes.R;

public class OTOIncidencia extends AppCompatActivity {

    RadioButton incidencia, ot;
    RadioGroup group;
    EditText numeroIncidencia, ticketSCM;
    String tipoRegistro;

    String destino, objetivo;
    Button button;

    //Método para usar flecha de atrás en Action Bar
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otoincidencia);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Parámetros generales");

        button = findViewById(R.id.btnOTIncidencia);

        Intent i = getIntent();
        String tipoReg = i.getStringExtra("Seleccion");
        if (tipoReg != null) {
            destino = "almacenero";
            if (tipoReg.equals("Salida a obra")) {
                tipoRegistro = "SALIDA A OBRA";
            } else if (tipoReg.equals("Devolución de obra")) {
                tipoRegistro = "DEVOLUCIÓN DE OBRA";
            }
        }


        incidencia = findViewById(R.id.optIncidencia);
        ot = findViewById(R.id.optOT);

        numeroIncidencia = findViewById(R.id.incidencia);
        ticketSCM = findViewById(R.id.ticketSCM);
        ot.setChecked(true);
        numeroIncidencia.setVisibility(View.INVISIBLE);


        group = findViewById(R.id.rg);
        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (incidencia.isChecked()) {
                    numeroIncidencia.setVisibility(View.VISIBLE);
                    numeroIncidencia.requestFocus();
                    button.setText("Continuar");
                    //ticketSCM.setVisibility(View.VISIBLE);
                } else {
                    numeroIncidencia.setVisibility(View.INVISIBLE);
                    button.setText("Buscar OT");
                    //ticketSCM.setVisibility(View.INVISIBLE);
                }
            }
        });


    }

    public void continuar(View v) {
        if (ot.isChecked()) {
            if (destino.equals("almacenero")) {
                Intent i = new Intent(OTOIncidencia.this, SalidaObra.class);
                i.putExtra("tipoRegistro", tipoRegistro);
                String ticket = ticketSCM.getText().toString();
                if(ticket.equals("")){
                    i.putExtra("ticketSCM", "-");
                }
                else {
                    i.putExtra("ticketSCM", ticket);
                }
                startActivity(i);
            } else if (destino.equals("jefeobra")) {
                Intent i = new Intent(OTOIncidencia.this, FiltroOT.class);
                i.putExtra("objetivo", objetivo);
                startActivity(i);
            }

        } else if (incidencia.isChecked()) {
            if (ticketSCM.getText().toString().length() < 1) {
                ticketSCM.requestFocus();
                ticketSCM.setError("Campo necesario");
            } else {
                if (destino.equals("almacenero")) {
                    Intent i = new Intent(OTOIncidencia.this, CreaLineas.class);
                    i.putExtra("tipoRegistro", tipoRegistro);
                    i.putExtra("incidenciaElegida", numeroIncidencia.getText().toString());
                    i.putExtra("ticketSCM", ticketSCM.getText().toString());
                    i.putExtra("otElegida", "-");
                    startActivity(i);
                } else if (destino.equals("jefeobra")) {
                    /*Intent i = new Intent(OTOIncidencia.this, FiltroOT.class);
                    i.putExtra("objetivo", objetivo);
                    startActivity(i);*/
                }
            }
        } else {
            Toast.makeText(OTOIncidencia.this, "Selecciona una opción para continuar", Toast.LENGTH_SHORT).show();
        }
    }
}
