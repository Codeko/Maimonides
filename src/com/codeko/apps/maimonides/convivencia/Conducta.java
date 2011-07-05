package com.codeko.apps.maimonides.convivencia;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.elementos.AnoEscolar;
import com.codeko.apps.maimonides.elementos.ObjetoBD;
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
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class Conducta extends ObjetoBD {

    @CdkAutoTablaCol(titulo = "Código")
    String codigo = "";
    @CdkAutoTablaCol(ignorar = true)
    AnoEscolar anoEscolar = MaimonidesApp.getApplication().getAnoEscolar();
    @CdkAutoTablaCol(titulo = "Tipo")
    TipoConducta tipo = null;
    @CdkAutoTablaCol(titulo = "Descripción")
    String descripcion = "";
    @CdkAutoTablaCol(titulo = "Acceso")
    String acceso = "";

    public Conducta() {
    }

    public Conducta(int id) throws Exception {
        PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM conv_conductas WHERE id=? ");
        st.setInt(1, id);
        ResultSet res = st.executeQuery();
        if (res.next()) {
            cargarDesdeResultSet(res);
        } else {
            throw new InvalidParameterException("No existe ninguna conducta con ID " + id);
        }
        Obj.cerrar(st, res);
    }

    public final void cargarDesdeResultSet(ResultSet res) throws SQLException, Exception {
        setId(res.getInt("id"));
        setCodigo(res.getString("cod"));
        setAcceso(res.getString("acceso"));
        setDescripcion(res.getString("descripcion"));
        setAnoEscolar(AnoEscolar.getAnoEscolar(res.getInt("ano")));
        setTipo(TipoConducta.getTipoConducta(res.getInt("tipo_id")));
    }

    @CdkAutoTablaCol(titulo = "Gravedad")
    public int getGravedad() {
        return getTipo().getGravedad();
    }

    public static Conducta getConducta(AnoEscolar ano, String codigo, TipoConducta tipo) {
        Conducta t = null;
        try {
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM conv_conductas WHERE ano=? AND cod=? AND tipo_id=? ");
            st.setInt(1, ano.getId());
            st.setString(2, codigo);
            st.setInt(3, tipo.getId());
            ResultSet res = st.executeQuery();
            if (res.next()) {
                t = new Conducta();
                t.cargarDesdeResultSet(res);
            }
            Obj.cerrar(st, res);
        } catch (Exception ex) {
            Logger.getLogger(Conducta.class.getName()).log(Level.SEVERE, null, ex);
        }
        return t;
    }

    public static Conducta getConducta(AnoEscolar ano, TipoConducta tipo, String nombre) {
        Conducta t = null;
        try {
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM conv_conductas WHERE ano=? AND descripcion=? AND tipo_id=? ");
            st.setInt(1, ano.getId());
            st.setString(2, nombre);
            st.setInt(3, tipo.getId());
            ResultSet res = st.executeQuery();
            if (res.next()) {
                t = new Conducta();
                t.cargarDesdeResultSet(res);
            }
            Obj.cerrar(st, res);
        } catch (Exception ex) {
            Logger.getLogger(Conducta.class.getName()).log(Level.SEVERE, null, ex);
        }
        return t;
    }

    public String getAcceso() {
        return acceso;
    }

    public void setAcceso(String acceso) {
        this.acceso = acceso;
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

    public TipoConducta getTipo() {
        return tipo;
    }

    public void setTipo(TipoConducta tipo) {
        this.tipo = tipo;
    }

    @Override
    public boolean guardar() {
        boolean ret = false;
        try {
            String sql = "UPDATE conv_conductas SET cod=?,ano=?,tipo_id=?,descripcion=?,acceso=? WHERE id=?";
            if (getId() == null) {
                sql = "INSERT INTO conv_conductas (cod,ano,tipo_id,descripcion,acceso,id) VALUES(?,?,?,?,?,?)";
            }
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            st.setObject(1, getCodigo());
            st.setInt(2, getAnoEscolar().getId());
            st.setInt(3, getTipo().getId());
            st.setString(4, getDescripcion());
            st.setString(5, getAcceso());
            st.setObject(6, getId());
            ret = st.executeUpdate() > 0;
            if (ret && getId() == null) {
                setId((int) st.getLastInsertID());
            }
            st.close();
            ret = true;
        } catch (SQLException ex) {
            Logger.getLogger(Conducta.class.getName()).log(Level.SEVERE, "Error guardando datos de conducta: " + this, ex);
        }
        return ret;
    }

    @Override
    public String getNombreObjeto() {
        return "Conducta/Medida disciplinaria";
    }

    @Override
    public String getDescripcionObjeto() {
        return getDescripcion();
    }

    @Override
    public String toString() {
        String gravedad = "";
        String sAcceso = "";
        if (getTipo() != null && getTipo().getGravedad() > TipoConducta.GRAVEDAD_INDEFINIDA) {
            gravedad = " [" + TipoConducta.getNombreGravedad(getTipo().getGravedad()) + "]";
        }
        if (!Str.noNulo(getAcceso()).trim().equals("")) {
            sAcceso = getAcceso() + " - ";
        }
        return sAcceso + getDescripcion() + gravedad;
    }

    @Override
    public String getTabla() {
        return "conv_conductas";
    }

    public static ArrayList<Conducta> getConductas(int tipo) {
        ArrayList<Conducta> datos = new ArrayList<Conducta>();
        PreparedStatement st = null;
        ResultSet res = null;
        try {
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT cc.* FROM conv_conductas AS cc JOIN conv_tipos AS ct ON cc.tipo_id=ct.id WHERE cc.ano=? AND ct.tipo=? ");
            st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
            st.setInt(2, tipo);
            res = st.executeQuery();
            while (res.next()) {
                Conducta tc = new Conducta();
                tc.cargarDesdeResultSet(res);
                datos.add(tc);
            }
        } catch (Exception ex) {
            Logger.getLogger(PanelConductas.class.getName()).log(Level.SEVERE, null, ex);
        }
        Obj.cerrar(st, res);
        return datos;
    }

    @Override
    public boolean equals(Object obj) {
        boolean ret = false;
        if (obj instanceof Conducta) {
            Conducta c = (Conducta) obj;
            int id = Num.getInt(c.getId());
            //Si no tiene id sólo son iguales si son el mismo objeto
            if (id == 0) {
                ret = super.equals(obj);
            } else {
                //Si tiene id son iguales si las ids son iguales
                ret = c.getId().equals(getId());
            }
        }
        return ret;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + (this.codigo != null ? this.codigo.hashCode() : 0);
        hash = 17 * hash + (this.anoEscolar != null ? this.anoEscolar.hashCode() : 0);
        hash = 17 * hash + (this.tipo != null ? this.tipo.hashCode() : 0);
        hash = 17 * hash + (this.descripcion != null ? this.descripcion.hashCode() : 0);
        hash = 17 * hash + (this.acceso != null ? this.acceso.hashCode() : 0);
        return hash;
    }
}
