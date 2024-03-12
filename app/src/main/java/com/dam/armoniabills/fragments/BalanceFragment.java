package com.dam.armoniabills.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.dam.armoniabills.R;
import com.dam.armoniabills.TopBarActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class BalanceFragment extends Fragment implements View.OnClickListener {

	Button btnDepositar, btnRetirar;
	TextView tvDinero;
	private FirebaseAuth mAuth;
	private FirebaseDatabase mDatabase;

	public BalanceFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_balance, container, false);

		tvDinero = v.findViewById(R.id.tvDinero);
		btnDepositar = v.findViewById(R.id.btnDepositarBalance);
		btnRetirar = v.findViewById(R.id.btnRetirarBalance);

		btnDepositar.setOnClickListener(this);
		btnRetirar.setOnClickListener(this);

		mAuth = FirebaseAuth.getInstance();
		mDatabase = FirebaseDatabase.getInstance();

		rellenarDinero();

		return v;
	}

	private void rellenarDinero() {
		FirebaseUser currentUser = mAuth.getCurrentUser();
		if (currentUser != null) {
			String uid = currentUser.getUid();
			DatabaseReference balanceRef = mDatabase.getReference("Usuarios").child(uid).child("balance");
			balanceRef.addValueEventListener(new ValueEventListener() {
				@Override
				public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
					Double balance = dataSnapshot.getValue(Double.class);
					if (balance != null && isAdded()) {
						tvDinero.setText(String.format(getString(R.string.balance), balance));
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
		if (v.getId() == R.id.btnDepositarBalance) {

			Intent i = new Intent(getContext(), TopBarActivity.class);
			i.putExtra("rellenar", "fragmentoBalanceDepositar");
			startActivity(i);

		} else if (v.getId() == R.id.btnRetirarBalance) {
			Intent i = new Intent(getContext(), TopBarActivity.class);
			i.putExtra("rellenar", "fragmentoBalanceRetirar");
			startActivity(i);
		}
	}
}