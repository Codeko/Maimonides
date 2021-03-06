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
 * PanelMaterias.java
 *
 * Created on 9 de septiembre de 2008, 8:12
 */
package com.codeko.apps.maimonides.materias;

import com.codeko.apps.maimonides.ICargable;
import com.codeko.apps.maimonides.IPanel;
import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesUtil;
import com.codeko.apps.maimonides.cursos.PanelGrupos;
import com.codeko.apps.maimonides.elementos.Curso;
import com.codeko.apps.maimonides.elementos.Materia;
import com.codeko.apps.maimonides.elementos.Profesor;
import com.codeko.apps.maimonides.usr.Permisos;
import com.codeko.swing.CodekoAutoTableModel;
import com.codeko.util.Obj;
import com.codeko.util.Str;
import com.mysql.jdbc.PreparedStatement;
import java.awt.Component;
import java.beans.Beans;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.JTextComponent;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 *
 * @author  Codeko
 */
public class PanelMaterias extends javax.swing.JPanel implements IPanel, ICargable {

    CodekoAutoTableModel<Materia> modelo = new CodekoAutoTableModel<Materia>(Materia.class) {

        @Override
        public void elementoModificado(Materia elemento, int col, Object valor) {
            elemento.guardar();
        }
    };
    boolean cargado = false;

    /** Creates new form PanelMaterias */
    public PanelMaterias() {
        initComponents();
        MaimonidesUtil.addMenuTabla(tabla, "Matérias");
        tabla.getColumnExt("Código").setVisible(false);
        TableColumnExt tc = tabla.getColumnExt("Capacidad");
        tc.setPreferredWidth(100);
        tc = tabla.getColumnExt("Evaluable");
        tc.setPreferredWidth(80);
        tc = tabla.getColumnExt("Abreviatura");
        tc.setPreferredWidth(80);

        tc = tabla.getColumnExt("Curso");
        tc.setCellRenderer(new DefaultTableCellRenderer() {

            @Override
            public void setValue(Object value) {
                setText(Str.noNulo(value));
            }
        });

        JComboBox comboCursos = new JComboBox(Curso.getCursos().toArray());
        comboCursos.insertItemAt("", 0);
        DefaultCellEditor dceCurso = new DefaultCellEditor(comboCursos) {

            @Override
            public Object getCellEditorValue() {
                Object obj = super.getCellEditorValue();
                if (obj instanceof Curso) {
                    return ((Curso) obj);
                }
                return null;
            }

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                Component c = super.getTableCellEditorComponent(tabla, value, isSelected, row, column);
                try {
                    if (value instanceof Curso) {
                        ((JComboBox) c).setSelectedItem(value);
                    } else {
                        ((JComboBox) c).setSelectedIndex(0);
                    }
                } catch (Exception ex) {
                    Logger.getLogger(PanelGrupos.class.getName()).log(Level.SEVERE, null, ex);
                }
                return c;
            }
        };
        dceCurso.setClickCountToStart(2);
        tc.setCellEditor(dceCurso);

        tabla.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int pos = tabla.getSelectedRow();
                    if (pos != -1) {
                        pos = tabla.convertRowIndexToModel(pos);
                        Object objeto = modelo.getElemento(pos);
                        setMateriaSeleccionada(objeto != null);
                    } else {
                        setMateriaSeleccionada(false);
                    }
                }
            }
        });

        modelo.setEditable(Permisos.edicion(getClass()));

        bNuevo.setEnabled(Permisos.creacion(getClass()));
        bNuevo.setVisible(Permisos.creacion(getClass()));

        bBorrar.setEnabled(Permisos.borrado(getClass()));
        bBorrar.setVisible(Permisos.borrado(getClass()));

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
        barraHerramientas = new javax.swing.JToolBar();
        bActualizar = new javax.swing.JButton();
        bNuevo = new javax.swing.JButton();
        bBorrar = new javax.swing.JButton();

        setName("maimonides.paneles.datos.listado_materias"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        tabla.setModel(modelo);
        tabla.setColumnControlVisible(true);
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
        tabla.setHighlighters(HighlighterFactory.createAlternateStriping());
        jScrollPane1.setViewportView(tabla);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);

        barraHerramientas.setFloatable(false);
        barraHerramientas.setRollover(true);
        barraHerramientas.setName("barraHerramientas"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getActionMap(PanelMaterias.class, this);
        bActualizar.setAction(actionMap.get("cargarMaterias")); // NOI18N
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

        bBorrar.setAction(actionMap.get("borrarMateria")); // NOI18N
        bBorrar.setFocusable(false);
        bBorrar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bBorrar.setName("bBorrar"); // NOI18N
        bBorrar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraHerramientas.add(bBorrar);

        add(barraHerramientas, java.awt.BorderLayout.PAGE_START);
    }// </editor-fold>//GEN-END:initComponents

private void tablaAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_tablaAncestorAdded
    cargar();
}//GEN-LAST:event_tablaAncestorAdded
    private boolean materiaSeleccionada = false;

    public boolean isMateriaSeleccionada() {
        return materiaSeleccionada;
    }

    public void setMateriaSeleccionada(boolean b) {
        boolean old = isMateriaSeleccionada();
        this.materiaSeleccionada = b;
        firePropertyChange("materiaSeleccionada", old, isMateriaSeleccionada());
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bActualizar;
    private javax.swing.JButton bBorrar;
    private javax.swing.JButton bNuevo;
    private javax.swing.JToolBar barraHerramientas;
    private javax.swing.JScrollPane jScrollPane1;
    private org.jdesktop.swingx.JXTable tabla;
    // End of variables declaration//GEN-END:variables

    @Override
    public boolean puedoSusituir() {
        return true;
    }

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task cargarMaterias() {
        return new CargarMateriasTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    @Override
    public void cargar() {
        if (!Beans.isDesignTime() && !isCargado()) {
            MaimonidesUtil.ejecutarTask(this, "cargarMaterias");
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

    private class CargarMateriasTask extends org.jdesktop.application.Task<ArrayList<Materia>, Void> {

        CargarMateriasTask(org.jdesktop.application.Application app) {
            super(app);
            vaciar();
            setCargado(true);
        }

        @Override
        protected ArrayList<Materia> doInBackground() {
            ArrayList<Materia> datos = new ArrayList<Materia>();
            try {
                Profesor p = null;
                String joinHorario = "";
                String whereHorario = "";
                if (Permisos.isUsuarioSoloProfesor()) {
                    p = Permisos.getFiltroProfesor();
                    if (p != null) {
                        joinHorario = " LEFT JOIN horarios AS h ON h.materia_id=m.id AND h.ano=m.ano ";
                        whereHorario = " AND h.profesor_id=? ";
                    }
                }
                PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT m.* FROM materias AS m " + joinHorario + " WHERE m.ano=? " + whereHorario + " ORDER BY m.curso_id,m.nombre");
                st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
                if (p != null) {
                    st.setInt(2, p.getId());
                }
                ResultSet res = st.executeQuery();
                while (res.next()) {
                    Materia m = new Materia();
                    m.cargarDesdeResultSet(res);
                    datos.add(m);
                }
                Obj.cerrar(st, res);
            } catch (Exception ex) {
                Logger.getLogger(PanelMaterias.class.getName()).log(Level.SEVERE, "Error cargando lista de materias para año: " + MaimonidesApp.getApplication().getAnoEscolar(), ex);
            }
            return datos;
        }

        @Override
        protected void succeeded(ArrayList<Materia> result) {
            modelo.addDatos(result);
            tabla.packAll();
        }
    }

    @Action(block = Task.BlockingScope.COMPONENT, enabledProperty = "materiaSeleccionada")
    public Task borrarMateria() {
        return new BorrarMateriaTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class BorrarMateriaTask extends org.jdesktop.application.Task<ArrayList<Materia>, Void> {

        boolean borrar = false;
        ArrayList<Materia> materias = new ArrayList<Materia>();

        BorrarMateriaTask(org.jdesktop.application.Application app) {
            super(app);
            int[] rows = tabla.getSelectedRows();
            int op = JOptionPane.showConfirmDialog(MaimonidesApp.getApplication().getMainFrame(), "¿Borrar las materias seleccionadas (" + rows.length + ")?", "Confirmación borrar", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (op == JOptionPane.YES_OPTION) {
                borrar = true;
                for (int i : rows) {
                    i = tabla.convertRowIndexToModel(i);
                    materias.add(modelo.getElemento(i));
                }
            }
        }

        @Override
        protected ArrayList<Materia> doInBackground() {
            if (borrar) {
                for (Materia m : materias) {
                    m.borrar();
                }
            }
            return materias;
        }

        @Override
        protected void succeeded(ArrayList<Materia> result) {
            if (borrar) {
                modelo.quitarDatos(result);
                setMateriaSeleccionada(false);
            }
        }
    }

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task nuevo() {
        return new NuevoTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class NuevoTask extends org.jdesktop.application.Task<Materia, Void> {

        NuevoTask(org.jdesktop.application.Application app) {
            super(app);
        }

        @Override
        protected Materia doInBackground() {
            setMessage("Creando materia...");
            Materia d = new Materia();
            d.setAnoEscolar(MaimonidesApp.getApplication().getAnoEscolar());
            d.setDescripcion("");
            d.setCodigoMateria("");
            if (!d.guardar()) {
                d = null;
            }
            return d;
        }

        @Override
        protected void succeeded(Materia result) {
            if (result != null && result.getId() != null) {
                modelo.addDato(result);
                int row = tabla.convertRowIndexToView(modelo.indexOf(result));
                tabla.scrollRowToVisible(row);
                //TODO Hay que ver la forma de referenciar a la columna por nombre no por posición
                tabla.editCellAt(row, 2, null);
                Object cec = tabla.getEditorComponent();
                if (cec instanceof JTextComponent) {
                    ((JTextComponent) tabla.getEditorComponent()).requestFocus();
                    ((JTextComponent) tabla.getEditorComponent()).setCaretPosition(0);
                    ((JTextComponent) tabla.getEditorComponent()).selectAll();
                }
                setMessage("Materia creada correctamente.");
            } else {
                setMessage("Error creando materia.");
            }
        }
    }
}
