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
package com.codeko.apps.maimonides.conf;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.util.Num;
import com.codeko.util.Obj;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import java.beans.Beans;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.swingx.util.OS;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class Configuracion {

    public static final String CARPETA_DOCUMENTOS = "documentos";
    public static final String CARPETA_INFORMES = "informes";
    public static final String CARPETA_SENECA = "seneca";
    //Contantes de acceso a los datos
    public static final String KEY_CRIPTO = System.getProperty("maimonides.crypt_key", "<@·Asdk907&8OPNiojasd'$%9kY_-");
    public static final String CENTRO_WEB = "web_centro";
    public static final String CENTRO_WEB_COMPATIBLE = "web_compatible";
    public static final String WEB_USUARIO = "web_usuario";
    public static final String WEB_CLAVE = "web_clave";
    //Contanstes de ficheros
    public static final String CONN_CFG_FILE = "cfg.txt";
    File archivoAccess = null;
    File carpetaPartes = null;
    File carpetaPartesDigitalizados = null;
    File carpetaPartesFallidos = null;
    File carpetaCopias = null;
    File carpetaSeguridadRestauracion = null;
    GregorianCalendar ultimoBackup = null;
    GregorianCalendar ultimaOptimizacion = null;

    public static int getHorasPorDia() {
        return 6;
    }

    public GregorianCalendar getUltimoBackup() {
        if (ultimoBackup == null) {
            long l = Num.getLong(get("ultimo_backup", "0"));
            ultimoBackup = new GregorianCalendar();
            ultimoBackup.setTimeInMillis(l);
        }
        return ultimoBackup;
    }

    public void setUltimoBackup(GregorianCalendar ultimoBackup) {
        this.ultimoBackup = ultimoBackup;
        set("ultimo_backup", this.ultimoBackup != null ? ultimoBackup.getTimeInMillis() + "" : null);
    }

    public GregorianCalendar getUltimaOptimizacion() {
        if (ultimaOptimizacion == null) {
            long l = Num.getLong(get("ultima_optimizacion", "0"));
            ultimaOptimizacion = new GregorianCalendar();
            ultimaOptimizacion.setTimeInMillis(l);
        }
        return ultimaOptimizacion;
    }

    public void setUltimaOptimizacion(GregorianCalendar ultimaOptimizacion) {
        this.ultimaOptimizacion = ultimaOptimizacion;
        set("ultima_optimizacion", this.ultimaOptimizacion != null ? ultimaOptimizacion.getTimeInMillis() + "" : null);
    }

    public File getCarpetaCopias() {
        if (carpetaCopias == null) {
            try {
                carpetaCopias = new File(get("ruta_carpeta_copias", "backup"));
            } catch (Exception e) {
            }
        }
        return carpetaCopias;
    }

    public void setCarpetaCopias(File carpetaCopias) {
        this.carpetaCopias = carpetaCopias;
        set("ruta_carpeta_copias", this.carpetaCopias != null ? this.carpetaCopias.getAbsolutePath() : null);
    }

    public File getCarpetaSeguridadRestauracion() {
        if (carpetaSeguridadRestauracion == null) {
            try {
                carpetaSeguridadRestauracion = new File(getCarpetaCopias(), get("ruta_carpeta_seg_rest", "seg"));
            } catch (Exception e) {
            }
        }
        return carpetaSeguridadRestauracion;
    }

    public void setCarpetaSeguridadRestauracion(File carpetaSeguridadRestauracion) {
        this.carpetaSeguridadRestauracion = carpetaSeguridadRestauracion;
        set("ruta_carpeta_seg_rest", this.carpetaSeguridadRestauracion != null ? this.carpetaSeguridadRestauracion.getAbsolutePath() : null);
    }

    public File getCarpetaPartes() {
        if (carpetaPartes == null) {
            try {
                String nombreCarpetaPartes = get("ruta_carpeta_partes", getSubCarpertaUsuarioMaimonides("partes").getAbsolutePath());
                carpetaPartes = new File(nombreCarpetaPartes);
                carpetaPartes.mkdirs();
            } catch (Exception e) {
            }
        }
        return carpetaPartes;
    }

    public void setCarpetaPartes(File carpetaPartes) {
        this.carpetaPartes = carpetaPartes;
        set("ruta_carpeta_partes", this.carpetaPartes != null ? this.carpetaPartes.getAbsolutePath() : null);
    }

    public File getCarpetaPartesDigitalizados() {
        if (carpetaPartesDigitalizados == null) {
            if (getCarpetaPartes() != null) {
                carpetaPartesDigitalizados = new File(getCarpetaPartes(), "procesados");
                carpetaPartesDigitalizados.mkdirs();
            }
        }
        return carpetaPartesDigitalizados;
    }

    public void setCarpetaPartesDigitalizados(File carpetaPartesDigitalizados) {
        this.carpetaPartesDigitalizados = carpetaPartesDigitalizados;
    }

    public File getCarpetaPartesFallidos() {
        if (carpetaPartesFallidos == null) {
            if (getCarpetaPartes() != null) {
                carpetaPartesFallidos = new File(getCarpetaPartes(), "fallidos");
                carpetaPartesFallidos.mkdirs();
            }
        }
        return carpetaPartesFallidos;
    }

    public void setCarpetaPartesFallidos(File carpetaPartesFallidos) {
        this.carpetaPartesFallidos = carpetaPartesFallidos;
    }

    public boolean isImprimirEnPDF() {
        return get("imprimir_en_PDF", "0").equals("1");
    }

    public boolean isComprimirPartes() {
        return get("partes_asistencia.comprimir", "0").equals("1");
    }

    public void set(String nombre, String valor) {
        set(MaimonidesApp.getApplication().getConector().getConexion(), nombre, valor);
    }

    public void set(Connection c, String nombre, String valor) {
        try {
            if (valor != null) {
                PreparedStatement st = (PreparedStatement) c.prepareStatement("REPLACE config SET nombre=?,valor=?");
                st.setString(1, nombre);
                st.setString(2, valor);
                st.executeUpdate();
            } else {
                PreparedStatement st = (PreparedStatement) c.prepareStatement("DELETE FROM config WHERE nombre=?");
                st.setString(1, nombre);
                st.executeUpdate();
            }
        } catch (SQLException ex) {
            Logger.getLogger(Configuracion.class.getName()).log(Level.SEVERE, "Error guardando configuración: [" + nombre + ":" + valor + "]", ex);
        }
    }

    public String get(String nombre, String porDefecto) {
        try {
            if (!Beans.isDesignTime()) {
                PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT valor FROM config WHERE nombre=?");
                st.setString(1, nombre);
                ResultSet res = st.executeQuery();
                if (res.next()) {
                    return res.getString(1);
                }
                Obj.cerrar(st, res);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Configuracion.class.getName()).log(Level.SEVERE, "Error recuperando configuración: [" + nombre + ":" + porDefecto + "]", ex);
        }
        return porDefecto;
    }

    public static HashMap<String, Object> getDatosBaseImpresion() {
        HashMap<String, Object> data = new HashMap<String, Object>();
        Configuracion c = MaimonidesApp.getApplication().getConfiguracion();
        data.put("codigoCentro", c.get("codigo_centro", ""));
        String nombre = c.get("nombre_centro", "");
        data.put("nombreCentro", nombre);
        data.put("direccionCentro", c.get("direccion_centro", ""));
        data.put("poblacionCentro", c.get("poblacion_centro", ""));
        data.put("cpCentro", c.get("cp_centro", ""));
        data.put("provinciaCentro", c.get("provincia_centro", ""));
        data.put("telefonoCentro", c.get("telefono_centro", ""));
        data.put("faxCentro", c.get("fax_centro", ""));
        data.put("emailCentro", c.get("email_centro", ""));
        return data;
    }

    public static File getCarpetaUsuarioMaimonides() {
        File base = javax.swing.filechooser.FileSystemView.getFileSystemView().getDefaultDirectory();
        String name = "Maimonides";
        if (!OS.isWindows()) {
            name = ".maimonides";
        }
        File maimonidesFolder = new File(base, name);
        maimonidesFolder.mkdirs();
        return maimonidesFolder;
    }

    public static File getSubCarpertaUsuarioMaimonides(String nombre) {
        File f = new File(getCarpetaUsuarioMaimonides(), nombre);
        f.mkdirs();
        return f;
    }
}
