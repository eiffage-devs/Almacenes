package com.eiffage.almacenes.Activities.TrazabilidadLote;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.eiffage.almacenes.Activities.Almacen.ScannerActivity;
import com.eiffage.almacenes.Activities.General.ExpandableHeightListView;
import com.eiffage.almacenes.Adapters.FotosCargadasAdapter;
import com.eiffage.almacenes.Adapters.MovimientosAdapter;
import com.eiffage.almacenes.Objetos.Movimiento;
import com.eiffage.almacenes.R;

import org.json.JSONArray;
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

public class Trazabilidad extends AppCompatActivity {

    private String URL_INFO_LOTE = "-";
    private String URL_TRAZABILIDAD_LOTE = "-";
    private String URL_FOTOS_LOTE = "-";
    private String URL_ENVIAR_FOTO = "-";

    EditText lote;
    Button añadirFoto;
    ProgressDialog progressDialog;

    String buscarLote, token, mCurrentPhotoPath;

    LinearLayout containerBobina, containerCelda, containerTrafo;

    ExpandableHeightListView listaMovimientos;
    ArrayList<Movimiento> movimientos;
    MovimientosAdapter adapter;

    //Bobina
    TextView bobinaNumSerie, bobinaTipoLote, bobinaProducto, bobinaMarca, bobinaObservaciones;

    //Celda
    TextView celdaNumSerie, celdaTipoLote, celdaProducto, celdaMarca, celdaTensionAT, celdaINominal, celdaICortocircuito,
            celdaSeguimiento, celdaObservaciones;

    //Trafo
    TextView trafoNumSerie, trafoTipoLote, trafoProducto, trafoMarca, trafoPotencia, trafoTensionAT, trafoTensionBT, trafoPeso,
            trafoAño, trafoTipoLlenado, trafoPasatapas, trafoSeguimiento, trafoObservaciones;

    //
    //      Método para usar flecha de atrás en Action Bar
    //
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trazabilidad);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Consulta de lote");

        URL_INFO_LOTE = this.getResources().getString(R.string.urlObtenerInfoLote);
        URL_TRAZABILIDAD_LOTE = this.getResources().getString(R.string.urlObtenerMovimientosLote);
        URL_FOTOS_LOTE = this.getResources().getString(R.string.urlObtenerFotosLote);
        URL_ENVIAR_FOTO = this.getResources().getString(R.string.urlEnviarFoto);

        SharedPreferences myPrefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        token = myPrefs.getString("token", "Sin valor");

        lote = findViewById(R.id.lote);
        listaMovimientos = findViewById(R.id.listaTrazabilidad);

        containerBobina = findViewById(R.id.containerBobina);
        containerCelda = findViewById(R.id.containerCelda);
        containerTrafo = findViewById(R.id.containerTrafo);

        //Bobina
        bobinaNumSerie = findViewById(R.id.bobinaNumSerie);
        bobinaTipoLote = findViewById(R.id.bobinaTipoLote);
        bobinaProducto = findViewById(R.id.bobinaProducto);
        bobinaMarca = findViewById(R.id.bobinaMarca);
        bobinaObservaciones = findViewById(R.id.bobinaObservaciones);

        //Celda
        celdaNumSerie = findViewById(R.id.celdaNumSerie);
        celdaTipoLote = findViewById(R.id.celdaTipoLote);
        celdaProducto = findViewById(R.id.celdaProducto);
        celdaMarca = findViewById(R.id.celdaMarca);
        celdaTensionAT = findViewById(R.id.celdaTensionAT);
        celdaINominal = findViewById(R.id.celdaINominal);
        celdaICortocircuito = findViewById(R.id.celdaICortocircuito);
        celdaSeguimiento = findViewById(R.id.celdaSeguimiento);
        celdaObservaciones = findViewById(R.id.celdaObservaciones);

        //Trafo
        trafoNumSerie = findViewById(R.id.trafoNumSerie);
        trafoTipoLote = findViewById(R.id.trafoTipoLote);
        trafoProducto = findViewById(R.id.trafoProducto);
        trafoMarca = findViewById(R.id.trafoMarca);
        trafoPotencia = findViewById(R.id.trafoPotencia);
        trafoTensionAT = findViewById(R.id.trafoTensionAT);
        trafoTensionBT = findViewById(R.id.trafoTensionBT);
        trafoPeso = findViewById(R.id.trafoPeso);
        trafoAño = findViewById(R.id.trafoAño);
        trafoTipoLlenado = findViewById(R.id.trafoTipoLlenado);
        trafoPasatapas = findViewById(R.id.trafoPasatapas);
        trafoSeguimiento = findViewById(R.id.trafoSeguimiento);
        trafoObservaciones = findViewById(R.id.trafoObservaciones);

        movimientos = new ArrayList<>();
        añadirFoto = findViewById(R.id.btnAñadirFoto);

        //
        //      Pedir permisos de cámara para poder escanear
        //
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(Trazabilidad.this, new String[]{Manifest.permission.CAMERA}, 0);

        }
    }

    public void traerInfoLote(View view) {
        muestraLoader("Cargando información del lote...");
        buscarLote = lote.getText().toString();

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_INFO_LOTE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Resp INFO LOTE", response);
                        ocultarTeclado(Trazabilidad.this);

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String jsonArray = jsonObject.getString("content");
                            JSONArray js = new JSONArray(jsonArray);
                            JSONObject jsonObject1 = js.getJSONObject(0);

                            switch (jsonObject1.getString("tipoLote")) {
                                case "Trafo":
                                    cargarTrafo(jsonObject1);
                                    break;
                                case "Celda":
                                    cargarCelda(jsonObject1);
                                    break;
                                case "Bobina":
                                    cargarBobina(jsonObject1);
                            }
                            progressDialog.dismiss();
                            muestraLoader("Cargando trazabilidad del lote...");
                            traerTrazabilidadLote();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
                Log.d("Error traer info lote", error.toString());
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

                params.put("lote", buscarLote);
                return params;
            }
        };
        queue.add(stringRequest);
    }

    //
    //  Cargar BOBINA
    //
    public void cargarBobina(JSONObject response) {
        containerBobina.setVisibility(View.VISIBLE);
        containerCelda.setVisibility(View.GONE);
        containerTrafo.setVisibility(View.GONE);
        try {
            bobinaNumSerie.setText("Lote: " + response.getString("numSerie"));
            bobinaTipoLote.setText(response.getString("tipoLote"));
            bobinaProducto.setText(response.getString("numProd"));
            bobinaMarca.setText(response.getString("marca"));
            bobinaObservaciones.setText(response.getString("observaciones"));

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    //
    //  Cargar CELDA
    //
    public void cargarCelda(JSONObject response) {

        containerBobina.setVisibility(View.GONE);
        containerCelda.setVisibility(View.VISIBLE);
        containerTrafo.setVisibility(View.GONE);

        try {
            celdaNumSerie.setText("Lote: " + response.getString("numSerie"));
            celdaTipoLote.setText(response.getString("tipoLote"));
            celdaProducto.setText(response.getString("numProd"));
            celdaMarca.setText(response.getString("marca"));
            celdaTensionAT.setText(response.getString("tensionAT"));
            celdaINominal.setText(response.getString("intensidadNominal"));
            celdaICortocircuito.setText(response.getString("intensidadCortocircuito"));
            celdaSeguimiento.setText(response.getString("seguimiento"));
            celdaObservaciones.setText(response.getString("observaciones"));
        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    //
    //  Cargar TRAFO
    //
    public void cargarTrafo(JSONObject response) {
        containerBobina.setVisibility(View.GONE);
        containerCelda.setVisibility(View.GONE);
        containerTrafo.setVisibility(View.VISIBLE);

        try {
            trafoNumSerie.setText("Lote: " + response.getString("numSerie"));
            trafoTipoLote.setText(response.getString("tipoLote"));
            trafoProducto.setText(response.getString("numProd"));
            trafoMarca.setText(response.getString("marca"));
            trafoPotencia.setText(response.getString("potencia"));
            trafoTensionAT.setText(response.getString("tensionAT"));
            trafoTensionBT.setText(response.getString("tensionBT"));
            trafoPeso.setText(response.getString("peso"));
            trafoAño.setText(response.getString("ano"));
            trafoTipoLlenado.setText(response.getString("tipoLlenado"));
            trafoPasatapas.setText(response.getString("pasaTapas"));
            trafoSeguimiento.setText(response.getString("seguimiento"));
            trafoObservaciones.setText(response.getString("observaciones"));

        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void traerTrazabilidadLote() {
        movimientos = new ArrayList<>();
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_TRAZABILIDAD_LOTE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jo = new JSONObject(response);
                            Log.d("RESP TRAZABILIDAD", response);
                            JSONArray jsonArray = jo.getJSONArray("content");
                            if (jsonArray.length() == 0) {
                                Toast.makeText(getApplicationContext(), "No hay registros", Toast.LENGTH_SHORT).show();
                            } else {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);

                                    String tipoMov = jsonObject1.getString("tipoMov");
                                    if (tipoMov.equals("Devolución_a_Endesa")) {
                                        tipoMov = "Devolución\nEndesa";
                                    }
                                    String fecha = jsonObject1.getString("fecha");
                                    String origen = jsonObject1.getString("codOrigen");
                                    String destino = jsonObject1.getString("codDestino");
                                    String obra = jsonObject1.getString("codObra");
                                    String ticket = jsonObject1.getString("codTicket");

                                    Movimiento m = new Movimiento(tipoMov, fecha, origen, destino, obra, ticket);
                                    movimientos.add(m);
                                }
                                adapter = new MovimientosAdapter(Trazabilidad.this, movimientos);
                                listaMovimientos.setAdapter(adapter);
                            }

                            progressDialog.dismiss();
                            muestraLoader("Cargando fotos del lote...");
                            traerFotosLote();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
                Log.d("Error trazabilidad lote", error.toString());
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

                params.put("lote", buscarLote);
                return params;
            }
        };
        queue.add(stringRequest);
    }

    public void traerFotosLote() {
        final ArrayList<String> urlFotos = new ArrayList<>();
        final ExpandableHeightListView listaFotos = findViewById(R.id.listaFotosLote);

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_FOTOS_LOTE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Respuesta fotos", response);
                        progressDialog.dismiss();
                        try {
                            JSONObject jo = new JSONObject(response);

                            JSONArray jsonArray = jo.getJSONArray("content");
                            if (jsonArray.length() == 0) {
                                Toast.makeText(getApplicationContext(), "No hay fotos", Toast.LENGTH_SHORT).show();
                            } else {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);

                                    String rutaSim = jsonObject1.getString("rutaSim");

                                    urlFotos.add(rutaSim);
                                }
                                FotosCargadasAdapter adapterFotos = new FotosCargadasAdapter(Trazabilidad.this, urlFotos);


                                listaFotos.setAdapter(adapterFotos);
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

                            }

                            progressDialog.dismiss();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
                Log.d("Error fotos lote", error.toString());
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

                params.put("lote", buscarLote);
                return params;
            }
        };
        queue.add(stringRequest);
    }


    //
    //      Botón de escanear
    //
    public void escanear(View view) {
        Intent i = new Intent(Trazabilidad.this, ScannerActivity.class);
        startActivityForResult(i, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //
        //      Escáner
        //
        if (requestCode == 0) {
            if (resultCode == Activity.RESULT_OK) {
                lote.setText(data.getStringExtra("codigo"));
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "No se ha capturado ningún código de lote", Toast.LENGTH_SHORT).show();
            }
        }
        //  ------------------------------------
        //              TEMPORAL
        //  ------------------------------------
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
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
                } else {
                    Bitmap bitmap1 = Bitmap.createScaledBitmap(bitmap, 1263, 893, true);
                    Matrix matrix = new Matrix();

                    matrix.postRotate(90);
                    nuevaFoto = Bitmap.createBitmap(bitmap1, 0, 0, bitmap1.getWidth(), bitmap1.getHeight(), matrix, true);

                    bitmap.recycle();
                    bitmap1.recycle();
                }

                enviarFoto(nuevaFoto);
            }
        }

        if(requestCode == 2){
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
                    enviarFoto(nuevaFoto);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(Trazabilidad.this, "No se puede enviar la foto", Toast.LENGTH_LONG).show();
                }
            }
            else {
                Toast.makeText(Trazabilidad.this, "No se ha seleccionado ninguna foto", Toast.LENGTH_LONG).show();
            }

        }
    }

    public static void ocultarTeclado(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void muestraLoader(String mensaje) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(mensaje);
        progressDialog.show();
    }

    //  -----------------------------------------------------
    //  -----------------------------------------------------
    //  FUNCIÓN TEMPORAL --> AÑADIR FOTOS A LOTE YA EXISTENTE
    //  -----------------------------------------------------
    //  -----------------------------------------------------

    public void añadirFoto(View view) {

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
                Uri photoURI = FileProvider.getUriForFile(Trazabilidad.this,
                        "com.eiffage.almacenes",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, 1);
            }
        }
    }

    public void añadirFotoDesdeGaleria(View view) {

        Intent getPictureIntent = new Intent(Intent.ACTION_PICK);
        getPictureIntent.setType("image/");
        startActivityForResult(getPictureIntent, 2);
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

    public void enviarFoto(Bitmap bitmap) {
        muestraLoader("Enviando fotos...");
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
        byte[] byteArray = stream.toByteArray();
        final String encodedImage = "holapaco, " + Base64.encodeToString(byteArray, Base64.DEFAULT);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_ENVIAR_FOTO,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        Log.d("Envio foto", response);
                        traerTrazabilidadLote();
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Log.d(" ERROR RESPONSE", error.toString());
                Toast.makeText(getApplicationContext(), "Error envio foto: " + error.toString(), Toast.LENGTH_SHORT).show();
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
                params.put("lote", buscarLote);
                params.put("foto", encodedImage);

                return params;
            }
        };
        stringRequest.setTag("ENVIO_FOTOS");
        stringRequest.setRetryPolicy((new DefaultRetryPolicy(10 * 1000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)));

        queue.add(stringRequest);
    }
}

