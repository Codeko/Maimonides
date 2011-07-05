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

package com.codeko.apps.maimonides.asistencia;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.util.Num;

public class ConfiguracionAsistencia {

    public static final String AAE_PARAMETRO = "accion_asistencia_expulsados";
    public static final int AAE_DEJAR_ASISTENCIA = 0;
    public static final int AAE_ADVERTENCIA = 1;
    public static final int AAE_ASIGNAR_EXPULSION = 2;
    public static final int AAE_ASIGNAR_EXPULSION_ADVERTENCIA = 3;

    public static int getAccionAsistenciaEnExpulsados() {
        return Num.getInt(MaimonidesApp.getApplication().getConfiguracion().get(AAE_PARAMETRO, AAE_ASIGNAR_EXPULSION + ""));
    }

    public static boolean isMostrarAdvertenciasAsignacionAsistenciaEnExpulsados() {
        return getAccionAsistenciaEnExpulsados() == AAE_ADVERTENCIA || getAccionAsistenciaEnExpulsados() == AAE_ASIGNAR_EXPULSION_ADVERTENCIA;
    }
}
