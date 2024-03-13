package com.dam.armoniabills.recyclerutils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dam.armoniabills.R;
import com.dam.armoniabills.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AdapterUsuariosGasto extends RecyclerView.Adapter<AdapterUsuariosGasto.UsuarioVH> implements View.OnClickListener {

	ArrayList<Usuario> listaUsuario;

	public AdapterUsuariosGasto(ArrayList<Usuario> listaUsuario, View.OnClickListener listener) {
		this.listaUsuario = listaUsuario;
		this.listener = listener;
	}

	View.OnClickListener listener;
	@NonNull
	@Override
	public UsuarioVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_usuario, parent, false);

		v.setOnClickListener(this);

		return new AdapterUsuariosGasto.UsuarioVH(v);
	}

	@Override
	public void onBindViewHolder(@NonNull UsuarioVH holder, int position) {
		holder.bindUsuario(listaUsuario.get(position));
	}

	@Override
	public int getItemCount() {
		return listaUsuario.size();
	}

	@Override
	public void onClick(View v) {
		listener.onClick(v);
	}

	public class UsuarioVH extends RecyclerView.ViewHolder {

		ImageView iv;
		TextView tv;
		Button btnCheck;
		FirebaseDatabase db;
		private static final String DB_PATH = "Usuarios";

		public UsuarioVH(@NonNull View itemView) {
			super(itemView);
			db = FirebaseDatabase.getInstance();
			iv = itemView.findViewById(R.id.ivFotoPerfilUsuarioGasto);
			tv = itemView.findViewById(R.id.tvUsuarioGasto);
			btnCheck = itemView.findViewById(R.id.btnAniadirGasto);
		}

		public void bindUsuario(Usuario usuario) {
			db.getReference(DB_PATH).child(usuario.getId()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
				@Override
				public void onComplete(@NonNull Task<DataSnapshot> task) {
					if (task.isSuccessful()) {
						if (task.getResult().exists()) {
							DataSnapshot dataSnapshot = task.getResult();

							String nombre = String.valueOf(dataSnapshot.child("nombre").getValue());
							String imageUrl = (String.valueOf(dataSnapshot.child("imagenPerfil").getValue()));
							Glide.with(itemView.getContext()).load(imageUrl).into(iv);
						}
					}
				}
			});
		}
	}
}
