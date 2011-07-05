/*
 * PanelCursos.java
 *
 * Created on 20-may-2009, 18:00:50
 */
package com.codeko.apps.maimonides.cursos;

import com.codeko.apps.maimonides.IPanel;
import com.codeko.apps.maimonides.impresion.Impresion;
import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesBean;
import com.codeko.apps.maimonides.MaimonidesUtil;
import com.codeko.apps.maimonides.elementos.Curso;
import com.codeko.apps.maimonides.elementos.Profesor;
import com.codeko.apps.maimonides.elementos.Unidad;
import com.codeko.apps.maimonides.usr.Permisos;
import com.codeko.swing.CodekoAutoTableModel;
import com.codeko.util.Fechas;
import com.codeko.util.Num;
import java.awt.Component;
import java.beans.Beans;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.GregorianCalendar;
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
import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 *
 * @author Codeko
 */
public class PanelGrupos extends javax.swing.JPanel implements IPanel {

    CodekoAutoTableModel<Unidad> modelo = new CodekoAutoTableModel<Unidad>(Unidad.class) {

        @Override
        public void elementoModificado(Unidad elemento, int col, Object valor) {
            elemento.guardar();
        }
    };
    boolean cargado = false;

    /** Creates new form PanelCursos */
    public PanelGrupos() {
        initComponents();
        MaimonidesUtil.addMenuTabla(tabla, "Grupos");
        tabla.getColumnExt("Código").setVisible(false);
        tabla.getColumnExt("Nombre Original").setVisible(false);
        TableColumnExt colTutor = tabla.getColumnExt("Tutor");
        colTutor.setCellRenderer(new DefaultTableCellRenderer() {

            @Override
            public void setValue(Object value) {
                try {
                    int id = Num.getInt(value);
                    if (id > 0) {
                        Profesor p = Profesor.getProfesor(id);
                        setText(p.getDescripcionObjeto());
                    } else {
                        setText("");
                    }
                } catch (Exception ex) {
                    Logger.getLogger(PanelGrupos.class.getName()).log(Level.SEVERE, null, ex);
                    setText("");
                }
            }
        });
        JComboBox comboTutores = new JComboBox(Profesor.getProfesores().toArray());
        comboTutores.insertItemAt("", 0);
        DefaultCellEditor dceTutor = new DefaultCellEditor(comboTutores) {

            @Override
            public Object getCellEditorValue() {
                Object obj = super.getCellEditorValue();
                if (obj instanceof Profesor) {
                    return ((Profesor) obj).getId();
                }
                return null;
            }

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                Component c = super.getTableCellEditorComponent(tabla, value, isSelected, row, column);
                try {
                    int id = Num.getInt(value);
                    if (id > 0) {
                        Profesor p = Profesor.getProfesor(id);
                        ((JComboBox) c).setSelectedItem(p);
                    } else {
                        ((JComboBox) c).setSelectedIndex(0);
                    }
                } catch (Exception ex) {
                    Logger.getLogger(PanelGrupos.class.getName()).log(Level.SEVERE, null, ex);
                }
                return c;
            }
        };
        dceTutor.setClickCountToStart(2);
        colTutor.setCellEditor(dceTutor);


        JComboBox comboCursos = new JComboBox(Curso.getCursos().toArray());
        DefaultCellEditor dceCurso = new DefaultCellEditor(comboCursos) {

            @Override
            public Object getCellEditorValue() {
                Object obj = super.getCellEditorValue();
                if (obj instanceof Curso) {
                    return ((Curso) obj).getId();
                }
                return null;
            }

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                Component c = super.getTableCellEditorComponent(tabla, value, isSelected, row, column);
                try {
                    Curso p = Curso.getCurso(Num.getInt(value));
                    ((JComboBox) c).setSelectedItem(p);
                } catch (Exception ex) {
                    Logger.getLogger(PanelGrupos.class.getName()).log(Level.SEVERE, null, ex);
                }
                return c;
            }
        };
        dceCurso.setClickCountToStart(2);


        tabla.getColumnExt("Posición").setVisible(false);

        TableColumnExt colCurso = tabla.getColumnExt("Curso");
        DefaultTableCellRenderer rendererCurso = new DefaultTableCellRenderer() {

            @Override
            public void setValue(Object value) {
                if (value != null) {
                    try {
                        Curso c = Curso.getCurso(Num.getInt(value));
                        setText(c.getCurso());
                    } catch (Exception ex) {
                        Logger.getLogger(PanelGrupos.class.getName()).log(Level.SEVERE, null, ex);
                        setText("");
                    }
                } else {
                    setText("");
                }
            }
        };
        colCurso.setCellRenderer(rendererCurso);
        TableColumnExt colCurso2 = tabla.getColumnExt("Curso mixto");
        colCurso2.setCellRenderer(rendererCurso);
        colCurso2.setVisible(false);
        colCurso.setCellEditor(dceCurso);
        colCurso2.setCellEditor(dceCurso);

        tabla.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int pos = tabla.getSelectedRow();
                    setElementoSeleccionado(pos > -1);
                }
            }
        });

        modelo.setEditable(Permisos.edicion(getClass()));

        bNuevo.setEnabled(Permisos.creacion(getClass()));
        bNuevo.setVisible(Permisos.creacion(getClass()));

        bBorrar.setEnabled(Permisos.borrado(getClass()));
        bBorrar.setVisible(Permisos.borrado(getClass()));
    }

    public boolean isCargado() {
        return cargado;
    }

    public void setCargado(boolean cargado) {
        this.cargado = cargado;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToolBar1 = new javax.swing.JToolBar();
        bActualizar = new javax.swing.JButton();
        bNuevo = new javax.swing.JButton();
        bBorrar = new javax.swing.JButton();
        bImprimirPartes = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabla = new org.jdesktop.swingx.JXTable();

        setName("maimonides.paneles.faltas.partes_genericos"); // NOI18N
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

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        jToolBar1.setName("jToolBar1"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getActionMap(PanelGrupos.class, this);
        bActualizar.setAction(actionMap.get("actualizar")); // NOI18N
        bActualizar.setFocusable(false);
        bActualizar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bActualizar.setName("bActualizar"); // NOI18N
        bActualizar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(bActualizar);

        bNuevo.setAction(actionMap.get("nuevo")); // NOI18N
        bNuevo.setFocusable(false);
        bNuevo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bNuevo.setName("bNuevo"); // NOI18N
        bNuevo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(bNuevo);

        bBorrar.setAction(actionMap.get("borrar")); // NOI18N
        bBorrar.setFocusable(false);
        bBorrar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bBorrar.setName("bBorrar"); // NOI18N
        bBorrar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(bBorrar);

        bImprimirPartes.setAction(actionMap.get("imprimirPartes")); // NOI18N
        bImprimirPartes.setFocusable(false);
        bImprimirPartes.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bImprimirPartes.setName("bImprimirPartes"); // NOI18N
        bImprimirPartes.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(bImprimirPartes);

        jButton1.setAction(actionMap.get("imprimirPartesSeleccionados")); // NOI18N
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setName("jButton1"); // NOI18N
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton1);

        add(jToolBar1, java.awt.BorderLayout.PAGE_START);

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        tabla.setModel(modelo);
        tabla.setColumnControlVisible(true);
        tabla.setName("tabla"); // NOI18N
        jScrollPane1.setViewportView(tabla);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void formAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_formAncestorAdded
        if (!Beans.isDesignTime() && !isCargado()) {
            MaimonidesUtil.ejecutarTask(this, "actualizar");
        }
    }//GEN-LAST:event_formAncestorAdded

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task actualizar() {
        return new ActualizarTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    @Override
    public boolean puedoSusituir() {
        return true;
    }

    private class ActualizarTask extends org.jdesktop.application.Task<ArrayList<Unidad>, Void> {

        ActualizarTask(org.jdesktop.application.Application app) {
            super(app);
            modelo.vaciar();
            setMessage("Cargando unidades...");
            setCargado(true);
        }

        @Override
        protected ArrayList<Unidad> doInBackground() {
            ArrayList<Unidad> unidades = Unidad.getUnidades();
            return unidades;
        }

        @Override
        protected void succeeded(ArrayList<Unidad> result) {
            modelo.addDatos(result);
            setMessage("Unidades cargadas correctamente.");
            setCargado(true);
            tabla.packAll();
        }
    }

    @Action(block = Task.BlockingScope.WINDOW)
    public Task imprimirPartes() {
        return new ImprimirPartesTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class), false);
    }

    @Action(block = Task.BlockingScope.WINDOW)
    public Task imprimirPartesSeleccionados() {
        return new ImprimirPartesTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class), true);
    }

    private class ImprimirPartesTask extends org.jdesktop.application.Task<Object, Void> {

        ArrayList<Unidad> unidades = new ArrayList<Unidad>();
        GregorianCalendar fecha = new GregorianCalendar();

        ImprimirPartesTask(org.jdesktop.application.Application app, boolean seleccionados) {
            super(app);
            fecha.add(GregorianCalendar.DAY_OF_MONTH, 1);
            JXDatePicker dp = new JXDatePicker(fecha.getTime());
            MaimonidesUtil.setFormatosFecha(dp, true);
            JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), dp, "Indique la fecha del parte a imprimir", JOptionPane.QUESTION_MESSAGE);

            if (dp.getDate() != null) {
                fecha = Fechas.toGregorianCalendar(dp.getDate());
            }
            if (seleccionados) {
                int[] rows = tabla.getSelectedRows();
                for (int i : rows) {
                    unidades.add(modelo.getElemento(tabla.convertRowIndexToModel(i)));
                }
            } else {
                unidades.addAll(modelo.getDatos());
            }
        }

        @Override
        protected Object doInBackground() {
            MaimonidesBean bean = new MaimonidesBean();
            bean.addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                }
            });
            Impresion.getImpresion().imprimirPartesGenericos(bean, MaimonidesApp.getApplication().getAnoEscolar(), fecha, unidades);
            return null;
        }

        @Override
        protected void succeeded(Object result) {
        }
    }

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task nuevo() {
        return new NuevoTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class NuevoTask extends org.jdesktop.application.Task<Unidad, Void> {

        NuevoTask(org.jdesktop.application.Application app) {
            super(app);
        }

        @Override
        protected Unidad doInBackground() {
            setMessage("Creando grupo...");
            Unidad d = new Unidad();
            d.setAnoEscolar(MaimonidesApp.getApplication().getAnoEscolar());
            d.setDescripcion("Nuevo grupo");
            if (!d.guardar()) {
                d = null;
            }
            return d;
        }

        @Override
        protected void succeeded(Unidad result) {
            if (result != null && result.getId() != null) {
                modelo.addDato(result);
                int row = tabla.convertRowIndexToView(modelo.indexOf(result));
                tabla.scrollRowToVisible(row);
                tabla.editCellAt(row, 0, null);
                Object cec = tabla.getEditorComponent();
                if (cec instanceof JTextComponent) {
                    ((JTextComponent) tabla.getEditorComponent()).requestFocus();
                    ((JTextComponent) tabla.getEditorComponent()).setCaretPosition(0);
                    ((JTextComponent) tabla.getEditorComponent()).selectAll();
                }
                setMessage("Grupo creada correctamente.");
            } else {
                setMessage("Error creando grupo.");
            }
        }
    }

    @Action(block = Task.BlockingScope.APPLICATION, enabledProperty = "elementoSeleccionado")
    public Task borrar() {
        return new BorrarTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class BorrarTask extends org.jdesktop.application.Task<ArrayList<Unidad>, Void> {

        ArrayList<Unidad> a = null;
        boolean borrar = false;

        BorrarTask(org.jdesktop.application.Application app) {
            super(app);
            a = getObjetosSeleccionados();

            if (a != null) {
                int op = JOptionPane.showConfirmDialog(MaimonidesApp.getApplication().getMainFrame(), "¿Esta seguro de que quiere eliminar las lineas seleccionados (" + a.size() + ")?", "Borrar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                borrar = op == JOptionPane.YES_OPTION;
            }
            firePropertyChange("setIniciado", null, true);
        }

        @Override
        protected ArrayList<Unidad> doInBackground() {
            //TODO Quizás habría que verificar primero que no se está usando
            if (a != null && borrar) {
                int count = 0;
                for (Unidad ae : a) {
                    setProgress(++count, 0, a.size());
                    setMessage("Borrando unidad: " + ae.getDescripcion() + "...");
                    ae.borrar();
                }

                return a;
            }
            return null;
        }

        @Override
        protected void succeeded(ArrayList<Unidad> result) {
            if (result != null) {
                setMessage("Grupos borrados correctamente.");
                modelo.quitarDatos(result);
            }
        }
    }

    public ArrayList<Unidad> getObjetosSeleccionados() {
        int[] sels = tabla.getSelectedRows();
        ArrayList<Unidad> arts = new ArrayList<Unidad>(sels.length);
        for (int pos : sels) {
            if (pos > -1) {
                pos = tabla.convertRowIndexToModel(pos);
                arts.add(modelo.getElemento(pos));
            }
        }
        return arts;
    }
    private boolean elementoSeleccionado = false;

    public boolean isElementoSeleccionado() {
        return elementoSeleccionado;
    }

    public void setElementoSeleccionado(boolean b) {
        boolean old = isElementoSeleccionado();
        this.elementoSeleccionado = b;
        firePropertyChange("elementoSeleccionado", old, isElementoSeleccionado());
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bActualizar;
    private javax.swing.JButton bBorrar;
    private javax.swing.JButton bImprimirPartes;
    private javax.swing.JButton bNuevo;
    private javax.swing.JButton jButton1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar jToolBar1;
    private org.jdesktop.swingx.JXTable tabla;
    // End of variables declaration//GEN-END:variables
}
