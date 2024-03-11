package com.dam.armoniabills;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.dam.armoniabills.fragments.DeudasFragment;
import com.dam.armoniabills.fragments.HistorialFragment;
import com.dam.armoniabills.fragments.HomeFragment;
import com.dam.armoniabills.fragments.NuevoFragment;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {

	NavigationBarView navBarView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		navBarView = findViewById(R.id.botNavVar);
		navBarView.setOnItemSelectedListener(this);

		OnBackPressedDispatcher dispatcher = getOnBackPressedDispatcher();
		dispatcher.addCallback(this, new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
			}
		});
	}

	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();

		if (menuItem.getItemId() == R.id.botNavVarHome) {
			HomeFragment homeFragment = new HomeFragment();
			transaction.replace(R.id.flPrincipal, homeFragment);
		} else if (menuItem.getItemId() == R.id.botNavVarMisDeudas) {
			DeudasFragment deudasFragment = new DeudasFragment();
			transaction.replace(R.id.flPrincipal, deudasFragment);
		} else if (menuItem.getItemId() == R.id.botNavVarNuevoGasto) {
			NuevoFragment nuevoFragment = new NuevoFragment();
			transaction.replace(R.id.flPrincipal, nuevoFragment);
		} else if (menuItem.getItemId() == R.id.botNavVarHistorial) {
			HistorialFragment historialFragment = new HistorialFragment();
			transaction.replace(R.id.flPrincipal, historialFragment);
		} else if (menuItem.getItemId() == R.id.botNavVarPerfil) {
			Intent i = new Intent(this, MiPerfilActivity.class);
			startActivity(i);
		}
		transaction.commit();
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		navBarView.setSelectedItemId(R.id.botNavVarHome);
	}

}