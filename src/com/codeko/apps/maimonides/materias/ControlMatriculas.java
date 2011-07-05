package com.codeko.apps.maimonides.materias;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesBean;
import com.codeko.apps.maimonides.alumnos.MatriculacionAlumno;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.Horario;
import com.codeko.apps.maimonides.elementos.Materia;
import com.codeko.util.Obj;
import com.mysql.jdbc.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
//TODO Todos estos métodos pueden ser estaticos o añadirlos a la clase de alumno
public class ControlMatriculas extends MaimonidesBean {

    private boolean desmatricular(Materia materia, Alumno alumno) throws SQLException, SQLException {
        boolean ret;
        //Si le estamos quitando la matricula hay que borrar
        PreparedStatement stDelMat = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("DELETE FROM materias_alumnos WHERE materia_id=? AND alumno_id=? ");
        stDelMat.setInt(1, materia.getId());
        stDelMat.setInt(2, alumno.getId());
        ret = stDelMat.executeUpdate() > 0;
        stDelMat.close();
        if (ret) {
            PreparedStatement stDelHor = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("DELETE alumnos_horarios FROM alumnos_horarios JOIN horarios ON horarios.id=alumnos_horarios.horario_id WHERE horarios.materia_id=? AND alumnos_horarios.alumno_id=? ");
            stDelHor.setInt(1, materia.getId());
            stDelHor.setInt(2, alumno.getId());
            stDelHor.executeUpdate();
            stDelHor.close();
        }
        return ret;
    }

    private boolean matricular(Alumno alumno, Materia materia) throws SQLException {
        boolean ret = false;
        try {
            PreparedStatement stInsertMat = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("INSERT INTO materias_alumnos(alumno_id,materia_id) VALUES(?,?)");
            stInsertMat.setInt(1, alumno.getId());
            stInsertMat.setInt(2, materia.getId());
            ret = stInsertMat.executeUpdate() > 0;
            stInsertMat.close();
        } catch (SQLException ex) {
            //Esto es normal y significa que ya está matriculado
        }
        boolean hayHorarios = false;
        if (ret) {
            String sql = "REPLACE alumnos_horarios SELECT h.id AS horario_id,a.id AS alumno_id,true AS activo FROM horarios_ AS h "
                    + " JOIN materias AS m ON m.id=h.materia_id "
                    + " JOIN materias_alumnos AS ma ON ma.materia_id=m.id "
                    + " JOIN alumnos AS a ON a.id=ma.alumno_id AND a.unidad_id=h.unidad_id WHERE m.id=? AND a.id=? ";
            PreparedStatement stInsertHor = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
            stInsertHor.setInt(1, materia.getId());
            stInsertHor.setInt(2, alumno.getId());
            hayHorarios = stInsertHor.executeUpdate() > 0;
            stInsertHor.close();
            if (!hayHorarios) {
                firePropertyChange("noHayHorarios", alumno, materia);
            }
            //Estaría bien ver que no se ha creado una imposibilidad
            //Revisar horarios
            MaimonidesApp.getApplication().getConector().getConexion().commit();
            revisarHorariosConflictivos(alumno);
        }
        return ret;
    }

    public boolean matricular(int alumno, int materia, boolean matricular) {
        boolean ret = false;
        try {
            Alumno a = Alumno.getAlumno(alumno);
            Materia m = Materia.getMateria(materia);
            ret = matricular(a, m, matricular);
        } catch (Exception ex) {
            Logger.getLogger(ControlMatriculas.class.getName()).log(Level.SEVERE, "Error matriculando (" + matricular + ") alumno '" + alumno + "' en materia '" + materia + "'", ex);
        }
        return ret;
    }

    public boolean matricular(Alumno alumno, Materia materia, boolean matricular) {
        boolean ret = false;
        try {
            MaimonidesApp.getApplication().getConector().getConexion().setAutoCommit(false);
            if (matricular) {
                ret = matricular(alumno, materia);
                if (ret) {
                    firePropertyChange("matricular", alumno, materia);
                }
            } else {
                ret = desmatricular(materia, alumno);
                if (ret) {
                    firePropertyChange("desmatricular", alumno, materia);
                }
            }
            MaimonidesApp.getApplication().getConector().getConexion().commit();
        } catch (SQLException ex) {
            Logger.getLogger(MatriculacionAlumno.class.getName()).log(Level.SEVERE, null, ex);
            try {
                MaimonidesApp.getApplication().getConector().getConexion().rollback();
            } catch (SQLException ex1) {
                Logger.getLogger(ControlMatriculas.class.getName()).log(Level.SEVERE, null, ex1);
            }
        } finally {
            try {
                MaimonidesApp.getApplication().getConector().getConexion().setAutoCommit(true);
            } catch (SQLException ex) {
                Logger.getLogger(MatriculacionAlumno.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return ret;
    }

    private void revisarHorariosConflictivos(Alumno alumno) {
        String sql = "SELECT h.dia,h.hora FROM alumnos_horarios AS ah JOIN horarios_ AS h ON h.id=ah.horario_id "
                + " WHERE h.activo=1 AND ah.activo=1 AND ah.alumno_id=? "
                + " GROUP BY ah.alumno_id,h.dia,h.hora "
                + " HAVING count(distinct materia_id)>1 ";
        try {
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
            st.setInt(1, alumno.getId());
            ResultSet res = st.executeQuery();
            ArrayList<ArrayList<Horario>> conflictos = new ArrayList<ArrayList<Horario>>();
            while (res.next()) {
                ArrayList<Horario> hors = new ArrayList<Horario>();
                int dia = res.getInt("dia");
                int hora = res.getInt("hora");
                String sqlHorarios = "SELECT h.* FROM horarios_ AS h JOIN alumnos_horarios AS ah ON ah.horario_id=h.id "
                        + " WHERE h.activo=1 AND ah.activo=1 AND ah.alumno_id=? AND h.dia=? AND h.hora=? ";
                PreparedStatement stHora = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sqlHorarios);
                stHora.setInt(1, alumno.getId());
                stHora.setInt(2, dia);
                stHora.setInt(3, hora);
                ResultSet resHora = stHora.executeQuery();
                while (resHora.next()) {
                    Horario h = new Horario();
                    h.cargarDesdeResultSet(resHora);
                    hors.add(h);
                }
                if (hors.size() > 0) {
                    conflictos.add(hors);
                }
                Obj.cerrar(stHora, resHora);
            }
            Obj.cerrar(st, res);
            if (conflictos.size() > 0) {
                firePropertyChange("conflictosHorarios", alumno, conflictos);
            }
        } catch (SQLException ex) {
            Logger.getLogger(MatriculacionAlumno.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static boolean isMatriculado(Alumno alumno,Collection<Materia> materias){
        for(Materia m:materias){
            if(isMatriculado(alumno, m)){
                return true;
            }
        }
        return false;
    }

    public static boolean isMatriculado(Alumno alumno,Materia materia){
        boolean ret=false;
        PreparedStatement st=null;
        ResultSet res=null;
        try {
            st = (PreparedStatement) MaimonidesApp.getConexion().prepareStatement("SELECT count(*) FROM materias_alumnos WHERE alumno_id=? AND materia_id=?");
            st.setInt(1, alumno.getId());
            st.setInt(2, materia.getId());
            res = st.executeQuery();
            ret=res.next() && res.getInt(1)>0;
        } catch (SQLException ex) {
            Logger.getLogger(MatriculacionAlumno.class.getName()).log(Level.SEVERE, "Error verificando si el alumno "+alumno.getId()+" está matriculado en "+materia.getId(), ex);
        }
        Obj.cerrar(st,res);
        return ret;
    }
}
