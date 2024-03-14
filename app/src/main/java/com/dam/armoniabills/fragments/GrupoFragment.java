package com.dam.armoniabills.fragments;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dam.armoniabills.MainActivity;
import com.dam.armoniabills.NuevoGastoActivity;
import com.dam.armoniabills.NuevoGrupoActivity;
import com.dam.armoniabills.R;
import com.dam.armoniabills.dialogutils.AniadirDialogFragment;
import com.dam.armoniabills.dialogutils.OnDatosDialogListener;
import com.dam.armoniabills.model.Gasto;
import com.dam.armoniabills.model.Grupo;
import com.dam.armoniabills.model.Usuario;
import com.dam.armoniabills.model.UsuarioGrupo;
import com.dam.armoniabills.recyclerutils.AdapterGastos;
import com.dam.armoniabills.recyclerutils.AdapterUsuariosGrupo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class GrupoFragment extends Fragment implements View.OnClickListener, OnDatosDialogListener {

	private static final String ARG_PARAM1 = "param1";
	
	TextView tvTitulo, tvDescripcion, tvDeuda, tvTotal;
	Button btnPagar;
	MaterialButton btnPers;
	ExtendedFloatingActionButton efab;
	RecyclerView rv;
	AdapterGastos adapter;
	
	ArrayList<Gasto> listaGastos;
	Usuario usuarioActual;
	Usuario usuarioAniadir;
	UsuarioGrupo usuarioGrupoActual;
	ArrayList<Usuario> listaUsuario;
	ArrayList<UsuarioGrupo> listaUsuarioGrupo;
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
		btnPagar = v.findViewById(R.id.btnPagarDeudas);

		btnPagar.setOnClickListener(this);
		btnPers.setOnClickListener(this);
		listaUsuario = new ArrayList<>();

		cargarGrupo();

		listaGastos = new ArrayList<>();
		listaUsuarioGrupo = new ArrayList<>();
		
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

				if (grupo != null) {
					ArrayList<UsuarioGrupo> listaUsuariosGrupo = grupo.getUsuarios();
					UsuarioGrupo usuarioGrupoActual = new UsuarioGrupo();

					for (UsuarioGrupo usuarioGrupo : listaUsuariosGrupo) {
						if (usuarioGrupo.getId().equals(user.getUid())) {
							usuarioGrupoActual = usuarioGrupo;
						}
					}

					double pago;
					String deudaStr = "";

					if (isAdded()) {
						Context context = getContext();
						if (context != null) {

							tvTitulo.setText(grupo.getTitulo());
							tvDescripcion.setText(grupo.getDescripcion());
							tvTotal.setText(String.format(getString(R.string.tv_grupo_total_pagar), grupo.getTotal()));
							btnPers.setText(String.valueOf(listaUsuariosGrupo.size()));

							if (usuarioGrupoActual.getDeben() > usuarioGrupoActual.getDebes()) {

								pago = usuarioGrupoActual.getDeben() - usuarioGrupoActual.getDebes();
								deudaStr = String.format("Te deben: %.2f€", pago);
								tvDeuda.setTextColor(ContextCompat.getColor(getContext(), R.color.verde));
							} else {

								pago = usuarioGrupoActual.getDebes() - usuarioGrupoActual.getDeben();
								deudaStr = String.format("Debes: %.2f€", pago);
								tvDeuda.setTextColor(ContextCompat.getColor(getContext(), R.color.rojo));
							}
							tvDeuda.setText(deudaStr);
						}
					}
				}
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
		} else if (v.getId() == R.id.btnPagarDeudas) {
			pagarDeudas();
		} else if (v.getId() == R.id.btnPersGrupo) {
			conseguirListaUsuarios();
		}
	}


	private void pagarDeudas() {
		FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

		FirebaseDatabase.getInstance().getReference("Usuarios").child(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
			@Override
			public void onComplete(@NonNull Task<DataSnapshot> task) {
				if (task.isSuccessful()) {
					if (task.getResult().exists()) {

						DataSnapshot dataSnapshot = task.getResult();
						usuarioActual = dataSnapshot.getValue(Usuario.class);

						ArrayList<UsuarioGrupo> listaUsuariosGrupo = grupo.getUsuarios();
						for (UsuarioGrupo usuarioGrupo : listaUsuariosGrupo) {
							if (usuarioGrupo.getId().equals(user.getUid())) {
								usuarioGrupoActual = usuarioGrupo;
							}
						}

						double balance = usuarioActual.getBalance();
						if (balance > 0) {
							if (usuarioGrupoActual.getDebes() > 0) {

								//pillar la lista de gastos
								//ver en que gastos estoy
								//coger cuanto tengo que pagar de cada gasto : total / size()
								//pagar esa deuda restando de tu balance la deuda y añadiendosela al balance del usuario que la ha pagado
								//asi con toda la lista hasta que se acabe la lista


							}
						}

					}
				}

			}
		});
	}


	private void conseguirListaUsuarios() {
		listaUsuario = new ArrayList<>();

		FirebaseDatabase db = FirebaseDatabase.getInstance();
		listaUsuarioGrupo = grupo.getUsuarios();
		db.getReference("Usuarios").addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot snapshot) {
				for (DataSnapshot data : snapshot.getChildren()) {
					Usuario usuario = data.getValue(Usuario.class);
					for (int i = 0; i < listaUsuarioGrupo.size(); i++) {
						String id = listaUsuarioGrupo.get(i).getId();
						String usuarioId = usuario.getId();
						if (id.equals(usuarioId)) {
							listaUsuario.add(usuario);
						}
					}
				}
				mostrarDialogUsuarios();
			}

			@Override
			public void onCancelled(@NonNull DatabaseError error) {
			}
		});
	}

	public void mostrarDialogUsuarios() {
		MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(getContext());
		View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_usuarios, null);

		RecyclerView usuariosRecyclerView = dialogView.findViewById(R.id.rvUsuariosGrupo);

		AdapterUsuariosGrupo adapter = new AdapterUsuariosGrupo(listaUsuario);

		usuariosRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		usuariosRecyclerView.setAdapter(adapter);

		materialAlertDialogBuilder
				.setTitle("Usuarios")
				.setView(dialogView)
				.setPositiveButton("Añadir persona", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						AniadirDialogFragment adf = new AniadirDialogFragment();
						adf.show(getActivity().getSupportFragmentManager(), "AniadirDialogFragment");
					}
				})
				.setNegativeButton("Cerrar", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});

		materialAlertDialogBuilder.show();

	}

	@Override
	public void onAceptarDatos(String email) {
		ArrayList<Usuario> listaUsuarios = new ArrayList();
		listaUsuarioGrupo = grupo.getUsuarios();
		
		FirebaseDatabase.getInstance().getReference("Usuarios").addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot snapshot) {
				for (DataSnapshot data : snapshot.getChildren()) {
					Usuario usuario = data.getValue(Usuario.class);
					listaUsuarios.add(usuario);
				}

				if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
					boolean usuarioEncontrado = false;

					for (Usuario usuario : listaUsuarios) {
						if (usuario.getEmail().equals(email)) {
							usuarioEncontrado = true;
							usuarioAniadir = usuario;
							break;
						}
					}

					if (usuarioEncontrado) {
						for (UsuarioGrupo usuarioGrupo : listaUsuarioGrupo) {
							if (usuarioGrupo.getId().equals(usuarioAniadir.getId())) {
								usuarioEncontrado = true;
								break;
							} else {
								usuarioEncontrado = false;
							}
						}

						if (!usuarioEncontrado) {
							listaUsuarioGrupo.add(new UsuarioGrupo(0, 0, 0, usuarioAniadir.getId()));
							aniadirUsuario();
						} else {
							Toast.makeText(getContext(), getString(R.string.usuario_grupo), Toast.LENGTH_SHORT).show();
						}
					} else {
						Toast.makeText(getContext(), getString(R.string.user_no_registrado), Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(getContext(), getString(R.string.email_incorrecto), Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError error) {
				Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void aniadirUsuario() {
		ArrayList<String> listaGrupos = new ArrayList<>();
		listaGrupos = usuarioAniadir.getGrupos();

		if (listaGrupos == null) {
			listaGrupos = new ArrayList<>();
			listaGrupos.add(grupo.getId());
		} else {
			listaGrupos.add(grupo.getId());
		}

		FirebaseDatabase.getInstance().getReference("Usuarios").child(usuarioAniadir.getId()).child("grupos").setValue(listaGrupos)
				.addOnCompleteListener(new OnCompleteListener<Void>() {
					@Override
					public void onComplete(@NonNull Task<Void> task) {
						updateGrupo();
					}
				});
	}

	private void updateGrupo() {
		listaUsuario.add(usuarioAniadir);

		Map<String, Object> mapa = new HashMap<>();
		mapa.put("usuarios", listaUsuario);

		FirebaseDatabase.getInstance().getReference("Grupos").child(grupo.getId()).updateChildren(mapa)
				.addOnCompleteListener(new OnCompleteListener<Void>() {
					@Override
					public void onComplete(@NonNull Task<Void> task) {
						Toast.makeText(getContext(), "Usuario añadido correctamente", Toast.LENGTH_SHORT).show();
					}
				});
	}
}