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


package com.codeko.apps.maimonides.usr;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.util.Cripto;
import java.util.prefs.Preferences;
import javax.swing.JOptionPane;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class GestorUsuarioClave {

    String usuario = "";
    String clave = "";
    boolean recordar = false;
    static GestorUsuarioClave gestor = null;

    public static GestorUsuarioClave getGestor() {
        if (gestor == null) {
            gestor = new GestorUsuarioClave();
        }
        return gestor;
    }

    private GestorUsuarioClave() {
        //Primero vemos si hay que recordar los datos
        setRecordar(Preferences.userNodeForPackage(this.getClass()).getBoolean("recordar", isRecordar()));
        setUsuario(Preferences.userNodeForPackage(this.getClass()).get("usuario", ""));
        if (isRecordar()) {
            setClave(Cripto.desencriptar(Preferences.userNodeForPackage(this.getClass()).get("clave", "")));
        }
    }

    public boolean pedirUsuarioClave() {
        boolean ret = false;
        PanelAcceso panel = new PanelAcceso();
        panel.validate();
        panel.setDatos(getUsuario(), isRecordar() ? getClave() : "", isRecordar());
        int op = JOptionPane.showConfirmDialog(MaimonidesApp.getApplication().getMainFrame(), panel, "Acceso a Maimónides", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        while (!ret && op == JOptionPane.OK_OPTION) {
            setRecordar(panel.isRecordar());
            setUsuario(panel.getUsuario());
            setClave(panel.getClave());
            ret = Usuario.login(panel.getUsuario(), panel.getClave());
            if (!ret) {
                panel.setError(true);
                op = JOptionPane.showConfirmDialog(MaimonidesApp.getApplication().getMainFrame(), panel, "Acceso a Maimónides", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            }
        }
        return ret;
    }

    public boolean isRecordar() {
        return recordar;
    }

    public void setRecordar(boolean recordar) {
        Preferences.userNodeForPackage(this.getClass()).put("recordar", recordar + "");
        this.recordar = recordar;
        if (!recordar) {
            Preferences.userNodeForPackage(this.getClass()).remove("clave");
        }
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        if (isRecordar()) {
            Preferences.userNodeForPackage(this.getClass()).put("clave", Cripto.encriptar(clave));
        } else {
            Preferences.userNodeForPackage(this.getClass()).remove("clave");
        }
        this.clave = clave;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        Preferences.userNodeForPackage(this.getClass()).put("usuario", usuario);
        this.usuario = usuario;
    }
}
