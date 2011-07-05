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
