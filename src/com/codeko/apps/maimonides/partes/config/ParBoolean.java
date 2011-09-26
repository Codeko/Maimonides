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

import com.codeko.swing.CdkAutoTablaCol;

/**
 *
 * @author Codeko <codeko@codeko.com>
 */
public class ParBoolean {

    @CdkAutoTablaCol(titulo = "Nombre")
    Object obj = null;
    @CdkAutoTablaCol(titulo = "No imprimir parte")
    Boolean valor = false;

    public Object getObj() {
        return obj;
    }

    public final void setObj(Object obj) {
        this.obj = obj;
    }

    public Boolean getValor() {
        return valor;
    }

    public final void setValor(Boolean valor) {
        this.valor = valor;
    }

    public ParBoolean(Object objr) {
        setObj(obj);
    }

    public ParBoolean(Object obj, boolean valor) {
        setObj(obj);
        setValor(valor);
    }
}
