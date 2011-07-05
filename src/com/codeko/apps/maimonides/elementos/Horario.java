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

import com.codeko.apps.maimonides.Conector;
import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.cache.Cache;
import com.codeko.apps.maimonides.horarios.BloqueHorario;
import com.codeko.apps.maimonides.horarios.ConflictoHorario;
import com.codeko.swing.CdkAutoTablaCol;
import com.codeko.util.Num;
import com.codeko.util.Obj;
import com.codeko.util.Str;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.Statement;
import java.io.IOException;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Codeko
 */
public class Horario extends ObjetoBD implements Cloneable {

    @CdkAutoTablaCol(titulo = "Año")
    AnoEscolar anoEscolar = null;
    @CdkAutoTablaCol(titulo = "Día")
    Integer dia = null;
    @CdkAutoTablaCol(ignorar = true)
    Integer tramo = null;
    @CdkAutoTablaCol(ignorar = true)
    Integer dependencia = null;
    @CdkAutoTablaCol(ignorar = true)
    Integer materia = null;
    @CdkAutoTablaCol(ignorar = true)
    Integer profesor = null;
    @CdkAutoTablaCol(ignorar = true)
    Integer actividad = null;
    @CdkAutoTablaCol(ignorar = true)
    Integer unidad = null;
    Integer hora = null;
    @CdkAutoTablaCol(titulo = "Alumnos")
    Integer numeroDeAlumnos = null;
    @CdkAutoTablaCol(titulo = "Materia")
    Materia objetoMateria = null;
    @CdkAutoTablaCol(titulo = "Actividad")
    Actividad objetoActividad = null;
    @CdkAutoTablaCol(titulo = "Profesor")
    Profesor objetoProfesor = null;
    @CdkAutoTablaCol(titulo = "Unidad")
    Unidad objetoUnidad = null;
    @CdkAutoTablaCol(titulo = "Aula")
    Dependencia objetoDependencia = null;
    @CdkAutoTablaCol(titulo = "D.I.C.U.")
    Integer dicu = 0;
    @CdkAutoTablaCol(ignorar = true)
    boolean activo = true;
    @CdkAutoTablaCol(ignorar = true)
    private ArrayList<ConflictoHorario> conflictos = null;

    public static Horario getHorario(int id) throws SQLException {
        Object obj = Cache.get(Horario.class, id);
        if (obj != null) {
            return (Horario) obj;
        } else {
            Horario h = new Horario(id);
            return h;
        }
    }

    public Profesor getObjetoProfesor() {
        if (objetoProfesor == null) {
            if (getProfesor() != null && getProfesor().intValue() > 0) {
                try {
                    objetoProfesor = Profesor.getProfesor(getProfesor());
                } catch (Exception ex) {
                    Logger.getLogger(Horario.class.getName()).log(Level.SEVERE, "Error cargando profesor: " + getProfesor(), ex);
                }
            }
        }
        return objetoProfesor;
    }

    public Dependencia getObjetoDependencia() {
        if (objetoDependencia == null) {
            if (getDependencia() != null && getDependencia().intValue() > 0) {
                try {
                    objetoDependencia = Dependencia.getDependencia(getDependencia());
                } catch (Exception ex) {
                    Logger.getLogger(Horario.class.getName()).log(Level.SEVERE, "Error cargando dependencia: " + getDependencia(), ex);
                }
            }
        }
        return objetoDependencia;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public boolean isDicu() {
        return dicu == 1;
    }

    public Integer getDicu() {
        return dicu;
    }

    public void setDicu(Integer dicu) {
        this.dicu = dicu;
    }

    public Object getObjetoMateriaOActividad() {
        if (getMateria() != null && getMateria() > 0) {
            return getObjetoMateria();
        } else {
            return getObjetoActividad();
        }
    }

    public Materia getObjetoMateria() {
        if (objetoMateria == null) {
            try {
                objetoMateria = Materia.getMateria(getMateria());
            } catch (Exception ex) {
                Logger.getLogger(Horario.class.getName()).log(Level.SEVERE, "Error cargando objeto de materia:" + getMateria(), ex);
            }
        }
        return objetoMateria;
    }

    public Actividad getObjetoActividad() {
        if (objetoActividad == null) {
            try {
                objetoActividad = Actividad.getActividad(getActividad());
            } catch (Exception ex) {
                Logger.getLogger(Horario.class.getName()).log(Level.SEVERE, "Error cargando objeto de actividad:" + getActividad(), ex);
            }
        }
        return objetoActividad;
    }

    public Unidad getObjetoUnidad() {
        if (objetoUnidad == null) {
            try {
                objetoUnidad = Unidad.getUnidad(getUnidad());
            } catch (Exception ex) {
                Logger.getLogger(Horario.class.getName()).log(Level.SEVERE, "Error cargando objeto de unidad:" + getUnidad(), ex);
            }
        }
        return objetoUnidad;
    }

    public Horario() {
    }

    private Horario(int id) throws SQLException {
        PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM horarios WHERE id=?");
        st.setInt(1, id);
        ResultSet res = st.executeQuery();
        if (res.next()) {
            //TODO ver como hacer este método final sin que interfiera con
            //horarioAlumno
            cargarDesdeResultSet(res);
        }
        Obj.cerrar(st, res);
    }

    public AnoEscolar getAnoEscolar() {
        return anoEscolar;
    }

    public void setAnoEscolar(AnoEscolar anoEscolar) {
        this.anoEscolar = anoEscolar;
    }

    public Integer getHora() {
        return hora;
    }

    public void setHora(Integer hora) {
        this.hora = hora;
    }

    public Integer getUnidad() {
        return unidad;
    }

    public void setUnidad(Integer unidad) {
        this.unidad = unidad;
    }

    public Integer getActividad() {
        return actividad;
    }

    public void setActividad(Integer actividad) {
        this.actividad = actividad;
    }

    public Integer getDependencia() {
        return dependencia;
    }

    public void setDependencia(Integer dependencia) {
        this.dependencia = dependencia;
    }

    public Integer getDia() {
        return dia;
    }

    public void setDia(Integer dia) {
        this.dia = dia;
    }

    public Integer getMateria() {
        return materia;
    }

    public void setMateria(Integer materia) {
        this.materia = materia;
    }

    public Integer getProfesor() {
        return profesor;
    }

    public void setProfesor(Integer profesor) {
        this.profesor = profesor;
        this.objetoProfesor = null;
    }

    public Integer getTramo() {
        return tramo;
    }

    public void setTramo(Integer tramo) {
        this.tramo = tramo;
    }

    @Override
    public boolean guardar() {
        boolean ret = false;
        try {
            String sql = "UPDATE horarios SET ano=?,dia=?,tramo_id=?,aula_id=?,materia_id=?,profesor_id=?,actividad_id=?,unidad_id=?,hora=?,dicu=?,activo=? WHERE id=?";
            if (getId() == null) {
                sql = "INSERT INTO horarios (ano,dia,tramo_id,aula_id,materia_id,profesor_id,actividad_id,unidad_id,hora,dicu,activo,id) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
            }
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            st.setObject(1, getAnoEscolar().getId());
            st.setObject(2, getDia());
            st.setObject(3, getTramo());
            st.setObject(4, getDependencia());
            st.setObject(5, getMateria());
            st.setObject(6, getProfesor());
            st.setObject(7, getActividad());
            st.setObject(8, getUnidad());
            st.setObject(9, getHora());
            st.setInt(10, getDicu());
            st.setBoolean(11, isActivo());
            st.setObject(12, getId());
            ret = st.executeUpdate() > 0;
            if (ret && getId() == null) {
                setId((int) st.getLastInsertID());
            }
            st.close();
            ret = true;
        } catch (SQLException ex) {
            Logger.getLogger(Curso.class.getName()).log(Level.SEVERE, "Error guardando datos de Horario: " + this, ex);
        }
        if (ret) {
            Cache.put(getClass(), getId(), this);
        }
        return ret;
    }

    public boolean cargarDesdeResultSet(ResultSet res) {
        boolean ret = false;
        try {
            setId(res.getInt("id"));
            setAnoEscolar(AnoEscolar.getAnoEscolar(res.getInt("ano")));
            setDia(res.getInt("dia"));
            setTramo(res.getInt("tramo_id"));
            setDependencia(res.getInt("aula_id"));
            setMateria(res.getInt("materia_id"));
            setProfesor(res.getInt("profesor_id"));
            setActividad(res.getInt("actividad_id"));
            setUnidad(res.getInt("unidad_id"));
            setHora(res.getInt("hora"));
            setDicu(res.getInt("dicu"));
            setActivo(res.getBoolean("activo"));
            ret = true;
            Cache.put(getClass(), getId(), this);
        } catch (Exception ex) {
            Logger.getLogger(Horario.class.getName()).log(Level.SEVERE, "Error cargando datos de horario desde ResultSet", ex);
        }

        return ret;
    }

    @Override
    public String getNombreObjeto() {
        return "Horario";
    }

    @Override
    public String getDescripcionObjeto() {
        return Str.noNulo(getId());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        sb.append("ID:");
        sb.append(getId());
        sb.append(" AÑO:");
        sb.append(getAnoEscolar());
        sb.append(" DIA:");
        sb.append(getDia());
        sb.append(" TRAMO:");
        sb.append(getTramo());
        sb.append(" HORA:");
        sb.append(getHora());
        sb.append(" DEPENDENCIA:");
        sb.append(getDependencia());
        sb.append(" MATERIA:");
        sb.append(getMateria());
        sb.append(" PROFESOR:");
        sb.append(getProfesor());
        sb.append(" ACTIVIDAD:");
        sb.append(getActividad());
        sb.append(" UNIDAD:");
        sb.append(getUnidad());
        sb.append(" DICU:");
        sb.append(getDicu());
        sb.append(" ACTIVO:");
        sb.append(isActivo());
        sb.append("]");
        return sb.toString();
    }

    public Integer getNumeroDeAlumnos() {
        if (numeroDeAlumnos == null) {
            if (getMateria() != null && getMateria() > 0) {
                numeroDeAlumnos = getNumeroDeAlumnosConMateria();
            } else {
                numeroDeAlumnos = getNumeroDeAlumnosConActividad();
            }
        }
        return numeroDeAlumnos;
    }

    private int getNumeroDeAlumnosConActividad() {
        int ret = 0;
        //Según sea una actividad o una materia hay que calcularlo de una forma u otra
        //Calculamos el número de alumnos
        String sql = "SELECT count(*) FROM alumnos AS a "
                + " JOIN horarios AS h ON h.unidad_id=a.unidad_id   "
                + " JOIN tramos AS t ON t.id=h.tramo_id  "
                + " JOIN alumnos_horarios AS ah ON ah.alumno_id=a.id AND ah.horario_id=h.id "
                + " WHERE a.borrado=0 AND t.id=? AND h.id=? AND ah.activo=1 AND h.activo=1 ";
        PreparedStatement st = null;
        ResultSet res = null;
        try {
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
            st.setInt(1, getTramo());
            st.setInt(2, getId());
            res = st.executeQuery();
            if (res.next()) {
                ret = res.getInt(1);
            }
            Obj.cerrar(st, res);
        } catch (SQLException ex) {
            Logger.getLogger(Horario.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            Obj.cerrar(st, res);
        }
        return ret;
    }

    private int getNumeroDeAlumnosConMateria() {
        int ret = 0;
        //Según sea una actividad o una materia hay que calcularlo de una forma u otra
        //Calculamos el número de alumnos
        String sql = "SELECT count(*) FROM alumnos AS a "
                + " JOIN materias_alumnos AS ma ON ma.alumno_id=a.id "
                + " JOIN horarios AS h ON h.unidad_id=a.unidad_id AND h.materia_id=ma.materia_id  "
                + " JOIN alumnos_horarios AS ah ON ah.alumno_id=a.id AND ah.horario_id=h.id "
                + " WHERE a.borrado=0 AND (h.dicu=" + ParteFaltas.DICU_AMBOS + " OR a.dicu=h.dicu) AND h.id=? AND ah.activo=1 AND h.activo=1 ";
        //TODO DICU Ver tema dicu : AND h.dicu=a.dicu LISTO ELIMINAR SI NO HAY PROBLEMAS
        PreparedStatement st = null;
        ResultSet res = null;
        try {
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
            st.setInt(1, getId());
            res = st.executeQuery();
            if (res.next()) {
                ret = res.getInt(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Horario.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            Obj.cerrar(st, res);
        }
        return ret;
    }

//    public int getNumeroDeAlumnosHorariosComunes() {
//        int ret = 0;
//        //Según sea una actividad o una materia hay que calcularlo de una forma u otra
//        //Calculamos el número de alumnos
//        String sql = "SELECT count(*) FROM alumnos AS a " +
//                " JOIN horarios AS h ON h.unidad_id=a.unidad_id   " +
//                " JOIN alumnos_horarios AS ah ON ah.alumno_id=a.id AND ah.horario_id=h.id " +
//                " WHERE a.borrado=0 AND h.tramo_id=? AND h.profesor_id=? AND ah.activo=1 AND h.activo=1 AND h.borrado=0";
//        PreparedStatement st = null;
//        ResultSet res = null;
//        try {
//            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
//            st.setInt(1, getTramo());
//            st.setInt(2, getProfesor());
//            res = st.executeQuery();
//            if (res.next()) {
//                ret = res.getInt(1);
//            }
//            Obj.cerrar(st, res);
//        } catch (SQLException ex) {
//            Logger.getLogger(Horario.class.getName()).log(Level.SEVERE, null, ex);
//        } finally {
//            Obj.cerrar(st, res);
//        }
//        return ret;
//    }
    public boolean equivalente(Horario h) {
        boolean ret = false;
        //Consideramos dos horarios equivalentes cuando tiene el mismo dia, hora, materia...
        if (Obj.comparar(h.getActividad(), getActividad())) {
            if (Obj.comparar(h.getMateria(), getMateria())) {
                if (Obj.comparar(h.getDia(), getDia())) {
                    if (Obj.comparar(h.getHora(), getHora())) {
                        if (Obj.comparar(h.getDependencia(), h.getDependencia())) {
                            if (Obj.comparar(h.getUnidad(), h.getUnidad())) {
                                if (Obj.comparar(h.getProfesor(), h.getProfesor())) {
                                    ret = true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return ret;
    }

    public boolean equivalenteMultiCurso(Horario h) {
        boolean ret = false;
        //Consideramos dos horarios equivalentes cuando tiene el mismo dia, hora, materia...
        if (h != null) {
            if (getId().equals(h.getId())) {
                ret = true;
            } else if (Obj.comparar(h.getActividad(), getActividad())) {
                if (comparaMaterias(h.getMateria(), getMateria())) {
                    if (Obj.comparar(h.getDia(), getDia())) {
                        if (Obj.comparar(h.getHora(), getHora())) {
                            if (Obj.comparar(h.getDependencia(), getDependencia())) {
                                //if (Obj.comparar(h.getUnidad(), getUnidad())) {
                                if (Obj.comparar(h.getProfesor(), getProfesor())) {
                                    ret = true;
                                }
                                //}
                            }
                        }
                    }
                }
            }
        }
        return ret;
    }

    private boolean comparaMaterias(Integer materiaA, Integer materiaB) {
        boolean ret = Obj.comparar(materiaA, materiaB);
        if (!ret) {
            String nombreA = Materia.getNombreMateria(Num.getInt(materiaA));
            String nombreB = Materia.getNombreMateria(Num.getInt(materiaB));
            ret = nombreA.equals(nombreB);
        }
        return ret;
    }

    @Override
    public boolean equals(Object obj) {
        boolean ret = false;
        if (obj instanceof Horario) {
            Horario h = (Horario) obj;
            if (h.getId() != null && this.getId() != null) {
                ret = this.getId().equals(h.getId());
            } else {
                //Si no lo consideramos igual si tiene los mismos valores
                ret = Obj.comparar(this.getActividad(), h.getActividad());
                ret = ret && Obj.comparar(this.getAnoEscolar(), h.getAnoEscolar());
                ret = ret && Obj.comparar(this.getDependencia(), h.getDependencia());
                ret = ret && Obj.comparar(this.getDia(), h.getDia());
                ret = ret && Obj.comparar(this.getDicu(), h.getDicu());
                ret = ret && Obj.comparar(this.getHora(), h.getHora());
                ret = ret && Obj.comparar(this.getMateria(), h.getMateria());
                ret = ret && Obj.comparar(this.getProfesor(), h.getProfesor());
                ret = ret && Obj.comparar(this.getTramo(), h.getTramo());
                ret = ret && Obj.comparar(this.getUnidad(), h.getUnidad());
            }
        }
        return ret;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 97 * hash + (this.anoEscolar != null ? this.anoEscolar.hashCode() : 0);
        hash = 97 * hash + (this.dia != null ? this.dia.hashCode() : 0);
        hash = 97 * hash + (this.tramo != null ? this.tramo.hashCode() : 0);
        hash = 97 * hash + (this.dependencia != null ? this.dependencia.hashCode() : 0);
        hash = 97 * hash + (this.materia != null ? this.materia.hashCode() : 0);
        hash = 97 * hash + (this.profesor != null ? this.profesor.hashCode() : 0);
        hash = 97 * hash + (this.actividad != null ? this.actividad.hashCode() : 0);
        hash = 97 * hash + (this.unidad != null ? this.unidad.hashCode() : 0);
        hash = 97 * hash + (this.hora != null ? this.hora.hashCode() : 0);
        hash = 97 * hash + (this.numeroDeAlumnos != null ? this.numeroDeAlumnos.hashCode() : 0);
        hash = 97 * hash + (this.objetoMateria != null ? this.objetoMateria.hashCode() : 0);
        return hash;
    }

    public boolean eliminar() {
        boolean borrado = false;
        PreparedStatement st = null;
        try {
            //Lo borramos si no está asociado a ningún parte
            String sqlBorrar = "DELETE horarios FROM horarios "
                    + " LEFT JOIN partes_alumnos ON partes_alumnos.horario_id=horarios.id "
                    + " LEFT JOIN partes_horarios ON partes_horarios.horario_id=horarios.id "
                    + " WHERE partes_alumnos.parte_id IS NULL AND partes_horarios.parte_id IS NULL  AND horarios.id=? ";
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sqlBorrar);
            st.setInt(1, getId());
            borrado = st.executeUpdate() > 0;
            Obj.cerrar(st);
            //Si no se ha borrado porque está asociado a un parte entonces lo marcamos como borrado
            if (!borrado) {
                String sql = "UPDATE horarios SET borrado=1, fborrado=NOW() WHERE id=?";
                st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
                st.setInt(1, getId());
                borrado = st.executeUpdate() > 0;
            }
        } catch (SQLException ex) {
            Logger.getLogger(Horario.class.getName()).log(Level.SEVERE, null, ex);
        }
        Obj.cerrar(st);
        return borrado;
    }

    public void resetearConflictos() {
        conflictos = null;
    }

    public ArrayList<ConflictoHorario> getConflictos() {
        if (conflictos == null) {
            conflictos = new ArrayList<ConflictoHorario>();
            //Primero vemos que haya alumnos matriculados en el horario
            if (getObjetoActividad().getNecesitaMateria()) {
                int alumnos = getNumeroDeAlumnosConMateria();
                if (alumnos == 0) {
                    ConflictoHorario c = new ConflictoHorario(this, ConflictoHorario.NO_HAY_ALUMNOS, "Sin matrículas", "No hay alumnos matriculados en " + getObjetoMateria());
                    conflictos.add(c);
                }
                //Ahora habria que ver que el profesor imparta esa materia pero para eso tendríamos que tener esa información
            }

            //Ahora vemos si el profesor esta impartiendo clase en otro sitio a esta hora/dia
            ArrayList<Horario> solapados = getHorariosSolapadosProfesor();
            if (solapados.size() > 0) {
                //Tenemos que ver que sean en aulas diferentes o en la misma aula con
                //asignaturas no equivalentes
                ArrayList<Horario> solapados2 = new ArrayList<Horario>();
                for (Horario h : solapados) {
                    //Si es distinta aula lo añadimos directamente
                    if (Num.getInt(h.getDependencia()) != Num.getInt(getDependencia())) {
                        solapados2.add(h);
                    } else {
                        //Si es distinta vemos que la actividad sea la misma
                        if (getObjetoActividad().equals(h.getObjetoActividad())) {
                            //Si es la misma actividad vemos que la materia sea equivalente
                            if (getObjetoActividad().getNecesitaMateria()) {
                                if (!getObjetoMateria().esEquivalente(h.getObjetoMateria())) {
                                    solapados2.add(h);
                                }
                            }
                        } else {
                            solapados2.add(h);
                        }
                    }
                }
                if (solapados2.size() > 0) {
                    ConflictoHorario c = new ConflictoHorario(this, ConflictoHorario.SOLAPADO_PROFESOR, "Profesor solapado", "El profesor esta dando otra materia o en otra aula en este dia/hora.");
                    c.getHorarios().addAll(solapados2);
                    conflictos.add(c);
                }
            }

            //Ahora vemos si se está impartiendo clase en el aula por otro profesor
            solapados = getHorariosSolapadosAula();
            if (solapados.size() > 0) {
                ConflictoHorario c = new ConflictoHorario(this, ConflictoHorario.SOLAPADO_AULA, "Aula solapada", "Se está impartiendo clase por otro profesor en la misma aula.");
                c.getHorarios().addAll(solapados);
                conflictos.add(c);
            }

            //Ahora vemos si algún alumno esta dando clase en otro sitio
            solapados = getHorariosSolapadosAlumno();
            if (solapados.size() > 0) {
                ConflictoHorario c = new ConflictoHorario(this, ConflictoHorario.SOLAPADO_ALUMNO, "Asignatura solapada", "Algún alumno tiene solapadas dos o más asignaturas.");
                c.getHorarios().addAll(solapados);
                conflictos.add(c);
            }
        }
        return conflictos;
    }

    public ArrayList<Horario> getHorariosSolapadosAlumno() {
        ArrayList<Horario> horarios = new ArrayList<Horario>();
        String sql = "SELECT distinct GROUP_CONCAT(h.id) FROM horarios_ AS h "
                + " JOIN alumnos_horarios AS ah ON ah.horario_id=h.id "
                + " JOIN alumnos AS a ON a.id=ah.alumno_id "
                + " WHERE a.ano=? AND a.borrado=0 AND h.activo=1 AND ah.activo=1 AND h.unidad_id=? AND h.hora=? AND h.dia=? "
                + " GROUP BY dia,hora,h.dicu,alumno_id "
                + " HAVING count(*)>1";
        PreparedStatement st = null;
        ResultSet res = null;
        try {
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
            st.setInt(1, getAnoEscolar().getId());
            st.setInt(2, getUnidad());
            st.setInt(3, getHora());
            st.setInt(4, getDia());
            res = st.executeQuery();
            while (res.next()) {
                //Obtenemos una cadena con las ids en conflicto
                String c = res.getString(1);
                String[] sIds = c.split(",");
                for (String sId : sIds) {
                    int idH = Num.getInt(sId);
                    if (idH != 0 && idH != getId()) {
                        Horario h = new Horario(idH);
                        if (!horarios.contains(h)) {
                            horarios.add(h);
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Horario.class.getName()).log(Level.SEVERE, null, ex);
        }
        return horarios;
    }

    public ArrayList<Horario> getHorariosSolapadosAula() {
        ArrayList<Horario> horarios = new ArrayList<Horario>();
        String sql = "SELECT * FROM horarios_ WHERE ano=? AND profesor_id!=? AND hora=? AND dia=? AND id!=? AND aula_id=?";
        PreparedStatement st = null;
        ResultSet res = null;
        try {
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
            st.setInt(1, getAnoEscolar().getId());
            st.setInt(2, getProfesor());
            st.setInt(3, getHora());
            st.setInt(4, getDia());
            st.setInt(5, getId());
            st.setInt(6, getDependencia());
            res = st.executeQuery();
            while (res.next()) {
                Horario h = new Horario();
                h.cargarDesdeResultSet(res);
                horarios.add(h);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Horario.class.getName()).log(Level.SEVERE, null, ex);
        }
        return horarios;
    }

    public ArrayList<Horario> getHorariosSolapadosProfesor() {
        ArrayList<Horario> horarios = new ArrayList<Horario>();
        String sql = "SELECT * FROM horarios_ WHERE ano=? AND profesor_id=? AND hora=? AND dia=? AND id!=?";
        PreparedStatement st = null;
        ResultSet res = null;
        try {
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
            st.setInt(1, getAnoEscolar().getId());
            st.setInt(2, getProfesor());
            st.setInt(3, getHora());
            st.setInt(4, getDia());
            st.setInt(5, getId());
            res = st.executeQuery();
            while (res.next()) {
                Horario h = new Horario();
                h.cargarDesdeResultSet(res);
                horarios.add(h);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Horario.class.getName()).log(Level.SEVERE, null, ex);
        }
        return horarios;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String getTabla() {
        return "horarios";
    }

    public static ArrayList<Horario> getHorarios(Profesor p) {
        ArrayList<Horario> horarios = new ArrayList<Horario>();
        PreparedStatement st = null;
        ResultSet res = null;
        try {
            st = Conector.prepareSt("SELECT * FROM horarios_ AS h WHERE profesor_id=?");
            st.setInt(1, p.getId());
            res = st.executeQuery();
            while (res.next()) {
                Horario h = new Horario();
                h.cargarDesdeResultSet(res);
                horarios.add(h);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Horario.class.getName()).log(Level.SEVERE, null, ex);
        }
        Obj.cerrar(st);
        return horarios;
    }

    public static ArrayList<BloqueHorario> getHorarios(Alumno alumno, Profesor profesor, Unidad unidad, Integer filtroDia, Integer filtroHora) {
        ArrayList<BloqueHorario> horarios = new ArrayList<BloqueHorario>();
        //Si se está filtrando por alumno si nos insteresa saber si es apoyo, si no no nos interesa
        String sql = "SELECT distinct h.dia,h.hora,h.profesor_id,h.actividad_id,h.aula_id,h.activo,h.dicu ";
        if (alumno != null) {
            sql += " ,IF(aa.alumno_id IS NULL,false,true) AS apoyo,ah.activo AS activoAlumno ";
        } else {
            sql += " ,false AS apoyo,false AS activoAlumno ";
        }
        sql += " , GROUP_CONCAT(distinct h.materia_id) AS materias,GROUP_CONCAT(distinct h.unidad_id) AS unidades FROM horarios_ AS h ";// +

        if (alumno != null) {
            sql += " LEFT JOIN alumnos_horarios AS ah ON ah.activo=1 AND ah.horario_id=h.id "
                    + " LEFT JOIN alumnos AS a ON a.id=ah.alumno_id AND (h.dicu=" + ParteFaltas.DICU_AMBOS + " OR a.dicu=h.dicu)"
                    + " LEFT JOIN apoyos_alumnos AS aa ON a.id=aa.alumno_id AND aa.horario_id=h.id ";
            //TODO DICU Ver tema del dicu: AND a.dicu=h.dicu. LISTO ELIMINAR SI NO HAY PROBLEMAS
        }
        sql += " LEFT JOIN materias AS m ON m.id=h.materia_id ";
        sql += " WHERE h.ano=? ";
        if (alumno != null) {
            sql += " AND ah.alumno_id=? ";
            Logger.getLogger(Horario.class.getName()).log(Level.INFO, "Alumno: {0}", alumno.getId());
        }
        if (profesor != null) {
            sql += " AND h.profesor_id=? ";
            Logger.getLogger(Horario.class.getName()).log(Level.INFO, "Profesor: {0}", profesor.getId());
        }
        if (filtroDia != null) {
            sql += " AND h.dia=? ";
            Logger.getLogger(Horario.class.getName()).log(Level.INFO, "Día: {0}", filtroDia);
        }
        if (filtroHora != null) {
            sql += " AND h.hora=? ";
            Logger.getLogger(Horario.class.getName()).log(Level.INFO, "Hora: {0}", filtroHora);
        }
        sql += " GROUP BY dia,hora,aula_id,profesor_id,actividad_id,m.nombre,h.activo,h.dicu";

        if (unidad != null) {
            sql += " HAVING find_in_set(?, group_concat(h.unidad_id)) ";
            Logger.getLogger(Horario.class.getName()).log(Level.INFO, "Unidad: {0}", unidad.getId());
        }
        sql += " ORDER BY h.dia,h.hora ";
        Logger.getLogger(Horario.class.getName()).info(sql);
        try {
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
            st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
            int pos = 2;
            if (alumno != null) {
                st.setInt(pos, alumno.getId());
                pos++;
            }
            if (profesor != null) {
                st.setInt(pos, profesor.getId());
                pos++;
            }
            if (unidad != null) {
                st.setInt(pos, unidad.getId());
                pos++;
            }
            if (filtroDia != null) {
                st.setInt(pos, filtroDia);
                pos++;
            }
            if (filtroHora != null) {
                st.setInt(pos, filtroHora);
                pos++;
            }
            ResultSet res = st.executeQuery();
            while (res.next()) {
                int dia = res.getInt("h.dia");
                int hora = res.getInt("h.hora");
                int profesorId = res.getInt("h.profesor_id");
                int actividadId = res.getInt("h.actividad_id");
                int aulaId = res.getInt("h.aula_id");
                BloqueHorario bloque = new BloqueHorario(MaimonidesApp.getApplication().getAnoEscolar(), dia, hora, actividadId, profesorId, aulaId);
                bloque.setDicu(res.getInt("h.dicu"));
                String sMat = "";
                try {
                    Blob b = res.getBlob("materias");
                    if (b != null) {
                        sMat = Str.leer(b.getBinaryStream(), "utf-8");
                    }
                } catch (Exception ex) {
                    Logger.getLogger(Horario.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (sMat != null && !sMat.equals("")) {
                    String[] sMats = sMat.split(",");
                    for (String sIdM : sMats) {
                        try {
                            int idMateria = Num.getInt(sIdM);
                            if (idMateria > 0) {
                                Materia m = Materia.getMateria(idMateria);
                                bloque.addMateria(m);
                            }
                        } catch (Exception e) {
                            Logger.getLogger(Horario.class.getName()).log(Level.SEVERE, null, e);
                        }
                    }
                }
                String sUd = "";
                try {
                    sUd = Str.leer(res.getBlob("unidades").getBinaryStream(), "utf-8");
                } catch (IOException ex) {
                    Logger.getLogger(Horario.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (sUd != null) {
                    String[] sUds = sUd.split(",");
                    for (String sIdU : sUds) {
                        try {
                            Unidad m = Unidad.getUnidad(Num.getInt(sIdU));
                            bloque.addUnidad(m);
                        } catch (Exception e) {
                            Logger.getLogger(Horario.class.getName()).log(Level.SEVERE, null, e);
                        }
                    }
                }
                bloque.setActivo(res.getBoolean("activo"));
                if (alumno != null) {
                    bloque.setActivo(bloque.isActivo() && res.getBoolean("activoAlumno"));
                }
                horarios.add(bloque);
            }
        } catch (Exception ex) {
            Logger.getLogger(Horario.class.getName()).log(Level.SEVERE, null, ex);
        }
        return horarios;
    }
}
