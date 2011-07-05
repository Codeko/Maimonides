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


package com.codeko.apps.maimonides.partes.informes.alumnos;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.mysql.jdbc.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class ResumenAsistenciaAlumno extends ResumenAsistencia {

    @Override
    public void setAlumno(Alumno alumno) {
        super.setAlumno(alumno);
        actualizar();
    }

    protected void actualizar() {
        limpiar();
        //Si tenemos alumno y este tiene ID
        if (getAlumno() != null && getAlumno().getId()!=null) {
            PreparedStatement st = null;
            ResultSet res = null;
            String sql = "SELECT pa.asistencia,count(*) AS total,count(distinct p.fecha) AS totalDias FROM partes_alumnos AS pa " +
                    " JOIN partes AS p ON pa.parte_id=p.id " +
                    " WHERE pa.alumno_id=? GROUP BY pa.asistencia";
            try {
                st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
                st.setInt(1, getAlumno().getId());
                res = st.executeQuery();
                while (res.next()) {
                    int tipo = res.getInt("asistencia");
                    int total = res.getInt("total");
                    int totalDias = res.getInt("totalDias");
                    setValor(tipo, total, totalDias);
                }
            } catch (SQLException ex) {
                Logger.getLogger(ResumenAsistencia.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
