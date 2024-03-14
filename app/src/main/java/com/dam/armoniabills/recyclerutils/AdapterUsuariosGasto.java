package com.dam.armoniabills.recyclerutils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
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

public class AdapterUsuariosGasto extends RecyclerView.Adapter<AdapterUsuariosGasto.UsuarioVH> implements View.OnClickListener {

	public static ArrayList<String> idsPagan;
	ArrayList<Usuario> listaUsuario;
	View.OnClickListener listener;


	public AdapterUsuariosGasto(ArrayList<Usuario> listaUsuario, View.OnClickListener listener) {
		this.listaUsuario = listaUsuario;
		this.listener = listener;
	}

	@NonNull
	@Override
	public UsuarioVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_usuario, parent, false);

		idsPagan = new ArrayList<>();
		for (Usuario usuario : listaUsuario) {
			idsPagan.add(usuario.getId());
		}

		v.setOnClickListener(this);

		return new AdapterUsuariosGasto.UsuarioVH(v);
	}

	@Override
	public void onBindViewHolder(@NonNull UsuarioVH holder, int position) {

		holder.bindUsuario(listaUsuario.get(position));
		holder.btnCheck.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String userId = listaUsuario.get(holder.getAdapterPosition()).getId();
				if (idsPagan.contains(userId)) {
					idsPagan.remove(userId);
				} else {
					idsPagan.add(userId);
				}
			}
		});
	}

	@Override
	public int getItemCount() {
		return listaUsuario.size();
	}

	@Override
	public void onClick(View v) {
		listener.onClick(v);
	}

	public ArrayList<String> getIdsPagan() {
		return idsPagan;
	}

	public class UsuarioVH extends RecyclerView.ViewHolder {

		ImageView iv;
		TextView tv;
		CheckBox btnCheck;
		FirebaseDatabase db;

		public UsuarioVH(@NonNull View itemView) {
			super(itemView);
			db = FirebaseDatabase.getInstance();
			iv = itemView.findViewById(R.id.ivFotoPerfilUsuarioGasto);
			tv = itemView.findViewById(R.id.tvUsuarioGasto);
			btnCheck = itemView.findViewById(R.id.btnChk);
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
