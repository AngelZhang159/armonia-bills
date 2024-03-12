package com.dam.armoniabills;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.dam.armoniabills.fragments.DepositoFragment;
import com.dam.armoniabills.fragments.RetirarFragment;

import com.dam.armoniabills.fragments.GrupoFragment;
import com.dam.armoniabills.fragments.HomeFragment;
import com.dam.armoniabills.model.Grupo;
import com.google.android.material.appbar.MaterialToolbar;

public class TopBarActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_top_bar);

		MaterialToolbar toolbar = findViewById(R.id.topAppBar);

		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}
		});

		String i = getIntent().getStringExtra("rellenar");

		if (i.equals("fragmentoHome")) {

			cargarGrupo();

		} else if (i.equals("fragmentoBalanceDepositar")) {

			cargarDepositar();

		} else if (i.equals("fragmentoBalanceRetirar")) {

			cargarRetirar();

		}
	}

	private void cargarGrupo() {

		Grupo grupo = getIntent().getParcelableExtra(HomeFragment.GRUPO_SELECCIONADO);
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();

		GrupoFragment grupoFragment = GrupoFragment.newInstance(grupo);
		transaction.replace(R.id.flTopBar, grupoFragment);

		transaction.commit();


	}

	private void cargarRetirar() {
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		RetirarFragment retirarFragment = new RetirarFragment();
		fragmentTransaction.replace(R.id.flTopBar, retirarFragment);

		fragmentTransaction.commit();
	}

	private void cargarDepositar() {
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		DepositoFragment depositoFragment = new DepositoFragment();
		fragmentTransaction.replace(R.id.flTopBar, depositoFragment);

		fragmentTransaction.commit();
	}
}