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



/*
 * PanelMapaAsistencia.java
 *
 * Created on 04-sep-2009, 14:47:30
 */
package com.codeko.apps.maimonides.partes.informes.alumnos;

import com.codeko.apps.maimonides.ICargable;
import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.alumnos.IFiltrableAlumno;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.LineaParteAlumno;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;

/**
 *
 * @author Codeko
 */
public class PanelMapaAsistencia extends javax.swing.JPanel implements ICargable, Printable, IFiltrableAlumno {

    Alumno alumno = null;
    boolean cargado = false;
    GregorianCalendar desde = null;
    GregorianCalendar hasta = null;
    ArrayList<PanelMapaAsistenciaMes> meses = new ArrayList<PanelMapaAsistenciaMes>();
    Task tareaCarga = null;
    PropertyChangeListener listenerMes = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if ("asistenciaSeleccionada".equals(evt.getPropertyName())) {
                panelDetalleAsistencia1.setDatos((LineaParteAlumno) evt.getNewValue());
            }
        }
    };

    public PanelMapaAsistencia() {
        this(null, null);
    }

    /** Creates new form PanelMapaAsistencia */
    public PanelMapaAsistencia(GregorianCalendar desde, GregorianCalendar hasta) {
        if (desde == null) {
            desde = new GregorianCalendar(MaimonidesApp.getApplication().getAnoEscolar().getAno(), GregorianCalendar.SEPTEMBER, 1);
            hasta = new GregorianCalendar(MaimonidesApp.getApplication().getAnoEscolar().getAno() + 1, GregorianCalendar.JUNE, 1);
        }
        initComponents();
        setDesde(desde);
        setHasta(hasta);
        while (desde.compareTo(hasta) <= 0) {
            PanelMapaAsistenciaMes pm = new PanelMapaAsistenciaMes(desde);
            pm.addPropertyChangeListener(listenerMes);
            panelMeses.add(pm);
            meses.add(pm);
            desde.add(GregorianCalendar.MONTH, 1);
        }
        bImprimir.setVisible(false);
        panelDetalleAsistencia1.setVisible(false);

    }

    public GregorianCalendar getDesde() {
        return desde;
    }

    public final void setDesde(GregorianCalendar desde) {
        this.desde = desde;
    }

    public GregorianCalendar getHasta() {
        return hasta;
    }

    public final void setHasta(GregorianCalendar hasta) {
        this.hasta = hasta;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        panelMeses = new javax.swing.JPanel();
        lCargando = new com.codeko.swing.CdkProcesoLabel();
        jToolBar1 = new javax.swing.JToolBar();
        bImprimir = new javax.swing.JButton();
        panelDetalleAsistencia1 = new com.codeko.apps.maimonides.partes.informes.alumnos.PanelDetalleAsistencia();

        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getResourceMap(PanelMapaAsistencia.class);
        panelMeses.setBackground(resourceMap.getColor("panelMeses.background")); // NOI18N
        panelMeses.setName("panelMeses"); // NOI18N
        panelMeses.setLayout(new java.awt.GridLayout(0, 1));

        lCargando.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lCargando.setText(resourceMap.getString("lCargando.text")); // NOI18N
        lCargando.setOK(false);
        lCargando.setName("lCargando"); // NOI18N
        lCargando.setProcesando(true);
        panelMeses.add(lCargando);

        jScrollPane1.setViewportView(panelMeses);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        jToolBar1.setName("jToolBar1"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getActionMap(PanelMapaAsistencia.class, this);
        bImprimir.setAction(actionMap.get("imprimir")); // NOI18N
        bImprimir.setFocusable(false);
        bImprimir.setName("bImprimir"); // NOI18N
        jToolBar1.add(bImprimir);

        add(jToolBar1, java.awt.BorderLayout.PAGE_START);

        panelDetalleAsistencia1.setName("panelDetalleAsistencia1"); // NOI18N
        add(panelDetalleAsistencia1, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bImprimir;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar jToolBar1;
    private com.codeko.swing.CdkProcesoLabel lCargando;
    private com.codeko.apps.maimonides.partes.informes.alumnos.PanelDetalleAsistencia panelDetalleAsistencia1;
    private javax.swing.JPanel panelMeses;
    // End of variables declaration//GEN-END:variables

    public Alumno getAlumno() {
        return alumno;
    }

    @Override
    public void setAlumno(Alumno alumno) {
        this.alumno = alumno;
    }

    @Override
    public void cargar() {
        if (getAlumno() != null) {
            vaciar();
            tareaCarga = cargarAsistencias();
            MaimonidesApp.getApplication().getContext().getTaskService().execute(tareaCarga);

        }
        setCargado(true);
    }

    @Override
    public void vaciar() {
        for (PanelMapaAsistenciaMes pm : meses) {
            pm.setAlumno(null);
            pm.vaciar();
        }
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

    @Action
    public Task cargarAsistencias() {
        return new CargarAsistenciasTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int page) throws PrinterException {
        if (page > 0) {
            return NO_SUCH_PAGE;
        }

        Graphics2D g2d = (Graphics2D) graphics;
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

        this.printAll(graphics);
        return PAGE_EXISTS;
    }

    private class CargarAsistenciasTask extends org.jdesktop.application.Task<Object, Void> {

        CargarAsistenciasTask(org.jdesktop.application.Application app) {
            super(app);
            panelMeses.removeAll();
            panelMeses.add(lCargando);
        }

        @Override
        protected Object doInBackground() {
            for (PanelMapaAsistenciaMes pm : meses) {
                pm.setAlumno(getAlumno());
                pm.cargar();
            }
            return null;
        }

        @Override
        protected void succeeded(Object result) {
            panelMeses.removeAll();
            for (PanelMapaAsistenciaMes p : meses) {
                panelMeses.add(p);
            }

        }
    }

    @Action
    public void imprimir() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(this);
        boolean ok = job.printDialog();
        if (ok) {
            try {
                job.print();
            } catch (PrinterException ex) {
                /* The job did not successfully complete */
            }
        }

    }
}
