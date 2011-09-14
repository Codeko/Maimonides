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
 * MaimonidesView.java
 */
package com.codeko.apps.maimonides;

import com.codeko.apps.maimonides.ayuda.Ayuda;
import com.codeko.apps.maimonides.alumnos.PanelAlumnos2;
import com.codeko.apps.maimonides.alumnos.PanelControlAlumnos;
import com.codeko.apps.maimonides.alumnos.PanelCorrespondencia;
import com.codeko.apps.maimonides.alumnos.PanelMatriculaciones;
import com.codeko.apps.maimonides.asistencia.escolaridad.PanelPerdidaEscolaridadGlobal;
import com.codeko.apps.maimonides.asistencia.escolaridad.PanelPerdidaEscolaridadPorMaterias;
import com.codeko.apps.maimonides.calendario.PanelCalendarioEscolar;
import com.codeko.apps.maimonides.cartero.PanelEnvioNotificacionesManuales;
import com.codeko.apps.maimonides.conf.PanelConfiguraciones;
import com.codeko.apps.maimonides.convivencia.PanelConfiguracionConvivencia;
import com.codeko.apps.maimonides.convivencia.PanelExpulsiones;
import com.codeko.apps.maimonides.convivencia.PanelGeneracionExpulsiones;
import com.codeko.apps.maimonides.convivencia.PanelListaPartesConvivencia;
import com.codeko.apps.maimonides.convivencia.PanelNotificacionesConvivencia;
import com.codeko.apps.maimonides.convivencia.PanelPartes;
import com.codeko.apps.maimonides.convivencia.ParteConvivencia;
import com.codeko.apps.maimonides.convivencia.expulsiones.PanelListadoExpulsiones;
import com.codeko.apps.maimonides.convivencia.informes.PanelResumenConvivencia;
import com.codeko.apps.maimonides.cursos.PanelCursosGrupos;
import com.codeko.apps.maimonides.cursos.PanelGrupos;
import com.codeko.apps.maimonides.dependencias.PanelDependencias;
import com.codeko.apps.maimonides.digitalizacion.PanelDigitalizacion;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.IObjetoBD;
import com.codeko.apps.maimonides.horarios.PanelEditorHorariosDoble;
import com.codeko.apps.maimonides.horarios.seneca.ExportadorHorariosSeneca;
import com.codeko.apps.maimonides.importadores.PanelActualizacionSeneca;
import com.codeko.apps.maimonides.importadores.PanelImportacionInicial;
import com.codeko.apps.maimonides.mantenimiento.PanelCopiasSeguridad;
import com.codeko.apps.maimonides.materias.PanelMaterias;
import com.codeko.apps.maimonides.partes.PanelEditorPartes;


import com.codeko.apps.maimonides.partes.cartas.PanelCartas;
import com.codeko.apps.maimonides.partes.cartas.PanelCartasAsistencia;
import com.codeko.apps.maimonides.partes.divisiones.PanelDivisionAlumnosMultimateria;
import com.codeko.apps.maimonides.partes.informes.PanelInformesPartesNoDigitalizados;
import com.codeko.apps.maimonides.partes.informes.PanelPartesMedioDigitalizados;
import com.codeko.apps.maimonides.partes.informes.PanelPartesNoFirmados;
import com.codeko.apps.maimonides.partes.informes.PanelPartesPendientes;
import com.codeko.apps.maimonides.partes.informes.asistencia.PanelEvolucionAsistencia;
import com.codeko.apps.maimonides.partes.justificaciones.PanelJustificaciones;
import com.codeko.apps.maimonides.partes.justificaciones.PanelJustificacionesRapidas;
import com.codeko.apps.maimonides.profesores.PanelProfesores;
import com.codeko.apps.maimonides.seneca.PanelExportacionSeneca;
import com.codeko.apps.maimonides.seneca.operaciones.envioFicherosFaltas.PanelDebugEnvioFicheros;
import com.codeko.apps.maimonides.usr.PanelEditorDatosUsuarioActual;
import com.codeko.apps.maimonides.usr.Permisos;
import com.codeko.apps.maimonides.usr.Rol;
import com.codeko.apps.maimonides.usr.Usuario;
import com.codeko.util.CTiempo;
import com.codeko.util.Obj;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.jdesktop.application.ApplicationAction;
import org.jdesktop.swingx.JXHyperlink;
import org.jdesktop.swingx.painter.ImagePainter;

/**
 * The application's main frame.
 */
public class MaimonidesView extends FrameView {

    ImagePainter imPainter = null;
    IPanel panelActual = null;
    boolean cambiandoUsuario = false;
    IObjetoBD elementoActivo = null;
    MaimonidesBean beanControl = new MaimonidesBean();
    org.jdesktop.application.ResourceMap rMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getResourceMap(MaimonidesView.class);
    ActionListener rolActivoActionPerformed = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!cambiandoUsuario) {
                Usuario u = MaimonidesApp.getApplication().getUsuario();
                if (u != null) {
                    if (e.getSource().equals(miRolesAdmin)) {
                        if (miRolesAdmin.isSelected()) {
                            u.setRolesEfectivos(u.getRolesEfectivos() | Rol.ROL_ADMIN);
                        } else {
                            u.setRolesEfectivos(u.getRolesEfectivos() ^ Rol.ROL_ADMIN);
                        }
                    } else if (e.getSource().equals(miRolesJE)) {
                        if (miRolesJE.isSelected()) {
                            u.setRolesEfectivos(u.getRolesEfectivos() | Rol.ROL_JEFE_ESTUDIOS);
                        } else {
                            u.setRolesEfectivos(u.getRolesEfectivos() ^ Rol.ROL_JEFE_ESTUDIOS);
                        }
                    } else if (e.getSource().equals(miRolesDirectivo)) {
                        if (miRolesDirectivo.isSelected()) {
                            u.setRolesEfectivos(u.getRolesEfectivos() | Rol.ROL_DIRECTIVO);
                        } else {
                            u.setRolesEfectivos(u.getRolesEfectivos() ^ Rol.ROL_DIRECTIVO);
                        }
                    } else if (e.getSource().equals(miRolesProfesor)) {
                        if (miRolesProfesor.isSelected()) {
                            u.setRolesEfectivos(u.getRolesEfectivos() | Rol.ROL_PROFESOR);
                        } else {
                            u.setRolesEfectivos(u.getRolesEfectivos() ^ Rol.ROL_PROFESOR);
                        }
                    }
                    MaimonidesApp.getApplication().setUsuario(u);
                }
            }
        }
    };

    public final MaimonidesBean getBeanControl() {
        return beanControl;
    }
    private PropertyChangeListener control;

    public IPanel getPanelActual() {
        return panelActual;
    }

    public boolean activarPanel(Class clase, String nombre) {
        boolean ret = false;
        int index = getIndexOfPanel(clase, nombre);
        if (index > -1) {
            Component c = panelPestanas.getComponentAt(index);
            panelPestanas.setSelectedComponent(c);
            activarAyuda(c);
            ret = true;
        }
        return ret;
    }

    public JComponent getPanel(Class clase, String nombre) {
        JComponent ret = null;
        int index = getIndexOfPanel(clase, nombre);
        if (index > -1) {
            JComponent c = (JComponent) panelPestanas.getComponentAt(index);
            panelPestanas.setSelectedComponent(c);
            activarAyuda(c);
            ret = c;
        }
        return ret;
    }

    public void activarAyuda(Component comp) {
        if (comp != null) {
            Ayuda.addHelpKey(this.getRootPane(), comp.getName());
        } else {
            Ayuda.addHelpKey(this.getRootPane());
        }
    }

    public void setPanelActual(IPanel panelActual, String titulo, Icon icono) {
        CTiempo t = new CTiempo(titulo);
        Component c = (Component) panelActual;
        if (!activarPanel(panelActual.getClass(), titulo)) {
            t.showTimer("activacion");
            JPanel tab = new JPanel();
            tab.setOpaque(false);
            tab.setLayout(new BorderLayout());
            JLabel tabLabel = new JLabel(titulo + "  ");
            tabLabel.setIcon(icono);
            final Component cFinal = c;
            tab.putClientProperty("panel", c);
            JXHyperlink tabCloseButton = new JXHyperlink();
            tabCloseButton.setHorizontalAlignment(SwingConstants.CENTER);
            Dimension closeButtonSize = new Dimension(15, 15);
            tabCloseButton.setPreferredSize(closeButtonSize);
            tabCloseButton.setIcon(rMap.getIcon("tab.close.icon"));
            tabCloseButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    int closeTabNumber = panelPestanas.indexOfComponent(cFinal);
                    if (cFinal instanceof IPanel) {
                        if (((IPanel) cFinal).puedoSusituir()) {
                            panelPestanas.removeTabAt(closeTabNumber);
                        }
                    } else {
                        panelPestanas.removeTabAt(closeTabNumber);
                    }
                    activarAyuda(panelPestanas.getSelectedComponent());
                }
            });
            tab.add(tabLabel, BorderLayout.WEST);
            tab.add(tabCloseButton, BorderLayout.EAST);
            panelPestanas.addTab(null, cFinal);
            panelPestanas.setTitleAt(panelPestanas.getTabCount() - 1, titulo);
            panelPestanas.setTabComponentAt(panelPestanas.getTabCount() - 1, tab);
            panelPestanas.setSelectedComponent(c);
            t.showTimer("creacion contenedor");
            activarAyuda(c);
            t.showTimer("ayuda");
        }
    }

    private IObjetoBD getElementoActivo() {
        return elementoActivo;
    }

    private void setElementoActivo(IObjetoBD elementoActivo) {
        this.elementoActivo = elementoActivo;
    }

    public MaimonidesView(SingleFrameApplication app) {
        super(app);
        getBeanControl().addPropertyChangeListener(getControlMensajes());
        initComponents();
        mainPanel.removeAll();
        mainPanel.add(panelPestanas, BorderLayout.CENTER);
        //panelSeparador.setRightComponent(panelPestanas);
        Ayuda.addHelpKey(this.getRootPane());
        //Ponemos a escucha la aplicación general
        MaimonidesApp.getApplication().addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("anoEscolar".equals(evt.getPropertyName())) {
                    setAnoAsignado(evt.getNewValue() != null);
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            cerrarPestanas();
                            if (isAnoAsignado()) {
                                //Y abrimos la pestaña de inicio
                                inicio();
                            } else {
                                MaimonidesUtil.ejecutarTask(MaimonidesApp.getMaimonidesView(), "configAnoEscolar");
                            }
                        }
                    });

                }
            }
        });
        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);
        memoriaLabel.setVisible(false);
        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = getApplication().getContext().getTaskMonitor();//new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(getControlMensajes());
        panelPestanas.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent ce) {
                activarAyuda(panelPestanas.getSelectedComponent());
            }
        });
        //Le añadimos el listener a los roles
        for (int i = 0; i < miRolesActivos.getItemCount(); i++) {
            JMenuItem mi = miRolesActivos.getItem(i);
            if (mi != null) {
                mi.addActionListener(rolActivoActionPerformed);
            }
        }
        //Añadimos el control de cambios de usuario
        MaimonidesApp.getApplication().addPropertyChangeListener("usuario", new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                usuarioCambiado();
            }
        });
        InputStream is = null;
        try {
            is = getClass().getResourceAsStream("resources/fondo.png");
            BufferedImage img = ImageIO.read(is);
            imPainter = new ImagePainter(img);
        } catch (IOException ex) {
            Logger.getLogger(MaimonidesView.class.getName()).log(Level.SEVERE, null, ex);
        }
        Obj.cerrar(is);
    }

    private void cerrarPestanas() {
        //Tenemos que recorrer cada panel preguntando si se puede cerrar
        for (int i = 0; i < panelPestanas.getTabCount(); i++) {
            JComponent c = (JComponent) panelPestanas.getTabComponentAt(i);
            Object obj = c.getClientProperty("panel");
            if (obj instanceof IPanel) {
                //TODO Lo interesante sería poder cancelar el cierre de pestañas
                ((IPanel) obj).puedoSusituir();
            }
        }
        panelPestanas.removeAll();
    }

    private void usuarioCambiado() {
        cambiandoUsuario = true;
        Usuario u = MaimonidesApp.getApplication().getUsuario();
        mUsuario.setEnabled(u != null);

        //Inicialmente ocultamos todo y cerramos todos los paneles y ya se iran activando según los roles
        cerrarPestanas();
        //Empezamos con el menú
        for (int i = 0; i < menuBar.getMenuCount(); i++) {
            JMenu jm = menuBar.getMenu(i);
            if (jm != null) {
                procesarPermisosJMenuItem(jm);
            }
        }

        //Ahora ocultamos los menus de los roles que no afectan
        int roles = Rol.ROL_NULO;
        int rolesEfectivos = Rol.ROL_NULO;
        if (u != null) {
            roles = u.getRoles();
            rolesEfectivos = u.getRolesEfectivos();
            miEditarMisDatos.setVisible(!u.isUsuarioVirtual());
            miRolesActivos.setVisible(!u.isUsuarioVirtual());
        }
        //TODO Los menús se tienen que generar dinamicamente
        
        miRolesAdmin.setVisible((roles & Rol.ROL_ADMIN) == Rol.ROL_ADMIN);
        miRolesDirectivo.setVisible((roles & Rol.ROL_DIRECTIVO) == Rol.ROL_DIRECTIVO);
        miRolesJE.setVisible((roles & Rol.ROL_JEFE_ESTUDIOS) == Rol.ROL_JEFE_ESTUDIOS);
        miRolesProfesor.setVisible((roles & Rol.ROL_PROFESOR) == Rol.ROL_PROFESOR);

        miRolesAdmin.setSelected((rolesEfectivos & Rol.ROL_ADMIN) == Rol.ROL_ADMIN);
        miRolesDirectivo.setSelected((rolesEfectivos & Rol.ROL_DIRECTIVO) == Rol.ROL_DIRECTIVO);
        miRolesJE.setSelected((rolesEfectivos & Rol.ROL_JEFE_ESTUDIOS) == Rol.ROL_JEFE_ESTUDIOS);
        miRolesProfesor.setSelected((rolesEfectivos & Rol.ROL_PROFESOR) == Rol.ROL_PROFESOR);

        cambiandoUsuario = false;
        if (u != null) {
            inicio();
        }
    }

    private void procesarPermisosJMenuItem(JMenuItem jm) {
        //Primero procesamos el menu
        //Lo activamos por defecto
        jm.setEnabled(true);
        jm.setVisible(true);
        if (jm.getAction() != null) {
            if (jm.getAction() instanceof ApplicationAction) {
                ApplicationAction aa = ((ApplicationAction) jm.getAction());
                boolean acceso = (Permisos.acceso(this, aa.getName()));
                //aa.setEnabled(acceso);
                jm.setVisible(acceso);
                jm.setEnabled(acceso);
            }
        }

        //Ahora procesamos sus elementos y subelementos
        if (jm instanceof JMenu) {
            JMenu m = (JMenu) jm;
            for (int i = 0; i < m.getItemCount(); i++) {
                if (m.getItem(i) != null) {
                    procesarPermisosJMenuItem(m.getItem(i));
                }
            }
            //Ahora revisamos si todos los elementos del menú están invisibles
            boolean deshabilitar = true;
            for (int i = 0; i < m.getItemCount(); i++) {
                if (m.getItem(i) != null && m.getItem(i).isEnabled()) {
                    deshabilitar = false;
                    break;
                }
            }
            //Si todos los elementos estan deshabilitados y el elemento no tiene accion
            //lo deshabilitamos tambien
            if (deshabilitar && m.isEnabled() && jm.getAction() == null) {
                m.setEnabled(!deshabilitar);
                if (m instanceof Component) {
                    ((Component) m).setVisible(false);
                }
            }
        }
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = MaimonidesApp.getApplication().getMainFrame();
            aboutBox = new MaimonidesAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        MaimonidesApp.getApplication().show(aboutBox);
    }

    public PropertyChangeListener getControlMensajes() {
        if (control == null) {
            control = new java.beans.PropertyChangeListener() {

                @Override
                public void propertyChange(java.beans.PropertyChangeEvent evt) {
                    String propertyName = evt.getPropertyName();
                    if ("started".equals(propertyName)) {
                        if (!busyIconTimer.isRunning()) {
                            statusAnimationLabel.setIcon(busyIcons[0]);
                            busyIconIndex = 0;
                            busyIconTimer.start();
                        }
                        progressBar.setVisible(true);
                        progressBar.setIndeterminate(true);
                    } else if ("done".equals(propertyName)) {
                        busyIconTimer.stop();
                        statusAnimationLabel.setIcon(idleIcon);
                        progressBar.setVisible(false);
                        progressBar.setValue(0);
                    } else if ("message".equals(propertyName)) {
                        String text = (String) (evt.getNewValue());
                        statusMessageLabel.setText((text == null) ? "" : text);
                        messageTimer.restart();
                    } else if ("progress".equals(propertyName)) {
                        if (evt.getNewValue() != null) {
                            int value = (Integer) (evt.getNewValue());
                            progressBar.setVisible(true);
                            progressBar.setIndeterminate(false);
                            progressBar.setValue(value);
                        } else {
                            progressBar.setIndeterminate(true);
                        }
                    }
                }
            };
        }
        return control;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        memoriaLabel = new com.codeko.swing.MemoriaLabel();
        panelPrincipal = new javax.swing.JPanel();
        panelPestanas = new javax.swing.JTabbedPane(){
            public void paint(Graphics g){
                super.paint(g);
                if(panelPestanas.getTabCount()==0){
                    Graphics2D g2=(Graphics2D)g;
                    imPainter.paint(g2, this, getWidth(), getHeight());
                }
            }
        };
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        jMenu1 = new javax.swing.JMenu();
        miConfConexion = new javax.swing.JMenuItem();
        menuConfigGeneral = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        mHerramientas = new javax.swing.JMenu();
        miInicio = new javax.swing.JMenuItem();
        miCopiaSeguridad = new javax.swing.JMenuItem();
        miImportacionInicialDatos = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        miNotificacionesGenerales = new javax.swing.JMenuItem();
        jMenuItem7 = new javax.swing.JMenuItem();
        mDatos = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        miDependencias = new javax.swing.JMenuItem();
        miProfesores = new javax.swing.JMenuItem();
        miFichaAlumnos = new javax.swing.JMenuItem();
        miMaterias = new javax.swing.JMenuItem();
        miMatriculaciones = new javax.swing.JMenuItem();
        cbCursoGrupos = new javax.swing.JMenuItem();
        miCalendario = new javax.swing.JMenuItem();
        miNotificaciones = new javax.swing.JMenuItem();
        mFaltas = new javax.swing.JMenu();
        miAsistencia = new javax.swing.JMenuItem();
        mPartesGenericos = new javax.swing.JMenuItem();
        miDigitalizar = new javax.swing.JMenuItem();
        miJustificacionesB = new javax.swing.JMenuItem();
        miJustificaciones = new javax.swing.JMenuItem();
        miExportarSeneca = new javax.swing.JMenuItem();
        mCartas = new javax.swing.JMenuItem();
        miInformesPartes = new javax.swing.JMenu();
        miInformePartesFirmas = new javax.swing.JMenuItem();
        miInformesPartesDigitalizacion = new javax.swing.JMenuItem();
        miInformesAsistencia = new javax.swing.JMenu();
        miInfAsistenciaListado = new javax.swing.JMenuItem();
        miInfAsistenciaEvolucion = new javax.swing.JMenuItem();
        miInfAsistenciaPerdidaEC = new javax.swing.JMenuItem();
        miInfAsistenciaPerdidaECMaterias = new javax.swing.JMenuItem();
        mHorarios = new javax.swing.JMenu();
        miEditorHorarios = new javax.swing.JMenuItem();
        miDivisionesAlumnos = new javax.swing.JMenuItem();
        miExportarHorariosSeneca = new javax.swing.JMenuItem();
        mConvivencia = new javax.swing.JMenu();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenuItem6 = new javax.swing.JMenuItem();
        mInformesConvivencia = new javax.swing.JMenu();
        jMenuItem8 = new javax.swing.JMenuItem();
        jMenuItem9 = new javax.swing.JMenuItem();
        jMenuItem10 = new javax.swing.JMenuItem();
        mCartasConvivencia = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        miAyuda = new javax.swing.JMenuItem();
        mUsuario = new javax.swing.JMenu();
        miCambiarUsuario = new javax.swing.JMenuItem();
        miEditarMisDatos = new javax.swing.JMenuItem();
        miRolesActivos = new javax.swing.JMenu();
        miRolesAdmin = new javax.swing.JCheckBoxMenuItem();
        miRolesDirectivo = new javax.swing.JCheckBoxMenuItem();
        miRolesJE = new javax.swing.JCheckBoxMenuItem();
        miRolesProfesor = new javax.swing.JCheckBoxMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();

        mainPanel.setName("mainPanel"); // NOI18N
        mainPanel.setLayout(new java.awt.BorderLayout());

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance().getContext().getResourceMap(MaimonidesView.class);
        memoriaLabel.setText(resourceMap.getString("memoriaLabel.text")); // NOI18N
        memoriaLabel.setName("memoriaLabel"); // NOI18N
        mainPanel.add(memoriaLabel, java.awt.BorderLayout.SOUTH);

        panelPrincipal.setName("panelPrincipal"); // NOI18N

        panelPestanas.setName("panelPestanas"); // NOI18N

        javax.swing.GroupLayout panelPrincipalLayout = new javax.swing.GroupLayout(panelPrincipal);
        panelPrincipal.setLayout(panelPrincipalLayout);
        panelPrincipalLayout.setHorizontalGroup(
            panelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 838, Short.MAX_VALUE)
            .addGroup(panelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panelPrincipalLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(panelPestanas, javax.swing.GroupLayout.DEFAULT_SIZE, 826, Short.MAX_VALUE)))
        );
        panelPrincipalLayout.setVerticalGroup(
            panelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 793, Short.MAX_VALUE)
            .addGroup(panelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panelPrincipalLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(panelPestanas, javax.swing.GroupLayout.DEFAULT_SIZE, 781, Short.MAX_VALUE)
                    .addGap(0, 0, 0)))
        );

        mainPanel.add(panelPrincipal, java.awt.BorderLayout.CENTER);

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setIcon(resourceMap.getIcon("fileMenu.icon")); // NOI18N
        fileMenu.setMnemonic('v');
        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        jMenu1.setIcon(resourceMap.getIcon("jMenu1.icon")); // NOI18N
        jMenu1.setMnemonic('c');
        jMenu1.setText(resourceMap.getString("jMenu1.text")); // NOI18N
        jMenu1.setName("jMenu1"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance().getContext().getActionMap(MaimonidesView.class, this);
        miConfConexion.setAction(actionMap.get("editarConexion")); // NOI18N
        miConfConexion.setName("miConfConexion"); // NOI18N
        jMenu1.add(miConfConexion);

        menuConfigGeneral.setAction(actionMap.get("mostrarPanelConfiguracion")); // NOI18N
        menuConfigGeneral.setName("menuConfigGeneral"); // NOI18N
        jMenu1.add(menuConfigGeneral);

        fileMenu.add(jMenu1);

        jSeparator2.setName("jSeparator2"); // NOI18N
        fileMenu.add(jSeparator2);

        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setText(resourceMap.getString("exitMenuItem.text")); // NOI18N
        exitMenuItem.setToolTipText(resourceMap.getString("exitMenuItem.toolTipText")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        mHerramientas.setIcon(resourceMap.getIcon("mHerramientas.icon")); // NOI18N
        mHerramientas.setMnemonic('t');
        mHerramientas.setText(resourceMap.getString("mHerramientas.text")); // NOI18N
        mHerramientas.setName("mHerramientas"); // NOI18N

        miInicio.setAction(actionMap.get("inicio")); // NOI18N
        miInicio.setName("miInicio"); // NOI18N
        mHerramientas.add(miInicio);

        miCopiaSeguridad.setAction(actionMap.get("backup")); // NOI18N
        miCopiaSeguridad.setName("miCopiaSeguridad"); // NOI18N
        mHerramientas.add(miCopiaSeguridad);

        miImportacionInicialDatos.setAction(actionMap.get("mostrarPanelImportacionInicialDatos")); // NOI18N
        miImportacionInicialDatos.setName("miImportacionInicialDatos"); // NOI18N
        mHerramientas.add(miImportacionInicialDatos);

        jMenuItem3.setAction(actionMap.get("mostrarPanelProblemasEnvioFaltas")); // NOI18N
        jMenuItem3.setName("jMenuItem3"); // NOI18N
        mHerramientas.add(jMenuItem3);

        miNotificacionesGenerales.setAction(actionMap.get("mostrarPanelNotificacionesManuales")); // NOI18N
        miNotificacionesGenerales.setName("miNotificacionesGenerales"); // NOI18N
        mHerramientas.add(miNotificacionesGenerales);

        jMenuItem7.setAction(actionMap.get("mostrarPanelControlDatosAlumnos")); // NOI18N
        jMenuItem7.setName("jMenuItem7"); // NOI18N
        mHerramientas.add(jMenuItem7);

        menuBar.add(mHerramientas);

        mDatos.setIcon(resourceMap.getIcon("mDatos.icon")); // NOI18N
        mDatos.setMnemonic('d');
        mDatos.setText(resourceMap.getString("mDatos.text")); // NOI18N
        mDatos.setName("mDatos"); // NOI18N

        jMenuItem2.setAction(actionMap.get("configAnoEscolar")); // NOI18N
        jMenuItem2.setName("jMenuItem2"); // NOI18N
        mDatos.add(jMenuItem2);

        miDependencias.setAction(actionMap.get("mostrarDependencias")); // NOI18N
        miDependencias.setName("miDependencias"); // NOI18N
        mDatos.add(miDependencias);

        miProfesores.setAction(actionMap.get("editarProfesores")); // NOI18N
        miProfesores.setName("miProfesores"); // NOI18N
        mDatos.add(miProfesores);

        miFichaAlumnos.setAction(actionMap.get("fichasAlumnos")); // NOI18N
        miFichaAlumnos.setName("miFichaAlumnos"); // NOI18N
        mDatos.add(miFichaAlumnos);

        miMaterias.setAction(actionMap.get("editarMaterias")); // NOI18N
        miMaterias.setName("miMaterias"); // NOI18N
        mDatos.add(miMaterias);

        miMatriculaciones.setAction(actionMap.get("editarMatriculaciones")); // NOI18N
        miMatriculaciones.setName("miMatriculaciones"); // NOI18N
        mDatos.add(miMatriculaciones);

        cbCursoGrupos.setAction(actionMap.get("editarCursosGrupos")); // NOI18N
        cbCursoGrupos.setName("cbCursoGrupos"); // NOI18N
        mDatos.add(cbCursoGrupos);

        miCalendario.setAction(actionMap.get("mostrarPanelCalendarioEscolar")); // NOI18N
        miCalendario.setName("miCalendario"); // NOI18N
        mDatos.add(miCalendario);

        miNotificaciones.setAction(actionMap.get("mostrarPanelNotificaciones")); // NOI18N
        miNotificaciones.setName("miNotificaciones"); // NOI18N
        mDatos.add(miNotificaciones);

        menuBar.add(mDatos);

        mFaltas.setIcon(resourceMap.getIcon("mFaltas.icon")); // NOI18N
        mFaltas.setMnemonic('a');
        mFaltas.setText(resourceMap.getString("mFaltas.text")); // NOI18N
        mFaltas.setName("mFaltas"); // NOI18N

        miAsistencia.setAction(actionMap.get("editarPartes")); // NOI18N
        miAsistencia.setName("miAsistencia"); // NOI18N
        mFaltas.add(miAsistencia);

        mPartesGenericos.setAction(actionMap.get("mostrarPanelPartesGenericos")); // NOI18N
        mPartesGenericos.setName("mPartesGenericos"); // NOI18N
        mFaltas.add(mPartesGenericos);

        miDigitalizar.setAction(actionMap.get("digitalizarPartes")); // NOI18N
        miDigitalizar.setName("miDigitalizar"); // NOI18N
        mFaltas.add(miDigitalizar);

        miJustificacionesB.setAction(actionMap.get("mostrarPanelJustificacionesRapidas")); // NOI18N
        miJustificacionesB.setName("miJustificacionesB"); // NOI18N
        mFaltas.add(miJustificacionesB);

        miJustificaciones.setAction(actionMap.get("mostrarPanelJustificaciones")); // NOI18N
        miJustificaciones.setName("miJustificaciones"); // NOI18N
        mFaltas.add(miJustificaciones);

        miExportarSeneca.setAction(actionMap.get("exportarFaltasSeneca")); // NOI18N
        miExportarSeneca.setName("miExportarSeneca"); // NOI18N
        mFaltas.add(miExportarSeneca);

        mCartas.setAction(actionMap.get("mostrarPanelCartas")); // NOI18N
        mCartas.setName("mCartas"); // NOI18N
        mFaltas.add(mCartas);

        miInformesPartes.setIcon(resourceMap.getIcon("miInformesPartes.icon")); // NOI18N
        miInformesPartes.setText(resourceMap.getString("miInformesPartes.text")); // NOI18N
        miInformesPartes.setToolTipText(resourceMap.getString("miInformesPartes.toolTipText")); // NOI18N
        miInformesPartes.setName("miInformesPartes"); // NOI18N

        miInformePartesFirmas.setAction(actionMap.get("mostrarInformePartesNoFirmados")); // NOI18N
        miInformePartesFirmas.setName("miInformePartesFirmas"); // NOI18N
        miInformesPartes.add(miInformePartesFirmas);

        miInformesPartesDigitalizacion.setAction(actionMap.get("mostrarInformePartesNoDigitalizados")); // NOI18N
        miInformesPartesDigitalizacion.setName("miInformesPartesDigitalizacion"); // NOI18N
        miInformesPartes.add(miInformesPartesDigitalizacion);

        mFaltas.add(miInformesPartes);

        miInformesAsistencia.setIcon(resourceMap.getIcon("miInformesAsistencia.icon")); // NOI18N
        miInformesAsistencia.setText(resourceMap.getString("miInformesAsistencia.text")); // NOI18N
        miInformesAsistencia.setToolTipText(resourceMap.getString("miInformesAsistencia.toolTipText")); // NOI18N
        miInformesAsistencia.setName("miInformesAsistencia"); // NOI18N

        miInfAsistenciaListado.setAction(actionMap.get("mostrarInformeListadoAsistencia")); // NOI18N
        miInfAsistenciaListado.setName("miInfAsistenciaListado"); // NOI18N
        miInformesAsistencia.add(miInfAsistenciaListado);

        miInfAsistenciaEvolucion.setAction(actionMap.get("mostrarInformeEvolucionAsistencia")); // NOI18N
        miInfAsistenciaEvolucion.setName("miInfAsistenciaEvolucion"); // NOI18N
        miInformesAsistencia.add(miInfAsistenciaEvolucion);

        miInfAsistenciaPerdidaEC.setAction(actionMap.get("mostrarInformePerdidaEscolaridadGlobal")); // NOI18N
        miInfAsistenciaPerdidaEC.setName("miInfAsistenciaPerdidaEC"); // NOI18N
        miInformesAsistencia.add(miInfAsistenciaPerdidaEC);

        miInfAsistenciaPerdidaECMaterias.setAction(actionMap.get("mostrarInformePerdidaEscolaridadMaterias")); // NOI18N
        miInfAsistenciaPerdidaECMaterias.setName("miInfAsistenciaPerdidaECMaterias"); // NOI18N
        miInformesAsistencia.add(miInfAsistenciaPerdidaECMaterias);

        mFaltas.add(miInformesAsistencia);

        menuBar.add(mFaltas);

        mHorarios.setIcon(resourceMap.getIcon("mHorarios.icon")); // NOI18N
        mHorarios.setMnemonic('h');
        mHorarios.setText(resourceMap.getString("mHorarios.text")); // NOI18N
        mHorarios.setName("mHorarios"); // NOI18N

        miEditorHorarios.setAction(actionMap.get("mostrarEditorHorarios")); // NOI18N
        miEditorHorarios.setName("miEditorHorarios"); // NOI18N
        mHorarios.add(miEditorHorarios);

        miDivisionesAlumnos.setAction(actionMap.get("divisionesAlumnos")); // NOI18N
        miDivisionesAlumnos.setName("miDivisionesAlumnos"); // NOI18N
        mHorarios.add(miDivisionesAlumnos);

        miExportarHorariosSeneca.setAction(actionMap.get("exportarFicheroHorariosSeneca")); // NOI18N
        miExportarHorariosSeneca.setName("miExportarHorariosSeneca"); // NOI18N
        mHorarios.add(miExportarHorariosSeneca);

        menuBar.add(mHorarios);

        mConvivencia.setIcon(resourceMap.getIcon("mConvivencia.icon")); // NOI18N
        mConvivencia.setMnemonic('c');
        mConvivencia.setText(resourceMap.getString("mConvivencia.text")); // NOI18N
        mConvivencia.setName("mConvivencia"); // NOI18N

        jMenuItem4.setAction(actionMap.get("mostrarPartesConvivencia")); // NOI18N
        jMenuItem4.setName("jMenuItem4"); // NOI18N
        mConvivencia.add(jMenuItem4);

        jMenuItem5.setAction(actionMap.get("mostrarExpulsiones")); // NOI18N
        jMenuItem5.setName("jMenuItem5"); // NOI18N
        mConvivencia.add(jMenuItem5);

        jMenuItem6.setAction(actionMap.get("mostrarPanelGeneracionExpulsiones")); // NOI18N
        jMenuItem6.setName("jMenuItem6"); // NOI18N
        mConvivencia.add(jMenuItem6);

        mInformesConvivencia.setIcon(resourceMap.getIcon("mInformesConvivencia.icon")); // NOI18N
        mInformesConvivencia.setText(resourceMap.getString("mInformesConvivencia.text")); // NOI18N
        mInformesConvivencia.setName("mInformesConvivencia"); // NOI18N

        jMenuItem8.setAction(actionMap.get("mostrarPanelResumenDeConvivencia")); // NOI18N
        jMenuItem8.setName("jMenuItem8"); // NOI18N
        mInformesConvivencia.add(jMenuItem8);

        jMenuItem9.setAction(actionMap.get("mostrarPanelInformeExpulsiones")); // NOI18N
        jMenuItem9.setName("jMenuItem9"); // NOI18N
        mInformesConvivencia.add(jMenuItem9);

        jMenuItem10.setAction(actionMap.get("mostrarPanelListadoPartesConvivencia")); // NOI18N
        jMenuItem10.setName("jMenuItem10"); // NOI18N
        mInformesConvivencia.add(jMenuItem10);

        mConvivencia.add(mInformesConvivencia);

        mCartasConvivencia.setAction(actionMap.get("mostrarCartasExpulsion")); // NOI18N
        mCartasConvivencia.setName("mCartasConvivencia"); // NOI18N
        mConvivencia.add(mCartasConvivencia);

        menuBar.add(mConvivencia);

        helpMenu.setIcon(resourceMap.getIcon("helpMenu.icon")); // NOI18N
        helpMenu.setMnemonic('y');
        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setText(resourceMap.getString("aboutMenuItem.text")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        miAyuda.setAction(actionMap.get("mostrarAyuda")); // NOI18N
        miAyuda.setName("miAyuda"); // NOI18N
        helpMenu.add(miAyuda);

        menuBar.add(helpMenu);

        mUsuario.setIcon(resourceMap.getIcon("mUsuario.icon")); // NOI18N
        mUsuario.setText(resourceMap.getString("mUsuario.text")); // NOI18N
        mUsuario.setEnabled(false);
        mUsuario.setName("mUsuario"); // NOI18N

        miCambiarUsuario.setAction(actionMap.get("cambiarUsuario")); // NOI18N
        miCambiarUsuario.setName("miCambiarUsuario"); // NOI18N
        mUsuario.add(miCambiarUsuario);

        miEditarMisDatos.setAction(actionMap.get("editarUsuarioActual")); // NOI18N
        miEditarMisDatos.setName("miEditarMisDatos"); // NOI18N
        mUsuario.add(miEditarMisDatos);

        miRolesActivos.setIcon(resourceMap.getIcon("miRolesActivos.icon")); // NOI18N
        miRolesActivos.setText(resourceMap.getString("miRolesActivos.text")); // NOI18N
        miRolesActivos.setName("miRolesActivos"); // NOI18N

        miRolesAdmin.setSelected(true);
        miRolesAdmin.setText(resourceMap.getString("miRolesAdmin.text")); // NOI18N
        miRolesAdmin.setName("miRolesAdmin"); // NOI18N
        miRolesActivos.add(miRolesAdmin);

        miRolesDirectivo.setSelected(true);
        miRolesDirectivo.setText(resourceMap.getString("miRolesDirectivo.text")); // NOI18N
        miRolesDirectivo.setName("miRolesDirectivo"); // NOI18N
        miRolesActivos.add(miRolesDirectivo);

        miRolesJE.setSelected(true);
        miRolesJE.setText(resourceMap.getString("miRolesJE.text")); // NOI18N
        miRolesJE.setName("miRolesJE"); // NOI18N
        miRolesActivos.add(miRolesJE);

        miRolesProfesor.setSelected(true);
        miRolesProfesor.setText(resourceMap.getString("miRolesProfesor.text")); // NOI18N
        miRolesProfesor.setName("miRolesProfesor"); // NOI18N
        miRolesActivos.add(miRolesProfesor);

        mUsuario.add(miRolesActivos);

        menuBar.add(Box.createHorizontalGlue());

        menuBar.add(mUsuario);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 838, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 654, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    @Action(enabledProperty = "conectado")
    public void configAnoEscolar() {
        mostrarPanel(PanelAnos.class, "Años escolares", getIcono("configAnoEscolar"));
    }
    private boolean conectado = false;

    public boolean isConectado() {
        return conectado;
    }

    public void setConectado(boolean b) {
        boolean old = isConectado();
        this.conectado = b;
        firePropertyChange("conectado", old, isConectado());
        //Vemos si en este punto existe año escolar
        if (b && (b != old) && MaimonidesApp.getApplication().getAnoEscolar() == null) {
            JOptionPane.showMessageDialog(this.getFrame(), "No existe ningún año escolar activo.\nPara poder trabajar necesita especificar el año escolar.\nPor favor asigne el año escolar activo.", "Año escolar activo", JOptionPane.WARNING_MESSAGE);
            configAnoEscolar();
        }
    }
    private boolean anoAsignado = false;

    public boolean isAnoAsignado() {
        return anoAsignado;
    }

    public void setAnoAsignado(boolean b) {
        boolean old = isAnoAsignado();
        this.anoAsignado = b;
        firePropertyChange("anoAsignado", old, isAnoAsignado());
    }
    private boolean desactivado = false;

    public boolean isDesactivado() {
        return desactivado;
    }

    public void setDesactivado(boolean b) {
        boolean old = isDesactivado();
        this.desactivado = b;
        firePropertyChange("desactivado", old, isDesactivado());
    }

    private int getIndexOfPanel(Class clase, String nombre) {
        int tc = panelPestanas.getTabCount();
        for (int i = 0; i < tc; i++) {
            Component c = panelPestanas.getComponentAt(i);
            if (c.getClass().equals(clase) && nombre.equals(panelPestanas.getTitleAt(i))) {
                return i;
            }
        }
        return -1;
    }

    @Action
    public void inicio() {
        mostrarPanel(PanelInicio.class, "Maimónides - Pantalla inicial", getIcono("inicio"));
    }

    @Action(enabledProperty = "conectado")
    public void mostrarPanelConfiguracion() {
        mostrarPanel(PanelConfiguraciones.class, "Configuración", getIcono("mostrarPanelConfiguracion"));
    }

    @Action(enabledProperty = "anoAsignado")
    public void editarPartes() {
        mostrarPanel(PanelEditorPartes.class, "Editor de partes", getIcono("editarPartes"));
    }

    @Action(enabledProperty = "anoAsignado")
    public void editarMaterias() {
        mostrarPanel(PanelMaterias.class, "Materias", getIcono("editarMaterias"));
    }

    @Action(enabledProperty = "anoAsignado")
    public void divisionesAlumnos() {
        mostrarPanel(PanelDivisionAlumnosMultimateria.class, "Divisiones de alumnos", getIcono("divisionesAlumnos"));
    }

    @Action(enabledProperty = "anoAsignado")
    public void fichasAlumnos() {
        CTiempo c = new CTiempo("Alumnos");
        mostrarPanel(PanelAlumnos2.class, "Alumnos", getIcono("fichasAlumnos"));
        c.showTimer("cargaTotal");
    }

    @Action(enabledProperty = "anoAsignado")
    public void editarCursosGrupos() {
        mostrarPanel(PanelCursosGrupos.class, "Cursos/Grupos", getIcono("editarCursosGrupos"));
    }

    @Action(enabledProperty = "anoAsignado")
    public void editarMatriculaciones() {
        mostrarPanel(PanelMatriculaciones.class, "Matriculaciones de alumnos", getIcono("editarMatriculaciones"));
    }

    @Action(enabledProperty = "anoAsignado")
    public void editarProfesores() {
        mostrarPanel(PanelProfesores.class, "Profesores", getIcono("editarProfesores"));
    }

    @Action(enabledProperty = "anoAsignado")
    public void actulizarDatos() {
        //TODO Borrar
        mostrarPanel(PanelActualizacionSeneca.class, "Actualización de datos", getIcono("actulizarDatos"));
    }

    @Action(enabledProperty = "anoAsignado")
    public void exportarFaltasSeneca() {
        mostrarPanel(PanelExportacionSeneca.class, "Exportación de faltas de asistencia", getIcono("exportarFaltasSeneca"));
    }

    @Action(enabledProperty = "anoAsignado")
    public void mostrarPanelJustificaciones() {
        mostrarPanel(PanelJustificaciones.class, "Justificación de faltas", getIcono("mostrarPanelJustificaciones"));
    }

    @Action(enabledProperty = "anoAsignado")
    public void digitalizarPartes() {
        mostrarPanel(PanelDigitalizacion.class, "Digitalización de partes de asistencia", getIcono("digitalizarPartes"));
    }

    @Action(enabledProperty = "anoAsignado")
    public void mostrarPanelPartesParcialmenteDigitalizados() {
        mostrarPanel(PanelPartesMedioDigitalizados.class, "Partes parcialmente digitalizados", getIcono("mostrarPanelPartesParcialmenteDigitalizados"));
    }

    @Action(enabledProperty = "anoAsignado")
    public void mostrarPanelPartesPendientes() {
        mostrarPanel(PanelPartesPendientes.class, "Partes pendientes de digitalizar", getIcono("mostrarPanelPartesPendientes"));
    }

    @Action(block = Task.BlockingScope.APPLICATION, enabledProperty = "conectado")
    public void backup() {
        mostrarPanel(PanelCopiasSeguridad.class, "Copias de seguridad", getIcono("backup"));
    }

    @Action
    public void mostrarAyuda() {
        Ayuda.mostrar();
    }

    @Action(enabledProperty = "anoAsignado")
    public void mostrarPanelJustificacionesRapidas() {
        mostrarPanel(PanelJustificacionesRapidas.class, "Justificación de faltas", getIcono("mostrarPanelJustificacionesRapidas"));
    }

    @Action(enabledProperty = "anoAsignado")
    public void mostrarEditorHorarios() {
        mostrarPanel(PanelEditorHorariosDoble.class, "Editor de horarios", getIcono("mostrarEditorHorarios"));

    }

    @Action(enabledProperty = "anoAsignado")
    public void mostrarDependencias() {
        mostrarPanel(PanelDependencias.class, "Dependencias", getIcono("mostrarDependencias"));
    }

    @Action(enabledProperty = "anoAsignado")
    public void mostrarInformePartesNoFirmados() {
        mostrarPanel(PanelPartesNoFirmados.class, "Informes: Partes no firmados", getIcono("mostrarInformePartesNoFirmados"));
    }

    @Action(enabledProperty = "anoAsignado")
    public void mostrarInformePartesNoDigitalizados() {
        mostrarPanel(PanelInformesPartesNoDigitalizados.class, "Informes: Partes no digitalizados", getIcono("mostrarInformePartesNoDigitalizados"));
    }

    @Action(enabledProperty = "anoAsignado")
    public void mostrarInformeListadoAsistencia() {
        mostrarPanel(PanelCartasAsistencia.class, "Informes: Listado de asistencia", getIcono("mostrarInformeListadoAsistencia"));
    }

    @Action(enabledProperty = "anoAsignado")
    public void mostrarInformeEvolucionAsistencia() {
        mostrarPanel(PanelEvolucionAsistencia.class, "Informes: Evolución de asistencia", getIcono("mostrarInformeEvolucionAsistencia"));
    }

    @Action(enabledProperty = "anoAsignado")
    public void mostrarInformePerdidaEscolaridadMaterias() {
        mostrarPanel(PanelPerdidaEscolaridadPorMaterias.class, "Informes: Perdida de evaluación continua por materias", getIcono("mostrarInformePerdidaEscolaridadMaterias"));
    }

    @Action(enabledProperty = "anoAsignado")
    public void mostrarInformePerdidaEscolaridadGlobal() {
        mostrarPanel(PanelPerdidaEscolaridadGlobal.class, "Informes: Perdida de evaluación continua", getIcono("mostrarInformePerdidaEscolaridadGlobal"));
    }

    @Action(enabledProperty = "anoAsignado")
    public void mostrarPanelCartas() {
        mostrarPanel(PanelCartas.class, "Cartas", getIcono("mostrarPanelCartas"));
    }

    @Action(enabledProperty = "anoAsignado")
    public void mostrarConfiguracionConvivencia() {
        mostrarPanel(PanelConfiguracionConvivencia.class, "Configuración convivencia", getIcono("mostrarConfiguracionConvivencia"));
    }

    @Action(enabledProperty = "anoAsignado")
    public void mostrarPartesConvivencia() {
        mostrarPanel(PanelPartes.class, "Partes de convivencia", getIcono("mostrarPartesConvivencia"));
    }

    @Action(enabledProperty = "anoAsignado")
    public void mostrarExpulsiones() {
        mostrarPanel(PanelExpulsiones.class, "Expulsiones", getIcono("mostrarExpulsiones"));
    }

    @Action(enabledProperty = "anoAsignado")
    public void mostrarPanelGeneracionExpulsiones() {
        mostrarPanel(PanelGeneracionExpulsiones.class, "Generación de Expulsiones", getIcono("mostrarPanelGeneracionExpulsiones"));
    }

    @Action(enabledProperty = "anoAsignado")
    public void mostrarPanelResumenDeConvivencia() {
        mostrarPanel(PanelResumenConvivencia.class, "Resumen de convivencia", getIcono("mostrarPanelResumenDeConvivencia"));
    }

    @Action(enabledProperty = "anoAsignado")
    public void mostrarPanelInformeExpulsiones() {
        mostrarPanel(PanelListadoExpulsiones.class, "Expulsiones", getIcono("mostrarPanelInformeExpulsiones"));
    }

    @Action(enabledProperty = "anoAsignado")
    public void mostrarPanelListadoPartesConvivencia() {
        mostrarPanel(PanelListaPartesConvivencia.class, "Partes de convivencia", getIcono("mostrarPanelListadoPartesConvivencia"));
    }

    @Action(enabledProperty = "anoAsignado")
    public void mostrarPanelImportacionInicialDatos() {
        mostrarPanel(PanelImportacionInicial.class, "Importación inicial de datos", getIcono("mostrarPanelImportacionInicialDatos"));
    }

    @Action(enabledProperty = "anoAsignado")
    public void mostrarPanelPartesGenericos() {
        mostrarPanel(PanelGrupos.class, "Partes de asistencia genéricos", getIcono("mostrarPanelPartesGenericos"));
    }

    @Action(enabledProperty = "anoAsignado")
    public void mostrarPanelProblemasEnvioFaltas() {
        mostrarPanel(PanelDebugEnvioFicheros.class, "Revisión errores de envío de faltas a Séneca", getIcono("mostrarPanelProblemasEnvioFaltas"));
    }

    @Action(enabledProperty = "anoAsignado")
    public void mostrarPanelCalendarioEscolar() {
        mostrarPanel(PanelCalendarioEscolar.class, "Calendario escolar", getIcono("mostrarPanelCalendarioEscolar"));
    }

    protected void mostrarPanel(Class clase, String titulo) {
        mostrarPanel(clase, titulo, null);
    }

    public void mostrarPanel(Class clase, String titulo, Icon icono) {
        if (Permisos.acceso(clase)) {
            if (!activarPanel(clase, titulo)) {
                try {
                    Object ni = clase.newInstance();
                    if (ni instanceof IPanel && ni instanceof Component) {
                        setPanelActual((IPanel) ni, titulo, icono);
                    } else {
                        throw new Exception("El panel no es IPanel no se puede añadir.");
                    }
                } catch (Exception ex) {
                    Logger.getLogger(MaimonidesView.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else {
            JOptionPane.showMessageDialog(getFrame(), "No tiene permisos para acceder a ese panel.", "Acceso denegado", JOptionPane.WARNING_MESSAGE);
        }
    }

    private Icon getIcono(String accion) {
        String acc = accion + ".Action.smallIcon";
        return rMap.getIcon(acc);
    }

    @Action(enabledProperty = "anoAsignado")
    public void mostrarCartasExpulsion() {
        mostrarPanel(PanelNotificacionesConvivencia.class, "Notificaciones de convivencia", getIcono("mostrarCartasExpulsion"));
    }

    @Action(enabledProperty = "anoAsignado")
    public void mostrarPanelNotificacionesManuales() {
        mostrarPanel(PanelEnvioNotificacionesManuales.class, "Notificaciones generales", getIcono("mostrarPanelNotificacionesManuales"));
    }

    @Action
    public void cambiarUsuario() {
        MaimonidesApp.getApplication().setUsuario(null);
    }

    @Action
    public void editarUsuarioActual() {
        PanelEditorDatosUsuarioActual p = new PanelEditorDatosUsuarioActual();
        boolean ok = false;
        while (ok == false) {
            ok = true;
            int op = JOptionPane.showConfirmDialog(getFrame(), p, "Editar mis datos de usuario", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (op == JOptionPane.OK_OPTION) {
                if (!p.getClave().equals("")) {
                    if (p.clavesOK()) {
                        MaimonidesApp.getApplication().getUsuario().setClave(p.getClave());
                        MaimonidesApp.getApplication().getUsuario().guardar();
                    } else {
                        ok = false;
                    }
                }
            }
        }
    }

    public void mostrarFichaAlumno(Alumno a) {
        setElementoActivo(a);
        MaimonidesUtil.ejecutarTask(this, "mostrarFichaElemento");
    }

    public void mostrarFichaElemento(IObjetoBD elemento) {
        setElementoActivo(elemento);
        MaimonidesUtil.ejecutarTask(this, "mostrarFichaElemento");
    }

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task<Void, Void>  mostrarFichaElemento() {
        return new MostrarElementoTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class MostrarElementoTask extends org.jdesktop.application.Task<Void, Void> {

        MostrarElementoTask(org.jdesktop.application.Application app) {
            super(app);
            setMessage("Abriendo ficha de " + getElementoActivo().getNombreObjeto() + "...");
        }

        @Override
        protected Void doInBackground() {
            if (getElementoActivo() instanceof Alumno) {
                Alumno a=(Alumno) getElementoActivo();
                setMessage("Abriendo ficha de " + a + "...");
                fichasAlumnos();
                JComponent c = getPanel(PanelAlumnos2.class, "Alumnos");
                if (c instanceof PanelAlumnos2) {
                    PanelAlumnos2 p = (PanelAlumnos2) c;
                    p.mostrarAlumno(a);
                }
            }else if(getElementoActivo() instanceof ParteConvivencia){
                ParteConvivencia p=(ParteConvivencia)getElementoActivo();
                mostrarPartesConvivencia();
                JComponent c = getPanel(PanelPartes.class,"Partes de convivencia");
                if (c instanceof PanelPartes) {
                    PanelPartes pp = (PanelPartes) c;
                    pp.setParte(p);
                }
            }else{
                setMessage("No se ha podido abrir la ficha de " + getElementoActivo().getNombreObjeto() + ".");
            }
            return null;
        }

        @Override
        protected void succeeded(Void result) {
        }
    }

    @Action(enabledProperty = "anoAsignado")
    public void mostrarPanelNotificaciones() {
        mostrarPanel(PanelCorrespondencia.class, "Notificaciones", getIcono("mostrarPanelNotificaciones"));
    }

    @Action(block = Task.BlockingScope.APPLICATION, enabledProperty = "anoAsignado")
    public Task exportarFicheroHorariosSeneca() {
        return new ExportarFicheroHorariosSenecaTask(getApplication());
    }

    private class ExportarFicheroHorariosSenecaTask extends org.jdesktop.application.Task<File, Void> {

        ExportarFicheroHorariosSenecaTask(org.jdesktop.application.Application app) {
            super(app);
        }

        @Override
        protected File doInBackground() throws IOException {
            ExportadorHorariosSeneca exportador = new ExportadorHorariosSeneca();
            exportador.addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                }
            });
            return exportador.exportarHorariosXMLSeneca();
        }

        @Override
        public void failed(Throwable t) {
            JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), "Ha habido un error generando el fichero de horarios:\n" + t.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        @Override
        protected void succeeded(File result) {
            //Ahora ofrecemos guardar el fichero
            if (MaimonidesUtil.dialogoGuardarArchivo("Guardar fichero de horarios para Séneca", result, "xml")) {
                JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), "Fichero XML de horarios para Séneca generado correctamente.", "Finalizado", JOptionPane.INFORMATION_MESSAGE);
                setMessage("Fichero guardado correctamente.");
            } else {
                setMessage("No se ha guardado el fichero.");
            }
        }
    }

    @Action(enabledProperty = "anoAsignado")
    public void mostrarPanelControlDatosAlumnos() {
        mostrarPanel(PanelControlAlumnos.class, "Control de datos de alumnos", getIcono("mostrarPanelControlDatosAlumnos"));
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem cbCursoGrupos;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuItem jMenuItem10;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JMenuItem jMenuItem9;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JMenuItem mCartas;
    private javax.swing.JMenuItem mCartasConvivencia;
    private javax.swing.JMenu mConvivencia;
    private javax.swing.JMenu mDatos;
    private javax.swing.JMenu mFaltas;
    private javax.swing.JMenu mHerramientas;
    private javax.swing.JMenu mHorarios;
    private javax.swing.JMenu mInformesConvivencia;
    private javax.swing.JMenuItem mPartesGenericos;
    private javax.swing.JMenu mUsuario;
    private javax.swing.JPanel mainPanel;
    private com.codeko.swing.MemoriaLabel memoriaLabel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem menuConfigGeneral;
    private javax.swing.JMenuItem miAsistencia;
    private javax.swing.JMenuItem miAyuda;
    private javax.swing.JMenuItem miCalendario;
    private javax.swing.JMenuItem miCambiarUsuario;
    private javax.swing.JMenuItem miConfConexion;
    private javax.swing.JMenuItem miCopiaSeguridad;
    private javax.swing.JMenuItem miDependencias;
    private javax.swing.JMenuItem miDigitalizar;
    private javax.swing.JMenuItem miDivisionesAlumnos;
    private javax.swing.JMenuItem miEditarMisDatos;
    private javax.swing.JMenuItem miEditorHorarios;
    private javax.swing.JMenuItem miExportarHorariosSeneca;
    private javax.swing.JMenuItem miExportarSeneca;
    private javax.swing.JMenuItem miFichaAlumnos;
    private javax.swing.JMenuItem miImportacionInicialDatos;
    private javax.swing.JMenuItem miInfAsistenciaEvolucion;
    private javax.swing.JMenuItem miInfAsistenciaListado;
    private javax.swing.JMenuItem miInfAsistenciaPerdidaEC;
    private javax.swing.JMenuItem miInfAsistenciaPerdidaECMaterias;
    private javax.swing.JMenuItem miInformePartesFirmas;
    private javax.swing.JMenu miInformesAsistencia;
    private javax.swing.JMenu miInformesPartes;
    private javax.swing.JMenuItem miInformesPartesDigitalizacion;
    private javax.swing.JMenuItem miInicio;
    private javax.swing.JMenuItem miJustificaciones;
    private javax.swing.JMenuItem miJustificacionesB;
    private javax.swing.JMenuItem miMaterias;
    private javax.swing.JMenuItem miMatriculaciones;
    private javax.swing.JMenuItem miNotificaciones;
    private javax.swing.JMenuItem miNotificacionesGenerales;
    private javax.swing.JMenuItem miProfesores;
    private javax.swing.JMenu miRolesActivos;
    private javax.swing.JCheckBoxMenuItem miRolesAdmin;
    private javax.swing.JCheckBoxMenuItem miRolesDirectivo;
    private javax.swing.JCheckBoxMenuItem miRolesJE;
    private javax.swing.JCheckBoxMenuItem miRolesProfesor;
    private javax.swing.JTabbedPane panelPestanas;
    private javax.swing.JPanel panelPrincipal;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    // End of variables declaration//GEN-END:variables
    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;
    private JDialog aboutBox;
}
