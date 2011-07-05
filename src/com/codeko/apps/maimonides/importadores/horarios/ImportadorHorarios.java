package com.codeko.apps.maimonides.importadores.horarios;

import com.codeko.apps.maimonides.alumnos.MatriculacionAlumno;
import com.codeko.apps.maimonides.elementos.Horario;
import com.codeko.apps.maimonides.elementos.Profesor;
import com.codeko.apps.maimonides.importadores.ImportadorBase;
import com.codeko.util.estructuras.Par;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author codeko
 */
public abstract class ImportadorHorarios extends ImportadorBase {

    public abstract boolean importarHorarios();

    public boolean guardarHorarios(ArrayList<Par<Profesor, ArrayList<Horario>>> horariosProfesor) {
        boolean ret = true;
        //Para cada profesor tenemos que coger sus horarios
        for (Par<Profesor, ArrayList<Horario>> par : horariosProfesor) {
            Profesor profesor = par.getA();
            ArrayList<Horario> horarios = par.getB();
            //Ahora recuperamos los horarios actuales del profesor
            ArrayList<Horario> horariosActuales = Horario.getHorarios(profesor);
            for (Horario h : horariosActuales) {
                if (!horarios.contains(h)) {
                    h.eliminar();
                } else {
                    //Si ya existe lo eliminamos del array de horarios a guardar
                    horarios.remove(h);
                }
            }
            //Y guardamos los horarios restantes
            ret = guardarObjetosBD(horarios, ret);
            if (ret) {
                firePropertyChange("setMensaje", null, "Asignado horarios a alumnos");
                try {
                    MatriculacionAlumno.reasignarHorariosAlumnos();
                } catch (SQLException ex) {
                    Logger.getLogger(ImportadorHorarios.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        return ret;
    }
}
