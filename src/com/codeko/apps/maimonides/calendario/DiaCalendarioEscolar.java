package com.codeko.apps.maimonides.calendario;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.elementos.ObjetoBD;
import com.codeko.apps.maimonides.seneca.operaciones.calendario.GestorCalendarioSeneca;
import com.codeko.swing.CdkAutoTablaCol;
import com.codeko.util.Fechas;
import com.codeko.util.Obj;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author codeko
 */
public class DiaCalendarioEscolar extends ObjetoBD {

    @CdkAutoTablaCol(titulo = "Día")
    GregorianCalendar dia = null;
    @CdkAutoTablaCol(titulo = "Descripción")
    String descripcion = "";
    @CdkAutoTablaCol(titulo = "Ámbito")
    String ambito = "";
    Boolean docentes = true;
    Boolean personal = true;

    public DiaCalendarioEscolar() {
    }

    public DiaCalendarioEscolar(GregorianCalendar dia, String descripcion) {
        setDia(dia);
        setDescripcion(descripcion);
    }

    public String getAmbito() {
        return ambito;
    }

    public void setAmbito(String ambito) {
        this.ambito = ambito;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public final void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public GregorianCalendar getDia() {
        return dia;
    }

    public final void setDia(GregorianCalendar dia) {
        this.dia = dia;
    }

    public Boolean getDocentes() {
        return docentes;
    }

    public void setDocentes(Boolean docentes) {
        this.docentes = docentes;
    }

    public Boolean getPersonal() {
        return personal;
    }

    public void setPersonal(Boolean personal) {
        this.personal = personal;
    }

    @Override
    public boolean guardar() {
        String sql = "UPDATE calendario_escolar SET dia=?,ano=?,descripcion=?,ambito=?,docentes=?,personal=? WHERE id=?";
        if (getId() == null) {
            sql = "INSERT INTO calendario_escolar(dia,ano,descripcion,ambito,docentes,personal,id) VALUES(?,?,?,?,?,?,?)";
        }
        PreparedStatement st = null;
        boolean ret = false;
        try {
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            st.setDate(1, new java.sql.Date(getDia().getTimeInMillis()));
            st.setInt(2, MaimonidesApp.getApplication().getAnoEscolar().getId());
            st.setString(3, getDescripcion());
            st.setString(4, getAmbito());
            st.setBoolean(5, getDocentes());
            st.setBoolean(6, getPersonal());
            st.setObject(7, getId());
            ret = st.executeUpdate() > 0;
            if (ret && getId() == null) {
                setId((int) st.getLastInsertID());
            }
            GestorCalendarioSeneca.resetearCalendario();
        } catch (SQLException ex) {
            Logger.getLogger(DiaCalendarioEscolar.class.getName()).log(Level.SEVERE, "Error guardando día escolar:" + getDescripcionObjeto(), ex);
        }
        Obj.cerrar(st);
        return ret;
    }

    public void cargarDesdeResultSet(ResultSet res) throws SQLException {
        setId(res.getInt("id"));
        setDia(Fechas.toGregorianCalendar(res.getDate("dia")));
        setAmbito(res.getString("ambito"));
        setDescripcion(res.getString("descripcion"));
        setDocentes(res.getBoolean("docentes"));
        setPersonal(res.getBoolean("personal"));
    }

    @Override
    public String getNombreObjeto() {
        return "Día calendario escolar";
    }

    @Override
    public String getDescripcionObjeto() {
        return Fechas.format(getDia()) + ", " + getDescripcion();
    }

    @Override
    protected void resetearCache() {
        GestorCalendarioSeneca.resetearCalendario();
    }

    public boolean borrarFechasIguales() {
        boolean ret = false;
        if (getDia() != null) {
            try {
                PreparedStatement st = (PreparedStatement) MaimonidesApp.getConexion().prepareStatement("DELETE FROM calendario_escolar WHERE dia=? AND ano=?");
                st.setDate(1, new java.sql.Date(getDia().getTimeInMillis()));
                st.setInt(2, MaimonidesApp.getApplication().getAnoEscolar().getId());
                ret = st.executeUpdate() > 0;
            } catch (SQLException ex) {
                Logger.getLogger(DiaCalendarioEscolar.class.getName()).log(Level.SEVERE, "Error borrando dia de calendario escolar: " + this, ex);
            }
            GestorCalendarioSeneca.resetearCalendario();
        }
        return ret;
    }

    @Override
    public String getTabla() {
        return "calendario_escolar";
    }
}
