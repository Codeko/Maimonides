/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codeko.apps.maimonides.usr;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.util.Num;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventObject;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author codeko
 */
public class EditorRoles extends AbstractCellEditor implements TableCellEditor  {

    PanelEditorRoles editor = new PanelEditorRoles();
    JButton b = new JButton("Editar");
    int rolInicial = 0;
    int rolActual = 0;

    public EditorRoles() {
        b.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                editor.setRoles(rolActual);
                JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), editor);
                rolActual = editor.getRoles();
                fireEditingStopped();
            }
        });
        b.setBorderPainted(false);
    }

    @Override
    public Component getTableCellEditorComponent(JTable jtable, Object o, boolean bln, int i, int i1) {
        rolInicial = Num.getInt(o);
        rolActual = rolInicial;
        b.setText(Rol.getTextoRoles(rolActual));
        return b;
    }

    @Override
    public Object getCellEditorValue() {
        return rolActual;
    }
}
