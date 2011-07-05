package com.codeko.apps.maimonides.seneca.operaciones.envioFicherosFaltas;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.ObjetoBD;
import com.codeko.swing.CdkAutoTablaCol;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author codeko
 */
public class AlumnoEnvioErroneo extends ObjetoBD {

    Alumno alumno = null;
    @CdkAutoTablaCol(ignorar = true)
    Integer tipo = 0;
    Boolean excluir = false;
    String info = "";

    public AlumnoEnvioErroneo(Alumno a) {
        setAlumno(a);
    }

    public AlumnoEnvioErroneo() {
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public Alumno getAlumno() {
        return alumno;
    }

    public void setAlumno(Alumno alumno) {
        this.alumno = alumno;
    }

    public Boolean getExcluir() {
        return excluir;
    }

    public void setExcluir(Boolean excluir) {
        this.excluir = excluir;
    }

    public Integer getTipo() {
        return tipo;
    }

    public void setTipo(Integer tipo) {
        this.tipo = tipo;
    }

    @Override
    public boolean guardar() {
        boolean ret = false;
        try {
            String sql = "UPDATE alumnos_problemas_envio SET alumno_id=?,tipo=?,excluir=?,info=? WHERE id=?";
            if (getId() == null) {
                sql = "INSERT INTO alumnos_problemas_envio (alumno_id,tipo,excluir,info,id) VALUES(?,?,?,?,?)";
            }
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            st.setInt(1, getAlumno().getId());
            st.setInt(2, getTipo());
            st.setInt(3, getExcluir() ? 1 : 0);
            st.setString(4, getInfo());
            st.setObject(5, getId());
            ret = st.executeUpdate() > 0;
            if (ret && getId() == null) {
                setId((int) st.getLastInsertID());
            }
            st.close();
        } catch (SQLException ex) {
            Logger.getLogger(AlumnoEnvioErroneo.class.getName()).log(Level.SEVERE, "Error guardando datos de fallo de envio de alumno: " + this, ex);
        }
        return ret;
    }

    public void cargarDesdeResultSet(ResultSet res) throws SQLException, Exception {
        setId(res.getInt("id"));
        setAlumno(Alumno.getAlumno(res.getInt("alumno_id")));
        setTipo(res.getInt("tipo"));
        setExcluir(res.getInt("excluir") > 0);
        setInfo(res.getString("info"));
    }

    @Override
    public String getNombreObjeto() {
        return "Alumno con envío erróneo";
    }

    @Override
    public String getDescripcionObjeto() {
        return getAlumno().toString();
    }

    @Override
    public String getTabla() {
        return "alumnos_problemas_envio";
    }
}
