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
package com.codeko.apps.maimonides.dnie;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.util.Num;

/**
 *
 * @author Codeko <codeko@codeko.com>
 */
public class Configuracion extends com.codeko.apps.maimonides.conf.Configuracion {

    public static final int DNIE_PASS_POLICY_NO_PASS = 0;
    public static final int DNIE_PASS_POLICY_PASS_FOR_EXTERNAL = 1;
    public static final int DNIE_PASS_POLICY_PASS_FOR_ALL = 2;

    public static boolean isGlobalDNIeAccessEnabled() {
        return Num.getInt(MaimonidesApp.getApplication().getConfiguracion().get("dnie_enabled", "1")) > 0;
    }

    public static boolean isDNIeAccessEnabled() {
        return Num.getInt(getLocal(Configuracion.class, "dnie_enabled", "1")) > 0 && isGlobalDNIeAccessEnabled();
    }

    public static void setDNIeAccessEnabled(boolean enabled) {
        setLocal(Configuracion.class, "dnie_enabled", enabled ? "1" : "0");
    }

    public static void setGlobalDNIeAccessEnabled(boolean enabled) {
        MaimonidesApp.getApplication().getConfiguracion().set("dnie_enabled", enabled ? "1" : "0");
    }

    public static int getDNIePassPolicy() {
        return Num.getInt(MaimonidesApp.getApplication().getConfiguracion().get("dnie_pass_policy", "" + DNIE_PASS_POLICY_NO_PASS));
    }

    public static void setDNIePassPolicy(int policy) {
        MaimonidesApp.getApplication().getConfiguracion().set("dnie_pass_policy", "" + policy);
    }
}
