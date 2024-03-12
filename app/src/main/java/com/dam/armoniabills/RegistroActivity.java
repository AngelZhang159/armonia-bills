package com.dam.armoniabills;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.dam.armoniabills.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegistroActivity extends AppCompatActivity implements View.OnClickListener {

	CircleImageView imvPerfil;
	EditText etNomApe, etEmail, etContra, etTlf;
	Button btnCrearCuenta;

	Uri imageUri;
	String imageUrl;

	ArrayList<Usuario> listaUsuarios;

	ActivityResultLauncher<PickVisualMediaRequest> pickMedia = registerForActivityResult(
			new ActivityResultContracts.PickVisualMedia(), uri -> {
				if (uri != null) {
					imageUri = uri;
					imvPerfil.setImageURI(uri);
				} else {
					Toast.makeText(RegistroActivity.this, getString(R.string.seleccionar_img), Toast.LENGTH_SHORT).show();
				}
			});

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_registro);

		etNomApe = findViewById(R.id.etNomApeReg);
		etEmail = findViewById(R.id.etEmailReg);
		etContra = findViewById(R.id.etContraReg);
		etTlf = findViewById(R.id.etTlfReg);
		imvPerfil = findViewById(R.id.imvPerfil);
		btnCrearCuenta = findViewById(R.id.btnCrearCuentaReg);

		listaUsuarios = new ArrayList<>();

		String fotoPerfil = "android.resource://"+  getPackageName() + "/" + R.drawable.perfil;
		imageUri = Uri.parse(fotoPerfil);

		imvPerfil.setOnClickListener(this);
		btnCrearCuenta.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btnCrearCuentaReg) {
			String nombre = etNomApe.getText().toString().trim();
			String email = etEmail.getText().toString().toLowerCase().trim();
			String contra = etContra.getText().toString().trim();
			String tlf = etTlf.getText().toString().trim();

			if (nombre.isEmpty() || email.isEmpty() || contra.isEmpty()) {
				Toast.makeText(RegistroActivity.this, "Debes introducir todos los campos obligatorios", Toast.LENGTH_SHORT).show();
			} else if (!tlf.isEmpty() && tlf.length() != 9 ) {
				Toast.makeText(RegistroActivity.this, "El número de teléfono no es válido", Toast.LENGTH_SHORT).show();
			} else {
				if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
					FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, contra).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
						@Override
						public void onComplete(@NonNull Task<AuthResult> task) {
							if (task.isSuccessful()) {
								guardarImagen();
							} else {
								Toast.makeText(RegistroActivity.this, "El correo electrónico introducido ya está registrado", Toast.LENGTH_SHORT).show();
							}
						}
					});
				} else {
					Toast.makeText(this, "El correo electrónico introducido es inválido", Toast.LENGTH_SHORT).show();
				}
			}
		} else if (v.getId() == R.id.imvPerfil) {
			pickMedia.launch(new PickVisualMediaRequest.Builder()
					.setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
					.build());
		}
	}

	private void guardarImagen() {
		StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("ProfileImages").child(imageUri.getLastPathSegment());
		storageReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
			@Override
			public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
				taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
					@Override
					public void onSuccess(Uri uri) {
						imageUrl = uri.toString();
						registrarUsuario();
					}
				});
			}
		});
	}

	private void registrarUsuario() {
		FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

		String id = user.getUid();
		String email = etEmail.getText().toString();
		String nombre = etNomApe.getText().toString();
		String tlf = etTlf.getText().toString();

		Usuario usuario = new Usuario(id, nombre, email, tlf, imageUrl, 0);

		FirebaseDatabase.getInstance().getReference("Usuarios").child(usuario.getId()).setValue(usuario).addOnCompleteListener(new OnCompleteListener<Void>() {
			@Override
			public void onComplete(@NonNull Task<Void> task) {
				if (task.isSuccessful()) {
					Toast.makeText(RegistroActivity.this, "Se ha registrado correctamente", Toast.LENGTH_SHORT).show();
					Intent i = new Intent(RegistroActivity.this, LoginActivity.class);
					startActivity(i);
				}
			}
		}).addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
				Toast.makeText(RegistroActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
			}
		});
	}
}