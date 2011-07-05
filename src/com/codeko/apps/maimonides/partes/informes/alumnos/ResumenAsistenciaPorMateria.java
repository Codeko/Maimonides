package com.codeko.apps.maimonides.partes.informes.alumnos;

import com.codeko.apps.maimonides.elementos.Materia;
import com.codeko.swing.CdkAutoTabla;
import com.codeko.swing.CdkAutoTablaCol;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
@CdkAutoTabla(procesarPadre = true)
public class ResumenAsistenciaPorMateria extends ResumenAsistencia {
    @CdkAutoTablaCol(titulo = "Cod.")
    String nombre = "";
    @CdkAutoTablaCol(titulo = "Descripción")
    String descripcion = "";
    @CdkAutoTablaCol(ignorar = true)
    Materia materia = null;

    @CdkAutoTablaCol(titulo="Máximo")
    public int getMaxFaltas(){
        return getMateria().getMaxFaltas();
    }
    
    public Materia getMateria() {
        return materia;
    }

    public void setMateria(Materia materia) {
        this.materia = materia;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    protected void limpiar() {
        super.limpiar();
        setNombre("");
        setDescripcion("");
    }
}
