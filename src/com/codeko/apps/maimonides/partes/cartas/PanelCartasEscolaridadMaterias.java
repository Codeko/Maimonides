/*
 * PanelCartasEscolaridadMaterias.java
 *
 * Created on 31-ago-2009, 11:34:16
 */
package com.codeko.apps.maimonides.partes.cartas;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesUtil;
import com.codeko.apps.maimonides.asistencia.escolaridad.DatoPerdidaEscolaridadPorMaterias;
import com.codeko.apps.maimonides.notificaciones.GestorNotificaciones;
import com.codeko.apps.maimonides.usr.Permisos;
import com.codeko.swing.CodekoAutoTableModel;
import com.codeko.util.Fechas;
import java.beans.Beans;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;

/**
 *
 * @author Codeko
 */
public class PanelCartasEscolaridadMaterias extends javax.swing.JPanel {

    CodekoAutoTableModel<DatoCartaPerdidaEscolaridadPorMaterias> modelo = new CodekoAutoTableModel<DatoCartaPerdidaEscolaridadPorMaterias>(DatoCartaPerdidaEscolaridadPorMaterias.class);
    boolean cargado = false;
    GregorianCalendar fecha = null;

    /** Creates new form PanelCartasEscolaridadMaterias */
    public PanelCartasEscolaridadMaterias() {
        initComponents();
        MaimonidesUtil.addMenuTabla(tabla, "Pérdidas de evaluación continua por materias");
        if (!Beans.isDesignTime()) {
            tfFecha.setDate(null);
        }
        MaimonidesUtil.setFormatosFecha(tfFecha, false);
    }

    public boolean isCargado() {
        return cargado;
    }

    public void setCargado(boolean cargado) {
        this.cargado = cargado;
    }

    @Action(block = Task.BlockingScope.WINDOW)
    public Task actualizar() {
        return new ActualizarTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class ActualizarTask extends org.jdesktop.application.Task<ArrayList<DatoCartaPerdidaEscolaridadPorMaterias>, Void> {

        ActualizarTask(org.jdesktop.application.Application app) {
            super(app);
            modelo.vaciar();
            setCargado(false);
            try {
                tfFecha.commitEdit();
            } catch (ParseException ex) {
                Logger.getLogger(PanelCartasEscolaridadMaterias.class.getName()).log(Level.SEVERE, null, ex);
            }
            fecha = Fechas.toGregorianCalendar(tfFecha.getDate());
        }

        @Override
        protected ArrayList<DatoCartaPerdidaEscolaridadPorMaterias> doInBackground() {
            setMessage("Cargando pérdidas de evaluación continua...");
            ArrayList<DatoPerdidaEscolaridadPorMaterias> ret = DatoPerdidaEscolaridadPorMaterias.getDatosPerdidaEscolaridad(100, null, Permisos.getFiltroUnidad(), null, fecha);

            ArrayList<DatoCartaPerdidaEscolaridadPorMaterias> datos = new ArrayList<DatoCartaPerdidaEscolaridadPorMaterias>();
            //Tenemos que unir todos los datos segun el alumno
            for (DatoPerdidaEscolaridadPorMaterias d1 : ret) {
                //vemos si ya existe el alumno
                DatoCartaPerdidaEscolaridadPorMaterias d2 = null;
                for (DatoCartaPerdidaEscolaridadPorMaterias d : datos) {
                    if (d.getAlumno().equals(d1.getAlumno())) {
                        d2 = d;
                        break;
                    }
                }
                if (d2 == null) {
                    d2 = new DatoCartaPerdidaEscolaridadPorMaterias(d1.getAlumno());
                    datos.add(d2);
                }
                d2.addDato(d1);
            }
            return datos;
        }

        @Override
        protected void succeeded(ArrayList<DatoCartaPerdidaEscolaridadPorMaterias> result) {
            setMessage("Pérdidas de evaluación continua cargadas correctamente.");
            modelo.addDatos(result);
            tabla.packAll();
            setCargado(true);

        }
    }

    @Action(block = Task.BlockingScope.WINDOW)
    public Task enviarTodos() {
        return new EnviarTodosTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class EnviarTodosTask extends org.jdesktop.application.Task<Boolean, Void> {

        EnviarTodosTask(org.jdesktop.application.Application app) {
            super(app);
        }

        @Override
        protected Boolean doInBackground() {
            setMessage("Enviando...");
            ArrayList<DatoCartaPerdidaEscolaridadPorMaterias> datos =new ArrayList<DatoCartaPerdidaEscolaridadPorMaterias>();
            for (int i = 0; i < tabla.getRowCount(); i++) {
                datos.add(modelo.getElemento(tabla.convertRowIndexToModel(i)));
            }
            setProgress(0, 0, datos.size());
            GestorNotificaciones gestor = new GestorNotificaciones();
            return gestor.enviarNotificacionesPerdidaEvaluacionContinuaMaterias(fecha, datos);
        }

        @Override
        protected void succeeded(Boolean result) {
            if (result) {
                setMessage("Cartas impresas/enviadas correctamente.");
                JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), "Cartas impresas/enviadas correctamente", "Enviar", JOptionPane.INFORMATION_MESSAGE);
            } else {
                setMessage("Ha habido algún error enviando las notificaciónes.");
            }
            MaimonidesUtil.ejecutarTask(this, "actualizar");
        }
    }

    @Action(block = Task.BlockingScope.WINDOW)
    public Task enviarSeleccionados() {
        return new EnviarSeleccionadosTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class EnviarSeleccionadosTask extends org.jdesktop.application.Task<Boolean, Void> {

        EnviarSeleccionadosTask(org.jdesktop.application.Application app) {
            super(app);
        }

        @Override
        protected Boolean doInBackground() {
            setMessage("Enviando...");
            ArrayList<DatoCartaPerdidaEscolaridadPorMaterias> datos = new ArrayList<DatoCartaPerdidaEscolaridadPorMaterias>();
            for (int i : tabla.getSelectedRows()) {
                datos.add(modelo.getElemento(tabla.convertRowIndexToModel(i)));
            }
            setProgress(0, 0, datos.size());
            GestorNotificaciones gestor = new GestorNotificaciones();
            return gestor.enviarNotificacionesPerdidaEvaluacionContinuaMaterias(fecha, datos);
        }

        @Override
        protected void succeeded(Boolean result) {
            if (result) {
                setMessage("Cartas impresas/enviadas correctamente.");
                JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), "Cartas impresas/enviadas correctamente", "Enviar", JOptionPane.INFORMATION_MESSAGE);
            }else{
                setMessage("Ha habido algún error enviando las notificaciones.");
            }
            MaimonidesUtil.ejecutarTask(this, "actualizar");
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToolBar1 = new javax.swing.JToolBar();
        bActualizar = new javax.swing.JButton();
        bEnviarTodos = new javax.swing.JButton();
        bEnviarSeleccionados = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabla = new org.jdesktop.swingx.JXTable();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        tfFecha = new org.jdesktop.swingx.JXDatePicker();
        jLabel2 = new javax.swing.JLabel();

        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        jToolBar1.setName("jToolBar1"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getActionMap(PanelCartasEscolaridadMaterias.class, this);
        bActualizar.setAction(actionMap.get("actualizar")); // NOI18N
        bActualizar.setFocusable(false);
        bActualizar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bActualizar.setName("bActualizar"); // NOI18N
        bActualizar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(bActualizar);

        bEnviarTodos.setAction(actionMap.get("enviarTodos")); // NOI18N
        bEnviarTodos.setFocusable(false);
        bEnviarTodos.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bEnviarTodos.setName("bEnviarTodos"); // NOI18N
        bEnviarTodos.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(bEnviarTodos);

        bEnviarSeleccionados.setAction(actionMap.get("enviarSeleccionados")); // NOI18N
        bEnviarSeleccionados.setFocusable(false);
        bEnviarSeleccionados.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bEnviarSeleccionados.setName("bEnviarSeleccionados"); // NOI18N
        bEnviarSeleccionados.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(bEnviarSeleccionados);

        add(jToolBar1, java.awt.BorderLayout.PAGE_START);

        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        tabla.setModel(modelo);
        tabla.setName("tabla"); // NOI18N
        tabla.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                tablaAncestorAdded(evt);
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });
        jScrollPane1.setViewportView(tabla);

        jPanel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getResourceMap(PanelCartasEscolaridadMaterias.class);
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N
        jPanel2.add(jLabel1);

        tfFecha.setName("tfFecha"); // NOI18N
        jPanel2.add(tfFecha);

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N
        jPanel2.add(jLabel2);

        jPanel1.add(jPanel2, java.awt.BorderLayout.NORTH);

        add(jPanel1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void tablaAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_tablaAncestorAdded
//        if (!Beans.isDesignTime() && !isCargado()) {
//            MaimonidesUtil.ejecutarTask(this, "actualizar");
//        }
    }//GEN-LAST:event_tablaAncestorAdded
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bActualizar;
    private javax.swing.JButton bEnviarSeleccionados;
    private javax.swing.JButton bEnviarTodos;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar jToolBar1;
    private org.jdesktop.swingx.JXTable tabla;
    private org.jdesktop.swingx.JXDatePicker tfFecha;
    // End of variables declaration//GEN-END:variables
}
