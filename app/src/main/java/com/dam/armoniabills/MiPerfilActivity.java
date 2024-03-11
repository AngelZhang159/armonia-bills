package com.dam.armoniabills;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.dam.armoniabills.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class MiPerfilActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView ivPerfil;
    EditText etNombre, etEmail, etTlf, etPassword, etRepPassword;
    Button btnUpdate, btnCambiar;

    FirebaseUser user;
    String id;

    Uri imageUri;
    String imageUrl;

    ActivityResultLauncher<PickVisualMediaRequest> pickMedia = registerForActivityResult(
            new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    imageUri = uri;
                    ivPerfil.setImageURI(uri);
                } else {
                    Toast.makeText(MiPerfilActivity.this, getString(R.string.seleccionar_img), Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mi_perfil);

        etNombre = findViewById(R.id.etNombreP);
        etEmail = findViewById(R.id.etEmailP);
        etTlf = findViewById(R.id.etTelefonoP);
        etPassword = findViewById(R.id.etPasswordP);
        etRepPassword = findViewById(R.id.etRepPasswordP);
        ivPerfil = findViewById(R.id.imvPerfilP);

        btnCambiar = findViewById(R.id.btnCambiar);
        btnUpdate = findViewById(R.id.btnUpdate);

        user = FirebaseAuth.getInstance().getCurrentUser();
        id = user.getUid();

        readUsuario();

        btnUpdate.setOnClickListener(this);
        btnCambiar.setOnClickListener(this);
        ivPerfil.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnCambiar) {
            updatePassword();
        } else if (v.getId() == R.id.btnUpdate) {
            updateData();
        } else if (v.getId() == R.id.imvPerfilP) {
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        }
    }

    private void updateData() {
        String nombre = etNombre.getText().toString();
        String tlf = etTlf.getText().toString();

        if (nombre.isEmpty()) {
            Toast.makeText(MiPerfilActivity.this, "Debes introducir todos los campos obligatorios", Toast.LENGTH_SHORT).show();
        } else if (!tlf.isEmpty() && tlf.length() != 9 ) {
            Toast.makeText(MiPerfilActivity.this, "El número de teléfono no es válido", Toast.LENGTH_SHORT).show();
        } else {
            updateImagen();
        }
    }

    private void updateImagen() {
        FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl).delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                FirebaseStorage.getInstance().getReference().child("ProfileImages").child(imageUri.getLastPathSegment())
                        .putFile(imageUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                imageUrl = uri.toString();
                                updateUsuario();
                            }
                        });
                    }
                });
            }
        });
    }

    private void updateUsuario() {
        String email = etEmail.getText().toString();
        String nombre = etNombre.getText().toString();
        String tlf = etTlf.getText().toString();

        Usuario usuario = new Usuario(id, nombre, email, tlf, imageUrl);

        FirebaseDatabase.getInstance().getReference("Usuarios").child(usuario.getId()).setValue(usuario).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(MiPerfilActivity.this, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MiPerfilActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePassword() {
        String password = etPassword.getText().toString();
        String repPassword = etRepPassword.getText().toString();
        
        if (password.isEmpty()) {
            Toast.makeText(this, "Debe introducir una contraseña", Toast.LENGTH_SHORT).show();
        } else if (repPassword.isEmpty()) {
            Toast.makeText(this, "Debe repetir la contraseña", Toast.LENGTH_SHORT).show();
        } else {
            if (password.equals(repPassword)) {
                user.updatePassword(password).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MiPerfilActivity.this, "Contraseña actualizada correctamente", Toast.LENGTH_SHORT).show(); 
                        } else {
                            Toast.makeText(MiPerfilActivity.this, "No se ha podido cambiar la contraseña", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void readUsuario() {
        FirebaseDatabase.getInstance().getReference("Usuarios").child(id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        DataSnapshot dataSnapshot = task.getResult();

                        String nombre = String.valueOf(dataSnapshot.child("nombre").getValue());
                        String email = String.valueOf(dataSnapshot.child("email").getValue());
                        String tlf = String.valueOf(dataSnapshot.child("tlf").getValue());
                        imageUrl = (String.valueOf(dataSnapshot.child("imagenPerfil").getValue()));

                        etNombre.setText(nombre);
                        etEmail.setText(email);
                        etTlf.setText(tlf);
                        Glide.with(MiPerfilActivity.this).load(imageUrl).into(ivPerfil);
                    }
                }
            }
        });
    }


}