package com.eiffage.almacenes.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;


import com.bumptech.glide.Glide;
import com.eiffage.almacenes.Activities.General.PantallaCompleta;
import com.eiffage.almacenes.R;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import static android.provider.CalendarContract.CalendarCache.URI;

public class ListaFotosAdapter extends ArrayAdapter<Bitmap> {

    //Del constructor...
    private final Context context;
    private ArrayList<Bitmap> values;
    private ArrayList<String> urlFotos;

    private Button borrar;

    public ListaFotosAdapter(Context context, ArrayList<Bitmap> values, ArrayList<String> urlFotos) {
        super(context, -1, values);
        this.context = context;
        this.values = values;
        this.urlFotos = urlFotos;
    }
    public View getView(final int position, View convertView, final ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View rowView = inflater.inflate(R.layout.item_lista_fotos, parent, false);

        ImageView imagen = rowView.findViewById(R.id.imagenItemFoto);


            Glide.with(context)
                    .load(values.get(position)) // Uri of the picture
                    .into(imagen);




        imagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, PantallaCompleta.class);
                i.putExtra("urlFoto", urlFotos.get(position));
                context.startActivity(i);
            }
        });

        borrar = rowView.findViewById(R.id.botonBorrarItemFoto);

        borrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                remove(getItem(position));
                notifyDataSetChanged();
            }
        });

        return rowView;
    }
}
