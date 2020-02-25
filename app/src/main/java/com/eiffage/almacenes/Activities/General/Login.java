package com.eiffage.almacenes.Activities.General;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.eiffage.almacenes.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Login extends AppCompatActivity {

    private String URL_LOGIN = "";
    private String URL_DATOS_USUARIO = "";

    String usuario, contraseña;
    EditText email, pass;

    SharedPreferences sharedPreferences;
    String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        URL_LOGIN = this.getResources().getString(R.string.urlLogin);
        URL_DATOS_USUARIO = this.getResources().getString(R.string.urlCheck);

        getSupportActionBar().hide();

        email = findViewById(R.id.email);
        pass = findViewById(R.id.contraseña);

        sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        token = sharedPreferences.getString("token", "Sin valor");

        recuperarUsuario(token);


        email.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && !pass.hasFocus()) {
                    hideKeyboard(v);
                }
            }
        });

        pass.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && !email.hasFocus()) {
                    hideKeyboard(v);
                }
            }
        });

    }

    public void iniciarSesion(View view){

        //Recoger datos

        usuario = email.getText().toString();
        contraseña = pass.getText().toString();

        //Comprobar usuario
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_LOGIN,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject job = new JSONObject(response);
                            token = job.getString("token");
                            //Guardamos el token en Shared Preferences
                            SharedPreferences.Editor editor = getSharedPreferences("myPrefs", MODE_PRIVATE).edit();
                            editor.putString("token", token);
                            editor.apply();
                            Intent i = new Intent(Login.this, Menu.class);
                            startActivity(i);
                            finish();
                            //Recuperamos el resto de datos del usuario
                            //recuperarUsuario(token);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error en login", error.toString());
                falloDeLogin();
            }
        })
        {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
                params.put("username", usuario);
                params.put("password", contraseña);

                return params;
            }
        };
        queue.add(stringRequest);


        //Correcto
        /*

        */
    }

    public void recuperarUsuario(final String token){
        //final Usuario[] nuevoUsuario = new Usuario[1];
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest sr = new StringRequest(Request.Method.GET, URL_DATOS_USUARIO,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {

                            JSONObject job=new JSONObject(response);
                            String email=job.getString("email");
                            String empresa=job.getString("empresa");
                            String nombre=job.getString("nombre");
                            String delegacion=job.getString("delegacion");
                            String cod_recurso=job.getString("cod_recurso");
                            //nuevoUsuario[0] = new Usuario(token, email, empresa, nombre, delegacion, cod_recurso);
                            //Enviar a la siguiente pantalla
                            Intent intent = new Intent(Login.this, Menu.class);
                            //intent.putExtra("miUsuario", nuevoUsuario[0]);
                            startActivity(intent);
                            finish();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {


            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                params.put("Authorization", "Bearer " + token);

                return params;
            }
        };
        queue.add(sr);
    }

    public void falloDeLogin(){

        final TextView message = new TextView(this);
        final SpannableString s = new SpannableString(" \n\t Usuario o contraseña incorrectos.\n\n\tSi es la primera vez que accede, debe activar su perfil \n\t accediendo a INET en el siguiente enlace");
        Pattern pattern = Pattern.compile("enlace");
        Linkify.addLinks(s, pattern , "http://inet.energia.eiffage.es?q=");
        message.setText(s);
        message.setMovementMethod(LinkMovementMethod.getInstance());

        AlertDialog.Builder alertdialogobuilder = new AlertDialog.Builder(this);
        alertdialogobuilder
                .setTitle("Login")
                .setView(message)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create();
        alertdialogobuilder.show();
    }


    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
