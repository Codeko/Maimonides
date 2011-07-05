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


package com.codeko.apps.maimonides.seneca;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.usr.Usuario;
import com.codeko.util.Cripto;
import java.util.prefs.Preferences;
import javax.swing.JOptionPane;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class GestorUsuarioClaveSeneca {

    String usuario = "";
    String clave = "";
    boolean recordar = false;
    static GestorUsuarioClaveSeneca gestor = null;

    public static GestorUsuarioClaveSeneca getGestor() {
        if (gestor == null) {
            gestor = new GestorUsuarioClaveSeneca();
        }
        return gestor;
    }

    private GestorUsuarioClaveSeneca() {
        //Primero vemos si hay que recordar los datos
        setRecordar(Preferences.userNodeForPackage(this.getClass()).getBoolean(Usuario.getIUA()+"recordar", isRecordar()));
        setUsuario(Preferences.userNodeForPackage(this.getClass()).get(Usuario.getIUA()+"usuario", ""));
        if (isRecordar()) {
            setClave(Cripto.desencriptar(Preferences.userNodeForPackage(this.getClass()).get(Usuario.getIUA()+"clave", "")));
        }
    }

    public boolean pedirUsuarioClave() {
        boolean ret = false;
        PanelAccesoSeneca panel = new PanelAccesoSeneca();
        panel.setDatos(getUsuario(), isRecordar()?getClave():"", isRecordar());
        int op = JOptionPane.showConfirmDialog(MaimonidesApp.getApplication().getMainFrame(), panel, "Acceso a Séneca", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (op == JOptionPane.OK_OPTION) {
            setRecordar(panel.isRecordar());
            setUsuario(panel.getUsuario());
            setClave(panel.getClave());
            ret = true;
        }
        return ret && isAccesoPreparado();
    }

    public boolean isAccesoPreparado() {
        return getUsuario().length() > 0 && getClave().length() > 0;
    }

    public boolean isRecordar() {
        return recordar;
    }

    public void setRecordar(boolean recordar) {
        Preferences.userNodeForPackage(this.getClass()).put(Usuario.getIUA()+"recordar", recordar + "");
        this.recordar = recordar;
        if (!recordar) {
            Preferences.userNodeForPackage(this.getClass()).remove(Usuario.getIUA()+"clave");
        }
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        if (isRecordar()) {
            Preferences.userNodeForPackage(this.getClass()).put(Usuario.getIUA()+"clave", Cripto.encriptar(clave));
        } else {
            Preferences.userNodeForPackage(this.getClass()).remove(Usuario.getIUA()+"clave");
        }
        this.clave = clave;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        Preferences.userNodeForPackage(this.getClass()).put(Usuario.getIUA()+"usuario", usuario);
        this.usuario = usuario;
    }
}
