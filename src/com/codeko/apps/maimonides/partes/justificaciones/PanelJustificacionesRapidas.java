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
 * PanelJustificacionesRapidas.java
 *
 * Created on 20 de noviembre de 2008, 9:14
 */
package com.codeko.apps.maimonides.partes.justificaciones;

import com.codeko.apps.maimonides.Configuracion;
import com.codeko.apps.maimonides.IPanel;
import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesUtil;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.ParteFaltas;
import com.codeko.apps.maimonides.partes.informes.alumnos.ResumenAsistenciaAlumno;
import com.codeko.swing.CdkProcesoLabel;
import com.codeko.swing.CodekoTableModel;
import com.codeko.util.Num;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;
import org.jdesktop.swingx.decorator.AbstractHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 *
 * @author  Codeko
 */
public class PanelJustificacionesRapidas extends javax.swing.JPanel implements IPanel {

    CodekoTableModel<JustificacionAlumno> modelo = new CodekoTableModel<JustificacionAlumno>(new JustificacionAlumno());
    Configuracion cfg = MaimonidesApp.getApplication().getConfiguracion();
    /** Creates new form PanelJustificacionesRapidas */
    public PanelJustificacionesRapidas() {
        initComponents();
        panelBusquedaAlumnos1.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("alumnoSeleccionado".equals(evt.getPropertyName())) {
                    cargar();
                } else if ("enterPulsado".equals(evt.getPropertyName())) {
                    tfControl.requestFocus();
                }
            }
        });
        configurarTabla();
    }

    private void configurarTabla() {
        MaimonidesUtil.addMenuTabla(tabla, "Justificaciones");
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
        tabla.setHighlighters(h);
        tabla.getColumnExt("Código").setVisible(false);
        tabla.getColumnExt("Alumno").setVisible(false);
        tabla.getColumnExt("Unidad").setVisible(false);
        tabla.getColumnExt("Fecha").setMaxWidth(100);

        TableColumnExt colNum = tabla.getColumnExt(0);
        colNum.setPreferredWidth(100);
        //colNum.setMaxWidth(100);
        colNum.setMinWidth(80);
        tabla.setEditable(false);
        int pos = tabla.getColumnCount() - 1;
        for (int i = 0; i < 6; i++) {
            TableColumnExt col = tabla.getColumnExt(pos - i);
            col.setPreferredWidth(65);
            col.setMinWidth(65);
            col.setCellRenderer(new DefaultTableCellRenderer() {

                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    if (c instanceof JLabel) {
                        JLabel t = (JLabel) c;
                        t.setHorizontalAlignment(SwingConstants.CENTER);
                        t.setFont(t.getFont().deriveFont(Font.BOLD));
                    }
                    return c;
                }

                @Override
                public void setValue(Object value) {
                    if (value == null) {
                        setBackground(Color.LIGHT_GRAY);
                        setText("");
                    } else {
                        int val = Num.getInt(value);
                        switch (val) {
                            case ParteFaltas.FALTA_ASISTENCIA:
                                setText("A");
                                break;
                            case ParteFaltas.FALTA_EXPULSION:
                                setText("E");
                                break;
                            case ParteFaltas.FALTA_JUSTIFICADA:
                                setText("J");
                                break;
                            case ParteFaltas.FALTA_INJUSTIFICADA:
                                setText("I");
                                break;
                            case ParteFaltas.FALTA_RETRASO:
                                setText("R");
                                break;
                            default:
                                setText("");
                        }
                    }
                }
            });
        }

        tabla.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                String letra = "";
                if (e.getKeyChar() == '\n') {
                    int tRow = tabla.getSelectedRow();
                    int row = tabla.convertRowIndexToModel(tRow);
                    int tCol = tabla.getSelectedColumn();
                    int col = tabla.convertColumnIndexToModel(tCol);
                    if (Num.getInt(modelo.getValueAt(row, col)) <= 0) {
                        letra = "AX";
                    }
                }
                if (!letra.equals("")) {
                    procesarLetra(letra, e.isControlDown());
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                //System.out.println(KeyEvent.getKeyText(e.getKeyCode()) + ":" + e.isControlDown() + ":" + e.getKeyCode());
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    tfControl.requestFocus();
                    tfControl.selectAll();
                } else {
                    String letra = KeyEvent.getKeyText(e.getKeyCode()).toUpperCase();
                    if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                        letra = " ";
                    }
                    if (!letra.equals("") && letra.length() == 1) {
                        procesarLetra(letra, e.isControlDown());
                    }
                }

            }

            void procesarLetra(String letra, boolean controlPulsado) {
                int tRow = tabla.getSelectedRow();
                int row = tabla.convertRowIndexToModel(tRow);
                int tCol = tabla.getSelectedColumn();
                int col = tabla.convertColumnIndexToModel(tCol);
                if (col <= 2 || controlPulsado) {
                    if (controlPulsado) {
                        col = 2;
                    }
                    for (int i = col + 1; i <= modelo.getColumnCount(); i++) {
                        //Vemos el valor actual
                        int valActu = Num.getInt(modelo.getValueAt(row, i));
                        if (controlPulsado) {
                            if (letra.equals("AX")) {
                                letra = "A";
                            }
                            modelo.setValueAt(letra, row, i);
                        } else if (valActu == ParteFaltas.FALTA_INJUSTIFICADA || valActu == ParteFaltas.FALTA_JUSTIFICADA || valActu == ParteFaltas.FALTA_INDETERMINADA) {
                            if (letra.equals("AX")) {
                                if (valActu <= 0) {
                                    modelo.setValueAt("A", row, i);
                                }
                            } else {
                                modelo.setValueAt(letra, row, i);
                            }
                        }
                    }
                    tabla.changeSelection(tRow, tCol, false, false);
                    tabla.getSelectionModel().setSelectionInterval(tRow, tRow);
                } else {
                    if (letra.equals("AX")) {
                        letra = "A";
                    }
                    modelo.setValueAt(letra, row, col);
                    tabla.changeSelection(tRow, tCol, false, false);
                    tabla.getSelectionModel().setSelectionInterval(tRow, tRow);
                }
            }
        });

        tabla.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1 && SwingUtilities.isLeftMouseButton(e)) {
                    int row = tabla.rowAtPoint(e.getPoint());
                    int col = tabla.columnAtPoint(e.getPoint());
                    row = tabla.convertRowIndexToModel(row);
                    col = tabla.convertColumnIndexToModel(col);
                    int val = Num.getInt(modelo.getValueAt(row, col));
                    if (val == ParteFaltas.FALTA_INJUSTIFICADA) {
                        modelo.setValueAt("J", row, col);
                    } else if (val == ParteFaltas.FALTA_JUSTIFICADA) {
                        modelo.setValueAt("I", row, col);
                    }

                }
            }
        });
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    }

    private void cargar() {
        MaimonidesUtil.ejecutarTask(this, "cargarAsistenciaAlumno");
    }

    public void abajo() {
        int row = tabla.getSelectedRow();
        row++;
        if (row < modelo.getRowCount()) {
            tabla.getSelectionModel().setSelectionInterval(row, row);
            tabla.scrollRowToVisible(row);
        }
    }

    public void arriba() {
        int row = tabla.getSelectedRow();
        row--;
        if (row >= 0 && row < modelo.getRowCount()) {
            tabla.getSelectionModel().setSelectionInterval(row, row);
            tabla.scrollRowToVisible(row);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelSplit = new javax.swing.JSplitPane();
        panelInferior = new javax.swing.JPanel();
        tfControl = new javax.swing.JTextField();
        panelInfoAsistencia = new javax.swing.JPanel();
        scrollTabla = new javax.swing.JScrollPane();
        tabla = new org.jdesktop.swingx.JXTable();
        lFiltroFecha = new javax.swing.JLabel();
        lAvisoExcesoFaltas = new javax.swing.JLabel();
        panelBusquedaAlumnos1 = new com.codeko.apps.maimonides.alumnos.PanelBusquedaAlumnos();

        setName("maimonides.paneles.faltas.justificaciones_a"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        panelSplit.setDividerLocation(250);
        panelSplit.setDividerSize(8);
        panelSplit.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        panelSplit.setName("panelSplit"); // NOI18N
        panelSplit.setOneTouchExpandable(true);

        panelInferior.setName("panelInferior"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getResourceMap(PanelJustificacionesRapidas.class);
        tfControl.setText(resourceMap.getString("tfControl.text")); // NOI18N
        tfControl.setToolTipText(resourceMap.getString("tfControl.toolTipText")); // NOI18N
        tfControl.setName("tfControl"); // NOI18N
        tfControl.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tfControlKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tfControlKeyReleased(evt);
            }
        });

        panelInfoAsistencia.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("panelInfoAsistencia.border.title"))); // NOI18N
        panelInfoAsistencia.setName("panelInfoAsistencia"); // NOI18N
        panelInfoAsistencia.setLayout(new java.awt.BorderLayout());

        scrollTabla.setName("scrollTabla"); // NOI18N

        tabla.setModel(modelo);
        tabla.setColumnControlVisible(true);
        tabla.setName("tabla"); // NOI18N
        tabla.setShowGrid(true);
        scrollTabla.setViewportView(tabla);

        panelInfoAsistencia.add(scrollTabla, java.awt.BorderLayout.CENTER);

        lFiltroFecha.setText(resourceMap.getString("lFiltroFecha.text")); // NOI18N
        lFiltroFecha.setName("lFiltroFecha"); // NOI18N

        lAvisoExcesoFaltas.setFont(resourceMap.getFont("lAvisoExcesoFaltas.font")); // NOI18N
        lAvisoExcesoFaltas.setForeground(resourceMap.getColor("lAvisoExcesoFaltas.foreground")); // NOI18N
        lAvisoExcesoFaltas.setText(resourceMap.getString("lAvisoExcesoFaltas.text")); // NOI18N
        lAvisoExcesoFaltas.setName("lAvisoExcesoFaltas"); // NOI18N

        javax.swing.GroupLayout panelInferiorLayout = new javax.swing.GroupLayout(panelInferior);
        panelInferior.setLayout(panelInferiorLayout);
        panelInferiorLayout.setHorizontalGroup(
            panelInferiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelInferiorLayout.createSequentialGroup()
                .addGroup(panelInferiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelInferiorLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(panelInfoAsistencia, javax.swing.GroupLayout.DEFAULT_SIZE, 565, Short.MAX_VALUE))
                    .addGroup(panelInferiorLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(lFiltroFecha)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfControl, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lAvisoExcesoFaltas, javax.swing.GroupLayout.DEFAULT_SIZE, 347, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelInferiorLayout.setVerticalGroup(
            panelInferiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelInferiorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelInferiorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lFiltroFecha)
                    .addComponent(tfControl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lAvisoExcesoFaltas))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelInfoAsistencia, javax.swing.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE)
                .addContainerGap())
        );

        panelSplit.setRightComponent(panelInferior);

        panelBusquedaAlumnos1.setName("panelBusquedaAlumnos1"); // NOI18N
        panelSplit.setLeftComponent(panelBusquedaAlumnos1);

        add(panelSplit, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

private void tfControlKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfControlKeyReleased
    if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
        tabla.changeSelection(tabla.getSelectedRow(), 0, false, false);
        tabla.requestFocus();
    } else if (evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
        tfControl.setText("");
        panelBusquedaAlumnos1.activar();
    } else if (!evt.isActionKey()) {
        String texto = tfControl.getText();
        StringBuilder sb = new StringBuilder();
        int cont = 0;
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if (Num.esNumero(c)) {
                if (cont % 2 == 0 && i > 0) {
                    sb.append("/");
                }
                sb.append(c);
                cont++;
            }

        }
        System.out.println("Buscando '" + sb.toString() + "'");
        tabla.getSearchable().search(Pattern.compile(sb.toString() + ".*"));
    }
}//GEN-LAST:event_tfControlKeyReleased

private void tfControlKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfControlKeyPressed
    if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
        abajo();
    } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
        arriba();
    }
}//GEN-LAST:event_tfControlKeyPressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lAvisoExcesoFaltas;
    private javax.swing.JLabel lFiltroFecha;
    private com.codeko.apps.maimonides.alumnos.PanelBusquedaAlumnos panelBusquedaAlumnos1;
    private javax.swing.JPanel panelInferior;
    private javax.swing.JPanel panelInfoAsistencia;
    private javax.swing.JSplitPane panelSplit;
    private javax.swing.JScrollPane scrollTabla;
    private org.jdesktop.swingx.JXTable tabla;
    private javax.swing.JTextField tfControl;
    // End of variables declaration//GEN-END:variables

    @Override
    public boolean puedoSusituir() {
        return true;
    }

    @Action(block = Task.BlockingScope.ACTION)
    public Task cargarAsistenciaAlumno() {
        return new CargarAsistenciaAlumnoTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class CargarAsistenciaAlumnoTask extends org.jdesktop.application.Task<Object, Void> {

        CargarAsistenciaAlumnoTask(org.jdesktop.application.Application app) {
            super(app);
            panelInfoAsistencia.removeAll();
            CdkProcesoLabel l = new CdkProcesoLabel();
            panelInfoAsistencia.add(l, BorderLayout.CENTER);
            l.setProcesando(true);
            l.setText("Cargando asistencia...");
            lAvisoExcesoFaltas.setText("");
        }

        @Override
        protected Object doInBackground() {
            modelo.vaciar();
            if (panelBusquedaAlumnos1.getAlumnoSeleccionado() != null) {
                //Primero vemos el número de faltas del alumno
                Alumno a=panelBusquedaAlumnos1.getAlumnoSeleccionado();
                ResumenAsistenciaAlumno r=new ResumenAsistenciaAlumno();
                r.setAlumno(a);
                lAvisoExcesoFaltas.setText("El alumno tiene "+(r.getInjustificadas()+r.getJustificadas())+" faltas (J+I)");
                if((r.getInjustificadas()+r.getJustificadas())>Num.getInt(cfg.get("faltas_aviso_justificacion", "100"))){
                    lAvisoExcesoFaltas.setForeground(Color.red);
                }else{
                    lAvisoExcesoFaltas.setForeground(Color.black);
                }
                modelo.addDatos(JustificacionAlumno.getJustificaciones(a));
            }
            return null;  // return your result
        }

        @Override
        protected void succeeded(Object result) {
            panelInfoAsistencia.removeAll();
            panelInfoAsistencia.add(scrollTabla, BorderLayout.CENTER);
            if (modelo.getRowCount() > 0) {
                tfControl.setText("");
                tfControl.requestFocus();
            }
        }
    }
}
