/*
 * PanelCorrespondencia.java
 *
 * Created on 09-sep-2009, 10:20:05
 */
package com.codeko.apps.maimonides.alumnos;

import com.codeko.apps.maimonides.ICargable;
import com.codeko.apps.maimonides.IPanel;
import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesUtil;
import com.codeko.apps.maimonides.cartero.Carta;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.Unidad;
import com.codeko.apps.maimonides.usr.Permisos;
import com.codeko.swing.CodekoAutoTableModel;
import com.codeko.util.Fechas;
import com.codeko.util.Num;
import com.codeko.util.Obj;
import com.mysql.jdbc.PreparedStatement;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.Beans;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;

/**
 *
 * @author Codeko
 */
public class PanelCorrespondencia extends javax.swing.JPanel implements ICargable, IAlumno, IPanel,IFiltrableAlumno {

    CodekoAutoTableModel<Carta> modelo = new CodekoAutoTableModel<Carta>(Carta.class);
    boolean cargado = false;
    Alumno alumno = null;
    boolean modoFichaAlumno = false;

    /** Creates new form PanelCorrespondencia */
    public PanelCorrespondencia() {
        initComponents();
        tabla.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
                    abrir();
                }
            }
        });
        tabla.getColumnExt("Notificación").setVisible(false);
        //tabla.getColumnExt("Alumno").setVisible(false);
        tabla.getColumnExt("Aviso").setVisible(false);
        tabla.getColumnExt("Localizador").setVisible(false);
        tabla.setDefaultRenderer(GregorianCalendar.class, new DefaultTableCellRenderer() {

            @Override
            public void setValue(Object val) {
                if (val instanceof GregorianCalendar) {
                    setText(Fechas.format((GregorianCalendar) val, "dd/MM/yyyy hh:mm"));
                } else {
                    setText("");
                }
            }
        });
        tabla.getColumnExt("Tipo").setCellRenderer(new DefaultTableCellRenderer() {

            @Override
            public void setValue(Object val) {
                int tipo = Num.getInt(val);
                setText(Carta.getNombreTipo(tipo));
            }
        });

        tabla.getColumnExt("Medio").setCellRenderer(new DefaultTableCellRenderer() {

            @Override
            public void setValue(Object val) {
                int medio = Num.getInt(val);
                StringBuilder sb = new StringBuilder();
                boolean primero = true;
                if ((medio & Alumno.NOTIFICAR_IMPRESO) == Alumno.NOTIFICAR_IMPRESO) {
                    sb.append("Carta");
                    primero = false;
                }
                if ((medio & Alumno.NOTIFICAR_EMAIL) == Alumno.NOTIFICAR_EMAIL) {
                    if (!primero) {
                        sb.append(", ");
                    }
                    sb.append("Email");
                    primero = false;
                }
                if ((medio & Alumno.NOTIFICAR_SMS) == Alumno.NOTIFICAR_SMS) {
                    if (!primero) {
                        sb.append(", ");
                    }
                    sb.append("SMS");
                    primero = false;
                }
                if ((medio & Alumno.NOTIFICAR_TELEFONO) == Alumno.NOTIFICAR_TELEFONO) {
                    if (!primero) {
                        sb.append(", ");
                    }
                    sb.append("Tlf.");
                    primero = false;
                }
                setText(sb.toString());
            }
        });
        tabla.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                int row = tabla.getSelectedRow();
                String texto = "";
                if (row > -1) {
                    row = tabla.convertRowIndexToModel(row);
                    texto = modelo.getElemento(row).getDescripcion();
                }
                taTextoAviso.setText(texto);
                panelInfo.setVisible(!texto.trim().equals(""));
            }
        });
        panelInfo.setVisible(false);
    }

    public boolean isModoFichaAlumno() {
        return modoFichaAlumno;
    }

    public void setModoFichaAlumno(boolean modoFichaAlumno) {
        this.modoFichaAlumno = modoFichaAlumno;
        tabla.getColumnExt("Alumno").setVisible(!modoFichaAlumno);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        barraHerramientas = new javax.swing.JToolBar();
        bActualizar = new javax.swing.JButton();
        bAbrir = new javax.swing.JButton();
        bIdentificador = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabla = new org.jdesktop.swingx.JXTable();
        panelInfo = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        taTextoAviso = new javax.swing.JTextArea();

        setName("maimonides.paneles.datos.notificaciones"); // NOI18N
        addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                formAncestorAdded(evt);
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });
        setLayout(new java.awt.BorderLayout());

        barraHerramientas.setRollover(true);
        barraHerramientas.setName("barraHerramientas"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getActionMap(PanelCorrespondencia.class, this);
        bActualizar.setAction(actionMap.get("actualizar")); // NOI18N
        bActualizar.setFocusable(false);
        bActualizar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bActualizar.setName("bActualizar"); // NOI18N
        bActualizar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraHerramientas.add(bActualizar);

        bAbrir.setAction(actionMap.get("abrir")); // NOI18N
        bAbrir.setFocusable(false);
        bAbrir.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bAbrir.setName("bAbrir"); // NOI18N
        bAbrir.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraHerramientas.add(bAbrir);

        bIdentificador.setAction(actionMap.get("buscar")); // NOI18N
        bIdentificador.setFocusable(false);
        bIdentificador.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bIdentificador.setName("bIdentificador"); // NOI18N
        bIdentificador.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barraHerramientas.add(bIdentificador);

        add(barraHerramientas, java.awt.BorderLayout.PAGE_START);

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        tabla.setModel(modelo);
        tabla.setColumnControlVisible(true);
        tabla.setName("tabla"); // NOI18N
        jScrollPane1.setViewportView(tabla);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);

        panelInfo.setName("panelInfo"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getResourceMap(PanelCorrespondencia.class);
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        taTextoAviso.setColumns(20);
        taTextoAviso.setRows(5);
        taTextoAviso.setName("taTextoAviso"); // NOI18N
        jScrollPane2.setViewportView(taTextoAviso);

        javax.swing.GroupLayout panelInfoLayout = new javax.swing.GroupLayout(panelInfo);
        panelInfo.setLayout(panelInfoLayout);
        panelInfoLayout.setHorizontalGroup(
            panelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelInfoLayout.createSequentialGroup()
                .addComponent(jLabel1)
                .addContainerGap(511, Short.MAX_VALUE))
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 545, Short.MAX_VALUE)
        );
        panelInfoLayout.setVerticalGroup(
            panelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelInfoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE))
        );

        add(panelInfo, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void formAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_formAncestorAdded
        if (!Beans.isDesignTime() && getAlumno() != null) {
            cargar();
        }
    }//GEN-LAST:event_formAncestorAdded

    @Override
    public void setAlumno(Alumno alumno) {
        this.alumno = alumno;
        vaciar();
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
        return cargado;
    }

    @Override
    public void setCargado(boolean cargado) {
        this.cargado = cargado;
    }

    @Override
    public Alumno getAlumno() {
        return alumno;
    }

    @Action(block = Task.BlockingScope.WINDOW)
    public Task actualizar() {
        return new ActualizarTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class), null);
    }

    @Override
    public boolean puedoSusituir() {
        return true;
    }

    private class ActualizarTask extends org.jdesktop.application.Task<ArrayList<Carta>, Void> {

        String localizador = null;

        ActualizarTask(org.jdesktop.application.Application app, String localizador) {
            super(app);
            modelo.vaciar();
            this.localizador = localizador;
            setUserCanCancel(true);
        }

        @Override
        protected ArrayList<Carta> doInBackground() {
            ArrayList<Carta> cartas = new ArrayList<Carta>();
            PreparedStatement st = null;
            ResultSet res = null;
            String extra = "";
            if (getAlumno() != null && getAlumno().getId() != null) {
                extra = " AND al.id=? ";
            }
            String extra2 = "";
            Unidad u = Permisos.getFiltroUnidad();
            if (u != null) {
                extra2 = " AND al.unidad_id=? ";
            }
            String extra3 = "";
            if (localizador != null) {
                extra3 = " AND c.localizador=? ";
            }
            try {
                String sql = "SELECT c.* FROM cartas AS c JOIN alumnos AS al ON al.id=c.alumno_id WHERE al.ano=? " + extra + extra2 + extra3 + " LIMIT 0,1000";
                st = (PreparedStatement) MaimonidesApp.getConexion().prepareStatement(sql);
                int cont = 0;
                cont++;
                st.setInt(cont, MaimonidesApp.getApplication().getAnoEscolar().getId());
                if (getAlumno() != null && getAlumno().getId() != null) {
                    cont++;
                    st.setInt(cont, getAlumno().getId());
                }
                if (u != null) {
                    cont++;
                    st.setInt(cont, u.getId());
                }
                if (localizador != null) {
                    cont++;
                    st.setString(cont, localizador);
                }
                res = st.executeQuery();
                while (res.next() && !isCancelled()) {
                    Carta c = new Carta();
                    c.cargarDesdeResultSet(res);
                    cartas.add(c);
                }
            } catch (SQLException ex) {
                Logger.getLogger(PanelCorrespondencia.class.getName()).log(Level.SEVERE, null, ex);
            }
            Obj.cerrar(st, res);
            return cartas;
        }

        @Override
        protected void succeeded(ArrayList<Carta> result) {
            setCargado(true);
            modelo.setDatos(result);
            tabla.packAll();
        }
    }

    @Action
    public void abrir() {
        int row = tabla.getSelectedRow();
        if (row > -1) {
            Carta c = modelo.getElemento(tabla.convertRowIndexToModel(row));
            File archivo = c.getArchivo();
            if (archivo != null && archivo.exists()) {
                try {
                    Desktop.getDesktop().open(archivo);
                } catch (IOException ex) {
                    Logger.getLogger(PanelCorrespondencia.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Action(block = Task.BlockingScope.APPLICATION)
    public Task buscar() {
        //Primero pedimos el identificador
        String ident = JOptionPane.showInputDialog(this, "Introduzca el localizador de la notificación:", "Localizar notificación", JOptionPane.PLAIN_MESSAGE);
        if (ident != null) {
            return new ActualizarTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class), ident);
        }
        return null;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bAbrir;
    private javax.swing.JButton bActualizar;
    private javax.swing.JButton bIdentificador;
    private javax.swing.JToolBar barraHerramientas;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPanel panelInfo;
    private javax.swing.JTextArea taTextoAviso;
    private org.jdesktop.swingx.JXTable tabla;
    // End of variables declaration//GEN-END:variables
}
