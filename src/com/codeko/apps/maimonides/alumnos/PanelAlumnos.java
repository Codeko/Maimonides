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


package com.codeko.apps.maimonides.alumnos;

import com.codeko.apps.maimonides.IPanel;
import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesUtil;
import com.codeko.apps.maimonides.apoyos.PanelApoyos;
import com.codeko.apps.maimonides.elementos.Alumno;

import com.codeko.apps.maimonides.elementos.Curso;
import com.codeko.apps.maimonides.elementos.Unidad;
import com.codeko.apps.maimonides.seneca.operaciones.actualizaciones.ActualizarAlumnosExtendidoTask;
import com.codeko.apps.maimonides.usr.Permisos;
import com.codeko.apps.maimonides.web.EnviosWeb;
import com.codeko.apps.maimonides.web.UsuarioWeb;
import com.codeko.swing.CodekoTableModel;
import com.codeko.util.Obj;
import com.mysql.jdbc.PreparedStatement;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskEvent;
import org.jdesktop.application.TaskListener;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.table.TableColumnExt;

public class PanelAlumnos extends javax.swing.JPanel implements IPanel {

    CodekoTableModel<Alumno> modelo = new CodekoTableModel<Alumno>(new Alumno());
    Object filtroAlumno = null;
    PanelAlumnos auto=this;
    /** Creates new form PanelAlumnos */
    public PanelAlumnos() {
        initComponents();
        MaimonidesUtil.addMenuTabla(tabla, "Listado de alumnos");
        MaimonidesUtil.implementarAccesoFichaAlumno(tabla);
        tabla.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getColumnExt("Código").setVisible(false);
        tabla.getColumnExt("N.Escolar").setVisible(false);
        TableColumnExt tc = tabla.getColumnExt("Bilingüe");
        tc.setPreferredWidth(80);
        tc = tabla.getColumnExt("Repetidor");
        tc.setPreferredWidth(80);
        tabla.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
                    int row = tabla.getSelectedRow();
                    if (row > -1) {
                        firePropertyChange("alumnoClickeado", null, getAlumnoSeleccionado());
                    }
                }
            }
        });
        tabla.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("alumnoClickeado".equals(evt.getPropertyName())) {
                    firePropertyChange("alumnoClickeado", null, evt.getNewValue());
                }
            }
        });
        tabla.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int pos = tabla.getSelectedRow();
                    if (pos != -1) {
                        pos = tabla.convertRowIndexToModel(pos);
                        Alumno objeto = modelo.getElemento(pos);
                        setAlumnoSeleccionado(objeto != null);
                    } else {
                        setAlumnoSeleccionado(false);
                    }
                }
            }
        });
        tc = tabla.getColumnExt("Unidad");
        tc.setCellEditor(new DefaultCellEditor(new JComboBox()) {

            @Override
            public Component getTableCellEditorComponent(JTable table,
                    Object value,
                    boolean isSelected,
                    int row,
                    int column) {
                Component c = super.getTableCellEditorComponent(table, value, isSelected, row, column);
                if (c instanceof JComboBox && value instanceof Unidad) {
                    Unidad ud = (Unidad) value;
                    JComboBox cb = (JComboBox) c;
                    cb.removeAllItems();
                    ArrayList<Unidad> unidades = Unidad.getUnidadesDisponibles(ud);
                    if (unidades.isEmpty()) {
                        cb.addItem(value);
                    } else {
                        for (Unidad u : unidades) {
                            cb.addItem(u);
                        }
                    }
                    cb.setSelectedItem(value);
                }
                return c;
            }
        });

        panelArbolUnidades1.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("seleccionArbol".equals(evt.getPropertyName())) {
                    Object obj = evt.getNewValue();
                    if (obj instanceof String) {
                        setFiltroAlumno(obj);
                    } else if (obj instanceof Unidad) {
                        setFiltroAlumno(obj);
                    } else {
                        setFiltroAlumno(null);
                    }
                }
            }
        });
        setHayEnviosWeb(EnviosWeb.hayEnviosWeb());

        modelo.setEditable(Permisos.edicion(getClass()));

        bBorrarAlumno.setVisible(Permisos.borrado(getClass()));

        bApoyos.setVisible(Permisos.especial(getClass(), "apoyos"));

        bActualizar.setVisible(Permisos.especial(getClass(), "importar"));

        bEnviarWeb.setVisible(Permisos.especial(getClass(), "enviarWeb"));
    }

    public Alumno getAlumnoSeleccionado() {
        Alumno a = null;
        int row = tabla.getSelectedRow();
        if (row > -1) {
            row = tabla.convertRowIndexToModel(row);
            a = modelo.getElemento(row);
        }
        return a;
    }

    public void setModoSeleccion() {
        barraHerramientas.setVisible(false);
        lInfo.setVisible(false);
        lTotal.setVisible(false);
        tabla.getColumnExt("Bilingüe").setVisible(false);
        tabla.getColumnExt("Repetidor").setVisible(false);
        tabla.getColumnExt("D.I.C.U.").setVisible(false);
    }

    public Object getFiltroAlumno() {
        return filtroAlumno;
    }

    public void setFiltroAlumno(Object filtroAlumno) {
        this.filtroAlumno = filtroAlumno;
        if (filtroAlumno instanceof Curso) {
            lInfo.setText(((Curso) filtroAlumno).toString());
            tabla.getColumnExt("Unidad").setVisible(true);
        } else if (filtroAlumno instanceof Unidad) {
            lInfo.setText(((Unidad) filtroAlumno).getDescripcion() + ": " + ((Unidad) filtroAlumno).getCursoGrupo());
            tabla.getColumnExt("Unidad").setVisible(false);
        } else {
            lInfo.setText("");
            tabla.getColumnExt("Unidad").setVisible(true);
        }
        MaimonidesUtil.ejecutarTask(this, "cargarAlumnos");
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelSeparador = new javax.swing.JSplitPane();
        panelArbolUnidades1 = new com.codeko.apps.maimonides.cursos.PanelArbolUnidades();
        panelTabla = new javax.swing.JPanel();
        scrollTabla = new javax.swing.JScrollPane();
        tabla = new org.jdesktop.swingx.JXTable();
        lInfo = new javax.swing.JLabel();
        lTotal = new javax.swing.JLabel();
        barraHerramientas = new javax.swing.JToolBar();
        bBorrarAlumno = new javax.swing.JButton();
        bApoyos = new javax.swing.JButton();
        bActualizar = new javax.swing.JButton();
        bEnviarWeb = new javax.swing.JButton();

        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        panelSeparador.setDividerLocation(150);
        panelSeparador.setDividerSize(8);
        panelSeparador.setName("panelSeparador"); // NOI18N
        panelSeparador.setOneTouchExpandable(true);

        panelArbolUnidades1.setName("panelArbolUnidades1"); // NOI18N
        panelSeparador.setLeftComponent(panelArbolUnidades1);

        panelTabla.setName("panelTabla"); // NOI18N

        scrollTabla.setName("scrollTabla"); // NOI18N

        tabla.setModel(modelo);
        tabla.setColumnControlVisible(true);
        tabla.setName("tabla"); // NOI18N
        tabla.setHighlighters(HighlighterFactory.createAlternateStriping());
        scrollTabla.setViewportView(tabla);

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getResourceMap(PanelAlumnos.class);
        lInfo.setText(resourceMap.getString("lInfo.text")); // NOI18N
        lInfo.setName("lInfo"); // NOI18N

        lTotal.setText(resourceMap.getString("lTotal.text")); // NOI18N
        lTotal.setName("lTotal"); // NOI18N

        javax.swing.GroupLayout panelTablaLayout = new javax.swing.GroupLayout(panelTabla);
        panelTabla.setLayout(panelTablaLayout);
        panelTablaLayout.setHorizontalGroup(
            panelTablaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTablaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelTablaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lInfo, javax.swing.GroupLayout.DEFAULT_SIZE, 408, Short.MAX_VALUE)
                    .addComponent(scrollTabla, javax.swing.GroupLayout.DEFAULT_SIZE, 408, Short.MAX_VALUE)
                    .addComponent(lTotal, javax.swing.GroupLayout.DEFAULT_SIZE, 408, Short.MAX_VALUE))
                .addContainerGap())
        );
        panelTablaLayout.setVerticalGroup(
            panelTablaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTablaLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lInfo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollTabla, javax.swing.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lTotal)
                .addContainerGap())
        );

        panelSeparador.setRightComponent(panelTabla);

        add(panelSeparador, java.awt.BorderLayout.CENTER);

        barraHerramientas.setRollover(true);
        barraHerramientas.setName("barraHerramientas"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getActionMap(PanelAlumnos.class, this);
        bBorrarAlumno.setAction(actionMap.get("borrarAlumnos")); // NOI18N
        bBorrarAlumno.setFocusable(false);
        bBorrarAlumno.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bBorrarAlumno.setName("bBorrarAlumno"); // NOI18N
        bBorrarAlumno.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraHerramientas.add(bBorrarAlumno);

        bApoyos.setAction(actionMap.get("mostrarEditorApoyos")); // NOI18N
        bApoyos.setFocusable(false);
        bApoyos.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bApoyos.setName("bApoyos"); // NOI18N
        bApoyos.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraHerramientas.add(bApoyos);

        bActualizar.setAction(actionMap.get("actualizar")); // NOI18N
        bActualizar.setFocusable(false);
        bActualizar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bActualizar.setName("bActualizar"); // NOI18N
        bActualizar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraHerramientas.add(bActualizar);

        bEnviarWeb.setAction(actionMap.get("enviarWeb")); // NOI18N
        bEnviarWeb.setFocusable(false);
        bEnviarWeb.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bEnviarWeb.setName("bEnviarWeb"); // NOI18N
        bEnviarWeb.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraHerramientas.add(bEnviarWeb);

        add(barraHerramientas, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

    @Override
    public boolean puedoSusituir() {
        return true;
    }

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task<Object, Void> cargarAlumnos() {
        return new CargarAlumnosTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class CargarAlumnosTask extends org.jdesktop.application.Task<Object, Void> {

        CargarAlumnosTask(org.jdesktop.application.Application app) {
            super(app);
            modelo.vaciar();
        }

        @Override
        protected Object doInBackground() {
            try {
                String where = "";
                if (getFiltroAlumno() instanceof Unidad) {
                    where = " AND a.unidad_id=? ";
                } else if (getFiltroAlumno() instanceof String) {
                    where = " AND u.curso=? ";
                }
                Unidad filtro = Permisos.getFiltroUnidad();
                if (filtro != null) {
                    where += " AND a.unidad_id=? ";
                }
                PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT a.* FROM alumnos AS a JOIN unidades AS u ON a.unidad_id=u.id WHERE a.borrado=0 AND a.ano=? " + where + " ORDER BY u.posicion," + Alumno.getCampoOrdenNombre(""));
                int cont = 1;
                st.setInt(cont, MaimonidesApp.getApplication().getAnoEscolar().getId());
                cont++;
                if (getFiltroAlumno() instanceof Unidad) {
                    st.setInt(cont, ((Unidad) getFiltroAlumno()).getId());
                    cont++;
                } else if (getFiltroAlumno() instanceof String) {
                    st.setString(cont, getFiltroAlumno().toString());
                    cont++;
                }
                if (filtro != null) {
                    st.setInt(cont, filtro.getId());
                    cont++;
                }
                ResultSet res = st.executeQuery();
                while (res.next()) {
                    Alumno a = new Alumno();
                    a.cargarDesdeResultSet(res);
                    a.addPropertyChangeListener(new PropertyChangeListener() {

                        @Override
                        public void propertyChange(PropertyChangeEvent evt) {
                            if ("mensajesUltimaOperacion".equals(evt.getPropertyName())) {
                                Alumno a = (Alumno) evt.getNewValue();
                                MaimonidesUtil.mostrarVentanaListaDatos("Modificación alumno", a.getMensajesUltimaOperacion());
                            }
                        }
                    });
                    modelo.addDato(a);
                }
                Obj.cerrar(st, res);
            } catch (Exception ex) {
                Logger.getLogger(PanelAlumnos.class.getName()).log(Level.SEVERE, "Error cargando lista de alumnos para año: " + MaimonidesApp.getApplication().getAnoEscolar(), ex);
            }
            return null;
        }

        @Override
        protected void succeeded(Object result) {
            tabla.packAll();
            lTotal.setText("Total de alumnos: " + modelo.getRowCount());
        }
    }

    @Action(enabledProperty = "alumnoSeleccionado")
    public void mostrarEditorApoyos() {
        int row = tabla.getSelectedRow();
        if (row > -1) {
            row = tabla.convertRowIndexToModel(row);
            Alumno a = modelo.getElemento(row);
            PanelApoyos pa = new PanelApoyos();
            pa.setAlumno(a);
            JOptionPane.showMessageDialog(this, pa, "Apoyos de " + a.getNombreFormateado(), JOptionPane.PLAIN_MESSAGE);
        }
    }
    private boolean alumnoSeleccionado = false;

    public boolean isAlumnoSeleccionado() {
        return alumnoSeleccionado;
    }

    public void setAlumnoSeleccionado(boolean b) {
        boolean old = isAlumnoSeleccionado();
        this.alumnoSeleccionado = b;
        firePropertyChange("alumnoSeleccionado", old, isAlumnoSeleccionado());
        firePropertyChange("alumnoMarcado", null, getAlumnoSeleccionado());
    }

    @Action(block = Task.BlockingScope.ACTION, enabledProperty = "alumnoSeleccionado")
    public Task<ArrayList<Alumno>, Void> borrarAlumnos() {
        return new BorrarAlumnosTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class BorrarAlumnosTask extends org.jdesktop.application.Task<ArrayList<Alumno>, Void> {

        ArrayList<Alumno> alumnos = new ArrayList<Alumno>();

        BorrarAlumnosTask(org.jdesktop.application.Application app) {
            super(app);
            int[] filas = tabla.getSelectedRows();
            int op = JOptionPane.showConfirmDialog(MaimonidesApp.getApplication().getMainFrame(), "¿Esta seguro de que desea borrar los alumnos seleccionados (" + filas.length + ")?", "Borrar", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (op == JOptionPane.YES_OPTION) {
                for (int i : filas) {
                    int row = tabla.convertRowIndexToModel(i);
                    Alumno a = modelo.getElemento(row);
                    alumnos.add(a);
                }
                modelo.quitarDatos(alumnos);
            }
        }

        @Override
        protected ArrayList<Alumno> doInBackground() {
            ArrayList<Alumno> noBorrados = new ArrayList<Alumno>();
            if (alumnos != null) {
                for (Alumno a : alumnos) {
                    if (!a.borrar()) {
                        noBorrados.add(a);
                    }
                }
            }
            return noBorrados;
        }

        @Override
        protected void succeeded(ArrayList<Alumno> result) {
            modelo.addDatos(result);
        }
    }

    @Action(block = Task.BlockingScope.APPLICATION, enabledProperty = "hayEnviosWeb")
    public Task<Short, Void> enviarWeb() {
        return new EnviarWebTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class EnviarWebTask extends org.jdesktop.application.Task<Short, Void> {

        EnviosWeb ew = null;

        EnviarWebTask(org.jdesktop.application.Application app) {
            super(app);
            int op = JOptionPane.showConfirmDialog(MaimonidesApp.getApplication().getMainFrame(), "Se va a enviar el listado de alumnos a la web,\nsólo se enviarán los alumnos con un email asignado.\nLos alumnos que no existan se desactivarán\ny los nuevos alumnos se crearán.\n¿Continuar?", "Confirmación", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (op != JOptionPane.OK_OPTION) {
                cancel(false);
            }
        }

        @Override
        protected Short doInBackground() {
            try {
                //Ahora cojemos cada elemento de la tabla y creamos un usuario web de tipo
                setMessage("Cargando listado de alumnos...");
                PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT a.* FROM alumnos AS a WHERE a.borrado=0 AND a.ano=? AND TRIM(a.email)!='' ");
                st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
                ResultSet res = st.executeQuery();
                ArrayList<Alumno> alumnos = new ArrayList<Alumno>();
                while (res.next()) {
                    Alumno a = new Alumno();
                    a.cargarDesdeResultSet(res);
                    alumnos.add(a);
                }
                setMessage("Creando paquete de datos...");
                ArrayList<UsuarioWeb> usr = EnviosWeb.getUsuariosAlumnosWeb(alumnos);

                ew = new EnviosWeb(EnviosWeb.TIPO_ENVIO_USUARIOS);
                ew.addPropertyChangeListener(new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                    }
                });
                ew.addElementos(usr);
                return ew.enviar();
            } catch (SQLException ex) {
                Logger.getLogger(PanelAlumnos.class.getName()).log(Level.SEVERE, null, ex);
                ew.setUltimoMensaje("Error recuperando listado de alumnos.");
            }
            return EnviosWeb.RETORNO_ERROR;
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

    public final void setHayEnviosWeb(boolean b) {
        boolean old = isHayEnviosWeb();
        this.hayEnviosWeb = b;
        firePropertyChange("hayEnviosWeb", old, isHayEnviosWeb());
    }

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task<Boolean, Void> actualizar() {
        boolean fichero = false;
        //Tenemos que ver si se van a actualizar desde fichero o desde Séneca
        int op = JOptionPane.showOptionDialog(MaimonidesApp.getApplication().getMainFrame(), "¿Desde donde desea actualizar los datos de alumnos?", "Actualizar datos de alumnos", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{"Desde fichero", "Desde Séneca", "Cancelar"}, "Desde Séneca");
        if (op == JOptionPane.YES_OPTION) {
            fichero = true;
            //Ahora preguntamos por el fichero
        } else if (op == JOptionPane.NO_OPTION) {
            fichero = false;
        } else {
            return null;
        }
        Task<Boolean, Void> t= new ActualizarAlumnosExtendidoTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class), fichero);
        t.addTaskListener(new TaskListener<Boolean, Void>() {

            @Override
            public void doInBackground(TaskEvent<Void> event) {
                
            }

            @Override
            public void process(TaskEvent<List<Void>> event) {
                
            }

            @Override
            public void succeeded(TaskEvent<Boolean> event) {
                
            }

            @Override
            public void failed(TaskEvent<Throwable> event) {
                
            }

            @Override
            public void cancelled(TaskEvent<Void> event) {
                
            }

            @Override
            public void interrupted(TaskEvent<InterruptedException> event) {
                
            }

            @Override
            public void finished(TaskEvent<Void> event) {
                MaimonidesUtil.ejecutarTask(auto, "cargarAlumnos");
            }
        });
        
        return t;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bActualizar;
    private javax.swing.JButton bApoyos;
    private javax.swing.JButton bBorrarAlumno;
    private javax.swing.JButton bEnviarWeb;
    private javax.swing.JToolBar barraHerramientas;
    private javax.swing.JLabel lInfo;
    private javax.swing.JLabel lTotal;
    private com.codeko.apps.maimonides.cursos.PanelArbolUnidades panelArbolUnidades1;
    private javax.swing.JSplitPane panelSeparador;
    private javax.swing.JPanel panelTabla;
    private javax.swing.JScrollPane scrollTabla;
    private org.jdesktop.swingx.JXTable tabla;
    // End of variables declaration//GEN-END:variables
}
