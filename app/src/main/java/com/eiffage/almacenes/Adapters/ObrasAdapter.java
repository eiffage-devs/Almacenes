package com.eiffage.almacenes.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.eiffage.almacenes.Objetos.Linea;
import com.eiffage.almacenes.Objetos.Obra;
import com.eiffage.almacenes.R;

import java.util.ArrayList;

public class ObrasAdapter extends ArrayAdapter<Obra> {

    Context context;
    ArrayList<Obra> values;

    public ObrasAdapter(Context context, ArrayList<Obra> values){
        super(context, 0, values);
        this.values = values;
        this.context = context;
    }

    public View getView(final int position, View convertView, ViewGroup parent){

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.item_lista_registros, parent, false);

        try{

            TextView cod = rowView.findViewById(R.id.txtCodigo);
            cod.setText(values.get(position).getDescripcion());

        }catch (NullPointerException e){
            e.printStackTrace();
        }

        return rowView;
    }
}
