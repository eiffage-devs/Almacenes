package com.eiffage.almacenes.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.eiffage.almacenes.Objetos.OT;
import com.eiffage.almacenes.R;

import java.util.ArrayList;

public class OTAdapter extends ArrayAdapter<OT> implements Filterable{

    Context context;
    ArrayList<OT> values;
    private ArrayList<OT> valuesFiltrados;

    public OTAdapter(Context context, ArrayList<OT> values){
        super(context, 0, values);
        this.values = values;
        this.context = context;
    }

    public View getView(final int position, View convertView, ViewGroup parent){

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.item_lista_ot, parent, false);

        try{

            TextView denominacion = rowView.findViewById(R.id.denominacion);
            denominacion.setText(values.get(position).getDenominacion());

            TextView codOT = rowView.findViewById(R.id.codigoOT);
            codOT.setText(values.get(position).getCodOT());

            TextView codObra = rowView.findViewById(R.id.codigoObra);
            codObra.setText(values.get(position).getCodObra());

        }catch (NullPointerException e){
            e.printStackTrace();
        }

        return rowView;
    }

    public Filter getFilter() {
        return new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                final FilterResults oReturn = new FilterResults();
                final ArrayList<OT> results = new ArrayList<OT>();
                if (valuesFiltrados == null)
                    valuesFiltrados = values;
                if (constraint != null) {
                    if (valuesFiltrados != null && valuesFiltrados.size() > 0) {
                        for (final OT g : valuesFiltrados) {
                            if (g.getDenominacion().toLowerCase()
                                    .contains(constraint.toString()) || g.getCodOT().toLowerCase()
                                    .contains(constraint.toString()))
                                results.add(g);
                        }
                    }
                    oReturn.values = results;
                }
                return oReturn;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint,
                                          FilterResults results) {
                values = (ArrayList<OT>) results.values;
                notifyDataSetChanged();
            }
        };
    }


    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return values.size();
    }

    @Override
    public OT getItem(int position) {
        return values.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}
