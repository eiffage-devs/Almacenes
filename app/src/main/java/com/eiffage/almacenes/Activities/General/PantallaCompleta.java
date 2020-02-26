package com.eiffage.almacenes.Activities.General;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.eiffage.almacenes.R;

public class PantallaCompleta extends AppCompatActivity {

    ImageView imagen;

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
        setContentView(R.layout.activity_pantalla_completa);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Foto pantalla completa");

        imagen = findViewById(R.id.imagenPantallaCompleta);

        Intent intent = getIntent();
        String urlFoto = intent.getStringExtra("urlFoto");

        Glide.with(getApplicationContext())
                .load(urlFoto) // Uri of the picture
                .into(imagen);
    }
}
