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


package com.codeko.apps.maimonides.profesores;

import com.codeko.apps.maimonides.elementos.Profesor;
import com.codeko.swing.comboBox.AutoCompleteComboBoxDocument;
import java.beans.Beans;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.text.JTextComponent;

/**
 *
 * @author codeko
 */
public class CbProfesores extends JComboBox {

    public CbProfesores() {
        this(true);
    }

    public CbProfesores(boolean itemTodos) {
        super();
        if (!Beans.isDesignTime()) {
            DefaultComboBoxModel modeloProfesores = new DefaultComboBoxModel(Profesor.getProfesores().toArray());
            this.setModel(modeloProfesores);
            this.setEditable(true);
            JTextComponent editorProfesores = (JTextComponent) this.getEditor().getEditorComponent();
            editorProfesores.setDocument(new AutoCompleteComboBoxDocument(this));
            if (itemTodos) {
                modeloProfesores.insertElementAt("Todos", 0);
                setSelectedIndex(0);
            } else {
                setSelectedIndex(-1);
            }
        }
    }

    public Profesor getProfesor() {
        return (getSelectedItem() instanceof Profesor) ? (Profesor) getSelectedItem() : null;
    }
}
