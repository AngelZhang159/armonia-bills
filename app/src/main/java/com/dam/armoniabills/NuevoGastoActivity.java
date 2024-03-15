package com.dam.armoniabills;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dam.armoniabills.model.Gasto;
import com.dam.armoniabills.model.Grupo;
import com.dam.armoniabills.model.Historial;
import com.dam.armoniabills.model.Usuario;
import com.dam.armoniabills.model.UsuarioGrupo;
import com.dam.armoniabills.recyclerutils.AdapterUsuariosGasto;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NuevoGastoActivity extends AppCompatActivity implements View.OnClickListener {

	EditText etPrecio, etTitulo, etDescripcion;
	Button btnAniadir;
	AdapterUsuariosGasto adapterUsuariosGasto;
	RecyclerView rv;
	ArrayList<Usuario> listaUsuarios;
	ArrayList<UsuarioGrupo> listaGrupoUsuarios;
	Grupo grupo;
	FirebaseUser user;
	Usuario usuarioActual;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nuevo_gasto);

		etPrecio = findViewById(R.id.etPrecioGasto);
		etTitulo = findViewById(R.id.etTituloGasto);
		etDescripcion = findViewById(R.id.etDescGasto);
		btnAniadir = findViewById(R.id.btnAniadirGasto);
		rv = findViewById(R.id.rvUsuariosGasto);
		listaUsuarios = new ArrayList<>();

		btnAniadir.setOnClickListener(this);

		grupo = getIntent().getParcelableExtra("grupo");
		consultaUsuarios();
	}

	private void consultaUsuarios() {
		FirebaseDatabase db = FirebaseDatabase.getInstance();
		db.getReference(MainActivity.DB_PATH_GRUPOS).child(grupo.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot snapshot) {
				listaUsuarios.clear();

				listaGrupoUsuarios = snapshot.getValue(Grupo.class).getUsuarios();

				for (UsuarioGrupo usuarioGrupo : listaGrupoUsuarios) {
					db.getReference(MainActivity.DB_PATH_USUARIOS).child(usuarioGrupo.getId()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
						@Override
						public void onComplete(@NonNull Task<DataSnapshot> task) {
							if (task.isSuccessful()) {
								if (task.getResult().exists()) {
									DataSnapshot dataSnapshot = task.getResult();
									listaUsuarios.add(dataSnapshot.getValue(Usuario.class));

									if (listaGrupoUsuarios.size() == listaUsuarios.size()) {
										configurarRv();
									}
								}
							}
						}
					});
				}

			}

			@Override
			public void onCancelled(@NonNull DatabaseError error) {
				Toast.makeText(NuevoGastoActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void configurarRv() {
		adapterUsuariosGasto = new AdapterUsuariosGasto(listaUsuarios, this);
		rv.setHasFixedSize(true);
		rv.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
		rv.setAdapter(adapterUsuariosGasto);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btnAniadirGasto) {

			String titulo = etTitulo.getText().toString();
			String descripcion = etDescripcion.getText().toString();
			String precioString = etPrecio.getText().toString();

			if (titulo.isEmpty() || precioString.isEmpty()) {
				Toast.makeText(NuevoGastoActivity.this, R.string.campos_obligatorios, Toast.LENGTH_SHORT).show();
			} else {

				ArrayList<String> idsPagan = adapterUsuariosGasto.getIdsPagan();
				user = FirebaseAuth.getInstance().getCurrentUser();

				if (idsPagan.isEmpty()) {
					Toast.makeText(this, R.string.una_persona, Toast.LENGTH_SHORT).show();
				} else if (idsPagan.size() == 1 && idsPagan.get(0).equals(user.getUid())) {

					Toast.makeText(this, R.string.pagar_solo, Toast.LENGTH_SHORT).show();

				} else {
					double precio = Double.parseDouble(precioString);

					DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(MainActivity.DB_PATH_GRUPOS);
					String id = databaseReference.push().getKey();
					Gasto gasto = new Gasto(titulo, descripcion, user.getUid(), precio, idsPagan, id);

					databaseReference.child(grupo.getId()).child("gastos").child(id).setValue(gasto).addOnCompleteListener(new OnCompleteListener<Void>() {
						@Override
						public void onComplete(@NonNull Task<Void> task) {
							if (task.isSuccessful()) {
								Toast.makeText(NuevoGastoActivity.this, R.string.gasto_correcto, Toast.LENGTH_SHORT).show();
								aniadirHistorial();
								finish();
							}
						}
					});

					double total = grupo.getTotal() + precio;
					Map<String, Object> mapaTotal = new HashMap<>();
					mapaTotal.put("total", total);
					databaseReference.child(grupo.getId()).updateChildren(mapaTotal);

					double deuda = precio / idsPagan.size();
					double teDeben = precio - deuda;

					DatabaseReference reference = FirebaseDatabase.getInstance().getReference(MainActivity.DB_PATH_GRUPOS).child(grupo.getId()).child("usuarios");

					for (int i = 0; i < grupo.getUsuarios().size(); i++) {
						final int index = i;
						reference.child(String.valueOf(index)).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
							@Override
							public void onComplete(@NonNull Task<DataSnapshot> task) {
								if (task.isSuccessful()) {
									if (task.getResult().exists()) {
										DataSnapshot dataSnapshot = task.getResult();
										UsuarioGrupo usuarioGrupo = dataSnapshot.getValue(UsuarioGrupo.class);
										double debesActualizado;
										double debenActualizado;
										for (int j = 0; j < idsPagan.size(); j++) {
											if (usuarioGrupo.getId().equals(user.getUid())) {
												Map<String, Object> mapDeben = new HashMap<>();
												if (usuarioGrupo.getId().equals(user.getUid()) && idsPagan.contains(user.getUid())) {
													debenActualizado = usuarioGrupo.getDeben() + teDeben;
												} else {
													debenActualizado = usuarioGrupo.getDeben() + precio;
												}
												if (usuarioGrupo.getDebes() == debenActualizado) {
													mapDeben.put("deben", 0);
													reference.child(String.valueOf(index)).updateChildren(mapDeben);
													mapDeben.clear();
													mapDeben.put("debes", 0);
													reference.child(String.valueOf(index)).updateChildren(mapDeben);
													eliminarDeListaGastos(user.getUid());
												} else if (debenActualizado > usuarioGrupo.getDebes()) {
													debenActualizado = debenActualizado - usuarioGrupo.getDebes();
													mapDeben.put("deben", debenActualizado);
													reference.child(String.valueOf(index)).updateChildren(mapDeben);
													mapDeben.clear();
													mapDeben.put("debes", 0);
													reference.child(String.valueOf(index)).updateChildren(mapDeben);
													eliminarDeListaGastos(user.getUid());
												} else {
													debesActualizado = usuarioGrupo.getDebes() - debenActualizado;
													mapDeben.put("debes", debesActualizado);
													reference.child(String.valueOf(index)).updateChildren(mapDeben);
													mapDeben.clear();
													mapDeben.put("deben", 0);
													reference.child(String.valueOf(index)).updateChildren(mapDeben);
												}
											} else if (usuarioGrupo.getId().equals(idsPagan.get(j))) {
												debesActualizado = usuarioGrupo.getDebes() + deuda;
												Map<String, Object> mapDebes = new HashMap<>();
												if (usuarioGrupo.getDeben() == debesActualizado) {
													mapDebes.put("deben", 0);
													reference.child(String.valueOf(index)).updateChildren(mapDebes);
													mapDebes.clear();
													mapDebes.put("debes", 0);
													reference.child(String.valueOf(index)).updateChildren(mapDebes);
													eliminarDeListaGastos(idsPagan.get(j));
												} else if (debesActualizado > usuarioGrupo.getDeben()) {
													debesActualizado = debesActualizado - usuarioGrupo.getDeben();
													mapDebes.put("debes", debesActualizado);
													reference.child(String.valueOf(index)).updateChildren(mapDebes);
													mapDebes.clear();
													mapDebes.put("deben", 0);
													reference.child(String.valueOf(index)).updateChildren(mapDebes);
												} else if (debesActualizado < usuarioGrupo.getDeben()) {
													eliminarDeListaGastos(idsPagan.get(j));
													debesActualizado = usuarioGrupo.getDeben() - debesActualizado;
													mapDebes.put("deben", debesActualizado);
													reference.child(String.valueOf(index)).updateChildren(mapDebes);
													mapDebes.clear();
													mapDebes.put("debes", 0);
													reference.child(String.valueOf(index)).updateChildren(mapDebes);
												}
											}
										}
									}
								}
							}
						});
					}
				}
			}
		}
	}

	private void eliminarDeListaGastos(String id) {
		ArrayList<Gasto> listaGastos = new ArrayList<>();
		FirebaseDatabase.getInstance().getReference("Grupos").child(grupo.getId()).child("gastos").addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot snapshot) {
				listaGastos.clear();
				for (DataSnapshot data : snapshot.getChildren()) {
					Gasto gasto = data.getValue(Gasto.class);
					listaGastos.add(gasto);
				}
				for (Gasto gasto : listaGastos) {
					ArrayList<String> listaIds = gasto.getListaUsuariosPagan();
					for (int i = 0; i < listaIds.size(); i++) {
						if (listaIds.get(i).equals(id)) {
							Map<String, Object> mapId = new HashMap<>();
							mapId.put(String.valueOf(i), String.valueOf(i));
							FirebaseDatabase.getInstance().getReference("Grupos").child(grupo.getId()).child("gastos").child(gasto.getId()).child("listaUsuariosPagan").updateChildren(mapId);
						}
					}
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError error) {
			}
		});
	}

	private void aniadirHistorial() {
		FirebaseDatabase.getInstance().getReference(MainActivity.DB_PATH_USUARIOS).child(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
			@Override
			public void onComplete(@NonNull Task<DataSnapshot> task) {
				if (task.isSuccessful()) {
					if (task.getResult().exists()) {
						usuarioActual = task.getResult().getValue(Usuario.class);
						String id = FirebaseDatabase.getInstance().getReference("Historial").push().getKey();
						Historial historial = new Historial(id, grupo.getTitulo(), usuarioActual.getNombre(), usuarioActual.getImagenPerfil(), new Date().getTime());
						for (Usuario usuario : listaUsuarios) {
							FirebaseDatabase.getInstance().getReference("Historial").child(usuario.getId()).child(id).setValue(historial).addOnCompleteListener(new OnCompleteListener<Void>() {
								@Override
								public void onComplete(@NonNull Task<Void> task) {
								}
							});
						}
					}
				}
			}
		});
	}

}