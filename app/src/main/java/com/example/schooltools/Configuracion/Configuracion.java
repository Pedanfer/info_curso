package com.example.schooltools.Configuracion;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.example.schooltools.MainActivity;
import com.example.schooltools.R;
import com.example.schooltools.ToolBarActivity;

public class Configuracion extends ToolBarActivity {

    TextView EliminarCuenta;

    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    Dialog dialog_autenticacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setChildContentView(R.layout.activity_configuracion);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("Configuracion");


        InicializarVariables();

        EliminarCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EliminarUsuarioAutenticacion();
            }
        });
    }

    private void InicializarVariables() {
        EliminarCuenta = findViewById(R.id.EliminarCuenta);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Usuarios");

        dialog_autenticacion = new Dialog(Configuracion.this);
    }

    private void EliminarUsuarioAutenticacion() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Configuracion.this);
        alertDialog.setTitle("¿Estás seguro(a)?");
        alertDialog.setMessage("Su cuenta se eliminará permanentemente");
        alertDialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            EliminarUsuarioBD();
                            Intent intent = new Intent(Configuracion.this, MainActivity.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                            Toast.makeText(Configuracion.this, "Se ha eliminado su cuenta con éxito", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Configuracion.this, "Ha ocurrido un problema", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Toast.makeText(Configuracion.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        Autenticacion();
                    }
                });
            }
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(Configuracion.this, "Cancelado por el usuario", Toast.LENGTH_SHORT).show();
            }
        });

        alertDialog.create().show();
    }

    private void EliminarUsuarioBD() {
        Query query = databaseReference.child(getIntent().getStringExtra("Uid"));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ds.getRef().removeValue();
                }
                Toast.makeText(Configuracion.this, "Se ha eliminado su cuenta", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void Autenticacion() {
        Button Btn_Entendido_Aut, Btn_Cerrar_Sesion_Aut;

        dialog_autenticacion.setContentView(R.layout.cuadro_dialogo_autenticacion);

        Btn_Entendido_Aut = dialog_autenticacion.findViewById(R.id.Btn_Entendido_Aut);
        Btn_Cerrar_Sesion_Aut = dialog_autenticacion.findViewById(R.id.Btn_Cerrar_Sesion_Aut);

        Btn_Entendido_Aut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_autenticacion.dismiss();
            }
        });

        Btn_Cerrar_Sesion_Aut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CerrarSesion();
                dialog_autenticacion.dismiss();
            }
        });

        dialog_autenticacion.show();
        dialog_autenticacion.setCanceledOnTouchOutside(false);
    }

    private void CerrarSesion() {
        firebaseAuth.signOut();
        startActivity(new Intent(Configuracion.this, MainActivity.class));
        finish();
        Toast.makeText(this, "Cerraste sesión exitosamente", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}