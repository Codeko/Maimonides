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

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.util.Num;
import com.codeko.util.Obj;
import com.mysql.jdbc.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author codeko
 */
public abstract class ObjetoBDConCod extends ObjetoBD implements IObjetoBDConCod {

    static boolean soloInsertar = false;

    public static boolean isSoloInsertar() {
        return ObjetoBDConCod.soloInsertar;
    }

    public static void setSoloInsertar(boolean soloInsertar) {
        ObjetoBDConCod.soloInsertar = soloInsertar;
    }

    public abstract boolean _guardar(boolean recrearEliminados);

    @Override
    public boolean guardar() {
        return guardar(false);
    }

    @Override
    public boolean guardar(boolean recrearEliminados) {
        boolean g = true;
        //Cargamos la ID si no existe
        cargarIdDesdeCod();
        if (isSoloInsertar() && Num.getInt(getId()) != 0) {
            g = false;
        }
        if (g) {
            return _guardar(recrearEliminados);
        }
        if(recrearEliminados) {
            recuperarBorrado();
        }
        return true;
    }

    @Override
    public int cargarIdDesdeCod() {
        if ((getId() == null || Num.getInt(getId()) == 0) && Num.getInt(getCodigo()) != 0) {
            String sql = "SELECT id FROM " + getTabla() + " WHERE ano=? AND cod=? ";
            PreparedStatement st = null;
            ResultSet res = null;
            try {
                st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
                st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
                st.setInt(2, getCodigo());
                res = st.executeQuery();
                if (res.next()) {
                    setId(res.getInt(1));
                }
            } catch (SQLException ex) {
                Logger.getLogger(Profesor.class.getName()).log(Level.SEVERE, null, ex);
            }
            Obj.cerrar(res, st);
        }
        return Num.getInt(getId());
    }

    @Override
    public boolean recuperarBorrado() {
        boolean ret = false;
        String sql = "UPDATE " + getTabla() + " SET fbaja=NULL WHERE ano=? AND id=? ";
        PreparedStatement st = null;

        try {
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
            st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
            st.setInt(2, getId());
            int res = st.executeUpdate();
            ret = res > 0;
        } catch (SQLException ex) {
            Logger.getLogger(Profesor.class.getName()).log(Level.SEVERE, null, ex);
        }
        Obj.cerrar(st);
        return ret;
    }
}
