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


package com.codeko.apps.maimonides.mantenimiento;

import com.codeko.apps.maimonides.Conector;
import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesBean;
import com.codeko.util.Fechas;
import com.codeko.util.Num;
import com.codeko.util.Obj;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.Statement;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileFilter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class Mantenimiento extends MaimonidesBean {
    //TODO Estaría bien que todas estas variables fuese configurables.

    // public static final int DIAS_MANTENIMIENTO_IMAGENES_PARTES_ASISTENCIA = 30;
    public static final int DIAS_MANTENIMIENTO_COPIAS_SEGURIDAD_RESTAURACION = 7;
    // public static final int DIAS_MANTENIMIENTO_PARTES_ESCANEADOS = 7;
    ArrayList<String> mensajes = new ArrayList<String>();

    public void mantenimiento() {
        getMensajes().clear();
        //Primero ejecutamos las actualizaciones de base de datos
        firePropertyChange("message", null, "Revisando actualizaciones...");
        actualizar();
        //Luego el mantenimeinto de base de datos
        firePropertyChange("message", null, "Realizando mantenimiento de la base de datos...");
        mantenimientoBD();
        //Luego el mantenimiento de ficheros.
//        firePropertyChange("message", null, "Realizando mantenimiento de ficheros...");
//        mantenimientoCopiasDeSeguridadRestauracion();
        //Luego el mantenimeinto de partes escaneados
        //TODO Configurar si se debe aplicar esto y cada cuanto tiempo
//        mantenimientoPartesEscaneados();
        //TODO Restaurar esto
        //optimizar();
    }

    public ArrayList<String> getMensajes() {
        return mensajes;
    }

    private int actualizar() {
        int actus = 0;
        //Recuperamos la versión de la base de datos
        int v = Num.getInt(MaimonidesApp.getApplication().getConfiguracion().get("version_actu", "0"));
        int i = v;
        boolean continuar = true;
        while (i < getVersionActualizacion() && continuar) {
            actus++;
            continuar = false;
            try {
                Object obj = Class.forName("com.codeko.apps.maimonides.mantenimiento.actualizaciones.Actualizacion" + (i + 1)).newInstance();
                if (obj instanceof Actualizacion) {
                    Actualizacion ia = (Actualizacion) obj;
                    Logger.getLogger(Mantenimiento.class.getName()).log(Level.INFO, "Aplicando actualizaci\u00f3n {0}: {1}", new Object[]{i + 1, ia.getDescripcion()});
                    firePropertyChange("message", null, ia.getDescripcion());
                    ia.addPropertyChangeListener(new PropertyChangeListener() {

                        @Override
                        public void propertyChange(PropertyChangeEvent evt) {
                            firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                        }
                    });
                    if (ia.necesitaConfirmacion()) {
                        int op = JOptionPane.showConfirmDialog(null, "La siguiente actualización necesita confirmación para continuar:\n\n" + ia.getDescripcion() + "\n\n¿Ejecutar actualización?", "Ejecutar actualización", JOptionPane.YES_NO_OPTION);
                        if (op == JOptionPane.YES_OPTION) {
                            continuar = ia.ejecutar();
                        }
                    } else {
                        continuar = ia.ejecutar();
                    }
                    if (continuar) {
                        if (ia.getNotificacion() != null) {
                            getMensajes().add(ia.getNotificacion());
                        }
                    }
                }
                if (continuar) {
                    i++;
                }
            } catch (Exception ex) {
                Logger.getLogger(Mantenimiento.class.getName()).log(Level.SEVERE, null, ex);
                break;
            }
        }
        MaimonidesApp.getApplication().getConfiguracion().set("version_actu", i + "");
        //Y ya podemos asignar la versión de la aplicación
        if (isAppPosteriorABD()) {
            setBDVersion();
        }

        return actus;
    }

    public static int getVersionActualizacion() {
        return 19;
    }

    public static String getAplicationVersion() {
        String versionApp = "1.0";
        versionApp = MaimonidesApp.getApplication().getContext().getResourceMap().getString("Application.version");
        return versionApp;
    }

    public static String getBDVersion() {
        //Por defecto devolvemos la primera versión en la que se inicia el control de versión de la aplicación
        return MaimonidesApp.getApplication().getConfiguracion().get("app_version", "1.29");
    }

    private void setBDVersion() {
        MaimonidesApp.getApplication().getConfiguracion().set("app_version", getAplicationVersion());
    }

    private void mantenimientoBD() {
        Statement st = null;
        try {
            st = (Statement) MaimonidesApp.getApplication().getConector().getConexion().createStatement();
            //TODO Poner configurable esta operación tanto si se debe ejecutar como el número de dias
            //ejecutarUpdateSeguro(st, "DELETE partes_imagenes FROM partes_imagenes JOIN partes AS p ON p.id=partes_imagenes.parte_id WHERE DATEDIFF(NOW(),p.fecha)>" + DIAS_MANTENIMIENTO_IMAGENES_PARTES_ASISTENCIA);
            ejecutarUpdateSeguro(st, "DELETE advertencias_imagenes FROM advertencias_imagenes LEFT JOIN partes_advertencias AS pa ON pa.imagen_id=advertencias_imagenes.id WHERE pa.id IS NULL");
        } catch (SQLException ex) {
            Logger.getLogger(Mantenimiento.class.getName()).log(Level.SEVERE, "Error realizando labores de mantenimiento.", ex);
        }
        Obj.cerrar(st);
    }

    public static boolean ejecutarUpdateSeguro(Statement st, String sql) {
        boolean ret = true;
        try {
            st.executeUpdate(sql);
        } catch (SQLException ex) {
            Logger.getLogger(Mantenimiento.class.getName()).log(Level.SEVERE, "Error ejecutando:'" + sql + "'", ex);
            ret = false;
        }
        return ret;
    }

    private void mantenimientoCopiasDeSeguridadRestauracion() {
        GregorianCalendar cal = new GregorianCalendar();
        File f = MaimonidesApp.getApplication().getConfiguracion().getCarpetaSeguridadRestauracion();
        File[] copias = f.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                return pathname.isFile() && pathname.getName().toLowerCase().endsWith(".zip.sql");
            }
        });
        if (copias != null) {
            for (File c : copias) {
                try {
                    GregorianCalendar modif = new GregorianCalendar();
                    modif.setTimeInMillis(c.lastModified());
                    if (Fechas.getDiferenciaTiempoEn(cal, modif, GregorianCalendar.DATE) > DIAS_MANTENIMIENTO_COPIAS_SEGURIDAD_RESTAURACION) {
                        c.delete();
                    }
                } catch (Exception e) {
                    Logger.getLogger(Mantenimiento.class.getName()).log(Level.WARNING, "No se ha podido eliminar o calcular si se debe eliminar el archivo '" + c + "'.", e);
                }
            }
        }
    }

    public static boolean existeTabla(String tabla) {
        return existeTabla(MaimonidesApp.getConexion(), tabla);
    }

    public static boolean existeTabla(Connection c, String tabla) {
        boolean ret = false;
        PreparedStatement st = null;
        ResultSet res = null;
        try {
            st = (PreparedStatement) c.prepareStatement("SELECT * FROM " + tabla + " LIMIT 0,1");
            res = st.executeQuery();
            ret = true;
        } catch (SQLException ex) {
        }
        Obj.cerrar(st, res);
        return ret;
    }

    private void optimizar() {
        GregorianCalendar cal = MaimonidesApp.getApplication().getConfiguracion().getUltimaOptimizacion();
        GregorianCalendar now = new GregorianCalendar();
        //TODO Configurar esto
        boolean optimizar = Fechas.getDiferenciaTiempoEn(now, cal, GregorianCalendar.DAY_OF_MONTH) > 6;
        if (optimizar) {
            try {
                MaimonidesApp.getApplication().getConfiguracion().setUltimaOptimizacion(now);
                LinkedHashMap<String, String> lhsTablas = Conector.getTablas();
                Iterator<String> it = lhsTablas.keySet().iterator();
                while (it.hasNext()) {
                    final String k = it.next();
                    Logger.getLogger(Conector.class.getName()).log(Level.INFO, "Optimizando tabla: {0}", k);
                    firePropertyChange("message", null, "Optimizando tabla " + k + "...");
                    Thread t = new Thread() {

                        @Override
                        public void run() {
                            Statement st = null;
                            try {
                                st = (Statement) MaimonidesApp.getApplication().getConector().getConexion().createStatement();
                                st.executeUpdate("OPTIMIZE TABLE " + k);
                            } catch (SQLException ex) {
                                Logger.getLogger(Mantenimiento.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            Obj.cerrar(st);
                        }
                    };
                    t.start();
                    t.join();
                }

            } catch (Exception ex) {
                Logger.getLogger(Mantenimiento.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    public static boolean isAppPosteriorABD() {
        boolean ret = false;
        double appVer = Num.getDouble(Mantenimiento.getAplicationVersion());
        double bdVer = Num.getDouble(Mantenimiento.getBDVersion());
        ret = appVer >= bdVer;
        return ret;
    }
//    private void mantenimientoPartesEscaneados() {
//        GregorianCalendar cal = new GregorianCalendar();
//        File f = MaimonidesApp.getApplication().getConfiguracion().getCarpetaPartesDigitalizados();
//        File[] copias = f.listFiles(new FileFilter() {
//
//            @Override
//            public boolean accept(File pathname) {
//                return pathname.isFile() && pathname.getName().toLowerCase().endsWith("." + ConfiguracionParte.getConfiguracion().getExtensionImagenes());
//            }
//        });
//        if (copias != null) {
//            for (File c : copias) {
//                try {
//                    GregorianCalendar modif = new GregorianCalendar();
//                    modif.setTimeInMillis(c.lastModified());
//                    if (Fechas.getDiferenciaTiempoEn(cal, modif, GregorianCalendar.DATE) > DIAS_MANTENIMIENTO_PARTES_ESCANEADOS) {
//                        c.delete();
//                    }
//                } catch (Exception e) {
//                    Logger.getLogger(Mantenimiento.class.getName()).log(Level.WARNING, "No se ha podido eliminar o calcular si se debe eliminar el archivo '" + c + "'.", e);
//                }
//            }
//        }
//    }
}
