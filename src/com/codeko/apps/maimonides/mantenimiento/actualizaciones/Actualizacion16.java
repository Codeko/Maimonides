package com.codeko.apps.maimonides.mantenimiento.actualizaciones;

import com.codeko.apps.maimonides.Conector;
import com.codeko.apps.maimonides.mantenimiento.Actualizacion;

/**
 *
 * @author codeko
 */
public class Actualizacion16 extends Actualizacion {

    @Override
    public String getDescripcion() {
        return "Versión 2.0.";
    }

    @Override
    public boolean necesitaConfirmacion() {
        return false;
    }

    @Override
    public boolean ejecutar() {
        boolean ret = true;
        //Quitamos todos los config de impresión de partes.
        ret = ret & ejecutarSQL("DELETE FROM config WHERE nombre LIKE 'Partes de __/__/____ impresos.'");
        ret = ret & Conector.crearTabla("usuarios");
        ret = ret & Conector.crearTabla("usuarios_profesores");
        ret = ret & ejecutarSQL("ALTER TABLE `materias` MODIFY COLUMN `curso_id` INTEGER UNSIGNED DEFAULT NULL");
        ret = ret & ejecutarSQL("ALTER TABLE `cursos` MODIFY COLUMN `cod` INTEGER UNSIGNED DEFAULT NULL");
        ret = ret & ejecutarSQL("ALTER TABLE `unidades` MODIFY COLUMN `curso` VARCHAR(10) DEFAULT NULL");
        ret = ret & ejecutarSQL("ALTER TABLE `unidades` MODIFY COLUMN `cod` INTEGER UNSIGNED DEFAULT NULL");
        ret = ret & ejecutarSQL("ALTER TABLE `unidades` MODIFY COLUMN `curso_id` INTEGER UNSIGNED DEFAULT NULL");
        ret = ret & ejecutarSQL("ALTER TABLE `cartas` MODIFY COLUMN `fecha` DATETIME  NOT NULL");
        return ret;
    }
}
