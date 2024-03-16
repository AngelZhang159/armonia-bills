package com.dam.armoniabills.recyclerutils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dam.armoniabills.MainActivity;
import com.dam.armoniabills.R;
import com.dam.armoniabills.model.Gasto;
import com.dam.armoniabills.model.Usuario;
import com.dam.armoniabills.model.UsuarioGrupo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AdapterGastos extends RecyclerView.Adapter<AdapterGastos.MyViewHolder> {

	private ArrayList<Gasto> listaGastos;
	private ArrayList<UsuarioGrupo> listaUsuariosGrupo;

	public AdapterGastos(ArrayList<Gasto> listaGastos, ArrayList<UsuarioGrupo> listaUsuariosGrupo) {
		this.listaGastos = listaGastos;
		this.listaUsuariosGrupo = listaUsuariosGrupo;
	}

	@NonNull
	@Override
	public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gasto, parent, false);

		MyViewHolder mvh = new MyViewHolder(v);
		return mvh;
	}

	@Override
	public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
		holder.bindGasto(listaGastos.get(position), listaUsuariosGrupo);
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

		public void bindGasto(Gasto gasto, ArrayList<UsuarioGrupo> listaUsuariosGrupo) {

			FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

			tvTitulo.setText(gasto.getTitulo());

			FirebaseDatabase.getInstance().getReference(MainActivity.DB_PATH_USUARIOS).child(gasto.getIdUsuario()).get()
					.addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
						@Override
						public void onComplete(@NonNull Task<DataSnapshot> task) {
							if (task.isSuccessful()) {
								if (task.getResult().exists()) {
									DataSnapshot dataSnapshot = task.getResult();
									Usuario usuario = dataSnapshot.getValue(Usuario.class);

									if (usuario.getId().equals(user.getUid())) {
										tvUsuarioPago.setText(String.format(itemView.getContext().getString(R.string.tv_gasto_pagado), gasto.getPrecio()));
									} else {
										tvUsuarioPago.setText(String.format(itemView.getContext().getString(R.string.tv_gasto_pago), usuario.getNombre(), gasto.getPrecio()));
									}
								}
							}
						}
					});

			String deudores = gasto.getListaUsuariosPagan().size() + "/" + listaUsuariosGrupo.size();
			tvGastoUsuario.setText(String.format(itemView.getContext().getString(R.string.tv_gasto_usuario), itemView.getContext().getString(R.string.tv_pagan), deudores));


		}
	}
}
