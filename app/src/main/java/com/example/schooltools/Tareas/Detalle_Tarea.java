package com.example.schooltools.Tareas;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.schooltools.R;
import com.example.schooltools.ToolBarActivity;

import java.util.HashMap;

public class Detalle_Tarea extends ToolBarActivity {

    FirebaseAuth firebaseAuth;
    FirebaseUser user;

    Button Boton_Importante;

    TextView  Titulo_Detalle, Descripcion_Detalle,
            Fecha_Registro_Detalle, Fecha_Tarea_Detalle, Estado_Detalle, Asignatura_Detalle;

    //DECLARAR LOS STRING PARA ALMACENAR LOS DATOS RECUPERADOS DE ACTIVIDAD ANTERIOR
    String id_tarea_R, fecha_registro_R, titulo_R, descripcion_R, fecha_R, estado_R, asignatura_R;

    boolean ComprobarTareaImportante = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setChildContentView(R.layout.activity_detalle_tarea);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("Detalle de tarea");


        InicializarVistas();
        RecuperarDatos();
        SetearDatosRecuperados();
        VerificarTareaImportante();

        Boton_Importante.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ComprobarTareaImportante) {
                    Eliminar_Tarea_Importante();
                } else {
                    Agregar_Tareas_Importantes();
                }
            }
        });
    }

    private void InicializarVistas() {
        Titulo_Detalle = findViewById(R.id.Titulo_Detalle);
        Descripcion_Detalle = findViewById(R.id.Descripcion_Detalle);
        Fecha_Registro_Detalle = findViewById(R.id.Fecha_Registro_Detalle);
        Fecha_Tarea_Detalle = findViewById(R.id.Fecha_Tarea_Detalle);
        Estado_Detalle = findViewById(R.id.Estado_Detalle);
        Asignatura_Detalle = findViewById(R.id.textoAsignatura);
        Boton_Importante = findViewById(R.id.Boton_Importante);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
    }

    private void RecuperarDatos() {
        Bundle intent = getIntent().getExtras();
        fecha_registro_R = intent.getString("fecha_registro");
        titulo_R = intent.getString("titulo");
        descripcion_R = intent.getString("descripcion");
        fecha_R = intent.getString("fecha_tarea");
        estado_R = intent.getString("estado");
        id_tarea_R = intent.getString("idTarea");
        asignatura_R = intent.getString("asignatura");
    }

    private void SetearDatosRecuperados() {
        Fecha_Registro_Detalle.setText(fecha_registro_R);
        Titulo_Detalle.setText(titulo_R);
        Descripcion_Detalle.setText(descripcion_R);
        Fecha_Tarea_Detalle.setText(fecha_R);
        Estado_Detalle.setText(estado_R);
        Asignatura_Detalle.setText(asignatura_R);
    }

    private void Agregar_Tareas_Importantes() {
        if (user == null) {
            //Si el usuario es nulo
            Toast.makeText(Detalle_Tarea.this, "Ha ocurrido un error", Toast.LENGTH_SHORT).show();
        } else {
            //Obtenemos los datos de la _tarea de la actividad anterior
            Bundle intent = getIntent().getExtras();

            id_tarea_R = intent.getString("id_tarea");
            fecha_registro_R = intent.getString("fecha_registro");
            titulo_R = intent.getString("titulo");
            descripcion_R = intent.getString("descripcion");
            fecha_R = intent.getString("fecha_tarea");
            estado_R = intent.getString("estado");
            asignatura_R = intent.getString("asignatura");


            HashMap<String, String> Tarea_Importante = new HashMap<>();
            Tarea_Importante.put("id_tarea", id_tarea_R);
            Tarea_Importante.put("fecha_hora_actual", fecha_registro_R);
            Tarea_Importante.put("titulo", titulo_R);
            Tarea_Importante.put("descripcion", descripcion_R);
            Tarea_Importante.put("fecha_tarea", fecha_R);
            Tarea_Importante.put("estado", estado_R);
            Tarea_Importante.put("asignatura", asignatura_R);


            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Usuarios");
            reference.child(firebaseAuth.getUid()).child("Mis Tareas importantes").child(id_tarea_R)
                    .setValue(Tarea_Importante)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(Detalle_Tarea.this, "Se ha añadido a Tareas importantes", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Detalle_Tarea.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void Eliminar_Tarea_Importante() {
        if (user == null) {
            Toast.makeText(Detalle_Tarea.this, "Ha ocurrido un error", Toast.LENGTH_SHORT).show();
        } else {
            Bundle intent = getIntent().getExtras();
            id_tarea_R = intent.getString("id_tarea");

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Usuarios");
            reference.child(firebaseAuth.getUid()).child("Mis Tareas importantes").child(id_tarea_R)
                    .removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(Detalle_Tarea.this, "La tarea ya no es importante", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Detalle_Tarea.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }
    }


    private void VerificarTareaImportante() {
        if (user == null) {
            Toast.makeText(Detalle_Tarea.this, "Ha ocurrido un error", Toast.LENGTH_SHORT).show();
        } else {
            Bundle intent = getIntent().getExtras();
            id_tarea_R = intent.getString("id_tarea");

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Usuarios");
            reference.child(firebaseAuth.getUid()).child("Mis Tareas importantes").child(id_tarea_R)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            ComprobarTareaImportante = snapshot.exists();
                            if (ComprobarTareaImportante) {
                                String importante = "Importante";
                                Boton_Importante.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.icono_tarea_importante, 0, 0);
                                Boton_Importante.setText(importante);
                            } else {
                                String no_importante = "No importante";
                                Boton_Importante.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.icono_tarea_no_importante, 0, 0);
                                Boton_Importante.setText(no_importante);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}