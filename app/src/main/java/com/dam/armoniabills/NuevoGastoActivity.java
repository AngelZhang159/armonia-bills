package com.dam.armoniabills;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dam.armoniabills.fragments.HomeFragment;
import com.dam.armoniabills.model.Gasto;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class NuevoGastoActivity extends AppCompatActivity {

	EditText etPrecio, etTitulo, etDescripcion;
	Button btnAniadir;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nuevo_gasto);

		etPrecio = findViewById(R.id.etPrecioGasto);
		etTitulo = findViewById(R.id.etTituloGasto);
		etDescripcion = findViewById(R.id.etDescGasto);
		btnAniadir = findViewById(R.id.btnAniadirGasto);

		btnAniadir.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				String titulo = etTitulo.getText().toString();
				String descripcion = etDescripcion.getText().toString();
				String precioString = etPrecio.getText().toString();

				if(titulo.isEmpty() || precioString.isEmpty()){
					Toast.makeText(NuevoGastoActivity.this, "Debes rellenar los campos necesarios", Toast.LENGTH_SHORT).show();
				} else {
					FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
					double precio = Double.parseDouble(etPrecio.getText().toString());
					Gasto gasto = new Gasto(titulo, descripcion, user.getEmail(), precio);
					DatabaseReference reference = FirebaseDatabase.getInstance().getReference(HomeFragment.PATH_GRUPO);
				}

			}
		});


	}

}