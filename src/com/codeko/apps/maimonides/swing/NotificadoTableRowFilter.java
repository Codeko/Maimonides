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


/*
 * Copyright (C) 2011 Codeko <codeko@codeko.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.codeko.apps.maimonides.swing;

import com.codeko.apps.maimonides.elementos.INotificado;
import com.codeko.swing.CodekoAutoTableModel;
import com.codeko.swing.CodekoTableModel;
import com.codeko.util.Num;
import org.jdesktop.swingx.sort.RowFilters.GeneralFilter;

/**
 *
 * @author Codeko <codeko@codeko.com>
 */
public class NotificadoTableRowFilter extends GeneralFilter {

    private boolean soloNotificados = false;

    @Override
    protected boolean include(Entry<? extends Object, ? extends Object> value, int index) {
        if (!isSoloNotificados()) {
            return true;
        }
        Object obj = null;
        int row = Num.getInt(value.getIdentifier());
        if (value.getModel() instanceof CodekoAutoTableModel) {
            obj = ((CodekoAutoTableModel) value.getModel()).getElemento(row);
        } else if (value.getModel() instanceof CodekoTableModel) {
            obj = ((CodekoTableModel) value.getModel()).getElemento(row);
        }
        if (obj instanceof INotificado) {
            INotificado dato = (INotificado) obj;
            return !dato.isNotificado();
        }
        return true;
    }

    /**
     * @return the soloNotificados
     */
    public boolean isSoloNotificados() {
        return soloNotificados;
    }

    /**
     * @param soloNotificados the soloNotificados to set
     */
    public void setSoloNotificados(boolean soloNotificados) {
        this.soloNotificados = soloNotificados;
    }
}
