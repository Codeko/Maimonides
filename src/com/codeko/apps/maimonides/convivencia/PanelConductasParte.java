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
 * PanelConductasParte.java
 *
 * Created on 14-ago-2009, 19:55:37
 */
package com.codeko.apps.maimonides.convivencia;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.swing.comboBox.AutoCompleteComboBoxDocument;
import com.codeko.util.Obj;
import com.mysql.jdbc.PreparedStatement;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.Beans;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.text.JTextComponent;
import org.jdesktop.application.Action;

/**
 *
 * @author Codeko
 */
public class PanelConductasParte extends javax.swing.JPanel {

    int tipo = TipoConducta.TIPO_CONDUCTA;
    DefaultListModel modeloLista = new DefaultListModel();
    JTextComponent editorProfesores = null;

    /** Creates new form PanelConductasParte */
    public PanelConductasParte() {
        initComponents();
        ini();
    }

    public PanelConductasParte(int tipo) {
        initComponents();
        setTipo(tipo);
        ini();
    }

    public JComboBox getCombo() {
        return cbConductas;
    }

    public int getTipo() {
        return tipo;
    }

    public final void setTipo(int tipo) {
        this.tipo = tipo;
    }

    public void limpiar() {
        modeloLista.removeAllElements();
    }

    public void cargar(ArrayList<Conducta> conductas) {
        for (Conducta c : conductas) {
            modeloLista.addElement(c);
        }
    }

    public ArrayList<Conducta> getConductas() {
        ArrayList<Conducta> cond = new ArrayList<Conducta>();
        for (Object o : modeloLista.toArray()) {
            if (o instanceof Conducta) {
                cond.add((Conducta) o);
            }
        }
        return cond;
    }

    private void ini() {
        if (!Beans.isDesignTime()) {
            ArrayList<Conducta> datos = new ArrayList<Conducta>();
            PreparedStatement st = null;
            ResultSet res = null;
            try {
                st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT cc.* FROM conv_conductas AS cc JOIN conv_tipos AS ct ON cc.tipo_id=ct.id WHERE cc.ano=? AND ct.tipo=? ORDER BY ct.gravedad ASC ");
                st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
                st.setInt(2, getTipo());
                res = st.executeQuery();
                while (res.next()) {
                    Conducta tc = new Conducta();
                    tc.cargarDesdeResultSet(res);
                    datos.add(tc);
                }
            } catch (Exception ex) {
                Logger.getLogger(PanelConductasParte.class.getName()).log(Level.SEVERE, null, ex);
            }
            Obj.cerrar(st, res);
            DefaultComboBoxModel modelo = new DefaultComboBoxModel(datos.toArray());
            cbConductas.setEditable(true);
            cbConductas.setModel(modelo);
            editorProfesores = (JTextComponent) cbConductas.getEditor().getEditorComponent();
            editorProfesores.setDocument(new AutoCompleteComboBoxDocument(cbConductas));
            editorProfesores.addKeyListener(new KeyListener() {

                @Override
                public void keyTyped(KeyEvent e) {
                    //throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void keyPressed(KeyEvent e) {
                    //throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        addElemento();
                    }
                }
            });
        }
    }

    @Action
    private void addElemento() {
        Object cond = cbConductas.getSelectedItem();
        if (cond instanceof Conducta) {
            if (!modeloLista.contains(cond)) {
                modeloLista.addElement(cond);
                editorProfesores.setText("");
                firePropertyChange("conductaAnadida", this, cond);
            }
        }
    }

    @Action
    private void quitar() {
        if (!modeloLista.isEmpty()) {
            Object[] vals = lista.getSelectedValues();
            if (vals.length > 0) {
                for (Object o : vals) {
                    modeloLista.removeElement(o);
                }
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

        panelGeneral = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lista = new javax.swing.JList();
        cbConductas = new javax.swing.JComboBox();
        bAdd = new javax.swing.JButton();
        bQuit = new javax.swing.JButton();

        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        panelGeneral.setName("panelGeneral"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        lista.setModel(modeloLista);
        lista.setName("lista"); // NOI18N
        jScrollPane1.setViewportView(lista);

        cbConductas.setName("cbConductas"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getResourceMap(PanelConductasParte.class);
        bAdd.setIcon(resourceMap.getIcon("bAdd.icon")); // NOI18N
        bAdd.setName("bAdd"); // NOI18N
        bAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bAddActionPerformed(evt);
            }
        });

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getActionMap(PanelConductasParte.class, this);
        bQuit.setAction(actionMap.get("sdfsdf")); // NOI18N
        bQuit.setIcon(resourceMap.getIcon("bQuit.icon")); // NOI18N
        bQuit.setName("bQuit"); // NOI18N
        bQuit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bQuitActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelGeneralLayout = new javax.swing.GroupLayout(panelGeneral);
        panelGeneral.setLayout(panelGeneralLayout);
        panelGeneralLayout.setHorizontalGroup(
            panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelGeneralLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 184, Short.MAX_VALUE)
                    .addGroup(panelGeneralLayout.createSequentialGroup()
                        .addComponent(cbConductas, 0, 102, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bQuit, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        panelGeneralLayout.setVerticalGroup(
            panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelGeneralLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(cbConductas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bAdd)
                    .addComponent(bQuit, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE)
                .addContainerGap())
        );

        add(panelGeneral, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void bAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bAddActionPerformed
        addElemento();
    }//GEN-LAST:event_bAddActionPerformed

    private void bQuitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bQuitActionPerformed
        quitar();
    }//GEN-LAST:event_bQuitActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bAdd;
    private javax.swing.JButton bQuit;
    private javax.swing.JComboBox cbConductas;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList lista;
    private javax.swing.JPanel panelGeneral;
    // End of variables declaration//GEN-END:variables
}
