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


package com.codeko.apps.maimonides.notificaciones;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.asistencia.escolaridad.DatoPerdidaEscolaridadGlobal;
import java.util.Collection;
import java.util.GregorianCalendar;
import javax.swing.JOptionPane;

/**
 *
 * @author codeko
 */
public class EnviarNotificacionesPerdidaEvaluacionContinuaGlobalTask extends org.jdesktop.application.Task<Boolean, Void> {

    GregorianCalendar fecha = null;
    Collection<DatoPerdidaEscolaridadGlobal> datos = null;

    public EnviarNotificacionesPerdidaEvaluacionContinuaGlobalTask(org.jdesktop.application.Application app, GregorianCalendar fechaHasta, Collection<DatoPerdidaEscolaridadGlobal> datos) {
        super(app);
        this.fecha = fechaHasta;
        this.datos = datos;
    }

    @Override
    protected Boolean doInBackground() {
        setMessage("Enviando...");
        setProgress(0, 0, datos.size());
        GestorNotificaciones gestor = new GestorNotificaciones();
        return gestor.enviarNotificacionesPerdidaEvaluacionContinuaGlobal(fecha, datos);
    }

    @Override
    protected void succeeded(Boolean result) {
        if (result) {
            setMessage("Cartas impresas/enviadas correctamente.");
            JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), "Cartas impresas/enviadas correctamente", "Enviar", JOptionPane.INFORMATION_MESSAGE);
        } else {
            setMessage("Ha habido algún error enviando las notificaciones.");
        }
    }
}
