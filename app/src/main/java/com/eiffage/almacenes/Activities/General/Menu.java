package com.eiffage.almacenes.Activities.General;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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

    private String URL_ULTIMA_VERSION = "-";

    TextView nombreAlmacen;
    String token;
    String usuario,empresa,cod_recurso,versionApp;

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
            case R.id.menuLogout:
                AlertDialog.Builder alertdialogobuilder = new AlertDialog.Builder(this);
                alertdialogobuilder
                        .setTitle("Confirmar cierre de sesión")
                        .setMessage("¿Seguro que deseas salir?")
                        .setCancelable(true)
                        .setPositiveButton("Cerrar sesión", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                SharedPreferences.Editor editor = getSharedPreferences("myPrefs", MODE_PRIVATE).edit();
                                editor.putString("token", "Sin valor");
                                editor.putString("usuario", "-");
                                editor.putString("cod_recurso", "-");
                                editor.putString("delegacion", "-");
                                editor.putString("nombre", "-");
                                editor.putString("empresa", "-");
                                editor.putString("email", "-");
                                editor.apply();

                                Intent i = new Intent(Menu.this, Login.class);
                                startActivity(i);
                                finish();
                            }
                        })
                        .setNegativeButton("En otro momento", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .create();
                alertdialogobuilder.show();
                break;
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        URL_ULTIMA_VERSION = getResources().getString(R.string.urlObtenerUltimaVersion);

        getSupportActionBar().setTitle("EE Almacenes");


        mySqliteOpenHelper = new MySqliteOpenHelper(this);
        db = mySqliteOpenHelper.getWritableDatabase();

        //
        //      Recuperar token
        //

        SharedPreferences myPrefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        token = myPrefs.getString("token", "Sin valor");
        usuario = myPrefs.getString("usuario", "-");
        empresa = myPrefs.getString("empresa", "-");
        cod_recurso = myPrefs.getString("cod_recurso", "-");

        actualizarAlmacen();

        try{
            PackageInfo packageInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            int buildVersion = packageInfo.versionCode;
            versionApp = packageInfo.versionName + "(" + buildVersion + ")" ;
            pedirUltimaVersion("" + buildVersion);
            Log.d("Version code", buildVersion + "");
        }catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }

        insertarMetrica("Acceso", String.valueOf(nombreAlmacen.getText()));
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
                //params.put("Content-Type", "application/json");
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

    public void pedirUltimaVersion(final String local){
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL_ULTIMA_VERSION,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("RESP ULTIMA VERSION", response);
                        try {
                            JSONObject jo = new JSONObject(response);
                            String ultima = jo.getString("content");

                            if(!ultima.equals(local)){
                                final AlertDialog.Builder builder = new AlertDialog.Builder(Menu.this);
                                builder.setTitle("Actualización disponible")
                                        .setMessage("Hay una nueva versión disponible de la aplicación.\n\t¿Quieres actualizarla ahora?")
                                        .setCancelable(false)
                                        .setNegativeButton("En otro momento", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        })
                                        . setPositiveButton("Actualizar", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.eiffage.almacenes"); // missing 'http://' will cause crashed
                                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                                startActivity(intent);
                                            }
                                        });
                                builder.show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error VERSION APP", error.toString());

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                //params.put("Content-Type", "application/json");
                params.put("Authorization", "Bearer " + token);

                return params;
            }
        };
        queue.add(stringRequest);
        stringRequest.setRetryPolicy((new DefaultRetryPolicy(10 * 1000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)));

    }

    public void insertarMetrica(final String tipo, final String subtipo){

        String URL_BASE = "https://serviciontg.energia.eiffage.es/";
        String URL_INSERTAR_METRICA = "api/codeunits/wsgestion/InsertarMetrica";

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = URL_BASE + URL_INSERTAR_METRICA;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("metrica resp", response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("metrica err", error.networkResponse + "");
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                return params;
            }

            @Override
            public Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("usuarioApp", "eiffGest");
                params.put("passwordApp", "U6ObJm9iwHWjYxL");
                params.put("empresa", empresa);
                params.put("usuario", cod_recurso);
                params.put("tipo", tipo);
                params.put("subtipo", subtipo);
                params.put("so", "android");
                params.put("version", versionApp);
                params.put("aplicacion", "almacenes");

                Log.d("metrica", params.toString());
                return params;
            }
        };
        queue.add(stringRequest);
    }

}
