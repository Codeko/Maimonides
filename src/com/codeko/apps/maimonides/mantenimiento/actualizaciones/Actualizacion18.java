package com.codeko.apps.maimonides.mantenimiento.actualizaciones;

import com.codeko.apps.maimonides.mantenimiento.Actualizacion;

/**
 *
 * @author codeko
 */
public class Actualizacion18 extends Actualizacion {

    @Override
    public String getDescripcion() {
        return "Cambios en modelos de curso/grupo.";
    }

    @Override
    public boolean necesitaConfirmacion() {
        return false;
    }

    @Override
    public boolean ejecutar() {
        boolean ret = true;
        ret = ret & ejecutarSQL("ALTER TABLE `cursos` MODIFY COLUMN `curso` VARCHAR(100)  CHARACTER SET utf8 COLLATE utf8_bin NOT NULL;");
        ret = ret & ejecutarSQL("ALTER TABLE `unidades` MODIFY COLUMN `curso` VARCHAR(100)  CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL;");
        ret = ret & ejecutarSQL("ALTER TABLE `unidades` MODIFY COLUMN `cursogrupo` VARCHAR(101)  CHARACTER SET utf8 COLLATE utf8_bin NOT NULL;");
        if (!existeCampo("nombre_original", "unidades")) {
            ret = ret & ejecutarSQL("ALTER TABLE `unidades` ADD COLUMN `nombre_original` VARCHAR(255)  NOT NULL;");
        }
        return ret;
    }
}
