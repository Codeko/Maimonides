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


package com.codeko.apps.maimonides;

import com.codeko.util.Fechas;
import java.awt.Component;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import org.jdesktop.swingx.JXDatePicker;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class DateCellEditor extends AbstractCellEditor implements TableCellEditor {

    JXDatePicker editor = new JXDatePicker();

    public DateCellEditor() {
        super();
        SimpleDateFormat[] formatos = new SimpleDateFormat[]{new SimpleDateFormat("dd/MM/yy"),new SimpleDateFormat("EEEE dd/MM/yy"),   new SimpleDateFormat("dd-MM-yy"),new SimpleDateFormat("EEEE dd-MM-yy"), new SimpleDateFormat("ddMMyy")};
        editor.setFormats(formatos);
    }

    @Override
    public Object getCellEditorValue() {
        return Fechas.toGregorianCalendar(editor.getDate());
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if (value instanceof GregorianCalendar) {
            editor.setDate(((GregorianCalendar) value).getTime());
        } else {
            editor.setDate(null);
        }
        return editor;
    }
}
