/*
 * PanelSelectorAlumnos.java
 *
 * Created on 01-jul-2010, 13:48:48
 */
package com.codeko.apps.maimonides.cartero;

import com.codeko.apps.maimonides.DateCellEditor;
import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesUtil;
import com.codeko.apps.maimonides.alumnos.PanelAlumnos;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.Curso;
import com.codeko.apps.maimonides.elementos.Profesor;
import com.codeko.apps.maimonides.elementos.Unidad;
import com.codeko.apps.maimonides.usr.Permisos;
import com.codeko.swing.CodekoTableModel;
import com.codeko.util.Fechas;
import com.codeko.util.Obj;
import com.mysql.jdbc.PreparedStatement;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;

/**
 *
 * @author codeko
 */
public class PanelSelectorDestinatarios extends javax.swing.JPanel {

    CodekoTableModel<Alumno> modeloAlumnos = new CodekoTableModel<Alumno>(new Alumno());
    CodekoTableModel<Profesor> modeloProfesores = new CodekoTableModel<Profesor>(new Profesor());
    Object filtroAlumno = null;
    DefaultListModel modeloSeleccionados = new DefaultListModel();
    boolean procesando = false;

    public boolean isProcesando() {
        return procesando;
    }

    public void setProcesando(boolean procesando) {
        boolean old = this.procesando;
        this.procesando = procesando;
        if (old != procesando && !procesando) {
            seleccionCambiada();
        }
    }

    /** Creates new form PanelSelectorAlumnos */
    public PanelSelectorDestinatarios() {
        initComponents();
        modeloAlumnos.setEditable(false);
        modeloProfesores.setEditable(false);
        modeloProfesores.addDatos(Profesor.getProfesores());

        tablaAlumnos.getColumnExt("Código").setVisible(false);
        tablaAlumnos.getColumnExt("N.Escolar").setVisible(false);
        tablaAlumnos.getColumnExt("Bilingüe").setVisible(false);
        tablaAlumnos.getColumnExt("Repetidor").setVisible(false);
        tablaAlumnos.getColumnExt("D.I.C.U.").setVisible(false);

        tablaProfesores.getColumnExt("Código Séneca").setVisible(false);
        tablaProfesores.getColumnExt("F. Toma").setVisible(false);
        tablaProfesores.getColumnExt("Puesto").setVisible(false);
        tablaProfesores.getColumnExt("e-mail").setVisible(false);
        tablaProfesores.setDefaultRenderer(GregorianCalendar.class, new DefaultTableCellRenderer() {

            @Override
            public void setValue(Object val) {
                if (val instanceof GregorianCalendar) {
                    setText(Fechas.format((GregorianCalendar) val));
                } else {
                    setText("");
                }
            }
        });

        tablaProfesores.setDefaultEditor(GregorianCalendar.class, new DateCellEditor());

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

        tablaProfesores.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int pos = tablaProfesores.getSelectedRow();
                    Profesor p = null;
                    if (pos != -1) {
                        pos = tablaProfesores.convertRowIndexToModel(pos);
                        p = modeloProfesores.getElemento(pos);
                        setProfesorSeleccionado(p != null);
                    } else {
                        setProfesorSeleccionado(false);
                    }
                    firePropertyChange("profesorAsignado", null, p);
                }
            }
        });

        tablaAlumnos.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int pos = tablaAlumnos.getSelectedRow();
                    if (pos != -1) {
                        pos = tablaAlumnos.convertRowIndexToModel(pos);
                        Alumno objeto = modeloAlumnos.getElemento(pos);
                        setAlumnoSeleccionado(objeto != null);
                    } else {
                        setAlumnoSeleccionado(false);
                    }
                }
            }
        });

        listaSeleccionados.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int pos = listaSeleccionados.getSelectedIndex();
                    if (pos != -1) {
                        setElementoListaSeleccionado(true);
                    } else {
                        setElementoListaSeleccionado(false);
                    }
                }
            }
        });

        modeloSeleccionados.addListDataListener(new ListDataListener() {

            @Override
            public void intervalAdded(ListDataEvent e) {
                seleccionCambiada();
            }

            @Override
            public void intervalRemoved(ListDataEvent e) {
                seleccionCambiada();
            }

            @Override
            public void contentsChanged(ListDataEvent e) {
                seleccionCambiada();
            }
        });

    }

    private void seleccionCambiada() {
        if (isProcesando()) {
            //Si estamos procesando no hacemos los calculos todavía
            return;
        }
        ArrayList<Profesor> profesores = getProfesoresSeleccionados();
        ArrayList<Alumno> alumnos = getAlumnosSeleccionados();
        String texto = "<html>Hay <b>" + profesores.size() + "</b> profesores y <b>" + alumnos.size() + "</b> alumnos incluidos.";
        lInfoSeleccionados.setText(texto);
    }

    public ArrayList<Profesor> getProfesoresSeleccionados() {
        ArrayList<Profesor> profesores = new ArrayList<Profesor>();
        for (Object obj : modeloSeleccionados.toArray()) {
            if (obj instanceof Profesor) {
                if (!profesores.contains((Profesor) obj)) {
                    profesores.add((Profesor) obj);
                }
            }
        }
        return profesores;
    }

    public ArrayList<Alumno> getAlumnosSeleccionados() {
        ArrayList<Alumno> alumnos = new ArrayList<Alumno>();
        for (Object obj : modeloSeleccionados.toArray()) {
            if (obj instanceof Unidad) {
                Unidad u = (Unidad) obj;
                for (Alumno a : u.getAlumnos()) {
                    if (!alumnos.contains(a)) {
                        alumnos.add(a);
                    }
                }
            } else if (obj instanceof Alumno) {
                if (!alumnos.contains((Alumno) obj)) {
                    alumnos.add((Alumno) obj);
                }
            } else if (obj instanceof String) {
                if (obj.equals(panelArbolUnidades1.getNombreElementoRaiz())) {
                    //Son todos los elementos
                    for (Alumno a : Alumno.getAlumnos()) {
                        if (!alumnos.contains(a)) {
                            alumnos.add(a);
                        }
                    }
                } else {
                    try {
                        //Si no es un curso
                        Curso c = new Curso(MaimonidesApp.getApplication().getAnoEscolar(), obj.toString());
                        for (Alumno a : c.getAlumnos()) {
                            if (!alumnos.contains(a)) {
                                alumnos.add(a);
                            }
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(PanelSelectorDestinatarios.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            }
        }
        return alumnos;
    }

    public Object getFiltroAlumno() {
        return filtroAlumno;


    }

    public void setFiltroAlumno(Object filtroAlumno) {
        this.filtroAlumno = filtroAlumno;


        if (filtroAlumno instanceof Curso) {
            tablaAlumnos.getColumnExt("Unidad").setVisible(true);


        } else if (filtroAlumno instanceof Unidad) {
            tablaAlumnos.getColumnExt("Unidad").setVisible(false);


        } else {
            tablaAlumnos.getColumnExt("Unidad").setVisible(true);


        }
        MaimonidesUtil.ejecutarTask(this, "cargarAlumnos");


    }

    @Action(block = Task.BlockingScope.ACTION)
    public Task cargarAlumnos() {
        return new CargarAlumnosTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class CargarAlumnosTask extends org.jdesktop.application.Task<Object, Void> {

        CargarAlumnosTask(org.jdesktop.application.Application app) {
            super(app);
            modeloAlumnos.vaciar();
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
                    modeloAlumnos.addDato(a);
                }
                Obj.cerrar(st, res);
            } catch (Exception ex) {
                Logger.getLogger(PanelAlumnos.class.getName()).log(Level.SEVERE, "Error cargando lista de alumnos para año: " + MaimonidesApp.getApplication().getAnoEscolar(), ex);
            }
            return null;
        }

        @Override
        protected void succeeded(Object result) {
            tablaAlumnos.packAll();
        }
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
        listaSeleccionados = new org.jdesktop.swingx.JXList();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        panelCursoGrupos = new javax.swing.JPanel();
        panelArbolUnidades1 = new com.codeko.apps.maimonides.cursos.PanelArbolUnidades();
        bAdd = new javax.swing.JButton();
        bDel = new javax.swing.JButton();
        panelAlumnos = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tablaAlumnos = new org.jdesktop.swingx.JXTable();
        bAdd1 = new javax.swing.JButton();
        bDel1 = new javax.swing.JButton();
        panelProfesores = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tablaProfesores = new org.jdesktop.swingx.JXTable();
        bAdd2 = new javax.swing.JButton();
        bDel2 = new javax.swing.JButton();
        lInfoSeleccionados = new javax.swing.JLabel();

        setName("Form"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        listaSeleccionados.setModel(modeloSeleccionados);
        listaSeleccionados.setName("listaSeleccionados"); // NOI18N
        jScrollPane1.setViewportView(listaSeleccionados);

        jTabbedPane1.setName("jTabbedPane1"); // NOI18N

        panelCursoGrupos.setName("panelCursoGrupos"); // NOI18N

        panelArbolUnidades1.setAutoOcultar(false);
        panelArbolUnidades1.setMostrarElementoRaiz(true);
        panelArbolUnidades1.setName("panelArbolUnidades1"); // NOI18N
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getResourceMap(PanelSelectorDestinatarios.class);
        panelArbolUnidades1.setNombreElementoRaiz(resourceMap.getString("panelArbolUnidades1.nombreElementoRaiz")); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getActionMap(PanelSelectorDestinatarios.class, this);
        bAdd.setAction(actionMap.get("addCursoGrupo")); // NOI18N
        bAdd.setName("bAdd"); // NOI18N

        bDel.setAction(actionMap.get("del")); // NOI18N
        bDel.setName("bDel"); // NOI18N

        javax.swing.GroupLayout panelCursoGruposLayout = new javax.swing.GroupLayout(panelCursoGrupos);
        panelCursoGrupos.setLayout(panelCursoGruposLayout);
        panelCursoGruposLayout.setHorizontalGroup(
            panelCursoGruposLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelCursoGruposLayout.createSequentialGroup()
                .addComponent(panelArbolUnidades1, javax.swing.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelCursoGruposLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bDel)
                    .addComponent(bAdd))
                .addContainerGap())
        );
        panelCursoGruposLayout.setVerticalGroup(
            panelCursoGruposLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCursoGruposLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(bAdd)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bDel)
                .addContainerGap(120, Short.MAX_VALUE))
            .addComponent(panelArbolUnidades1, javax.swing.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(resourceMap.getString("panelCursoGrupos.TabConstraints.tabTitle"), panelCursoGrupos); // NOI18N

        panelAlumnos.setName("panelAlumnos"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        tablaAlumnos.setModel(modeloAlumnos);
        tablaAlumnos.setName("tablaAlumnos"); // NOI18N
        jScrollPane2.setViewportView(tablaAlumnos);

        bAdd1.setAction(actionMap.get("addAlumno")); // NOI18N
        bAdd1.setName("bAdd1"); // NOI18N

        bDel1.setAction(actionMap.get("del")); // NOI18N
        bDel1.setName("bDel1"); // NOI18N

        javax.swing.GroupLayout panelAlumnosLayout = new javax.swing.GroupLayout(panelAlumnos);
        panelAlumnos.setLayout(panelAlumnosLayout);
        panelAlumnosLayout.setHorizontalGroup(
            panelAlumnosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelAlumnosLayout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
                .addGap(8, 8, 8)
                .addGroup(panelAlumnosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(bAdd1)
                    .addComponent(bDel1))
                .addContainerGap())
        );
        panelAlumnosLayout.setVerticalGroup(
            panelAlumnosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelAlumnosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(bAdd1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bDel1)
                .addGap(328, 328, 328))
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 402, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(resourceMap.getString("panelAlumnos.TabConstraints.tabTitle"), panelAlumnos); // NOI18N

        panelProfesores.setName("panelProfesores"); // NOI18N

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        tablaProfesores.setModel(modeloProfesores);
        tablaProfesores.setName("tablaProfesores"); // NOI18N
        jScrollPane3.setViewportView(tablaProfesores);

        bAdd2.setAction(actionMap.get("addProfesor")); // NOI18N
        bAdd2.setName("bAdd2"); // NOI18N

        bDel2.setAction(actionMap.get("del")); // NOI18N
        bDel2.setName("bDel2"); // NOI18N

        javax.swing.GroupLayout panelProfesoresLayout = new javax.swing.GroupLayout(panelProfesores);
        panelProfesores.setLayout(panelProfesoresLayout);
        panelProfesoresLayout.setHorizontalGroup(
            panelProfesoresLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelProfesoresLayout.createSequentialGroup()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelProfesoresLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bDel2)
                    .addComponent(bAdd2))
                .addContainerGap())
        );
        panelProfesoresLayout.setVerticalGroup(
            panelProfesoresLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelProfesoresLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(bAdd2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bDel2)
                .addContainerGap(120, Short.MAX_VALUE))
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(resourceMap.getString("panelProfesores.TabConstraints.tabTitle"), panelProfesores); // NOI18N

        lInfoSeleccionados.setText(resourceMap.getString("lInfoSeleccionados.text")); // NOI18N
        lInfoSeleccionados.setName("lInfoSeleccionados"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 338, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 234, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(lInfoSeleccionados, javax.swing.GroupLayout.DEFAULT_SIZE, 566, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 224, Short.MAX_VALUE))
                    .addComponent(jTabbedPane1, 0, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lInfoSeleccionados, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    @Action(enabledProperty = "elementoListaSeleccionado")
    public void del() {
        setProcesando(true);
        for (Object obj : listaSeleccionados.getSelectedValues()) {
            modeloSeleccionados.removeElement(obj);
        }
        setProcesando(false);
    }

    @Action
    public void addCursoGrupo() {
        setProcesando(true);
        for (Object obj : panelArbolUnidades1.getObjetosSeleccionados()) {
            if (!modeloSeleccionados.contains(obj)) {
                modeloSeleccionados.addElement(obj);
            }
        }
        setProcesando(false);
    }
    private boolean elementoListaSeleccionado = false;

    public boolean isElementoListaSeleccionado() {
        return elementoListaSeleccionado;
    }

    public void setElementoListaSeleccionado(boolean b) {
        boolean old = isElementoListaSeleccionado();
        this.elementoListaSeleccionado = b;
        firePropertyChange("elementoListaSeleccionado", old, isElementoListaSeleccionado());
    }

    @Action(enabledProperty = "alumnoSeleccionado")
    public void addAlumno() {
        setProcesando(true);
        for (int i : tablaAlumnos.getSelectedRows()) {
            i = tablaAlumnos.convertRowIndexToModel(i);
            Alumno al = modeloAlumnos.getElemento(i);
            if (!modeloSeleccionados.contains(al)) {
                modeloSeleccionados.addElement(al);
            }
        }
        setProcesando(false);
    }
    private boolean alumnoSeleccionado = false;

    public boolean isAlumnoSeleccionado() {
        return alumnoSeleccionado;
    }

    public void setAlumnoSeleccionado(boolean b) {
        boolean old = isAlumnoSeleccionado();
        this.alumnoSeleccionado = b;
        firePropertyChange("alumnoSeleccionado", old, isAlumnoSeleccionado());
    }

    @Action(enabledProperty = "profesorSeleccionado")
    public void addProfesor() {
        setProcesando(true);
        for (int i : tablaProfesores.getSelectedRows()) {
            i = tablaProfesores.convertRowIndexToModel(i);
            Profesor prof = modeloProfesores.getElemento(i);
            if (!modeloSeleccionados.contains(prof)) {
                modeloSeleccionados.addElement(prof);
            }
        }
        setProcesando(false);
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
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bAdd;
    private javax.swing.JButton bAdd1;
    private javax.swing.JButton bAdd2;
    private javax.swing.JButton bDel;
    private javax.swing.JButton bDel1;
    private javax.swing.JButton bDel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel lInfoSeleccionados;
    private org.jdesktop.swingx.JXList listaSeleccionados;
    private javax.swing.JPanel panelAlumnos;
    private com.codeko.apps.maimonides.cursos.PanelArbolUnidades panelArbolUnidades1;
    private javax.swing.JPanel panelCursoGrupos;
    private javax.swing.JPanel panelProfesores;
    private org.jdesktop.swingx.JXTable tablaAlumnos;
    private org.jdesktop.swingx.JXTable tablaProfesores;
    // End of variables declaration//GEN-END:variables
}
