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
package com.codeko.apps.maimonides.partes.config;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.elementos.AnoEscolar;
import com.codeko.util.Num;
import com.codeko.util.Str;
import java.util.ArrayList;

/**
 *
 * @author Codeko <codeko@codeko.com>
 */
public class Configuracion extends com.codeko.apps.maimonides.conf.Configuracion {

    public static ArrayList<Integer> getIdsProfesoresImpresionParteFiltrado(AnoEscolar ano) {
        return getIdsElementoImpresionParteFiltrado("profs", ano);
    }

    public static ArrayList<Integer> getIdsProfesoresImpresionParteFiltrado() {
        return getIdsElementoImpresionParteFiltrado("profs", null);
    }

    public static ArrayList<Integer> getIdsGruposImpresionParteFiltrado(AnoEscolar ano) {
        return getIdsElementoImpresionParteFiltrado("uds", ano);
    }

    public static ArrayList<Integer> getIdsGruposImpresionParteFiltrado() {
        return getIdsElementoImpresionParteFiltrado("uds", null);
    }

    public static ArrayList<Integer> getIdsAulasImpresionParteFiltrado(AnoEscolar ano) {
        return getIdsElementoImpresionParteFiltrado("aulas", ano);
    }

    public static ArrayList<Integer> getIdsAulasImpresionParteFiltrado() {
        return getIdsElementoImpresionParteFiltrado("aulas", null);
    }

    public static void setIdsProfesoresImpresionParteFiltrado(ArrayList<Integer> ids, AnoEscolar ano) {
        setIdsElementoImpresionParteFiltrado(ids, "profs", ano);
    }

    public static void setIdsProfesoresImpresionParteFiltrado(ArrayList<Integer> ids) {
        setIdsElementoImpresionParteFiltrado(ids, "profs", null);
    }

    public static void setIdsGruposImpresionParteFiltrado(ArrayList<Integer> ids, AnoEscolar ano) {
        setIdsElementoImpresionParteFiltrado(ids, "uds", ano);
    }

    public static void setIdsGruposImpresionParteFiltrado(ArrayList<Integer> ids) {
        setIdsElementoImpresionParteFiltrado(ids, "uds", null);
    }

    public static void setIdsAulasImpresionParteFiltrado(ArrayList<Integer> ids, AnoEscolar ano) {
        setIdsElementoImpresionParteFiltrado(ids, "aulas", ano);
    }

    public static void setIdsAulasImpresionParteFiltrado(ArrayList<Integer> ids) {
        setIdsElementoImpresionParteFiltrado(ids, "aulas", null);
    }

    private static ArrayList<Integer> getIdsElementoImpresionParteFiltrado(String elemento, AnoEscolar ano) {
        if (ano == null) {
            ano = MaimonidesApp.getApplication().getAnoEscolar();
        }
        ArrayList<Integer> ids = new ArrayList<Integer>();
        String strIds = MaimonidesApp.getApplication().getConfiguracion().get("filtro-imp-partes-" + elemento + "-ano-" + ano.getId(), "");
        if (!strIds.equals("")) {
            String[] strIdsArray = strIds.split(",");
            for (String id : strIdsArray) {
                int v = Num.getInt(id);
                if (v > 0) {
                    ids.add(v);
                }
            }
        }
        return ids;
    }

    private static void setIdsElementoImpresionParteFiltrado(ArrayList<Integer> ids, String elemento, AnoEscolar ano) {
        if (ano == null) {
            ano = MaimonidesApp.getApplication().getAnoEscolar();
        }
        MaimonidesApp.getApplication().getConfiguracion().set("filtro-imp-partes-" + elemento + "-ano-" + ano.getId(), Str.implode(ids, ","));
    }
}
