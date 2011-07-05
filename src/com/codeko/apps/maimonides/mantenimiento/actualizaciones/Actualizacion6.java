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
public class Actualizacion6 extends Actualizacion {

    @Override
    public String getDescripcion() {
        return "Mejoras en sistema de horarios";
    }

    @Override
    public boolean necesitaConfirmacion() {
        return false;
    }

    @Override
    public boolean ejecutar() {
        boolean ret = true;
        ret = ret && ejecutarSQL("ALTER TABLE `partes_horarios` MODIFY COLUMN `firmado` TINYINT(1) NOT NULL DEFAULT 1;");
        ret = ret && ejecutarSQL("ALTER TABLE `horarios` ADD COLUMN `falta` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, ADD COLUMN `fborrado` TIMESTAMP ");
        ret = ret && ejecutarSQL("CREATE VIEW `horarios_` AS SELECT * FROM horarios WHERE borrado=0 ");
        //Se borran las asignaciones con horarios que van con unidades que no corresponde
        ret= ret && ejecutarSQL("DELETE alumnos_horarios FROM alumnos_horarios JOIN alumnos AS a ON alumnos_horarios.alumno_id=a.id JOIN horarios AS h ON h.id=alumnos_horarios.horario_id WHERE h.unidad_id!=a.unidad_id ");
        return true;
    }
}
