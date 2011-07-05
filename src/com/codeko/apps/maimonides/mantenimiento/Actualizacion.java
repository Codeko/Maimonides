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


package com.codeko.apps.maimonides.mantenimiento;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesBean;
import com.codeko.util.Obj;
import com.mysql.jdbc.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public abstract class Actualizacion extends MaimonidesBean implements IActualizacion {

    protected boolean ejecutarSQL(String sql) {
        boolean ret = false;
        PreparedStatement st = null;
        try {
            st = (PreparedStatement) com.codeko.apps.maimonides.MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
            st.executeUpdate();
            ret = true;
        } catch (SQLException ex) {
            Logger.getLogger(Actualizacion.class.getName()).log(Level.SEVERE, "Error ejecutando SQL: '" + sql + "'", ex);
        } finally {
            Obj.cerrar(st);
        }
        return ret;
    }

    public static boolean existeCampo(String campo, String tabla) {
        boolean ret = false;
        PreparedStatement st = null;
        ResultSet res = null;
        try {
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT " + campo + " FROM " + tabla + " LIMIT 0,1");
            res = st.executeQuery();
            ret = true;
        } catch (SQLException ex) {
        }
        Obj.cerrar(st, res);
        return ret;
    }

    public String getNotificacion(){
        return null;
    }
    
}
