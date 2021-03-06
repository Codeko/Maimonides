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
 * PanelTipos.java
 *
 * Created on 13-ago-2009, 13:19:03
 */
package com.codeko.apps.maimonides.convivencia;

import com.codeko.apps.maimonides.MaimonidesUtil;
import com.codeko.swing.CodekoAutoTableModel;
import com.codeko.util.Num;
import java.awt.Component;
import java.beans.Beans;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 *
 * @author Codeko
 */
public class PanelConductas extends javax.swing.JPanel {

    int tipo = TipoConducta.TIPO_CONDUCTA;
    CodekoAutoTableModel<Conducta> modelo = new CodekoAutoTableModel<Conducta>(Conducta.class) {

        @Override
        public void elementoModificado(Conducta elemento, int col, Object valor) {
            elemento.guardar();
        }
    };

    /** Creates new form PanelTipos */
    public PanelConductas() {
        initComponents();
        initTabla();

    }

    public PanelConductas(int tipo) {
        initComponents();
        setTipo(tipo);
        initTabla();
    }

    public void setModoCompacto(boolean compacto) {
        barraHerramientas.setVisible(!compacto);
    }

    public boolean isModoCompacto() {
        return !barraHerramientas.isVisible();
    }

    public int getTipo() {
        return tipo;
    }

    public final void setTipo(int tipo) {
        this.tipo = tipo;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        barraHerramientas = new javax.swing.JToolBar();
        bActualizar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabla = new org.jdesktop.swingx.JXTable();

        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        barraHerramientas.setFloatable(false);
        barraHerramientas.setRollover(true);
        barraHerramientas.setName("barraHerramientas"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getActionMap(PanelConductas.class, this);
        bActualizar.setAction(actionMap.get("actualizar")); // NOI18N
        bActualizar.setFocusable(false);
        bActualizar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bActualizar.setName("bActualizar"); // NOI18N
        bActualizar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraHerramientas.add(bActualizar);

        add(barraHerramientas, java.awt.BorderLayout.NORTH);

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        tabla.setModel(modelo);
        tabla.setColumnControlVisible(true);
        tabla.setName("tabla"); // NOI18N
        tabla.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                tablaAncestorAdded(evt);
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });
        jScrollPane1.setViewportView(tabla);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void initTabla() {
        if (!Beans.isDesignTime()) {
            tabla.getColumnExt("Código").setVisible(false);
            tabla.getColumnExt("Acceso").setVisible(false);
            TableColumnExt colGravedad = tabla.getColumnExt("Gravedad");
            final ArrayList<String> tipos = new ArrayList<String>();
            tipos.add("Indefinido");
            tipos.add("Leve");
            tipos.add("Grave");

            colGravedad.setCellRenderer(new DefaultTableCellRenderer() {

                @Override
                public void setValue(Object val) {
                    int v = Num.getInt(val);
                    if (v < 0 || v >= tipos.size()) {
                        v = 0;
                    }
                    setText(tipos.get(v));
                }
            });

            TableColumnExt colTipo = tabla.getColumnExt("Tipo");
            colTipo.setVisible(false);
            JComboBox comboTipos = new JComboBox(TipoConducta.getTiposConducta(getTipo()).toArray());
            DefaultCellEditor dceTipo = new DefaultCellEditor(comboTipos) {

                @Override
                public Object getCellEditorValue() {
                    Object obj = super.getCellEditorValue();
                    if (obj instanceof TipoConducta) {
                        return ((TipoConducta) obj);
                    }
                    return null;
                }

                @Override
                public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                    Component c = super.getTableCellEditorComponent(tabla, value, isSelected, row, column);
                    try {
                        TipoConducta p = (TipoConducta) value;
                        ((JComboBox) c).setSelectedItem(p);
                    } catch (Exception ex) {
                        Logger.getLogger(PanelConductas.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return c;
                }
            };
            dceTipo.setClickCountToStart(2);
            colTipo.setCellEditor(dceTipo);
        }
    }

    private void tablaAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_tablaAncestorAdded
        if (!Beans.isDesignTime()) {
            MaimonidesUtil.ejecutarTask(this, "actualizar");
        }
    }//GEN-LAST:event_tablaAncestorAdded

    @Action(block = Task.BlockingScope.WINDOW)
    public Task actualizar() {
        return new ActualizarTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class ActualizarTask extends org.jdesktop.application.Task<ArrayList<Conducta>, Void> {

        ActualizarTask(org.jdesktop.application.Application app) {
            super(app);
            //Primero vaciamos el modelo
            setMessage("Limpiando datos...");
            modelo.vaciar();
        }

        @Override
        protected ArrayList<Conducta> doInBackground() {
            setMessage("Cargando datos...");
            return Conducta.getConductas(getTipo());
        }

        @Override
        protected void succeeded(ArrayList<Conducta> result) {
            modelo.addDatos(result);
            tabla.packAll();
            setMessage("Datos cargados correctamente.");
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bActualizar;
    private javax.swing.JToolBar barraHerramientas;
    private javax.swing.JScrollPane jScrollPane1;
    private org.jdesktop.swingx.JXTable tabla;
    // End of variables declaration//GEN-END:variables
}
