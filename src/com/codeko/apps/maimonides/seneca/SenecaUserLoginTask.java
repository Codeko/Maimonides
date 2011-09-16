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
import com.codeko.apps.maimonides.MaimonidesUtil;
import com.codeko.apps.maimonides.usr.Usuario;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;

/**
 *
 * @author Codeko <codeko@codeko.com>
 */
public class SenecaUserLoginTask extends org.jdesktop.application.Task<Usuario, Void> {

    String usr = null;
    String pass = null;
    public boolean ret=false;

    public SenecaUserLoginTask(org.jdesktop.application.Application app, String usr, String pass) {
        super(app);
        this.usr = usr;
        this.pass = pass;
    }
    
    @Action(block = Task.BlockingScope.APPLICATION)
    public Task<Usuario, Void> senecaUserLogin() {
        return this;
    }
    
    public SenecaUserLoginTask doLogin(){
        MaimonidesUtil.ejecutarTask(this, "senecaUserLogin");
        return this;
    }

    @Override
    protected Usuario doInBackground() throws Exception {
        ClienteSeneca c = new ClienteSeneca(usr, pass);
        setMessage("Iniciando login en Maimónides con Séneca");
        c.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                System.out.println(evt.getPropertyName()+":"+evt.getNewValue());
                firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            }
        });
        Usuario u= c.senecaUserLogin();
        setMessage("Espere por favor...");
        ret=u!=null;
        return u;
    }

    @Override
    protected void succeeded(Usuario result) {
        if (result != null) {
            MaimonidesApp.getApplication().setUsuario(result);
        }
    }
}
