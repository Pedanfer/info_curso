package com.redsystem.agendaonline.Tareas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.redsystem.agendaonline.Objetos.Tarea;
import com.redsystem.agendaonline.R;
import com.redsystem.agendaonline.ToolBarActivity;
import com.redsystem.agendaonline.ViewHolder.ViewHolder_Tarea_Importante;
import org.jetbrains.annotations.NotNull;

public class Tareas_Importantes extends ToolBarActivity {

    RecyclerView RecyclerViewTareasImportantes;
    FirebaseDatabase firebaseDatabase;

    DatabaseReference Mis_Usuarios;

    FirebaseAuth firebaseAuth;
    FirebaseUser user;

    FirebaseRecyclerAdapter<Tarea , ViewHolder_Tarea_Importante> firebaseRecyclerAdapter;
    FirebaseRecyclerOptions<Tarea> firebaseRecyclerOptions;

    LinearLayoutManager linearLayoutManager;

    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setChildContentView(R.layout.activity_tareas_archivadas);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Tareas importantes");


        RecyclerViewTareasImportantes = findViewById(R.id.RecyclerViewTareasImportantes);
        RecyclerViewTareasImportantes.setHasFixedSize(true);
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        Mis_Usuarios = firebaseDatabase.getReference("Usuarios");

        dialog = new Dialog(Tareas_Importantes.this);

        ComprobarUsuario();

    }

    private void ComprobarUsuario(){
        if (user == null){
            Toast.makeText(Tareas_Importantes.this, "Ha ocurrido un error", Toast.LENGTH_SHORT).show();
        }else {
            ListarTareasImportantes();
        }
    }

    private void ListarTareasImportantes() {
        firebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<Tarea>().setQuery(Mis_Usuarios.child(user.getUid()).child("Mis Tareas importantes").orderByChild("fecha_tarea"), Tarea.class).build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Tarea, ViewHolder_Tarea_Importante>(firebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull ViewHolder_Tarea_Importante viewHolder_tarea_importante, int position, @NotNull Tarea _tarea) {
                viewHolder_tarea_importante.SetearDatos(
                        getApplicationContext(),
                        _tarea.getId_tarea(),
                        _tarea.getUid_usuario(),
                        _tarea.getCorreo_usuario(),
                        _tarea.getFecha_hora_actual(),
                        _tarea.getTitulo(),
                        _tarea.getDescripcion(),
                        _tarea.getFecha_tarea(),
                        _tarea.getEstado()
                );
            }


            @Override
            public ViewHolder_Tarea_Importante onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tarea_importante,parent,false);
                ViewHolder_Tarea_Importante viewHolder_tarea_importante = new ViewHolder_Tarea_Importante(view);
                viewHolder_tarea_importante.setOnClickListener(new ViewHolder_Tarea_Importante.ClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {


                    }

                    @Override
                    public void onItemLongClick(View view, int position) {

                        String id_tarea = getItem(position).getId_tarea();

                        //Declaramos las vistas
                        Button EliminarTarea , EliminarTareaCancelar;

                        //Realizamos la conexión con el diseño
                        dialog.setContentView(R.layout.cuadro_dialogo_eliminar_tarea_importante);

                        //Inicializar las vistas
                        EliminarTarea = dialog.findViewById(R.id.EliminarTarea);
                        EliminarTareaCancelar = dialog.findViewById(R.id.EliminarTareaCancelar);

                        EliminarTarea.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //Toast.makeText(Tareas_Importantes.this, "Tarea eliminada", Toast.LENGTH_SHORT).show();
                                Eliminar_Tarea_Importante(id_tarea);
                                dialog.dismiss();
                            }
                        });

                        EliminarTareaCancelar.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Toast.makeText(Tareas_Importantes.this, "Cancelado por el usuario", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        });

                        dialog.show();

                    }
                });
                return viewHolder_tarea_importante;
            }
        };

        linearLayoutManager = new LinearLayoutManager(Tareas_Importantes.this, LinearLayoutManager.VERTICAL, false);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        RecyclerViewTareasImportantes.setLayoutManager(linearLayoutManager);
        RecyclerViewTareasImportantes.setAdapter(firebaseRecyclerAdapter);

    }

    private void Eliminar_Tarea_Importante(String id_tarea){
        if (user == null){
            Toast.makeText(Tareas_Importantes.this, "Ha ocurrido un error", Toast.LENGTH_SHORT).show();
        }else {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Usuarios");
            reference.child(firebaseAuth.getUid()).child("Mis Tareas importantes").child(id_tarea)
                    .removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(Tareas_Importantes.this, "La tarea ya no es importante", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(Tareas_Importantes.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    @Override
    protected void onStart() {
        if (firebaseRecyclerAdapter != null){
            firebaseRecyclerAdapter.startListening();
        }
        super.onStart();
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

}