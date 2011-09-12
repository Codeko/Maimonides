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
import com.codeko.apps.maimonides.MaimonidesBean;
import com.codeko.apps.maimonides.MaimonidesView;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.AnoEscolar;
import com.codeko.apps.maimonides.usr.Rol;
import com.codeko.apps.maimonides.usr.Usuario;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 *
 * @author Codeko <codeko@codeko.com>
 */
public class DNIeLoginManager extends MaimonidesBean {

    private static DNIeLoginManager loginManager = null;

    private DNIeLoginManager() {
    }

    public static DNIeLoginManager getLoginManager() {
        if (loginManager == null) {
            loginManager = new DNIeLoginManager();
        }
        return loginManager;
    }

    public static void init() {
        if (Configuracion.isDNIeAccessEnabled()) {
            DNIeObserver.addPropertyListener(MaimonidesApp.getMaimonidesView().getControlMensajes());
            DNIeObserver.addPropertyListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent pce) {
                    if ("message".equals(pce.getPropertyName())) {
                        getLoginManager().firePropertyChange(pce.getPropertyName(), pce.getOldValue(), pce.getNewValue());
                        System.out.println(pce.getPropertyName() + ": " + pce.getNewValue());
                    } else if ("dnieDisconnected".equals(pce.getPropertyName())) {
                        //Si hay usuario y este estaba conectado por DNIe lo desconectamos
                        //TODO En el caso de varios lectores de DNIe esto puede dar problemas
                        Usuario usr = MaimonidesApp.getApplication().getUsuario();
                        if (usr != null && usr.isDNIe()) {
                            MaimonidesApp.getApplication().setUsuario(null);
                        }
                    } else if ("dnieConnected".equals(pce.getPropertyName())) {
                        if (!MaimonidesApp.getApplication().isLoggedIn()) {
                            //Buscamos a un usuario con el DNI inicado
                            DNIe dni = (DNIe) pce.getNewValue();
                            AnoEscolar ano = AnoEscolar.getAnoEscolar();
                            Usuario u =null;
                            //TODO Hay que buscar tambien en los profesores y padres
                            Alumno a = Alumno.getAlumnoDesdeCampo("dni", dni.getNif(), ano);
                            if (a != null) {
                                u= new Usuario();
                                u.setAlumno(a);
                                u.setRoles(Rol.ROL_ALUMNO);
                                u.setDNIe(true);
                                u.setUsuarioVirtual(true);
                                u.setNombre(a.getNombreFormateado());
                            }
                            if(u!=null){
                                MaimonidesApp.getApplication().setUsuario(u);
                            }
                        }
                    }
                }
            });
        }
    }
}
