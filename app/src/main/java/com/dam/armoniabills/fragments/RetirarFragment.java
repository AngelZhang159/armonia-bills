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

public class RetirarFragment extends Fragment implements View.OnClickListener {

	Button btnRetirar;
	TextView tvCantidad;
	EditText etCantidadRetirar;
	Double balanceCuenta;
	FirebaseUser currentUser;
	private FirebaseAuth mAuth;
	private FirebaseDatabase mDatabase;

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
		if (v.getId() == R.id.btnRetirarDinero) {
			if (!etCantidadRetirar.getText().toString().isEmpty()) {
				Double cantidad = Double.parseDouble(etCantidadRetirar.getText().toString());

				if (cantidad > balanceCuenta) {
					Toast.makeText(getContext(), R.string.error_retiro, Toast.LENGTH_SHORT).show();
				} else {
					retirar(cantidad);
				}
			} else {
				Toast.makeText(getContext(), R.string.campos_obligatorios, Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void retirar(Double cantidad) {

		Double nuevoBalance = balanceCuenta - cantidad;

		BigDecimal bd = new BigDecimal(nuevoBalance);
		bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);

		double roundedNum = bd.doubleValue();

		if (currentUser != null) {

			String uid = currentUser.getUid();
			DatabaseReference balanceRef = mDatabase.getReference(MainActivity.DB_PATH_USUARIOS).child(uid).child("balance");

			balanceRef.setValue(roundedNum).addOnCompleteListener(new OnCompleteListener<Void>() {
				@Override
				public void onComplete(@NonNull Task<Void> task) {
					aniadirHistorial(roundedNum);
				}
			});

		}

	}

	private void aniadirHistorial(double cantidadFormateada) {
		FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

		String id = FirebaseDatabase.getInstance().getReference("HistorialBalance").child(user.getUid()).push().getKey();

		HistorialBalance historialBalance = new HistorialBalance(id, "retirado", cantidadFormateada);

		FirebaseDatabase.getInstance().getReference("HistorialBalance").child(user.getUid()).child(id).setValue(historialBalance)
				.addOnCompleteListener(new OnCompleteListener<Void>() {
					@Override
					public void onComplete(@NonNull Task<Void> task) {
						etCantidadRetirar.setText("");
						Toast.makeText(getContext(), R.string.retiro_correcto, Toast.LENGTH_SHORT).show();
					}
				});
	}
}