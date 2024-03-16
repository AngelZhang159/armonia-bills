package com.dam.armoniabills.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dam.armoniabills.R;
import com.dam.armoniabills.model.Historial;
import com.dam.armoniabills.recyclerutils.AdapterHistorial;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class HistorialFragment extends Fragment {

	ArrayList<Historial> listaHistorial;
	RecyclerView rv;
	AdapterHistorial adapterHistorial;
	MaterialButton btnElim;
	FirebaseUser user;
	FirebaseDatabase db;

	public HistorialFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_historial, container, false);

		rv = v.findViewById(R.id.rvHistorial);
		listaHistorial = new ArrayList<>();
		user = FirebaseAuth.getInstance().getCurrentUser();
		db = FirebaseDatabase.getInstance();
		btnElim = v.findViewById(R.id.btnBorrarHist);
		btnElim.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
				builder.setTitle(R.string.eliminar_hist);
				builder.setMessage(R.string.seguro_borrar);
				builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						borrarHist();
					}
				}).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
				builder.show();
			}
		});

		db.getReference("Historial").child(user.getUid()).addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot snapshot) {
				listaHistorial.clear();
				for (DataSnapshot data : snapshot.getChildren()) {
					Historial historial = data.getValue(Historial.class);
					listaHistorial.add(historial);
				}
				Collections.reverse(listaHistorial);
				configurarRV();
			}

			@Override
			public void onCancelled(@NonNull DatabaseError error) {

			}
		});
		return v;
	}

	private void borrarHist() {
		db.getReference("Historial").child(user.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
			@Override
			public void onComplete(@NonNull Task<Void> task) {
				Toast.makeText(getContext(), getString(R.string.historial_borrado_con_xito), Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void configurarRV() {

		adapterHistorial = new AdapterHistorial(listaHistorial);
		rv.setHasFixedSize(true);
		rv.setLayoutManager(new LinearLayoutManager(getContext()));
		rv.setAdapter(adapterHistorial);

	}
}