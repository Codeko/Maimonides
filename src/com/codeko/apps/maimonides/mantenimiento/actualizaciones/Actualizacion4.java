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

import com.codeko.apps.maimonides.mantenimiento.Actualizacion;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class Actualizacion4 extends Actualizacion {

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
        boolean ret = false;
        String sql = "ALTER TABLE `alumnos` ADD COLUMN `dni` VARCHAR(45) AFTER `obs`, " +
                " ADD COLUMN `fnacimiento` DATE AFTER `dni`, " +
                " ADD COLUMN `loc_nacimiento` VARCHAR(255) NOT NULL DEFAULT '' AFTER `fnacimiento`, " +
                " ADD COLUMN `prov_nacimiento` VARCHAR(255) NOT NULL DEFAULT '' AFTER `loc_nacimiento`, " +
                " ADD COLUMN `pais_nacimiento` VARCHAR(255) NOT NULL DEFAULT '' AFTER `prov_nacimiento`, " +
                " ADD COLUMN `nacionalidad` VARCHAR(255) NOT NULL DEFAULT '' AFTER `pais_nacimiento`, " +
                " ADD COLUMN `sexo` ENUM('H','M') NOT NULL  AFTER `nacionalidad`, " +
                " ADD COLUMN `telefono_urgencia` VARCHAR(45) NOT NULL DEFAULT '' AFTER `sexo`, " +
                " ADD COLUMN `expediente` VARCHAR(45) NOT NULL DEFAULT '' AFTER `telefono_urgencia`, " +
                " ADD COLUMN `t1_dni` VARCHAR(45) NOT NULL DEFAULT '' AFTER `expediente`, " +
                " ADD COLUMN `t1_nombre` VARCHAR(255) NOT NULL DEFAULT '' AFTER `t1_dni`, " +
                " ADD COLUMN `t1_apellido1` VARCHAR(255) NOT NULL DEFAULT '' AFTER `t1_nombre`, " +
                " ADD COLUMN `t1_apellido2` VARCHAR(255) NOT NULL DEFAULT '' AFTER `t1_apellido1`, " +
                " ADD COLUMN `t1_sexo` ENUM('H','M') NOT NULL AFTER `t1_apellido2`, " +
                " ADD COLUMN `t2_dni` VARCHAR(45) NOT NULL DEFAULT '' AFTER `t1_sexo`, " +
                " ADD COLUMN `t2_nombre` VARCHAR(255) NOT NULL DEFAULT '' AFTER `t2_dni`, " +
                " ADD COLUMN `t2_apellido1` VARCHAR(255) NOT NULL DEFAULT '' AFTER `t2_nombre`, " +
                " ADD COLUMN `t2_apellido2` VARCHAR(255) NOT NULL DEFAULT '' AFTER `t2_apellido1`, " +
                " ADD COLUMN `t2_sexo` ENUM('H','M') NOT NULL  AFTER `t2_apellido2`, " +
                " MODIFY COLUMN `cod` INTEGER UNSIGNED NOT NULL DEFAULT 0,  DROP INDEX `cod`,MODIFY COLUMN `unidad_id` INTEGER UNSIGNED, MODIFY COLUMN `curso_id` INTEGER UNSIGNED ";
        ret = ejecutarSQL(sql);
        ret= ret & ejecutarSQL("ALTER TABLE `profesores` ADD COLUMN `fbaja` DATE AFTER `ftoma`;");
        ret= ret & ejecutarSQL("CREATE VIEW `profesores_` AS SELECT * FROM profesores WHERE fbaja IS NULL;");
        return ret;
    }
}
