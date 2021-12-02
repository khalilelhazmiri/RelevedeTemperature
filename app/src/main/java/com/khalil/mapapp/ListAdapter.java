package com.khalil.mapapp;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    private List<Mesure> mesures;


    public ListAdapter(List<Mesure> itemList){
        super();
        this.mesures = itemList;
        Log.d("3lach", "ListAdapter: "+itemList);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_element,parent,false);
        Log.d("3lach", "onCreateViewHolder: "+viewType);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Mesure item = this.mesures.get(position);
        holder.idMesure.setText(item.getIdMesure());
        holder.mesure.setText(item.getMesure());
        holder.user.setText(item.getUser());
        holder.latlng.setText(item.getLatLng());
        holder.city.setText(item.getCity());
        holder.country.setText(item.getCountry());
        holder.timestamp.setText(item.getTimestamp());

        Log.d("3lach", "onBindViewHolder: "+ position);
//        holder.bind(mesures.get(position));
    }

    @Override
    public int getItemCount() {
        Log.d("3lach", "getItemCount: "+mesures.size());
        return mesures.size();
    }

    public void setData(List<Mesure> items){
        mesures = items;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView idMesure, mesure, user, latlng, city, country, timestamp;
        View itemView;

        ViewHolder(View itemView){
            super(itemView);
            idMesure = (TextView) itemView.findViewById(R.id.id_mesure);
            mesure = (TextView) itemView.findViewById(R.id.nMesure);
            user = (TextView) itemView.findViewById(R.id.user);
            latlng = (TextView) itemView.findViewById(R.id.latlng);
            city = (TextView) itemView.findViewById(R.id.city);
            country = (TextView) itemView.findViewById(R.id.country);
            timestamp = (TextView) itemView.findViewById(R.id.timestamp);
        }
//        void bindData(final Mesure item){
//            idMesure.setText(item.getIdMesure());
//            mesure.setText(item.getMesure());
//            user.setText(item.getUser());
//            latlng.setText(item.getLatLng());
//            city.setText(item.getCity());
//            country.setText(item.getCountry());
//            timestamp.setText(item.getTimestamp());
//        }

        void bind(Mesure item){
//            idMesure = itemView.findViewById(R.id.id_mesure);
//            mesure = itemView.findViewById(R.id.mesure);
//            user = itemView.findViewById(R.id.user);
//            latlng = itemView.findViewById(R.id.latlng);
//            city = itemView.findViewById(R.id.city);
//            country = itemView.findViewById(R.id.country);
//            timestamp = itemView.findViewById(R.id.timestamp);

            idMesure.setText(item.getIdMesure());
            mesure.setText(item.getMesure());
            user.setText(item.getUser());
            latlng.setText(item.getLatLng());
            city.setText(item.getCity());
            country.setText(item.getCountry());
            timestamp.setText(item.getTimestamp());
        }
    }


}
