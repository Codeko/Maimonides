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
package com.codeko.apps.maimonides;

import com.codeko.apps.maimonides.conf.Configuracion;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.mantenimiento.Mantenimiento;
import com.codeko.util.Cripto;
import com.codeko.util.Obj;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.Statement;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JOptionPane;
import javax.swing.Timer;

public class Conector extends MaimonidesBean {

    private static LinkedHashMap<String, String> tablas = null;
    private static LinkedHashMap<String, String> vistas = null;
    private static final String DRIVER = "com.mysql.jdbc.Driver";
    private Connection conexion = null;
    private String baseDeDatos = "mm";
    private String host = "localhost";
    private String usuario = "root";
    private String clave = "";
    private boolean errorCreando = false;
    private int errorCode = 0;
    boolean conectando = false;
    Timer controlConexion = null;
    private static URL configURL = null;
    private static boolean configReadOnly = false;

    public static boolean isConfigReadOnly() {
        return configReadOnly;
    }

    public static void setConfigReadOnly(boolean configReadOnly) {
        Conector.configReadOnly = configReadOnly;
    }

    public static URL getConfigURL() {
        return configURL;
    }

    public static void setConfigURL(URL configURL) {
        Conector.configURL = configURL;
    }

    public Timer getControlConexion() {
        if (controlConexion == null) {
            controlConexion = new Timer(1000 * 30, new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    verificarConexion();
                }
            });
            controlConexion.setRepeats(true);
        }
        return controlConexion;
    }

    public synchronized boolean isConectando() {
        return conectando;
    }

    public synchronized void setConectando(boolean conectando) {
        boolean old = this.conectando;
        this.conectando = conectando;
        firePropertyChange("conectado", old, conectando);
    }

    public Conector() {
        try {
            Class.forName(DRIVER).newInstance();
        } catch (Exception e) {
            Logger.getLogger(Conector.class.getName()).log(Level.SEVERE, "Error registrando driver: " + DRIVER, e);
        }


    }

    public static boolean guardarConfiguracion(Properties p) {
        boolean ret = false;
        if (!isConfigReadOnly()) {
            if (!p.containsKey("pass") || p.getProperty("pass", "").equals("")) {
                p.put("pass", Cripto.encriptar(p.getProperty("clave", ""), Configuracion.KEY_CRIPTO));
            }
            p.remove("clave");
            if (Conector.getConfigURL() != null) {
                FileOutputStream os = null;
                try {
                    File f = new File(Conector.getConfigURL().toURI());
                    if (f != null && f.exists() && f.canWrite()) {
                        os = new FileOutputStream(f);
                        p.store(os, "Configuración de conexión");
                        ret = true;
                    } else {
                        Logger.getLogger(Conector.class.getName()).log(Level.WARNING, "No se puede escribir en {0}, se guardar\u00e1 configuraci\u00f3n en propiedades.", f);
                    }
                } catch (Exception ex) {
                    Logger.getLogger(Conector.class.getName()).log(Level.SEVERE, "No se puede conectar con la url " + Conector.getConfigURL(), ex);
                } finally {
                    Obj.cerrar(os);
                }
            }
            //Si algo falla lo guardamos en preferencias
            if (ret == false) {
                Preferences prefs = Preferences.userNodeForPackage(Conector.class);
                prefs.put("bd", p.getProperty("bd"));
                prefs.put("host", p.getProperty("host"));
                prefs.put("usuario", p.getProperty("usuario"));
                prefs.put("pass", p.getProperty("pass"));
                ret = true;
            }
            MaimonidesApp.getApplication().getConector().cargarConfiguracion();
        }
        return ret;
    }

    public Properties getConfiguracion() {
        Properties p = new Properties();
        try {
            setConfigReadOnly(false);
            //Primero vemos si existe fichero de configuración en la carpeta de usuario
            File configFile = new File(Configuracion.getCarpetaUsuarioMaimonides(), Configuracion.CONN_CFG_FILE);
            Logger.getLogger(Conector.class.getName()).log(Level.INFO, "Intentando cargar configuraci\u00f3n desde {0}...", configFile.getAbsolutePath());
            setConfigURL(configFile.toURI().toURL());
            if (!loadPropertiesFromFile(p, configFile)) {
                configFile = new File(Configuracion.CONN_CFG_FILE);
                Logger.getLogger(Conector.class.getName()).log(Level.INFO, "Intentando cargar configuraci\u00f3n desde {0}...", configFile.getAbsolutePath());
                setConfigURL(configFile.toURI().toURL());
                //Vemos si se encuentra en la carpeta de ejecución
                if (!loadPropertiesFromFile(p, configFile)) {
                    //Finalmente intentamos cargarlo desde fichero de configuración dentro de los jar
                    Logger.getLogger(Conector.class.getName()).log(Level.INFO, "Intentando cargar configuraci\u00f3n desde jar...");
                    if (loadPropertiesFromStream(p, Configuracion.class.getResourceAsStream(Configuracion.CONN_CFG_FILE))) {
                        setConfigURL(Configuracion.class.getResource(Configuracion.CONN_CFG_FILE));
                        setConfigReadOnly(true);
                    } else {
                        Logger.getLogger(Conector.class.getName()).log(Level.INFO, "Cargando configuraci\u00f3n desde properties...");
                        setConfigURL(null);
                        //Si no está en ninguna las cogemos de las propiedades de usuario 
                        //con valors por defecto de variables de sistema
                        Preferences prefs = Preferences.userNodeForPackage(Conector.class);
                        p.setProperty("pass", prefs.get("pass", System.getProperty("maimonides.conn.pass", "")));
                        p.setProperty("clave", prefs.get("clave", System.getProperty("maimonides.conn.clave", "")));
                        p.setProperty("bd", prefs.get("bd", System.getProperty("maimonides.conn.bd", "maimonides")));
                        p.setProperty("usuario", prefs.get("usuario", System.getProperty("maimonides.conn.usuario", "root")));
                        p.setProperty("host", prefs.get("host", System.getProperty("maimonides.conn.host", "localhost")));
                    }
                }
            }
            if (p.containsKey("pass") && !p.getProperty("pass").equals("")) {
                p.put("clave", Cripto.desencriptar(p.getProperty("pass", ""), Configuracion.KEY_CRIPTO));
            }
            setBaseDeDatos(p.getProperty("bd", "maimonides"));
            setHost(p.getProperty("host", "localhost"));
            setUsuario(p.getProperty("usuario", "root"));
            setClave(p.getProperty("clave", ""));
        } catch (Exception ex) {
            setConfigURL(null);
            Logger.getLogger(Conector.class.getName()).log(Level.SEVERE, "Error cargando configuracion", ex);
        }
        return p;
    }

    public boolean cargarConfiguracion() {
        boolean ret = false;
        try {
            Properties p = getConfiguracion();
            setBaseDeDatos(p.getProperty("bd", "maimonides"));
            setHost(p.getProperty("host", "localhost"));
            setUsuario(p.getProperty("usuario", "root"));
            setClave(p.getProperty("clave", ""));
            ret = true;
        } catch (Exception ex) {
            setConfigURL(null);
            Logger.getLogger(Conector.class.getName()).log(Level.SEVERE, "Error cargando configuracion", ex);
            ret = false;
        }
        return ret;
    }

    private static boolean loadPropertiesFromFile(Properties p, File f) {
        FileInputStream fis = null;
        if (f != null && f.exists() && f.canRead()) {
            try {
                fis = new FileInputStream(f);
                return loadPropertiesFromStream(p, fis);
            } catch (Exception ex) {
                Logger.getLogger(Conector.class.getName()).log(Level.WARNING, "Error cargando configuracion de " + f, ex);
            }
        }
        return false;
    }

    private static boolean loadPropertiesFromStream(Properties p, InputStream is) {
        if (is != null) {
            try {
                p.load(is);
                return true;
            } catch (Exception ex) {
                Logger.getLogger(Conector.class.getName()).log(Level.WARNING, "Error cargando configuracion de stream", ex);
            } finally {
                Obj.cerrar(is);
            }
        }
        return false;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public boolean isErrorCreando() {
        return errorCreando;
    }

    public void setErrorCreando(boolean errorCreando) {
        this.errorCreando = errorCreando;
    }

    public String getBaseDeDatos() {
        return baseDeDatos;
    }

    public void setBaseDeDatos(String baseDeDatos) {
        this.baseDeDatos = baseDeDatos;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String ip) {
        this.host = ip;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public Connection getConexionSinConectar() {
        return conexion;
    }

    public void verificarConexion() {
        verificarConexion(true);
    }

    public void verificarConexion(boolean conMensaje) {
        try {
            Logger.getLogger(Conector.class.getName()).fine("Verificando estado de la conexión a la base de datos.");
            if (conexion == null || conexion.isClosed() || !conexion.isValid(300)) {
                conexion = conectar();
                if (conexion == null || conexion.isClosed() || !conexion.isValid(300)) {
                    if (conMensaje) {
                        JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), "Parece que se ha perdido la conexión con la base de datos.\nPuede que su equipo haya perdido la conexión a la red \no que la base de datos haya fallado.\nRevise la base de datos y reinicie el programa.", "Error conectando a la base de datos", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Conector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Connection getConexion() {
        try {
            if (conexion == null || conexion.isClosed()) {
                conexion = conectar();
                verificarConexion(false);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Conector.class.getName()).log(Level.SEVERE, null, ex);
        }
        return conexion;
    }

    public static PreparedStatement prepareSt(String sql) throws SQLException {
        return (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
    }

    public boolean isConectado() {
        try {
            return getConexionSinConectar() != null && !getConexionSinConectar().isClosed();
        } catch (SQLException ex) {
            Logger.getLogger(Conector.class.getName()).log(Level.SEVERE, "Error verificando estado de la conexión", ex);
        }
        return false;
    }

    private String getURL() {
        return "jdbc:mysql://" + getHost() + "/" + getBaseDeDatos() + "?useServerPrepStmts=true&zeroDateTimeBehavior=convertToNull&user=" + getUsuario() + "&password=" + getClave();
    }

    private String getURLGeneral() {
        return "jdbc:mysql://" + getHost() + "/mysql?user=" + getUsuario() + "&password=" + getClave() + "";
    }

    private Connection conectar() {
        if (isConectado()) {
            return null;
        }
        setConectando(true);
        Connection c = null;
        try {
            Logger.getLogger(Conector.class.getName()).log(Level.FINE, "Conectando a:{0}", getURL());
            c = (com.mysql.jdbc.Connection) DriverManager.getConnection(getURL());
            //Vemos si existe la tabla config, si no existe se asume que hay que regenerar la base de datos
            if (!Mantenimiento.existeTabla(c, "config")) {
                if (crearEstructuraBaseDeDatos(c)) {
                    firePropertyChange("setMensaje", null, "Estructura para base de datos " + getBaseDeDatos() + " verificada.");
                    Logger.getLogger(Conector.class.getName()).log(Level.INFO, "Estructura para base de datos {0} verificada.", getBaseDeDatos());
                } else {
                    firePropertyChange("setMensaje", null, "No se ha podido crear la estructura para base de datos " + getBaseDeDatos() + ".");
                    //TODO Mostrar error
                }
            }
            getControlConexion().start();
        } catch (SQLException ex) {
            getControlConexion().stop();
            setErrorCode(ex.getErrorCode());
            if (ex.getErrorCode() == 1049) {
                //Entonces creamos la base de datos
                boolean creada = true;
                try {
                    firePropertyChange("setMensaje", null, "Se va a crear la base de datos " + getBaseDeDatos());
                    Logger.getLogger(Conector.class.getName()).log(Level.INFO, "Se va a crear la base de datos {0}", getBaseDeDatos());
                    //DriverManager.setLogWriter(new PrintWriter(System.out));
                    Connection con = (com.mysql.jdbc.Connection) DriverManager.getConnection(getURLGeneral());
                    con.setCharacterEncoding("utf8");
                    con.setUseUnicode(true);
                    java.sql.PreparedStatement st = con.clientPrepareStatement("CREATE DATABASE " + getBaseDeDatos() + " DEFAULT CHARACTER SET = utf8 DEFAULT COLLATE = utf8_bin ");
                    st.execute();
                    st.execute("SET NAMES utf8");
                } catch (SQLException exc) {
                    firePropertyChange("setMensaje", null, "Error creando base de datos " + getBaseDeDatos());
                    Logger.getLogger(Conector.class.getName()).log(Level.SEVERE, "Error creando base de datos " + getBaseDeDatos(), exc);
                    creada = false;
                }
                //Con esto evitamos meternos en un bucle eterno de erorres de creacion
                if (creada && !isErrorCreando()) {
                    firePropertyChange("setMensaje", null, "Base de datos " + getBaseDeDatos() + " creada correctamente.");
                    Logger.getLogger(Conector.class.getName()).log(Level.INFO, "Base de datos {0} creada correctamente.", getBaseDeDatos());
                    c = conectar();
                    if (c == null) {
                        //TODO Mostrar error
                        firePropertyChange("setMensaje", null, "No se ha podido conectar con la base de datos " + getBaseDeDatos() + ".");
                    }
                } else {
                    setErrorCreando(true);
                }
            } else if (ex.getErrorCode() == 1045) {
                //TODO Mostrar mensaje de error de datos
                firePropertyChange("setMensaje", null, "Error conectando con la base de datos, usuario o clave no válidos.");
                Logger.getLogger(Conector.class.getName()).log(Level.SEVERE, "Error conectando con la base de datos, usuario o clave no válidos.", ex);
            } else {
                firePropertyChange("setMensaje", null, "Error conectando con la base de datos: " + ex.getErrorCode());
                Logger.getLogger(Conector.class.getName()).log(Level.SEVERE, "Error conectando con la base de datos: " + ex.getErrorCode(), ex);
            }

        }
        setConectando(false);
        return c;
    }

    public static LinkedHashMap<String, String> getVistas() {
        if (vistas == null) {
            vistas = new LinkedHashMap<String, String>();
            String profesores_ = "CREATE VIEW `profesores_` AS SELECT * FROM profesores WHERE fbaja IS NULL;";
            vistas.put("profesores_", profesores_);

            String asistencia_ = "CREATE VIEW `asistencia_` AS select a.id AS alumno_id,a.unidad_id,a.curso_id,a.ano,h.hora,pa.asistencia,p.fecha,h.id AS horario_id FROM alumnos AS a JOIN partes_alumnos AS pa ON pa.alumno_id=a.id JOIN partes AS p ON p.id=pa.parte_id JOIN horarios AS h ON h.id=pa.horario_id WHERE pa.asistencia>1;";
            vistas.put("asistencia_", asistencia_);

            String horarios_ = "CREATE VIEW `horarios_` AS SELECT * FROM horarios WHERE borrado=0";
            vistas.put("horarios_", horarios_);
        }
        return vistas;
    }

    public static LinkedHashMap<String, String> getTablas() {
        if (tablas == null) {
            tablas = new LinkedHashMap<String, String>();
            //El orden importa!
            String anos = "CREATE TABLE IF NOT EXISTS `anos` ("
                    + "  `id` int(10) unsigned NOT NULL auto_increment,"
                    + "  `nombre` varchar(255) collate utf8_bin NOT NULL default 'Nuevo año',"
                    + "  `ano` int(10) unsigned NOT NULL,"
                    + "  PRIMARY KEY  (`id`)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='Almacena los años escolares' ;";
            tablas.put("anos", anos);

            String actividades = "CREATE TABLE IF NOT EXISTS `actividades` ("
                    + "  `id` int(10) unsigned NOT NULL auto_increment,"
                    + "  `cod` int(10) unsigned NOT NULL,"
                    + "  `ano` int(10) unsigned NOT NULL,"
                    + "  `descripcion` text collate utf8_bin NOT NULL,"
                    + "  `es_regular` tinyint(1) NOT NULL default '0',"
                    + "  `necesita_unidad` tinyint(1) NOT NULL default '0',"
                    + "  `necesita_materia` tinyint(1) NOT NULL default '0',"
                    + "  PRIMARY KEY  (`id`),"
                    + "  UNIQUE KEY  (`cod`,`ano`),"
                    + "  CONSTRAINT `fk_actividades_ano` FOREIGN KEY (`ano`) REFERENCES `anos` (`id`) ON DELETE CASCADE ON UPDATE CASCADE"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='Almacena los distintos tipos de actividades';";
            tablas.put("actividades", actividades);

            String tramos = "CREATE TABLE IF NOT EXISTS `tramos` ("
                    + "  `id` int(10) unsigned NOT NULL auto_increment,"
                    + "  `cod` int(10) unsigned NOT NULL,"
                    + "  `ano` int(10) unsigned NOT NULL,"
                    + "  `hora` tinyint(3) unsigned NOT NULL,"
                    + "  `inicio` int(10) unsigned NOT NULL,"
                    + "  `fin` int(10) unsigned NOT NULL,"
                    + "  `jornada` int(10) unsigned NOT NULL,"
                    + "  PRIMARY KEY  (`id`),"
                    + "  UNIQUE KEY  (`cod`,`ano`),"
                    + "  CONSTRAINT `fk_tramos_ano` FOREIGN KEY (`ano`) REFERENCES `anos` (`id`) ON DELETE CASCADE ON UPDATE CASCADE "
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='Almacena los periodos de tiempo para una actividad';";
            tablas.put("tramos", tramos);

            String profesores = "CREATE TABLE IF NOT EXISTS `profesores` ("
                    + "  `id` int(10) unsigned NOT NULL auto_increment,"
                    + "  `cod` int(10) unsigned NOT NULL,"
                    + "  `ano` int(10) unsigned NOT NULL,"
                    + "  `nombre` varchar(255) collate utf8_bin NOT NULL,"
                    + "  `apellido1` varchar(255) collate utf8_bin NOT NULL,"
                    + "  `apellido2` varchar(255) collate utf8_bin NOT NULL,"
                    + "  `puesto` varchar(255) collate utf8_bin NOT NULL,"
                    + "  `ftoma` date DEFAULT NULL,"
                    + "  `fbaja` DATE DEFAULT NULL,"
                    + "  `email` VARCHAR(255) NOT NULL DEFAULT '', "
                    + "  PRIMARY KEY  (`id`),"
                    + " CONSTRAINT `fk_profesores_ano` FOREIGN KEY (`ano`) REFERENCES `anos` (`id`) ON DELETE CASCADE ON UPDATE CASCADE "
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='Almacena datos de los profesores';";
            tablas.put("profesores", profesores);


            String cursos = "CREATE TABLE IF NOT EXISTS `cursos` ("
                    + "  `id` int(10) unsigned NOT NULL auto_increment,"
                    + "  `cod` INTEGER UNSIGNED DEFAULT NULL,"
                    + "  `ano` int(10) unsigned NOT NULL,"
                    + "  `curso` varchar(100) NOT NULL,"
                    + "  `descripcion` varchar(255) collate utf8_bin NOT NULL,"
                    + "  `posicion` int(10) unsigned NOT NULL DEFAULT 0,"
                    + "  `maxFaltas` INTEGER UNSIGNED NOT NULL DEFAULT 0 ,"
                    + "  `fini` DATE,"
                    + "  `ffin` DATE,"
                    + "  PRIMARY KEY  (`id`),"
                    + "  UNIQUE KEY  (`cod`,`ano`),"
                    + "  KEY `Index_cursos_curso` (`curso`),"
                    + "  CONSTRAINT `fk_cursos_ano` FOREIGN KEY (`ano`) REFERENCES `anos` (`id`) ON DELETE CASCADE ON UPDATE CASCADE"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='Almacena los cursos escolares';";
            tablas.put("cursos", cursos);

            String materias = "CREATE TABLE IF NOT EXISTS `materias` ("
                    + "  `id` int(10) unsigned NOT NULL auto_increment,"
                    + "  `cod` int(10) unsigned NOT NULL,"
                    + "  `ano` int(10) unsigned NOT NULL,"
                    + "  `codigo_materia` varchar(10) collate utf8_bin NOT NULL,"
                    + "  `nombre` varchar(255) collate utf8_bin NOT NULL,"
                    + "  `curso_id` INTEGER UNSIGNED DEFAULT NULL,"
                    + "  `evaluable` tinyint(1) unsigned NOT NULL DEFAULT '1',"
                    + "  `maximo_alumnos` int(10) unsigned NOT NULL,"
                    + "  `maxFaltas` INTEGER UNSIGNED NOT NULL DEFAULT 0 ,"
                    + "  PRIMARY KEY  (`id`),"
                    + "  CONSTRAINT `fk_materias_ano` FOREIGN KEY (`ano`) REFERENCES `anos` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,"
                    + "  CONSTRAINT `fk_materias_curso` FOREIGN KEY (`curso_id`) REFERENCES `cursos` (`id`) ON DELETE CASCADE ON UPDATE CASCADE"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='Almacena las distintas asignaturas';";
            tablas.put("materias", materias);

            String unidades = "CREATE TABLE IF NOT EXISTS `unidades` ("
                    + "  `id` int(10) unsigned NOT NULL auto_increment,"
                    + "  `cod` INTEGER UNSIGNED DEFAULT NULL,"
                    + "  `ano` int(10) unsigned NOT NULL,"
                    + "  `descripcion` varchar(255) collate utf8_bin NOT NULL,"
                    + "  `nombre_original` VARCHAR(255)  NOT NULL,"
                    + "  `curso` VARCHAR(100) DEFAULT NULL,"
                    + "  `grupo` char(1) collate utf8_bin NOT NULL,"
                    + "  `cursogrupo` varchar(101) collate utf8_bin NOT NULL,"
                    + "  `curso_id` INTEGER UNSIGNED DEFAULT NULL,"
                    + "  `curso2_id` INTEGER UNSIGNED DEFAULT NULL,"
                    + "  `posicion` int(10) unsigned NOT NULL DEFAULT 0,"
                    + "  `capacidad` int(10) unsigned NOT NULL DEFAULT 0,"
                    + "  `tutor_id` int(10) unsigned,"
                    + "  PRIMARY KEY  (`id`),"
                    + "  UNIQUE KEY  (`cod`,`ano`),"
                    + "  CONSTRAINT `fk_unidades_ano` FOREIGN KEY (`ano`) REFERENCES `anos` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,"
                    + "  CONSTRAINT `fk_unidades_curso` FOREIGN KEY (`curso_id`) REFERENCES `cursos` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,"
                    + "  CONSTRAINT `fk_unidades_curso2` FOREIGN KEY (`curso2_id`) REFERENCES `cursos` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,"
                    + "  CONSTRAINT `fk_unidades_profesores` FOREIGN KEY (`tutor_id`) REFERENCES `profesores` (`id`) ON DELETE SET NULL ON UPDATE CASCADE"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='Almacena las distintas unidades escolares (curso + grupo)';";
            tablas.put("unidades", unidades);

            String dependencias = "CREATE TABLE IF NOT EXISTS `dependencias` ("
                    + "  `id` int(10) unsigned NOT NULL auto_increment,"
                    + "  `cod` int(10) unsigned DEFAULT NULL,"
                    + "  `ano` int(10) unsigned NOT NULL,"
                    + "  `nombre` varchar(255) NOT NULL DEFAULT '',"
                    + "  `descripcion` varchar(255) NOT NULL DEFAULT '',"
                    + "  PRIMARY KEY  (`id`),"
                    + "  CONSTRAINT `fk_dependencias_ano` FOREIGN KEY (`ano`) REFERENCES `anos` (`id`) ON DELETE CASCADE ON UPDATE CASCADE"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='Almacena los datos de aulas y otras dependencias';";
            tablas.put("dependencias", dependencias);

            String alumnos = "CREATE TABLE IF NOT EXISTS `alumnos` ("
                    + "  `id` int(10) unsigned NOT NULL auto_increment,"
                    + "  `cod` int(10) UNSIGNED NOT NULL DEFAULT 0,"
                    + "  `ano` int(10) unsigned NOT NULL,"
                    + "  `nombre` varchar(255) collate utf8_bin NOT NULL,"
                    + "  `unidad_id` int(10) unsigned,"
                    + "  `curso_id` int(10) unsigned,"
                    + "  `apellido1` varchar(255) collate utf8_bin NOT NULL,"
                    + "  `apellido2` varchar(255) collate utf8_bin NOT NULL,"
                    + "  `numescolar` varchar(255) collate utf8_bin NOT NULL,"
                    + "  `bilingue` tinyint(1) unsigned NOT NULL DEFAULT '0',"
                    + "  `repetidor` tinyint(1) unsigned NOT NULL DEFAULT '0',"
                    + "  `dicu` tinyint(1) unsigned NOT NULL DEFAULT '0',"
                    + "  `borrado` tinyint(1) unsigned NOT NULL DEFAULT '0',"
                    + "  `email` VARCHAR(255) NOT NULL  DEFAULT '',"
                    + "  `telefono` VARCHAR(255) NOT NULL  DEFAULT '',"
                    + "  `sms` VARCHAR(255) NOT NULL  DEFAULT '',"
                    + "  `direccion` VARCHAR(255) NOT NULL  DEFAULT '',"
                    + "  `cp` VARCHAR(10) NOT NULL  DEFAULT '',"
                    + "  `poblacion` VARCHAR(255) NOT NULL  DEFAULT '',"
                    + "  `notificar` INTEGER UNSIGNED NOT NULL DEFAULT 1,"
                    + "  `obs` LONGTEXT,"
                    + "  `dni` VARCHAR(45),"
                    + "  `fnacimiento` DATE,"
                    + "  `loc_nacimiento` VARCHAR(255) NOT NULL DEFAULT '',"
                    + "  `prov_nacimiento` VARCHAR(255) NOT NULL DEFAULT '',"
                    + "  `pais_nacimiento` VARCHAR(255) NOT NULL DEFAULT '',"
                    + "  `nacionalidad` VARCHAR(255) NOT NULL DEFAULT '',"
                    + "  `sexo` ENUM('H','M') NOT NULL,"
                    + "  `telefono_urgencia` VARCHAR(45) NOT NULL DEFAULT '',"
                    + "  `expediente` VARCHAR(45) NOT NULL DEFAULT '',"
                    + "  `t1_dni` VARCHAR(45) NOT NULL DEFAULT '',"
                    + "  `t1_nombre` VARCHAR(255) NOT NULL DEFAULT '',"
                    + "  `t1_apellido1` VARCHAR(255) NOT NULL DEFAULT '',"
                    + "  `t1_apellido2` VARCHAR(255) NOT NULL DEFAULT '',"
                    + "  `t1_sexo` ENUM('H','M') NOT NULL,"
                    + "  `t2_dni` VARCHAR(45) NOT NULL DEFAULT '',"
                    + "  `t2_nombre` VARCHAR(255) NOT NULL DEFAULT '',"
                    + "  `t2_apellido1` VARCHAR(255) NOT NULL DEFAULT '',"
                    + "  `t2_apellido2` VARCHAR(255) NOT NULL DEFAULT '',"
                    + "  `t2_sexo` ENUM('H','M') NOT NULL,"
                    + "  `codFaltas` VARCHAR(50)  NOT NULL DEFAULT '', "
                    + "  PRIMARY KEY  (`id`),"
                    //+ "  UNIQUE KEY  (`cod`,`ano`),"
                    + "  CONSTRAINT `FK_alumnos_cursos` FOREIGN KEY `FK_alumnos_cursos` (`curso_id`) REFERENCES `cursos` (`id`) ON DELETE CASCADE ON UPDATE CASCADE, "
                    + "  CONSTRAINT `fk_alumnos_ano` FOREIGN KEY (`ano`) REFERENCES `anos` (`id`) ON DELETE CASCADE ON UPDATE CASCADE, "
                    + "  CONSTRAINT `fk_alumnos_unidad` FOREIGN KEY (`unidad_id`) REFERENCES `unidades` (`id`) ON DELETE CASCADE ON UPDATE CASCADE"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='Almacena los datos básicos de alumnos';";
            tablas.put("alumnos", alumnos);


            String materias_alumnos = "CREATE TABLE IF NOT EXISTS `materias_alumnos` ("
                    + "  `alumno_id` int(10) unsigned NOT NULL,"
                    + "  `materia_id` int(10) unsigned NOT NULL,"
                    + "  PRIMARY KEY  (`alumno_id`,`materia_id`),"
                    + "  CONSTRAINT `fk_materias_alumnos_alumno` FOREIGN KEY (`alumno_id`) REFERENCES `alumnos` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,"
                    + "  CONSTRAINT `fk_materias_alumnos_materia` FOREIGN KEY (`materia_id`) REFERENCES `materias` (`id`) ON DELETE CASCADE ON UPDATE CASCADE"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='Relaciona a los alumnos con sus asignaturas';";
            tablas.put("materias_alumnos", materias_alumnos);

            String horarios = "CREATE TABLE IF NOT EXISTS `horarios` ("
                    + "  `id` int(10) unsigned NOT NULL auto_increment,"
                    + "  `ano` int(10) unsigned NOT NULL,"
                    + "  `dia` tinyint(3) unsigned NOT NULL,"
                    + "  `tramo_id` int(10) unsigned NOT NULL,"
                    + "  `hora` tinyint(3) unsigned NOT NULL,"
                    + "  `aula_id` int(10) unsigned default NULL,"
                    + "  `materia_id` int(10) unsigned ,"
                    + "  `profesor_id` int(10) unsigned default NULL,"
                    + "  `actividad_id` int(10) unsigned NOT NULL,"
                    + "  `unidad_id` int(10) unsigned NOT NULL,"
                    + "  `dicu` tinyint(1) unsigned NOT NULL DEFAULT '0',"
                    + "  `borrado` tinyint(1) unsigned NOT NULL DEFAULT '0',"
                    + "  `activo` tinyint(1) unsigned NOT NULL DEFAULT '1',"
                    + "  `falta` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                    + "  `fborrado` TIMESTAMP,"
                    + "  PRIMARY KEY  (`id`),"
                    + "  CONSTRAINT `fk_horarios_actividad` FOREIGN KEY (`actividad_id`) REFERENCES `actividades` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,"
                    + "  CONSTRAINT `fk_horarios_dependencia` FOREIGN KEY (`aula_id`) REFERENCES `dependencias` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,"
                    + "  CONSTRAINT `fk_horarios_materia` FOREIGN KEY (`materia_id`) REFERENCES `materias` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,"
                    + "  CONSTRAINT `fk_horarios_tramo` FOREIGN KEY (`tramo_id`) REFERENCES `tramos` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,"
                    + "  CONSTRAINT `fk_horarios_unidad` FOREIGN KEY (`unidad_id`) REFERENCES `unidades` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,"
                    + "  CONSTRAINT `fk_horarios_profesor` FOREIGN KEY (`profesor_id`) REFERENCES `profesores` (`id`) ON DELETE CASCADE ON UPDATE CASCADE"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='Almacena los datos de una actividad en un tramo concreto' ;";
            tablas.put("horarios", horarios);

            String apoyos_alumnos = "CREATE TABLE IF NOT EXISTS  `apoyos_alumnos` ("
                    + "`alumno_id` INTEGER UNSIGNED NOT NULL,"
                    + "`horario_id` INTEGER UNSIGNED NOT NULL,"
                    + "PRIMARY KEY (`alumno_id`, `horario_id`),"
                    + "CONSTRAINT `FK_apoyos_alumnos_alumnos` FOREIGN KEY `FK_apoyos_alumnos_alumnos` (`alumno_id`) REFERENCES `alumnos` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,"
                    + "CONSTRAINT `FK_apoyos_alumnos_horarios` FOREIGN KEY `FK_apoyos_alumnos_horarios` (`horario_id`) REFERENCES `horarios` (`id`) ON DELETE CASCADE ON UPDATE CASCADE"
                    + ") ENGINE = InnoDB;";
            tablas.put("apoyos_alumnos", apoyos_alumnos);

            String partes = "CREATE TABLE IF NOT EXISTS `partes` ("
                    + "`id` int(10) unsigned NOT NULL auto_increment,"
                    + "`ano` int(10) unsigned NOT NULL,"
                    + "`curso` varchar(10) NOT NULL,"
                    + "`unidad_id` int(10) unsigned default NULL,"
                    + "`descripcion` varchar(255) collate utf8_bin NOT NULL,"
                    + "`fecha` date NOT NULL,"
                    + "`digitalizado` tinyint(1) NOT NULL default '0',"
                    + "`primario` tinyint(1) NOT NULL default '0',"
                    + "`necesita_revision` tinyint(1) NOT NULL default '0',"
                    + "`fecha_revision` datetime default NULL,"
                    + "`justificado` tinyint(1) NOT NULL default '0',"
                    + "`procesado` tinyint(1) NOT NULL default '0',"
                    + "`apoyo` tinyint(1) NOT NULL default '0',"
                    + "`enviado` tinyint(1) NOT NULL default '0',"
                    + "PRIMARY KEY  (`id`),"
                    + " KEY `Index_partes_curso` (`curso`),"
                    + "CONSTRAINT `FK_partes_ano` FOREIGN KEY (`ano`) REFERENCES `anos` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,"
                    + "CONSTRAINT `FK_partes_unidad` FOREIGN KEY (`unidad_id`) REFERENCES `unidades` (`id`) ON DELETE CASCADE ON UPDATE CASCADE"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='almacena las cabeceras de los partes de faltas'";
            tablas.put("partes", partes);

            String partes_unidades = "CREATE TABLE IF NOT EXISTS `partes_unidades` ("
                    + "`parte_id` int(10) unsigned NOT NULL,"
                    + "`unidad_id` int(10) unsigned NOT NULL,"
                    + "PRIMARY KEY  (`parte_id`,`unidad_id`),"
                    + "CONSTRAINT `FK_partes_unidades_parte` FOREIGN KEY (`parte_id`) REFERENCES `partes` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,"
                    + "CONSTRAINT `FK_partes_unidades_unidad` FOREIGN KEY (`unidad_id`) REFERENCES `unidades` (`id`) ON DELETE CASCADE ON UPDATE CASCADE"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='Almacena las undiades relacionadas con cada parte'";
            tablas.put("partes_unidades", partes_unidades);

            String partes_horarios = "CREATE TABLE IF NOT EXISTS `partes_horarios` ("
                    + "`parte_id` int(10) unsigned NOT NULL,"
                    + "`horario_id` int(10) unsigned NOT NULL,"
                    + "`dividido` tinyint(1) NOT NULL default '0',"
                    + "`firmado` TINYINT(1) NOT NULL DEFAULT '1',"
                    + "PRIMARY KEY  (`parte_id`,`horario_id`),"
                    + "CONSTRAINT `FK_horarios_parte_partes` FOREIGN KEY (`parte_id`) REFERENCES `partes` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,"
                    + "CONSTRAINT `FK_horarios_parte_horarios` FOREIGN KEY (`horario_id`) REFERENCES `horarios` (`id`) ON DELETE CASCADE ON UPDATE CASCADE"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='Almacena los horarios asociados a un parte'";

            tablas.put("partes_horarios", partes_horarios);

            String alumnos_horarios = "CREATE TABLE IF NOT EXISTS `alumnos_horarios` ( "
                    + " `horario_id` int(10) unsigned NOT NULL default '0', "
                    + " `alumno_id` int(10) unsigned NOT NULL default '0', "
                    + " `activo` tinyint(1) unsigned NOT NULL default '1', "
                    + " PRIMARY KEY  (`horario_id`,`alumno_id`), "
                    + " KEY `FK_alumnos_horarios_alumnos` (`alumno_id`), "
                    + " CONSTRAINT `FK_alumnos_horarios_horarios` FOREIGN KEY (`horario_id`) REFERENCES `horarios` (`id`) ON DELETE CASCADE ON UPDATE CASCADE, "
                    + " CONSTRAINT `FK_alumnos_horarios_alumnos` FOREIGN KEY (`alumno_id`) REFERENCES `alumnos` (`id`) ON DELETE CASCADE ON UPDATE CASCADE "
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='Almacena los horarios asociados a un alumno'";
            tablas.put("alumnos_horarios", alumnos_horarios);

            String partes_alumnos = "CREATE TABLE IF NOT EXISTS `partes_alumnos` ("
                    + "  `parte_id` int(10) unsigned NOT NULL,"
                    + "  `horario_id` int(10) unsigned NOT NULL ,"
                    + "  `alumno_id` int(10) unsigned NOT NULL,"
                    + "  `asistencia` tinyint(3) unsigned NOT NULL default '0' COMMENT 'O: Indeterminado 1: Asistencia 2: Falta Injustificada 3: Expulsion 4:Falta Justificada',"
                    + "  `posicion` INT(10) NOT NULL DEFAULT 0 COMMENT 'Posición de la linea en el parte', "
                    + "  `estado` INT(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '0=No hay cambios, 1=Enviado', "
                    + "  PRIMARY KEY  (`parte_id`,`horario_id`,`alumno_id`),"
                    + "  KEY `FK_partes_alumnos_horarios` (`horario_id`),"
                    + "  KEY `FK_partes_alumnos_alumnos` (`alumno_id`),"
                    + "  KEY `Index_partes_alumnos_asistencia`(`asistencia`),"
                    + "  CONSTRAINT `FK_partes_alumnos_partes` FOREIGN KEY (`parte_id`) REFERENCES `partes` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,"
                    + "  CONSTRAINT `FK_partes_alumnos_horarios` FOREIGN KEY (`horario_id`) REFERENCES `horarios` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,"
                    + "  CONSTRAINT `FK_partes_alumnos_alumnos` FOREIGN KEY (`alumno_id`) REFERENCES `alumnos` (`id`) ON DELETE CASCADE ON UPDATE CASCADE"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin";

            tablas.put("partes_alumnos", partes_alumnos);

            String advertencias_imagenes = "CREATE TABLE IF NOT EXISTS `advertencias_imagenes` ("
                    + "  `id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,"
                    + "  `imagen` LONGBLOB NOT NULL,"
                    + "  PRIMARY KEY (`id`)"
                    + ") ENGINE = InnoDB COMMENT = 'Almacena la imagen escaneada de cada parte.'";
            tablas.put("advertencias_imagenes", advertencias_imagenes);

            String partes_advertencias = "CREATE TABLE IF NOT EXISTS `partes_advertencias` ("
                    + "  `id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,"
                    + "  `parte_id` INTEGER UNSIGNED ,"
                    + "  `mensaje` VARCHAR(255) NOT NULL,"
                    + "  `fecha` date NOT NULL,"
                    + "  `horario_id` int(10) unsigned ,"
                    + "  `alumno_id` int(10) unsigned ,"
                    // + "  `asistencia1` tinyint(3) NOT NULL default '0' COMMENT 'O: Indeterminado 1: Asistencia 2: Falta Injustificada 3: Expulsion 4:Falta Justificada',"
                    // + "  `asistencia2` tinyint(3) NOT NULL default '0' COMMENT 'O: Indeterminado 1: Asistencia 2: Falta Injustificada 3: Expulsion 4:Falta Justificada',"
                    // + "  `asistencia3` tinyint(3) NOT NULL default '0' COMMENT 'O: Indeterminado 1: Asistencia 2: Falta Injustificada 3: Expulsion 4:Falta Justificada',"
                    + "  `imagen_id` int(10) unsigned ,"
                    + "   `tipo` INTEGER UNSIGNED NOT NULL DEFAULT 0 ,"
                    + "   PRIMARY KEY (`id`),"
                    + "   CONSTRAINT `FK_partes_advertencias_partes` FOREIGN KEY `FK_partes_advertencias_partes` (`parte_id`) REFERENCES `partes` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,"
                    + "   CONSTRAINT `FK_partes_advertencias_horarios` FOREIGN KEY (`horario_id`) REFERENCES `horarios` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,"
                    + "   CONSTRAINT `FK_partes_advertencias_imagenes` FOREIGN KEY (`imagen_id`) REFERENCES `advertencias_imagenes` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,"
                    + "   CONSTRAINT `FK_partes_advertencias_alumnos` FOREIGN KEY (`alumno_id`) REFERENCES `alumnos` (`id`) ON DELETE CASCADE ON UPDATE CASCADE"
                    + "  ) ENGINE = InnoDB COMMENT = 'Almacena las advertencias al procesar los distintos partes'";
            tablas.put("partes_advertencias", partes_advertencias);

            String partes_imagenes = "CREATE TABLE IF NOT EXISTS `partes_imagenes` ("
                    + " `parte_id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,"
                    + " `imagen` LONGBLOB NOT NULL,"
                    + " PRIMARY KEY (`parte_id`),"
                    + " CONSTRAINT `FK_partes_imagenes_partes` FOREIGN KEY `FK_partes_imagenes_partes` (`parte_id`)"
                    + "  REFERENCES `partes` (`id`) ON DELETE CASCADE  ON UPDATE CASCADE"
                    + " ) ENGINE = InnoDB COMMENT = 'Almacena los archivos de escaneado de cada parte';";
            tablas.put("partes_imagenes", partes_imagenes);

            String config = "CREATE TABLE IF NOT EXISTS `config` ("
                    + "  `nombre` varchar(100) collate utf8_bin NOT NULL,"
                    + "  `valor` varchar(255) collate utf8_bin default NULL,"
                    + "  PRIMARY KEY  (`nombre`)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='Almacena variables de configuración del programa'";
            tablas.put("config", config);



            String expulsiones = "CREATE TABLE IF NOT EXISTS `expulsiones` ("
                    + "`id` int(10) unsigned NOT NULL auto_increment,"
                    + "`ano` int(10) unsigned NOT NULL,"
                    + "`alumno_id` int(10) unsigned NOT NULL,"
                    + "`fecha` date NOT NULL,"
                    + "`dias` int(10) unsigned NOT NULL,"
                    + "PRIMARY KEY  (`id`),"
                    + "CONSTRAINT `FK_expulsiones_anos` FOREIGN KEY (`ano`) REFERENCES `anos` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,"
                    + "CONSTRAINT `FK_expulsiones_alumnos` FOREIGN KEY (`alumno_id`) REFERENCES `alumnos` (`id`) ON DELETE CASCADE ON UPDATE CASCADE"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='Almacena los datos de expulsiones de alumnos'";
            tablas.put("expulsiones", expulsiones);

            String provincias = "CREATE TABLE IF NOT EXISTS `provincias` ("
                    + "  `id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,"
                    + "  `nombre` VARCHAR(255) NOT NULL,"
                    + "  PRIMARY KEY (`id`) "
                    + ") ENGINE = InnoDB;";
            tablas.put("provincias", provincias);

            String conv_tipos = " CREATE TABLE IF NOT EXISTS `conv_tipos` ("
                    + "  `id` int(10) unsigned NOT NULL auto_increment, "
                    + "  `cod` varchar(10) collate utf8_bin NOT NULL, "
                    + "  `ano` int(10) unsigned NOT NULL, "
                    + "  `tipo` int(10) unsigned NOT NULL default '0' COMMENT '0: Conductas 1:Medidas disciplinarias', "
                    + "  `gravedad` TINYINT UNSIGNED NOT NULL DEFAULT 0  COMMENT '0: Indeterminado 1:Leve 2:Grave', "
                    + "  `descripcion` varchar(255) collate utf8_bin NOT NULL, "
                    + "  PRIMARY KEY  (`id`), "
                    + "  KEY `FK_conv_tipos_anos` (`ano`), "
                    + "  CONSTRAINT `FK_conv_tipos_anos` FOREIGN KEY (`ano`) REFERENCES `anos` (`id`) ON DELETE CASCADE ON UPDATE CASCADE "
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='Tipos de conductas contrarias y de medidas disciplinarias'";
            tablas.put("conv_tipos", conv_tipos);

            String conv_conductas = "CREATE TABLE IF NOT EXISTS `conv_conductas` ( "
                    + " `id` int(10) unsigned NOT NULL auto_increment, "
                    + " `cod` varchar(10) collate utf8_bin NOT NULL, "
                    + " `ano` int(10) unsigned NOT NULL, "
                    + " `tipo_id` int(10) unsigned NOT NULL, "
                    + " `descripcion` varchar(255) collate utf8_bin NOT NULL, "
                    + " `acceso` varchar(255) collate utf8_bin default NULL, "
                    + "  PRIMARY KEY  (`id`), "
                    + "  KEY `FK_conv_conductas_anos` (`ano`), "
                    + "  KEY `FK_conv_conductas_tipos` (`tipo_id`), "
                    + "  CONSTRAINT `FK_conv_conductas_anos` FOREIGN KEY (`ano`) REFERENCES `anos` (`id`) ON DELETE CASCADE ON UPDATE CASCADE, "
                    + "  CONSTRAINT `FK_conv_conductas_tipos` FOREIGN KEY (`tipo_id`) REFERENCES `conv_tipos` (`id`) ON DELETE CASCADE ON UPDATE CASCADE "
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='Conductas y medidas disciplinarias'";
            tablas.put("conv_conductas", conv_conductas);

            String conv_tramos_horarios = "CREATE TABLE IF NOT EXISTS `conv_tramos_horarios` ( "
                    + "  `id` int(10) unsigned NOT NULL auto_increment, "
                    + "  `cod` varchar(10) collate utf8_bin NOT NULL, "
                    + "  `ano` int(10) unsigned NOT NULL, "
                    + "  `descripcion` varchar(255) collate utf8_bin NOT NULL, "
                    + "  PRIMARY KEY  (`id`), "
                    + "  KEY `FK_conv_tramos_horarios_anos` (`ano`), "
                    + "  CONSTRAINT `FK_conv_tramos_horarios_anos` FOREIGN KEY (`ano`) REFERENCES `anos` (`id`) ON DELETE CASCADE ON UPDATE CASCADE "
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='Tramos horarios de ocurrencia de incidentes'";
            tablas.put("conv_tramos_horarios", conv_tramos_horarios);

            String conv_partes = "CREATE TABLE IF NOT EXISTS `conv_partes` ( "
                    + "  `id` int(10) unsigned NOT NULL auto_increment, "
                    + "  `ano` int(10) unsigned NOT NULL, "
                    + "  `fecha` datetime NOT NULL, "
                    + "  `alumno_id` int(10) unsigned NOT NULL, "
                    + "  `profesor_id` int(10) unsigned NOT NULL, "
                    + "  `estado` int(10) unsigned NOT NULL default '0' COMMENT 'Estado de las medidas disciplinarias 0-Pendiente 1.Ignorado 2:Sancionado', "
                    + "  `descripcion` varchar(255) collate utf8_bin NOT NULL, "
                    + "  `observaciones` text collate utf8_bin NOT NULL, "
                    + "  `tramo_horario_id` int(10) unsigned default NULL, "
                    + "  `tipo` int(10) unsigned NOT NULL default '0' COMMENT '0: Leve, 1: Grave', "
                    + "  `informados` int(10) unsigned NOT NULL default '0' COMMENT 'Mascara de bits Alumno, Tutor, Padres', "
                    + "  `expulsion_id` int(10) unsigned zerofill default NULL, "
                    + "  `situacion` INTEGER UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Situación de control', "
                    + "  PRIMARY KEY  (`id`), "
                    + "  KEY `FK_conv_partes_anos` (`ano`), "
                    + "  KEY `FK_conv_partes_alumnos` (`alumno_id`), "
                    + "  KEY `FK_conv_partes_profesores` (`profesor_id`), "
                    + "  KEY `FK_conv_partes_tramos_horarios` (`tramo_horario_id`), "
                    + "  KEY `FK_conv_partes_expulsiones` (`expulsion_id`),  CONSTRAINT `FK_conv_partes_expulsiones` FOREIGN KEY (`expulsion_id`) REFERENCES `expulsiones` (`id`) ON DELETE SET NULL ON UPDATE CASCADE, "
                    + "  CONSTRAINT `FK_conv_partes_alumnos` FOREIGN KEY (`alumno_id`) REFERENCES `alumnos` (`id`) ON DELETE CASCADE ON UPDATE CASCADE, "
                    + "  CONSTRAINT `FK_conv_partes_anos` FOREIGN KEY (`ano`) REFERENCES `anos` (`id`) ON DELETE CASCADE ON UPDATE CASCADE, "
                    + "  CONSTRAINT `FK_conv_partes_profesores` FOREIGN KEY (`profesor_id`) REFERENCES `profesores` (`id`) ON DELETE CASCADE ON UPDATE CASCADE, "
                    + "  CONSTRAINT `FK_conv_partes_tramos_horarios` FOREIGN KEY (`tramo_horario_id`) REFERENCES `conv_tramos_horarios` (`id`) ON DELETE SET NULL ON UPDATE CASCADE "
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='Partes de convivencia'";

            tablas.put("conv_partes", conv_partes);

            String conv_lineas = "CREATE TABLE IF NOT EXISTS `conv_lineas` ( "
                    + "  `parte_id` int(10) unsigned NOT NULL, "
                    + "  `conducta_id` int(10) unsigned NOT NULL, "
                    + "  PRIMARY KEY  (`parte_id`,`conducta_id`), "
                    + "  CONSTRAINT `FK_conv_lineas_conductas` FOREIGN KEY (`conducta_id`) REFERENCES `conv_conductas` (`id`) ON DELETE CASCADE ON UPDATE CASCADE, "
                    + "  CONSTRAINT `FK_conv_lineas_partes` FOREIGN KEY (`parte_id`) REFERENCES `conv_partes` (`id`) ON DELETE CASCADE ON UPDATE CASCADE "
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='Lineas de los partes de conducta'";
            tablas.put("conv_lineas", conv_lineas);

            String cartas = "CREATE TABLE IF NOT EXISTS `cartas` ( "
                    + " `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, "
                    + " `alumno_id` int(10) UNSIGNED NOT NULL, "
                    + " `usuario_id` INTEGER UNSIGNED,"
                    + " `nombre` VARCHAR(255) NOT NULL DEFAULT '', "
                    + " `localizador` VARCHAR(255) NOT NULL DEFAULT '', "
                    + " `descripcion` TEXT, "
                    + " `archivo` LONGBLOB , "
                    + " `fecha` DATETIME  NOT NULL, "
                    + " `tipo` INTEGER UNSIGNED NOT NULL DEFAULT 0, "
                    + " `parametros` VARCHAR(255) NOT NULL, "
                    + " `modo` INTEGER UNSIGNED NOT NULL DEFAULT 0, "
                    + " PRIMARY KEY (`id`), "
                    + " CONSTRAINT `FK_cartero_alumno` FOREIGN KEY `FK_cartero_alumno` (`alumno_id`) REFERENCES `alumnos` (`id`) ON DELETE CASCADE ON UPDATE CASCADE, "
                    + " CONSTRAINT `FK_cartero_usuario` FOREIGN KEY `FK_cartero_usuario` (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE ON UPDATE CASCADE "
                    + ") ENGINE = InnoDB COMMENT = 'Almacena los diferentes envíos de cartas'";
            tablas.put("cartas", cartas);

            String alumnos_problemas_envio = "CREATE TABLE IF NOT EXISTS `alumnos_problemas_envio` ( "
                    + " `id` int(10)  UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID Del registro',"
                    + " `alumno_id` int(10) UNSIGNED  NOT NULL COMMENT 'ID del alumno',"
                    + " `tipo` TINYINT  NOT NULL DEFAULT 0 COMMENT 'Tipo de problema (0: Envío faltas)',"
                    + " `excluir` TINYINT  NOT NULL DEFAULT 0 COMMENT 'Excluir al alumno del envío de faltas',"
                    + " `info` TEXT COMMENT 'Información extra',"
                    + " PRIMARY KEY (`id`), "
                    + " CONSTRAINT `fk_problemas_envio_alumnos` FOREIGN KEY `fk_problemas_envio_alumnos` (`alumno_id`)"
                    + " REFERENCES `alumnos` (`id`)  ON DELETE CASCADE ON UPDATE CASCADE "
                    + " ) ENGINE = InnoDB COMMENT = 'Alumnos con problemas de envío';";
            tablas.put("alumnos_problemas_envio", alumnos_problemas_envio);

            String calendario_escolar = "CREATE TABLE IF NOT EXISTS `calendario_escolar` ( "
                    + " `id` int(10)  UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID Del registro',"
                    + " `dia` DATE NOT NULL, "
                    + " `descripcion` varchar(255)  NOT NULL, "
                    + " `ambito` varchar(255)  NOT NULL, "
                    + " `docentes` TINYINT  NOT NULL DEFAULT 1, "
                    + " `personal` TINYINT  NOT NULL DEFAULT 1, "
                    + " `ano` int(10) unsigned NOT NULL, "
                    + " PRIMARY KEY (`id`), "
                    + " CONSTRAINT `fk_calendario_ano` FOREIGN KEY `fk_calendario_ano` (`ano`) REFERENCES `anos` (`id`) ON DELETE CASCADE ON UPDATE CASCADE "
                    + " ) ENGINE = InnoDB COMMENT = 'Días festivos del año escolar'";
            tablas.put("calendario_escolar", calendario_escolar);


            String usuarios = "CREATE TABLE IF NOT EXISTS `usuarios` ( "
                    + " `id` int(10) unsigned NOT NULL auto_increment, "
                    + " `nombre` VARCHAR(255)  NOT NULL COMMENT 'Nombre del usuario', "
                    + " `clave` VARCHAR(60)  NOT NULL COMMENT 'Clave de acceso', "
                    + " `falta` DATE  NOT NULL COMMENT 'Fecha de alta del usuario', "
                    + " `fbaja` DATE  COMMENT 'Fecha de baja del usuario', "
                    + " `roles` int(10) NOT NULL DEFAULT 0 COMMENT 'Roles del usuario', "
                    + " PRIMARY KEY (`id`) "
                    + " )ENGINE = InnoDB COMMENT = 'Almacena los usuarios del sistema'";
            tablas.put("usuarios", usuarios);

            String usuarios_profesores = "CREATE TABLE  IF NOT EXISTS `usuarios_profesores` ( "
                    + " `usuario_id` INT(10) UNSIGNED NOT NULL, "
                    + " `ano` INT(10) UNSIGNED NOT NULL, "
                    + " `profesor_id` INT(10) UNSIGNED NOT NULL, "
                    + " PRIMARY KEY (`usuario_id`, `ano`), "
                    + " CONSTRAINT `fk_up_usuarios` FOREIGN KEY `fk_up_usuarios` (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE ON UPDATE CASCADE, "
                    + " CONSTRAINT `fk_up_profesor` FOREIGN KEY `fk_up_profesor` (`profesor_id`) REFERENCES `profesores` (`id`) ON DELETE CASCADE ON UPDATE CASCADE, "
                    + " CONSTRAINT `fk_up_ano` FOREIGN KEY `fk_up_ano` (`ano`) REFERENCES `anos` (`id`) ON DELETE CASCADE ON UPDATE CASCADE "
                    + " ) ENGINE = InnoDB COMMENT = 'Relaciona usuarios con profesores de cada año';";
            tablas.put("usuarios_profesores", usuarios_profesores);
        }
        return tablas;
    }

    private ArrayList<String> getProcedimientos() {
        ArrayList<String> procs = new ArrayList<String>();
        procs.add(" CREATE PROCEDURE `asignarPosicionLineasParte` (parte INT)"
                + " BEGIN "
                + " DECLARE done INT DEFAULT 0;"
                + " DECLARE idAl INT;"
                + " DECLARE pos INT DEFAULT 1;"
                + " DECLARE cur1 CURSOR FOR SELECT distinct a.id FROM partes_alumnos AS pa JOIN alumnos AS a ON a.id=pa.alumno_id JOIN unidades AS u ON a.unidad_id=u.id WHERE pa.parte_id=parte ORDER BY u.posicion," + Alumno.getCampoOrdenNombre("a") + ";"
                + " DECLARE CONTINUE HANDLER FOR SQLSTATE '02000' SET done = 1;"
                + " OPEN cur1; "
                + " REPEAT "
                + " FETCH cur1 INTO idAl; "
                + " IF NOT done THEN "
                + " UPDATE partes_alumnos SET posicion=pos WHERE parte_id=parte AND alumno_id=idAl; "
                + " SET pos=pos+1; "
                + " END IF; "
                + " UNTIL done END REPEAT; "
                + " CLOSE cur1; "
                + " END");
        procs.add("CREATE TRIGGER actualizar_estado_lineas_faltas BEFORE UPDATE ON partes_alumnos "
                + " FOR EACH ROW BEGIN "
                + " IF OLD.estado = 0 THEN "
                + " SET NEW.estado=1; "
                + " END IF; "
                + " END ");
        return procs;
    }

    private boolean crearEstructuraBaseDeDatos(Connection c) {
        boolean ok = false;
        String sql = "";
        try {
            firePropertyChange("setMensaje", null, "Revisando estructura de BD...");
            Statement st = (Statement) c.createStatement();
            LinkedHashMap<String, String> lhsTablas = getTablas();
            Iterator<String> it = lhsTablas.keySet().iterator();
            while (it.hasNext()) {
                String tabla = it.next();
                crearTabla(c, tabla);
            }
            LinkedHashMap<String, String> lhsVistas = getVistas();
            Iterator<String> itVistas = lhsVistas.keySet().iterator();
            while (itVistas.hasNext()) {
                String k = itVistas.next();
                String sqlV = lhsVistas.get(k);
                try {
                    st.executeUpdate(sqlV);
                } catch (SQLException ex) {
                    //Si es el error de que ya existe lo ignoramos
                    if (ex.getErrorCode() != 1304) {
                        Logger.getLogger(Conector.class.getName()).log(Level.SEVERE, "Error creando vista de base de datos (" + ex.getErrorCode() + ":" + ex.getSQLState() + "):\n" + sqlV, ex);
                    }
                }
            }

            for (String proc : getProcedimientos()) {
                String sqlProc = proc;
                try {
                    st.executeUpdate(sqlProc);
                } catch (SQLException ex) {
                    //Si es el error de que ya existe lo ignoramos
                    if (ex.getErrorCode() != 1304) {
                        Logger.getLogger(Conector.class.getName()).log(Level.SEVERE, "Error creando procedimiento de base de datos (" + ex.getErrorCode() + ":" + ex.getSQLState() + "):\n" + sqlProc, ex);
                    }
                }
            }

            Obj.cerrar(st);
            ok = true;
            //Una vez creada se le asigna la versión correspondiente
            MaimonidesApp.getApplication().getConfiguracion().set(c, "version_actu", Mantenimiento.getVersionActualizacion() + "");
        } catch (SQLException ex) {
            Logger.getLogger(Conector.class.getName()).log(Level.SEVERE, "Error creando estrucutura de base de datos (" + ex.getErrorCode() + ":" + ex.getSQLState() + "):\n" + sql, ex);
        }
        return ok;
    }

    public static boolean crearTabla(String tabla) {
        return crearTabla(MaimonidesApp.getConexion(), tabla);
    }

    public static boolean crearTabla(Connection c, String tabla) {
        boolean ret = false;
        Logger.getLogger(Conector.class.getName()).log(Level.INFO, "Verificando tabla: {0}...", tabla);
        if (!Mantenimiento.existeTabla(c, tabla)) {
            if (Conector.getTablas().containsKey(tabla)) {
                Statement st = null;
                try {
                    st = (Statement) c.createStatement();
                    String sqlTabla = Conector.getTablas().get(tabla);
                    st.executeUpdate(sqlTabla);
                    Logger.getLogger(Conector.class.getName()).log(Level.INFO, "La tabla: {0} se ha creado correctamente.", tabla);
                    ret = crearContenidoTabla(c, tabla);
                } catch (SQLException ex) {
                    Logger.getLogger(Mantenimiento.class.getName()).log(Level.SEVERE, "Error creando estructura de tabla:" + tabla, ex);
                }
                Obj.cerrar(st);
            }
        } else {
            Logger.getLogger(Conector.class.getName()).log(Level.INFO, "La tabla: {0} ya existe.", tabla);
            ret = true;
        }
        return ret;
    }

    private static boolean crearContenidoTabla(Connection c, String tabla) {
        boolean ret = true;
        //Ahora vemos si existe el fichero de datos
        URL url = Conector.class.getResource("resources/sql/" + tabla + ".sql");
        Statement st = null;
        ResultSet rs = null;
        BufferedReader br = null;
        try {
            if (url != null) {
                st = (Statement) c.createStatement();
                //vemos si hay contenido
                String sql = "SELECT count(*) FROM " + tabla;
                rs = st.executeQuery(sql);
                if (rs != null && rs.next() && rs.getInt(1) == 0) {
                    Object cont = url.getContent();
                    br = new BufferedReader(new InputStreamReader((InputStream) cont, "UTF-8"));
                    String l = br.readLine();
                    while (l != null) {
                        l = l.trim();
                        if (!l.equals("")) {
                            Logger.getLogger(Conector.class.getName()).log(Level.INFO, "Ejecutando:{0}", l);
                            st.executeUpdate(l);
                        }
                        l = br.readLine();
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(Mantenimiento.class.getName()).log(Level.SEVERE, "Error creando contenido de tabla:" + tabla, ex);
            ret = false;
        }
        Obj.cerrar(rs, st, br);
        return ret;
    }
}
