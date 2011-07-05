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


package com.codeko.apps.maimonides.elementos;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesBean;
import com.codeko.util.Obj;
import com.codeko.util.Str;
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
public class Tutor extends MaimonidesBean {

    String nombre = "";
    String apellido1 = "";
    String apellido2 = "";
    String dni = "";
    String sexo = "H";
    int alumno = 0;
    int numTutor = 1;

    public Tutor(int alumno, int numTutor) {
        setAlumno(alumno);
        setNumTutor(numTutor);
    }

    public int getNumTutor() {
        return numTutor;
    }

    public final void setNumTutor(int numTutor) {
        this.numTutor = numTutor;
    }

    public int getAlumno() {
        return alumno;
    }

    public final void setAlumno(int alumno) {
        this.alumno = alumno;
    }

    public String getApellido1() {
        return apellido1;
    }

    public void setApellido1(String apellido1) {
        this.apellido1 = apellido1;
    }

    public String getApellido2() {
        return apellido2;
    }

    public void setApellido2(String apellido2) {
        this.apellido2 = apellido2;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getSexo() {
        if (sexo == null || sexo.trim().equals("")) {
            sexo = "H";
        }
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public void cargarDesdeResultSet(ResultSet res) {
        if (res != null) {
            try {
                setDni(Str.noNulo(res.getString("t" + getNumTutor() + "_dni")));
                setNombre(Str.noNulo(res.getString("t" + getNumTutor() + "_nombre")));
                setApellido1(Str.noNulo(res.getString("t" + getNumTutor() + "_apellido1")));
                setApellido2(Str.noNulo(res.getString("t" + getNumTutor() + "_apellido2")));
                setSexo(Str.noNulo(res.getString("t" + getNumTutor() + "_sexo")));
            } catch (SQLException ex) {
                Logger.getLogger(Tutor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public boolean hayDatos() {
        return !getDni().trim().equals("") || !getNombre().trim().equals("") || !getApellido1().trim().equals("") || !getApellido2().trim().equals("");
    }

    public boolean guardar() {
        boolean ret = false;
        String sql = "UPDATE alumnos SET t" + getNumTutor() + "_dni=?,t" + getNumTutor() + "_nombre=?,t" + getNumTutor() + "_apellido1=?,t" + getNumTutor() + "_apellido2=?,t" + getNumTutor() + "_sexo=? WHERE id=?";
        PreparedStatement st = null;
        try {
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
            st.setString(1, getDni());
            st.setString(2, getNombre());
            st.setString(3, getApellido1());
            st.setString(4, getApellido2());
            st.setString(5, getSexo());
            st.setInt(6, getAlumno());
            ret = st.executeUpdate() > 0;
        } catch (SQLException ex) {
            Logger.getLogger(Tutor.class.getName()).log(Level.SEVERE, null, ex);
        }
        Obj.cerrar(st);
        return ret;
    }

    public String getNombreObjeto() {
        return "Tutor";
    }

    public String getDescripcionObjeto() {
        return getApellido1() + " " + getApellido2() + ", " + getNombre();
    }

    public String getNombreConDon() {
        StringBuilder nomTutor = new StringBuilder();
        if (getSexo().equals("M")) {
            nomTutor.append("Sra. Doña ");
        } else {
            nomTutor.append("Sr. Don ");
        }
        nomTutor.append(getNombre());
        nomTutor.append(" ");
        nomTutor.append(getApellido1());
        nomTutor.append(" ");
        nomTutor.append(getApellido2());
        return nomTutor.toString();
    }
}
