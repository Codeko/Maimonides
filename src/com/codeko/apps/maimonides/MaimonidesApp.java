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
 * MaimonidesApp.java
 */
package com.codeko.apps.maimonides;

import com.codeko.apps.maimonides.conf.Configuracion;
import com.codeko.apps.maimonides.cache.Cache;
import com.codeko.apps.maimonides.conf.PanelConfiguracionAccesoBD;
import com.codeko.apps.maimonides.mantenimiento.Mantenimiento;
import com.codeko.apps.maimonides.elementos.AnoEscolar;
import com.codeko.apps.maimonides.mantenimiento.Backup;
import com.codeko.apps.maimonides.mantenimiento.PanelMensajesActualizacion;
import com.codeko.apps.maimonides.usr.GestorUsuarioClave;
import com.codeko.apps.maimonides.usr.Usuario;
import com.codeko.swing.CdkControlProgresos;
import com.codeko.util.CTiempo;
import com.codeko.util.GUI;
import com.codeko.util.Str;
import com.mysql.jdbc.Connection;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.TrayIcon;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.EventObject;
import java.util.GregorianCalendar;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.prefs.Preferences;
import javax.jnlp.ServiceManager;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskEvent;
import org.jdesktop.application.TaskListener;

public class MaimonidesApp extends SingleFrameApplication {

    AnoEscolar ano = null;
    TrayIcon trayIcon = null;
    private File ultimoArchivo = null;
    private Conector conector = new Conector();
    private CdkControlProgresos controlProgresos = new CdkControlProgresos();
    private Configuracion configuracion = null;
    private static Boolean debug = null;
    private static boolean jnlp=MaimonidesApp._isJnlp();
    Usuario usuario = null;
    static boolean reiniciarTrasEditarConfig = false;
    static String[] baseArgs = null;
    PropertyChangeListener usrListener = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent pce) {
            actualizarTitulo();
        }
    };
    private static boolean _isJnlp(){
        return ServiceManager.getServiceNames()!=null;
    }
    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        if (this.usuario != null) {
            this.usuario.removePropertyChangeListener(usrListener);
        }
        Usuario old = this.usuario;
        this.usuario = usuario;
        if (this.usuario != null) {
            this.usuario.addPropertyChangeListener(usrListener);
        }
        //Ahora asignamos el año escolar del usuario
        asignarAnoEscolarUsuario();
        actualizarTitulo();
        firePropertyChange("usuario", old, null);
        firePropertyChange("usuario", old, usuario);
        if (usuario == null && old != null) {
            boolean ret = GestorUsuarioClave.getGestor().pedirUsuarioClave();
            if (!ret) {
                quit(null);
            }
        }

    }

    private void asignarAnoEscolarUsuario() {
        //Cargamos el año actual
        if (getUsuario() != null) {
            int anoUsuario = Preferences.userNodeForPackage(MaimonidesApp.class).getInt(Usuario.getIUA() + "ultimoAno", 0);
            if (anoUsuario != 0) {
                //cargamos el ano actual
                AnoEscolar anoEscolar;
                try {
                    anoEscolar = AnoEscolar.getAnoEscolar(anoUsuario);
                    setAno(anoEscolar);
                } catch (SQLException ex) {
                    Logger.getLogger(MaimonidesApp.class.getName()).log(Level.SEVERE, "Error SQL cargando año escolar: " + anoUsuario, ex);
                } catch (Exception ex) {
                    Logger.getLogger(MaimonidesApp.class.getName()).log(Level.WARNING, "Error cargando ano escolar : " + anoUsuario, ex);
                }
            }
        }
    }

    public Configuracion getConfiguracion() {
        if (configuracion == null) {
            configuracion = new Configuracion();
        }
        return configuracion;
    }

    public static boolean isDebug() {
        if (debug == null) {
            debug = new File("debug").exists();
        }
        return debug;
    }

    public static boolean isJnlp() {
        return jnlp;
    }
    
    public static Connection getConexion() {
        return getApplication().getConector().getConexion();
    }

    public CdkControlProgresos getControlProgresos() {
        return controlProgresos;
    }

    public void setControlProgresos(CdkControlProgresos controlProgresos) {
        this.controlProgresos = controlProgresos;
    }

    public AnoEscolar getAnoEscolar() {
        return ano;
    }

    public void setAno(AnoEscolar ano) {
        AnoEscolar old = this.ano;
        this.ano = ano;
        firePropertyChange("anoEscolar", old, ano);
        actualizarTitulo();
        //Al cambiar de año escolar hay que limpiar la cache
        Cache.clear();
    }

    public void actualizarTitulo() {
        String infoAno = "No se ha especificado año escolar";
        if (getAnoEscolar() != null) {
            infoAno = getAnoEscolar().getNombre() + " [" + getAnoEscolar().getId() + "]";
        }
        String infoUsr = " ";
        if (getUsuario() != null) {
            infoUsr = " :: " + getUsuario().getNombre();
            if (getUsuario().getProfesor() != null) {
                infoUsr += " - " + getUsuario().getProfesor().getDescripcionObjeto();
            }
        }        
        getMainFrame().setTitle(this.getContext().getResourceMap().getString("Application.title") + infoAno + infoUsr);
    }

    public Conector getConector() {
        return conector;
    }

    public File getUltimoArchivo() {
        if (ultimoArchivo == null) {
            String fUlt = Preferences.userNodeForPackage(MaimonidesApp.class).get(Usuario.getIUA() + "ultimoArchivo", null);
            if (fUlt != null && !fUlt.trim().equals("")) {
                ultimoArchivo = new File(fUlt);
            } else {
                ultimoArchivo = new File(System.getProperty("user.dir"));
            }
        }
        return ultimoArchivo;
    }

    public void setUltimoArchivo(File ultimoArchivo) {
        this.ultimoArchivo = ultimoArchivo;
    }

    @Override
    protected void startup() {
        configurarLogging();
        if (getConector().cargarConfiguracion()) {
            addExitListener(new ExitListener() {

                @Override
                public boolean canExit(EventObject e) {
                    boolean bOkToExit = true;
                    if (e != null && e.getSource() != null) {
                        bOkToExit = false;
                        Component source = (Component) e.getSource();
                        bOkToExit = JOptionPane.showConfirmDialog(source, "¿Realmente desea salir?", "Salir", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
                    }
                    return bOkToExit;
                }

                @Override
                public void willExit(EventObject event) {
                }
            });

            MaimonidesView view = new MaimonidesView(this);
            show(view);
            //A veces pierde el tamaño así que si tiene un tamaño no lógico se cambia al por defecto
            if (getMainFrame().getSize().getWidth() < 300 || getMainFrame().getSize().getHeight() < 300) {
                getMainFrame().setSize(800, 600);
                GUI.centrar(getMainFrame());
            }
            //setAno(null);
            //TODO Corregir en versión para linux o eliminar del todo
            //cargarIcono();
            addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if ("conectar".equals(evt.getPropertyName())) {
                        if (evt.getNewValue() != null) {
                        }
                    }
                }
            });
            MaimonidesUtil.ejecutarTask(this, "conectar");

        } else {
            JOptionPane.showMessageDialog(null, "No existe configuración de conexión.\nRellene los campos de conexión y vuelva a iniciar el programa.", "Error", JOptionPane.ERROR_MESSAGE);
            reiniciarTrasEditarConfig = true;
            MaimonidesUtil.ejecutarTask(this, "editarConexion");
        }
    }

    @Override
    protected void shutdown() {
        operacionesPreSalida();
        try {
            if (getConector() != null && getConector().getConexionSinConectar() != null) {
                getConector().getConexion().close();
            }
        } catch (Exception ex) {
            Logger.getLogger(MaimonidesApp.class.getName()).log(Level.SEVERE, null, ex);
        }
        super.shutdown();
    }

    @Override
    protected void configureWindow(java.awt.Window root) {
        try {
            root.setIconImage(this.getContext().getResourceMap().getImageIcon("Application.icon").getImage());
        } catch (Exception e) {
            Logger.getLogger(MaimonidesApp.class.getName()).log(Level.SEVERE, "No se encuentra el icono de la aplicación", e);
        }
    }

    public static MaimonidesApp getApplication() {
        return Application.getInstance(MaimonidesApp.class);
    }

    public static void main(String[] args) {
        baseArgs = args;
        CTiempo.setActivo(false);
        launch(MaimonidesApp.class, args);
    }

    public void reiniciar() {
        //Hacemos las operaciones presalida antes para que la nueva copia las tenga disponibles
        operacionesPreSalida();
        ProcessBuilder pb = new ProcessBuilder("maimonides.exe", "-reiniciado", "restauracion_copia");
        try {
            pb.start();
        } catch (IOException ex) {
            Logger.getLogger(MaimonidesApp.class.getName()).log(Level.SEVERE, null, ex);
        }
        exit();
    }

    private void configurarLogging() {
        try {
            FileHandler fh = null;
            if(isJnlp()){
                fh=new FileHandler(Configuracion.getSubCarpertaUsuarioMaimonides("log")+"/mm_%g.log", 1024 * 1024 * 10, 10, true);
            }else{
                File f = new File("log");
                f.mkdirs();
                fh=new FileHandler("log/mm_%g.log", 1024 * 1024 * 10, 10, true);
            }
            fh.setFormatter(new SimpleFormatter());
            Logger.getLogger("").addHandler(fh);
        } catch (Exception ex) {
            Logger.getLogger(MaimonidesApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Action
    public void editarConexion() {
        if(isJnlp()){
            JOptionPane.showMessageDialog(getMainFrame(), "No se puede editar la configuración de conexión en modo de ejecución web.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        final JFrame f = new JFrame("Editor de configuración de conexión");
        f.setName("FrameEditorConexion");
        f.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent we) {
                if (reiniciarTrasEditarConfig) {
                    exit();
                }
            }
        });
        PanelConfiguracionAccesoBD panel = new PanelConfiguracionAccesoBD();
        panel.addPropertyChangeListener("guardar", new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                if (pce.getNewValue() instanceof Boolean && ((Boolean) pce.getNewValue())) {
                    f.dispose();
                    if (reiniciarTrasEditarConfig) {
                        exit();
                    }
                }
            }
        });
        f.add(panel);
        f.setAlwaysOnTop(true);
        f.validate();
        f.pack();
        show(f);
    }

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task conectar() {
        ConectarTask t = new ConectarTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
        t.addTaskListener(new TaskListener<Boolean, Void>() {

            @Override
            public void doInBackground(TaskEvent te) {
                //throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void process(TaskEvent te) {
                //throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void succeeded(TaskEvent te) {
                Boolean ret = (Boolean) te.getValue();
                if (ret) {
                    if (operacionesPostConexion()) {
                        MaimonidesUtil.ejecutarTask(this, "actualizarVersion");
                    } else {
                        quit(null);
                    }
                } else {
                    MaimonidesUtil.ejecutarTask(this, "editarConexion");
                }
                getMaimonidesView().setConectado(ret);

            }

            @Override
            public void failed(TaskEvent te) {
                //throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void cancelled(TaskEvent te) {
                //throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void interrupted(TaskEvent te) {
                //throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void finished(TaskEvent te) {
                //throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        return t;
    }

    private void operacionesPreSalida() {
        if (getUltimoArchivo() != null) {
            Preferences.userNodeForPackage(MaimonidesApp.class).put(Usuario.getIUA() + "ultimoArchivo", getUltimoArchivo().getAbsolutePath());
        } else {
            Preferences.userNodeForPackage(MaimonidesApp.class).remove(Usuario.getIUA() + "ultimoArchivo");
        }
        if (getAnoEscolar() != null) {
            Preferences.userNodeForPackage(MaimonidesApp.class).put(Usuario.getIUA() + "ultimoAno", getAnoEscolar().getId() + "");
        } else {
            Preferences.userNodeForPackage(MaimonidesApp.class).remove(Usuario.getIUA() + "ultimoAno");
        }
    }

    private class ConectarTask extends org.jdesktop.application.Task<Boolean, Void> {

        ConectarTask(org.jdesktop.application.Application app) {
            super(app);
            this.addPropertyChangeListener(MaimonidesApp.getApplication().getControlProgresos());
            firePropertyChange("setIniciado", null, true);
            setMessage("Conectando con la base de datos...");
            firePropertyChange("setMensaje", null, "Conectando con la base de datos...");
            setUserCanCancel(false);

        }

        @Override
        protected Boolean doInBackground() {
            boolean ret = false;
            try {
                setMessage("Conectando con la base de datos...");
                firePropertyChange("setMensaje", null, "Conectando con la base de datos...");
                getConector().addPropertyChangeListener(new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                        if ("setMensaje".equals(evt.getPropertyName())) {
                            setMessage(Str.noNulo(evt.getNewValue()));
                        }
                    }
                });
                Connection c = getConector().getConexion();
                if (c != null && !c.isClosed()) {
                    ret = true;
                }
            } catch (SQLException ex) {
                Logger.getLogger(MaimonidesApp.class.getName()).log(Level.SEVERE, null, ex);
            }
            return ret;
        }

        @Override
        protected void succeeded(Boolean result) {
            firePropertyChange("setTerminado", null, result);
            if (result) {
                setMessage("Conexión realizada con éxito.");
            } else {
                setMessage("Error conectando con la base de datos.");
                JOptionPane.showMessageDialog(null, "Error conectando a la base de datos.\nRevise la configuración.", "Error", JOptionPane.ERROR_MESSAGE);
                reiniciarTrasEditarConfig = true;
            }
            MaimonidesApp.getApplication().firePropertyChange("conectar", null, result);
        }
    }

    private boolean operacionesPostConexion() {
        boolean ret = true;
        //Vemos si la versión del programa es la misma que la versión de la base de datos
        if (!Mantenimiento.isAppPosteriorABD()) {
            JOptionPane.showMessageDialog(getMainFrame(), "La versión de Maimónides que está ejecutando (" + Mantenimiento.getAplicationVersion() + ") es anterior a la versión del servidor (" + Mantenimiento.getBDVersion() + ").\nLo más probable es que este ejecutando una copia antigua de Maimónides, actualice la aplicación.\nMaimónides se cerrará ahora.", "Error de versiones", JOptionPane.ERROR_MESSAGE);
            ret = false;
        }
        //Ahora preguntamos el usuario y clave
        if (ret) {
            ret = GestorUsuarioClave.getGestor().pedirUsuarioClave();
        }
        return ret;
    }

    public static MaimonidesView getMaimonidesView() {
        return (MaimonidesView) getApplication().getMainView();
    }

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task actualizarVersion() {
        return new ActualizarVersionTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class ActualizarVersionTask extends org.jdesktop.application.Task<Object, Void> {

        Mantenimiento c = null;

        public ActualizarVersionTask(org.jdesktop.application.Application app) {
            super(app);
            setUserCanCancel(false);
        }

        @Override
        protected Object doInBackground() {
            Backup b = new Backup();
            //TODO Volver a activar esto
            if (false && b.esNecesarioHacerBackup()) {
                b.addPropertyChangeListener(new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                    }
                });
                File f = b.hacerBackup(true);
                if (f != null && f.exists() && f.length() > 0) {
                    getConfiguracion().setUltimoBackup(new GregorianCalendar());
                }
            }
            c = new Mantenimiento();
            c.addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                }
            });

            c.mantenimiento();
            return null;
        }

        @Override
        protected void succeeded(Object result) {
            if (c != null) {
                if (!c.getMensajes().isEmpty()) {
                    //Si hay mensajes los mostramos
                    PanelMensajesActualizacion p = new PanelMensajesActualizacion();
                    p.setMensajes(c.getMensajes());
                    p.setPreferredSize(new Dimension(500, 500));
                    JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), p, "Notificaciones de actualización", JOptionPane.PLAIN_MESSAGE);
                }
            }
        }
    }
}
