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
 * PanelDivisionAlumnos.java
 *
 * Created on 29 de septiembre de 2008, 9:47
 */
package com.codeko.apps.maimonides.partes.divisiones;

import com.codeko.apps.maimonides.IPanel;
import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesUtil;
import com.codeko.apps.maimonides.elementos.Actividad;
import com.codeko.swing.CodekoTableModel;
import com.codeko.util.Obj;
import com.mysql.jdbc.PreparedStatement;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultCellEditor;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;
import org.jdesktop.swingx.decorator.AbstractHighlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

/**
 *
 * @author  Codeko
 * @deprecated 
 */
public class PanelDivisionAlumnos extends javax.swing.JPanel implements IPanel {

    CodekoTableModel<LineaDivisionAlumno> modelo = new CodekoTableModel<LineaDivisionAlumno>(new LineaDivisionAlumno(null, null));
    DefaultMutableTreeNode nodoBase = new DefaultMutableTreeNode("Divisiones");
    DivisionAlumnos divisionActiva = null;

    /** Creates new form PanelDivisionAlumnos */
    public PanelDivisionAlumnos() {
        initComponents();
        MaimonidesUtil.addMenuTabla(tabla, "Divisiones de alumnos");
        nodoBase.setAllowsChildren(true);
        arbol.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        arbol.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {

            @Override
            public void valueChanged(TreeSelectionEvent e) {
                firePropertyChange("seleccionArbol", e.getOldLeadSelectionPath(), e.getNewLeadSelectionPath());
            }
        });
        addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("seleccionArbol".equals(evt.getPropertyName())) {
                    Object obj = evt.getNewValue();
                    if (obj instanceof TreePath) {
                        TreePath path = (TreePath) obj;
                        Object val = path.getLastPathComponent();
                        if (val instanceof DefaultMutableTreeNode && ((DefaultMutableTreeNode) val).getUserObject() instanceof DivisionAlumnos) {
                            DivisionAlumnos div = (DivisionAlumnos) ((DefaultMutableTreeNode) val).getUserObject();
                            setDivisionActiva(div);
                        }
                    }
                }
            }
        });
        modelo.addTableModelListener(new TableModelListener() {

            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getType() == TableModelEvent.UPDATE) {
                    actualizarTotales();
                }
            }
        });
        tabla.setCellSelectionEnabled(true);
        tabla.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tabla.setDefaultRenderer(Boolean.class, new DefaultTableCellRenderer() {

            @Override
            public void setValue(Object val) {
                setOpaque(true);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (val instanceof Boolean) {
                    if ((Boolean) val) {
                        setBackground(Color.GREEN.darker());
                        setText("Si");
                    } else {
                        setBackground(Color.WHITE);
                        setText("No");
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
    }

    private void actualizarTotales() {
        //vemos cuantas columnas tiene por encima de 5
        //TODO Deberías ser varios JLabel y cambiar de color si se pasan del tope
        int offset = LineaDivisionAlumno.OFFSET;
        int cols = modelo.getColumnCount() - offset;
        ArrayList<Integer> totales = new ArrayList<Integer>();
        for (int i = 0; i < cols; i++) {
            int suma = 0;
            for (int x = 0; x < modelo.getRowCount(); x++) {
                if ((Boolean) modelo.getValueAt(x, i + offset)) {
                    suma++;
                }
            }
            totales.add(suma);
        }
        StringBuilder sb = new StringBuilder();
        int pos = offset;
        for (Integer z : totales) {
            sb.append(getDivisionActiva().getLineas().get(0).getTitleAt(pos));
//            sb.append(tabla.getColumn(pos).getHeaderValue());
            sb.append(": ");
            sb.append(z);
            sb.append("  ");
            pos++;
        }
        lInfoPie.setText(sb.toString());
    }

    public DivisionAlumnos getDivisionActiva() {
        return divisionActiva;
    }

    public void setDivisionActiva(DivisionAlumnos divisionActiva) {
        this.divisionActiva = divisionActiva;
        setDivisionCargada(divisionActiva != null);
        if (divisionActiva != null) {
            lInfo.setText(divisionActiva.toStringExtendido());

        } else {
            lInfo.setText("");
        }
        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getActionMap(PanelDivisionAlumnos.class, this);
        actionMap.get("cargarLineas").actionPerformed(new ActionEvent(MaimonidesApp.getMaimonidesView().getFrame(), 0, "cargarLineas"));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lInfo = new javax.swing.JLabel();
        lInfoPie = new javax.swing.JLabel();
        lInfoArbol = new javax.swing.JLabel();
        bQuitar = new javax.swing.JButton();
        bPoner = new javax.swing.JButton();
        bCopiar = new javax.swing.JButton();
        bInvertir = new javax.swing.JButton();
        split = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        arbol = new javax.swing.JTree(nodoBase);
        jScrollPane2 = new javax.swing.JScrollPane();
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

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getResourceMap(PanelDivisionAlumnos.class);
        lInfo.setText(resourceMap.getString("lInfo.text")); // NOI18N
        lInfo.setName("lInfo"); // NOI18N

        lInfoPie.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lInfoPie.setText(resourceMap.getString("lInfoPie.text")); // NOI18N
        lInfoPie.setName("lInfoPie"); // NOI18N

        lInfoArbol.setText(resourceMap.getString("lInfoArbol.text")); // NOI18N
        lInfoArbol.setName("lInfoArbol"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getActionMap(PanelDivisionAlumnos.class, this);
        bQuitar.setAction(actionMap.get("desmarcarSelccionados")); // NOI18N
        bQuitar.setName("bQuitar"); // NOI18N

        bPoner.setAction(actionMap.get("marcarSeleccionados")); // NOI18N
        bPoner.setName("bPoner"); // NOI18N

        bCopiar.setAction(actionMap.get("copiarDivision")); // NOI18N
        bCopiar.setName("bCopiar"); // NOI18N

        bInvertir.setAction(actionMap.get("invertirMarca")); // NOI18N
        bInvertir.setName("bInvertir"); // NOI18N

        split.setDividerLocation(250);
        split.setDividerSize(8);
        split.setAutoscrolls(true);
        split.setName("split"); // NOI18N
        split.setOneTouchExpandable(true);

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        arbol.setToolTipText(resourceMap.getString("arbol.toolTipText")); // NOI18N
        arbol.setName("arbol"); // NOI18N
        jScrollPane1.setViewportView(arbol);
        DefaultTreeCellRenderer renderer =new DefaultTreeCellRenderer();
        renderer.setLeafIcon(resourceMap.getIcon("arbol.leaft.icon"));
        arbol.setCellRenderer(renderer);

        split.setLeftComponent(jScrollPane1);

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        tabla.setAutoCreateRowSorter(true);
        tabla.setModel(modelo);
        tabla.setColumnControlVisible(true);
        tabla.setColumnSelectionAllowed(true);
        tabla.setName("tabla"); // NOI18N
        tabla.setShowGrid(true);
        ((DefaultCellEditor)tabla.getDefaultEditor(Boolean.class)).setClickCountToStart(2);
        tabla.setHighlighters(HighlighterFactory.createAlternateStriping());
        jScrollPane2.setViewportView(tabla);

        split.setRightComponent(jScrollPane2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(lInfoArbol)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 79, Short.MAX_VALUE)
                        .addComponent(lInfo, javax.swing.GroupLayout.DEFAULT_SIZE, 191, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bPoner)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bQuitar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bInvertir)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bCopiar))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lInfoPie, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 632, Short.MAX_VALUE)
                            .addComponent(split, javax.swing.GroupLayout.DEFAULT_SIZE, 632, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lInfoArbol)
                    .addComponent(bCopiar)
                    .addComponent(bInvertir)
                    .addComponent(bQuitar)
                    .addComponent(bPoner)
                    .addComponent(lInfo))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(split, javax.swing.GroupLayout.DEFAULT_SIZE, 465, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lInfoPie, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

private void formAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_formAncestorAdded
    javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getActionMap(PanelDivisionAlumnos.class, this);
    actionMap.get("cargarArbol").actionPerformed(new ActionEvent(MaimonidesApp.getMaimonidesView().getFrame(), 0, "cargarArbol"));
}//GEN-LAST:event_formAncestorAdded

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task cargarArbol() {
        return new CargarArbolTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class CargarArbolTask extends org.jdesktop.application.Task<ArrayList<DivisionAlumnos>, Void> {

        CargarArbolTask(org.jdesktop.application.Application app) {
            super(app);
            nodoBase.removeAllChildren();
            setDivisionActiva(null);
        }

        @Override
        protected ArrayList<DivisionAlumnos> doInBackground() {
            ArrayList<DivisionAlumnos> division = new ArrayList<DivisionAlumnos>();

            String sql = "SELECT distinct h.dia,h.hora,h.actividad_id,m.nombre AS materia,c.curso FROM alumnos_horarios AS ah "
                    + " JOIN alumnos AS a ON a.id=ah.alumno_id "
                    + " JOIN horarios_ AS h ON h.id=ah.horario_id AND h.unidad_id=a.unidad_id "
                    + " JOIN materias AS m ON h.materia_id = m.id "
                    + " JOIN cursos AS c ON c.id=a.curso_id "
                    + " WHERE h.ano=? AND h.activo=1 "
                    + " GROUP BY h.dia,h.hora,h.dicu,c.curso,m.nombre,ah.alumno_id "
                    + " HAVING count(*)>1 AND count(distinct h.profesor_id)>1 "
                    + " ORDER BY c.curso,h.dia,h.hora";
            PreparedStatement st = null;
            ResultSet res = null;
            try {
                st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
                st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
                res = st.executeQuery();
                while (res.next()) {
                    String materia = res.getString("materia");
                    try {
                        Actividad actividad = Actividad.getActividad(res.getInt("actividad_id"));
                        String curso = res.getString("curso");
                        DivisionAlumnos div = new DivisionAlumnos(res.getInt("dia"), res.getInt("hora"), actividad, materia, curso);
                        division.add(div);
                    } catch (Exception ex) {
                        Logger.getLogger(PanelDivisionAlumnos.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(PanelDivisionAlumnos.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                Obj.cerrar(st, res);
            }
            return division;
        }

        @Override
        protected void succeeded(ArrayList<DivisionAlumnos> divisiones) {
            String ultimoCurso = "sin curso";
            DefaultMutableTreeNode ultimoNodo = null;
            for (DivisionAlumnos div : divisiones) {
                DefaultMutableTreeNode nodo = new DefaultMutableTreeNode(div);
                if (ultimoNodo == null || !div.getCurso().equals(ultimoCurso)) {
                    //Si hemos cambiado de curso creamos un nuevo nodo para el curso
                    ultimoNodo = new DefaultMutableTreeNode(div.getCurso());
                    nodoBase.add(ultimoNodo);
                }
                ultimoCurso = div.getCurso();
                ultimoNodo.add(nodo);
            }
            arbol.updateUI();
            arbol.expandRow(0);
            arbol.setSelectionInterval(0, 0);
            MaimonidesUtil.expandAll(arbol, true);
        }
    }
    private boolean divisionCargada = false;

    public boolean isDivisionCargada() {
        return divisionCargada;
    }

    public void setDivisionCargada(boolean b) {
        boolean old = isDivisionCargada();
        this.divisionCargada = b;
        firePropertyChange("divisionCargada", old, isDivisionCargada());
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTree arbol;
    private javax.swing.JButton bCopiar;
    private javax.swing.JButton bInvertir;
    private javax.swing.JButton bPoner;
    private javax.swing.JButton bQuitar;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lInfo;
    private javax.swing.JLabel lInfoArbol;
    private javax.swing.JLabel lInfoPie;
    private javax.swing.JSplitPane split;
    private org.jdesktop.swingx.JXTable tabla;
    // End of variables declaration//GEN-END:variables

    @Override
    public boolean puedoSusituir() {
        return true;
    }

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task cargarLineas() {
        return new CargarLineasTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class CargarLineasTask extends org.jdesktop.application.Task<ArrayList<LineaDivisionAlumno>, Void> {

        CargarLineasTask(org.jdesktop.application.Application app) {
            super(app);
            modelo.vaciar();
        }

        @Override
        protected ArrayList<LineaDivisionAlumno> doInBackground() {
            ArrayList<LineaDivisionAlumno> lineas = null;
            if (getDivisionActiva() != null) {
                lineas = getDivisionActiva().getLineas();
            }
            return lineas;  // return your result
        }

        @Override
        protected void succeeded(ArrayList<LineaDivisionAlumno> result) {
            if (result != null) {
                modelo.setObjetoModelo(result.get(0));
                modelo.addDatos(result);
                tabla.packAll();
            }
        }
    }

    @Action(block = Task.BlockingScope.COMPONENT)
    public Task marcarSeleccionados() {
        return new MarcarSeleccionadosTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class MarcarSeleccionadosTask extends org.jdesktop.application.Task<Object, Void> {

        MarcarSeleccionadosTask(org.jdesktop.application.Application app) {
            super(app);
        }

        @Override
        protected Object doInBackground() {
            int rowIndexStart = tabla.getSelectedRow();
            int rowIndexEnd = tabla.getSelectionModel().getMaxSelectionIndex();
            int colIndexStart = tabla.getSelectedColumn();
            int colIndexEnd = tabla.getColumnModel().getSelectionModel().getMaxSelectionIndex();
            ArrayList<Integer> rows = new ArrayList<Integer>();
            ArrayList<Integer> cols = new ArrayList<Integer>();
            for (int r = rowIndexStart; r <= rowIndexEnd; r++) {
                for (int c = colIndexStart; c <= colIndexEnd; c++) {
                    if (tabla.isCellSelected(r, c)) {
                        rows.add(r);
                        cols.add(c);
                    }
                }
            }
            for (int i = 0; i < rows.size(); i++) {
                tabla.setValueAt(true, rows.get(i), cols.get(i));
            }
            return null;
        }

        @Override
        protected void succeeded(Object result) {
        }
    }

    @Action(block = Task.BlockingScope.COMPONENT)
    public Task desmarcarSelccionados() {
        return new DesmarcarSelccionadosTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class DesmarcarSelccionadosTask extends org.jdesktop.application.Task<Object, Void> {

        DesmarcarSelccionadosTask(org.jdesktop.application.Application app) {
            super(app);
        }

        @Override
        protected Object doInBackground() {
            int rowIndexStart = tabla.getSelectedRow();
            int rowIndexEnd = tabla.getSelectionModel().getMaxSelectionIndex();
            int colIndexStart = tabla.getSelectedColumn();
            int colIndexEnd = tabla.getColumnModel().getSelectionModel().getMaxSelectionIndex();
            ArrayList<Integer> rows = new ArrayList<Integer>();
            ArrayList<Integer> cols = new ArrayList<Integer>();
            for (int r = rowIndexStart; r <= rowIndexEnd; r++) {
                for (int c = colIndexStart; c <= colIndexEnd; c++) {
                    if (tabla.isCellSelected(r, c)) {
                        rows.add(r);
                        cols.add(c);
                    }
                }
            }
            for (int i = 0; i < rows.size(); i++) {
                tabla.setValueAt(false, rows.get(i), cols.get(i));
            }
            return null;
        }

        @Override
        protected void succeeded(Object result) {
        }
    }

    @Action(block = Task.BlockingScope.APPLICATION, enabledProperty = "divisionCargada")
    public Task copiarDivision() {
        return new CopiarDivisionTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class CopiarDivisionTask extends org.jdesktop.application.Task<Object, Void> {

        boolean aplicar = true;

        CopiarDivisionTask(org.jdesktop.application.Application app) {
            super(app);
            int op = JOptionPane.showConfirmDialog(MaimonidesApp.getApplication().getMainFrame(), "¿Esta seguro de que desea aplicar esta división a todas las similares?.\nSe consideran similares aquellas con misma asignatura y profesor. ", "Aplicar división", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            aplicar = op == JOptionPane.YES_OPTION;
        }

        @Override
        protected Object doInBackground() {
            if (aplicar) {
                for (LineaDivisionAlumno l : modelo.getDatos()) {
                    l.guardarEnSimilares();
                }
            }
            return null;
        }

        @Override
        protected void succeeded(Object result) {
            if (aplicar) {
                String cursoActivo = getDivisionActiva().getCurso();
                int count = nodoBase.getChildCount();
                for (int i = 0; i < count; i++) {
                    if (((DefaultMutableTreeNode) nodoBase.getChildAt(i)).getUserObject().equals(cursoActivo)) {
                        int divisiones = nodoBase.getChildAt(i).getChildCount();
                        for (int x = 0; x < divisiones; x++) {
                            DivisionAlumnos div = (DivisionAlumnos) ((DefaultMutableTreeNode) nodoBase.getChildAt(i).getChildAt(x)).getUserObject();
                            div.resetearLineas();
                        }
                        break;
                    }
                }
                JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), "Cambios aplicados con éxito.", "Copiar división", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    @Action(block = Task.BlockingScope.COMPONENT)
    public Task invertirMarca() {
        return new InvertirMarcaTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class InvertirMarcaTask extends org.jdesktop.application.Task<Object, Void> {

        InvertirMarcaTask(org.jdesktop.application.Application app) {
            super(app);
        }

        @Override
        protected Object doInBackground() {
            int rowIndexStart = tabla.getSelectedRow();
            int rowIndexEnd = tabla.getSelectionModel().getMaxSelectionIndex();
            int colIndexStart = tabla.getSelectedColumn();
            int colIndexEnd = tabla.getColumnModel().getSelectionModel().getMaxSelectionIndex();
            ArrayList<Integer> rows = new ArrayList<Integer>();
            ArrayList<Integer> cols = new ArrayList<Integer>();
            for (int r = rowIndexStart; r <= rowIndexEnd; r++) {
                for (int c = colIndexStart; c <= colIndexEnd; c++) {
                    if (tabla.isCellSelected(r, c)) {
                        rows.add(r);
                        cols.add(c);
                    }
                }
            }
            for (int i = 0; i < rows.size(); i++) {
                tabla.setValueAt(!(Boolean) tabla.getValueAt(rows.get(i), cols.get(i)), rows.get(i), cols.get(i));
            }
            return null;  // return your result
        }

        @Override
        protected void succeeded(Object result) {
        }
    }
}
