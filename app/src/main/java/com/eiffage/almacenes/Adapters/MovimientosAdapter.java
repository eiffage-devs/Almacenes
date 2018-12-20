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
import com.eiffage.almacenes.Objetos.Movimiento;
import com.eiffage.almacenes.Objetos.OT;
import com.eiffage.almacenes.R;

import java.util.ArrayList;

public class MovimientosAdapter extends ArrayAdapter<Movimiento> {

    Context context;
    ArrayList<Movimiento> values;

    public MovimientosAdapter(Context context, ArrayList<Movimiento> values){
        super(context, 0, values);
        this.values = values;
        this.context = context;
    }

    public View getView(final int position, View convertView, ViewGroup parent){

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.item_movimientos, parent, false);

        try{

            TextView tipoMov = rowView.findViewById(R.id.tipoMov);
            tipoMov.setText(values.get(position).getTipoMov());

            TextView fecha = rowView.findViewById(R.id.fecha);
            fecha.setText(values.get(position).getFecha());

            TextView origen = rowView.findViewById(R.id.origen);
            origen.setText(values.get(position).getOrigen());

            TextView destino = rowView.findViewById(R.id.destino);
            destino.setText(values.get(position).getDestino());

            TextView obra = rowView.findViewById(R.id.obra);
            obra.setText(values.get(position).getObra());

            TextView ticket = rowView.findViewById(R.id.ticket);
            ticket.setText(values.get(position).getTicket());

        }catch (NullPointerException e){
            e.printStackTrace();
        }

        return rowView;
    }
}