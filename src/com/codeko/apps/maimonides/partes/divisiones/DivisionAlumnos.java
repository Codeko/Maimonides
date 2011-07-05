package com.codeko.apps.maimonides.partes.divisiones;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesUtil;
import com.codeko.apps.maimonides.elementos.Actividad;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.util.Obj;
import com.mysql.jdbc.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class DivisionAlumnos {

    int dia = 0;
    int hora = 0;
    String nombreMateria = null;
    String curso = null;
    Actividad actividad = null;
    ArrayList<LineaDivisionAlumno> lineas = null;

    public DivisionAlumnos(int dia, int hora, Actividad actividad, String nombreMateria, String curso) {
        setDia(dia);
        setHora(hora);
        setActividad(actividad);
        setNombreMateria(nombreMateria);
        setCurso(curso);
    }

    public String getNombreMateria() {
        return nombreMateria;
    }

    public final void setNombreMateria(String nombreMateria) {
        this.nombreMateria = nombreMateria;
    }

    public Actividad getActividad() {
        return actividad;
    }

    public final void setActividad(Actividad actividad) {
        this.actividad = actividad;
    }

    public String getCurso() {
        return curso;
    }

    public final void setCurso(String curso) {
        this.curso = curso;
    }

    public int getDia() {
        return dia;
    }

    public final void setDia(int dia) {
        this.dia = dia;
    }

    public int getHora() {
        return hora;
    }

    public final void setHora(int hora) {
        this.hora = hora;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(MaimonidesUtil.getNombreDiaSemana(getDia(), false));
        sb.append(" ");
        sb.append(getHora());
        sb.append("ª");
        sb.append(" ");
        if (getNombreMateria() != null) {
            sb.append(getNombreMateria());
        } else {
            sb.append(getActividad().getCodigoActividad());
        }
        return sb.toString();
    }

    public String toStringExtendido() {
        StringBuilder sb = new StringBuilder();
        sb.append(MaimonidesUtil.getNombreDiaSemana(getDia(), true));
        sb.append(" ");
        sb.append(getHora());
        sb.append("ª Hora: ");
        if (getNombreMateria() != null) {
            sb.append(getNombreMateria());
        } else {
            sb.append(getActividad().getDescripcion());
        }
        return sb.toString();
    }

    public ArrayList<LineaDivisionAlumno> getLineas() {
        if (lineas == null) {
            lineas = new ArrayList<LineaDivisionAlumno>();
            String sql = "SELECT h.*,a.id AS alumno_id FROM alumnos_horarios AS ah "
                    + " JOIN alumnos AS a ON a.id=ah.alumno_id "
                    + " JOIN materias_alumnos AS ma ON ma.alumno_id=a.id "
                    + " JOIN horarios_ AS h ON h.id=ah.horario_id AND a.unidad_id=h.unidad_id"
                    + " JOIN materias AS m ON h.materia_id=m.id AND m.id=ma.materia_id "
                    + " JOIN cursos AS c ON c.id=m.curso_id "
                    + " WHERE a.borrado=0 AND h.ano=? AND h.dia=? AND h.hora=? AND m.nombre=? AND h.actividad_id=? AND c.curso=? "
                    + " ORDER BY a.unidad_id," + Alumno.getCampoOrdenNombre("a") + ",h.profesor_id";
            try {
                PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
                st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
                st.setInt(2, getDia());
                st.setInt(3, getHora());
                if (getNombreMateria() != null) {
                    st.setString(4, getNombreMateria());
                } else {
                    st.setObject(4, null);
                }
                st.setInt(5, getActividad().getId());
                st.setString(6, getCurso());
                ResultSet res = st.executeQuery();
                ArrayList<HorarioAlumno> horarios = new ArrayList<HorarioAlumno>();
                int ultimoAlumno = -1;
                while (res.next()) {
                    HorarioAlumno h = new HorarioAlumno();
                    h.cargarDesdeResultSet(res);
                    if (h.getAlumno().getId() != ultimoAlumno && ultimoAlumno != -1) {
                        LineaDivisionAlumno lin = new LineaDivisionAlumno(horarios, horarios.get(horarios.size() - 1).getAlumno());
                        lineas.add(lin);
                        horarios.clear();
                    }
                    ultimoAlumno = h.getAlumno().getId();
                    horarios.add(h);
                }
                Obj.cerrar(st, res);
                if (horarios.size() > 0) {
                    LineaDivisionAlumno lin = new LineaDivisionAlumno(horarios, horarios.get(horarios.size() - 1).getAlumno());
                    lineas.add(lin);
                    horarios.clear();
                }
            } catch (SQLException ex) {
                Logger.getLogger(DivisionAlumnos.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        return lineas;
    }

    public void resetearLineas() {
        lineas = null;
    }
}
