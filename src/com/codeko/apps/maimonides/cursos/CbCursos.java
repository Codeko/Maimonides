package com.codeko.apps.maimonides.cursos;

import com.codeko.apps.maimonides.elementos.Curso;
import com.codeko.apps.maimonides.elementos.Unidad;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.Beans;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComboBox;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class CbCursos extends JComboBox {

    CbGrupos comboGrupos = null;

    public CbCursos() {
        super();
        addItem("Todos");
        if (!Beans.isDesignTime()) {
            ArrayList<Curso> cursos = Curso.getCursos();
            for (Curso c : cursos) {
                addItem(c);
            }
        }
        addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (getComboGrupos() != null) {
                    Object item = getSelectedItem();
                    getComboGrupos().removeAllItems();
                    getComboGrupos().addItem("Todos");
                    if (item instanceof Curso) {
                        Curso c = (Curso) item;
                        for (Unidad u : c.getUnidades()) {
                            getComboGrupos().addItem(u);
                        }
                    }
                }
            }
        });
        setPrototypeDisplayValue("Esto es un ejemplo de curso");
    }

    public CbGrupos getComboGrupos() {
        return comboGrupos;
    }

    public void setComboGrupos(CbGrupos comboGrupos) {
        this.comboGrupos = comboGrupos;
    }

    public Curso getCurso() {
        return (getSelectedItem() instanceof Curso) ? (Curso) getSelectedItem() : null;
    }

    public void setUnidad(Unidad u) {
        Curso c = null;
        try {
            c = Curso.getCurso(u.getIdCurso());
        } catch (Exception ex) {
            Logger.getLogger(CbCursos.class.getName()).log(Level.SEVERE, null, ex);
        }
        setSelectedItem(c);
        if (getComboGrupos() != null) {
            getComboGrupos().setSelectedItem(u);
        }
    }
}
