package com.dam.armoniabills.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dam.armoniabills.R;
import com.dam.armoniabills.model.Grupo;
import com.dam.armoniabills.model.UsuarioGrupo;

import java.util.ArrayList;
import java.util.List;


public class GrupoFragment extends Fragment {

    TextView tvTitulo, tvDescripcion, tvDeuda, tvNumPersonas;
    RecyclerView rv;

    private static final String ARG_PARAM1 = "param1";

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
        tvNumPersonas = v.findViewById(R.id.tvNumPersonasGrupoDetalle);
        rv = v.findViewById(R.id.rvGastosGrupoDetalle);

        cargarGrupo();

        return v;
    }

    private void cargarGrupo() {

        //TODO
        /*ArrayList<UsuarioGrupo> listaUsuariosGrupo = grupo.getUsuarios();
        String pago = "0";
        if(usuarioGrupo.getDeben() == 0){
            pago = String.valueOf(usuarioGrupo.getDebes());
        } else {
            pago = String.valueOf(usuarioGrupo.getDeben());
        }

        tvTitulo.setText(grupo.getTitulo());
        tvDescripcion.setText(grupo.getDescripcion());
        tvDeuda.setText(grupo.get);*/

    }
}