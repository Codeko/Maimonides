/*
 * PanelInfoCopiasSeguridad.java
 *
 * Created on 30 de octubre de 2008, 16:29
 */
package com.codeko.apps.maimonides.seneca;

import com.codeko.apps.maimonides.ICargable;
import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesInputBlocker;
import com.codeko.apps.maimonides.MaimonidesUtil;
import com.codeko.apps.maimonides.convivencia.ParteConvivencia;
import com.codeko.apps.maimonides.seneca.operaciones.convivencia.GestorConvivenciaSeneca;
import com.codeko.apps.maimonides.seneca.operaciones.envioFicherosFaltas.ExportarFaltasSenecaTask;
import com.codeko.swing.CdkProcesoLabel;
import com.codeko.util.Fechas;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JLabel;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskEvent;
import org.jdesktop.application.TaskListener;
import org.jdesktop.swingx.JXHyperlink;

/**
 *
 * @author  Codeko
 */
public class PanelInfoSeneca extends javax.swing.JPanel implements ICargable {

    boolean cargado = false;
    boolean resumido = false;
    PanelInfoSeneca auto = this;
    GeneradorFicherosSeneca gen = null;
    ArrayList<ParteConvivencia> partes = null;
    TaskListener<java.util.ArrayList<java.lang.String>, java.lang.Void> tl = new TaskListener<java.util.ArrayList<java.lang.String>, java.lang.Void>() {

        @Override
        public void doInBackground(TaskEvent event) {
        }

        @Override
        public void process(TaskEvent event) {
        }

        @Override
        public void succeeded(TaskEvent event) {
        }

        @Override
        public void failed(TaskEvent event) {
        }

        @Override
        public void cancelled(TaskEvent event) {
        }

        @Override
        public void interrupted(TaskEvent event) {
        }

        @Override
        public void finished(TaskEvent event) {
            actualizarDatos();
        }
    };

    /** Creates new form PanelInfoCopiasSeguridad */
    public PanelInfoSeneca() {
        initComponents();
    }

    public PanelInfoSeneca(boolean resumido) {
        initComponents();
        setResumido(resumido);
    }

    public boolean isResumido() {
        return resumido;
    }

    public final void setResumido(boolean resumido) {
        this.resumido = resumido;
    }

    public void actualizarDatos() {
        MaimonidesUtil.ejecutarTask(this, "actualizar");
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

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
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));
    }// </editor-fold>//GEN-END:initComponents

private void formAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_formAncestorAdded
    cargar();
}//GEN-LAST:event_formAncestorAdded

    @Action(block = Task.BlockingScope.ACTION)
    public Task actualizar() {
        return new ActualizarTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    @Override
    public void cargar() {
        if (!isCargado()) {
            actualizarDatos();
            setCargado(true);
        }
    }

    @Override
    public void vaciar() {
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

    private class ActualizarTask extends org.jdesktop.application.Task<ArrayList<Component>, Void> {

        boolean hayTitular = false;
        JXHyperlink jh = null;
        JXHyperlink jg = null;
        int numFicheros = 0;
        boolean anadidoPuntos = false;
        JLabel lf = null;
        ArrayList<Component> comps = new ArrayList<Component>();

        ActualizarTask(org.jdesktop.application.Application app) {
            super(app);
            removeAll();
            CdkProcesoLabel cpl = new CdkProcesoLabel();
            cpl.setText("Verificando datos pendientes de enviar...");
            cpl.setProcesando(true);
            add(cpl);
        }

        private void procesarEtiqueta(final GregorianCalendar fechaDesde, final GregorianCalendar fechaHasta, ArrayList<String> cursos) {
            if (!hayTitular) {
                hayTitular = true;
                jh = new JXHyperlink();
                jh.setAction(MaimonidesUtil.getActionTask(MaimonidesApp.getMaimonidesView(), "exportarFaltasSeneca"));
                jh.setText("<html>Exportar faltas a Séneca.");
                comps.add(jh);
                JLabel l = new JLabel("Envíos pendientes:");
                comps.add(l);
            }
            numFicheros++;
            if (!isResumido() || comps.size() < 7) {
                //Ahora creamos la etiqueta.
                String texto = "    - Fecha: ";
                if (Fechas.getDiferenciaTiempoEn(fechaDesde, fechaHasta, GregorianCalendar.DATE) == 0) {
                    texto += Fechas.format(fechaHasta, "dd/MM");
                } else {
                    texto += Fechas.format(fechaDesde, "dd/MM") + " a " + Fechas.format(fechaHasta, "dd/MM");
                }
                texto += " para: ";
                boolean primero = true;
                for (String c : cursos) {
                    if (primero) {
                        primero = false;
                    } else {
                        texto += ",";
                    }
                    texto += c;
                }
                JXHyperlink l = new JXHyperlink();
                l.setText(texto);
                AbstractAction a = new AbstractAction() {

                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        ExportarFaltasSenecaTask t = new ExportarFaltasSenecaTask(MaimonidesApp.getApplication(), false, fechaDesde, fechaHasta);
                        t.setInputBlocker(new MaimonidesInputBlocker(t, Task.BlockingScope.APPLICATION, MaimonidesApp.getApplication().getMainFrame(), null));
                        t.addTaskListener(tl);
                        MaimonidesApp.getApplication().getContext().getTaskService().execute(t);
                    }
                };
                a.putValue(AbstractAction.SHORT_DESCRIPTION, l.getText());
                a.putValue(AbstractAction.LONG_DESCRIPTION, l.getText());
                a.putValue(AbstractAction.NAME, l.getText());
                l.setAction(a);
                l.setToolTipText("Haga clic para iniciar el envío de este fichero.");
                comps.add(l);
            } else {
                if (!anadidoPuntos) {
                    anadidoPuntos = true;
                    lf = new JLabel("");
                    comps.add(lf);
                }
                lf.setText("    - " + (numFicheros - 5) + " ficheros más... ");
            }
        }

        @Override
        protected ArrayList<Component> doInBackground() {

            firePropertyChange("message", null, "Verificando datos pendientes de enviar...");

            gen = new GeneradorFicherosSeneca() {

                @Override
                public ArrayList<String> procesarFaltas(GregorianCalendar fechaDesde, GregorianCalendar fechaHasta, ArrayList<String> cursos) {
                    procesarEtiqueta(fechaDesde, fechaHasta, cursos);
                    return new ArrayList<String>();
                }
            };
            //Comenzamos con las faltas de asistencia
            comps.add(new JLabel("<html><h3>Faltas de asistencia</h3>"));
            //Ahora vemos si hay ficheros ya generados
            File[] pendientes = gen.getFicherosExistentes();
            if (pendientes.length > 0) {
                jg = new JXHyperlink();
                jg.setAction(MaimonidesUtil.getActionTask(auto, "abrirCarpetaEnvios"));
                jg.setText("<html>Hay <b>" + pendientes.length + "</b> ficheros generados pendientes de enviarse a Séneca.");
                jg.setToolTipText("Haga clic para abrir la carpeta de ficheros generados pendientes de enviarse a Séneca.");
                comps.add(jg);
            }

            File[] fallidos = gen.getFicherosFallidos();
            if (fallidos.length > 0) {
                jg = new JXHyperlink();
                jg.setAction(MaimonidesUtil.getActionTask(auto, "abrirCarpetaFallidos"));
                jg.setText("<html>Hay <b>" + fallidos.length + "</b> ficheros que ha fallado su envío a Séneca.");
                jg.setToolTipText("Haga clic para abrir la carpeta de ficheros fallidos.");
                comps.add(jg);
            }
            gen.exportarFaltas();
            if (numFicheros > 0) {
                jh.setText("<html>Hay <b>" + numFicheros + "</b> ficheros pendientes de generarse y enviarse a Séneca.");
                jh.setToolTipText("Haga clic para abrir iniciar el envío.");
            } else {
                JLabel l = new JLabel("Las faltas están sincronizadas con Séneca.");
                comps.add(l);
            }
            //Ahora vemos los partes de asistencia
            comps.add(new JLabel("<html><h3>Partes de convivencia</h3>"));
            partes = GestorConvivenciaSeneca.getPartesAEnviar();
            if (partes.size() > 0) {
                int total = partes.size();
                ArrayList<ParteConvivencia> noEnviables = GestorConvivenciaSeneca.limpiarPartesNoEnviables(partes);
                jg = new JXHyperlink();
                jg.setAction(MaimonidesUtil.getActionTask(auto, "enviarPartesConvivencia"));
                String extra = "";
                if (noEnviables.size() > 0) {
                    extra = " de los cuales <b>" + noEnviables.size() + "</b> no se pueden enviar todavía";
                }
                jg.setText("<html>Hay <b>" + total + "</b> partes de convivencia pendientes de enviarse a Séneca" + extra + ".");
                jg.setToolTipText("Haga clic para iniciar el envío de los partes.");
                comps.add(jg);
            } else {
                JLabel l = new JLabel("Los partes de convivencia están sincronizadas con Séneca.");
                comps.add(l);
            }
            return comps;
        }

        @Override
        protected void succeeded(ArrayList<Component> result) {
            removeAll();
            for (Component c : result) {
                add(c);
                add(Box.createRigidArea(new Dimension(10, 6)));
            }
            updateUI();
        }
    }

    @Action
    public void abrirCarpetaEnvios() {
        try {
            Desktop.getDesktop().open(gen.getCarpetaSalida());
            // Desktop.getDesktop().browse(new URI("https://www.juntadeandalucia.es/educacion/seneca/seneca/jsp/pag_inicio800.html"));
        } catch (Exception ex) {
            Logger.getLogger(PanelInfoSeneca.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Action
    public void abrirCarpetaFallidos() {
        MaimonidesApp.getMaimonidesView().mostrarPanelProblemasEnvioFaltas();
    }

    @Action(block = Task.BlockingScope.ACTION)
    public Task enviarPartesConvivencia() {
        Task t = GestorConvivenciaSeneca.getTaskEnvioPartes(partes);
        t.addTaskListener(tl);
        return t;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
