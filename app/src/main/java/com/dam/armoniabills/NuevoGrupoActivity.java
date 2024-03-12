package com.dam.armoniabills;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.dam.armoniabills.model.Gasto;
import com.dam.armoniabills.model.Grupo;
import com.dam.armoniabills.model.Usuario;
import com.dam.armoniabills.model.UsuarioGrupo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class NuevoGrupoActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

	EditText etNombre, etDescripcion, etEmail;
	ListView listView;
	Button btnAniadir, btnAceptar, btnCancelar;

	ArrayAdapter<String> adapter;

	ArrayList<Usuario> listaUsuario;
	ArrayList<UsuarioGrupo> listaUsuarioGrupo;
	ArrayList<Gasto> listaGastos;
	ArrayList<String> listaNombres;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nuevo_grupo);

		etNombre = findViewById(R.id.etTituloNuevoGrupo);
		etDescripcion = findViewById(R.id.etDescNuevoGrupo);
		etEmail = findViewById(R.id.etAniadirPersonaEmail);

		listView = findViewById(R.id.lvUsuarios);

		btnAniadir = findViewById(R.id.btnAniadirEmailNuevoGrupo);
		btnAceptar = findViewById(R.id.btnCrearNuevoGrupo);
		btnCancelar = findViewById(R.id.btnCancelarNuevoGrupo);

		listaUsuarioGrupo = new ArrayList<>();
		listaGastos = new ArrayList<>();
		listaUsuario = new ArrayList<>();
		listaNombres = new ArrayList<>();

		FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
		listaUsuarioGrupo.add(new UsuarioGrupo(0, 0, 0, user.getEmail()));
		listaNombres.add("Yo (" + user.getEmail() + ")");

		adapter = new ArrayAdapter<>(NuevoGrupoActivity.this, android.R.layout.simple_list_item_1, listaNombres);
		listView.setAdapter(adapter);

		btnAniadir.setOnClickListener(this);
		btnAceptar.setOnClickListener(this);
		btnCancelar.setOnClickListener(this);
		listView.setOnItemClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btnCrearNuevoGrupo) {
			String titulo = etNombre.getText().toString();
			String descripcion = etDescripcion.getText().toString();

			if (titulo.isEmpty()) {
				Toast.makeText(this, "Debe introducir un titulo", Toast.LENGTH_SHORT).show();
			} else {
				Grupo grupo = new Grupo(titulo, descripcion, listaUsuarioGrupo, 0, listaGastos);

				FirebaseDatabase.getInstance().getReference("Grupos").push().setValue(grupo).addOnCompleteListener(new OnCompleteListener<Void>() {
					@Override
					public void onComplete(@NonNull Task<Void> task) {
						if (task.isSuccessful()) {
							Toast.makeText(NuevoGrupoActivity.this, "Grupo creado correctamente", Toast.LENGTH_SHORT).show();
							Intent i = new Intent(NuevoGrupoActivity.this, MainActivity.class);
							startActivity(i);
							finish();
						}
					}
				});
			}
		} else if (v.getId() == R.id.btnAniadirEmailNuevoGrupo) {
			aniadirUsuario();
		} else if (v.getId() == R.id.btnCancelarNuevoGrupo) {
			finish();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (position == 0) {
			Toast.makeText(NuevoGrupoActivity.this, "No puede eliminarse del grupo", Toast.LENGTH_SHORT).show();
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Eliminar participante");
			builder.setCancelable(false);
			builder.setMessage("¿Estás seguro de que quieres eliminar este participante?");
			builder.setPositiveButton(R.string.btn_aceptar_d, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					listaUsuarioGrupo.remove(position);
					listaNombres.remove(position);
					adapter.notifyDataSetChanged();
				}
			});
			builder.setNegativeButton(R.string.btn_cancelar_d, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});

			AlertDialog ad = builder.create();
			ad.setCanceledOnTouchOutside(false);

			ad.show();
		}
	}

	private void aniadirUsuario() {
		FirebaseDatabase.getInstance().getReference("Usuarios").addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot snapshot) {
				for (DataSnapshot data : snapshot.getChildren()) {
					Usuario usuario = data.getValue(Usuario.class);
					listaUsuario.add(usuario);
				}

				String email = etEmail.getText().toString();
				String nombre = "";

				if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
					boolean usuarioEncontrado = false;

					for (Usuario usuario : listaUsuario) {
						if (usuario.getEmail().equals(email)) {
							usuarioEncontrado = true;
							nombre = usuario.getNombre();
							break;
						}
					}

					if (usuarioEncontrado) {
						for (UsuarioGrupo usuarioGrupo : listaUsuarioGrupo) {
							if (usuarioGrupo.getEmail().equals(email)) {
								usuarioEncontrado = true;
								break;
							} else {
								usuarioEncontrado = false;
							}
						}

						if (!usuarioEncontrado) {
							listaUsuarioGrupo.add(new UsuarioGrupo(0, 0, 0, email));
							listaNombres.add(nombre);
							adapter.notifyDataSetChanged();
							etEmail.setText("");
						} else {
							Toast.makeText(NuevoGrupoActivity.this, "Este usuario ya pertenece al grupo", Toast.LENGTH_SHORT).show();
						}
					} else {
						Toast.makeText(NuevoGrupoActivity.this, "El email introducido no está registrado", Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(NuevoGrupoActivity.this, "El email introducido es inválido", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError error) {
				Toast.makeText(NuevoGrupoActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
			}
		});
	}
}