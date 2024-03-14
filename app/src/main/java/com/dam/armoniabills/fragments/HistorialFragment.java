package com.dam.armoniabills.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dam.armoniabills.R;
import com.dam.armoniabills.model.Historial;
import com.dam.armoniabills.recyclerutils.AdapterHistorial;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class HistorialFragment extends Fragment {

	ArrayList<Historial> listaHistorial;
	RecyclerView rv;
	AdapterHistorial adapterHistorial;

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
		FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
		FirebaseDatabase db = FirebaseDatabase.getInstance();

		db.getReference("Historial").child(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
			@Override
			public void onComplete(@NonNull Task<DataSnapshot> task) {
				if (task.isSuccessful()) {
					if (task.getResult().exists()) {

						DataSnapshot dataSnapshot = task.getResult();
						for (DataSnapshot data : dataSnapshot.getChildren()) {
							Historial historial = data.getValue(Historial.class);
							listaHistorial.add(historial);
						}

						configurarRV();
					}
				}
			}
		});

		return v;
	}

	private void configurarRV() {

		adapterHistorial = new AdapterHistorial(listaHistorial);
		rv.setHasFixedSize(true);
		rv.setLayoutManager(new LinearLayoutManager(getContext()));
		rv.setAdapter(adapterHistorial);

	}
}