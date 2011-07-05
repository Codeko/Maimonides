/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * PanelConfigSeneca.java
 *
 * Created on 20-may-2009, 11:11:21
 */
package com.codeko.apps.maimonides.seneca;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.util.Num;
import org.jdesktop.application.Action;

/**
 *
 * @author Codeko
 */
public class PanelConfigSeneca extends javax.swing.JPanel {

    /** Creates new form PanelConfigSeneca */
    public PanelConfigSeneca() {
        initComponents();
        String urlBase = ClienteSeneca.getUrlBase();
        cbUrls.setSelectedItem(urlBase);
        if (!urlBase.equals(cbUrls.getSelectedItem())) {
            cbUrls.addItem(urlBase);
            cbUrls.setSelectedItem(urlBase);
        }
        boolean enviar=Num.getInt(MaimonidesApp.getApplication().getConfiguracion().get("seneca_convivencia_enviar_ignorados", "0"))>0;
        cbEnviarIgnorados.setSelected(enviar);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        cbUrls = new javax.swing.JComboBox();
        bGuardar = new javax.swing.JButton();
        cbEnviarIgnorados = new javax.swing.JCheckBox();
        jXTitledSeparator1 = new org.jdesktop.swingx.JXTitledSeparator();

        setName("Form"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getResourceMap(PanelConfigSeneca.class);
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        cbUrls.setEditable(true);
        cbUrls.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "https://www.juntadeandalucia.es/educacion/seneca/seneca/jsp/", "http://seneca.ced.junta-andalucia.es:9000/seneca/jsp/" }));
        cbUrls.setName("cbUrls"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getActionMap(PanelConfigSeneca.class, this);
        bGuardar.setAction(actionMap.get("guardar")); // NOI18N
        bGuardar.setName("bGuardar"); // NOI18N

        cbEnviarIgnorados.setText(resourceMap.getString("cbEnviarIgnorados.text")); // NOI18N
        cbEnviarIgnorados.setName("cbEnviarIgnorados"); // NOI18N

        jXTitledSeparator1.setTitle(resourceMap.getString("jXTitledSeparator1.title")); // NOI18N
        jXTitledSeparator1.setName("jXTitledSeparator1"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cbUrls, 0, 499, Short.MAX_VALUE)
                    .addComponent(jLabel1)
                    .addComponent(bGuardar, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jXTitledSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 499, Short.MAX_VALUE)
                    .addComponent(cbEnviarIgnorados, javax.swing.GroupLayout.DEFAULT_SIZE, 499, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbUrls, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jXTitledSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbEnviarIgnorados)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 149, Short.MAX_VALUE)
                .addComponent(bGuardar)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    @Action
    public void guardar() {
        ClienteSeneca.setUrlBase(cbUrls.getSelectedItem().toString());
        MaimonidesApp.getApplication().getConfiguracion().set("seneca_convivencia_enviar_ignorados", cbEnviarIgnorados.isSelected()?"1":"0");
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bGuardar;
    private javax.swing.JCheckBox cbEnviarIgnorados;
    private javax.swing.JComboBox cbUrls;
    private javax.swing.JLabel jLabel1;
    private org.jdesktop.swingx.JXTitledSeparator jXTitledSeparator1;
    // End of variables declaration//GEN-END:variables
}
