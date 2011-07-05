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
 * PanelPartes.java
 *
 * Created on 14-ago-2009, 12:01:49
 */
package com.codeko.apps.maimonides.convivencia;

import com.codeko.apps.maimonides.IPanel;
import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesUtil;
import com.codeko.apps.maimonides.alumnos.PanelBusquedaAlumnos;
import com.codeko.apps.maimonides.alumnos.PanelFichaAlumno;
import com.codeko.apps.maimonides.cartero.Carta;
import com.codeko.apps.maimonides.cartero.CarteroAlumno;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.Profesor;
import com.codeko.apps.maimonides.usr.Permisos;
import com.codeko.swing.comboBox.AutoCompleteComboBoxDocument;
import com.codeko.swing.forms.FormControl;
import com.codeko.swing.forms.TextFieldControl;
import com.codeko.util.Fechas;
import com.codeko.util.Str;
import java.awt.BorderLayout;
import java.beans.Beans;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.text.JTextComponent;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;

/**
 *
 * @author Codeko
 */
public class PanelPartes extends javax.swing.JPanel implements IPanel {

    PanelBusquedaAlumnos panelBusqueda = new PanelBusquedaAlumnos();
    PanelPartes auto = this;
    ParteConvivencia parte = null;
    FormControl control = null;
    JFrame frameBusquedaAlumnos = new JFrame("Búsqueda de alumnos");
    Alumno alumnoFijo = null;

    /** Creates new form PanelPartes */
    public PanelPartes() {
        initComponents();
        panelBusqueda.setFiltrarPorUsuario(false);
        MaimonidesUtil.setFormatosFecha(tfFecha, false);
        cbEstado.addItem(ParteConvivencia.getTextoEstado(ParteConvivencia.ESTADO_PENDIENTE));
        cbEstado.addItem(ParteConvivencia.getTextoEstado(ParteConvivencia.ESTADO_IGNORADO));
        cbEstado.addItem(ParteConvivencia.getTextoEstado(ParteConvivencia.ESTADO_SANCIONADO));

        cbGravedad.addItem(TipoConducta.getNombreGravedad(TipoConducta.GRAVEDAD_INDEFINIDA));
        cbGravedad.addItem(TipoConducta.getNombreGravedad(TipoConducta.GRAVEDAD_LEVE));
        cbGravedad.addItem(TipoConducta.getNombreGravedad(TipoConducta.GRAVEDAD_GRAVE));

        if (!Beans.isDesignTime()) {
            DefaultComboBoxModel modeloTramos = new DefaultComboBoxModel(TramoHorario.getTramosHorarios().toArray());
            cbTramosHorarios.setModel(modeloTramos);
            cbTramosHorarios.setEditable(true);
            JTextComponent editorTramos = (JTextComponent) cbTramosHorarios.getEditor().getEditorComponent();
            editorTramos.setDocument(new AutoCompleteComboBoxDocument(cbTramosHorarios));
            DefaultComboBoxModel modeloProfesores = new DefaultComboBoxModel(Profesor.getProfesores().toArray());
            cbProfesores.setModel(modeloProfesores);
            cbProfesores.setEditable(true);
            JTextComponent editorProfesores = (JTextComponent) cbProfesores.getEditor().getEditorComponent();
            editorProfesores.setDocument(new AutoCompleteComboBoxDocument(cbProfesores));
        }
        frameBusquedaAlumnos.add(panelBusqueda, BorderLayout.CENTER);
        frameBusquedaAlumnos.setAlwaysOnTop(true);
        frameBusquedaAlumnos.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frameBusquedaAlumnos.setName("frameBusquedaAlumnos");
        panelBusqueda.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("alumnoSeleccionado".equals(evt.getPropertyName())) {
                    if (panelBusqueda.isCerrarAlSeleccionar()) {
                        frameBusquedaAlumnos.setVisible(false);
                    }
                    if (panelBusqueda.getAlumnoSeleccionado() != null) {
                        setAlumno(panelBusqueda.getAlumnoSeleccionado());
                        panelBusqueda.setAlumnoSeleccionado(null);
                    }
                } else if ("enterPulsado".equals(evt.getPropertyName())) {
                    //TODO Acceso a campos de ficha
                }
            }
        });
        panelConductasParte1.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("conductaAnadida".equals(evt.getPropertyName())) {
                    verificarGravedadParte();
                }
            }
        });
        getControl().setControlarCambios(panelConductasParte1.getCombo(), false);
        getControl().setControlarCambios(panelMedidasParte.getCombo(), false);
        getControl().habilitar(false);
        if (Permisos.isUsuarioSoloProfesor()) {
            cbProfesores.setSelectedItem(Permisos.getFiltroProfesor());
            cbProfesores.setEnabled(false);
        }
        bBorrar.setVisible(Permisos.borrado(getClass()));
        bGuardar.setVisible(Permisos.edicion(getClass()));
        bNuevo.setVisible(Permisos.creacion(getClass()));
        if (!Permisos.especial(getClass(), "asignarMedidas")) {
            tabMedidasConductas.remove(panelMedidasParte);
        }
        if (!Permisos.especial(getClass(), "asignarExpulsion")) {
            bAsignarExpulsion.setVisible(false);
        }
        TextFieldControl.setMaxLength(tfDescripcion, 70);
    }

    private void verificarGravedadParte() {
        if (getParte() != null) {
            //Vemos la gravedad de las conductas
            int gravedad = TipoConducta.GRAVEDAD_INDEFINIDA;
            for (Conducta c : panelConductasParte1.getConductas()) {
                if (c.getGravedad() > gravedad) {
                    gravedad = c.getGravedad();
                }
            }
            //Ahora vemos la gravedad del parte
            if (cbGravedad.getSelectedIndex() < gravedad) {
                cbGravedad.setSelectedIndex(gravedad);
            }
        }

    }

    public Alumno getAlumnoFijo() {
        return alumnoFijo;
    }

    public void setAlumnoFijo(Alumno alumnoFijo) {
        this.alumnoFijo = alumnoFijo;
    }

    public final FormControl getControl() {
        if (control == null) {
            control = new FormControl(panelPrincipal);
            control.addPropertyChangueListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if ("cambiado".equals(evt.getPropertyName())) {
                        setDatosPendientesGuardar((Boolean) evt.getNewValue());
                    }
                }
            });
        }
        return control;
    }

    public void setSelectorAlumnoVisible(boolean visible) {
        lAlumno.setVisible(visible);
        lValorAlumno.setVisible(visible);
        bBuscar.setVisible(visible);
        bFichaAlumno.setVisible(visible);
    }

    public boolean isSelectorAlumnoVisible() {
        return lAlumno.isVisible();
    }

    public void setModoCompacto() {
        setSelectorAlumnoVisible(false);
    }
    boolean datosPendientesGuardar = false;

    public boolean isDatosPendientesGuardar() {
        return datosPendientesGuardar;
    }

    public void setDatosPendientesGuardar(boolean b) {
        b = b && parte != null;
        boolean old = isDatosPendientesGuardar();
        this.datosPendientesGuardar = b;
        firePropertyChange("datosPendientesGuardar", old, isDatosPendientesGuardar());
    }

    public void setAlumno(Alumno alumno) {
        if (getParte() != null) {
            getParte().setAlumno(alumno);
        }
        if (alumno != null) {
            lValorAlumno.setText(alumno.toString() + " [" + alumno.getUnidad() + "]");
        } else {
            lValorAlumno.setText("NO ASIGNADO");
        }
    }

    public ParteConvivencia getParte() {
        return parte;
    }

    public boolean setParte(ParteConvivencia parte) {
        if (parte != null && parte.equals(this.parte)) {
            return true;
        }
        if (this.parte != null && isDatosPendientesGuardar()) {
            int op = JOptionPane.showConfirmDialog(this, "Hay datos pendientes de guardarse.\n¿Guardar datos antes de cambiar de parte?", "Datos no guardados", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            if (op == JOptionPane.YES_OPTION) {
                Task tg = guardar();
                MaimonidesApp.getApplication().getContext().getTaskService().execute(tg);
                try {
                    //Esperamos a que termine
                    System.out.println(tg.get());
                } catch (Exception ex) {
                    Logger.getLogger(PanelPartes.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (op != JOptionPane.NO_OPTION) {
                //Si es distinto de no cancelamos
                return false;
            }
        }
        asignarParte(parte);
        return true;
    }

    private void asignarParte(ParteConvivencia parte) {
        limpiar();
        this.parte = parte;
        if (parte != null) {
            cargarParte(parte);
        }
        setParteCargado(parte != null);
        getControl().resetearCambios();
        if (Permisos.isUsuarioSoloProfesor()) {
            cbProfesores.setSelectedItem(Permisos.getFiltroProfesor());
            cbProfesores.setEnabled(false);
        }
    }

    public void limpiar() {
        setAlumno(getAlumnoFijo());
        tfFecha.setDate(new GregorianCalendar().getTime());
        cbTramosHorarios.setSelectedIndex(0);
        tfDescripcion.setText("");
        cbProfesores.setSelectedIndex(-1);
        taObservaciones.setText("");
        cbEstado.setSelectedIndex(0);
        cbGravedad.setSelectedIndex(0);
        cbAlumno.setSelected(false);
        cbPadres.setSelected(false);
        cbTutor.setSelected(false);
        cbSitEnviado.setSelected(false);
        cbSitJE.setSelected(false);
        cbSitTutor.setSelected(false);
        setExpulsion(null);
        panelConductasParte1.limpiar();
        panelMedidasParte.limpiar();
        getControl().habilitar(false);
        if (Permisos.isUsuarioSoloProfesor()) {
            cbProfesores.setSelectedItem(Permisos.getFiltroProfesor());
            cbProfesores.setEnabled(false);
        }
    }

    private void setExpulsion(Expulsion e) {
        if (e == null) {
            lExpulsionValor.setText("No aplicada");
        } else {
            lExpulsionValor.setText(e.toString());
            //Si el estado es pendiente y se asigna una expulsión se considera sancionado.
            if (cbEstado.getSelectedIndex() == ParteConvivencia.ESTADO_PENDIENTE) {
                cbEstado.setSelectedIndex(ParteConvivencia.ESTADO_SANCIONADO);
            }
        }
    }

    private void cargarParte(ParteConvivencia parte) {
        setAlumno(parte.getAlumno());
        if (parte.getExpulsionID() != null) {
            try {
                setExpulsion(Expulsion.getExpulsion(parte.getExpulsionID()));
            } catch (Exception ex) {
                Logger.getLogger(PanelPartes.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        tfFecha.setDate(parte.getFecha().getTime());
        if (parte.getTramoHorario() != null) {
            //TODO Si no se hace no editable a la hora de asignar el ITEM se cuelga el GUI (a veces)
            cbTramosHorarios.setEditable(false);
            cbTramosHorarios.setSelectedItem(parte.getTramoHorario());
            cbTramosHorarios.setEditable(true);
        }
        tfDescripcion.setText(Str.noNulo(parte.getDescripcion()));
        if (parte.getProfesor() != null) {
            //TODO Si no se hace no editable a la hora de asignar el ITEM se cuelga el GUI (a veces)
            cbProfesores.setEditable(false);
            cbProfesores.setSelectedItem(parte.getProfesor());
            cbProfesores.setEditable(true);
        }
        taObservaciones.setText(Str.noNulo(parte.getObservaciones()));
        cbEstado.setSelectedIndex(parte.getEstado());
        cbGravedad.setSelectedIndex(parte.getTipo());
        int tmp = parte.getInformados() & ParteConvivencia.MASCARA_INFORMADO_ALUMNO;
        cbAlumno.setSelected((tmp == ParteConvivencia.MASCARA_INFORMADO_ALUMNO));
        tmp = parte.getInformados() & ParteConvivencia.MASCARA_INFORMADO_PADRES;
        cbPadres.setSelected((tmp == ParteConvivencia.MASCARA_INFORMADO_PADRES));
        tmp = parte.getInformados() & ParteConvivencia.MASCARA_INFORMADO_TUTOR;
        cbTutor.setSelected((tmp == ParteConvivencia.MASCARA_INFORMADO_TUTOR));

        tmp = parte.getSituacion() & ParteConvivencia.SIT_REVISADO_TUTOR;
        cbSitTutor.setSelected((tmp == ParteConvivencia.SIT_REVISADO_TUTOR));
        tmp = parte.getSituacion() & ParteConvivencia.SIT_REVISADO_JE;
        cbSitJE.setSelected((tmp == ParteConvivencia.SIT_REVISADO_JE));
        tmp = parte.getSituacion() & ParteConvivencia.SIT_ENVIADO_SENECA;
        cbSitEnviado.setSelected((tmp == ParteConvivencia.SIT_ENVIADO_SENECA));

        panelConductasParte1.cargar(parte.getConductas());
        panelMedidasParte.cargar(parte.getMedidas());
        getControl().resetearCambios();
        getControl().habilitar(true);
        setParteActualizable(parte != null && parte.getId() != null);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelPrincipal = new javax.swing.JPanel();
        pCampos = new javax.swing.JPanel();
        lFecha = new javax.swing.JLabel();
        tfFecha = new org.jdesktop.swingx.JXDatePicker();
        lProfesor = new javax.swing.JLabel();
        cbProfesores = new javax.swing.JComboBox();
        lAlumno = new javax.swing.JLabel();
        lDescripcion = new javax.swing.JLabel();
        tfDescripcion = new javax.swing.JTextField();
        lTramoHorario = new javax.swing.JLabel();
        cbTramosHorarios = new javax.swing.JComboBox();
        lValorAlumno = new javax.swing.JLabel();
        bBuscar = new javax.swing.JButton();
        lObs = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        taObservaciones = new javax.swing.JTextArea();
        lEstado = new javax.swing.JLabel();
        cbEstado = new javax.swing.JComboBox();
        lInformados = new javax.swing.JLabel();
        cbAlumno = new javax.swing.JCheckBox();
        cbTutor = new javax.swing.JCheckBox();
        cbPadres = new javax.swing.JCheckBox();
        lSancion = new javax.swing.JLabel();
        lExpulsionValor = new javax.swing.JLabel();
        bAsignarExpulsion = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        cbGravedad = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        cbSitEnviado = new javax.swing.JCheckBox();
        cbSitTutor = new javax.swing.JCheckBox();
        cbSitJE = new javax.swing.JCheckBox();
        bFichaAlumno = new javax.swing.JButton();
        tabMedidasConductas = new javax.swing.JTabbedPane();
        panelConductasParte1 = new com.codeko.apps.maimonides.convivencia.PanelConductasParte(TipoConducta.TIPO_CONDUCTA);
        panelMedidasParte = new com.codeko.apps.maimonides.convivencia.PanelConductasParte(TipoConducta.TIPO_MEDIDA);
        barraHerramientas = new javax.swing.JToolBar();
        bActualizar = new javax.swing.JButton();
        bNuevo = new javax.swing.JButton();
        bGuardar = new javax.swing.JButton();
        bBorrar = new javax.swing.JButton();
        bNotificar = new javax.swing.JButton();

        setName("maimonides.paneles.convivencia.partes"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        panelPrincipal.setName("panelPrincipal"); // NOI18N

        pCampos.setName("pCampos"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getResourceMap(PanelPartes.class);
        lFecha.setText(resourceMap.getString("lFecha.text")); // NOI18N
        lFecha.setName("lFecha"); // NOI18N

        tfFecha.setName("tfFecha"); // NOI18N

        lProfesor.setText(resourceMap.getString("lProfesor.text")); // NOI18N
        lProfesor.setName("lProfesor"); // NOI18N

        cbProfesores.setName("cbProfesores"); // NOI18N

        lAlumno.setText(resourceMap.getString("lAlumno.text")); // NOI18N
        lAlumno.setName("lAlumno"); // NOI18N

        lDescripcion.setText(resourceMap.getString("lDescripcion.text")); // NOI18N
        lDescripcion.setName("lDescripcion"); // NOI18N

        tfDescripcion.setText(resourceMap.getString("tfDescripcion.text")); // NOI18N
        tfDescripcion.setName("tfDescripcion"); // NOI18N

        lTramoHorario.setText(resourceMap.getString("lTramoHorario.text")); // NOI18N
        lTramoHorario.setName("lTramoHorario"); // NOI18N

        cbTramosHorarios.setName("cbTramosHorarios"); // NOI18N

        lValorAlumno.setFont(resourceMap.getFont("lValorAlumno.font")); // NOI18N
        lValorAlumno.setText(resourceMap.getString("lValorAlumno.text")); // NOI18N
        lValorAlumno.setName("lValorAlumno"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getActionMap(PanelPartes.class, this);
        bBuscar.setAction(actionMap.get("mostraBusquedaAlumnos")); // NOI18N
        bBuscar.setName("bBuscar"); // NOI18N

        lObs.setText(resourceMap.getString("lObs.text")); // NOI18N
        lObs.setName("lObs"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        taObservaciones.setColumns(20);
        taObservaciones.setLineWrap(true);
        taObservaciones.setRows(5);
        taObservaciones.setWrapStyleWord(true);
        taObservaciones.setName("taObservaciones"); // NOI18N
        jScrollPane1.setViewportView(taObservaciones);

        lEstado.setText(resourceMap.getString("lEstado.text")); // NOI18N
        lEstado.setName("lEstado"); // NOI18N

        cbEstado.setName("cbEstado"); // NOI18N

        lInformados.setText(resourceMap.getString("lInformados.text")); // NOI18N
        lInformados.setName("lInformados"); // NOI18N

        cbAlumno.setText(resourceMap.getString("cbAlumno.text")); // NOI18N
        cbAlumno.setName("cbAlumno"); // NOI18N

        cbTutor.setText(resourceMap.getString("cbTutor.text")); // NOI18N
        cbTutor.setName("cbTutor"); // NOI18N

        cbPadres.setText(resourceMap.getString("cbPadres.text")); // NOI18N
        cbPadres.setName("cbPadres"); // NOI18N

        lSancion.setText(resourceMap.getString("lSancion.text")); // NOI18N
        lSancion.setName("lSancion"); // NOI18N

        lExpulsionValor.setFont(resourceMap.getFont("lExpulsionValor.font")); // NOI18N
        lExpulsionValor.setText(resourceMap.getString("lExpulsionValor.text")); // NOI18N
        lExpulsionValor.setName("lExpulsionValor"); // NOI18N

        bAsignarExpulsion.setAction(actionMap.get("asignarExpulsion")); // NOI18N
        bAsignarExpulsion.setName("bAsignarExpulsion"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        cbGravedad.setName("cbGravedad"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        cbSitEnviado.setText(resourceMap.getString("cbSitEnviado.text")); // NOI18N
        cbSitEnviado.setName("cbSitEnviado"); // NOI18N

        cbSitTutor.setText(resourceMap.getString("cbSitTutor.text")); // NOI18N
        cbSitTutor.setName("cbSitTutor"); // NOI18N

        cbSitJE.setText(resourceMap.getString("cbSitJE.text")); // NOI18N
        cbSitJE.setName("cbSitJE"); // NOI18N

        bFichaAlumno.setAction(actionMap.get("mostrarFichaAlumno")); // NOI18N
        bFichaAlumno.setName("bFichaAlumno"); // NOI18N

        javax.swing.GroupLayout pCamposLayout = new javax.swing.GroupLayout(pCampos);
        pCampos.setLayout(pCamposLayout);
        pCamposLayout.setHorizontalGroup(
            pCamposLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pCamposLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pCamposLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lSancion)
                    .addGroup(pCamposLayout.createSequentialGroup()
                        .addGroup(pCamposLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lFecha)
                            .addComponent(lProfesor)
                            .addComponent(lDescripcion)
                            .addComponent(lObs)
                            .addComponent(lAlumno, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(10, 10, 10)
                        .addGroup(pCamposLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pCamposLayout.createSequentialGroup()
                                .addComponent(lExpulsionValor, javax.swing.GroupLayout.DEFAULT_SIZE, 642, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(bAsignarExpulsion))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pCamposLayout.createSequentialGroup()
                                .addComponent(lValorAlumno, javax.swing.GroupLayout.DEFAULT_SIZE, 608, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(bBuscar)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(bFichaAlumno))
                            .addComponent(tfDescripcion, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 682, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pCamposLayout.createSequentialGroup()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 385, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pCamposLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(cbTutor)
                                    .addComponent(cbPadres)
                                    .addComponent(lInformados)
                                    .addComponent(cbAlumno))
                                .addGap(18, 30, Short.MAX_VALUE)
                                .addGroup(pCamposLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2)
                                    .addComponent(cbSitTutor)
                                    .addComponent(cbSitJE)
                                    .addComponent(cbSitEnviado))
                                .addGap(0, 0, 0))
                            .addGroup(pCamposLayout.createSequentialGroup()
                                .addGroup(pCamposLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(cbProfesores, javax.swing.GroupLayout.Alignment.LEADING, 0, 515, Short.MAX_VALUE)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pCamposLayout.createSequentialGroup()
                                        .addComponent(tfFecha, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(lTramoHorario)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cbTramosHorarios, 0, 0, Short.MAX_VALUE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(pCamposLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lEstado)
                                    .addComponent(jLabel1))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pCamposLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(cbEstado, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(cbGravedad, 0, 89, Short.MAX_VALUE))))))
                .addContainerGap())
        );
        pCamposLayout.setVerticalGroup(
            pCamposLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pCamposLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pCamposLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lAlumno)
                    .addComponent(lValorAlumno)
                    .addComponent(bFichaAlumno)
                    .addComponent(bBuscar))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pCamposLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lFecha)
                    .addComponent(tfFecha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lTramoHorario)
                    .addComponent(cbTramosHorarios, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lEstado)
                    .addComponent(cbEstado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pCamposLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbProfesores, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lProfesor)
                    .addComponent(jLabel1)
                    .addComponent(cbGravedad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pCamposLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfDescripcion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lDescripcion))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pCamposLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE)
                    .addGroup(pCamposLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pCamposLayout.createSequentialGroup()
                            .addComponent(lObs)
                            .addGap(82, 82, 82))
                        .addGroup(pCamposLayout.createSequentialGroup()
                            .addGroup(pCamposLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(lInformados)
                                .addComponent(jLabel2))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(pCamposLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(cbAlumno)
                                .addComponent(cbSitTutor))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(pCamposLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(cbTutor)
                                .addComponent(cbSitJE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(pCamposLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(cbPadres)
                                .addComponent(cbSitEnviado)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pCamposLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lSancion)
                    .addComponent(lExpulsionValor)
                    .addComponent(bAsignarExpulsion)))
        );

        tabMedidasConductas.setName("tabMedidasConductas"); // NOI18N

        panelConductasParte1.setName("panelConductasParte1"); // NOI18N
        tabMedidasConductas.addTab(resourceMap.getString("panelConductasParte1.TabConstraints.tabTitle"), panelConductasParte1); // NOI18N

        panelMedidasParte.setName("panelMedidasParte"); // NOI18N
        tabMedidasConductas.addTab(resourceMap.getString("panelMedidasParte.TabConstraints.tabTitle"), panelMedidasParte); // NOI18N

        javax.swing.GroupLayout panelPrincipalLayout = new javax.swing.GroupLayout(panelPrincipal);
        panelPrincipal.setLayout(panelPrincipalLayout);
        panelPrincipalLayout.setHorizontalGroup(
            panelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pCampos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(panelPrincipalLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabMedidasConductas, javax.swing.GroupLayout.DEFAULT_SIZE, 775, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelPrincipalLayout.setVerticalGroup(
            panelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPrincipalLayout.createSequentialGroup()
                .addComponent(pCampos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(tabMedidasConductas, javax.swing.GroupLayout.PREFERRED_SIZE, 142, Short.MAX_VALUE)
                .addContainerGap())
        );

        add(panelPrincipal, java.awt.BorderLayout.CENTER);

        barraHerramientas.setFloatable(false);
        barraHerramientas.setRollover(true);
        barraHerramientas.setName("barraHerramientas"); // NOI18N

        bActualizar.setAction(actionMap.get("actualizar")); // NOI18N
        bActualizar.setFocusable(false);
        bActualizar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bActualizar.setName("bActualizar"); // NOI18N
        bActualizar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraHerramientas.add(bActualizar);

        bNuevo.setAction(actionMap.get("nuevo")); // NOI18N
        bNuevo.setFocusable(false);
        bNuevo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bNuevo.setName("bNuevo"); // NOI18N
        bNuevo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraHerramientas.add(bNuevo);

        bGuardar.setAction(actionMap.get("guardar")); // NOI18N
        bGuardar.setFocusable(false);
        bGuardar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bGuardar.setName("bGuardar"); // NOI18N
        bGuardar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraHerramientas.add(bGuardar);

        bBorrar.setAction(actionMap.get("borrar")); // NOI18N
        bBorrar.setFocusable(false);
        bBorrar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bBorrar.setName("bBorrar"); // NOI18N
        bBorrar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraHerramientas.add(bBorrar);

        bNotificar.setAction(actionMap.get("notificar")); // NOI18N
        bNotificar.setFocusable(false);
        bNotificar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bNotificar.setName("bNotificar"); // NOI18N
        bNotificar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraHerramientas.add(bNotificar);

        add(barraHerramientas, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents
    private boolean parteCargado = false;

    public boolean isParteCargado() {
        return parteCargado;
    }

    public void setParteCargado(boolean b) {
        boolean old = isParteCargado();
        this.parteCargado = b;
        firePropertyChange("parteCargado", old, isParteCargado());
    }
    private boolean parteActualizable = false;

    public boolean isParteActualizable() {
        return parteActualizable;
    }

    public void setParteActualizable(boolean b) {
        boolean old = isParteActualizable();
        this.parteActualizable = b;
        firePropertyChange("parteActualizable", old, isParteActualizable());
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bActualizar;
    private javax.swing.JButton bAsignarExpulsion;
    private javax.swing.JButton bBorrar;
    private javax.swing.JButton bBuscar;
    private javax.swing.JButton bFichaAlumno;
    private javax.swing.JButton bGuardar;
    private javax.swing.JButton bNotificar;
    private javax.swing.JButton bNuevo;
    private javax.swing.JToolBar barraHerramientas;
    private javax.swing.JCheckBox cbAlumno;
    private javax.swing.JComboBox cbEstado;
    private javax.swing.JComboBox cbGravedad;
    private javax.swing.JCheckBox cbPadres;
    private javax.swing.JComboBox cbProfesores;
    private javax.swing.JCheckBox cbSitEnviado;
    private javax.swing.JCheckBox cbSitJE;
    private javax.swing.JCheckBox cbSitTutor;
    private javax.swing.JComboBox cbTramosHorarios;
    private javax.swing.JCheckBox cbTutor;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lAlumno;
    private javax.swing.JLabel lDescripcion;
    private javax.swing.JLabel lEstado;
    private javax.swing.JLabel lExpulsionValor;
    private javax.swing.JLabel lFecha;
    private javax.swing.JLabel lInformados;
    private javax.swing.JLabel lObs;
    private javax.swing.JLabel lProfesor;
    private javax.swing.JLabel lSancion;
    private javax.swing.JLabel lTramoHorario;
    private javax.swing.JLabel lValorAlumno;
    private javax.swing.JPanel pCampos;
    private com.codeko.apps.maimonides.convivencia.PanelConductasParte panelConductasParte1;
    private com.codeko.apps.maimonides.convivencia.PanelConductasParte panelMedidasParte;
    private javax.swing.JPanel panelPrincipal;
    private javax.swing.JTextArea taObservaciones;
    private javax.swing.JTabbedPane tabMedidasConductas;
    private javax.swing.JTextField tfDescripcion;
    private org.jdesktop.swingx.JXDatePicker tfFecha;
    // End of variables declaration//GEN-END:variables

    @Override
    public boolean puedoSusituir() {
        if (isDatosPendientesGuardar()) {
            int op = JOptionPane.showConfirmDialog(this, "Hay datos pendientes de guardarse.\n¿Guardar datos antes de cambiar de ficha?", "Datos no guardados", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            if (op == JOptionPane.YES_OPTION) {
                Task tg = guardar();
                MaimonidesApp.getApplication().getContext().getTaskService().execute(tg);
                try {
                    //Esperamos a que termine
                    System.out.println(tg.get());
                } catch (Exception ex) {
                    Logger.getLogger(PanelPartes.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (op != JOptionPane.NO_OPTION) {
                //Si es distinto de no cancelamos
                return false;
            }
        }
        return true;
    }

    @Action
    public void mostraBusquedaAlumnos() {
        MaimonidesApp.getApplication().show(frameBusquedaAlumnos);
    }

    @Action
    public void nuevo() {
        ParteConvivencia p = new ParteConvivencia();
        p.setAlumno(getAlumnoFijo());
        p.setTramoHorario(TramoHorario.getDefaultTramoHorario());
        setParte(p);
    }

    @Action(block = Task.BlockingScope.WINDOW, enabledProperty = "datosPendientesGuardar")
    public Task guardar() {
        return new GuardarTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class GuardarTask extends org.jdesktop.application.Task<Boolean, Void> {

        ArrayList<String> mensajes = new ArrayList<String>();

        GuardarTask(org.jdesktop.application.Application app) {
            super(app);
            if (parte == null) {
                cancel(true);
            }
        }

        @Override
        protected Boolean doInBackground() {
            mensajes = guardarParte();
            return mensajes.isEmpty();
        }

        @Override
        protected void succeeded(Boolean result) {
            if (result) {
                setMessage("Parte de convivencia guardado correctamente.");
                MaimonidesUtil.mostrarVentanaListaDatos("Parte de convivencia guardado correctamente.", mensajes);
            } else {
                setMessage("Error guardando parte de convivencia.");
                MaimonidesUtil.mostrarVentanaListaDatos("Error guardando parte de convivencia.", mensajes);
            }
        }
    }

    private ArrayList<String> guardarParte() {
        ArrayList<String> mensajes = new ArrayList<String>();
        if (parte.getAlumno() == null) {
            mensajes.add("Debe asignar el parte de convivencia a un alumno.");
        }
        parte.setFecha(Fechas.toGregorianCalendar(tfFecha.getDate()));
        if (parte.getFecha() == null) {
            mensajes.add("Debe especificar una fecha válida para el parte de convivencia.");
        }
        Object th = cbTramosHorarios.getSelectedItem();
        if (th instanceof TramoHorario) {
            parte.setTramoHorario((TramoHorario) th);
        } else {
            mensajes.add("Debe especificar un tramo horario para el parte de convivencia.");
        }
        parte.setDescripcion(tfDescripcion.getText());
        Object prof = cbProfesores.getSelectedItem();
        if (prof instanceof Profesor) {
            parte.setProfesor((Profesor) prof);
        } else {
            mensajes.add("Debe especificar el profesor que ha creado parte de convivencia.");
        }
        if (cbGravedad.getSelectedIndex() <= 0) {
            mensajes.add("Debe especificar la gravedad del parte.");
        }
        parte.setObservaciones(taObservaciones.getText());
        parte.setEstado(cbEstado.getSelectedIndex());
        parte.setTipo(cbGravedad.getSelectedIndex());
        int infomado = (cbAlumno.isSelected() ? ParteConvivencia.MASCARA_INFORMADO_ALUMNO : 0) | (cbTutor.isSelected() ? ParteConvivencia.MASCARA_INFORMADO_TUTOR : 0) | (cbPadres.isSelected() ? ParteConvivencia.MASCARA_INFORMADO_PADRES : 0);
        parte.setInformados(infomado);
        int situacion = (cbSitTutor.isSelected() ? ParteConvivencia.SIT_REVISADO_TUTOR : 0) | (cbSitJE.isSelected() ? ParteConvivencia.SIT_REVISADO_JE : 0) | (cbSitEnviado.isSelected() ? ParteConvivencia.SIT_ENVIADO_SENECA : 0);
        parte.setSituacion(situacion);
        parte.setConductas(panelConductasParte1.getConductas());
        parte.setMedidas(panelMedidasParte.getConductas());
        if (mensajes.isEmpty()) {
            boolean nuevo = parte.getId() == null;
            if (!parte.guardar()) {
                mensajes.add("Ha ocurrido un error desconocido guardando el parte de convivencia.");
            } else if (nuevo) {
                firePropertyChange("nuevoParteCreado", auto, parte);
                auto.firePropertyChange("nuevoParteCreado", auto, parte);
            }
        }
        if (mensajes.isEmpty()) {
            getControl().resetearCambios();
        }
        setParteActualizable(parte != null && parte.getId() != null);
        return mensajes;
    }

    @Action(block = Task.BlockingScope.WINDOW, enabledProperty = "parteActualizable")
    public Task borrar() {
        return new BorrarTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class BorrarTask extends org.jdesktop.application.Task<Boolean, Void> {

        ParteConvivencia p = null;

        BorrarTask(org.jdesktop.application.Application app) {
            super(app);
            int op = JOptionPane.showConfirmDialog(MaimonidesApp.getApplication().getMainFrame(), "¿Esta seguro de que desea borrar el parte de convivencia actual?", "Borrar", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (op == JOptionPane.YES_OPTION) {
                p = getParte();
            }
        }

        @Override
        protected Boolean doInBackground() {
            if (p != null) {
                return p.borrar();
            }
            return true;
        }

        @Override
        protected void succeeded(Boolean result) {
            if (p != null) {
                if (result) {
                    setMessage("Parte de convivencia borrado correctamente.");
                    firePropertyChange("parteBorrado", auto, parte);
                    auto.firePropertyChange("parteBorrado", auto, parte);
                    asignarParte(null);
                } else {
                    setMessage("No se ha podido borrar el parte de convivencia.");
                }
            } else {
                setMessage("Operación cancelada.");
            }
        }
    }

    @Action(block = Task.BlockingScope.WINDOW, enabledProperty = "parteActualizable")
    public Task actualizar() {
        return new ActualizarTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class ActualizarTask extends org.jdesktop.application.Task<ParteConvivencia, Void> {

        ParteConvivencia p = null;

        ActualizarTask(org.jdesktop.application.Application app) {
            super(app);
            p = getParte();
            if (!setParte(null)) {
                cancel(false);
                p = null;
            }
        }

        @Override
        protected ParteConvivencia doInBackground() {
            try {
                if (p != null) {
                    ParteConvivencia p2 = ParteConvivencia.getParte(p.getId());
                    //setParte(p2);
                    p = null;
                    return p2;
                }
            } catch (Exception ex) {
                Logger.getLogger(PanelFichaAlumno.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        }

        @Override
        protected void succeeded(ParteConvivencia result) {
            if(result!=null){
                setParte(result);
            }
        }
    }

    @Action(enabledProperty = "parteCargado")
    public void asignarExpulsion() {
        Expulsion e = PanelExpulsiones.mostrarSelectorExpulsiones(getParte().getAlumno());
        if (e != null) {
            getParte().setExpulsionID(e.getId());
            setExpulsion(e);
            getControl().setCambiado(true);
        }
    }

    @Action(block = Task.BlockingScope.APPLICATION, enabledProperty = "parteCargado")
    public Task notificar() {
        return new NotificarTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class NotificarTask extends org.jdesktop.application.Task<Boolean, Void> {

        ArrayList<String> mensajes = new ArrayList<String>();

        NotificarTask(org.jdesktop.application.Application app) {
            super(app);
            if (getParte() == null) {
                cancel(false);
            } else if (getParte().isNotificadoTutores()) {
                int op = JOptionPane.showConfirmDialog(MaimonidesApp.getApplication().getMainFrame(), "El parte está marcado como ya notificado a los Padres/Tutores.\n¿Desea notificarlo de todas formas?", "¿Notificar parte ya notificado?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (op != JOptionPane.YES_OPTION) {
                    cancel(false);
                }
            }
        }

        @Override
        protected Boolean doInBackground() {
            setMessage("Guardando parte...");
            mensajes = guardarParte();
            if (mensajes.isEmpty()) {
                setMessage("Enviando/Imprimiendo notificaciones...");
                CarteroAlumno<ParteConvivencia> cartero = new CarteroAlumno<ParteConvivencia>("partes de convivencia", Carta.TIPO_CARTA_PARTE_CONVIVENCIA) {

                    @Override
                    protected void addDatosExtra(Map<String, Object> data, ParteConvivencia p, Carta carta) {
                        p.addDatosExtraImpresion(data, carta);
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
                cartero.setCargarAsistenciaTotal(false);
                ArrayList<ParteConvivencia> partes = new ArrayList<ParteConvivencia>();
                partes.add(getParte());
                boolean ret = cartero.enviar(partes);
                if (ret) {
                    getParte().setInformados(getParte().getInformados() | ParteConvivencia.MASCARA_INFORMADO_PADRES);
                    getParte().guardar();
                    cbPadres.setSelected(true);
                }
                return ret;
            }
            return false;
        }

        @Override
        protected void succeeded(Boolean result) {
            if (!result) {
                setMessage("Error notificando parte de convivencia.");
                MaimonidesUtil.mostrarVentanaListaDatos("Error guardando parte de convivencia.", mensajes);
            } else {
                JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), "Parte de convivencia notificado correctamente", "Notificar parte de convivencia", JOptionPane.PLAIN_MESSAGE);
            }
        }
    }

    @Action(enabledProperty = "selectorAlumnoVisible")
    public void mostrarFichaAlumno() {
        if (getParte()!= null && getParte().getAlumno()!=null) {
            MaimonidesApp.getMaimonidesView().mostrarFichaAlumno( getParte().getAlumno());
        }
    }
}
