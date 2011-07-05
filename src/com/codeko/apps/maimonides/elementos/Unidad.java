package com.codeko.apps.maimonides.elementos;

import com.codeko.apps.maimonides.*;
import com.codeko.apps.maimonides.cache.Cache;
import com.codeko.swing.CdkAutoTablaCol;
import com.codeko.util.Num;
import com.codeko.util.Obj;
import com.codeko.util.Str;
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
public class Unidad extends ObjetoBDConCod implements Comparable<Unidad> {

    @CdkAutoTablaCol(titulo = "Código")
    Integer codigo = null;
    @CdkAutoTablaCol(titulo = "Descripción")
    String descripcion = "";
    @CdkAutoTablaCol(titulo = "Curso")
    Integer idCurso = null;
    @CdkAutoTablaCol(titulo = "Curso mixto")
    Integer idCurso2 = null;
    @CdkAutoTablaCol(ignorar = true)
    String curso = "";
    String grupo = "";
    @CdkAutoTablaCol(ignorar = true)
    String cursoGrupo = "";
    @CdkAutoTablaCol(ignorar = true)
    AnoEscolar anoEscolar = MaimonidesApp.getApplication().getAnoEscolar();
    @CdkAutoTablaCol(titulo = "Tutor")
    Integer idProfesor = null;
    Integer capacidad = 0;
    @CdkAutoTablaCol(titulo = "Posición")
    Integer posicion = 0;
    String nombreOriginal = "";
    @CdkAutoTablaCol(ignorar = true)
    ArrayList<Alumno> alumnos = null;

    public static Unidad getUnidad(int id) throws Exception {
        Object obj = Cache.get(Unidad.class, id);
        if (obj != null) {
            return (Unidad) obj;
        } else {
            Unidad h = new Unidad(id);
            return h;
        }
    }

    public static Unidad getUnidadPorTutor(int idProfesor) {
        Unidad u = null;
        try {
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT id FROM unidades WHERE tutor_id=?");
            st.setInt(1, idProfesor);
            ResultSet res = st.executeQuery();
            if (res.next()) {
                u = getUnidad(res.getInt(1));
            }
            Obj.cerrar(st, res);
        } catch (Exception ex) {
            Logger.getLogger(Unidad.class.getName()).log(Level.SEVERE, null, ex);
        }
        return u;
    }

    public static Unidad getUnidad(AnoEscolar ano, int codigo) {
        Unidad u = null;
        try {
            u=new Unidad(ano, codigo);
        } catch (Exception ex) {
            Logger.getLogger(Unidad.class.getName()).log(Level.SEVERE, null, ex);
        }
        return u;
    }

    public Unidad() {
    }

    public Unidad(AnoEscolar ano, int codigo) throws Exception {
        PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM unidades WHERE ano=? AND cod=?");
        st.setInt(1, ano.getId());
        st.setInt(2, codigo);
        ResultSet res = st.executeQuery();
        if (res.next()) {
            cargarDesdeResultSet(res);
        }
        Obj.cerrar(st, res);
    }

    private Unidad(int id) throws Exception {
        PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM unidades WHERE id=?");
        st.setInt(1, id);
        ResultSet res = st.executeQuery();
        if (res.next()) {
            cargarDesdeResultSet(res);
            Obj.cerrar(st, res);
        } else {
            Obj.cerrar(st, res);
            throw new InvalidParameterException("No existe ninguna unidad con ID " + id);
        }
    }

    public static Unidad getUnidadPorNombreOriginal(AnoEscolar ano, String nombre) {
        Unidad u = null;
        PreparedStatement st = null;
        ResultSet res = null;
        try {
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM unidades WHERE ano=? AND nombre_original=?");
            st.setInt(1, ano.getId());
            st.setString(2, nombre);
            res = st.executeQuery();
            if (res.next()) {
                u = new Unidad();
                u.cargarDesdeResultSet(res);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Unidad.class.getName()).log(Level.SEVERE, null, ex);
        }
        Obj.cerrar(st, res);
        return u;
    }

    public Integer getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
    }

    public Integer getIdProfesor() {
        return idProfesor;
    }

    public void setIdProfesor(Integer idProfesor) {
        this.idProfesor = idProfesor;
    }

    public Integer getPosicion() {
        return posicion;
    }

    public void setPosicion(Integer posicion) {
        this.posicion = posicion;
    }

    public String getNombreOriginal() {
        return nombreOriginal;
    }

    public void setNombreOriginal(String nombreOriginal) {
        this.nombreOriginal = nombreOriginal;
    }

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    public String getCursoGrupo() {
        return cursoGrupo;
    }

    public void setCursoGrupo(String cursoGrupo) {
        this.cursoGrupo = cursoGrupo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }

    @Override
    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public Integer getIdCurso() {
        return idCurso;
    }

    public Integer getIdCurso2() {
        return idCurso2;
    }

    public void setIdCurso(Integer idCurso) {
        this.idCurso = idCurso;
    }

    public void setIdCurso2(Integer idCurso2) {
        this.idCurso2 = idCurso2;
        if (idCurso2 != null && idCurso2 == 0) {
            this.idCurso2 = null;
        }
    }

    public AnoEscolar getAnoEscolar() {
        return anoEscolar;
    }

    public void setAnoEscolar(AnoEscolar anoEscolar) {
        this.anoEscolar = anoEscolar;
    }

    @Override
    public String toString() {
        return getCursoGrupo();
    }

    public String toTexto() {
        StringBuilder sb = new StringBuilder("[ID:");
        sb.append(getId());
        sb.append(", CODIGO:");
        sb.append(getCodigo());
        sb.append(", ID CURSO:");
        sb.append(getIdCurso());
        sb.append(", ID CURSO2:");
        sb.append(getIdCurso2());
        sb.append(", DESCRIPCION:");
        sb.append(getDescripcion());
        sb.append(", CURSO:");
        sb.append(getCurso());
        sb.append(", GRUPO:");
        sb.append(getGrupo());
        sb.append(", CURSO_GRUPO:");
        sb.append(getCursoGrupo());
        sb.append(", AÑO:");
        sb.append(getAnoEscolar());
        sb.append(", POSICION:");
        sb.append(getPosicion());
        sb.append(", CAPACIDAD:");
        sb.append(getCapacidad());
        sb.append(", TUTOR:");
        sb.append(getIdProfesor());
        sb.append("]");
        return sb.toString();
    }

    @Override
    public boolean _guardar(boolean crear) {
        boolean ret = false;
        try {
            String sql = "UPDATE unidades SET cod=?,ano=?,descripcion=?,curso=?,grupo=?,cursogrupo=?,curso_id=?,curso2_id=?,posicion=?,tutor_id=?,capacidad=?,nombre_original=? WHERE id=?";
            if (getId() == null) {
                sql = "INSERT INTO unidades (cod,ano,descripcion,curso,grupo,cursogrupo,curso_id,curso2_id,posicion,tutor_id,capacidad,nombre_original,id) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
            }
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            //Vemos si la cadena de curso está vacía y la asignamos
            if (Str.noNulo(getCurso()).equals("")) {
                try {
                    if (Num.getInt(getIdCurso()) > 0) {
                        Curso c = Curso.getCurso(idCurso);
                        setCurso(c.getCurso());
                    }
                } catch (Exception ex) {
                    Logger.getLogger(Unidad.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            //Ahora asignamos el curso grupo
            setGrupo(Str.noNulo(getGrupo()).toUpperCase());
            setCurso(Str.noNulo(getCurso()).toUpperCase());
            setCursoGrupo(getCurso() + "-" + getGrupo());

            st.setObject(1, getCodigo());
            st.setInt(2, getAnoEscolar().getId());
            st.setString(3, getDescripcion());
            st.setString(4, getCurso());
            st.setString(5, getGrupo());
            st.setString(6, getCursoGrupo());
            st.setObject(7, getIdCurso());
            st.setObject(8, getIdCurso2());
            st.setInt(9, getPosicion());
            st.setObject(10, getIdProfesor());
            st.setInt(11, getCapacidad());
            st.setString(12, getNombreOriginal());
            st.setObject(13, getId());
            ret = st.executeUpdate() > 0;
            if (ret && getId() == null) {
                setId((int) st.getLastInsertID());
            }
            st.close();
        } catch (SQLException ex) {
            ret = false;
            Logger.getLogger(Unidad.class.getName()).log(Level.SEVERE, "Error guardando datos de Unidad: " + this, ex);
        }
        if (ret) {
            Cache.put(getClass(), getId(), this);
        }
        return ret;
    }

    public final boolean cargarDesdeResultSet(ResultSet res) {
        boolean ret = false;
        try {
            setId(res.getInt("id"));
            setCodigo(res.getInt("cod"));
            setAnoEscolar(AnoEscolar.getAnoEscolar(res.getInt("ano")));
            setDescripcion(res.getString("descripcion"));
            setCurso(res.getString("curso"));
            setGrupo(res.getString("grupo"));
            setCursoGrupo(res.getString("cursogrupo"));
            setIdCurso(res.getInt("curso_id"));
            setIdCurso2(res.getInt("curso2_id"));
            setPosicion(res.getInt("posicion"));
            setIdProfesor(res.getInt("tutor_id"));
            setCapacidad(res.getInt("capacidad"));
            setNombreOriginal(res.getString("nombre_original"));
            ret = true;
            Cache.put(getClass(), getId(), this);
        } catch (Exception ex) {
            Logger.getLogger(Unidad.class.getName()).log(Level.SEVERE, "Error cargando unidad desde ResultSet", ex);
        }
        return ret;
    }

    @Override
    public String getNombreObjeto() {
        return "Unidad";
    }

    @Override
    public String getDescripcionObjeto() {
        return getDescripcion();
    }

    public static ArrayList<Unidad> ordenarPorPosicion(ArrayList<Unidad> vector) {
        ArrayList<Unidad> ordenado = new ArrayList<Unidad>();
        for (int i = 0; i < vector.size(); i++) {
            Unidad u = vector.get(i);
            boolean insertado = false;
            for (int x = 0; x < ordenado.size() && !insertado; x++) {
                Unidad uo = ordenado.get(x);
                if (u.getPosicion() < uo.getPosicion()) {
                    insertado = true;
                    ordenado.add(x, u);
                }
            }
            if (!insertado) {
                ordenado.add(u);
            }
        }
        vector.clear();
        vector.addAll(ordenado);
        return vector;
    }

    @Override
    public boolean equals(Object obj) {
        boolean ret = false;
        if (obj instanceof Unidad) {
            Unidad u = (Unidad) obj;
            if (u.getId() != null && getId() != null) {
                ret = u.getId().equals(getId());
            } else {
                try {
                    ret = (u.getCodigo().equals(getCodigo()) && getAnoEscolar().getId().equals(u.getAnoEscolar().getId()));
                } catch (Exception e) {
                }
            }
        } else {
            ret = super.equals(obj);
        }
        return ret;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + (this.codigo != null ? this.codigo.hashCode() : 0);
        hash = 67 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 67 * hash + this.idCurso;
        hash = 67 * hash + (this.descripcion != null ? this.descripcion.hashCode() : 0);
        hash = 67 * hash + (this.curso != null ? this.curso.hashCode() : 0);
        hash = 67 * hash + (this.grupo != null ? this.grupo.hashCode() : 0);
        hash = 67 * hash + (this.cursoGrupo != null ? this.cursoGrupo.hashCode() : 0);
        hash = 67 * hash + (this.anoEscolar != null ? this.anoEscolar.hashCode() : 0);
        hash = 67 * hash + (this.posicion != null ? this.posicion.hashCode() : 0);
        hash = 67 * hash + (this.idProfesor != null ? this.idProfesor.hashCode() : 0);
        hash = 67 * hash + this.capacidad;
        return hash;
    }

    public static ArrayList<Unidad> getUnidadesDisponibles(Unidad unidadActual) {
        ArrayList<Unidad> unidades = new ArrayList<Unidad>();
        try {
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM unidades WHERE curso_id=? OR curso2_id=? OR curso_id=? OR curso2_id=? ORDER BY posicion");
            st.setInt(1, unidadActual.getIdCurso());
            st.setInt(2, unidadActual.getIdCurso());
            if (unidadActual.getIdCurso2() != null) {
                st.setInt(3, unidadActual.getIdCurso2());
                st.setInt(4, unidadActual.getIdCurso2());
            } else {
                st.setInt(3, unidadActual.getIdCurso());
                st.setInt(4, unidadActual.getIdCurso());
            }
            ResultSet res = st.executeQuery();
            while (res.next()) {
                Unidad u = new Unidad();
                u.cargarDesdeResultSet(res);
                unidades.add(u);
            }
            Obj.cerrar(st, res);
        } catch (SQLException ex) {
            Logger.getLogger(Unidad.class.getName()).log(Level.SEVERE, null, ex);
        }
        return unidades;
    }

    public static ArrayList<Unidad> getUnidades() {
        ArrayList<Unidad> unidades = new ArrayList<Unidad>();
        try {
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT u.* FROM unidades AS u LEFT JOIN cursos AS c ON c.id=u.curso_id WHERE u.ano=? ORDER BY c.posicion,u.posicion");
            st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
            ResultSet res = st.executeQuery();
            while (res.next()) {
                Unidad u = new Unidad();
                if (u.cargarDesdeResultSet(res)) {
                    unidades.add(u);
                }
            }
            Obj.cerrar(st, res);
        } catch (SQLException ex) {
            Logger.getLogger(Unidad.class.getName()).log(Level.SEVERE, null, ex);
        }
        return unidades;
    }

    public static ArrayList<Unidad> getUnidadesDisponibles(Curso curso) {
        ArrayList<Unidad> unidades = new ArrayList<Unidad>();
        try {
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM unidades WHERE curso_id=? OR curso2_id=? ORDER BY posicion");
            st.setInt(1, curso.getId());
            st.setInt(2, curso.getId());
            ResultSet res = st.executeQuery();
            while (res.next()) {
                Unidad u = new Unidad();
                u.cargarDesdeResultSet(res);
                unidades.add(u);
            }
            Obj.cerrar(st, res);
        } catch (SQLException ex) {
            Logger.getLogger(Unidad.class.getName()).log(Level.SEVERE, null, ex);
        }
        return unidades;
    }

    @Override
    public int compareTo(Unidad o) {
        return getPosicion().compareTo(o.getPosicion());
    }

    public ArrayList<Alumno> getAlumnos() {
        if (alumnos == null) {
            alumnos = new ArrayList<Alumno>();
            PreparedStatement st = null;
            ResultSet res = null;
            try {
                st = (PreparedStatement) MaimonidesApp.getConexion().prepareStatement("SELECT * FROM alumnos WHERE borrado=0 AND unidad_id=?");
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
    public String getTabla() {
        return "unidades";
    }
}
