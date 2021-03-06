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
 * PanelDebugEnvioFicheros.java
 *
 * Created on 12-feb-2010, 14:18:17
 */
package com.codeko.apps.maimonides.seneca.operaciones.envioFicherosFaltas;

import com.codeko.apps.maimonides.IPanel;
import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesUtil;
import com.codeko.apps.maimonides.seneca.ClienteSeneca;
import com.codeko.apps.maimonides.seneca.GeneradorFicherosSeneca;
import com.codeko.apps.maimonides.seneca.GestorUsuarioClaveSeneca;
import com.codeko.swing.CodekoAutoTableModel;
import com.codeko.util.Obj;
import com.codeko.util.Str;
import com.mysql.jdbc.PreparedStatement;
import java.beans.Beans;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;

/**
 *
 * @author codeko
 */
public class PanelDebugEnvioFicheros extends javax.swing.JPanel implements IPanel {

    boolean cargado = false;
    GeneradorFicherosSeneca gen = new GeneradorFicherosSeneca();
    DefaultListModel modelo = new DefaultListModel();
    CodekoAutoTableModel<AlumnoEnvioErroneo> modeloTabla = new CodekoAutoTableModel<AlumnoEnvioErroneo>(AlumnoEnvioErroneo.class) {

        @Override
        public void elementoModificado(AlumnoEnvioErroneo elemento, int col, Object valor) {
            elemento.guardar();
        }
    };

    /** Creates new form PanelDebugEnvioFicheros */
    public PanelDebugEnvioFicheros() {
        initComponents();
        lista.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent lse) {
                setElementoSeleccionado(lista.getSelectedIndex() > -1);
            }
        });
        MaimonidesUtil.addMenuTabla(tabla, "Alumnos con problemas de envío de faltas");
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tabla = new org.jdesktop.swingx.JXTable();
        Actualizar = new javax.swing.JButton();
        bBorrarAlumnoConFallo = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lista = new javax.swing.JList();
        bProcesar = new javax.swing.JButton();
        bBorrar = new javax.swing.JButton();
        bActualizar = new javax.swing.JButton();

        setName("maimonides.paneles.herramientas.debug_faltas"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getResourceMap(PanelDebugEnvioFicheros.class);
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel1.border.title"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        tabla.setModel(modeloTabla);
        tabla.setName("tabla"); // NOI18N
        jScrollPane2.setViewportView(tabla);

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getActionMap(PanelDebugEnvioFicheros.class, this);
        Actualizar.setAction(actionMap.get("actualizar")); // NOI18N
        Actualizar.setName("Actualizar"); // NOI18N

        bBorrarAlumnoConFallo.setAction(actionMap.get("borrarAlumnoConFallo")); // NOI18N
        bBorrarAlumnoConFallo.setName("bBorrarAlumnoConFallo"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 526, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addComponent(Actualizar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bBorrarAlumnoConFallo)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Actualizar)
                    .addComponent(bBorrarAlumnoConFallo))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 32, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel2.border.title"))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                jPanel2AncestorAdded(evt);
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        lista.setModel(modelo);
        lista.setName("lista"); // NOI18N
        jScrollPane1.setViewportView(lista);

        bProcesar.setAction(actionMap.get("procesarError")); // NOI18N
        bProcesar.setName("bProcesar"); // NOI18N

        bBorrar.setAction(actionMap.get("borrarEnvio")); // NOI18N
        bBorrar.setName("bBorrar"); // NOI18N

        bActualizar.setAction(actionMap.get("actualizarFicheros")); // NOI18N
        bActualizar.setName("bActualizar"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 526, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                        .addComponent(bActualizar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bProcesar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bBorrar)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bProcesar)
                    .addComponent(bBorrar)
                    .addComponent(bActualizar))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 538, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jPanel2AncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_jPanel2AncestorAdded
        if (!cargado) {
            cargado = true;
            MaimonidesUtil.ejecutarTask(this, "actualizarFicheros");
        }
    }//GEN-LAST:event_jPanel2AncestorAdded
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
    private javax.swing.JButton Actualizar;
    private javax.swing.JButton bActualizar;
    private javax.swing.JButton bBorrar;
    private javax.swing.JButton bBorrarAlumnoConFallo;
    private javax.swing.JButton bProcesar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JList lista;
    private org.jdesktop.swingx.JXTable tabla;
    // End of variables declaration//GEN-END:variables

    @Override
    public boolean puedoSusituir() {
        return true;
    }

    @Action(block = Task.BlockingScope.APPLICATION, enabledProperty = "elementoSeleccionado")
    public Task procesarError() {
        return new ProcesarErrorTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class ProcesarErrorTask extends org.jdesktop.application.Task<Object, Void> {

        EnvioErroneo envio = null;
        GeneradorFicherosSeneca gen = new GeneradorFicherosSeneca();
        ClienteSeneca cli = null;

        ProcesarErrorTask(org.jdesktop.application.Application app) {
            super(app);
            modeloTabla.vaciar();
            Object val = lista.getSelectedValue();
            if (val instanceof EnvioErroneo) {
                envio = (EnvioErroneo) val;
                pedirUsuarioClave();
                gen.addPropertyChangeListener(new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                    }
                });
            }
        }

        protected void pedirUsuarioClave() {
            if (!GestorUsuarioClaveSeneca.getGestor().pedirUsuarioClave()) {
                cancel(false);
            } else {
                cli = new ClienteSeneca(GestorUsuarioClaveSeneca.getGestor().getUsuario(), GestorUsuarioClaveSeneca.getGestor().getClave());
                cli.setDebugMode(MaimonidesApp.isDebug());
                cli.addPropertyChangeListener(new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                        if ("error".equals(evt.getPropertyName())) {
                            gen.getErrores().add(Str.noNulo(evt.getNewValue()));
                        }
                    }
                });
                gen.setClienteSeneca(cli);
            }
        }

        @Override
        protected Object doInBackground() {
            if (envio != null) {
                GestorEnvioFaltas gestor = new GestorEnvioFaltas(cli);
                gestor.limpiarTodosLosResultadosEnvioDeFicheros();
                gen.setDebug(true);
                gen.enviarFicheroGeneralEnPorciones(envio);
                for (File f : envio.getErroneos()) {
                    //Ahora hay que mostrar los datos de cada envío erroneo
                    System.out.println("Envío erroneo:" + f.getName());
                }
                for (File f : envio.getFallidos()) {
                    System.out.println("Envío fallido:" + f.getName());
                    f.delete();
                }
                //Insertamos los errores de envio
                for (AlumnoEnvioErroneo aee : envio.getAlumnosFallidos()) {
                    aee.guardar();
                    modeloTabla.addDato(aee);
                }
            }
            return null;
        }

        @Override
        protected void succeeded(Object result) {
            if (envio != null) {
                JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), "Se han encontrado problemas enviando las faltas de " + envio.getAlumnosFallidos().size() + " alumnos", "Problemas detectados", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    @Action(enabledProperty = "elementoSeleccionado")
    public void borrarEnvio() {
        Object[] vals = lista.getSelectedValues();
        for (Object val : vals) {
            if (val instanceof EnvioErroneo) {
                EnvioErroneo e = (EnvioErroneo) val;
                e.getFicheroEnvio().delete();
                e.getFicheroPropiedades().delete();
                modelo.removeElement(val);
            }
        }
    }

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task actualizar() {
        return new ActualizarTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class ActualizarTask extends org.jdesktop.application.Task<Vector<AlumnoEnvioErroneo>, Void> {

        ActualizarTask(org.jdesktop.application.Application app) {
            super(app);
            modeloTabla.vaciar();
        }

        @Override
        protected Vector<AlumnoEnvioErroneo> doInBackground() {
            Vector<AlumnoEnvioErroneo> ret = new Vector<AlumnoEnvioErroneo>();
            if (!Beans.isDesignTime()) {
                try {
                    PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM alumnos_problemas_envio WHERE tipo=0 ");
                    ResultSet res = st.executeQuery();
                    while (res.next()) {
                        AlumnoEnvioErroneo aee = new AlumnoEnvioErroneo();
                        try {
                            aee.cargarDesdeResultSet(res);
                            ret.add(aee);
                        } catch (SQLException ex) {
                            Logger.getLogger(PanelDebugEnvioFicheros.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (Exception ex) {
                            Logger.getLogger(PanelDebugEnvioFicheros.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    Obj.cerrar(st, res);
                } catch (SQLException ex) {
                    Logger.getLogger(PanelDebugEnvioFicheros.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            return ret;
        }

        @Override
        protected void succeeded(Vector<AlumnoEnvioErroneo> result) {
            modeloTabla.addDatos(result);
        }
    }

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task borrarAlumnoConFallo() {
        return new BorrarAlumnoConFalloTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class BorrarAlumnoConFalloTask extends org.jdesktop.application.Task<Vector<AlumnoEnvioErroneo>, Void> {

        Vector<AlumnoEnvioErroneo> alumnos = new Vector<AlumnoEnvioErroneo>();

        BorrarAlumnoConFalloTask(org.jdesktop.application.Application app) {
            super(app);
            int[] filas = tabla.getSelectedRows();
            if (filas.length > 0) {
                int op = JOptionPane.showConfirmDialog(MaimonidesApp.getApplication().getMainFrame(), "¿Esta seguro de que desea borrar los registros seleccionados (" + filas.length + ")?", "Borrar", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (op == JOptionPane.YES_OPTION) {
                    for (int i : filas) {
                        int row = tabla.convertRowIndexToModel(i);
                        AlumnoEnvioErroneo p = modeloTabla.getElemento(row);
                        alumnos.add(p);
                    }
                    modeloTabla.quitarDatos(alumnos);
                }
            }
        }

        @Override
        protected Vector<AlumnoEnvioErroneo> doInBackground() {
            setMessage("Borrando registros...");
            Vector<AlumnoEnvioErroneo> noBorrados = new Vector<AlumnoEnvioErroneo>();
            if (alumnos != null) {
                for (AlumnoEnvioErroneo p : alumnos) {
                    if (!p.borrar()) {
                        noBorrados.add(p);
                    }
                }
            }
            return noBorrados;
        }

        @Override
        protected void succeeded(Vector<AlumnoEnvioErroneo> result) {
            if (result != null && result.size() > 0) {
                JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), "Algunos registro de alumno no se han podido borrar.", "Borrar registros", JOptionPane.WARNING_MESSAGE);
                modeloTabla.addDatos(result);
            } else {
                setMessage("Registro(s) de alumno(s) borrado(s) correctamente.");
            }
        }
    }

    @Action(block = Task.BlockingScope.COMPONENT)
    public Task actualizarFicheros() {
        return new ActualizarFicherosTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class ActualizarFicherosTask extends org.jdesktop.application.Task<Vector<EnvioErroneo>, Void> {

        ActualizarFicherosTask(org.jdesktop.application.Application app) {
            super(app);
            modelo.removeAllElements();
        }

        @Override
        protected Vector<EnvioErroneo> doInBackground() {
            Vector<EnvioErroneo> ret = new Vector<EnvioErroneo>();
            File[] props = gen.getFicherosPropiedadesFallidos();
            for (File f : props) {
                try {
                    EnvioErroneo e = new EnvioErroneo(f);
                    ret.addElement(e);
                } catch (IOException ex) {
                    Logger.getLogger(PanelDebugEnvioFicheros.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            return ret;
        }

        @Override
        protected void succeeded(Vector<EnvioErroneo> result) {
            for (EnvioErroneo e : result) {
                modelo.addElement(e);
            }
            setMessage("Ficheros cargados correctamente.");
        }
    }
}
