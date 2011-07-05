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
 * PanelConfiguracionMedidasExpulsion.java
 *
 * Created on 09-jun-2010, 18:15:32
 */
package com.codeko.apps.maimonides.convivencia.config;

import com.codeko.apps.maimonides.convivencia.Conducta;
import com.codeko.apps.maimonides.convivencia.TipoConducta;
import com.codeko.apps.maimonides.cursos.PanelGrupos;
import com.codeko.swing.CodekoAutoTableModel;
import com.codeko.util.Num;
import com.codeko.util.estructuras.Par;
import java.awt.Component;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.SortOrder;
import javax.swing.table.DefaultTableCellRenderer;
//import org.jdesktop.swingx.decorator.SortOrder;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 *
 * @author codeko
 */
public class PanelConfiguracionMedidasExpulsion extends javax.swing.JPanel {

    CodekoAutoTableModel<ConfiguracionMedidasExpulsion> modelo = new CodekoAutoTableModel<ConfiguracionMedidasExpulsion>(ConfiguracionMedidasExpulsion.class);
    ArrayList<Par<Integer, Integer>> datos = new ArrayList<Par<Integer, Integer>>();
    ArrayList<ConfiguracionMedidasExpulsion> original = new ArrayList<ConfiguracionMedidasExpulsion>();

    /** Creates new form PanelConfiguracionMedidasExpulsion */
    public PanelConfiguracionMedidasExpulsion() {
        initComponents();
        TableColumnExt tc = tabla.getColumnExt("Medida");
        tc.setCellRenderer(new DefaultTableCellRenderer() {

            @Override
            public void setValue(Object val) {
                if (val instanceof Conducta) {
                    setText(((Conducta) val).getDescripcion());
                } else {
                    setText("");
                }
            }
        });

        JComboBox comboMedidas = new JComboBox(Conducta.getConductas(TipoConducta.TIPO_MEDIDA).toArray());
        comboMedidas.insertItemAt("", 0);
        DefaultCellEditor dceMedidas = new DefaultCellEditor(comboMedidas) {

            @Override
            public Object getCellEditorValue() {
                Object obj = super.getCellEditorValue();
                if (obj instanceof Conducta) {
                    return ((Conducta) obj);
                }
                return null;
            }

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                Component c = super.getTableCellEditorComponent(tabla, value, isSelected, row, column);
                try {
                    if (value instanceof Conducta) {
                        ((JComboBox) c).setSelectedItem(value);
                    } else {
                        ((JComboBox) c).setSelectedIndex(0);
                    }
                } catch (Exception ex) {
                    Logger.getLogger(PanelGrupos.class.getName()).log(Level.SEVERE, null, ex);
                }
                return c;
            }
        };
        //dceMedidas.setClickCountToStart(2);
        tc.setCellEditor(dceMedidas);

    }

    public void setDato(String dato) {
        //Recibimos el dato con formato dias=id_medida,dia=id_medida
        modelo.vaciar();
        original.clear();
        String[] bloques = dato.split(",");
        //De cada bloque sacamos los dos datos
        for (String bloque : bloques) {
            if (!bloque.trim().equals("")) {
                String[] d = bloque.split("=");
                int dias = Num.getInt(d[0]);
                int idMedida = Num.getInt(d[1]);
                ConfiguracionMedidasExpulsion dcme = new ConfiguracionMedidasExpulsion();
                dcme.setDias(dias);
                if (idMedida > 0) {
                    try {
                        Conducta c = new Conducta(idMedida);
                        dcme.setMedida(c);
                        modelo.addDato(dcme);
                        original.add(dcme);
                    } catch (Exception ex) {
                        Logger.getLogger(PanelConfiguracionMedidasExpulsion.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        tabla.packAll();
        tabla.setSortOrder(0, SortOrder.ASCENDING);
    }

    public void setNuevosDias(ArrayList<Integer> dias) {
        //Si se asignan nuevos días hay que verificar los que ya existan
        //para matener las configuraciones anteriores
        //Primero eliminamos los que no existan
        for (ConfiguracionMedidasExpulsion d : new ArrayList<ConfiguracionMedidasExpulsion>(modelo.getDatos())) {
            if (!dias.contains(d.getDias())) {
                modelo.quitarDato(d);
            }
        }
        //Y ahora añadimos los que no existan
        for (Integer i : dias) {
            ConfiguracionMedidasExpulsion dcme = null;
            boolean existe = false;
            for (ConfiguracionMedidasExpulsion d : modelo.getDatos()) {
                if (d.getDias() == i.intValue()) {
                    dcme = d;
                    existe = true;
                    break;
                }
            }
            //Si no existe buscamos en los datos originales
            if (dcme == null) {
                for (ConfiguracionMedidasExpulsion d : original) {
                    if (d.getDias() == i.intValue()) {
                        dcme = d;
                        break;
                    }
                }
            }
            if (dcme == null) {
                //Si no existe lo creamos
                dcme = new ConfiguracionMedidasExpulsion();
                dcme.setDias(i);
            }
            //Si no existe añadimos el creado o el de los datos originales
            if (!existe) {
                modelo.addDato(dcme);
            }
        }
        tabla.packAll();
        tabla.setSortOrder(0, SortOrder.ASCENDING);
    }

    public String getDato() {
        String dato = "";
        boolean primero = false;
        for (ConfiguracionMedidasExpulsion d : modelo.getDatos()) {
            if (!primero) {
                dato += ",";
            }
            primero = false;
            int idDato = 0;
            if (d.getMedida() != null) {
                idDato = d.getMedida().getId();
            }
            dato += d.getDias() + "=" + idDato;
        }
        return dato;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        tabla = new org.jdesktop.swingx.JXTable();

        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        tabla.setModel(modelo);
        tabla.setName("tabla"); // NOI18N
        jScrollPane1.setViewportView(tabla);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private org.jdesktop.swingx.JXTable tabla;
    // End of variables declaration//GEN-END:variables
}
