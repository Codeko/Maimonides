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
