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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dam.armoniabills.MainActivity;
import com.dam.armoniabills.R;
import com.dam.armoniabills.TopBarActivity;
import com.dam.armoniabills.model.HistorialBalance;
import com.dam.armoniabills.recyclerutils.AdapterBalance;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class BalanceFragment extends Fragment implements View.OnClickListener {

	Button btnDepositar, btnRetirar;
	TextView tvDinero;
	private FirebaseAuth mAuth;
	private FirebaseDatabase mDatabase;
	RecyclerView rv;
	ArrayList<HistorialBalance> listaHistorialBalance;
	AdapterBalance adapterBalance;

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
		listaHistorialBalance = new ArrayList<>();
		rv = v.findViewById(R.id.rvBalance);

		rellenarDinero();
		rellenarHistorial();

		return v;
	}

	private void rellenarHistorial() {
		FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
		FirebaseDatabase.getInstance().getReference(getString(R.string.historialbalance)).child(user.getUid()).addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot snapshot) {
				listaHistorialBalance.clear();
				for (DataSnapshot data : snapshot.getChildren()) {
					HistorialBalance historial = data.getValue(HistorialBalance.class);
					listaHistorialBalance.add(historial);
				}
				Collections.reverse(listaHistorialBalance);
				configurarRV();
			}

			@Override
			public void onCancelled(@NonNull DatabaseError error) {

			}
		});
	}

	private void rellenarDinero() {
		FirebaseUser currentUser = mAuth.getCurrentUser();
		if (currentUser != null) {
			String uid = currentUser.getUid();
			DatabaseReference balanceRef = mDatabase.getReference(MainActivity.DB_PATH_USUARIOS).child(uid).child(getString(R.string.balancee));
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
					Toast.makeText(getContext(), databaseError.toString(), Toast.LENGTH_SHORT).show();
				}
			});
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btnDepositarBalance) {

			Intent i = new Intent(getContext(), TopBarActivity.class);
			i.putExtra(getString(R.string.rellenar), getString(R.string.fragDep));
			startActivity(i);

		} else if (v.getId() == R.id.btnRetirarBalance) {
			Intent i = new Intent(getContext(), TopBarActivity.class);
			i.putExtra(getString(R.string.rellenar), getString(R.string.fragRet));
			startActivity(i);
		}
	}

	public void configurarRV() {
		adapterBalance = new AdapterBalance(listaHistorialBalance);

		rv.setLayoutManager(new LinearLayoutManager(getContext()));
		rv.setAdapter(adapterBalance);
		rv.setHasFixedSize(true);
	}

}