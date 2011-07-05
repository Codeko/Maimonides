package com.codeko.apps.maimonides.seneca.operaciones.envioFicherosFaltas;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.seneca.ClienteSeneca;
import com.codeko.apps.maimonides.seneca.GeneradorFicherosSeneca;
import com.codeko.apps.maimonides.seneca.GestorUsuarioClaveSeneca;
import com.codeko.apps.maimonides.seneca.PanelExportacionSeneca;
import com.codeko.util.Str;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class ExportarFaltasSenecaTask extends org.jdesktop.application.Task<ArrayList<String>, Void> {

    GeneradorFicherosSeneca gen = new GeneradorFicherosSeneca();
    ClienteSeneca cli = null;
    boolean soloFicheros = false;
    GregorianCalendar fechaDesde = null;
    GregorianCalendar fechaHasta = null;

    public ExportarFaltasSenecaTask(org.jdesktop.application.Application app, boolean soloFicheros) {
        this(app, soloFicheros, null, null);
    }

    public ExportarFaltasSenecaTask(org.jdesktop.application.Application app, boolean soloFicheros, GregorianCalendar fechaDesde, GregorianCalendar fechaHasta) {
        super(app);
        this.soloFicheros = soloFicheros;
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;
        if (!soloFicheros) {
            pedirUsuarioClave();
        }
        gen.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            }
        });
    }

    protected final void pedirUsuarioClave() {
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
    public void cancelled() {
        gen.setCancelado(true);
        setMessage("Cancelado");
    }

    @Override
    protected ArrayList<String> doInBackground() {
        return gen.exportarFaltas(fechaDesde, fechaHasta);
    }

    @Override
    protected void succeeded(ArrayList<String> result) {
        if (result != null && result.size() > 0) {
            //Entonces ha habido errores y los mostramos
            JPanel panel = new JPanel();
            BorderLayout layout = new BorderLayout();
            layout.setVgap(10);
            panel.setLayout(layout);
            JList lista = new JList(result.toArray());
            JScrollPane scroll = new JScrollPane(lista);
            panel.add(scroll, BorderLayout.CENTER);
            JLabel label = new JLabel("Se ha producido errores sincronizando los datos con Séneca.");
            panel.add(label, BorderLayout.NORTH);
            JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), panel, "Errores de sincronización.", JOptionPane.ERROR_MESSAGE);
        } else {
            if (!gen.isFicherosGenerados()) {
                JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), "No hay faltas que exportar", "Exportación de faltas", JOptionPane.WARNING_MESSAGE);
            } else {
                if (this.soloFicheros) {
                    JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), "Ficheros de faltas generados correctamente.\nProceda a realizar el envío manual.", "Exportación de faltas", JOptionPane.INFORMATION_MESSAGE);
                    try {
                        Desktop.getDesktop().open(gen.getCarpetaSalida());
                    } catch (IOException ex) {
                        Logger.getLogger(PanelExportacionSeneca.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), "Ficheros de faltas exportados a Séneca correctamente", "Exportación de faltas", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }


    }
}
