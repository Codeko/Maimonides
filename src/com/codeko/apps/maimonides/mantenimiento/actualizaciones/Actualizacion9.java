package com.codeko.apps.maimonides.mantenimiento.actualizaciones;

import com.codeko.apps.maimonides.mantenimiento.Actualizacion;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 * @version 20-07-2009
 */
public class Actualizacion9 extends Actualizacion {

    @Override
    public String getDescripcion() {
        return "Campos para la web";
    }

    @Override
    public boolean necesitaConfirmacion() {
        return false;
    }

    @Override
    public boolean ejecutar() {
        boolean ret = true;
        if (!existeCampo("email", "profesores")) {
            ret = ret & ejecutarSQL("ALTER TABLE `profesores` ADD COLUMN `email` VARCHAR(255) NOT NULL DEFAULT '' ");
            ret = ret & ejecutarSQL("CREATE OR REPLACE VIEW `profesores_` AS SELECT * FROM profesores WHERE fbaja IS NULL;");
        }

        return ret;
    }
}
