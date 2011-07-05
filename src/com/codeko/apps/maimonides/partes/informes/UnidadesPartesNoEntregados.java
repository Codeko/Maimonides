package com.codeko.apps.maimonides.partes.informes;

import com.codeko.apps.maimonides.elementos.Unidad;
import com.codeko.swing.IObjetoTabla;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class UnidadesPartesNoEntregados implements IObjetoTabla {

    int partesNoFirmados = 0;
    Unidad unidad = null;

    public Unidad getUnidad() {
        return unidad;
    }

    public void setUnidad(Unidad curso) {
        this.unidad = curso;
    }

    public UnidadesPartesNoEntregados() {
    }

    public UnidadesPartesNoEntregados(Unidad c, int partesNoFirmados) {
        setUnidad(c);
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
            val = getUnidad().getCursoGrupo();
        }
        return val;
    }

    @Override
    public String getTitleAt(int index) {
        String val = "Nº Partes";
        if (index != 0) {
            val = "Unidad";
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
