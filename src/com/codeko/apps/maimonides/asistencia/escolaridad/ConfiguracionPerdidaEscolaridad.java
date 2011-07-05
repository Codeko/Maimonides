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

package com.codeko.apps.maimonides.asistencia.escolaridad;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.elementos.ParteFaltas;
import com.codeko.util.Num;
import com.codeko.util.Str;
import java.util.ArrayList;

public class ConfiguracionPerdidaEscolaridad {

    static ArrayList<Integer> tipos = null;

    public static void setTipoFaltasContabilizables(ArrayList<Integer> tipos) {
        String val=Str.implode(tipos, ",", "-1");
        MaimonidesApp.getApplication().getConfiguracion().set("faltas_escolaridad_contabilizables", val);
        ConfiguracionPerdidaEscolaridad.tipos = tipos;
    }

    public static ArrayList<Integer> getTiposFaltasContabilizables() {
        if (ConfiguracionPerdidaEscolaridad.tipos == null) {
            String sTipos = MaimonidesApp.getApplication().getConfiguracion().get("faltas_escolaridad_contabilizables", ParteFaltas.FALTA_INJUSTIFICADA + "");
            String[] aTipos = sTipos.split(",");
            ConfiguracionPerdidaEscolaridad.tipos = new ArrayList<Integer>();
            for (String s : aTipos) {
                ConfiguracionPerdidaEscolaridad.tipos.add(Num.getInt(s));
            }
        }
        return ConfiguracionPerdidaEscolaridad.tipos;
    }

    public static String getTiposFaltasContabilizablesSQL() {
        return Str.implode(ConfiguracionPerdidaEscolaridad.getTiposFaltasContabilizables(), ",", "-1");
    }
}
