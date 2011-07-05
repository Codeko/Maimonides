/*
 * PanelPartesMedioDigitalizados.java
 *
 * Created on 21-may-2009, 14:15:13
 */
package com.codeko.apps.maimonides.partes.informes;

import com.codeko.apps.maimonides.IPanel;
import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesUtil;
import com.codeko.apps.maimonides.elementos.ParteFaltas;
import com.codeko.apps.maimonides.partes.PanelListaAlumnosParte;
import com.codeko.swing.CodekoTableModel;
import com.codeko.util.Fechas;
import com.codeko.util.Obj;
import com.mysql.jdbc.PreparedStatement;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 *
 * @author Codeko
 */
public class PanelPartesMedioDigitalizados extends javax.swing.JPanel implements IPanel {

    CodekoTableModel<ParteFaltas> modelo = new CodekoTableModel<ParteFaltas>(new ParteFaltas());
    boolean cargado = false;

    /** Creates new form PanelPartesMedioDigitalizados */
    public PanelPartesMedioDigitalizados() {
        initComponents();
        MaimonidesUtil.addMenuTabla(tabla, "Partes no digitalizados parcialmente rellenos.");
        tabla.getColumnExt("Código").setVisible(false);
        TableColumnExt tc = tabla.getColumnExt("Fecha");
        tc.setCellRenderer(new DefaultTableCellRenderer() {

            @Override
            public void setValue(Object val) {
                setText(Fechas.format(val));
            }
        });
        tc.setMaxWidth(80);
        tabla.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
                    int row = tabla.rowAtPoint(e.getPoint());
                    editarParte(modelo.getElemento(tabla.convertRowIndexToModel(row)));
                }
            }
        });
        tabla.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                setParteSeleccionado(tabla.getSelectedRow() != -1);
            }
        });
    }

    @Action
    private void editarParte(ParteFaltas parte) {
        PanelListaAlumnosParte.abrirEditorParte(parte);
    }

    public boolean isCargado() {
        return cargado;
    }

    public void setCargado(boolean cargado) {
        this.cargado = cargado;
    }

    public static int getTotalPartes() {
        int total = 0;
        PreparedStatement st = null;
        ResultSet res = null;
        try {
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT count(distinct p.id) AS total FROM partes AS p LEFT JOIN cursos AS c ON c.curso=p.curso JOIN partes_alumnos AS pa ON pa.parte_id=p.id WHERE p.ano=? AND p.digitalizado=0 AND pa.asistencia NOT IN (" + ParteFaltas.FALTA_INDETERMINADA + "," + ParteFaltas.FALTA_JUSTIFICADA + ") ORDER BY p.fecha DESC,c.posicion,p.id");
            st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
            res = st.executeQuery();
            if (res.next()) {
                total = res.getInt("total");
            }
        } catch (SQLException ex) {
            Logger.getLogger(PanelPartesMedioDigitalizados.class.getName()).log(Level.SEVERE, null, ex);
        }
        Obj.cerrar(res, st);
        return total;
    }

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task actualizar() {
        return new ActualizarTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class ActualizarTask extends org.jdesktop.application.Task<Object, Void> {

        ActualizarTask(org.jdesktop.application.Application app) {
            super(app);
            modelo.vaciar();
            setProgress(1);
            setCargado(false);
        }

        @Override
        protected Object doInBackground() {
            PreparedStatement st = null;
            ResultSet res = null;
            try {
                int total = getTotalPartes();
                st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT distinct p.* FROM partes AS p LEFT JOIN cursos AS c ON c.curso=p.curso JOIN partes_alumnos AS pa ON pa.parte_id=p.id WHERE p.ano=? AND p.digitalizado=0 AND pa.asistencia NOT IN (" + ParteFaltas.FALTA_INDETERMINADA + "," + ParteFaltas.FALTA_JUSTIFICADA + ") ORDER BY p.fecha DESC,c.posicion,p.id");
                st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
                res = st.executeQuery();
                int pos = 0;
                while (res.next() && !isCancelled()) {
                    pos++;
                    setMessage(String.format("Cargando partes de asistencias %d de %d...", pos, total));
                    setProgress(pos, 0, total);
                    ParteFaltas p = new ParteFaltas();
                    p.cargarDesdeResultSet(res);
                    modelo.addDato(p);
                }

            } catch (SQLException ex) {
                Logger.getLogger(PanelPartesMedioDigitalizados.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                Obj.cerrar(st, res);
            }
            return null;
        }

        @Override
        protected void succeeded(Object result) {
            tabla.packAll();
            setCargado(true);
        }
    }
    private boolean parteSeleccionado = false;

    public boolean isParteSeleccionado() {
        return parteSeleccionado;
    }

    public void setParteSeleccionado(boolean b) {
        boolean old = isParteSeleccionado();
        this.parteSeleccionado = b;
        firePropertyChange("parteSeleccionado", old, isParteSeleccionado());
    }

    @Override
    public boolean puedoSusituir() {
        return true;
    }

    @Action(enabledProperty = "parteSeleccionado")
    public void editarParteSeleccionado() {
        int row = tabla.getSelectedRow();
        if (row > -1) {
            ParteFaltas p = modelo.getElemento(tabla.convertRowIndexToModel(row));
            editarParte(p);
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

        jScrollPane1 = new javax.swing.JScrollPane();
        tabla = new org.jdesktop.swingx.JXTable();
        jToolBar1 = new javax.swing.JToolBar();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

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
        setLayout(new java.awt.BorderLayout());

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        tabla.setModel(modelo);
        tabla.setColumnControlVisible(true);
        tabla.setName("tabla"); // NOI18N
        jScrollPane1.setViewportView(tabla);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jToolBar1.setRollover(true);
        jToolBar1.setName("jToolBar1"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getActionMap(PanelPartesMedioDigitalizados.class, this);
        jButton1.setAction(actionMap.get("actualizar")); // NOI18N
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setName("jButton1"); // NOI18N
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton1);

        jButton2.setAction(actionMap.get("editarParteSeleccionado")); // NOI18N
        jButton2.setFocusable(false);
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton2.setName("jButton2"); // NOI18N
        jButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton2);

        add(jToolBar1, java.awt.BorderLayout.PAGE_START);
    }// </editor-fold>//GEN-END:initComponents

    private void formAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_formAncestorAdded
//        if (!Beans.isDesignTime() && !isCargado()) {
//            MaimonidesUtil.ejecutarTask(this, "actualizar");
//        }
    }//GEN-LAST:event_formAncestorAdded
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar jToolBar1;
    private org.jdesktop.swingx.JXTable tabla;
    // End of variables declaration//GEN-END:variables
}
