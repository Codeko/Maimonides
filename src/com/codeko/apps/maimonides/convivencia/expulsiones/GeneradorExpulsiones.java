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


package com.codeko.apps.maimonides.convivencia.expulsiones;

import com.codeko.apps.maimonides.conf.Configuracion;
import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesBean;
import com.codeko.apps.maimonides.convivencia.PanelConfiguracionExpulsiones;
import com.codeko.apps.maimonides.convivencia.ParteConvivencia;

import com.codeko.apps.maimonides.convivencia.TipoConducta;
import com.codeko.util.Num;
import com.codeko.util.Obj;
import com.mysql.jdbc.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class GeneradorExpulsiones extends MaimonidesBean {

    GregorianCalendar fecha = new GregorianCalendar();

    public GregorianCalendar getFecha() {
        return fecha;
    }

    public void setFecha(GregorianCalendar fecha) {
        this.fecha = fecha;
    }

    public ArrayList<PropuestaExpulsion> getPropuestasExpulsion() {
        ArrayList<PropuestaExpulsion> propuestas = new ArrayList<PropuestaExpulsion>();
        PreparedStatement st = null;
        ResultSet res = null;
        try {
            //Primero sacamos las propuestas de expulsion del año activo, con estado pendiente y la gravedad definida (ya que sin gravedad no hay expulsión).
            String sql = "SELECT * FROM conv_partes WHERE ano=? AND estado=" + ParteConvivencia.ESTADO_PENDIENTE + " AND tipo>" + TipoConducta.GRAVEDAD_INDEFINIDA + " ORDER BY alumno_id,tipo DESC,fecha ASC";
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
            st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
            res = st.executeQuery();
            PropuestaExpulsion propuesta = null;
            while (res.next()) {
                ParteConvivencia parte = new ParteConvivencia();
                parte.cargarDesdeResultSet(res);
                if (propuesta == null || !propuesta.getAlumno().equals(parte.getAlumno())) {
                    if (propuesta != null) {
                        propuestas.add(propuesta);
                    }
                    propuesta = new PropuestaExpulsion(parte.getAlumno(), getFecha());
                }
                propuesta.addParteConvivencia(parte);
            }
            if (propuesta != null) {
                propuestas.add(propuesta);
            }
            //Ahora tenemos que ver que se cumplen los requisitos para volver a expulsar un alumno
            ArrayList<PropuestaExpulsion> eliminar = new ArrayList<PropuestaExpulsion>();
            for (PropuestaExpulsion pe : propuestas) {
                if (!pe.isExpulsable()) {
                    eliminar.add(pe);
                }
            }
            //Quitamos de la lista todas los partes no válidos
            propuestas.removeAll(eliminar);
        } catch (Exception ex) {
            Logger.getLogger(GeneradorExpulsiones.class.getName()).log(Level.SEVERE, null, ex);
        }
        Obj.cerrar(res, st);
        return propuestas;
    }

    public static int getEquivalenciaExpulsionesLeves() {
        Configuracion cfg = MaimonidesApp.getApplication().getConfiguracion();
        return Num.getInt(cfg.get("convivencia_equivalencia_leves", "3"));
    }

    public static ArrayList<Integer> getSecuenciaExpulsiones() {
        Configuracion cfg = MaimonidesApp.getApplication().getConfiguracion();
        String str = cfg.get("convivencia_secuncia_expulsion", "3,7,15,30");
        ArrayList<Integer> sec = PanelConfiguracionExpulsiones.getSecuenciaExpulsiones(str);
        return sec;
    }
}
