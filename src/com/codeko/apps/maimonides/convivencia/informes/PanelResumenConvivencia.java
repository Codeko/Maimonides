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
 * PanelResumenConvivencia.java
 *
 * Created on 31-ago-2009, 14:05:18
 */
package com.codeko.apps.maimonides.convivencia.informes;

import com.codeko.apps.maimonides.IPanel;
import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesUtil;
import com.codeko.apps.maimonides.convivencia.TipoConducta;
import com.codeko.apps.maimonides.elementos.Curso;
import com.codeko.apps.maimonides.elementos.Profesor;
import com.codeko.apps.maimonides.elementos.Unidad;
import com.codeko.apps.maimonides.usr.Permisos;
import com.codeko.apps.maimonides.usr.Rol;
import com.codeko.swing.CodekoAutoTableModel;
import com.codeko.util.Fechas;
import java.awt.Point;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;

/**
 *
 * @author Codeko
 */
public class PanelResumenConvivencia extends javax.swing.JPanel implements IPanel {

    CodekoAutoTableModel<DatoResumenConvivencia> modelo = new CodekoAutoTableModel<DatoResumenConvivencia>(DatoResumenConvivencia.class);

    /** Creates new form PanelResumenConvivencia */
    public PanelResumenConvivencia() {
        initComponents();
        MaimonidesUtil.setFormatosFecha(tfFechaDesde, false);
        MaimonidesUtil.addMenuTabla(tabla, "Resumen de convivencia");
        cbTipoParte.addItem("Todos");
        cbTipoParte.addItem(TipoConducta.getNombreGravedad(TipoConducta.GRAVEDAD_LEVE));
        cbTipoParte.addItem(TipoConducta.getNombreGravedad(TipoConducta.GRAVEDAD_GRAVE));

        MaimonidesUtil.setFormatosFecha(tfFechaDesde, false);
        MaimonidesUtil.setFormatosFecha(tfFechaHasta, false);
        if (Permisos.isUsuarioSoloProfesor()) {
            Unidad u = Permisos.getFiltroUnidad();
            if (u != null) {
                cbCursos1.setUnidad(u);
                cbCursos1.setEnabled(false);
                cbGrupos1.setEnabled(false);
            }
            //Si no es tutor sólo el dejamos acceder a sus partes
            if (!Permisos.isRol(Rol.ROL_TUTOR)) {
                Profesor p = Permisos.getFiltroProfesor();
                cbProfesores.setSelectedItem(p);
                cbProfesores.setEnabled(false);
            }
        }
    }

    @Action(block = Task.BlockingScope.WINDOW)
    public Task actualizar() {
        return new ActualizarTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    @Override
    public boolean puedoSusituir() {
        return true;
    }

    private class ActualizarTask extends org.jdesktop.application.Task<ArrayList<DatoResumenConvivencia>, Void> {

        GregorianCalendar fechaDesde = null;
        GregorianCalendar fechaHasta = null;
        Profesor profesor = null;

        ActualizarTask(org.jdesktop.application.Application app) {
            super(app);
            modelo.vaciar();
            try {
                tfFechaDesde.commitEdit();
            } catch (ParseException ex) {
                Logger.getLogger(PanelResumenConvivencia.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                tfFechaHasta.commitEdit();
            } catch (ParseException ex) {
                Logger.getLogger(PanelResumenConvivencia.class.getName()).log(Level.SEVERE, null, ex);
            }
            fechaDesde = Fechas.toGregorianCalendar(tfFechaDesde.getDate());
            fechaHasta = Fechas.toGregorianCalendar(tfFechaHasta.getDate());
            profesor = cbProfesores.getProfesor();
        }

        @Override
        protected ArrayList<DatoResumenConvivencia> doInBackground() {
            setMessage("Cargando datos...");
            Unidad u = cbGrupos1.getUnidad();
            Curso c = cbCursos1.getCurso();
            Integer tipo = null;

            if (cbTipoParte.getSelectedIndex() > 0) {
                tipo = cbTipoParte.getSelectedIndex();
            }
            return DatoResumenConvivencia.getDatosResumenConvivencia(fechaDesde, fechaHasta, c, u, profesor, tipo, panelConductasParte1.getConductas());
        }

        @Override
        protected void succeeded(ArrayList<DatoResumenConvivencia> result) {
            modelo.setDatos(result);
            tabla.packAll();
            setMessage("Datos cargados correctamente.");
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelCentral = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabla = new org.jdesktop.swingx.JXTable();
        panelFiltro = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        tfFechaDesde = new org.jdesktop.swingx.JXDatePicker();
        jLabel3 = new javax.swing.JLabel();
        cbTipoParte = new javax.swing.JComboBox();
        tfFechaHasta = new org.jdesktop.swingx.JXDatePicker();
        jLabel1 = new javax.swing.JLabel();
        panelConductasParte1 = new com.codeko.apps.maimonides.convivencia.PanelConductasParte();
        lProfesor = new javax.swing.JLabel();
        cbProfesores = new com.codeko.apps.maimonides.profesores.CbProfesores();
        jLabel4 = new javax.swing.JLabel();
        cbCursos1 = new com.codeko.apps.maimonides.cursos.CbCursos();
        cbGrupos1 = new com.codeko.apps.maimonides.cursos.CbGrupos();
        jToolBar1 = new javax.swing.JToolBar();
        bActualizar = new javax.swing.JButton();

        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        panelCentral.setName("panelCentral"); // NOI18N
        panelCentral.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        tabla.setModel(modelo);
        tabla.setColumnControlVisible(true);
        tabla.setName("tabla"); // NOI18N
        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tablaMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tabla);

        panelCentral.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        panelFiltro.setName("panelFiltro"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getResourceMap(PanelResumenConvivencia.class);
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        tfFechaDesde.setName("tfFechaDesde"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        cbTipoParte.setName("cbTipoParte"); // NOI18N

        tfFechaHasta.setName("tfFechaHasta"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        panelConductasParte1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("panelConductasParte1.border.title"))); // NOI18N
        panelConductasParte1.setName("panelConductasParte1"); // NOI18N

        lProfesor.setText(resourceMap.getString("lProfesor.text")); // NOI18N
        lProfesor.setName("lProfesor"); // NOI18N

        cbProfesores.setName("cbProfesores"); // NOI18N

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        cbCursos1.setComboGrupos(cbGrupos1);
        cbCursos1.setName("cbCursos1"); // NOI18N

        cbGrupos1.setName("cbGrupos1"); // NOI18N

        javax.swing.GroupLayout panelFiltroLayout = new javax.swing.GroupLayout(panelFiltro);
        panelFiltro.setLayout(panelFiltroLayout);
        panelFiltroLayout.setHorizontalGroup(
            panelFiltroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFiltroLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelFiltroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lProfesor)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelFiltroLayout.createSequentialGroup()
                        .addGroup(panelFiltroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelFiltroLayout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addGap(18, 18, 18)
                                .addComponent(cbCursos1, javax.swing.GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE))
                            .addGroup(panelFiltroLayout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(18, 18, 18)
                                .addComponent(tfFechaDesde, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel1)
                                .addGap(4, 4, 4)
                                .addComponent(tfFechaHasta, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbGrupos1, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelFiltroLayout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelFiltroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbProfesores, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 373, Short.MAX_VALUE)
                            .addComponent(cbTipoParte, 0, 262, Short.MAX_VALUE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelConductasParte1, javax.swing.GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE))
        );
        panelFiltroLayout.setVerticalGroup(
            panelFiltroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFiltroLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelFiltroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(cbGrupos1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbCursos1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelFiltroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1)
                    .addComponent(tfFechaHasta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfFechaDesde, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelFiltroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(cbTipoParte, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelFiltroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lProfesor)
                    .addComponent(cbProfesores, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(75, Short.MAX_VALUE))
            .addComponent(panelConductasParte1, javax.swing.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)
        );

        panelCentral.add(panelFiltro, java.awt.BorderLayout.PAGE_START);

        add(panelCentral, java.awt.BorderLayout.CENTER);

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        jToolBar1.setName("jToolBar1"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getActionMap(PanelResumenConvivencia.class, this);
        bActualizar.setAction(actionMap.get("actualizar")); // NOI18N
        bActualizar.setFocusable(false);
        bActualizar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bActualizar.setName("bActualizar"); // NOI18N
        bActualizar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(bActualizar);

        add(jToolBar1, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

    private void tablaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tablaMouseClicked
        if (evt.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(evt)) {
            //tenemos que ver la fila donde se ha hecho clic
            Point p = evt.getPoint();
            int row = tabla.rowAtPoint(p);
            if (row > -1) {
                row = tabla.convertRowIndexToModel(row);
                DatoResumenConvivencia drc = modelo.getElemento(row);
                if (drc != null) {
                    MaimonidesApp.getMaimonidesView().mostrarFichaAlumno(drc.getAlumno());
                }
            }
        }
    }//GEN-LAST:event_tablaMouseClicked
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bActualizar;
    private com.codeko.apps.maimonides.cursos.CbCursos cbCursos1;
    private com.codeko.apps.maimonides.cursos.CbGrupos cbGrupos1;
    private com.codeko.apps.maimonides.profesores.CbProfesores cbProfesores;
    private javax.swing.JComboBox cbTipoParte;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JLabel lProfesor;
    private javax.swing.JPanel panelCentral;
    private com.codeko.apps.maimonides.convivencia.PanelConductasParte panelConductasParte1;
    private javax.swing.JPanel panelFiltro;
    private org.jdesktop.swingx.JXTable tabla;
    private org.jdesktop.swingx.JXDatePicker tfFechaDesde;
    private org.jdesktop.swingx.JXDatePicker tfFechaHasta;
    // End of variables declaration//GEN-END:variables
}
