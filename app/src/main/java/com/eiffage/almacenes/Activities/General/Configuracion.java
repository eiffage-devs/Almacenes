package com.eiffage.almacenes.Activities.General;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.eiffage.almacenes.Objetos.Almacen;
import com.eiffage.almacenes.Objetos.MySqliteOpenHelper;
import com.eiffage.almacenes.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Configuracion extends AppCompatActivity {

    Spinner spinner;
    ArrayList<Almacen> almacenes;

    String almacenElegido, token;
    SharedPreferences sp;

    MySqliteOpenHelper mySqliteOpenHelper;
    SQLiteDatabase db;

    //Método para usar flecha de atrás en Action Bar
    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Configuración");

        mySqliteOpenHelper = new MySqliteOpenHelper(this);
        db = mySqliteOpenHelper.getWritableDatabase();

        sp = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        token = sp.getString("token", "Sin valor");

        spinner = findViewById(R.id.spinner);
        almacenes = new ArrayList<>();

        //----- Inflamos Spinner almacenes -----\\

        cargarAlmacenesFiltrados();

    }

    public void cargarAlmacenesFiltrados(){
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, "http://82.223.65.75:8000/api_endesa/obtenerAlmacenesFiltrados",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("ALMACENES FILTRADOS", response);

                        try {
                            JSONObject jo = new JSONObject(response);
                            JSONArray jsonArray = jo.getJSONArray("content");

                            String [] lista = new String[jsonArray.length()];

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String prov = jsonObject.getString("provincia");
                                String alm = "" + jsonObject.getString("almacen");

                                Almacen almacen = new Almacen(prov, alm);
                                almacenes.add(almacen);

                                lista[i] = alm;
                            }
                            ArrayAdapter<String> adapter= new ArrayAdapter<>(Configuracion.this,android.R.layout.simple_spinner_dropdown_item, lista);
                            spinner.setAdapter(adapter);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error almacenes filtro", error.toString());

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                params.put("Authorization", "Bearer " + token);

                return params;
            }
        };
        queue.add(stringRequest);
        stringRequest.setRetryPolicy((new DefaultRetryPolicy(60 * 1000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)));

    }

    public void guardar(View view){
        almacenElegido = spinner.getSelectedItem().toString();

        mySqliteOpenHelper.cambiarAlmacenElegido(db, new Almacen("-", almacenElegido));

        muestraAlert("Almacén actualizado", "Ahora se trabaja en:  " + almacenElegido);
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

    public void cerrarSesion(View view){
        SharedPreferences.Editor editor = getSharedPreferences("myPrefs", MODE_PRIVATE).edit();
        editor.putString("token", "Sin valor");
        editor.apply();

        Intent i = new Intent(Configuracion.this, Login.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
