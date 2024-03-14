package com.dam.armoniabills.recyclerutils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dam.armoniabills.R;
import com.dam.armoniabills.model.Historial;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class AdapterHistorial extends RecyclerView.Adapter<AdapterHistorial.HistorialVH> {
	ArrayList<Historial> listaHistorial;

	public AdapterHistorial(ArrayList<Historial> listaHistorial) {
		this.listaHistorial = listaHistorial;
	}

	@NonNull
	@Override
	public HistorialVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_historial, parent, false);

		return new HistorialVH(v);
	}

	@Override
	public void onBindViewHolder(@NonNull HistorialVH holder, int position) {
		holder.bindGrupo(listaHistorial.get(position));
	}

	@Override
	public int getItemCount() {
		return listaHistorial.size();
	}

	public class HistorialVH extends RecyclerView.ViewHolder {

		ImageView iv;
		TextView tvNombre, tvDesc, tvTiempo;

		public HistorialVH(@NonNull View itemView) {
			super(itemView);
			iv = itemView.findViewById(R.id.ivFotoPerfilUsuarioHistorial);
			tvNombre = itemView.findViewById(R.id.tvUsuarioHistorial);
			tvDesc = itemView.findViewById(R.id.tvDescHistorial);
			tvTiempo = itemView.findViewById(R.id.tvTiempoHistorial);
		}

		public void bindGrupo(Historial historial) {
			tvNombre.setText(historial.getNombreGrupo());
			tvDesc.setText(historial.getDescripcion());

			long tiempoNoti = historial.getTiempo();
			long tiempoAhora = System.currentTimeMillis();
			long difEnMilis = tiempoAhora - tiempoNoti;

			long mins = TimeUnit.MILLISECONDS.toMinutes(difEnMilis);
			long horas = TimeUnit.MILLISECONDS.toHours(difEnMilis);
			long dias = TimeUnit.MILLISECONDS.toDays(difEnMilis);

			String tiempoString;
			if (dias > 0) {
				tiempoString = "Hace " + dias + " días";
			} else if (horas > 0) {
				tiempoString = "Hace " + horas + " horas";
			} else if (mins > 0) {
				tiempoString = "Hace " + mins + " minutos";
			} else {
				tiempoString = "Hace menos de un minuto";
			}
			tvTiempo.setText(tiempoString);
			Glide.with(itemView.getContext()).load(historial.getImagenPerfil()).into(iv);

		}
	}
}
