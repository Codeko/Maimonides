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


package com.codeko.apps.maimonides.partes.informes.asistencia;

import com.codeko.apps.maimonides.Configuracion;
import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.util.Fechas;
import com.codeko.util.Obj;
import com.mysql.jdbc.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class Asistencia {

    Vector<Integer> asistencia = new Vector<Integer>();
    GregorianCalendar fecha = null;

    public Asistencia(GregorianCalendar fecha) {
        setFecha(fecha);
        for (int i = 0; i < Configuracion.getHorasPorDia(); i++) {
            asistencia.add(0);
        }
    }

    public Vector<Integer> getAsistencia() {
        return asistencia;
    }

    public void addAsistencia(int hora, int asistencia) {
        hora--;
        getAsistencia().setElementAt(asistencia, hora);
    }

    public GregorianCalendar getFecha() {
        return fecha;
    }

    public void setFecha(GregorianCalendar fecha) {
        this.fecha = fecha;
    }

    public static ArrayList<Asistencia> getAsistencia(Alumno a, int maxDias, GregorianCalendar fechaFin) {
        ArrayList<Asistencia> asistencia = new ArrayList<Asistencia>();
        PreparedStatement st = null;
        ResultSet res = null;
        try {
            String extra = "";
            if (fechaFin != null) {
                extra = " AND fecha<=? ";
            }
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("select hora,asistencia,fecha FROM asistencia_ WHERE alumno_id=? " + extra + " ORDER BY fecha DESC,hora");
            st.setInt(1, a.getId());
            if (fechaFin != null) {
                st.setDate(2, new java.sql.Date(fechaFin.getTimeInMillis()));
            }
            res = st.executeQuery();
            Asistencia ultimaAsis = null;
            while (res.next() && asistencia.size() < maxDias) {
                GregorianCalendar cal = Fechas.toGregorianCalendar(res.getDate("fecha"));
                int hora = res.getInt("hora");
                int asis = res.getInt("asistencia");
                if (ultimaAsis == null || !ultimaAsis.getFecha().equals(cal)) {
                    if (ultimaAsis != null) {
                        asistencia.add(ultimaAsis);
                    }
                    ultimaAsis = new Asistencia(cal);
                }
                ultimaAsis.addAsistencia(hora, asis);
            }
            if (ultimaAsis != null && asistencia.size() < maxDias) {
                asistencia.add(ultimaAsis);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Asistencia.class.getName()).log(Level.SEVERE, null, ex);
        }
        Obj.cerrar(res, st);
        return asistencia;
    }
}
