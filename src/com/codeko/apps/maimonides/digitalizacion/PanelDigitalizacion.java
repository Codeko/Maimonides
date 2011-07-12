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
 * PanelDigitalizacion.java
 *
 * Created on 21 de octubre de 2008, 13:14
 */
package com.codeko.apps.maimonides.digitalizacion;

import com.codeko.apps.maimonides.IPanel;
import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesUtil;
import com.codeko.swing.CodekoTableModel;
import com.codeko.util.Fechas;
import com.codeko.util.Num;
import com.codeko.util.Obj;
import com.mysql.jdbc.PreparedStatement;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileFilter;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.Timer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;

/**
 *
 * @author  Codeko
 */
public class PanelDigitalizacion extends javax.swing.JPanel implements IPanel {

    CodekoTableModel<MensajeDigitalizacion> modelo = new CodekoTableModel<MensajeDigitalizacion>(new MensajeDigitalizacion());
    JCheckBoxMenuItem miForzar = new JCheckBoxMenuItem("Forzar digitalización", false);
    boolean cargado = false;
    boolean running = false;
    JPopupMenu menuForzar = new JPopupMenu();
    Timer t = new Timer(2000, new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!isRunning()) {
                recalcularPartesEnCarpetas();
            }
        }
    });

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean isCargado() {
        return cargado;
    }

    public void setCargado(boolean cargado) {
        this.cargado = cargado;
    }
    MensajeDigitalizacion mensajeActual = null;

    public MensajeDigitalizacion getMensajeActual() {
        return mensajeActual;
    }

    public void setMensajeActual(MensajeDigitalizacion mensajeActual) {
        this.mensajeActual = mensajeActual;
        MaimonidesUtil.ejecutarTask(this, "cargarMensajeDigitalizacion");
    }

    /** Creates new form PanelDigitalizacion */
    public PanelDigitalizacion() {
        initComponents();
        menuForzar.add(miForzar);
        bDigitalizar.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    menuForzar.show((Component) e.getSource(), e.getX(), e.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    menuForzar.show((Component) e.getSource(), e.getX(), e.getY());
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });

        MaimonidesUtil.addMenuTabla(tabla, "Advertencias de digitalización");
        tabla.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
        tabla.getColumnExt(0).setCellRenderer(new DefaultTableCellRenderer() {

            org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getResourceMap(PanelEditorMensajeDigitalizacion.class);

            @Override
            public void setValue(Object obj) {
                setText(((MensajeDigitalizacion) obj).getMensaje());

                switch (((MensajeDigitalizacion) obj).getTipo()) {
                    case MensajeDigitalizacion.TIPO_ADVERTENCIA:
                        if (((MensajeDigitalizacion) obj).getTipoAdvertencia() == MensajeDigitalizacion.TIPOA_DUDA_SALTOS_ASISTENCIA) {
                            setIcon(resourceMap.getIcon("advertencia.duda.icon"));
                        } else if (((MensajeDigitalizacion) obj).getTipoAdvertencia() == MensajeDigitalizacion.TIPOA_SIN_FIRMA || ((MensajeDigitalizacion) obj).getTipoAdvertencia() == MensajeDigitalizacion.TIPOA_PARTE_INCORRECTO) {
                            setIcon(resourceMap.getIcon("advertencia.adv.icon"));
                        } else {
                            setIcon(resourceMap.getIcon("advertencia.icon"));
                        }
                        break;
                    case MensajeDigitalizacion.TIPO_ERROR:
                    case MensajeDigitalizacion.TIPO_PARTE_FALLIDO:
                        setIcon(resourceMap.getIcon("error.icon"));
                        break;
                    case MensajeDigitalizacion.TIPO_IGNORADO:
                        setIcon(resourceMap.getIcon("ignorado.icon"));
                        break;
                    case MensajeDigitalizacion.TIPO_NO_EXISTE:
                        setIcon(resourceMap.getIcon("noexiste.icon"));
                        break;
                    case MensajeDigitalizacion.TIPO_OK:
                        setIcon(resourceMap.getIcon("ok.icon"));
                        break;
                }
            }
        });
        tabla.getColumnExt(1).setCellRenderer(new DefaultTableCellRenderer() {

            @Override
            public void setValue(Object obj) {
                setText(Fechas.format(obj));
            }
        });
        tabla.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                int selRow = tabla.getSelectedRow();
                if (selRow > -1) {
                    Object obj = tabla.getValueAt(selRow, 0);
                    if (obj instanceof MensajeDigitalizacion) {
                        setMensajeActual(((MensajeDigitalizacion) obj));
                    }
                } else {
                    setMensajeActual(null);
                }
            }
        });
        pEditor.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("borrado".equals(evt.getPropertyName())) {
                    quitarMensaje((MensajeDigitalizacion) evt.getNewValue());
                }
            }
        });
        MensajeDigitalizacion.getControl().addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("borrarPorIdParte".equals(evt.getPropertyName())) {
                    int idParte = Num.getInt(evt.getNewValue());
                    ArrayList<MensajeDigitalizacion> quitar = new ArrayList<MensajeDigitalizacion>();
                    for (MensajeDigitalizacion msg : modelo.getDatos()) {
                        if (msg.getParte() != null && msg.getParte().getId() == idParte) {
                            quitar.add(msg);
                        }
                    }
                    quitarMensajes(quitar);
                }
            }
        });
        t.setRepeats(true);
        t.start();
    }

    private void quitarMensajes(ArrayList<MensajeDigitalizacion> mensajes) {
        int posFinal = 0;
        for (MensajeDigitalizacion msg : mensajes) {
            int pos = tabla.convertRowIndexToView(modelo.indexOf(msg));
            modelo.quitarDato(msg);
            if (pos >= 0 && modelo.getRowCount() > 0) {
                if (pos >= modelo.getRowCount()) {
                    posFinal = modelo.getRowCount() - 1;
                }
            }
        }
        tabla.getSelectionModel().setSelectionInterval(posFinal, posFinal);
        Object obj = tabla.getValueAt(posFinal, 0);
        if (obj instanceof MensajeDigitalizacion) {
            setMensajeActual((MensajeDigitalizacion) obj);
        }
    }

    private void quitarMensaje(MensajeDigitalizacion msg) {
        int pos = tabla.convertRowIndexToView(modelo.indexOf(msg));
        modelo.quitarDato(msg);
        if (pos >= 0 && modelo.getRowCount() > 0) {
            if (pos >= modelo.getRowCount()) {
                pos = modelo.getRowCount() - 1;
            }
            tabla.getSelectionModel().setSelectionInterval(pos, pos);
            Object obj = tabla.getValueAt(pos, 0);
            if (obj instanceof MensajeDigitalizacion) {

                setMensajeActual((MensajeDigitalizacion) obj);
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

        bDigitalizar = new javax.swing.JButton();
        bActuLista = new javax.swing.JButton();
        scrollAdvertencias = new javax.swing.JScrollPane();
        tabla = new org.jdesktop.swingx.JXTable();
        lInfoPartes = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        pEditor = new com.codeko.apps.maimonides.digitalizacion.PanelEditorMensajeDigitalizacion();

        setName("maimonides.paneles.faltas.digitalizar_partes"); // NOI18N
        addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                formAncestorAdded(evt);
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance().getContext().getActionMap(PanelDigitalizacion.class, this);
        bDigitalizar.setAction(actionMap.get("digitalizar")); // NOI18N
        bDigitalizar.setName("bDigitalizar"); // NOI18N

        bActuLista.setAction(actionMap.get("cargarMensajes")); // NOI18N
        bActuLista.setName("bActuLista"); // NOI18N

        scrollAdvertencias.setName("scrollAdvertencias"); // NOI18N

        tabla.setModel(modelo);
        tabla.setName("tabla"); // NOI18N
        scrollAdvertencias.setViewportView(tabla);

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance().getContext().getResourceMap(PanelDigitalizacion.class);
        lInfoPartes.setText(resourceMap.getString("lInfoPartes.text")); // NOI18N
        lInfoPartes.setName("lInfoPartes"); // NOI18N

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        pEditor.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("pEditor.border.title"))); // NOI18N
        pEditor.setName("pEditor"); // NOI18N
        jScrollPane1.setViewportView(pEditor);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(scrollAdvertencias, javax.swing.GroupLayout.DEFAULT_SIZE, 591, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(bActuLista)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(bDigitalizar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lInfoPartes, javax.swing.GroupLayout.DEFAULT_SIZE, 595, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(bActuLista)
                    .addComponent(bDigitalizar)
                    .addComponent(lInfoPartes))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrollAdvertencias, javax.swing.GroupLayout.DEFAULT_SIZE, 501, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 501, Short.MAX_VALUE))
                .addGap(12, 12, 12))
        );
    }// </editor-fold>//GEN-END:initComponents

private void formAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_formAncestorAdded
    if (!isCargado()) {
        MaimonidesUtil.ejecutarTask(this, "cargarMensajes");
    }
}//GEN-LAST:event_formAncestorAdded

    @Action(block = Task.BlockingScope.COMPONENT, enabledProperty = "partesEnCarpeta")
    public Task<ArrayList<MensajeDigitalizacion>, Void> digitalizar() {
        return new DigitalizarTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class DigitalizarTask extends org.jdesktop.application.Task<ArrayList<MensajeDigitalizacion>, Void> {

        Digitalizador dig = new Digitalizador(MaimonidesApp.getApplication().getConfiguracion().getCarpetaPartes(), MaimonidesApp.getApplication().getConfiguracion().getCarpetaPartesDigitalizados(), MaimonidesApp.getApplication().getConfiguracion().getCarpetaPartesFallidos());

        DigitalizarTask(org.jdesktop.application.Application app) {
            super(app);
            setUserCanCancel(false);
            setRunning(true);
        }

        @Override
        protected ArrayList<MensajeDigitalizacion> doInBackground() {
            dig.addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if ("mensajeDigitalizacion".equals(evt.getPropertyName())) {
                        modelo.addDato((MensajeDigitalizacion) evt.getNewValue());
                    } else {
                        firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                    }
                }
            });
            dig.digitalizar(miForzar.isSelected());
            recalcularPartesEnCarpetas();
            return null;
        }

        @Override
        protected void finished() {
            setPartesEnCarpeta(isPartesEnCarpeta());
            setRunning(false);
        }

        @Override
        protected void succeeded(ArrayList<MensajeDigitalizacion> result) {
            setMessage("Digitalización de partes finalizada.");
        }
    }
    private boolean partesEnCarpeta = false;

    public boolean isPartesEnCarpeta() {
        return partesEnCarpeta;
    }

    public void setPartesEnCarpeta(boolean b) {
        boolean old = isPartesEnCarpeta();
        this.partesEnCarpeta = b;
        firePropertyChange("partesEnCarpeta", null, isPartesEnCarpeta());
    }
    private boolean existenPartesSinIntervencion = false;

    public boolean isExistenPartesSinIntervencion() {
        return existenPartesSinIntervencion;
    }

    public void setExistenPartesSinIntervencion(boolean b) {
        boolean old = isExistenPartesSinIntervencion();
        this.existenPartesSinIntervencion = b;
        firePropertyChange("existenPartesSinIntervencion", old, isExistenPartesSinIntervencion());
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bActuLista;
    private javax.swing.JButton bDigitalizar;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lInfoPartes;
    private com.codeko.apps.maimonides.digitalizacion.PanelEditorMensajeDigitalizacion pEditor;
    private javax.swing.JScrollPane scrollAdvertencias;
    private org.jdesktop.swingx.JXTable tabla;
    // End of variables declaration//GEN-END:variables

    @Override
    public boolean puedoSusituir() {
        return true;
    }

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task<ArrayList<MensajeDigitalizacion>, Void> cargarMensajes() {
        return new CargarMensajesTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class CargarMensajesTask extends org.jdesktop.application.Task<ArrayList<MensajeDigitalizacion>, Void> {

        CargarMensajesTask(org.jdesktop.application.Application app) {
            super(app);
            modelo.vaciar();
            setCargado(true);
        }

        @Override
        protected ArrayList<MensajeDigitalizacion> doInBackground() {
            ArrayList<MensajeDigitalizacion> mensajes = new ArrayList<MensajeDigitalizacion>();
            //Primero cargamos los partes erroneos
            File[] erroneos = MaimonidesApp.getApplication().getConfiguracion().getCarpetaPartesFallidos().listFiles(new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    return pathname.isFile() && pathname.getName().toLowerCase().endsWith("." + ConfiguracionParte.getConfiguracion().getExtensionImagenes());
                }
            });
            for (File f : erroneos) {
                MensajeDigitalizacion m = new MensajeDigitalizacion(f);
                mensajes.add(m);
            }
            try {
                boolean haySinIntervencion = false;
                PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM partes_advertencias ");
                ResultSet res = st.executeQuery();
                while (res.next()) {
                    MensajeDigitalizacion m = new MensajeDigitalizacion();
                    m.cargarDesdeResultSet(res);
                    mensajes.add(m);
                }
                setExistenPartesSinIntervencion(haySinIntervencion);
                Obj.cerrar(st, res);
            } catch (Exception ex) {
                Logger.getLogger(PanelDigitalizacion.class.getName()).log(Level.SEVERE, null, ex);
            }
            recalcularPartesEnCarpetas();
            return mensajes;
        }

        @Override
        protected void succeeded(ArrayList<MensajeDigitalizacion> result) {
            modelo.addDatos(result);
            tabla.packAll();
        }
    }

    private void recalcularPartesEnCarpetas() {
        //Vemos tambien si hay partes en la carpeta
        File carpeta = MaimonidesApp.getApplication().getConfiguracion().getCarpetaPartes();
        File[] pendientes = carpeta.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                return pathname.isFile() && pathname.getName().toLowerCase().endsWith("." + ConfiguracionParte.getConfiguracion().getExtensionImagenes());
            }
        });
        if (pendientes != null && pendientes.length > 0) {
            setPartesEnCarpeta(true);
            lInfoPartes.setText("<html>Hay <b>" + pendientes.length + "</b> partes escaneados pendientes de digitalizar.");
        } else {
            setPartesEnCarpeta(false);
            lInfoPartes.setText("No hay partes pendientes de digitalizar en la carpeta de partes.");
        }
        lInfoPartes.setToolTipText("Carpeta:" + carpeta.getAbsolutePath());
    }

    @Action(block = Task.BlockingScope.ACTION)
    public Task<Object, Void> cargarMensajeDigitalizacion() {
        return new CargarMensajeDigitalizacionTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class CargarMensajeDigitalizacionTask extends org.jdesktop.application.Task<Object, Void> {

        CargarMensajeDigitalizacionTask(org.jdesktop.application.Application app) {
            super(app);
        }

        @Override
        protected Object doInBackground() {
            setMessage("Cargando mensaje digitalización...");
            pEditor.cargar(getMensajeActual());
            return null;
        }

        @Override
        protected void succeeded(Object result) {
            setMessage("Mensaje digitalización cargado.");
        }
    }
}
