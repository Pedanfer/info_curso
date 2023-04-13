package com.example.schooltools.Contactos;

import static android.content.Context.MODE_PRIVATE;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
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
import com.example.schooltools.Objetos.Contacto;
import com.example.schooltools.R;
import com.example.schooltools.ViewHolder.ViewHolderContacto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Listar_Contactos extends Fragment {

    RecyclerView recyclerViewContactos;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference BD_Usuarios;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;

    FloatingActionButton addContact, removeAll, filter, fabOptions;

    static String searchText = "";

    FirebaseRecyclerAdapter<Contacto, ViewHolderContacto> firebaseRecyclerAdapter;
    FirebaseRecyclerOptions<Contacto> firebaseRecyclerOptions;

    Dialog dialog, dialog_filtrar;

    static SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getActivity().getSharedPreferences("Contactos", MODE_PRIVATE);

        View view = inflater.inflate(R.layout.activity_listar_contactos, container, false);
        recyclerViewContactos = view.findViewById(R.id.recyclerViewContactos);
        recyclerViewContactos.setHasFixedSize(true);

        LinearLayout tasksFabColumn = view.findViewById(R.id.fabLayout);

        addContact = view.findViewById(R.id.fab_add_contactos);
        removeAll = view.findViewById(R.id.fab_remove_contactos);
        filter = view.findViewById(R.id.fab_filter_contactos);

        filter.setOnClickListener(vi -> FiltrarContactos());
        addContact.setOnClickListener(v -> startActivity(new Intent(getActivity(), Agregar_Contacto.class)));
        removeAll.setOnClickListener(vi -> Vaciar_Registro_Contactos());
        firebaseDatabase = FirebaseDatabase.getInstance();
        BD_Usuarios = firebaseDatabase.getReference("Usuarios");

        dialog = new Dialog(getActivity());
        dialog_filtrar = new Dialog(getActivity());

        dialog_filtrar.setOnDismissListener(dialogInterface -> recreate(searchText));

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();


        fabOptions = view.findViewById(R.id.fab_options_contactos);
        tasksFabColumn.setVisibility(View.GONE);
        fabOptions.setOnClickListener(new View.OnClickListener() {
            boolean showOptions = false;

            @Override
            public void onClick(View view) {
                showOptions = !showOptions;
                tasksFabColumn.setVisibility(showOptions ? View.VISIBLE : View.GONE);
            }
        });

        ListarContactos();
        return view;
    }

    public void recreate(String searchText) {
        this.searchText = searchText;
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new Listar_Contactos(), "Contactos")
                .commit();
    }

    private void FiltrarContactos() {
        dialog_filtrar.setContentView(R.layout.cuadro_dialogo_filtrar_contactos);

        Button todos_contactos = dialog_filtrar.findViewById(R.id.filtro_todos);
        Button profesores_contactos = dialog_filtrar.findViewById(R.id.filtro_profesores);
        Button alumnos_contactos = dialog_filtrar.findViewById(R.id.filtro_alumnos);

        List<Button> buttons = new ArrayList<Button>(Arrays.asList(todos_contactos, profesores_contactos, alumnos_contactos));

        changeColors(todos_contactos, profesores_contactos, alumnos_contactos, buttons);

        todos_contactos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("Listar", "Todos");
                editor.apply();
                changeColors(todos_contactos, profesores_contactos, alumnos_contactos, buttons);
            }
        });

        profesores_contactos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("Listar", "Profesores");
                editor.apply();
                changeColors(todos_contactos, profesores_contactos, alumnos_contactos, buttons);
            }
        });

        alumnos_contactos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("Listar", "Alumnos");
                editor.apply();
                changeColors(todos_contactos, profesores_contactos, alumnos_contactos, buttons);
            }
        });

        dialog_filtrar.show();
    }

    private void changeColors(Button todos_contactos, Button profesores_contactos, Button alunmnos_contactos, List<Button> buttons) {
        switch (sharedPreferences.getString("Listar", "")) {
            case "Todos":
                for (Button button : buttons) {
                    if (button == todos_contactos)
                        button.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                    else
                        button.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                }
                break;
            case "Alumnos":
                for (Button button : buttons) {
                    if (button == alunmnos_contactos)
                        button.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                    else
                        button.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                }
                break;
            case "Profesores":
                for (Button button : buttons) {
                    if (button == profesores_contactos)
                        button.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                    else
                        button.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                }
                break;
            default:
                break;
        }
    }

    private void ListarContactos() {
        Query query = BD_Usuarios.child(user.getUid()).child("Contactos").orderByChild("nombres");
        switch (sharedPreferences.getString("Listar", "")) {
            case "Profesores":
                query = BD_Usuarios.child(user.getUid()).child("Contactos").orderByChild("isProf").equalTo(true);
                break;
            case "Alumnos":
                query = BD_Usuarios.child(user.getUid()).child("Contactos").orderByChild("isProf").equalTo(false);
                break;
        }
        firebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<Contacto>().setQuery(query, Contacto.class).build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Contacto, ViewHolderContacto>(firebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull ViewHolderContacto viewHolderContacto, int position, @NonNull Contacto contacto) {
                if (!contacto.getNombres().contains(searchText)) {
                    viewHolderContacto.itemView.getLayoutParams().height = 0;
                    return;
                }
                viewHolderContacto.SetearDatosContacto(
                        getContext(),
                        contacto.getId_contacto(),
                        contacto.getUid_contacto(),
                        contacto.getNombres(),
                        contacto.getApellidos(),
                        contacto.getCorreo(),
                        contacto.getTelefono(),
                        contacto.getEdad(),
                        contacto.getDireccion(),
                        contacto.getImagen()
                );

            }

            @NonNull
            @Override
            public ViewHolderContacto onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contacto, parent, false);
                ViewHolderContacto viewHolderContacto = new ViewHolderContacto(view);
                viewHolderContacto.setOnClickListener(new ViewHolderContacto.ClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        //Toast.makeText(getActivity(), "On item click", Toast.LENGTH_SHORT).show();
                        //Obteniendo los datos del contacto seleccionado
                        String id_c = getItem(position).getId_contacto();
                        String uid_usuario = getItem(position).getUid_contacto();
                        String nombres_c = getItem(position).getNombres();
                        String apellidos_c = getItem(position).getApellidos();
                        String correo_c = getItem(position).getCorreo();
                        String telefono_c = getItem(position).getTelefono();
                        String edad_c = getItem(position).getEdad();
                        String direccion_c = getItem(position).getDireccion();
                        String imagen_c = getItem(position).getImagen();

                        //Enviar los datos a la siguiente actividad
                        Intent intent = new Intent(getActivity(), Detalle_contacto.class);
                        intent.putExtra("id_c", id_c);
                        intent.putExtra("uid_usuario", uid_usuario);
                        intent.putExtra("nombres_c", nombres_c);
                        intent.putExtra("apellidos_c", apellidos_c);
                        intent.putExtra("correo_c", correo_c);
                        intent.putExtra("telefono_c", telefono_c);
                        intent.putExtra("edad_c", edad_c);
                        intent.putExtra("direccion_c", direccion_c);
                        intent.putExtra("imagen_c", imagen_c);
                        startActivity(intent);
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {
                        String id_c = getItem(position).getId_contacto();
                        String uid_usuario = getItem(position).getUid_contacto();
                        String nombres_c = getItem(position).getNombres();
                        String apellidos_c = getItem(position).getApellidos();
                        String correo_c = getItem(position).getCorreo();
                        String telefono_c = getItem(position).getTelefono();
                        String edad_c = getItem(position).getEdad();
                        String direccion_c = getItem(position).getDireccion();
                        String imagen_c = getItem(position).getImagen();

                        //Toast.makeText(getActivity(), "On item long click", Toast.LENGTH_SHORT).show();
                        Button Btn_Eliminar_C, Btn_Actualizar_C;

                        dialog.setContentView(R.layout.cuadro_dialogo_opciones_contacto);

                        Btn_Eliminar_C = dialog.findViewById(R.id.Btn_Eliminar_C);
                        Btn_Actualizar_C = dialog.findViewById(R.id.Btn_Actualizar_C);

                        Btn_Eliminar_C.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //Toast.makeText(getActivity(), "Eliminar contacto", Toast.LENGTH_SHORT).show();
                                EliminarContacto(id_c);
                                dialog.dismiss();
                            }
                        });

                        Btn_Actualizar_C.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getActivity(), Actualizar_Contacto.class);
                                intent.putExtra("id_c", id_c);
                                intent.putExtra("uid_usuario", uid_usuario);
                                intent.putExtra("nombres_c", nombres_c);
                                intent.putExtra("apellidos_c", apellidos_c);
                                intent.putExtra("correo_c", correo_c);
                                intent.putExtra("telefono_c", telefono_c);
                                intent.putExtra("edad_c", edad_c);
                                intent.putExtra("direccion_c", direccion_c);
                                intent.putExtra("imagen_c", imagen_c);
                                startActivity(intent);
                                dialog.dismiss();
                            }
                        });

                        dialog.show();
                    }
                });
                return viewHolderContacto;
            }
        };

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        recyclerViewContactos.setLayoutManager(linearLayoutManager);
        recyclerViewContactos.setAdapter(firebaseRecyclerAdapter);
    }

    private void BuscarContactos(String Nombre_Contacto) {
        Query query = BD_Usuarios.child(user.getUid()).child("Contactos").orderByChild("nombres").startAt(Nombre_Contacto).endAt(Nombre_Contacto + "\uf8ff");
        firebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<Contacto>().setQuery(query, Contacto.class).build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Contacto, ViewHolderContacto>(firebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull ViewHolderContacto viewHolderContacto, int position, @NonNull Contacto contacto) {
                viewHolderContacto.SetearDatosContacto(
                        getContext(),
                        contacto.getId_contacto(),
                        contacto.getUid_contacto(),
                        contacto.getNombres(),
                        contacto.getApellidos(),
                        contacto.getCorreo(),
                        contacto.getTelefono(),
                        contacto.getEdad(),
                        contacto.getDireccion(),
                        contacto.getImagen()
                );

            }

            @NonNull
            @Override
            public ViewHolderContacto onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contacto, parent, false);
                ViewHolderContacto viewHolderContacto = new ViewHolderContacto(view);
                viewHolderContacto.setOnClickListener(new ViewHolderContacto.ClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        //Toast.makeText(getActivity(), "On item click", Toast.LENGTH_SHORT).show();
                        //Obteniendo los datos del contacto seleccionado
                        String id_c = getItem(position).getId_contacto();
                        String uid_usuario = getItem(position).getUid_contacto();
                        String nombres_c = getItem(position).getNombres();
                        String apellidos_c = getItem(position).getApellidos();
                        String correo_c = getItem(position).getCorreo();
                        String telefono_c = getItem(position).getTelefono();
                        String edad_c = getItem(position).getEdad();
                        String direccion_c = getItem(position).getDireccion();
                        String imagen_c = getItem(position).getImagen();

                        //Enviar los datos a la siguiente actividad
                        Intent intent = new Intent(getActivity(), Detalle_contacto.class);
                        intent.putExtra("id_c", id_c);
                        intent.putExtra("uid_usuario", uid_usuario);
                        intent.putExtra("nombres_c", nombres_c);
                        intent.putExtra("apellidos_c", apellidos_c);
                        intent.putExtra("correo_c", correo_c);
                        intent.putExtra("telefono_c", telefono_c);
                        intent.putExtra("edad_c", edad_c);
                        intent.putExtra("direccion_c", direccion_c);
                        intent.putExtra("imagen_c", imagen_c);
                        startActivity(intent);
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {
                        String id_c = getItem(position).getId_contacto();
                        String uid_usuario = getItem(position).getUid_contacto();
                        String nombres_c = getItem(position).getNombres();
                        String apellidos_c = getItem(position).getApellidos();
                        String correo_c = getItem(position).getCorreo();
                        String telefono_c = getItem(position).getTelefono();
                        String edad_c = getItem(position).getEdad();
                        String direccion_c = getItem(position).getDireccion();
                        String imagen_c = getItem(position).getImagen();

                        //Toast.makeText(getActivity(), "On item long click", Toast.LENGTH_SHORT).show();
                        Button Btn_Eliminar_C, Btn_Actualizar_C;

                        dialog.setContentView(R.layout.cuadro_dialogo_opciones_contacto);

                        Btn_Eliminar_C = dialog.findViewById(R.id.Btn_Eliminar_C);
                        Btn_Actualizar_C = dialog.findViewById(R.id.Btn_Actualizar_C);

                        Btn_Eliminar_C.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //Toast.makeText(getActivity(), "Eliminar contacto", Toast.LENGTH_SHORT).show();
                                EliminarContacto(id_c);
                                dialog.dismiss();
                            }
                        });

                        Btn_Actualizar_C.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getActivity(), Actualizar_Contacto.class);
                                intent.putExtra("id_c", id_c);
                                intent.putExtra("uid_usuario", uid_usuario);
                                intent.putExtra("nombres_c", nombres_c);
                                intent.putExtra("apellidos_c", apellidos_c);
                                intent.putExtra("correo_c", correo_c);
                                intent.putExtra("telefono_c", telefono_c);
                                intent.putExtra("edad_c", edad_c);
                                intent.putExtra("direccion_c", direccion_c);
                                intent.putExtra("imagen_c", imagen_c);
                                startActivity(intent);
                                dialog.dismiss();
                            }
                        });

                        dialog.show();
                    }
                });
                return viewHolderContacto;
            }
        };

        recyclerViewContactos.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        firebaseRecyclerAdapter.startListening();
        recyclerViewContactos.setAdapter(firebaseRecyclerAdapter);
    }

    private void EliminarContacto(String id_c) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Eliminar");
        builder.setMessage("¿Desea eliminar este contacto?");

        builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Query query = BD_Usuarios.child(user.getUid()).child("Contactos").orderByChild("id_contacto").equalTo(id_c);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ds.getRef().removeValue();
                        }
                        Toast.makeText(getActivity(), "Contacto eliminado", Toast.LENGTH_SHORT).show();
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
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getActivity(), "Cancelado por el usuario", Toast.LENGTH_SHORT).show();
            }
        });

        builder.create().show();
    }

    private void Vaciar_Registro_Contactos() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Vaciar todos los contactos");
        builder.setMessage("¿Estás seguro(a) de eliminar todos los contactos?");

        builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Query query = BD_Usuarios.child(user.getUid()).child("Contactos");
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ds.getRef().removeValue();
                        }
                        Toast.makeText(getActivity(), "Todos los contactos se han eliminado correctamente", Toast.LENGTH_SHORT).show();
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
    public void onStart() {
        super.onStart();
        if (firebaseRecyclerAdapter != null) {
            firebaseRecyclerAdapter.startListening();
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu_agregar_contacto, menu);
//        MenuItem item = menu.findItem(R.id.Buscar_contactos);
//        SearchView searchView = (SearchView) item.getActionView();
//        searchView.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                BuscarContactos(query);
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                BuscarContactos(newText);
//                return false;
//            }
//        });
//        return super.onCreateOptionsMenu(menu);
//    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.Agregar_contacto) {
            /*Recuperamos el uid de la actividad anterior*/
            //String Uid_Recuperado = getIntent().getStringExtra("Uid");
            Intent intent = new Intent(getActivity(), Agregar_Contacto.class);
            /*Enviamos el dato uid a la siguiente a actividad*/
            //intent.putExtra("Uid", Uid_Recuperado);
            startActivity(intent);
        }
        if (item.getItemId() == R.id.Vaciar_contactos) {
            Vaciar_Registro_Contactos();
        }
        return super.onOptionsItemSelected(item);
    }

}