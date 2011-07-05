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
 * PanelEditorMensajeDigitalizacion.java
 *
 * Created on 22 de octubre de 2008, 13:18
 */
package com.codeko.apps.maimonides.digitalizacion;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.elementos.LineaParteAlumno;
import com.codeko.apps.maimonides.elementos.ParteFaltas;
import com.codeko.apps.maimonides.partes.AsistenciaAlumno;
import com.codeko.apps.maimonides.partes.PanelListaAlumnosParte;
import com.codeko.util.Fechas;
import com.codeko.util.Num;
import com.codeko.util.Obj;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;
import org.jdesktop.swingx.VerticalLayout;

/**
 *
 * @author  Codeko
 */
public class PanelEditorMensajeDigitalizacion extends javax.swing.JPanel {

    org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getResourceMap(PanelEditorMensajeDigitalizacion.class);
    MensajeDigitalizacion msg = null;
    boolean cargando = false;

    public boolean isCargando() {
        return cargando;
    }

    public synchronized void setCargando(boolean cargando) {
        this.cargando = cargando;
    }

    /** Creates new form PanelEditorMensajeDigitalizacion */
    public PanelEditorMensajeDigitalizacion() {
        initComponents();
        cargar(null);
        MensajeDigitalizacion.getControl().addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("borrarPorIdParte".equals(evt.getPropertyName())) {
                    if (msg != null && msg.getParte() != null && msg.getParte().getId() == Num.getInt(evt.getNewValue())) {
                        firePropertyChange("borrado", null, msg);
                        cargar(null);
                    }
                }
            }
        });
        setLayout(new VerticalLayout(10));
    }

    public final void cargar(MensajeDigitalizacion m) {
        setCargando(true);
        msg = m;

        if (m == null) {
            lMensaje.setText("<html>Seleccione una advertencia de digitalización para resolverla.");
            lIcono.setIcon(resourceMap.getIcon("lIcono.icon")); // NOI18N
            setParteTieneImagen(false);
            panelOpciones.setVisible(false);
            lInfoNoHayOpciones.setVisible(false);
            panelVisorCasilla1.limpiar();
            panelVisorCasilla1.setVisible(false);
            panelDescripcion.setVisible(false);
            setHayIdParte(false);
        } else {
            asignarMensaje(m);
            if (m.getLineaParteAlumno() != null) {
                panelVisorCasilla1.setVisible(true);
                panelVisorCasilla1.mostrar(m);
            } else {
                panelVisorCasilla1.setVisible(false);
            }
            switch (m.getTipo()) {
                case MensajeDigitalizacion.TIPO_ADVERTENCIA:
                    lIcono.setIcon(resourceMap.getIcon("advertencia.icon"));
                    break;
                case MensajeDigitalizacion.TIPO_ERROR:
                case MensajeDigitalizacion.TIPO_PARTE_FALLIDO:
                    lIcono.setIcon(resourceMap.getIcon("error.icon"));
                    break;
                case MensajeDigitalizacion.TIPO_IGNORADO:
                    lIcono.setIcon(resourceMap.getIcon("ignorado.icon"));
                    break;
                case MensajeDigitalizacion.TIPO_NO_EXISTE:
                    lIcono.setIcon(resourceMap.getIcon("noexiste.icon"));
                    break;
                case MensajeDigitalizacion.TIPO_OK:
                    lIcono.setIcon(resourceMap.getIcon("ok.icon"));
                    break;
            }

            setParteTieneImagen((m.getIdImagen() != null && m.getIdImagen() > 0) || (m.getParte() != null && m.getParte().hayImagen()) || (m.getParteErroneo() != null && m.getParteErroneo().exists()));
            panelOpciones.setVisible(isMensajeConOpciones(m));

            lTextoAsignarAsistencia.setVisible(isMostrarOpcionesAsistencia(m));
            bA.setVisible(isMostrarOpcionesAsistencia(m));
            bI.setVisible(isMostrarOpcionesAsistencia(m));
            bR.setVisible(isMostrarOpcionesAsistencia(m));
            bE.setVisible(isMostrarOpcionesAsistencia(m));
            lInfoNoHayOpciones.setVisible(!panelOpciones.isVisible());
            setHayIdParte(m.getParte() != null);
        }
        setCargando(false);
    }

    public static boolean isMensajeConOpciones(MensajeDigitalizacion m) {
        return m.getTipo() == MensajeDigitalizacion.TIPO_ADVERTENCIA || m.getTipo() == MensajeDigitalizacion.TIPO_PARTE_FALLIDO || m.getTipo() == MensajeDigitalizacion.TIPO_NO_EXISTE;
    }

    public static boolean isMostrarOpcionesAsistencia(MensajeDigitalizacion m) {
        boolean mostrar = m.getTipo() == MensajeDigitalizacion.TIPO_ADVERTENCIA;
        if (mostrar) {
            //En ciertos tipos de advertencias no hay opciones
            switch (m.getTipoAdvertencia()) {
                case MensajeDigitalizacion.TIPOA_ERROR_DIGITALIZACION:
                case MensajeDigitalizacion.TIPOA_PARTE_INCORRECTO:
                case MensajeDigitalizacion.TIPOA_SIN_FIRMA:
                    mostrar = false;
            }
        }
        return mostrar;
    }

    private void asignarAsistencia(int asistencia) {
        if (Num.getInt(msg.getId()) > 0) {
            if (asistencia >= 0 && msg.getLineaParteAlumno() != null) {
                msg.getLineaParteAlumno().setAsistencia(asistencia);
                msg.getLineaParteAlumno().guardarAsistencia();
            } else if (asistencia >= 0 && msg.getTipoAdvertencia() == MensajeDigitalizacion.TIPOA_DUDA_SALTOS_ASISTENCIA && msg.getParte() != null && msg.getAlumno() != null) {
                //En este caso tenemos que buscar las asistencias de salto y asignarles la asistencia indicada.
                AsistenciaAlumno asis = new AsistenciaAlumno(msg.getParte(), msg.getAlumno());
                for (Integer i : asis.getHorasSaltosAsistencia()) {
                    LineaParteAlumno lpa = asis.getLineaHora(i);
                    if (lpa != null) {
                        lpa.setAsistencia(asistencia);
                        lpa.guardarAsistencia();
                    }
                }
            }
            //Y ahora borramos el mensaje
            msg.borrar();
        }
        if (msg.getParteErroneo() != null && msg.getParteErroneo().exists()) {
            msg.getParteErroneo().delete();
        }
        if (msg.getMetadatosParte() != null && msg.getMetadatosParte().exists()) {
            msg.getMetadatosParte().delete();
        }
        MensajeDigitalizacion m2 = msg;
        this.cargar(null);
        firePropertyChange("borrado", null, m2);
        m2 = null;
    }

    private void asignarMensaje(MensajeDigitalizacion m) {
        String mensajeHTML = "<html>" + m.getMensaje().replace("\n", "<br/>");
        StringBuilder sb = new StringBuilder("<html>");
        if (m.getParte() != null) {
            sb.append("Fecha:<b> ");
            sb.append(Fechas.format(m.getParte().getFecha()));
            sb.append("</b><br/>");
            sb.append("Parte:<b> ");
            sb.append(m.getParte().getDescripcionObjeto());
            sb.append(" (");
            sb.append(m.getParte().getId());
            sb.append(")");
            sb.append("</b><br/>");
        }

        if (m.getAlumno() != null) {
            sb.append("Alumno:<b> ");
            sb.append(m.getAlumno().getNombreFormateado());
            sb.append("</b><br/>");
        }

        if (m.getHorario() != null) {
            sb.append("Hora:<b> ");
            sb.append(m.getHorario().getHora());
            sb.append("ª</b> ");
            if (m.getLineaParteAlumno() != null) {
                sb.append("&nbsp;&nbsp;Fila:<b> ");
                sb.append(m.getLineaParteAlumno().getPosicion());
                sb.append("ª</b><br/>");
                if (!m.getLineaParteAlumno().isFirmado()) {
                    sb.append("Firmado:<b> ");
                    sb.append("NO");
                    sb.append("</b><br/>");
                }

                sb.append("Actual:<b> ");
                sb.append(ParteFaltas.getNombreTipoFalta(m.getLineaParteAlumno().getAsistencia()));
                sb.append("</b><br/>");
            } else {
                sb.append("<br/>");
            }

        }

        switch (m.getTipoAdvertencia()) {
            case MensajeDigitalizacion.TIPOA_SIN_FIRMA:
                sb.append("Problema:<b> ");
                sb.append("No firmado");
                sb.append("</b><br/>");
                break;

            case MensajeDigitalizacion.TIPOA_FALTA_Y_RETRASO:
                sb.append("Problema:<b> ");
                sb.append("¿Falta o Retraso?");
                sb.append("</b><br/>");
                break;

            case MensajeDigitalizacion.TIPOA_DUDA_MARCA:
                sb.append("Problema:<b> ");
                sb.append("¿Marcado?");
                sb.append("</b><br/>");
                break;

            case MensajeDigitalizacion.TIPOA_DUDA_ANULACION:
                sb.append("Problema:<b> ");
                sb.append("¿Anulado?");
                sb.append("</b><br/>");
                break;

            case MensajeDigitalizacion.TIPOA_DUDA_SALTOS_ASISTENCIA:
                if (m.getParte() != null && m.getAlumno() != null) {
                    AsistenciaAlumno asis = new AsistenciaAlumno(msg.getParte(), msg.getAlumno());
                    sb.append("Faltas:<b>");
                    ArrayList<Integer> saltos = asis.getHorasSaltosAsistencia();
                    for (int i = 0; i
                            < 6; i++) {
                        LineaParteAlumno l = asis.getLineaHora(i + 1);
                        if (l == null) {
                            sb.append(" -");
                        } else {
                            if (saltos.contains(i + 1)) {
                                sb.append(" <font color='red'>");
                                sb.append(ParteFaltas.getCodigoTipoFalta(l.getAsistencia()));
                                sb.append("</font>");
                            } else {
                                sb.append(" ");
                                sb.append(ParteFaltas.getCodigoTipoFalta(l.getAsistencia()));
                            }

                        }
                    }
                    sb.append("</b><br/>");
                }

                sb.append("Problema:<b> ");
                sb.append("¿No marcada falta/s?");
                sb.append("</b><br/>");

                break;

            case MensajeDigitalizacion.TIPOA_PARTE_INCORRECTO:
                sb.append("Problema:<b> ");
                sb.append("Falta en parte incorrecto");
                sb.append("</b><br/>");
                break;

            case MensajeDigitalizacion.TIPOA_ERROR_DIGITALIZACION:
                sb.append("Problema:<b> ");
                sb.append("¿Parte mal escaneado?");
                sb.append("</b><br/>");
                break;

        }


        if (!sb.toString().equals("<html>")) {
            lInfoExtra.setText(mensajeHTML);
            panelDescripcion.setVisible(true);
            lMensaje.setText(sb.toString());
        } else {
            lInfoExtra.setText("");
            panelDescripcion.setVisible(false);
            lMensaje.setText(mensajeHTML);
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

        jPanel1 = new javax.swing.JPanel();
        lMensaje = new javax.swing.JLabel();
        lIcono = new javax.swing.JLabel();
        lInfoNoHayOpciones = new javax.swing.JLabel();
        panelOpciones = new javax.swing.JPanel();
        b4 = new javax.swing.JButton();
        lTextoAsignarAsistencia = new javax.swing.JLabel();
        bA = new javax.swing.JButton();
        bI = new javax.swing.JButton();
        bR = new javax.swing.JButton();
        bE = new javax.swing.JButton();
        panelVisorCasilla1 = new com.codeko.apps.maimonides.digitalizacion.PanelVisorCasilla();
        panelDescripcion = new javax.swing.JPanel();
        lInfoExtra = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        bVer = new javax.swing.JButton();
        bEditarParte = new javax.swing.JButton();
        bAnularDigitalizacion = new javax.swing.JButton();

        setName("Form"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getResourceMap(PanelEditorMensajeDigitalizacion.class);
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel1.border.title"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        lMensaje.setText(resourceMap.getString("lMensaje.text")); // NOI18N
        lMensaje.setName("lMensaje"); // NOI18N

        lIcono.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lIcono.setIcon(resourceMap.getIcon("lIcono.icon")); // NOI18N
        lIcono.setText(resourceMap.getString("lIcono.text")); // NOI18N
        lIcono.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lIcono.setName("lIcono"); // NOI18N

        lInfoNoHayOpciones.setText(resourceMap.getString("lInfoNoHayOpciones.text")); // NOI18N
        lInfoNoHayOpciones.setName("lInfoNoHayOpciones"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(lIcono, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lMensaje, javax.swing.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE))
            .addComponent(lInfoNoHayOpciones, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lMensaje, javax.swing.GroupLayout.DEFAULT_SIZE, 31, Short.MAX_VALUE)
                    .addComponent(lIcono, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lInfoNoHayOpciones, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        add(jPanel1);

        panelOpciones.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("panelOpciones.border.title"))); // NOI18N
        panelOpciones.setName("panelOpciones"); // NOI18N
        panelOpciones.setOpaque(false);

        b4.setText(resourceMap.getString("b4.text")); // NOI18N
        b4.setName("b4"); // NOI18N
        b4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                b4ActionPerformed(evt);
            }
        });

        lTextoAsignarAsistencia.setText(resourceMap.getString("lTextoAsignarAsistencia.text")); // NOI18N
        lTextoAsignarAsistencia.setName("lTextoAsignarAsistencia"); // NOI18N

        bA.setText(resourceMap.getString("bA.text")); // NOI18N
        bA.setToolTipText(resourceMap.getString("bA.toolTipText")); // NOI18N
        bA.setName("bA"); // NOI18N
        bA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bAActionPerformed(evt);
            }
        });

        bI.setText(resourceMap.getString("bI.text")); // NOI18N
        bI.setToolTipText(resourceMap.getString("bI.toolTipText")); // NOI18N
        bI.setName("bI"); // NOI18N
        bI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bIActionPerformed(evt);
            }
        });

        bR.setText(resourceMap.getString("bR.text")); // NOI18N
        bR.setToolTipText(resourceMap.getString("bR.toolTipText")); // NOI18N
        bR.setName("bR"); // NOI18N
        bR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bRActionPerformed(evt);
            }
        });

        bE.setText(resourceMap.getString("bE.text")); // NOI18N
        bE.setToolTipText(resourceMap.getString("bE.toolTipText")); // NOI18N
        bE.setName("bE"); // NOI18N
        bE.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bEActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelOpcionesLayout = new javax.swing.GroupLayout(panelOpciones);
        panelOpciones.setLayout(panelOpcionesLayout);
        panelOpcionesLayout.setHorizontalGroup(
            panelOpcionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelOpcionesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelOpcionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelOpcionesLayout.createSequentialGroup()
                        .addComponent(lTextoAsignarAsistencia)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bA)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bI)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bR)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bE))
                    .addComponent(b4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 189, Short.MAX_VALUE))
                .addContainerGap())
        );
        panelOpcionesLayout.setVerticalGroup(
            panelOpcionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelOpcionesLayout.createSequentialGroup()
                .addGroup(panelOpcionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lTextoAsignarAsistencia)
                    .addComponent(bA)
                    .addComponent(bI)
                    .addComponent(bR)
                    .addComponent(bE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(b4)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        add(panelOpciones);

        panelVisorCasilla1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("panelVisorCasilla1.border.title"))); // NOI18N
        panelVisorCasilla1.setName("panelVisorCasilla1"); // NOI18N
        add(panelVisorCasilla1);

        panelDescripcion.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("panelDescripcion.border.title"))); // NOI18N
        panelDescripcion.setName("panelDescripcion"); // NOI18N

        lInfoExtra.setText(resourceMap.getString("lInfoExtra.text")); // NOI18N
        lInfoExtra.setName("lInfoExtra"); // NOI18N

        javax.swing.GroupLayout panelDescripcionLayout = new javax.swing.GroupLayout(panelDescripcion);
        panelDescripcion.setLayout(panelDescripcionLayout);
        panelDescripcionLayout.setHorizontalGroup(
            panelDescripcionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDescripcionLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lInfoExtra, javax.swing.GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelDescripcionLayout.setVerticalGroup(
            panelDescripcionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDescripcionLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lInfoExtra)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        add(panelDescripcion);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel2.border.title"))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getActionMap(PanelEditorMensajeDigitalizacion.class, this);
        bVer.setAction(actionMap.get("verParte")); // NOI18N
        bVer.setName("bVer"); // NOI18N

        bEditarParte.setAction(actionMap.get("abrirParte")); // NOI18N
        bEditarParte.setName("bEditarParte"); // NOI18N

        bAnularDigitalizacion.setAction(actionMap.get("anularDigitalizacion")); // NOI18N
        bAnularDigitalizacion.setName("bAnularDigitalizacion"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bEditarParte, javax.swing.GroupLayout.DEFAULT_SIZE, 198, Short.MAX_VALUE)
                    .addComponent(bVer, javax.swing.GroupLayout.DEFAULT_SIZE, 198, Short.MAX_VALUE)
                    .addComponent(bAnularDigitalizacion, javax.swing.GroupLayout.DEFAULT_SIZE, 198, Short.MAX_VALUE)))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(bVer)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bEditarParte)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(bAnularDigitalizacion))
        );

        add(jPanel2);
    }// </editor-fold>//GEN-END:initComponents

private void b4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_b4ActionPerformed
    asignarAsistencia(-1);
}//GEN-LAST:event_b4ActionPerformed

private void bAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bAActionPerformed
    asignarAsistencia(ParteFaltas.FALTA_ASISTENCIA);
}//GEN-LAST:event_bAActionPerformed

private void bIActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bIActionPerformed
    asignarAsistencia(ParteFaltas.FALTA_INJUSTIFICADA);
}//GEN-LAST:event_bIActionPerformed

private void bRActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bRActionPerformed
    asignarAsistencia(ParteFaltas.FALTA_RETRASO);
}//GEN-LAST:event_bRActionPerformed

private void bEActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bEActionPerformed
    asignarAsistencia(ParteFaltas.FALTA_EXPULSION);
}//GEN-LAST:event_bEActionPerformed

    @Action(enabledProperty = "parteTieneImagen")
    public void verParte() {
        if (msg != null) {
            String titulo = "Escaneado de parte.";
            if (msg.getParte() != null) {
                titulo = msg.getParte().getDescripcionObjeto() + " : " + Fechas.format(msg.getParte().getFecha());
            }
            //VisorImagenes.mostrarImagen(msg.cargarImagen()).setTitle(titulo);
            //TODO Aquí esta puesta la página a mano!
            if (msg.getParte() != null) {
                VisorImagenes.mostrarImagen(CacheImagenes.getImagenParte(msg.getParte(), 1)).setTitle(titulo);
            } else if (msg.getParteErroneo() != null && msg.getParteErroneo().exists()) {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(msg.getParteErroneo());
                    Image img = ImageIO.read(fis);
                    VisorImagenes.mostrarImagen(img).setTitle(titulo);
                } catch (IOException ex) {
                    Logger.getLogger(MensajeDigitalizacion.class.getName()).log(Level.SEVERE, null, ex);
                }
                Obj.cerrar(fis);
            }
        }
    }
    private boolean parteTieneImagen = false;

    public boolean isParteTieneImagen() {
        return parteTieneImagen;
    }

    public void setParteTieneImagen(boolean b) {
        boolean old = isParteTieneImagen();
        this.parteTieneImagen = b;
        firePropertyChange("parteTieneImagen", old, isParteTieneImagen());
    }

    @Action(block = Task.BlockingScope.ACTION, enabledProperty = "hayIdParte")
    public void abrirParte() {
        if (msg != null && msg.getParte() != null) {
            msg.getParte().resetearAsistencia();
            PanelListaAlumnosParte.abrirEditorParte(msg.getParte());
        }
    }
    private boolean hayIdParte = false;

    public boolean isHayIdParte() {
        return hayIdParte;
    }

    public void setHayIdParte(boolean b) {
        boolean old = isHayIdParte();
        this.hayIdParte = b;
        firePropertyChange("hayIdParte", old, isHayIdParte());
    }

    @Action(block = Task.BlockingScope.WINDOW, enabledProperty = "hayIdParte")
    public Task anularDigitalizacion() {
        return new AnularDigitalizacionTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class AnularDigitalizacionTask extends org.jdesktop.application.Task<Boolean, Void> {

        boolean anular = false;

        AnularDigitalizacionTask(org.jdesktop.application.Application app) {
            super(app);
            int op = JOptionPane.showConfirmDialog(MaimonidesApp.getApplication().getMainFrame(), "Al anular la digitalización se eliminarán todas las advertencias,\nse limpiará el parte de asistencia y se marcará como no digitalizado.\n¿Está seguro de que desea anular la digitalización del parte entero?", "Anular digitalización", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            anular = op == JOptionPane.OK_OPTION;
        }

        @Override
        protected Boolean doInBackground() {
            Boolean ret = null;
            if (anular) {
                msg.getParte().limpiar();
                msg.getParte().setDigitalizado(false);
                msg.getParte().borrarImagen();
                msg.getParte().guardar();
                ret = MensajeDigitalizacion.borrarMensajesPendientes(msg.getParte().getId());
            }
            return ret;
        }

        @Override
        protected void succeeded(Boolean ret) {
            if (ret != null) {
                if (ret) {
                    JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), "Digitalización de parte anulada correctamente", "Anular digitalización", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), "Se ha producido un error anulando la digitalización.\nActualice la listas de advertencias y verifique que el parte ha sido marcado como no digitalizado.", "Anular digitalización", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton b4;
    private javax.swing.JButton bA;
    private javax.swing.JButton bAnularDigitalizacion;
    private javax.swing.JButton bE;
    private javax.swing.JButton bEditarParte;
    private javax.swing.JButton bI;
    private javax.swing.JButton bR;
    private javax.swing.JButton bVer;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel lIcono;
    private javax.swing.JLabel lInfoExtra;
    private javax.swing.JLabel lInfoNoHayOpciones;
    private javax.swing.JLabel lMensaje;
    private javax.swing.JLabel lTextoAsignarAsistencia;
    private javax.swing.JPanel panelDescripcion;
    private javax.swing.JPanel panelOpciones;
    private com.codeko.apps.maimonides.digitalizacion.PanelVisorCasilla panelVisorCasilla1;
    // End of variables declaration//GEN-END:variables
}
