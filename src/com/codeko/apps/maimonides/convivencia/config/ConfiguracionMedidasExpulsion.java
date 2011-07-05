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


package com.codeko.apps.maimonides.convivencia.config;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.convivencia.Conducta;
import com.codeko.swing.CdkAutoTablaCol;
import com.codeko.util.Num;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author codeko
 */
public class ConfiguracionMedidasExpulsion {

    @CdkAutoTablaCol(titulo = "Días")
    int dias = 0;
    Conducta medida = null;

    public int getDias() {
        return dias;
    }

    public void setDias(int dias) {
        this.dias = dias;
    }

    public Conducta getMedida() {
        return medida;
    }

    public void setMedida(Conducta medida) {
        this.medida = medida;
    }

    public static Integer getIdMedidaParaExpulsion(int diasExpulsion) {
        Integer c = null;
        //recuperamos la configuración de las medidas asociadas a las expulsiones
        String dato = MaimonidesApp.getApplication().getConfiguracion().get(ConfiguracionMedidasExpulsion.getParametroExpulsionesMedidas(), "");
        String[] bloques = dato.split(",");
        //De cada bloque sacamos los dos datos
        for (String bloque : bloques) {
            if (!bloque.trim().equals("")) {
                String[] d = bloque.split("=");
                int dias = Num.getInt(d[0]);
                int idMedida = Num.getInt(d[1]);
                if (idMedida > 0 && dias == diasExpulsion) {
                    c = idMedida;
                    break;
                }
            }
        }
        return c;
    }

    public static String getParametroExpulsionesMedidas() {
        //Esta configuración es dependiente del año escopar
        if (MaimonidesApp.getApplication().getAnoEscolar() != null) {
            return "convivencia_expulsiones_medidas[" + MaimonidesApp.getApplication().getAnoEscolar().getId() + "]";
        }
        return "convivencia_expulsiones_medidas";
    }

    public static String getParametroExpulsionesMedidaPorDefecto() {
        //Esta configuración es dependiente del año escopar
        if (MaimonidesApp.getApplication().getAnoEscolar() != null) {
            return "convivencia_expulsiones_medida_defecto[" + MaimonidesApp.getApplication().getAnoEscolar().getId() + "]";
        }
        return "convivencia_expulsiones_medida_defecto";
    }

    public static Conducta getMedidaParaExpulsion(int diasExpulsion) {
        Integer id = getIdMedidaParaExpulsion(diasExpulsion);
        Conducta c = null;
        if (id != null) {
            try {
                c = new Conducta(id);
            } catch (Exception ex) {
                Logger.getLogger(ConfiguracionMedidasExpulsion.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //Si no hay medida vemos si hay una por defecto
        if (c == null) {
            int medida = Num.getInt(MaimonidesApp.getApplication().getConfiguracion().get(ConfiguracionMedidasExpulsion.getParametroExpulsionesMedidaPorDefecto(), ""));
            if (medida > 0) {
                try {
                    c = new Conducta(medida);
                } catch (Exception ex) {
                    Logger.getLogger(ConfiguracionMedidasExpulsion.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return c;
    }
}
