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
import com.dam.armoniabills.model.Usuario;
import com.dam.armoniabills.model.UsuarioGrupo;
import com.dam.armoniabills.recyclerutils.AdapterUsuariosGasto;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
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
		db.getReference("Grupos").child(grupo.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot snapshot) {
				listaUsuarios.clear();

				listaGrupoUsuarios = snapshot.getValue(Grupo.class).getUsuarios();

				for (UsuarioGrupo usuarioGrupo : listaGrupoUsuarios) {
					db.getReference("Usuarios").child(usuarioGrupo.getId()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
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
				Toast.makeText(NuevoGastoActivity.this, "Debes rellenar los campos necesarios", Toast.LENGTH_SHORT).show();
			} else {

				ArrayList<String> idsPagan = adapterUsuariosGasto.getIdsPagan();
				FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

				if (idsPagan.isEmpty()) {
					Toast.makeText(this, "Debes seleccionar al menos 1 personas", Toast.LENGTH_SHORT).show();
				} else if (idsPagan.size() == 1 && idsPagan.get(0).equals(user.getUid())) {

					Toast.makeText(this, "No puedes pagarlo solo tu", Toast.LENGTH_SHORT).show();

				} else {
					double precio = Double.parseDouble(etPrecio.getText().toString());

					Gasto gasto = new Gasto(titulo, descripcion, user.getUid(), precio, idsPagan);

//					Introducir datos en grupos/grupo/gastos

					DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Grupos");

					String id = databaseReference.push().getKey();
					databaseReference.child(grupo.getId()).child("gastos").child(id).setValue(gasto).addOnCompleteListener(new OnCompleteListener<Void>() {
						@Override
						public void onComplete(@NonNull Task<Void> task) {
							if (task.isSuccessful()) {
								Toast.makeText(NuevoGastoActivity.this, "Gasto añadido con éxito", Toast.LENGTH_SHORT).show();
								finish();
							}
						}
					}).addOnFailureListener(new OnFailureListener() {
						@Override
						public void onFailure(@NonNull Exception e) {
							Toast.makeText(NuevoGastoActivity.this, "Error al añadir el gasto", Toast.LENGTH_SHORT).show();
						}
					});


					//Actualizar total

					double total = grupo.getTotal() + precio;
					Map<String, Object> mapaTotal = new HashMap<>();
					mapaTotal.put("total", total);
					databaseReference.child(grupo.getId()).updateChildren(mapaTotal);


//					Hacer que paguen

					double deuda = precio / idsPagan.size();
					double teDeben = precio - deuda;

					DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Grupos").child(grupo.getId()).child("usuarios");

					for (int i = 0; i < idsPagan.size(); i++) {

						final int index = i;

						reference.child(String.valueOf(index)).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
							@Override
							public void onComplete(@NonNull Task<DataSnapshot> task) {
								if (task.isSuccessful()) {
									if (task.getResult().exists()) {
										DataSnapshot dataSnapshot = task.getResult();
										UsuarioGrupo usuarioGrupo = dataSnapshot.getValue(UsuarioGrupo.class);

										double debes;
										double deben;
										for (int j = 0; j < idsPagan.size(); j++) {

											if(usuarioGrupo.getId().equals(user.getUid())){
												deben = usuarioGrupo.getDeben() + teDeben;
												Map<String, Object> mapDeben = new HashMap<>();
												mapDeben.put("deben", deben);
												reference.child(String.valueOf(index)).updateChildren(mapDeben);

											} else if (usuarioGrupo.getId().equals(idsPagan.get(j))) {
												debes = usuarioGrupo.getDebes() + deuda;
												Map<String, Object> mapDebes = new HashMap<>();
												mapDebes.put("debes", debes);
												reference.child(String.valueOf(index)).updateChildren(mapDebes);
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
}