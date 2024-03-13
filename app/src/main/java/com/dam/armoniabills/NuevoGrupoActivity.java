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

	private static final String DB_PATH_GRUPOS = "Grupos";
	private static final String DB_PATH_USERS = "Usuarios";

	EditText etNombre, etDescripcion, etEmail;
	ListView listView;
	Button btnAniadir, btnAceptar, btnCancelar;

	ArrayAdapter<String> adapter;

	ArrayList<Usuario> listaUsuario;
	ArrayList<UsuarioGrupo> listaUsuarioGrupo;
	ArrayList<Gasto> listaGastos;
	ArrayList<String> listaNombres;
	ArrayList<String> listaId;
	ArrayList<String> listaIdGrupos;

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
		listaIdGrupos = new ArrayList<>();
		listaId = new ArrayList<>();

		FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
		listaUsuarioGrupo.add(new UsuarioGrupo(0, 0, 0, user.getUid()));
		listaNombres.add("Yo (" + user.getEmail() + ")");
		listaId.add(user.getUid());

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
				Toast.makeText(this, getString(R.string.campos_obligatorios), Toast.LENGTH_SHORT).show();
			} else {
				if (listaUsuarioGrupo.size() == 1) {
					Toast.makeText(this, getString(R.string.minimo_personas), Toast.LENGTH_SHORT).show();
				} else {
					String id = FirebaseDatabase.getInstance().getReference(DB_PATH_GRUPOS).push().getKey();

					Grupo grupo = new Grupo(id, titulo, descripcion, listaUsuarioGrupo, 0, listaGastos);

					FirebaseDatabase.getInstance().getReference(DB_PATH_GRUPOS).child(id).setValue(grupo).addOnCompleteListener(new OnCompleteListener<Void>() {
						@Override
						public void onComplete(@NonNull Task<Void> task) {
							if (task.isSuccessful()) {
								rellenarListaGrupos(grupo);
							}
						}
					});
				}
			}
		} else if (v.getId() == R.id.btnAniadirEmailNuevoGrupo) {
			aniadirUsuario();
		} else if (v.getId() == R.id.btnCancelarNuevoGrupo) {
			finish();
		}
	}

	private void rellenarListaGrupos(Grupo grupo) {
		for (Usuario usuario : listaUsuario) {
			for (String id : listaId) {
				if (usuario.getId().equals(id)) {
					FirebaseDatabase.getInstance().getReference(DB_PATH_USERS).child(id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
						@Override
						public void onComplete(@NonNull Task<DataSnapshot> task) {
							if (task.isSuccessful()) {
								if (task.getResult().exists()) {
									DataSnapshot dataSnapshot = task.getResult();

									Usuario usuario = dataSnapshot.getValue(Usuario.class);

									listaIdGrupos = usuario.getGrupos();

									if (listaIdGrupos == null) {
										listaIdGrupos = new ArrayList<>();
										listaIdGrupos.add(grupo.getId());
									} else {
										listaIdGrupos.add(grupo.getId());
									}

									aniadirGrupoUsuario(id);
								}
							}
						}
					});
				}
			}
		}
	}

	private void aniadirGrupoUsuario(String id) {
		FirebaseDatabase.getInstance().getReference(DB_PATH_USERS).child(id).child("grupos").setValue(listaIdGrupos)
				.addOnCompleteListener(new OnCompleteListener<Void>() {
					@Override
					public void onComplete(@NonNull Task<Void> task) {
						Toast.makeText(NuevoGrupoActivity.this, getString(R.string.grupo_correcto), Toast.LENGTH_SHORT).show();
						Intent i = new Intent(NuevoGrupoActivity.this, MainActivity.class);
						startActivity(i);
						finish();
					}
				});
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (position == 0) {
			Toast.makeText(NuevoGrupoActivity.this, getString(R.string.eliminar_yo), Toast.LENGTH_SHORT).show();
		} else {
			mostrarDialog(position);
		}
	}

	private void mostrarDialog(int position) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.tit_dialog_grupo);
		builder.setCancelable(false);
		builder.setMessage(R.string.dialog_msg_grupo);
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

	private void aniadirUsuario() {
		FirebaseDatabase.getInstance().getReference(DB_PATH_USERS).addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot snapshot) {
				for (DataSnapshot data : snapshot.getChildren()) {
					Usuario usuario = data.getValue(Usuario.class);
					listaUsuario.add(usuario);
				}

				String email = etEmail.getText().toString();
				String nombre = "";
				String idUsuario = "";

				if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
					boolean usuarioEncontrado = false;

					for (Usuario usuario : listaUsuario) {
						if (usuario.getEmail().equals(email)) {
							usuarioEncontrado = true;
							nombre = usuario.getNombre();
							idUsuario = usuario.getId();
							break;
						}
					}

					if (usuarioEncontrado) {
						for (UsuarioGrupo usuarioGrupo : listaUsuarioGrupo) {
							if (usuarioGrupo.getId().equals(email)) {
								usuarioEncontrado = true;
								break;
							} else {
								usuarioEncontrado = false;
							}
						}

						if (!usuarioEncontrado) {
							listaUsuarioGrupo.add(new UsuarioGrupo(0, 0, 0, idUsuario));
							listaNombres.add(nombre);
							listaId.add(idUsuario);
							adapter.notifyDataSetChanged();
							etEmail.setText("");
						} else {
							Toast.makeText(NuevoGrupoActivity.this, getString(R.string.usuario_grupo), Toast.LENGTH_SHORT).show();
						}
					} else {
						Toast.makeText(NuevoGrupoActivity.this, getString(R.string.user_no_registrado), Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(NuevoGrupoActivity.this, getString(R.string.email_incorrecto), Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError error) {
				Toast.makeText(NuevoGrupoActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
			}
		});
	}
}