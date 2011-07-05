package com.codeko.apps.maimonides.mantenimiento.actualizaciones;

import com.codeko.apps.maimonides.mantenimiento.Actualizacion;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class Actualizacion8 extends Actualizacion {

    @Override
    public String getDescripcion() {
        return "Campos de pérdida de escolaridad";
    }

    @Override
    public boolean necesitaConfirmacion() {
        return false;
    }

    @Override
    public boolean ejecutar() {
        boolean ret = true;
        if (!existeCampo("maxFaltas", "cursos")) {
            ret = ret & ejecutarSQL("ALTER TABLE `cursos` ADD COLUMN `maxFaltas` INTEGER UNSIGNED NOT NULL DEFAULT 0 ");
        }

        if (!existeCampo("maxFaltas", "materias")) {
            ret = ret & ejecutarSQL("ALTER TABLE `materias` ADD COLUMN `maxFaltas` INTEGER UNSIGNED NOT NULL DEFAULT 0 ");
        }
//            ALTER TABLE `mmp1`.`alumnos` MODIFY COLUMN `apellido1` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '',
// MODIFY COLUMN `apellido2` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '',
// MODIFY COLUMN `numescolar` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '';
        return ret;
    }
}
