package com.redsystem.agendaonline.ViewHolder;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.redsystem.agendaonline.R;

public class ViewHolder_Tarea extends RecyclerView.ViewHolder {

    View mView;

    private ViewHolder_Tarea.ClickListener mClickListener;

    public interface ClickListener{
        void onItemClick(View view, int position); /*SE EJECUTA AL PRESIONAR EN EL ITEM*/
        void onItemLongClick(View view, int position); /*SE EJECUTA AL MANTENER PRESIONADO EN EL ITEM*/
    }

    public void setOnClickListener(ViewHolder_Tarea.ClickListener clickListener){
        mClickListener = clickListener;
    }

    public ViewHolder_Tarea(@NonNull View itemView) {
        super(itemView);
        mView = itemView;

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClickListener.onItemClick(view, getBindingAdapterPosition());
            }
        });

        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mClickListener.onItemLongClick(view, getBindingAdapterPosition());
                return false;
            }
        });
    }

    public void SetearDatos(Context context, String id_tarea , String uid_usuario,  String correo_usuario,
                            String fecha_hora_registro, String titulo, String descripcion, String fecha_tarea,
                            String estado, String asignatura){

        //DECLARAR LAS VISTAS
        TextView Id_tarea_Item, Uid_Usuario_Item, Correo_usuario_Item,Fecha_hora_registro_Item,Titulo_Item,
                Descripcion_Item, Fecha_Item, Estado_Item, Asignatura_Item;

        ImageView Tarea_Finalizada_Item, Tarea_No_Finalizada_Item;

        //ESTABLECER LA CONEXIÓN CON EL ITEM
        Id_tarea_Item = mView.findViewById(R.id.Id_tarea_Item);
        Uid_Usuario_Item = mView.findViewById(R.id.Uid_Usuario_Item);
        Correo_usuario_Item = mView.findViewById(R.id.Correo_usuario_Item);
        Fecha_hora_registro_Item = mView.findViewById(R.id.Fecha_hora_registro_Item);
        Titulo_Item = mView.findViewById(R.id.Titulo_Item);
        Descripcion_Item = mView.findViewById(R.id.Descripcion_Item);
        Fecha_Item = mView.findViewById(R.id.Fecha_Item);
        Estado_Item = mView.findViewById(R.id.Estado_Item);
        Asignatura_Item = mView.findViewById(R.id.Asignatura_Item);

        Tarea_Finalizada_Item = mView.findViewById(R.id.Tarea_Finalizada_Item);
        Tarea_No_Finalizada_Item = mView.findViewById(R.id.Tarea_No_Finalizada_Item);

        //SETEAR LA INFORMACIÓN DENTRO DEL ITEM
        Id_tarea_Item.setText(id_tarea);
        Uid_Usuario_Item.setText(uid_usuario);
        Correo_usuario_Item.setText(correo_usuario);
        Fecha_hora_registro_Item.setText(fecha_hora_registro);
        Titulo_Item.setText(titulo);
        Descripcion_Item.setText(descripcion);
        Fecha_Item.setText(fecha_tarea);
        Estado_Item.setText(estado);
        Asignatura_Item.setText(asignatura);

        //GESTIONAMOS EL COLOR DEL ESTADO
        if (estado.equals("Finalizada")){
            Tarea_Finalizada_Item.setVisibility(View.VISIBLE);
        }else {
            Tarea_No_Finalizada_Item.setVisibility(View.VISIBLE);
        }
    }
}
