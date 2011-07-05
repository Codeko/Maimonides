/*
 * PanelResumen.java
 *
 * Created on 30-ago-2009, 14:22:14
 */
package com.codeko.apps.maimonides.convivencia;

import com.codeko.apps.maimonides.ICargable;
import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesUtil;
import com.codeko.apps.maimonides.alumnos.IFiltrableAlumno;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.util.Obj;
import com.mysql.jdbc.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;

/**
 *
 * @author Codeko
 */
public class PanelResumen extends javax.swing.JPanel implements ICargable, IFiltrableAlumno {

    boolean cargado = false;
    Alumno alumno = null;

    /** Creates new form PanelResumen */
    public PanelResumen() {
        initComponents();
    }

    public Alumno getAlumno() {
        return alumno;
    }

    @Override
    public void setAlumno(Alumno alumno) {
        this.alumno = alumno;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lGraves = new javax.swing.JLabel();
        tfTotalGraves = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        tfTotalLeves = new javax.swing.JTextField();
        tfExpulsiones = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        tfPendientesGraves = new javax.swing.JTextField();
        tfPendientesLeves = new javax.swing.JTextField();

        setName("Form"); // NOI18N
        addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                formAncestorAdded(evt);
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getResourceMap(PanelResumen.class);
        lGraves.setFont(resourceMap.getFont("lGraves.font")); // NOI18N
        lGraves.setText(resourceMap.getString("lGraves.text")); // NOI18N
        lGraves.setName("lGraves"); // NOI18N

        tfTotalGraves.setEditable(false);
        tfTotalGraves.setFont(resourceMap.getFont("tfPendientesGraves.font")); // NOI18N
        tfTotalGraves.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfTotalGraves.setText(resourceMap.getString("tfTotalGraves.text")); // NOI18N
        tfTotalGraves.setName("tfTotalGraves"); // NOI18N

        jLabel2.setFont(resourceMap.getFont("lGraves.font")); // NOI18N
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jLabel3.setFont(resourceMap.getFont("lGraves.font")); // NOI18N
        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        tfTotalLeves.setEditable(false);
        tfTotalLeves.setFont(resourceMap.getFont("tfPendientesGraves.font")); // NOI18N
        tfTotalLeves.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfTotalLeves.setText(resourceMap.getString("tfTotalLeves.text")); // NOI18N
        tfTotalLeves.setName("tfTotalLeves"); // NOI18N

        tfExpulsiones.setEditable(false);
        tfExpulsiones.setFont(resourceMap.getFont("tfPendientesGraves.font")); // NOI18N
        tfExpulsiones.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfExpulsiones.setText(resourceMap.getString("tfExpulsiones.text")); // NOI18N
        tfExpulsiones.setName("tfExpulsiones"); // NOI18N

        jLabel1.setFont(resourceMap.getFont("lGraves.font")); // NOI18N
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setToolTipText(resourceMap.getString("jLabel1.toolTipText")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel4.setFont(resourceMap.getFont("lGraves.font")); // NOI18N
        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setToolTipText(resourceMap.getString("jLabel4.toolTipText")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        tfPendientesGraves.setEditable(false);
        tfPendientesGraves.setFont(resourceMap.getFont("tfPendientesGraves.font")); // NOI18N
        tfPendientesGraves.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfPendientesGraves.setText(resourceMap.getString("tfPendientesGraves.text")); // NOI18N
        tfPendientesGraves.setName("tfPendientesGraves"); // NOI18N

        tfPendientesLeves.setEditable(false);
        tfPendientesLeves.setFont(resourceMap.getFont("tfPendientesGraves.font")); // NOI18N
        tfPendientesLeves.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfPendientesLeves.setText(resourceMap.getString("tfPendientesLeves.text")); // NOI18N
        tfPendientesLeves.setName("tfPendientesLeves"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel2)
                    .addComponent(lGraves))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(tfTotalGraves)
                    .addComponent(tfTotalLeves, javax.swing.GroupLayout.DEFAULT_SIZE, 41, Short.MAX_VALUE)
                    .addComponent(tfExpulsiones, 0, 0, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tfPendientesLeves)
                    .addComponent(tfPendientesGraves, javax.swing.GroupLayout.DEFAULT_SIZE, 41, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel4))
                .addGap(8, 8, 8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lGraves)
                    .addComponent(tfTotalGraves, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfPendientesGraves, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(tfTotalLeves, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(tfPendientesLeves, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel3)
                    .addComponent(tfExpulsiones, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_formAncestorAdded
        //cargar();
    }//GEN-LAST:event_formAncestorAdded
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel lGraves;
    private javax.swing.JTextField tfExpulsiones;
    private javax.swing.JTextField tfPendientesGraves;
    private javax.swing.JTextField tfPendientesLeves;
    private javax.swing.JTextField tfTotalGraves;
    private javax.swing.JTextField tfTotalLeves;
    // End of variables declaration//GEN-END:variables

    @Override
    public void cargar() {
        if (getAlumno() != null) {
            MaimonidesUtil.ejecutarTask(this, "actualizar");
            setCargado(true);
        }
    }

    @Override
    public void vaciar() {
        setCargado(false);
        setAlumno(null);
        tfExpulsiones.setText("");
        tfTotalGraves.setText("");
        tfTotalLeves.setText("");
        tfPendientesGraves.setText("");
        tfPendientesLeves.setText("");
    }

    @Override
    public boolean isCargado() {
        return cargado;
    }

    @Override
    public void setCargado(boolean cargado) {
        this.cargado = cargado;
    }

    @Action(block = Task.BlockingScope.ACTION)
    public Task actualizar() {
        return new ActualizarTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class ActualizarTask extends org.jdesktop.application.Task<Object, Void> {

        ActualizarTask(org.jdesktop.application.Application app) {
            super(app);
        }

        @Override
        protected Object doInBackground() {
            if (getAlumno() != null && getAlumno().getId() != null) {
                PreparedStatement st = null;
                ResultSet res = null;
                String sqlAlumnos = "select count(*) FROM expulsiones WHERE alumno_id=?";
                try {
                    st = (PreparedStatement) MaimonidesApp.getConexion().prepareStatement(sqlAlumnos);
                    st.setInt(1, getAlumno().getId());
                    res = st.executeQuery();
                    int expulsiones = 0;
                    if (res.next()) {
                        expulsiones = res.getInt(1);
                    }
                    tfExpulsiones.setText(expulsiones + "");
                    Obj.cerrar(st, res);

                    String sqlPartes = "SELECT sum(IF(tipo=" + TipoConducta.GRAVEDAD_GRAVE + " && estado=" + ParteConvivencia.ESTADO_SANCIONADO + ",1,0)) AS gravesSancionadas,sum(IF(tipo=" + TipoConducta.GRAVEDAD_GRAVE + " && estado=" + ParteConvivencia.ESTADO_PENDIENTE + ",1,0)) AS gravesPendientes,sum(IF(tipo=" + TipoConducta.GRAVEDAD_LEVE + " && estado=" + ParteConvivencia.ESTADO_SANCIONADO + ",1,0)) AS levesSancionadas,sum(IF(tipo=" + TipoConducta.GRAVEDAD_LEVE + " && estado=" + ParteConvivencia.ESTADO_PENDIENTE + ",1,0)) AS levesPendientes FROM conv_partes WHERE alumno_id=?";
                    st = (PreparedStatement) MaimonidesApp.getConexion().prepareStatement(sqlPartes);
                    st.setInt(1, getAlumno().getId());
                    res = st.executeQuery();
                    int gp = 0;
                    int gs = 0;
                    int lp = 0;
                    int ls = 0;
                    if (res.next()) {
                        gp = res.getInt("gravesPendientes");
                        gs = res.getInt("gravesSancionadas");
                        lp = res.getInt("levesPendientes");
                        ls = res.getInt("levesSancionadas");
                    }
                    tfPendientesGraves.setText(gp + "");
                    tfPendientesLeves.setText(lp + "");
                    tfTotalGraves.setText(gs + "");
                    tfTotalLeves.setText(ls + "");
                } catch (SQLException ex) {
                    Logger.getLogger(PanelResumen.class.getName()).log(Level.SEVERE, null, ex);
                }
                Obj.cerrar(st, res);
            }
            return null;
        }

        @Override
        protected void succeeded(Object result) {
        }
    }
}
