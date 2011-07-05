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
 * PanelFaltasAlumno.java
 *
 * Created on 30-ene-2009, 18:19:28
 */
package com.codeko.apps.maimonides.partes;

import com.codeko.apps.maimonides.ICargable;
import com.codeko.apps.maimonides.MaimonidesUtil;
import com.codeko.apps.maimonides.alumnos.IFiltrableAlumno;
import com.codeko.apps.maimonides.asistencia.ConfiguracionAsistencia;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.LineaParteAlumno;
import com.codeko.apps.maimonides.elementos.ParteFaltas;
import com.codeko.apps.maimonides.partes.justificaciones.JustificacionAlumno;
import com.codeko.swing.CdkProcesoLabel;
import com.codeko.swing.CodekoTableModel;
import com.codeko.util.Num;
import com.lowagie.text.Font;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;


import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
 * @author Codeko
 */
public class PanelFaltasAlumno extends javax.swing.JPanel implements ICargable,IFiltrableAlumno {

    CodekoTableModel<JustificacionAlumno> modelo = new CodekoTableModel<JustificacionAlumno>(new JustificacionAlumno());
    Alumno alumno = null;
    boolean cargado = false;
    private static final int PRIMERA_COL_DATOS = 4;
    boolean mensajeAdvertenciaMostrado = false;
    boolean opcionMensajeAdvertencia = false;
    boolean usarControlMensajeAdvertencia = false;
    PropertyChangeListener listenerAsistencia = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if ("asistenciaSeleccionada".equals(evt.getPropertyName())) {
                panelDetalleAsistencia1.setDatos((LineaParteAlumno) evt.getNewValue());
            }
        }
    };

    /** Creates new form PanelFaltasAlumno */
    public PanelFaltasAlumno() {
        initComponents();
        configurarTabla();
        panelDetalleAsistencia1.setVisible(false);
        this.addPropertyChangeListener(listenerAsistencia);
    }

    public Alumno getAlumno() {
        return alumno;
    }

    @Override
    public void setAlumno(Alumno alumno) {
        this.alumno = alumno;
        if (alumno == null) {
            vaciar();
        }
    }

    @Override
    public void vaciar() {
        modelo.vaciar();
        setCargado(false);
    }

    @Override
    public void cargar() {
        MaimonidesUtil.ejecutarTask(this, "cargarAsistenciaAlumno");

    }

    private void configurarTabla() {
        MaimonidesUtil.addMenuTabla(tabla, "Asistencia del alumno");
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
        //tabla.getColumnExt("Fecha").setMaxWidth(100);

//        TableColumnExt colNum = tabla.getColumnExt(0);
//        colNum.setPreferredWidth(80);
//        colNum.setMaxWidth(80);
//        colNum.setMinWidth(80);
        tabla.setEditable(false);
        int pos = tabla.getColumnCount() - 1;
        for (int i = 0; i < 6; i++) {
            TableColumnExt col = tabla.getColumnExt(pos - i);
            col.setPreferredWidth(65);
            col.setMinWidth(35);
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
                //TODO EL control-A parece capturado por otro envento pero el control enter no lo veo como alternativa
//                if (e.getKeyChar() == '\n') {
//                    int tRow = tabla.getSelectedRow();
//                    //int row = tabla.convertRowIndexToModel(tRow);
//                    int tCol = tabla.getSelectedColumn();
//                    int col = tabla.convertColumnIndexToModel(tCol);
//                    if (col<PRIMERA_COL_DATOS){//Num.getInt(modelo.getValueAt(row, col)) <= 0) {
//                        letra = "AX";
//                    }
//                }
                if (!letra.equals("")) {
                    procesarLetra(letra, e.isControlDown());
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                String letra = KeyEvent.getKeyText(e.getKeyCode()).toUpperCase();
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    letra = " ";
                }
                if (!letra.equals("") && letra.length() == 1) {
                    procesarLetra(letra, e.isControlDown());
                }
            }

            void procesarLetra(String letra, boolean controlPulsado) {
                int tRow = tabla.getSelectedRow();
                int row = tabla.convertRowIndexToModel(tRow);
                int tCol = tabla.getSelectedColumn();
                int col = tabla.convertColumnIndexToModel(tCol);
                if (col < PRIMERA_COL_DATOS || controlPulsado) {
                    if (controlPulsado) {
                        col = PRIMERA_COL_DATOS;
                    }
                    Boolean crearNoExistentes = null;
                    mensajeAdvertenciaMostrado = false;
                    opcionMensajeAdvertencia = false;
                    usarControlMensajeAdvertencia = true;
                    for (int i = col; i < modelo.getColumnCount(); i++) {
                        //Vemos el valor actual
                        Object val = modelo.getValueAt(row, i);
                        int valActu = Num.getInt(modelo.getValueAt(row, i));
                        if (controlPulsado) {
                            if (letra.equals("AX")) {
                                letra = "A";
                            }
                            if (val == null && crearNoExistentes == null) {
                                int op = JOptionPane.showConfirmDialog(tabla, "Se va a asignar asistencia a un bloque horario sin parte.\nPara poder asignarlo se añadirá el alumno al parte o se creará un nuevo parte.\nSi el alumno no está matriculado en ninguna asignatura que se imparta en\nese bloque horario no se podrá asignar ninguna asistencia.\n¿Continuar?", "Asignar asistencia", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                                crearNoExistentes = op == JOptionPane.YES_OPTION;
                            }
                            if (val != null || (crearNoExistentes == null || crearNoExistentes)) {
                                //modelo.setValueAt(letra, row, i);
                                setValorAsistencia(row, i, letra);
                            }
                        } else if (val != null && (valActu == ParteFaltas.FALTA_INJUSTIFICADA || valActu == ParteFaltas.FALTA_JUSTIFICADA || valActu == ParteFaltas.FALTA_INDETERMINADA)) {
                            if (letra.equals("AX")) {
                                if (valActu <= 0) {
                                    //modelo.setValueAt("A", row, i);
                                    setValorAsistencia(row, i, "A");
                                }
                            } else {
                                //modelo.setValueAt(letra, row, i);
                                setValorAsistencia(row, i, letra);
                            }
                        }
                    }
                    mensajeAdvertenciaMostrado = false;
                    opcionMensajeAdvertencia = false;
                    usarControlMensajeAdvertencia = false;
                    tabla.changeSelection(tRow, tCol, false, false);
                    tabla.getSelectionModel().setSelectionInterval(tRow, tRow);
                } else {
                    if (letra.equals("AX")) {
                        letra = "A";
                    }
                    Object val = modelo.getValueAt(row, col);
                    boolean ok = true;
                    if (val == null) {
                        ok = false;
                        int op = JOptionPane.showConfirmDialog(tabla, "Se va a asignar asistencia a un bloque horario sin parte.\nPara poder asignarlo se añadirá el alumno al parte o se creará un nuevo parte.\nSi el alumno no está matriculado en ninguna asignatura que se imparta en\nese bloque horario no se podrá asignar ninguna asistencia.\n¿Continuar?", "Asignar asistencia", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                        ok = op == JOptionPane.YES_OPTION;
                    }
                    if (ok) {
                        //modelo.setValueAt(letra, row, col);
                        setValorAsistencia(row, col, letra);
                        tabla.changeSelection(tRow, tCol, false, false);
                        tabla.getSelectionModel().setSelectionInterval(tRow, tRow);
                    }
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
                        //modelo.setValueAt("J", row, col);
                        setValorAsistencia(row, col, "J");
                    } else if (val == ParteFaltas.FALTA_JUSTIFICADA) {
                        //modelo.setValueAt("I", row, col);
                        setValorAsistencia(row, col, "I");
                    }

                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    int row = tabla.rowAtPoint(e.getPoint());
                    int col = tabla.columnAtPoint(e.getPoint());
                    if (row > -1) {
                        row = tabla.convertRowIndexToModel(row);
                        col = tabla.convertColumnIndexToModel(col);
                        JustificacionAlumno justi = modelo.getElemento(row);
                        LineaParteAlumno lin = justi.getLineaHora(col-3);
                        firePropertyChange("asistenciaSeleccionada", null, lin);
                    }
                }
            }
        });
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    }

    protected void setValorAsistencia(int row, int col, String asistencia) {
        JustificacionAlumno ja = modelo.getDatos().elementAt(row);
        boolean asignar = true;
        if (!asistencia.equals("E") && ja.getAlumno().isExpulsado(ja.getFecha())) {
            if (ConfiguracionAsistencia.getAccionAsistenciaEnExpulsados() == ConfiguracionAsistencia.AAE_ADVERTENCIA) {
                if (!mensajeAdvertenciaMostrado || (!usarControlMensajeAdvertencia)) {
                    int op = JOptionPane.showConfirmDialog(this, "Se va a asignar asistencia a distinta de expulsión a un alumno expulsado.\n¿Continuar?", "Asignar asistencia", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (op != JOptionPane.YES_OPTION) {
                        asignar = false;
                    }
                    opcionMensajeAdvertencia = asignar;
                } else {
                    asignar = opcionMensajeAdvertencia;
                }
            } else if (ConfiguracionAsistencia.getAccionAsistenciaEnExpulsados() == ConfiguracionAsistencia.AAE_ASIGNAR_EXPULSION_ADVERTENCIA) {
                if (!mensajeAdvertenciaMostrado || (!usarControlMensajeAdvertencia)) {
                    JOptionPane.showMessageDialog(this, "No se permite asignar asistencia distinta de expulsado a un alumno expulsado", "Error asignando asistencia", JOptionPane.INFORMATION_MESSAGE);
                    mensajeAdvertenciaMostrado = true;
                }
                asignar = false;
            } else if (ConfiguracionAsistencia.getAccionAsistenciaEnExpulsados() == ConfiguracionAsistencia.AAE_ASIGNAR_EXPULSION) {
                asignar = false;
            }
        }
        if (asignar) {
            modelo.setValueAt(asistencia, row, col);
        }
    }

    @Action(block = Task.BlockingScope.ACTION)
    public Task cargarAsistenciaAlumno() {
        return new CargarAsistenciaAlumnoTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    @Override
    public boolean isCargado() {
        return this.cargado;
    }

    @Override
    public void setCargado(boolean cargado) {
        this.cargado = cargado;
    }

    private class CargarAsistenciaAlumnoTask extends org.jdesktop.application.Task<Object, Void> {
        CdkProcesoLabel l=null;
        CargarAsistenciaAlumnoTask(org.jdesktop.application.Application app) {
            super(app);
            remove(scroll);
            panelDetalleAsistencia1.setVisible(false);
            l = new CdkProcesoLabel();
            add(l, BorderLayout.CENTER);
            l.setHorizontalAlignment(SwingConstants.CENTER);
            l.setProcesando(true);
            l.setText("Cargando asistencia...");
        }

        @Override
        protected Object doInBackground() {
            modelo.vaciar();
            if (getAlumno() != null && getAlumno().getId() != null) {
                modelo.addDatos(JustificacionAlumno.getJustificaciones(getAlumno()));
            }
            return null;
        }

        @Override
        protected void succeeded(Object result) {
            remove(l);
            add(scroll, BorderLayout.CENTER);
            setCargado(true);
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

        scroll = new javax.swing.JScrollPane();
        tabla = new org.jdesktop.swingx.JXTable();
        panelDetalleAsistencia1 = new com.codeko.apps.maimonides.partes.informes.alumnos.PanelDetalleAsistencia();

        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        scroll.setName("scroll"); // NOI18N

        tabla.setModel(modelo);
        tabla.setName("tabla"); // NOI18N
        scroll.setViewportView(tabla);

        add(scroll, java.awt.BorderLayout.CENTER);

        panelDetalleAsistencia1.setName("panelDetalleAsistencia1"); // NOI18N
        add(panelDetalleAsistencia1, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.codeko.apps.maimonides.partes.informes.alumnos.PanelDetalleAsistencia panelDetalleAsistencia1;
    private javax.swing.JScrollPane scroll;
    private org.jdesktop.swingx.JXTable tabla;
    // End of variables declaration//GEN-END:variables
}
