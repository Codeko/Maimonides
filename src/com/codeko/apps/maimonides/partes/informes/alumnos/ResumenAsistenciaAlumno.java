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
