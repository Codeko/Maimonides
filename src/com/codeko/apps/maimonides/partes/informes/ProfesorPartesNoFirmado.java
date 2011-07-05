package com.codeko.apps.maimonides.partes.informes;

import com.codeko.apps.maimonides.elementos.Profesor;
import com.codeko.swing.IObjetoTabla;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class ProfesorPartesNoFirmado implements IObjetoTabla {

    int partesNoFirmados = 0;
    Profesor profesor = null;

    public Profesor getProfesor() {
        return profesor;
    }

    public void setProfesor(Profesor profesor) {
        this.profesor = profesor;
    }

    public ProfesorPartesNoFirmado() {
    }

    public ProfesorPartesNoFirmado(Profesor p, int partesNoFirmados) {
        setProfesor(p);
        setPartesNoFirmados(partesNoFirmados);
    }

    public int getPartesNoFirmados() {
        return partesNoFirmados;
    }

    public void setPartesNoFirmados(int partesNoFirmados) {
        this.partesNoFirmados = partesNoFirmados;
    }

    @Override
    public int getNumeroDeCampos() {
        return 2;
    }

    @Override
    public Object getValueAt(int index) {
        Object val = getPartesNoFirmados();
        if (index != 0) {
            val = getProfesor().getDescripcionObjeto();
        }
        return val;
    }

    @Override
    public String getTitleAt(int index) {
        String val = "Nº Partes";
        if (index != 0) {
            val = "Profesor";
        }
        return val;
    }

    @Override
    public Class getClassAt(int index) {
        Class val = Integer.class;
        if (index != 0) {
            val = String.class;
        }
        return val;
    }

    @Override
    public boolean setValueAt(int index, Object val) {
        return false;
    }

    @Override
    public boolean esCampoEditable(int index) {
        return false;
    }
}
