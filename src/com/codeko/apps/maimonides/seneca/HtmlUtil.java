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


package com.codeko.apps.maimonides.seneca;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.Attributes;
import net.htmlparser.jericho.Element;

/**
 * Utilidades para procesado de HTML
 * @author codeko
 */
public class HtmlUtil {

    /**
     * Informa de si un elemento option de un select está seleccionado
     * @param option
     * @return
     */
    public static boolean isSelected(Element option) {

        Attributes atts = option.getAttributes();
        for (int ci = 0; ci < atts.getCount(); ci++) {
            Attribute att = atts.get(ci);
            if (att.getName().toLowerCase().equals("selected")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Devuelve el valor seleccionado de un elemento select
     * @param select
     * @return el valor del option seleccionado o nulo si ninguno lo esta
     */
    public static String getSelectedVal(Element select) {
        String val = null;
        List<Element> lOpt = select.getAllElements("option");
        Iterator<Element> iOpt = lOpt.iterator();
        while (iOpt.hasNext()) {
            Element opt = iOpt.next();
            String sVal = opt.getAttributeValue("value");
            if (HtmlUtil.isSelected(opt)) {
                val = sVal;
                break;
            }
        }
        return val;
    }
    /**
     * Devuelve todos los valores de los option de un <select>
     * @param select
     * @return
     */
    public static ArrayList<String> getSelectValues(Element select) {
        return HtmlUtil.getSelectValues(select, "");
    }

    /**
     * Devuelve todos los valores de los option de un <select>
     * @param select
     * @param exclude Valor a excluir
     * @return
     */
    public static ArrayList<String> getSelectValues(Element select, String exclude) {
        ArrayList<String> vals = new ArrayList<String>();
        List<Element> lOpt = select.getAllElements("option");
        Iterator<Element> iOpt = lOpt.iterator();
        while (iOpt.hasNext()) {
            Element opt = iOpt.next();
            String sVal = opt.getAttributeValue("value");
            if (!sVal.equals(exclude)) {
                vals.add(sVal);
            }

        }
        return vals;
    }
}
