package com.dam.armoniabills.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dam.armoniabills.NuevoGastoActivity;
import com.dam.armoniabills.R;
import com.dam.armoniabills.model.Gasto;
import com.dam.armoniabills.model.Grupo;
import com.dam.armoniabills.model.UsuarioGrupo;
import com.dam.armoniabills.recyclerutils.AdapterGastos;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class GrupoFragment extends Fragment implements View.OnClickListener {

	private static final String ARG_PARAM1 = "param1";
	TextView tvTitulo, tvDescripcion, tvDeuda, tvTotal, tvNumPersonas;
	MaterialButton btnPers;
	RecyclerView rv;
	AdapterGastos adapter;
	ArrayList<Gasto> listaGastos;
	ExtendedFloatingActionButton efab;
	private Grupo grupo;

	public GrupoFragment() {
		// Required empty public constructor
	}

	public static GrupoFragment newInstance(Grupo param1) {
		GrupoFragment fragment = new GrupoFragment();
		Bundle args = new Bundle();
		args.putParcelable(ARG_PARAM1, param1);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			grupo = getArguments().getParcelable(ARG_PARAM1);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_grupo, container, false);

		tvTitulo = v.findViewById(R.id.tvTituloGrupoDetalle);
		tvDescripcion = v.findViewById(R.id.tvDescripcionGrupoDetalle);
		tvDeuda = v.findViewById(R.id.tvDeudaGrupoDetalle);
		tvTotal = v.findViewById(R.id.tvTotalGrupoDetalle);
		rv = v.findViewById(R.id.rvGastosGrupoDetalle);
		efab = v.findViewById(R.id.efabNuevoGasto);
		btnPers = v.findViewById(R.id.btnPersGrupo);

		btnPers.setOnClickListener(this);

		cargarGrupo();

		listaGastos = new ArrayList<>();


		rellenarListaGastos();

		efab.setOnClickListener(this);
		return v;
	}

	private void configurarRV() {
		adapter = new AdapterGastos(listaGastos);

		rv.setLayoutManager(new LinearLayoutManager(getContext()));
		rv.setAdapter(adapter);
		rv.setHasFixedSize(true);
	}

	private void rellenarListaGastos() {
		FirebaseDatabase.getInstance().getReference("Grupos").child(grupo.getId()).child("gastos").addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot snapshot) {
				listaGastos.clear(); // Clear the list before adding new data
				for (DataSnapshot data : snapshot.getChildren()) {
					Gasto gasto = data.getValue(Gasto.class);
					listaGastos.add(gasto);
				}
				configurarRV();
				adapter.notifyDataSetChanged();
			}

			@Override
			public void onCancelled(@NonNull DatabaseError error) {
				Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show();
			}
		});
	}


	private void cargarGrupo() {

		FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

		FirebaseDatabase db = FirebaseDatabase.getInstance();

		db.getReference("Grupos").child(grupo.getId()).addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot snapshot) {

				grupo = snapshot.getValue(Grupo.class);


				ArrayList<UsuarioGrupo> listaUsuariosGrupo = grupo.getUsuarios();
				UsuarioGrupo usuarioGrupoActual = new UsuarioGrupo();

				for (UsuarioGrupo usuarioGrupo : listaUsuariosGrupo) {
					if (usuarioGrupo.getId().equals(user.getUid())) {
						usuarioGrupoActual = usuarioGrupo;
					}
				}

				double pago;
				if (usuarioGrupoActual.getDeben() == 0) {
					pago = usuarioGrupoActual.getDebes();
				} else {
					pago = usuarioGrupoActual.getDeben();
				}

				tvTitulo.setText(grupo.getTitulo());
				tvDescripcion.setText(grupo.getDescripcion());
				tvTotal.setText(String.format(getString(R.string.tv_grupo_total_pagar), grupo.getTotal()));
				btnPers.setText(String.valueOf(listaUsuariosGrupo.size()));
				tvDeuda.setText(String.format(getString(R.string.tv_grupo_tu_pagas), pago));

			}

			@Override
			public void onCancelled(@NonNull DatabaseError error) {

			}
		});

	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.efabNuevoGasto) {
			Intent i = new Intent(getContext(), NuevoGastoActivity.class);
			i.putExtra("grupo", grupo);
			startActivity(i);
//		} else if (v.getId() == R.id.btnPersGrupo) {
//			new MaterialAlertDialogBuilder(this)
//					.setTitle("Personas")
//					.setAdapter(new ArrayAdapter<Usuario>(this, ))
		}
	}
}