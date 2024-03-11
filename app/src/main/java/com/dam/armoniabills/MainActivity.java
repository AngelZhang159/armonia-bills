package com.dam.armoniabills;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

	private DrawerLayout drawerLayout;
	private ImageView ivHamburger, ivPerfil;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		drawerLayout = findViewById(R.id.drawer_layout);
		ivHamburger = findViewById(R.id.ivHamburger);
		ivPerfil = findViewById(R.id.ivPerfil);

		ivHamburger.setOnClickListener(this);
		ivPerfil.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
//			Si está abierto el menú hamburger se cierra
			drawerLayout.closeDrawer(GravityCompat.START);
		} else if (v.getId() == R.id.ivHamburger) {
//			Abrir menu hamburger
			drawerLayout.openDrawer(GravityCompat.START);
		} else if (v.getId() == R.id.ivPerfil) {
			Intent i = new Intent(this, MiPerfilActivity.class);
			startActivity(i);
		}
	}
}