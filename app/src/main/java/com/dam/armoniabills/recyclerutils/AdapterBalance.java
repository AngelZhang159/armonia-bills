package com.dam.armoniabills.recyclerutils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dam.armoniabills.R;
import com.dam.armoniabills.model.HistorialBalance;

import java.util.ArrayList;

public class AdapterBalance extends RecyclerView.Adapter<AdapterBalance.BalanceVH> {

    ArrayList<HistorialBalance> listaHistorial;

    public AdapterBalance(ArrayList<HistorialBalance> listaHistorial) {
        this.listaHistorial = listaHistorial;
    }

    @NonNull
    @Override
    public AdapterBalance.BalanceVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_balance, parent, false);

        return new AdapterBalance.BalanceVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterBalance.BalanceVH holder, int position) {
        holder.bindHistorial(listaHistorial.get(position));
    }

    @Override
    public int getItemCount() {
        return listaHistorial.size();
    }

    public class BalanceVH extends RecyclerView.ViewHolder {

        ImageView iv;
        TextView tvInformacion;

        public BalanceVH(@NonNull View itemView) {
            super(itemView);
            iv = itemView.findViewById(R.id.ivIcono);
            tvInformacion = itemView.findViewById(R.id.tvInformacion);
        }

        public void bindHistorial(HistorialBalance historial) {
            if (historial.getAccion().equals("retirado")) {
                iv.setImageResource(R.drawable.baseline_arrow_circle_down_24);
                tvInformacion.setText(String.format(itemView.getContext().getString(R.string.historial_retiro), historial.getCantidad()));
            } else if (historial.getAccion().equals("ingresado")) {
                iv.setImageResource(R.drawable.baseline_arrow_circle_up_24);
                tvInformacion.setText(String.format(itemView.getContext().getString(R.string.historial_ingreso), historial.getCantidad()));
            } else if (historial.getAccion().equals("pagado")) {
                iv.setImageResource(R.drawable.baseline_payments_24);
                tvInformacion.setText(String.format(itemView.getContext().getString(R.string.historial_pagado), historial.getCantidad()));
            }
        }
    }
}
