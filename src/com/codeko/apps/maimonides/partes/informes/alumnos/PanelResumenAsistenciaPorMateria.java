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
 * PanelResumenAsistenciaPorMateria.java
 *
 * Created on 11-may-2009, 11:47:39
 */
package com.codeko.apps.maimonides.partes.informes.alumnos;

import com.codeko.apps.maimonides.ICargable;
import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesUtil;
import com.codeko.apps.maimonides.alumnos.IFiltrableAlumno;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.Materia;
import com.codeko.swing.CodekoAutoTableModel;
import com.mysql.jdbc.PreparedStatement;
import java.awt.Color;
import java.awt.Component;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;

/**
 *
 * @author Codeko
 */
public class PanelResumenAsistenciaPorMateria extends javax.swing.JPanel implements ICargable, IFiltrableAlumno {

    CodekoAutoTableModel<ResumenAsistenciaPorMateria> modelo = new CodekoAutoTableModel<ResumenAsistenciaPorMateria>(ResumenAsistenciaPorMateria.class);
    Alumno alumno = null;
    boolean cargado = false;

    /** Creates new form PanelResumenAsistenciaPorMateria */
    public PanelResumenAsistenciaPorMateria() {
        initComponents();
        ColorHighlighter co = new ColorHighlighter(new HighlightPredicate() {

            @Override
            public boolean isHighlighted(Component arg0, ComponentAdapter arg1) {
                ResumenAsistenciaPorMateria res = modelo.getElemento(tabla.convertRowIndexToModel(arg1.row));
                if (res.getMateria() != null && res.getMateria().getMaxFaltas() > 0) {
                    return res.getTotalFaltas() >= res.getMateria().getMaxFaltas();
                } else {
                    return false;
                }
            }
        }, Color.RED.brighter().brighter(), Color.BLACK);


        ColorHighlighter co2 = new ColorHighlighter(new HighlightPredicate() {

            @Override
            public boolean isHighlighted(Component arg0, ComponentAdapter arg1) {
                ResumenAsistenciaPorMateria res = modelo.getElemento(tabla.convertRowIndexToModel(arg1.row));
                if (res.getMateria() != null && res.getMateria().getMaxFaltas() > 0) {
                    int faltas = res.getTotalFaltas();
                    //TODO Este porcentaje estaría bien que fuese configurable
                    int max = (int) (res.getMateria().getMaxFaltas() * 0.80);
                    return faltas >= max;
                } else {
                    return false;
                }
            }
        }, Color.ORANGE, Color.BLACK);

        ColorHighlighter co3 = new ColorHighlighter(new HighlightPredicate() {

            @Override
            public boolean isHighlighted(Component arg0, ComponentAdapter arg1) {
                ResumenAsistenciaPorMateria res = modelo.getElemento(tabla.convertRowIndexToModel(arg1.row));
                if (res.getMateria() != null && res.getMateria().getMaxFaltas() > 0) {
                    int faltas = res.getTotalFaltas();
                    //TODO Este porcentaje estaría bien que fuese configurable
                    int max = (int) (res.getMateria().getMaxFaltas() * 0.50);
                    return faltas >= max;
                } else {
                    return false;
                }
            }
        }, Color.lightGray, Color.BLACK);
        tabla.addHighlighter(co3);
        tabla.addHighlighter(co2);
        tabla.addHighlighter(co);
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

    @Action(block = Task.BlockingScope.ACTION)
    public Task actualizar() {
        return new ActualizarTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    @Override
    public void cargar() {
        MaimonidesUtil.ejecutarTask(this, "actualizar");
    }

    @Override
    public void vaciar() {
        modelo.vaciar();
        setCargado(false);
    }

    @Override
    public boolean isCargado() {
        return this.cargado;
    }

    @Override
    public void setCargado(boolean cargado) {
        this.cargado = cargado;
    }

    private class ActualizarTask extends org.jdesktop.application.Task<Object, Void> {

        ActualizarTask(org.jdesktop.application.Application app) {
            super(app);
            vaciar();
        }

        @Override
        protected Object doInBackground() {
            //Si tenemos alumno y este esta guardado
            if (getAlumno() != null && getAlumno().getId() != null) {
                setMessage("Actualizando...");
                PreparedStatement st = null;
                ResultSet res = null;
                String sql = "SELECT pa.asistencia,count(*)AS total ,count(distinct p.fecha)AS totalDias , "
                        + " IFNULL(m.nombre,a.descripcion) AS descripcion,IFNULL(m.codigo_materia,\"\") AS nombre,IFNULL(m.id,0) AS materia "
                        + " FROM partes_alumnos AS pa "
                        + " JOIN partes AS p ON pa.parte_id=p.id "
                        + " JOIN horarios AS h ON h.id=pa.horario_id "
                        + " JOIN actividades AS a ON a.id=h.actividad_id "
                        + " LEFT JOIN materias AS m ON m.id=h.materia_id "
                        + " WHERE pa.alumno_id=? AND asistencia>1 GROUP BY pa.asistencia,a.id,m.id ORDER BY nombre ";
                try {
                    st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
                    st.setInt(1, getAlumno().getId());
                    res = st.executeQuery();
                    ResumenAsistenciaPorMateria m = null;
                    while (res.next()) {
                        String desc = res.getString("descripcion");
                        if (m == null || !m.getDescripcion().equals(desc)) {
                            if (m != null) {
                                modelo.addDato(m);
                            }
                            m = new ResumenAsistenciaPorMateria();
                            m.setNombre(res.getString("nombre"));
                            m.setDescripcion(desc);
                            int mat = res.getInt("materia");
                            if (mat > 0) {
                                try {
                                    m.setMateria(Materia.getMateria(mat));
                                } catch (Exception ex) {
                                    Logger.getLogger(PanelResumenAsistenciaPorMateria.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }
                        int tipo = res.getInt("asistencia");
                        int total = res.getInt("total");
                        int totalDias = res.getInt("totalDias");
                        m.setValor(tipo, total, totalDias);
                    }
                    if (m != null) {
                        modelo.addDato(m);
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(PanelResumenAsistenciaPorMateria.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            return null;
        }

        @Override
        protected void succeeded(Object result) {
            setMessage("Resumen de asistencia cargado correctamente.");
            tabla.packAll();
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
        jPanel1 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();

        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        scroll.setName("scroll"); // NOI18N

        tabla.setModel(modelo);
        tabla.setColumnControlVisible(true);
        tabla.setName("tabla"); // NOI18N
        scroll.setViewportView(tabla);

        add(scroll, java.awt.BorderLayout.CENTER);

        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 5));

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getResourceMap(PanelResumenAsistenciaPorMateria.class);
        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N
        jPanel1.add(jLabel4);

        jLabel1.setBackground(Color.RED.brighter().brighter());
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N
        jLabel1.setOpaque(true);
        jPanel1.add(jLabel1);

        jLabel2.setBackground(Color.ORANGE);
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N
        jLabel2.setOpaque(true);
        jPanel1.add(jLabel2);

        jLabel3.setBackground(Color.lightGray);
        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N
        jLabel3.setOpaque(true);
        jPanel1.add(jLabel3);

        add(jPanel1, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane scroll;
    private org.jdesktop.swingx.JXTable tabla;
    // End of variables declaration//GEN-END:variables
}
