/*
 * PanelFichaAlumno.java
 *
 * Created on 23-ene-2009, 12:21:57
 */
package com.codeko.apps.maimonides.alumnos;

import com.codeko.apps.maimonides.ICargable;
import com.codeko.apps.maimonides.IPanel;
import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesBean;
import com.codeko.apps.maimonides.MaimonidesUtil;
import com.codeko.apps.maimonides.apoyos.PanelApoyos;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.Curso;
import com.codeko.apps.maimonides.elementos.Unidad;
import com.codeko.apps.maimonides.horarios.BloqueHorario;
import com.codeko.apps.maimonides.partes.informes.alumnos.PanelMapaAsistencia;
import com.codeko.apps.maimonides.usr.Permisos;
import com.codeko.local.es.Provincias;
import com.codeko.swing.NumericDocument;
import com.codeko.swing.forms.FormControl;
import com.codeko.util.Fechas;
import com.codeko.util.Num;
import com.codeko.util.Str;
import java.awt.BorderLayout;
import java.awt.Component;
import java.beans.Beans;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;

/**
 *
 * @author Codeko
 */
public class PanelFichaAlumno extends javax.swing.JPanel implements IPanel {

    DefaultComboBoxModel modeloComboBoxUnidades = new DefaultComboBoxModel();
    Alumno alumno = null;
    FormControl control = null;
    JFrame frameBusquedaAlumnos = new JFrame("Búsqueda de alumnos");
    PanelFichaAlumno auto = this;
    PanelMapaAsistencia panelMapaAsistencia = new PanelMapaAsistencia();
    boolean cargando = false;

    /** Creates new form PanelFichaAlumno */
    public PanelFichaAlumno() {
        initComponents();
        if (!Beans.isDesignTime()) {
            panelBusqueda.addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if ("alumnoSeleccionado".equals(evt.getPropertyName())) {
                        if (panelBusqueda.isCerrarAlSeleccionar()) {
                            frameBusquedaAlumnos.setVisible(false);
                        }
                        MaimonidesUtil.ejecutarTask(auto, "cargarAlumno");
                    } else if ("enterPulsado".equals(evt.getPropertyName())) {
                        //TODO Acceso a campos de ficha
                    }
                }
            });
            frameBusquedaAlumnos.add(panelBusqueda, BorderLayout.CENTER);
            frameBusquedaAlumnos.setAlwaysOnTop(true);
            frameBusquedaAlumnos.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            frameBusquedaAlumnos.setName("frameBusquedaAlumnos");
            SimpleDateFormat[] formatos = new SimpleDateFormat[]{new SimpleDateFormat("dd/MM/yy"), new SimpleDateFormat("EEEE dd/MM/yy"), new SimpleDateFormat("EEEE dd-MM-yy"), new SimpleDateFormat("dd-MM-yy"), new SimpleDateFormat("ddMMyy")};
            tfFechaNac.setFormats(formatos);
            tfCP.getDocument().addDocumentListener(new DocumentListener() {

                private void updateProv() {
                    final String text = Provincias.getProvincia(tfCP.getText());
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            lProvincia.setText(text);
                        }
                    });
                }

                @Override
                public void insertUpdate(DocumentEvent e) {
                    updateProv();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    updateProv();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    updateProv();
                }
            });
            tfSms.getDocument().addDocumentListener(new DocumentListener() {

                private void updateSms() {
                    final String text = tfSms.getText();
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            panelEnvioSMS1.setNumeroSMS(text);
                        }
                    });
                }

                @Override
                public void insertUpdate(DocumentEvent e) {
                    updateSms();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    updateSms();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    updateSms();
                }
            });

            tfEmail.getDocument().addDocumentListener(new DocumentListener() {

                private void updateEmail() {
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            panelEnvioEmail1.setDestinatario(getAlumno());
                        }
                    });
                }

                @Override
                public void insertUpdate(DocumentEvent e) {
                    updateEmail();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    updateEmail();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    updateEmail();
                }
            });

            tfTelefono.getDocument().addDocumentListener(new DocumentListener() {

                private void updateSMS() {
                    final String text = tfTelefono.getText();
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            panelEnvioSMSVoz1.setNumeroSMS(text);
                        }
                    });
                }

                @Override
                public void insertUpdate(DocumentEvent e) {
                    updateSMS();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    updateSMS();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    updateSMS();
                }
            });
            panelPestanasAsistencia.add(panelMapaAsistencia, "Mapa");
            panelVisionHorario1.addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if ("focoEnBloqueGanado".equals(evt.getPropertyName())) {
                        BloqueHorario bloque = (BloqueHorario) evt.getNewValue();
                        panelInfoBloque1.setBloque(bloque);
                    } else if ("focoEnBloquePerdido".equals(evt.getPropertyName())) {
                        panelInfoBloque1.setBloque(null);
                    }
                }
            });

            getControl().setRevisarSubcomponentes(panelComunicacion, false);
            getControl().setRevisarSubcomponentes(panelCargable1, false);
            //getControl().setRevisarSubcomponentes(panelPestanaConvivencia1, false);
            getControl().setRevisarSubcomponentes(panelAsistencias, false);
            getControl().habilitar(false);
        }
        //Ahora asignamos los controles
        bNuevo.setVisible(Permisos.creacion(getClass()));
        bBorrar.setVisible(Permisos.borrado(getClass()));
        bGuardar.setVisible(Permisos.edicion(getClass()));
        bApoyos.setVisible(Permisos.especial(getClass(), "apoyos"));
    }

    public boolean isCargando() {
        return cargando;
    }

    public void setCargando(boolean cargando) {
        this.cargando = cargando;
    }

    public final FormControl getControl() {
        if (control == null) {
            control = new FormControl(pCentral);
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

    public Alumno getAlumno() {
        return alumno;
    }

    public boolean setAlumno(Alumno alumno) {
        setCargando(true);
        if (alumno != null && alumno.equals(this.alumno)) {
            return true;
        }
        if (this.alumno != null && isDatosPendientesGuardar() && Permisos.edicion(this)) {
            int op = JOptionPane.showConfirmDialog(this, "Hay datos pendientes de guardarse.\n¿Guardar datos antes de cambiar de ficha?", "Datos no guardados", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            if (op == JOptionPane.YES_OPTION) {
                Task tg = guardar();
                MaimonidesApp.getApplication().getContext().getTaskService().execute(tg);
                try {
                    //Esperamos a que termine
                    System.out.println(tg.get());
                } catch (Exception ex) {
                    Logger.getLogger(PanelFichaAlumno.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (op != JOptionPane.NO_OPTION) {
                //Si es distinto de no cancelamos
                return false;
            }
        }
        limpiar();
        this.alumno = alumno;
        if (alumno != null) {
            cargarAlumno(alumno);
        }
        setAlumnoCargado(alumno != null);
        getControl().resetearCambios();
        setCargando(false);
        return true;
    }

    public void limpiar() {
        modeloComboBoxUnidades.removeAllElements();
        tfNombre.setText("");
        tfApellido1.setText("");
        tfApellido2.setText("");
        tfNumEscolar.setText("");
        tfNumSeneca.setText("");
        tfCodFaltas.setText("");
        lCurso.setText("");
        cbBilingue.setSelected(false);
        cbRepetidor.setSelected(false);
        cbDicu.setSelected(false);
        tfEmail.setText("");
        tfTelefono.setText("");
        tfSms.setText("");
        tfDireccion.setText("");
        tfCP.setText("");
        tfPoblacion.setText("");
        cbNotificarEmail.setSelected(false);
        cbNotificarSMS.setSelected(false);
        cbNotificarImpreso.setSelected(false);
        cbNotificarTelefono.setSelected(false);
        cbNotificarPresencial.setSelected(false);
        taObservaciones.setText("");
        panelTutor1.setTutor(null);
        panelTutor2.setTutor(null);
        tfDni.setText("");
        tfExpediente.setText("");
        tfNacionalidad.setText("");
        tfFechaNac.setDate(null);
        cbSexo.setSelectedIndex(0);
        tfPaisNac.setText("");
        tfLocNac.setText("");
        tfProvNac.setText("");
        tfTelUrg.setText("");
        lCabeceraAlumno.setText("Ningún alumno seleccionado");
        for (Component c : panelPestanas.getComponents()) {
            if (c instanceof ICargable) {
                ((ICargable) c).vaciar();
            }
        }
        firePropertyChange("vaciar", false, true);
        getControl().habilitar(false);
    }

    @Action
    public void nuevoAlumno() {
        ArrayList<Curso> cursos = Curso.getCursos();
        Object ret = JOptionPane.showInputDialog(this, "Indique el curso para el que desea crear el alumno:", "Nuevo alumno", JOptionPane.QUESTION_MESSAGE, bNuevo.getIcon(), cursos.toArray(), cursos.get(0));
        if (ret != null) {
            Alumno a = new Alumno();
            a.setCurso(((Curso) ret));
            panelPestanas.setSelectedComponent(panelDatos);
            tfNombre.requestFocus();
            setAlumno(a);
            panelBusqueda.setAlumnoSeleccionado(null);
        }
    }

    @Action(block = Task.BlockingScope.WINDOW, enabledProperty = "datosPendientesGuardar")
    public Task guardar() {
        return new GuardarTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));

    }

    private void cargarAlumno(final Alumno alumno) {
        ArrayList<Unidad> uds = null;
        if (alumno.getUnidad() != null) {
            uds = Unidad.getUnidadesDisponibles(alumno.getUnidad());
        } else if (alumno.getObjetoCurso() != null) {
            uds = Unidad.getUnidadesDisponibles(alumno.getObjetoCurso());
        }
        if (uds != null) {
            for (Unidad u : uds) {
                modeloComboBoxUnidades.addElement(u);
            }
        }
        if (alumno.getUnidad() != null) {
            cbUnidades.setSelectedItem(alumno.getUnidad());
        } else {
            cbUnidades.setSelectedIndex(-1);
        }
        lCabeceraAlumno.setText(alumno.getDescripcionObjeto() + ", " + Str.noNulo(alumno.getUnidad()));
        tfNombre.setText(alumno.getNombre());
        tfApellido1.setText(alumno.getApellido1());
        tfApellido2.setText(alumno.getApellido2());
        tfNumEscolar.setText(alumno.getNumeroEscolar());
        tfNumSeneca.setText(Str.noNulo(alumno.getCodigo()));
        tfCodFaltas.setText(Str.noNulo(alumno.getCodFaltas()));
        lCurso.setText(alumno.getObjetoCurso().toString());
        cbBilingue.setSelected(alumno.isBilingue());
        cbRepetidor.setSelected(alumno.isRepetidor());
        cbDicu.setSelected(alumno.isDicu());
        tfEmail.setText(alumno.getEmail());
        tfTelefono.setText(alumno.getTelefono());
        tfSms.setText(alumno.getSms());
        tfDireccion.setText(alumno.getDireccion());
        tfCP.setText(alumno.getCp());
        tfPoblacion.setText(alumno.getPoblacion());
        cbNotificarEmail.setSelected(alumno.isNotificar(Alumno.NOTIFICAR_EMAIL));
        cbNotificarSMS.setSelected(alumno.isNotificar(Alumno.NOTIFICAR_SMS));
        cbNotificarImpreso.setSelected(alumno.isNotificar(Alumno.NOTIFICAR_IMPRESO));
        cbNotificarTelefono.setSelected(alumno.isNotificar(Alumno.NOTIFICAR_TELEFONO));
        cbNotificarPresencial.setSelected(alumno.isNotificar(Alumno.NOTIFICAR_PRESENCIAL));
        taObservaciones.setText(alumno.getObservaciones());
        tfNombre.requestFocus();
        panelTutor1.setTutor(alumno.getTutor());
        panelTutor2.setTutor(alumno.getTutor2());
        tfDni.setText(alumno.getDni());
        tfExpediente.setText(alumno.getExpediente());
        tfNacionalidad.setText(alumno.getNacionalidad());
        if (alumno.getFechaNacimiento() != null) {
            tfFechaNac.setDate(alumno.getFechaNacimiento().getTime());
        }
        cbSexo.setSelectedIndex(alumno.getSexo().equals("M") ? 1 : 0);
        tfPaisNac.setText(alumno.getPaisNacimiento());
        tfLocNac.setText(alumno.getLocalidadNacimiento());
        tfProvNac.setText(alumno.getProvinciaNacimiento());
        tfTelUrg.setText(alumno.getTelefonoUrgencia());
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                asignarAlumnoPestanas(alumno);
                firePropertyChange("alumno", null, alumno);
                cargarPestanaSeleccionada();
                getControl().resetearCambios();
                getControl().habilitar(true);
            }
        });
    }

    private void cargarPestanaSeleccionada() {
        Component comp = panelPestanas.getSelectedComponent();
        if (comp instanceof ICargable) {
            ((ICargable) comp).cargar();
        } else if (comp instanceof JTabbedPane) {
            JTabbedPane subPestanas = (JTabbedPane) comp;
            comp = subPestanas.getSelectedComponent();
            if (comp instanceof ICargable) {
                ((ICargable) comp).cargar();
            }
        } else if (comp instanceof JPanel) {
            JPanel panel = (JPanel) comp;
            for (Component c : panel.getComponents()) {
                if (c instanceof ICargable) {
                    ((ICargable) c).cargar();
                }
            }
        }
    }

    private void asignarAlumnoPestanas(Alumno alumno) {
        for (Component comp : panelPestanas.getComponents()) {
            asignarAlumnoComponente(comp, alumno);
        }
    }

    private void asignarAlumnoComponente(Component comp, Alumno alumno) {
        if (comp instanceof IFiltrableAlumno) {
            ((IFiltrableAlumno) comp).setAlumno(alumno);
        } else if (comp instanceof JTabbedPane) {
            JTabbedPane subPestanas = (JTabbedPane) comp;
            for (Component c : subPestanas.getComponents()) {
                asignarAlumnoComponente(c, alumno);
            }
        } else if (comp instanceof JPanel) {
            JPanel panel = (JPanel) comp;
            for (Component c : panel.getComponents()) {
                if (c instanceof IFiltrableAlumno) {
                    ((IFiltrableAlumno) c).setAlumno(alumno);
                } else if (c instanceof JPanel || c instanceof JTabbedPane) {
                    asignarAlumnoComponente(c, alumno);
                }
            }
        }
    }

    private class GuardarTask extends org.jdesktop.application.Task<Boolean, Void> {

        ArrayList<String> mensajes = new ArrayList<String>();

        GuardarTask(org.jdesktop.application.Application app) {
            super(app);
            if (alumno == null) {
                cancel(true);
            }
        }

        @Override
        protected Boolean doInBackground() {
            boolean ret = false;
            alumno.setUnidad((Unidad) modeloComboBoxUnidades.getSelectedItem());
            alumno.setNombre(tfNombre.getText());
            alumno.setApellido1(tfApellido1.getText());
            alumno.setApellido2(tfApellido2.getText());
            alumno.setNumeroEscolar(tfNumEscolar.getText());
            alumno.setCodigo(Num.getInt(tfNumSeneca.getText()));
            alumno.setCodFaltas(tfCodFaltas.getText().trim());
            alumno.setBilingue(cbBilingue.isSelected());
            alumno.setRepetidor(cbRepetidor.isSelected());
            alumno.setDicu(cbDicu.isSelected());
            alumno.setEmail(tfEmail.getText());
            alumno.setTelefono(tfTelefono.getText());
            alumno.setSms(tfSms.getText());
            alumno.setDireccion(tfDireccion.getText());
            alumno.setCp(tfCP.getText());
            alumno.setPoblacion(tfPoblacion.getText());
            alumno.setNotificar(cbNotificarImpreso.isSelected(), cbNotificarEmail.isSelected(), cbNotificarSMS.isSelected(), cbNotificarTelefono.isSelected(), cbNotificarPresencial.isSelected());
            alumno.setObservaciones(taObservaciones.getText());
            alumno.setTutor(panelTutor1.getTutor());
            alumno.setTutor2(panelTutor2.getTutor());
            alumno.setDni(tfDni.getText());
            alumno.setExpediente(tfExpediente.getText());
            alumno.setNacionalidad(tfNacionalidad.getText());
            alumno.setFechaNacimiento(Fechas.toGregorianCalendar(tfFechaNac.getDate()));
            alumno.setSexo(cbSexo.getSelectedIndex() == 1 ? "M" : "H");
            alumno.setPaisNacimiento(tfPaisNac.getText());
            alumno.setLocalidadNacimiento(tfLocNac.getText());
            alumno.setProvinciaNacimiento(tfProvNac.getText());
            alumno.setTelefonoUrgencia(tfTelUrg.getText());
            ret = alumno.guardar();
            mensajes.addAll(alumno.getMensajesUltimaOperacion());
            if (alumno.getUnidadAnterior() != null && !alumno.getUnidad().equals(alumno.getUnidadAnterior())) {
                if (panelPestanas.getSelectedComponent() == panelVisionHorario1) {
                    panelVisionHorario1.cargar();
                }
            }
            return ret;
        }

        @Override
        protected void succeeded(Boolean result) {
            if (result) {
                setMessage("Alumno guardado correctamente.");
                MaimonidesUtil.mostrarVentanaListaDatos("Alumno guardado correctamente.", mensajes);
                getControl().resetearCambios();
                //Hay que hacer esto por si se ha asignado la id ahora
                firePropertyChange("guardado", false, true);
            } else {
                setMessage("Error guardando datos de alumno.");
                MaimonidesUtil.mostrarVentanaListaDatos("Error guardando datos de alumno.", mensajes);
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
        java.awt.GridBagConstraints gridBagConstraints;

        panelBusqueda = new com.codeko.apps.maimonides.alumnos.PanelBusquedaAlumnos();
        barraHerramientas = new javax.swing.JToolBar();
        bBuscarAlumno = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        bActualizar = new javax.swing.JButton();
        bNuevo = new javax.swing.JButton();
        bGuardar = new javax.swing.JButton();
        bBorrar = new javax.swing.JButton();
        bApoyos = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        bConvivencia = new javax.swing.JToggleButton();
        pCentral = new javax.swing.JPanel();
        panelInfoBasica = new javax.swing.JPanel();
        lCabeceraAlumno = new javax.swing.JLabel();
        panelPestanas = new javax.swing.JTabbedPane();
        panelCargable1 = new com.codeko.apps.maimonides.PanelCargable();
        panelResumenAsistenciaPorMateria1 = new com.codeko.apps.maimonides.partes.informes.alumnos.PanelResumenAsistenciaPorMateria();
        panelResumenFaltas1 = new com.codeko.apps.maimonides.partes.informes.alumnos.PanelResumenFaltas();
        panelResumen1 = new com.codeko.apps.maimonides.convivencia.PanelResumen();
        panelDatos = new javax.swing.JPanel();
        panelOtrosDatos = new javax.swing.JPanel();
        pNotificar = new javax.swing.JPanel();
        cbNotificarImpreso = new javax.swing.JCheckBox();
        cbNotificarEmail = new javax.swing.JCheckBox();
        cbNotificarSMS = new javax.swing.JCheckBox();
        cbNotificarTelefono = new javax.swing.JCheckBox();
        cbNotificarPresencial = new javax.swing.JCheckBox();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel4 = new javax.swing.JPanel();
        pDatos = new javax.swing.JPanel();
        lAlumno = new javax.swing.JLabel();
        tfNombre = new javax.swing.JTextField();
        tfApellido1 = new javax.swing.JTextField();
        tfApellido2 = new javax.swing.JTextField();
        lNumEscolar = new javax.swing.JLabel();
        tfNumEscolar = new javax.swing.JTextField();
        lUnidad = new javax.swing.JLabel();
        cbUnidades = new javax.swing.JComboBox();
        lCurso = new javax.swing.JLabel();
        cbBilingue = new javax.swing.JCheckBox();
        cbRepetidor = new javax.swing.JCheckBox();
        cbDicu = new javax.swing.JCheckBox();
        lDireccion = new javax.swing.JLabel();
        tfDireccion = new javax.swing.JTextField();
        lCp = new javax.swing.JLabel();
        tfCP = new javax.swing.JTextField();
        tfEmail = new javax.swing.JTextField();
        lEmail = new javax.swing.JLabel();
        tfTelefono = new javax.swing.JTextField();
        tfSms = new javax.swing.JTextField();
        lTelefono = new javax.swing.JLabel();
        lSms = new javax.swing.JLabel();
        lPoblacion = new javax.swing.JLabel();
        tfPoblacion = new javax.swing.JTextField();
        lProvincia = new javax.swing.JLabel();
        lNumSeneca = new javax.swing.JLabel();
        tfNumSeneca = new javax.swing.JTextField();
        tfTelUrg = new javax.swing.JTextField();
        lTelUrg = new javax.swing.JLabel();
        tfCodFaltas = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        panelDatosExtra = new javax.swing.JPanel();
        lDni = new javax.swing.JLabel();
        tfDni = new javax.swing.JTextField();
        lSexo = new javax.swing.JLabel();
        cbSexo = new javax.swing.JComboBox();
        lNacionalidad = new javax.swing.JLabel();
        tfNacionalidad = new javax.swing.JTextField();
        lFechaNac = new javax.swing.JLabel();
        tfFechaNac = new org.jdesktop.swingx.JXDatePicker();
        lExpediente = new javax.swing.JLabel();
        tfExpediente = new javax.swing.JTextField();
        jXTitledSeparator1 = new org.jdesktop.swingx.JXTitledSeparator();
        lPaisNacimiento = new javax.swing.JLabel();
        tfPaisNac = new javax.swing.JTextField();
        lProvNac = new javax.swing.JLabel();
        tfProvNac = new javax.swing.JTextField();
        lLocNac = new javax.swing.JLabel();
        tfLocNac = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        panelTutor1 = new com.codeko.apps.maimonides.alumnos.PanelTutor();
        panelTutor2 = new com.codeko.apps.maimonides.alumnos.PanelTutor();
        jPanel1 = new javax.swing.JPanel();
        scrollObservaciones = new javax.swing.JScrollPane();
        taObservaciones = new javax.swing.JTextArea();
        panelComunicacion = new javax.swing.JTabbedPane();
        panelEnvioEmail1 = new com.codeko.apps.maimonides.alumnos.PanelEnvioEmail();
        panelEnvioSMS1 = new com.codeko.apps.maimonides.alumnos.PanelEnvioSMS();
        panelEnvioSMSVoz1 = new com.codeko.apps.maimonides.alumnos.PanelEnvioSMSVoz();
        panelCorrespondencia1 = new com.codeko.apps.maimonides.alumnos.PanelCorrespondencia();
        panelHorario = new javax.swing.JPanel();
        panelVisionHorario1 = new com.codeko.apps.maimonides.horarios.PanelVisionHorario();
        barraHerramientasHorario = new javax.swing.JToolBar();
        bImprimir = new javax.swing.JButton();
        panelInfoBloque1 = new com.codeko.apps.maimonides.horarios.PanelInfoBloque();
        panelAsistencias = new com.codeko.apps.maimonides.PanelCargable();
        panelPestanasAsistencia = new javax.swing.JTabbedPane();
        panelFaltasAlumno1 = new com.codeko.apps.maimonides.partes.PanelFaltasAlumno();

        panelBusqueda.setName("panelBusqueda"); // NOI18N
        panelBusqueda.setMostrarOpcionesVentana(true);

        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        barraHerramientas.setFloatable(false);
        barraHerramientas.setRollover(true);
        barraHerramientas.setName("barraHerramientas"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getActionMap(PanelFichaAlumno.class, this);
        bBuscarAlumno.setAction(actionMap.get("buscarAlumno")); // NOI18N
        bBuscarAlumno.setFocusable(false);
        bBuscarAlumno.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bBuscarAlumno.setName("bBuscarAlumno"); // NOI18N
        bBuscarAlumno.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraHerramientas.add(bBuscarAlumno);

        jSeparator2.setName("jSeparator2"); // NOI18N
        barraHerramientas.add(jSeparator2);

        bActualizar.setAction(actionMap.get("actualizarAlumno")); // NOI18N
        bActualizar.setFocusable(false);
        bActualizar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bActualizar.setName("bActualizar"); // NOI18N
        bActualizar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraHerramientas.add(bActualizar);

        bNuevo.setAction(actionMap.get("nuevoAlumno")); // NOI18N
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

        bBorrar.setAction(actionMap.get("borrarAlumnos")); // NOI18N
        bBorrar.setFocusable(false);
        bBorrar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bBorrar.setName("bBorrar"); // NOI18N
        bBorrar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraHerramientas.add(bBorrar);

        bApoyos.setAction(actionMap.get("mostrarEditorApoyos")); // NOI18N
        bApoyos.setFocusable(false);
        bApoyos.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bApoyos.setName("bApoyos"); // NOI18N
        bApoyos.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraHerramientas.add(bApoyos);

        jSeparator1.setName("jSeparator1"); // NOI18N
        barraHerramientas.add(jSeparator1);

        bConvivencia.setAction(actionMap.get("mostrarConvivencia")); // NOI18N
        bConvivencia.setFocusable(false);
        bConvivencia.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bConvivencia.setName("bConvivencia"); // NOI18N
        bConvivencia.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraHerramientas.add(bConvivencia);

        add(barraHerramientas, java.awt.BorderLayout.PAGE_START);

        pCentral.setName("pCentral"); // NOI18N
        pCentral.setLayout(new java.awt.BorderLayout());

        panelInfoBasica.setName("panelInfoBasica"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getResourceMap(PanelFichaAlumno.class);
        lCabeceraAlumno.setFont(resourceMap.getFont("lCabeceraAlumno.font")); // NOI18N
        lCabeceraAlumno.setText(resourceMap.getString("lCabeceraAlumno.text")); // NOI18N
        lCabeceraAlumno.setName("lCabeceraAlumno"); // NOI18N

        javax.swing.GroupLayout panelInfoBasicaLayout = new javax.swing.GroupLayout(panelInfoBasica);
        panelInfoBasica.setLayout(panelInfoBasicaLayout);
        panelInfoBasicaLayout.setHorizontalGroup(
            panelInfoBasicaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelInfoBasicaLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lCabeceraAlumno, javax.swing.GroupLayout.DEFAULT_SIZE, 814, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelInfoBasicaLayout.setVerticalGroup(
            panelInfoBasicaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelInfoBasicaLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lCabeceraAlumno)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pCentral.add(panelInfoBasica, java.awt.BorderLayout.PAGE_START);

        panelPestanas.setName("panelPestanas"); // NOI18N
        panelPestanas.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                panelPestanasStateChanged(evt);
            }
        });

        panelCargable1.setName("panelCargable1"); // NOI18N
        panelCargable1.setLayout(new java.awt.GridBagLayout());

        panelResumenAsistenciaPorMateria1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("panelResumenAsistenciaPorMateria1.border.title"))); // NOI18N
        panelResumenAsistenciaPorMateria1.setName("panelResumenAsistenciaPorMateria1"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        panelCargable1.add(panelResumenAsistenciaPorMateria1, gridBagConstraints);

        panelResumenFaltas1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("panelResumenFaltas1.border.title"))); // NOI18N
        panelResumenFaltas1.setName("panelResumenFaltas1"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panelCargable1.add(panelResumenFaltas1, gridBagConstraints);

        panelResumen1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("panelResumen1.border.title"))); // NOI18N
        panelResumen1.setName("panelResumen1"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        panelCargable1.add(panelResumen1, gridBagConstraints);

        panelPestanas.addTab(resourceMap.getString("panelCargable1.TabConstraints.tabTitle"), resourceMap.getIcon("panelCargable1.TabConstraints.tabIcon"), panelCargable1); // NOI18N

        panelDatos.setName("panelDatos"); // NOI18N

        panelOtrosDatos.setName("panelOtrosDatos"); // NOI18N

        pNotificar.setToolTipText(resourceMap.getString("pNotificar.toolTipText")); // NOI18N
        pNotificar.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("pNotificar.border.title"))); // NOI18N
        pNotificar.setName("pNotificar"); // NOI18N

        cbNotificarImpreso.setText(resourceMap.getString("cbNotificarImpreso.text")); // NOI18N
        cbNotificarImpreso.setName("cbNotificarImpreso"); // NOI18N

        cbNotificarEmail.setText(resourceMap.getString("cbNotificarEmail.text")); // NOI18N
        cbNotificarEmail.setName("cbNotificarEmail"); // NOI18N

        cbNotificarSMS.setText(resourceMap.getString("cbNotificarSMS.text")); // NOI18N
        cbNotificarSMS.setName("cbNotificarSMS"); // NOI18N

        cbNotificarTelefono.setText(resourceMap.getString("cbNotificarTelefono.text")); // NOI18N
        cbNotificarTelefono.setName("cbNotificarTelefono"); // NOI18N

        cbNotificarPresencial.setText(resourceMap.getString("cbNotificarPresencial.text")); // NOI18N
        cbNotificarPresencial.setName("cbNotificarPresencial"); // NOI18N

        javax.swing.GroupLayout pNotificarLayout = new javax.swing.GroupLayout(pNotificar);
        pNotificar.setLayout(pNotificarLayout);
        pNotificarLayout.setHorizontalGroup(
            pNotificarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pNotificarLayout.createSequentialGroup()
                .addContainerGap(142, Short.MAX_VALUE)
                .addComponent(cbNotificarImpreso)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbNotificarEmail)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbNotificarSMS)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbNotificarTelefono)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbNotificarPresencial))
        );
        pNotificarLayout.setVerticalGroup(
            pNotificarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pNotificarLayout.createSequentialGroup()
                .addGroup(pNotificarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbNotificarImpreso)
                    .addComponent(cbNotificarEmail)
                    .addComponent(cbNotificarSMS)
                    .addComponent(cbNotificarTelefono)
                    .addComponent(cbNotificarPresencial))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.setName("jTabbedPane1"); // NOI18N

        jPanel4.setName("jPanel4"); // NOI18N

        pDatos.setName("pDatos"); // NOI18N

        lAlumno.setDisplayedMnemonic('a');
        lAlumno.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lAlumno.setLabelFor(tfNombre);
        lAlumno.setText(resourceMap.getString("lAlumno.text")); // NOI18N
        lAlumno.setName("lAlumno"); // NOI18N

        tfNombre.setText(resourceMap.getString("tfNombre.text")); // NOI18N
        tfNombre.setToolTipText(resourceMap.getString("tfNombre.toolTipText")); // NOI18N
        tfNombre.setName("tfNombre"); // NOI18N

        tfApellido1.setText(resourceMap.getString("tfApellido1.text")); // NOI18N
        tfApellido1.setToolTipText(resourceMap.getString("tfApellido1.toolTipText")); // NOI18N
        tfApellido1.setName("tfApellido1"); // NOI18N

        tfApellido2.setText(resourceMap.getString("tfApellido2.text")); // NOI18N
        tfApellido2.setToolTipText(resourceMap.getString("tfApellido2.toolTipText")); // NOI18N
        tfApellido2.setName("tfApellido2"); // NOI18N

        lNumEscolar.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lNumEscolar.setText(resourceMap.getString("lNumEscolar.text")); // NOI18N
        lNumEscolar.setName("lNumEscolar"); // NOI18N

        tfNumEscolar.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfNumEscolar.setText(resourceMap.getString("tfNumEscolar.text")); // NOI18N
        tfNumEscolar.setName("tfNumEscolar"); // NOI18N

        lUnidad.setDisplayedMnemonic('u');
        lUnidad.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lUnidad.setLabelFor(cbUnidades);
        lUnidad.setText(resourceMap.getString("lUnidad.text")); // NOI18N
        lUnidad.setName("lUnidad"); // NOI18N

        cbUnidades.setModel(modeloComboBoxUnidades);
        cbUnidades.setName("cbUnidades"); // NOI18N

        lCurso.setText(resourceMap.getString("lCurso.text")); // NOI18N
        lCurso.setName("lCurso"); // NOI18N

        cbBilingue.setMnemonic('b');
        cbBilingue.setText(resourceMap.getString("cbBilingue.text")); // NOI18N
        cbBilingue.setName("cbBilingue"); // NOI18N

        cbRepetidor.setMnemonic('r');
        cbRepetidor.setText(resourceMap.getString("cbRepetidor.text")); // NOI18N
        cbRepetidor.setName("cbRepetidor"); // NOI18N

        cbDicu.setMnemonic('d');
        cbDicu.setText(resourceMap.getString("cbDicu.text")); // NOI18N
        cbDicu.setName("cbDicu"); // NOI18N

        lDireccion.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lDireccion.setText(resourceMap.getString("lDireccion.text")); // NOI18N
        lDireccion.setName("lDireccion"); // NOI18N

        tfDireccion.setText(resourceMap.getString("tfDireccion.text")); // NOI18N
        tfDireccion.setName("tfDireccion"); // NOI18N

        lCp.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lCp.setText(resourceMap.getString("lCp.text")); // NOI18N
        lCp.setName("lCp"); // NOI18N

        tfCP.setText(resourceMap.getString("tfCP.text")); // NOI18N
        tfCP.setName("tfCP"); // NOI18N

        tfEmail.setText(resourceMap.getString("tfEmail.text")); // NOI18N
        tfEmail.setName("tfEmail"); // NOI18N

        lEmail.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lEmail.setText(resourceMap.getString("lEmail.text")); // NOI18N
        lEmail.setName("lEmail"); // NOI18N

        tfTelefono.setDocument(new NumericDocument(0, false));
        tfTelefono.setText(resourceMap.getString("tfTelefono.text")); // NOI18N
        tfTelefono.setName("tfTelefono"); // NOI18N

        tfSms.setDocument(new NumericDocument(0, false));
        tfSms.setText(resourceMap.getString("tfSms.text")); // NOI18N
        tfSms.setName("tfSms"); // NOI18N

        lTelefono.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lTelefono.setText(resourceMap.getString("lTelefono.text")); // NOI18N
        lTelefono.setName("lTelefono"); // NOI18N

        lSms.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lSms.setText(resourceMap.getString("lSms.text")); // NOI18N
        lSms.setName("lSms"); // NOI18N

        lPoblacion.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lPoblacion.setLabelFor(tfPoblacion);
        lPoblacion.setText(resourceMap.getString("lPoblacion.text")); // NOI18N
        lPoblacion.setName("lPoblacion"); // NOI18N

        tfPoblacion.setText(resourceMap.getString("tfPoblacion.text")); // NOI18N
        tfPoblacion.setName("tfPoblacion"); // NOI18N

        lProvincia.setText(resourceMap.getString("lProvincia.text")); // NOI18N
        lProvincia.setName("lProvincia"); // NOI18N

        lNumSeneca.setText(resourceMap.getString("lNumSeneca.text")); // NOI18N
        lNumSeneca.setName("lNumSeneca"); // NOI18N

        tfNumSeneca.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfNumSeneca.setText(resourceMap.getString("tfNumSeneca.text")); // NOI18N
        tfNumSeneca.setToolTipText(resourceMap.getString("tfNumSeneca.toolTipText")); // NOI18N
        tfNumSeneca.setName("tfNumSeneca"); // NOI18N

        tfTelUrg.setText(resourceMap.getString("tfTelUrg.text")); // NOI18N
        tfTelUrg.setName("tfTelUrg"); // NOI18N

        lTelUrg.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lTelUrg.setText(resourceMap.getString("lTelUrg.text")); // NOI18N
        lTelUrg.setName("lTelUrg"); // NOI18N

        tfCodFaltas.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfCodFaltas.setToolTipText(resourceMap.getString("tfCodFaltas.toolTipText")); // NOI18N
        tfCodFaltas.setName("tfCodFaltas"); // NOI18N

        javax.swing.GroupLayout pDatosLayout = new javax.swing.GroupLayout(pDatos);
        pDatos.setLayout(pDatosLayout);
        pDatosLayout.setHorizontalGroup(
            pDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pDatosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(lNumEscolar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lAlumno, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lUnidad, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lDireccion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lCp, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lPoblacion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lEmail, javax.swing.GroupLayout.DEFAULT_SIZE, 66, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tfPoblacion, javax.swing.GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE)
                    .addGroup(pDatosLayout.createSequentialGroup()
                        .addGroup(pDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(tfNumEscolar)
                            .addComponent(tfNombre, javax.swing.GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE)
                            .addComponent(cbUnidades, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pDatosLayout.createSequentialGroup()
                                .addComponent(tfApellido1, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfApellido2, javax.swing.GroupLayout.DEFAULT_SIZE, 175, Short.MAX_VALUE))
                            .addGroup(pDatosLayout.createSequentialGroup()
                                .addComponent(lNumSeneca)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfNumSeneca, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfCodFaltas, javax.swing.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE))
                            .addComponent(lCurso, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(pDatosLayout.createSequentialGroup()
                        .addComponent(tfCP, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lProvincia, javax.swing.GroupLayout.DEFAULT_SIZE, 276, Short.MAX_VALUE))
                    .addComponent(tfDireccion, javax.swing.GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE)
                    .addComponent(tfEmail, javax.swing.GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE)))
            .addGroup(pDatosLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(pDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lSms)
                    .addComponent(lTelefono, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(tfSms)
                    .addComponent(tfTelefono, javax.swing.GroupLayout.DEFAULT_SIZE, 107, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pDatosLayout.createSequentialGroup()
                        .addComponent(lTelUrg)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfTelUrg, javax.swing.GroupLayout.DEFAULT_SIZE, 211, Short.MAX_VALUE))
                    .addGroup(pDatosLayout.createSequentialGroup()
                        .addComponent(cbDicu)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cbRepetidor)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cbBilingue))))
        );
        pDatosLayout.setVerticalGroup(
            pDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pDatosLayout.createSequentialGroup()
                .addGroup(pDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lNumEscolar)
                    .addComponent(tfNumEscolar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lNumSeneca)
                    .addComponent(tfNumSeneca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfCodFaltas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lAlumno)
                    .addComponent(tfNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfApellido1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfApellido2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbUnidades, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lCurso)
                    .addComponent(lUnidad))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfDireccion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lDireccion))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lProvincia)
                    .addComponent(lCp)
                    .addComponent(tfCP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lPoblacion)
                    .addComponent(tfPoblacion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lEmail)
                    .addComponent(tfEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lTelefono)
                    .addComponent(tfTelefono, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lTelUrg)
                    .addComponent(tfTelUrg, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lSms)
                    .addComponent(tfSms, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbRepetidor)
                    .addComponent(cbBilingue)
                    .addComponent(cbDicu))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pDatos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pDatos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel4.TabConstraints.tabTitle"), jPanel4); // NOI18N

        jPanel3.setName("jPanel3"); // NOI18N

        panelDatosExtra.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("panelDatosExtra.border.title"))); // NOI18N
        panelDatosExtra.setName("panelDatosExtra"); // NOI18N
        panelDatosExtra.setLayout(new java.awt.GridBagLayout());

        lDni.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lDni.setText(resourceMap.getString("lDni.text")); // NOI18N
        lDni.setName("lDni"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        panelDatosExtra.add(lDni, gridBagConstraints);

        tfDni.setText(resourceMap.getString("tfDni.text")); // NOI18N
        tfDni.setName("tfDni"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 100;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        panelDatosExtra.add(tfDni, gridBagConstraints);

        lSexo.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lSexo.setText(resourceMap.getString("lSexo.text")); // NOI18N
        lSexo.setName("lSexo"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        panelDatosExtra.add(lSexo, gridBagConstraints);

        cbSexo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Hombre", "Mujer" }));
        cbSexo.setName("cbSexo"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        panelDatosExtra.add(cbSexo, gridBagConstraints);

        lNacionalidad.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lNacionalidad.setText(resourceMap.getString("lNacionalidad.text")); // NOI18N
        lNacionalidad.setName("lNacionalidad"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        panelDatosExtra.add(lNacionalidad, gridBagConstraints);

        tfNacionalidad.setText(resourceMap.getString("tfNacionalidad.text")); // NOI18N
        tfNacionalidad.setName("tfNacionalidad"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        panelDatosExtra.add(tfNacionalidad, gridBagConstraints);

        lFechaNac.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lFechaNac.setText(resourceMap.getString("lFechaNac.text")); // NOI18N
        lFechaNac.setName("lFechaNac"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        panelDatosExtra.add(lFechaNac, gridBagConstraints);

        tfFechaNac.setName("tfFechaNac"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        panelDatosExtra.add(tfFechaNac, gridBagConstraints);

        lExpediente.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lExpediente.setText(resourceMap.getString("lExpediente.text")); // NOI18N
        lExpediente.setName("lExpediente"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        panelDatosExtra.add(lExpediente, gridBagConstraints);

        tfExpediente.setText(resourceMap.getString("tfExpediente.text")); // NOI18N
        tfExpediente.setName("tfExpediente"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        panelDatosExtra.add(tfExpediente, gridBagConstraints);

        jXTitledSeparator1.setTitle(resourceMap.getString("jXTitledSeparator1.title")); // NOI18N
        jXTitledSeparator1.setName("jXTitledSeparator1"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        panelDatosExtra.add(jXTitledSeparator1, gridBagConstraints);

        lPaisNacimiento.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lPaisNacimiento.setText(resourceMap.getString("lPaisNacimiento.text")); // NOI18N
        lPaisNacimiento.setName("lPaisNacimiento"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        panelDatosExtra.add(lPaisNacimiento, gridBagConstraints);

        tfPaisNac.setText(resourceMap.getString("tfPaisNac.text")); // NOI18N
        tfPaisNac.setName("tfPaisNac"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        panelDatosExtra.add(tfPaisNac, gridBagConstraints);

        lProvNac.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lProvNac.setText(resourceMap.getString("lProvNac.text")); // NOI18N
        lProvNac.setName("lProvNac"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        panelDatosExtra.add(lProvNac, gridBagConstraints);

        tfProvNac.setText(resourceMap.getString("tfProvNac.text")); // NOI18N
        tfProvNac.setName("tfProvNac"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        panelDatosExtra.add(tfProvNac, gridBagConstraints);

        lLocNac.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lLocNac.setText(resourceMap.getString("lLocNac.text")); // NOI18N
        lLocNac.setName("lLocNac"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        panelDatosExtra.add(lLocNac, gridBagConstraints);

        tfLocNac.setText(resourceMap.getString("tfLocNac.text")); // NOI18N
        tfLocNac.setName("tfLocNac"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        panelDatosExtra.add(tfLocNac, gridBagConstraints);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelDatosExtra, javax.swing.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelDatosExtra, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(74, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel3.TabConstraints.tabTitle"), jPanel3); // NOI18N

        jPanel2.setName("jPanel2"); // NOI18N

        panelTutor1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("panelTutor1.border.title"))); // NOI18N
        panelTutor1.setName("panelTutor1"); // NOI18N

        panelTutor2.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("panelTutor2.border.title"))); // NOI18N
        panelTutor2.setName("panelTutor2"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelTutor2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE)
                    .addComponent(panelTutor1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelTutor1, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelTutor2, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(91, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel2.TabConstraints.tabTitle"), jPanel2); // NOI18N

        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setLayout(new java.awt.BorderLayout());

        scrollObservaciones.setName("scrollObservaciones"); // NOI18N

        taObservaciones.setColumns(20);
        taObservaciones.setLineWrap(true);
        taObservaciones.setRows(5);
        taObservaciones.setName("taObservaciones"); // NOI18N
        scrollObservaciones.setViewportView(taObservaciones);

        jPanel1.add(scrollObservaciones, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab(resourceMap.getString("jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        javax.swing.GroupLayout panelOtrosDatosLayout = new javax.swing.GroupLayout(panelOtrosDatos);
        panelOtrosDatos.setLayout(panelOtrosDatosLayout);
        panelOtrosDatosLayout.setHorizontalGroup(
            panelOtrosDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelOtrosDatosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelOtrosDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pNotificar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        panelOtrosDatosLayout.setVerticalGroup(
            panelOtrosDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelOtrosDatosLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 343, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pNotificar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panelDatosLayout = new javax.swing.GroupLayout(panelDatos);
        panelDatos.setLayout(panelDatosLayout);
        panelDatosLayout.setHorizontalGroup(
            panelDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelOtrosDatos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        panelDatosLayout.setVerticalGroup(
            panelDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelOtrosDatos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        panelPestanas.addTab(resourceMap.getString("panelDatos.TabConstraints.tabTitle"), resourceMap.getIcon("panelDatos.TabConstraints.tabIcon"), panelDatos); // NOI18N

        panelComunicacion.setName("panelComunicacion"); // NOI18N

        panelEnvioEmail1.setName("panelEnvioEmail1"); // NOI18N
        panelComunicacion.addTab(resourceMap.getString("panelEnvioEmail1.TabConstraints.tabTitle"), panelEnvioEmail1); // NOI18N

        panelEnvioSMS1.setName("panelEnvioSMS1"); // NOI18N
        panelComunicacion.addTab(resourceMap.getString("panelEnvioSMS1.TabConstraints.tabTitle"), panelEnvioSMS1); // NOI18N

        panelEnvioSMSVoz1.setName("panelEnvioSMSVoz1"); // NOI18N
        panelComunicacion.addTab(resourceMap.getString("panelEnvioSMSVoz1.TabConstraints.tabTitle"), panelEnvioSMSVoz1); // NOI18N

        panelCorrespondencia1.setModoFichaAlumno(true);
        panelCorrespondencia1.setName("panelCorrespondencia1"); // NOI18N
        panelComunicacion.addTab(resourceMap.getString("panelCorrespondencia1.TabConstraints.tabTitle"), panelCorrespondencia1); // NOI18N

        panelPestanas.addTab(resourceMap.getString("panelComunicacion.TabConstraints.tabTitle"), resourceMap.getIcon("panelComunicacion.TabConstraints.tabIcon"), panelComunicacion); // NOI18N

        panelHorario.setName("panelHorario"); // NOI18N
        panelHorario.setLayout(new java.awt.BorderLayout());

        panelVisionHorario1.setName("panelVisionHorario1"); // NOI18N
        panelHorario.add(panelVisionHorario1, java.awt.BorderLayout.CENTER);

        barraHerramientasHorario.setRollover(true);
        barraHerramientasHorario.setName("barraHerramientasHorario"); // NOI18N

        bImprimir.setAction(actionMap.get("imprimirHorario")); // NOI18N
        bImprimir.setFocusable(false);
        bImprimir.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bImprimir.setName("bImprimir"); // NOI18N
        bImprimir.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraHerramientasHorario.add(bImprimir);

        panelInfoBloque1.setName("panelInfoBloque1"); // NOI18N
        barraHerramientasHorario.add(panelInfoBloque1);

        panelHorario.add(barraHerramientasHorario, java.awt.BorderLayout.PAGE_START);

        panelPestanas.addTab(resourceMap.getString("panelHorario.TabConstraints.tabTitle"), resourceMap.getIcon("panelHorario.TabConstraints.tabIcon"), panelHorario); // NOI18N

        panelAsistencias.setName("panelAsistencias"); // NOI18N
        panelAsistencias.setLayout(new java.awt.BorderLayout());

        panelPestanasAsistencia.setName("panelPestanasAsistencia"); // NOI18N

        panelFaltasAlumno1.setName("panelFaltasAlumno1"); // NOI18N
        panelPestanasAsistencia.addTab(resourceMap.getString("panelFaltasAlumno1.TabConstraints.tabTitle"), panelFaltasAlumno1); // NOI18N

        panelAsistencias.add(panelPestanasAsistencia, java.awt.BorderLayout.CENTER);

        panelPestanas.addTab(resourceMap.getString("panelAsistencias.TabConstraints.tabTitle"), resourceMap.getIcon("panelAsistencias.TabConstraints.tabIcon"), panelAsistencias); // NOI18N

        pCentral.add(panelPestanas, java.awt.BorderLayout.CENTER);

        add(pCentral, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void panelPestanasStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_panelPestanasStateChanged
        cargarPestanaSeleccionada();
    }//GEN-LAST:event_panelPestanasStateChanged
    boolean datosPendientesGuardar = false;

    public boolean isDatosPendientesGuardar() {
        return datosPendientesGuardar;
    }

    public void setDatosPendientesGuardar(boolean b) {
        b = b && alumno != null;
        boolean old = isDatosPendientesGuardar();
        this.datosPendientesGuardar = b;
        firePropertyChange("datosPendientesGuardar", old, isDatosPendientesGuardar());
    }

    @Action(block = Task.BlockingScope.APPLICATION, enabledProperty = "alumnoCargado")
    public Task actualizarAlumno() {
        return new ActualizarAlumnoTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class ActualizarAlumnoTask extends org.jdesktop.application.Task<Object, Void> {

        Alumno a = null;

        ActualizarAlumnoTask(org.jdesktop.application.Application app) {
            super(app);
            a = getAlumno();
            if (!setAlumno(null)) {
                cancel(false);
                a = null;
            }
        }

        @Override
        protected Object doInBackground() {
            try {
                if (a != null) {
                    Alumno b = Alumno.getAlumno(a.getId());
                    setAlumno(b);
                    a = null;
                }

            } catch (SQLException ex) {
                Logger.getLogger(PanelFichaAlumno.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        }

        @Override
        protected void succeeded(Object result) {
        }
    }
    private boolean alumnoCargado = false;

    public boolean isAlumnoCargado() {
        return alumnoCargado;
    }

    public void setAlumnoCargado(boolean b) {
        boolean old = isAlumnoCargado();
        this.alumnoCargado = b;
        firePropertyChange("alumnoCargado", old, isAlumnoCargado());
    }

    @Action(enabledProperty = "alumnoCargado")
    public void mostrarEditorApoyos() {
        if (getAlumno() != null) {
            PanelApoyos pa = new PanelApoyos();
            pa.setAlumno(getAlumno());
            JOptionPane.showMessageDialog(this, pa, "Apoyos de " + getAlumno().getNombreFormateado(), JOptionPane.PLAIN_MESSAGE);
        }
    }

    @Action(block = Task.BlockingScope.APPLICATION, enabledProperty = "alumnoCargado")
    public Task borrarAlumnos() {
        return new BorrarAlumnosTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class BorrarAlumnosTask extends org.jdesktop.application.Task<Boolean, Void> {

        Alumno a = null;

        BorrarAlumnosTask(org.jdesktop.application.Application app) {
            super(app);
            int op = JOptionPane.showConfirmDialog(MaimonidesApp.getApplication().getMainFrame(), "¿Esta seguro de que desea borrar al alumno " + getAlumno().getNombreFormateado() + "?", "Borrar", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (op == JOptionPane.YES_OPTION) {
                a = getAlumno();
            }
        }

        @Override
        protected Boolean doInBackground() {
            if (a != null) {
                return a.borrar();
            }
            return true;
        }

        @Override
        protected void succeeded(Boolean result) {
            if (a != null) {
                if (result) {
                    setMessage("Alumno borrado correctamente.");
                    setAlumno(null);
                } else {
                    setMessage("No se ha podido borrar el alumno.");
                }
            } else {
                setMessage("Operación cancelada.");
            }
        }
    }

    @Action
    public void buscarAlumno() {
        MaimonidesApp.getApplication().show(frameBusquedaAlumnos);
    }

    @Action
    public void cargarAlumno() {
        if (panelBusqueda.getAlumnoSeleccionado() != null) {
            if (!this.setAlumno(panelBusqueda.getAlumnoSeleccionado())) {
                panelBusqueda.setAlumnoSeleccionado(this.getAlumno());
            }
        }
    }

    @Override
    public boolean puedoSusituir() {
        return setAlumno(null);
    }

    @Action(enabledProperty = "alumnoCargado")
    public void mostrarConvivencia() {
        firePropertyChange("mostrarConvivencia", !bConvivencia.isSelected(), bConvivencia.isSelected());
    }

    @Action(block = Task.BlockingScope.ACTION, enabledProperty = "alumnoCargado")
    public Task imprimirHorario() {
        return new ImprimirHorarioTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class ImprimirHorarioTask extends org.jdesktop.application.Task<Object, Void> {

        ImprimirHorarioTask(org.jdesktop.application.Application app) {
            super(app);
        }

        @Override
        protected Object doInBackground() {
            MaimonidesBean bean = new MaimonidesBean();
            bean.addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                }
            });
            panelVisionHorario1.imprimir(bean);
            return null;  // return your result
        }

        @Override
        protected void succeeded(Object result) {
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bActualizar;
    private javax.swing.JButton bApoyos;
    private javax.swing.JButton bBorrar;
    private javax.swing.JButton bBuscarAlumno;
    private javax.swing.JToggleButton bConvivencia;
    private javax.swing.JButton bGuardar;
    private javax.swing.JButton bImprimir;
    private javax.swing.JButton bNuevo;
    private javax.swing.JToolBar barraHerramientas;
    private javax.swing.JToolBar barraHerramientasHorario;
    private javax.swing.JCheckBox cbBilingue;
    private javax.swing.JCheckBox cbDicu;
    private javax.swing.JCheckBox cbNotificarEmail;
    private javax.swing.JCheckBox cbNotificarImpreso;
    private javax.swing.JCheckBox cbNotificarPresencial;
    private javax.swing.JCheckBox cbNotificarSMS;
    private javax.swing.JCheckBox cbNotificarTelefono;
    private javax.swing.JCheckBox cbRepetidor;
    private javax.swing.JComboBox cbSexo;
    private javax.swing.JComboBox cbUnidades;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private org.jdesktop.swingx.JXTitledSeparator jXTitledSeparator1;
    private javax.swing.JLabel lAlumno;
    private javax.swing.JLabel lCabeceraAlumno;
    private javax.swing.JLabel lCp;
    private javax.swing.JLabel lCurso;
    private javax.swing.JLabel lDireccion;
    private javax.swing.JLabel lDni;
    private javax.swing.JLabel lEmail;
    private javax.swing.JLabel lExpediente;
    private javax.swing.JLabel lFechaNac;
    private javax.swing.JLabel lLocNac;
    private javax.swing.JLabel lNacionalidad;
    private javax.swing.JLabel lNumEscolar;
    private javax.swing.JLabel lNumSeneca;
    private javax.swing.JLabel lPaisNacimiento;
    private javax.swing.JLabel lPoblacion;
    private javax.swing.JLabel lProvNac;
    private javax.swing.JLabel lProvincia;
    private javax.swing.JLabel lSexo;
    private javax.swing.JLabel lSms;
    private javax.swing.JLabel lTelUrg;
    private javax.swing.JLabel lTelefono;
    private javax.swing.JLabel lUnidad;
    private javax.swing.JPanel pCentral;
    private javax.swing.JPanel pDatos;
    private javax.swing.JPanel pNotificar;
    private com.codeko.apps.maimonides.PanelCargable panelAsistencias;
    private com.codeko.apps.maimonides.alumnos.PanelBusquedaAlumnos panelBusqueda;
    private com.codeko.apps.maimonides.PanelCargable panelCargable1;
    private javax.swing.JTabbedPane panelComunicacion;
    private com.codeko.apps.maimonides.alumnos.PanelCorrespondencia panelCorrespondencia1;
    private javax.swing.JPanel panelDatos;
    private javax.swing.JPanel panelDatosExtra;
    private com.codeko.apps.maimonides.alumnos.PanelEnvioEmail panelEnvioEmail1;
    private com.codeko.apps.maimonides.alumnos.PanelEnvioSMS panelEnvioSMS1;
    private com.codeko.apps.maimonides.alumnos.PanelEnvioSMSVoz panelEnvioSMSVoz1;
    private com.codeko.apps.maimonides.partes.PanelFaltasAlumno panelFaltasAlumno1;
    private javax.swing.JPanel panelHorario;
    private javax.swing.JPanel panelInfoBasica;
    private com.codeko.apps.maimonides.horarios.PanelInfoBloque panelInfoBloque1;
    private javax.swing.JPanel panelOtrosDatos;
    private javax.swing.JTabbedPane panelPestanas;
    private javax.swing.JTabbedPane panelPestanasAsistencia;
    private com.codeko.apps.maimonides.convivencia.PanelResumen panelResumen1;
    private com.codeko.apps.maimonides.partes.informes.alumnos.PanelResumenAsistenciaPorMateria panelResumenAsistenciaPorMateria1;
    private com.codeko.apps.maimonides.partes.informes.alumnos.PanelResumenFaltas panelResumenFaltas1;
    private com.codeko.apps.maimonides.alumnos.PanelTutor panelTutor1;
    private com.codeko.apps.maimonides.alumnos.PanelTutor panelTutor2;
    private com.codeko.apps.maimonides.horarios.PanelVisionHorario panelVisionHorario1;
    private javax.swing.JScrollPane scrollObservaciones;
    private javax.swing.JTextArea taObservaciones;
    private javax.swing.JTextField tfApellido1;
    private javax.swing.JTextField tfApellido2;
    private javax.swing.JTextField tfCP;
    private javax.swing.JTextField tfCodFaltas;
    private javax.swing.JTextField tfDireccion;
    private javax.swing.JTextField tfDni;
    private javax.swing.JTextField tfEmail;
    private javax.swing.JTextField tfExpediente;
    private org.jdesktop.swingx.JXDatePicker tfFechaNac;
    private javax.swing.JTextField tfLocNac;
    private javax.swing.JTextField tfNacionalidad;
    private javax.swing.JTextField tfNombre;
    private javax.swing.JTextField tfNumEscolar;
    private javax.swing.JTextField tfNumSeneca;
    private javax.swing.JTextField tfPaisNac;
    private javax.swing.JTextField tfPoblacion;
    private javax.swing.JTextField tfProvNac;
    private javax.swing.JTextField tfSms;
    private javax.swing.JTextField tfTelUrg;
    private javax.swing.JTextField tfTelefono;
    // End of variables declaration//GEN-END:variables
}
