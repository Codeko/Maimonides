/*
 * PanelJustificacionesRapidas.java
 *
 * Created on 20 de noviembre de 2008, 9:14
 */
package com.codeko.apps.maimonides.alumnos;

import com.codeko.apps.maimonides.partes.justificaciones.*;
import com.codeko.apps.maimonides.IPanel;
import com.codeko.apps.maimonides.MaimonidesUtil;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.ParteFaltas;
import com.codeko.swing.CodekoTableModel;
import com.codeko.util.Num;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import org.jdesktop.application.Action;
import org.jdesktop.swingx.decorator.AbstractHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;

/**
 *
 * @author  Codeko
 */
public class PanelAccesoFichaAlumnos extends javax.swing.JPanel implements IPanel {

    CodekoTableModel<JustificacionAlumno> modelo = new CodekoTableModel<JustificacionAlumno>(new JustificacionAlumno());

    /** Creates new form PanelJustificacionesRapidas */
    public PanelAccesoFichaAlumnos() {
        initComponents();

        panelBusquedaAlumnos1.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("alumnoSeleccionado".equals(evt.getPropertyName())) {
                    cargar();
                } else if ("enterPulsado".equals(evt.getPropertyName())) {
                    //TODO Acceso a campos de ficha
                }
            }
        });
        configurarTabla();
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "inicio");
        getActionMap().put("inicio", new javax.swing.Action() {

            @Override
            public Object getValue(String key) {
                return null;
            }

            @Override
            public void putValue(String key, Object value) {
                //throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void setEnabled(boolean b) {
                //throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean isEnabled() {
                return true;
            }

            @Override
            public void addPropertyChangeListener(PropertyChangeListener listener) {
                //throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void removePropertyChangeListener(PropertyChangeListener listener) {
                //throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                buscar();
            }
        });

    }

    private void configurarTabla() {
        AbstractHighlighter h = new AbstractHighlighter() {

            @Override
            protected Component doHighlight(Component c, ComponentAdapter adapt) {
                Object valor = adapt.getValue();

                if (valor instanceof Alumno) {
                    Alumno a = (Alumno) adapt.getValue();

                    if (adapt.isSelected()) {
                        Color back = UIManager.getDefaults().getColor("Table.selectionBackground");
                        c.setBackground(back);
                        Color fore = UIManager.getDefaults().getColor("Table.selectionForeground");
                        c.setForeground(fore);
                    } else {
                        Color back = UIManager.getDefaults().getColor("Table.background");
                        c.setBackground(back);
                        Color fore = UIManager.getDefaults().getColor("Table.foreground");
                        c.setForeground(fore);
                    }

                } else if (valor instanceof Integer && Num.getInt(valor) < 10) {
                    if (!adapt.isSelected()) {
                        c.setBackground(ParteFaltas.getColorTipoFalta(valor));
                        c.setForeground(UIManager.getDefaults().getColor("Table.foreground"));
                    }
                } else if (valor == null) {
                    c.setBackground(Color.LIGHT_GRAY);
                }
                if (adapt.hasFocus()) {
                    ((JComponent) c).setBorder(BorderFactory.createLineBorder(Color.red, 2));
                    c.setForeground(Color.red);
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                }

                return c;
            }
        };
    }

    public void buscar() {
        panelFichaAlumno1.setAlumno(null);
        panelBusquedaAlumnos1.activar();
    }

    private void cargar() {
        MaimonidesUtil.ejecutarTask(this, "cargarAlumno");
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panleSplit = new javax.swing.JSplitPane();
        panelInferior = new javax.swing.JPanel();
        panelFichaAlumno1 = new com.codeko.apps.maimonides.alumnos.PanelFichaAlumno();
        panelBusquedaAlumnos1 = new com.codeko.apps.maimonides.alumnos.PanelBusquedaAlumnos();

        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        panleSplit.setDividerLocation(190);
        panleSplit.setDividerSize(8);
        panleSplit.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        panleSplit.setName("panleSplit"); // NOI18N
        panleSplit.setOneTouchExpandable(true);

        panelInferior.setName("panelInferior"); // NOI18N
        panelInferior.setLayout(new java.awt.BorderLayout());

        panelFichaAlumno1.setMinimumSize(new java.awt.Dimension(0, 0));
        panelFichaAlumno1.setName("panelFichaAlumno1"); // NOI18N
        panelInferior.add(panelFichaAlumno1, java.awt.BorderLayout.CENTER);

        panleSplit.setRightComponent(panelInferior);

        panelBusquedaAlumnos1.setMinimumSize(new java.awt.Dimension(0, 0));
        panelBusquedaAlumnos1.setName("panelBusquedaAlumnos1"); // NOI18N
        panleSplit.setLeftComponent(panelBusquedaAlumnos1);

        add(panleSplit, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.codeko.apps.maimonides.alumnos.PanelBusquedaAlumnos panelBusquedaAlumnos1;
    private com.codeko.apps.maimonides.alumnos.PanelFichaAlumno panelFichaAlumno1;
    private javax.swing.JPanel panelInferior;
    private javax.swing.JSplitPane panleSplit;
    // End of variables declaration//GEN-END:variables

    @Override
    public boolean puedoSusituir() {
        return panelFichaAlumno1.setAlumno(null);
    }

    @Action
    public void cargarAlumno() {
        if (panelBusquedaAlumnos1.getAlumnoSeleccionado() != null) {
            if (!panelFichaAlumno1.setAlumno(panelBusquedaAlumnos1.getAlumnoSeleccionado())) {
                panelBusquedaAlumnos1.setAlumnoSeleccionado(panelFichaAlumno1.getAlumno());
            }
        }
    }
}
