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
 * PanelPestanaConvivencia.java
 *
 * Created on 25-ago-2009, 18:14:08
 */
package com.codeko.apps.maimonides.convivencia;

import com.codeko.apps.maimonides.ICargable;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.util.CTiempo;
import java.beans.Beans;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 *
 * @author Codeko
 */
public class PanelPestanaConvivencia extends javax.swing.JPanel implements ICargable {

    boolean cargado = false;
    Alumno alumno = null;
    boolean splitPosicionado = false;

    /** Creates new form PanelPestanaConvivencia */
    public PanelPestanaConvivencia() {
        if (!Beans.isDesignTime()) {
            CTiempo t = new CTiempo("P CONV");
            initComponents();
            t.showTimer("INIT");
            panelPartes1.addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if ("nuevoParteCreado".equals(evt.getPropertyName())) {
                        panelListaPartesConvivencia1.addParte((ParteConvivencia) evt.getNewValue());
                    } else if ("parteBorrado".equals(evt.getPropertyName())) {
                        panelListaPartesConvivencia1.quitarParte((ParteConvivencia) evt.getNewValue());
                    }
                }
            });
            panelListaPartesConvivencia1.addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if ("parteSeleccionado".equals(evt.getPropertyName()) && evt.getNewValue() instanceof ParteConvivencia) {
                        panelPartes1.setParte((ParteConvivencia) evt.getNewValue());
                    }
                }
            });

            panelExpulsiones1.addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if ("seleccionadaExpulsion".equals(evt.getPropertyName())) {
                        panelListaPartesConvivencia1.setFiltroPartes((Expulsion) evt.getNewValue());
                    }
                }
            });
            t.showTimer("PL");
        }
    }

    public Alumno getAlumno() {
        return alumno;
    }

    public void setAlumno(Alumno alumno) {
        this.alumno = alumno;
        setCargado(false);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelPartes1 = new com.codeko.apps.maimonides.convivencia.PanelPartes();
        split = new javax.swing.JSplitPane();
        panelExpulsiones1 = new com.codeko.apps.maimonides.convivencia.PanelExpulsiones();
        panelListaPartesConvivencia1 = new com.codeko.apps.maimonides.convivencia.PanelListaPartesConvivencia();

        setName("Form"); // NOI18N
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                formComponentShown(evt);
            }
        });
        setLayout(new java.awt.BorderLayout());

        panelPartes1.setName("panelPartes1"); // NOI18N
        panelPartes1.setSelectorAlumnoVisible(false);
        panelPartes1.setModoCompacto();
        add(panelPartes1, java.awt.BorderLayout.CENTER);

        split.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        split.setName("split"); // NOI18N
        split.setOneTouchExpandable(true);

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getResourceMap(PanelPestanaConvivencia.class);
        panelExpulsiones1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("panelExpulsiones1.border.title"))); // NOI18N
        panelExpulsiones1.setDesmarcarVisible(true);
        panelExpulsiones1.setName("panelExpulsiones1"); // NOI18N
        panelExpulsiones1.setPreferredSize(new java.awt.Dimension(150, 100));
        panelExpulsiones1.setSelectorAlumnoVisible(false);
        panelExpulsiones1.setModoCompacto();
        split.setLeftComponent(panelExpulsiones1);

        panelListaPartesConvivencia1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("panelListaPartesConvivencia1.border.title"))); // NOI18N
        panelListaPartesConvivencia1.setModoFichaAlumno(true);
        panelListaPartesConvivencia1.setName("panelListaPartesConvivencia1"); // NOI18N
        panelListaPartesConvivencia1.setPreferredSize(new java.awt.Dimension(302, 200));
        split.setRightComponent(panelListaPartesConvivencia1);

        add(split, java.awt.BorderLayout.WEST);
    }// </editor-fold>//GEN-END:initComponents

    private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
        if (!splitPosicionado) {
            split.setDividerLocation(0.5d);
            splitPosicionado = true;
        }
        cargar();
    }//GEN-LAST:event_formComponentShown
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.codeko.apps.maimonides.convivencia.PanelExpulsiones panelExpulsiones1;
    private com.codeko.apps.maimonides.convivencia.PanelListaPartesConvivencia panelListaPartesConvivencia1;
    private com.codeko.apps.maimonides.convivencia.PanelPartes panelPartes1;
    private javax.swing.JSplitPane split;
    // End of variables declaration//GEN-END:variables

    @Override
    public void cargar() {
        if (!isCargado() && !Beans.isDesignTime()) {
            panelExpulsiones1.setAlumno(getAlumno());
            panelListaPartesConvivencia1.setAlumno(getAlumno());
            panelPartes1.setAlumnoFijo(getAlumno());
            setCargado(true);
        }
    }

    @Override
    public void vaciar() {
        panelExpulsiones1.setAlumno(null);
        panelListaPartesConvivencia1.setAlumno(null);
        panelPartes1.setAlumnoFijo(null);
        setCargado(false);
    }

    @Override
    public boolean isCargado() {
        return this.cargado;
    }

    @Override
    public void setCargado(boolean cargado) {
        this.cargado = cargado;
    }
}
