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
import com.codeko.apps.maimonides.excepciones.NoExisteElementoException;
import com.codeko.swing.IObjetoTabla;
import com.codeko.util.Num;
import com.codeko.util.Obj;
import com.codeko.util.Str;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Codeko
 */
public class AnoEscolar extends MaimonidesBean implements IObjetoTabla, Cloneable {

    Integer id = null;
    Integer ano = new GregorianCalendar().get(GregorianCalendar.YEAR);
    String nombre = "Año " + ano + "-" + (ano + 1);

    public static AnoEscolar getAnoEscolar(int id) throws Exception {
        Object obj = Cache.get(AnoEscolar.class, id);
        if (obj != null) {
            return (AnoEscolar) obj;
        } else {
            AnoEscolar a = new AnoEscolar(id);
            return a;
        }
    }

    public static AnoEscolar getAnoEscolar() {
        Statement st = null;
        ResultSet res = null;
        try {
            st = (Statement) MaimonidesApp.getApplication().getConector().getConexion().createStatement();
            res = st.executeQuery("SELECT id FROM anos ORDER BY ano DESC LIMIT 0,1");
            Integer id = null;
            if (res.next()) {
                id = res.getInt("id");
            }
            if (id != null) {
                Object obj = Cache.get(AnoEscolar.class, id);
                if (obj != null) {
                    return (AnoEscolar) obj;
                } else {
                    AnoEscolar a = new AnoEscolar(id);
                    return a;
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AnoEscolar.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            Obj.cerrar(st, res);
        }
        return null;
    }

    public AnoEscolar() {
    }

    private AnoEscolar(int id) throws SQLException, NoExisteElementoException {
        PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM anos WHERE id=? LIMIT 1");
        st.setInt(1, id);
        ResultSet res = st.executeQuery();
        if (res.next()) {
            cargarDesdeResultSet(res);
            Obj.cerrar(st, res);
        } else {
            Obj.cerrar(st, res);
            throw new NoExisteElementoException("No existe el año escolar " + id);
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    @Override
    public int getNumeroDeCampos() {
        return 3;
    }

    @Override
    public Object getValueAt(int index) {
        Object val = null;
        switch (index) {
            case 0:
                val = getId();
                break;
            case 1:
                val = getAno();
                break;
            case 2:
                val = getNombre();
                break;

        }
        return val;
    }

    @Override
    public String getTitleAt(int index) {
        String val = null;
        switch (index) {
            case 0:
                val = "Código";
                break;
            case 1:
                val = "Año";
                break;
            case 2:
                val = "Nombre";
                break;

        }
        return val;
    }

    @Override
    public Class getClassAt(int index) {
        Class val = null;
        switch (index) {
            case 0:
                val = Integer.class;
                break;
            case 1:
                val = Integer.class;
                break;
            case 2:
                val = String.class;
                break;
        }
        return val;
    }

    @Override
    public boolean setValueAt(int index, Object valor) {
        boolean hayCambios = true;
        switch (index) {
            case 0:
                setId(Num.getInt(valor));
                break;
            case 1:
                setAno(Num.getInt(valor));
                break;
            case 2:
                setNombre(Str.noNulo(valor));
                break;
            default:
                hayCambios = false;
        }
        if (hayCambios) {
            return guardar();
        }
        return false;
    }

    public final void cargarDesdeResultSet(ResultSet res) throws SQLException {
        if (res != null) {
            setId(res.getInt("id"));
            setNombre(res.getString("nombre"));
            setAno(res.getInt("ano"));
            Cache.put(getClass(), getId(), this, 60);
        }
    }

    public boolean guardar() {
        boolean ret = false;
        try {
            String sql = "UPDATE anos SET nombre=?,ano=? WHERE id=?";
            if (getId() == null) {
                sql = "INSERT INTO anos (nombre,ano,id) VALUES(?,?,?)";
            }
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            st.setString(1, getNombre());
            st.setInt(2, getAno());
            st.setObject(3, getId());
            ret = st.executeUpdate() > 0;
            if (ret && getId() == null) {
                setId((int) st.getLastInsertID());
            }
            st.close();
        } catch (SQLException ex) {
            ret = false;
            Logger.getLogger(AnoEscolar.class.getName()).log(Level.SEVERE, "Error guardando AnoEscolar: " + this, ex);
        }
        if (ret) {
            Cache.put(getClass(), getId(), this, 60);
        }
        return ret;
    }

    public boolean borrar() {
        try {
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("DELETE FROM anos WHERE id=?");
            st.setObject(1, getId());
            boolean ret = st.executeUpdate() > 0;
            //Ahora vemos si es el año asignado
            if (MaimonidesApp.getApplication().getAnoEscolar() != null && MaimonidesApp.getApplication().getAnoEscolar().getId() == getId()) {
                MaimonidesApp.getApplication().setAno(null);
            }
            return ret;
        } catch (SQLException ex) {
            Logger.getLogger(AnoEscolar.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public boolean esCampoEditable(int index) {
        boolean val = index > 0;
        return val;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getNombre());
        sb.append("[");
        sb.append(getAno());
        sb.append(":");
        sb.append(getId());
        sb.append("]");
        return sb.toString();
    }
}
