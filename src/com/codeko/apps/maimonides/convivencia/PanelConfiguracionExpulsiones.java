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
 * PanelConfiguracionExpulsiones.java
 *
 * Created on 28-ago-2009, 13:14:48
 */
package com.codeko.apps.maimonides.convivencia;

import com.codeko.apps.maimonides.conf.Configuracion;
import com.codeko.apps.maimonides.ICargable;
import com.codeko.apps.maimonides.MaimonidesApp;

import com.codeko.apps.maimonides.MaimonidesUtil;
import com.codeko.apps.maimonides.convivencia.config.ConfiguracionMedidasExpulsion;
import com.codeko.util.Num;
import com.codeko.util.Str;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;

/**
 *
 * @author Codeko
 */
public class PanelConfiguracionExpulsiones extends javax.swing.JPanel implements ICargable {

    boolean cargado = false;

    /** Creates new form PanelConfiguracionExpulsiones */
    public PanelConfiguracionExpulsiones() {
        initComponents();
        cargar();
        for (Conducta c : Conducta.getConductas(TipoConducta.TIPO_MEDIDA)) {
            cbMedidaExpulsionesIndefinidas.addItem(c);
        }
    }

    @Action(block = Task.BlockingScope.ACTION)
    public Task guardarDatos() {
        return new GuardarDatosTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    @Override
    public final void cargar() {
        if (!isCargado()) {
            MaimonidesUtil.ejecutarTask(this, "cargarDatos");
            setCargado(true);
        }
    }

    @Override
    public void vaciar() {
        setCargado(false);
    }

    @Override
    public boolean isCargado() {
        return cargado;
    }

    @Override
    public void setCargado(boolean cargado) {
        this.cargado = cargado;
    }

    private class GuardarDatosTask extends org.jdesktop.application.Task<Object, Void> {

        HashMap<String, String> datos = new HashMap<String, String>();

        GuardarDatosTask(org.jdesktop.application.Application app) {
            super(app);
            try {
                spEquivalencia.commitEdit();
            } catch (ParseException ex) {
                Logger.getLogger(PanelConfiguracionExpulsiones.class.getName()).log(Level.SEVERE, null, ex);
            }
            datos.put("convivencia_equivalencia_leves", Num.getInt(spEquivalencia.getValue()) + "");
            ArrayList<Integer> ok = getSecuenciaExpulsiones(tfSecuencia.getText());
            //Ahora reconstruimos la cadena
            String sec = Str.implode(ok, ",");
            tfSecuencia.setText(sec);
            datos.put("convivencia_secuncia_expulsion", sec);
            datos.put(ConfiguracionMedidasExpulsion.getParametroExpulsionesMedidas(), panelConfiguracionMedidasExpulsion1.getDato());
            Object obj = cbMedidaExpulsionesIndefinidas.getSelectedItem();
            int medida = 0;
            if (obj instanceof Conducta) {
                medida = ((Conducta) obj).getId();
            }
            datos.put(ConfiguracionMedidasExpulsion.getParametroExpulsionesMedidaPorDefecto(), "" + medida);
            setMessage("Guardando datos...");
        }

        @Override
        protected Object doInBackground() {
            Iterator it = datos.keySet().iterator();
            while (it.hasNext()) {
                String nombre = it.next().toString();
                String valor = datos.get(nombre);
                MaimonidesApp.getApplication().getConfiguracion().set(nombre, valor);
            }
            return null;  // return your result
        }

        @Override
        protected void succeeded(Object result) {
            setMessage("Datos guardados correctamente.");
        }
    }

    public static ArrayList<Integer> getSecuenciaExpulsiones(String cadena) {
        String sec = cadena;
        String[] vals = sec.split(",");
        ArrayList<Integer> ok = new ArrayList<Integer>();
        for (String s : vals) {
            int i = Num.getInt(s);
            if (i > 0) {
                ok.add(i);
            }
        }
        return ok;
    }

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task cargarDatos() {
        return new CargarDatosTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class CargarDatosTask extends org.jdesktop.application.Task<HashMap<String, String>, Void> {

        CargarDatosTask(org.jdesktop.application.Application app) {
            super(app);
        }

        @Override
        protected HashMap<String, String> doInBackground() {
            //TODO Convertir estos textos a constantes y bsucar y cambiarlos por ellas
            HashMap<String, String> datos = new HashMap<String, String>();
            Configuracion cfg = MaimonidesApp.getApplication().getConfiguracion();
            datos.put("convivencia_equivalencia_leves", cfg.get("convivencia_equivalencia_leves", "3"));
            datos.put("convivencia_secuncia_expulsion", cfg.get("convivencia_secuncia_expulsion", "3,7,15,30"));
            datos.put(ConfiguracionMedidasExpulsion.getParametroExpulsionesMedidas(), cfg.get(ConfiguracionMedidasExpulsion.getParametroExpulsionesMedidas(), ""));
            datos.put(ConfiguracionMedidasExpulsion.getParametroExpulsionesMedidaPorDefecto(), cfg.get(ConfiguracionMedidasExpulsion.getParametroExpulsionesMedidaPorDefecto(), ""));
            return datos;
        }

        @Override
        protected void succeeded(HashMap<String, String> datos) {
            spEquivalencia.setValue(Num.getInt(datos.get("convivencia_equivalencia_leves")));
            tfSecuencia.setText(datos.get("convivencia_secuncia_expulsion"));
            panelConfiguracionMedidasExpulsion1.setDato(datos.get(ConfiguracionMedidasExpulsion.getParametroExpulsionesMedidas()));
            int medida = Num.getInt(datos.get(ConfiguracionMedidasExpulsion.getParametroExpulsionesMedidaPorDefecto()));
            if (medida == 0) {
                cbMedidaExpulsionesIndefinidas.setSelectedIndex(0);
            } else {
                try {
                    Conducta c = new Conducta(medida);
                    cbMedidaExpulsionesIndefinidas.setSelectedItem(c);
                } catch (Exception ex) {
                    Logger.getLogger(PanelConfiguracionExpulsiones.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            asignarSecuenciaAMedidas();
        }
    }

    private void asignarSecuenciaAMedidas() {
        //Recuperamos la secuencia
        ArrayList<Integer> sec = getSecuenciaExpulsiones(tfSecuencia.getText());
        //Y se la asignamos al panel
        panelConfiguracionMedidasExpulsion1.setNuevosDias(sec);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        spEquivalencia = new javax.swing.JSpinner();
        jLabel2 = new javax.swing.JLabel();
        tfSecuencia = new javax.swing.JTextField();
        bGuardar = new javax.swing.JButton();
        panelConfiguracionMedidasExpulsion1 = new com.codeko.apps.maimonides.convivencia.config.PanelConfiguracionMedidasExpulsion();
        jLabel3 = new javax.swing.JLabel();
        cbMedidaExpulsionesIndefinidas = new javax.swing.JComboBox();

        setName("Form"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getResourceMap(PanelConfiguracionExpulsiones.class);
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        spEquivalencia.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(3), Integer.valueOf(0), null, Integer.valueOf(1)));
        spEquivalencia.setName("spEquivalencia"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        tfSecuencia.setText(resourceMap.getString("tfSecuencia.text")); // NOI18N
        tfSecuencia.setName("tfSecuencia"); // NOI18N
        tfSecuencia.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tfSecuenciaKeyReleased(evt);
            }
        });

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getActionMap(PanelConfiguracionExpulsiones.class, this);
        bGuardar.setAction(actionMap.get("guardarDatos")); // NOI18N
        bGuardar.setName("bGuardar"); // NOI18N

        panelConfiguracionMedidasExpulsion1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("panelConfiguracionMedidasExpulsion1.border.title"))); // NOI18N
        panelConfiguracionMedidasExpulsion1.setName("panelConfiguracionMedidasExpulsion1"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        cbMedidaExpulsionesIndefinidas.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Ninguna" }));
        cbMedidaExpulsionesIndefinidas.setName("cbMedidaExpulsionesIndefinidas"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(panelConfiguracionMedidasExpulsion1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 418, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spEquivalencia, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfSecuencia, javax.swing.GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE))
                    .addComponent(bGuardar)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbMedidaExpulsionesIndefinidas, 0, 192, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(spEquivalencia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(tfSecuencia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelConfiguracionMedidasExpulsion1, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(cbMedidaExpulsionesIndefinidas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(bGuardar)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void tfSecuenciaKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfSecuenciaKeyReleased
        asignarSecuenciaAMedidas();
    }//GEN-LAST:event_tfSecuenciaKeyReleased
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bGuardar;
    private javax.swing.JComboBox cbMedidaExpulsionesIndefinidas;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private com.codeko.apps.maimonides.convivencia.config.PanelConfiguracionMedidasExpulsion panelConfiguracionMedidasExpulsion1;
    private javax.swing.JSpinner spEquivalencia;
    private javax.swing.JTextField tfSecuencia;
    // End of variables declaration//GEN-END:variables
}
