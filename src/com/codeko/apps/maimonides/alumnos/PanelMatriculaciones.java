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
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.Horario;
import com.codeko.apps.maimonides.elementos.Materia;
import com.codeko.apps.maimonides.elementos.Unidad;
import com.codeko.apps.maimonides.seneca.operaciones.actualizaciones.ImportarMatriculacionesTask;
import com.codeko.apps.maimonides.usr.Permisos;
import com.codeko.swing.CodekoTableModel;
import com.codeko.util.Obj;
import com.mysql.jdbc.PreparedStatement;
import java.awt.Color;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;
import org.jdesktop.swingx.decorator.AbstractHighlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.table.TableColumnExt;

public class PanelMatriculaciones extends javax.swing.JPanel implements IPanel {

    CodekoTableModel<MatriculacionAlumno> modelo = new CodekoTableModel<MatriculacionAlumno>(new MatriculacionAlumno(new ArrayList<Materia>()));
    String curso = null;
    Unidad unidad = null;
    boolean cursoSeleccionado = false;

    public boolean isCursoSeleccionado() {
        return cursoSeleccionado;
    }

    public void setCursoSeleccionado(boolean b) {
        boolean old = isCursoSeleccionado();
        this.cursoSeleccionado = b;
        firePropertyChange("cursoSeleccionado", old, isCursoSeleccionado());
    }

    /** Creates new form PanelMatriculaciones */
    public PanelMatriculaciones() {
        initComponents();
        configurarTabla();
        MaimonidesUtil.addMenuTabla(tabla, "Matriculaciones");
        panelArbolUnidades1.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("seleccionArbol".equals(evt.getPropertyName())) {
                    Object obj = evt.getNewValue();
                    if (obj == null) {
                        setUnidad(null);
                    } else if (obj instanceof String) {
                        setUnidad(null);
                        setCurso(obj.toString());
                    } else if (obj instanceof Unidad) {
                        setUnidad((Unidad) obj);
                    }
                }
            }
        });
        modelo.setEditable(Permisos.edicion(getClass()));
        bImportar.setEnabled(Permisos.especial(getClass()));
        bImportar.setVisible(Permisos.especial(getClass()));
    }

    public String getCurso() {
        return curso;
    }

    public Unidad getUnidad() {
        return unidad;
    }

    public void setUnidad(Unidad unidad) {
        this.unidad = unidad;
        if (unidad == null) {
            setCurso(null);
        } else {
            try {
                setCurso(unidad.getCurso());
            } catch (Exception ex) {
                Logger.getLogger(PanelMatriculaciones.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void setCurso(String curso) {
        this.curso = curso;
        setCursoSeleccionado(curso != null);
        if (curso != null) {
            lInfoCurso.setText(curso);
            if (getUnidad() != null) {
                lInfoCurso.setText(lInfoCurso.getText() + ": " + getUnidad());
            }
            MaimonidesUtil.ejecutarTask(this, "cargarAlumnos");
        } else {
            lInfoCurso.setText("");
            modelo.vaciar();
        }
    }

    private void configurarTabla() {

        tabla.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getColumnExt("Código").setVisible(false);
        tabla.getColumnExt("N.Escolar").setVisible(false);
        ((DefaultCellEditor) tabla.getDefaultEditor(Boolean.class)).setClickCountToStart(2);
        tabla.setDefaultRenderer(Boolean.class, new DefaultTableCellRenderer() {

            @Override
            public void setValue(Object val) {
                setOpaque(true);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (val instanceof Boolean) {
                    if ((Boolean) val) {
                        setBackground(Color.GREEN.darker());
                        setText("M");
                    } else {
                        setBackground(Color.WHITE);
                        setText("");
                    }
                }
            }
        });
        AbstractHighlighter h = new AbstractHighlighter() {

            @Override
            protected Component doHighlight(Component c, org.jdesktop.swingx.decorator.ComponentAdapter adapt) {
                if (adapt.getValue() instanceof Boolean) {
                    if ((Boolean) adapt.getValue()) {
                        if (adapt.isSelected()) {
                            c.setForeground(Color.GREEN.darker());
                            c.setBackground(UIManager.getDefaults().getColor("Table.selectionBackground"));
                        } else {
                            c.setForeground(UIManager.getDefaults().getColor("Table.foreground"));
                            c.setBackground(Color.GREEN.darker());
                        }
                    } else {
                        if (adapt.isSelected()) {
                            c.setForeground(Color.white);
                            c.setBackground(UIManager.getDefaults().getColor("Table.selectionBackground"));
                        } else {
                            c.setForeground(UIManager.getDefaults().getColor("Table.foreground"));
                            c.setBackground(Color.white);
                        }

                    }
                }

                return c;
            }
        };

        tabla.setHighlighters(h);

        DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer() {

            @Override
            public void setValue(Object val) {
                setOpaque(true);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (val instanceof Boolean) {
                    if ((Boolean) val) {
                        //setBackground(Color.GREEN.darker());
                        setText("Si");
                    } else {
                        //setBackground(Color.WHITE);
                        setText("No");
                    }
                }
            }
        };
        TableColumnExt tc = tabla.getColumnExt("Bilingüe");
        tc.setPreferredWidth(80);
        tc.setVisible(false);
        tc.setCellRenderer(dtcr);
        tc = tabla.getColumnExt("Repetidor");
        tc.setVisible(false);
        tc.setPreferredWidth(80);
        tc.setCellRenderer(dtcr);
        tc = tabla.getColumnExt("D.I.C.U.");
        tc.setPreferredWidth(80);
        tc.setVisible(false);
        tc.setCellRenderer(dtcr);
        tc = tabla.getColumnExt("Unidad");
        tc.setVisible(getUnidad() == null);
        tc.setCellEditor(new DefaultCellEditor(new JComboBox()) {

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                Component c = super.getTableCellEditorComponent(table, value, isSelected, row, column);
                if (c instanceof JComboBox && value instanceof Unidad) {
                    Unidad ud = (Unidad) value;
                    JComboBox cb = (JComboBox) c;
                    cb.removeAllItems();
                    try {
                        PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM unidades WHERE curso_id=? OR curso2_id=? OR curso_id=? OR curso2_id=? ORDER BY posicion");
                        st.setInt(1, ud.getIdCurso());
                        st.setInt(2, ud.getIdCurso());
                        if (ud.getIdCurso2() != null) {
                            st.setInt(3, ud.getIdCurso2());
                            st.setInt(4, ud.getIdCurso2());
                        } else {
                            st.setInt(3, ud.getIdCurso());
                            st.setInt(4, ud.getIdCurso());
                        }
                        ResultSet res = st.executeQuery();
                        while (res.next()) {
                            Unidad u = new Unidad();
                            u.cargarDesdeResultSet(res);
                            cb.addItem(u);
                        }
                        Obj.cerrar(st, res);
                    } catch (SQLException ex) {
                        cb.removeAllItems();
                        cb.addItem(value);
                        Logger.getLogger(PanelAlumnos.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    cb.setSelectedItem(value);
                }
                return c;
            }
        });

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelSeparador = new javax.swing.JSplitPane();
        panelTabla = new javax.swing.JPanel();
        scrollTabla = new javax.swing.JScrollPane(tabla);
        tabla = new org.jdesktop.swingx.JXTable() {
        };
        lInfoCurso = new javax.swing.JLabel();
        cbOcultarNoRelevantes = new javax.swing.JCheckBox();
        panelArbolUnidades1 = new com.codeko.apps.maimonides.cursos.PanelArbolUnidades();
        jToolBar1 = new javax.swing.JToolBar();
        bImportar = new javax.swing.JButton();

        setName("maimonides.paneles.datos.matriculaciones_alumnos"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        panelSeparador.setDividerLocation(200);
        panelSeparador.setDividerSize(8);
        panelSeparador.setName("panelSeparador"); // NOI18N
        panelSeparador.setOneTouchExpandable(true);

        panelTabla.setName("panelTabla"); // NOI18N

        scrollTabla.setName("scrollTabla"); // NOI18N

        tabla.setModel(modelo);
        tabla.setColumnControlVisible(true);
        tabla.setHorizontalScrollEnabled(true);
        tabla.setName("tabla"); // NOI18N
        tabla.setShowGrid(true);
        tabla.setHighlighters(HighlighterFactory.createAlternateStriping());
        scrollTabla.setViewportView(tabla);

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance().getContext().getResourceMap(PanelMatriculaciones.class);
        lInfoCurso.setText(resourceMap.getString("lInfoCurso.text")); // NOI18N
        lInfoCurso.setName("lInfoCurso"); // NOI18N

        cbOcultarNoRelevantes.setSelected(true);
        cbOcultarNoRelevantes.setText(resourceMap.getString("cbOcultarNoRelevantes.text")); // NOI18N
        cbOcultarNoRelevantes.setToolTipText(resourceMap.getString("cbOcultarNoRelevantes.toolTipText")); // NOI18N
        cbOcultarNoRelevantes.setName("cbOcultarNoRelevantes"); // NOI18N
        cbOcultarNoRelevantes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbOcultarNoRelevantesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelTablaLayout = new javax.swing.GroupLayout(panelTabla);
        panelTabla.setLayout(panelTablaLayout);
        panelTablaLayout.setHorizontalGroup(
            panelTablaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTablaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelTablaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelTablaLayout.createSequentialGroup()
                        .addComponent(lInfoCurso, javax.swing.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbOcultarNoRelevantes))
                    .addComponent(scrollTabla, javax.swing.GroupLayout.DEFAULT_SIZE, 425, Short.MAX_VALUE))
                .addContainerGap())
        );
        panelTablaLayout.setVerticalGroup(
            panelTablaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTablaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelTablaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbOcultarNoRelevantes)
                    .addComponent(lInfoCurso))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollTabla, javax.swing.GroupLayout.DEFAULT_SIZE, 369, Short.MAX_VALUE)
                .addContainerGap())
        );

        panelSeparador.setRightComponent(panelTabla);

        panelArbolUnidades1.setName("panelArbolUnidades1"); // NOI18N
        panelSeparador.setLeftComponent(panelArbolUnidades1);

        add(panelSeparador, java.awt.BorderLayout.CENTER);

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        jToolBar1.setName("jToolBar1"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance().getContext().getActionMap(PanelMatriculaciones.class, this);
        bImportar.setAction(actionMap.get("actualizar")); // NOI18N
        bImportar.setFocusable(false);
        bImportar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bImportar.setName("bImportar"); // NOI18N
        bImportar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(bImportar);

        add(jToolBar1, java.awt.BorderLayout.PAGE_START);
    }// </editor-fold>//GEN-END:initComponents

private void cbOcultarNoRelevantesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbOcultarNoRelevantesActionPerformed
    recargarAlumnos();
}//GEN-LAST:event_cbOcultarNoRelevantesActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bImportar;
    private javax.swing.JCheckBox cbOcultarNoRelevantes;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JLabel lInfoCurso;
    private com.codeko.apps.maimonides.cursos.PanelArbolUnidades panelArbolUnidades1;
    private javax.swing.JSplitPane panelSeparador;
    private javax.swing.JPanel panelTabla;
    private javax.swing.JScrollPane scrollTabla;
    private org.jdesktop.swingx.JXTable tabla;
    // End of variables declaration//GEN-END:variables

    @Override
    public boolean puedoSusituir() {
        return true;
    }

    @Action(block = Task.BlockingScope.APPLICATION, enabledProperty = "cursoSeleccionado")
    public Task cargarAlumnos() {
        return new CargarAlumnosTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private void recargarAlumnos() {
        MaimonidesUtil.ejecutarTask(this, "cargarAlumnos");
    }

    private class CargarAlumnosTask extends org.jdesktop.application.Task<ArrayList<MatriculacionAlumno>, Void> {

        CargarAlumnosTask(org.jdesktop.application.Application app) {
            super(app);
            modelo.vaciar();
            scrollTabla.setVisible(false);

        }

        @Override
        protected ArrayList<MatriculacionAlumno> doInBackground() {
            setMessage("Cargando matriculación de alumnos...");
            ArrayList<MatriculacionAlumno> datos = new ArrayList<MatriculacionAlumno>();
            if (getCurso() != null) {
                try {
                    setMessage("Cargando materias del curso...");
                    //Sacamos las materias del curso
                    ArrayList<Materia> materias = getMaterias();
                    modelo.setObjetoModelo(new MatriculacionAlumno(materias));
                    configurarTabla();
                    int cols = tabla.getColumnCount();
                    for (int i = 0; i < materias.size(); i++) {
                        int pos = i + (cols - materias.size());
                        Materia m = materias.get(i);
                        TableColumnExt tc = tabla.getColumnExt(pos);
                        tc.setTitle(m.getCodigoMateria());
                        tc.setToolTipText(m.getDescripcion());
                    }
                    setMessage("Cargando matriculacions...");
                    String sql = "SELECT a.* FROM alumnos AS a JOIN unidades AS u ON a.unidad_id=u.id WHERE a.borrado=0 AND a.ano=? AND u.curso=? " + (getUnidad() != null ? " AND a.unidad_id=? " : "") + " ORDER BY u.posicion," + Alumno.getCampoOrdenNombre("a");
                    PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
                    st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
                    st.setString(2, getCurso());
                    if (getUnidad() != null) {
                        st.setInt(3, getUnidad().getId());
                    }
                    ResultSet res = st.executeQuery();
                    while (res.next()) {
                        MatriculacionAlumno ma = new MatriculacionAlumno(materias);
                        ma.cargarDesdeResultSet(res);
                        ma.addPropertyChangeListener(new PropertyChangeListener() {

                            @Override
                            public void propertyChange(PropertyChangeEvent evt) {
                                if ("noHayHorarios".equals(evt.getPropertyName())) {
                                    JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), "No hay horarios asignados para:\n'" + evt.getNewValue() + "'.\nEl alumno no podría dar clase pues no hay\nasignado ningún horario,profesor o aula para él.", "No hay horarios", JOptionPane.WARNING_MESSAGE);
                                } else if ("conflictosHorarios".equals(evt.getPropertyName())) {
                                    StringBuilder sb = new StringBuilder("La siguiente matriculación puede crear conflictos en los horarios del alumno pues las materias se solapan en los siguientes casos:\n");
                                    @SuppressWarnings("unchecked")
                                    ArrayList<ArrayList<Horario>> conflictos = (ArrayList<ArrayList<Horario>>) evt.getNewValue();
                                    for (ArrayList<Horario> vh : conflictos) {
                                        String sDia = MaimonidesUtil.getNombreDiaSemana(vh.get(0).getDia(), true);
                                        sb.append("\n     El ").append(sDia).append(" a ").append(vh.get(0).getHora()).append("ª Hora el alumno tendía que asistir a las siguientes asignaturas:");
                                        ArrayList<Integer> mats = new ArrayList<Integer>();
                                        for (Horario h : vh) {

                                            if (!mats.contains(h.getMateria())) {
                                                mats.add(h.getMateria());
                                            }
                                        }
                                        for (Integer m : mats) {
                                            Materia mat;
                                            try {
                                                mat = Materia.getMateria(m);
                                                sb.append("\n          ").append(mat.getDescripcion());
                                            } catch (Exception ex) {
                                                Logger.getLogger(PanelMatriculaciones.class.getName()).log(Level.SEVERE, null, ex);
                                            }

                                        }
                                    }
                                    sb.append("\n\nSe recomienda que desmatricule al alumno en alguna de las asignaturas o modifique los horarios.");
                                    JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), sb.toString(), "Advertencia", JOptionPane.WARNING_MESSAGE);
                                }
                            }
                        });
                        datos.add(ma);
                    }
                    Obj.cerrar(st, res);
                    modelo.setDatos(datos);

                    if (cbOcultarNoRelevantes.isSelected()) {
                        setMessage("Ocultando columnas no relevantes...");
                        cols = tabla.getColumnCount() - 1;
                        for (int i = 0; i < materias.size(); i++) {
                            int pos = cols - i;
                            TableColumnExt tc = tabla.getColumnExt(pos);
                            Object valor = null;
                            boolean cambiado = false;
                            for (int x = 0; x < modelo.getRowCount(); x++) {
                                if (valor == null) {
                                    valor = tabla.getValueAt(x, pos);
                                } else {
                                    if (!valor.equals(tabla.getValueAt(x, pos))) {
                                        cambiado = true;
                                        break;
                                    }
                                }
                            }
                            if (!cambiado) {
                                tc.setVisible(false);
                            }
                        }
                    }
                    modelo.fireTableDataChanged();
                } catch (Exception ex) {
                    Logger.getLogger(PanelMatriculaciones.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            return datos;  // return your result
        }

        @Override
        protected void succeeded(ArrayList<MatriculacionAlumno> result) {
            scrollTabla.setVisible(true);
            tabla.packAll();
            panelTabla.updateUI();
        }

        private ArrayList<Materia> getMaterias() throws SQLException, Exception {
            ArrayList<Materia> mats = new ArrayList<Materia>();
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT m.* FROM materias AS m JOIN cursos AS c ON c.id=m.curso_id WHERE c.curso=? AND c.ano=? AND m.ano=? ORDER BY m.nombre");
            st.setString(1, getCurso());
            st.setInt(2, MaimonidesApp.getApplication().getAnoEscolar().getId());
            st.setInt(3, MaimonidesApp.getApplication().getAnoEscolar().getId());
            ResultSet res = st.executeQuery();
            while (res.next()) {
                Materia m = new Materia();
                m.cargarDesdeResultSet(res);
                mats.add(m);
            }
            Obj.cerrar(st, res);
            return mats;
        }
    }

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task actualizar() {
        return new ImportarMatriculacionesTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }
}
