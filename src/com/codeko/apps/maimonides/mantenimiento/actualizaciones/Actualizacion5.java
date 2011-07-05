package com.codeko.apps.maimonides.mantenimiento.actualizaciones;

import com.codeko.apps.maimonides.mantenimiento.Actualizacion;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class Actualizacion5 extends Actualizacion{

    @Override
    public String getDescripcion() {
        return "Nuevos campos aulas";
    }

    @Override
    public boolean necesitaConfirmacion() {
        return false;
    }

    @Override
    public boolean ejecutar() {
        boolean ret=true;
        ret=ret& ejecutarSQL("ALTER TABLE `dependencias` ADD COLUMN `descripcion` VARCHAR(255) NOT NULL DEFAULT ''");
        return ret;
    }

}
