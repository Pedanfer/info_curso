package com.example.schooltools.Objetos;

public class Tarea {

    //Atributos con los que contará una NOTA
    String id_tarea, uid_usuario, correo_usuario, fecha_hora_actual, titulo, descripcion, fecha_tarea, estado, asignatura;

    public Tarea() {

    }

    public Tarea(String id_tarea, String uid_usuario, String correo_usuario, String fecha_hora_actual, String titulo, String descripcion, String fecha_tarea, String estado, String asignatura) {
        this.id_tarea = id_tarea;
        this.uid_usuario = uid_usuario;
        this.correo_usuario = correo_usuario;
        this.fecha_hora_actual = fecha_hora_actual;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fecha_tarea = fecha_tarea;
        this.estado = estado;
        this.asignatura = asignatura;
    }

    public String getId_tarea() {
        return id_tarea;
    }

    public void setId_tarea(String id_tarea) {
        this.id_tarea = id_tarea;
    }

    public String getAsignatura() {
        return asignatura;
    }

    public void setAsignatura(String asignatura) {
        this.asignatura = asignatura;
    }

    public String getUid_usuario() {
        return uid_usuario;
    }

    public void setUid_usuario(String uid_usuario) {
        this.uid_usuario = uid_usuario;
    }

    public String getCorreo_usuario() {
        return correo_usuario;
    }

    public void setCorreo_usuario(String correo_usuario) {
        this.correo_usuario = correo_usuario;
    }

    public String getFecha_hora_actual() {
        return fecha_hora_actual;
    }

    public void setFecha_hora_actual(String fecha_hora_actual) {
        this.fecha_hora_actual = fecha_hora_actual;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFecha_tarea() {
        return fecha_tarea;
    }

    public void setFecha_tarea(String fecha_tarea) {
        this.fecha_tarea = fecha_tarea;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
