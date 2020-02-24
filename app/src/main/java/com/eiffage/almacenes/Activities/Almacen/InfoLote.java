package com.eiffage.almacenes.Activities.Almacen;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.icu.text.IDNA;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.eiffage.almacenes.Activities.General.Configuracion;
import com.eiffage.almacenes.Activities.General.ExpandableHeightListView;
import com.eiffage.almacenes.Activities.TrazabilidadLote.Trazabilidad;
import com.eiffage.almacenes.Adapters.ListaFotosAdapter;
import com.eiffage.almacenes.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class InfoLote extends AppCompatActivity {

    String cod_lote, num_serie, token, mCurrentPhotoPath;
    boolean esBobina;
    Spinner tipoLote;

    TextView numSerie;

    TextView labelMarca, labelPotencia, labelTensionAT, labelTensionBT, labelPeso, labelAño, labelObservaciones, labelIntensidadNominal,
            labelIntensidadCortocircuito, labelTipoLlenado, labelPasatapas, labelSeguimiento, labelMetrosCable, labelCodigoCable, descripcionCable;
    EditText marca, potencia, tensionAT, tensionBT, peso, año, observaciones, intensidadNominal, intensidadCortocircuito, metrosCable, codigoCable;

    Spinner  tipoLlenado, pasatapas, seguimiento;
    RelativeLayout relativeTipoLlenado, relativePasatapas, relativeSeguimiento;

    ArrayList<Bitmap> fotos;
    ArrayList<String> urlFotos;
    ExpandableHeightListView listaFotos;
    ListAdapter listaFotosAdapter;

    ProgressDialog progressDialog;
    Button escanear;
    //
    //      Método para usar flecha de atrás en Action Bar
    //
    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_lote);

        Intent i = getIntent();
        cod_lote = i.getStringExtra("cod_lote");
        num_serie = i.getStringExtra("num_serie");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Lote: " + cod_lote);

        SharedPreferences myPrefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        token = myPrefs.getString("token", "Sin valor");

        //
        //      Identificamos los elementos de la vista
        //
        tipoLote = findViewById(R.id.tipoLote);
        numSerie = findViewById(R.id.numSerie);
        numSerie.setText(num_serie);
        labelMarca = findViewById(R.id.labelMarca);
        marca = findViewById(R.id.marca);
        labelCodigoCable = findViewById(R.id.labelCodigoCable);
        codigoCable = findViewById(R.id.codigoCable);
        descripcionCable = findViewById(R.id.descripcionCable);
        labelMetrosCable = findViewById(R.id.labelMetros);
        escanear = findViewById(R.id.btnEscanear);
        metrosCable = findViewById(R.id.metrosCable);
        labelObservaciones = findViewById(R.id.labelObservaciones);
        observaciones = findViewById(R.id.observaciones);

        relativeTipoLlenado = findViewById(R.id.relative2);
        relativePasatapas = findViewById(R.id.relative3);
        relativeSeguimiento = findViewById(R.id.relative4);
        listaFotos = findViewById(R.id.listaFotos);

        codigoCable.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    // code to execute when EditText loses focus
                    if(codigoCable.getText().toString().length() == 6){
                        obtenerDescArticulo();
                    }
                    else
                        descripcionCable.setText("");
                }
            }
        });

        fotos = new ArrayList<>();
        urlFotos = new ArrayList<>();

        listaFotos.setScrollContainer(false);
        listaFotos.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(false);
                return false;
            }
        });


        //
        //      Cuando tipoLote = TRAFO
        //
        labelPotencia = findViewById(R.id.labelPotencia);
        labelTensionAT = findViewById(R.id.labelTensionAT); //También en Celdas
        labelTensionBT = findViewById(R.id.labelTensionBT);
        labelPeso = findViewById(R.id.labelPeso);
        labelAño = findViewById(R.id.labelAño);
        labelTipoLlenado = findViewById(R.id.labelTipoLlenado);
        labelPasatapas = findViewById(R.id.labelPasatapas);
        labelSeguimiento = findViewById(R.id.labelSeguimiento);

        potencia = findViewById(R.id.potencia);
        tensionAT = findViewById(R.id.tensionAT);   //También en Celdas
        tensionBT = findViewById(R.id.tensionBT);
        peso = findViewById(R.id.peso);
        año = findViewById(R.id.año);

        tipoLlenado = findViewById(R.id.tipoLlenado);
        pasatapas = findViewById(R.id.pasaTapas);
        seguimiento = findViewById(R.id.seguimiento);       //También en Celdas

        //
        //      Cuando tipoLote = CELDA
        //
        labelIntensidadNominal = findViewById(R.id.labelIntensidadNominal);
        labelIntensidadCortocircuito = findViewById(R.id.labelIntensidadCortocircuito);

        intensidadNominal = findViewById(R.id.intensidadNominal);
        intensidadCortocircuito = findViewById(R.id.intensidadCortocircuito);



        ArrayAdapter<String> adapter= new ArrayAdapter<>(InfoLote.this,android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.tipoLote));
        tipoLote.setAdapter(adapter);

        //
        //      Mostrar los campos que correspondan en función del TIPO LOTE
        //
        tipoLote.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(tipoLote.getSelectedItem().toString().equals("Bobina")){

                    esBobina = true;

                    labelCodigoCable.setVisibility(View.VISIBLE);
                    codigoCable.setVisibility(View.VISIBLE);
                    descripcionCable.setVisibility(View.VISIBLE);
                    escanear.setVisibility(View.VISIBLE);
                    labelMetrosCable.setVisibility(View.VISIBLE);
                    metrosCable.setVisibility(View.VISIBLE);

                    //Quitamos trafos
                    labelPotencia.setVisibility(View.GONE);
                    labelTensionAT.setVisibility(View.GONE);
                    labelTensionBT.setVisibility(View.GONE);
                    labelPeso.setVisibility(View.GONE);
                    labelAño.setVisibility(View.GONE);
                    labelTipoLlenado.setVisibility(View.GONE);
                    labelPasatapas.setVisibility(View.GONE);
                    labelSeguimiento.setVisibility(View.GONE);

                    potencia.setVisibility(View.GONE);
                    tensionAT.setVisibility(View.GONE);
                    tensionBT.setVisibility(View.GONE);
                    peso.setVisibility(View.GONE);
                    año.setVisibility(View.GONE);
                    tipoLlenado.setVisibility(View.GONE);
                    pasatapas.setVisibility(View.GONE);
                    seguimiento.setVisibility(View.GONE);
                    relativeTipoLlenado.setVisibility(View.GONE);
                    relativePasatapas.setVisibility(View.GONE);
                    relativeSeguimiento.setVisibility(View.GONE);

                    //Quitamos celdas
                    labelTensionAT.setVisibility(View.GONE);
                    labelIntensidadNominal.setVisibility(View.GONE);
                    labelIntensidadCortocircuito.setVisibility(View.GONE);

                    tensionAT.setVisibility(View.GONE);
                    intensidadNominal.setVisibility(View.GONE);
                    intensidadCortocircuito.setVisibility(View.GONE);
                }
                else if(tipoLote.getSelectedItem().toString().equals("Celda")){

                    esBobina = false;

                    labelCodigoCable.setVisibility(View.GONE);
                    codigoCable.setVisibility(View.GONE);
                    descripcionCable.setVisibility(View.GONE);
                    escanear.setVisibility(View.GONE);
                    labelMetrosCable.setVisibility(View.GONE);
                    metrosCable.setVisibility(View.GONE);

                    //Quitamos trafos
                    labelPotencia.setVisibility(View.GONE);
                    labelTensionAT.setVisibility(View.GONE);
                    labelTensionBT.setVisibility(View.GONE);
                    labelPeso.setVisibility(View.GONE);
                    labelAño.setVisibility(View.GONE);
                    labelTipoLlenado.setVisibility(View.GONE);
                    labelPasatapas.setVisibility(View.GONE);
                    labelSeguimiento.setVisibility(View.GONE);

                    potencia.setVisibility(View.GONE);
                    tensionAT.setVisibility(View.GONE);
                    tensionBT.setVisibility(View.GONE);
                    peso.setVisibility(View.GONE);
                    año.setVisibility(View.GONE);
                    tipoLlenado.setVisibility(View.GONE);
                    pasatapas.setVisibility(View.GONE);
                    seguimiento.setVisibility(View.GONE);
                    relativeTipoLlenado.setVisibility(View.GONE);
                    relativePasatapas.setVisibility(View.GONE);
                    relativeSeguimiento.setVisibility(View.GONE);

                    //Mostramos celdas
                    labelTensionAT.setVisibility(View.VISIBLE);
                    labelIntensidadNominal.setVisibility(View.VISIBLE);
                    labelIntensidadCortocircuito.setVisibility(View.VISIBLE);
                    labelSeguimiento.setVisibility(View.VISIBLE);

                    tensionAT.setVisibility(View.VISIBLE);
                    intensidadNominal.setVisibility(View.VISIBLE);
                    intensidadCortocircuito.setVisibility(View.VISIBLE);
                    relativeSeguimiento.setVisibility(View.VISIBLE);
                    seguimiento.setVisibility(View.VISIBLE);

                    ArrayAdapter<String> adapter= new ArrayAdapter<>(InfoLote.this,android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.seguimientoCelda));
                    seguimiento.setAdapter(adapter);

                }
                else if(tipoLote.getSelectedItem().toString().equals("Trafo")){

                    esBobina = false;

                    labelCodigoCable.setVisibility(View.GONE);
                    codigoCable.setVisibility(View.GONE);
                    descripcionCable.setVisibility(View.GONE);
                    escanear.setVisibility(View.GONE);
                    labelMetrosCable.setVisibility(View.GONE);
                    metrosCable.setVisibility(View.GONE);

                    //Quitamos celdas
                    labelTensionAT.setVisibility(View.GONE);
                    labelIntensidadNominal.setVisibility(View.GONE);
                    labelIntensidadCortocircuito.setVisibility(View.GONE);

                    tensionAT.setVisibility(View.GONE);
                    intensidadNominal.setVisibility(View.GONE);
                    intensidadCortocircuito.setVisibility(View.GONE);

                    //Ponemos trafos
                    labelPotencia.setVisibility(View.VISIBLE);
                    labelTensionAT.setVisibility(View.VISIBLE);
                    labelTensionBT.setVisibility(View.VISIBLE);
                    labelPeso.setVisibility(View.VISIBLE);
                    labelAño.setVisibility(View.VISIBLE);
                    labelTipoLlenado.setVisibility(View.VISIBLE);
                    labelPasatapas.setVisibility(View.VISIBLE);
                    labelSeguimiento.setVisibility(View.VISIBLE);

                    potencia.setVisibility(View.VISIBLE);
                    tensionAT.setVisibility(View.VISIBLE);
                    tensionBT.setVisibility(View.VISIBLE);
                    peso.setVisibility(View.VISIBLE);
                    año.setVisibility(View.VISIBLE);
                    tipoLlenado.setVisibility(View.VISIBLE);
                    pasatapas.setVisibility(View.VISIBLE);
                    seguimiento.setVisibility(View.VISIBLE);
                    relativeTipoLlenado.setVisibility(View.VISIBLE);
                    relativePasatapas.setVisibility(View.VISIBLE);
                    relativeSeguimiento.setVisibility(View.VISIBLE);

                    ArrayAdapter<String> adapter1= new ArrayAdapter<>(InfoLote.this,android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.seguimientoTrafo));
                    seguimiento.setAdapter(adapter1);

                    ArrayAdapter<String> adapter2= new ArrayAdapter<>(InfoLote.this,android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.tipoLlenado));
                    tipoLlenado.setAdapter(adapter2);

                    ArrayAdapter<String> adapter3= new ArrayAdapter<>(InfoLote.this,android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.pasatapas));
                    pasatapas.setAdapter(adapter3);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        //obtenerInfoLote();
    }

    //A día de hoy no se utiliza
    public void obtenerInfoLote(){

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://82.223.65.75:8000/api_endesa/obtenerInfoLote",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Respuesta info lote", response);

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String resp = jsonObject.getString("content");

                            String tempTipoLote = resp.substring(0,1);
                            switch (tempTipoLote){
                                case "B":
                                    tipoLote.setSelection(0);

                                    String[] partsB = resp.split("\\;");
                                    marca.setText(partsB[1]);
                                    observaciones.setText(partsB[2]);
                                    break;
                                case "C":
                                    tipoLote.setSelection(1);

                                    String[] partsC = resp.split("\\;");
                                    marca.setText(partsC[1]);
                                    tensionAT.setText(partsC[2]);
                                    intensidadNominal.setText(partsC[3]);
                                    intensidadCortocircuito.setText(partsC[4]);

                                    String tempSeguimientoC = partsC[5];
                                    switch (tempSeguimientoC){
                                        case "PDI":
                                            seguimiento.setSelection(0);
                                            break;
                                        case "Reutilizable":
                                            seguimiento.setSelection(1);
                                            break;
                                        case "Averiado":
                                            seguimiento.setSelection(2);
                                            break;
                                        case "Chatarra":
                                            seguimiento.setSelection(3);
                                            break;
                                    }

                                    observaciones.setText(partsC[6]);
                                    break;
                                case "T":
                                    tipoLote.setSelection(2);

                                    String[] partsT = resp.split("\\;"); // String array, each element is text between dots

                                    marca.setText(partsT[1]);
                                    potencia.setText(partsT[2]);
                                    tensionAT.setText(partsT[3]);
                                    tensionBT.setText(partsT[4]);
                                    peso.setText(partsT[5]);
                                    año.setText(partsT[6]);

                                    String tempTipoLlenado = partsT[7];
                                    switch (tempTipoLlenado){
                                        case "Integral":
                                            tipoLlenado.setSelection(0);
                                            break;
                                        case "Depósito expansión":
                                            tipoLlenado.setSelection(1);
                                            break;
                                        case "Hermético":
                                            tipoLlenado.setSelection(2);
                                            break;
                                    }


                                    String tempPasatapas = partsT[8];
                                    switch (tempPasatapas){
                                        case "Abierto":
                                            pasatapas.setSelection(0);
                                            break;
                                        case "Enchufable":
                                            pasatapas.setSelection(1);
                                            break;
                                    }

                                    String tempSeguimiento = partsT[9];
                                    switch (tempSeguimiento){
                                        case "PDI":
                                            seguimiento.setSelection(0);
                                            break;
                                        case "Reutilizable":
                                            seguimiento.setSelection(1);
                                            break;
                                        case "Averiado":
                                            seguimiento.setSelection(2);
                                            break;
                                        case "Chatarra":
                                            seguimiento.setSelection(3);
                                            break;
                                        case "Contaminado":
                                            seguimiento.setSelection(4);
                                            break;
                                    }

                                    if(!partsT[10].equals("-"))
                                        observaciones.setText(partsT[10]);

                                    break;
                                case "X":
                                    break;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error info lote", error.toString());
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
                params.put("producto", num_serie);
                params.put("lote", cod_lote);
                return params;
            }
        };
        queue.add(stringRequest);
    }

    public void enviarDatosLote(View v){
        //URL Servidor de producción --> http://82.223.65.75:8000/api_endesa/confirmarLote_v2
        if(validarCampos()){
            muestraLoader("Enviando información del lote...");
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://82.223.65.75:8000/api_endesa/confirmarLote_v2",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("Resp confirmar lote", response);
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                String resp = jsonObject.getString("content");
                                if(resp.equals("OK")){
                                    progressDialog.dismiss();
                                    enviarFotos();
                                }
                                else {
                                    progressDialog.dismiss();
                                    Toast.makeText(getApplicationContext(), "Error confirmando info lote. Fotos no enviadas", Toast.LENGTH_SHORT).show();
                                }
                            }
                            catch (JSONException e){
                                progressDialog.dismiss();
                                e.printStackTrace();
                            }

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    progressDialog.dismiss();
                    Log.d("Error confirmar lote", error.toString());
                    Toast.makeText(getApplicationContext(), "Error confirmar lote: " + error.toString(), Toast.LENGTH_SHORT).show();
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

                    params.put("lote", cod_lote);
                    params.put("tipoLote", tipoLote.getSelectedItem().toString());
                    params.put("producto", num_serie);
                    params.put("marca", marca.getText().toString());

                    if(codigoCable.getText().toString().length() > 0)
                        params.put("codigoCable", codigoCable.getText().toString());
                    else
                        params.put("codigoCable", "0");

                    if(metrosCable.getText().toString().length() > 0)
                        params.put("metrosCable", metrosCable.getText().toString());
                    else
                        params.put("metrosCable", "0");

                    if(observaciones.getText().toString().length() > 0)
                        params.put("observaciones", observaciones.getText().toString());
                    else
                        params.put("observaciones", "-");

                    if(tensionAT.getText().toString().length() > 0)
                        params.put("tensionAT", tensionAT.getText().toString());
                    else
                        params.put("tensionAT", "0");

                    if(intensidadNominal.getText().toString().length() > 0)
                        params.put("intensidadNominal", intensidadNominal.getText().toString());
                    else
                        params.put("intensidadNominal", "0");

                    if(intensidadCortocircuito.getText().toString().length() > 0)
                        params.put("intensidadCortocircuito", intensidadCortocircuito.getText().toString());
                    else
                        params.put("intensidadCortocircuito", "0");

                    if(seguimiento.getSelectedItem() != null)
                        params.put("seguimiento", seguimiento.getSelectedItem().toString());
                    else
                        params.put("seguimiento", "-");

                    if(potencia.getText().toString().length() > 0)
                        params.put("potencia", potencia.getText().toString());
                    else
                        params.put("potencia", "0");

                    if(tensionBT.getText().toString().length() > 0)
                        params.put("tensionBT", tensionBT.getText().toString());
                    else
                        params.put("tensionBT", "0");

                    if(peso.getText().toString().length() > 0)
                        params.put("peso", peso.getText().toString());
                    else
                        params.put("peso", "0");

                    if(año.getText().toString().length() > 0)
                        params.put("ano", año.getText().toString());
                    else
                        params.put("ano", "0");

                    if(tipoLlenado.getSelectedItem() != null)
                        params.put("tipoLlenado", tipoLlenado.getSelectedItem().toString());
                    else
                        params.put("tipoLlenado", "-");

                    if(pasatapas.getSelectedItem() != null)
                        params.put("pasaTapas", pasatapas.getSelectedItem().toString());
                    else
                        params.put("pasaTapas", "-");

                    return params;
                }
            };
            queue.add(stringRequest);
        }
    }

    public boolean validarCampos(){
        if(marca.getText().toString().length() <= 0){
            marca.requestFocus();
            marca.setError("Campo necesario");
            return false;
        }
        else {
            switch (tipoLote.getSelectedItem().toString()){
                case "Bobina":
                    if(codigoCable.getText().toString().length() <= 0){
                        codigoCable.requestFocus();
                        codigoCable.setError("Campo necesario");
                        return false;
                    }
                    else if(metrosCable.getText().toString().length() <= 0){
                        metrosCable.requestFocus();
                        metrosCable.setError("Campo necesario");
                        return false;
                    }
                    return true;

                case "Celda":
                    if(tensionAT.getText().toString().length() <= 0){
                        tensionAT.requestFocus();
                        tensionAT.setError("Campo necesario");
                        return false;
                    }
                    else if(intensidadNominal.getText().toString().length() <= 0){
                        intensidadNominal.requestFocus();
                        intensidadNominal.setError("Campo necesario");
                        return false;
                    }
                    else if(intensidadCortocircuito.getText().toString().length() <= 0){
                        intensidadCortocircuito.requestFocus();
                        intensidadCortocircuito.setError("Campo necesario");
                        return false;
                    }
                    return true;

                case "Trafo":
                    if(potencia.getText().toString().length() <= 0){
                        potencia.requestFocus();
                        potencia.setError("Campo necesario");
                        return false;
                    }
                    else if(tensionAT.getText().toString().length() <= 0){
                        tensionAT.requestFocus();
                        tensionAT.setError("Campo necesario");
                        return false;
                    }
                    else if(tensionBT.getText().toString().length() <= 0){
                        tensionBT.requestFocus();
                        tensionBT.setError("Campo necesario");
                        return false;
                    }
                    else if(peso.getText().toString().length() <= 0){
                        peso.requestFocus();
                        peso.setError("Campo necesario");
                        return false;
                    }
                    else if(año.getText().toString().length() <= 0){
                        año.requestFocus();
                        año.setError("Campo necesario");
                        return false;
                    }
                    return true;
            }
            return false;
        }
    }

    public void enviarFotos(){
        if(fotos.size() > 0){
            muestraLoader("Enviando fotos...");
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            for(int i = 0; i < fotos.size(); i++){
                final int j = i;
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                fotos.get(i).compress(Bitmap.CompressFormat.JPEG, 50, stream);
                byte[] byteArray = stream.toByteArray();
                final String encodedImage = "holapaco, " + Base64.encodeToString(byteArray, Base64.DEFAULT);

                StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://82.223.65.75:8000/api_endesa/creaFoto",
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                progressDialog.dismiss();
                                if(j == fotos.size() -1){
                                    progressDialog.dismiss();
                                    int posLote = response.indexOf("NumLote") + 12;
                                    int posId = response.indexOf("IdFoto") -7;
                                    try {
                                        Toast.makeText(getApplicationContext(), "Lote " + response.substring(posLote, posId) + " validado", Toast.LENGTH_SHORT).show();
                                        Log.d("Toast", "Lote " + response.substring(posLote, posId) + " validado");
                                        Intent returnIntent = new Intent(InfoLote.this, CreaLineas.class);
                                        if(esBobina){
                                            returnIntent.putExtra("codigoCable", codigoCable.getText().toString());
                                            returnIntent.putExtra("metrosCable", metrosCable.getText().toString());
                                        }
                                        setResult(Activity.RESULT_OK,returnIntent);
                                        finish();
                                    }
                                    catch (Exception e){
                                        Toast.makeText(getApplicationContext(), "Fallo al enviar la información: " + response, Toast.LENGTH_SHORT).show();
                                    }
                                }
                                Log.d("RESPONSE", response);


                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Log.d(" ERROR RESPONSE", error.toString());
                        Toast.makeText(getApplicationContext(), "Error envio fotos: " + error.toString(), Toast.LENGTH_SHORT).show();
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
                        params.put("lote", cod_lote);
                        params.put("foto", encodedImage);

                        return params;
                    }
                };
                stringRequest.setTag("ENVIO_FOTOS");
                stringRequest.setRetryPolicy((new DefaultRetryPolicy(60 * 1000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)));

                queue.add(stringRequest);
            }
        }
        else {
            Toast.makeText(InfoLote.this, "Se ha validado el lote, pero no se han adjuntado fotos.", Toast.LENGTH_SHORT).show();
            Intent returnIntent = new Intent(InfoLote.this, CreaLineas.class);
            if(esBobina){
                returnIntent.putExtra("codigoCable", codigoCable.getText().toString());
                returnIntent.putExtra("metrosCable", metrosCable.getText().toString());
            }
            setResult(Activity.RESULT_OK,returnIntent);
            finish();
        }

    }

    public void añadirFoto(View view){

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(InfoLote.this,
                        "com.eiffage.almacenes",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, 0);
            }
        }
    }

    public void añadirFotoDesdeGaleria(View view) {

        Intent getPictureIntent = new Intent(Intent.ACTION_PICK);
        getPictureIntent.setType("image/");
        startActivityForResult(getPictureIntent, 2);
    }

    //
    //      Botón de escanear
    //
    public void escanear(View view){
        Intent i = new Intent(InfoLote.this, ScannerActivity.class);
        startActivityForResult(i, 2);
    }

    //
    //De aquí para abajo nos ocupamos de la gestión de las fotos que se hacen en la activity, y de recoger el código escaneado
    //

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == 0){
            if(resultCode == RESULT_OK){
                Bitmap nuevaFoto;

                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                bmOptions.inJustDecodeBounds = true;

                // Decode the image file into a Bitmap sized to fill the View
                bmOptions.inJustDecodeBounds = false;
                bmOptions.inPurgeable = true;

                int photoW = bmOptions.outWidth;
                int photoH = bmOptions.outHeight;

                Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

                if (photoW < photoH) {
                    Bitmap bitmap1 = Bitmap.createScaledBitmap(bitmap, 893, 1263, true);
                    Matrix matrix = new Matrix();

                    matrix.postRotate(180);
                    nuevaFoto = Bitmap.createBitmap(bitmap1, 0, 0, bitmap1.getWidth(), bitmap1.getHeight(), matrix, true);

                    bitmap.recycle();
                    bitmap1.recycle();
                }
                else {
                    Bitmap bitmap1 = Bitmap.createScaledBitmap(bitmap, 1263, 893, true);
                    Matrix matrix = new Matrix();

                    matrix.postRotate(90);
                    nuevaFoto = Bitmap.createBitmap(bitmap1, 0, 0, bitmap1.getWidth(), bitmap1.getHeight(), matrix, true);

                    bitmap.recycle();
                    bitmap1.recycle();
                }

                fotos.add(nuevaFoto);
                urlFotos.add(mCurrentPhotoPath);
                listaFotosAdapter = new ListaFotosAdapter(this, fotos, urlFotos);
                listaFotos.setAdapter(listaFotosAdapter);
            }
        }
        if(requestCode == 1){
            if(resultCode == RESULT_OK){
                try {
                    final Uri imageUri = data.getData();
                    final InputStream imageStream = getContentResolver().openInputStream(imageUri);

                    Bitmap nuevaFoto;

                    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                    bmOptions.inJustDecodeBounds = true;

                    // Decode the image file into a Bitmap sized to fill the View
                    bmOptions.inJustDecodeBounds = false;
                    bmOptions.inPurgeable = true;

                    int photoW = bmOptions.outWidth;
                    int photoH = bmOptions.outHeight;

                    Bitmap bitmap = BitmapFactory.decodeStream(imageStream);

                    if (photoW < photoH) {
                        Bitmap bitmap1 = Bitmap.createScaledBitmap(bitmap, 893, 1263, true);
                        Matrix matrix = new Matrix();

                        matrix.postRotate(180);
                        nuevaFoto = Bitmap.createBitmap(bitmap1, 0, 0, bitmap1.getWidth(), bitmap1.getHeight(), matrix, true);

                        bitmap.recycle();
                        bitmap1.recycle();
                    } else {
                        Bitmap bitmap1 = Bitmap.createScaledBitmap(bitmap, 1263, 893, true);
                        Matrix matrix = new Matrix();

                        matrix.postRotate(90);
                        nuevaFoto = Bitmap.createBitmap(bitmap1, 0, 0, bitmap1.getWidth(), bitmap1.getHeight(), matrix, true);

                        bitmap.recycle();
                        bitmap1.recycle();
                    }
                    fotos.add(nuevaFoto);
                    urlFotos.add(mCurrentPhotoPath);
                    listaFotosAdapter = new ListaFotosAdapter(this, fotos, urlFotos);
                    listaFotos.setAdapter(listaFotosAdapter);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(InfoLote.this, "No se puede enviar la foto", Toast.LENGTH_LONG).show();
                }
            }
            else {
                Toast.makeText(InfoLote.this, "No se ha seleccionado ninguna foto", Toast.LENGTH_LONG).show();
            }
        }
        if(requestCode == 2){
            if(resultCode == Activity.RESULT_OK){
                //
                //      Si tiene longitud 6. es un código de artículo
                //
                if(data.getStringExtra("codigo").length() == 6){
                    codigoCable.setText(data.getStringExtra("codigo"));
                    obtenerDescArticulo();
                }

                //
                //      En caso contrario, es un código de lote
                //
                else {
                    Toast.makeText(InfoLote.this, "No se ha detectado un código de artículo válido", Toast.LENGTH_SHORT).show();
                }
            }
            else if(resultCode == Activity.RESULT_CANCELED){
                Toast.makeText(this, "No se ha capturado ningún código", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public void obtenerDescArticulo(){
        muestraLoader("Obteniendo información del artículo...");
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://82.223.65.75:8000/api_endesa/obtenerDescripcionMaterial",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        Log.d("Info material", response);
                        JSONObject  jo = null;

                        try {
                            jo = new JSONObject(response);
                            String res = jo.getString("desc");
                            if(res.contains("ERROR")){
                                codigoCable.setText("");
                                Toast.makeText(InfoLote.this, "No existe el material indicado", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                descripcionCable.setText(res);
                            }
                        } catch (JSONException e) {
                            progressDialog.dismiss();
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error info material", error.toString());
                progressDialog.dismiss();
                Toast.makeText(InfoLote.this, "No se ha podido recuperar la información del material", Toast.LENGTH_SHORT).show();
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
                params.put("codigo", codigoCable.getText().toString());
                return params;
            }
        };
        queue.add(stringRequest);
    }

    public void muestraLoader(String message){
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.show();
    }
}
