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


package com.codeko.apps.maimonides.alumnos;

import com.codeko.apps.maimonides.IPanel;
import com.codeko.apps.maimonides.MaimonidesUtil;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.swing.CodekoTableModel;
import java.awt.event.KeyEvent;
import java.beans.Beans;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;


public class PanelBusquedaAlumnos extends javax.swing.JPanel implements IPanel {

    String ultimoTexto = "";
    Alumno alumnoSeleccionado = null;
    Thread tBusqueda = null;
    boolean cargarAlSeleccionar = true;
    boolean filtrarPorUsuario = true;
    PropertyChangeListener listener = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if ("borrado".equals(evt.getPropertyName())) {
                if (evt.getSource() instanceof Alumno) {
                    Alumno a = (Alumno) evt.getSource();
                    ResultadoBusquedaAlumno borrar = null;
                    for (ResultadoBusquedaAlumno rba : modelo.getDatos()) {
                        if (rba.getId() == a.getId()) {
                            borrar = rba;
                        }
                    }
                    if (borrar != null) {
                        modelo.quitarDato(borrar);
                    }
                    if (a.equals(getAlumnoSeleccionado())) {
                        setAlumnoSeleccionado(null);
                    }
                }
            }
        }
    };

    public boolean isCargarAlSeleccionar() {
        return cargarAlSeleccionar;
    }

    public void setCargarAlSeleccionar(boolean cargarAlSeleccionar) {
        this.cargarAlSeleccionar = cargarAlSeleccionar;
    }

    public boolean isFiltrarPorUsuario() {
        return filtrarPorUsuario;
    }

    public void setFiltrarPorUsuario(boolean filtrarPorUsuario) {
        this.filtrarPorUsuario = filtrarPorUsuario;
        buscador.setFiltrarPorUsuario(filtrarPorUsuario);
    }
    
    CodekoTableModel<ResultadoBusquedaAlumno> modelo = new CodekoTableModel<ResultadoBusquedaAlumno>(new ResultadoBusquedaAlumno());
    BuscadorAlumnos buscador = new BuscadorAlumnos() {

        @Override
        public void addResultadoBusqueda(ResultadoBusquedaAlumno rba) {
            modelo.addDato(rba);
            if (tabla.getSelectedRow() == -1) {
                tabla.getSelectionModel().setSelectionInterval(0, 0);
            }
        }

        @Override
        public void prepararBusqueda() {
            modelo.vaciar();
        }
    };

    /** Creates new form PanelBusquedaAlumnos */
    public PanelBusquedaAlumnos() {
        if (!Beans.isDesignTime()) {
            initComponents();
            tabla.getColumnExt(0).setMaxWidth(100);
            setAlumno(null);
            MaimonidesUtil.addMenuTabla(tabla, "Búsqueda de alumnos");
            panelAlumnos1.addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if ("alumnoClickeado".equals(evt.getPropertyName()) || (isCargarAlSeleccionar() && !isCerrarAlSeleccionar() && "alumnoMarcado".equals(evt.getPropertyName()))) {
                        if (evt.getNewValue() != null) {
                            ResultadoBusquedaAlumno rba = new ResultadoBusquedaAlumno();
                            Alumno a = (Alumno) evt.getNewValue();
                            rba.setId(a.getId());
                            rba.setNombre(a.getNombreFormateado());
                            if(a.getUnidad()!=null){
                                rba.setUnidad(a.getUnidad().getCursoGrupo());
                            }
                            setAlumno(rba);
                        }
                    }
                }
            });
        }
    }

    public boolean isCerrarAlSeleccionar() {
        return cbCerrarAlSeleccionar.isSelected();
    }

    public void setMostrarOpcionesVentana(boolean mostar) {
        cbCerrarAlSeleccionar.setVisible(mostar);
    }

    public boolean isMostrarOpcionesVentana() {
        return cbCerrarAlSeleccionar.isVisible();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pestanas = new javax.swing.JTabbedPane();
        panelBuscar = new javax.swing.JPanel();
        scrollTabla = new javax.swing.JScrollPane();
        tabla = new org.jdesktop.swingx.JXTable();
        lAlumno = new javax.swing.JLabel();
        tfAlumno = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        panelAlumnos1 = new com.codeko.apps.maimonides.alumnos.PanelAlumnos();
        panelAlumnos1.setModoSeleccion();
        lAlumnoSeleccionado = new javax.swing.JLabel();
        cbCerrarAlSeleccionar = new javax.swing.JCheckBox();

        setName("Form"); // NOI18N
        addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                formAncestorAdded(evt);
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });

        pestanas.setTabPlacement(javax.swing.JTabbedPane.RIGHT);
        pestanas.setName("pestanas"); // NOI18N

        panelBuscar.setName("panelBuscar"); // NOI18N

        scrollTabla.setName("scrollTabla"); // NOI18N

        tabla.setModel(modelo);
        tabla.setName("tabla"); // NOI18N
        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tablaMouseClicked(evt);
            }
        });
        scrollTabla.setViewportView(tabla);

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getResourceMap(PanelBusquedaAlumnos.class);
        lAlumno.setText(resourceMap.getString("lAlumno.text")); // NOI18N
        lAlumno.setName("lAlumno"); // NOI18N

        tfAlumno.setText(resourceMap.getString("tfAlumno.text")); // NOI18N
        tfAlumno.setName("tfAlumno"); // NOI18N
        tfAlumno.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tfAlumnoFocusGained(evt);
            }
        });
        tfAlumno.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tfAlumnoKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tfAlumnoKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout panelBuscarLayout = new javax.swing.GroupLayout(panelBuscar);
        panelBuscar.setLayout(panelBuscarLayout);
        panelBuscarLayout.setHorizontalGroup(
            panelBuscarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBuscarLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelBuscarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrollTabla, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 541, Short.MAX_VALUE)
                    .addGroup(panelBuscarLayout.createSequentialGroup()
                        .addComponent(lAlumno)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfAlumno, javax.swing.GroupLayout.DEFAULT_SIZE, 467, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelBuscarLayout.setVerticalGroup(
            panelBuscarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBuscarLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelBuscarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfAlumno, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lAlumno))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollTabla, javax.swing.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE)
                .addContainerGap())
        );

        pestanas.addTab(resourceMap.getString("panelBuscar.TabConstraints.tabTitle"), resourceMap.getIcon("panelBuscar.TabConstraints.tabIcon"), panelBuscar); // NOI18N

        jPanel1.setName("jPanel1"); // NOI18N

        panelAlumnos1.setName("panelAlumnos1"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 561, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(panelAlumnos1, javax.swing.GroupLayout.DEFAULT_SIZE, 541, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 165, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(panelAlumnos1, javax.swing.GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        pestanas.addTab(resourceMap.getString("jPanel1.TabConstraints.tabTitle"), resourceMap.getIcon("jPanel1.TabConstraints.tabIcon"), jPanel1); // NOI18N

        lAlumnoSeleccionado.setFont(resourceMap.getFont("lAlumnoSeleccionado.font")); // NOI18N
        lAlumnoSeleccionado.setText(resourceMap.getString("lAlumnoSeleccionado.text")); // NOI18N
        lAlumnoSeleccionado.setName("lAlumnoSeleccionado"); // NOI18N

        cbCerrarAlSeleccionar.setSelected(true);
        cbCerrarAlSeleccionar.setText(resourceMap.getString("cbCerrarAlSeleccionar.text")); // NOI18N
        cbCerrarAlSeleccionar.setName("cbCerrarAlSeleccionar"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lAlumnoSeleccionado, javax.swing.GroupLayout.DEFAULT_SIZE, 483, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbCerrarAlSeleccionar)
                .addContainerGap())
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(pestanas, javax.swing.GroupLayout.DEFAULT_SIZE, 602, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(180, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lAlumnoSeleccionado)
                    .addComponent(cbCerrarAlSeleccionar))
                .addContainerGap())
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(pestanas, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
                    .addGap(29, 29, 29)))
        );

        cbCerrarAlSeleccionar.setVisible(false);
    }// </editor-fold>//GEN-END:initComponents

private void tfAlumnoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfAlumnoKeyPressed
    if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
        abajo();
    } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
        arriba();
    }
}//GEN-LAST:event_tfAlumnoKeyPressed

private void tfAlumnoKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfAlumnoKeyReleased
    if (!evt.isActionKey() && !ultimoTexto.equals(tfAlumno.getText().trim())) {
        ultimoTexto = tfAlumno.getText().trim();
        if (tBusqueda != null && tBusqueda.isAlive()) {
            tBusqueda.interrupt();
        }
        if (ultimoTexto.length() >= 2) {
            tBusqueda = new Thread() {

                @Override
                public void run() {
                    try {
                        Thread.sleep(500);
                        if (!isInterrupted()) {
                            System.out.println("Buscando por:" + ultimoTexto);
                            buscador.buscar(ultimoTexto);
                        }
                    } catch (InterruptedException ex) {
                        //Logger.getLogger(PanelBusquedaAlumnos.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };
            tBusqueda.setPriority(Thread.NORM_PRIORITY);
            tBusqueda.start();

        } else {
            modelo.vaciar();
        }
    } else if (evt.getKeyChar() == '\n') {
        int row = tabla.getSelectedRow();
        if (row > -1) {
            row = tabla.convertRowIndexToModel(row);
            setAlumno(modelo.getElemento(row));
            firePropertyChange("enterPulsado", null, null);
        }
    }
}//GEN-LAST:event_tfAlumnoKeyReleased

private void tfAlumnoFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfAlumnoFocusGained
    tfAlumno.selectAll();
}//GEN-LAST:event_tfAlumnoFocusGained

private void formAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_formAncestorAdded
    tfAlumno.requestFocus();
}//GEN-LAST:event_formAncestorAdded

private void tablaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tablaMouseClicked
    if (SwingUtilities.isLeftMouseButton(evt) && evt.getClickCount() > 1) {
        int row = tabla.rowAtPoint(evt.getPoint());
        row = tabla.convertRowIndexToModel(row);
        setAlumno(modelo.getElemento(row));
    }
}//GEN-LAST:event_tablaMouseClicked

    @Override
    public boolean puedoSusituir() {
        return true;
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

    public Alumno getAlumnoSeleccionado() {
        return alumnoSeleccionado;
    }

    public void setAlumnoSeleccionado(Alumno alumnoSeleccionado) {
        Alumno ultimo = this.alumnoSeleccionado;
        if (ultimo != null) {
            ultimo.removePropertyChangeListener(listener);
        }
        if (alumnoSeleccionado != null) {
            alumnoSeleccionado.addPropertyChangeListener(listener);
        }
        this.alumnoSeleccionado = alumnoSeleccionado;
        if (alumnoSeleccionado == null) {
            lAlumnoSeleccionado.setText("No hay ningún alumno seleccionado.");
        } else {
            lAlumnoSeleccionado.setText(alumnoSeleccionado.getNombreFormateado() + " (" + alumnoSeleccionado.getUnidad() + ")");
        }
        firePropertyChange("alumnoSeleccionado", ultimo, alumnoSeleccionado);
    }

    private void setAlumno(ResultadoBusquedaAlumno rba) {
        if (rba == null) {
            lAlumnoSeleccionado.setText("No hay ningún alumno seleccionado.");
        }
        if (rba != null) {
            try {
                setAlumnoSeleccionado(Alumno.getAlumno(rba.getId()));
            } catch (SQLException ex) {
                Logger.getLogger(PanelBusquedaAlumnos.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            setAlumnoSeleccionado(null);
        }
    }

    public void activar() {
        tfAlumno.requestFocus();
        tfAlumno.selectAll();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbCerrarAlSeleccionar;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lAlumno;
    private javax.swing.JLabel lAlumnoSeleccionado;
    private com.codeko.apps.maimonides.alumnos.PanelAlumnos panelAlumnos1;
    private javax.swing.JPanel panelBuscar;
    private javax.swing.JTabbedPane pestanas;
    private javax.swing.JScrollPane scrollTabla;
    private org.jdesktop.swingx.JXTable tabla;
    private javax.swing.JTextField tfAlumno;
    // End of variables declaration//GEN-END:variables
}
