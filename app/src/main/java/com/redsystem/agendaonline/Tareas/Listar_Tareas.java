package com.redsystem.agendaonline.Tareas;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.redsystem.agendaonline.Objetos.Tarea;
import com.redsystem.agendaonline.R;
import com.redsystem.agendaonline.ViewHolder.ViewHolder_Tarea;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Listar_Tareas extends Fragment {

    RecyclerView recyclerviewTareas;
    FirebaseDatabase firebaseDatabase;

    DatabaseReference BD_Usuarios;
    LinearLayoutManager linearLayoutManager;

    FirebaseRecyclerAdapter<Tarea, ViewHolder_Tarea> firebaseRecyclerAdapter;
    FirebaseRecyclerOptions<Tarea> options;

    String filtro_asignatura;

    LinearLayout tasksFabColumn;

    FloatingActionButton addTask, removeTask, filter, fabOptions;

    static Dialog dialog, dialog_filtrar;

    FirebaseAuth auth;
    FirebaseUser user;

    static SharedPreferences sharedPreferences;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getActivity().getSharedPreferences("Tareas", MODE_PRIVATE);

        filtro_asignatura = sharedPreferences.getString("filtro_asignatura", "Todas");

        View view = inflater.inflate(R.layout.activity_listar_tareas, container, false);
        recyclerviewTareas = view.findViewById(R.id.recyclerviewTareas);
        recyclerviewTareas.setHasFixedSize(true);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        dialog = new Dialog(getActivity());
        dialog_filtrar = new Dialog(getActivity());

        dialog_filtrar.setOnDismissListener(dialogInterface -> recreate());

        tasksFabColumn = view.findViewById(R.id.tasksFabLayout);

        addTask = view.findViewById(R.id.fab_add);
        removeTask = view.findViewById(R.id.fab_remove);
        filter = view.findViewById(R.id.fab_filter);
        fabOptions = view.findViewById(R.id.fab_options);
        tasksFabColumn.setVisibility(View.GONE);

        fabOptions.setOnClickListener(new View.OnClickListener() {
            boolean showOptions = false;

            @Override
            public void onClick(View view) {
                showOptions = !showOptions;
                tasksFabColumn.setVisibility(showOptions ? View.VISIBLE : View.GONE);
            }
        });

        filter.setOnClickListener(v -> FiltrarTareas());
        removeTask.setOnClickListener(v -> Vaciar_Registro_De_tareas());
        addTask.setOnClickListener(view1 -> {
            SharedPreferences prefs = getActivity().getSharedPreferences("Tareas", MODE_PRIVATE);
            if (!prefs.getBoolean("subjectsAsigned", false))
                startActivity(new Intent(getActivity(), Definir_Asignaturas.class));
            else
                startActivity(new Intent(getActivity(), Agregar_Tarea.class));
        });

        firebaseDatabase = FirebaseDatabase.getInstance();
        BD_Usuarios = firebaseDatabase.getReference("Usuarios");
        Estado_Filtro();

        return view;
    }

    private void ListarTodasTareas() {
        //consulta
        Query query = BD_Usuarios.child(user.getUid()).child("Tareas_Publicadas").orderByChild("fecha_tarea");
        options = new FirebaseRecyclerOptions.Builder<Tarea>().setQuery(query, Tarea.class).build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Tarea, ViewHolder_Tarea>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ViewHolder_Tarea viewHolder_tarea, int position, @NonNull Tarea _tarea) {
                if (!filtro_asignatura.equals("Todas") && !_tarea.getAsignatura().equals(filtro_asignatura)) {
                    viewHolder_tarea.itemView.getLayoutParams().height = 0;
                    return;
                }
                viewHolder_tarea.SetearDatos(
                        getContext(),
                        _tarea.getId_tarea(),
                        _tarea.getUid_usuario(),
                        _tarea.getCorreo_usuario(),
                        _tarea.getFecha_hora_actual(),
                        _tarea.getTitulo(),
                        _tarea.getDescripcion(),
                        _tarea.getFecha_tarea(),
                        _tarea.getEstado(),
                        _tarea.getAsignatura()
                );
            }


            @Override
            public ViewHolder_Tarea onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tarea, parent, false);
                ViewHolder_Tarea viewHolder_tarea = new ViewHolder_Tarea(view);
                viewHolder_tarea.setOnClickListener(new ViewHolder_Tarea.ClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                        //Obtener los datos de la _tarea seleccionada
                        String id_tarea = getItem(position).getId_tarea();
                        String uid_usuario = getItem(position).getUid_usuario();
                        String correo_usuario = getItem(position).getCorreo_usuario();
                        String fecha_registro = getItem(position).getFecha_hora_actual();
                        String titulo = getItem(position).getTitulo();
                        String descripcion = getItem(position).getDescripcion();
                        String fecha_tarea = getItem(position).getFecha_tarea();
                        String estado = getItem(position).getEstado();
                        String asignatura = getItem(position).getAsignatura();

                        //Enviamos los datos a la siguiente actividad
                        Intent intent = new Intent(getActivity(), Detalle_Tarea.class);
                        intent.putExtra("id_tarea", id_tarea);
                        intent.putExtra("uid_usuario", uid_usuario);
                        intent.putExtra("correo_usuario", correo_usuario);
                        intent.putExtra("fecha_registro", fecha_registro);
                        intent.putExtra("titulo", titulo);
                        intent.putExtra("descripcion", descripcion);
                        intent.putExtra("fecha_tarea", fecha_tarea);
                        intent.putExtra("estado", estado);
                        intent.putExtra("asignatura", asignatura);
                        startActivity(intent);
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {

                        //Obtener los datos de la _tarea seleccionada
                        String id_tarea = getItem(position).getId_tarea();
                        String uid_usuario = getItem(position).getUid_usuario();
                        String correo_usuario = getItem(position).getCorreo_usuario();
                        String fecha_registro = getItem(position).getFecha_hora_actual();
                        String titulo = getItem(position).getTitulo();
                        String descripcion = getItem(position).getDescripcion();
                        String fecha_tarea = getItem(position).getFecha_tarea();
                        String estado = getItem(position).getEstado();

                        //Declarar las vistas
                        Button CD_Eliminar, CD_Actualizar;

                        //Realizar la conexión con el diseño
                        dialog.setContentView(R.layout.dialogo_opciones);

                        //Inicializar las vistas
                        CD_Eliminar = dialog.findViewById(R.id.CD_Eliminar);
                        CD_Actualizar = dialog.findViewById(R.id.CD_Actualizar);

                        CD_Eliminar.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                EliminarTarea(id_tarea);
                                dialog.dismiss();
                            }
                        });

                        CD_Actualizar.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //Toast.makeText(getActivity(), "Actualizar _tarea", Toast.LENGTH_SHORT).show();
                                //startActivity(new Intent(getActivity(), Actualizar_Tarea.class));
                                Intent intent = new Intent(getActivity(), Actualizar_Tarea.class);
                                intent.putExtra("id_tarea", id_tarea);
                                intent.putExtra("uid_usuario", uid_usuario);
                                intent.putExtra("correo_usuario", correo_usuario);
                                intent.putExtra("fecha_registro", fecha_registro);
                                intent.putExtra("titulo", titulo);
                                intent.putExtra("descripcion", descripcion);
                                intent.putExtra("fecha_tarea", fecha_tarea);
                                intent.putExtra("estado", estado);
                                startActivity(intent);
                                dialog.dismiss();

                            }
                        });
                        dialog.show();
                    }
                });
                return viewHolder_tarea;
            }
        };

        linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        recyclerviewTareas.setLayoutManager(linearLayoutManager);
        recyclerviewTareas.setAdapter(firebaseRecyclerAdapter);

    }

    private void ListarTareasFinalizadas() {
        //consulta
        String estado_tarea = "Finalizada";
        Query query = BD_Usuarios.child(user.getUid()).child("Tareas_Publicadas").orderByChild("estado").equalTo(estado_tarea);
        options = new FirebaseRecyclerOptions.Builder<Tarea>().setQuery(query, Tarea.class).build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Tarea, ViewHolder_Tarea>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ViewHolder_Tarea viewHolder_tarea, int position, @NonNull Tarea _tarea) {
                if (!filtro_asignatura.equals("Todas") && !_tarea.getAsignatura().equals(filtro_asignatura)) {
                    viewHolder_tarea.itemView.getLayoutParams().height = 0;
                    return;
                }
                viewHolder_tarea.SetearDatos(
                        getContext(),
                        _tarea.getId_tarea(),
                        _tarea.getUid_usuario(),
                        _tarea.getCorreo_usuario(),
                        _tarea.getFecha_hora_actual(),
                        _tarea.getTitulo(),
                        _tarea.getDescripcion(),
                        _tarea.getFecha_tarea(),
                        _tarea.getEstado(),
                        _tarea.getAsignatura()
                );
            }


            @Override
            public ViewHolder_Tarea onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tarea, parent, false);
                ViewHolder_Tarea viewHolder_tarea = new ViewHolder_Tarea(view);
                viewHolder_tarea.setOnClickListener(new ViewHolder_Tarea.ClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                        //Obtener los datos de la _tarea seleccionada
                        String id_tarea = getItem(position).getId_tarea();
                        String uid_usuario = getItem(position).getUid_usuario();
                        String correo_usuario = getItem(position).getCorreo_usuario();
                        String fecha_registro = getItem(position).getFecha_hora_actual();
                        String titulo = getItem(position).getTitulo();
                        String descripcion = getItem(position).getDescripcion();
                        String fecha_tarea = getItem(position).getFecha_tarea();
                        String estado = getItem(position).getEstado();
                        String asignatura = getItem(position).getAsignatura();

                        //Enviamos los datos a la siguiente actividad
                        Intent intent = new Intent(getActivity(), Detalle_Tarea.class);
                        intent.putExtra("id_tarea", id_tarea);
                        intent.putExtra("uid_usuario", uid_usuario);
                        intent.putExtra("correo_usuario", correo_usuario);
                        intent.putExtra("fecha_registro", fecha_registro);
                        intent.putExtra("titulo", titulo);
                        intent.putExtra("descripcion", descripcion);
                        intent.putExtra("fecha_tarea", fecha_tarea);
                        intent.putExtra("estado", estado);
                        intent.putExtra("asignatura", asignatura);
                        startActivity(intent);
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {

                        //Obtener los datos de la _tarea seleccionada
                        String id_tarea = getItem(position).getId_tarea();
                        String uid_usuario = getItem(position).getUid_usuario();
                        String correo_usuario = getItem(position).getCorreo_usuario();
                        String fecha_registro = getItem(position).getFecha_hora_actual();
                        String titulo = getItem(position).getTitulo();
                        String descripcion = getItem(position).getDescripcion();
                        String fecha_tarea = getItem(position).getFecha_tarea();
                        String estado = getItem(position).getEstado();

                        //Declarar las vistas
                        Button CD_Eliminar, CD_Actualizar;

                        //Realizar la conexión con el diseño
                        dialog.setContentView(R.layout.dialogo_opciones);

                        //Inicializar las vistas
                        CD_Eliminar = dialog.findViewById(R.id.CD_Eliminar);
                        CD_Actualizar = dialog.findViewById(R.id.CD_Actualizar);

                        CD_Eliminar.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                EliminarTarea(id_tarea);
                                dialog.dismiss();
                            }
                        });

                        CD_Actualizar.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //Toast.makeText(getActivity(), "Actualizar _tarea", Toast.LENGTH_SHORT).show();
                                //startActivity(new Intent(getActivity(), Actualizar_Tarea.class));
                                Intent intent = new Intent(getActivity(), Actualizar_Tarea.class);
                                intent.putExtra("id_tarea", id_tarea);
                                intent.putExtra("uid_usuario", uid_usuario);
                                intent.putExtra("correo_usuario", correo_usuario);
                                intent.putExtra("fecha_registro", fecha_registro);
                                intent.putExtra("titulo", titulo);
                                intent.putExtra("descripcion", descripcion);
                                intent.putExtra("fecha_tarea", fecha_tarea);
                                intent.putExtra("estado", estado);
                                startActivity(intent);
                                dialog.dismiss();

                            }
                        });
                        dialog.show();
                    }
                });
                return viewHolder_tarea;
            }
        };

        linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        recyclerviewTareas.setLayoutManager(linearLayoutManager);
        recyclerviewTareas.setAdapter(firebaseRecyclerAdapter);

    }

    private void ListarTareasNoFinalizadas() {
        //consulta
        String estado_tarea = "Por finalizar";
        Query query = BD_Usuarios.child(user.getUid()).child("Tareas_Publicadas").orderByChild("estado").equalTo(estado_tarea);
        options = new FirebaseRecyclerOptions.Builder<Tarea>().setQuery(query, Tarea.class).build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Tarea, ViewHolder_Tarea>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ViewHolder_Tarea viewHolder_tarea, int position, @NonNull Tarea _tarea) {
                if (!filtro_asignatura.equals("Todas") && !_tarea.getAsignatura().equals(filtro_asignatura)) {
                    viewHolder_tarea.itemView.getLayoutParams().height = 0;
                    return;
                }
                viewHolder_tarea.SetearDatos(
                        getContext(),
                        _tarea.getId_tarea(),
                        _tarea.getUid_usuario(),
                        _tarea.getCorreo_usuario(),
                        _tarea.getFecha_hora_actual(),
                        _tarea.getTitulo(),
                        _tarea.getDescripcion(),
                        _tarea.getFecha_tarea(),
                        _tarea.getEstado(),
                        _tarea.getAsignatura()
                );
            }


            @Override
            public ViewHolder_Tarea onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tarea, parent, false);
                ViewHolder_Tarea viewHolder_tarea = new ViewHolder_Tarea(view);
                viewHolder_tarea.setOnClickListener(new ViewHolder_Tarea.ClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                        //Obtener los datos de la _tarea seleccionada
                        String id_tarea = getItem(position).getId_tarea();
                        String uid_usuario = getItem(position).getUid_usuario();
                        String correo_usuario = getItem(position).getCorreo_usuario();
                        String fecha_registro = getItem(position).getFecha_hora_actual();
                        String titulo = getItem(position).getTitulo();
                        String descripcion = getItem(position).getDescripcion();
                        String fecha_tarea = getItem(position).getFecha_tarea();
                        String estado = getItem(position).getEstado();
                        String asignatura = getItem(position).getAsignatura();

                        //Enviamos los datos a la siguiente actividad
                        Intent intent = new Intent(getActivity(), Detalle_Tarea.class);
                        intent.putExtra("id_tarea", id_tarea);
                        intent.putExtra("uid_usuario", uid_usuario);
                        intent.putExtra("correo_usuario", correo_usuario);
                        intent.putExtra("fecha_registro", fecha_registro);
                        intent.putExtra("titulo", titulo);
                        intent.putExtra("descripcion", descripcion);
                        intent.putExtra("fecha_tarea", fecha_tarea);
                        intent.putExtra("estado", estado);
                        intent.putExtra("asignatura", asignatura);
                        startActivity(intent);
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {

                        //Obtener los datos de la _tarea seleccionada
                        String id_tarea = getItem(position).getId_tarea();
                        String uid_usuario = getItem(position).getUid_usuario();
                        String correo_usuario = getItem(position).getCorreo_usuario();
                        String fecha_registro = getItem(position).getFecha_hora_actual();
                        String titulo = getItem(position).getTitulo();
                        String descripcion = getItem(position).getDescripcion();
                        String fecha_tarea = getItem(position).getFecha_tarea();
                        String estado = getItem(position).getEstado();

                        //Declarar las vistas
                        Button CD_Eliminar, CD_Actualizar;

                        //Realizar la conexión con el diseño
                        dialog.setContentView(R.layout.dialogo_opciones);

                        //Inicializar las vistas
                        CD_Eliminar = dialog.findViewById(R.id.CD_Eliminar);
                        CD_Actualizar = dialog.findViewById(R.id.CD_Actualizar);

                        CD_Eliminar.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                EliminarTarea(id_tarea);
                                dialog.dismiss();
                            }
                        });

                        CD_Actualizar.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //Toast.makeText(getActivity(), "Actualizar _tarea", Toast.LENGTH_SHORT).show();
                                //startActivity(new Intent(getActivity(), Actualizar_Tarea.class));
                                Intent intent = new Intent(getActivity(), Actualizar_Tarea.class);
                                intent.putExtra("id_tarea", id_tarea);
                                intent.putExtra("uid_usuario", uid_usuario);
                                intent.putExtra("correo_usuario", correo_usuario);
                                intent.putExtra("fecha_registro", fecha_registro);
                                intent.putExtra("titulo", titulo);
                                intent.putExtra("descripcion", descripcion);
                                intent.putExtra("fecha_tarea", fecha_tarea);
                                intent.putExtra("estado", estado);
                                startActivity(intent);
                                dialog.dismiss();

                            }
                        });
                        dialog.show();
                    }
                });
                return viewHolder_tarea;
            }
        };

        linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        recyclerviewTareas.setLayoutManager(linearLayoutManager);
        recyclerviewTareas.setAdapter(firebaseRecyclerAdapter);

    }

    private void EliminarTarea(String id_tarea) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Eliminar tarea");
        builder.setMessage("¿Desea eliminar la tarea?");
        builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //ELIMINAR NOTA EN BD
                Query query = BD_Usuarios.child(user.getUid()).child("Tareas_Publicadas").orderByChild("id_tarea").equalTo(id_tarea);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ds.getRef().removeValue();
                        }
                        Toast.makeText(getActivity(), "Tarea eliminada", Toast.LENGTH_SHORT).show();
                        recreate();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getActivity(), "Cancelado por el usuario", Toast.LENGTH_SHORT).show();
            }
        });

        builder.create().show();
    }

    private void Vaciar_Registro_De_tareas() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Vaciar todos los registros");
        builder.setMessage("¿Estás seguro(a) de eliminar todas las Tareas?");

        builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Eliminación de todas las Tareas
                Query query = BD_Usuarios.child(user.getUid()).child("Tareas_Publicadas");
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ds.getRef().removeValue();
                        }
                        recreate();
                        Toast.makeText(getActivity(), "Todas las Tareas se han eliminado correctamente", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getActivity(), "Cancelado por el usuario", Toast.LENGTH_SHORT).show();
            }
        });

        builder.create().show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.Vaciar_Todas_Las_Tareas) {
            Vaciar_Registro_De_tareas();
        }
        if (item.getItemId() == R.id.Filtrar_Tareas) {
            FiltrarTareas();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (firebaseRecyclerAdapter != null) {
            firebaseRecyclerAdapter.startListening();
        }
    }

    private void recreate() {
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new Listar_Tareas())
                .commit();
    }

    private void configSpinner() {
        Spinner spinnerSubjects = dialog_filtrar.findViewById(R.id.spinnerSubjects);

        AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener() {
            boolean firstIgnore = true;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinnerSubjects.setSelection(position);
                if (firstIgnore) {
                    firstIgnore = false;
                    return;
                }
                String asignatura = parent.getItemAtPosition(position).toString();
                sharedPreferences.edit().putString("filtro_asignatura", asignatura).commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        };

        spinnerSubjects.setOnItemSelectedListener(spinnerListener);

        List<String> subjects = new ArrayList<>();
        Query query = BD_Usuarios.child(user.getUid()).child("Asignaturas");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    subjects.add(ds.getValue(String.class));
                }

                ArrayAdapter adapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, subjects);
                spinnerSubjects.setAdapter(adapter);
                spinnerSubjects.setSelection(subjects.indexOf(filtro_asignatura));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void FiltrarTareas() {
        dialog_filtrar.setContentView(R.layout.cuadro_dialogo_filtrar_tareas);
        configSpinner();

        Button Todas_tareas = dialog_filtrar.findViewById(R.id.Todas_Tareas);
        Button Tareas_Finalizadas = dialog_filtrar.findViewById(R.id.Tareas_Finalizadas);
        Button Tareas_No_Finalizadas = dialog_filtrar.findViewById(R.id.Tareas_No_Finalizadas);

        List<Button> buttons = new ArrayList<Button>(Arrays.asList(Todas_tareas, Tareas_Finalizadas, Tareas_No_Finalizadas));

        changeColors(Todas_tareas, Tareas_Finalizadas, Tareas_No_Finalizadas, buttons);

        Todas_tareas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("Listar", "Todas");
                editor.apply();
                changeColors(Todas_tareas, Tareas_Finalizadas, Tareas_No_Finalizadas, buttons);
            }
        });

        Tareas_Finalizadas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("Listar", "Finalizadas");
                editor.apply();
                changeColors(Todas_tareas, Tareas_Finalizadas, Tareas_No_Finalizadas, buttons);
            }
        });

        Tareas_No_Finalizadas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("Listar", "Por finalizar");
                editor.apply();
                changeColors(Todas_tareas, Tareas_Finalizadas, Tareas_No_Finalizadas, buttons);
            }
        });

        dialog_filtrar.show();
    }

    private void changeColors(Button Todas_tareas, Button Tareas_Finalizadas, Button Tareas_No_Finalizadas, List<Button> buttons) {
        switch (sharedPreferences.getString("Listar", "")) {
            case "Todas":
                for (Button button : buttons) {
                    if (button == Todas_tareas)
                        button.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                    else
                        button.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                }
                break;
            case "Finalizadas":
                for (Button button : buttons) {
                    if (button == Tareas_Finalizadas)
                        button.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                    else
                        button.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                }
                break;
            case "Por finalizar":
                for (Button button : buttons) {
                    if (button == Tareas_No_Finalizadas)
                        button.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                    else
                        button.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                }
                break;
            default:
                break;
        }
    }

    private void Estado_Filtro() {

        String estado_filtro = sharedPreferences.getString("Listar", "Todas");

        switch (estado_filtro) {
            case "Todas":
                ListarTodasTareas();
                break;
            case "Finalizadas":
                ListarTareasFinalizadas();
                break;
            case "Por finalizar":
                ListarTareasNoFinalizadas();
                break;
        }

    }

}