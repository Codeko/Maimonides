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
 * PanelCreacionPartes.java
 *
 * Created on 19 de agosto de 2008, 13:30
 */
package com.codeko.apps.maimonides.partes;

import com.codeko.apps.maimonides.IPanel;
import com.codeko.apps.maimonides.impresion.Impresion;
import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesBean;
import com.codeko.apps.maimonides.MaimonidesUtil;
import com.codeko.apps.maimonides.elementos.AnoEscolar;
import com.codeko.apps.maimonides.elementos.ParteFaltas;
import com.codeko.apps.maimonides.seneca.operaciones.calendario.GestorCalendarioSeneca;
import com.codeko.apps.maimonides.usr.Permisos;
import com.codeko.util.Fechas;
import com.codeko.util.Obj;
import com.mysql.jdbc.PreparedStatement;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.Beans;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;

/**
 *
 * @author  Codeko
 */
public class PanelListaPartes extends javax.swing.JPanel implements IPanel {

    PanelListaPartes auto = this;
    DefaultMutableTreeNode nodoBase = new DefaultMutableTreeNode("-");
    boolean generarPartesComprimidos = MaimonidesApp.getApplication().getConfiguracion().isComprimirPartes();

    public boolean isGenerarPartesComprimidos() {
        return generarPartesComprimidos;
    }

    public void setGenerarPartesComprimidos(boolean generarPartesComprimidos) {
        this.generarPartesComprimidos = generarPartesComprimidos;
    }

    public void setMostrarBotonImprimir(boolean mostrar) {
        bImprimirPartes.setVisible(mostrar);
    }

    public void setMostrarBotonGenerar(boolean mostrar) {
        bCargarPartes.setVisible(mostrar);
    }

    public boolean isMostrarBotonImprimir() {
        return bImprimirPartes.isVisible();
    }

    public boolean isMostrarBotonGenerar() {
        return bCargarPartes.isVisible();
    }

    /** Creates new form PanelCreacionPartes */
    public PanelListaPartes() {
        initComponents();
        nodoBase.setAllowsChildren(true);
        vaciarPartes(getFecha());
        arbol.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tfFecha.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("date".equals(evt.getPropertyName())) {
                    vaciarPartes(Fechas.toGregorianCalendar((Date) evt.getNewValue()));
                    verificarPartesFecha();
                    firePropertyChange("cambioFecha", evt.getOldValue(), evt.getNewValue());
                }
            }
        });

        arbol.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {

            @Override
            public void valueChanged(TreeSelectionEvent e) {
                firePropertyChange("seleccionArbol", e.getOldLeadSelectionPath(), e.getNewLeadSelectionPath());
            }
        });
        MaimonidesUtil.setFormatosFecha(tfFecha, true);
        tfFecha.setDate(new Date());
        bBorrar.setVisible(Permisos.borrado(this));
        bCargarPartes.setVisible(Permisos.creacion(this));
    }

    private void verificarPartesFecha() {
        MaimonidesUtil.ejecutarTask(this, "cargarListaPartes");
    }

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task cargarListaPartes() {
        return new CargarListaPartesTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class CargarListaPartesTask extends org.jdesktop.application.Task<ArrayList<ParteFaltas>, Void> {

        public CargarListaPartesTask(org.jdesktop.application.Application app) {
            super(app);
            firePropertyChange("setIniciado", null, true);
            setMessage("Cargando partes de asistencia...");
        }

        @Override
        protected ArrayList<ParteFaltas> doInBackground() {
            ParteDataSourceProvider pdsp = new ParteDataSourceProvider(MaimonidesApp.getApplication().getAnoEscolar(), getFecha(), null);
            pdsp.getBean().addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                }
            });
            pdsp.cargarPartes();
            if (pdsp.getPartes().size() != 0) {
                return pdsp.getPartes();
            }
            return null;
        }

        @Override
        protected void succeeded(ArrayList<ParteFaltas> result) {
            firePropertyChange("setTerminado", null, result != null);
            if (result != null) {
                cargarArbolPartes(result, getFecha());
            }
        }
    }

    public GregorianCalendar getFecha() {
        return Fechas.toGregorianCalendar(tfFecha.getDate());
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        arbol = new javax.swing.JTree(nodoBase);
        lFecha = new javax.swing.JLabel();
        tfFecha = new org.jdesktop.swingx.JXDatePicker();
        jPanel1 = new javax.swing.JPanel();
        bCargarPartes = new javax.swing.JButton();
        bImprimirPartes = new javax.swing.JButton();
        bBorrar = new javax.swing.JButton();

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

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getResourceMap(PanelListaPartes.class);
        arbol.setToolTipText(resourceMap.getString("arbol.toolTipText")); // NOI18N
        arbol.setName("arbol"); // NOI18N
        jScrollPane1.setViewportView(arbol);
        DefaultTreeCellRenderer renderer =new DefaultTreeCellRenderer();
        renderer.setLeafIcon(resourceMap.getIcon("arbol.leaft.icon"));
        arbol.setCellRenderer(renderer);

        lFecha.setIcon(resourceMap.getIcon("lFecha.icon")); // NOI18N
        lFecha.setToolTipText(resourceMap.getString("lFecha.toolTipText")); // NOI18N
        lFecha.setName("lFecha"); // NOI18N

        tfFecha.setName("tfFecha"); // NOI18N

        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getActionMap(PanelListaPartes.class, this);
        bCargarPartes.setAction(actionMap.get("cargarPartes")); // NOI18N
        bCargarPartes.setName("bCargarPartes"); // NOI18N
        bCargarPartes.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                bCargarPartesMouseClicked(evt);
            }
        });
        jPanel1.add(bCargarPartes);

        bImprimirPartes.setAction(actionMap.get("imprimirPartes")); // NOI18N
        bImprimirPartes.setName("bImprimirPartes"); // NOI18N
        jPanel1.add(bImprimirPartes);

        bBorrar.setAction(actionMap.get("borrarPartes")); // NOI18N
        bBorrar.setName("bBorrar"); // NOI18N
        jPanel1.add(bBorrar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 132, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lFecha)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfFecha, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lFecha)
                    .addComponent(tfFecha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 357, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

private void formAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_formAncestorAdded
    if (!Beans.isDesignTime() && !isPartesCargados()) {
        verificarPartesFecha();
    }
}//GEN-LAST:event_formAncestorAdded

private void bCargarPartesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bCargarPartesMouseClicked
    if (SwingUtilities.isRightMouseButton(evt) && evt.getClickCount() == 1) {
        JPopupMenu m = new JPopupMenu();
        final JCheckBoxMenuItem mi = new JCheckBoxMenuItem("Intentar comprimir partes");
        mi.setSelected(isGenerarPartesComprimidos());
        mi.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setGenerarPartesComprimidos(mi.isSelected());
            }
        });
        m.add(mi);
        m.show((Component) evt.getSource(), evt.getX(), evt.getY());
    }
}//GEN-LAST:event_bCargarPartesMouseClicked

    public void vaciarPartes(GregorianCalendar fecha) {
        setPartesCargados(false);
        nodoBase.removeAllChildren();
        nodoBase.setUserObject("No existen partes para " + Fechas.format(fecha) + ".");
        arbol.updateUI();
    }

    public void cargarArbolPartes(Collection<ParteFaltas> partes, GregorianCalendar fecha) {
        vaciarPartes(fecha);
        nodoBase.setUserObject("Partes del " + Fechas.format(fecha));
        for (ParteFaltas p : partes) {
            DefaultMutableTreeNode nodo = new DefaultMutableTreeNode(p);
            //Ahora buscamos donde colocarlo
            int count = nodoBase.getChildCount();
            boolean encontrado = false;
            for (int i = 0; i < count && !encontrado; i++) {
                DefaultMutableTreeNode n = (DefaultMutableTreeNode) nodoBase.getChildAt(i);
                if (n.getUserObject().equals(p.getCurso())) {
                    n.add(nodo);
                    encontrado = true;
                }
            }
            if (!encontrado) {
                String curso = p.getCurso();
                DefaultMutableTreeNode nodoCurso = new DefaultMutableTreeNode(curso);
                nodoCurso.add(nodo);
                nodoBase.add(nodoCurso);
            }
        }
        arbol.updateUI();
        arbol.expandRow(0);
        arbol.setSelectionInterval(0, 0);
        expandAll(arbol, true);
        setPartesCargados(true);
    }
    //TODO Estas dos funciones moverlas a CodekoLib

    public void expandAll(JTree tree, boolean expand) {
        TreeNode root = (TreeNode) tree.getModel().getRoot();
        expandAll(tree, new TreePath(root), expand);
    }

    private void expandAll(JTree tree, TreePath parent, boolean expand) {
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (Enumeration e = node.children(); e.hasMoreElements();) {
                TreeNode n = (TreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandAll(tree, path, expand);
            }
        }
        if (expand) {
            tree.expandPath(parent);
        } else {
            tree.collapsePath(parent);
        }
    }
    private boolean partesCargados = false;

    public boolean isPartesCargados() {
        return partesCargados;
    }

    public void setPartesCargados(boolean b) {
        boolean old = isPartesCargados();
        this.partesCargados = b;
        firePropertyChange("partesCargados", old, isPartesCargados());
        setPartesNoCargados(!b);
    }
    private boolean partesNoCargados = false;

    public boolean isPartesNoCargados() {
        return partesNoCargados;
    }

    public void setPartesNoCargados(boolean b) {
        boolean old = isPartesNoCargados();
        this.partesNoCargados = b;
        firePropertyChange("partesNoCargados", old, isPartesNoCargados());
    }

    public Object getSeleccionado() {
        Object seleccionado = arbol.getSelectionPath().getLastPathComponent();
        return seleccionado;
    }

    @Action(block = Task.BlockingScope.APPLICATION, enabledProperty = "partesNoCargados")
    public Task cargarPartes() {
        return new CargarPartesTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class CargarPartesTask extends org.jdesktop.application.Task<Object, Void> {

        CreadorPartes creador = new CreadorPartes(MaimonidesApp.getApplication().getAnoEscolar());

        CargarPartesTask(org.jdesktop.application.Application app) {
            super(app);
            GregorianCalendar fecha=Fechas.toGregorianCalendar(tfFecha.getDate());
            if(GestorCalendarioSeneca.isFestivoDocente(fecha)){
                int op=JOptionPane.showConfirmDialog(MaimonidesApp.getApplication().getMainFrame(), "La fecha indicada está marcada como festivo ("+GestorCalendarioSeneca.getDia(fecha).getDescripcion()+").\n¿Crear partes de todas formas?", "Día festivo", JOptionPane.OK_CANCEL_OPTION);
                if(op!=JOptionPane.OK_OPTION){
                    cancel(true);
                }
            }
            creador.setComprimir(isGenerarPartesComprimidos());
            creador.setFecha(fecha);
        }

        @Override
        protected Object doInBackground() {
            creador.setCrear(Permisos.creacion(PanelListaPartes.class));
            creador.addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                }
            });
            creador.recuperarPartes();
            return null;
        }

        @Override
        protected void succeeded(Object result) {
            //Una vez recuperados cargamos el arbol
            cargarArbolPartes(creador.getPartesFaltas(), creador.getFecha());
        }
    }

    @Action(block = Task.BlockingScope.APPLICATION, enabledProperty = "partesCargados")
    public Task imprimirPartes() {
        return new ImprimirPartesTask(MaimonidesApp.getApplication());
    }

    private class ImprimirPartesTask extends org.jdesktop.application.Task<Object, Void> {
        //CreadorPartes creador=new CreadorPartes(MaimonidesApp.getApplication().getAnoEscolar());

        AnoEscolar anoEscolar = MaimonidesApp.getApplication().getAnoEscolar();
        GregorianCalendar fecha = null;

        ImprimirPartesTask(org.jdesktop.application.Application app) {
            super(app);
            fecha = Fechas.toGregorianCalendar(tfFecha.getDate());
        }

        @Override
        protected Object doInBackground() {
            //Según la selección imprimirmos de una forma u otra
            Object seleccionado = arbol.getSelectionPath().getLastPathComponent();
            MaimonidesBean bean = new MaimonidesBean();
            bean.addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                }
            });
            if (seleccionado == null || seleccionado.equals(nodoBase)) {
                Impresion.getImpresion().imprimirPartes(bean, anoEscolar, fecha);
            } else if (seleccionado instanceof DefaultMutableTreeNode && ((DefaultMutableTreeNode) seleccionado).getUserObject() instanceof String) {
                Impresion.getImpresion().imprimirPartes(bean, anoEscolar, fecha, ((DefaultMutableTreeNode) seleccionado).getUserObject().toString());
            } else if (seleccionado instanceof DefaultMutableTreeNode && ((DefaultMutableTreeNode) seleccionado).getUserObject() instanceof ParteFaltas) {
                Impresion.getImpresion().imprimirPartes(bean, anoEscolar, fecha, (ParteFaltas) ((DefaultMutableTreeNode) seleccionado).getUserObject());
            }
            return null;
        }

        @Override
        protected void succeeded(Object result) {
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTree arbol;
    private javax.swing.JButton bBorrar;
    private javax.swing.JButton bCargarPartes;
    private javax.swing.JButton bImprimirPartes;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lFecha;
    private org.jdesktop.swingx.JXDatePicker tfFecha;
    // End of variables declaration//GEN-END:variables

    @Override
    public boolean puedoSusituir() {
        return true;
    }

    @Action(block = Task.BlockingScope.APPLICATION, enabledProperty = "partesCargados")
    public Task borrarPartes() {
        return new BorrarPartesTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    private class BorrarPartesTask extends org.jdesktop.application.Task<Boolean, Void> {

        boolean borrar = false;

        BorrarPartesTask(org.jdesktop.application.Application app) {
            super(app);
            int op = JOptionPane.showConfirmDialog(MaimonidesApp.getApplication().getMainFrame(), "¿Borrar todos los partes de la fecha seleccionada?.\nSi ha impreso los partes no podrá digitalizarlos una vez borrados.", "Borrar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            borrar = op == JOptionPane.YES_OPTION;
        }

        @Override
        protected Boolean doInBackground() {
            if (borrar) {
                PreparedStatement stBorrar = null;
                try {
                    stBorrar = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("DELETE FROM partes WHERE fecha =?");
                    stBorrar.setDate(1, new java.sql.Date(getFecha().getTime().getTime()));
                    return stBorrar.executeUpdate() > 0;
                } catch (SQLException ex) {
                    Logger.getLogger(PanelListaPartes.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    Obj.cerrar(stBorrar);
                }
            }
            return false;
        }

        @Override
        protected void succeeded(Boolean result) {
            if (borrar && result) {
                vaciarPartes(getFecha());
                setMessage("Partes elíminados correctamente");
            }
        }
    }
}
