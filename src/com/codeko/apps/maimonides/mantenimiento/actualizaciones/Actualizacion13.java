/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codeko.apps.maimonides.mantenimiento.actualizaciones;

import com.codeko.apps.maimonides.alumnos.MatriculacionAlumno;
import com.codeko.apps.maimonides.mantenimiento.Actualizacion;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author codeko
 */
public class Actualizacion13 extends Actualizacion {

    @Override
    public String getDescripcion() {
        return "Reasignación de horarios cambiados en asignación anterior.";
    }

    @Override
    public boolean necesitaConfirmacion() {
        return false;
    }

    @Override
    public boolean ejecutar() {
        boolean ret = true;
        try {
            MatriculacionAlumno.asignarHorariosFaltantesAlumnos();
        } catch (SQLException ex) {
            Logger.getLogger(Actualizacion13.class.getName()).log(Level.SEVERE, null, ex);
            ret=false;
        }
        return ret;
    }
}
