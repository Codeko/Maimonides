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


package com.codeko.apps.maimonides.alumnos;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.Materia;
import com.codeko.apps.maimonides.materias.ControlMatriculas;
import com.codeko.util.Obj;
import com.mysql.jdbc.PreparedStatement;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MatriculacionAlumno extends Alumno {

    ArrayList<Materia> materias = null;
    ArrayList<Boolean> matriculaciones = null;
    int offset = new Alumno().getNumeroDeCampos();

    public MatriculacionAlumno(ArrayList<Materia> materias) {
        super();
        this.materias = materias;
        //Creamos el vector de matriculaciones como nulo
        this.matriculaciones = new ArrayList<Boolean>(materias.size());
        for (Materia m : materias) {
            this.matriculaciones.add(null);
        }
    }

    public ArrayList<Materia> getMaterias() {
        return materias;
    }

    public ArrayList<Boolean> getMatriculaciones() {
        return matriculaciones;
    }

    @Override
    public int getNumeroDeCampos() {
        return super.getNumeroDeCampos() + getMaterias().size();
    }

    @Override
    public Object getValueAt(int index) {
        if (index < super.getNumeroDeCampos()) {
            return super.getValueAt(index);
        }
        index -= offset;
        if (getMatriculaciones().get(index) == null) {
            try {
                PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM materias_alumnos WHERE materia_id=? AND alumno_id=?");
                st.setInt(1, getMaterias().get(index).getId());
                st.setInt(2, getId());
                ResultSet res = st.executeQuery();
                getMatriculaciones().set(index, res.next());
                Obj.cerrar(st, res);
            } catch (SQLException ex) {
                Logger.getLogger(MatriculacionAlumno.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return getMatriculaciones().get(index);
    }

    @Override
    public String getTitleAt(int index) {
        if (index < super.getNumeroDeCampos()) {
            return super.getTitleAt(index);
        }
        index -= offset;
        return getMaterias().get(index).getCodigoMateria();
    }

    @Override
    public Class getClassAt(int index) {
        if (index < super.getNumeroDeCampos()) {
            return super.getClassAt(index);
        }
        return Boolean.class;
    }

    @Override
    public boolean setValueAt(int index, Object val) {
        if (index < super.getNumeroDeCampos()) {
            return super.setValueAt(index, val);
        }
        index -= offset;
        //vemos de que materia se trata
        Materia m = getMaterias().get(index);
        Boolean matriculado = (Boolean) val;
        ControlMatriculas control = new ControlMatriculas();
        control.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            }
        });
        boolean ret = control.matricular(this, m, matriculado);
        if (ret) {
            getMatriculaciones().set(index, matriculado);
        }
        return ret;
    }

    @Override
    public boolean esCampoEditable(int index) {
        if (index < super.getNumeroDeCampos()) {
            return super.esCampoEditable(index);
        }
        return true;
    }

    public static ArrayList<String> reasignarHorariosAlumno(Alumno alumno) throws SQLException, SQLException {
        ArrayList<String> mensajes = new ArrayList<String>();
        String sql = "select count(distinct ah.activo) from alumnos_horarios AS ah JOIN horarios_ AS h ON h.id=ah.horario_id WHERE h.activo AND h.ano=? AND h.unidad_id=? ";
        PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
        st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
        st.setInt(2, alumno.getIdUnidad());
        ResultSet res = st.executeQuery();
        if (res != null & res.next()) {
            int num = res.getInt(1);
            if (num > 1) {
                mensajes.add("La unidad del alumno tiene horarios divididos. Debe asignar la división de horarios para este alumno.");
            }
        }
        Obj.cerrar(res, st);
        //Primero borramos los horarios actuales del alumno
        sql = "DELETE FROM alumnos_horarios WHERE alumno_id=? ";
        st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
        st.setInt(1, alumno.getId());
        st.executeUpdate();
        Obj.cerrar(st);
        //Luego borramos los posibles apoyos
        sql = "DELETE FROM apoyos_alumnos WHERE alumno_id=? ";
        st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
        st.setInt(1, alumno.getId());
        int numApoyos = st.executeUpdate();
        if (numApoyos > 0) {
            //TODO Lo ideal es que los asigne solos basicamente cambiando el horario al equivalente de la nueva unidad
            mensajes.add("El alumno tenía asignado apoyos. Al cambiar los horarios debe volver a asignarle los apoyos.");
        }
        Obj.cerrar(st);
        asignarHorariosAlumnos(alumno);
        return mensajes;
    }

    public static void reasignarHorariosAlumnos() throws SQLException, SQLException {
        reasignarHorariosMateriasAlumnos();
        reasignarHorariosActividadAlumnos();
    }

    public static void reasignarHorariosActividadAlumnos() throws SQLException {
        //Ahora las actividades solo
        String sql = "REPLACE alumnos_horarios SELECT h.id AS horario_id,a.id AS alumno_id,true AS activo FROM horarios_ AS h "
                + " JOIN alumnos AS a ON a.unidad_id=h.unidad_id "
                + " WHERE a.borrado=0 AND h.ano=? AND h.materia_id IS NULL ";
        PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
        st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
        st.executeUpdate();
        st.close();
    }

    public static void reasignarHorariosMateriasAlumnos() throws SQLException {
        String sql = "REPLACE alumnos_horarios SELECT h.id AS horario_id,a.id AS alumno_id,true AS activo FROM horarios_ AS h "
                + " JOIN materias AS m ON m.id=h.materia_id "
                + " JOIN materias_alumnos AS ma ON ma.materia_id=m.id "
                + " JOIN alumnos AS a ON a.id=ma.alumno_id AND a.unidad_id=h.unidad_id WHERE a.borrado=0 AND h.ano=? ";
        PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
        st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
        st.executeUpdate();
        st.close();
    }

    public static void asignarHorariosFaltantesAlumnos() throws SQLException {
        String sql = "INSERT INTO alumnos_horarios SELECT h.id AS horario_id,a.id AS alumno_id,true AS activo FROM horarios_ AS h  "
                + " JOIN materias AS m ON m.id=h.materia_id  "
                + " JOIN materias_alumnos AS ma ON ma.materia_id=m.id  "
                + " JOIN alumnos AS a ON a.id=ma.alumno_id AND a.unidad_id=h.unidad_id  "
                + " LEFT JOIN alumnos_horarios AS ah ON ah.horario_id=h.id AND ah.alumno_id=a.id "
                + " WHERE a.borrado=0 AND h.ano=? AND ah.horario_id IS NULL";
        PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
        st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
        st.executeUpdate();
        st.close();

        sql = "INSERT INTO alumnos_horarios SELECT h.id AS horario_id,a.id AS alumno_id,true AS activo FROM horarios_ AS h "
                + " JOIN alumnos AS a ON a.unidad_id=h.unidad_id "
                + " LEFT JOIN alumnos_horarios AS ah ON ah.horario_id=h.id AND ah.alumno_id=a.id "
                + " WHERE a.borrado=0 AND h.ano=? AND h.materia_id IS NULL AND ah.horario_id IS NULL ";
        st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
        st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
        st.executeUpdate();
        st.close();
    }

    public static void asignarHorariosAlumnos(Alumno alumno) throws SQLException {
        String sql = "REPLACE alumnos_horarios SELECT h.id AS horario_id,a.id AS alumno_id,true AS activo FROM horarios_ AS h "
                + " JOIN materias AS m ON m.id=h.materia_id "
                + " JOIN materias_alumnos AS ma ON ma.materia_id=m.id "
                + " JOIN alumnos AS a ON a.id=ma.alumno_id AND a.unidad_id=h.unidad_id WHERE a.borrado=0 AND h.ano=? AND a.id=?";
        PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
        st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
        st.setInt(2, alumno.getId());
        st.executeUpdate();
        Obj.cerrar(st);
        //Ahora las actividades solo
        sql = "REPLACE alumnos_horarios SELECT h.id AS horario_id,a.id AS alumno_id,true AS activo FROM horarios_ AS h "
                + " JOIN alumnos AS a ON a.unidad_id=h.unidad_id "
                + " WHERE a.borrado=0 AND h.ano=? AND h.materia_id IS NULL AND a.id=?  ";
        st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
        st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
        st.setInt(2, alumno.getId());
        st.executeUpdate();
        Obj.cerrar(st);
    }
}
