package com.eiffage.almacenes.Activities.Almacen.TiposRegistro;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import androidx.core.view.MenuItemCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.eiffage.almacenes.Activities.Almacen.CreaLineas;
import com.eiffage.almacenes.Objetos.OT;
import com.eiffage.almacenes.Adapters.OTAdapter;
import com.eiffage.almacenes.Objetos.MySqliteOpenHelper;
import com.eiffage.almacenes.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SalidaObra extends AppCompatActivity implements SearchView.OnQueryTextListener{

    private String URL_FILTROS_ALMACEN = "-";

    SharedPreferences myPrefs;
    String token;
    OTAdapter otAdapter;

    ProgressDialog progressDialog;
    ListView listaOTs;
    String almacen, filtro;
    ArrayList<OT> ots;

    String tipoRegistro, ticketSCM, tecnicoEndesa;


    //Método para usar flecha de atrás en Action Bar
    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        final MenuItem searchItem = menu.findItem(R.id.menuSearch);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint("Código de OT");
        searchView.setOnQueryTextListener(this);

        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salida_obra);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Parámetros generales");

        URL_FILTROS_ALMACEN = this.getResources().getString(R.string.urlFiltrosAlmacen);

        MySqliteOpenHelper mySqliteOpenHelper = new MySqliteOpenHelper(this);
        SQLiteDatabase db = mySqliteOpenHelper.getWritableDatabase();

        Intent i = getIntent();
        tipoRegistro = i.getStringExtra("tipoRegistro");
        ticketSCM = i.getStringExtra("ticketSCM");

        if(i.getStringExtra("tecnicoEndesa") != null){
            tecnicoEndesa = i.getStringExtra("tecnicoEndesa");
        }

        listaOTs = findViewById(R.id.listaOT);
        listaOTs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(SalidaObra.this, CreaLineas.class);
                intent.putExtra("tipoRegistro", tipoRegistro);
                intent.putExtra("otElegida", ots.get(position).getCodOT());
                intent.putExtra("incidenciaElegida", "-");
                intent.putExtra("ticketSCM", ticketSCM);
                if(tipoRegistro.equals("RESERVA PARA OBRA"))
                    intent.putExtra("tecnicoEndesa", tecnicoEndesa);
                startActivity(intent);
            }
        });

        //
        //      Recuperar token
        //

        myPrefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        token = myPrefs.getString("token", "Sin valor");

        //
        //      Obtener código Nav del almacén
        //

        almacen = mySqliteOpenHelper.getElegido(db).getAlmacen();
        pedirFiltroBusqueda();
    }

    public void pedirFiltroBusqueda(){
        AlertDialog.Builder alertdialogobuilder = new AlertDialog.Builder(this);
        alertdialogobuilder
                .setTitle("¿Quieres filtrar la búsqueda?")
                .setMessage("Filtro (mínimo 4 caracteres):")
                .setCancelable(false);

        final EditText input = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertdialogobuilder.setView(input);
        alertdialogobuilder.setPositiveButton("Filtrar búsqueda", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        filtro = input.getText().toString();
                        if(filtro.length() < 4 || filtro.length() > 6) {
                            Toast.makeText(getApplicationContext(), "El filtro debe tener entre 4 y 6 caracteres.\n Trayendo todas las OT disponibles", Toast.LENGTH_SHORT).show();
                            filtro = "";
                        }
                            muestraProgress();
                            obtenerListado(almacen);

                    }
                })
                .setNegativeButton("No filtrar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        filtro = "";
                        dialog.dismiss();
                        muestraProgress();
                        obtenerListado(almacen);
                    }
                })
                .create();
        if (!isFinishing()) {
            alertdialogobuilder.show();
        }
    }

    public void obtenerListado(final String almacen) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_FILTROS_ALMACEN,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("LISTA OT", response);

                        //
                        //      Pintar la lista en la Activity
                        //

                        pintarLista(response);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error recoger filtro", error.toString());
                progressDialog.dismiss();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                //params.put("Content-Type", "application/json");
                params.put("Authorization", "Bearer " + token);

                return params;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("almacen", almacen);
                params.put("manual", filtro);
                return params;
            }
        };
        queue.add(stringRequest);
        stringRequest.setRetryPolicy((new DefaultRetryPolicy(10 * 1000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)));
    }


    public void pintarLista(String response){
        ots = new ArrayList<>();
           try {
                JSONObject jo = new JSONObject(response);
                JSONArray jsonArray = jo.getJSONArray("content");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String denominacion = jsonObject.getString("denominacion");
                    String codOT = "" + jsonObject.getString("CodOT");
                    String codObra = "" + jsonObject.getString("CodObra");


                    OT o = new OT(denominacion, codOT, codObra);
                    ots.add(o);

                    progressDialog.dismiss();
                }
               otAdapter = new OTAdapter(getApplicationContext(), ots);
               listaOTs.setAdapter(otAdapter);
               Log.d("Líneas mostradas", ots.size() + "");

            } catch (JSONException e) {
                e.printStackTrace();
                progressDialog.dismiss();
            }
    }

    public void muestraProgress(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Espere, por favor"); // Setting Message
        progressDialog.setTitle("Recuperando OT's disponibles..."); // Setting Title
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
        progressDialog.show(); // Display Progress Dialog
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public void codigoManual(View view){
        AlertDialog.Builder alertdialogobuilder = new AlertDialog.Builder(this);
        alertdialogobuilder
                .setTitle("¿Quieres usar una OT que no aparece?")
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

                Intent intent = new Intent(SalidaObra.this, CreaLineas.class);
                intent.putExtra("tipoRegistro", tipoRegistro);
                intent.putExtra("otElegida", codOT);
                intent.putExtra("incidenciaElegida", "-");
                intent.putExtra("ticketSCM", ticketSCM);
                if(tipoRegistro.equals("RESERVA PARA OBRA"))
                    intent.putExtra("tecnicoEndesa", tecnicoEndesa);
                startActivity(intent);
            }
        })

                .create();
        if (!isFinishing()) {
            alertdialogobuilder.show();
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        otAdapter.getFilter().filter(newText.toLowerCase());
        return false;
    }

}
