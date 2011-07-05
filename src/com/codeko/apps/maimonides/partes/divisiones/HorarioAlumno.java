package com.codeko.apps.maimonides.partes.divisiones;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.Horario;
import com.codeko.util.Obj;
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
public class HorarioAlumno extends Horario implements Cloneable {

    Boolean activoAlumno = null;
    Alumno alumno = null;

    public Alumno getAlumno() {
        return alumno;
    }

    public void setAlumno(Alumno alumno) {
        this.alumno = alumno;
    }

    public Boolean isActivoAlumno() {
        if (activoAlumno == null) {
            if (!isActivo()) {
                activoAlumno = false;
            } else {
                try {
                    PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT activo FROM alumnos_horarios WHERE alumno_id=? AND horario_id=?");
                    st.setInt(1, getAlumno().getId());
                    st.setInt(2, getId());
                    ResultSet res = st.executeQuery();
                    if (res.next()) {
                        activoAlumno = res.getBoolean(1);
                    }
                    Obj.cerrar(st, res);
                } catch (SQLException ex) {
                    Logger.getLogger(HorarioAlumno.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return activoAlumno;
    }

    public void setActivoAlumno(Boolean activoAlumno) {
        this.activoAlumno = activoAlumno;
    }

    @Override
    public boolean guardar() {
        boolean ret = false;
        PreparedStatement st;
        try {
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("REPLACE alumnos_horarios SET activo=?, alumno_id=?, horario_id=?");
            st.setBoolean(1, isActivoAlumno());
            st.setInt(2, getAlumno().getId());
            st.setInt(3, getId());
            ret = st.executeUpdate() > 0;
        } catch (SQLException ex) {
            Logger.getLogger(HorarioAlumno.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    @Override
    public boolean cargarDesdeResultSet(ResultSet res) {
        boolean ret = super.cargarDesdeResultSet(res);
        if (ret) {
            Alumno a;
            try {
                a = Alumno.getAlumno(res.getInt("alumno_id"));
                setAlumno(a);
            } catch (SQLException ex) {
                Logger.getLogger(HorarioAlumno.class.getName()).log(Level.SEVERE, null, ex);
                ret = false;
            }
        }
        return ret;

    }
}
