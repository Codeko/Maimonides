package com.codeko.apps.maimonides.elementos;

import com.codeko.apps.maimonides.*;
import com.codeko.apps.maimonides.cache.Cache;
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
public class Actividad extends ObjetoBDConCod {

    Integer codigo = null;
    AnoEscolar anoEscolar = MaimonidesApp.getApplication().getAnoEscolar();
    String descripcion = null;
    Boolean esRegular = null;
    Boolean necesitaMateria = null;
    Boolean necesitaUnidad = null;

    public static Actividad getActividad(int id) throws Exception {
        Object obj = Cache.get(Actividad.class, id);
        if (obj != null) {
            return (Actividad) obj;
        } else {
            Actividad p = new Actividad(id);
            return p;
        }
    }

    public static Actividad getActividad(AnoEscolar ano, String descripcion) throws Exception {
        Actividad a = null;
        PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM actividades WHERE ano=? AND trim(lower(descripcion)) LIKE ? ");
        st.setInt(1, ano.getId());
        st.setString(2, descripcion.toLowerCase().trim());
        ResultSet res = st.executeQuery();
        if (res.next()) {
            a = new Actividad();
            a.cargarDesdeResultSet(res);
        }
        Obj.cerrar(st, res);
        return a;
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

    public Boolean getEsRegular() {
        return esRegular;
    }

    public void setEsRegular(Boolean esRegular) {
        this.esRegular = esRegular;
    }

    @Override
    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public Boolean getNecesitaMateria() {
        return necesitaMateria;
    }

    public void setNecesitaMateria(Boolean necesitaMateria) {
        this.necesitaMateria = necesitaMateria;
    }

    public Boolean getNecesitaUnidad() {
        return necesitaUnidad;
    }

    public void setNecesitaUnidad(Boolean necesitaUnidad) {
        this.necesitaUnidad = necesitaUnidad;
    }

    public Actividad() {
    }

    private Actividad(int id) throws Exception {
        PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM actividades WHERE id=? ");
        st.setInt(1, id);
        ResultSet res = st.executeQuery();
        if (res.next()) {
            cargarDesdeResultSet(res);
        } else {
            throw new InvalidParameterException("No existe ninguna actividad con ID " + id);
        }
        Obj.cerrar(st, res);
    }

    public Actividad(AnoEscolar ano, int cod) throws Exception {
        //TODO Implementar cache para este contructor
        PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM actividades WHERE cod=? AND ano=? ");
        st.setInt(1, cod);
        st.setInt(2, ano.getId());
        ResultSet res = st.executeQuery();
        if (res.next()) {
            cargarDesdeResultSet(res);
        } else {
            throw new InvalidParameterException("No existe ninguna actividad con COD " + cod);
        }
        Obj.cerrar(st, res);
    }

    public final boolean cargarDesdeResultSet(ResultSet res) {
        boolean ret = true;
        try {
            setId(res.getInt("id"));
            setCodigo(res.getInt("cod"));
            setDescripcion(res.getString("descripcion"));
            setAnoEscolar(AnoEscolar.getAnoEscolar(res.getInt("ano")));
            setEsRegular(res.getBoolean("es_regular"));
            setNecesitaUnidad(res.getBoolean("necesita_unidad"));
            setNecesitaMateria(res.getBoolean("necesita_materia"));
            Cache.put(getClass(), getId(), this);
        } catch (Exception ex) {
            ret = false;
            Logger.getLogger(Actividad.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    @Override
    public boolean _guardar(boolean crear) {
        boolean ret = false;
        try {
            String sql = "UPDATE actividades SET cod=?,ano=?,descripcion=?,es_regular=?,necesita_unidad=?,necesita_materia=? WHERE id=?";
            if (getId() == null) {
                sql = "INSERT INTO actividades (cod,ano,descripcion,es_regular,necesita_unidad,necesita_materia,id) VALUES(?,?,?,?,?,?,?)";
            }
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            st.setObject(1, getCodigo());
            st.setInt(2, getAnoEscolar().getId());
            st.setString(3, getDescripcion());
            st.setBoolean(4, getEsRegular());
            st.setBoolean(5, getNecesitaUnidad());
            st.setBoolean(6, getNecesitaMateria());
            st.setObject(7, getId());
            ret = st.executeUpdate() > 0;
            if (ret && getId() == null) {
                setId((int) st.getLastInsertID());
            }
            st.close();
        } catch (SQLException ex) {
            Logger.getLogger(Actividad.class.getName()).log(Level.SEVERE, "Error guardando datos de Actividad: " + this, ex);
        }
        if (ret) {
            Cache.put(getClass(), getId(), this);
        }
        return ret;
    }

    @Override
    public String getNombreObjeto() {
        return "Actividad";
    }

    @Override
    public String getDescripcionObjeto() {
        return getDescripcion();
    }
    static int ultimoAnoEscolar = 0;
    static int ultimoCod = 0;

    public static int getIdActividadDocencia(AnoEscolar anoEscolar) {
        int cod = 0;
        if (anoEscolar.getId() == ultimoAnoEscolar) {
            cod = ultimoCod;
        } else {
            PreparedStatement st = null;
            ResultSet res = null;
            try {
                st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT id FROM actividades WHERE ano=? AND descripcion='Docencia'");
                st.setInt(1, anoEscolar.getId());
                res = st.executeQuery();
                if (res.next()) {
                    cod = res.getInt(1);
                }
                Obj.cerrar(st, res);
            } catch (SQLException ex) {
                Logger.getLogger(Actividad.class.getName()).log(Level.SEVERE, "Error recuperando ID de actividad 'Docencia' para el año: " + anoEscolar, ex);
            }
            Obj.cerrar(st, res);
        }
        ultimoAnoEscolar = anoEscolar.getId();
        ultimoCod = cod;
        return cod;
    }

    public static String getNombreActividad(int idActividad) {
        String nombre = "No existe actividad.";
        try {
            Actividad m = new Actividad(idActividad);
            nombre = m.getDescripcion();
        } catch (Exception ex) {
            Logger.getLogger(Actividad.class.getName()).log(Level.SEVERE, "Error recuperando descripcion de CTIVIDAD " + idActividad, ex);
        }
        return nombre;
    }

    public String getCodigoActividad() {
        //TODO El código de actividad debe poder guardarse
        return getCodigoActividad(getId());
    }

    public static String getCodigoActividad(int idActividad) {
        String cod = "-";
        try {
            Actividad m = new Actividad(idActividad);
            cod = MaimonidesUtil.getCodigoAbreviado(m.getDescripcion());
        } catch (Exception ex) {
            Logger.getLogger(Actividad.class.getName()).log(Level.SEVERE, "Error recuperando codigo_materia de materia " + idActividad, ex);
        }
        return cod;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Actividad) {
            Actividad a = (Actividad) obj;
            if (this.getId() != null && a.getId() != null) {
                return getId().equals(a.getId());
            } else {
                return super.equals(obj);
            }
        } else {
            return super.equals(obj);
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.codigo != null ? this.codigo.hashCode() : 0);
        hash = 97 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 97 * hash + (this.anoEscolar != null ? this.anoEscolar.hashCode() : 0);
        hash = 97 * hash + (this.descripcion != null ? this.descripcion.hashCode() : 0);
        hash = 97 * hash + (this.esRegular != null ? this.esRegular.hashCode() : 0);
        hash = 97 * hash + (this.necesitaMateria != null ? this.necesitaMateria.hashCode() : 0);
        hash = 97 * hash + (this.necesitaUnidad != null ? this.necesitaUnidad.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return getDescripcionObjeto();
    }

    public String getNombrePara(Materia m) {
        if (m == null) {
            return getDescripcion();
        } else {
            return m.getDescripcion();
        }
    }

    public String getCodigoPara(Materia m) {
        if (m == null) {
            return getCodigoActividad();
        } else {
            return m.getCodigoMateria();
        }
    }

    @Override
    public String getTabla() {
        return "actividades";
    }

    public static ArrayList<Actividad> getActividades() {
        ArrayList<Actividad> actividades = new ArrayList<Actividad>();
        try {
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM actividades WHERE ano=? ");
            st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
            ResultSet res = st.executeQuery();
            while (res.next()) {
                Actividad a = new Actividad();
                if (a.cargarDesdeResultSet(res)) {
                    actividades.add(a);
                }
            }
            Obj.cerrar(st, res);
        } catch (SQLException ex) {
            Logger.getLogger(Unidad.class.getName()).log(Level.SEVERE, null, ex);
        }
        return actividades;
    }
}
