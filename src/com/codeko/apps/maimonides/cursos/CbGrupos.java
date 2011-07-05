package com.codeko.apps.maimonides.cursos;

import com.codeko.apps.maimonides.elementos.Unidad;
import javax.swing.JComboBox;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class CbGrupos extends JComboBox {

    public CbGrupos() {
        super();
        addItem("Todos");
    }

    public Unidad getUnidad() {
        return (getSelectedItem() instanceof Unidad) ? (Unidad) getSelectedItem() : null;
    }
}
