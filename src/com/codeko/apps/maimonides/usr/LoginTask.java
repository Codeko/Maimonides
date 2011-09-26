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
import com.codeko.apps.maimonides.MaimonidesUtil;
import javax.swing.JOptionPane;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;

/**
 *
 * @author Codeko <codeko@codeko.com>
 */
public class LoginTask extends org.jdesktop.application.Task<Boolean, Void> {

    GestorUsuarioClave gestor = GestorUsuarioClave.getGestor();

    public LoginTask(org.jdesktop.application.Application app) {
        super(app);
    }

    @Action(block = Task.BlockingScope.NONE)
    public Task<Boolean, Void> userLogin() {
        return this;
    }

    public static LoginTask doLogin() {
        LoginTask lt = new LoginTask(MaimonidesApp.getApplication());
        MaimonidesUtil.ejecutarTask(lt, "userLogin");
        return lt;
    }

    @Override
    protected Boolean doInBackground() throws Exception {
        boolean ret = false;
        PanelAcceso panel = new PanelAcceso();
        panel.validate();
        panel.setDatos(gestor.getUsuario(), gestor.isRecordar() ? gestor.getClave() : "", gestor.isRecordar());
        int op = JOptionPane.showConfirmDialog(MaimonidesApp.getApplication().getMainFrame(), panel, "Acceso a Maimónides", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (MaimonidesApp.getApplication().getUsuario() == null) {
            while (!ret && op == JOptionPane.OK_OPTION) {
                gestor.setRecordar(panel.isRecordar());
                gestor.setUsuario(panel.getUsuario());
                gestor.setClave(panel.getClave());
                ret = Usuario.login(panel.getUsuario(), panel.getClave());
                if (!ret) {
                    panel.setError(true);
                    op = JOptionPane.showConfirmDialog(MaimonidesApp.getApplication().getMainFrame(), panel, "Acceso a Maimónides", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                }
            }
        } else {
            ret = true;
        }
        return ret;
    }

    @Override
    protected void succeeded(Boolean result) {
        if (result == null || !result) {
            MaimonidesApp.getApplication().quit(null);
        }
    }
}