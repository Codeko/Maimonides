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

import com.codeko.apps.maimonides.MaimonidesBean;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.ParteFaltas;
import com.codeko.apps.maimonides.partes.informes.asistencia.Asistencia;
import com.codeko.apps.maimonides.partes.informes.asistencia.AsistenciaAlumno;
import com.codeko.apps.maimonides.partes.informes.asistencia.FiltroAsistenciaAlumno;
import com.codeko.util.Fechas;
import com.codeko.util.Str;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class CarteroAsistenciaAlumo extends MaimonidesBean {

    GregorianCalendar fechaInicial = null;
    GregorianCalendar fechaFinal = null;

    public GregorianCalendar getFechaFinal() {
        return fechaFinal;
    }

    public final void setFechaFinal(GregorianCalendar fechaFinal) {
        this.fechaFinal = fechaFinal;
    }

    public GregorianCalendar getFechaInicial() {
        return fechaInicial;
    }

    public final void setFechaInicial(GregorianCalendar fechaInicial) {
        this.fechaInicial = fechaInicial;
    }

    public CarteroAsistenciaAlumo(GregorianCalendar ini, GregorianCalendar fin) {
        setFechaInicial(ini);
        setFechaFinal(fin);
    }

    public void addDatosExtra(Map<String, Object> data, AsistenciaAlumno a) {
        data.put("fechaInicial", Fechas.format(getFechaInicial()));
        data.put("fechaFinal", Fechas.format(getFechaFinal()));
        Alumno al = a.getAlumno();
        data.put("faltasInjustificadas", a.getValor(FiltroAsistenciaAlumno.TIPO_FALTAS, ParteFaltas.FALTA_INJUSTIFICADA));
        data.put("faltasJustificadas", a.getValor(FiltroAsistenciaAlumno.TIPO_FALTAS, ParteFaltas.FALTA_JUSTIFICADA));
        data.put("faltasRetrasos", a.getValor(FiltroAsistenciaAlumno.TIPO_FALTAS, ParteFaltas.FALTA_RETRASO));
        data.put("faltasExpulsiones", a.getValor(FiltroAsistenciaAlumno.TIPO_FALTAS, ParteFaltas.FALTA_EXPULSION));
        //Para el total de asistencia sólo filtramos por la fecha final ya que nos insteresan todas las faltas hasta esa fecha
        AsistenciaAlumno at = AsistenciaAlumno.getAsistencia(al, null, getFechaFinal(), this);
        data.put("totalFaltasInjustificadas", at.getValor(FiltroAsistenciaAlumno.TIPO_FALTAS, ParteFaltas.FALTA_INJUSTIFICADA));
        data.put("totalFaltasJustificadas", at.getValor(FiltroAsistenciaAlumno.TIPO_FALTAS, ParteFaltas.FALTA_JUSTIFICADA));
        data.put("totalFaltasRetrasos", at.getValor(FiltroAsistenciaAlumno.TIPO_FALTAS, ParteFaltas.FALTA_RETRASO));
        data.put("totalFaltasExpulsiones", at.getValor(FiltroAsistenciaAlumno.TIPO_FALTAS, ParteFaltas.FALTA_EXPULSION));
        ArrayList<Asistencia> asisPorFecha = Asistencia.getAsistencia(al, 30, getFechaFinal());
        ArrayList<HashMap<String, String>> faltas = new ArrayList<HashMap<String, String>>();
        for (Asistencia aF : asisPorFecha) {
            HashMap<String, String> falta = new HashMap<String, String>();
            falta.put("fecha", Fechas.format(aF.getFecha()));
            for (int x = 0; x < aF.getAsistencia().size(); x++) {
                String c = Str.noNulo(ParteFaltas.getCodigoTipoFalta(aF.getAsistencia().elementAt(x))).replaceAll("\\?", "");
                falta.put("a" + (x + 1), c);
            }
            faltas.add(falta);
        }
        data.put("faltas", faltas);
    }
}
