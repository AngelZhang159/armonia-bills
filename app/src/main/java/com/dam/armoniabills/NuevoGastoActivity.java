package com.dam.armoniabills;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
	private String current;

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

		etPrecio.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (!s.toString().equals(current)) {
					// Enforce two decimal places
					int decimalIndex = s.toString().indexOf(".");
					if (decimalIndex > 0) {
						if (s.toString().length() - decimalIndex - 1 > 2) {
							String newText = s.toString().substring(0, decimalIndex + 3);
							current = newText;
							etPrecio.setText(newText);
							etPrecio.setSelection(newText.length());
						} else {
							current = s.toString();
						}
					} else {
						current = s.toString();
					}
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

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
//		Botón añadir nuevo gasto

		if (v.getId() == R.id.btnAniadirGasto) {
			String titulo = etTitulo.getText().toString();
			String descripcion = etDescripcion.getText().toString();
			String precioString = etPrecio.getText().toString();

//			Comprobar vacío titulo o precio
			if (titulo.isEmpty() || precioString.isEmpty()) {
//				Algun campo vacio - Error
				Toast.makeText(NuevoGastoActivity.this, R.string.campos_obligatorios, Toast.LENGTH_SHORT).show();
			} else {
//				Están rellenos titulo y precio
				ArrayList<String> idsPagan = adapterUsuariosGasto.getIdsPagan();
				user = FirebaseAuth.getInstance().getCurrentUser();

				if (idsPagan.isEmpty()) {
//					No hay una persona seleccionada - Error
					Toast.makeText(this, R.string.una_persona, Toast.LENGTH_SHORT).show();
				} else if (idsPagan.size() == 1 && idsPagan.get(0).equals(user.getUid())) {
//					Solo una persona y es el propio usuario - Error
					Toast.makeText(this, R.string.pagar_solo, Toast.LENGTH_SHORT).show();
				} else {
//					Continuar
					guardarNuevoGasto(titulo, descripcion, idsPagan);
				}
			}
		}

	}

	private void guardarNuevoGasto(String titulo, String descripcion, ArrayList<String> idsPagan) {
//		Precio parseado solo 2 decimales
		double precio = Double.parseDouble(etPrecio.getText().toString());

//		Lista de ref de los grupos
		DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(MainActivity.DB_PATH_GRUPOS);
//		Id para nuevo gasto
		String id = databaseReference.push().getKey();
//		Nuevo gasto

		Gasto gasto = new Gasto(titulo, descripcion, user.getUid(), precio, idsPagan, id);

		guardarEnBaseDatos(gasto, databaseReference, id);

		actualizarTotalDeGrupo(precio, databaseReference);

		dividirPagoCadaPersona(precio, idsPagan);
	}

	private void dividirPagoCadaPersona(double precio, ArrayList<String> idsPagan) {
		double deudaCadaUno = precio / idsPagan.size();

		int deudaTruncInt = (int) (deudaCadaUno * 100);
		double deudaTruncFin = (double) deudaTruncInt / 100;

		DatabaseReference reference = FirebaseDatabase.getInstance().getReference(MainActivity.DB_PATH_GRUPOS).child(grupo.getId()).child("usuarios");

//		Actualizar debes de los demas
		double faltanCents = (precio - (deudaTruncFin * idsPagan.size()));
		double totalTeDeben = Math.round((precio - deudaTruncFin) * 100) / 100.0;
		actualizarDeben(reference, totalTeDeben, faltanCents, idsPagan, deudaTruncFin, precio);
	}

	private void actualizarDeben(DatabaseReference reference, double totalTeDeben, double faltanCents, ArrayList<String> idsPagan, double deudaTruncFin, double precio) {

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
							double deuda;
							double falta = faltanCents;
							for (int j = 0; j < idsPagan.size(); j++) {

								if (falta > 0) {
									deuda = deudaTruncFin + .01;
									falta = (Math.round((falta) * 100) / 100.0) - 0.01;
								} else {
									deuda = deudaTruncFin;
								}

								if (usuarioGrupo.getId().equals(user.getUid())) {

									Map<String, Object> mapDeben = new HashMap<>();

									//Si el usuario que ha pagado no está en la lista de ids que han pagado, se le suma el precio a lo que le deben en vez de el precio menos su parte

									if (usuarioGrupo.getId().equals(user.getUid()) && idsPagan.contains(user.getUid())) {

										debenActualizado = (Math.round((usuarioGrupo.getDeben() + totalTeDeben) * 100) / 100.0);

									} else {
										debenActualizado = (Math.round((usuarioGrupo.getDeben() + precio) * 100) / 100.0);
									}

									if (usuarioGrupo.getDebes() == debenActualizado) {

										// Si debes y deben son iguales se cambian a 0 los dos

										mapDeben.put("deben", 0);
										reference.child(String.valueOf(index)).updateChildren(mapDeben);

										mapDeben.clear();
										mapDeben.put("debes", 0);
										reference.child(String.valueOf(index)).updateChildren(mapDeben);

										eliminarDeListaGastos(user.getUid());


									} else if (debenActualizado > usuarioGrupo.getDebes()) {

										//Si lo que te deben ahora es mayor a lo que debes entonces lo que te deben es la diferencia
										debenActualizado = (Math.round((debenActualizado - usuarioGrupo.getDebes()) * 100) / 100.0);
										mapDeben.put("deben", debenActualizado);
										reference.child(String.valueOf(index)).updateChildren(mapDeben);

										mapDeben.clear();
										mapDeben.put("debes", 0);
										reference.child(String.valueOf(index)).updateChildren(mapDeben);

										//TODO borrar id del usuario en todos los gastos
										eliminarDeListaGastos(user.getUid());

									} else {

										//Si lo que debes ahora es mayor a lo que deben entonces lo que debes es la diferencia

										debesActualizado = (Math.round((usuarioGrupo.getDebes() - debenActualizado) * 100) / 100.0);
										mapDeben.put("debes", debesActualizado);
										reference.child(String.valueOf(index)).updateChildren(mapDeben);

										mapDeben.clear();
										mapDeben.put("deben", 0);
										reference.child(String.valueOf(index)).updateChildren(mapDeben);

									}


								} else if (usuarioGrupo.getId().equals(idsPagan.get(j))) {

									//TODO se ha bugueao
									debesActualizado = (Math.round((usuarioGrupo.getDebes() + deuda) * 100) / 100.0);

									Map<String, Object> mapDebes = new HashMap<>();

									if (usuarioGrupo.getDeben() == debesActualizado) {

										// Si debes y deben son iguales se cambian a 0 los dos

										mapDebes.put("deben", 0);
										reference.child(String.valueOf(index)).updateChildren(mapDebes);

										mapDebes.clear();
										mapDebes.put("debes", 0);
										reference.child(String.valueOf(index)).updateChildren(mapDebes);

										eliminarDeListaGastos(idsPagan.get(j));


									} else if (debesActualizado > usuarioGrupo.getDeben()) {

										//Si lo que debes ahora es mayor a lo que te deben entonces lo que debes es la diferencia
										debesActualizado = (Math.round((debesActualizado - usuarioGrupo.getDeben()) * 100) / 100.0);

										mapDebes.put("debes", debesActualizado);
										reference.child(String.valueOf(index)).updateChildren(mapDebes);

										mapDebes.clear();
										mapDebes.put("deben", 0);
										reference.child(String.valueOf(index)).updateChildren(mapDebes);

									} else {

										//Si lo que te deben ahora es mayor a lo que debes entonces lo que te deben es la diferencia

										//TODO borrar id del usuario en todos los gastos
										eliminarDeListaGastos(idsPagan.get(j));

										debesActualizado = (Math.round((usuarioGrupo.getDeben() - debesActualizado) * 100) / 100.0);

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

	private void actualizarTotalDeGrupo(double precio, DatabaseReference databaseReference) {
		double total = grupo.getTotal() + precio;

		Map<String, Object> mapaTotal = new HashMap<>();
		mapaTotal.put("total", total);
		databaseReference.child(grupo.getId()).updateChildren(mapaTotal);
	}

	private void guardarEnBaseDatos(Gasto gasto, DatabaseReference databaseReference, String id) {
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
	}

	private void eliminarDeListaGastos(String id) {


		ArrayList<Gasto> listaGastos = new ArrayList<>();
		FirebaseDatabase.getInstance().getReference("Grupos").child(grupo.getId()).child("gastos").addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot snapshot) {
				listaGastos.clear(); // Clear the list before adding new data
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
		FirebaseDatabase.getInstance().getReference(MainActivity.DB_PATH_USUARIOS).child(user.getUid()).get()
				.addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
					@Override
					public void onComplete(@NonNull Task<DataSnapshot> task) {
						if (task.isSuccessful()) {
							if (task.getResult().exists()) {
								usuarioActual = task.getResult().getValue(Usuario.class);

								String id = FirebaseDatabase.getInstance().getReference("Historial").push().getKey();

								Historial historial = new Historial(id, grupo.getTitulo(),
										usuarioActual.getNombre(),
										usuarioActual.getImagenPerfil(), new Date().getTime());

								for (Usuario usuario : listaUsuarios) {
									FirebaseDatabase.getInstance().getReference("Historial").child(usuario.getId()).child(id)
											.setValue(historial).addOnCompleteListener(new OnCompleteListener<Void>() {
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
	public static boolean acaba99(double number) {
		int multiplied = (int) (number * 100);
		return multiplied % 100 == 99;
	}
}