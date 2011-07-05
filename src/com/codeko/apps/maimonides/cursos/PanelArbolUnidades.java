/*
 * PanelArbolUnidades.java
 *
 * Created on 9 de septiembre de 2008, 10:48
 */
package com.codeko.apps.maimonides.cursos;

import com.codeko.apps.maimonides.ICargable;
import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesUtil;
import com.codeko.apps.maimonides.elementos.Unidad;
import com.codeko.apps.maimonides.usr.Permisos;
import com.codeko.util.Obj;
import com.mysql.jdbc.PreparedStatement;
import java.beans.Beans;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;

/**
 *
 * @author  Codeko
 */
public class PanelArbolUnidades extends javax.swing.JPanel implements ICargable {

    DefaultMutableTreeNode nodoBase = new DefaultMutableTreeNode("Cursos");
    boolean cargado = false;
    PanelArbolUnidades auto = this;
    boolean autoOcultar = true;
    boolean mostrarElementoRaiz=false;

    /** Creates new form PanelArbolUnidades */
    public PanelArbolUnidades() {
        initComponents();
        arbol.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {

            @Override
            public void valueChanged(TreeSelectionEvent e) {
                Object old = getObjetoArbol(e.getOldLeadSelectionPath());
                Object nuevo = getObjetoArbol(e.getNewLeadSelectionPath());
                if ("Cursos".equals(nuevo)) {
                    nuevo = null;
                }
                firePropertyChange("seleccionArbol", old, nuevo);
                if (nuevo instanceof Unidad) {
                    firePropertyChange("unidadSeleccionada", old, nuevo);
                } else if (old instanceof Unidad) {
                    firePropertyChange("unidadSeleccionada", old, null);
                }
            }
        });
        arbol.setRootVisible(false);
        arbol.setShowsRootHandles(true);
    }

    public boolean isAutoOcultar() {
        return autoOcultar;
    }

    public void setAutoOcultar(boolean autoOcultar) {
        this.autoOcultar = autoOcultar;
    }

    public boolean isMostrarElementoRaiz() {
        return mostrarElementoRaiz;
    }

    public void setNombreElementoRaiz(String nombre){
        nodoBase.setUserObject(nombre);
    }

    public String getNombreElementoRaiz(){
        return nodoBase.getUserObject().toString();
    }

    public void setMostrarElementoRaiz(boolean mostrarElementoRaiz) {
        this.mostrarElementoRaiz = mostrarElementoRaiz;
        arbol.setRootVisible(mostrarElementoRaiz);
    }



    //TODO Esto sería interesante moverlo a util
    private Object getObjetoArbol(TreePath path) {
        if (path != null) {
            Object val = path.getLastPathComponent();
            if (val instanceof DefaultMutableTreeNode) {
                return ((DefaultMutableTreeNode) val).getUserObject();
            }
        }
        return null;
    }

    public void desmarcar() {
        arbol.getSelectionModel().clearSelection();
    }

    public Object getObjetoSeleccionado() {
        TreePath tp = arbol.getSelectionPath();
        Object ret = null;
        if (tp != null) {
            ret = getObjetoArbol(tp);
        }
        return ret;
    }

    public ArrayList<Object> getObjetosSeleccionados() {
        TreePath[] tp = arbol.getSelectionPaths();
        ArrayList<Object> ret = new ArrayList<Object>();
        for (TreePath t : tp) {
            ret.add(getObjetoArbol(t));
        }
        return ret;
    }

    public ArrayList<Unidad> getUnidadesSeleccionadas() {
        TreePath[] tp = arbol.getSelectionPaths();
        ArrayList<Unidad> ret = new ArrayList<Unidad>();
        for (TreePath t : tp) {
            ret.addAll(getUnidadesRama(t));
        }
        return ret;
    }

    private ArrayList<Unidad> getUnidadesRama(TreePath t) {
        ArrayList<Unidad> uds = new ArrayList<Unidad>();
        Object obj = getObjetoArbol(t);
        if (obj instanceof Unidad) {
            uds.add((Unidad) obj);
        } else {
            Object val = t.getLastPathComponent();
            if (val instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) val;
                int count = dmtn.getChildCount();
                for (int i = 0; i < count; i++) {
                    TreeNode[] ptr = ((DefaultMutableTreeNode) dmtn.getChildAt(i)).getPath();
                    TreePath tp = new TreePath(ptr);
                    uds.addAll(getUnidadesRama(tp));
                }
            }
        }
        return uds;
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
        arbol = new javax.swing.JTree(nodoBase);

        setName("Form"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getResourceMap(PanelArbolUnidades.class);
        arbol.setToolTipText(resourceMap.getString("arbol.toolTipText")); // NOI18N
        arbol.setName("arbol"); // NOI18N
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setLeafIcon(resourceMap.getIcon("arbol.leaft.icon"));
        renderer.setOpenIcon(resourceMap.getIcon("arbol.open.icon"));
        renderer.setClosedIcon(resourceMap.getIcon("arbol.closed.icon"));
        arbol.setCellRenderer(renderer);
        arbol.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                arbolAncestorAdded(evt);
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });
        jScrollPane1.setViewportView(arbol);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

private void arbolAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_arbolAncestorAdded
    cargar();
}//GEN-LAST:event_arbolAncestorAdded

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task cargarUnidades() {
        return new CargarUnidadesTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
    }

    @Override
    public void cargar() {
        if (!Beans.isDesignTime() && !isCargado()) {
            MaimonidesUtil.ejecutarTask(this, "cargarUnidades");
        }
    }

    @Override
    public void vaciar() {
        nodoBase.removeAllChildren();
        setCargado(false);
    }

    @Override
    public boolean isCargado() {
        return cargado;
    }

    @Override
    public void setCargado(boolean cargado) {
        this.cargado = cargado;
    }

    private class CargarUnidadesTask extends org.jdesktop.application.Task<Object, Void> {

        CargarUnidadesTask(org.jdesktop.application.Application app) {
            super(app);
            vaciar();
            setCargado(true);
        }

        @Override
        protected Object doInBackground() {
            if (!Beans.isDesignTime()) {
                try {
                    //tenemos que recuperar todos los cursos y las unidades de estos
                    String filtro = "";
                    Unidad ud = Permisos.getFiltroUnidad();
                    if (ud != null) {
                        filtro = " AND u.id=? ";
                    }
                    PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT distinct u.* FROM unidades AS u LEFT JOIN cursos AS c ON c.curso=u.curso WHERE u.ano=? " + filtro + " ORDER BY c.posicion,u.posicion");
                    st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
                    if (ud != null) {
                        st.setInt(2, ud.getId());
                    }
                    ResultSet res = st.executeQuery();
                    String ultimoCurso = null;
                    DefaultMutableTreeNode nodoCurso = null;
                    while (res.next()) {
                        String curso = res.getString("curso");
                        Unidad u = new Unidad();
                        u.cargarDesdeResultSet(res);
                        if (nodoCurso == null || !curso.equals(ultimoCurso)) {
                            nodoCurso = new DefaultMutableTreeNode(curso);
                            nodoBase.add(nodoCurso);
                        }
                        ultimoCurso = curso;
                        DefaultMutableTreeNode nodoUnidad = new DefaultMutableTreeNode(u);
                        nodoCurso.add(nodoUnidad);
                    }
                    Obj.cerrar(st, res);
                } catch (Exception ex) {
                    Logger.getLogger(PanelArbolUnidades.class.getName()).log(Level.SEVERE, "Error cargando unidades", ex);
                }
            }
            return null;
        }

        @Override
        protected void succeeded(Object result) {
            //Ahora revisamos los nodos para aquellos que solo tengan una unidad sustituirlos por us contenido
            ArrayList<TreeNode> nodosBorrar = new ArrayList<TreeNode>();
            for (int i = 0; i < nodoBase.getChildCount(); i++) {
                TreeNode t = nodoBase.getChildAt(i);
                if (t.getChildCount() == 1) {
                    nodosBorrar.add(t);
                }
            }
            for (TreeNode t : nodosBorrar) {
                nodoBase.insert((DefaultMutableTreeNode) t.getChildAt(0), nodoBase.getIndex(t));
                nodoBase.remove((DefaultMutableTreeNode) t);
            }
            arbol.expandPath(new TreePath(nodoBase));
            setCargado(true);
            //Ahora vemos si hay un sólo nodo
            if (isAutoOcultar() && nodoBase.getChildCount() == 1) {
                arbol.setSelectionInterval(0, 0);
                auto.setVisible(false);
            }
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTree arbol;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
