package com.codeko.apps.maimonides.alumnos;

import com.codeko.apps.maimonides.MaimonidesBean;
import com.codeko.swing.IObjetoTabla;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class ResultadoBusquedaAlumno extends MaimonidesBean implements IObjetoTabla {

    int id = 0;
    String nombre = "";
    String unidad = "";

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    @Override
    public int getNumeroDeCampos() {
        return 2;
    }

    @Override
    public Object getValueAt(int index) {
        Object val = null;
        switch (index) {
            case 0:
                val = getUnidad();
                break;
            case 1:
                val = getNombre();
                break;
        }
        return val;
    }

    @Override
    public String getTitleAt(int index) {
        String val = null;
        switch (index) {
            case 0:
                val = "Unidad";
                break;
            case 1:
                val = "Alumno";
                break;
        }
        return val;
    }

    @Override
    public Class getClassAt(int index) {
        return String.class;
    }

    @Override
    public boolean setValueAt(int index, Object valor) {
        return false;
    }

    @Override
    public boolean esCampoEditable(int index) {
        return false;
    }

    @Override
    public String toString() {
        return getNombre() + " [" + getUnidad() + "]";
    }
}
