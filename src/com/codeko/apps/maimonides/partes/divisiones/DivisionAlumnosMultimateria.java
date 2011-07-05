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


package com.codeko.apps.maimonides.partes.divisiones;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesUtil;
import com.codeko.apps.maimonides.elementos.Actividad;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.Materia;
import com.codeko.apps.maimonides.elementos.Unidad;
import com.codeko.util.Obj;
import com.codeko.util.Str;
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
public class DivisionAlumnosMultimateria {

    int dia = 0;
    int hora = 0;
    String curso = null;
    Actividad actividad = null;
    ArrayList<LineaDivisionAlumno> lineas = null;
    ArrayList<Integer> profesores = new ArrayList<Integer>();
    ArrayList<Integer> unidades = new ArrayList<Integer>();
    ArrayList<Unidad> objetosUnidad = new ArrayList<Unidad>();
    ArrayList<Materia> materias = new ArrayList<Materia>();
    boolean multimateria = false;
    LineaDivisionAlumno lineaModelo = null;

    public DivisionAlumnosMultimateria(int dia, int hora, String curso) {
        setDia(dia);
        setHora(hora);
        setCurso(curso);
    }

    /**
     * Para evitar problemas de orden todas las lineas tienen que tener el orden de los horarios de la linea que se usa como modelo
     * @param l
     */
    public void setLineaModelo(LineaDivisionAlumno lineaModelo) {
        this.lineaModelo = lineaModelo;
        //Procedemos a ordenar los horarios de todos las lineas para que se
        //correspondan con la del modelo
        for (LineaDivisionAlumno lineaActual : getLineas()) {
            if (lineaActual != lineaModelo) {
                //Primero hacemos que tenga el mismo tamaño
                while (lineaActual.getHorarios().size() < lineaModelo.getHorarios().size()) {
                    lineaActual.getHorarios().add(null);
                }
                ArrayList<HorarioAlumno> nuevoOrden = new ArrayList<HorarioAlumno>();
                for (int i = 0; i < lineaModelo.getHorarios().size(); i++) {
                    HorarioAlumno horarioModelo = lineaModelo.getHorarios().get(i);
                    //Ahora buscamos ese mismo horario en la linea
                    HorarioAlumno horarioEquivalente = null;
                    for (HorarioAlumno h : lineaActual.getHorarios()) {
                        if (h != null && h.equivalenteMultiCurso(horarioModelo)) {
                            horarioEquivalente = h;
                            break;
                        }
                    }
                    nuevoOrden.add(horarioEquivalente);
//                    if (horarioEquivalente != null) {
//                        //Y lo colocamos en la posición correcta
//                        //lineaActual.getHorarios().removeElement(horarioEquivalente);
//                        //lineaActual.getHorarios().add(i, horarioEquivalente);
//                    }
                }
                lineaActual.getHorarios().clear();
                lineaActual.getHorarios().addAll(nuevoOrden);
            }
        }
    }

    public LineaDivisionAlumno getLineaModelo() {
        return lineaModelo;
    }

    public ArrayList<Integer> getUnidades() {
        return unidades;
    }

    public ArrayList<Unidad> getObjetosUnidad() {
        return objetosUnidad;
    }

    public void addUnidad(int idUnidad) {
        if (!getUnidades().contains(idUnidad)) {
            getUnidades().add(idUnidad);
            try {
                getObjetosUnidad().add(Unidad.getUnidad(idUnidad));
            } catch (Exception ex) {
                Logger.getLogger(DivisionAlumnosMultimateria.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void addUnidades(Collection<Integer> idUnidades) {
        for (Integer id : idUnidades) {
            if (!getUnidades().contains(id)) {
                getUnidades().add(id);
            }
        }
    }

    public ArrayList<Integer> getProfesores() {
        return profesores;
    }

    public void addProfesor(Integer id) {
        if (!getProfesores().contains(id)) {
            getProfesores().add(id);
        }
    }

    public void addProfesores(Collection<Integer> idProfesores) {
        for (Integer id : idProfesores) {
            if (!getProfesores().contains(id)) {
                getProfesores().add(id);
            }
        }
    }

    public boolean isMultimateria() {
        return multimateria;
    }

    public void setMultimateria(boolean multimateria) {
        this.multimateria = multimateria;
    }

    public ArrayList<Materia> getMaterias() {
        return materias;
    }

    public void addMaterias(Collection<Materia> materias) {
        for (Materia m : materias) {
            if (!getMaterias().contains(m)) {
                getMaterias().add(m);
            }
        }
    }

    public void addMateria(Materia m) {
        if (!getMaterias().contains(m)) {
            getMaterias().add(m);
        }
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

    public Actividad getActividad() {
        return actividad;
    }

    public void setActividad(Actividad actividad) {
        this.actividad = actividad;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(MaimonidesUtil.getNombreDiaSemana(getDia(), false));
        sb.append(" ");
        sb.append(getHora());
        sb.append("ª");
        sb.append(" ");
        if (getMaterias().size() > 0) {
            sb.append(getMaterias());
        } else {
            sb.append("[");
            sb.append(getActividad());
            sb.append("]");
        }
        sb.append(getObjetosUnidad());
        return sb.toString();
    }

    public String toStringExtendido() {
        StringBuilder sb = new StringBuilder();
        sb.append(MaimonidesUtil.getNombreDiaSemana(getDia(), true));
        sb.append(" ");
        sb.append(getHora());
        sb.append("ª Hora: ");
        if (getMaterias().size() > 0) {
            sb.append(getMaterias());
        } else {
            sb.append("[");
            sb.append(getActividad());
            sb.append("]");
        }
        sb.append(getObjetosUnidad());
        return sb.toString();
    }

    public ArrayList<LineaDivisionAlumno> getLineas() {
        if (lineas == null) {
            lineas = new ArrayList<LineaDivisionAlumno>();
            String sUnidades = Str.implode(getUnidades(), ",", "0");
            ArrayList<Integer> idMaterias = new ArrayList<Integer>();
            for (Materia m : getMaterias()) {
                idMaterias.add(m.getId());
            }
            String sMaterias = Str.implode(idMaterias, ",", "0");
            String sql = "SELECT h.*,a.id AS alumno_id FROM alumnos_horarios AS ah "
                    + " JOIN alumnos AS a ON a.id=ah.alumno_id "
                    + " JOIN horarios_ AS h ON h.id=ah.horario_id AND a.unidad_id=h.unidad_id"
                    + " JOIN materias AS m ON h.materia_id=m.id AND m.id=h.materia_id "
                    + " JOIN materias_alumnos AS ma ON ma.alumno_id=a.id AND ma.materia_id=m.id "
                    + " JOIN unidades AS u ON u.id=a.unidad_id "
                    + " WHERE a.borrado=0 AND h.ano=? AND h.dia=? AND h.hora=? AND u.curso=? AND h.unidad_id IN (" + sUnidades + ") AND m.id IN (" + sMaterias + ")"
                    + " ORDER BY a.unidad_id," + Alumno.getCampoOrdenNombre("a") + ",h.profesor_id";
            if (getMaterias().isEmpty()) {
                sql = "SELECT h.*,a.id AS alumno_id FROM alumnos_horarios AS ah "
                        + " JOIN alumnos AS a ON a.id=ah.alumno_id "
                        + " JOIN horarios_ AS h ON h.id=ah.horario_id AND a.unidad_id=h.unidad_id"
                        + " JOIN unidades AS u ON u.id=a.unidad_id "
                        + " WHERE a.borrado=0 AND h.ano=? AND h.dia=? AND h.hora=? AND u.curso=? AND h.unidad_id IN (" + sUnidades + ") AND h.actividad_id=" + getActividad().getId() + ""
                        + " ORDER BY a.unidad_id," + Alumno.getCampoOrdenNombre("a") + ",h.profesor_id";
            }
            try {
                PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
                st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
                st.setInt(2, getDia());
                st.setInt(3, getHora());
                st.setString(4, getCurso());
                ResultSet res = st.executeQuery();
                ArrayList<HorarioAlumno> horarios = new ArrayList<HorarioAlumno>();
                int ultimoAlumno = -1;
                while (res.next()) {
                    HorarioAlumno h = new HorarioAlumno();
                    h.cargarDesdeResultSet(res);
                    if (h.getAlumno().getId() != ultimoAlumno && ultimoAlumno != -1) {
                        //if (!isMultimateria() ){//|| horarios.size() > 1) { //En el caso de los de un curso diferente el horarios.size es uno pero se mezclan con los otros
                        LineaDivisionAlumno lin = new LineaDivisionAlumno(horarios, horarios.get(horarios.size() - 1).getAlumno());
                        lin.setDivision(this);
                        lineas.add(lin);
                        horarios.clear();
                    }
                    ultimoAlumno = h.getAlumno().getId();
                    horarios.add(h);
                }
                Obj.cerrar(st, res);
                if (horarios.size() > 1) {
                    LineaDivisionAlumno lin = new LineaDivisionAlumno(horarios, horarios.get(horarios.size() - 1).getAlumno());
                    lin.setDivision(this);
                    lineas.add(lin);
                    horarios.clear();
                }
            } catch (SQLException ex) {
                Logger.getLogger(DivisionAlumnosMultimateria.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        return lineas;
    }

    public void resetearLineas() {
        lineas = null;
    }

    public boolean isIntegrable(DivisionAlumnosMultimateria da, boolean comprobarProfesores, boolean comprobarMaterias) {
        boolean integrable = false;
        //Dos unidades son integrables si
        if (getDia() == da.getDia() && getHora() == da.getHora() && getCurso().equals(da.getCurso())) {
            integrable = true;
            //Además los profesores deben ser los mismos
            if (comprobarProfesores) {
                integrable = integrable && getProfesores().containsAll(da.getProfesores());
            }
            if (comprobarMaterias) {
                integrable = integrable && getMaterias().containsAll(da.getMaterias());
            }
        }
        return integrable;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + this.dia;
        hash = 89 * hash + this.hora;
        hash = 89 * hash + (this.curso != null ? this.curso.hashCode() : 0);
        hash = 89 * hash + (this.materias != null ? this.materias.hashCode() : 0);
        hash = 89 * hash + (this.unidades != null ? this.unidades.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DivisionAlumnosMultimateria other = (DivisionAlumnosMultimateria) obj;
        if (this.dia != other.dia) {
            return false;
        }
        if (this.hora != other.hora) {
            return false;
        }
        if ((this.curso == null) ? (other.curso != null) : !this.curso.equals(other.curso)) {
            return false;
        }
        if (this.actividad != other.actividad && (this.actividad == null || !this.actividad.equals(other.actividad))) {
            return false;
        }
        if (this.lineas != other.lineas && (this.lineas == null || !this.lineas.equals(other.lineas))) {
            return false;
        }
        if (this.profesores != other.profesores && (this.profesores == null || !this.profesores.equals(other.profesores))) {
            return false;
        }
        if (this.unidades != other.unidades && (this.unidades == null || !this.unidades.equals(other.unidades))) {
            return false;
        }
        if (this.objetosUnidad != other.objetosUnidad && (this.objetosUnidad == null || !this.objetosUnidad.equals(other.objetosUnidad))) {
            return false;
        }
        if (this.materias != other.materias && (this.materias == null || !this.materias.equals(other.materias))) {
            return false;
        }
        return true;
    }
}
