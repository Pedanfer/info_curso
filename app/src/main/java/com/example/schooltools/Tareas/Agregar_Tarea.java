package com.example.schooltools.Tareas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.example.schooltools.Objetos.Tarea;
import com.example.schooltools.R;
import com.example.schooltools.ToolBarActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class Agregar_Tarea extends ToolBarActivity {
    Spinner spinnerSubjects;
    MaterialButton finish;

    TextView Uid_Usuario, Correo_usuario, Fecha_hora_actual, Fecha;
    EditText Titulo, Descripcion;
    Button Btn_Calendario;

    int dia, mes , anio;

    FirebaseAuth firebaseAuth;
    FirebaseUser user;

    DatabaseReference BD_Firebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setChildContentView(R.layout.activity_agregar_tarea);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Agregar tarea");

        InicializarVariables();

        ObtenerDatos();

        configSpinner();

        Obtener_Fecha_Hora_Actual();

        Btn_Calendario.setOnClickListener(view -> {
            final Calendar calendario = Calendar.getInstance();

            dia = calendario.get(Calendar.DAY_OF_MONTH);
            mes = calendario.get(Calendar.MONTH);
            anio = calendario.get(Calendar.YEAR);

            DatePickerDialog datePickerDialog = new DatePickerDialog(Agregar_Tarea.this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int AnioSeleccionado, int MesSeleccionado, int DiaSeleccionado) {

                    String diaFormateado, mesFormateado;

                    //OBTENER DIA
                    if (DiaSeleccionado < 10){
                        diaFormateado = "0"+String.valueOf(DiaSeleccionado);
                        // Antes: 9/11/2022 -  Ahora 09/11/2022
                    }else {
                        diaFormateado = String.valueOf(DiaSeleccionado);
                        //Ejemplo 13/08/2022
                    }

                    //OBTENER EL MES
                    int Mes = MesSeleccionado + 1;

                    if (Mes < 10){
                        mesFormateado = "0"+String.valueOf(Mes);
                        // Antes: 09/8/2022 -  Ahora 09/08/2022
                    }else {
                        mesFormateado = String.valueOf(Mes);
                        //Ejemplo 13/10/2022 - 13/11/2022 - 13/12/2022

                    }

                    //Setear fecha en TextView
                    Fecha.setText(diaFormateado + "/" + mesFormateado + "/"+ AnioSeleccionado);

                }
            }
            ,anio,mes,dia);
            datePickerDialog.show();

        });
    }

    private void configSpinner() {
        spinnerSubjects = findViewById(R.id.spinnerSubjects);

        List<String> subjects = new ArrayList<>();
        Query query = BD_Firebase.child(user.getUid()).child("Asignaturas");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    subjects.add(ds.getValue(String.class));
                }

                ArrayAdapter adapter = new ArrayAdapter(getBaseContext(), android.R.layout.simple_spinner_item, subjects);
                spinnerSubjects.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void InicializarVariables(){
        Uid_Usuario = findViewById(R.id.Uid_Usuario);
        Correo_usuario = findViewById(R.id.Correo_usuario);
        Fecha_hora_actual = findViewById(R.id.Fecha_hora_actual);
        Fecha = findViewById(R.id.Fecha);

        finish = findViewById(R.id.finish);
        finish.setOnClickListener(view -> Agregar_Tarea());

        Titulo = findViewById(R.id.Titulo);
        Descripcion = findViewById(R.id.Descripcion);
        Btn_Calendario = findViewById(R.id.Btn_Calendario);

        BD_Firebase = FirebaseDatabase.getInstance().getReference("Usuarios");

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
    }

    private void ObtenerDatos(){
        String uid_recuperado = getIntent().getStringExtra("Uid");
        String correo_recuperado = getIntent().getStringExtra("Correo");

        Uid_Usuario.setText(uid_recuperado);
        Correo_usuario.setText(correo_recuperado);
    }

    private void Obtener_Fecha_Hora_Actual(){
        String Fecha_hora_registro = new SimpleDateFormat("dd-MM-yyyy/HH:mm:ss a",
                Locale.getDefault()).format(System.currentTimeMillis());
        //EJEMPLO: 13-11-2022/06:30:20 pm
        Fecha_hora_actual.setText(Fecha_hora_registro);
    }

    private void Agregar_Tarea(){

        String fecha_hora_actual = Fecha_hora_actual.getText().toString();
        String titulo = Titulo.getText().toString();
        String descripcion = Descripcion.getText().toString();
        String fecha = Fecha.getText().toString();
        String id_tarea = BD_Firebase.push().getKey();
        String asignatura = spinnerSubjects.getSelectedItem().toString();

        //Validar datos
        if (!fecha_hora_actual.equals("") &&
                !titulo.equals("") && !descripcion.equals("") && ! fecha.equals("")){

            Tarea tarea = new Tarea(id_tarea,
                    user.getUid(),
                    user.getEmail(),
                    fecha_hora_actual,
                    titulo,
                    descripcion,
                    fecha,
                    "Por finalizar",
                    asignatura);

            //Establecer el nombre de la BD
            String Nombre_BD = "Tareas_Publicadas";

            assert id_tarea != null;
            BD_Firebase.child(user.getUid()).child(Nombre_BD).child(id_tarea).setValue(tarea);

            Toast.makeText(this, "Se ha agregado la tarea exitosamente", Toast.LENGTH_SHORT).show();
            onBackPressed();

        }
        else {
            Toast.makeText(this, "Llenar todos los campos", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}