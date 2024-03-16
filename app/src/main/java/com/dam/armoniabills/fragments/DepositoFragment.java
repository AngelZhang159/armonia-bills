package com.dam.armoniabills.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

public class DepositoFragment extends Fragment implements View.OnClickListener {

	Button btnDepositar;
	EditText etCantidad;
	FirebaseUser currentUser;
	TextView tvCantidad;
	Double balanceCuenta;
	private FirebaseAuth mAuth;
	private FirebaseDatabase mDatabase;
	private String current;

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
		current = getString(R.string.vacio);

		etCantidad.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (!s.toString().equals(current)) {
					// Enforce two decimal places without Euro symbol
					int decimalIndex = s.toString().indexOf(getString(R.string.punto));
					if (decimalIndex > 0) {
						if (s.toString().length() - decimalIndex - 1 > 2) {
							String newText = s.toString().substring(0, decimalIndex + 3);
							current = newText;
							etCantidad.setText(newText);
							etCantidad.setSelection(newText.length());
						} else {
							current = s.toString();
						}
					} else {
						current = s.toString();
					}
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		rellenarDinero();

		return v;
	}



	private void rellenarDinero() {
		if (currentUser != null) {
			String uid = currentUser.getUid();
			DatabaseReference balanceRef = mDatabase.getReference(MainActivity.DB_PATH_USUARIOS).child(uid).child(getString(R.string.balancee));
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
			if (!current.isEmpty()) {
				String uid = currentUser.getUid();
				DatabaseReference balanceRef = mDatabase.getReference(MainActivity.DB_PATH_USUARIOS).child(uid).child(getString(R.string.balancee));
				balanceRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
					@Override
					public void onComplete(@NonNull Task<DataSnapshot> task) {
						double cantidad = task.getResult().getValue(Double.class) + Double.parseDouble(current);

						balanceRef.setValue(cantidad).addOnCompleteListener(new OnCompleteListener<Void>() {
							@Override
							public void onComplete(@NonNull Task<Void> task) {
								aniadirHistorial(Double.parseDouble(current));
							}
						});
					}
				});


			} else {
				Toast.makeText(getContext(), R.string.cantidad_obligatoria, Toast.LENGTH_SHORT).show();
			}
		}
	}


	private void aniadirHistorial(double cantidadFormateada) {
		FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

		String id = FirebaseDatabase.getInstance().getReference(getString(R.string.historialbalance)).child(user.getUid()).push().getKey();

		HistorialBalance historialBalance = new HistorialBalance(id, getString(R.string.ingresado), cantidadFormateada);

		FirebaseDatabase.getInstance().getReference(getString(R.string.historialbalance)).child(user.getUid()).child(id).setValue(historialBalance)
				.addOnCompleteListener(new OnCompleteListener<Void>() {
					@Override
					public void onComplete(@NonNull Task<Void> task) {
						etCantidad.setText("");
						Toast.makeText(getContext(), R.string.deposito_correcto, Toast.LENGTH_SHORT).show();
					}
				});
	}

}