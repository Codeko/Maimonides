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
