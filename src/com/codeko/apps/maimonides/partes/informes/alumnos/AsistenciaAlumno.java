package com.codeko.apps.maimonides.partes.informes.alumnos;

import com.codeko.apps.maimonides.elementos.Alumno;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class AsistenciaAlumno {

    Alumno alumno = null;
    ResumenAsistencia resumen = null;

    public AsistenciaAlumno(Alumno alumno, ResumenAsistencia resumen) {
        setAlumno(alumno);
        setResumen(resumen);
    }

    public Alumno getAlumno() {
        return alumno;
    }

    public void setAlumno(Alumno alumno) {
        this.alumno = alumno;
    }

    public ResumenAsistencia getResumen() {
        return resumen;
    }

    public void setResumen(ResumenAsistencia resumen) {
        this.resumen = resumen;
    }
}
