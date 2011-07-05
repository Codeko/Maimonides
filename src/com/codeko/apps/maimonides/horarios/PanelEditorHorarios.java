/*
 * PanelEditorHorarios.java
 *
 * Created on 07-abr-2009, 12:55:30
 */
package com.codeko.apps.maimonides.horarios;

import com.codeko.apps.maimonides.IPanel;
import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesBean;
import com.codeko.apps.maimonides.elementos.Actividad;
import com.codeko.apps.maimonides.elementos.Profesor;
import com.codeko.apps.maimonides.elementos.Unidad;
import com.codeko.apps.maimonides.usr.Permisos;
import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;

/**
 *
 * @author Codeko
 */
public class PanelEditorHorarios extends javax.swing.JPanel implements IPanel {

    BloqueHorario bloqueActivo = null;
    JFrame frameConflictos = null;
    PanelVisorConflictos panelVisorConflictos = null;

    /** Creates new form PanelEditorHorarios */
    public PanelEditorHorarios() {
        initComponents();
        setBloqueActivo(null);
        panelProfesores1.getTabla().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panelProfesores1.getTabla().getColumnExt("F. Toma").setVisible(false);
        panelProfesores1.getTabla().getColumnExt("Puesto").setVisible(false);
        panelProfesores1.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("profesorAsignado".equals(evt.getPropertyName())) {
                    Profesor p = (Profesor) evt.getNewValue();
                    panelVisionHorario1.setProfesor(p);
                    actualizar();
                    if (p != null) {
                        bLimpiarProfesor.setText("Profesor: " + p.getDescripcionObjeto());
                    } else {
                        bLimpiarProfesor.setText("Profesor: Todos");
                    }
                }
            }
        });
        panelArbolUnidades1.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("unidadSeleccionada".equals(evt.getPropertyName())) {
                    Unidad obj = (Unidad) evt.getNewValue();
                    panelVisionHorario1.setUnidad(obj);
                    actualizar();
                    if (obj != null) {
                        bLimpiarUnidad.setText("Unidad: " + obj.getDescripcionObjeto());
                    } else {
                        bLimpiarUnidad.setText("Unidad: Todas");
                    }
                }
            }
        });
        panelVisionHorario1.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("focoEnBloqueGanado".equals(evt.getPropertyName())) {
                    BloqueHorario bloque = (BloqueHorario) evt.getNewValue();
                    if (!bloque.equals(getBloqueActivo())) {
                        setBloqueActivo(bloque);
                    }
                } else if ("focoEnBloquePerdido".equals(evt.getPropertyName())) {
                    setBloqueActivo(null);
                }
            }
        });
        if (Permisos.isUsuarioSoloProfesor()) {
            removeAll();
            add(panelHorario, BorderLayout.CENTER);
            panelVisionHorario1.setProfesor(Permisos.getFiltroProfesor());
            actualizar();
        }
        bNuevo.setVisible(Permisos.creacion(getClass()));
        bEliminar.setVisible(Permisos.borrado(getClass()));
        bEditar.setVisible(Permisos.edicion(getClass()));
        panelVisionHorario1.setEditable(Permisos.edicion(getClass()));
    }

    public void addBoton(JButton b) {
        barraHerramientas.remove(panelInfoBloque1);
        barraHerramientas.remove(jSeparator1);

        barraHerramientas.add(b);
        barraHerramientas.add(jSeparator1);
        barraHerramientas.add(panelInfoBloque1);
    }

    public BloqueHorario getBloqueActivo() {
        return bloqueActivo;
    }

    public final void setBloqueActivo(BloqueHorario bloqueActivo) {
        this.bloqueActivo = bloqueActivo;
        setBloqueSeleccionado(bloqueActivo != null);
        panelInfoBloque1.setBloque(bloqueActivo);
        if (isBloqueSeleccionado()) {
            setBloqueConConflictosSeleccionado(bloqueActivo.hayConflictos());
        } else {
            setBloqueConConflictosSeleccionado(false);
        }
        if (isFrameConflictosVisible()) {
            verConlictos();
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

        split = new javax.swing.JSplitPane();
        panelHorario = new javax.swing.JPanel();
        panelVisionHorario1 = new com.codeko.apps.maimonides.horarios.PanelVisionHorario(true);
        barraHerramientas = new javax.swing.JToolBar();
        bActualizar = new javax.swing.JButton();
        bNuevo = new javax.swing.JButton();
        bEditar = new javax.swing.JButton();
        bEliminar = new javax.swing.JButton();
        bVerConflictos = new javax.swing.JButton();
        bImprimir = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        panelInfoBloque1 = new com.codeko.apps.maimonides.horarios.PanelInfoBloque();
        panelLateral = new javax.swing.JPanel();
        splitCabecera = new javax.swing.JSplitPane();
        panelCursos = new javax.swing.JPanel();
        bLimpiarUnidad = new javax.swing.JButton();
        panelArbolUnidades1 = new com.codeko.apps.maimonides.cursos.PanelArbolUnidades();
        panelProfesores = new javax.swing.JPanel();
        bLimpiarProfesor = new javax.swing.JButton();
        panelProfesores1 = new com.codeko.apps.maimonides.profesores.PanelProfesores();

        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        split.setDividerLocation(600);
        split.setResizeWeight(1.0);
        split.setName("split"); // NOI18N

        panelHorario.setName("panelHorario"); // NOI18N
        panelHorario.setLayout(new java.awt.BorderLayout());

        panelVisionHorario1.setName("panelVisionHorario1"); // NOI18N
        panelHorario.add(panelVisionHorario1, java.awt.BorderLayout.CENTER);

        barraHerramientas.setRollover(true);
        barraHerramientas.setName("barraHerramientas"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getActionMap(PanelEditorHorarios.class, this);
        bActualizar.setAction(actionMap.get("actualizar")); // NOI18N
        bActualizar.setFocusable(false);
        bActualizar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bActualizar.setName("bActualizar"); // NOI18N
        bActualizar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraHerramientas.add(bActualizar);

        bNuevo.setAction(actionMap.get("nuevoHorario")); // NOI18N
        bNuevo.setFocusable(false);
        bNuevo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bNuevo.setName("bNuevo"); // NOI18N
        bNuevo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraHerramientas.add(bNuevo);

        bEditar.setAction(actionMap.get("editarBloque")); // NOI18N
        bEditar.setFocusable(false);
        bEditar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bEditar.setName("bEditar"); // NOI18N
        bEditar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraHerramientas.add(bEditar);

        bEliminar.setAction(actionMap.get("elimiarBloqueHorario")); // NOI18N
        bEliminar.setFocusable(false);
        bEliminar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bEliminar.setName("bEliminar"); // NOI18N
        bEliminar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraHerramientas.add(bEliminar);

        bVerConflictos.setAction(actionMap.get("verConlictos")); // NOI18N
        bVerConflictos.setFocusable(false);
        bVerConflictos.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bVerConflictos.setName("bVerConflictos"); // NOI18N
        bVerConflictos.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraHerramientas.add(bVerConflictos);

        bImprimir.setAction(actionMap.get("imprimirHorario")); // NOI18N
        bImprimir.setFocusable(false);
        bImprimir.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bImprimir.setName("bImprimir"); // NOI18N
        bImprimir.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraHerramientas.add(bImprimir);

        jSeparator1.setName("jSeparator1"); // NOI18N
        barraHerramientas.add(jSeparator1);

        panelInfoBloque1.setName("panelInfoBloque1"); // NOI18N
        barraHerramientas.add(panelInfoBloque1);

        panelHorario.add(barraHerramientas, java.awt.BorderLayout.PAGE_START);

        split.setLeftComponent(panelHorario);

        panelLateral.setMinimumSize(new java.awt.Dimension(0, 0));
        panelLateral.setName("panelLateral"); // NOI18N
        panelLateral.setLayout(new java.awt.BorderLayout());

        splitCabecera.setDividerLocation(250);
        splitCabecera.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        splitCabecera.setName("splitCabecera"); // NOI18N

        panelCursos.setName("panelCursos"); // NOI18N
        panelCursos.setLayout(new java.awt.BorderLayout());

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getResourceMap(PanelEditorHorarios.class);
        bLimpiarUnidad.setIcon(resourceMap.getIcon("bLimpiarUnidad.icon")); // NOI18N
        bLimpiarUnidad.setText(resourceMap.getString("bLimpiarUnidad.text")); // NOI18N
        bLimpiarUnidad.setToolTipText(resourceMap.getString("bLimpiarUnidad.toolTipText")); // NOI18N
        bLimpiarUnidad.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        bLimpiarUnidad.setName("bLimpiarUnidad"); // NOI18N
        bLimpiarUnidad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bLimpiarUnidadActionPerformed(evt);
            }
        });
        panelCursos.add(bLimpiarUnidad, java.awt.BorderLayout.PAGE_START);

        panelArbolUnidades1.setName("panelArbolUnidades1"); // NOI18N
        panelCursos.add(panelArbolUnidades1, java.awt.BorderLayout.CENTER);

        splitCabecera.setTopComponent(panelCursos);

        panelProfesores.setName("panelProfesores"); // NOI18N
        panelProfesores.setLayout(new java.awt.BorderLayout());

        bLimpiarProfesor.setIcon(resourceMap.getIcon("bLimpiarProfesor.icon")); // NOI18N
        bLimpiarProfesor.setText(resourceMap.getString("bLimpiarProfesor.text")); // NOI18N
        bLimpiarProfesor.setToolTipText(resourceMap.getString("bLimpiarProfesor.toolTipText")); // NOI18N
        bLimpiarProfesor.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        bLimpiarProfesor.setName("bLimpiarProfesor"); // NOI18N
        bLimpiarProfesor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bLimpiarProfesorActionPerformed(evt);
            }
        });
        panelProfesores.add(bLimpiarProfesor, java.awt.BorderLayout.PAGE_START);

        panelProfesores1.setBarraHerramientasVisible(false);
        panelProfesores1.setName("panelProfesores1"); // NOI18N
        panelProfesores.add(panelProfesores1, java.awt.BorderLayout.CENTER);

        splitCabecera.setBottomComponent(panelProfesores);

        panelLateral.add(splitCabecera, java.awt.BorderLayout.CENTER);

        split.setRightComponent(panelLateral);

        add(split, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void bLimpiarUnidadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bLimpiarUnidadActionPerformed
        panelArbolUnidades1.desmarcar();
    }//GEN-LAST:event_bLimpiarUnidadActionPerformed

    private void bLimpiarProfesorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bLimpiarProfesorActionPerformed
        panelProfesores1.desmarcar();
    }//GEN-LAST:event_bLimpiarProfesorActionPerformed
    private boolean bloqueSeleccionado = false;

    public boolean isBloqueSeleccionado() {
        return bloqueSeleccionado;
    }

    public void setBloqueSeleccionado(boolean b) {
        boolean old = isBloqueSeleccionado();
        this.bloqueSeleccionado = b;
        firePropertyChange("bloqueSeleccionado", old, isBloqueSeleccionado());
    }
    private boolean bloqueConConflictosSeleccionado = false;

    public boolean isBloqueConConflictosSeleccionado() {
        return bloqueConConflictosSeleccionado;
    }

    public void setBloqueConConflictosSeleccionado(boolean b) {
        boolean old = isBloqueConConflictosSeleccionado();
        this.bloqueConConflictosSeleccionado = b;
        firePropertyChange("bloqueConConflictosSeleccionado", old, isBloqueConConflictosSeleccionado());
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bActualizar;
    private javax.swing.JButton bEditar;
    private javax.swing.JButton bEliminar;
    private javax.swing.JButton bImprimir;
    private javax.swing.JButton bLimpiarProfesor;
    private javax.swing.JButton bLimpiarUnidad;
    private javax.swing.JButton bNuevo;
    private javax.swing.JButton bVerConflictos;
    protected javax.swing.JToolBar barraHerramientas;
    private javax.swing.JToolBar.Separator jSeparator1;
    private com.codeko.apps.maimonides.cursos.PanelArbolUnidades panelArbolUnidades1;
    private javax.swing.JPanel panelCursos;
    private javax.swing.JPanel panelHorario;
    private com.codeko.apps.maimonides.horarios.PanelInfoBloque panelInfoBloque1;
    private javax.swing.JPanel panelLateral;
    private javax.swing.JPanel panelProfesores;
    private com.codeko.apps.maimonides.profesores.PanelProfesores panelProfesores1;
    private com.codeko.apps.maimonides.horarios.PanelVisionHorario panelVisionHorario1;
    private javax.swing.JSplitPane split;
    private javax.swing.JSplitPane splitCabecera;
    // End of variables declaration//GEN-END:variables

    @Override
    public boolean puedoSusituir() {
        return true;
    }

    @Action
    public void nuevoHorario() {
        Actividad a;
        try {
            a = Actividad.getActividad(Actividad.getIdActividadDocencia(MaimonidesApp.getApplication().getAnoEscolar()));
            BloqueHorario bloque = new BloqueHorario(MaimonidesApp.getApplication().getAnoEscolar(), 0, 0, a, null, null);
            bloque.addPropertyChangeListener(panelVisionHorario1.listenerBloques);
            if (panelVisionHorario1.getUnidad() != null) {
                bloque.addUnidad(panelVisionHorario1.getUnidad());
            }
            if (panelVisionHorario1.getProfesor() != null) {
                bloque.setProfesor(panelVisionHorario1.getProfesor());
            }
            DialogoEditorHorarios dlg = new DialogoEditorHorarios(panelVisionHorario1, bloque);
            dlg.setTitle("Nuevo bloque horario");
            dlg.mostrar();
        } catch (Exception ex) {
            Logger.getLogger(PanelEditorHorarios.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Action
    public final void actualizar() {
        setBloqueActivo(null);
        panelVisionHorario1.vaciar();
        panelVisionHorario1.cargar();
    }

    @Action(enabledProperty = "bloqueSeleccionado")
    public void editarBloque() {
        DialogoEditorHorarios dlg = new DialogoEditorHorarios(panelVisionHorario1, getBloqueActivo());
        dlg.setTitle("Editar bloque horario");
        dlg.pack();
        dlg.mostrar();
    }

    @Action(enabledProperty = "bloqueSeleccionado")
    public void elimiarBloqueHorario() {
        BloqueHorario bloque = getBloqueActivo();
        int op = JOptionPane.showConfirmDialog(MaimonidesApp.getApplication().getMainFrame(), "¿Está seguro de que desea eliminar el bloque horario seleccionado?", "Eliminar bloque horario", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (op == JOptionPane.YES_OPTION) {
            if (bloque.eliminar()) {
                //panelVisionHorario1.bloqueHorarioBorrado(bloque);
                // JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), "Bloque horario eliminado correctamente.", "Bloque horario eliminado", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), "Por alguna razón no se ha podido borrar el bloque horario.", "Bloque horario NO eliminado", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private PanelVisorConflictos getPanelVisorConflictos() {
        if (panelVisorConflictos == null) {
            panelVisorConflictos = new PanelVisorConflictos();
        }
        return panelVisorConflictos;
    }

    private boolean isFrameConflictosVisible() {
        return this.frameConflictos != null && this.frameConflictos.isVisible();
    }

    private JFrame getFrameConflictos() {
        if (frameConflictos == null) {
            frameConflictos = new JFrame("Conflictos horarios");
            frameConflictos.setName("VisorConflictosHorarios");
            frameConflictos.setAlwaysOnTop(true);
            frameConflictos.add(getPanelVisorConflictos(), BorderLayout.CENTER);
            frameConflictos.pack();
        }
        return frameConflictos;
    }

    @Action(enabledProperty = "bloqueConConflictosSeleccionado")
    public void verConlictos() {
        BloqueHorario bloque = getBloqueActivo();
        if (bloque != null) {
            getPanelVisorConflictos().setDatos(bloque.getConflictos());
            if (!isFrameConflictosVisible()) {
                MaimonidesApp.getApplication().show(getFrameConflictos());
            }
        }
    }

    @Action(block = Task.BlockingScope.ACTION)
    public Task imprimirHorario() {
        return new ImprimirHorarioTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class ImprimirHorarioTask extends org.jdesktop.application.Task<Object, Void> {

        ImprimirHorarioTask(org.jdesktop.application.Application app) {
            super(app);
        }

        @Override
        protected Object doInBackground() {
            setMessage("Imprimiendo horario...");
            MaimonidesBean bean = new MaimonidesBean();
            bean.addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                }
            });
            panelVisionHorario1.imprimir(bean);
            return null;
        }

        @Override
        protected void succeeded(Object result) {
        }
    }
}
