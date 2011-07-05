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
 * PanelJustificaciones.java
 *
 * Created on 20 de octubre de 2008, 9:27
 */
package com.codeko.apps.maimonides.partes.justificaciones;

import com.codeko.apps.maimonides.IPanel;
import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesUtil;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.ParteFaltas;
import com.codeko.apps.maimonides.elementos.Unidad;
import com.codeko.swing.CodekoTableModel;
import com.codeko.util.Fechas;
import com.codeko.util.Num;
import com.codeko.util.Obj;
import com.codeko.util.Str;
import com.lowagie.text.Font;
import com.mysql.jdbc.PreparedStatement;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class PanelJustificaciones extends javax.swing.JPanel implements IPanel {

    CodekoTableModel<JustificacionAlumno> modelo = new CodekoTableModel<JustificacionAlumno>(new JustificacionAlumno());
    Object filtro = null;

    public Object getFiltro() {
        return filtro;
    }

    public void setFiltro(Object filtro) {
        this.filtro = filtro;
    }

    /** Creates new form PanelJustificaciones */
    public PanelJustificaciones() {
        initComponents();
        MaimonidesUtil.addMenuTabla(tabla, "Justificaciones");
        panelArbolUnidades1.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("seleccionArbol".equals(evt.getPropertyName())) {
                    Object obj = evt.getNewValue();
                    if (obj instanceof String) {
                        setFiltro(obj);
                        tabla.getColumnExt("Unidad").setVisible(true);
                    } else if (obj instanceof Unidad) {
                        setFiltro(obj);
                        tabla.getColumnExt("Unidad").setVisible(false);
                    } else {
                        setFiltro(null);
                        tabla.getColumnExt("Unidad").setVisible(true);
                    }
                    cargarDatos();
                }
            }
        });
        configurarTabla();
        SimpleDateFormat[] formatos = new SimpleDateFormat[]{new SimpleDateFormat("EEEE dd/MM/yy"), new SimpleDateFormat("dd/MM/yy"), new SimpleDateFormat("EEEE dd-MM-yy"), new SimpleDateFormat("dd-MM-yy"), new SimpleDateFormat("ddMMyy")};
        tfFecha.setFormats(formatos);
        barraHerramientas.setVisible(false);
    }

    private void cargarDatos() {
        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getActionMap(PanelJustificaciones.class, this);
        actionMap.get("cargarAsistencia").actionPerformed(new ActionEvent(this, 0, "cargarAsistencia"));
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
        tabla.setHighlighters(h);
        tabla.getColumnExt("Código").setVisible(false);
        tabla.getColumnExt("Unidad").setVisible(false);
        tabla.getColumnExt("Alumno").setCellRenderer(new DefaultTableCellRenderer() {

            @Override
            public void setValue(Object val) {
                if (val instanceof Alumno) {
                    Alumno a = (Alumno) val;
                    setText(a.getNombreFormateado());
                } else {
                    setText(Str.noNulo(val));
                }
            }
        });

        tabla.getColumnExt("Fecha").setCellRenderer(new DefaultTableCellRenderer() {

            @Override
            public void setValue(Object val) {
                if (val instanceof GregorianCalendar) {
                    setText(Fechas.format(val));
                } else {
                    setText(Str.noNulo(val));
                }
            }
        });


        TableColumnExt colNum = tabla.getColumnExt(0);
        colNum.setPreferredWidth(80);
        colNum.setMaxWidth(80);
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

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelSeparador = new javax.swing.JSplitPane();
        panelArbolUnidades1 = new com.codeko.apps.maimonides.cursos.PanelArbolUnidades();
        panelFaltas = new javax.swing.JPanel();
        scrollFaltas = new javax.swing.JScrollPane();
        tabla = new org.jdesktop.swingx.JXTable();
        leyendaPartes1 = new com.codeko.apps.maimonides.partes.LeyendaPartes();
        panelFiltro = new javax.swing.JPanel();
        cbOcultarJustificados = new javax.swing.JCheckBox();
        cbFiltroFecha = new javax.swing.JCheckBox();
        tfFecha = new org.jdesktop.swingx.JXDatePicker();
        barraHerramientas = new javax.swing.JToolBar();
        bJustificacionMasiva = new javax.swing.JButton();

        setName("maimonides.paneles.faltas.justificaciones_b"); // NOI18N

        panelSeparador.setDividerLocation(150);
        panelSeparador.setName("panelSeparador"); // NOI18N

        panelArbolUnidades1.setName("panelArbolUnidades1"); // NOI18N
        panelSeparador.setLeftComponent(panelArbolUnidades1);

        panelFaltas.setName("panelFaltas"); // NOI18N

        scrollFaltas.setName("scrollFaltas"); // NOI18N

        tabla.setModel(modelo);
        tabla.setColumnControlVisible(true);
        tabla.setName("tabla"); // NOI18N
        scrollFaltas.setViewportView(tabla);

        leyendaPartes1.setName("leyendaPartes1"); // NOI18N

        panelFiltro.setName("panelFiltro"); // NOI18N

        cbOcultarJustificados.setSelected(true);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getResourceMap(PanelJustificaciones.class);
        cbOcultarJustificados.setText(resourceMap.getString("cbOcultarJustificados.text")); // NOI18N
        cbOcultarJustificados.setName("cbOcultarJustificados"); // NOI18N
        cbOcultarJustificados.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbOcultarJustificadosActionPerformed(evt);
            }
        });

        cbFiltroFecha.setText(resourceMap.getString("cbFiltroFecha.text")); // NOI18N
        cbFiltroFecha.setName("cbFiltroFecha"); // NOI18N
        cbFiltroFecha.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbFiltroFechaActionPerformed(evt);
            }
        });

        tfFecha.setName("tfFecha"); // NOI18N

        javax.swing.GroupLayout panelFiltroLayout = new javax.swing.GroupLayout(panelFiltro);
        panelFiltro.setLayout(panelFiltroLayout);
        panelFiltroLayout.setHorizontalGroup(
            panelFiltroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFiltroLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelFiltroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cbOcultarJustificados)
                    .addGroup(panelFiltroLayout.createSequentialGroup()
                        .addComponent(cbFiltroFecha)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfFecha, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelFiltroLayout.setVerticalGroup(
            panelFiltroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFiltroLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cbOcultarJustificados)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelFiltroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbFiltroFecha)
                    .addComponent(tfFecha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panelFaltasLayout = new javax.swing.GroupLayout(panelFaltas);
        panelFaltas.setLayout(panelFaltasLayout);
        panelFaltasLayout.setHorizontalGroup(
            panelFaltasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFaltasLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelFaltasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelFaltasLayout.createSequentialGroup()
                        .addComponent(leyendaPartes1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelFiltro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addComponent(scrollFaltas, javax.swing.GroupLayout.DEFAULT_SIZE, 793, Short.MAX_VALUE)))
        );
        panelFaltasLayout.setVerticalGroup(
            panelFaltasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelFaltasLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollFaltas, javax.swing.GroupLayout.DEFAULT_SIZE, 324, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelFaltasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(leyendaPartes1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(panelFiltro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        panelSeparador.setRightComponent(panelFaltas);

        barraHerramientas.setRollover(true);
        barraHerramientas.setName("barraHerramientas"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getActionMap(PanelJustificaciones.class, this);
        bJustificacionMasiva.setAction(actionMap.get("justificarPeriodo")); // NOI18N
        bJustificacionMasiva.setFocusable(false);
        bJustificacionMasiva.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bJustificacionMasiva.setName("bJustificacionMasiva"); // NOI18N
        bJustificacionMasiva.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraHerramientas.add(bJustificacionMasiva);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(barraHerramientas, javax.swing.GroupLayout.DEFAULT_SIZE, 697, Short.MAX_VALUE)
            .addComponent(panelSeparador, javax.swing.GroupLayout.DEFAULT_SIZE, 697, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(barraHerramientas, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelSeparador, javax.swing.GroupLayout.DEFAULT_SIZE, 433, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

private void cbOcultarJustificadosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbOcultarJustificadosActionPerformed
    cargarDatos();
}//GEN-LAST:event_cbOcultarJustificadosActionPerformed

private void cbFiltroFechaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbFiltroFechaActionPerformed
    cargarDatos();
}//GEN-LAST:event_cbFiltroFechaActionPerformed

    @Override
    public boolean puedoSusituir() {
        return true;
    }

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task cargarAsistencia() {
        return new CargarAsistenciaTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class CargarAsistenciaTask extends org.jdesktop.application.Task<ArrayList<JustificacionAlumno>, Void> {

        CargarAsistenciaTask(org.jdesktop.application.Application app) {
            super(app);
            modelo.vaciar();
        }

        @Override
        protected ArrayList<JustificacionAlumno> doInBackground() {
            ArrayList<JustificacionAlumno> justif = new ArrayList<JustificacionAlumno>();
            String filtro = "";
            if (getFiltro() instanceof Unidad) {
                filtro = " AND a.unidad_id=? ";
            } else if (getFiltro() instanceof String) {
                filtro = " AND u.curso=? ";
            }
            if (cbFiltroFecha.isSelected()) {
                filtro += " AND p.fecha>? ";
            }
            String sql = "select distinct p.fecha,pa.alumno_id from partes_alumnos AS pa "
                    + " JOIN partes AS p ON p.id=pa.parte_id "
                    + " JOIN alumnos AS a ON pa.alumno_id=a.id "
                    + " JOIN cursos AS c ON c.id=a.curso_id "
                    + " JOIN unidades AS u ON u.id=a.unidad_id "
                    + " WHERE a.borrado=0 AND p.ano=? " + ((cbOcultarJustificados.isSelected()) ? " AND p.justificado=0 " : "") + " AND pa.asistencia IN ( " + ParteFaltas.FALTA_INJUSTIFICADA + "," + ParteFaltas.FALTA_RETRASO + ") "
                    + filtro
                    + " ORDER BY c.posicion,u.posicion," + Alumno.getCampoOrdenNombre("a") + ",p.fecha";
            try {
                PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
                int pos = 1;
                st.setInt(pos, MaimonidesApp.getApplication().getAnoEscolar().getId());
                pos++;
                if (getFiltro() instanceof Unidad) {
                    st.setInt(pos, ((Unidad) getFiltro()).getId());
                    pos++;
                } else if (getFiltro() instanceof String) {
                    st.setString(pos, getFiltro().toString());
                    pos++;
                }
                if (cbFiltroFecha.isSelected()) {
                    st.setDate(pos, new java.sql.Date(tfFecha.getDate().getTime()));
                    pos++;
                }
                ResultSet res = st.executeQuery();
                while (res.next()) {
                    GregorianCalendar fecha = Fechas.toGregorianCalendar(res.getDate(1));
                    Alumno a = Alumno.getAlumno(res.getInt(2));
                    JustificacionAlumno j = new JustificacionAlumno(fecha, MaimonidesApp.getApplication().getAnoEscolar(), a);
                    justif.add(j);
                }
                Obj.cerrar(st, res);
            } catch (SQLException ex) {
                Logger.getLogger(PanelJustificaciones.class.getName()).log(Level.SEVERE, null, ex);
            }
            return justif;
        }

        @Override
        protected void succeeded(ArrayList<JustificacionAlumno> result) {
            modelo.addDatos(result);
            tabla.packAll();
        }
    }

    @Action
    public void justificarPeriodo() {
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bJustificacionMasiva;
    private javax.swing.JToolBar barraHerramientas;
    private javax.swing.JCheckBox cbFiltroFecha;
    private javax.swing.JCheckBox cbOcultarJustificados;
    private com.codeko.apps.maimonides.partes.LeyendaPartes leyendaPartes1;
    private com.codeko.apps.maimonides.cursos.PanelArbolUnidades panelArbolUnidades1;
    private javax.swing.JPanel panelFaltas;
    private javax.swing.JPanel panelFiltro;
    private javax.swing.JSplitPane panelSeparador;
    private javax.swing.JScrollPane scrollFaltas;
    private org.jdesktop.swingx.JXTable tabla;
    private org.jdesktop.swingx.JXDatePicker tfFecha;
    // End of variables declaration//GEN-END:variables
}
