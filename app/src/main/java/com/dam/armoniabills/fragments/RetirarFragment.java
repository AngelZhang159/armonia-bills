package com.dam.armoniabills.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.dam.armoniabills.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RetirarFragment extends Fragment implements View.OnClickListener {

	Button btnRetirar;
	TextView tvCantidad;
	private FirebaseAuth mAuth;
	private FirebaseDatabase mDatabase;
	EditText etCantidadRetirar;
	Double balanceCuenta;
	FirebaseUser currentUser;

	public RetirarFragment() {
	}

	public static RetirarFragment newInstance() {
		RetirarFragment fragment = new RetirarFragment();
		Bundle args = new Bundle();
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_retirar, container, false);

		btnRetirar = v.findViewById(R.id.btnRetirarDinero);
		tvCantidad = v.findViewById(R.id.tvDineroDisponibleRet);
		etCantidadRetirar = v.findViewById(R.id.etCantidadDineroRetirar);

		mAuth = FirebaseAuth.getInstance();
		mDatabase = FirebaseDatabase.getInstance();
		currentUser = mAuth.getCurrentUser();

		rellenarDinero();

		btnRetirar.setOnClickListener(this);

		return v;
	}

	private void rellenarDinero() {
		if (currentUser != null) {
			String uid = currentUser.getUid();
			DatabaseReference balanceRef = mDatabase.getReference("Usuarios").child(uid).child("balance");
			balanceRef.addValueEventListener(new ValueEventListener() {
				@Override
				public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
					balanceCuenta = dataSnapshot.getValue(Double.class);
					if (balanceCuenta != null && isAdded()) {
						tvCantidad.setText(String.format(getString(R.string.cantidad_disponible), balanceCuenta));
					}
				}

				@Override
				public void onCancelled(@NonNull DatabaseError databaseError) {
					Toast.makeText(getContext(), "Error balance dinero", Toast.LENGTH_SHORT).show();
				}
			});
		} else {
			Toast.makeText(getContext(), "Usuario null", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btnRetirarDinero) {
			if (!etCantidadRetirar.getText().toString().isEmpty()) {
				Double cantidad = Double.parseDouble(etCantidadRetirar.getText().toString());

				if (cantidad > balanceCuenta) {
					Toast.makeText(getContext(), "ERROR, Estás intentado retirar más dinero del que tienes en la cuenta", Toast.LENGTH_SHORT).show();
				} else {
					retirar(cantidad);
				}
			} else {
				Toast.makeText(getContext(), "Debes de introducir una cantidad", Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void retirar(Double cantidad) {

		Double nuevoBalance = balanceCuenta - cantidad;

		if (currentUser != null) {

			String uid = currentUser.getUid();
			DatabaseReference balanceRef = mDatabase.getReference("Usuarios").child(uid).child("balance");

			balanceRef.setValue(nuevoBalance).addOnCompleteListener(new OnCompleteListener<Void>() {
				@Override
				public void onComplete(@NonNull Task<Void> task) {
					etCantidadRetirar.setText("");
					Toast.makeText(getContext(), "Dinero retirado con éxito", Toast.LENGTH_SHORT).show();
				}
			});

		}

	}
}