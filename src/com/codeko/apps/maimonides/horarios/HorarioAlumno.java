package com.codeko.apps.maimonides.horarios;

import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.Horario;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class HorarioAlumno extends Horario {

    Alumno alumno = null;
    boolean activoAlumno = true;
    boolean apoyo = false;

    public boolean isActivoAlumno() {
        return activoAlumno;
    }

    public void setActivoAlumno(boolean activoAlumno) {
        this.activoAlumno = activoAlumno;
    }

    public Alumno getAlumno() {
        return alumno;
    }

    public void setAlumno(Alumno alumno) {
        this.alumno = alumno;
    }

    public boolean isApoyo() {
        return apoyo;
    }

    public void setApoyo(boolean apoyo) {
        this.apoyo = apoyo;
    }

    @Override
    public boolean isActivo() {
        return super.isActivo() && isActivoAlumno();
    }

    @Override
    public boolean cargarDesdeResultSet(ResultSet res) {
        boolean ret = false;
        if (super.cargarDesdeResultSet(res)) {
            try {
                setActivoAlumno(res.getBoolean("activoAlumno"));
                setApoyo(res.getBoolean("apoyo"));
                ret = true;
            } catch (SQLException ex) {
                Logger.getLogger(HorarioAlumno.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return ret;
    }
}
