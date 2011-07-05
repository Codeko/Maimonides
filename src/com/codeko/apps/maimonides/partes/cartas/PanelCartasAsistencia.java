/*
 * PanelFaltaAsistenciaAlumnos.java
 *
 * Created on 20-may-2009, 20:01:28
 */
package com.codeko.apps.maimonides.partes.cartas;

import com.codeko.apps.maimonides.partes.informes.asistencia.*;
import com.codeko.apps.maimonides.IPanel;
import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesUtil;
import com.codeko.apps.maimonides.cartero.Carta;
import com.codeko.apps.maimonides.cartero.CarteroAlumno;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.Curso;
import com.codeko.apps.maimonides.elementos.Unidad;
import com.codeko.apps.maimonides.usr.Permisos;
import com.codeko.swing.CodekoTableModel;
import com.codeko.util.Fechas;
import com.codeko.util.Str;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;

/**
 *
 * @author Codeko
 */
public class PanelCartasAsistencia extends javax.swing.JPanel implements IPanel {

    CodekoTableModel<AsistenciaAlumno> modelo = new CodekoTableModel<AsistenciaAlumno>(new AsistenciaAlumno(new Alumno()));
    boolean cargado = false;
    PanelCartasAsistencia auto=this;
    /** Creates new form PanelFaltaAsistenciaAlumnos */
    public PanelCartasAsistencia() {
        initComponents();
        tabla.getColumnExt("Código").setVisible(false);
        //Hay que asignar las fechas de la última semana
        //TODO Esto debería ser configurable
        GregorianCalendar cal = new GregorianCalendar();
        cal.add(GregorianCalendar.DATE, -1);
        //Vamos retrocediendo hasta que sea viernes
        while (cal.get(GregorianCalendar.DAY_OF_WEEK) != GregorianCalendar.FRIDAY) {
            cal.add(GregorianCalendar.DATE, -1);
        }
        tfFechaHasta.setDate(cal.getTime());
        //Ahora seguimos hasta el lunes
        while (cal.get(GregorianCalendar.DAY_OF_WEEK) != GregorianCalendar.MONDAY) {
            cal.add(GregorianCalendar.DATE, -1);
        }
        tfFechaDesde.setDate(cal.getTime());
        MaimonidesUtil.setFormatosFecha(tfFechaDesde, false);
        MaimonidesUtil.setFormatosFecha(tfFechaHasta, false);
        MaimonidesUtil.addMenuTabla(tabla, "Listado de asistencia");
        //Ahora vemos si el usuario tiene filtro de unidad
        Unidad u = Permisos.getFiltroUnidad();
        if (u != null) {
            Curso c;
            try {
                c = Curso.getCurso(u.getIdCurso());
                cbCursos1.setSelectedItem(c);
            } catch (Exception ex) {
                Logger.getLogger(PanelCartasAsistencia.class.getName()).log(Level.SEVERE, null, ex);
            }
            cbCursos1.setEnabled(false);
            cbGrupos1.setSelectedItem(u);
            cbGrupos1.setEnabled(false);
        }
    }

    public void setOcultarBotonera(boolean ocultar) {
        for (Component c : barraHerramientas.getComponents()) {
            if (c != bActualizar) {
                c.setVisible(!ocultar);
            }
        }

    }

    public boolean isCargado() {
        return cargado;
    }

    public void setCargado(boolean cargado) {
        this.cargado = cargado;
    }

    @Action
    public Task actualizar() {
        return new ActualizarTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    @Override
    public boolean puedoSusituir() {
        return true;
    }

    private void commitEdiciones() {
        try {
            tfFechaDesde.commitEdit();
        } catch (ParseException ex) {
            Logger.getLogger(PanelCartasAsistencia.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            tfFechaHasta.commitEdit();
        } catch (ParseException ex) {
            Logger.getLogger(PanelCartasAsistencia.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private class ActualizarTask extends org.jdesktop.application.Task<ArrayList<AsistenciaAlumno>, Void> {

        ActualizarTask(org.jdesktop.application.Application app) {
            super(app);
            modelo.vaciar();
            setCargado(false);
            commitEdiciones();
        }

        @Override
        protected ArrayList<AsistenciaAlumno> doInBackground() {
            setMessage("Cargando asistencia...");
            return cargarAsistencia();
        }

        @Override
        protected void succeeded(ArrayList<AsistenciaAlumno> result) {
            setMessage("Asistencia cargada correctamente.");
            modelo.addDatos(result);
            setCargado(true);
            tabla.packAll();
        }
    }

    private ArrayList<AsistenciaAlumno> cargarAsistencia() {
        ArrayList<FiltroAsistenciaAlumno> filtros = panelFiltroFaltas1.getFiltros();
        filtros.addAll(panelFiltroFaltas2.getFiltros());
        ArrayList<AsistenciaAlumno> asistencia = AsistenciaAlumno.getAsistencias(null, cbCursos1.getCurso(), cbGrupos1.getUnidad(), Fechas.toGregorianCalendar(tfFechaDesde.getDate()), Fechas.toGregorianCalendar(tfFechaHasta.getDate()), filtros, null);
        return asistencia;
    }

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task enviar() {
        return new EnviarTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class EnviarTask extends org.jdesktop.application.Task<Boolean, Void> {

        EnviarTask(org.jdesktop.application.Application app) {
            super(app);
            modelo.vaciar();
            commitEdiciones();
        }

        @Override
        protected Boolean doInBackground() {
            setMessage("Cargando asistencia...");
            ArrayList<AsistenciaAlumno> asistencia = cargarAsistencia();
            setProgress(0, 0, asistencia.size());
            modelo.addDatos(asistencia);
            //Ahora vamos carta por carta preparando y enviado.
            GregorianCalendar ini = Fechas.toGregorianCalendar(tfFechaDesde.getDate());
            GregorianCalendar fin = Fechas.toGregorianCalendar(tfFechaHasta.getDate());
            if (ini == null) {
                ini = new GregorianCalendar();
                ini.set(MaimonidesApp.getApplication().getAnoEscolar().getAno(), GregorianCalendar.SEPTEMBER, 1);
            }
            if (fin == null) {
                fin = new GregorianCalendar();
            }
            final CarteroAsistenciaAlumo ca = new CarteroAsistenciaAlumo(ini, fin);
            CarteroAlumno<AsistenciaAlumno> cartero = new CarteroAlumno<AsistenciaAlumno>("faltas de asistencia", Carta.TIPO_CARTA_AVISO_FALTAS) {

                @Override
                protected void addDatosExtra(Map<String, Object> data, AsistenciaAlumno a, Carta carta) {
                    ca.addDatosExtra(data, a);
                }
            };
            cartero.addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                    if ("error".equals(evt.getPropertyName())) {
                        JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), Str.noNulo(evt.getNewValue()), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            cartero.setCargarAsistenciaTotal(false);//La asignamos a mano nosotros
            return cartero.enviar(asistencia);
        }

        @Override
        protected void succeeded(Boolean result) {
            if (result) {
                setMessage("Cartas impresas/enviadas correctamente.");
                JOptionPane.showMessageDialog(panelCentral, "Cartas impresas/enviadas correctamente", "Enviar", JOptionPane.INFORMATION_MESSAGE);
            } else {
                setMessage("Ha habido algún error enviando las notificaciones.");
            }
        }
    }

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task enviarSeleccionados() {
        return new EnviarSeleccionadosTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class EnviarSeleccionadosTask extends org.jdesktop.application.Task<Boolean, Void> {

        ArrayList<AsistenciaAlumno> asistencia = new ArrayList<AsistenciaAlumno>();

        EnviarSeleccionadosTask(org.jdesktop.application.Application app) {
            super(app);
            int[] rows = tabla.getSelectedRows();
            for (int i : rows) {
                AsistenciaAlumno a = modelo.getElemento(tabla.convertRowIndexToModel(i));
                asistencia.add(a);
            }
        }

        @Override
        protected Boolean doInBackground() {
            setMessage("Cargando asistencia...");
            setProgress(0, 0, asistencia.size());
            //Ahora vamos carta por carta preparando y enviado.
            GregorianCalendar ini = Fechas.toGregorianCalendar(tfFechaDesde.getDate());
            GregorianCalendar fin = Fechas.toGregorianCalendar(tfFechaHasta.getDate());
            if (ini == null) {
                ini = new GregorianCalendar();
                ini.set(MaimonidesApp.getApplication().getAnoEscolar().getAno(), GregorianCalendar.SEPTEMBER, 1);
            }
            if (fin == null) {
                fin = new GregorianCalendar();
            }
            final CarteroAsistenciaAlumo ca = new CarteroAsistenciaAlumo(ini, fin);
            CarteroAlumno<AsistenciaAlumno> cartero = new CarteroAlumno<AsistenciaAlumno>("faltas de asistencia", Carta.TIPO_CARTA_AVISO_FALTAS) {

                @Override
                protected void addDatosExtra(Map<String, Object> data, AsistenciaAlumno a, Carta carta) {
                    ca.addDatosExtra(data, a);
                }
            };
            cartero.addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                    if ("error".equals(evt.getPropertyName())) {
                        JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), Str.noNulo(evt.getNewValue()), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            cartero.setCargarAsistenciaTotal(false);//La asignamos a mano nosotros
            return cartero.enviar(asistencia);
        }

        @Override
        protected void succeeded(Boolean result) {
            if (result) {
                setMessage("Notificaciónes impresas/enviadas correctamente.");
                JOptionPane.showMessageDialog(panelCentral, "Notificaciónes impresas/enviadas correctamente", "Enviar", JOptionPane.INFORMATION_MESSAGE);
            } else {
                setMessage("Ha habido algún error enviando las notificaciones.");
            }
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

        barraHerramientas = new javax.swing.JToolBar();
        bActualizar = new javax.swing.JButton();
        bEnviar = new javax.swing.JButton();
        bEnviarSeleccionados = new javax.swing.JButton();
        panelCentral = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabla = new org.jdesktop.swingx.JXTable();
        panelFiltro = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        tfFechaDesde = new org.jdesktop.swingx.JXDatePicker();
        jLabel2 = new javax.swing.JLabel();
        tfFechaHasta = new org.jdesktop.swingx.JXDatePicker();
        panelFiltroFaltas1 = new com.codeko.apps.maimonides.partes.informes.PanelFiltroFaltas();
        panelFiltroFaltas2 = new com.codeko.apps.maimonides.partes.informes.PanelFiltroFaltas();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        cbCursos1 = new com.codeko.apps.maimonides.cursos.CbCursos();
        cbGrupos1 = new com.codeko.apps.maimonides.cursos.CbGrupos();

        setName("maimonides.paneles.faltas.informes.listado_asistencia"); // NOI18N
        addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                formAncestorAdded(evt);
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });
        setLayout(new java.awt.BorderLayout());

        barraHerramientas.setFloatable(false);
        barraHerramientas.setRollover(true);
        barraHerramientas.setName("barraHerramientas"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getActionMap(PanelCartasAsistencia.class, this);
        bActualizar.setAction(actionMap.get("actualizar")); // NOI18N
        bActualizar.setFocusable(false);
        bActualizar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bActualizar.setName("bActualizar"); // NOI18N
        bActualizar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraHerramientas.add(bActualizar);

        bEnviar.setAction(actionMap.get("enviar")); // NOI18N
        bEnviar.setFocusable(false);
        bEnviar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bEnviar.setName("bEnviar"); // NOI18N
        bEnviar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraHerramientas.add(bEnviar);

        bEnviarSeleccionados.setAction(actionMap.get("enviarSeleccionados")); // NOI18N
        bEnviarSeleccionados.setFocusable(false);
        bEnviarSeleccionados.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bEnviarSeleccionados.setName("bEnviarSeleccionados"); // NOI18N
        bEnviarSeleccionados.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraHerramientas.add(bEnviarSeleccionados);

        add(barraHerramientas, java.awt.BorderLayout.PAGE_START);

        panelCentral.setName("panelCentral"); // NOI18N
        panelCentral.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        tabla.setModel(modelo);
        tabla.setColumnControlVisible(true);
        tabla.setName("tabla"); // NOI18N
        jScrollPane1.setViewportView(tabla);

        panelCentral.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        panelFiltro.setName("panelFiltro"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getResourceMap(PanelCartasAsistencia.class);
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        tfFechaDesde.setName("tfFechaDesde"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        tfFechaHasta.setName("tfFechaHasta"); // NOI18N

        panelFiltroFaltas1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("panelFiltroFaltas1.border.title"))); // NOI18N
        panelFiltroFaltas1.setName("panelFiltroFaltas1"); // NOI18N
        panelFiltroFaltas1.setTipo(FiltroAsistenciaAlumno.TIPO_FALTAS);

        panelFiltroFaltas2.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("panelFiltroFaltas2.border.title"))); // NOI18N
        panelFiltroFaltas2.setName("panelFiltroFaltas2"); // NOI18N
        panelFiltroFaltas2.setTipo(FiltroAsistenciaAlumno.TIPO_DIAS_COMPLETOS);

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        cbCursos1.setComboGrupos(cbGrupos1);
        cbCursos1.setName("cbCursos1"); // NOI18N

        cbGrupos1.setName("cbGrupos1"); // NOI18N

        javax.swing.GroupLayout panelFiltroLayout = new javax.swing.GroupLayout(panelFiltro);
        panelFiltro.setLayout(panelFiltroLayout);
        panelFiltroLayout.setHorizontalGroup(
            panelFiltroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFiltroLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelFiltroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelFiltroFaltas1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelFiltroLayout.createSequentialGroup()
                        .addGroup(panelFiltroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelFiltroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelFiltroLayout.createSequentialGroup()
                                .addComponent(tfFechaDesde, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(33, 33, 33)
                                .addGroup(panelFiltroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(panelFiltroLayout.createSequentialGroup()
                                        .addComponent(jLabel2)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(tfFechaHasta, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(panelFiltroFaltas2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(panelFiltroLayout.createSequentialGroup()
                                .addComponent(cbCursos1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cbGrupos1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(298, Short.MAX_VALUE))
        );
        panelFiltroLayout.setVerticalGroup(
            panelFiltroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFiltroLayout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(panelFiltroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(cbCursos1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(cbGrupos1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelFiltroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(panelFiltroLayout.createSequentialGroup()
                        .addGroup(panelFiltroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(jLabel1)
                            .addComponent(tfFechaDesde, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelFiltroFaltas1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelFiltroLayout.createSequentialGroup()
                        .addGroup(panelFiltroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(jLabel2)
                            .addComponent(tfFechaHasta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelFiltroFaltas2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelCentral.add(panelFiltro, java.awt.BorderLayout.PAGE_START);

        add(panelCentral, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void formAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_formAncestorAdded
//        if (!Beans.isDesignTime() && !isCargado()) {
//            MaimonidesUtil.ejecutarTask(this, "actualizar");
//        }
    }//GEN-LAST:event_formAncestorAdded
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bActualizar;
    private javax.swing.JButton bEnviar;
    private javax.swing.JButton bEnviarSeleccionados;
    private javax.swing.JToolBar barraHerramientas;
    private com.codeko.apps.maimonides.cursos.CbCursos cbCursos1;
    private com.codeko.apps.maimonides.cursos.CbGrupos cbGrupos1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel panelCentral;
    private javax.swing.JPanel panelFiltro;
    private com.codeko.apps.maimonides.partes.informes.PanelFiltroFaltas panelFiltroFaltas1;
    private com.codeko.apps.maimonides.partes.informes.PanelFiltroFaltas panelFiltroFaltas2;
    private org.jdesktop.swingx.JXTable tabla;
    private org.jdesktop.swingx.JXDatePicker tfFechaDesde;
    private org.jdesktop.swingx.JXDatePicker tfFechaHasta;
    // End of variables declaration//GEN-END:variables
}
