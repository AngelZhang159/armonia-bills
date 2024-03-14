package com.dam.armoniabills.recyclerutils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dam.armoniabills.MainActivity;
import com.dam.armoniabills.R;
import com.dam.armoniabills.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AdapterUsuariosGrupo extends RecyclerView.Adapter<AdapterUsuariosGrupo.UsuarioGrupoVH> {
	ArrayList<Usuario> listaUsuario;


	public AdapterUsuariosGrupo(ArrayList<Usuario> listaUsuario) {
		this.listaUsuario = listaUsuario;
	}

	@NonNull
	@Override
	public AdapterUsuariosGrupo.UsuarioGrupoVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lista_usuario, parent, false);

		return new AdapterUsuariosGrupo.UsuarioGrupoVH(v);
	}

	@Override
	public void onBindViewHolder(@NonNull AdapterUsuariosGrupo.UsuarioGrupoVH holder, int position) {

		holder.bindUsuario(listaUsuario.get(position));
	}

	@Override
	public int getItemCount() {
		return listaUsuario.size();
	}

	public class UsuarioGrupoVH extends RecyclerView.ViewHolder {

		ImageView iv;
		TextView tv;
		FirebaseDatabase db;

		public UsuarioGrupoVH(@NonNull View itemView) {
			super(itemView);
			db = FirebaseDatabase.getInstance();
			iv = itemView.findViewById(R.id.ivUsuarioListaGrupo);
			tv = itemView.findViewById(R.id.tvNomUsuarioListaGrupo);
		}

		public void bindUsuario(Usuario usuario) {

			db.getReference(MainActivity.DB_PATH_USUARIOS).child(usuario.getId()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
				@Override
				public void onComplete(@NonNull Task<DataSnapshot> task) {
					if (task.isSuccessful()) {
						if (task.getResult().exists()) {

							String nombre = String.valueOf(usuario.getNombre());
							String imageUrl = (String.valueOf(usuario.getImagenPerfil()));
							tv.setText(nombre);
							Glide.with(itemView.getContext()).load(imageUrl).into(iv);

						}
					}
				}
			});

		}

	}


}
