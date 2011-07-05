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
