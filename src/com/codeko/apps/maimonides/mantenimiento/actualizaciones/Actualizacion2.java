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
