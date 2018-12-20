package com.eiffage.almacenes.Activities.JefeObra;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.eiffage.almacenes.Activities.Almacen.CreaLineas;
import com.eiffage.almacenes.Activities.Almacen.TiposRegistro.SalidaObra;
import com.eiffage.almacenes.R;

public class FiltroOT extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtro_ot);
    }

    public void codigoManual(View view) {
        AlertDialog.Builder alertdialogobuilder = new AlertDialog.Builder(this);
        alertdialogobuilder
                .setTitle("¿Quieres usar una OT que no apareceeeeeeeeeeeeeeeeee?")
                .setMessage("¡Cuidado! Asegúrate de poner bien el código, ya que no se va a verificar su existencia y podría causar problemas.")
                .setCancelable(true);

        final EditText otManual = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        otManual.setLayoutParams(lp);
        alertdialogobuilder.setView(otManual);
        alertdialogobuilder.setPositiveButton("Utilizar el código de OT introducido", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String codOT = otManual.getText().toString();
                if (codOT.length() != 6) {
                    Toast.makeText(getApplicationContext(), "El código de la OT debe tener 6 caracteres.", Toast.LENGTH_SHORT).show();
                    codOT = "";
                } else {
                    Intent intent = new Intent(FiltroOT.this, CreaLineas.class);
                    intent.putExtra("otElegida", codOT);
                    intent.putExtra("incidenciaElegida", "-");
                    intent.putExtra("ticketSCM", "-");
                    startActivity(intent);
                }

            }
        })

                .create();
        if (!isFinishing()) {
            alertdialogobuilder.show();
        }
    }
}


