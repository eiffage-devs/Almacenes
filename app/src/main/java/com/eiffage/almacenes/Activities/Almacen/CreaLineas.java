package com.eiffage.almacenes.Activities.Almacen;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import com.eiffage.almacenes.Adapters.LineasAdapter;
import com.eiffage.almacenes.Objetos.Linea;
import com.eiffage.almacenes.Objetos.MySqliteOpenHelper;
import com.eiffage.almacenes.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CreaLineas extends AppCompatActivity {

    private String URL_CODIGO_ARTICULO = "-";
    private String URL_NECESITA_LOTE = "-";
    //-----------Elementos de la vista-----------
    ListView lineas;
    EditText codigo, unidades, lote;
    TextView tipoR, infoAdicional;
    Button nuevaLinea, enviaRegistros, infoLote;
    LinearLayout superior3;

    //-----------Variables generales------------
    ArrayList<Linea> lineasNuevas;
    SharedPreferences myPrefs;
    String token, desc;

    String urlFinal;
    ProgressDialog progressDialog;
    RequestQueue colaRegistros = null;
    boolean vieneConProducto = false;
    String codigoCable, metrosCable;

    boolean infoLoteValidada = false;

    //
    //  CAMPOS DE LOS DISTINTOS TIPOS DE REGISTRO
    //

    //-----------Comunes-------------------------
    String almacen, tipoRegistro, codMaterial, cantidad, loteMaterial;

    //-----------Entradas------------------------
    String albaran, etiqueta;
    private String urlEntradas = "-";

    //-----------Devoluciones a Endesa-----------
    private String urlDevolucionesEndesa = "-";

    //-----------Salidas a almacén---------------
    String almacenDestino;
    private String urlSalidasAlmacen = "-";

    //----------Salidas a obra------------------
    String incidenciaElegida, otElegida, ticketSCM;
    private String urlSalidasObra = "-";

    //---------Devoluciones de obra------------
    //Utilizamos las mismas variables String que en las salidas a obra
    private String urlDevolucionesObra = "-";

    //---------Reservas para obra------------
    String tecnicoEndesa;
    private String urlReservaObra = "-";

    //
    //      Método para usar flecha de atrás en Action Bar
    //
    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crea_lineas);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Nuevos movimientos");

        URL_CODIGO_ARTICULO = getResources().getString(R.string.urlObtenerCodigoArticulo);
        URL_NECESITA_LOTE = getResources().getString(R.string.urlNecesitaLote);
        urlEntradas = getResources().getString(R.string.urlRegistrarEntrada);
        urlDevolucionesEndesa = getResources().getString(R.string.urlRegistrarDevEmdesa);
        urlSalidasAlmacen = getResources().getString(R.string.urlRegistrarSalidaAlmacen);
        urlSalidasObra = getResources().getString(R.string.urlRegistrarSalidaObra);
        urlDevolucionesObra = getResources().getString(R.string.urlRegistrarDevObra);

        urlSalidasObra = getResources().getString(R.string.urlInsMovSalidaObraENDESA);
        urlReservaObra = getResources().getString(R.string.urlInsMovReservaObraENDESA);

        //
        //      Pedir permisos de cámara para poder escanear
        //
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(CreaLineas.this, new String[]{Manifest.permission.CAMERA}, 0);

        }

        //
        //      Recuperar token
        //
        myPrefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        token = myPrefs.getString("token", "Sin valor");


        //
        //      Identificamos elementos de la vista
        //
        lineas = findViewById(R.id.lineas);
        codigo = findViewById(R.id.codigo);
        unidades = findViewById(R.id.unidades);
        lote = findViewById(R.id.lote);
        nuevaLinea = findViewById(R.id.nuevaLinea);
        enviaRegistros = findViewById(R.id.enviaRegistros);
        tipoR = findViewById(R.id.tipoR);
        infoAdicional = findViewById(R.id.infoAdicional);
        superior3 = findViewById(R.id.superior3);
        infoLote = findViewById(R.id.infoLote);

        //
        //      Obtener el tipo de registro
        //
        Intent i = getIntent();
        tipoRegistro = i.getStringExtra("tipoRegistro");


        //
        //      Configurar la información en base al tipo de registro elegido
        //
        switch (tipoRegistro){
            case "ENTRADA":
                tipoR.setText("Tipo de registro:\nENTRADA");
                albaran = i.getStringExtra("albaran");
                etiqueta = i.getStringExtra("etiqueta");
                urlFinal = urlEntradas;

                infoAdicional.setText("Albarán:\n" + albaran);
                break;
            case "DEVOLUCIÓN A ENDESA":
                tipoR.setText("Tipo de registro:\nDEVOLUCIÓN A ENDESA");
                infoAdicional.setVisibility(View.GONE);
                break;
            case "SALIDA A ALMACÉN":
                tipoR.setText("Tipo de registro:\nSALIDA A ALMACÉN");
                almacenDestino = i.getStringExtra("almacenDestino");
                infoAdicional.setText("Almacén destino:\n" + almacenDestino);
                break;
            case "SALIDA A OBRA":
                tipoR.setText("Tipo de registro:\nSALIDA A OBRA");
                otElegida = i.getStringExtra("otElegida");
                incidenciaElegida = i.getStringExtra("incidenciaElegida");
                ticketSCM = i.getStringExtra("ticketSCM");

                if(otElegida.equals("-")){
                    infoAdicional.setText("Incidencia:\n" + incidenciaElegida);
                }
                else {
                    infoAdicional.setText("OT destino:\n" + otElegida);
                }
                break;
            case "DEVOLUCIÓN DE OBRA":
                tipoR.setText("Tipo de registro:\nDEVOLUCIÓN DE OBRA");
                otElegida = i.getStringExtra("otElegida");
                ticketSCM = i.getStringExtra("ticketSCM");
                incidenciaElegida = i.getStringExtra("incidenciaElegida");

                if(otElegida.equals("-")){
                    infoAdicional.setText("Incidencia:\n" + incidenciaElegida);
                }
                else {
                    infoAdicional.setText("OT origen:\n" + otElegida);
                }
                break;
            case "RESERVA PARA OBRA":
                tipoR.setText("Tipo de registro:\nRESERVA PARA OBRA");
                otElegida = i.getStringExtra("otElegida");
                incidenciaElegida = i.getStringExtra("incidenciaElegida");
                ticketSCM = i.getStringExtra("ticketSCM");
                tecnicoEndesa = i.getStringExtra("tecnicoEndesa");
                if(otElegida.equals("-")){
                    infoAdicional.setText("Incidencia:\n" + incidenciaElegida);
                }
                else {
                    infoAdicional.setText("OT destino:\n" + otElegida);
                }

                break;
        }

        lineasNuevas = new ArrayList<>();



    }

    //
    //      Botón de escanear
    //
    public void escanear(View view){
        Intent i = new Intent(CreaLineas.this, ScannerActivity.class);
        startActivityForResult(i, 0);
    }

    //
    //      Botón de info lote
    //
    public void abrirInfoLote(View view){
        if(lote.getText().toString().length() > 0){
            Intent i = new Intent(CreaLineas.this, InfoLote.class);
            i.putExtra("cod_lote", lote.getText().toString());
            i.putExtra("num_serie", codigo.getText().toString());
            startActivityForResult(i, 1);
        }
        else {
            lote.requestFocus();
            lote.setError("Campo necesario");

        }

    }

    //
    //      Al escanear y volver a la vista actual
    //
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //
        //      Escáner
        //
        if(requestCode == 0){
            if(resultCode == Activity.RESULT_OK){
                //
                //      Si tiene longitud 6. es un código de artículo
                //
                if(data.getStringExtra("codigo").length() == 6){
                    resetCampos();
                    codigo.setText(data.getStringExtra("codigo"));
                    necesitaLote(false);
                }

                //
                //      En caso contrario, es un código de lote
                //
                else {
                    lote.setText(data.getStringExtra("codigo"));

                    //
                    //      Mostramos el linearlayout con el botón de info lote
                    //
                    if(tipoRegistro.equals("ENTRADA"))
                        superior3.setVisibility(View.VISIBLE);

                    //
                    //      Obtener el código de artículo asociado al lote
                    //
                    obtenerCodigoArticulo(data.getStringExtra("codigo"));
                }
            }
            else if(resultCode == Activity.RESULT_CANCELED){
                Toast.makeText(this, "No se ha capturado ningún código", Toast.LENGTH_SHORT).show();
            }
        }
        //
        //      Info Lote
        //
        else if(requestCode == 1){
            if(resultCode == Activity.RESULT_OK){
                infoLoteValidada = true;
                nuevaLinea.setEnabled(true);
                vieneConProducto = false;


                try{
                    codigoCable = data.getStringExtra("codigoCable");
                    metrosCable = data.getStringExtra("metrosCable");
                    vieneConProducto = true;
                }
                catch (NullPointerException e){

                }
                necesitaLote(true);
            }
            else if(resultCode == Activity.RESULT_CANCELED){
                Toast.makeText(this, "No se ha validado la información del lote", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void insertarProductoCable(){
        codigo.setText(codigoCable);
        unidades.setText(metrosCable);
        vieneConProducto = false;
        necesitaLote(true);
        superior3.setVisibility(View.GONE);
    }

    public void obtenerCodigoArticulo(final String codigoLote){
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_CODIGO_ARTICULO,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("CODIGO MATERIAL", response);
                        JSONObject  jo = null;

                        try {
                            jo = new JSONObject(response);
                            String res = jo.getString("content");
                            if(res.contains("ERROR")){
                                muestraAlert("Error al recuperar el código de material", res);
                                resetCampos();
                            }
                            else {
                                codigo.setText(res);
                                unidades.setText("1");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error obtener cod", error.toString());
                nuevaLinea.setEnabled(true);
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
                params.put("lote", codigoLote);
                return params;
            }
        };
        queue.add(stringRequest);
    }

    public void añadirLinea(View view){
        nuevaLinea.setEnabled(false);
        if(codigo.getText().toString().length() != 6){
            codigo.setError("El código de material debe ser de 6 caracteres");
            nuevaLinea.setEnabled(true);
        }
        else if(codigo.getText().toString().equals("")){
            codigo.requestFocus();
            codigo.setError("Campo necesario");
            nuevaLinea.setEnabled(true);
        }
        else if(unidades.getText().toString().equals("0")){
            unidades.requestFocus();
            unidades.setError("¿Piensas registrar 0 unidades?");
            nuevaLinea.setEnabled(true);
        }
        else {
            nuevaLinea.setEnabled(true);
            necesitaLote(true);

        }
    }

    public void necesitaLote(final boolean insertaSiPuedes){
        muestraLoader();
        //Comprobar en Navision si necesita lote
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_NECESITA_LOTE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            progressDialog.dismiss();
                            JSONObject  jo = new JSONObject(response);
                            String res = jo.getString("lote");
                            desc = jo.getString("desc");
                            Log.d("Respuesta", res);

                            if(res.equals("SI")){
                                //
                                //      Mostrar botón Info Lote si no se ve ya
                                //
                                if(tipoRegistro.equals("ENTRADA") && !infoLoteValidada){
                                    if(superior3.getVisibility() == View.GONE){
                                        superior3.setVisibility(View.VISIBLE);
                                    }
                                }
                                else infoLoteValidada = true;


                                if(lote.getText().toString().length() > 0){
                                    if(unidades.getText().toString().equals("1")){
                                        unidades.setText("1");
                                        if(insertaSiPuedes){
                                            if(infoLoteValidada){
                                                insertarEnTabla();
                                            }
                                            else {
                                                //
                                                //      Muestra alert para entrar a validar la información del lote
                                                //
                                                final AlertDialog.Builder builder = new AlertDialog.Builder(CreaLineas.this);
                                                builder.setTitle("Artículo con lote")
                                                        .setMessage("Es necesario confirmar la información del lote")
                                                        .setCancelable(true)
                                                        . setPositiveButton("Información del lote", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                Intent intent = new Intent(CreaLineas.this, InfoLote.class);
                                                                intent.putExtra("cod_lote", lote.getText().toString());
                                                                intent.putExtra("num_serie", codigo.getText().toString());
                                                                startActivityForResult(intent, 1);
                                                            }
                                                        });
                                                builder.show();
                                            }
                                        }
                                    }
                                    else {
                                        unidades.requestFocus();
                                        unidades.setError("Los materiales con lote deben tener 1 unidad");
                                        nuevaLinea.setEnabled(true);
                                    }
                                }
                                else {
                                    progressDialog.dismiss();
                                    lote.requestFocus();
                                    lote.setError("Campo necesario");
                                    unidades.setText("1");
                                    unidades.setEnabled(true);
                                    nuevaLinea.setEnabled(true);
                                }
                            }
                            else if(res.equals("NO")){
                                if(lote.getText().toString().length() > 1){
                                    muestraAlert("Error de datos", "Según Navision, este producto no requiere lote.\nSi no es así, por favor informe al responsable para que actualice el producto en Navision.");
                                    nuevaLinea.setEnabled(true);
                                }
                                else {
                                    lote.setText("");
                                    if(unidades.getText().toString().equals("")){
                                        unidades.requestFocus();
                                        unidades.setError("Este campo es necesario");
                                        nuevaLinea.setEnabled(true);
                                        progressDialog.dismiss();
                                    }
                                    else {
                                        lote.setError(null);
                                        lote.setText("");
                                        unidades.setEnabled(true);
                                        if(insertaSiPuedes){
                                            insertarEnTabla();
                                        }
                                    }
                                }

                            }
                            else {
                                muestraAlert("El código no existe", "Introduce un materíal válido");
                                resetCampos();
                                nuevaLinea.setEnabled(true);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error necesita lote", error.toString());
                nuevaLinea.setEnabled(true);
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
                params.put("codigo", codigo.getText().toString());
                return params;
            }
        };
        queue.add(stringRequest);

    }

    public void insertarEnTabla() {
        Linea l = new Linea(codigo.getText().toString(), Integer.parseInt(unidades.getText().toString()), lote.getText().toString(), desc);
        lineasNuevas.add(l);

        LineasAdapter adapter = new LineasAdapter(this, lineasNuevas);
        lineas.setAdapter(adapter);
        //muestraAlert("Nueva línea", "Se ha añadido una línea ---> " + l.toString());
        resetCampos();
        nuevaLinea.setEnabled(true);
        if(vieneConProducto){
            insertarProductoCable();
        }
    }

    public void enviarRegistros(View view){
        enviaRegistros.setEnabled(false);
        muestraLoader();
        MySqliteOpenHelper mySqliteOpenHelper = new MySqliteOpenHelper(this);
        SQLiteDatabase db = mySqliteOpenHelper.getWritableDatabase();


        almacen = mySqliteOpenHelper.getElegido(db).getAlmacen();

        colaRegistros = Volley.newRequestQueue(this);

        for(int i = 0; i<lineasNuevas.size(); i++){
            Linea l = lineasNuevas.get(i);
            codMaterial = l.getCodigo();
            cantidad = "" + l.getUnidades();
            loteMaterial = l.getLote();

            final Map<String, String> parametros = new HashMap<String, String>();

            parametros.put("articulo", codMaterial);
            parametros.put("cantidad", cantidad);
            parametros.put("lote", loteMaterial);
            parametros.put("almacen", almacen);

            if(i == lineasNuevas.size() -1){
                enviarLinea(parametros, true);
            }
            else {
                enviarLinea(parametros, false);
            }
        }
        enviaRegistros.setEnabled(true);
    }

    public void enviarLinea(Map<String,String> params, final boolean muestraMensaje){

        final Map<String,String> parametros = params;

        switch (tipoRegistro){
            case "ENTRADA":
                urlFinal = urlEntradas;
                parametros.put("observaciones", albaran);
                parametros.put("etiqueta", etiqueta);
                break;
            case "DEVOLUCIÓN A ENDESA":
                urlFinal = urlDevolucionesEndesa;
                break;
            case "SALIDA A ALMACÉN":
                urlFinal = urlSalidasAlmacen;
                parametros.put("almacenDestino", almacenDestino);
                break;
            case "SALIDA A OBRA":
                urlFinal = urlSalidasObra;
                params.put("usuarioApp", "eiffGest");
                params.put("passwordApp", "U6ObJm9iwHWjYxL");
                params.put("empresa", "Eiffage Energía, S.L.U.");
                parametros.put("pOt", otElegida);
                parametros.put("pIncidencia", incidenciaElegida);
                parametros.put("pTicket", ticketSCM);
                break;
            case "DEVOLUCIÓN DE OBRA":
                urlFinal = urlDevolucionesObra;
                parametros.put("ot", otElegida);
                parametros.put("incidencia", incidenciaElegida);
                parametros.put("ticketSCM", ticketSCM);
                break;
            case "RESERVA PARA OBRA":
                urlFinal = urlReservaObra;
                params.put("usuarioApp", "eiffGest");
                params.put("passwordApp", "U6ObJm9iwHWjYxL");
                params.put("empresa", "Eiffage Energía, S.L.U.");

                parametros.put("pOt", otElegida);
                parametros.put("pIncidencia", incidenciaElegida);
                parametros.put("pTicket", ticketSCM);
                parametros.put("pJefeObra", tecnicoEndesa);
                break;
        }
        Log.d("TIPO REGISTRO ===> ", tipoRegistro);
        Log.d("URL FINAL ===> ", urlFinal);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, urlFinal,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("CODIGO MATERIAL", response);
                        progressDialog.dismiss();
                        if(response.contains("insertado correctamente")){
                            if(muestraMensaje){
                                muestraAlert("Líneas registradas", "Los registros ya están disponibles en Navision.");
                            }
                            lineasNuevas = new ArrayList<>();
                            LineasAdapter adapter = new LineasAdapter(CreaLineas.this, lineasNuevas);
                            lineas.setAdapter(adapter);
                        }
                        else{
                            muestraAlert("Respuesta de Navision", response);
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error registro linea", error.toString());
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Error enviando líneas", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                //headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + token);

                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Log.d("PARAMSSSS", "" + parametros.toString());
                return parametros;
            }
        };
        stringRequest.setRetryPolicy((new DefaultRetryPolicy(10 * 1000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)));
        colaRegistros.add(stringRequest);
    }

    public void resetCampos(){
        codigo.setText("");
        unidades.setText("");
        lote.setText("");
        codigo.setError(null);
        unidades.setError(null);
        lote.setError(null);
        unidades.setEnabled(true);
        codigo.requestFocus();
        infoLoteValidada = false;
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

    public void muestraLoader(){
        progressDialog = new ProgressDialog(this);
        progressDialog.show();
    }
}





