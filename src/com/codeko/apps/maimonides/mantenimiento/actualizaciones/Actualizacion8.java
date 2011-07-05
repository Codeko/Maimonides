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
