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
        gen.setEnviarASeneca(!soloFicheros);    
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
    
    public boolean isSoloMarcarComoEnviados() {
        return gen.isSoloMarcarComoEnviados();
    }

    public void setSoloMarcarComoEnviados(boolean soloMarcarComoEnviados) {
        gen.setSoloMarcarComoEnviados(soloMarcarComoEnviados);
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
                    if(this.isSoloMarcarComoEnviados()){
                        JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), "Tanda de faltas marcada como enviada correctamente.", "Marcar faltas como enviadas", JOptionPane.INFORMATION_MESSAGE);
                    }else{
                        JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), "Ficheros de faltas generados correctamente.\nProceda a realizar el envío manual.", "Exportación de faltas", JOptionPane.INFORMATION_MESSAGE);
                        try {
                            Desktop.getDesktop().open(gen.getCarpetaSalida());
                        } catch (IOException ex) {
                            Logger.getLogger(PanelExportacionSeneca.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), "Ficheros de faltas exportados a Séneca correctamente", "Exportación de faltas", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }


    }
}
