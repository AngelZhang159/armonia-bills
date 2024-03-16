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

import com.dam.armoniabills.fragments.BalanceFragment;
import com.dam.armoniabills.fragments.HistorialFragment;
import com.dam.armoniabills.fragments.HomeFragment;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {

	public static final String DB_PATH_USUARIOS = "Usuarios";
	public static final String DB_PATH_GRUPOS = "Grupos";

	private int itemMenuSeleccionado = R.id.botNavVarHome;
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

		itemMenuSeleccionado = menuItem.getItemId();

		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();

		if (menuItem.getItemId() == R.id.botNavVarHome) {
			HomeFragment homeFragment = new HomeFragment();
			transaction.replace(R.id.flPrincipal, homeFragment);
		} else if (menuItem.getItemId() == R.id.botNavVarMisDeudas) {
			BalanceFragment deudasFragment = new BalanceFragment();
			transaction.replace(R.id.flPrincipal, deudasFragment);
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

		if(itemMenuSeleccionado == R.id.botNavVarPerfil){
			navBarView.setSelectedItemId(R.id.botNavVarHome);
		} else {
			navBarView.setSelectedItemId(itemMenuSeleccionado);

		}
	}

}