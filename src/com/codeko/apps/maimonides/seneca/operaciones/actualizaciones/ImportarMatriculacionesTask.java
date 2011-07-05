package com.codeko.apps.maimonides.seneca.operaciones.actualizaciones;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.importadores.ImportadorArchivosMatriculas;
import com.codeko.apps.maimonides.seneca.ClienteSeneca;
import com.codeko.apps.maimonides.seneca.GestorUsuarioClaveSeneca;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

public class ImportarMatriculacionesTask extends org.jdesktop.application.Task<Object, Void> {

    ArrayList<File> ficheros = null;
    ClienteSeneca cli = null;
    ImportadorArchivosMatriculas importador = null;

    public ImportarMatriculacionesTask(org.jdesktop.application.Application app) {
        super(app);
        int op = JOptionPane.showOptionDialog(MaimonidesApp.getApplication().getMainFrame(), "Se van a importar los datos de matriculación.\nPuede importar estos datos desde un fichero descargado de Séneca o\ndejar que Maimónides lo descargue automáticamente.\n¿Desde donde quiere importar los datos?", "Importación de datos generales", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new String[]{"Desde Séneca", "Desde fichero/s", "Cancelar"}, "Desde Séneca");
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
            f.setMultiSelectionEnabled(true);
            f.setFileFilter(new FileFilter() {

                @Override
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().toLowerCase().endsWith(".xls");
                }

                @Override
                public String getDescription() {
                    return "Archivos EXCEL de Séneca(*.xls)";
                }
            });
            int res = f.showOpenDialog(MaimonidesApp.getMaimonidesView().getFrame());
            if (res != JFileChooser.APPROVE_OPTION) {
                cancel(false);
            } else {
                File[] fs = f.getSelectedFiles();
                if (fs.length > 0) {
                    MaimonidesApp.getApplication().setUltimoArchivo(fs[0]);
                    ficheros = new ArrayList<File>();
                    ficheros.addAll(Arrays.asList(fs));
                }
            }
        } else {
            setMessage("Importación cancelada.");
            cancel(false);
        }
    }

    @Override
    protected Object doInBackground() {
        if (ficheros == null) {
            //Entonces importamos los datos desde séneca
            setMessage("Descargando fichero de Séneca...");
            cli.addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                }
            });
            ficheros = cli.getArchivosMatriculasAlumnado();
        }
        boolean ret = false;
        if (ficheros != null && ficheros.size() > 0) {
            importador = new ImportadorArchivosMatriculas(MaimonidesApp.getApplication().getAnoEscolar(), ficheros);
            importador.addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                }
            });
            setMessage("Importando datos...");
            ret = importador.importarMatriculasExcel(this);
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
            }
        }
    }
}
