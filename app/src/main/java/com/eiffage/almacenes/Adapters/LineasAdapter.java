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
import com.eiffage.almacenes.R;

import java.util.ArrayList;

public class LineasAdapter extends ArrayAdapter<Linea>{

    Context context;
    ArrayList<Linea> values;

    public LineasAdapter(Context context, ArrayList<Linea> values){
        super(context, 0, values);
        this.values = values;
        this.context = context;
    }

    public View getView(final int position, View convertView, ViewGroup parent){

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.item_lista_registros, parent, false);

        try{

            TextView cod = rowView.findViewById(R.id.txtCodigo);
            cod.setText(values.get(position).getCodigo());

            TextView desc = rowView.findViewById(R.id.txtDesc);
            desc.setText(values.get(position).getDescripcion());

            TextView uds = rowView.findViewById(R.id.txtUnidades);
            uds.setText(String.valueOf(values.get(position).getUnidades()));

            ImageView borrar = rowView.findViewById(R.id.eliminar);
            borrar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Eliminar")
                            .setMessage("¿Seguro que quieres borrar esta línea?")
                            .setCancelable(true)
                            . setPositiveButton("Cancelar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            . setPositiveButton("Borrar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    remove(getItem(position));
                                    notifyDataSetChanged();
                                    dialog.dismiss();
                                }
                            });;

                    builder.show();
                }
            });

        }catch (NullPointerException e){
            e.printStackTrace();
        }

        return rowView;
    }
}
