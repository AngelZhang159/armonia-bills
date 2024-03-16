package com.dam.armoniabills.fragments;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dam.armoniabills.MainActivity;
import com.dam.armoniabills.NuevoGastoActivity;
import com.dam.armoniabills.R;
import com.dam.armoniabills.model.Gasto;
import com.dam.armoniabills.model.Grupo;
import com.dam.armoniabills.model.Usuario;
import com.dam.armoniabills.model.UsuarioGrupo;
import com.dam.armoniabills.recyclerutils.AdapterGastos;
import com.dam.armoniabills.recyclerutils.AdapterUsuariosGrupo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class GrupoFragment extends Fragment implements View.OnClickListener {

    private static final String ARG_PARAM1 = "param1";

    TextView tvTitulo, tvDescripcion, tvDeuda, tvTotal;
    Button btnPagar;
    MaterialButton btnPers;
    ExtendedFloatingActionButton efab;
    RecyclerView rv;
    AdapterGastos adapter;

    ArrayList<Gasto> listaGastos;
    Usuario usuarioActual;
    UsuarioGrupo usuarioGrupoActual;
    ArrayList<Usuario> listaUsuario;
    ArrayList<UsuarioGrupo> listaUsuarioGrupo;

    FirebaseUser user;

    private Grupo grupo;

    int posicionUsuarioActual;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_grupo, container, false);

        tvTitulo = v.findViewById(R.id.tvTituloGrupoDetalle);
        tvDescripcion = v.findViewById(R.id.tvDescripcionGrupoDetalle);
        tvDeuda = v.findViewById(R.id.tvDeudaGrupoDetalle);
        tvTotal = v.findViewById(R.id.tvTotalGrupoDetalle);
        rv = v.findViewById(R.id.rvGastosGrupoDetalle);
        efab = v.findViewById(R.id.efabNuevoGasto);
        btnPers = v.findViewById(R.id.btnPersGrupo);
        btnPagar = v.findViewById(R.id.btnPagarDeudas);

        btnPagar.setOnClickListener(this);
        btnPers.setOnClickListener(this);
        listaUsuario = new ArrayList<>();

        cargarGrupo();
        obtenerUsuarioActual();

        listaGastos = new ArrayList<>();
        listaUsuarioGrupo = new ArrayList<>();

        rellenarListaGastos();

        user = FirebaseAuth.getInstance().getCurrentUser();

        efab.setOnClickListener(this);
        return v;
    }

    private void configurarRV() {
        adapter = new AdapterGastos(listaGastos);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);
        rv.setHasFixedSize(true);
    }

    @Override
    public void onDestroy() {
        rv.setAdapter(null);
        super.onDestroy();
    }

    private void rellenarListaGastos() {
        FirebaseDatabase.getInstance().getReference(MainActivity.DB_PATH_GRUPOS).child(grupo.getId()).child("gastos").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaGastos.clear(); // Clear the list before adding new data
                for (DataSnapshot data : snapshot.getChildren()) {
                    Gasto gasto = data.getValue(Gasto.class);
                    listaGastos.add(gasto);
                }
                configurarRV();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void cargarGrupo() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        FirebaseDatabase db = FirebaseDatabase.getInstance();

        db.getReference(MainActivity.DB_PATH_GRUPOS).child(grupo.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                grupo = snapshot.getValue(Grupo.class);

                if (grupo != null) {
                    ArrayList<UsuarioGrupo> listaUsuariosGrupo = grupo.getUsuarios();
                    UsuarioGrupo usuarioGrupoActual = new UsuarioGrupo();

                    for (UsuarioGrupo usuarioGrupo : listaUsuariosGrupo) {
                        if (usuarioGrupo.getId().equals(user.getUid())) {
                            usuarioGrupoActual = usuarioGrupo;
                        }
                    }

                    double pago;
                    String deudaStr = "";

                    if (isAdded()) {
                        Context context = getContext();
                        if (context != null) {

                            tvTitulo.setText(grupo.getTitulo());
                            tvDescripcion.setText(grupo.getDescripcion());
                            tvTotal.setText(String.format(getString(R.string.tv_grupo_total_pagar), grupo.getTotal()));
                            btnPers.setText(String.valueOf(listaUsuariosGrupo.size()));

                            if (usuarioGrupoActual.getDeben() > usuarioGrupoActual.getDebes()) {

                                pago = usuarioGrupoActual.getDeben() - usuarioGrupoActual.getDebes();
                                deudaStr = String.format(getString(R.string.te_deben_g), pago);
                                tvDeuda.setTextColor(ContextCompat.getColor(getContext(), R.color.verde));
                            } else {

                                pago = usuarioGrupoActual.getDebes() - usuarioGrupoActual.getDeben();
                                deudaStr = String.format(getString(R.string.debes_g), pago);
                                tvDeuda.setTextColor(ContextCompat.getColor(getContext(), R.color.rojo));
                            }
                            tvDeuda.setText(deudaStr);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.efabNuevoGasto) {
            Intent i = new Intent(getContext(), NuevoGastoActivity.class);
            i.putExtra("grupo", grupo);
            startActivity(i);
        } else if (v.getId() == R.id.btnPagarDeudas) {
            pagarDeudas();
        } else if (v.getId() == R.id.btnPersGrupo) {
            conseguirListaUsuarios();
        }
    }

    private void pagarDeudas() {

        //si debes entonces restas tu balance y ves a quien le deben con un for de todas las personas del grupo
        //lo que debes tu - lo que le deben a al persona
        // actualizas el balance de la persona a la que has pagado
        // si lo debesActualizado > 0 entonces  pasa a la siguiente persona que le deban dinero


        listaUsuarioGrupo = grupo.getUsuarios();


        final double[] dineroDebes = {usuarioGrupoActual.getDebes()};
        double dineroLeDeben;
        double debenUsuarioAPagar;

        //para obtener el id de la lista de usuariosGrupo
        int i = 0;

        Map<String, Object> map = new HashMap<>();

        DatabaseReference referenceGrupos = FirebaseDatabase.getInstance().getReference(MainActivity.DB_PATH_GRUPOS).child(grupo.getId()).child("usuarios");

        if (usuarioActual.getBalance() >= usuarioGrupoActual.getDebes()) {

            for (UsuarioGrupo usuarioAPagar : listaUsuarioGrupo) {

                if (!usuarioAPagar.getId().equals(user.getUid())) {

                    if (dineroDebes[0] > 0) {

                        if (usuarioAPagar.getDeben() > 0) {

                            // usuarioAPagar no es el usuario logueado y le deben dinero

                            dineroLeDeben = usuarioAPagar.getDeben();


                            if (dineroDebes[0] > dineroLeDeben) {

                                // actualizar el getDeben del usuarioAPagar y su balance

                                dineroDebes[0] = (Math.round((dineroDebes[0] -= dineroLeDeben) * 100) / 100.0);

                                map.clear();
                                map.put("deben", 0);
                                usuarioAPagar.setDeben(0);
                                referenceGrupos.child(String.valueOf(i)).updateChildren(map);

                                // Actualizar el balances

                                actualizarBalances(usuarioAPagar, dineroLeDeben, dineroLeDeben);


                            } else if(dineroLeDeben > dineroDebes[0]){

                                debenUsuarioAPagar = (Math.round((dineroLeDeben - dineroDebes[0]) * 100) / 100.0);

                                usuarioGrupoActual.setDebes(0);


                                //actualizar deben del usuario a pagar

                                map.clear();
                                map.put("deben", debenUsuarioAPagar);

                                referenceGrupos.child(String.valueOf(i)).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        // Actualizar debes de tu usuario

                                        map.clear();
                                        map.put("debes", 0);

                                        referenceGrupos.child(String.valueOf(posicionUsuarioActual)).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                actualizarBalances(usuarioAPagar, dineroDebes[0], dineroDebes[0]);
                                                dineroDebes[0] = 0;

                                            }
                                        });
                                    }
                                });


                            } else if(dineroLeDeben == dineroDebes[0]) {

                                // los dos a cero

                                map.clear();
                                map.put("deben", 0);
                                usuarioAPagar.setDeben(0);

                                double finalDineroLeDeben = dineroLeDeben;
                                referenceGrupos.child(String.valueOf(i)).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        map.clear();
                                        map.put("debes", 0);
                                        usuarioGrupoActual.setDebes(0);

                                        referenceGrupos.child(String.valueOf(posicionUsuarioActual)).updateChildren(map);

                                        actualizarBalances(usuarioAPagar, finalDineroLeDeben, dineroDebes[0]);

                                    }
                                });


                            }


                        }


                    } else {
                        break;
                    }


                }

                i++;

            }

        } else {
            Toast.makeText(getContext(), R.string.no_tienes_dinero, Toast.LENGTH_SHORT).show();
        }


    }

    private void actualizarBalances(UsuarioGrupo usuarioAPagar, double dineroLeDeben, double dineroDebes) {

        DatabaseReference referenceUsuarios = FirebaseDatabase.getInstance().getReference(MainActivity.DB_PATH_USUARIOS);

        Map<String, Object> map = new HashMap<>();


        //Actualizar balance del usuarioAPagar
        referenceUsuarios.child(usuarioAPagar.getId()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().exists()){
                        DataSnapshot dataSnapshot = task.getResult();
                        double balanceActualizado = dataSnapshot.getValue(Usuario.class).getBalance() + dineroLeDeben;

                        map.clear();
                        map.put("balance", balanceActualizado);
                        referenceUsuarios.child(usuarioAPagar.getId()).updateChildren(map);


                        //Actualizar tu balance
                        referenceUsuarios.child(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                if(task.isSuccessful()){
                                    if(task.getResult().exists()){
                                        DataSnapshot dataSnapshot = task.getResult();
                                        double balanceActualizado = dataSnapshot.getValue(Usuario.class).getBalance() - dineroDebes;

                                        map.clear();
                                        map.put("balance", balanceActualizado);
                                        referenceUsuarios.child(user.getUid()).updateChildren(map);

                                    }
                                }
                            }
                        });


                    }
                }
            }
        });

    }

    private void obtenerUsuarioActual() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(MainActivity.DB_PATH_USUARIOS).child(user.getUid());
        reference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {

                        DataSnapshot dataSnapshot = task.getResult();
                        usuarioActual = dataSnapshot.getValue(Usuario.class);

                        ArrayList<UsuarioGrupo> listaUsuariosGrupo = grupo.getUsuarios();
                        posicionUsuarioActual = 0;
                        for (UsuarioGrupo usuarioGrupo : listaUsuariosGrupo) {
                            if (usuarioGrupo.getId().equals(user.getUid())) {
                                usuarioGrupoActual = usuarioGrupo;
                                break;
                            }
                            posicionUsuarioActual++;
                        }

                    }
                }
            }
        });


    }


    private void conseguirListaUsuarios() {
        listaUsuario = new ArrayList<>();

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        listaUsuarioGrupo = grupo.getUsuarios();
        db.getReference(MainActivity.DB_PATH_USUARIOS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    Usuario usuario = data.getValue(Usuario.class);
                    for (int i = 0; i < listaUsuarioGrupo.size(); i++) {
                        String id = listaUsuarioGrupo.get(i).getId();
                        String usuarioId = usuario.getId();
                        if (id.equals(usuarioId)) {
                            listaUsuario.add(usuario);
                        }
                    }
                }
                mostrarDialogUsuarios();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void mostrarDialogUsuarios() {

        if (isAdded()) {
            Context context = getContext();
            if (context != null) {
                MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(getContext());
                View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_usuarios, null);

                RecyclerView usuariosRecyclerView = dialogView.findViewById(R.id.rvUsuariosGrupo);

                AdapterUsuariosGrupo adapter = new AdapterUsuariosGrupo(listaUsuario);

                usuariosRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                usuariosRecyclerView.setAdapter(adapter);

                materialAlertDialogBuilder
                        .setTitle(R.string.tit_dialog_users)
                        .setView(dialogView)
                        .setNegativeButton(R.string.btn_aceptar_d, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                materialAlertDialogBuilder.show();
            }
        }


    }
}