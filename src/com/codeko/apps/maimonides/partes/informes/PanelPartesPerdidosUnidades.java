/*
 * PanelPartesPendientes.java
 *
 * Created on 4 de noviembre de 2008, 17:35
 */
package com.codeko.apps.maimonides.partes.informes;

import com.codeko.apps.maimonides.partes.*;
import com.codeko.apps.maimonides.IPanel;
import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesUtil;
import com.codeko.apps.maimonides.elementos.ParteFaltas;
import com.codeko.apps.maimonides.elementos.Unidad;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import com.codeko.swing.CodekoTableModel;
import com.codeko.util.Fechas;
import com.codeko.util.Obj;
import com.mysql.jdbc.PreparedStatement;
import java.awt.event.MouseAdapter;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 *
 * @author  Codeko
 */
public class PanelPartesPerdidosUnidades extends javax.swing.JPanel implements IPanel {

    CodekoTableModel<ParteFaltas> modelo = new CodekoTableModel<ParteFaltas>(new ParteFaltas());
    CodekoTableModel<UnidadesPartesNoEntregados> modeloCursos = new CodekoTableModel<UnidadesPartesNoEntregados>(new UnidadesPartesNoEntregados());
    PanelPartesPerdidosUnidades auto = this;
    boolean cargado = false;

    /** Creates new form PanelPartesPendientes */
    public PanelPartesPerdidosUnidades() {
        initComponents();
        MaimonidesUtil.addMenuTabla(tabla, "Partes pendientes de digitalizar");
        MaimonidesUtil.addMenuTabla(tablaCursos, "Relación de partes pendientes de digitalizar agrupados por curso");
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

        tablaCursos.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                MaimonidesUtil.ejecutarTask(auto, "cargarPartes");
            }
        });
        tablaCursos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    public boolean isCargado() {
        return cargado;
    }

    public void setCargado(boolean cargado) {
        this.cargado = cargado;
    }

    @Action
    private void editarParte(ParteFaltas parte) {
        PanelListaAlumnosParte.abrirEditorParte(parte);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bHerramientas = new javax.swing.JToolBar();
        bActualizar = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        split = new javax.swing.JSplitPane();
        scrollProfesores = new javax.swing.JScrollPane();
        tablaCursos = new org.jdesktop.swingx.JXTable();
        scroll = new javax.swing.JScrollPane();
        tabla = new org.jdesktop.swingx.JXTable();

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

        bHerramientas.setRollover(true);
        bHerramientas.setName("bHerramientas"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getActionMap(PanelPartesPerdidosUnidades.class, this);
        bActualizar.setAction(actionMap.get("actualizar")); // NOI18N
        bActualizar.setFocusable(false);
        bActualizar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bActualizar.setName("bActualizar"); // NOI18N
        bActualizar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        bHerramientas.add(bActualizar);

        jButton1.setAction(actionMap.get("editarParteSeleccionado")); // NOI18N
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setName("jButton1"); // NOI18N
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        bHerramientas.add(jButton1);

        add(bHerramientas, java.awt.BorderLayout.PAGE_START);

        split.setDividerLocation(250);
        split.setName("split"); // NOI18N

        scrollProfesores.setName("scrollProfesores"); // NOI18N

        tablaCursos.setModel(modeloCursos);
        tablaCursos.setColumnControlVisible(true);
        tablaCursos.setName("tablaCursos"); // NOI18N
        scrollProfesores.setViewportView(tablaCursos);

        split.setLeftComponent(scrollProfesores);

        scroll.setName("scroll"); // NOI18N

        tabla.setModel(modelo);
        tabla.setColumnControlVisible(true);
        tabla.setName("tabla"); // NOI18N
        scroll.setViewportView(tabla);

        split.setRightComponent(scroll);

        add(split, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

private void formAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_formAncestorAdded
//    if (!Beans.isDesignTime() && !isCargado()) {
//        MaimonidesUtil.ejecutarTask(this, "actualizar");
//    }
}//GEN-LAST:event_formAncestorAdded

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task actualizar() {
        return new ActualizarProfesoresTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task cargarPartes() {
        return new ActualizarPartesTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    public static int getTotalPartes(Unidad unidad) {
        int total = 0;
        PreparedStatement st = null;
        ResultSet res = null;
        try {
            String sql = "SELECT count(distinct p.id) AS total FROM partes AS p "
                    + " JOIN partes_unidades AS pu ON pu.parte_id=p.id "
                    + " WHERE p.ano=? AND p.digitalizado=0 AND pu.unidad_id=? AND DATE(p.fecha)< DATE(NOW()) ";
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
            st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
            st.setInt(2, unidad.getId());
            res = st.executeQuery();
            if (res.next()) {
                total = res.getInt("total");
            }
        } catch (SQLException ex) {
            Logger.getLogger(PanelPartesPerdidosUnidades.class.getName()).log(Level.SEVERE, null, ex);
        }

        Obj.cerrar(res, st);
        return total;
    }

    private class ActualizarPartesTask extends org.jdesktop.application.Task<Object, Void> {

        Unidad u = null;

        ActualizarPartesTask(org.jdesktop.application.Application app) {
            super(app);
            modelo.vaciar();
            setProgress(1);
            int row = tablaCursos.getSelectedRow();
            if (row > -1) {
                UnidadesPartesNoEntregados pf = modeloCursos.getElemento(tablaCursos.convertRowIndexToModel(row));
                u = pf.getUnidad();
            }
        }

        @Override
        protected Object doInBackground() {
            if (u != null) {
                PreparedStatement st = null;
                ResultSet res = null;
                try {
                    int total = getTotalPartes(u);
                    String sql = "SELECT distinct p.*  FROM partes AS p "
                            + " JOIN partes_unidades AS pu ON pu.parte_id=p.id "
                            + " WHERE p.ano=? AND p.digitalizado=0 AND pu.unidad_id=? AND DATE(p.fecha)< DATE(NOW()) "
                            + " ORDER BY p.fecha,p.id ";
                    st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
                    st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
                    st.setInt(2, u.getId());
                    res = st.executeQuery();
                    int pos = 0;
                    while (res.next() && !isCancelled()) {
                        pos++;
                        setMessage(String.format("Cargando partes de asistencias %d de %d...", pos, total));
                        setProgress(pos, 0, total);
                        ParteFaltas pFaltas = new ParteFaltas();
                        pFaltas.cargarDesdeResultSet(res);
                        modelo.addDato(pFaltas);
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(PanelPartesPerdidosUnidades.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    Obj.cerrar(st, res);
                }
            }
            return null;
        }

        @Override
        protected void succeeded(Object result) {
            tabla.packAll();
        }
    }

    private class ActualizarProfesoresTask extends org.jdesktop.application.Task<Object, Void> {

        ActualizarProfesoresTask(org.jdesktop.application.Application app) {
            super(app);
            modeloCursos.vaciar();
        }

        @Override
        protected Object doInBackground() {
            PreparedStatement st = null;
            ResultSet res = null;
            try {
                String sql = "SELECT u.*,count(distinct p.id) AS total FROM partes AS p "
                        + " JOIN partes_unidades AS pu ON pu.parte_id=p.id "
                        + " JOIN unidades AS u ON u.id=pu.unidad_id "
                        + " WHERE p.ano=? AND p.digitalizado=0 AND DATE(p.fecha)< DATE(NOW()) "
                        + " GROUP BY u.id "
                        + " ORDER BY total DESC";
                st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
                st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
                res = st.executeQuery();
                int pos = 0;
                while (res.next() && !isCancelled()) {
                    pos++;
                    Unidad c = new Unidad();
                    c.cargarDesdeResultSet(res);
                    UnidadesPartesNoEntregados pn = new UnidadesPartesNoEntregados(c, res.getInt("total"));
                    modeloCursos.addDato(pn);
                }
            } catch (Exception ex) {
                Logger.getLogger(PanelPartesPerdidosUnidades.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                Obj.cerrar(st, res);
            }
            return null;
        }

        @Override
        protected void succeeded(Object result) {
            tablaCursos.packAll();
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
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bActualizar;
    private javax.swing.JToolBar bHerramientas;
    private javax.swing.JButton jButton1;
    private javax.swing.JScrollPane scroll;
    private javax.swing.JScrollPane scrollProfesores;
    private javax.swing.JSplitPane split;
    private org.jdesktop.swingx.JXTable tabla;
    private org.jdesktop.swingx.JXTable tablaCursos;
    // End of variables declaration//GEN-END:variables

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
}
