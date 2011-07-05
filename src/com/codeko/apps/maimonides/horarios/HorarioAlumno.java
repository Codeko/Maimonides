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
