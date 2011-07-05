package com.codeko.apps.maimonides.impresion;

import com.codeko.util.Fechas;
import com.codeko.util.Str;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import javax.swing.table.TableModel;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRRewindableDataSource;

public class JRModeloTablaDS implements JRRewindableDataSource {

    /**
     *
     */
    private TableModel tableModel = null;
    private int index = -1;
    private HashMap<String, Integer> columnNames = new HashMap<String, Integer>();

    /**
     *
     */
    public JRModeloTablaDS(TableModel model) {
        this.tableModel = model;

        if (this.tableModel != null) {
            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                this.columnNames.put(tableModel.getColumnName(i), new Integer(i));
            }
        }
    }

    /**
     *
     */
    @Override
    public boolean next() {
        this.index++;

        if (this.tableModel != null) {
            return (this.index < this.tableModel.getRowCount());
        }

        return false;
    }

    /**
     *
     */
    @Override
    public Object getFieldValue(JRField jrField) throws JRException {
        String fieldName = jrField.getName();
        Integer columnIndex = this.columnNames.get(fieldName);
        Object ret = null;
        if (columnIndex != null) {
            ret = this.tableModel.getValueAt(index, columnIndex.intValue());
        } else if (fieldName.startsWith("COLUMN_")) {
            ret = this.tableModel.getValueAt(index, Integer.parseInt(fieldName.substring(7)));
        } else {
            throw new JRException("Unknown column name : " + fieldName);
        }
        if (ret instanceof Calendar || ret instanceof Date) {
            ret = Fechas.format(ret);
        } else if (ret instanceof Boolean) {
            ret = ((Boolean) ret) ? "Si" : "No";
        } else {
            ret = Str.noNulo(ret);
        }
        return ret;
    }

    /**
     *
     */
    @Override
    public void moveFirst() {
        this.index = -1;
    }
}
