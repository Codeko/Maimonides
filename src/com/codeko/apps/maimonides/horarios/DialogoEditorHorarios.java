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
 * DialogoEditorHorarios.java
 *
 * Created on 20-abr-2009, 13:50:06
 */
package com.codeko.apps.maimonides.horarios;

import com.codeko.apps.maimonides.MaimonidesApp;
import java.awt.BorderLayout;

/**
 *
 * @author Codeko
 */
public class DialogoEditorHorarios extends javax.swing.JDialog {

    PanelVisionHorario padre = null;
    PanelEditorBloqueHorario panelEditorBloqueHorario1=null;
    public DialogoEditorHorarios(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.setName("editorBloquesHorarios");
    }

    public DialogoEditorHorarios(PanelVisionHorario padre, BloqueHorario bloque) {
        super(MaimonidesApp.getApplication().getMainFrame(), true);
        setPadre(padre);
        initComponents();
        panelEditorBloqueHorario1=new PanelEditorBloqueHorario(padre);
        add(panelEditorBloqueHorario1,BorderLayout.CENTER);
        this.setName("editorBloquesHorarios");
        getEditor().setPanelVisor(padre);
        getEditor().setBloqueHorario(bloque);

    }

    public final PanelEditorBloqueHorario getEditor() {
        return panelEditorBloqueHorario1;
    }

    public PanelVisionHorario getPadre() {
        return padre;
    }

    public final void setPadre(PanelVisionHorario padre) {
        this.padre = padre;
    }

    public void mostrar() {
        this.pack();
        MaimonidesApp.getApplication().show(this);
    }

   
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setName("Form"); // NOI18N

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
