package com.dam.armoniabills.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dam.armoniabills.NuevoGastoActivity;
import com.dam.armoniabills.R;
import com.dam.armoniabills.model.Grupo;
import com.dam.armoniabills.model.UsuarioGrupo;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;


public class GrupoFragment extends Fragment implements View.OnClickListener {

    private static final String ARG_PARAM1 = "param1";
    TextView tvTitulo, tvDescripcion, tvDeuda, tvTotal, tvNumPersonas;
    RecyclerView rv;
    ExtendedFloatingActionButton efab;
    private Grupo grupo;

    public GrupoFragment() {
        // Required empty public constructor
    }

    public static GrupoFragment newInstance(Grupo param1) {
        GrupoFragment fragment = new GrupoFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            grupo = getArguments().getParcelable(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_grupo, container, false);

        tvTitulo = v.findViewById(R.id.tvTituloGrupoDetalle);
        tvDescripcion = v.findViewById(R.id.tvDescripcionGrupoDetalle);
        tvDeuda = v.findViewById(R.id.tvDeudaGrupoDetalle);
        tvTotal = v.findViewById(R.id.tvTotalGrupoDetalle);
        tvNumPersonas = v.findViewById(R.id.tvNumPersonasGrupoDetalle);
        rv = v.findViewById(R.id.rvGastosGrupoDetalle);
        efab = v.findViewById(R.id.efabNuevoGasto);

        cargarGrupo();

        efab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), NuevoGastoActivity.class);
                i.putExtra("grupo", grupo);
                startActivity(i);
            }
        });
        return v;
    }

    private void cargarGrupo() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        
        ArrayList<UsuarioGrupo> listaUsuariosGrupo = grupo.getUsuarios();
        UsuarioGrupo usuarioGrupoActual = null;

        for(UsuarioGrupo usuarioGrupo : listaUsuariosGrupo){
            if(usuarioGrupo.getId().equals(user.getEmail())){
                usuarioGrupoActual = usuarioGrupo;
            }
        }
        
        String pago = "0";
        if(usuarioGrupoActual.getDeben() == 0){
            pago = String.valueOf(usuarioGrupoActual.getDebes());
        } else {
            pago = String.valueOf(usuarioGrupoActual.getDeben());
        }

        tvTitulo.setText(grupo.getTitulo());
        tvDescripcion.setText(grupo.getDescripcion());
        tvTotal.setText(String.valueOf(grupo.getTotal()));
        tvNumPersonas.setText(String.valueOf(listaUsuariosGrupo.size()));
        tvDeuda.setText(pago);
    }

    @Override
    public void onClick(View v) {

    }
}