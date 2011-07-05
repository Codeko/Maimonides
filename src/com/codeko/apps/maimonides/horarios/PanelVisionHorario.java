/*
 * PanelVisionHorario.java
 *
 * Created on 03-mar-2009, 18:30:39
 */
package com.codeko.apps.maimonides.horarios;

import com.codeko.apps.maimonides.ICargable;
import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesBean;
import com.codeko.apps.maimonides.MaimonidesUtil;
import com.codeko.apps.maimonides.alumnos.IFiltrableAlumno;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.Horario;

import com.codeko.apps.maimonides.elementos.Profesor;
import com.codeko.apps.maimonides.elementos.Unidad;
import com.codeko.apps.maimonides.horarios.impresion.HorarioImprimible;
import com.codeko.apps.maimonides.horarios.impresion.ImpresionHorarios;
import com.codeko.apps.maimonides.materias.ControlMatriculas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;

/**
 *
 * @author Codeko
 */
public class PanelVisionHorario extends javax.swing.JPanel implements ICargable, IFiltrableAlumno {

    boolean cargado = false;
    boolean editable = false;
    PanelVisionHorario auto = this;
    ArrayList<ArrayList<PanelCeldaHorario>> datos = new ArrayList<ArrayList<PanelCeldaHorario>>();
    Alumno alumno = null;
    Profesor profesor = null;
    Unidad unidad = null;
    PanelCabeceraScrollHorario cab = new PanelCabeceraScrollHorario();
    PanelLateralScrollHorario lat = new PanelLateralScrollHorario();
    public static int DIAS_SEMANA = 5;
    public static int HORAS_DIA = 6;
    PropertyChangeListener listenerBloques = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if ("bloqueHorarioGuardado".equals(evt.getPropertyName())) {
                //vemos si corresponde con el visor
                addBloqueSiProcede((BloqueHorario) evt.getNewValue());
            } else if ("bloqueHorarioEliminado".equals(evt.getPropertyName())) {
                BloqueHorario b = (BloqueHorario) evt.getNewValue();
                bloqueHorarioBorrado(b);
            } else if ("bloqueHorarioMovido".equals(evt.getPropertyName())) {
                BloqueHorario b = (BloqueHorario) evt.getNewValue();
                moverBloque(b);
            } else if ("focoEnBloqueGanado".equals(evt.getPropertyName()) || "focoEnBloquePerdido".equals(evt.getPropertyName())) {
                firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            }
        }
    };

    public PanelVisionHorario(boolean editable) {
        setEditable(editable);
        initComponents();
        init();
    }

    public PanelVisionHorario() {
        initComponents();
        init();
    }

    private void init() {
        scroll.setColumnHeaderView(cab);
        scroll.setRowHeaderView(lat);
        JLabel l = new JLabel("Día");
        l.setBorder(BorderFactory.createLineBorder(Color.black));
        l.setHorizontalAlignment(SwingConstants.CENTER);
        l.setFont(l.getFont().deriveFont(Font.BOLD));
        l.setFont(l.getFont().deriveFont(12f));
        scroll.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, l);
        for (int hora = 0; hora < HORAS_DIA; hora++) {
            ArrayList<PanelCeldaHorario> v = new ArrayList<PanelCeldaHorario>();
            for (int dia = 0; dia < DIAS_SEMANA; dia++) {
                PanelCeldaHorario p = new PanelCeldaHorario(this, dia + 1, hora + 1);
                pDatos.add(p);
                v.add(p);
            }
            getDatos().add(v);
        }
        pDatos.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                refrescarScroll();
            }
        });
    }

    public void imprimir(MaimonidesBean bean) {
        ImpresionHorarios imp = new ImpresionHorarios();
        HorarioImprimible h = new HorarioImprimible(getAlumno(), getProfesor(), getUnidad());
        ArrayList<HorarioImprimible> horarios = new ArrayList<HorarioImprimible>();
        horarios.add(h);
        boolean multiple = getProfesor() == null && getAlumno() == null;
        imp.imprimirHorarios(bean, horarios, multiple);
    }

    public boolean isEditable() {
        return editable;
    }

    public final void setEditable(boolean editable) {
        this.editable = editable;
    }

    public void resetearConflictos() {
        MaimonidesApp.getApplication().getContext().getTaskService().execute(revisarConflictos());
    }

    public void moverBloque(BloqueHorario bloque) {
        PanelCeldaHorario o = getPanel(bloque.getDiaAnterior(), bloque.getHoraAnterior());
        o.getHorarios().remove(bloque);
        o.reprocesar();
        o = getPanel(bloque.getDia(), bloque.getHora());
        o.getHorarios().add(bloque);
        o.reprocesar();
        o.marcar(bloque);
        resetearConflictos();
    }

    public void bloqueHorarioBorrado(BloqueHorario bloque) {
        PanelCeldaHorario o = getPanel(bloque.getDia(), bloque.getHora());
        o.getHorarios().remove(bloque);
        bloque.removePropertyChangeListener(listenerBloques);
        resetearConflictos();
        o.reprocesar();
    }

    public Alumno getAlumno() {
        return alumno;
    }

    @Override
    public void setAlumno(Alumno alumno) {
        this.alumno = alumno;
    }

    public Profesor getProfesor() {
        return profesor;
    }

    public void setProfesor(Profesor profesor) {
        this.profesor = profesor;
        PanelCeldaHorario.setProfesorActivo(profesor);
    }

    public Unidad getUnidad() {
        return unidad;
    }

    public void setUnidad(Unidad unidad) {
        this.unidad = unidad;
        PanelCeldaHorario.setUnidadActiva(unidad);
    }

    public ArrayList<ArrayList<PanelCeldaHorario>> getDatos() {
        return datos;
    }

    public PanelCeldaHorario getPanel(int dia, int hora) {
        try {
            return getDatos().get(hora - 1).get(dia - 1);

        } catch (Exception e) {
            Logger.getLogger(PanelVisionHorario.class.getName()).log(Level.WARNING, "Intentando recuperar Dia/Hora inv\u00e1lido: dia:{0} hora:{1}. Error: {2}", new Object[]{dia, hora, e.getLocalizedMessage()});
        }
        return null;
    }

    @Override
    public boolean isCargado() {
        return cargado;
    }

    void addBloqueSiProcede(BloqueHorario b) {
        boolean add = true;
        if (getProfesor() != null) {
            add = getProfesor().equals(b.getProfesor());
        }
        if (add) {
            if (getUnidad() != null) {
                add = b.getUnidades().contains(getUnidad());
            }
        }
        if (add) {
            if (getAlumno() != null) {
                add = b.getUnidades().contains(getAlumno().getUnidad());
            }
        }
        if (add) {
            b.addPropertyChangeListener(listenerBloques);
            PanelCeldaHorario o = getPanel(b.getDia(), b.getHora());
            o.getHorarios().add(b);
            o.reprocesar();
            o.marcar(b);
            resetearConflictos();
        }
    }

    @Override
    public void setCargado(boolean cargado) {
        this.cargado = cargado;
    }

    public void cargarHorario(Alumno a) {
        setAlumno(a);
        cargar();
    }

    @Override
    public void cargar() {
        if (!isCargado()) {
            MaimonidesUtil.ejecutarTask(this, "cargarDatos");
        }
    }

    @Override
    public void vaciar() {
        for (ArrayList<PanelCeldaHorario> vp : getDatos()) {
            for (PanelCeldaHorario p : vp) {
                p.limpiar();
            }
        }
        setCargado(false);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pOpciones = new javax.swing.JPanel();
        cbMostrar = new javax.swing.JComboBox();
        jLabel8 = new javax.swing.JLabel();
        lProceso = new com.codeko.swing.CdkProcesoLabel();
        scroll = new javax.swing.JScrollPane();
        pDatos = new javax.swing.JPanel();

        setName("Form"); // NOI18N

        pOpciones.setName("pOpciones"); // NOI18N

        cbMostrar.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Código y Nombre", "Nombre", "Código" }));
        cbMostrar.setSelectedIndex(2);
        cbMostrar.setName("cbMostrar"); // NOI18N
        cbMostrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbMostrarActionPerformed(evt);
            }
        });

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getResourceMap(PanelVisionHorario.class);
        jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N

        lProceso.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lProceso.setText(resourceMap.getString("lProceso.text")); // NOI18N
        lProceso.setName("lProceso"); // NOI18N
        lProceso.setProcesando(false);

        javax.swing.GroupLayout pOpcionesLayout = new javax.swing.GroupLayout(pOpciones);
        pOpciones.setLayout(pOpcionesLayout);
        pOpcionesLayout.setHorizontalGroup(
            pOpcionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pOpcionesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lProceso, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 35, Short.MAX_VALUE)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbMostrar, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        pOpcionesLayout.setVerticalGroup(
            pOpcionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pOpcionesLayout.createSequentialGroup()
                .addGroup(pOpcionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(lProceso, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbMostrar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        scroll.setName("scroll"); // NOI18N

        pDatos.setName("pDatos"); // NOI18N
        pDatos.setLayout(new java.awt.GridLayout(HORAS_DIA, DIAS_SEMANA, 5, 5));
        scroll.setViewportView(pDatos);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(scroll, javax.swing.GroupLayout.DEFAULT_SIZE, 427, Short.MAX_VALUE)
            .addComponent(pOpciones, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(scroll, javax.swing.GroupLayout.DEFAULT_SIZE, 385, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pOpciones, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void cbMostrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbMostrarActionPerformed
        for (ArrayList<PanelCeldaHorario> vp : getDatos()) {
            for (PanelCeldaHorario p : vp) {
                p.setModoMostrar(cbMostrar.getSelectedIndex());
            }
        }
        refrescarScroll();
    }

    private void refrescarScroll() {
        cab.setPreferredSize(new Dimension(pDatos.getWidth(), 30));
        lat.setPreferredSize(new Dimension(45, pDatos.getHeight()));
        scroll.updateUI();
        scroll.getColumnHeader().updateUI();
        scroll.getRowHeader().updateUI();
    }

    @Action
    public Task cargarDatos() {
        return new CargarDatosTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }//GEN-LAST:event_cbMostrarActionPerformed

    private class CargarDatosTask extends org.jdesktop.application.Task<Object, Void> {

        CargarDatosTask(org.jdesktop.application.Application app) {
            super(app);
            vaciar();
        }

        @Override
        protected Object doInBackground() throws Exception {
            if (getAlumno() != null || getProfesor() != null || getUnidad() != null) {
                lProceso.setProcesando(true);
                lProceso.setText("Cargando horario...");
                ArrayList<BloqueHorario> horarios = Horario.getHorarios(getAlumno(), getProfesor(), getUnidad(), null, null);
                for (BloqueHorario bh : horarios) {
                    if (getAlumno() != null && !bh.getMaterias().isEmpty() && !ControlMatriculas.isMatriculado(getAlumno(), bh.getMaterias())) {
                        continue;
                    }
                    bh.addPropertyChangeListener(listenerBloques);
                    PanelCeldaHorario pch = getPanel(bh.getDia(), bh.getHora());
                    if (pch != null) {
                        pch.getHorarios().add(bh);
                    }
                }
                setCargado(true);
                for (ArrayList<PanelCeldaHorario> vp : getDatos()) {
                    for (PanelCeldaHorario p : vp) {
                        p.setForzarMostrarUnidades(getAlumno() == null);
                        p.setModoMostrar(cbMostrar.getSelectedIndex());
                    }
                }
                lProceso.setProcesando(false);
                lProceso.setIcon(null);
                lProceso.setText("");
            } else {
                vaciar();
            }
            return null;  // return your result
        }

        @Override
        protected void succeeded(Object result) {
            MaimonidesApp.getApplication().getContext().getTaskService().execute(revisarConflictos());
        }
    }

    @Action
    public Task revisarConflictos() {
        return new RevisarConflictosTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class RevisarConflictosTask extends org.jdesktop.application.Task<Object, Void> {

        RevisarConflictosTask(org.jdesktop.application.Application app) {
            super(app);
            setMessage("Revisando conflictos horarios...");
            lProceso.setProcesando(true);
            lProceso.setText("Revisando conflictos horarios...");
        }

        @Override
        protected Object doInBackground() {

            for (ArrayList<PanelCeldaHorario> vp : getDatos()) {
                if (!isCargado()) {
                    break;
                }
                for (PanelCeldaHorario p : vp) {
                    if (!isCargado()) {
                        break;
                    }
                    p.revisarConflictos();
                }
            }
            return null;  // return your result
        }

        @Override
        protected void succeeded(Object result) {
            setMessage("Conflictos horarios revisados...");
            lProceso.setProcesando(false);
            lProceso.setIcon(null);
            lProceso.setText("");
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cbMostrar;
    private javax.swing.JLabel jLabel8;
    private com.codeko.swing.CdkProcesoLabel lProceso;
    private javax.swing.JPanel pDatos;
    private javax.swing.JPanel pOpciones;
    private javax.swing.JScrollPane scroll;
    // End of variables declaration//GEN-END:variables
}
