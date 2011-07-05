/*
 * PanelLateralScrollHorario.java
 *
 * Created on 04-mar-2009, 12:54:07
 */
package com.codeko.apps.maimonides.horarios;

import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 *
 * @author Codeko
 */
public class PanelLateralScrollHorario extends javax.swing.JPanel {

    /** Creates new form PanelLateralScrollHorario */
    public PanelLateralScrollHorario() {
        initComponents();
        for (int i = 1; i < 7; i++) {
            JLabel l = new JLabel(+i + "ª");
            l.setHorizontalAlignment(SwingConstants.CENTER);
            l.setFont(l.getFont().deriveFont(Font.BOLD));
            l.setFont(l.getFont().deriveFont(12f));
            add(l);
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
        setName("Form"); // NOI18N
        setLayout(new java.awt.GridLayout(PanelVisionHorario.HORAS_DIA, 1, 5, 5));
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
