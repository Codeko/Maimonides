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
 * PanelImportacionInicial.java
 *
 * Created on 31-ago-2009, 18:14:42
 */
package com.codeko.apps.maimonides.importadores;

import com.codeko.apps.maimonides.importadores.horarios.ImportadorFicheroHorariosSeneca;
import com.codeko.apps.maimonides.IPanel;
import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesUtil;
import com.codeko.apps.maimonides.convivencia.PanelConfiguracionConvivencia;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.importadores.horarios.ImportadorHorarios;
import com.codeko.apps.maimonides.importadores.horarios.ImportadorHorariosSeneca;
import com.codeko.apps.maimonides.seneca.ClienteSeneca;
import com.codeko.apps.maimonides.seneca.GestorUsuarioClaveSeneca;
import com.codeko.apps.maimonides.seneca.operaciones.actualizaciones.ImportarDatosBaseSenecaTask;
import com.codeko.apps.maimonides.seneca.operaciones.actualizaciones.ImportarMatriculacionesTask;
import com.codeko.apps.maimonides.seneca.operaciones.calendario.TaskImportarCalendarioDesdeSeneca;
import com.codeko.apps.maimonides.seneca.operaciones.convivencia.GestorConvivenciaSeneca;
import com.codeko.util.Archivo;
import com.codeko.util.Num;
import com.codeko.util.Obj;
import com.codeko.util.Str;
import com.mysql.jdbc.PreparedStatement;
import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;

/**
 *
 * @author Codeko
 */
public class PanelImportacionInicial extends javax.swing.JPanel implements IPanel {

    /** Creates new form PanelImportacionInicial */
    public PanelImportacionInicial() {
        initComponents();

        actualizarCodigosPendientesAlumnos();
    }

    private void actualizarCodigosPendientesAlumnos() {
        //Ahora cargamos los alumnos que no tienen código
        PreparedStatement stSel = null;
        ResultSet res = null;
        try {
            int numSinCod = 0;
            int numSinCodFaltas = 0;
            stSel = (PreparedStatement) MaimonidesApp.getConexion().prepareStatement("SELECT count(*) FROM alumnos WHERE ano=? AND borrado=0 AND cod=0 ");
            stSel.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
            res = stSel.executeQuery();
            if (res.next()) {
                numSinCod = res.getInt(1);
            }
            Obj.cerrar(stSel, res);
            stSel = (PreparedStatement) MaimonidesApp.getConexion().prepareStatement("SELECT count(*) FROM alumnos WHERE ano=? AND borrado=0 AND codFaltas='' ");
            stSel.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
            res = stSel.executeQuery();
            if (res.next()) {
                numSinCodFaltas = res.getInt(1);
            }
            final String text = "(" + numSinCod + " alumnos sin código Séneca, " + numSinCodFaltas + " sin código de faltas)";
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    lInfoCodigosPendientes.setText(text);
                    updateUI();
                }
            });

        } catch (SQLException ex) {
            Logger.getLogger(PanelImportacionInicial.class.getName()).log(Level.SEVERE, null, ex);
        }
        Obj.cerrar(stSel, res);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bImportarDatosBaseSeneca = new org.jdesktop.swingx.JXHyperlink();
        bImportarEmailsProfesoresOtrosAnos = new org.jdesktop.swingx.JXHyperlink();
        bImportarTutores = new org.jdesktop.swingx.JXHyperlink();
        bImportarDatosAlumnos = new org.jdesktop.swingx.JXHyperlink();
        bDatosBaseAlumnos = new org.jdesktop.swingx.JXHyperlink();
        bImportarEmailsProfesoresOtrosAnos1 = new org.jdesktop.swingx.JXHyperlink();
        jXHyperlink1 = new org.jdesktop.swingx.JXHyperlink();
        jXHyperlink2 = new org.jdesktop.swingx.JXHyperlink();
        bImportarHorarios = new org.jdesktop.swingx.JXHyperlink();
        bImportarCalendario = new org.jdesktop.swingx.JXHyperlink();
        lInfoCodigosPendientes = new javax.swing.JLabel();

        setName("maimonides.paneles.herramientas.importacion_inicial_datos"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getActionMap(PanelImportacionInicial.class, this);
        bImportarDatosBaseSeneca.setAction(actionMap.get("importarDatosBaseSeneca")); // NOI18N
        bImportarDatosBaseSeneca.setName("bImportarDatosBaseSeneca"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getResourceMap(PanelImportacionInicial.class);
        bImportarEmailsProfesoresOtrosAnos.setText(resourceMap.getString("bImportarEmailsProfesoresOtrosAnos.text")); // NOI18N
        bImportarEmailsProfesoresOtrosAnos.setToolTipText(resourceMap.getString("bImportarEmailsProfesoresOtrosAnos.toolTipText")); // NOI18N
        bImportarEmailsProfesoresOtrosAnos.setEnabled(false);
        bImportarEmailsProfesoresOtrosAnos.setName("bImportarEmailsProfesoresOtrosAnos"); // NOI18N

        bImportarTutores.setAction(actionMap.get("importarTutores")); // NOI18N
        bImportarTutores.setName("bImportarTutores"); // NOI18N

        bImportarDatosAlumnos.setAction(actionMap.get("importarDatosAlumnos")); // NOI18N
        bImportarDatosAlumnos.setName("bImportarDatosAlumnos"); // NOI18N

        bDatosBaseAlumnos.setAction(actionMap.get("importarDatosBaseAlumnado")); // NOI18N
        bDatosBaseAlumnos.setName("bDatosBaseAlumnos"); // NOI18N

        bImportarEmailsProfesoresOtrosAnos1.setText(resourceMap.getString("bImportarEmailsProfesoresOtrosAnos1.text")); // NOI18N
        bImportarEmailsProfesoresOtrosAnos1.setToolTipText(resourceMap.getString("bImportarEmailsProfesoresOtrosAnos1.toolTipText")); // NOI18N
        bImportarEmailsProfesoresOtrosAnos1.setEnabled(false);
        bImportarEmailsProfesoresOtrosAnos1.setName("bImportarEmailsProfesoresOtrosAnos1"); // NOI18N

        jXHyperlink1.setAction(actionMap.get("importarMatriculaciones")); // NOI18N
        jXHyperlink1.setText(resourceMap.getString("jXHyperlink1.text")); // NOI18N
        jXHyperlink1.setName("jXHyperlink1"); // NOI18N

        jXHyperlink2.setAction(actionMap.get("importarConvivencia")); // NOI18N
        jXHyperlink2.setName("jXHyperlink2"); // NOI18N

        bImportarHorarios.setAction(actionMap.get("importarHorarios")); // NOI18N
        bImportarHorarios.setName("bImportarHorarios"); // NOI18N

        bImportarCalendario.setAction(actionMap.get("importarCalendarioEscolar")); // NOI18N
        bImportarCalendario.setName("bImportarCalendario"); // NOI18N

        lInfoCodigosPendientes.setText(resourceMap.getString("lInfoCodigosPendientes.text")); // NOI18N
        lInfoCodigosPendientes.setName("lInfoCodigosPendientes"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bImportarDatosBaseSeneca, javax.swing.GroupLayout.PREFERRED_SIZE, 487, Short.MAX_VALUE)
                    .addComponent(bImportarEmailsProfesoresOtrosAnos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bImportarEmailsProfesoresOtrosAnos1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bImportarTutores, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jXHyperlink1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bImportarDatosAlumnos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(bDatosBaseAlumnos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lInfoCodigosPendientes, javax.swing.GroupLayout.DEFAULT_SIZE, 234, Short.MAX_VALUE))
                    .addComponent(jXHyperlink2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bImportarHorarios, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bImportarCalendario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(bImportarDatosBaseSeneca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bImportarEmailsProfesoresOtrosAnos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bImportarTutores, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bImportarDatosAlumnos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bDatosBaseAlumnos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lInfoCodigosPendientes))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bImportarEmailsProfesoresOtrosAnos1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jXHyperlink1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jXHyperlink2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bImportarHorarios, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bImportarCalendario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(120, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task<Object, Void> importarDatosBaseSeneca() {
        return new ImportarDatosBaseSenecaTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class), ImportadorDatosGeneralesSeneca.TODO);
    }

    @Override
    public boolean puedoSusituir() {
        return true;
    }

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task<Object, Void> importarTutores() {
        return new ImportarTutoresTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class ImportarTutoresTask extends org.jdesktop.application.Task<Object, Void> {

        File fichero = null;
        ClienteSeneca cli = null;
        ImportadorArchivoTutores importador = null;

        ImportarTutoresTask(org.jdesktop.application.Application app) {
            super(app);
            int op = JOptionPane.showOptionDialog(MaimonidesApp.getApplication().getMainFrame(), "Se van a importar los datos de tutores.\nPuede importar estos datos desde un fichero descargado de Séneca o\ndejar que Maimónides lo descargue automáticamente.\n¿Desde donde quiere importar los datos?", "Importación de datos generales", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new String[]{"Desde Séneca", "Desde fichero", "Cancelar"}, "Desde Séneca");
            if (op == JOptionPane.YES_OPTION) {
                //Tenemos que pedir los datos de acceso a séneca
                if (!GestorUsuarioClaveSeneca.getGestor().pedirUsuarioClave()) {
                    cancel(false);
                } else {
                    cli = new ClienteSeneca(GestorUsuarioClaveSeneca.getGestor().getUsuario(), GestorUsuarioClaveSeneca.getGestor().getClave());
                    cli.setDebugMode(MaimonidesApp.isDebug());
                }
            } else if (op == JOptionPane.NO_OPTION) {
                //Tenemos que pedir el fichero
                JFileChooser f = new JFileChooser(MaimonidesApp.getApplication().getUltimoArchivo());
                f.setFileSelectionMode(JFileChooser.FILES_ONLY);
                f.setFileFilter(new FileFilter() {

                    @Override
                    public boolean accept(File f) {
                        return f.isDirectory() || f.getName().toLowerCase().endsWith(".csv");
                    }

                    @Override
                    public String getDescription() {
                        return "Archivos CSV de Séneca(*.csv)";
                    }
                });
                int res = f.showOpenDialog(MaimonidesApp.getMaimonidesView().getFrame());
                if (res != JFileChooser.APPROVE_OPTION) {
                    cancel(false);
                } else {
                    fichero = f.getSelectedFile();
                    MaimonidesApp.getApplication().setUltimoArchivo(fichero);
                }
            } else {
                setMessage("Importación cancelada.");
                cancel(false);
            }
        }

        @Override
        protected Object doInBackground() {
            if (fichero == null) {
                //Entonces importamos los datos desde séneca
                setMessage("Descargando fichero de Séneca...");
                cli.addPropertyChangeListener(new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                    }
                });
                fichero = cli.getArchivoTutores();
            }
            boolean ret = false;
            if (fichero.exists()) {
                importador = new ImportadorArchivoTutores(MaimonidesApp.getApplication().getAnoEscolar(), fichero);
                importador.addPropertyChangeListener(new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                    }
                });
                ret = importador.importarTutores();
            }
            return ret;
        }

        @Override
        protected void succeeded(Object result) {
            setMessage("Importación finalizada.");
            if (result instanceof Boolean) {
                if ((Boolean) result) {
                    JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), "Se han importado los datos correctamente.", "Importación realizada con éxito", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), "No se han podido importar correctamente los datos.\nRevise que el fichero es el correcto (o que los datos están en Séneca)\no contacte con el servicio técnico si no consigue solucionar el error.", "Error importando datos", JOptionPane.ERROR_MESSAGE);
                    if (importador.getErrores().size() > 0) {
                        MaimonidesUtil.mostrarVentanaListaDatos("Errores importación", importador.getErrores());
                    }
                }
            }
        }
    }

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task<Boolean, Void> importarDatosAlumnos() {
        return new ImportarDatosAlumnosTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class ImportarDatosAlumnosTask extends org.jdesktop.application.Task<Boolean, Void> {

        File fichero = null;
        ClienteSeneca cli = null;
        ImportadorListadoExtendidoAlumnos importador = null;
        ArrayList<Alumno> borrados = new ArrayList<Alumno>();
        ArrayList<Alumno> nuevos = new ArrayList<Alumno>();
        ArrayList<ArrayList<Object>> errores = new ArrayList<ArrayList<Object>>();
        int guardados = 0;
        int procesados = 0;
        String error = "";

        ImportarDatosAlumnosTask(org.jdesktop.application.Application app) {
            super(app);
            int op = JOptionPane.showOptionDialog(MaimonidesApp.getApplication().getMainFrame(), "Se van a importar los datos de alumnos.\nPuede importar estos datos desde un fichero descargado de Séneca o\ndejar que Maimónides lo descargue automáticamente.\n¿Desde donde quiere importar los datos?", "Importación de datos generales", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new String[]{"Desde Séneca", "Desde fichero", "Cancelar"}, "Desde Séneca");
            if (op == JOptionPane.YES_OPTION) {
                //Tenemos que pedir los datos de acceso a séneca
                if (!GestorUsuarioClaveSeneca.getGestor().pedirUsuarioClave()) {
                    cancel(false);
                } else {
                    cli = new ClienteSeneca(GestorUsuarioClaveSeneca.getGestor().getUsuario(), GestorUsuarioClaveSeneca.getGestor().getClave());
                    cli.setDebugMode(MaimonidesApp.isDebug());
                }
            } else if (op == JOptionPane.NO_OPTION) {
                //Tenemos que pedir el fichero
                JFileChooser f = new JFileChooser(MaimonidesApp.getApplication().getUltimoArchivo());
                f.setFileSelectionMode(JFileChooser.FILES_ONLY);
                f.setFileFilter(new FileFilter() {

                    @Override
                    public boolean accept(File f) {
                        return f.isDirectory() || f.getName().toLowerCase().endsWith(".xls");
                    }

                    @Override
                    public String getDescription() {
                        return "Archivos XLS de Séneca(*.xls)";
                    }
                });
                int res = f.showOpenDialog(MaimonidesApp.getMaimonidesView().getFrame());
                if (res != JFileChooser.APPROVE_OPTION) {
                    cancel(false);
                } else {
                    fichero = f.getSelectedFile();
                    MaimonidesApp.getApplication().setUltimoArchivo(fichero);
                }
            } else {
                setMessage("Importación cancelada.");
                cancel(false);
            }
        }

        @Override
        protected Boolean doInBackground() {
            if (fichero == null) {
                //Entonces importamos los datos desde séneca
                setMessage("Descargando fichero de Séneca...");
                cli.addPropertyChangeListener(new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                    }
                });
                fichero = cli.getArchivoDatosExtendidosAlumnado();
            }
            boolean ret = false;
            if (fichero.exists()) {
                importador = new ImportadorListadoExtendidoAlumnos(MaimonidesApp.getApplication().getAnoEscolar(), fichero);
                importador.addPropertyChangeListener(new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        String name = evt.getPropertyName();
                        if ("borrarAlumno".equals(evt.getPropertyName())) {
                            borrados.add((Alumno) evt.getNewValue());
                        } else if ("nuevoAlumno".equals(evt.getPropertyName())) {
                            nuevos.add((Alumno) evt.getNewValue());
                        } else if ("errorCurso".equals(evt.getPropertyName())) {
                            Alumno a = (Alumno) evt.getNewValue();
                            ArrayList<Object> er = new ArrayList<Object>();
                            er.add(a);
                            er.add(a.getUnidad());
                            er.add("No se ha encontrado el curso '" + evt.getOldValue() + "' se ha asignado:" + a.getObjetoCurso());
                            errores.add(er);
                        } else if ("errorUnidad".equals(evt.getPropertyName())) {
                            Alumno a = (Alumno) evt.getNewValue();
                            ArrayList<Object> er = new ArrayList<Object>();
                            er.add(a);
                            er.add(a.getUnidad());
                            er.add("No se ha encontrado la unidad '" + evt.getOldValue() + "'");
                            errores.add(er);
                        } else if ("errorGuardando".equals(evt.getPropertyName())) {
                            Alumno a = (Alumno) evt.getNewValue();
                            ArrayList<Object> er = new ArrayList<Object>();
                            er.add(a);
                            er.add(a.getUnidad());
                            er.add("No se han podidos guardar los datos del alumno.");
                            errores.add(er);
                        } else if ("guardado".equals(evt.getPropertyName())) {
                            guardados++;
                        } else if ("procesado".equals(evt.getPropertyName())) {
                            procesados++;
                        } else if ("errorGeneral".equals(evt.getPropertyName())) {
                            error = evt.getNewValue().toString();
                        }
                        firePropertyChange(name, evt.getOldValue(), evt.getNewValue());
                    }
                });
                ret = importador.importar();
            }
            return ret;
        }

        @Override
        protected void succeeded(Boolean result) {
            setMessage("Importación finalizada.");
            if (result) {
                JFrame f = new JFrame("Actualización realizada con éxito.");
                f.setAlwaysOnTop(true);
                f.setName("FRAME_INFO_IMPORTACION_DATOS_EXTRA");
                f.add(new PanelInfoImportacionAlumnos(borrados, nuevos, errores, procesados, guardados), BorderLayout.CENTER);
                f.pack();
                f.validate();
                MaimonidesApp.getApplication().show(f);
            } else {
                JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), "Error actualizando alumnos:\n" + error, "Error actualizando alumnos", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task<Object, Void> importarDatosBaseAlumnado() {
        return new ImportarDatosBaseAlumnadoTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class ImportarDatosBaseAlumnadoTask extends org.jdesktop.application.Task<Object, Void> {

        File fichero = null;
        ClienteSeneca cli = null;
        ImportadorArchivoAlumnos importador = null;
        int cont = 0;

        ImportarDatosBaseAlumnadoTask(org.jdesktop.application.Application app) {
            super(app);
            int op = JOptionPane.showOptionDialog(MaimonidesApp.getApplication().getMainFrame(), "Se van a importar los datos básicos de alumnos.\nPuede importar estos datos desde un fichero descargado de Séneca o\ndejar que Maimónides lo descargue automáticamente (puede ser un proceso lento).\n¿Desde donde quiere importar los datos?", "Importación de datos generales", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new String[]{"Desde Séneca", "Desde fichero", "Cancelar"}, "Desde Séneca");
            if (op == JOptionPane.YES_OPTION) {
                //Tenemos que pedir los datos de acceso a séneca
                if (!GestorUsuarioClaveSeneca.getGestor().pedirUsuarioClave()) {
                    cancel(false);
                } else {
                    cli = new ClienteSeneca(GestorUsuarioClaveSeneca.getGestor().getUsuario(), GestorUsuarioClaveSeneca.getGestor().getClave());
                    cli.setDebugMode(MaimonidesApp.isDebug());
                }
            } else if (op == JOptionPane.NO_OPTION) {
                //Tenemos que pedir el fichero
                JFileChooser f = new JFileChooser(MaimonidesApp.getApplication().getUltimoArchivo());
                f.setFileSelectionMode(JFileChooser.FILES_ONLY);
                f.setFileFilter(new FileFilter() {

                    @Override
                    public boolean accept(File f) {
                        return f.isDirectory() || f.getName().toLowerCase().endsWith(".zip");
                    }

                    @Override
                    public String getDescription() {
                        return "Archivos ZIP de Séneca(*.zip)";
                    }
                });
                int res = f.showOpenDialog(MaimonidesApp.getMaimonidesView().getFrame());
                if (res != JFileChooser.APPROVE_OPTION) {
                    cancel(false);
                } else {
                    fichero = f.getSelectedFile();
                    MaimonidesApp.getApplication().setUltimoArchivo(fichero);
                }
            } else {
                setMessage("Importación cancelada.");
                cancel(false);
            }
        }

        @Override
        protected Object doInBackground() {
            boolean ret = false;
            if (fichero == null) {
                //Entonces importamos los datos desde séneca
                setMessage("Actualizando códigos desde Séneca...");
                cli.addPropertyChangeListener(new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                    }
                });
                ret = true;
                ret = actualizarCodigoSeneca();
                ret = actualizarCodigoFaltas() && ret;
            } else if (fichero.exists()) {
                ArrayList<File> archivos = new ArrayList<File>();
                File tmpDir = new File(System.getProperty("java.io.tmpdir"), "faltas");
                archivos.addAll(Archivo.descomprimirZip(fichero, tmpDir, true));
                importador = new ImportadorArchivoAlumnos(MaimonidesApp.getApplication().getAnoEscolar(), archivos);
                importador.addPropertyChangeListener(new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                    }
                });
                ret = importador.actualizarCodigoSenecaAlumnos();
                setMessage("Importación finalizada.");
            }
            return ret;
        }

        protected boolean actualizarCodigoSeneca() {
            boolean ret = false;
            //Ahora tenemos que ir cargando cada alumno y enviandoselo al proceso
            PreparedStatement st = null;
            PreparedStatement stSel = null;
            ResultSet res = null;
            HashMap<String, Integer> alumnos = new HashMap<String, Integer>();
            try {
                stSel = (PreparedStatement) MaimonidesApp.getConexion().prepareStatement("SELECT numescolar FROM alumnos WHERE ano=? AND borrado=0 AND cod=0 ");
                stSel.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
                res = stSel.executeQuery();
                while (res.next()) {
                    alumnos.put(res.getString(1), null);
                }
            } catch (SQLException ex) {
                Logger.getLogger(PanelImportacionInicial.class.getName()).log(Level.SEVERE, null, ex);
            }
            Obj.cerrar(stSel, res);
            ret = cli.actualizarCodigoSenecaAlumnos(alumnos, this) == alumnos.size();
            Iterator<String> it = alumnos.keySet().iterator();
            try {
                st = (PreparedStatement) MaimonidesApp.getConexion().prepareStatement("UPDATE alumnos SET cod=? WHERE ano=? AND borrado=0 AND numescolar=?");
                st.setInt(2, MaimonidesApp.getApplication().getAnoEscolar().getId());
                while (it.hasNext()) {
                    String numEscolar = it.next();
                    Integer cod = alumnos.get(numEscolar);
                    if (Num.getInt(cod) > 0) {
                        st.setInt(1, cod);
                        st.setString(3, numEscolar);
                        st.addBatch();
                        cont++;
                    }
                }
                st.executeBatch();
            } catch (SQLException ex) {
                Logger.getLogger(PanelImportacionInicial.class.getName()).log(Level.SEVERE, null, ex);
            }
            Obj.cerrar(st);
            setMessage("Se ha actualizado el código séneca de " + cont + " alumnos.");
            return ret;
        }

        protected boolean actualizarCodigoFaltas() {
            boolean ret = false;
            //Ahora tenemos que ir cargando cada alumno y enviandoselo al proceso
            PreparedStatement st = null;
            PreparedStatement stSel = null;
            ResultSet res = null;
            HashMap<String, String> alumnos = new HashMap<String, String>();
            try {
                stSel = (PreparedStatement) MaimonidesApp.getConexion().prepareStatement("SELECT cod FROM alumnos WHERE ano=? AND cod>0 AND borrado=0 AND codFaltas='' ");
                stSel.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
                res = stSel.executeQuery();
                while (res.next()) {
                    alumnos.put(res.getString(1), null);
                }
            } catch (SQLException ex) {
                Logger.getLogger(PanelImportacionInicial.class.getName()).log(Level.SEVERE, null, ex);
            }
            Obj.cerrar(stSel, res);
            ret = cli.actualizarCodigoFaltasSenecaAlumnos(alumnos, this) == alumnos.size();
            Iterator<String> it = alumnos.keySet().iterator();
            try {
                st = (PreparedStatement) MaimonidesApp.getConexion().prepareStatement("UPDATE alumnos SET codFaltas=? WHERE ano=? AND cod=?");
                st.setInt(2, MaimonidesApp.getApplication().getAnoEscolar().getId());
                while (it.hasNext()) {
                    String cod = it.next();
                    String codFaltas = alumnos.get(cod);
                    if (!Str.noNulo(codFaltas).equals("")) {
                        st.setString(1, codFaltas);
                        st.setString(3, cod);
                        st.addBatch();
                        cont++;
                    }
                }
                st.executeBatch();
            } catch (SQLException ex) {
                Logger.getLogger(PanelImportacionInicial.class.getName()).log(Level.SEVERE, null, ex);
            }
            Obj.cerrar(st);
            setMessage("Se ha actualizado el código de faltas de séneca de " + cont + " alumnos.");
            return ret;
        }

        @Override
        protected void finished() {
            actualizarCodigosPendientesAlumnos();
        }

        @Override
        protected void cancelled() {
            setMessage("Cancelada recuperación de códigos Séneca.");
        }

        @Override
        protected void succeeded(Object result) {
            if (result instanceof Boolean) {
                if ((Boolean) result) {
                    JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), "Se han importado los datos correctamente.", "Importación realizada con éxito", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), "No se han podido importar correctamente los datos.\nRevise que el fichero es el correcto (o que los datos están en Séneca)\no contacte con el servicio técnico si no consigue solucionar el error.\nEn muchos casos el error es simplemente debido a un cierre de sesión\nen Séneca; pruebe a repetir el proceso.", "Error importando datos", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task<Object, Void> importarMatriculaciones() {
        return new ImportarMatriculacionesTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task<Object, Void> importarHorarios() {
        return new ImportarHorariosTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class ImportarHorariosTask extends org.jdesktop.application.Task<Object, Void> {

        File fichero = null;
        ClienteSeneca cli = null;
        ImportadorHorarios importador = null;

        ImportarHorariosTask(org.jdesktop.application.Application app) {
            super(app);
            int op = JOptionPane.showOptionDialog(MaimonidesApp.getApplication().getMainFrame(), "Se van a importar los datos de horarios.\nPuede importar estos datos desde un fichero de horarios para Séneca o\ndejar que Maimónides lo descargue automáticamente.\n¿Desde donde quiere importar los datos?", "Importación de horarios", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new String[]{"Desde Séneca", "Desde fichero", "Cancelar"}, "Desde Séneca");
            if (op == JOptionPane.YES_OPTION) {
                //Tenemos que pedir los datos de acceso a séneca
                if (!GestorUsuarioClaveSeneca.getGestor().pedirUsuarioClave()) {
                    cancel(false);
                } else {
                    cli = new ClienteSeneca(GestorUsuarioClaveSeneca.getGestor().getUsuario(), GestorUsuarioClaveSeneca.getGestor().getClave());
                    cli.setDebugMode(MaimonidesApp.isDebug());
                    cli.addPropertyChangeListener(new PropertyChangeListener() {

                        @Override
                        public void propertyChange(PropertyChangeEvent evt) {
                            firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                        }
                    });
                }
            } else if (op == JOptionPane.NO_OPTION) {
                //Tenemos que pedir el fichero
                JFileChooser f = new JFileChooser(MaimonidesApp.getApplication().getUltimoArchivo());
                f.setFileSelectionMode(JFileChooser.FILES_ONLY);
                f.setFileFilter(new FileFilter() {

                    @Override
                    public boolean accept(File f) {
                        return f.isDirectory() || f.getName().toLowerCase().endsWith(".xml");
                    }

                    @Override
                    public String getDescription() {
                        return "Archivo de horarios para Séneca(*.xml)";
                    }
                });
                int res = f.showOpenDialog(MaimonidesApp.getMaimonidesView().getFrame());
                if (res != JFileChooser.APPROVE_OPTION) {
                    cancel(false);
                } else {
                    fichero = f.getSelectedFile();
                    MaimonidesApp.getApplication().setUltimoArchivo(fichero);
                }
            } else {
                setMessage("Importación cancelada.");
                cancel(false);
            }
        }

        @Override
        protected Object doInBackground() {
            boolean ret = false;
            if (fichero != null && fichero.exists()) {
                importador = new ImportadorFicheroHorariosSeneca(MaimonidesApp.getApplication().getAnoEscolar(), fichero);
            } else if (cli != null) {
                importador = new ImportadorHorariosSeneca(MaimonidesApp.getApplication().getAnoEscolar(), cli);
            }
            if (importador != null) {
                importador.addPropertyChangeListener(new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                    }
                });
                ret = importador.importarHorarios();
            }
            return ret;
        }

        @Override
        protected void succeeded(Object result) {
            setMessage("Importación finalizada.");
            if (result instanceof Boolean) {
                if ((Boolean) result) {
                    JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), "Se han importado los datos correctamente.", "Importación realizada con éxito", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), "No se han podido importar correctamente los datos.\nRevise que el fichero es el correcto (o que los datos están en Séneca)\no contacte con el servicio técnico si no consigue solucionar el error.\nMensaje de error:\n" + importador.getMensajeError(), "Error importando datos", JOptionPane.ERROR_MESSAGE);
                }
//                if (importador.getAsignaturasNoExistentes().size() > 0) {
//                    MaimonidesUtil.mostrarVentanaListaDatos("Las siguientes asignaturas no existen en Séneca y sí en Horw", importador.getAsignaturasNoExistentes());
//                }
//                if (importador.getDependenciasNoExistentes().size() > 0) {
//                    MaimonidesUtil.mostrarVentanaListaDatos("Las siguientes dependencias (Aulas) no existen en Séneca y sí en Horw", importador.getDependenciasNoExistentes());
//                }
//                if (importador.getUnidadesNoExistentes().size() > 0) {
//                    MaimonidesUtil.mostrarVentanaListaDatos("Las siguientes unidades (Curso+Grupo) no existen en Séneca y sí en Horw", importador.getUnidadesNoExistentes());
//                }
//                if (importador.getProfesoresNoExistentes().size() > 0) {
//                    MaimonidesUtil.mostrarVentanaListaDatos("Los siguientes profesores no existen en Séneca y sí en Horw", importador.getProfesoresNoExistentes());
//                }
//                if (importador.getAsignaturasRepetidas().size() > 0) {
//                    MaimonidesUtil.mostrarVentanaListaDatos("Las siguientes asignaturas existen duplicadas (Igual nombre y curso) por lo que no se puede determinar cual es la correcta.", importador.getAsignaturasRepetidas());
//                }
//                if (importador.getDependenciasRepetidas().size() > 0) {
//                    MaimonidesUtil.mostrarVentanaListaDatos("Las siguientes dependencias existen duplicadas (Igual nombre) por lo que no se puede determinar cual es la correcta.", importador.getDependenciasRepetidas());
//                }
//                if (importador.getUnidadesRepetidas().size() > 0) {
//                    MaimonidesUtil.mostrarVentanaListaDatos("Las siguientes unidades existen duplicadas (Igual nombre y curso) por lo que no se puede determinar cual es la correcta.", importador.getUnidadesRepetidas());
//                }
//                if (importador.getProfesoresRepetidos().size() > 0) {
//                    MaimonidesUtil.mostrarVentanaListaDatos("Los siguientes profesores existen duplicados (Igual nombre y apellidos) por lo que no se puede determinar cual es la correcta.", importador.getProfesoresRepetidos());
//                }
            }
        }
    }

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task<Object, Void> importarConvivencia() {
        return new ImportarConvivenciaTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class ImportarConvivenciaTask extends org.jdesktop.application.Task<Object, Void> {

        ImportarConvivenciaTask(org.jdesktop.application.Application app) {
            super(app);
            if (!GestorUsuarioClaveSeneca.getGestor().pedirUsuarioClave()) {
                cancel(false);
            }
        }

        @Override
        protected Object doInBackground() {
            String retorno = "Ha ocurrido algún error recuperando los datos.";
            ClienteSeneca cli = new ClienteSeneca(GestorUsuarioClaveSeneca.getGestor().getUsuario(), GestorUsuarioClaveSeneca.getGestor().getClave());
            cli.setDebugMode(MaimonidesApp.isDebug());
            cli.addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                }
            });
            GestorConvivenciaSeneca gestorConv = new GestorConvivenciaSeneca(cli);
            try {
                if (gestorConv.recuperarDatosConvivenciaSeneca()) {
                    retorno = "Datos recuperados correctamente desde Séneca";
                }
            } catch (IOException ex) {
                Logger.getLogger(PanelConfiguracionConvivencia.class.getName()).log(Level.SEVERE, null, ex);
            }
            return retorno;
        }

        @Override
        protected void succeeded(Object result) {
            setMessage(result.toString());
            JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), result, "Importación terminada", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task<Integer, Void> importarCalendarioEscolar() {
        Task<Integer, Void> t = new TaskImportarCalendarioDesdeSeneca(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
        return t;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.jdesktop.swingx.JXHyperlink bDatosBaseAlumnos;
    private org.jdesktop.swingx.JXHyperlink bImportarCalendario;
    private org.jdesktop.swingx.JXHyperlink bImportarDatosAlumnos;
    private org.jdesktop.swingx.JXHyperlink bImportarDatosBaseSeneca;
    private org.jdesktop.swingx.JXHyperlink bImportarEmailsProfesoresOtrosAnos;
    private org.jdesktop.swingx.JXHyperlink bImportarEmailsProfesoresOtrosAnos1;
    private org.jdesktop.swingx.JXHyperlink bImportarHorarios;
    private org.jdesktop.swingx.JXHyperlink bImportarTutores;
    private org.jdesktop.swingx.JXHyperlink jXHyperlink1;
    private org.jdesktop.swingx.JXHyperlink jXHyperlink2;
    private javax.swing.JLabel lInfoCodigosPendientes;
    // End of variables declaration//GEN-END:variables
}
