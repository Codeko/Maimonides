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

package com.codeko.apps.maimonides.apoyos;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesUtil;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.util.Obj;
import com.mysql.jdbc.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

public class PanelApoyos extends javax.swing.JPanel {

    Alumno alumno = null;

    public PanelApoyos() {
        initComponents();
        MaimonidesUtil.addMenuTabla(tabla, "Apoyo");
        tabla.getModel().addTableModelListener(new TableModelListener() {

            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getType() == TableModelEvent.UPDATE) {
                    if (getAlumno() != null) {
                        int dia = e.getColumn();
                        int hora = e.getFirstRow();
                        Boolean val = (Boolean) tabla.getModel().getValueAt(hora, dia);
                        hora++;//La hora ya no debe empezar a 0
                        setValorApoyo(dia, hora, val);
                    }
                }
            }
        });
    }

    private void setValorApoyo(int dia, int hora, Boolean val) {
        if (val == null || !val) {
            //Borramos la asignacion
            String sql = "DELETE apoyos_alumnos FROM apoyos_alumnos JOIN horarios AS h ON h.id=apoyos_alumnos.horario_id WHERE apoyos_alumnos.alumno_id=? AND h.hora=? AND h.dia=?";
            try {
                PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
                st.setInt(1, getAlumno().getId());
                st.setInt(2, hora);
                st.setInt(3, dia);
                st.executeUpdate();
            } catch (SQLException ex) {
                Logger.getLogger(PanelApoyos.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            //Creamos la asignacion
            String sql = "INSERT INTO apoyos_alumnos SELECT distinct ?, h.id FROM horarios_ AS h WHERE h.hora=? AND h.dia=? AND h.unidad_id=? ";
            try {
                PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
                st.setInt(1, getAlumno().getId());
                st.setInt(2, hora);
                st.setInt(3, dia);
                st.setInt(4, getAlumno().getUnidad().getId());
                st.executeUpdate();
            } catch (SQLException ex) {
                Logger.getLogger(PanelApoyos.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public Alumno getAlumno() {
        return alumno;
    }

    public void setAlumno(Alumno alumno) {
        this.alumno = null;
        lInfoAlumno.setText("");
        //De primeras ponemos todos los apoyos a false
        for (int hora = 0; hora < 6; hora++) {
            for (int dia = 0; dia < 5; dia++) {
                tabla.getModel().setValueAt(false, hora, dia + 1);
            }
        }
        //Sacamos todos sus apoyos
        if (alumno != null) {
            lInfoAlumno.setText(alumno.getNombreFormateado());
            try {
                String sql = "SELECT distinct h.dia,h.hora FROM horarios_ AS h JOIN apoyos_alumnos AS aa ON aa.horario_id=h.id WHERE aa.alumno_id=? ";
                PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
                st.setInt(1, alumno.getId());
                ResultSet res = st.executeQuery();
                while (res.next()) {
                    tabla.getModel().setValueAt(true, res.getInt("hora") - 1, res.getInt("dia"));
                }
                Obj.cerrar(st, res);
            } catch (SQLException ex) {
                Logger.getLogger(PanelApoyos.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        this.alumno = alumno;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabla = new org.jdesktop.swingx.JXTable();
        lInfoAlumno = new javax.swing.JLabel();

        setName("Form"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getResourceMap(PanelApoyos.class);
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        tabla.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"1ª Hora", new Boolean(false), new Boolean(false), new Boolean(false), new Boolean(false), new Boolean(false)},
                {"2ª Hora", new Boolean(false), new Boolean(false), new Boolean(false), new Boolean(false), new Boolean(false)},
                {"3ª Hora", new Boolean(false), new Boolean(false), new Boolean(false), new Boolean(false), new Boolean(false)},
                {"4ª Hora", new Boolean(false), new Boolean(false), new Boolean(false), new Boolean(false), new Boolean(false)},
                {"5ª Hora", new Boolean(false), new Boolean(false), new Boolean(false), new Boolean(false), new Boolean(false)},
                {"6ª Hora", new Boolean(false), new Boolean(false), new Boolean(false), new Boolean(false), new Boolean(false)}
            },
            new String [] {
                "Hora", "Lunes", "Martes", "Miercoles", "Jueves", "Viernes"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Boolean.class, java.lang.Boolean.class, java.lang.Boolean.class, java.lang.Boolean.class, java.lang.Boolean.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        tabla.setName("tabla"); // NOI18N
        tabla.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(tabla);
        tabla.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("tabla.columnModel.title0")); // NOI18N
        tabla.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("tabla.columnModel.title1")); // NOI18N
        tabla.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("tabla.columnModel.title2")); // NOI18N
        tabla.getColumnModel().getColumn(3).setHeaderValue(resourceMap.getString("tabla.columnModel.title3")); // NOI18N
        tabla.getColumnModel().getColumn(4).setHeaderValue(resourceMap.getString("tabla.columnModel.title4")); // NOI18N
        tabla.getColumnModel().getColumn(5).setHeaderValue(resourceMap.getString("tabla.columnModel.title5")); // NOI18N

        lInfoAlumno.setText(resourceMap.getString("lInfoAlumno.text")); // NOI18N
        lInfoAlumno.setName("lInfoAlumno"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lInfoAlumno, javax.swing.GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 347, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(lInfoAlumno))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lInfoAlumno;
    private org.jdesktop.swingx.JXTable tabla;
    // End of variables declaration//GEN-END:variables
}
