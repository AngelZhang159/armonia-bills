package com.dam.armoniabills.fragments;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import com.dam.armoniabills.R;
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


public class GrupoFragment extends Fragment implements View.OnClickListener {

	private static final String ARG_PARAM1 = "param1";
	
	TextView tvTitulo, tvDescripcion, tvDeuda, tvTotal;
	Button btnPagar;
	MaterialButton btnPers;
	ExtendedFloatingActionButton efab;
	RecyclerView rv;
	AdapterGastos adapter;
	
	ArrayList<Gasto> listaGastos;
	Usuario usuarioActual;
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
		FirebaseDatabase.getInstance().getReference(MainActivity.DB_PATH_GRUPOS).child(grupo.getId()).child("gastos").addValueEventListener(new ValueEventListener() {
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

		db.getReference(MainActivity.DB_PATH_GRUPOS).child(grupo.getId()).addValueEventListener(new ValueEventListener() {
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
								deudaStr = String.format(getString(R.string.te_deben_g), pago);
								tvDeuda.setTextColor(ContextCompat.getColor(getContext(), R.color.verde));
							} else {

								pago = usuarioGrupoActual.getDebes() - usuarioGrupoActual.getDeben();
								deudaStr = String.format(getString(R.string.debes_g), pago);
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

		FirebaseDatabase.getInstance().getReference(MainActivity.DB_PATH_USUARIOS).child(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
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

						//TODO dejar solo si el balance es mayor que la deuda?
						//TODO alomejor hay que actualizar el balance mas a abajo
						double balance = usuarioActual.getBalance();
						if (balance > 0) {
							if (usuarioGrupoActual.getDebes() > 0) {

								//Obtener la lista de gastos en los que estas y que no has pagado tu

								ArrayList<Gasto> listaGastosUsuario = new ArrayList<>();
								for(Gasto gasto : listaGastos){
									if(!gasto.getIdUsuario().equals(user.getUid())){
										for (int i = 0; i < gasto.getListaUsuariosPagan().size(); i++) {
											if(gasto.getListaUsuariosPagan().get(i).equals(user.getUid()))
												listaGastosUsuario.add(gasto);
										}
									}
								}

								for (int i = 0; i < listaGastosUsuario.size(); i++) {
									String idUsuarioAPagar = listaGastosUsuario.get(i).getIdUsuario();

									double deudaPorUsuario = (listaGastosUsuario.get(i).getPrecio() / listaGastosUsuario.get(i).getListaUsuariosPagan().size());
									double balanceMenosDeuda = balance - deudaPorUsuario;
									Map<String, Object> mapBalance = new HashMap<>();
									mapBalance.put("balance", balanceMenosDeuda);

									// Actualizar tu balance
									FirebaseDatabase.getInstance().getReference(MainActivity.DB_PATH_USUARIOS).child(user.getUid()).updateChildren(mapBalance);

									//Actualizar el balance del que ha pagado
									FirebaseDatabase.getInstance().getReference(MainActivity.DB_PATH_USUARIOS).child(idUsuarioAPagar).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
										@Override
										public void onComplete(@NonNull Task<DataSnapshot> task) {
											if(task.isSuccessful()){
												if(task.getResult().exists()){
													DataSnapshot snapshot = task.getResult();
													Usuario usuarioAPagar = snapshot.getValue(Usuario.class);
													double balanceUsuarioAPagar = usuarioAPagar.getBalance();

													mapBalance.clear();
													mapBalance.put("balance", (balanceUsuarioAPagar + deudaPorUsuario));
													FirebaseDatabase.getInstance().getReference(MainActivity.DB_PATH_USUARIOS).child(idUsuarioAPagar).updateChildren(mapBalance);
												}
											}
										}
									});

									//cambiar el id del usuario del gasto a 0 cuando lo pague para que cuando obtenga la lista de gastos no lo tenga qeu pagar otra vez

									int posicionUsuarioListaUsuariosPagan = 0;
									for (int j = 0; j <listaGastosUsuario.get(i).getListaUsuariosPagan().size(); j++) {
										if(listaGastosUsuario.get(i).getListaUsuariosPagan().get(j).equals(user.getUid())){
											posicionUsuarioListaUsuariosPagan = j;
										}

									}
									mapBalance.clear();
									mapBalance.put(String.valueOf(posicionUsuarioListaUsuariosPagan), String.valueOf(posicionUsuarioListaUsuariosPagan));

									FirebaseDatabase.getInstance().getReference(MainActivity.DB_PATH_GRUPOS).child(grupo.getId()).child("gastos").child(String.valueOf(listaGastosUsuario.get(i).getId())).child("listaUsuariosPagan").updateChildren(mapBalance);


									// Actualizar debes y deben

									for (int k = 0; k < listaUsuariosGrupo.size(); k++) {

										if(listaUsuariosGrupo.get(k).getId().equals(user.getUid())){

											mapBalance.clear();
											double debesAcutalizado = listaUsuariosGrupo.get(k).getDebes() - deudaPorUsuario;
											mapBalance.put("debes", debesAcutalizado);
											FirebaseDatabase.getInstance().getReference(MainActivity.DB_PATH_GRUPOS).child(grupo.getId()).child("usuarios").child(String.valueOf(k)).updateChildren(mapBalance);

										} else if(listaUsuariosGrupo.get(k).getId().equals(idUsuarioAPagar)){

											mapBalance.clear();
											double debenAcutalizado = listaUsuariosGrupo.get(k).getDeben() - deudaPorUsuario;
											mapBalance.put("deben", debenAcutalizado);
											FirebaseDatabase.getInstance().getReference(MainActivity.DB_PATH_GRUPOS).child(grupo.getId()).child("usuarios").child(String.valueOf(k)).updateChildren(mapBalance);

										}

									}

								}





							} else {
								Toast.makeText(getContext(), "No tienes deudas", Toast.LENGTH_SHORT).show();
							}
						} else {
							Toast.makeText(getContext(), "No tienes suficiente saldo para pagar tus deudas", Toast.LENGTH_SHORT).show();
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
				.setTitle(R.string.tit_dialog_users)
				.setView(dialogView)
				.setNegativeButton(R.string.btn_aceptar_d, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});

		materialAlertDialogBuilder.show();

	}
}