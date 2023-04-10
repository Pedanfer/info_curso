package com.example.schooltools;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.schooltools.Configuracion.Configuracion;
import com.example.schooltools.Contactos.Listar_Contactos;
import com.example.schooltools.Tareas.Listar_Tareas;
import com.example.schooltools.Tareas.Tareas_Importantes;
import com.example.schooltools.Objetos.DrawerItemViewModel;
import com.example.schooltools.Perfil.Perfil_Usuario;

import java.util.ArrayList;
import java.util.List;

public class MenuPrincipal extends ToolBarActivity {

    private String[] mNavigationDrawerItemTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    private ImageView profilePic;
    ActionBarDrawerToggle mDrawerToggle;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;

    TextView UidPrincipal, NombresPrincipal, CorreoPrincipal;
    Button EstadoCuentaPrincipal;
    ProgressBar progressBarDatos;
    ProgressDialog progressDialog;
    LinearLayoutCompat Linear_Nombres, Linear_Correo, Linear_Verificacion;

    Toolbar toolbar;

    DatabaseReference Usuarios;

    Dialog dialog_cuenta_verificada, dialog_informacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setChildContentView(R.layout.activity_menu_principal);

        initializeDrawer();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setSelectedItemId(R.id.tasks);
        setFragmentListarTareas();

        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.tasks:
                    setFragmentListarTareas();
                    return true;
                case R.id.contacts:
                    setFragmentListarContactos();
                    return true;
            }
            return false;
        });

        NombresPrincipal = findViewById(R.id.NombresPrincipal);
        CorreoPrincipal = findViewById(R.id.CorreoPrincipal);
        EstadoCuentaPrincipal = findViewById(R.id.EstadoCuentaPrincipal);
        progressBarDatos = findViewById(R.id.progressBarDatos);
        profilePic = findViewById(R.id.profile_pic_main);

        Perfil_Usuario.getUserImageInto(profilePic, this);

        dialog_cuenta_verificada = new Dialog(this);
        dialog_informacion = new Dialog(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Espere por favor ...");
        progressDialog.setCanceledOnTouchOutside(false);

        Linear_Nombres = findViewById(R.id.Linear_Nombres);
        Linear_Correo = findViewById(R.id.Linear_Correo);
        Linear_Verificacion = findViewById(R.id.Linear_Verficacion);

        Usuarios = FirebaseDatabase.getInstance().getReference("Usuarios");

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        CardView profileCard = findViewById(R.id.profile_card);

        profileCard.setOnClickListener(v -> {
            startActivity(new Intent(MenuPrincipal.this, Perfil_Usuario.class));
        });

        ImageButton closeButton = findViewById(R.id.closeButton);

        closeButton.setOnClickListener((View.OnClickListener) v -> profileCard.setVisibility(View.GONE));

        EstadoCuentaPrincipal.setOnClickListener(view -> {
            if (user.isEmailVerified()) {
                AnimacionCuentaVerificada();
            } else {
                VerificarCuentaCorreo();
            }
        });
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }

    }

    private void setFragmentListarTareas() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new Listar_Tareas())
                .commit();
    }

    private void setFragmentListarContactos() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new Listar_Contactos())
                .commit();
    }

    private void selectItem(int position) {
        mDrawerList.setItemChecked(position, false);
        mDrawerLayout.closeDrawer(mDrawerList);

        switch (position) {
            case 0:
                startActivity(new Intent(MenuPrincipal.this, Perfil_Usuario.class));
                break;
            case 1:
                Informacion();
                break;
            case 2:
                startActivity(new Intent(MenuPrincipal.this, Configuracion.class));
                break;
            case 3:
                startActivity(new Intent(MenuPrincipal.this, Tareas_Importantes.class));
                break;
            case 4:
                SalirAplicacion();
                break;
            default:
                break;
        }
    }

    private void initializeDrawer() {
        mTitle = mDrawerTitle = getTitle();
        mNavigationDrawerItemTitles = getResources().getStringArray(R.array.navigation_drawer_items_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        List<DrawerItemViewModel> drawerItems = new ArrayList<>();

        drawerItems.add(new DrawerItemViewModel(R.drawable.icono_perfil_usuario, "Perfil"));
        drawerItems.add(new DrawerItemViewModel(R.drawable.info, "Acerca de"));
        drawerItems.add(new DrawerItemViewModel(R.drawable.icono_configuracion, "Configuración"));
        drawerItems.add(new DrawerItemViewModel(R.drawable.alert, "Tareas importantes"));
        drawerItems.add(new DrawerItemViewModel(R.drawable.logout, "Salir"));

        DrawerItemAdapter adapter = new DrawerItemAdapter(this, R.layout.drawer_row_item, drawerItems.toArray(new DrawerItemViewModel[3]));
        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        setupDrawerToggle();
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    void setupDrawerToggle() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.app_name, R.string.app_name);
        //This is necessary to change the icon of the Drawer Toggle upon state change.
        mDrawerToggle.syncState();
    }

    private void VerificarCuentaCorreo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("verificar cuenta")
                .setMessage("¿Estás seguro(a) de enviar instrucciones de verificación a su correo electrónico? "
                        + user.getEmail())
                .setPositiveButton("Enviar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        EnviarCorreoAVerificacion();
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(MenuPrincipal.this, "Cancelado por el usuario", Toast.LENGTH_SHORT).show();
                    }
                }).show();
    }

    private void EnviarCorreoAVerificacion() {
        progressDialog.setMessage("Enviando instrucciones de verificación a su correo electrónico " + user.getEmail());
        progressDialog.show();

        user.sendEmailVerification()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //Envío fue exitoso
                        progressDialog.dismiss();
                        Toast.makeText(MenuPrincipal.this, "Instrucciones enviadas, revise su bandeja " + user.getEmail(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Falló el envío
                        Toast.makeText(MenuPrincipal.this, "Falló debido a: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void VerificarEstadoDeCuenta() {
        String Verificado = "Verificado";
        String No_Verificado = "No Verificado";
        if (user.isEmailVerified()) {
            EstadoCuentaPrincipal.setText(Verificado);
            EstadoCuentaPrincipal.setBackgroundColor(Color.rgb(41, 128, 185));
        } else {
            EstadoCuentaPrincipal.setText(No_Verificado);
            EstadoCuentaPrincipal.setBackgroundColor(Color.rgb(231, 76, 60));
        }
    }

    private void AnimacionCuentaVerificada() {
        Button EntendidoVerificado;

        dialog_cuenta_verificada.setContentView(R.layout.dialogo_cuenta_verificada);

        EntendidoVerificado = dialog_cuenta_verificada.findViewById(R.id.EntendidoVerificado);

        EntendidoVerificado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog_cuenta_verificada.dismiss();
            }
        });

        dialog_cuenta_verificada.show();
        dialog_cuenta_verificada.setCanceledOnTouchOutside(false);
    }

    private void Informacion() {
        Button EntendidoInfo;

        dialog_informacion.setContentView(R.layout.cuadro_dialogo_informacion);

        EntendidoInfo = dialog_informacion.findViewById(R.id.EntendidoInfo);

        EntendidoInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_informacion.dismiss();
            }
        });

        dialog_informacion.show();
        dialog_informacion.setCanceledOnTouchOutside(false);
    }

    @Override
    protected void onStart() {
        ComprobarInicioSesion();
        super.onStart();
    }

    private void ComprobarInicioSesion() {
        if (user != null) {
            //El usuario ha iniciado sesión
            CargaDeDatos();
        } else {
            //Lo dirigirá al MainActivity
            startActivity(new Intent(MenuPrincipal.this, MainActivity.class));
            finish();
        }
    }

    private void CargaDeDatos() {
        VerificarEstadoDeCuenta();

        Usuarios.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    progressBarDatos.setVisibility(View.GONE);
                    Linear_Nombres.setVisibility(View.VISIBLE);
                    Linear_Correo.setVisibility(View.VISIBLE);
                    Linear_Verificacion.setVisibility(View.VISIBLE);

                    String nombres = "" + snapshot.child("nombres").getValue();
                    String correo = "" + snapshot.child("correo").getValue();

                    NombresPrincipal.setText(nombres);
                    CorreoPrincipal.setText(correo);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_principal, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void SalirAplicacion() {
        firebaseAuth.signOut();
        startActivity(new Intent(MenuPrincipal.this, MainActivity.class));
        Toast.makeText(this, "Cerraste sesión exitosamente", Toast.LENGTH_SHORT).show();
    }
}