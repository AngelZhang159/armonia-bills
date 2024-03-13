package com.dam.armoniabills.recyclerutils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dam.armoniabills.R;
import com.dam.armoniabills.model.Gasto;
import com.dam.armoniabills.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AdapterGastos extends RecyclerView.Adapter<AdapterGastos.MyViewHolder> {

    private ArrayList<Gasto> listaGastos;

    public AdapterGastos(ArrayList<Gasto> listaGastos) {
        this.listaGastos = listaGastos;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gasto, parent, false);

        MyViewHolder mvh = new MyViewHolder(v);
        return mvh;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.bindIncidencia(listaGastos.get(position));
    }

    @Override
    public int getItemCount() {
        return listaGastos.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitulo, tvUsuarioPago, tvGastoUsuario;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitulo = itemView.findViewById(R.id.tvTituloGasto);
            tvUsuarioPago = itemView.findViewById(R.id.tvUsuarioPago);
            tvGastoUsuario = itemView.findViewById(R.id.tvGastoUsuario);
        }

        public void bindIncidencia(Gasto gasto) {
            tvTitulo.setText(gasto.getTitulo());
            FirebaseDatabase.getInstance().getReference("Usuarios").child(gasto.getUsuario()).get()
                            .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        if (task.getResult().exists()) {
                                            DataSnapshot dataSnapshot = task.getResult();
                                            Usuario usuario = dataSnapshot.getValue(Usuario.class);

                                            tvUsuarioPago.setText(usuario.getNombre() + " pagó " + gasto.getPrecio());
                                        }
                                    }
                                }
                            });

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            double deuda = 0;
            if (user.getUid().equals(gasto.getUsuario())) {
                deuda = gasto.getPrecio() - (gasto.getPrecio() / gasto.getListaUsuariosPagan().size());
                tvGastoUsuario.setText(String.format(String.valueOf(R.string.tv_gasto_usuario), "prestaste", deuda));
            } else {
                deuda = gasto.getPrecio() / gasto.getListaUsuariosPagan().size();
                tvGastoUsuario.setText(String.format(String.valueOf(R.string.tv_gasto_usuario), "pediste", deuda));
            }
        }
    }
}
