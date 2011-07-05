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
 * PanelProfesores.java
 *
 * Created on 3 de octubre de 2008, 10:59
 */
package com.codeko.apps.maimonides.profesores;

import com.codeko.apps.maimonides.DateCellEditor;
import com.codeko.apps.maimonides.ICargable;
import com.codeko.apps.maimonides.IPanel;
import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesBean;
import com.codeko.apps.maimonides.MaimonidesUtil;
import com.codeko.apps.maimonides.elementos.Profesor;
import com.codeko.apps.maimonides.horarios.impresion.HorarioImprimible;
import com.codeko.apps.maimonides.horarios.impresion.ImpresionHorarios;
import com.codeko.apps.maimonides.importadores.ImportadorDatosGeneralesSeneca;
import com.codeko.apps.maimonides.seneca.operaciones.actualizaciones.ImportarDatosBaseSenecaTask;
import com.codeko.apps.maimonides.usr.Permisos;
import com.codeko.apps.maimonides.web.EnviosWeb;
import com.codeko.apps.maimonides.web.UsuarioWeb;
import com.codeko.swing.CodekoTableModel;
import com.codeko.util.Fechas;
import com.codeko.util.Obj;
import com.mysql.jdbc.PreparedStatement;
import java.beans.Beans;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskEvent;
import org.jdesktop.application.TaskListener;
import org.jdesktop.swingx.JXTable;

/**
 *
 * @author  Codeko
 */
public class PanelProfesores extends javax.swing.JPanel implements IPanel, ICargable {

    CodekoTableModel<Profesor> modelo = new CodekoTableModel<Profesor>(new Profesor());
    boolean cargado = false;

    public boolean isBarraHerramientasVisible() {
        return barraHerramientas.isVisible();
    }

    public void setBarraHerramientasVisible(boolean visible) {
        barraHerramientas.setVisible(visible);
    }

    /** Creates new form PanelProfesores */
    public PanelProfesores() {
        initComponents();
        if (!Beans.isDesignTime()) {
            MaimonidesUtil.addMenuTabla(tabla, "Listado de Profesores");
            tabla.getColumnExt("Código Séneca").setVisible(false);
            tabla.setDefaultRenderer(GregorianCalendar.class, new DefaultTableCellRenderer() {

                @Override
                public void setValue(Object val) {
                    if (val instanceof GregorianCalendar) {
                        setText(Fechas.format((GregorianCalendar) val));
                    } else {
                        setText("");
                    }
                }
            });

            tabla.setDefaultEditor(GregorianCalendar.class, new DateCellEditor());
            tabla.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

                @Override
                public void valueChanged(ListSelectionEvent e) {
                    if (!e.getValueIsAdjusting()) {
                        int pos = tabla.getSelectedRow();
                        Profesor p = null;
                        if (pos != -1) {
                            pos = tabla.convertRowIndexToModel(pos);
                            p = modelo.getElemento(pos);
                            setProfesorSeleccionado(p != null);
                        } else {
                            setProfesorSeleccionado(false);
                        }
                        firePropertyChange("profesorAsignado", null, p);
                    }
                }
            });
        }
        setHayEnviosWeb(EnviosWeb.hayEnviosWeb());
    }

    public void desmarcar() {
        tabla.getSelectionModel().clearSelection();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scroll = new javax.swing.JScrollPane();
        tabla = new org.jdesktop.swingx.JXTable();
        barraHerramientas = new javax.swing.JToolBar();
        bActualizar = new javax.swing.JButton();
        bBorrar = new javax.swing.JButton();
        bNuevo = new javax.swing.JButton();
        bActualizarDatos = new javax.swing.JButton();
        bEnviarWeb = new javax.swing.JButton();
        bImprimirHorarioProfesoresSeleccionados = new javax.swing.JButton();
        bImprimirHorarios = new javax.swing.JButton();

        setName("maimonides.paneles.datos.profesores"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        scroll.setName("scroll"); // NOI18N

        tabla.setModel(modelo);
        tabla.setColumnControlVisible(true);
        tabla.setName("tabla"); // NOI18N
        tabla.setShowGrid(true);
        tabla.setSurrendersFocusOnKeystroke(true);
        tabla.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                tablaAncestorAdded(evt);
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });
        scroll.setViewportView(tabla);

        add(scroll, java.awt.BorderLayout.CENTER);

        barraHerramientas.setRollover(true);
        barraHerramientas.setName("barraHerramientas"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getActionMap(PanelProfesores.class, this);
        bActualizar.setAction(actionMap.get("cargarProfesores")); // NOI18N
        bActualizar.setFocusable(false);
        bActualizar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bActualizar.setName("bActualizar"); // NOI18N
        bActualizar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraHerramientas.add(bActualizar);

        bBorrar.setAction(actionMap.get("borrarProfesor")); // NOI18N
        bBorrar.setFocusable(false);
        bBorrar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bBorrar.setName("bBorrar"); // NOI18N
        bBorrar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraHerramientas.add(bBorrar);

        bNuevo.setAction(actionMap.get("nuevoProfesor")); // NOI18N
        bNuevo.setFocusable(false);
        bNuevo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bNuevo.setName("bNuevo"); // NOI18N
        bNuevo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraHerramientas.add(bNuevo);

        bActualizarDatos.setAction(actionMap.get("actualizarDatos")); // NOI18N
        bActualizarDatos.setFocusable(false);
        bActualizarDatos.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bActualizarDatos.setName("bActualizarDatos"); // NOI18N
        bActualizarDatos.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraHerramientas.add(bActualizarDatos);

        bEnviarWeb.setAction(actionMap.get("enviarWeb")); // NOI18N
        bEnviarWeb.setFocusable(false);
        bEnviarWeb.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bEnviarWeb.setName("bEnviarWeb"); // NOI18N
        bEnviarWeb.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraHerramientas.add(bEnviarWeb);

        bImprimirHorarioProfesoresSeleccionados.setAction(actionMap.get("imprimirHorariosProfesoresSeleccionados")); // NOI18N
        bImprimirHorarioProfesoresSeleccionados.setFocusable(false);
        bImprimirHorarioProfesoresSeleccionados.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bImprimirHorarioProfesoresSeleccionados.setName("bImprimirHorarioProfesoresSeleccionados"); // NOI18N
        bImprimirHorarioProfesoresSeleccionados.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraHerramientas.add(bImprimirHorarioProfesoresSeleccionados);

        bImprimirHorarios.setAction(actionMap.get("imprimirHorarios")); // NOI18N
        bImprimirHorarios.setFocusable(false);
        bImprimirHorarios.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bImprimirHorarios.setName("bImprimirHorarios"); // NOI18N
        bImprimirHorarios.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraHerramientas.add(bImprimirHorarios);

        add(barraHerramientas, java.awt.BorderLayout.PAGE_START);
    }// </editor-fold>//GEN-END:initComponents

private void tablaAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_tablaAncestorAdded
    cargar();
}//GEN-LAST:event_tablaAncestorAdded

    @Override
    public boolean puedoSusituir() {
        return true;
    }

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task cargarProfesores() {
        return new CargarProfesoresTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    @Override
    public void cargar() {
        if (!Beans.isDesignTime() && !isCargado()) {
            MaimonidesUtil.ejecutarTask(this, "cargarProfesores");
        }
    }

    @Override
    public void vaciar() {
        modelo.vaciar();
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

    private class CargarProfesoresTask extends org.jdesktop.application.Task<Vector<Profesor>, Void> {

        CargarProfesoresTask(org.jdesktop.application.Application app) {
            super(app);
            vaciar();
            setCargado(true);
        }

        @Override
        protected Vector<Profesor> doInBackground() {
            Vector<Profesor> ret = new Vector<Profesor>();
            if (!Beans.isDesignTime()) {
                try {
                    if (Permisos.isUsuarioSoloProfesor() && Permisos.getFiltroProfesor() != null) {
                        ret.add(Permisos.getFiltroProfesor());
                    } else {
                        PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM profesores_ WHERE ano=? ORDER BY nombre,apellido1,apellido2");
                        st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
                        ResultSet res = st.executeQuery();
                        while (res.next()) {
                            Profesor p = new Profesor();
                            try {
                                p.cargarDesdeResultSet(res);
                                ret.add(p);
                            } catch (SQLException ex) {
                                Logger.getLogger(PanelProfesores.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (Exception ex) {
                                Logger.getLogger(PanelProfesores.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        Obj.cerrar(st, res);
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(PanelProfesores.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            return ret;  // return your result
        }

        @Override
        protected void succeeded(Vector<Profesor> result) {
            modelo.addDatos(result);
            tabla.packAll();

        }
    }

    @Action(block = Task.BlockingScope.APPLICATION, enabledProperty = "profesorSeleccionado")
    public Task borrarProfesor() {
        return new BorrarProfesorTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class BorrarProfesorTask extends org.jdesktop.application.Task<Vector<Profesor>, Void> {

        Vector<Profesor> profs = new Vector<Profesor>();

        BorrarProfesorTask(org.jdesktop.application.Application app) {
            super(app);
            int[] filas = tabla.getSelectedRows();
            int op = JOptionPane.showConfirmDialog(MaimonidesApp.getApplication().getMainFrame(), "¿Esta seguro de que desea borrar los profesores seleccionados (" + filas.length + ")?", "Borrar", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (op == JOptionPane.YES_OPTION) {
                for (int i : filas) {
                    int row = tabla.convertRowIndexToModel(i);
                    Profesor p = modelo.getElemento(row);
                    profs.add(p);
                }
                modelo.quitarDatos(profs);
            }
        }

        @Override
        protected Vector<Profesor> doInBackground() {
            firePropertyChange("message", null, "Borrando profesores...");
            Vector<Profesor> noBorrados = new Vector<Profesor>();
            if (profs != null) {
                for (Profesor p : profs) {
                    if (!p.borrar()) {
                        noBorrados.add(p);
                    }
                }
            }
            return noBorrados;
        }

        @Override
        protected void succeeded(Vector<Profesor> result) {
            if (result != null && result.size() > 0) {
                JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), "Algunos profesores no se han podido borrar.", "Borrar profesores", JOptionPane.WARNING_MESSAGE);
                modelo.addDatos(result);
            }
        }
    }
    private boolean profesorSeleccionado = false;

    public boolean isProfesorSeleccionado() {
        return profesorSeleccionado;
    }

    public void setProfesorSeleccionado(boolean b) {
        boolean old = isProfesorSeleccionado();
        this.profesorSeleccionado = b;
        firePropertyChange("profesorSeleccionado", old, isProfesorSeleccionado());
    }

    @Action(block = Task.BlockingScope.ACTION)
    public Task nuevoProfesor() {
        return new NuevoProfesorTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class NuevoProfesorTask extends org.jdesktop.application.Task<Profesor, Void> {

        NuevoProfesorTask(org.jdesktop.application.Application app) {
            super(app);

        }

        @Override
        protected Profesor doInBackground() {
            Profesor p = new Profesor();
            p.setNombre("");
            p.guardar();
            return p;
        }

        @Override
        protected void succeeded(Profesor result) {
            modelo.addDato(result);
            int row = tabla.getRowCount() - 1;
            tabla.scrollRowToVisible(row);
            tabla.editCellAt(row, 0);
            tabla.requestFocus();
            TableCellEditor tce = tabla.getCellEditor(row, 0);
            tce.shouldSelectCell(new ListSelectionEvent(tabla, row, row, false));
        }
    }

    public JXTable getTabla() {
        return tabla;
    }

    @Action(block = Task.BlockingScope.APPLICATION, enabledProperty = "hayEnviosWeb")
    public Task enviarWeb() {
        return new EnviarWebTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class EnviarWebTask extends org.jdesktop.application.Task<Short, Void> {

        EnviosWeb ew = null;

        EnviarWebTask(org.jdesktop.application.Application app) {
            super(app);
            int op = JOptionPane.showConfirmDialog(MaimonidesApp.getApplication().getMainFrame(), "Se va a enviar el listado de profesores a la web,\nlos profesores que no existan se desactivarán\ny los nuevos profesores se crearán.\n¿Continuar?", "Confirmación", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (op != JOptionPane.OK_OPTION) {
                cancel(false);
            }
        }

        @Override
        protected Short doInBackground() {
            //Ahora cojemos cada elemento de la tabla y creamos un usuario web de tipo
            setMessage("Creando paquete de datos...");
            Vector<UsuarioWeb> usr = EnviosWeb.getUsuariosProfesoresWeb(modelo.getDatos());
            ew = new EnviosWeb(EnviosWeb.TIPO_ENVIO_USUARIOS);
            ew.addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                }
            });
            ew.addElementos(usr);
            return ew.enviar();
        }

        @Override
        protected void succeeded(Short result) {
            String titulo = "Datos enviados correctamente";
            int tipo = JOptionPane.INFORMATION_MESSAGE;
            if (result < 1) {
                titulo = "Error enviando datos";
                tipo = JOptionPane.ERROR_MESSAGE;
            }
            setMessage(titulo);
            JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), ew.getUltimoMensaje(), titulo, tipo);
        }
    }
    private boolean hayEnviosWeb = false;

    public boolean isHayEnviosWeb() {
        return hayEnviosWeb;
    }

    public void setHayEnviosWeb(boolean b) {
        boolean old = isHayEnviosWeb();
        this.hayEnviosWeb = b;
        firePropertyChange("hayEnviosWeb", old, isHayEnviosWeb());
    }

    @SuppressWarnings("unchecked")
    @Action(block = Task.BlockingScope.APPLICATION)
    public Task actualizarDatos() {
        Task t = new ImportarDatosBaseSenecaTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class), ImportadorDatosGeneralesSeneca.PROFESORES);
        TaskListener tl = new TaskListener() {

            @Override
            public void doInBackground(TaskEvent event) {
            }

            @Override
            public void process(TaskEvent event) {
            }

            @Override
            public void succeeded(TaskEvent event) {
                setCargado(false);
                cargar();
            }

            @Override
            public void failed(TaskEvent event) {
            }

            @Override
            public void cancelled(TaskEvent event) {
            }

            @Override
            public void interrupted(TaskEvent event) {
            }

            @Override
            public void finished(TaskEvent event) {
            }
        };
        t.addTaskListener(tl);
        return t;
    }

        @Action(block = Task.BlockingScope.ACTION, enabledProperty = "profesorSeleccionado")
    public Task imprimirHorariosProfesoresSeleccionados() {
        return new ImprimirHorariosProfesoresSeleccionadosTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class), true);
    }

    private class ImprimirHorariosProfesoresSeleccionadosTask extends org.jdesktop.application.Task<Object, Void> {

        ArrayList<Profesor> profesores = new ArrayList<Profesor>();

        ImprimirHorariosProfesoresSeleccionadosTask(org.jdesktop.application.Application app, boolean seleccionados) {
            super(app);
            if (seleccionados) {
                for (int i : tabla.getSelectedRows()) {
                    int x = tabla.convertRowIndexToModel(i);
                    Profesor p = modelo.getElemento(x);
                    profesores.add(p);
                }
            } else {
                profesores.addAll(modelo.getDatos());
            }
        }

        @Override
        protected Object doInBackground() {
            if (!profesores.isEmpty()) {
                ImpresionHorarios imp = new ImpresionHorarios();
                ArrayList<HorarioImprimible> horarios = new ArrayList<HorarioImprimible>();
                for (Profesor p : profesores) {
                    HorarioImprimible h = new HorarioImprimible(null, p, null);
                    horarios.add(h);
                }
                MaimonidesBean bean = new MaimonidesBean();
                bean.addPropertyChangeListener(new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                    }
                });
                imp.imprimirHorarios(bean, horarios,false);
            }
            return null;
        }

        @Override
        protected void succeeded(Object result) {
        }
    }

        @Action(block = Task.BlockingScope.ACTION)
    public Task imprimirHorarios() {
        return new ImprimirHorariosProfesoresSeleccionadosTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class), false);
    }

    private class ImprimirHorariosTask extends org.jdesktop.application.Task<Object, Void> {
        ImprimirHorariosTask(org.jdesktop.application.Application app) {
            // Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to ImprimirHorariosTask fields, here.
            super(app);
        }
        @Override protected Object doInBackground() {
            // Your Task's code here.  This method runs
            // on a background thread, so don't reference
            // the Swing GUI from here.
            return null;  // return your result
        }
        @Override protected void succeeded(Object result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bActualizar;
    private javax.swing.JButton bActualizarDatos;
    private javax.swing.JButton bBorrar;
    private javax.swing.JButton bEnviarWeb;
    private javax.swing.JButton bImprimirHorarioProfesoresSeleccionados;
    private javax.swing.JButton bImprimirHorarios;
    private javax.swing.JButton bNuevo;
    private javax.swing.JToolBar barraHerramientas;
    private javax.swing.JScrollPane scroll;
    private org.jdesktop.swingx.JXTable tabla;
    // End of variables declaration//GEN-END:variables
}
