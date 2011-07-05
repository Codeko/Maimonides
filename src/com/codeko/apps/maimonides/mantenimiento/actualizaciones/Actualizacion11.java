package com.codeko.apps.maimonides.mantenimiento.actualizaciones;

import com.codeko.apps.maimonides.mantenimiento.Actualizacion;

/**
 *
 * @author codeko
 */
public class Actualizacion11 extends Actualizacion {

    @Override
    public String getDescripcion() {
        return "Dependencias no necesiten código Séneca";
    }

    @Override
    public boolean necesitaConfirmacion() {
        return false;
    }

    @Override
    public boolean ejecutar() {
        boolean ret = true;
        ret = ret & ejecutarSQL("ALTER TABLE `dependencias` MODIFY COLUMN `cod` INTEGER UNSIGNED DEFAULT NULL;");
        return ret;
    }
}
