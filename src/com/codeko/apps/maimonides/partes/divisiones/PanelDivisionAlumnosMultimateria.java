/*
 * PanelDivisionAlumnos.java
 *
 * Created on 29 de septiembre de 2008, 9:47
 */
package com.codeko.apps.maimonides.partes.divisiones;

import com.codeko.apps.maimonides.ICargable;
import com.codeko.apps.maimonides.IPanel;
import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesUtil;
import com.codeko.apps.maimonides.elementos.Actividad;
import com.codeko.apps.maimonides.elementos.Materia;
import com.codeko.apps.maimonides.elementos.Profesor;
import com.codeko.swing.CodekoTableModel;
import com.codeko.util.Num;
import com.codeko.util.Obj;
import com.mysql.jdbc.Blob;
import com.mysql.jdbc.PreparedStatement;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.beans.Beans;
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
import org.jdesktop.swingx.table.TableColumnExt;

/**
 *
 * @author  Codeko
 */
public class PanelDivisionAlumnosMultimateria extends javax.swing.JPanel implements IPanel, ICargable {

    CodekoTableModel<LineaDivisionAlumno> modelo = new CodekoTableModel<LineaDivisionAlumno>(new LineaDivisionAlumno(null, null));
    DefaultMutableTreeNode nodoBase = new DefaultMutableTreeNode("Divisiones");
    DivisionAlumnosMultimateria divisionActiva = null;
    boolean cargado = false;

    /** Creates new form PanelDivisionAlumnos */
    public PanelDivisionAlumnosMultimateria() {
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
                        if (val instanceof DefaultMutableTreeNode && ((DefaultMutableTreeNode) val).getUserObject() instanceof DivisionAlumnosMultimateria) {
                            DivisionAlumnosMultimateria div = (DivisionAlumnosMultimateria) ((DefaultMutableTreeNode) val).getUserObject();
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
                if (val != null) {
                    if (val instanceof Boolean) {
                        if ((Boolean) val) {
                            setBackground(Color.GREEN.darker());
                            setText("Si");
                        } else {
                            setBackground(Color.WHITE);
                            setText("No");
                        }
                    } else {
                        setBackground(Color.WHITE);
                        setText("ERROR");
                    }
                } else {
                    setBackground(Color.GRAY);
                    setText("No hay datos");
                }
            }
        });
        AbstractHighlighter h = new AbstractHighlighter() {

            @Override
            protected Component doHighlight(Component c, org.jdesktop.swingx.decorator.ComponentAdapter adapt) {
                Color fondoSeleccion = UIManager.getDefaults().getColor("Table.selectionBackground");//Color.BLUE.darker()
                if (fondoSeleccion == null) {
                    fondoSeleccion = Color.BLUE.darker();
                }
                if (adapt.getValue() != null) {
                    if (adapt.getValue() instanceof Boolean) {
                        if ((Boolean) adapt.getValue()) {
                            if (adapt.isSelected()) {
                                c.setForeground(Color.GREEN.darker());
                                c.setBackground(fondoSeleccion);
                            } else {
                                c.setForeground(UIManager.getDefaults().getColor("Table.foreground"));
                                c.setBackground(Color.GREEN.darker());
                            }
                        } else {
                            if (adapt.isSelected()) {
                                c.setForeground(Color.white);
                                c.setBackground(fondoSeleccion);
                            } else {
                                c.setForeground(UIManager.getDefaults().getColor("Table.foreground"));
                                c.setBackground(Color.white);
                            }

                        }
                    }
                } else {
                    if (adapt.isSelected()) {
                        c.setForeground(Color.gray);
                        c.setBackground(fondoSeleccion);
                    } else {
                        c.setForeground(UIManager.getDefaults().getColor("Table.foreground"));
                        c.setBackground(Color.gray);
                    }
                }
                if (adapt.isSelected()) {
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else {
                    c.setFont(c.getFont().deriveFont(Font.PLAIN));
                }
                return c;
            }
        };

        tabla.setHighlighters(h);
        arbol.setShowsRootHandles(true);
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
                try {
                    if ((Boolean) modelo.getValueAt(x, i + offset)) {
                        suma++;
                    }
                } catch (Exception e) {
                    //Logger.getLogger(PanelDivisionAlumnosMultimateria.class.getName()).log(Level.WARNING, "Error buscando datos en pos " + x + ":" + (i + offset), e);
                }
            }
            totales.add(suma);
        }
        StringBuilder sb = new StringBuilder();
        int pos = offset;
        for (Integer z : totales) {
            if (getDivisionActiva().getLineaModelo() != null && getDivisionActiva().getLineas().size() > 0) {
                sb.append(getDivisionActiva().getLineaModelo().getTitleAt(pos));
                sb.append(": ");
                sb.append(z);
                sb.append("  ");
            }
            pos++;
        }
        lInfoPie.setText(sb.toString());
    }

    public DivisionAlumnosMultimateria getDivisionActiva() {
        return divisionActiva;
    }

    public void setDivisionActiva(DivisionAlumnosMultimateria divisionActiva) {
        this.divisionActiva = divisionActiva;
        setDivisionCargada(divisionActiva != null);
        if (!isDivisionCargada()) {
            setNoEsMultihorario(false);
        } else {
            setNoEsMultihorario(!divisionActiva.isMultimateria());
        }
        if (divisionActiva != null) {
            lInfo.setText(divisionActiva.toStringExtendido());

        } else {
            lInfo.setText("");
        }
        MaimonidesUtil.ejecutarTask(this, "cargarLineas");
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

        setName("maimonides.paneles.horarios.divisiones_alumnos"); // NOI18N
        addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                formAncestorAdded(evt);
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getResourceMap(PanelDivisionAlumnosMultimateria.class);
        lInfo.setText(resourceMap.getString("lInfo.text")); // NOI18N
        lInfo.setName("lInfo"); // NOI18N

        lInfoPie.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lInfoPie.setText(resourceMap.getString("lInfoPie.text")); // NOI18N
        lInfoPie.setName("lInfoPie"); // NOI18N

        lInfoArbol.setText(resourceMap.getString("lInfoArbol.text")); // NOI18N
        lInfoArbol.setName("lInfoArbol"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getActionMap(PanelDivisionAlumnosMultimateria.class, this);
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
        arbol.setRootVisible(false);
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
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lInfoArbol)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bPoner)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bQuitar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bInvertir)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bCopiar))
                    .addComponent(lInfoPie, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
                    .addComponent(split, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE))
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
    cargar();
}//GEN-LAST:event_formAncestorAdded

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task cargarArbol() {
        return new CargarArbolTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    @Override
    public void cargar() {
        if (!Beans.isDesignTime() && !isCargado()) {
            MaimonidesUtil.ejecutarTask(this, "cargarArbol");
        }
    }

    @Override
    public void vaciar() {
        nodoBase.removeAllChildren();
        setDivisionActiva(null);
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

    private class CargarArbolTask extends org.jdesktop.application.Task<ArrayList<DivisionAlumnosMultimateria>, Void> {

        CargarArbolTask(org.jdesktop.application.Application app) {
            super(app);
            vaciar();
            setCargado(true);
        }

        @Override
        protected ArrayList<DivisionAlumnosMultimateria> doInBackground() {

            ArrayList<DivisionAlumnosMultimateria> divisiones = new ArrayList<DivisionAlumnosMultimateria>();
            cargarDivisionesMonomateria(divisiones);
            cargarDivisionesMultimateria(divisiones);

            return divisiones;
        }

        private ArrayList<Integer> blob2Vector(Blob dato) throws SQLException {
            ArrayList<Integer> vec = new ArrayList<Integer>();
            if (dato != null) {
                byte[] ba = dato.getBytes(1, (int) dato.length());
                String str = new String(ba);
                String[] sIds = str.split(",");
                for (String sId : sIds) {
                    vec.add(Num.getInt(sId));
                }
            }
            return vec;
        }

        @Override
        protected void succeeded(ArrayList<DivisionAlumnosMultimateria> divisiones) {
            for (DivisionAlumnosMultimateria div : divisiones) {
                DefaultMutableTreeNode nodo = new DefaultMutableTreeNode(div);
                //Ahora buscamos el nodo para el curso
                DefaultMutableTreeNode nodoPadre = null;
                for (int i = 0; i < nodoBase.getChildCount(); i++) {
                    DefaultMutableTreeNode n = (DefaultMutableTreeNode) nodoBase.getChildAt(i);
                    if (div.getCurso().equals(n.getUserObject())) {
                        nodoPadre = n;
                        break;
                    }
                }
                if (nodoPadre == null) {
                    nodoPadre = new DefaultMutableTreeNode(div.getCurso());
                    nodoBase.add(nodoPadre);
                }
                nodoPadre.add(nodo);
            }
            arbol.updateUI();
            arbol.expandRow(0);
            arbol.setSelectionInterval(0, 0);
            MaimonidesUtil.expandAll(arbol, true);
            setCargado(true);
        }

        private void cargarDivisionesMonomateria(ArrayList<DivisionAlumnosMultimateria> divisiones) {
            /** Con esta revisamos todas las materias iguales (por nombre de actividad y materia) que tienen más de un profesor */
            String sql = "SELECT h.dia,h.hora,u.curso,a.id AS idActividad, "
                    + " GROUP_CONCAT(distinct h.profesor_id ORDER BY h.profesor_id) AS idProfesores,"
                    + " GROUP_CONCAT(distinct h.materia_id ORDER BY h.materia_id) AS idMaterias, h.unidad_id "
                    + " FROM horarios_ AS h "
                    + " JOIN unidades AS u ON h.unidad_id=u.id "
                    + " JOIN actividades AS a ON a.id=h.actividad_id "
                    + " LEFT JOIN materias AS m ON h.materia_id=m.id "
                    + " LEFT JOIN cursos AS c ON c.curso=u.curso "
                    + " WHERE h.ano=? "
                    + " GROUP BY u.curso,h.dia,h.hora,h.unidad_id,a.descripcion,m.nombre "
                    + " HAVING count(distinct h.profesor_id) >1 "
                    + " ORDER BY c.posicion,h.dia,h.hora ";
            PreparedStatement st = null;
            ResultSet res = null;
            try {
                st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
                st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
                res = st.executeQuery();
                ArrayList<DivisionAlumnosMultimateria> divisionesCurso = new ArrayList<DivisionAlumnosMultimateria>();
                String ultimoCurso = "";
                while (res.next()) {
                    try {
                        int dia = res.getInt("dia");
                        int hora = res.getInt("hora");
                        int unidad = res.getInt("unidad_id");
                        int actividad = res.getInt("idActividad");
                        String curso = res.getString("curso");
                        if (!curso.equals(ultimoCurso)) {
                            divisionesCurso.clear();
                        }
                        ultimoCurso = curso;
                        setMessage("Cargando divisiones de " + curso + "...");
                        //Añadimos las materias
                        ArrayList<Integer> idMats = blob2Vector((Blob) res.getBlob("idMaterias"));
                        ArrayList<Materia> mats = new ArrayList<Materia>();
                        for (Integer xId : idMats) {
                            mats.add(Materia.getMateria(xId));
                        }
                        //Añadimos los profesores
                        ArrayList<Integer> profs = blob2Vector((Blob) res.getBlob("idProfesores"));
                        //Antes de añadir la divison vemos si existe alguna equivalente
                        DivisionAlumnosMultimateria div = new DivisionAlumnosMultimateria(dia, hora, curso);
                        boolean integrado = false;
                        for (DivisionAlumnosMultimateria d : divisionesCurso) {
                            if (d.isIntegrable(div, true, false)) {
                                div = d;
                                integrado = true;
                                break;
                            }
                        }
                        if (!integrado) {
                            divisiones.add(div);
                            divisionesCurso.add(div);
                        }
                        div.addProfesores(profs);
                        div.addMaterias(mats);
                        div.addUnidad(unidad);
                        div.setActividad(Actividad.getActividad(actividad));
                    } catch (Exception ex) {
                        Logger.getLogger(PanelDivisionAlumnosMultimateria.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(PanelDivisionAlumnosMultimateria.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                Obj.cerrar(st, res);
            }
        }

        private void cargarDivisionesMultimateria(ArrayList<DivisionAlumnosMultimateria> divisiones) {
            /** Aqui revisamos por matriculaciones alumnos que tengan varias clases simultaneas */
            String sql = "SELECT distinct h.dia,h.hora,u.curso,h.actividad_id,h.unidad_id, "
                    + " GROUP_CONCAT(distinct h.materia_id ORDER BY h.materia_id) AS idMaterias, "
                    + " GROUP_CONCAT(distinct h.profesor_id ORDER BY h.profesor_id) AS idProfesores "
                    + " FROM alumnos_horarios AS ah "
                    + " JOIN alumnos AS a ON a.id=ah.alumno_id "
                    + " JOIN horarios_ AS h ON h.id=ah.horario_id "
                    + " JOIN materias_alumnos AS ma ON a.id=ma.alumno_id AND ma.materia_id=h.materia_id "
                    + " JOIN unidades AS u ON u.id=h.unidad_id "
                    + " LEFT JOIN materias AS m ON m.id=h.materia_id "
                    + " LEFT JOIN cursos AS c ON c.curso=u.curso "
                    + " WHERE a.borrado=0 AND h.ano=? "
                    + " GROUP BY h.dia,h.hora,ah.alumno_id "
                    + " HAVING count(h.id)>1 AND count(distinct m.nombre)>1 "
                    + " ORDER BY c.posicion,h.dia,h.hora";
            PreparedStatement st = null;
            ResultSet res = null;
            try {
                st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
                st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
                res = st.executeQuery();
                ArrayList<DivisionAlumnosMultimateria> divisionesCurso = new ArrayList<DivisionAlumnosMultimateria>();
                String ultimoCurso = "";
                while (res.next()) {
                    try {
                        int dia = res.getInt("dia");
                        int hora = res.getInt("hora");
                        int unidad = res.getInt("unidad_id");
                        int actividad = res.getInt("actividad_id");
                        String curso = res.getString("curso");
                        if (!curso.equals(ultimoCurso)) {
                            divisionesCurso.clear();
                        }
                        ultimoCurso = curso;
                        setMessage("Cargando divisiones de " + curso + "...");
                        //Añadimos las materias
                        ArrayList<Integer> idMats = blob2Vector((Blob) res.getBlob("idMaterias"));
                        ArrayList<Materia> mats = new ArrayList<Materia>();
                        for (Integer xId : idMats) {
                            mats.add(Materia.getMateria(xId));
                        }
                        //Añadimos los profesores
                        ArrayList<Integer> profs = blob2Vector((Blob) res.getBlob("idProfesores"));
                        //Antes de añadir la divison vemos si existe alguna equivalente
                        DivisionAlumnosMultimateria div = new DivisionAlumnosMultimateria(dia, hora, curso);
                        div.setMultimateria(true);
                        div.addProfesores(profs);
                        div.addMaterias(mats);
                        div.addUnidad(unidad);
                        div.setActividad(Actividad.getActividad(actividad));
                        boolean integrado = false;
                        for (DivisionAlumnosMultimateria d : divisionesCurso) {
                            if (d.isIntegrable(div, false, false)) {
                                div = d;
                                integrado = true;
                                break;
                            }
                        }
                        if (!integrado) {
                            //Si no está integrado lo añadimos
                            divisiones.add(div);
                            divisionesCurso.add(div);
                        } else {
                            //Si se ha integrado le añadimos al original los datos de este
                            div.addProfesores(profs);
                            div.addMaterias(mats);
                            div.addUnidad(unidad);
                            div.setActividad(Actividad.getActividad(actividad));
                        }

                    } catch (Exception ex) {
                        Logger.getLogger(PanelDivisionAlumnosMultimateria.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(PanelDivisionAlumnosMultimateria.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                Obj.cerrar(st, res);
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
    private boolean noEsMultihorario = false;

    public boolean isNoEsMultihorario() {
        return noEsMultihorario;
    }

    public void setNoEsMultihorario(boolean b) {
        boolean old = isNoEsMultihorario();
        this.noEsMultihorario = b;
        firePropertyChange("noEsMultihorario", old, isNoEsMultihorario());
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
            return lineas;
        }

        @Override
        protected void succeeded(ArrayList<LineaDivisionAlumno> result) {
            if (result != null && result.size() > 0) {
                //Creamos un objeto para que haga de modelo
                //El objeto modelo que nos insteresa es el que tenga más horarios para evitar problemas por falta de matriculaciones
                //No se trata sólo de mas horarios ya que se puede dar el caso de que unos tengan A/B  y otros B/C con lo que
                //hay que sacar todos los horarios distintos (por equivalencia)
                LineaDivisionAlumno lm = new LineaDivisionAlumno(new ArrayList<HorarioAlumno>(), null);
                lm.setDivision(result.get(0).getDivision());
                for (LineaDivisionAlumno l : result) {
                    for (HorarioAlumno h : l.getHorarios()) {
                        //Vemos si el horario ya está en el modelo (o alguno equivalente)
                        boolean added = false;
                        for (HorarioAlumno hm : lm.getHorarios()) {
                            if (hm.equivalenteMultiCurso(h)) {
                                added = true;
                                break;
                            }
                        }
                        if (!added && h != null) {
                            lm.getHorarios().add(h);
                        }
                    }
                }
                lm.getDivision().setLineaModelo(lm);
                modelo.setObjetoModelo(lm);
                modelo.addDatos(result);
                tabla.packAll();
                //Ahora asignamos los tooltips
                for (int i = 0; i < lm.getHorarios().size(); i++) {
                    int pos = i + LineaDivisionAlumno.OFFSET;
                    TableColumnExt tc = tabla.getColumnExt(pos);
                    String val = tc.getTitle();
                    if (lm.getDivision().isMultimateria()) {
                        Actividad a = lm.getHorarios().get(i).getObjetoActividad();
                        if (a != null) {
                            val = a.getNombrePara(lm.getHorarios().get(i).getObjetoMateria());
                        }
                    } else {
                        Profesor p = lm.getHorarios().get(i).getObjetoProfesor();
                        if (p != null) {
                            val = p.getDescripcionObjeto();
                        }
                    }
                    tc.setToolTipText(val);
                }
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

    @Action(block = Task.BlockingScope.APPLICATION, enabledProperty = "noEsMultihorario")
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
                            DivisionAlumnosMultimateria div = (DivisionAlumnosMultimateria) ((DefaultMutableTreeNode) nodoBase.getChildAt(i).getChildAt(x)).getUserObject();
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
