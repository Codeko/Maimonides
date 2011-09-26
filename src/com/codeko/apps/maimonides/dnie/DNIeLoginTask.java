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
package com.codeko.apps.maimonides.dnie;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesUtil;
import com.codeko.apps.maimonides.usr.Rol;
import com.codeko.apps.maimonides.usr.Usuario;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;

/**
 *
 * @author Codeko <codeko@codeko.com>
 */
public class DNIeLoginTask extends org.jdesktop.application.Task<Boolean, Void> {

    DNIe dni = null;
    Usuario u = null;

    public DNIeLoginTask(org.jdesktop.application.Application app, Usuario u, DNIe dni) {
        super(app);
        this.u = u;
        this.dni = dni;
    }

    @Action(block = Task.BlockingScope.NONE)
    public Task<Boolean, Void> userLogin() {
        return this;
    }

    public static DNIeLoginTask doLogin(Usuario u, DNIe dni) {
        DNIeLoginTask lt = new DNIeLoginTask(MaimonidesApp.getApplication(), u, dni);
        MaimonidesUtil.ejecutarTask(lt, "userLogin");
        return lt;
    }

    @Override
    @SuppressWarnings("fallthrough")
    protected Boolean doInBackground() throws Exception {
        boolean ret = false;
        boolean go = true;
        dni.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                firePropertyChange(pce.getPropertyName(), pce.getOldValue(), pce.getNewValue());
            }
        });
        switch (Configuracion.getDNIePassPolicy()) {
            case Configuracion.DNIE_PASS_POLICY_PASS_FOR_EXTERNAL:
                //Si no tiene roles externos 
                if ((u.getRoles() & Rol.ROLES_EXTERNOS) == 0) {
                    break;
                }
            case Configuracion.DNIE_PASS_POLICY_PASS_FOR_ALL:
                go = validarPinDNIe(dni);
                break;
//                                    case Configuracion.DNIE_PASS_POLICY_NO_PASS:
//                                        break;
        }
        if (go) {
            MaimonidesApp.getApplication().setUsuario(u);
        }
        return ret;
    }

    @Override
    protected void succeeded(Boolean result) {
    }

    private boolean validarPinDNIe(DNIe dni) {
        boolean ok = false;
        JPasswordField passwordField = new JPasswordField();
        Object[] obj = {"<html>Introduzca el PIN del DNIe:\n", passwordField};
        Object stringArray[] = {"Aceptar", "Cancelar"};
        int op = JOptionPane.showOptionDialog(null, obj, "Acceso con DNIe", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, stringArray, obj);
        while (op == JOptionPane.OK_OPTION && !ok) {
            String password = new String(passwordField.getPassword());
            String msg = "Pin incorrecto";
            try {
                ok = dni.autentificarValidar(password);
            } catch (Exception e) {
                msg = e.getMessage();
            }
            if (!ok) {
                Object[] obj2 = {"<html><b><font color='red'>" + msg + "</font></b>\n", "Introduzca el PIN del DNIe:\n", passwordField};
                //JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), msg, "Error", JOptionPane.ERROR_MESSAGE);
                op = JOptionPane.showOptionDialog(null, obj2, "Acceso con DNIe", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, stringArray, obj);
            }
        }
        return ok;
    }
}
