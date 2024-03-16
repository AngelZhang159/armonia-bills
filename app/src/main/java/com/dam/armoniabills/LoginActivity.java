package com.dam.armoniabills;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

	EditText etEmail, etContra;
	Button btnLogin, btnReg;

	FirebaseAuth mAuth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		etEmail = findViewById(R.id.etEmailLogin);
		etContra = findViewById(R.id.etContraLogin);

		btnLogin = findViewById(R.id.btnIniciarSesionLogin);
		btnReg = findViewById(R.id.btnRegistroLogin);

		btnLogin.setOnClickListener(this);
		btnReg.setOnClickListener(this);

		mAuth = FirebaseAuth.getInstance();

		OnBackPressedDispatcher dispatcher = getOnBackPressedDispatcher();

		dispatcher.addCallback(this, new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {

			}
		});
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btnIniciarSesionLogin) {
			String email = etEmail.getText().toString();
			String contra = etContra.getText().toString();

			if (email.isEmpty() || contra.isEmpty()) {
				Toast.makeText(this, getString(R.string.campos_obligatorios), Toast.LENGTH_SHORT).show();
			} else {
				mAuth.signInWithEmailAndPassword(email, contra).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
					@Override
					public void onComplete(@NonNull Task<AuthResult> task) {
						if (task.isSuccessful()) {
							Toast.makeText(LoginActivity.this, getString(R.string.inicio_sesion), Toast.LENGTH_SHORT).show();

							Intent i = new Intent(LoginActivity.this, MainActivity.class);
							startActivity(i);
							finish();
						} else {
							Toast.makeText(LoginActivity.this, getString(R.string.credenciales_incorrectas), Toast.LENGTH_SHORT).show();
						}
					}
				});
			}
		} else if (v.getId() == R.id.btnRegistroLogin) {
			Intent i = new Intent(this, RegistroActivity.class);
			startActivity(i);
		}

	}


}