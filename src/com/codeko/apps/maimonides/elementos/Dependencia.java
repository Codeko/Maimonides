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
public class Dependencia extends ObjetoBDConCod {

    @CdkAutoTablaCol(titulo = "Código")
    Integer codigo = null;
    String nombre = "";
    @CdkAutoTablaCol(titulo = "Descripción")
    String descripcion = "";
    @CdkAutoTablaCol(ignorar = true)
    AnoEscolar anoEscolar = MaimonidesApp.getApplication().getAnoEscolar();

    public static Dependencia getDependencia(int id) throws Exception {
        Object obj = Cache.get(Dependencia.class, id);
        if (obj != null) {
            return (Dependencia) obj;
        } else {
            Dependencia p = new Dependencia(id);
            return p;
        }
    }

    public Dependencia() {
    }

    private Dependencia(int id) throws Exception {
        PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM dependencias WHERE id=?");
        st.setInt(1, id);
        ResultSet res = st.executeQuery();
        if (res.next()) {
            cargarDesdeResultSet(res);
            Obj.cerrar(st, res);
        } else {
            Obj.cerrar(st, res);
            throw new InvalidParameterException("No existe ninguna dependencia con ID " + id);
        }
    }

    public Dependencia(AnoEscolar ano, String codigo) throws Exception {
        //TODO Implementar cache de este contructor
        PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM dependencias WHERE cod=? AND ano=?");
        st.setString(1, codigo);
        st.setInt(2, ano.getId());
        ResultSet res = st.executeQuery();
        if (res.next()) {
            cargarDesdeResultSet(res);
            Obj.cerrar(st, res);
        } else {
            Obj.cerrar(st, res);
            throw new InvalidParameterException("No existe ninguna dependencia con ID " + id);
        }
    }

    public static Dependencia getDependencia(AnoEscolar ano, String codigo) {
        Dependencia d = null;
        try {
            d=new Dependencia(ano, codigo);
        } catch (Exception ex) {
            Logger.getLogger(Unidad.class.getName()).log(Level.SEVERE, null, ex);
        }
        return d;
    }

    public final boolean cargarDesdeResultSet(ResultSet res) {
        boolean ret = true;
        try {
            setId(res.getInt("id"));
            setCodigo(res.getInt("cod"));
            setNombre(res.getString("nombre"));
            setDescripcion(res.getString("descripcion"));
            setAnoEscolar(AnoEscolar.getAnoEscolar(res.getInt("ano")));
            Cache.put(getClass(), getId(), this);
        } catch (Exception ex) {
            ret = false;
            Logger.getLogger(Dependencia.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    public static ArrayList<Dependencia> getDependencias() {
        ArrayList<Dependencia> dependencias = new ArrayList<Dependencia>();
        try {
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM dependencias AS d WHERE d.ano=? ");
            st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
            ResultSet res = st.executeQuery();
            while (res.next()) {
                Dependencia d = new Dependencia();
                if (d.cargarDesdeResultSet(res)) {
                    dependencias.add(d);
                }
            }
            Obj.cerrar(st, res);
        } catch (SQLException ex) {
            Logger.getLogger(Unidad.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dependencias;
    }

    public AnoEscolar getAnoEscolar() {
        return anoEscolar;
    }

    public void setAnoEscolar(AnoEscolar anoEscolar) {
        this.anoEscolar = anoEscolar;
    }

    @Override
    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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
        try {
            String sql = "UPDATE dependencias SET cod=?,ano=?,nombre=?,descripcion=? WHERE id=?";
            if (getId() == null) {
                sql = "INSERT INTO dependencias (cod,ano,nombre,descripcion,id) VALUES(?,?,?,?,?)";
            }
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            st.setObject(1, getCodigo());
            st.setInt(2, getAnoEscolar().getId());
            st.setString(3, getNombre());
            st.setString(4, getDescripcion());
            st.setObject(5, getId());
            ret = st.executeUpdate() > 0;
            if (ret && getId() == null) {
                setId((int) st.getLastInsertID());
            }
            st.close();
        } catch (SQLException ex) {
            Logger.getLogger(Dependencia.class.getName()).log(Level.SEVERE, "Error guardando datos de Dependencia: " + this, ex);
        }
        if (ret) {
            Cache.put(getClass(), getId(), this);
        }
        return ret;
    }

    @Override
    public String getNombreObjeto() {
        return "Dependencia";
    }

    @Override
    public String getDescripcionObjeto() {
        return getNombre() + ((Str.noNulo(getDescripcion()).trim().equals("")) ? "" : ": " + getDescripcion());
    }

    @Override
    public String toString() {
        return getDescripcionObjeto();
    }

    @Override
    public boolean equals(Object obj) {
        boolean ret = false;
        if (obj instanceof Dependencia) {
            if (getId() != null) {
                ret = getId().equals(((Dependencia) obj).getId());
            }
        }
        return ret;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 53 * hash + (this.codigo != null ? this.codigo.hashCode() : 0);
        hash = 53 * hash + (this.nombre != null ? this.nombre.hashCode() : 0);
        hash = 53 * hash + (this.anoEscolar != null ? this.anoEscolar.hashCode() : 0);
        hash = 53 * hash + (this.descripcion != null ? this.descripcion.hashCode() : 0);
        return hash;
    }

    @Override
    public String getTabla() {
        return "dependencias";
    }
}
