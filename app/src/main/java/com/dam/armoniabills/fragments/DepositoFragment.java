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

import com.dam.armoniabills.MainActivity;
import com.dam.armoniabills.R;
import com.dam.armoniabills.model.HistorialBalance;
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

public class DepositoFragment extends Fragment implements View.OnClickListener {

	Button btnDepositar;
	EditText etCantidad;
	FirebaseUser currentUser;
	TextView tvCantidad;
	Double balanceCuenta;
	private FirebaseAuth mAuth;
	private FirebaseDatabase mDatabase;

	public DepositoFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_deposito, container, false);

		btnDepositar = v.findViewById(R.id.btnDepositarDinero);
		etCantidad = v.findViewById(R.id.etCantidadDineroDepositar);
		tvCantidad = v.findViewById(R.id.tvDineroDisponibleDep);

		btnDepositar.setOnClickListener(this);

		mAuth = FirebaseAuth.getInstance();
		mDatabase = FirebaseDatabase.getInstance();
		currentUser = mAuth.getCurrentUser();

		rellenarDinero();

		return v;
	}



	private void rellenarDinero() {
		if (currentUser != null) {
			String uid = currentUser.getUid();
			DatabaseReference balanceRef = mDatabase.getReference(MainActivity.DB_PATH_USUARIOS).child(uid).child("balance");
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
					Toast.makeText(getContext(), databaseError.toString(), Toast.LENGTH_SHORT).show();
				}
			});
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btnDepositarDinero) {
			if (!etCantidad.getText().toString().isEmpty()) {
				Double cantidad = Double.parseDouble(etCantidad.getText().toString());

				BigDecimal bd = new BigDecimal(cantidad);
				bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);

				double cantidadFormateada = bd.doubleValue();
				String uid = currentUser.getUid();
				DatabaseReference balanceRef = mDatabase.getReference(MainActivity.DB_PATH_USUARIOS).child(uid).child("balance");

				balanceRef.setValue(cantidadFormateada).addOnCompleteListener(new OnCompleteListener<Void>() {
					@Override
					public void onComplete(@NonNull Task<Void> task) {
						aniadirHistorial(cantidadFormateada);
					}
				});

			} else {
				Toast.makeText(getContext(), R.string.cantidad_obligatoria, Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void aniadirHistorial(double cantidadFormateada) {
		FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

		String id = FirebaseDatabase.getInstance().getReference("HistorialBalance").child(user.getUid()).push().getKey();

		HistorialBalance historialBalance = new HistorialBalance(id, "ingresado", cantidadFormateada);

		FirebaseDatabase.getInstance().getReference("HistorialBalance").child(user.getUid()).child(id).setValue(historialBalance)
				.addOnCompleteListener(new OnCompleteListener<Void>() {
					@Override
					public void onComplete(@NonNull Task<Void> task) {
						etCantidad.setText("");
						Toast.makeText(getContext(), R.string.deposito_correcto, Toast.LENGTH_SHORT).show();
					}
				});
	}

}