package com.codeko.apps.maimonides.asistencia.escolaridad;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.alumnos.IAlumno;
import com.codeko.apps.maimonides.cartero.Carta;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.Curso;
import com.codeko.apps.maimonides.elementos.Materia;
import com.codeko.apps.maimonides.elementos.Unidad;
import com.codeko.swing.CdkAutoTablaCol;
import com.codeko.util.Obj;
import com.mysql.jdbc.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class DatoPerdidaEscolaridadPorMaterias implements IAlumno {

    Alumno alumno = null;
    Materia materia = null;
    Integer maxFaltas = 0;
    Integer faltas = 0;
    Boolean notificado = null;

    public DatoPerdidaEscolaridadPorMaterias(Alumno alumno, Materia materia, Integer maxFaltas, Integer faltas) {
        setAlumno(alumno);
        setMateria(materia);
        setMaxFaltas(maxFaltas);
        setFaltas(faltas);
    }

    @Override
    public Alumno getAlumno() {
        return alumno;
    }

    public final void setAlumno(Alumno alumno) {
        this.alumno = alumno;
    }

    public Integer getFaltas() {
        return faltas;
    }

    public final void setFaltas(Integer faltas) {
        this.faltas = faltas;
    }

    public Materia getMateria() {
        return materia;
    }

    public final void setMateria(Materia materia) {
        this.materia = materia;
    }

    public Integer getMaxFaltas() {
        return maxFaltas;
    }

    public final void setMaxFaltas(Integer maxFaltas) {
        this.maxFaltas = maxFaltas;
    }

    @CdkAutoTablaCol(titulo = "Porcentaje")
    public Integer getPorcentaje() {
        return (getFaltas() * 100) / getMaxFaltas();
    }

    @CdkAutoTablaCol(titulo = "Curso")
    public Curso getCurso() {
        return getAlumno().getObjetoCurso();
    }

    @CdkAutoTablaCol(titulo = "Unidad")
    public Unidad getUnidad() {
        return getAlumno().getUnidad();
    }

    public Boolean isNotificado() {
        if (notificado == null) {
            notificado = (Carta.isNotificado(getAlumno(), Carta.TIPO_CARTA_PERDIDA_ESCOLARIDA_MATERIAS, getMateria().getId() + ""));
//            PreparedStatement st = null;
//            ResultSet res = null;
//            try {
//                st = (PreparedStatement) MaimonidesApp.getConexion().prepareStatement("SELECT 1 FROM cartas WHERE alumno_id=? AND tipo=? AND (parametros LIKE ? OR parametros LIKE ? OR parametros LIKE ? OR parametros LIKE ? ) ");
//                st.setInt(1, getAlumno().getId());
//                st.setInt(2, Carta.TIPO_CARTA_PERDIDA_ESCOLARIDA_MATERIAS);
//                st.setString(3, getMateria().getId()+"");
//                st.setString(4, getMateria().getId()+"|%");
//                st.setString(5, "%|"+getMateria().getId()+"|%");
//                st.setString(6, "%|"+getMateria().getId());
//                res = st.executeQuery();
//                notificado = res.next();
//            } catch (SQLException ex) {
//                Logger.getLogger(Carta.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            Obj.cerrar(st, res);
        }
        return notificado;
    }

    public static ArrayList<DatoPerdidaEscolaridadPorMaterias> getDatosPerdidaEscolaridad(int porcentajePerdida, Curso curso, Unidad unidad, Alumno alumno, GregorianCalendar fecha) {
        ArrayList<DatoPerdidaEscolaridadPorMaterias> retorno = new ArrayList<DatoPerdidaEscolaridadPorMaterias>();
        StringBuilder sql = new StringBuilder("SELECT alumno_id,h.materia_id, m.maxFaltas,count(*) AS faltas "
                + " FROM asistencia_ AS a "
                + " JOIN horarios AS h ON h.id=a.horario_id "
                + " JOIN materias As m ON m.id=h.materia_id "
                + " WHERE a.ano=? AND m.maxFaltas>0 AND a.asistencia IN (" + ConfiguracionPerdidaEscolaridad.getTiposFaltasContabilizablesSQL() + ") ");
        if (alumno != null && alumno.getId() != null) {
            sql.append(" AND alumno_id=").append(alumno.getId()).append(" ");
        }
        if (curso != null && curso.getId() != null) {
            sql.append(" AND a.curso_id=").append(curso.getId()).append(" ");
        }
        if (unidad != null && unidad.getId() != null) {
            sql.append(" AND a.unidad_id=").append(unidad.getId()).append(" ");
        }
        if (fecha != null) {
            sql.append(" AND a.fecha<=? ");
        }
        sql.append(" GROUP BY a.alumno_id,h.materia_id "
                + " HAVING count(*)>=(m.maxFaltas*(?/100)) "
                + " ORDER BY a.curso_id,a.unidad_id,alumno_id");
        PreparedStatement st = null;
        ResultSet res = null;
        try {
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql.toString());
            int cont = 1;
            st.setInt(cont, MaimonidesApp.getApplication().getAnoEscolar().getId());
            cont++;
            if (fecha != null) {
                st.setDate(cont, new java.sql.Date(fecha.getTimeInMillis()));
                cont++;
            }
            st.setInt(cont, porcentajePerdida);
            res = st.executeQuery();

            while (res.next()) {
                Alumno a = Alumno.getAlumno(res.getInt("alumno_id"));
                Materia m = Materia.getMateria(res.getInt("materia_id"));
                DatoPerdidaEscolaridadPorMaterias dato = new DatoPerdidaEscolaridadPorMaterias(a, m, res.getInt("maxFaltas"), res.getInt("faltas"));
                retorno.add(dato);
            }
        } catch (Exception ex) {
            Logger.getLogger(DatoPerdidaEscolaridadPorMaterias.class.getName()).log(Level.SEVERE, null, ex);
        }
        Obj.cerrar(res, st);
        return retorno;
    }
}
