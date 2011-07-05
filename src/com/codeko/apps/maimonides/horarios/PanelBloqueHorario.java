/*
 * PanelBloqueHorario.java
 *
 * Created on 21-abr-2009, 17:51:59
 */
package com.codeko.apps.maimonides.horarios;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.elementos.Actividad;
import com.codeko.apps.maimonides.elementos.Unidad;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.swingx.JXHyperlink;

/**
 *
 * @author Codeko
 */
public class PanelBloqueHorario extends javax.swing.JPanel {

    BloqueHorario bloque = null;
    PanelCeldaHorario padre = null;
    ResourceMap rmap = MaimonidesApp.getApplication().getContext().getResourceMap(PanelCeldaHorario.class);

    /** Creates new form PanelBloqueHorario */
    public PanelBloqueHorario() {
        initComponents();
    }

    public final PanelCeldaHorario getPadre() {
        return padre;
    }

    public final void setPadre(PanelCeldaHorario padre) {
        this.padre = padre;
    }

    public final BloqueHorario getBloque() {
        return bloque;
    }

    public final void setBloque(BloqueHorario bloque) {
        this.bloque = bloque;
    }

    public JXHyperlink getBoton() {
        return bBoton;
    }

    public PanelBloqueHorario(PanelCeldaHorario padre, BloqueHorario bloque, String br, Color colorFondo, int modoMostrar, int tam, boolean forzarMostrarUnidades) {
        initComponents();
        setPadre(padre);
        setBloque(bloque);
        bBoton.putClientProperty("bloque", getBloque());
        if (getPadre().getPadre().isEditable()) {
            bBoton.setTransferHandler(new TransferHandler() {

                @Override
                public boolean canImport(TransferHandler.TransferSupport info) {
                    info.setShowDropLocation(true);
                    if (!info.isDrop()) {
                        return false;
                    }
                    if (info.isDataFlavorSupported(BloqueHorarioTransferible.BH_FLAVOR)) {
                        //return true;
                        try {
                            Transferable t = info.getTransferable();
                            Object oBloqueDestino = ((JComponent) info.getComponent()).getClientProperty("bloque");
                            Object d = t.getTransferData(BloqueHorarioTransferible.BH_FLAVOR);
                            if (d instanceof BloqueHorario && oBloqueDestino instanceof BloqueHorario) {
                                BloqueHorario bloqueDestino = (BloqueHorario) oBloqueDestino;
                                BloqueHorario original = (BloqueHorario) d;
                                if (original.getDia() != bloqueDestino.getDia() || original.getHora() != bloqueDestino.getHora()) {
                                    return true;
                                }
                            }
                        } catch (Exception ex) {
                            //Logger.getLogger(PanelCeldaHorario.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    return false;
                }

                @Override
                public boolean importData(TransferHandler.TransferSupport info) {
                    info.setShowDropLocation(true);
                    if (!canImport(info)) {
                        return false;
                    }
                    try {
                        Transferable t = info.getTransferable();
                        Object oBloqueDestino = ((JComponent) info.getComponent()).getClientProperty("bloque");
                        Object d = t.getTransferData(BloqueHorarioTransferible.BH_FLAVOR);
                        if (d instanceof BloqueHorario && oBloqueDestino instanceof BloqueHorario) {
                            BloqueHorario bloqueDestino = (BloqueHorario) oBloqueDestino;
                            BloqueHorario original = (BloqueHorario) d;
                            if (original.getDia() != bloqueDestino.getDia() || original.getHora() != bloqueDestino.getHora()) {
                                return original.mover(bloqueDestino.getDia(), bloqueDestino.getHora());
                            }
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(PanelCeldaHorario.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return false;
                }

                @Override
                public void exportDone(JComponent c, Transferable t, int action) {
                    //System.out.println("Exportado:" + c + " T:" + t + " A:" + action);
                }

                @Override
                public int getSourceActions(JComponent c) {
                    return MOVE;
                }

                @Override
                protected Transferable createTransferable(JComponent c) {
                    return new BloqueHorarioTransferible(getBloque());
                }
            });
            final JPopupMenu menu = new JPopupMenu("Bloque horario");
            JMenuItem mEditar = new JMenuItem(rmap.getString("mEditar.text"));
            mEditar.setIcon(rmap.getIcon("mEditar.icon"));
            mEditar.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    editarBloque(getBloque());
                }
            });
            menu.add(mEditar);
            bBoton.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 1 && SwingUtilities.isRightMouseButton(e)) {
                        menu.show((Component) e.getSource(), e.getX(), e.getY());
                    } else if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                        editarBloque(getBloque());
                    }
                }
            });
            bBoton.addMouseMotionListener(new MouseMotionListener() {

                @Override
                public void mouseDragged(MouseEvent e) {
                    JComponent jc = (JComponent) e.getSource();
                    TransferHandler th = jc.getTransferHandler();
                    th.exportAsDrag(jc, e, TransferHandler.MOVE);
                }

                @Override
                public void mouseMoved(MouseEvent arg0) {
                }
            });
            JMenuItem mBorrar = new JMenuItem(rmap.getString("mBorrar.text"));
            mBorrar.setIcon(rmap.getIcon("mBorrar.icon"));
            mBorrar.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    int op = JOptionPane.showConfirmDialog(MaimonidesApp.getApplication().getMainFrame(), "¿Está seguro de que desea eliminar el bloque horario seleccionado?", "Eliminar bloque horario", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (op == JOptionPane.YES_OPTION) {
                        if (getBloque().eliminar()) {
                            JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), "Bloque horario eliminado correctamente.", "Bloque horario eliminado", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), "Por alguna razón no se ha podido borrar el bloque horario.", "Bloque horario NO eliminado", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            });
            menu.add(mBorrar);

            JMenuItem mNuevo = new JMenuItem(rmap.getString("mNuevo.text"));
            mNuevo.setIcon(rmap.getIcon("mNuevo.icon"));
            mNuevo.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    Actividad a;
                    try {
                        a = Actividad.getActividad(Actividad.getIdActividadDocencia(MaimonidesApp.getApplication().getAnoEscolar()));
                        BloqueHorario bloque2 = new BloqueHorario(MaimonidesApp.getApplication().getAnoEscolar(), 0, 0, a, null, null);
                        if (PanelCeldaHorario.getUnidadActiva() != null) {
                            bloque2.addUnidad(PanelCeldaHorario.getUnidadActiva());
                        } else {
                            for (Unidad u : getBloque().getUnidades()) {
                                bloque2.addUnidad(u);
                            }
                        }
                        if (PanelCeldaHorario.getProfesorActivo() != null) {
                            bloque2.setProfesor(PanelCeldaHorario.getProfesorActivo());
                        } else {
                            bloque2.setProfesor(getBloque().getProfesor());
                        }

                        bloque2.setDia(getBloque().getDia());
                        bloque2.setHora(getBloque().getHora());

                        DialogoEditorHorarios dlg = new DialogoEditorHorarios(getPadre().getPadre(), bloque2);
                        dlg.setTitle("Nuevo bloque horario");
                        dlg.mostrar();
                    } catch (Exception ex) {
                        Logger.getLogger(PanelEditorHorarios.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
            menu.add(mNuevo);
        }
        bBoton.setBackground(colorFondo);
        if (getBloque().isDicu()) {
            bBoton.setForeground(Color.DARK_GRAY);
            bBoton.setClickedColor(Color.DARK_GRAY);
        }
        bBoton.putClientProperty("color_original", colorFondo);
        bBoton.putClientProperty("color_original_letra", bBoton.getForeground());
        bBoton.addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
                if (!e.isTemporary()) {
                    ((JXHyperlink) e.getSource()).setBackground(Color.BLUE);
                    ((JXHyperlink) e.getSource()).setForeground(Color.white);
                    ((JXHyperlink) e.getSource()).setClickedColor(Color.white);
                    getBloque().firePropertyChange("focoEnBloqueGanado", bBoton, getBloque());
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (!e.isTemporary()) {
                    Color color = (Color) ((JComponent) e.getSource()).getClientProperty("color_original");
                    Color colorLetra = (Color) ((JComponent) e.getSource()).getClientProperty("color_original_letra");
                    ((JComponent) e.getSource()).setBackground(color);
                    ((JComponent) e.getSource()).setForeground(colorLetra);
                    ((JXHyperlink) e.getSource()).setClickedColor(colorLetra);
                    getBloque().firePropertyChange("focoEnBloquePerdido", bBoton, getBloque());
                }
            }
        });
        String s = "<html><center><font size=3>%1$s</font>" + br + "<font size=2 face='verdana'>%2$s</font>" + br + "<font size=2 face='verdana'>%3$s</font></center>";
        if (modoMostrar == PanelCeldaHorario.MOSTRAR_NOMBRE) {
            s = "<html><center><font size=2 face='verdana'>%2$s</font>" + br + "<font size=2 face='verdana'>%3$s</font></center>";
        } else if (modoMostrar == PanelCeldaHorario.MOSTRAR_CODIGO) {
            s = "<html><center><font size=" + tam + ">%1$s</font>" + br + "<font size=2 face='verdana'>%3$s</font></center>";
        }
        String uds = "";
        if (getBloque().getUnidades().size() > 1 || (forzarMostrarUnidades && getBloque().getUnidades().size() > 0)) {
            uds += getBloque().getUnidades().get(0).getCurso();
            for (Unidad u : getBloque().getUnidades()) {
                uds += " " + u.getGrupo();
            }
        }
        if (getBloque().getMaterias().size() > 0) {
            String nombre = procesarNombre(getBloque().getMaterias().get(0).getDescripcion());
            s = String.format(s, getBloque().getMaterias().get(0).getCodigoMateria(), nombre, uds);
            bBoton.setToolTipText(getBloque().getMaterias().get(0).getDescripcion());
        } else {
            String nombre = procesarNombre(getBloque().getActividad().getDescripcion());
            s = String.format(s, getBloque().getActividad().getCodigoActividad(), nombre, uds);
            bBoton.setToolTipText(getBloque().getActividad().getDescripcion());
        }
        bBoton.setText(s);
        if (tam > 3) {
            bBoton.setHorizontalAlignment(SwingConstants.CENTER);
        }
        bBoton.setFont(bBoton.getFont().deriveFont(Font.BOLD));
    }

    public void revisarConflictos() {
        getBloque().resetearConflictos();
        if (getBloque().hayConflictos()) {
            bBoton.setIcon(rmap.getIcon("conflictos.icon"));
        } else {
            bBoton.setIcon(null);
        }
    }

    private void editarBloque(BloqueHorario bloque) {
        DialogoEditorHorarios dlg = new DialogoEditorHorarios(getPadre().getPadre(), bloque);
        dlg.setTitle("Editar bloque horario");
        dlg.mostrar();
    }

    private String procesarNombre(String nom) {
//        StringBuilder s = new StringBuilder(nom);
//        for (int i = 6; i < s.length(); i++) {
//            if (s.charAt(i) == ' ') {
//                s.replace(i, i, "<br/>");
//                i += 11;
//            }
//        }
//        return s.toString();
        return nom;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bBoton = new org.jdesktop.swingx.JXHyperlink();

        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout(5, 0));

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getResourceMap(PanelBloqueHorario.class);
        bBoton.setForeground(resourceMap.getColor("bBoton.foreground")); // NOI18N
        bBoton.setText(resourceMap.getString("bBoton.text")); // NOI18N
        bBoton.setClickedColor(resourceMap.getColor("bBoton.clickedColor")); // NOI18N
        bBoton.setName("bBoton"); // NOI18N
        bBoton.setOpaque(true);
        add(bBoton, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.jdesktop.swingx.JXHyperlink bBoton;
    // End of variables declaration//GEN-END:variables
}
