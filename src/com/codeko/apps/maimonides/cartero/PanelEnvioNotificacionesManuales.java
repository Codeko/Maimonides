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


package com.codeko.apps.maimonides.cartero;

import com.codeko.apps.maimonides.IPanel;
import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.IEmailable;
import com.codeko.apps.maimonides.elementos.Profesor;
import com.codeko.util.Str;
import java.io.File;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.commons.validator.EmailValidator;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;

public class PanelEnvioNotificacionesManuales extends javax.swing.JPanel implements IPanel {

    private static final int TIPO_FICHA = 1;
    private static final int TIPO_EMAIL = 2;
    private static final int TIPO_SMS = 3;
    DefaultListModel modeloAdjuntos = new DefaultListModel();

    /** Creates new form PanelEnvioNotificacionesManuales */
    public PanelEnvioNotificacionesManuales() {
        initComponents();
        taEmail.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                verificarTextoEmail();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                verificarTextoEmail();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                verificarTextoEmail();
            }
        });
        taSMS.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                verificarTextoSMS();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                verificarTextoSMS();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                verificarTextoSMS();
            }
        });
        listaAdjuntos.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                setAdjuntoSeleccionado(listaAdjuntos.getSelectedValue() != null);
            }
        });
    }

    private void verificarTextoEmail() {
        setTextoEnEmail(!taEmail.getText().trim().equals(""));
        setTextoEnEmailSMS(isTextoEnEmail() && isTextoEnSMS());
    }

    private void verificarTextoSMS() {
        setTextoEnSMS(!taSMS.getText().trim().equals(""));
        setTextoEnEmailSMS(isTextoEnEmail() && isTextoEnSMS());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pestanas = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        taEmail = new javax.swing.JTextArea();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        taSMS = new javax.swing.JTextArea();
        bEnviarSMS = new javax.swing.JButton();
        bEnviarEmail = new javax.swing.JButton();
        bEnviar = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        tfAsunto = new javax.swing.JTextField();
        jScrollPane3 = new javax.swing.JScrollPane();
        listaAdjuntos = new org.jdesktop.swingx.JXList();
        jLabel4 = new javax.swing.JLabel();
        bAddAdjunto = new javax.swing.JButton();
        bDelAdjunto = new javax.swing.JButton();
        panelSelectorAlumnos1 = new com.codeko.apps.maimonides.cartero.PanelSelectorDestinatarios();

        setName("maimonides.paneles.herramientas.notificaciones_generales"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        pestanas.setName("pestanas"); // NOI18N

        jPanel1.setName("jPanel1"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getResourceMap(PanelEnvioNotificacionesManuales.class);
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        taEmail.setColumns(20);
        taEmail.setRows(5);
        taEmail.setName("taEmail"); // NOI18N
        jScrollPane1.setViewportView(taEmail);

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        taSMS.setColumns(20);
        taSMS.setRows(5);
        taSMS.setName("taSMS"); // NOI18N
        jScrollPane2.setViewportView(taSMS);

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getActionMap(PanelEnvioNotificacionesManuales.class, this);
        bEnviarSMS.setAction(actionMap.get("enviarSMS")); // NOI18N
        bEnviarSMS.setName("bEnviarSMS"); // NOI18N

        bEnviarEmail.setAction(actionMap.get("enviarEmail")); // NOI18N
        bEnviarEmail.setName("bEnviarEmail"); // NOI18N

        bEnviar.setAction(actionMap.get("enviar")); // NOI18N
        bEnviar.setName("bEnviar"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        tfAsunto.setText(resourceMap.getString("tfAsunto.text")); // NOI18N
        tfAsunto.setName("tfAsunto"); // NOI18N

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        listaAdjuntos.setModel(modeloAdjuntos);
        listaAdjuntos.setName("listaAdjuntos"); // NOI18N
        jScrollPane3.setViewportView(listaAdjuntos);

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        bAddAdjunto.setAction(actionMap.get("addAdjunto")); // NOI18N
        bAddAdjunto.setName("bAddAdjunto"); // NOI18N

        bDelAdjunto.setAction(actionMap.get("delAdjunto")); // NOI18N
        bDelAdjunto.setName("bDelAdjunto"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 698, Short.MAX_VALUE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(bEnviar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bEnviarEmail)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bEnviarSMS))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfAsunto, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 513, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(bAddAdjunto)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(bDelAdjunto))
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(6, 6, 6)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel3)
                    .addComponent(tfAsunto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(bAddAdjunto)
                    .addComponent(bDelAdjunto))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bEnviarSMS)
                    .addComponent(bEnviarEmail)
                    .addComponent(bEnviar))
                .addContainerGap())
        );

        pestanas.addTab(resourceMap.getString("jPanel1.TabConstraints.tabTitle"), resourceMap.getIcon("jPanel1.TabConstraints.tabIcon"), jPanel1); // NOI18N

        panelSelectorAlumnos1.setName("panelSelectorAlumnos1"); // NOI18N
        pestanas.addTab(resourceMap.getString("panelSelectorAlumnos1.TabConstraints.tabTitle"), resourceMap.getIcon("panelSelectorAlumnos1.TabConstraints.tabIcon"), panelSelectorAlumnos1); // NOI18N

        add(pestanas, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    private boolean textoEnEmail = false;

    public boolean isTextoEnEmail() {
        return textoEnEmail;
    }

    public void setTextoEnEmail(boolean b) {
        boolean old = isTextoEnEmail();
        this.textoEnEmail = b;
        firePropertyChange("textoEnEmail", old, isTextoEnEmail());
    }
    private boolean textoEnSMS = false;

    public boolean isTextoEnSMS() {
        return textoEnSMS;
    }

    public void setTextoEnSMS(boolean b) {
        boolean old = isTextoEnSMS();
        this.textoEnSMS = b;
        firePropertyChange("textoEnSMS", old, isTextoEnSMS());
    }
    private boolean textoEnEmailSMS = false;

    public boolean isTextoEnEmailSMS() {
        return textoEnEmailSMS;
    }

    public void setTextoEnEmailSMS(boolean b) {
        boolean old = isTextoEnEmailSMS();
        this.textoEnEmailSMS = b;
        firePropertyChange("textoEnEmailSMS", old, isTextoEnEmailSMS());
    }
    private boolean adjuntoSeleccionado = false;

    public boolean isAdjuntoSeleccionado() {
        return adjuntoSeleccionado;
    }

    public void setAdjuntoSeleccionado(boolean b) {
        boolean old = isAdjuntoSeleccionado();
        this.adjuntoSeleccionado = b;
        firePropertyChange("adjuntoSeleccionado", old, isAdjuntoSeleccionado());
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bAddAdjunto;
    private javax.swing.JButton bDelAdjunto;
    private javax.swing.JButton bEnviar;
    private javax.swing.JButton bEnviarEmail;
    private javax.swing.JButton bEnviarSMS;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private org.jdesktop.swingx.JXList listaAdjuntos;
    private com.codeko.apps.maimonides.cartero.PanelSelectorDestinatarios panelSelectorAlumnos1;
    private javax.swing.JTabbedPane pestanas;
    private javax.swing.JTextArea taEmail;
    private javax.swing.JTextArea taSMS;
    private javax.swing.JTextField tfAsunto;
    // End of variables declaration//GEN-END:variables

    @Override
    public boolean puedoSusituir() {
        return true;
    }

    @Action(block = Task.BlockingScope.APPLICATION, enabledProperty = "textoEnEmailSMS")
    public Task enviar() {
        return new EnviarTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class), TIPO_FICHA);
    }

    private class EnviarTask extends org.jdesktop.application.Task<Object, Void> {

        int tipo = 0;
        String textoSMS = "";
        String textoMail = "";
        String titulo = "";

        EnviarTask(org.jdesktop.application.Application app, int tipo) {
            super(app);
            this.tipo = tipo;
            textoMail = taEmail.getText();
            textoSMS = taSMS.getText();
            titulo = tfAsunto.getText();
        }

        @Override
        protected Object doInBackground() throws Exception {
            setMessage("Filtrando destinatarios validos...");
            ArrayList<IEmailable> emails = new ArrayList<IEmailable>();
            ArrayList<Alumno> smss = new ArrayList<Alumno>();
            ArrayList<Profesor> profesores = panelSelectorAlumnos1.getProfesoresSeleccionados();
            for (Profesor p : profesores) {
                String email = p.getEmail();
                if (EmailValidator.getInstance().isValid(email)) {
                    emails.add(p);
                }
            }
            for (Alumno a : panelSelectorAlumnos1.getAlumnosSeleccionados()) {
                String email = a.getEmail();
                String sms = a.getSms();
                if (tipo == TIPO_EMAIL) {
                    if (EmailValidator.getInstance().isValid(email)) {
                        emails.add(a);
                    }
                } else if (tipo == TIPO_SMS) {
                    if (!Str.noNulo(sms).trim().equals("")) {
                        smss.add(a);
                    }
                } else if (tipo == TIPO_FICHA) {
                    if (EmailValidator.getInstance().isValid(email) && a.isNotificar(Alumno.NOTIFICAR_EMAIL)) {
                        emails.add(a);
                    }
                    if (!Str.noNulo(sms).trim().equals("") && a.isNotificar(Alumno.NOTIFICAR_SMS)) {
                        smss.add(a);
                    }
                }
            }
            if (smss.isEmpty() && emails.isEmpty()) {
                setMessage("No existen destinatarios válidos.");
                throw new Exception("No existen destinatarios válidos.");
            } else {
                if (!smss.isEmpty()) {
                    setMessage("Enviando SMS...");
                    if (!SMS.enviarSMS(textoSMS, smss, false)) {
                        setMessage("Ha habido algún error enviado los SMS, se cancela el envío.");
                        throw new Exception("Ha habido algún error enviado los SMS, se cancela el envío.");
                    }
                }
                if (!emails.isEmpty()) {
                    setMessage("Enviando emails...");
                    ArrayList<File> adjuntos = new ArrayList<File>();
                    for (Object obj : modeloAdjuntos.toArray()) {
                        if (obj instanceof File) {
                            adjuntos.add((File) obj);
                        }
                    }
                    CarteroAlumno.enviarEmail(titulo, textoMail, adjuntos, emails);
                }
            }
            return null;
        }

        @Override
        protected void failed(Throwable t) {
            JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), t.getLocalizedMessage(), "Error enviando", JOptionPane.ERROR_MESSAGE);
            setMessage(t.getLocalizedMessage());
        }

        @Override
        protected void succeeded(Object result) {
            setMessage("Notificación enviada correctamente.");
            JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), "Notificación enviada correctamente.", "Finalizado", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    @Action(block = Task.BlockingScope.APPLICATION, enabledProperty = "textoEnEmail")
    public Task enviarEmail() {
        return new EnviarTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class), TIPO_EMAIL);
    }

    @Action(block = Task.BlockingScope.APPLICATION, enabledProperty = "textoEnSMS")
    public Task enviarSMS() {
        return new EnviarTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class), TIPO_SMS);
    }

    @Action
    public void addAdjunto() {
        JFileChooser jfc = new JFileChooser(MaimonidesApp.getApplication().getUltimoArchivo());
        jfc.setMultiSelectionEnabled(true);
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int res = jfc.showOpenDialog(MaimonidesApp.getMaimonidesView().getFrame());
        if (res == JFileChooser.APPROVE_OPTION) {
            File[] files = jfc.getSelectedFiles();
            for (File f : files) {
                if (!modeloAdjuntos.contains(f)) {
                    modeloAdjuntos.addElement(f);
                }
            }
            MaimonidesApp.getApplication().setUltimoArchivo(jfc.getSelectedFile());
        }
    }

    @Action(enabledProperty = "adjuntoSeleccionado")
    public void delAdjunto() {
        for (Object val : listaAdjuntos.getSelectedValues()) {
            modeloAdjuntos.removeElement(val);
        }
    }
}
