package com.codeko.apps.maimonides;

import com.codeko.apps.maimonides.ayuda.Ayuda;
import com.codeko.apps.maimonides.elementos.AnoEscolar;
import com.codeko.apps.maimonides.importadores.GestorNuevoAnoEscolar;
import com.codeko.apps.maimonides.usr.Permisos;
import com.codeko.swing.CodekoTableModel;
import com.codeko.swing.IObjetoTabla;
import com.codeko.util.Obj;
import com.codeko.util.Str;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;

/**
 *
 * @author  Codeko
 */
public class PanelAnos extends javax.swing.JPanel implements IPanel {

    CodekoTableModel<AnoEscolar> modelo = new CodekoTableModel<AnoEscolar>(new AnoEscolar());

    public CodekoTableModel<AnoEscolar> getModelo() {
        return modelo;
    }

    public PanelAnos() {
        initComponents();
        Ayuda.addHelpKey(this, "maimonides.paneles.anos");
        tablaAnos.getColumnExt(0).setVisible(false);
        tablaAnos.getColumnExt("Año").setMaxWidth(300);
        tablaAnos.getColumnExt("Año").setMinWidth(100);
        getModelo().addTableModelListener(new TableModelListener() {

            @Override
            public void tableChanged(TableModelEvent e) {
                lTotales.setText("    " + tablaAnos.getRowCount() + " registros.");
                tablaAnos.packAll();
            }
        });
        tablaAnos.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int pos = tablaAnos.getSelectedRow();
                    if (pos != -1) {
                        pos = tablaAnos.convertRowIndexToModel(pos);
                        IObjetoTabla objeto = getModelo().getElemento(pos);
                        setObjetoSeleccionado(objeto != null);
                    } else {
                        setObjetoSeleccionado(false);
                    }
                }
            }
        });
        MaimonidesApp.getApplication().getContext().getTaskService().execute(actualizar());
        bNuevo.setVisible(false);

        //Ahora gestionamos los permisos
        //bNuevo.setEnabled(Permisos.creacion(this));
        //bNuevo.setVisible(Permisos.creacion(this));
        bNuevoSimple.setEnabled(Permisos.creacion(this));
        bNuevoSimple.setVisible(Permisos.creacion(this));

        bBorrar.setEnabled(Permisos.borrado(this));
        bBorrar.setVisible(Permisos.borrado(this));

        getModelo().setEditable(Permisos.edicion(this));

    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrollTablaAnos = new javax.swing.JScrollPane();
        tablaAnos = new org.jdesktop.swingx.JXTable();
        barraHerramientas = new javax.swing.JToolBar();
        bActualizar = new javax.swing.JButton();
        bNuevoSimple = new javax.swing.JButton();
        bNuevo = new javax.swing.JButton();
        bBorrar = new javax.swing.JButton();
        bSetAnoActivo = new javax.swing.JButton();
        lTotales = new javax.swing.JLabel();

        setName("maimonides.paneles.datos.anos_escolares"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        scrollTablaAnos.setName("scrollTablaAnos"); // NOI18N

        tablaAnos.setModel(modelo);
        tablaAnos.setColumnControlVisible(true);
        tablaAnos.setName("tablaAnos"); // NOI18N
        scrollTablaAnos.setViewportView(tablaAnos);

        add(scrollTablaAnos, java.awt.BorderLayout.CENTER);

        barraHerramientas.setRollover(true);
        barraHerramientas.setName("barraHerramientas"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getActionMap(PanelAnos.class, this);
        bActualizar.setAction(actionMap.get("actualizar")); // NOI18N
        bActualizar.setFocusable(false);
        bActualizar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bActualizar.setName("bActualizar"); // NOI18N
        bActualizar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraHerramientas.add(bActualizar);

        bNuevoSimple.setAction(actionMap.get("nuevoAnoEscolar")); // NOI18N
        bNuevoSimple.setFocusable(false);
        bNuevoSimple.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bNuevoSimple.setName("bNuevoSimple"); // NOI18N
        bNuevoSimple.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraHerramientas.add(bNuevoSimple);

        bNuevo.setAction(actionMap.get("nuevo")); // NOI18N
        bNuevo.setFocusable(false);
        bNuevo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bNuevo.setName("bNuevo"); // NOI18N
        bNuevo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraHerramientas.add(bNuevo);

        bBorrar.setAction(actionMap.get("borrar")); // NOI18N
        bBorrar.setFocusable(false);
        bBorrar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bBorrar.setName("bBorrar"); // NOI18N
        bBorrar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraHerramientas.add(bBorrar);

        bSetAnoActivo.setAction(actionMap.get("setAnoActivo")); // NOI18N
        bSetAnoActivo.setFocusable(false);
        bSetAnoActivo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bSetAnoActivo.setName("bSetAnoActivo"); // NOI18N
        bSetAnoActivo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraHerramientas.add(bSetAnoActivo);

        add(barraHerramientas, java.awt.BorderLayout.PAGE_START);

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getResourceMap(PanelAnos.class);
        lTotales.setText(resourceMap.getString("lTotales.text")); // NOI18N
        lTotales.setName("lTotales"); // NOI18N
        add(lTotales, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents

    @Action
    public Task actualizar() {
        return new ActualizarTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class ActualizarTask extends org.jdesktop.application.Task<ArrayList<AnoEscolar>, Void> {

        ActualizarTask(org.jdesktop.application.Application app) {
            super(app);
            modelo.vaciar();
            this.addPropertyChangeListener(MaimonidesApp.getApplication().getControlProgresos());
            firePropertyChange("setIniciado", null, true);
        }

        @Override
        protected ArrayList<AnoEscolar> doInBackground() {
            ArrayList<AnoEscolar> anos = new ArrayList<AnoEscolar>();
            setMessage("Cargando años escolares...");
            firePropertyChange("setMensaje", null, "Cargando años escolares...");
            Connection c = MaimonidesApp.getApplication().getConector().getConexion();
            try {
                firePropertyChange("setProgreso", null, null);
                Statement st = (Statement) c.createStatement();
                ResultSet res = st.executeQuery("SELECT * FROM anos");
                while (res.next()) {
                    AnoEscolar a = new AnoEscolar();
                    a.cargarDesdeResultSet(res);
                    anos.add(a);
                }
                Obj.cerrar(st, res);
                setMessage("Años escolares cargados correctamente.");
            } catch (SQLException ex) {
                Logger.getLogger(PanelAnos.class.getName()).log(Level.SEVERE, null, ex);
            }

            return anos;
        }

        @Override
        protected void succeeded(ArrayList<AnoEscolar> result) {
            firePropertyChange("setTerminado", null, true);
            modelo.addDatos(result);
        }
    }

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task nuevo() {
        return new NuevoTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class NuevoTask extends org.jdesktop.application.Task<Boolean, Void> {

        File directorio = null;
        GestorNuevoAnoEscolar gestor = null;

        public File getDirectorio() {
            return directorio;
        }

        public void setDirectorio(File directorio) {
            this.directorio = directorio;
            MaimonidesApp.getApplication().setUltimoArchivo(directorio);
        }

        NuevoTask(org.jdesktop.application.Application app) {
            super(app);
            firePropertyChange("setIniciado", null, true);
            //Vemos el directorio de importación
            JOptionPane.showMessageDialog(MaimonidesApp.getMaimonidesView().getFrame(), "A continuación se le pedirá el directorio donde se encuentran todos los datos a importar para el nuevo año escolar.\nConsulte la documentación para ver que archivos necesita incluir en este directorio y como obtenerlos.", "Directorio de datos", JOptionPane.INFORMATION_MESSAGE);
            JFileChooser f = new JFileChooser(MaimonidesApp.getApplication().getUltimoArchivo());
            f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int res = f.showOpenDialog(MaimonidesApp.getMaimonidesView().getFrame());
            if (res != JFileChooser.APPROVE_OPTION) {
                cancel(true);
            } else {
                setDirectorio(f.getSelectedFile());
            }
        }

        @Override
        protected Boolean doInBackground() {
            if (!isCancelled()) {
                setMessage("Creando nuevo año escolar...");

                //Y ahora iniciamos la importacion
                gestor = new GestorNuevoAnoEscolar(null);
                gestor.addPropertyChangeListener(new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        if ("setMensaje".equals(evt.getPropertyName())) {
                            setMessage(Str.noNulo(evt.getNewValue()));
                        }
                    }
                });
                try {
                    return gestor.nuevoAnoEscolar(getDirectorio());
                } catch (Exception ex) {
                    Logger.getLogger(PanelAnos.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            return false;
        }

        @Override
        protected void cancelled() {
            if (gestor != null && gestor.getAnoEscolar() != null) {
                setMessage("Limpiando datos...");
                if (gestor.getAnoEscolar().borrar()) {
                    gestor.setAnoEscolar(null);
                }
                setMessage("Cancelada creación nuevo año escolar.");
            }
        }

        @Override
        protected void succeeded(Boolean result) {
            firePropertyChange("setTerminado", null, result);
            String mensaje = "";
            if (result) {
                if (gestor.isHayErrores()) {
                    if (gestor.borrarAnoEscolar()) {
                        setMessage("Borrando año escolar...");
                        if (gestor.getAnoEscolar().borrar()) {
                            gestor.setAnoEscolar(null);
                            mensaje = ("Año escolar borrado.");
                        } else {
                            mensaje = ("Error borrando año escolar.");
                        }
                    } else {
                        modelo.addDato(gestor.getAnoEscolar());
                        mensaje = ("Nuevo año escolar creado.");
                        seleccionar(gestor.getAnoEscolar());
                    }
                }
            } else {
                if (gestor.isHayErrores()) {
                    gestor.mostrarErrores();
                }
                setMessage("Limpiando datos...");
                if (gestor.getAnoEscolar().borrar()) {
                    gestor.setAnoEscolar(null);
                }
                mensaje = ("Error creando nuevo año escolar.");
            }
            setMessage(mensaje);
            JOptionPane.showMessageDialog(MaimonidesApp.getMaimonidesView().getFrame(), mensaje);
        }
    }

    @Action(block = Task.BlockingScope.APPLICATION, enabledProperty = "objetoSeleccionado")
    public Task borrar() {
        return new BorrarTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class BorrarTask extends org.jdesktop.application.Task<Vector<AnoEscolar>, Void> {

        Vector<AnoEscolar> a = null;
        boolean borrar = false;

        BorrarTask(org.jdesktop.application.Application app) {
            super(app);
            a = getObjetosSeleccionados();

            if (a != null) {
                int op = JOptionPane.showConfirmDialog(MaimonidesApp.getApplication().getMainFrame(), "¿Esta seguro de que quiere eliminar las lineas seleccionados (" + a.size() + ")?\nSe borrarán todos los datos asociados al año escolar.", "Borrar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                borrar = op == JOptionPane.YES_OPTION;
            }
            firePropertyChange("setIniciado", null, true);
        }

        @Override
        protected Vector<AnoEscolar> doInBackground() {
            if (a != null && borrar) {
                int count = 0;
                for (AnoEscolar ae : a) {
                    setProgress(++count, 0, a.size());
                    setMessage("Borrando año escolar: " + ae.getNombre() + "...");
                    ae.borrar();
                }
                setMessage("Años escolares borrados correctamente.");
                return a;
            }
            return null;
        }

        @Override
        protected void succeeded(Vector<AnoEscolar> result) {
            if (result != null) {
                firePropertyChange("setTerminado", null, true);
                getModelo().quitarDatos(result);
            } else {
                firePropertyChange("setTerminado", null, false);
            }
        }
    }

    public AnoEscolar getObjetoSeleccionado() {
        int pos = tablaAnos.getSelectedRow();
        if (pos > -1) {
            pos = tablaAnos.convertRowIndexToModel(pos);
            return getModelo().getElemento(pos);
        }
        return null;
    }

    public Vector<AnoEscolar> getObjetosSeleccionados() {
        int[] sels = tablaAnos.getSelectedRows();
        Vector<AnoEscolar> arts = new Vector<AnoEscolar>(sels.length);
        for (int pos : sels) {
            if (pos > -1) {
                pos = tablaAnos.convertRowIndexToModel(pos);
                arts.add(getModelo().getElemento(pos));
            }
        }
        return arts;
    }

    private void seleccionar(AnoEscolar anoEscolar) {
        int pos = modelo.indexOf(anoEscolar);
        pos = tablaAnos.convertRowIndexToView(pos);
        tablaAnos.getSelectionModel().setSelectionInterval(pos, pos);
    }
    private boolean objetoSeleccionado = false;

    public boolean isObjetoSeleccionado() {
        return objetoSeleccionado;
    }

    public void setObjetoSeleccionado(boolean b) {
        boolean old = isObjetoSeleccionado();
        this.objetoSeleccionado = b;
        firePropertyChange("objetoSeleccionado", old, isObjetoSeleccionado());
    }

    @Action(enabledProperty = "objetoSeleccionado")
    public void setAnoActivo() {
        AnoEscolar ano = getObjetoSeleccionado();
        MaimonidesApp.getApplication().setAno(ano);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bActualizar;
    private javax.swing.JButton bBorrar;
    private javax.swing.JButton bNuevo;
    private javax.swing.JButton bNuevoSimple;
    private javax.swing.JButton bSetAnoActivo;
    private javax.swing.JToolBar barraHerramientas;
    private javax.swing.JLabel lTotales;
    private javax.swing.JScrollPane scrollTablaAnos;
    private org.jdesktop.swingx.JXTable tablaAnos;
    // End of variables declaration//GEN-END:variables

    @Override
    public boolean puedoSusituir() {
        return true;
    }

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task nuevoAnoEscolar() {
        return new NuevoAnoEscolarTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class NuevoAnoEscolarTask extends org.jdesktop.application.Task<AnoEscolar, Void> {

        NuevoAnoEscolarTask(org.jdesktop.application.Application app) {
            super(app);
        }

        @Override
        protected AnoEscolar doInBackground() {
            AnoEscolar ano = new AnoEscolar();
            ano.setAno(new GregorianCalendar().get(GregorianCalendar.YEAR));
            if (ano.guardar()) {
                return ano;
            }
            return null;  // return your result
        }

        @Override
        protected void succeeded(AnoEscolar result) {
            if (result != null) {
                modelo.addDato(result);
                setMessage("Año escolar creado correctamente.");
            } else {
                setMessage("Se ha producido algún error creando el año escolar.");
            }
        }
    }
}
