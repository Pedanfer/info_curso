package com.example.schooltools.Tareas;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.example.schooltools.R;
import com.example.schooltools.ToolBarActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Actualizar_Tarea extends ToolBarActivity implements AdapterView.OnItemSelectedListener {

    Spinner spinnerSubjects;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;

    DatabaseReference BD_Firebase;

    TextView Fecha_registro_A, Fecha_A, Estado_A, Estado_nuevo;
    EditText Titulo_A, Descripcion_A;
    Button Btn_Calendario_A;

    //DECLARAR LOS STRING PARA ALMACENAR LOS DATOS RECUPERADOS DE ACTIVIDAD ANTERIOR
    String id_tarea_R, fecha_registro_R, titulo_R, descripcion_R, fecha_R, estado_R, asignatura_R;

    ImageView Tarea_Finalizada, Tarea_No_Finalizada;

    Spinner Spinner_estado;
    int dia, mes, anio;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setChildContentView(R.layout.activity_actualizar_tarea);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Actualizar tarea");

        InicializarVistas();
        RecuperarDatos();
        SetearDatos();
        ComprobarEstadoTarea();
        Spinner_Estado();
        configSpinner();

        findViewById(R.id.finish).setOnClickListener(v -> {
            ActualizarTareaBD();
            Toast.makeText(this, "Tarea actualizada", Toast.LENGTH_SHORT).show();
        });

        Btn_Calendario_A.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SeleccionarFecha();
            }
        });
    }

    private void InicializarVistas() {
        Fecha_registro_A = findViewById(R.id.Fecha_registro_A);
        Fecha_A = findViewById(R.id.Fecha_A);
        Estado_A = findViewById(R.id.Estado_A);
        Titulo_A = findViewById(R.id.Titulo_A);
        Descripcion_A = findViewById(R.id.Descripcion_A);
        Btn_Calendario_A = findViewById(R.id.Btn_Calendario_A);

        Tarea_Finalizada = findViewById(R.id.Tarea_Finalizada);
        Tarea_No_Finalizada = findViewById(R.id.Tarea_No_Finalizada);

        Spinner_estado = findViewById(R.id.Spinner_estado);
        Estado_nuevo = findViewById(R.id.Estado_nuevo);

        BD_Firebase = FirebaseDatabase.getInstance().getReference("Usuarios");

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
        asignatura_R = intent.getString("asignatura");
        id_tarea_R = intent.getString("id_tarea");
        Estado_nuevo.setText(estado_R);
    }

    private void SetearDatos() {
        Fecha_registro_A.setText(fecha_registro_R);
        Titulo_A.setText(titulo_R);
        Descripcion_A.setText(descripcion_R);
        Fecha_A.setText(fecha_R);
        Estado_A.setText(estado_R);
    }

    private void ComprobarEstadoTarea() {
        String estado_tarea = Estado_A.getText().toString();

        if (estado_tarea.equals("Por finalizar")) {
            Tarea_No_Finalizada.setVisibility(View.VISIBLE);
        }
        if (estado_tarea.equals("Finalizada")) {
            Tarea_Finalizada.setVisibility(View.VISIBLE);
        }
    }

    private void SeleccionarFecha() {
        final Calendar calendario = Calendar.getInstance();

        dia = calendario.get(Calendar.DAY_OF_MONTH);
        mes = calendario.get(Calendar.MONTH);
        anio = calendario.get(Calendar.YEAR);

        DatePickerDialog datePickerDialog = new DatePickerDialog(Actualizar_Tarea.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int AnioSeleccionado, int MesSeleccionado, int DiaSeleccionado) {

                String diaFormateado, mesFormateado;

                //OBTENER DIA
                if (DiaSeleccionado < 10) {
                    diaFormateado = "0" + String.valueOf(DiaSeleccionado);
                    // Antes: 9/11/2022 -  Ahora 09/11/2022
                } else {
                    diaFormateado = String.valueOf(DiaSeleccionado);
                    //Ejemplo 13/08/2022
                }

                //OBTENER EL MES
                int Mes = MesSeleccionado + 1;

                if (Mes < 10) {
                    mesFormateado = "0" + String.valueOf(Mes);
                    // Antes: 09/8/2022 -  Ahora 09/08/2022
                } else {
                    mesFormateado = String.valueOf(Mes);
                    //Ejemplo 13/10/2022 - 13/11/2022 - 13/12/2022

                }

                //Setear fecha en TextView
                Fecha_A.setText(diaFormateado + "/" + mesFormateado + "/" + AnioSeleccionado);

            }
        }
                , anio, mes, dia);
        datePickerDialog.show();
    }

    private void Spinner_Estado() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.Estadostarea, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner_estado.setAdapter(adapter);
        Spinner_estado.setOnItemSelectedListener(this);

    }

    private void ActualizarTareaBD() {
        String tituloActualizar = Titulo_A.getText().toString();
        String descripcionActualizar = Descripcion_A.getText().toString();
        String fechaActualizar = Fecha_A.getText().toString();
        String estadoActualizar = Estado_nuevo.getText().toString();
        String asignaturaActualizar = asignatura_R;

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("Usuarios");

        //Consulta
        Query query = databaseReference.child(user.getUid()).child("Tareas_Publicadas").orderByChild("id_tarea").equalTo(id_tarea_R);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ds.getRef().child("titulo").setValue(tituloActualizar);
                    ds.getRef().child("descripcion").setValue(descripcionActualizar);
                    ds.getRef().child("fecha_tarea").setValue(fechaActualizar);
                    ds.getRef().child("estado").setValue(estadoActualizar);
                    ds.getRef().child("asignatura").setValue(asignaturaActualizar);
                }

                Toast.makeText(Actualizar_Tarea.this, "Tarea actualizada con éxito", Toast.LENGTH_SHORT).show();
                onBackPressed();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void configSpinner() {
        spinnerSubjects = findViewById(R.id.spinnerSubjects);
        spinnerSubjects.setOnItemSelectedListener(this);

        List<String> subjects = new ArrayList<>();
        Query query = BD_Firebase.child(user.getUid()).child("Asignaturas");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    subjects.add(ds.getValue(String.class));
                }

                ArrayAdapter adapter = new ArrayAdapter(getBaseContext(), android.R.layout.simple_spinner_item, subjects);
                spinnerSubjects.setAdapter(adapter);
                spinnerSubjects.setSelection(subjects.indexOf(getIntent().getStringExtra("asignatura")));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String estado_seleccionado = adapterView.getItemAtPosition(i).toString();
        if (estado_seleccionado.contains("inaliz")) {
            Estado_nuevo.setText(estado_seleccionado);
        } else {
            asignatura_R = adapterView.getItemAtPosition(i).toString();
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}