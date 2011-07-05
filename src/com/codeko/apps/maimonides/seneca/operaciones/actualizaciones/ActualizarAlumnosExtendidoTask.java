package com.codeko.apps.maimonides.seneca.operaciones.actualizaciones;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.importadores.ImportadorListadoExtendidoAlumnos;
import com.codeko.apps.maimonides.importadores.PanelInfoImportacionAlumnos;
import com.codeko.apps.maimonides.seneca.ClienteSeneca;
import com.codeko.apps.maimonides.seneca.GestorUsuarioClaveSeneca;
import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class ActualizarAlumnosExtendidoTask extends org.jdesktop.application.Task<Boolean, Void> {

    File archivo = null;
    ImportadorListadoExtendidoAlumnos imp = null;
    ArrayList<Alumno> borrados = new ArrayList<Alumno>();
    ArrayList<Alumno> nuevos = new ArrayList<Alumno>();
    ArrayList<ArrayList<Object>> errores = new ArrayList<ArrayList<Object>>();
    int guardados = 0;
    int procesados = 0;
    String error = "";
    boolean pedirArchivo = false;

    public ActualizarAlumnosExtendidoTask(org.jdesktop.application.Application app, boolean pedirArchivo) {
        super(app);
        this.pedirArchivo = pedirArchivo;
        if (pedirArchivo) {
            pedirArchivo();
        } else {
            if (!GestorUsuarioClaveSeneca.getGestor().pedirUsuarioClave()) {
                cancel(false);
            }
        }
    }

    public void canceled() {
        if (imp != null) {
            imp.setCancelado(true);
        }
    }

    @Override
    protected Boolean doInBackground() {
        boolean ret = false;
        if (!pedirArchivo) {
            ClienteSeneca cli = new ClienteSeneca(GestorUsuarioClaveSeneca.getGestor().getUsuario(), GestorUsuarioClaveSeneca.getGestor().getClave());
            cli.setDebugMode(MaimonidesApp.isDebug());
            cli.addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                }
            });
            archivo = cli.getArchivoDatosExtendidosAlumnado();
        }
        if (archivo != null) {
            imp = new ImportadorListadoExtendidoAlumnos(MaimonidesApp.getApplication().getAnoEscolar(), archivo);
            imp.addPropertyChangeListener(new PropertyChangeListener() {

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
            ret = imp.importar();
        }
        return ret;  // return your result
    }

    @Override
    protected void succeeded(Boolean result) {
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

    private void pedirArchivo() {
        JFileChooser jfc = new JFileChooser(MaimonidesApp.getApplication().getUltimoArchivo());
        jfc.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".xls");
            }

            @Override
            public String getDescription() {
                return "Listado de alumnos de Séneca (*.xls)";
            }
        });
        jfc.setMultiSelectionEnabled(false);
        int res = jfc.showOpenDialog(MaimonidesApp.getMaimonidesView().getFrame());
        if (res == JFileChooser.APPROVE_OPTION) {
            archivo = jfc.getSelectedFile();
            MaimonidesApp.getApplication().setUltimoArchivo(jfc.getSelectedFile());
        } else {
            cancel(true);
        }
    }
}
