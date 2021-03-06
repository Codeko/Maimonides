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
 * PanelInfoBloque.java
 *
 * Created on 29-abr-2009, 13:13:09
 */
package com.codeko.apps.maimonides.horarios;

import java.beans.Beans;

/**
 *
 * @author Codeko
 */
public class PanelInfoBloque extends javax.swing.JPanel {

    /** Creates new form PanelInfoBloque */
    public PanelInfoBloque() {
        initComponents();
        if(!Beans.isDesignTime()){
            limpiar();
        }
    }

    public void setBloque(BloqueHorario bloque) {
        limpiar();
        if (bloque != null) {
            if (bloque.getProfesor() != null) {
                lProfesor.setText(bloque.getProfesor().getDescripcionObjeto());
            }
            if (bloque.getDependencia() != null) {
                lAula.setText(bloque.getDependencia().getDescripcionObjeto());
            }
            if (bloque.getMateriasVirtuales().size() > 0) {
                if (bloque.getMateriasVirtuales().size() == 1) {
                    lMateria.setText(bloque.getMateriasVirtuales().get(0).getNombre());
                } else {
                    lMateria.setText(bloque.getMateriasVirtuales().get(0).getNombre() + " [...]");
                }
            } else if (bloque.getActividad() != null) {
                lMateria.setText(bloque.getActividad().getDescripcionObjeto());
            }
            if (bloque.isDicu()) {
                lMateria.setText(lMateria.getText() + " D.I.C.U.");
            }
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

        lProfesor = new javax.swing.JLabel();
        lAula = new javax.swing.JLabel();
        lMateria = new javax.swing.JLabel();

        setName("Form"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getResourceMap(PanelInfoBloque.class);
        lProfesor.setText(resourceMap.getString("lProfesor.text")); // NOI18N
        lProfesor.setName("lProfesor"); // NOI18N

        lAula.setText(resourceMap.getString("lAula.text")); // NOI18N
        lAula.setName("lAula"); // NOI18N

        lMateria.setText(resourceMap.getString("lMateria.text")); // NOI18N
        lMateria.setName("lMateria"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(lMateria, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(lProfesor)
                        .addGap(18, 18, 18)
                        .addComponent(lAula)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lProfesor)
                    .addComponent(lAula))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lMateria, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lAula;
    private javax.swing.JLabel lMateria;
    private javax.swing.JLabel lProfesor;
    // End of variables declaration//GEN-END:variables

    private void limpiar() {
        lAula.setText("");
        lMateria.setText("");
        lProfesor.setText("");
    }
}
