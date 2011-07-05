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


package com.codeko.apps.maimonides.mantenimiento.actualizaciones;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.mantenimiento.Actualizacion;
import com.codeko.util.Obj;

import com.mysql.jdbc.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class Actualizacion2 extends Actualizacion {

    @Override
    public boolean ejecutar() {
        boolean ret = true;
        String sql = "ALTER TABLE `alumnos` ADD COLUMN `email` VARCHAR(255) NOT NULL  DEFAULT '' AFTER `borrado`," +
                " ADD COLUMN `telefono` VARCHAR(255) NOT NULL  DEFAULT '' AFTER `email`," +
                " ADD COLUMN `sms` VARCHAR(255) NOT NULL  DEFAULT '' AFTER `telefono`," +
                " ADD COLUMN `direccion` VARCHAR(255) NOT NULL  DEFAULT '' AFTER `sms`," +
                " ADD COLUMN `cp` VARCHAR(10) NOT NULL  DEFAULT '' AFTER `direccion`," +
                " ADD COLUMN `poblacion` VARCHAR(255) NOT NULL  DEFAULT '' AFTER `cp`," +
                " ADD COLUMN `notificar` INTEGER UNSIGNED NOT NULL DEFAULT 1 AFTER `cp`";
        PreparedStatement st = null;
        try {
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
            st.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(Actualizacion2.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            Obj.cerrar(st);
        }
        return ret;
    }

    @Override
    public String getDescripcion() {
        return "Añade los nuevos campos en fichero de alumnos.";
    }

    @Override
    public boolean necesitaConfirmacion() {
        return false;
    }
}
