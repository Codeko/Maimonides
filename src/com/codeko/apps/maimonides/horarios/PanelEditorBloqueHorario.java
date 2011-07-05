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
 * PanelEditorBloqueHorario.java
 *
 * Created on 08-abr-2009, 10:09:42
 */
package com.codeko.apps.maimonides.horarios;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.elementos.Actividad;
import com.codeko.apps.maimonides.elementos.Dependencia;
import com.codeko.apps.maimonides.elementos.Profesor;
import com.codeko.apps.maimonides.elementos.Unidad;
import com.codeko.swing.CodekoAutoTableModel;
import com.codeko.swing.SwingUtil;
import com.codeko.swing.forms.FormControl;
import com.codeko.util.Obj;
import com.codeko.util.Str;
import com.mysql.jdbc.PreparedStatement;
import java.awt.Color;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.Beans;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;
import org.jdesktop.swingx.decorator.AbstractHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;

/**
 *
 * @author Codeko
 */
public class PanelEditorBloqueHorario extends javax.swing.JPanel {

    FormControl control = null;
    BloqueHorario bloqueOriginal = null;
    DefaultComboBoxModel modeloActividades = new DefaultComboBoxModel();
    DefaultComboBoxModel modeloProfesores = new DefaultComboBoxModel();
    DefaultComboBoxModel modeloMaterias = new DefaultComboBoxModel();
    DefaultComboBoxModel modeloAulas = new DefaultComboBoxModel();
    DefaultListModel modeloUnidadesAsignadas = new DefaultListModel();
    CodekoAutoTableModel<MateriaVirtual> modeloTablaMateriasDisponibles = new CodekoAutoTableModel<MateriaVirtual>(MateriaVirtual.class);
    CodekoAutoTableModel<MateriaVirtual> modeloTablaMateriasAsignadas = new CodekoAutoTableModel<MateriaVirtual>(MateriaVirtual.class);
    BloqueHorario bloqueResultado = null;

    public final FormControl getControl() {
        if (control == null) {
            control = new FormControl(this);
        }
        return control;
    }
    PanelVisionHorario panelVisor = null;

//    public PanelEditorBloqueHorario() {
//        initComponents();
//    }
    public PanelEditorBloqueHorario(PanelVisionHorario panel) {
        initComponents();
        setPanelVisor(panel);
        if (!Beans.isDesignTime()) {
            cargarActividades();
            cargarProfesores();
            cargarAulas();

            listaUnidades.setCellRenderer(new DefaultListCellRenderer() {

                @Override
                public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    label.setIcon(MaimonidesApp.getApplication().getContext().getResourceMap(PanelEditorBloqueHorario.class).getIcon("rendererListaUnidades.icon"));
                    return label;

                }
            });
            cbMaterias.setRenderer(new DefaultListCellRenderer() {

                @Override
                public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof MateriaVirtual && !modeloTablaMateriasDisponibles.getDatos().contains((MateriaVirtual) value)) {
                        label.setBackground(Color.RED);
                    }
                    return label;

                }
            });
            cbMaterias.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    Object value = cbMaterias.getSelectedItem();
                    if (value instanceof MateriaVirtual && !modeloTablaMateriasDisponibles.getDatos().contains((MateriaVirtual) value)) {
                        cbMaterias.setForeground(Color.red);
                        lMateria.setForeground(Color.red);
                    } else {
                        cbMaterias.setForeground(Color.black);
                        lMateria.setForeground(Color.black);
                    }
                }
            });
            modeloUnidadesAsignadas.addListDataListener(new ListDataListener() {

                @Override
                public void intervalAdded(ListDataEvent e) {
                    cargarMateriasDisponibles();
                }

                @Override
                public void intervalRemoved(ListDataEvent e) {
                    cargarMateriasDisponibles();
                }

                @Override
                public void contentsChanged(ListDataEvent e) {
                }
            });

            tablaMateriasAsignadas.addHighlighter(new AbstractHighlighter() {

                @Override
                protected Component doHighlight(Component c, ComponentAdapter ca) {
                    System.out.println(ca.getValue());
                    MateriaVirtual m = modeloTablaMateriasAsignadas.getElemento(tablaMateriasAsignadas.convertRowIndexToModel(ca.row));
                    if (!modeloTablaMateriasDisponibles.getDatos().contains(m)) {
                        c.setBackground(Color.red);
                    }
                    return c;
                }
            });
            cbActividad.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    cambioActividad();
                }
            });
            cbActividad.setSelectedIndex(-1);
            cbProfesores.setSelectedIndex(-1);
            cbMaterias.setSelectedIndex(-1);
            cbAulas.setSelectedIndex(-1);
            panelMaterias.setVisible(false);
            getControl().habilitar(false, panelMaterias);
            getControl().habilitar(false, panelUnidades);
            getControl().habilitar(false, panelAsignacionesMateria);
            getControl().resetearCambios();
        }
    }

    public PanelVisionHorario getPanelVisor() {
        return panelVisor;
    }

    public final void setPanelVisor(PanelVisionHorario panelVisor) {
        this.panelVisor = panelVisor;
    }

    public BloqueHorario getBloqueOriginal() {
        return bloqueOriginal;
    }

    public BloqueHorario getBloqueResultado() {
        return bloqueResultado;
    }

    public void setBloqueResultado(BloqueHorario bloqueResultado) {
        this.bloqueResultado = bloqueResultado;
    }

    public boolean validarFicha() {
        boolean ret = true;
        //&& cbAulas.getSelectedIndex() > -1
        if (cbActividad.getSelectedIndex() > -1 && cbProfesores.getSelectedIndex() > -1) {
            Actividad a = (Actividad) cbActividad.getSelectedItem();
            if (a.getNecesitaUnidad()) {
                if (modeloUnidadesAsignadas.getSize() <= 0) {
                    JOptionPane.showMessageDialog(this, "La actividad seleccionada necesita que asigne al menos una unidad.", "Error", JOptionPane.ERROR_MESSAGE);
                    bAddUnidad.requestFocus();
                    SwingUtil.blink(panelUnidades);
                    ret = false;
                }
            }
            if (ret && a.getNecesitaMateria()) {
                //Vemos si necesitamos una sola materia o varias
                if (panelAsignacionesMateria.isVisible()) {
                    if (cbMaterias.getSelectedIndex() < 0) {
                        JOptionPane.showMessageDialog(this, "La actividad seleccionada necesita que asigne una materia.", "Error", JOptionPane.ERROR_MESSAGE);
                        cbMaterias.requestFocus();
                        SwingUtil.blink(cbMaterias);
                        ret = false;
                    }
                } else if (panelMaterias.isVisible()) {
                    if (modeloTablaMateriasAsignadas.getDatos().size() <= 0) {
                        JOptionPane.showMessageDialog(this, "La actividad seleccionada necesita que asigne una materia.", "Error", JOptionPane.ERROR_MESSAGE);
                        bAddMateria.requestFocus();
                        SwingUtil.blink(panelMaterias);
                        ret = false;
                    }
                }
            }
        } else {
            if (cbActividad.getSelectedIndex() == -1) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar una actividad.", "Error", JOptionPane.ERROR_MESSAGE);
                cbActividad.requestFocus();
                SwingUtil.blink(cbActividad);
            } else if (cbProfesores.getSelectedIndex() == -1) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar un profesor.", "Error", JOptionPane.ERROR_MESSAGE);
                cbProfesores.requestFocus();
                SwingUtil.blink(cbProfesores);
            }
            //TODO No tengo claro si se debe obligar a que haya aula o no
//            else if (cbAulas.getSelectedIndex() == -1) {
//                JOptionPane.showMessageDialog(this, "Debe seleccionar un aula.", "Error", JOptionPane.ERROR_MESSAGE);
//                cbAulas.requestFocus();
//                SwingUtil.blink(cbAulas);
//            }
            ret = false;
        }
        return ret;
    }

    public BloqueHorario getBloqueModificado() {
        BloqueHorario bloque = null;
        //&& cbAulas.getSelectedIndex() > -1
        //TODO Ver si es necesario obligar a que se le asigne un aula
        if (cbActividad.getSelectedIndex() > -1 && cbProfesores.getSelectedIndex() > -1) {
            bloque = new BloqueHorario(MaimonidesApp.getApplication().getAnoEscolar(), cbDia.getSelectedIndex() + 1, cbHora.getSelectedIndex() + 1, (Actividad) cbActividad.getSelectedItem(), (Profesor) cbProfesores.getSelectedItem(), (Dependencia) cbAulas.getSelectedItem());
            bloque.addPropertyChangeListener(getPanelVisor().listenerBloques);
            bloque.setDicu(cbDicu.getSelectedIndex());
            Actividad a = bloque.getActividad();
            if (a.getNecesitaUnidad()) {
                if (modeloUnidadesAsignadas.getSize() > 0) {
                    int tam = modeloUnidadesAsignadas.getSize();
                    for (int i = 0; i < tam; i++) {
                        bloque.addUnidad((Unidad) modeloUnidadesAsignadas.getElementAt(i));
                    }
                } else {
                    bloque = null;
                }
            }
            if (bloque != null && a.getNecesitaMateria()) {
                //Vemos si necesitamos una sola materia o varias
                if (panelMonoMateria.isVisible()) {
                    if (cbMaterias.getSelectedIndex() > -1) {
                        bloque.addMateriaVirtual((MateriaVirtual) cbMaterias.getSelectedItem());
                    } else {
                        bloque = null;
                    }
                } else if (panelMaterias.isVisible()) {
                    if (modeloTablaMateriasAsignadas.getDatos().size() > 0) {
                        bloque.addMateriasVirtuales(modeloTablaMateriasAsignadas.getDatos());
                    } else {
                        bloque = null;
                    }
                } else {
                    bloque = null;
                }
            }
        } else {
            bloque = null;
        }
        return bloque;
    }

    private void setBloqueOriginal(BloqueHorario bloque) {
        this.bloqueOriginal = bloque;
    }

    private void cambioActividad() {
        if (cbActividad.getSelectedIndex() > -1) {
            Actividad a = (Actividad) cbActividad.getSelectedItem();
            getControl().habilitar(a.getNecesitaMateria(), panelMaterias);
            getControl().habilitar(a.getNecesitaUnidad(), panelUnidades);
            getControl().habilitar(a.getNecesitaMateria(), panelAsignacionesMateria);
        } else {
            getControl().habilitar(false, panelMaterias);
            getControl().habilitar(false, panelUnidades);
            getControl().habilitar(false, panelAsignacionesMateria);
        }
    }

    private void cargarMateriasDisponibles() {
        //TODO Implementar que esto sea configurable por checkbox
        boolean soloMatriculadas = true;
        modeloTablaMateriasDisponibles.vaciar();
        Object materiaSel = cbMaterias.getSelectedItem();
        modeloMaterias.removeAllElements();
        if (modeloUnidadesAsignadas.size() > 0) {
            ArrayList<String> cursos = new ArrayList<String>();
            for (int i = 0; i < modeloUnidadesAsignadas.getSize(); i++) {
                Unidad u = (Unidad) modeloUnidadesAsignadas.getElementAt(i);
                if (u.getIdCurso() != null) {
                    String c = u.getIdCurso().toString();
                    if (!cursos.contains(c)) {
                        cursos.add(c);
                    }
                }
                if (u.getIdCurso2() != null) {
                    String c = u.getIdCurso2().toString();
                    if (!cursos.contains(c)) {
                        cursos.add(c);
                    }
                }
            }
            String lCursos = Str.implode(cursos, ",");
            PreparedStatement st = null;
            ResultSet res = null;
            try {
                String sql = "SELECT codigo_materia,nombre FROM materias ";
                if (soloMatriculadas) {
                    sql += " JOIN materias_alumnos AS ma ON ma.materia_id=materias.id ";
                }
                sql += "WHERE ano=? AND curso_id IN(" + lCursos + ") GROUP BY codigo_materia,nombre,evaluable";
                st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
                st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
                res = st.executeQuery();
                Logger.getLogger(PanelEditorBloqueHorario.class.getName()).info(sql);
                while (res.next()) {
                    MateriaVirtual m = new MateriaVirtual(res.getString(1), res.getString(2));
                    modeloTablaMateriasDisponibles.addDato(m);
                    modeloMaterias.addElement(m);
                }
            } catch (SQLException ex) {
                Logger.getLogger(PanelEditorBloqueHorario.class.getName()).log(Level.SEVERE, null, ex);
            }
            Obj.cerrar(st, res);
        }
        if (materiaSel != null && materiaSel instanceof MateriaVirtual) {
            if (!modeloTablaMateriasDisponibles.getDatos().contains((MateriaVirtual) materiaSel)) {
                modeloMaterias.addElement(materiaSel);
            }
            modeloMaterias.setSelectedItem(materiaSel);
        } else {
            cbMaterias.setSelectedIndex(-1);
        }
        tablaMateriasDisponibles.packAll();
        tablaMateriasAsignadas.updateUI();
    }

    private void cargarActividades() {
        PreparedStatement st = null;
        ResultSet res = null;
        try {
            //TODO Actualmente no contemplamos actividades sin unidades
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM actividades WHERE ano=? AND necesita_unidad=1 ");
            st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
            res = st.executeQuery();
            while (res.next()) {
                Actividad a = new Actividad();
                try {
                    a.cargarDesdeResultSet(res);
                    modeloActividades.addElement(a);
                } catch (Exception ex) {
                    Logger.getLogger(PanelEditorBloqueHorario.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        } catch (SQLException ex) {
            Logger.getLogger(PanelEditorBloqueHorario.class.getName()).log(Level.SEVERE, null, ex);
        }
        Obj.cerrar(st, res);
    }

    private void cargarProfesores() {
        PreparedStatement st = null;
        ResultSet res = null;
        try {
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM profesores_ WHERE ano=?");
            st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
            res = st.executeQuery();
            while (res.next()) {
                Profesor p = new Profesor();
                try {
                    p.cargarDesdeResultSet(res);
                    modeloProfesores.addElement(p);
                } catch (Exception ex) {
                    Logger.getLogger(PanelEditorBloqueHorario.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        } catch (SQLException ex) {
            Logger.getLogger(PanelEditorBloqueHorario.class.getName()).log(Level.SEVERE, null, ex);
        }
        Obj.cerrar(st, res);
    }

    private void cargarAulas() {
        PreparedStatement st = null;
        ResultSet res = null;
        try {
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM dependencias WHERE ano=?");
            st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
            res = st.executeQuery();
            while (res.next()) {
                Dependencia p = new Dependencia();
                try {
                    p.cargarDesdeResultSet(res);
                    modeloAulas.addElement(p);
                } catch (Exception ex) {
                    Logger.getLogger(PanelEditorBloqueHorario.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        } catch (SQLException ex) {
            Logger.getLogger(PanelEditorBloqueHorario.class.getName()).log(Level.SEVERE, null, ex);
        }
        Obj.cerrar(st, res);
    }

    @Action
    public void addUnidad() {
        Collection<Unidad> objs = panelArbolUnidades1.getUnidadesSeleccionadas();
        for (Unidad obj : objs) {
            if (!modeloUnidadesAsignadas.contains(obj)) {
                modeloUnidadesAsignadas.addElement(obj);
            }
        }
    }

    @Action
    public void quitUnidad() {
        Object[] objs = listaUnidades.getSelectedValues();
        for (Object obj : objs) {
            modeloUnidadesAsignadas.removeElement(obj);
        }
    }

    @Action
    public void addMateria() {
        int[] rows = tablaMateriasDisponibles.getSelectedRows();
        for (int i : rows) {
            MateriaVirtual m = (modeloTablaMateriasDisponibles.getElemento(tablaMateriasDisponibles.convertRowIndexToModel(i)));
            modeloTablaMateriasAsignadas.addDato(m);
        }
    }

    @Action
    public void quitMateria() {
        ArrayList<MateriaVirtual> materias = new ArrayList<MateriaVirtual>();
        int[] rows = tablaMateriasAsignadas.getSelectedRows();
        for (int i : rows) {
            MateriaVirtual m = (modeloTablaMateriasAsignadas.getElemento(tablaMateriasAsignadas.convertRowIndexToModel(i)));
            materias.add(m);

        }
        modeloTablaMateriasAsignadas.quitarDatos(materias);
    }

    public void setBloqueHorario(BloqueHorario bloque) {
        setBloqueOriginal(bloque);
        modeloTablaMateriasAsignadas.vaciar();
        modeloTablaMateriasDisponibles.vaciar();
        modeloUnidadesAsignadas.clear();
        modeloMaterias.removeAllElements();
        cbDicu.setSelectedIndex(bloque.getDicu());
        cbDia.setSelectedIndex(bloque.getDia() - 1);
        cbHora.setSelectedIndex(bloque.getHora() - 1);
        if (cbHora.getSelectedIndex() < 0) {
            cbHora.setSelectedIndex(0);
        }
        if (cbDia.getSelectedIndex() < 0) {
            cbDia.setSelectedIndex(0);
        }
        cbProfesores.setSelectedItem(bloque.getProfesor());
        cbActividad.setSelectedItem(bloque.getActividad());
        cbAulas.setSelectedItem(bloque.getDependencia());
        for (Unidad u : bloque.getUnidades()) {
            modeloUnidadesAsignadas.addElement(u);
        }
        if (bloque.getMaterias().size() > 0) {
            modeloTablaMateriasAsignadas.addDatos(bloque.getMateriasVirtuales());
            panelMaterias.setVisible((bloque.getMateriasVirtuales().size() > 1));
            panelMonoMateria.setVisible(!(bloque.getMateriasVirtuales().size() > 1));
            cbMaterias.setSelectedItem(bloque.getMateriasVirtuales().get(0));
        }
        tablaMateriasAsignadas.packAll();
        getControl().resetearCambios();
        this.validate();
    }

    @Action(block = Task.BlockingScope.ACTION)
    public Task guardar() {
        return new GuardarTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class GuardarTask extends org.jdesktop.application.Task<BloqueHorario, Void> {

        GuardarTask(org.jdesktop.application.Application app) {
            super(app);
            if (!validarFicha()) {
                cancel(false);
            }
        }

        @Override
        protected BloqueHorario doInBackground() {
            setMessage("Guardando bloque horario...");
            BloqueHorario bloque = getBloqueModificado();
            setBloqueResultado(bloque);
            //Eliminamos el bloque original
            if (getBloqueOriginal().eliminar()) {
                //Y guardamos el nuevo bloque
                if (!bloque.guardar()) {
                    bloque = null;
                }
            } else {
                bloque = null;
            }
            return bloque;
        }

        @Override
        protected void succeeded(BloqueHorario bloque) {
            if (bloque != null) {
                setMessage("Bloque horario creado correctamente.");
                cerrarVentanaSuperior();
            } else {
                //TODO Mostrar ventana 
                setMessage("No se ha creado el bloque horario.");
            }
        }
    }

    private void cerrarVentanaSuperior() {
        Window w = SwingUtilities.getWindowAncestor(this);
        if (w != null) {
            w.dispose();
        }
    }

    @Action
    public void cancelar() {
        cerrarVentanaSuperior();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        lDia = new javax.swing.JLabel();
        cbDia = new javax.swing.JComboBox();
        panelAsignacionesMateria = new javax.swing.JPanel();
        panelMonoMateria = new javax.swing.JPanel();
        lMateria = new javax.swing.JLabel();
        cbMaterias = new javax.swing.JComboBox();
        panelMaterias = new javax.swing.JPanel();
        lMateriasDisponibles = new javax.swing.JLabel();
        lMateriasAsignadas = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tablaMateriasDisponibles = new org.jdesktop.swingx.JXTable();
        jPanel3 = new javax.swing.JPanel();
        bAddMateria = new javax.swing.JButton();
        bQuitMateria = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        tablaMateriasAsignadas = new org.jdesktop.swingx.JXTable();
        lHora = new javax.swing.JLabel();
        cbHora = new javax.swing.JComboBox();
        lActividad = new javax.swing.JLabel();
        cbActividad = new javax.swing.JComboBox();
        panelUnidades = new javax.swing.JPanel();
        panelArbolUnidades1 = new com.codeko.apps.maimonides.cursos.PanelArbolUnidades();
        jScrollPane1 = new javax.swing.JScrollPane();
        listaUnidades = new javax.swing.JList();
        panelBotonesUnidades = new javax.swing.JPanel();
        bAddUnidad = new javax.swing.JButton();
        bQuitUnidad = new javax.swing.JButton();
        lCursosDisponibles = new javax.swing.JLabel();
        lCursosAsignados = new javax.swing.JLabel();
        lProfesor = new javax.swing.JLabel();
        bCancelar = new javax.swing.JButton();
        bAceptar = new javax.swing.JButton();
        lAula = new javax.swing.JLabel();
        cbAulas = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        cbDicu = new javax.swing.JComboBox();
        cbProfesores = new com.codeko.apps.maimonides.profesores.CbProfesores(false);

        setName("Form"); // NOI18N

        lDia.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getResourceMap(PanelEditorBloqueHorario.class);
        lDia.setText(resourceMap.getString("lDia.text")); // NOI18N
        lDia.setName("lDia"); // NOI18N

        cbDia.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Lunes", "Martes", "Miércoles", "Jueves", "Viernes" }));
        cbDia.setName("cbDia"); // NOI18N

        panelAsignacionesMateria.setName("panelAsignacionesMateria"); // NOI18N

        panelMonoMateria.setName("panelMonoMateria"); // NOI18N

        lMateria.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lMateria.setText(resourceMap.getString("lMateria.text")); // NOI18N
        lMateria.setName("lMateria"); // NOI18N

        cbMaterias.setModel(modeloMaterias);
        cbMaterias.setName("cbMaterias"); // NOI18N

        javax.swing.GroupLayout panelMonoMateriaLayout = new javax.swing.GroupLayout(panelMonoMateria);
        panelMonoMateria.setLayout(panelMonoMateriaLayout);
        panelMonoMateriaLayout.setHorizontalGroup(
            panelMonoMateriaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMonoMateriaLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lMateria)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbMaterias, 0, 596, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelMonoMateriaLayout.setVerticalGroup(
            panelMonoMateriaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMonoMateriaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(lMateria)
                .addComponent(cbMaterias, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        panelMaterias.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("panelMaterias.border.title"))); // NOI18N
        panelMaterias.setName("panelMaterias"); // NOI18N
        panelMaterias.setLayout(new java.awt.GridBagLayout());

        lMateriasDisponibles.setFont(resourceMap.getFont("lMateriasDisponibles.font")); // NOI18N
        lMateriasDisponibles.setText(resourceMap.getString("lMateriasDisponibles.text")); // NOI18N
        lMateriasDisponibles.setName("lMateriasDisponibles"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        panelMaterias.add(lMateriasDisponibles, gridBagConstraints);

        lMateriasAsignadas.setFont(resourceMap.getFont("lMateriasAsignadas.font")); // NOI18N
        lMateriasAsignadas.setText(resourceMap.getString("lMateriasAsignadas.text")); // NOI18N
        lMateriasAsignadas.setName("lMateriasAsignadas"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        panelMaterias.add(lMateriasAsignadas, gridBagConstraints);

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        tablaMateriasDisponibles.setModel(modeloTablaMateriasDisponibles);
        tablaMateriasDisponibles.setColumnControlVisible(true);
        tablaMateriasDisponibles.setName("tablaMateriasDisponibles"); // NOI18N
        jScrollPane2.setViewportView(tablaMateriasDisponibles);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        panelMaterias.add(jScrollPane2, gridBagConstraints);

        jPanel3.setName("jPanel3"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getActionMap(PanelEditorBloqueHorario.class, this);
        bAddMateria.setAction(actionMap.get("addMateria")); // NOI18N
        bAddMateria.setName("bAddMateria"); // NOI18N

        bQuitMateria.setAction(actionMap.get("quitMateria")); // NOI18N
        bQuitMateria.setName("bQuitMateria"); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bAddMateria)
                    .addComponent(bQuitMateria))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(bAddMateria)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bQuitMateria)
                .addContainerGap(33, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        panelMaterias.add(jPanel3, gridBagConstraints);

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        tablaMateriasAsignadas.setModel(modeloTablaMateriasAsignadas);
        tablaMateriasAsignadas.setColumnControlVisible(true);
        tablaMateriasAsignadas.setName("tablaMateriasAsignadas"); // NOI18N
        jScrollPane3.setViewportView(tablaMateriasAsignadas);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        panelMaterias.add(jScrollPane3, gridBagConstraints);

        javax.swing.GroupLayout panelAsignacionesMateriaLayout = new javax.swing.GroupLayout(panelAsignacionesMateria);
        panelAsignacionesMateria.setLayout(panelAsignacionesMateriaLayout);
        panelAsignacionesMateriaLayout.setHorizontalGroup(
            panelAsignacionesMateriaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelMonoMateria, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(panelAsignacionesMateriaLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(panelMaterias, javax.swing.GroupLayout.DEFAULT_SIZE, 660, Short.MAX_VALUE)
                .addGap(10, 10, 10))
        );
        panelAsignacionesMateriaLayout.setVerticalGroup(
            panelAsignacionesMateriaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelAsignacionesMateriaLayout.createSequentialGroup()
                .addComponent(panelMonoMateria, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(panelMaterias, javax.swing.GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE)
                .addContainerGap())
        );

        lHora.setText(resourceMap.getString("lHora.text")); // NOI18N
        lHora.setName("lHora"); // NOI18N

        cbHora.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1ª", "2ª", "3ª", "4ª", "5ª", "6ª" }));
        cbHora.setName("cbHora"); // NOI18N

        lActividad.setText(resourceMap.getString("lActividad.text")); // NOI18N
        lActividad.setName("lActividad"); // NOI18N

        cbActividad.setModel(modeloActividades);
        cbActividad.setName("cbActividad"); // NOI18N

        panelUnidades.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("panelUnidades.border.title"))); // NOI18N
        panelUnidades.setName("panelUnidades"); // NOI18N
        panelUnidades.setLayout(new java.awt.GridBagLayout());

        panelArbolUnidades1.setName("panelArbolUnidades1"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        panelUnidades.add(panelArbolUnidades1, gridBagConstraints);

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        listaUnidades.setModel(modeloUnidadesAsignadas);
        listaUnidades.setName("listaUnidades"); // NOI18N
        jScrollPane1.setViewportView(listaUnidades);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        panelUnidades.add(jScrollPane1, gridBagConstraints);

        panelBotonesUnidades.setName("panelBotonesUnidades"); // NOI18N

        bAddUnidad.setAction(actionMap.get("addUnidad")); // NOI18N
        bAddUnidad.setName("bAddUnidad"); // NOI18N

        bQuitUnidad.setAction(actionMap.get("quitUnidad")); // NOI18N
        bQuitUnidad.setName("bQuitUnidad"); // NOI18N

        javax.swing.GroupLayout panelBotonesUnidadesLayout = new javax.swing.GroupLayout(panelBotonesUnidades);
        panelBotonesUnidades.setLayout(panelBotonesUnidadesLayout);
        panelBotonesUnidadesLayout.setHorizontalGroup(
            panelBotonesUnidadesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBotonesUnidadesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(bQuitUnidad)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelBotonesUnidadesLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(bAddUnidad)
                .addContainerGap())
        );
        panelBotonesUnidadesLayout.setVerticalGroup(
            panelBotonesUnidadesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBotonesUnidadesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(bAddUnidad)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bQuitUnidad)
                .addContainerGap(64, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        panelUnidades.add(panelBotonesUnidades, gridBagConstraints);

        lCursosDisponibles.setFont(resourceMap.getFont("lCursosDisponibles.font")); // NOI18N
        lCursosDisponibles.setText(resourceMap.getString("lCursosDisponibles.text")); // NOI18N
        lCursosDisponibles.setName("lCursosDisponibles"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        panelUnidades.add(lCursosDisponibles, gridBagConstraints);

        lCursosAsignados.setFont(resourceMap.getFont("lCursosAsignados.font")); // NOI18N
        lCursosAsignados.setText(resourceMap.getString("lCursosAsignados.text")); // NOI18N
        lCursosAsignados.setName("lCursosAsignados"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        panelUnidades.add(lCursosAsignados, gridBagConstraints);

        lProfesor.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lProfesor.setText(resourceMap.getString("lProfesor.text")); // NOI18N
        lProfesor.setName("lProfesor"); // NOI18N

        bCancelar.setAction(actionMap.get("cancelar")); // NOI18N
        bCancelar.setName("bCancelar"); // NOI18N

        bAceptar.setAction(actionMap.get("guardar")); // NOI18N
        bAceptar.setName("bAceptar"); // NOI18N

        lAula.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lAula.setText(resourceMap.getString("lAula.text")); // NOI18N
        lAula.setName("lAula"); // NOI18N

        cbAulas.setModel(modeloAulas);
        cbAulas.setName("cbAulas"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        cbDicu.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Sólo no diversificación", "Sólo diversificación", "Diversificación y no diversificación" }));
        cbDicu.setName("cbDicu"); // NOI18N

        cbProfesores.setName("cbProfesores"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(490, Short.MAX_VALUE)
                .addComponent(bAceptar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bCancelar)
                .addContainerGap())
            .addComponent(panelAsignacionesMateria, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelUnidades, javax.swing.GroupLayout.DEFAULT_SIZE, 656, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(34, 34, 34)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lDia, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lProfesor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lAula, javax.swing.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(cbDia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(lHora)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cbHora, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(lActividad)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cbActividad, 0, 256, Short.MAX_VALUE))
                    .addComponent(cbAulas, 0, 553, Short.MAX_VALUE)
                    .addComponent(cbDicu, 0, 553, Short.MAX_VALUE)
                    .addComponent(cbProfesores, javax.swing.GroupLayout.DEFAULT_SIZE, 553, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lDia)
                    .addComponent(cbActividad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbDia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lHora)
                    .addComponent(cbHora, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lActividad))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lProfesor)
                    .addComponent(cbProfesores, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lAula)
                    .addComponent(cbAulas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbDicu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addGap(13, 13, 13)
                .addComponent(panelUnidades, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelAsignacionesMateria, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bCancelar)
                    .addComponent(bAceptar))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bAceptar;
    private javax.swing.JButton bAddMateria;
    private javax.swing.JButton bAddUnidad;
    private javax.swing.JButton bCancelar;
    private javax.swing.JButton bQuitMateria;
    private javax.swing.JButton bQuitUnidad;
    private javax.swing.JComboBox cbActividad;
    private javax.swing.JComboBox cbAulas;
    private javax.swing.JComboBox cbDia;
    private javax.swing.JComboBox cbDicu;
    private javax.swing.JComboBox cbHora;
    private javax.swing.JComboBox cbMaterias;
    private com.codeko.apps.maimonides.profesores.CbProfesores cbProfesores;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lActividad;
    private javax.swing.JLabel lAula;
    private javax.swing.JLabel lCursosAsignados;
    private javax.swing.JLabel lCursosDisponibles;
    private javax.swing.JLabel lDia;
    private javax.swing.JLabel lHora;
    private javax.swing.JLabel lMateria;
    private javax.swing.JLabel lMateriasAsignadas;
    private javax.swing.JLabel lMateriasDisponibles;
    private javax.swing.JLabel lProfesor;
    private javax.swing.JList listaUnidades;
    private com.codeko.apps.maimonides.cursos.PanelArbolUnidades panelArbolUnidades1;
    private javax.swing.JPanel panelAsignacionesMateria;
    private javax.swing.JPanel panelBotonesUnidades;
    private javax.swing.JPanel panelMaterias;
    private javax.swing.JPanel panelMonoMateria;
    private javax.swing.JPanel panelUnidades;
    private org.jdesktop.swingx.JXTable tablaMateriasAsignadas;
    private org.jdesktop.swingx.JXTable tablaMateriasDisponibles;
    // End of variables declaration//GEN-END:variables
}
