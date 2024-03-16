package com.dam.armoniabills.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dam.armoniabills.MainActivity;
import com.dam.armoniabills.NuevoGrupoActivity;
import com.dam.armoniabills.R;
import com.dam.armoniabills.TopBarActivity;
import com.dam.armoniabills.model.Grupo;
import com.dam.armoniabills.model.Usuario;
import com.dam.armoniabills.recyclerutils.AdapterGrupos;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HomeFragment extends Fragment implements View.OnClickListener {

	public static final String GRUPO_SELECCIONADO = "Grupo_seleccionado";

	ExtendedFloatingActionButton efab;
	RecyclerView rv;
	AdapterGrupos adapter;
	ArrayList<Grupo> lista;
	ArrayList<String> listaGruposUsuario;

	public HomeFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_home, container, false);

		efab = v.findViewById(R.id.efabNuevoGrupo);
		rv = v.findViewById(R.id.rvGrupos);

		lista = new ArrayList<>();
		listaGruposUsuario = new ArrayList<>();

		cargarGrupos();

		efab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getContext(), NuevoGrupoActivity.class);
				startActivity(i);
			}
		});
		efab.extend();
		return v;


	}

	private void cargarGrupos() {

		FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
		FirebaseDatabase db = FirebaseDatabase.getInstance();

		if(user != null){
			db.getReference(MainActivity.DB_PATH_USUARIOS).child(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
				@Override
				public void onComplete(@NonNull Task<DataSnapshot> task) {
					if (task.isSuccessful()) {
						if (task.getResult().exists()) {
							DataSnapshot dataSnapshot = task.getResult();

							Usuario usuario = dataSnapshot.getValue(Usuario.class);

							listaGruposUsuario = usuario.getGrupos();

							if (listaGruposUsuario != null) {
								db.getReference(MainActivity.DB_PATH_GRUPOS).addValueEventListener(new ValueEventListener() {
									@Override
									public void onDataChange(@NonNull DataSnapshot snapshot) {
										lista.clear();

										for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

											Grupo grupo = dataSnapshot.getValue(Grupo.class);

											for (int i = 0; i < listaGruposUsuario.size(); i++) {
												if (grupo.getId().equals(listaGruposUsuario.get(i))) {
													lista.add(grupo);
												}
											}

										}

										configurarRV();

									}

								@Override
								public void onCancelled(@NonNull DatabaseError error) {
								}
							});

							}
						}
					}
				}
			});
		}



	}

	private void configurarRV() {

		adapter = new AdapterGrupos(lista, this);
		rv.setHasFixedSize(true);
		rv.setLayoutManager(new LinearLayoutManager(getContext()));
		rv.setAdapter(adapter);

	}

	@Override
	public void onClick(View v) {
		int pos = rv.getChildAdapterPosition(v);
		Grupo grupo = lista.get(pos);

		Intent i = new Intent(getContext(), TopBarActivity.class);
		i.putExtra(GRUPO_SELECCIONADO, grupo);
		i.putExtra(getString(R.string.rellenar), getString(R.string.fragHome));
		startActivity(i);
	}
}