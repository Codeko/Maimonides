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
