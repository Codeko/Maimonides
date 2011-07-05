package com.codeko.apps.maimonides.web;

import com.codeko.util.Fechas;
import java.util.GregorianCalendar;

/**
 *
 * @author codeko
 */
public class FaltaWeb {

    int id = 0;
    int hora = 0;
    String fecha = "";
    int asistencia = 0;
    String materia = "";
    String descripcion = "";

    public FaltaWeb(int alumnoId, GregorianCalendar fecha, int hora, int asistencia, String codigoMateriaActividad, String descripcion) {
        setId(alumnoId);
        setFecha(Fechas.format(fecha, "yyyyMMdd"));
        setHora(hora);
        setAsistencia(asistencia);
        setMateria(codigoMateriaActividad);
        setDescripcion(descripcion);
    }

    public int getAsistencia() {
        return asistencia;
    }

    public void setAsistencia(int asistencia) {
        this.asistencia = asistencia;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public int getHora() {
        return hora;
    }

    public void setHora(int hora) {
        this.hora = hora;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMateria() {
        return materia;
    }

    public void setMateria(String materia) {
        this.materia = materia;
    }
}
