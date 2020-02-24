package com.eiffage.almacenes.Activities.Almacen.TiposRegistro;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
import com.eiffage.almacenes.Activities.Almacen.CreaLineas;
import com.eiffage.almacenes.Objetos.Almacen;
import com.eiffage.almacenes.Objetos.MySqliteOpenHelper;
import com.eiffage.almacenes.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SalidaAlmacen extends AppCompatActivity {

    private final String URL_OBTENER_ALMACENES = this.getResources().getString(R.string.urlObtenerAlmacenes);
    private final String URL_SALIDA_ALMACEN_CORRECTA = this.getResources().getString(R.string.urlSalidaAlmacenCorrecta);

    Spinner spinner;
    String token;
    ProgressDialog progressDialog;

    MySqliteOpenHelper mySqliteOpenHelper;
    SQLiteDatabase db;

    ArrayList<Almacen> almacenes;

    //Método para usar flecha de atrás en Action Bar
    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salida_almacen);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Parámetros generales");

        mySqliteOpenHelper = new MySqliteOpenHelper(this);
        db = mySqliteOpenHelper.getWritableDatabase();

        spinner = findViewById(R.id.spinnerAlmacenDestino);


        //
        //      Recuperar token
        //

        SharedPreferences myPrefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        token = myPrefs.getString("token", "Sin valor");


        //----- Inflamos Spinner almacenes -----\\

        almacenes = new ArrayList<>();
        traerAlmacenes();

    }

    public void traerAlmacenes(){
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL_OBTENER_ALMACENES,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("LISTA ALMACENES", response);

                        try {
                            JSONObject jo = new JSONObject(response);
                            JSONArray jsonArray = jo.getJSONArray("content");

                            String [] lista = new String[jsonArray.length()];

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String prov = jsonObject.getString("provincia");
                                String alm = "" + jsonObject.getString("almacen");

                                Almacen almacen = new Almacen(prov, alm);
                                mySqliteOpenHelper.insertarAlmacen(db, almacen);

                                lista[i] = alm;

                            }

                            ArrayAdapter<String> adapter= new ArrayAdapter<>(SalidaAlmacen.this,android.R.layout.simple_spinner_dropdown_item, lista);
                            spinner.setAdapter(adapter);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error recoger almacenes", error.toString());
                muestraAlert("Error al conectar", "No se han podido cargar los almacenes.");

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
        stringRequest.setRetryPolicy((new DefaultRetryPolicy(10 * 1000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)));

    }

    public void siguiente(View view){

        muestraProgress();
        //
        //      Comprobación para no enviarse material a si mismo
        //

        final String almacenElegido = spinner.getSelectedItem().toString();

        Almacen a = mySqliteOpenHelper.getAlmacen(db, almacenElegido);
        final Almacen elegido = mySqliteOpenHelper.getElegido(db);

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_SALIDA_ALMACEN_CORRECTA,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Almacén destino", response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if(jsonObject.getString("content").equals("SI")){
                                progressDialog.dismiss();
                                Intent i = new Intent(SalidaAlmacen.this, CreaLineas.class);
                                i.putExtra("tipoRegistro", "SALIDA A ALMACÉN");
                                i.putExtra("almacenDestino", almacenElegido);
                                startActivity(i);
                            }
                            else {
                                progressDialog.dismiss();
                                muestraAlert("Destino incorrecto", "No puedes enviar material al almacén seleccionado");
                            }
                        } catch (JSONException e) {
                            progressDialog.dismiss();
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error recoger filtro", error.toString());
                progressDialog.dismiss();
                muestraAlert("Error al conectar", "Revisa tu conexión a internet y vuelve a intentarlo");

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                params.put("Authorization", "Bearer " + token);

                return params;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("origen", elegido.getAlmacen());
                params.put("destino", almacenElegido);
                return params;
            }
        };
        queue.add(stringRequest);


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

    public void muestraProgress(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Espere, por favor"); // Setting Message
        progressDialog.setTitle("Comprobando el almacén destino..."); // Setting Title
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
        progressDialog.show(); // Display Progress Dialog
        progressDialog.setCancelable(false);
        progressDialog.show();
    }
}
