package com.redsystem.agendaonline.Tareas;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.redsystem.agendaonline.R;
import com.redsystem.agendaonline.ToolBarActivity;

import java.util.ArrayList;

public class Definir_Asignaturas extends ToolBarActivity {
    ListView listSubjects;
    ArrayAdapter<String> adapter;
    MaterialButton addSubject, saveChanges;
    TextInputEditText nameField;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    DatabaseReference BD_Firebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setChildContentView(R.layout.activity_definir_asignaturas);
        InicializarVariables();
    }

    private void InicializarVariables() {
        BD_Firebase = FirebaseDatabase.getInstance().getReference("Usuarios");
        nameField = findViewById(R.id.nombreAsignatura);
        addSubject = findViewById(R.id.add_subject);
        saveChanges = findViewById(R.id.finish);
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        listSubjects = findViewById(R.id.lista_asignaturas);

        ListView listView = findViewById(R.id.lista_asignaturas);
        ArrayList<String> data = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, data);
        listView.setAdapter(adapter);

        addSubject.setOnClickListener(view -> Agregar_Asignatura());
        saveChanges.setOnClickListener(view -> {
            SharedPreferences.Editor prefsEditor = getSharedPreferences("prefs", Context.MODE_PRIVATE).edit();
            prefsEditor.putBoolean("subjectsAsigned", true);
            prefsEditor.commit();
            finish();
        });
    }

    private void Agregar_Asignatura() {
        String nombreAsig = nameField.getText().toString();

        if (!nombreAsig.isEmpty()) {
            String Nombre_BD = "Asignaturas";
            String idAsignatura = BD_Firebase.push().getKey();

            BD_Firebase.child(user.getUid()).child(Nombre_BD).child(idAsignatura).setValue(nombreAsig);

            adapter.add(nombreAsig);
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "Añadida asignatura", Toast.LENGTH_SHORT).show();
            nameField.setText("");
        } else {
            Toast.makeText(this, "Nombre vacío.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        BD_Firebase.child(user.getUid()).child("Asignaturas").removeValue();
        onBackPressed();
        return super.onSupportNavigateUp();
    }

}