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
import com.codeko.apps.maimonides.MaimonidesBean;
import com.codeko.apps.maimonides.asistencia.escolaridad.DatoPerdidaEscolaridadGlobal;
import com.codeko.apps.maimonides.cartero.Carta;
import com.codeko.apps.maimonides.cartero.CarteroAlumno;
import com.codeko.apps.maimonides.elementos.Materia;
import com.codeko.apps.maimonides.partes.cartas.DatoCartaPerdidaEscolaridadPorMaterias;
import com.codeko.util.Str;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Map;
import javax.swing.JOptionPane;

/**
 *
 * @author codeko
 */
public class GestorNotificaciones extends MaimonidesBean {

    public boolean enviarNotificacionesPerdidaEvaluacionContinuaGlobal(GregorianCalendar fechaHasta, Collection<DatoPerdidaEscolaridadGlobal> datos) {
        String nombre = "perdida del derecho a la evaluación continua";
        CarteroAlumno<DatoPerdidaEscolaridadGlobal> cartero = new CarteroAlumno<DatoPerdidaEscolaridadGlobal>(nombre, Carta.TIPO_CARTA_PERDIDA_ESCOLARIDA_GLOBAL);
        cartero.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                if ("error".equals(evt.getPropertyName())) {
                    JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), Str.noNulo(evt.getNewValue()), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        cartero.setCargarAsistenciaTotal(true);
        cartero.setFechaHasta(fechaHasta);
        return cartero.enviar(datos);
    }

    public boolean enviarNotificacionesPerdidaEvaluacionContinuaMaterias(GregorianCalendar fechaHasta, Collection<DatoCartaPerdidaEscolaridadPorMaterias> datos) {
        String nombre = "perdida del derecho a la evaluación continua";
        CarteroAlumno<DatoCartaPerdidaEscolaridadPorMaterias> cartero = new CarteroAlumno<DatoCartaPerdidaEscolaridadPorMaterias>(nombre, Carta.TIPO_CARTA_PERDIDA_ESCOLARIDA_MATERIAS) {

            @Override
            protected void addDatosExtra(Map<String, Object> data, DatoCartaPerdidaEscolaridadPorMaterias a, Carta carta) {
                ArrayList<String> m = new ArrayList<String>();
                for (Materia mat : a.getVectorMaterias()) {
                    m.add(mat.toString());
                    carta.addParametro(mat.getId() + "");
                }
                data.put("listaMaterias", Str.implode(m, ", "));
                data.put("materias", m);
            }
        };
        cartero.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                if ("error".equals(evt.getPropertyName())) {
                    JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), Str.noNulo(evt.getNewValue()), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        cartero.setCargarAsistenciaTotal(true);
        cartero.setFechaHasta(fechaHasta);
        return cartero.enviar(datos);
    }
}
