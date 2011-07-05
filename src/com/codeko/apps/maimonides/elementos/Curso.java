package com.codeko.apps.maimonides.elementos;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.cache.Cache;
import com.codeko.swing.CdkAutoTablaCol;
import com.codeko.util.Fechas;
import com.codeko.util.Obj;
import com.codeko.util.Str;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.Statement;
import java.beans.Beans;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Codeko
 */
//TODO El curso tiene 2 descripciones el de la exportación de faltas y la de para generadores de horarios. Parece que nos interesa el 2 no el primero (o los dos mejor).
public class Curso extends ObjetoBDConCod {

    @CdkAutoTablaCol(titulo = "Código")
    Integer codigo = null;
    @CdkAutoTablaCol(titulo = "Curso")
    String curso = null;
    @CdkAutoTablaCol(titulo = "Descripción")
    String descripcion = null;
    @CdkAutoTablaCol(ignorar = true)
    AnoEscolar anoEscolar = null;
    @CdkAutoTablaCol(ignorar = true)
    ArrayList<Unidad> unidades = null;
    @CdkAutoTablaCol(titulo = "Nº Alumnos")
    Integer numeroDeAlumnos = null;
    @CdkAutoTablaCol(titulo = "F. Inicio")
    GregorianCalendar fechaInicio = null;
    @CdkAutoTablaCol(titulo = "F. Fin")
    GregorianCalendar fechaFin = null;
    @CdkAutoTablaCol(ignorar = true)
    Boolean mixto = null;
    @CdkAutoTablaCol(titulo = "Max. Faltas")
    Integer maxFaltas = 0;
    @CdkAutoTablaCol(titulo = "Posición")
    Integer posicion = 0;
    @CdkAutoTablaCol(ignorar = true)
    ArrayList<Alumno> alumnos = new ArrayList<Alumno>();

    public static Curso getCurso(int id) throws Exception {
        Object obj = Cache.get(Curso.class, id);
        if (obj != null) {
            return (Curso) obj;
        } else {
            Curso h = new Curso(id);
            return h;
        }
    }

    public Curso(AnoEscolar ano, String nombreCurso) throws Exception {
        //TODO Implementar cache en este constructor
        PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM cursos WHERE ano=? AND descripcion=?");
        st.setInt(1, ano.getId());
        st.setString(2, nombreCurso);
        ResultSet res = st.executeQuery();
        if (res.next()) {
            cargarDesdeResultSet(res);
        } else {
            throw new Exception("No existe ningún curso con nombre '" + nombreCurso + "'");
        }
        Obj.cerrar(st, res);
    }

    public static ArrayList<Curso> getCursos() {
        ArrayList<Curso> cursos = new ArrayList<Curso>();
        PreparedStatement st = null;
        ResultSet res = null;
        try {
            if (!Beans.isDesignTime()) {
                st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM cursos WHERE ano=? ORDER BY posicion ");
                st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
                res = st.executeQuery();
                while (res.next()) {
                    try {
                        Curso c = new Curso(res);
                        cursos.add(c);
                    } catch (Exception ex) {
                        Logger.getLogger(Curso.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Curso.class.getName()).log(Level.SEVERE, null, ex);
        }
        Obj.cerrar(st, res);
        return cursos;
    }

    public ArrayList<Materia> getMaterias() {
        ArrayList<Materia> materias = new ArrayList<Materia>();
        PreparedStatement st = null;
        ResultSet res = null;
        try {
            String sql = "select distinct m.* FROM materias AS m "
                    + " JOIN materias_alumnos AS ma ON m.id=ma.materia_id "
                    + " WHERE m.ano=? AND m.curso_id=? AND m.evaluable>0 AND m.cod>0 ";
            //"SELECT * FROM materias WHERE ano=? AND curso_id=? AND evaluable>0 AND cod>0 "
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
            st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
            st.setInt(2, getId());
            res = st.executeQuery();
            while (res.next()) {
                try {
                    Materia m = new Materia();
                    m.cargarDesdeResultSet(res);
                    materias.add(m);
                } catch (Exception ex) {
                    Logger.getLogger(Curso.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Curso.class.getName()).log(Level.SEVERE, null, ex);
        }
        Obj.cerrar(st, res);
        return materias;
    }

    public Integer getMaxFaltas() {
        return maxFaltas;
    }

    public void setMaxFaltas(Integer maxFaltas) {
        this.maxFaltas = maxFaltas;
    }

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    public GregorianCalendar getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(GregorianCalendar fechaFin) {
        this.fechaFin = fechaFin;
    }

    public GregorianCalendar getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(GregorianCalendar fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Boolean isMixto() {
        if (mixto == null) {
            mixto = false;
            if (getId() != null) {
                for (Unidad u : getUnidades()) {
                    if (u.getIdCurso2() != null) {
                        mixto = true;
                        break;
                    }
                }
            }
        }
        return mixto;
    }

    public ArrayList<Unidad> getUnidades() {
        if (unidades == null) {
            unidades = new ArrayList<Unidad>();
            PreparedStatement st;
            try {
                st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM unidades WHERE curso_id=?");
                st.setInt(1, getId());
                ResultSet res = st.executeQuery();
                while (res.next()) {
                    Unidad u = new Unidad();
                    u.cargarDesdeResultSet(res);
                    unidades.add(u);
                }
                Obj.cerrar(st, res);
            } catch (SQLException ex) {
                Logger.getLogger(Curso.class.getName()).log(Level.SEVERE, "Error cargando unidades de curso:" + this, ex);
            }
        }
        return unidades;
    }

    public int getNumeroDeAlumnos() {
        if (numeroDeAlumnos == null) {
            try {
                PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT count(*) FROM alumnos WHERE borrado=0 AND curso_id=?");
                st.setInt(1, getId());
                ResultSet res = st.executeQuery();
                if (res.next()) {
                    numeroDeAlumnos = res.getInt(1);
                }
                Obj.cerrar(st, res);
            } catch (SQLException ex) {
                Logger.getLogger(Curso.class.getName()).log(Level.SEVERE, "Error calculando numero de alumnos de curso: " + this, ex);
            }
        }
        return numeroDeAlumnos;
    }

    public int getNumeroDeAlumnosUnidad(int idUnidad) {
        int num = 0;
        try {
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT count(*) FROM alumnos WHERE borrado=0 AND curso_id=? AND unidad_id=?");
            st.setInt(1, getId());
            st.setInt(2, idUnidad);
            ResultSet res = st.executeQuery();
            if (res.next()) {
                num = res.getInt(1);
            }
            Obj.cerrar(st, res);
        } catch (SQLException ex) {
            Logger.getLogger(Curso.class.getName()).log(Level.SEVERE, "Error calculando numero de alumnos de curso: " + this, ex);
        }
        return num;
    }

    public void resetearNumeroDeAlumnos() {
        this.numeroDeAlumnos = null;
    }

    @Override
    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    @Override
    public String toString() {
        return getDescripcionObjeto();
    }

    public Curso(AnoEscolar anoEscolar) {
        setAnoEscolar(anoEscolar);
    }

    public Curso(ResultSet res) throws Exception {
        cargarDesdeResultSet(res);
    }

    /*public Curso(AnoEscolar ano, int codigo) throws Exception {
    //TODO Implementar cache en este constructor
    PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM cursos WHERE ano=? AND cod=?");
    st.setInt(1, ano.getId());
    st.setInt(2, codigo);
    ResultSet res = st.executeQuery();
    if (res.next()) {
    cargarDesdeResultSet(res);
    }
    Obj.cerrar(st, res);
    }*/
    private Curso(int id) throws Exception {
        PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM cursos WHERE id=?");
        st.setInt(1, id);
        ResultSet res = st.executeQuery();
        if (res.next()) {
            cargarDesdeResultSet(res);
        }
        Obj.cerrar(st, res);
    }

    public Integer getPosicion() {
        return posicion;
    }

    public void setPosicion(Integer posicion) {
        this.posicion = posicion;
    }

    public AnoEscolar getAnoEscolar() {
        return anoEscolar;
    }

    public final void setAnoEscolar(AnoEscolar anoEscolar) {
        this.anoEscolar = anoEscolar;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public boolean _guardar(boolean crear) {
        boolean ret = false;
        PreparedStatement st = null;
        try {
            String sql = "UPDATE cursos SET cod=?,ano=?,descripcion=?,posicion=?,curso=?,fini=?,ffin=?,maxFaltas=? WHERE id=?";
            if (getId() == null) {
                sql = "INSERT INTO cursos (cod,ano,descripcion,posicion,curso,fini,ffin,maxFaltas,id) VALUES(?,?,?,?,?,?,?,?,?)";
            }
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            st.setObject(1, getCodigo());
            st.setInt(2, getAnoEscolar().getId());
            st.setString(3, getDescripcion());
            st.setInt(4, getPosicion());
            st.setString(5, getCurso());
            if (getFechaInicio() != null) {
                st.setDate(6, new java.sql.Date(getFechaInicio().getTime().getTime()));
            } else {
                st.setObject(6, null);
            }
            if (getFechaFin() != null) {
                st.setDate(7, new java.sql.Date(getFechaFin().getTime().getTime()));
            } else {
                st.setObject(7, null);
            }
            st.setInt(8, getMaxFaltas());
            st.setObject(9, getId());
            ret = st.executeUpdate() > 0;
            if (ret && getId() == null) {
                setId((int) st.getLastInsertID());
            }
            st.close();
            if (ret) {
                //Tenemos que actualizar el código de curso de las unidades
                st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("UPDATE unidades SET curso=?, cursogrupo=CONCAT(?,'-',grupo) WHERE curso_id=? OR curso2_id=? ");
                st.setString(1, getCurso());
                st.setString(2, getCurso());
                st.setInt(3, getId());
                st.setInt(4, getId());
                st.executeUpdate();
                st.close();
                //Y reseteamos la cache de unidades
                Cache.clear(Unidad.class);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Curso.class.getName()).log(Level.SEVERE, "Error guardando datos de Curso: " + this, ex);
        }
        Obj.cerrar(st);
        if (ret) {
            Cache.put(getClass(), getId(), this);
        }
        return ret;
    }

    public final boolean cargarDesdeResultSet(ResultSet res) throws SQLException, Exception {
        boolean ret = true;
        setAnoEscolar(AnoEscolar.getAnoEscolar(res.getInt("ano")));
        setDescripcion(res.getString("descripcion"));
        setId(res.getInt("id"));
        setPosicion(res.getInt("posicion"));
        setCodigo(res.getInt("cod"));
        setCurso(res.getString("curso"));
        setFechaInicio(Fechas.toGregorianCalendar(res.getDate("fini")));
        setFechaFin(Fechas.toGregorianCalendar(res.getDate("ffin")));
        setMaxFaltas(res.getInt("maxFaltas"));
        Cache.put(getClass(), getId(), this);
        return ret;
    }

    @Override
    public String getNombreObjeto() {
        return "Curso";
    }

    @Override
    public String getDescripcionObjeto() {
        return Str.noNulo(getDescripcion()) + (isMixto() ? " [Mixto]" : "");
    }

    public ArrayList<Alumno> getAlumnos() {
        if (alumnos == null) {
            alumnos = new ArrayList<Alumno>();
            PreparedStatement st = null;
            ResultSet res = null;
            try {
                st = (PreparedStatement) MaimonidesApp.getConexion().prepareStatement("SELECT * FROM alumnos WHERE borrado=0 AND curso_id=?");
                st.setInt(1, getId());
                res = st.executeQuery();
                while (res.next()) {
                    Alumno a = new Alumno();
                    a.cargarDesdeResultSet(res);
                    alumnos.add(a);
                }
            } catch (SQLException ex) {
                Logger.getLogger(Unidad.class.getName()).log(Level.SEVERE, null, ex);
            }
            Obj.cerrar(st, res);
        }
        return alumnos;
    }

    @Override
    public boolean equals(Object obj) {
        boolean igual = false;
        if (obj instanceof Curso) {
            Curso objCurso = (Curso) obj;
            if (objCurso.getId() != null && getId() != null) {
                igual = objCurso.getId().intValue() == getId();
            } else {
                igual = super.equals(obj);
            }
        }
        return igual;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 83 * hash + (this.codigo != null ? this.codigo.hashCode() : 0);
        hash = 83 * hash + (this.descripcion != null ? this.descripcion.hashCode() : 0);
        hash = 83 * hash + (this.anoEscolar != null ? this.anoEscolar.hashCode() : 0);
        hash = 83 * hash + (this.posicion != null ? this.posicion.hashCode() : 0);
        hash = 83 * hash + (this.curso != null ? this.curso.hashCode() : 0);
        return hash;
    }

    @Override
    public String getTabla() {
        return "cursos";
    }
}
