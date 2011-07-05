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
 * PanelConfiguracionConvivencia.java
 *
 * Created on 13-ago-2009, 13:18:52
 */
package com.codeko.apps.maimonides.convivencia;

import com.codeko.apps.maimonides.IPanel;
import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.seneca.ClienteSeneca;
import com.codeko.apps.maimonides.seneca.GestorUsuarioClaveSeneca;
import com.codeko.apps.maimonides.seneca.operaciones.convivencia.GestorConvivenciaSeneca;
import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;

/**
 *
 * @author Codeko
 */
public class PanelConfiguracionConvivencia extends javax.swing.JPanel implements IPanel {

    /** Creates new form PanelConfiguracionConvivencia */
    public PanelConfiguracionConvivencia() {
        initComponents();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pGeneral = new javax.swing.JPanel();
        panelTipos1 = new com.codeko.apps.maimonides.convivencia.PanelTipos(TipoConducta.TIPO_CONDUCTA);
        panelTipos2 = new com.codeko.apps.maimonides.convivencia.PanelTipos(TipoConducta.TIPO_MEDIDA);
        panelConductas1 = new com.codeko.apps.maimonides.convivencia.PanelConductas(TipoConducta.TIPO_CONDUCTA);
        panelConductas2 = new com.codeko.apps.maimonides.convivencia.PanelConductas(TipoConducta.TIPO_MEDIDA);
        barraHerramientas = new javax.swing.JToolBar();
        bActualizarSeneca = new javax.swing.JButton();
        bTramosHorarios = new javax.swing.JButton();

        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        pGeneral.setName("pGeneral"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getResourceMap(PanelConfiguracionConvivencia.class);
        panelTipos1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("panelTipos1.border.title"))); // NOI18N
        panelTipos1.setModoCompacto(true);
        panelTipos1.setName("panelTipos1"); // NOI18N

        panelTipos2.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("panelTipos2.border.title"))); // NOI18N
        panelTipos2.setModoCompacto(true);
        panelTipos2.setName("panelTipos2"); // NOI18N

        panelConductas1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("panelConductas1.border.title"))); // NOI18N
        panelConductas1.setModoCompacto(true);
        panelConductas1.setName("panelConductas1"); // NOI18N

        panelConductas2.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("panelConductas2.border.title"))); // NOI18N
        panelConductas2.setModoCompacto(true);
        panelConductas2.setName("panelConductas2"); // NOI18N

        javax.swing.GroupLayout pGeneralLayout = new javax.swing.GroupLayout(pGeneral);
        pGeneral.setLayout(pGeneralLayout);
        pGeneralLayout.setHorizontalGroup(
            pGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pGeneralLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(panelTipos1, javax.swing.GroupLayout.DEFAULT_SIZE, 285, Short.MAX_VALUE)
                    .addComponent(panelConductas1, javax.swing.GroupLayout.DEFAULT_SIZE, 285, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pGeneralLayout.createSequentialGroup()
                        .addComponent(panelTipos2, javax.swing.GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE)
                        .addGap(10, 10, 10))
                    .addGroup(pGeneralLayout.createSequentialGroup()
                        .addComponent(panelConductas2, javax.swing.GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        pGeneralLayout.setVerticalGroup(
            pGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pGeneralLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(panelTipos1, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(panelTipos2, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelConductas2, 0, 0, Short.MAX_VALUE)
                    .addComponent(panelConductas1, javax.swing.GroupLayout.DEFAULT_SIZE, 306, Short.MAX_VALUE))
                .addGap(29, 29, 29))
        );

        add(pGeneral, java.awt.BorderLayout.CENTER);

        barraHerramientas.setFloatable(false);
        barraHerramientas.setRollover(true);
        barraHerramientas.setName("barraHerramientas"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getActionMap(PanelConfiguracionConvivencia.class, this);
        bActualizarSeneca.setAction(actionMap.get("actualizarDesdeSeneca")); // NOI18N
        bActualizarSeneca.setFocusable(false);
        bActualizarSeneca.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bActualizarSeneca.setName("bActualizarSeneca"); // NOI18N
        bActualizarSeneca.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraHerramientas.add(bActualizarSeneca);

        bTramosHorarios.setAction(actionMap.get("editarTramosHorarios")); // NOI18N
        bTramosHorarios.setFocusable(false);
        bTramosHorarios.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bTramosHorarios.setName("bTramosHorarios"); // NOI18N
        bTramosHorarios.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraHerramientas.add(bTramosHorarios);

        add(barraHerramientas, java.awt.BorderLayout.PAGE_START);
    }// </editor-fold>//GEN-END:initComponents

    @Override
    public boolean puedoSusituir() {
        return true;
    }

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task actualizarDesdeSeneca() {
        return new ActualizarDesdeSenecaTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class ActualizarDesdeSenecaTask extends org.jdesktop.application.Task<Object, Void> {

        ActualizarDesdeSenecaTask(org.jdesktop.application.Application app) {
            super(app);
            if (!GestorUsuarioClaveSeneca.getGestor().pedirUsuarioClave()) {
                cancel(false);
            }
        }

        @Override
        protected Object doInBackground() {
            String retorno = "Ha ocurrido algún error recuperando los datos.";
            ClienteSeneca cli = new ClienteSeneca(GestorUsuarioClaveSeneca.getGestor().getUsuario(), GestorUsuarioClaveSeneca.getGestor().getClave());
            cli.setDebugMode(MaimonidesApp.isDebug());
            cli.addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                }
            });
            GestorConvivenciaSeneca gestorConv = new GestorConvivenciaSeneca(cli);
            try {
                if (gestorConv.recuperarDatosConvivenciaSeneca()) {
                    retorno = "Datos recuperados correctamente desde Séneca";
                }
            } catch (IOException ex) {
                Logger.getLogger(PanelConfiguracionConvivencia.class.getName()).log(Level.SEVERE, null, ex);
            }
            return retorno;
        }

        @Override
        protected void succeeded(Object result) {
            setMessage(result.toString());
            JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), result, "Actualización terminada", JOptionPane.INFORMATION_MESSAGE);
            //Ahora tenemos que actualizar los distintos paneles
            MaimonidesApp.getApplication().getContext().getTaskService().execute(panelConductas1.actualizar());
            MaimonidesApp.getApplication().getContext().getTaskService().execute(panelConductas2.actualizar());
            MaimonidesApp.getApplication().getContext().getTaskService().execute(panelTipos1.actualizar());
            MaimonidesApp.getApplication().getContext().getTaskService().execute(panelTipos2.actualizar());
        }
    }

    @Action
    public void editarTramosHorarios() {
        PanelTramosHorarios panel = new PanelTramosHorarios();
        JDialog dlg = new JDialog(MaimonidesApp.getApplication().getMainFrame(), "Tramos horarios", true);
        dlg.setName("convivencia_editor_tramos_horarios");
        dlg.add(panel, BorderLayout.CENTER);
        MaimonidesApp.getApplication().show(dlg);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bActualizarSeneca;
    private javax.swing.JButton bTramosHorarios;
    private javax.swing.JToolBar barraHerramientas;
    private javax.swing.JPanel pGeneral;
    private com.codeko.apps.maimonides.convivencia.PanelConductas panelConductas1;
    private com.codeko.apps.maimonides.convivencia.PanelConductas panelConductas2;
    private com.codeko.apps.maimonides.convivencia.PanelTipos panelTipos1;
    private com.codeko.apps.maimonides.convivencia.PanelTipos panelTipos2;
    // End of variables declaration//GEN-END:variables
}
