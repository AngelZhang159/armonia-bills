package com.dam.armoniabills;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RegistroActivity extends AppCompatActivity implements View.OnClickListener {

	EditText etNomApe, etFech, etEmail, etContra;
	Button btnCrearCuenta;

	private FirebaseAuth mAuth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_registro);

		etNomApe = findViewById(R.id.etNomApeReg);
		etFech = findViewById(R.id.etFechReg);

		etEmail = findViewById(R.id.etEmailReg);
		etContra = findViewById(R.id.etContraReg);

		btnCrearCuenta = findViewById(R.id.btnCrearCuentaReg);

		mAuth = FirebaseAuth.getInstance();

		etFech.setOnClickListener(this);

		btnCrearCuenta.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {

		if (v.getId() == R.id.etFechReg) {

			MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker().setTitleText("Selecciona tu fecha de nacimiento").setSelection(MaterialDatePicker.todayInUtcMilliseconds()).build();
			datePicker.show(getSupportFragmentManager(), datePicker.toString());

			datePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
				@Override
				public void onPositiveButtonClick(Long selection) {
					Date date = new Date(selection);
					SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
					String dateString = dateFormat.format(date);
					etFech.setText(dateString);
				}
			});

		} else {

			String email = etEmail.getText().toString();
			String contra = etContra.getText().toString();

			if (email.isEmpty() || contra.isEmpty()) {
				Toast.makeText(RegistroActivity.this, "Debes introducir todos los campos", Toast.LENGTH_SHORT).show();
			} else {
				mAuth.createUserWithEmailAndPassword(email, contra).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
					@Override
					public void onComplete(@NonNull Task<AuthResult> task) {
						if (task.isSuccessful()) {

							Toast.makeText(RegistroActivity.this, "Usuario creado", Toast.LENGTH_SHORT).show();
							finish();

						} else {

							Toast.makeText(RegistroActivity.this, "Se ha producido un error, no se ha podido crear la cuenta", Toast.LENGTH_SHORT).show();

						}
					}
				});

			}
		}

	}
}