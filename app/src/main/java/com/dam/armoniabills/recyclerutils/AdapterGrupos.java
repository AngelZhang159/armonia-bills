package com.dam.armoniabills.recyclerutils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.dam.armoniabills.R;
import com.dam.armoniabills.model.Grupo;
import com.dam.armoniabills.model.UsuarioGrupo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class AdapterGrupos extends RecyclerView.Adapter<AdapterGrupos.GrupoVH> implements View.OnClickListener {

	ArrayList<Grupo> listaGrupos;
	View.OnClickListener listener;


	public AdapterGrupos(ArrayList<Grupo> listaGrupos, View.OnClickListener listener) {
		this.listaGrupos = listaGrupos;
		this.listener = listener;
	}

	@NonNull
	@Override
	public GrupoVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grupo, parent, false);

		v.setOnClickListener(this);

		return new GrupoVH(v);
	}

	@Override
	public void onBindViewHolder(@NonNull GrupoVH holder, int position) {
		holder.bindGrupo(listaGrupos.get(position));
	}

	@Override
	public int getItemCount() {
		return listaGrupos.size();
	}

	@Override
	public void onClick(View v) {
		listener.onClick(v);
	}


	public class GrupoVH extends RecyclerView.ViewHolder {

		TextView tvTitulo, tvPagoUsuario, tvTotal, tvNumPersonas;

		public GrupoVH(@NonNull View itemView) {
			super(itemView);
			tvTitulo = itemView.findViewById(R.id.tvTituloGrupo);
			tvPagoUsuario = itemView.findViewById(R.id.tvPagoUsuarioGrupo);
			tvTotal = itemView.findViewById(R.id.tvTotalGrupo);
			tvNumPersonas = itemView.findViewById(R.id.tvNumPersonasGrupo);


		}

		public void bindGrupo(Grupo grupo) {

			FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

			ArrayList<UsuarioGrupo> listaUsuariosGrupo = grupo.getUsuarios();
			UsuarioGrupo usuarioGrupoActual = new UsuarioGrupo();

			for (UsuarioGrupo usuarioGrupo : listaUsuariosGrupo) {
				if (usuarioGrupo.getId().equals(user.getUid())) {
					usuarioGrupoActual = usuarioGrupo;
				}
			}

			double pago = 0;
			if (usuarioGrupoActual.getDeben() == 0) {
				pago = usuarioGrupoActual.getDebes();
			} else {
				pago = usuarioGrupoActual.getDeben();
			}

			tvTitulo.setText(grupo.getTitulo());
			tvTotal.setText(String.format(itemView.getContext().getString(R.string.tv_total), grupo.getTotal()));
			tvNumPersonas.setText(String.valueOf(grupo.getUsuarios().size()));

			String pagoUsuarioStr = "";
			if (usuarioGrupoActual.getDeben() > usuarioGrupoActual.getDebes()) {
				pago = usuarioGrupoActual.getDeben() - usuarioGrupoActual.getDebes();
				pagoUsuarioStr = String.format(itemView.getContext().getString(R.string.te_deben_g), pago);
				tvPagoUsuario.setTextColor(itemView.getContext().getColor(R.color.verde));
			} else {
				pago = usuarioGrupoActual.getDebes() - usuarioGrupoActual.getDeben();
				pagoUsuarioStr = String.format(itemView.getContext().getString(R.string.debes_g), pago);
				tvPagoUsuario.setTextColor(itemView.getContext().getColor(R.color.rojo));
			}
			tvPagoUsuario.setText(pagoUsuarioStr);

		}
	}
}
