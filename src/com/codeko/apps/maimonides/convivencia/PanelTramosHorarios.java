/**
 *  Maimónides, gestión para centros escolares.
 *  Copyright Codeko and individual contributors
 *  as indicated by the @author tags.
 * 
 *  This is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as
 *  published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 * 
 *  This software is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public
 *  License along with this software; if not, write to the Free
 *  Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *  
 *  For more information:
 *  maimonides@codeko.com
 *  http://codeko.com/maimonides
**/


/*
 * PanelTipos.java
 *
 * Created on 13-ago-2009, 13:19:03
 */
package com.codeko.apps.maimonides.convivencia;

import com.codeko.apps.maimonides.MaimonidesUtil;
import com.codeko.swing.CodekoAutoTableModel;
import java.beans.Beans;
import java.util.ArrayList;
import javax.swing.DefaultComboBoxModel;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;

/**
 *
 * @author Codeko
 */
public class PanelTramosHorarios extends javax.swing.JPanel {

    CodekoAutoTableModel<TramoHorario> modelo = new CodekoAutoTableModel<TramoHorario>(TramoHorario.class) {

        @Override
        public void elementoModificado(TramoHorario elemento, int col, Object valor) {
            elemento.guardar();
        }
    };

    /** Creates new form PanelTipos */
    public PanelTramosHorarios() {
        initComponents();
        tabla.getColumnExt("Código").setVisible(false);
        if (!Beans.isDesignTime()) {
            DefaultComboBoxModel modeloTramos = new DefaultComboBoxModel(TramoHorario.getTramosHorarios().toArray());
            cbTramosHorarios.setModel(modeloTramos);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        barraHerramientas = new javax.swing.JToolBar();
        bActualizar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabla = new org.jdesktop.swingx.JXTable();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        cbTramosHorarios = new javax.swing.JComboBox();
        bGuardar = new javax.swing.JButton();

        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        barraHerramientas.setFloatable(false);
        barraHerramientas.setRollover(true);
        barraHerramientas.setName("barraHerramientas"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getActionMap(PanelTramosHorarios.class, this);
        bActualizar.setAction(actionMap.get("actualizar")); // NOI18N
        bActualizar.setFocusable(false);
        bActualizar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bActualizar.setName("bActualizar"); // NOI18N
        bActualizar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraHerramientas.add(bActualizar);

        add(barraHerramientas, java.awt.BorderLayout.NORTH);

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        tabla.setModel(modelo);
        tabla.setColumnControlVisible(true);
        tabla.setName("tabla"); // NOI18N
        tabla.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                tablaAncestorAdded(evt);
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });
        jScrollPane1.setViewportView(tabla);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getResourceMap(PanelTramosHorarios.class);
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N
        jPanel1.add(jLabel1);

        cbTramosHorarios.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbTramosHorarios.setName("cbTramosHorarios"); // NOI18N
        jPanel1.add(cbTramosHorarios);

        bGuardar.setAction(actionMap.get("guardar")); // NOI18N
        bGuardar.setText(resourceMap.getString("bGuardar.text")); // NOI18N
        bGuardar.setName("bGuardar"); // NOI18N
        jPanel1.add(bGuardar);

        add(jPanel1, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void tablaAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_tablaAncestorAdded
        if (!Beans.isDesignTime()) {
            MaimonidesUtil.ejecutarTask(this, "actualizar");
        }
    }//GEN-LAST:event_tablaAncestorAdded

    @Action(block = Task.BlockingScope.WINDOW)
    public Task actualizar() {
        return new ActualizarTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class ActualizarTask extends org.jdesktop.application.Task<ArrayList<TramoHorario>, Void> {

        ActualizarTask(org.jdesktop.application.Application app) {
            super(app);
            //Primero vaciamos el modelo
            setMessage("Limpiando datos...");
            modelo.vaciar();
        }

        @Override
        protected ArrayList<TramoHorario> doInBackground() {
            setMessage("Cargando datos...");
            return TramoHorario.getTramosHorarios();
        }

        @Override
        protected void succeeded(ArrayList<TramoHorario> result) {
            modelo.addDatos(result);
            tabla.packAll();
            setMessage("Datos cargados correctamente.");
        }
    }

    @Action(block = Task.BlockingScope.ACTION)
    public Task guardar() {
        return new GuardarTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class GuardarTask extends org.jdesktop.application.Task<Object, Void> {

        GuardarTask(org.jdesktop.application.Application app) {
            super(app);
        }

        @Override
        protected Object doInBackground() {
            TramoHorario th = null;
            Object selObj = cbTramosHorarios.getSelectedItem();
            if (selObj instanceof TramoHorario) {
                th = (TramoHorario) selObj;
            }
            TramoHorario.setDefaultTramoHorario(th);
            return null;
        }

        @Override
        protected void succeeded(Object result) {
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bActualizar;
    private javax.swing.JButton bGuardar;
    private javax.swing.JToolBar barraHerramientas;
    private javax.swing.JComboBox cbTramosHorarios;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private org.jdesktop.swingx.JXTable tabla;
    // End of variables declaration//GEN-END:variables
}
