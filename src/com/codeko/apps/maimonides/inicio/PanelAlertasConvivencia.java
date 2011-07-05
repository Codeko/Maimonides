/*
 * PanelAlertasConvivencia.java
 *
 * Created on 27-ago-2010, 13:07:42
 */
package com.codeko.apps.maimonides.inicio;

import com.codeko.apps.maimonides.ICargable;
import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesInputBlocker;
import com.codeko.apps.maimonides.MaimonidesUtil;
import com.codeko.apps.maimonides.convivencia.NotificarPartesDeConvivenciaTask;
import com.codeko.apps.maimonides.convivencia.ParteConvivencia;
import com.codeko.apps.maimonides.elementos.Profesor;
import com.codeko.apps.maimonides.elementos.Unidad;
import com.codeko.apps.maimonides.usr.Usuario;
import com.codeko.swing.CdkProcesoLabel;
import java.awt.Component;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskEvent;
import org.jdesktop.application.TaskListener;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXHyperlink;

/**
 *
 * @author codeko
 */
public class PanelAlertasConvivencia extends javax.swing.JPanel implements ICargable {

    PanelAlertasConvivencia auto = this;
    final org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getResourceMap(PanelAlertasPerdidaEscolaridad.class);
    boolean cargado = false;
    TaskListener<Boolean, java.lang.Void> tl = new TaskListener<Boolean, java.lang.Void>() {

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

    /** Creates new form PanelAlertasConvivencia */
    public PanelAlertasConvivencia() {
        initComponents();
    }

    @Action(block = Task.BlockingScope.ACTION)
    public Task actualizar() {
        return new ActualizarTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    class ActualizarTask extends org.jdesktop.application.Task<ArrayList<Component>, Void> {

        ActualizarTask(org.jdesktop.application.Application app) {
            super(app);
            removeAll();
            CdkProcesoLabel cpl = new CdkProcesoLabel();
            cpl.setText("Verificando alertas de tutor...");
            cpl.setProcesando(true);
            add(cpl);
        }

        @Override
        protected ArrayList<Component> doInBackground() {
            ArrayList<Component> comps = new ArrayList<Component>();
            Usuario usr = MaimonidesApp.getApplication().getUsuario();
            Profesor p = null;
            if (usr != null) {
                p = usr.getProfesor();
            }
            Unidad unidad = null;
            if (p != null) {
                unidad = Unidad.getUnidadPorTutor(p.getId());
            }
            if (unidad != null) {
                cargarPartesPendientesRevisar(comps, unidad);
                cargarPartesPendientesNotificar(comps, unidad);
            }
            return comps;
        }

        @Override
        protected void succeeded(ArrayList<Component> result) {
            removeAll();
            for (Component c : result) {
                add(c);
                Component comp = Box.createRigidArea(new Dimension(10, 0));
                add(comp);
            }
            updateUI();
        }
    }

    private void cargarPartesPendientesRevisar(ArrayList<Component> comps, Unidad unidad) {
        ArrayList<ParteConvivencia> partes = ParteConvivencia.getPartes(null, null, null, null, null, unidad, null, null, null, ParteConvivencia.SIT_REVISADO_TUTOR, null, null, null);
        if (partes.isEmpty()) {
            comps.add(new JLabel("<html><b><font color='#009900'>No hay partes de convivencia pendientes de revisar.</font></b>"));
        } else {
            JXCollapsiblePane panelDatos = getPanelDatos();
            comps.add(getBotonOcultar(panelDatos, "<html><h4><font color='#FF8000'>Partes de convivencia pendientes de revisar (<b>" + partes.size() + "</b>)</font></h4>"));
            comps.add(panelDatos);
            for (ParteConvivencia d : partes) {
                JXHyperlink l = new JXHyperlink();
                String descripcion = d.getDescripcion();
                if (descripcion.trim().equals("")) {
                    descripcion = "Sin descripción";
                }
                l.setText("<html><b>" + d.getAlumno() + "</b>: " + descripcion);
                l.setToolTipText("Abrir ficha de parte de convivencia.");
                l.putClientProperty("parte", d);
                l.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        MaimonidesApp.getMaimonidesView().mostrarFichaElemento((ParteConvivencia) ((JXHyperlink) e.getSource()).getClientProperty("parte"));
                    }
                });
                panelDatos.add(l);
            }
        }
    }

    private void cargarPartesPendientesNotificar(ArrayList<Component> comps, Unidad unidad) {
        final ArrayList<ParteConvivencia> partes = ParteConvivencia.getPartes(null, null, null, null, null, unidad, null, null, null, null, null, ParteConvivencia.MASCARA_INFORMADO_PADRES, null);
        if (partes.isEmpty()) {
            comps.add(new JLabel("<html><b><font color='#009900'>No hay partes de convivencia pendientes de notificar.</font></b>"));
        } else {
            JXCollapsiblePane panelDatos = getPanelDatos();
            FlowLayout fl = new FlowLayout(FlowLayout.LEFT);
            fl.setHgap(0);
            fl.setVgap(0);
            JPanel panelBoton = new JPanel(fl);
            panelBoton.setAlignmentX(Component.LEFT_ALIGNMENT);
            comps.add(panelBoton);
            panelBoton.add(getBotonOcultar(panelDatos, "<html><h4><font color='#FF8000'>Partes de convivencia pendientes de notificar (<b>" + partes.size() + "</b>)</font></h4>"));
            panelBoton.add(Box.createRigidArea(new Dimension(10, 6)));
            comps.add(panelDatos);
            for (ParteConvivencia d : partes) {
                final ArrayList<ParteConvivencia> partesLinea = new ArrayList<ParteConvivencia>();
                partesLinea.add(d);
                JXHyperlink l = new JXHyperlink();
                String descripcion = d.getDescripcion();
                if (descripcion.trim().equals("")) {
                    descripcion = "Sin descripción";
                }
                l.setText("<html><b>" + d.getAlumno() + "</b>: " + descripcion);
                l.setToolTipText("Abrir ficha de parte de convivencia.");
                l.putClientProperty("parte", d);
                l.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        MaimonidesApp.getMaimonidesView().mostrarFichaElemento((ParteConvivencia) ((JXHyperlink) e.getSource()).getClientProperty("parte"));
                    }
                });
                FlowLayout flb = new FlowLayout(FlowLayout.LEFT);
                flb.setHgap(0);
                flb.setVgap(0);
                JPanel panelBotonLinea = new JPanel(fl);
                panelBotonLinea.add(l);
                panelBotonLinea.add(Box.createRigidArea(new Dimension(10, 6)));
                JXHyperlink lNot = new JXHyperlink();
                lNot.setText("Notificar");
                AbstractAction a = new AbstractAction() {

                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        int op = JOptionPane.showConfirmDialog(MaimonidesApp.getApplication().getMainFrame(), "Se va ha enviar la notificación del parte de convivencia a " + partesLinea.get(0).getAlumno() + "\n¿Enviar notificación?", "Confirmación de envío de notificación", JOptionPane.YES_NO_OPTION);
                        if (op == JOptionPane.YES_OPTION) {
                            NotificarPartesDeConvivenciaTask t = new NotificarPartesDeConvivenciaTask(MaimonidesApp.getApplication(), partesLinea);
                            t.setInputBlocker(new MaimonidesInputBlocker(t, Task.BlockingScope.APPLICATION, MaimonidesApp.getApplication().getMainFrame(), null));
                            t.addTaskListener(tl);
                            MaimonidesApp.getApplication().getContext().getTaskService().execute(t);
                        }
                    }
                };
                a.putValue(AbstractAction.SHORT_DESCRIPTION, lNot.getText());
                a.putValue(AbstractAction.LONG_DESCRIPTION, lNot.getText());
                a.putValue(AbstractAction.NAME, lNot.getText());
                lNot.setAction(a);
                lNot.setIcon(MaimonidesApp.getApplication().getContext().getResourceMap(auto.getClass()).getIcon("notificacionPartesConvivencia.icon"));
                lNot.setToolTipText("Haga clic para notificar el parte de convivencia.");
                panelBotonLinea.add(lNot);
                panelDatos.add(panelBotonLinea);
            }
            JXHyperlink l = new JXHyperlink();
            l.setText("Notificar todos");
            AbstractAction a = new AbstractAction() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    int op = JOptionPane.showConfirmDialog(MaimonidesApp.getApplication().getMainFrame(), "Se van ha enviar la notificaciones de los partes de convivencia.\n¿Enviar notificaciones?", "Confirmación de envío de notificaciones", JOptionPane.YES_NO_OPTION);
                    if (op == JOptionPane.YES_OPTION) {
                        NotificarPartesDeConvivenciaTask t = new NotificarPartesDeConvivenciaTask(MaimonidesApp.getApplication(), partes);
                        t.setInputBlocker(new MaimonidesInputBlocker(t, Task.BlockingScope.APPLICATION, MaimonidesApp.getApplication().getMainFrame(), null));
                        t.addTaskListener(tl);
                        MaimonidesApp.getApplication().getContext().getTaskService().execute(t);
                    }
                }
            };
            a.putValue(AbstractAction.SHORT_DESCRIPTION, l.getText());
            a.putValue(AbstractAction.LONG_DESCRIPTION, l.getText());
            a.putValue(AbstractAction.NAME, l.getText());
            l.setAction(a);
            l.setIcon(MaimonidesApp.getApplication().getContext().getResourceMap(auto.getClass()).getIcon("notificacionPartesConvivencia.icon"));
            l.setToolTipText("Haga clic para notificar los partes de convivencia.");
            panelBoton.add(l);
        }
    }

    private JXHyperlink getBotonOcultar(final JXCollapsiblePane panel, String texto) {
        final JXHyperlink boton = new JXHyperlink();
        boton.setIcon(resourceMap.getIcon("bOcultar.icon")); // NOI18N
        boton.setText(texto); // NOI18N
        boton.setToolTipText("Haga clic para mostrar/ocultar los detalles"); // NOI18N
        boton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                panel.setCollapsed(!panel.isCollapsed());
                if (panel.isCollapsed()) {
                    boton.setIcon(resourceMap.getIcon("bOcultar.icon"));
                } else {
                    boton.setIcon(resourceMap.getIcon("bOcultar.down.icon"));
                }
            }
        });
        boton.setAlignmentX(Component.LEFT_ALIGNMENT);
        return boton;
    }

    private JXCollapsiblePane getPanelDatos() {
        JXCollapsiblePane panelDatos = new org.jdesktop.swingx.JXCollapsiblePane();
        panelDatos.setCollapsed(true);
        panelDatos.getContentPane().setLayout(new javax.swing.BoxLayout(panelDatos.getContentPane(), javax.swing.BoxLayout.Y_AXIS));
        panelDatos.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panelDatos;
    }

    @Override
    public void cargar() {
        if (!isCargado()) {
            actualizarDatos();
            setCargado(true);
        }
    }

    public void actualizarDatos() {
        MaimonidesUtil.ejecutarTask(this, "actualizar");
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
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
