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


package com.codeko.apps.maimonides.partes.cartas;

import com.codeko.apps.maimonides.conf.Configuracion;
import com.codeko.apps.maimonides.MaimonidesApp;

import com.codeko.apps.maimonides.cartero.Carta;
import com.codeko.apps.maimonides.cartero.CarteroAlumno;
import com.codeko.apps.maimonides.partes.informes.asistencia.AsistenciaAlumno;
import com.codeko.util.Fechas;
import com.codeko.util.Num;
import com.codeko.util.Str;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Map;
import javax.swing.JOptionPane;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class ConfiguracionCartasAsistencia {

    public static boolean isImprimirCartas() {
        boolean imprimir = false;
        GregorianCalendar cal = new GregorianCalendar();
        Configuracion cfg = MaimonidesApp.getApplication().getConfiguracion();
        int dow = cal.get(GregorianCalendar.DAY_OF_WEEK);
        int doi = Num.getInt(cfg.get("cartas_faltas_dia_semana", "5")) + 1;
        imprimir = dow == doi;
        return imprimir;
    }

    public static GregorianCalendar getFechaIniCartas() {
        GregorianCalendar cal = new GregorianCalendar();
        Configuracion cfg = MaimonidesApp.getApplication().getConfiguracion();
        int dias = Num.getInt(cfg.get("cartas_faltas_dias_inicio", "11"));
        //spCartasNumDias.setValue(Num.getInt(cfg.get("cartas_faltas_num_dias", "7")));
        cal.add(GregorianCalendar.DATE, dias * -1);
        return cal;
    }

    public static GregorianCalendar getFechaFinCartas() {
        GregorianCalendar cal = getFechaIniCartas();
        Configuracion cfg = MaimonidesApp.getApplication().getConfiguracion();
        int diasAdd = Num.getInt(cfg.get("cartas_faltas_num_dias", "7"));
        cal.add(GregorianCalendar.DATE, diasAdd);
        return cal;
    }

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task enviar() {
        return new EnviarTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    class EnviarTask extends org.jdesktop.application.Task<Boolean, Void> {

        GregorianCalendar ini = ConfiguracionCartasAsistencia.getFechaIniCartas();
        GregorianCalendar fin = ConfiguracionCartasAsistencia.getFechaFinCartas();

        EnviarTask(org.jdesktop.application.Application app) {
            super(app);
        }

        @Override
        protected Boolean doInBackground() {
            setMessage("Cargando asistencia...");

            ArrayList<AsistenciaAlumno> asistencia = AsistenciaAlumno.getAsistencias(null, null, null, ini, fin, null, null);
            if (asistencia.size() > 0) {
                setProgress(0, 0, asistencia.size());
                //Ahora vamos carta por carta preparando y enviado.
                if (ini == null) {
                    ini = new GregorianCalendar();
                    ini.set(MaimonidesApp.getApplication().getAnoEscolar().getAno(), GregorianCalendar.SEPTEMBER, 1);
                }
                if (fin == null) {
                    fin = new GregorianCalendar();
                }
                final CarteroAsistenciaAlumo ca = new CarteroAsistenciaAlumo(ini, fin);
                CarteroAlumno<AsistenciaAlumno> cartero = new CarteroAlumno<AsistenciaAlumno>("faltas de asistencia", Carta.TIPO_CARTA_AVISO_FALTAS) {

                    @Override
                    protected void addDatosExtra(Map<String, Object> data, AsistenciaAlumno a, Carta carta) {
                        ca.addDatosExtra(data, a);
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
                cartero.setCargarAsistenciaTotal(false);//La asignamos a mano nosotros
                return cartero.enviar(asistencia);
            } else {
                setMessage("No hay faltas nuevas entre las fechas " + (Fechas.format(ini)) + " y " + (Fechas.format(fin)) + " para notificar.");
                JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), "No hay faltas nuevas entre las fechas " + (Fechas.format(ini)) + " y " + (Fechas.format(fin)) + " para notificar.", "No hay datos", JOptionPane.WARNING_MESSAGE);
            }
            return false;
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
}


