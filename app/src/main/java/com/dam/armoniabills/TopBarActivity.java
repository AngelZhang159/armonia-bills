package com.dam.armoniabills;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.dam.armoniabills.fragments.ConfiguracionFragment;
import com.dam.armoniabills.fragments.DepositoFragment;
import com.dam.armoniabills.fragments.GrupoFragment;
import com.dam.armoniabills.fragments.HomeFragment;
import com.dam.armoniabills.fragments.RetirarFragment;
import com.dam.armoniabills.model.Grupo;
import com.google.android.material.appbar.MaterialToolbar;

public class TopBarActivity extends AppCompatActivity {

	MaterialToolbar toolbar;
	Grupo grupo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_top_bar);

		toolbar = findViewById(R.id.topAppBar);

		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				finish();
			}
		});

		toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				if (item.getItemId() == R.id.item_config) {
					cargarConfigGrupo();
				}

				return false;
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
		grupo = getIntent().getParcelableExtra(HomeFragment.GRUPO_SELECCIONADO);

		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();

		GrupoFragment grupoFragment = GrupoFragment.newInstance(grupo);
		transaction.replace(R.id.flTopBar, grupoFragment);

		transaction.commit();

		toolbar.inflateMenu(R.menu.topbar_menu);
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

	private void cargarConfigGrupo() {
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		ConfiguracionFragment configuracionFragment = ConfiguracionFragment.newInstance(grupo);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.replace(R.id.flTopBar, configuracionFragment);

		fragmentTransaction.commit();
	}
}