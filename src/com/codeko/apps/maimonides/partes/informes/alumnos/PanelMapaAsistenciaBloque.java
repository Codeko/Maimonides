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
 * PanelMapaAsistenciaBloque.java
 *
 * Created on 04-sep-2009, 14:50:48
 */
package com.codeko.apps.maimonides.partes.informes.alumnos;

import com.codeko.apps.maimonides.ICargable;
import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.LineaParteAlumno;
import com.codeko.apps.maimonides.elementos.ParteFaltas;
import java.awt.GridBagConstraints;
import java.awt.event.MouseEvent;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Vector;
import javax.swing.JLabel;
import com.codeko.apps.maimonides.partes.AsistenciaAlumno;
import com.codeko.util.Fechas;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.SwingConstants;

/**
 *
 * @author Codeko
 */
public class PanelMapaAsistenciaBloque extends javax.swing.JPanel implements ICargable {

    Vector<Vector<JLabel>> dias = new Vector<Vector<JLabel>>();
    int diaSemana = 0;
    GregorianCalendar fecha = null;
    boolean cargado = false;
    Alumno alumno = null;
    MouseAdapter gestorClic = new MouseAdapter() {

        @Override
        public void mouseClicked(MouseEvent e) {
            Object source = e.getSource();
            if (source instanceof JComponent) {
                if (((JComponent) source).getClientProperty("l") instanceof LineaParteAlumno) {
                    firePropertyChange("asistenciaSeleccionada", null, (LineaParteAlumno) ((JComponent) source).getClientProperty("l"));
                }
            }
        }
    };

    public PanelMapaAsistenciaBloque() {
        this(GregorianCalendar.MONDAY);
    }

    public PanelMapaAsistenciaBloque(int diaSemana) {
        initComponents();
        GregorianCalendar cTmp = new GregorianCalendar();
        cTmp.set(GregorianCalendar.DAY_OF_WEEK, diaSemana);
        lCab.setText(cTmp.getDisplayName(GregorianCalendar.DAY_OF_WEEK, GregorianCalendar.LONG, Locale.getDefault()));
        cTmp = null;
        setDiaSemana(diaSemana);
        for (int y = 0; y < 6; y++) {
            Vector<JLabel> actual = new Vector<JLabel>();
            for (int x = 0; x < 7; x++) {
                JLabel l = new JLabel(" ");
                l.setOpaque(true);
                l.setBackground(Color.WHITE);
                if (y > 0 && x > 0) {
                    l.setBorder(BorderFactory.createLineBorder(Color.gray));
                }
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setPreferredSize(new Dimension(15, 15));
                GridBagConstraints gridBagConstraints = new GridBagConstraints();
                gridBagConstraints.gridx = x;
                gridBagConstraints.gridy = y + 1;
                gridBagConstraints.insets = new java.awt.Insets(1, 1, 1, 1);
                add(l, gridBagConstraints);
                actual.add(l);
            }
            getDias().add(actual);
        }
        //La primera fila tíene las horas
        Vector<JLabel> primera = getDias().firstElement();
        primera.firstElement().setText(" ");
        for (int i = 1; i < 7; i++) {
            primera.elementAt(i).setText(i + "");
        }

    }

    public Vector<Vector<JLabel>> getDias() {
        return dias;
    }

    public int getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(int diaSemana) {
        this.diaSemana = diaSemana;
    }

    public GregorianCalendar getFecha() {
        return fecha;
    }

    public void setFecha(GregorianCalendar fechaX) {
        this.fecha = (GregorianCalendar) fechaX.clone();
        cargarDatos();
    }

    public Alumno getAlumno() {
        return alumno;
    }

    public void setAlumno(Alumno alumno) {
        this.alumno = alumno;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        lCab = new javax.swing.JLabel();

        setName("Form"); // NOI18N
        setOpaque(false);
        setLayout(new java.awt.GridBagLayout());

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getResourceMap(PanelMapaAsistenciaBloque.class);
        lCab.setText(resourceMap.getString("lCab.text")); // NOI18N
        lCab.setName("lCab"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        add(lCab, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lCab;
    // End of variables declaration//GEN-END:variables

    @Override
    public void cargar() {
        cargarDatos();
        setCargado(true);
    }

    private void cargarDatos() {
        GregorianCalendar f=(GregorianCalendar)getFecha().clone();
        f.set(GregorianCalendar.DAY_OF_MONTH, 1);
        //Tenemos que buscar el primer día de la fecha que se corresponde con el de la semana
        //y a partir de ahí avanzar sin salirnos del mes
        int mes = f.get(GregorianCalendar.MONTH);
        while (f.get(GregorianCalendar.DAY_OF_WEEK) != getDiaSemana()) {
            f.add(GregorianCalendar.DAY_OF_MONTH, 1);
        }
        int wm = f.get(GregorianCalendar.WEEK_OF_MONTH);
        for (int i = 1; i < 6; i++) {
            //Nos saltamos hasta la semana del més correspondiente
            if (i < wm) {
                getDias().elementAt(i).firstElement().setText(" ");
            } else if (f.get(GregorianCalendar.MONTH) == mes) {
                getDias().elementAt(i).firstElement().setText(f.get(GregorianCalendar.DAY_OF_MONTH) + "");
                cargarAsistencia(getDias().elementAt(i), f);
                f.add(GregorianCalendar.DAY_OF_MONTH, 7);
            } else {
                getDias().elementAt(i).firstElement().setText(" ");
            }
        }
    }

    @Override
    public void vaciar() {
        for (int i = 1; i < 6; i++) {
            Vector<JLabel> v = getDias().elementAt(i);
            for (int x = 1; x < v.size(); x++) {
                v.elementAt(x).setText(" ");
                v.elementAt(x).setBackground(Color.WHITE);
            }
        }
        setAlumno(null);
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

    private void cargarAsistencia(Vector<JLabel> etiquetas, GregorianCalendar fecha) {
        if (getAlumno() != null && getAlumno().getId() != null) {
            AsistenciaAlumno asis = new AsistenciaAlumno(fecha, MaimonidesApp.getApplication().getAnoEscolar(), getAlumno());
            for (int i = 1; i < 7; i++) {
                LineaParteAlumno l = asis.getLineaHora(i);
                if (l != null) {
                    int a = l.getAsistencia();
                    if (a < ParteFaltas.FALTA_INJUSTIFICADA) {
                        etiquetas.elementAt(i).setText(" ");
                        etiquetas.elementAt(i).setBackground(Color.WHITE);
                    } else {
                        String texto = ParteFaltas.getCodigoTipoFalta(a);
                        Color c = ParteFaltas.getColorTipoFalta(a);
                        texto = "<html><b>" + texto + "</b>";
                        etiquetas.elementAt(i).putClientProperty("l", l);
                        etiquetas.elementAt(i).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        etiquetas.elementAt(i).addMouseListener(gestorClic);
                        etiquetas.elementAt(i).setBackground(c);
                        etiquetas.elementAt(i).setText(texto);
                    }
                } else {
                    etiquetas.elementAt(i).setText(" ");
                    etiquetas.elementAt(i).setBackground(Color.WHITE);
                }
            }
        }
    }
}
