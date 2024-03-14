package com.dam.armoniabills.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dam.armoniabills.R;
import com.dam.armoniabills.TopBarActivity;
import com.dam.armoniabills.model.Grupo;
import com.dam.armoniabills.model.Usuario;
import com.dam.armoniabills.model.UsuarioGrupo;
import com.dam.armoniabills.recyclerutils.AdapterUsuariosGrupo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ConfiguracionFragment extends Fragment implements View.OnClickListener {

    private static final String ARG_GRUPO = "grupo";
    private Grupo grupo;

    EditText etTitulo, etDescripcion, etPersona;
    Button btnEditar, btnEliminar, btnAniadir;

    ArrayList<Usuario> listaUsuarios;
    ArrayList<UsuarioGrupo> listaUsuarioGrupo;
    Usuario usuarioAniadir;

    public ConfiguracionFragment() {
        // Required empty public constructor
    }

    public static ConfiguracionFragment newInstance(Grupo grupo) {
        ConfiguracionFragment fragment = new ConfiguracionFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_GRUPO, grupo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            grupo = getArguments().getParcelable(ARG_GRUPO);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_configuracion, container, false);

        etTitulo = v.findViewById(R.id.etTituloConfig);
        etDescripcion = v.findViewById(R.id.etDescripcionConfig);
        etPersona = v.findViewById(R.id.etAniadirPersonaConfig);
        btnEditar = v.findViewById(R.id.btnUpdateConfig);
        btnEliminar = v.findViewById(R.id.btnEliminar);
        btnAniadir = v.findViewById(R.id.btnAniadirConfig);

        etTitulo.setText(grupo.getTitulo());
        etDescripcion.setText(grupo.getDescripcion());

        listaUsuarios = new ArrayList();
        listaUsuarioGrupo = new ArrayList<>();

        btnEliminar.setOnClickListener(this);
        btnEditar.setOnClickListener(this);
        btnAniadir.setOnClickListener(this);

        return v;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnEliminar) {
            mostrarDialogEliminar();
        } else if (v.getId() == R.id.btnUpdateConfig) {
            updateGrupo();
        } else if (v.getId() == R.id.btnAniadirConfig) {
            readUsuario();
        }
    }

    private void readUsuario() {
        listaUsuarioGrupo = grupo.getUsuarios();

        FirebaseDatabase.getInstance().getReference("Usuarios").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    Usuario usuario = data.getValue(Usuario.class);
                    listaUsuarios.add(usuario);
                }

                String email = etPersona.getText().toString();

                if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    boolean usuarioEncontrado = false;

                    for (Usuario usuario : listaUsuarios) {
                        if (usuario.getEmail().equals(email)) {
                            usuarioEncontrado = true;
                            usuarioAniadir = usuario;
                            break;
                        }
                    }

                    if (usuarioEncontrado) {
                        for (UsuarioGrupo usuarioGrupo : listaUsuarioGrupo) {
                            if (usuarioGrupo.getId().equals(usuarioAniadir.getId())) {
                                usuarioEncontrado = true;
                                break;
                            } else {
                                usuarioEncontrado = false;
                            }
                        }

                        if (!usuarioEncontrado) {
                            listaUsuarioGrupo.add(new UsuarioGrupo(0, 0, 0, usuarioAniadir.getId()));
                            aniadirUsuario();
                        } else {
                            Toast.makeText(getContext(), getString(R.string.usuario_grupo), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), getString(R.string.user_no_registrado), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), getString(R.string.email_incorrecto), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void aniadirUsuario() {
        ArrayList<String> listaGrupos = new ArrayList<>();
        listaGrupos = usuarioAniadir.getGrupos();

        if (listaGrupos == null) {
            listaGrupos = new ArrayList<>();
            listaGrupos.add(grupo.getId());
        } else {
            listaGrupos.add(grupo.getId());
        }

        FirebaseDatabase.getInstance().getReference("Usuarios").child(usuarioAniadir.getId()).child("grupos").setValue(listaGrupos)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateGrupoUsuario();
                    }
                });
    }

    private void updateGrupoUsuario() {
        Map<String, Object> mapa = new HashMap<>();
        mapa.put("usuarios", listaUsuarioGrupo);

        FirebaseDatabase.getInstance().getReference("Grupos").child(grupo.getId()).updateChildren(mapa)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        etPersona.setText("");
                        Toast.makeText(getContext(), "Usuario añadido correctamente", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void mostrarDialogEliminar() {
        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(getContext());

        materialAlertDialogBuilder
                .setCancelable(false)
                .setTitle("Eliminar grupo")
                .setMessage("¿Desea eliminar el grupo definitivamente?")
                .setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        borrarGrupo();
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        materialAlertDialogBuilder.show();
    }

    private void updateGrupo() {
        String titulo = etTitulo.getText().toString();
        String descripcion = etDescripcion.getText().toString();

        if (titulo.isEmpty()) {
            Toast.makeText(getContext(), "Debe introducir un titulo", Toast.LENGTH_SHORT).show();
        } else {
            Map<String, Object> mapa = new HashMap<>();
            mapa.put("titulo", titulo);
            mapa.put("descripcion", descripcion);

            FirebaseDatabase.getInstance().getReference("Grupos").child(grupo.getId()).updateChildren(mapa)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(getContext(), "Grupo editado correctamente", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
    
    private void borrarGrupo() {
        FirebaseDatabase.getInstance().getReference("Usuarios").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Usuario> listaUsuario = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Usuario usuario = data.getValue(Usuario.class);
                    listaUsuario.add(usuario);
                }

                ArrayList<String> listaGruposUsuario = new ArrayList<>();
                for (Usuario usuario : listaUsuario) {
                    if (usuario.getGrupos() != null) {
                        listaGruposUsuario = usuario.getGrupos();

                        for (String idGrupo : listaGruposUsuario) {
                            if (grupo.getId().equals(idGrupo)) {
                                listaGruposUsuario.remove(idGrupo);
                                usuario.setGrupos(listaGruposUsuario);

                                FirebaseDatabase.getInstance().getReference("Usuarios").child(usuario.getId()).setValue(usuario)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                            }
                                        });
                            }
                        }
                    }
                }

                FirebaseDatabase.getInstance().getReference("Grupos").child(grupo.getId()).removeValue()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                getActivity().finish();
                                Toast.makeText(getContext(), "Grupo eliminado correctamente", Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}