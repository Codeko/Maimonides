/*
 * PanelCeldaHorario.java
 *
 * Created on 03-mar-2009, 19:47:53
 */
package com.codeko.apps.maimonides.horarios;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.elementos.Actividad;
import com.codeko.apps.maimonides.elementos.Profesor;
import com.codeko.apps.maimonides.elementos.Unidad;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import org.jdesktop.application.ResourceMap;

/**
 *
 * @author Codeko
 */
public class PanelCeldaHorario extends javax.swing.JPanel {

    private static Profesor profesorActivo = null;
    private static Unidad unidadActiva = null;

    public static Profesor getProfesorActivo() {
        return profesorActivo;
    }

    public static void setProfesorActivo(Profesor profesorActivo) {
        PanelCeldaHorario.profesorActivo = profesorActivo;
    }

    public static Unidad getUnidadActiva() {
        return unidadActiva;
    }

    public static void setUnidadActiva(Unidad unidadActiva) {
        PanelCeldaHorario.unidadActiva = unidadActiva;
    }
    PanelVisionHorario padre = null;
    PanelCeldaHorario auto = this;
    int dia = 0;
    int hora = 0;
    ArrayList<BloqueHorario> horarios = new ArrayList<BloqueHorario>();
    public static final int MOSTRAR_CODIGO_Y_NOMBRE = 0;
    public static final int MOSTRAR_NOMBRE = 1;
    public static final int MOSTRAR_CODIGO = 2;
    int modoMostrar = MOSTRAR_CODIGO_Y_NOMBRE;
    static HashMap<String, Color> colores = new HashMap<String, Color>();
    static int posicionColor = 0;
    private static Color[] codigosColores = new Color[]{
        Color.decode("#0099FF"),
        Color.decode("#00CC00"),
        Color.decode("#FFCC00"),
        Color.decode("#00CC99"),
        Color.decode("#FF6600"),
        Color.decode("#FFFF99"),
        Color.decode("#99FFFF"),
        Color.decode("#CC9900"),
        Color.decode("#3366FF"),
        Color.decode("#999900"),
        Color.decode("#FFCCFF"),
        Color.decode("#FF3333"),
        Color.decode("#00FF99"),
        Color.decode("#99FF99"),
        Color.decode("#6699FF"),
        Color.decode("#9999FF"),
        Color.decode("#99CC00"),
        Color.decode("#CCCC00"),
        Color.decode("#FF99FF"),
        Color.decode("#CCCC99"),
        Color.decode("#CCFF33"),
        Color.decode("#9966FF"),
        Color.decode("#CCCCCC"),
        Color.decode("#FFFFFF"),
        Color.decode("#00FFFF"),
        Color.decode("#CC6666"),
        Color.decode("#FF6699"),
        Color.decode("#009999"),
        Color.decode("#FFFF00")
    };
    boolean forzarMostrarUnidades = false;

    public static HashMap<String, Color> getColores() {
        return colores;
    }

    public static Color getColor(String cod) {
        Color c = null;
        if (getColores().containsKey(cod)) {
            c = getColores().get(cod);
        } else {
            if (posicionColor >= codigosColores.length) {
                posicionColor = 0;
            }
            c = codigosColores[posicionColor];
            posicionColor++;
            getColores().put(cod, c);
        }
        return c;
    }

    public static void resetearColores() {
        getColores().clear();
        posicionColor = 0;
    }

    public final PanelVisionHorario getPadre() {
        return padre;
    }

    private void setPadre(PanelVisionHorario padre) {
        this.padre = padre;
    }

    public PanelCeldaHorario() {
        initComponents();
    }

    public PanelCeldaHorario(PanelVisionHorario padre, int dia, int hora) {
        setPadre(padre);
        setDia(dia);
        setHora(hora);
        initComponents();
        if (getPadre().isEditable()) {
            setTransferHandler(new TransferHandler() {

                @Override
                public boolean canImport(TransferHandler.TransferSupport info) {
                    info.setShowDropLocation(true);
                    if (info.isDataFlavorSupported(BloqueHorarioTransferible.BH_FLAVOR)) {
                        //return true;
                        try {
                            Transferable t = info.getTransferable();
                            Object d = t.getTransferData(BloqueHorarioTransferible.BH_FLAVOR);
                            if (d instanceof BloqueHorario) {
                                BloqueHorario original = (BloqueHorario) d;
                                if (original.getDia() != getDia() || original.getHora() != getHora()) {
                                    return true;
                                }
                            }
                        } catch (UnsupportedFlavorException ex) {
                            Logger.getLogger(PanelCeldaHorario.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IOException ex) {
                            Logger.getLogger(PanelCeldaHorario.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    return false;
                }

                @Override
                public boolean importData(TransferHandler.TransferSupport info) {
                    info.setShowDropLocation(true);
                    try {
                        Transferable t = info.getTransferable();
                        Object d = t.getTransferData(BloqueHorarioTransferible.BH_FLAVOR);
                        if (d instanceof BloqueHorario) {
                            BloqueHorario original = (BloqueHorario) d;
                            if (original.getDia() != getDia() || original.getHora() != getHora()) {
                                return original.mover(getDia(), getHora());
                            }
                        }
                    } catch (UnsupportedFlavorException ex) {
                        Logger.getLogger(PanelCeldaHorario.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(PanelCeldaHorario.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return false;
                }

                @Override
                public void exportDone(JComponent c, Transferable t, int action) {
                    System.out.println("PC Exportado:" + c + " T:" + t + " A:" + action);
                }

                @Override
                public int getSourceActions(JComponent c) {
                    return NONE;
                }

                @Override
                protected Transferable createTransferable(JComponent c) {
                    return null;
                }
            });

            ResourceMap rmap = MaimonidesApp.getApplication().getContext().getResourceMap(PanelCeldaHorario.class);
            final JPopupMenu menu = new JPopupMenu("Bloque horario");
            JMenuItem mNuevo = new JMenuItem(rmap.getString("mNuevo.text"));
            mNuevo.setIcon(rmap.getIcon("mNuevo.icon"));
            mNuevo.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    Actividad a;
                    try {
                        a = Actividad.getActividad(Actividad.getIdActividadDocencia(MaimonidesApp.getApplication().getAnoEscolar()));
                        BloqueHorario bloque2 = new BloqueHorario(MaimonidesApp.getApplication().getAnoEscolar(), 0, 0, a, null, null);
                        if (getUnidadActiva() != null) {
                            bloque2.addUnidad(getUnidadActiva());
                        }
                        if (getProfesorActivo() != null) {
                            bloque2.setProfesor(getProfesorActivo());
                        }
                        bloque2.setDia(getDia());
                        bloque2.setHora(getHora());
                        DialogoEditorHorarios dlg = new DialogoEditorHorarios(getPadre(), bloque2);
                        dlg.setTitle("Nuevo bloque horario");
                        dlg.mostrar();
                    } catch (Exception ex) {
                        Logger.getLogger(PanelEditorHorarios.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
            menu.add(mNuevo);
            this.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 1 && SwingUtilities.isRightMouseButton(e)) {
                        menu.show((Component) e.getSource(), e.getX(), e.getY());
                    }
                }
            });
        }
    }

    public boolean isForzarMostrarUnidades() {
        return forzarMostrarUnidades;
    }

    public void setForzarMostrarUnidades(boolean forzarMostrarUnidades) {
        this.forzarMostrarUnidades = forzarMostrarUnidades;
    }

    public ArrayList<BloqueHorario> getHorarios() {
        return horarios;
    }

    public void limpiar() {
        for (BloqueHorario b : getHorarios()) {
            b.removePropertyChangeListener(getPadre().listenerBloques);
        }
        getHorarios().clear();
        resetearColores();
        reprocesar();
    }

    public int getModoMostrar() {
        return modoMostrar;
    }

    public void setModoMostrar(int modoMostrar) {
        this.modoMostrar = modoMostrar;
        reprocesar();
    }

    public void reprocesar() {
        removeAll();
        GridLayout l = ((GridLayout) getLayout());
        l.setColumns(1);
        l.setRows(0);
        ArrayList<BloqueHorario> hs = getHorariosActivos();
        Collections.sort(hs);
        l.setVgap(3);
        int tam = 3;
        if (hs.size() == 1) {
            tam += 2;
        } else if (hs.size() < 3) {
            tam++;
        } else {
            tam--;
        }
        String br = "<br/>";
        if (hs.size() > 2) {
            br = " ";
        }
        for (BloqueHorario bloque : hs) {
            String cod = bloque.getActividad() + ":";
            if (bloque.getMaterias().size() > 0) {
                cod += bloque.getMaterias().get(0);
            }
            PanelBloqueHorario pbh = new PanelBloqueHorario(this, bloque, br, getColor(cod), getModoMostrar(), tam, isForzarMostrarUnidades());
            pbh.putClientProperty("bloque", bloque);
            add(pbh);
        }
        updateUI();
    }

    public void revisarConflictos() {
        for (Component c : getComponents()) {
            JComponent jc = (JComponent) c;
            if (jc instanceof PanelBloqueHorario) {
                ((PanelBloqueHorario) jc).revisarConflictos();
            }
        }
    }

    public int getDia() {
        return dia;
    }

    public final void setDia(int dia) {
        this.dia = dia;
    }

    public int getHora() {
        return hora;
    }

    public final void setHora(int hora) {
        this.hora = hora;
    }

    public ArrayList<BloqueHorario> getHorariosActivos() {
        ArrayList<BloqueHorario> hs = new ArrayList<BloqueHorario>();
        for (BloqueHorario h : getHorarios()) {
            if (h.isActivo()) {
                hs.add(h);
            }

        }
        return hs;
    }

    void marcar(BloqueHorario b) {
        for (Component c : getComponents()) {
            JComponent jc = (JComponent) c;
            if (b.equals(jc.getClientProperty("bloque"))) {
                if (jc instanceof PanelBloqueHorario) {
                    ((PanelBloqueHorario) jc).getBoton().requestFocus();
                } else {
                    jc.requestFocus();
                }

            }
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

        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        setMaximumSize(new java.awt.Dimension(100, 100));
        setName("Form"); // NOI18N
        setLayout(new java.awt.GridLayout(1, 0));
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
