/*
 * PanelConfiguracionMail.java
 *
 * Created on 19-feb-2009, 18:26:51
 */
package com.codeko.apps.maimonides.conf.mail;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.swing.NumericDocument;
import com.codeko.util.Num;
import java.beans.Beans;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;

/**
 *
 * @author Codeko
 */
public class PanelConfiguracionMail extends javax.swing.JPanel {

    ConfiguracionMail conf = null;

    /** Creates new form PanelConfiguracionMail */
    public PanelConfiguracionMail() {
        initComponents();
        if (!Beans.isDesignTime()) {
            conf = new ConfiguracionMail("");
            cargarConf();
        }
    }

    public PanelConfiguracionMail(String nombre) {
        initComponents();
        if (!Beans.isDesignTime()) {
            conf = new ConfiguracionMail(nombre);
            cargarConf();
        }
    }

    public final void cargarConf() {
        tfSMTP.setText(getConf().getHost());
        tfClave.setText(getConf().getClave());
        tfEmail.setText(getConf().getFrom());
        tfPuerto.setText(getConf().getPuerto() + "");
        tfUsuario.setText(getConf().getUsuario());
        spCaracteres.setValue(getConf().getMaximoCaracteres());
        cbSSL.setSelected(getConf().isSsl());
        taPie.setText(getConf().getPie());
        tfExtra.setText(getConf().getExtra());
    }

    public ConfiguracionMail getConf() {
        return conf;
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
            getConf().setHost(tfSMTP.getText().trim());
            getConf().setClave(new String(tfClave.getPassword()));
            getConf().setFrom(tfEmail.getText().trim());
            getConf().setPuerto(Num.getInt(tfPuerto.getText()));
            getConf().setUsuario(tfUsuario.getText().trim());
            getConf().setMaximoCaracteres(Num.getInt(spCaracteres.getValue()));
            getConf().setSsl(cbSSL.isSelected());
            getConf().setPie("\r\n" + taPie.getText());
            getConf().setExtra(tfExtra.getText());
            //Guardamos el tipo de servicio a usar.
            MaimonidesApp.getApplication().getConfiguracion().set(getConf().getNombre().toLowerCase() + ".servicio", "mail");
            return null;
        }

        @Override
        protected void succeeded(Object result) {
            setMessage("Datos guardados correctamente.");
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

        lSmtp = new javax.swing.JLabel();
        tfSMTP = new javax.swing.JTextField();
        lPuerto = new javax.swing.JLabel();
        tfPuerto = new javax.swing.JTextField();
        cbSSL = new javax.swing.JCheckBox();
        lusuario = new javax.swing.JLabel();
        tfUsuario = new javax.swing.JTextField();
        lClave = new javax.swing.JLabel();
        lFrom = new javax.swing.JLabel();
        tfEmail = new javax.swing.JTextField();
        lCaracteres = new javax.swing.JLabel();
        spCaracteres = new javax.swing.JSpinner();
        lPie = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        taPie = new javax.swing.JTextArea();
        tfClave = new javax.swing.JPasswordField();
        lInfoCaracteres = new javax.swing.JLabel();
        bGuardar = new javax.swing.JButton();
        lExtra = new javax.swing.JLabel();
        tfExtra = new javax.swing.JTextField();

        setName("Form"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getResourceMap(PanelConfiguracionMail.class);
        lSmtp.setText(resourceMap.getString("lSmtp.text")); // NOI18N
        lSmtp.setName("lSmtp"); // NOI18N

        tfSMTP.setText(resourceMap.getString("tfSMTP.text")); // NOI18N
        tfSMTP.setName("tfSMTP"); // NOI18N

        lPuerto.setText(resourceMap.getString("lPuerto.text")); // NOI18N
        lPuerto.setName("lPuerto"); // NOI18N

        tfPuerto.setDocument(new NumericDocument(0, false));
        tfPuerto.setText(resourceMap.getString("tfPuerto.text")); // NOI18N
        tfPuerto.setName("tfPuerto"); // NOI18N

        cbSSL.setText(resourceMap.getString("cbSSL.text")); // NOI18N
        cbSSL.setName("cbSSL"); // NOI18N

        lusuario.setText(resourceMap.getString("lusuario.text")); // NOI18N
        lusuario.setName("lusuario"); // NOI18N

        tfUsuario.setText(resourceMap.getString("tfUsuario.text")); // NOI18N
        tfUsuario.setName("tfUsuario"); // NOI18N

        lClave.setText(resourceMap.getString("lClave.text")); // NOI18N
        lClave.setName("lClave"); // NOI18N

        lFrom.setText(resourceMap.getString("lFrom.text")); // NOI18N
        lFrom.setName("lFrom"); // NOI18N

        tfEmail.setText(resourceMap.getString("tfEmail.text")); // NOI18N
        tfEmail.setName("tfEmail"); // NOI18N

        lCaracteres.setText(resourceMap.getString("lCaracteres.text")); // NOI18N
        lCaracteres.setName("lCaracteres"); // NOI18N

        spCaracteres.setName("spCaracteres"); // NOI18N

        lPie.setText(resourceMap.getString("lPie.text")); // NOI18N
        lPie.setName("lPie"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        taPie.setColumns(20);
        taPie.setRows(5);
        taPie.setName("taPie"); // NOI18N
        jScrollPane1.setViewportView(taPie);

        tfClave.setText(resourceMap.getString("tfClave.text")); // NOI18N
        tfClave.setName("tfClave"); // NOI18N

        lInfoCaracteres.setText(resourceMap.getString("lInfoCaracteres.text")); // NOI18N
        lInfoCaracteres.setName("lInfoCaracteres"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getActionMap(PanelConfiguracionMail.class, this);
        bGuardar.setAction(actionMap.get("guardar")); // NOI18N
        bGuardar.setName("bGuardar"); // NOI18N

        lExtra.setText(resourceMap.getString("lExtra.text")); // NOI18N
        lExtra.setName("lExtra"); // NOI18N

        tfExtra.setText(resourceMap.getString("tfExtra.text")); // NOI18N
        tfExtra.setName("tfExtra"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lPie, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 498, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addComponent(lCaracteres)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(spCaracteres, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(lFrom)
                                            .addComponent(lusuario)
                                            .addComponent(lSmtp))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(tfEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(tfUsuario, javax.swing.GroupLayout.DEFAULT_SIZE, 184, Short.MAX_VALUE)
                                            .addComponent(tfSMTP, javax.swing.GroupLayout.DEFAULT_SIZE, 184, Short.MAX_VALUE))))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lExtra)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addGroup(layout.createSequentialGroup()
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addComponent(lClave, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(lPuerto, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addGroup(layout.createSequentialGroup()
                                                    .addComponent(tfPuerto, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                    .addComponent(cbSSL))
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                    .addComponent(tfExtra, javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(tfClave, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                        .addComponent(lInfoCaracteres, javax.swing.GroupLayout.DEFAULT_SIZE, 217, Short.MAX_VALUE)))))
                        .addGap(0, 0, 0))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(bGuardar)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lSmtp)
                    .addComponent(tfSMTP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lPuerto)
                    .addComponent(tfPuerto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbSSL))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lusuario)
                    .addComponent(tfUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lClave)
                    .addComponent(tfClave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lFrom)
                    .addComponent(tfEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lExtra)
                    .addComponent(tfExtra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lCaracteres)
                    .addComponent(spCaracteres, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lInfoCaracteres))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lPie)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 216, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(bGuardar)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bGuardar;
    private javax.swing.JCheckBox cbSSL;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lCaracteres;
    private javax.swing.JLabel lClave;
    private javax.swing.JLabel lExtra;
    private javax.swing.JLabel lFrom;
    private javax.swing.JLabel lInfoCaracteres;
    private javax.swing.JLabel lPie;
    private javax.swing.JLabel lPuerto;
    private javax.swing.JLabel lSmtp;
    private javax.swing.JLabel lusuario;
    private javax.swing.JSpinner spCaracteres;
    private javax.swing.JTextArea taPie;
    private javax.swing.JPasswordField tfClave;
    private javax.swing.JTextField tfEmail;
    private javax.swing.JTextField tfExtra;
    private javax.swing.JTextField tfPuerto;
    private javax.swing.JTextField tfSMTP;
    private javax.swing.JTextField tfUsuario;
    // End of variables declaration//GEN-END:variables
}
