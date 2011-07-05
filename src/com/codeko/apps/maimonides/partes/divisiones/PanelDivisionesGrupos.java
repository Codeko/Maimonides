/*
 * PanelDivisionesGrupos.java
 *
 * Created on 26 de septiembre de 2008, 10:41
 */
package com.codeko.apps.maimonides.partes.divisiones;

import com.codeko.apps.maimonides.IPanel;
import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesUtil;
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
import javax.swing.SwingConstants;
import javax.swing.UIManager;
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
 */
public class PanelDivisionesGrupos extends javax.swing.JPanel implements IPanel {

    CodekoTableModel<LineaDivisionGrupo> modelo = new CodekoTableModel<LineaDivisionGrupo>(new LineaDivisionGrupo(null, null));
    DefaultMutableTreeNode nodoBase = new DefaultMutableTreeNode("Uniones");
    DivisionGrupos divisionActiva = null;

    /** Creates new form PanelDivisionesGrupos */
    public PanelDivisionesGrupos() {
        initComponents();
        MaimonidesUtil.addMenuTabla(tabla, "Divisiones de grupos");
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
                        if (val instanceof DefaultMutableTreeNode && ((DefaultMutableTreeNode) val).getUserObject() instanceof DivisionGrupos) {
                            DivisionGrupos div = (DivisionGrupos) ((DefaultMutableTreeNode) val).getUserObject();
                            setDivisionActiva(div);
                        }
                    }
                }
            }
        });
        ((DefaultCellEditor) tabla.getDefaultEditor(Boolean.class)).setClickCountToStart(2);
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

    public DivisionGrupos getDivisionActiva() {
        return divisionActiva;
    }

    public void setDivisionActiva(DivisionGrupos divisionActiva) {
        this.divisionActiva = divisionActiva;
        setDivisionCargada(divisionActiva != null);
        if (divisionActiva != null) {
            lInfoDivision.setText(divisionActiva.toStringExtendido());
        } else {
            lInfoDivision.setText("");
        }
        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getActionMap(PanelDivisionesGrupos.class, this);
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

        jScrollPane1 = new javax.swing.JScrollPane();
        arbol = new javax.swing.JTree(nodoBase);
        jScrollPane2 = new javax.swing.JScrollPane();
        tabla = new org.jdesktop.swingx.JXTable();
        lInfoDivision = new javax.swing.JLabel();
        lTitulo = new javax.swing.JLabel();
        bCopiar = new javax.swing.JButton();

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

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getResourceMap(PanelDivisionesGrupos.class);
        arbol.setToolTipText(resourceMap.getString("arbol.toolTipText")); // NOI18N
        arbol.setName("arbol"); // NOI18N
        jScrollPane1.setViewportView(arbol);
        DefaultTreeCellRenderer renderer =new DefaultTreeCellRenderer();
        renderer.setLeafIcon(resourceMap.getIcon("arbol.leaft.icon"));
        arbol.setCellRenderer(renderer);

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        tabla.setModel(modelo);
        tabla.setName("tabla"); // NOI18N
        tabla.setHighlighters(HighlighterFactory.createAlternateStriping());
        jScrollPane2.setViewportView(tabla);

        lInfoDivision.setText(resourceMap.getString("lInfoDivision.text")); // NOI18N
        lInfoDivision.setName("lInfoDivision"); // NOI18N

        lTitulo.setText(resourceMap.getString("lTitulo.text")); // NOI18N
        lTitulo.setName("lTitulo"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getActionMap(PanelDivisionesGrupos.class, this);
        bCopiar.setAction(actionMap.get("copiarUnion")); // NOI18N
        bCopiar.setName("bCopiar"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lTitulo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(lInfoDivision, javax.swing.GroupLayout.DEFAULT_SIZE, 387, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(bCopiar))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 446, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(bCopiar)
                    .addComponent(lTitulo)
                    .addComponent(lInfoDivision))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 404, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 404, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

private void formAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_formAncestorAdded
    javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getActionMap(PanelDivisionesGrupos.class, this);
    actionMap.get("cargarDivisiones").actionPerformed(new ActionEvent(MaimonidesApp.getMaimonidesView().getFrame(), 0, "cargarDivisiones"));
}//GEN-LAST:event_formAncestorAdded

    @Override
    public boolean puedoSusituir() {
        return true;
    }

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task cargarDivisiones() {
        return new CargarDivisionesTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class CargarDivisionesTask extends org.jdesktop.application.Task<ArrayList<DivisionGrupos>, Void> {

        CargarDivisionesTask(org.jdesktop.application.Application app) {
            super(app);
            nodoBase.removeAllChildren();
            setDivisionActiva(null);
        }

        @Override
        protected ArrayList<DivisionGrupos> doInBackground() {
            ArrayList<DivisionGrupos> divisiones = new ArrayList<DivisionGrupos>();
            String sql = "select h.dia,h.hora,h.materia_id,h.actividad_id,u.curso AS curso from horarios_ AS h "
                    + " JOIN unidades AS u ON u.id=h.unidad_id "
                    + " WHERE h.ano=?  "
                    + " GROUP BY h.dia,h.hora,h.materia_id,h.actividad_id,u.curso "
                    + " HAVING count(*)>3 AND count(distinct profesor_id)>1"
                    + " ORDER BY u.curso,h.dia,h.hora";
            try {
                PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
                st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
                ResultSet res = st.executeQuery();
                while (res.next()) {
                    try {
                        DivisionGrupos div = new DivisionGrupos(res.getInt("dia"), res.getInt("hora"), res.getInt("materia_id"), res.getInt("actividad_id"), res.getString("curso"));
                        divisiones.add(div);
                    } catch (Exception ex) {
                        Logger.getLogger(PanelDivisionesGrupos.class.getName()).log(Level.SEVERE, "No se ha podido crear la division.", ex);
                    }
                }
                Obj.cerrar(st, res);
            } catch (SQLException ex) {
                Logger.getLogger(PanelDivisionesGrupos.class.getName()).log(Level.SEVERE, "Error ejecutando: \n" + sql, ex);
            }
            return divisiones;  // return your result
        }

        @Override
        protected void succeeded(ArrayList<DivisionGrupos> divisiones) {
            String ultimoCurso = "Sin curso";
            DefaultMutableTreeNode ultimoNodo = null;
            for (DivisionGrupos div : divisiones) {
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

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task cargarLineas() {
        return new CargarLineasTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class CargarLineasTask extends org.jdesktop.application.Task<ArrayList<LineaDivisionGrupo>, Void> {

        CargarLineasTask(org.jdesktop.application.Application app) {
            super(app);
            modelo.vaciar();
        }

        @Override
        protected ArrayList<LineaDivisionGrupo> doInBackground() {
            ArrayList<LineaDivisionGrupo> lineas = null;
            if (getDivisionActiva() != null) {
                lineas = getDivisionActiva().getLineas();
            }
            return lineas;  // return your result
        }

        @Override
        protected void succeeded(ArrayList<LineaDivisionGrupo> result) {
            if (result != null) {
                modelo.setObjetoModelo(result.get(0));
                modelo.addDatos(result);
            }
        }
    }

    @Action(block = Task.BlockingScope.APPLICATION, enabledProperty = "divisionCargada")
    public Task copiarUnion() {
        return new CopiarUnionTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class CopiarUnionTask extends org.jdesktop.application.Task<Object, Void> {

        boolean aplicar = true;

        CopiarUnionTask(org.jdesktop.application.Application app) {
            super(app);
            int op = JOptionPane.showConfirmDialog(MaimonidesApp.getApplication().getMainFrame(), "¿Esta seguro de que desea aplicar esta unión a todas las similares?.\nSe consideran similares aquellas con misma asignatura y profesor. ", "Aplicar división", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            aplicar = op == JOptionPane.YES_OPTION;
        }

        @Override
        protected Object doInBackground() {
            if (aplicar) {
                for (LineaDivisionGrupo l : modelo.getDatos()) {
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
                    Object obj = ((DefaultMutableTreeNode) nodoBase.getChildAt(i)).getUserObject();
                    if (obj.equals(cursoActivo)) {
                        int divisiones = nodoBase.getChildAt(i).getChildCount();
                        for (int x = 0; x < divisiones; x++) {
                            DivisionGrupos div = (DivisionGrupos) ((DefaultMutableTreeNode) nodoBase.getChildAt(i).getChildAt(x)).getUserObject();
                            if (div.getMateria().equals(getDivisionActiva().getMateria())) {
                                div.resetearLineas();
                            }
                        }
                        break;
                    }
                }
                JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), "Cambios aplicados con éxito.", "Copiar división", JOptionPane.INFORMATION_MESSAGE);
            }
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
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lInfoDivision;
    private javax.swing.JLabel lTitulo;
    private org.jdesktop.swingx.JXTable tabla;
    // End of variables declaration//GEN-END:variables
}
