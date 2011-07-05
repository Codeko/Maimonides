package com.codeko.apps.maimonides.partes.cartas;

import com.codeko.apps.maimonides.alumnos.IAlumno;
import com.codeko.apps.maimonides.asistencia.escolaridad.DatoPerdidaEscolaridadPorMaterias;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.Curso;
import com.codeko.apps.maimonides.elementos.Materia;
import com.codeko.apps.maimonides.elementos.Unidad;
import com.codeko.swing.CdkAutoTablaCol;
import java.util.Vector;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class DatoCartaPerdidaEscolaridadPorMaterias implements IAlumno {

    Alumno alumno = null;
    Vector<DatoPerdidaEscolaridadPorMaterias> datos = new Vector<DatoPerdidaEscolaridadPorMaterias>();

    public DatoCartaPerdidaEscolaridadPorMaterias(Alumno alumno) {
        setAlumno(alumno);
    }

    public void addDato(DatoPerdidaEscolaridadPorMaterias dato) {
        datos.add(dato);
    }

    @Override
    public Alumno getAlumno() {
        return alumno;
    }

    public void setAlumno(Alumno alumno) {
        this.alumno = alumno;
    }

    @CdkAutoTablaCol(titulo = "Curso")
    public Curso getCurso() {
        return getAlumno().getObjetoCurso();
    }

    @CdkAutoTablaCol(titulo = "Unidad")
    public Unidad getUnidad() {
        return getAlumno().getUnidad();
    }

    @CdkAutoTablaCol(titulo = "Materias")
    public String getMaterias() {
        StringBuilder sb = new StringBuilder("<html>");
        boolean primero = true;
        for (DatoPerdidaEscolaridadPorMaterias dc : datos) {
            if (!primero) {
                sb.append(" / ");
            }
            if(dc.isNotificado()){
                sb.append("<b>");
            }
            sb.append(dc.getMateria().toString());
            if(dc.isNotificado()){
                sb.append("</b>");
            }
            primero = false;
        }
        return sb.toString();
    }
    @CdkAutoTablaCol(titulo = "Notificado")
    public String getNotificado(){
        boolean haySi=false;
        boolean hayNo=false;
        String not="";
        for (DatoPerdidaEscolaridadPorMaterias dc : datos) {
            if(dc.isNotificado()){
                haySi=true;
            }else{
                hayNo=true;
            }
        }
        if(haySi && !hayNo){
            not="Si";
        }else if(haySi && hayNo){
            not="Parcialmente";
        }else{
            not="No";
        }
        return not;
    }

    public Vector<Materia> getVectorMaterias() {
        Vector<Materia> m = new Vector<Materia>();
        for (DatoPerdidaEscolaridadPorMaterias dc : datos) {
            m.add(dc.getMateria());
        }
        return m;
    }
}
