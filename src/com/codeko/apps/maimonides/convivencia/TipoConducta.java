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


package com.codeko.apps.maimonides.convivencia;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.elementos.AnoEscolar;
import com.codeko.apps.maimonides.elementos.ObjetoBD;
import com.codeko.swing.CdkAutoTablaCol;
import com.codeko.util.Obj;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.Statement;
import java.security.InvalidParameterException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class TipoConducta extends ObjetoBD {

    @CdkAutoTablaCol(ignorar = true)
    public static final int GRAVEDAD_INDEFINIDA = 0;
    @CdkAutoTablaCol(ignorar = true)
    public static final int GRAVEDAD_LEVE = 1;
    @CdkAutoTablaCol(ignorar = true)
    public static final int GRAVEDAD_GRAVE = 2;
    @CdkAutoTablaCol(ignorar = true)
    public static final int TIPO_CONDUCTA = 0;
    @CdkAutoTablaCol(ignorar = true)
    public static final int TIPO_MEDIDA = 1;
    @CdkAutoTablaCol(titulo = "Código")
    String codigo = "";
    @CdkAutoTablaCol(ignorar = true)
    AnoEscolar anoEscolar = MaimonidesApp.getApplication().getAnoEscolar();
    @CdkAutoTablaCol(ignorar = true)
    Integer tipo = TIPO_CONDUCTA;
    @CdkAutoTablaCol(titulo = "Descripción")
    String descripcion = "";
    @CdkAutoTablaCol(titulo = "Gravedad")
    Integer gravedad = 0;//0 Indefinido, 1 Leve ,2 Grave
    @CdkAutoTablaCol(ignorar = true)
    private static boolean cacheActiva = false;
    @CdkAutoTablaCol(ignorar = true)
    private static HashMap<Integer, TipoConducta> cache = new HashMap<Integer, TipoConducta>();

    public static boolean isCacheActiva() {
        return cacheActiva;
    }

    public static void setCacheActiva(boolean cacheActiva) {
        TipoConducta.cacheActiva = cacheActiva;
        if (!cacheActiva) {
            cache.clear();
        }
    }

    public static TipoConducta getTipoConducta(int id) throws Exception {
        if (isCacheActiva() && cache.containsKey(id)) {
            return cache.get(id);
        } else {
            TipoConducta p = new TipoConducta(id);
            if (isCacheActiva()) {
                cache.put(id, p);
            }
            return p;
        }
    }

    public static TipoConducta geTipoConducta(AnoEscolar ano, String codigo, int tipo) {
        TipoConducta t = null;
        try {
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM conv_tipos WHERE ano=? AND cod=? AND tipo=? ");
            st.setInt(1, ano.getId());
            st.setString(2, codigo);
            st.setInt(3, tipo);
            ResultSet res = st.executeQuery();
            if (res.next()) {
                t = new TipoConducta();
                t.cargarDesdeResultSet(res);
            }
            Obj.cerrar(st, res);
        } catch (Exception ex) {
            Logger.getLogger(TipoConducta.class.getName()).log(Level.SEVERE, null, ex);
        }
        return t;
    }

    public static TipoConducta geTipoConducta(AnoEscolar ano, int tipo, String nombre) {
        TipoConducta t = null;
        try {
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM conv_tipos WHERE ano=? AND descripcion=? AND tipo=? ");
            st.setInt(1, ano.getId());
            st.setString(2, nombre);
            st.setInt(3, tipo);
            ResultSet res = st.executeQuery();
            if (res.next()) {
                t = new TipoConducta();
                t.cargarDesdeResultSet(res);
            }
            Obj.cerrar(st, res);
        } catch (Exception ex) {
            Logger.getLogger(TipoConducta.class.getName()).log(Level.SEVERE, null, ex);
        }
        return t;
    }

    public TipoConducta() {
    }

    public TipoConducta(int id) throws Exception {
        PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM conv_tipos WHERE id=? ");
        st.setInt(1, id);
        ResultSet res = st.executeQuery();
        if (res.next()) {
            cargarDesdeResultSet(res);
        } else {
            throw new InvalidParameterException("No existe ningun tipo de conducta con ID " + id);
        }
        Obj.cerrar(st, res);
    }

    public final void cargarDesdeResultSet(ResultSet res) throws SQLException, Exception {
        setId(res.getInt("id"));
        setCodigo(res.getString("cod"));
        setDescripcion(res.getString("descripcion"));
        setAnoEscolar(AnoEscolar.getAnoEscolar(res.getInt("ano")));
        setTipo(res.getInt("tipo"));
        setGravedad(res.getInt("gravedad"));
    }

    public Integer getGravedad() {
        return gravedad;
    }

    public void setGravedad(Integer gravedad) {
        this.gravedad = gravedad;
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
            String sql = "UPDATE conv_tipos SET cod=?,ano=?,tipo=?,gravedad=?,descripcion=? WHERE id=?";
            if (getId() == null) {
                sql = "INSERT INTO conv_tipos (cod,ano,tipo,gravedad,descripcion,id) VALUES(?,?,?,?,?,?)";
            }
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            st.setObject(1, getCodigo());
            st.setInt(2, getAnoEscolar().getId());
            st.setInt(3, getTipo());
            st.setInt(4, getGravedad());
            st.setString(5, getDescripcion());
            st.setObject(6, getId());
            ret = st.executeUpdate() > 0;
            if (ret && getId() == null) {
                setId((int) st.getLastInsertID());
            }
            st.close();
        } catch (SQLException ex) {
            Logger.getLogger(TipoConducta.class.getName()).log(Level.SEVERE, "Error guardando datos de Tipo de Conducta: " + this, ex);
        }
        return ret;
    }

    @Override
    public String getNombreObjeto() {
        return "Tipo de Conducta/Medida disciplinaria";
    }

    @Override
    public String getDescripcionObjeto() {
        return getDescripcion();
    }

    @Override
    public String toString() {
        return getDescripcionObjeto();
    }

    public static ArrayList<TipoConducta> getTiposConducta(int tipo) {
        ArrayList<TipoConducta> tipos = new ArrayList<TipoConducta>();
        try {
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM conv_tipos WHERE ano=? AND tipo=? ");
            st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
            st.setInt(2, tipo);
            ResultSet res = st.executeQuery();
            while (res.next()) {
                TipoConducta u = new TipoConducta();
                u.cargarDesdeResultSet(res);
                tipos.add(u);
            }
            Obj.cerrar(st, res);
        } catch (Exception ex) {
            Logger.getLogger(TipoConducta.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tipos;
    }

    public static String getNombreGravedad(int gravedad) {
        switch (gravedad) {
            case GRAVEDAD_LEVE:
                return "Leve";
            case GRAVEDAD_GRAVE:
                return "Grave";
            case GRAVEDAD_INDEFINIDA:
            default:
                return "";
        }
    }

    @Override
    public String getTabla() {
        return "conv_tipos";
    }
}
