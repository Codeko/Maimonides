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
 *
 * @author codeko
 */
public class Actualizacion17 extends Actualizacion {

    @Override
    public String getDescripcion() {
        return "Envío de partes de convivencia a Séneca.";
    }

    @Override
    public boolean necesitaConfirmacion() {
        return false;
    }

    @Override
    public boolean ejecutar() {
        boolean ret = true;
        if (!existeCampo("situacion", "conv_partes")) {
            ret = ret & ejecutarSQL("ALTER TABLE `conv_partes` ADD COLUMN `situacion` INTEGER UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Situación de control' ");
        }
        return ret;
    }

    @Override
    public String getNotificacion(){
        return "<html>Para el correcto funcionamiento de los envíos de partes a Séneca es necesario:<ul><li>Volver a recuperar los datos de medidas y conductas desde Séneca en el panel de configuración pestaña Convivencia/Partes de convivencia</li><li>Para una correcta asignación de las medidas de convivencia tomadas es necesario asociar los periodos de expulsión a las medidas en el panel de configuración pestaña Convivencia/Generador de expulsiones</li></ul>";
    }
}
