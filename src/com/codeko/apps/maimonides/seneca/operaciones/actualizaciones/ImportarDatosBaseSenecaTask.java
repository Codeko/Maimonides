/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codeko.apps.maimonides.seneca.operaciones.actualizaciones;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.elementos.ObjetoBDConCod;
import com.codeko.apps.maimonides.importadores.ImportadorDatosGeneralesSeneca;
import com.codeko.apps.maimonides.seneca.ClienteSeneca;
import com.codeko.apps.maimonides.seneca.GestorUsuarioClaveSeneca;
import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.JCheckBox;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

public class ImportarDatosBaseSenecaTask extends org.jdesktop.application.Task<Object, Void> {

    File fichero = null;
    ClienteSeneca cli = null;
    boolean soloNuevos = false;
    int tipo = ImportadorDatosGeneralesSeneca.TODO;

    public ImportarDatosBaseSenecaTask(org.jdesktop.application.Application app, int tipo) {
        super(app);
        this.tipo = tipo;
        setUserCanCancel(false);
        String sImp = "los datos generales del año escolar.<br/>";
        if (!ImportadorDatosGeneralesSeneca.isImportar(tipo, ImportadorDatosGeneralesSeneca.TODO)) {
            sImp = ":<br/>";
            if (ImportadorDatosGeneralesSeneca.isImportar(tipo, ImportadorDatosGeneralesSeneca.ACTIVIDADES)) {
                sImp += " - Actividades.<br/>";
            }
            if (ImportadorDatosGeneralesSeneca.isImportar(tipo, ImportadorDatosGeneralesSeneca.PROFESORES)) {
                sImp += " - Profesores.<br/>";
            }
            if (ImportadorDatosGeneralesSeneca.isImportar(tipo, ImportadorDatosGeneralesSeneca.DEPENDENCIAS)) {
                sImp += " - Dependencias.<br/>";
            }
            if (ImportadorDatosGeneralesSeneca.isImportar(tipo, ImportadorDatosGeneralesSeneca.MATERIAS)) {
                sImp += " - Materias.<br/>";
            }
            if (ImportadorDatosGeneralesSeneca.isImportar(tipo, ImportadorDatosGeneralesSeneca.TRAMOS)) {
                sImp += " - Tramos horarios.<br/>";
            }
            if (ImportadorDatosGeneralesSeneca.isImportar(tipo, ImportadorDatosGeneralesSeneca.UNIDADES)) {
                sImp += " - Unidades.<br/>";
            }
            if (ImportadorDatosGeneralesSeneca.isImportar(tipo, ImportadorDatosGeneralesSeneca.CURSOS)) {
                sImp += " - Cursos.<br/>";
            }
        }
        sImp = "<html><body>Se van a importar " + sImp + "<br/></body></html>";
        String s2 = "<html><body><br/>Puede importar estos datos desde un fichero descargado de Séneca o<br/>dejar que Maimónides lo descargue automáticamente.<br/>¿Desde donde quiere importar los datos?</body></html>";
        JLabel l = new JLabel(sImp);
        JLabel l2 = new JLabel(s2);
        JCheckBox cb = new JCheckBox("Sólo importar datos nuevos. No actualizar datos existentes.");
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(l, BorderLayout.NORTH);
        panel.add(cb, BorderLayout.CENTER);
        panel.add(l2, BorderLayout.SOUTH);

        int op = JOptionPane.showOptionDialog(MaimonidesApp.getApplication().getMainFrame(), panel, "Importación de datos generales", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new String[]{"Desde Séneca", "Desde fichero", "Cancelar"}, "Desde Séneca");
        soloNuevos = cb.isSelected();
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
                    return f.isDirectory() || f.getName().toLowerCase().endsWith(".xml");
                }

                @Override
                public String getDescription() {
                    return "Archivos XML de Séneca(*.xml)";
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
    protected Object doInBackground() throws Exception {
        if (fichero == null) {
            //Entonces importamos los datos desde séneca
            setMessage("Descargando fichero de Séneca...");
            cli.addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                }
            });
            fichero = cli.getArchivoGeneradoresDeHorarios();
        }
        boolean ret = false;
        if (fichero.exists()) {
            //TODO Debería poder importarse los datos aunque estos ya existan...
            ImportadorDatosGeneralesSeneca importador = new ImportadorDatosGeneralesSeneca(MaimonidesApp.getApplication().getAnoEscolar(), fichero, this.tipo);
            importador.addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                }
            });

            ObjetoBDConCod.setSoloInsertar(soloNuevos);
            try {
                ret = importador.importarDatosGeneralesSeneca();
            } catch (Exception e) {
                throw e;
            } finally {
                ObjetoBDConCod.setSoloInsertar(false);
            }
        }
        return ret;
    }

    @Override
    protected void failed(Throwable t) {
        setMessage("Importación finalizada con errores.");
        JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), "No se han podido importar correctamente los datos:\n" + t.getLocalizedMessage(), "Error importando datos", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    protected void succeeded(Object result) {
        setMessage("Importación finalizada.");
        if (result instanceof Boolean) {
            if ((Boolean) result) {
                String extra = "";
                if (ImportadorDatosGeneralesSeneca.isImportar(tipo, ImportadorDatosGeneralesSeneca.UNIDADES) || ImportadorDatosGeneralesSeneca.isImportar(tipo, ImportadorDatosGeneralesSeneca.CURSOS)) {
                    extra = "\nRecuerde revisar las asignaciones de los códigos cortos para cursos y unidades.";
                }
                JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), "Se han importado los datos correctamente." + extra, "Importación realizada con éxito", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), "No se han podido importar correctamente los datos.\nRevise que el fichero es el correcto (o que los datos están en Séneca)\no contacte con el servicio técnico si no consigue solucionar el error.", "Error importando datos", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
