package com.codeko.apps.maimonides.convivencia;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.elementos.AnoEscolar;
import com.codeko.apps.maimonides.elementos.ObjetoBD;
import com.codeko.swing.CdkAutoTablaCol;
import com.codeko.util.Num;
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
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class TramoHorario extends ObjetoBD {

    @CdkAutoTablaCol(titulo = "Código")
    String codigo = "";
    @CdkAutoTablaCol(ignorar = true)
    AnoEscolar anoEscolar = MaimonidesApp.getApplication().getAnoEscolar();
    @CdkAutoTablaCol(titulo = "Descripción")
    String descripcion = "";

    public TramoHorario() {
    }

    public TramoHorario(int id) throws Exception {
        PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM conv_tramos_horarios WHERE id=? ");
        st.setInt(1, id);
        ResultSet res = st.executeQuery();
        if (res.next()) {
            cargarDesdeResultSet(res);
        } else {
            throw new InvalidParameterException("No existe ningun tramo horario con ID " + id);
        }
        Obj.cerrar(st, res);
    }

    public static TramoHorario getTramoHorario(AnoEscolar ano, String codigo) {
        TramoHorario t = null;
        try {
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM conv_tramos_horarios WHERE ano=? AND cod=?");
            st.setInt(1, ano.getId());
            st.setString(2, codigo);
            ResultSet res = st.executeQuery();
            if (res.next()) {
                t = new TramoHorario();
                t.cargarDesdeResultSet(res);
            }
            Obj.cerrar(st, res);
        } catch (Exception ex) {
            Logger.getLogger(TramoHorario.class.getName()).log(Level.SEVERE, null, ex);
        }
        return t;
    }

    public final void cargarDesdeResultSet(ResultSet res) throws SQLException, Exception {
        setId(res.getInt("id"));
        setCodigo(res.getString("cod"));
        setDescripcion(res.getString("descripcion"));
        setAnoEscolar(AnoEscolar.getAnoEscolar(res.getInt("ano")));
    }

    public static TramoHorario geTramoHorario(int id) {
        TramoHorario t = null;
        try {
            t = new TramoHorario(id);
        } catch (Exception ex) {
            Logger.getLogger(TramoHorario.class.getName()).log(Level.SEVERE, null, ex);
        }
        return t;
    }

    public static ArrayList<TramoHorario> getTramosHorarios() {
        ArrayList<TramoHorario> datos = new ArrayList<TramoHorario>();
        PreparedStatement st = null;
        ResultSet res = null;
        try {
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM conv_tramos_horarios WHERE ano=? ");
            st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
            res = st.executeQuery();
            while (res.next()) {
                TramoHorario tc = new TramoHorario();
                tc.cargarDesdeResultSet(res);
                datos.add(tc);
            }
        } catch (Exception ex) {
            Logger.getLogger(TramoHorario.class.getName()).log(Level.SEVERE, null, ex);
        }
        Obj.cerrar(st, res);
        return datos;
    }

    public static TramoHorario getDefaultTramoHorario() {
        int thId = Num.getInt(MaimonidesApp.getApplication().getConfiguracion().get("convivencia_default_tramo", "0"));
        if (thId == 0) {
            //Si no hay tramo horario buscamos aquel que sea "En clase" para asignarlo como por defecto
            for (TramoHorario th : TramoHorario.getTramosHorarios()) {
                if (th.getDescripcion().toLowerCase().contains("en clase")) {
                    setDefaultTramoHorario(th);
                    return th;
                }

            }
        }
        return TramoHorario.geTramoHorario(thId);
    }

    public static void setDefaultTramoHorario(TramoHorario th) {
        int id = 0;
        if (th != null) {
            id = th.getId();
        }
        TramoHorario.setDefaultTramoHorario(id);
    }

    public static void setDefaultTramoHorario(int thId) {
        MaimonidesApp.getApplication().getConfiguracion().set("convivencia_default_tramo", thId + "");
    }

    public AnoEscolar getAnoEscolar() {
        return anoEscolar;
    }

    public void setAnoEscolar(AnoEscolar anoEscolar) {
        this.anoEscolar = anoEscolar;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public boolean guardar() {
        boolean ret = false;
        try {
            String sql = "UPDATE conv_tramos_horarios SET cod=?,ano=?,descripcion=? WHERE id=?";
            if (getId() == null) {
                sql = "INSERT INTO conv_tramos_horarios (cod,ano,descripcion,id) VALUES(?,?,?,?)";
            }
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            st.setObject(1, getCodigo());
            st.setInt(2, getAnoEscolar().getId());
            st.setString(3, getDescripcion());
            st.setObject(4, getId());
            ret = st.executeUpdate() > 0;
            if (ret && getId() == null) {
                setId((int) st.getLastInsertID());
            }
            st.close();
        } catch (SQLException ex) {
            Logger.getLogger(TramoHorario.class.getName()).log(Level.SEVERE, "Error guardando datos de tramo horario: " + this, ex);
        }
        return ret;
    }

    @Override
    public String getNombreObjeto() {
        return "Tramo Horario";
    }

    @Override
    public String getDescripcionObjeto() {
        return getDescripcion();
    }

    @Override
    public String toString() {
        return getDescripcionObjeto();
    }

    @Override
    public String getTabla() {
        return "conv_tramos_horarios";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TramoHorario) {
            TramoHorario th = (TramoHorario) obj;
            if (th.getId() == getId()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + (this.codigo != null ? this.codigo.hashCode() : 0);
        hash = 17 * hash + (this.anoEscolar != null ? this.anoEscolar.hashCode() : 0);
        hash = 17 * hash + (this.descripcion != null ? this.descripcion.hashCode() : 0);
        return hash;
    }
}
