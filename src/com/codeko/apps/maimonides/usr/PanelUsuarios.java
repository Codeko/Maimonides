/*
 * PanelUsuarios.java
 *
 * Created on 26-mar-2010, 12:11:37
 */
package com.codeko.apps.maimonides.usr;

import com.codeko.apps.maimonides.DateCellEditor;
import com.codeko.apps.maimonides.ICargable;
import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesUtil;
import com.codeko.apps.maimonides.cursos.PanelGrupos;
import com.codeko.apps.maimonides.elementos.Profesor;
import com.codeko.swing.CodekoAutoTableModel;
import com.codeko.util.Fechas;
import com.codeko.util.Num;
import com.codeko.util.Obj;
import com.mysql.jdbc.PreparedStatement;
import java.awt.Component;
import java.beans.Beans;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 *
 * @author codeko
 */
public class PanelUsuarios extends javax.swing.JPanel implements ICargable {
    //TODO Queda pendiente revisar como se recuperan usuarios borrados o simplemente borrarlos

    boolean cargado = false;
    CodekoAutoTableModel<Usuario> modelo = new CodekoAutoTableModel<Usuario>(Usuario.class) {

        @Override
        public void elementoModificado(Usuario elemento, int col, Object valor) {
            //Tenemos que verificar si existe ya el nombre de usuario
            boolean existe = true;
            String nombre = elemento.getNombre();
            int cont = 0;
            boolean cambiado = false;
            while (existe) {
                cont++;
                existe = false;
                for (Usuario u : modelo.getDatos()) {
                    if (elemento != u && u.getNombre().equals(elemento.getNombre())) {
                        existe = true;

                        break;
                    }
                }
                if (existe) {
                    cambiado = true;
                    elemento.setNombre(nombre + cont);
                }
            }

            if (cambiado) {
                JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), "Ya existe un usuario con ese nombre.\nSe le añadirá automáticamente un contador para diferenciarlo.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            elemento.guardar();
        }
    };

    /** Creates new form PanelUsuarios */
    public PanelUsuarios() {
        initComponents();
        tabla.setDefaultEditor(GregorianCalendar.class, new DateCellEditor());
        tabla.setDefaultRenderer(GregorianCalendar.class, new DefaultTableCellRenderer() {

            @Override
            public void setValue(Object val) {
                if (val instanceof GregorianCalendar) {
                    setText(Fechas.format((GregorianCalendar) val));
                } else {
                    setText("");
                }
            }
        });
        TableColumnExt colRoles = tabla.getColumnExt("Roles");
        colRoles.setCellEditor(new EditorRoles());
        colRoles.setCellRenderer(new DefaultTableCellRenderer() {

            @Override
            public void setValue(Object val) {
                setText(Rol.getTextoRoles(Num.getInt(val)));
            }
        });
        TableColumnExt colTutor = tabla.getColumnExt("Profesor");
        TableColumnExt colClave = tabla.getColumnExt("Clave");
        colClave.setCellRenderer(new DefaultTableCellRenderer() {

            @Override
            public void setValue(Object val) {
                setText("*********");
            }
        });
        colClave.setCellEditor(new DefaultCellEditor(new JPasswordField()));
        JComboBox comboTutores = new JComboBox(Profesor.getProfesores().toArray());
        comboTutores.insertItemAt("Sin profesor asignado", 0);
        DefaultCellEditor dceTutor = new DefaultCellEditor(comboTutores) {

            @Override
            public Object getCellEditorValue() {
                Object obj = super.getCellEditorValue();
                if (obj instanceof Profesor) {
                    return ((Profesor) obj);
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
    }

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task borrar() {
        return new BorrarTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task nuevo() {
        return new NuevoTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task importarUsuarios() {
        return new ImportarUsuariosTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task asociarProfesores() {
        return new AsociarProfesoresTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class AsociarProfesoresTask extends org.jdesktop.application.Task<Integer, Void> {

        AsociarProfesoresTask(org.jdesktop.application.Application app) {
            super(app);
            int op = JOptionPane.showConfirmDialog(MaimonidesApp.getMaimonidesView().getFrame(), "Se van a asociar los usuarios con los profesores basándose en los datos del año anterior.\n¿Continuar?", "Importación de usuarios", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (op != JOptionPane.OK_OPTION) {
                cancel(false);
            }
        }

        @Override
        protected Integer doInBackground() {
            setMessage("Procensado usuarios...");
            int cont = 0;
            PreparedStatement st = null;
            ResultSet res = null;
            int asoc = 0;
            try {
                st = (PreparedStatement) MaimonidesApp.getConexion().prepareStatement("SELECT p.cod FROM profesores AS p JOIN usuarios_profesores AS up ON up.profesor_id=p.id AND p.ano=up.ano WHERE p.ano<>? AND up.usuario_id=? ORDER BY p.ano DESC LIMIT 0,1");
                st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
                for (Usuario u : modelo.getDatos()) {
                    cont++;
                    setProgress(cont, 0, modelo.getRowCount());
                    setMessage("Verificando " + u + "...");
                    if (u.getProfesor() == null) {
                        //Vemos la id del profesor del año anterior
                        st.setInt(2, u.getId());
                        res = st.executeQuery();
                        if (res.next()) {
                            Profesor p = Profesor.getProfesorPorCodigo(res.getInt(1), MaimonidesApp.getApplication().getAnoEscolar());
                            if (p != null) {
                                u.setProfesor(p);
                                if (u.guardar()) {
                                    asoc++;
                                }
                            }
                        }
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(PanelUsuarios.class.getName()).log(Level.SEVERE, null, ex);
            }

            return asoc;
        }

        @Override
        protected void succeeded(Integer result) {
            setMessage("Se han asociado " + result + " profesores.");
        }
    }

    private class ImportarUsuariosTask extends org.jdesktop.application.Task<ArrayList<Usuario>, Void> {

        ImportarUsuariosTask(org.jdesktop.application.Application app) {
            super(app);
            int op = JOptionPane.showConfirmDialog(MaimonidesApp.getMaimonidesView().getFrame(), "Esta operación creará un usuario para cada profesor.\n¿Continuar?", "Importación de usuarios", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (op != JOptionPane.OK_OPTION) {
                cancel(false);
            }
        }

        @Override
        protected ArrayList<Usuario> doInBackground() {
            ArrayList<Usuario> usuarios = new ArrayList<Usuario>();
            setMessage("Cargando profesores...");
            ArrayList<Profesor> profesores = Profesor.getProfesores();
            int i = 0;
            for (Profesor p : profesores) {
                i++;
                setProgress(i, 0, profesores.size());
                setMessage("Procesando " + p + "...");
                //Ahora vemos si ya existe un usuario con ese profesor asignado
                boolean existe = false;
                for (Usuario usr : modelo.getDatos()) {
                    if (p.equals(usr.getProfesor())) {
                        existe = true;
                        break;
                    }
                }
                if (existe) {
                    setMessage("Ya existe usuario para el profesor...");
                } else {
                    setMessage("Creando usuario para el profesor...");
                    Usuario u = new Usuario();
                    u.setRoles(Rol.ROL_PROFESOR);
                    u.setClave("");
                    u.setFechaAlta(new GregorianCalendar());
                    u.setProfesor(p);
                    String nombre = (p.getNombre().substring(0, 1) + p.getApellido1().substring(0, 1) + p.getApellido2()).toLowerCase().replaceAll("á", "a").replaceAll("é", "e").replaceAll("í", "i").replaceAll("ó", "o").replaceAll("ú", "u");
                    u.setNombre(nombre);
                    //Vemos si existe el nombre
                    existe = true;
                    int cont = 0;
                    while (existe) {
                        cont++;
                        existe = false;
                        //Revisamos en los usuarios de la tabla
                        for (Usuario usr : modelo.getDatos()) {
                            if (usr.getNombre().equals(u.getNombre())) {
                                existe = true;
                                break;
                            }
                        }
                        //Si no existe revisamos en los usuarios que estamos creando
                        if (!existe) {
                            for (Usuario usr : usuarios) {
                                if (usr.getNombre().equals(u.getNombre())) {
                                    existe = true;
                                    break;
                                }
                            }
                        }
                        if (existe) {
                            u.setNombre(nombre + cont);
                        }
                    }
                    if (u.guardar()) {
                        usuarios.add(u);
                    }
                }
            }
            return usuarios;
        }

        @Override
        protected void succeeded(ArrayList<Usuario> result) {
            modelo.addDatos(result);
            setMessage(result.size() + " usuarios importados correctamente.");
        }
    }

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task actualizar() {
        return new ActualizarTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    @Override
    public void cargar() {
        if (!isCargado()) {
            MaimonidesUtil.ejecutarTask(this, "actualizar");
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

    private class ActualizarTask extends org.jdesktop.application.Task<ArrayList<Usuario>, Void> {

        ActualizarTask(org.jdesktop.application.Application app) {
            super(app);
            vaciar();
            setCargado(true);
        }

        @Override
        protected ArrayList<Usuario> doInBackground() {
            ArrayList<Usuario> ret = new ArrayList<Usuario>();
            if (!Beans.isDesignTime()) {
                try {
                    PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM usuarios WHERE fbaja IS NULL ORDER BY nombre");
                    ResultSet res = st.executeQuery();
                    while (res.next()) {
                        Usuario p = new Usuario();
                        try {
                            p.cargarDesdeResultSet(res);
                            ret.add(p);
                        } catch (SQLException ex) {
                            Logger.getLogger(PanelUsuarios.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (Exception ex) {
                            Logger.getLogger(PanelUsuarios.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    Obj.cerrar(st, res);
                } catch (SQLException ex) {
                    Logger.getLogger(PanelUsuarios.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            return ret;  // return your result
        }

        @Override
        protected void succeeded(ArrayList<Usuario> result) {
            modelo.addDatos(result);
            tabla.packAll();
        }
    }

    private class NuevoTask extends org.jdesktop.application.Task<Usuario, Void> {

        NuevoTask(org.jdesktop.application.Application app) {
            super(app);
        }

        @Override
        protected Usuario doInBackground() {
            Usuario p = new Usuario();
            p.setNombre("usuario");
            //Vemos si existe el nombre
            boolean existe = true;
            int cont = 0;
            while (existe) {
                cont++;
                existe = false;
                for (Usuario u : modelo.getDatos()) {
                    if (u.getNombre().equals(p.getNombre())) {
                        existe = true;
                        break;
                    }
                }
                if (existe) {
                    p.setNombre("usuario" + cont);
                }
            }
            p.guardar();
            return p;
        }

        @Override
        protected void succeeded(Usuario result) {
            modelo.addDato(result);
            int row = tabla.convertRowIndexToView(modelo.indexOf(result));
            tabla.scrollRowToVisible(row);
            tabla.editCellAt(row, 0);
            tabla.requestFocus();
            TableCellEditor tce = tabla.getCellEditor(row, 0);
            tce.shouldSelectCell(new ListSelectionEvent(tabla, row, row, false));
        }
    }

    private class BorrarTask extends org.jdesktop.application.Task<ArrayList<Usuario>, Void> {

        ArrayList<Usuario> profs = new ArrayList<Usuario>();

        BorrarTask(org.jdesktop.application.Application app) {
            super(app);
            int[] filas = tabla.getSelectedRows();
            int op = JOptionPane.showConfirmDialog(MaimonidesApp.getApplication().getMainFrame(), "¿Esta seguro de que desea borrar los usuarios seleccionados (" + filas.length + ")?", "Borrar", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (op == JOptionPane.YES_OPTION) {
                for (int i : filas) {
                    int row = tabla.convertRowIndexToModel(i);
                    Usuario u = modelo.getElemento(row);
                    if (u.getId() == 1) {
                        JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), "El usuario administrador no puede ser eliminado", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        profs.add(u);
                    }
                }
                modelo.quitarDatos(profs);
            }
        }

        @Override
        protected ArrayList<Usuario> doInBackground() {
            firePropertyChange("message", null, "Borrando usuarios...");
            ArrayList<Usuario> noBorrados = new ArrayList<Usuario>();
            if (profs != null) {
                for (Usuario p : profs) {
                    if (!p.borrar()) {
                        noBorrados.add(p);
                    }
                }
            }
            return noBorrados;
        }

        @Override
        protected void succeeded(ArrayList<Usuario> result) {
            if (result != null && result.size() > 0) {
                JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), "Algunos usuarios no se han podido borrar.", "Borrar profesores", JOptionPane.WARNING_MESSAGE);
                modelo.addDatos(result);
            }
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

        jToolBar1 = new javax.swing.JToolBar();
        bActualizar = new javax.swing.JButton();
        bNuevo = new javax.swing.JButton();
        bBorrar = new javax.swing.JButton();
        bImportarUsuarios = new javax.swing.JButton();
        bAsociar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
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
        setLayout(new java.awt.BorderLayout());

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        jToolBar1.setName("jToolBar1"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getActionMap(PanelUsuarios.class, this);
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

        bImportarUsuarios.setAction(actionMap.get("importarUsuarios")); // NOI18N
        bImportarUsuarios.setFocusable(false);
        bImportarUsuarios.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bImportarUsuarios.setName("bImportarUsuarios"); // NOI18N
        bImportarUsuarios.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(bImportarUsuarios);

        bAsociar.setAction(actionMap.get("asociarProfesores")); // NOI18N
        bAsociar.setFocusable(false);
        bAsociar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bAsociar.setName("bAsociar"); // NOI18N
        bAsociar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(bAsociar);

        add(jToolBar1, java.awt.BorderLayout.PAGE_START);

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        tabla.setModel(modelo);
        tabla.setName("tabla"); // NOI18N
        jScrollPane1.setViewportView(tabla);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void formAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_formAncestorAdded
        cargar();
    }//GEN-LAST:event_formAncestorAdded
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bActualizar;
    private javax.swing.JButton bAsociar;
    private javax.swing.JButton bBorrar;
    private javax.swing.JButton bImportarUsuarios;
    private javax.swing.JButton bNuevo;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar jToolBar1;
    private org.jdesktop.swingx.JXTable tabla;
    // End of variables declaration//GEN-END:variables
}
