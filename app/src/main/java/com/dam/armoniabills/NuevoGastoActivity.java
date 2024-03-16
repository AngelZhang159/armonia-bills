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

import java.math.BigDecimal;
import java.math.RoundingMode;
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

			//TODO optimizar

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
					double precioAnt = Double.parseDouble(etPrecio.getText().toString());
					BigDecimal bd = new BigDecimal(precioAnt);
					bd = bd.setScale(2, RoundingMode.HALF_UP);

					double precio = bd.doubleValue();

//					Introducir datos en grupos/grupo/gastos

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


					//Actualizar total

					double precioFrm = grupo.getTotal() + precio;

					BigDecimal bdt = new BigDecimal(precioFrm);
					bdt = bdt.setScale(2, RoundingMode.HALF_UP);

					double total = bdt.doubleValue();

					Map<String, Object> mapaTotal = new HashMap<>();
					mapaTotal.put("total", total);
					databaseReference.child(grupo.getId()).updateChildren(mapaTotal);


//					Hacer que paguen

					double deuda = precio / idsPagan.size();
					double totalp = precio - deuda;

					BigDecimal bdp = new BigDecimal(totalp);
					bdp = bdp.setScale(2, RoundingMode.HALF_UP);

					double teDeben = bdp.doubleValue();

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

												//Si el usuario que ha pagado no está en la lista de ids que han pagado, se le suma el precio a lo que le deben en vez de el precio menos su parte

												if(usuarioGrupo.getId().equals(user.getUid()) && idsPagan.contains(user.getUid())){

													debenActualizado = usuarioGrupo.getDeben() + teDeben;

												} else {

													debenActualizado = usuarioGrupo.getDeben() + precio;
												}

												if(usuarioGrupo.getDebes() == debenActualizado){

													// Si debes y deben son iguales se cambian a 0 los dos

													mapDeben.put("deben", 0);
													reference.child(String.valueOf(index)).updateChildren(mapDeben);

													mapDeben.clear();
													mapDeben.put("debes", 0);
													reference.child(String.valueOf(index)).updateChildren(mapDeben);

													eliminarDeListaGastos(user.getUid());


												} else if(debenActualizado > usuarioGrupo.getDebes()){

													//Si lo que te deben ahora es mayor a lo que debes entonces lo que te deben es la diferencia

													debenActualizado = debenActualizado - usuarioGrupo.getDebes();
													mapDeben.put("deben", debenActualizado);
													reference.child(String.valueOf(index)).updateChildren(mapDeben);

													mapDeben.clear();
													mapDeben.put("debes", 0);
													reference.child(String.valueOf(index)).updateChildren(mapDeben);

													//TODO borrar id del usuario en todos los gastos
													eliminarDeListaGastos(user.getUid());

												} else {

													//Si lo que debes ahora es mayor a lo que deben entonces lo que debes es la diferencia

													debesActualizado = usuarioGrupo.getDebes() - debenActualizado;
													mapDeben.put("debes", debesActualizado);
													reference.child(String.valueOf(index)).updateChildren(mapDeben);

													mapDeben.clear();
													mapDeben.put("deben", 0);
													reference.child(String.valueOf(index)).updateChildren(mapDeben);

												}


											} else if (usuarioGrupo.getId().equals(idsPagan.get(j))) {

												//TODO se ha bugueao

												debesActualizado = usuarioGrupo.getDebes() + deuda;
												Map<String, Object> mapDebes = new HashMap<>();

												if(usuarioGrupo.getDeben() == debesActualizado){

													// Si debes y deben son iguales se cambian a 0 los dos

													mapDebes.put("deben", 0);
													reference.child(String.valueOf(index)).updateChildren(mapDebes);

													mapDebes.clear();
													mapDebes.put("debes", 0);
													reference.child(String.valueOf(index)).updateChildren(mapDebes);

													eliminarDeListaGastos(idsPagan.get(j));


												} else if(debesActualizado > usuarioGrupo.getDeben()){

													//Si lo que debes ahora es mayor a lo que te deben entonces lo que debes es la diferencia

													debesActualizado = debesActualizado - usuarioGrupo.getDeben();
													//

													BigDecimal bdda = new BigDecimal(debesActualizado);
													bdda = bdda.setScale(2, RoundingMode.HALF_UP);

													double debesActualizadoF = bdda.doubleValue();

													mapDebes.put("debes", debesActualizadoF);
													reference.child(String.valueOf(index)).updateChildren(mapDebes);

													mapDebes.clear();
													mapDebes.put("deben", 0);
													reference.child(String.valueOf(index)).updateChildren(mapDebes);

												} else {

													//Si lo que te deben ahora es mayor a lo que debes entonces lo que te deben es la diferencia

													//TODO borrar id del usuario en todos los gastos
													eliminarDeListaGastos(idsPagan.get(j));


													debesActualizado = usuarioGrupo.getDeben() - debesActualizado;

													BigDecimal bdda = new BigDecimal(debesActualizado);
													bdda = bdda.setScale(2, RoundingMode.HALF_UP);

													double debesActualizadoF = bdda.doubleValue();

													mapDebes.put("deben", debesActualizadoF);
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

//					Fin hacer que paguen


				}

			}
		}
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

				for(Gasto gasto : listaGastos){

					ArrayList<String> listaIds = gasto.getListaUsuariosPagan();

					for (int i = 0; i < listaIds.size(); i++) {

						if(listaIds.get(i).equals(id)){

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
}