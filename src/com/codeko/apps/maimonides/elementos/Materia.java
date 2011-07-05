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

import com.codeko.apps.maimonides.*;
import com.codeko.apps.maimonides.cache.Cache;
import com.codeko.swing.CdkAutoTablaCol;
import com.codeko.util.Obj;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.Statement;
import java.security.InvalidParameterException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Codeko
 */
public class Materia extends ObjetoBDConCod {

    @CdkAutoTablaCol(ignorar = true)
    public static final int MAXIMO_ALUMNOS = 33;
    @CdkAutoTablaCol(titulo = "Código")
    Integer codigo = 0;
    Curso curso = null;
    @CdkAutoTablaCol(titulo = "Abreviatura")
    String codigoMateria = null;
    @CdkAutoTablaCol(titulo = "Descripción")
    String descripcion = "";
    @CdkAutoTablaCol(ignorar = true)
    AnoEscolar anoEscolar = MaimonidesApp.getApplication().getAnoEscolar();
    Boolean evaluable = true;
    @CdkAutoTablaCol(titulo = "Capacidad")
    Integer maximoAlumnos = null;
    @CdkAutoTablaCol(titulo = "Max. Faltas")
    Integer maxFaltas = 0;

    public static Materia getMateria(int id) throws Exception {
        Object obj = Cache.get(Materia.class, id);
        if (obj != null) {
            return (Materia) obj;
        } else {
            Materia p = new Materia(id);
            return p;
        }
    }

    public static Materia getMateria(AnoEscolar ano, int codigo) {
        Materia t = null;
        try {
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM materias WHERE ano=? AND cod=?");
            st.setInt(1, ano.getId());
            st.setInt(2, codigo);
            ResultSet res = st.executeQuery();
            if (res.next()) {
                t = new Materia();
                t.cargarDesdeResultSet(res);
            }
            Obj.cerrar(st, res);
        } catch (Exception ex) {
            Logger.getLogger(Materia.class.getName()).log(Level.SEVERE, null, ex);
        }
        return t;
    }

    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }

    public int getMaxFaltas() {
        return maxFaltas;
    }

    public void setMaxFaltas(Integer maxFaltas) {
        this.maxFaltas = maxFaltas;
    }

    public Integer getMaximoAlumnos() {
        if (maximoAlumnos == null) {
            maximoAlumnos = MAXIMO_ALUMNOS;
        }
        return maximoAlumnos;
    }

    public void setMaximoAlumnos(Integer maximoAlumnos) {
        this.maximoAlumnos = maximoAlumnos;
    }

    public Boolean isEvaluable() {
        return evaluable;
    }

    public void setEvaluable(Boolean evaluable) {
        this.evaluable = evaluable;
    }

    @Override
    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public String getCodigoMateria() {
        if (codigoMateria == null || codigoMateria.equals("")) {
            codigoMateria = MaimonidesUtil.getCodigoAbreviado(getDescripcion());
        }
        return codigoMateria;
    }

    public void setCodigoMateria(String codigo) {
        this.codigoMateria = codigo;
    }

    public AnoEscolar getAnoEscolar() {
        return anoEscolar;
    }

    public void setAnoEscolar(AnoEscolar anoEscolar) {
        this.anoEscolar = anoEscolar;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Materia() {
    }

    public Materia(AnoEscolar anoEscolar, int codigo) throws SQLException, Exception {
        PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM materias WHERE ano=? AND cod=? ");
        st.setInt(1, anoEscolar.getId());
        st.setInt(2, codigo);
        ResultSet res = st.executeQuery();
        if (res.next()) {
            cargarDesdeResultSet(res);
            Obj.cerrar(st, res);
        } else {
            Obj.cerrar(st, res);
            throw new InvalidParameterException("No existe ninguna materia con Código " + codigo + " para el año:" + anoEscolar);
        }
    }

    private Materia(int id) throws Exception {
        PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM materias WHERE id=? ");
        st.setInt(1, id);
        ResultSet res = st.executeQuery();
        if (res.next()) {
            cargarDesdeResultSet(res);
            Obj.cerrar(st, res);
        } else {
            Obj.cerrar(st, res);
            throw new InvalidParameterException("No existe ninguna materia con ID " + id);
        }
    }

    public final boolean cargarDesdeResultSet(ResultSet res) {
        boolean ret = true;
        try {
            int idAno = res.getInt("ano");
            setId(res.getInt("id"));
            setCodigo(res.getInt("cod"));
            setDescripcion(res.getString("nombre"));
            setCurso(Curso.getCurso(res.getInt("curso_id")));
            setAnoEscolar(AnoEscolar.getAnoEscolar(idAno));
            setCodigoMateria(res.getString("codigo_materia"));
            setEvaluable(res.getBoolean("evaluable"));
            setMaximoAlumnos(res.getInt("maximo_alumnos"));
            setMaxFaltas(res.getInt("maxFaltas"));
            Cache.put(getClass(), getId(), this);
        } catch (Exception ex) {
            ret = false;
            Logger.getLogger(Materia.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    @Override
    public boolean _guardar(boolean crear) {
        boolean ret = false;
        try {
            String sql = "UPDATE materias SET cod=?,ano=?,codigo_materia=?,nombre=?,curso_id=?,evaluable=?,maximo_alumnos=?,maxFaltas=? WHERE id=?";
            if (getId() == null) {
                sql = "INSERT INTO materias (cod,ano,codigo_materia,nombre,curso_id,evaluable,maximo_alumnos,maxFaltas,id) VALUES (?,?,?,?,?,?,?,?,?)";
            }
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            st.setObject(1, getCodigo());
            st.setInt(2, getAnoEscolar().getId());
            st.setString(3, getCodigoMateria());
            st.setString(4, getDescripcion());
            Integer idCurso = null;
            if (getCurso() != null) {
                idCurso = getCurso().getId();
            }
            st.setObject(5, idCurso);
            st.setBoolean(6, isEvaluable());
            st.setInt(7, getMaximoAlumnos());
            st.setInt(8, getMaxFaltas());
            st.setObject(9, getId());
            ret = st.executeUpdate() > 0;
            if (ret && getId() == null) {
                setId((int) st.getLastInsertID());
            }
            st.close();
        } catch (SQLException ex) {
            ret = false;
            Logger.getLogger(Materia.class.getName()).log(Level.SEVERE, "Error guardando datos de Materia: " + this, ex);
        }
        if (ret) {
            Cache.put(getClass(), getId(), this);
        }
        return ret;
    }

    @Override
    public String getNombreObjeto() {
        return "Materia";
    }

    @Override
    public String getDescripcionObjeto() {
        return getDescripcion();
    }

    public static Materia confirmarExistencia(int codigo, String nombre, Alumno a) {
        Materia ret = null;
        try {
            ret = new Materia(a.getAnoEscolar(), codigo);

        } catch (Exception ex) {
            Logger.getLogger(Materia.class.getName()).log(Level.WARNING, "Se est\u00e1 creando nueva materia {0} ({1}) para {2} en el a\u00f1o: {3}", new Object[]{nombre, codigo, a.getUnidad(), a.getAnoEscolar()});
            //Si da error es que no existe así que la creamos
            Materia m = new Materia();
            m.setCodigo(codigo);
            m.setDescripcion(nombre);
            m.setAnoEscolar(a.getAnoEscolar());
            try {
                Curso curso = Curso.getCurso(a.getIdCurso());
                m.setCurso(curso);
                if (m.guardar()) {
                    ret = m;
                }
                curso = null;
            } catch (Exception ex1) {
                Logger.getLogger(Materia.class.getName()).log(Level.SEVERE, "No existe curso " + a.getIdCurso(), ex1);
            }
        }
        return ret;
    }

    public static String getNombreMateria(int idMateria) {
        String nombre = "No existe materia.";
        try {
            Materia m = new Materia(idMateria);
            nombre = m.getDescripcion();
        } catch (Exception ex) {
            Logger.getLogger(Materia.class.getName()).log(Level.SEVERE, "Error recuperando descripcion de materia " + idMateria, ex);
        }
        return nombre;
    }

    public static String getCodigoMateria(int idMateria) {
        String cod = "-";
        try {
            Materia m = new Materia(idMateria);
            cod = m.getCodigoMateria();
        } catch (Exception ex) {
            Logger.getLogger(Materia.class.getName()).log(Level.SEVERE, "Error recuperando codigo_materia de materia " + idMateria, ex);
        }
        return cod;
    }

    @Override
    public String toString() {
        return getDescripcion();
    }

    @Override
    public boolean equals(Object obj) {
        boolean ret = false;
        if (obj instanceof Materia) {
            Materia m = (Materia) obj;
            if (m.getId() != null && getId() != null) {
                ret = m.getId().equals(getId());
            } else {
                try {
                    ret = (m.getCodigo().equals(getCodigo()) && getAnoEscolar().getId().equals(m.getAnoEscolar().getId()));
                } catch (Exception e) {
                }
            }
        } else {
            ret = super.equals(obj);
        }
        return ret;
    }

    public boolean esEquivalente(Object obj) {
        boolean equivalente = false;
        if (obj instanceof Materia) {
            Materia m = (Materia) obj;
            //TODO No se si con esto es suficiente
            //TODO habría que asegurarse de que se usa sólo cuando profesor y unidad es la misma
            if (m != null) {
                if (m.getDescripcion().equals(getDescripcion())) {
                    equivalente = true;
                }
            }
        }
        return equivalente;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + (this.codigo != null ? this.codigo.hashCode() : 0);
        hash = 83 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 83 * hash + (this.curso != null ? this.curso.hashCode() : 0);
        hash = 83 * hash + (this.descripcion != null ? this.descripcion.hashCode() : 0);
        hash = 83 * hash + (this.anoEscolar != null ? this.anoEscolar.hashCode() : 0);
        hash = 83 * hash + (this.codigoMateria != null ? this.codigoMateria.hashCode() : 0);
        hash = 83 * hash + (this.evaluable ? 1 : 0);
        hash = 83 * hash + (this.maximoAlumnos != null ? this.maximoAlumnos.hashCode() : 0);
        hash = 83 * hash + (this.curso != null ? this.curso.hashCode() : 0);
        return hash;
    }

    @Override
    public String getTabla() {
        return "materias";
    }

    public static ArrayList<Materia> getMaterias() {
        ArrayList<Materia> materias = new ArrayList<Materia>();
        try {
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM materias WHERE ano=? ");
            st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
            ResultSet res = st.executeQuery();
            while (res.next()) {
                Materia m = new Materia();
                if (m.cargarDesdeResultSet(res)) {
                    materias.add(m);
                }
            }
            Obj.cerrar(st, res);
        } catch (SQLException ex) {
            Logger.getLogger(Unidad.class.getName()).log(Level.SEVERE, null, ex);
        }
        return materias;
    }
}
