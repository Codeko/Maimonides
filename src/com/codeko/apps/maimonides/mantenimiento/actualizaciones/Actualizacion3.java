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
public class Actualizacion3 extends Actualizacion {

    @Override
    public String getDescripcion() {
        return "Nuevos campos para la ficha de alumnos";
    }

    @Override
    public boolean necesitaConfirmacion() {
        return false;
    }

    @Override
    public boolean ejecutar() {
        boolean ret = true;
        String sql = "ALTER TABLE `alumnos` ADD COLUMN `obs` LONGTEXT AFTER `poblacion`";
        PreparedStatement st = null;
        try {
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
            st.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(Actualizacion3.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            Obj.cerrar(st);
        }
        return ret;
    }
}
