package com.dam.armoniabills.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dam.armoniabills.NuevoGrupoActivity;
import com.dam.armoniabills.R;
import com.dam.armoniabills.TopBarActivity;
import com.dam.armoniabills.model.Grupo;
import com.dam.armoniabills.model.UsuarioGrupo;
import com.dam.armoniabills.recyclerutils.AdapterGrupos;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;

public class HomeFragment extends Fragment implements View.OnClickListener{

	public static final String GRUPO_SELECCIONADO = "Grupo_seleccionado";
	ExtendedFloatingActionButton efab;

	RecyclerView rv;
	AdapterGrupos adapter;
	ArrayList<Grupo> lista;

	public HomeFragment() {
		// Required empty public constructor
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


		//TODO cargar Grupos
		//TODO cargar deuda
		UsuarioGrupo usuario1 = new UsuarioGrupo(20, 0, 20, "pepe@gmail.com");
		UsuarioGrupo usuario2 = new UsuarioGrupo(0, 50, 20, "pepe@gmail.com");
		ArrayList<UsuarioGrupo> listaUsuarios = new ArrayList<>();
		listaUsuarios.add(usuario1);
		listaUsuarios.add(usuario2);
		listaUsuarios.add(usuario2);
		ArrayList<UsuarioGrupo> listaUsuarios2 = new ArrayList<>();
		listaUsuarios2.add(usuario2);
		listaUsuarios2.add(usuario2);


		Grupo grupo1 = new Grupo("Viaje", "Me duele el pie", listaUsuarios2, 250, null);
		Grupo grupo2 = new Grupo("Mallorca", "Hola buenas", listaUsuarios, 324, null);
		Grupo grupo3 = new Grupo("monos", "Hola", listaUsuarios, 500, null);

		lista.add(grupo1);
		lista.add(grupo2);
		lista.add(grupo3);

		configurarRV();


	}

	private void configurarRV() {

		adapter = new AdapterGrupos(getContext(), lista, this);
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
		i.putExtra("rellenar", "fragmentoHome");
		startActivity(i);
	}
}