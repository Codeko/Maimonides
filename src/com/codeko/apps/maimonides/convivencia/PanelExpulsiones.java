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
 * PanelExpulsiones.java
 *
 * Created on 25-ago-2009, 13:00:54
 */
package com.codeko.apps.maimonides.convivencia;

import com.codeko.apps.maimonides.DateCellEditor;
import com.codeko.apps.maimonides.IPanel;
import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesUtil;
import com.codeko.apps.maimonides.alumnos.PanelBusquedaAlumnos;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.usr.Permisos;
import com.codeko.swing.CodekoAutoTableModel;
import com.codeko.util.Fechas;
import com.codeko.util.Obj;
import com.mysql.jdbc.PreparedStatement;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;

/**
 *
 * @author Codeko
 */
public class PanelExpulsiones extends javax.swing.JPanel implements IPanel {

    CodekoAutoTableModel<Expulsion> modelo = new CodekoAutoTableModel<Expulsion>(Expulsion.class) {

        @Override
        public void elementoModificado(Expulsion elemento, int col, Object valor) {
            elemento.guardar();
        }
    };
    PanelBusquedaAlumnos panelBusqueda = new PanelBusquedaAlumnos();
    JFrame frameBusquedaAlumnos = new JFrame("Búsqueda de alumnos");
    Alumno alumno = null;
    PanelExpulsiones auto = this;

    /** Creates new form PanelExpulsiones */
    public PanelExpulsiones() {
        initComponents();
        frameBusquedaAlumnos.add(panelBusqueda, BorderLayout.CENTER);
        frameBusquedaAlumnos.setAlwaysOnTop(true);
        frameBusquedaAlumnos.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frameBusquedaAlumnos.setName("frameBusquedaAlumnos");
        panelBusqueda.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("alumnoSeleccionado".equals(evt.getPropertyName())) {
                    if (panelBusqueda.isCerrarAlSeleccionar()) {
                        frameBusquedaAlumnos.setVisible(false);
                    }
                    if (panelBusqueda.getAlumnoSeleccionado() != null) {
                        setAlumno(panelBusqueda.getAlumnoSeleccionado());
                        panelBusqueda.setAlumnoSeleccionado(null);
                    }
                } else if ("enterPulsado".equals(evt.getPropertyName())) {
                    //TODO Acceso a campos de ficha
                }
            }
        });
        tabla.setDefaultRenderer(GregorianCalendar.class, new DefaultTableCellRenderer() {

            @Override
            public void setValue(Object value) {
                if (value instanceof GregorianCalendar) {
                    setText(Fechas.format((GregorianCalendar) value));
                }
            }
        });
        tabla.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                setExpulsionSeleccionada(tabla.getSelectedRowCount() > 0);
                if (isExpulsionSeleccionada()) {
                    int row = tabla.getSelectedRow();
                    if (row > -1) {
                        //expulsionClickeada(modelo.getElemento(tabla.convertRowIndexToModel(row)));
                        auto.firePropertyChange("seleccionadaExpulsion", auto, modelo.getElemento(tabla.convertRowIndexToModel(row)));
                    }
                } else {
                    auto.firePropertyChange("seleccionadaExpulsion", auto, null);
                }
            }
        });
        tabla.setDefaultEditor(GregorianCalendar.class, new DateCellEditor());
        setDesmarcarVisible(false);
        bNuevo.setVisible(Permisos.creacion(getClass()));
        bBorrar.setVisible(Permisos.borrado(getClass()));
    }

    public void setModoCompacto() {
        panelSeleccionAlumno.setVisible(false);
    }

    public final void setDesmarcarVisible(boolean visible) {
        bDesmarcar.setVisible(visible);
    }

    public boolean isDesmarcarVisible() {
        return bDesmarcar.isVisible();
    }

    public static Expulsion mostrarSelectorExpulsiones(Alumno alumno) {
        Expulsion e = null;
        if (alumno != null && alumno.getId() != null) {
            PanelExpulsiones p = new PanelExpulsiones();
            p.setModoCompacto();
            p.barraHerramientas.setVisible(false);
            p.tabla.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {
                }
            });
            p.tabla.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            p.tabla.setEditable(false);
            p.setAlumno(alumno);
            p.setPreferredSize(new Dimension(400, 500));
            int op = JOptionPane.showConfirmDialog(MaimonidesApp.getApplication().getMainFrame(), p, "Seleccione la expulsión a asignar", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (op == JOptionPane.OK_OPTION) {
                int row = p.tabla.getSelectedRow();
                if (row > -1) {
                    e = p.modelo.getElemento(p.tabla.convertRowIndexToModel(row));
                }
            }
        }
        return e;
    }

    public void setSelectorAlumnoVisible(boolean visible) {
        panelSeleccionAlumno.setVisible(visible);
    }

    public boolean isSelectorAlumnoVisible() {
        return panelSeleccionAlumno.isVisible();
    }

    public Alumno getAlumno() {
        return alumno;
    }

    public void setAlumno(Alumno alumno) {
        this.alumno = alumno;
        if (alumno != null) {
            lNombreAlumno.setText(alumno.getNombreFormateado());
        } else {
            lNombreAlumno.setText("SIN SELECCIONAR");
        }
        setAlumnoAsignado(alumno != null && alumno.getId() != null);
        MaimonidesUtil.ejecutarTask(this, "actualizar");
    }

    @Action
    public void mostraBusquedaAlumnos() {
        MaimonidesApp.getApplication().show(frameBusquedaAlumnos);
    }

        @Action(block = Task.BlockingScope.COMPONENT, enabledProperty = "alumnoAsignado")
    public Task nuevo() {
        return new NuevoTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));

    }

    @Override
    public boolean puedoSusituir() {
        return true;
    }

    private class NuevoTask extends org.jdesktop.application.Task<Expulsion, Void> {

        NuevoTask(org.jdesktop.application.Application app) {
            super(app);
        }

        @Override
        protected Expulsion doInBackground() {
            Expulsion e = new Expulsion();
            e.setAlumno(getAlumno());
            if (e.guardar()) {
                return e;
            }
            return null;
        }

        @Override
        protected void succeeded(Expulsion e) {
            if (e != null) {
                modelo.addDato(e);
            } else {
                setMessage("Se ha producido algún error creando la nueva expulsión.");
            }
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        barraHerramientas = new javax.swing.JToolBar();
        bActualizar = new javax.swing.JButton();
        bNuevo = new javax.swing.JButton();
        bBorrar = new javax.swing.JButton();
        bDesmarcar = new javax.swing.JButton();
        panelGeneral = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabla = new org.jdesktop.swingx.JXTable();
        panelSeleccionAlumno = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        lNombreAlumno = new javax.swing.JLabel();
        bSeleccionarAlumno = new javax.swing.JButton();

        setName("maimonides.paneles.convivencia.expulsiones"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        barraHerramientas.setFloatable(false);
        barraHerramientas.setRollover(true);
        barraHerramientas.setName("barraHerramientas"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getActionMap(PanelExpulsiones.class, this);
        bActualizar.setAction(actionMap.get("actualizar")); // NOI18N
        bActualizar.setFocusable(false);
        bActualizar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bActualizar.setName("bActualizar"); // NOI18N
        bActualizar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraHerramientas.add(bActualizar);

        bNuevo.setAction(actionMap.get("nuevo")); // NOI18N
        bNuevo.setFocusable(false);
        bNuevo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bNuevo.setName("bNuevo"); // NOI18N
        bNuevo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraHerramientas.add(bNuevo);

        bBorrar.setAction(actionMap.get("borrar")); // NOI18N
        bBorrar.setFocusable(false);
        bBorrar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bBorrar.setName("bBorrar"); // NOI18N
        bBorrar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraHerramientas.add(bBorrar);

        bDesmarcar.setAction(actionMap.get("desmarcar")); // NOI18N
        bDesmarcar.setFocusable(false);
        bDesmarcar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bDesmarcar.setName("bDesmarcar"); // NOI18N
        bDesmarcar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraHerramientas.add(bDesmarcar);

        add(barraHerramientas, java.awt.BorderLayout.PAGE_START);

        panelGeneral.setName("panelGeneral"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        tabla.setModel(modelo);
        tabla.setName("tabla"); // NOI18N
        jScrollPane1.setViewportView(tabla);

        panelSeleccionAlumno.setName("panelSeleccionAlumno"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getResourceMap(PanelExpulsiones.class);
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        lNombreAlumno.setFont(resourceMap.getFont("lNombreAlumno.font")); // NOI18N
        lNombreAlumno.setText(resourceMap.getString("lNombreAlumno.text")); // NOI18N
        lNombreAlumno.setName("lNombreAlumno"); // NOI18N

        bSeleccionarAlumno.setAction(actionMap.get("mostraBusquedaAlumnos")); // NOI18N
        bSeleccionarAlumno.setName("bSeleccionarAlumno"); // NOI18N

        javax.swing.GroupLayout panelSeleccionAlumnoLayout = new javax.swing.GroupLayout(panelSeleccionAlumno);
        panelSeleccionAlumno.setLayout(panelSeleccionAlumnoLayout);
        panelSeleccionAlumnoLayout.setHorizontalGroup(
            panelSeleccionAlumnoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelSeleccionAlumnoLayout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lNombreAlumno, javax.swing.GroupLayout.DEFAULT_SIZE, 215, Short.MAX_VALUE)
                .addGap(14, 14, 14)
                .addComponent(bSeleccionarAlumno))
        );
        panelSeleccionAlumnoLayout.setVerticalGroup(
            panelSeleccionAlumnoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSeleccionAlumnoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(lNombreAlumno)
                .addComponent(bSeleccionarAlumno)
                .addComponent(jLabel1))
        );

        javax.swing.GroupLayout panelGeneralLayout = new javax.swing.GroupLayout(panelGeneral);
        panelGeneral.setLayout(panelGeneralLayout);
        panelGeneralLayout.setHorizontalGroup(
            panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelGeneralLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(panelSeleccionAlumno, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 334, Short.MAX_VALUE)
        );
        panelGeneralLayout.setVerticalGroup(
            panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelGeneralLayout.createSequentialGroup()
                .addComponent(panelSeleccionAlumno, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 224, Short.MAX_VALUE))
        );

        add(panelGeneral, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    private boolean alumnoAsignado = false;

    public boolean isAlumnoAsignado() {
        return alumnoAsignado;
    }

    public void setAlumnoAsignado(boolean b) {
        boolean old = isAlumnoAsignado();
        this.alumnoAsignado = b;
        firePropertyChange("alumnoAsignado", old, isAlumnoAsignado());
    }

        @Action(block = Task.BlockingScope.COMPONENT, enabledProperty = "expulsionSeleccionada")
    public Task borrar() {
        return new BorrarTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class BorrarTask extends org.jdesktop.application.Task<ArrayList<Expulsion>, Void> {

        BorrarTask(org.jdesktop.application.Application app) {
            super(app);
            boolean ok = tabla.getSelectedRowCount() > 0;
            if (ok) {
                int op = JOptionPane.showConfirmDialog(MaimonidesApp.getApplication().getMainFrame(), "¿Está seguro de que desea eliminar las expulsiones seleccionadas?", "Borrar", JOptionPane.OK_CANCEL_OPTION);
                if (op == JOptionPane.OK_OPTION) {
                    ok = true;
                } else {
                    ok = false;
                }
            }
            if (!ok) {
                cancel(false);
            }
        }

        @Override
        protected ArrayList<Expulsion> doInBackground() {
            ArrayList<Expulsion> datos = new ArrayList<Expulsion>();
            for (int r : tabla.getSelectedRows()) {
                Expulsion e = modelo.getElemento(tabla.convertRowIndexToModel(r));
                if (e.borrar()) {
                    datos.add(e);
                }
            }
            return datos;
        }

        @Override
        protected void succeeded(ArrayList<Expulsion> result) {
            modelo.quitarDatos(result);
            setMessage(result.size() + " expulsiones borradas correctamente.");
        }
    }
    private boolean expulsionSeleccionada = false;

    public boolean isExpulsionSeleccionada() {
        return expulsionSeleccionada;
    }

    public void setExpulsionSeleccionada(boolean b) {
        boolean old = isExpulsionSeleccionada();
        this.expulsionSeleccionada = b;
        firePropertyChange("expulsionSeleccionada", old, isExpulsionSeleccionada());
    }

        @Action(block = Task.BlockingScope.COMPONENT, enabledProperty = "alumnoAsignado")
    public Task actualizar() {
        return new ActualizarTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class ActualizarTask extends org.jdesktop.application.Task<ArrayList<Expulsion>, Void> {

        ActualizarTask(org.jdesktop.application.Application app) {
            super(app);
            modelo.vaciar();
        }

        @Override
        protected ArrayList<Expulsion> doInBackground() {
            ArrayList<Expulsion> datos = new ArrayList<Expulsion>();
            if (getAlumno() != null && getAlumno().getId() != null) {
                PreparedStatement st = null;
                ResultSet res = null;
                try {
                    st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM expulsiones WHERE alumno_id=?");
                    st.setInt(1, getAlumno().getId());
                    res = st.executeQuery();
                    while (res.next()) {
                        Expulsion e = new Expulsion();
                        e.cargarDesdeResultSet(res);
                        datos.add(e);
                    }
                } catch (Exception ex) {
                    Logger.getLogger(PanelExpulsiones.class.getName()).log(Level.SEVERE, null, ex);
                }
                Obj.cerrar(st, res);
            }
            return datos;
        }

        @Override
        protected void succeeded(ArrayList<Expulsion> result) {
            modelo.addDatos(result);
            tabla.packAll();
        }
    }

    @Action
    public void desmarcar() {
        tabla.getSelectionModel().clearSelection();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bActualizar;
    private javax.swing.JButton bBorrar;
    private javax.swing.JButton bDesmarcar;
    private javax.swing.JButton bNuevo;
    private javax.swing.JButton bSeleccionarAlumno;
    private javax.swing.JToolBar barraHerramientas;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lNombreAlumno;
    private javax.swing.JPanel panelGeneral;
    private javax.swing.JPanel panelSeleccionAlumno;
    private org.jdesktop.swingx.JXTable tabla;
    // End of variables declaration//GEN-END:variables
}
