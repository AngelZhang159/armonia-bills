package com.dam.armoniabills.recyclerutils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dam.armoniabills.R;
import com.dam.armoniabills.model.Grupo;
import com.dam.armoniabills.model.UsuarioGrupo;

import java.util.ArrayList;

public class AdapterGrupos extends RecyclerView.Adapter<AdapterGrupos.GrupoVH> implements View.OnClickListener {

    Context context;
    ArrayList<Grupo> listaGrupos;
    View.OnClickListener listener;


    public AdapterGrupos(Context context, ArrayList<Grupo> listaGrupos, View.OnClickListener listener) {
        this.context = context;
        this.listaGrupos = listaGrupos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GrupoVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(context).inflate(R.layout.item_grupo, parent, false);

        v.setOnClickListener(this);

        return new GrupoVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull GrupoVH holder, int position) {
        holder.bindGrupo(listaGrupos.get(position), position);

    }

    @Override
    public int getItemCount() {
        return listaGrupos.size();
    }

    @Override
    public void onClick(View v) {
        listener.onClick(v);
    }


    public class GrupoVH extends RecyclerView.ViewHolder{

        TextView tvTitulo, tvPagoUsuario, tvTotal, tvNumPersonas;

        public GrupoVH(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTituloGrupo);
            tvPagoUsuario = itemView.findViewById(R.id.tvPagoUsuarioGrupo);
            tvTotal = itemView.findViewById(R.id.tvTotalGrupo);
            tvNumPersonas = itemView.findViewById(R.id.tvNumPersonasGrupo);


        }

        public void bindGrupo(Grupo grupo, int position) {

            UsuarioGrupo usuarioGrupo = grupo.getUsuarios().get(position);
            String pago = "0";
            if(usuarioGrupo.getDeben() == 0){
                pago = String.valueOf(usuarioGrupo.getDebes());
            } else {
                pago = String.valueOf(usuarioGrupo.getDeben());
            }

            tvTitulo.setText(grupo.getTitulo());
            tvPagoUsuario.setText(pago);
            tvTotal.setText(String.valueOf(grupo.getTotal()));
            tvNumPersonas.setText(String.valueOf(grupo.getUsuarios().size()));

        }
    }
}
