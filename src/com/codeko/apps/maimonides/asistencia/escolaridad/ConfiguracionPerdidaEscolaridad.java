/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codeko.apps.maimonides.asistencia.escolaridad;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.elementos.ParteFaltas;
import com.codeko.util.Num;
import com.codeko.util.Str;
import java.util.ArrayList;

/**
 *
 * @author codeko
 */
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
