package com.eiffage.almacenes.Activities.General;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.eiffage.almacenes.Activities.Almacen.OpcionesAlmacenero;
import com.eiffage.almacenes.Activities.JefeObra.OpcionesJefeObra;
import com.eiffage.almacenes.Activities.TrazabilidadLote.Trazabilidad;
import com.eiffage.almacenes.Objetos.Almacen;
import com.eiffage.almacenes.Objetos.MySqliteOpenHelper;
import com.eiffage.almacenes.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Menu extends AppCompatActivity {

    TextView nombreAlmacen;
    String token;

    MySqliteOpenHelper mySqliteOpenHelper;
    SQLiteDatabase db;

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_config, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.menuConfig:
                Intent i = new Intent(Menu.this, Configuracion.class);
                startActivity(i);
                break;
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        getSupportActionBar().setTitle("Menú principal");

        mySqliteOpenHelper = new MySqliteOpenHelper(this);
        db = mySqliteOpenHelper.getWritableDatabase();

        //
        //      Recuperar token
        //

        SharedPreferences myPrefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        token = myPrefs.getString("token", "Sin valor");
        actualizarAlmacen();

    }

    public void almaceneros(View view){
        Intent intent = new Intent(Menu.this, OpcionesAlmacenero.class);
        startActivity(intent);
    }

    public void consultaLote(View v){
        Intent i = new Intent(getApplicationContext(), Trazabilidad.class);
        startActivity(i);
    }

    public void traerAlmacenes(){
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, "http://82.223.65.75:8000/api_endesa/obtenerAlmacenes",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("LISTA ALMACENES", response);

                        try {
                            JSONObject jo = new JSONObject(response);
                            JSONArray jsonArray = jo.getJSONArray("content");

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String prov = jsonObject.getString("provincia");
                                String alm = "" + jsonObject.getString("almacen");

                                Almacen almacen = new Almacen(prov, alm);
                                mySqliteOpenHelper.insertarAlmacen(db, almacen);

                            }

                            actualizarAlmacen();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error recoger almacenes", error.toString());

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


    public void actualizarAlmacen(){
        nombreAlmacen = findViewById(R.id.nombreAlmacen);
        nombreAlmacen.setText("Cargando...");

        if(!mySqliteOpenHelper.isElegido(db)){
            nombreAlmacen.setText("-");
        }
        else {
            Almacen elegido = mySqliteOpenHelper.getElegido(db);
            nombreAlmacen.setText(elegido.getAlmacen());
        }
    }

    @Override
    protected void onRestart() {
        actualizarAlmacen();
        super.onRestart();
    }
}
