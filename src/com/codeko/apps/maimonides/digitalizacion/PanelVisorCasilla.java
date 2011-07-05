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
 * PanelVisorCasilla.java
 *
 * Created on 11 de noviembre de 2008, 12:21
 */
package com.codeko.apps.maimonides.digitalizacion;

import com.codeko.apps.maimonides.MaimonidesUtil;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;

/**
 *
 * @author  Codeko
 */
public class PanelVisorCasilla extends javax.swing.JPanel {

    Task tCarga = null;
    BufferedImage imagen = null;
    int fila = 0;
    int hora = 0;
    MensajeDigitalizacion mensajeActual = null;

    public MensajeDigitalizacion getMensajeActual() {
        return mensajeActual;
    }

    public void setMensajeActual(MensajeDigitalizacion mensajeActual) {
        this.mensajeActual = mensajeActual;
    }

    public BufferedImage getImagen() {
        return imagen;
    }

    public void setImagen(BufferedImage imagen) {
        this.imagen = imagen;
    }

    public int getFila() {
        return fila;
    }

    public void setFila(int fila) {
        this.fila = fila;
    }

    public int getHora() {
        return hora;
    }

    public void setHora(int hora) {
        this.hora = hora;
    }

    /** Creates new form PanelVisorCasilla */
    public PanelVisorCasilla() {
        initComponents();
    }

    public void limpiar() {
        lImagen.setIcon(null);
    }

    public void mostrar(MensajeDigitalizacion m) {
        setMensajeActual(m);
        limpiar();
        if (m.getLineaParteAlumno() != null) {
            if (m.getImagenCasilla() != null) {
                lImagen.setIcon(m.getImagenCasilla());
            } else {
                mostrar(null, m.getLineaParteAlumno().getPosicion(), m.getHorario().getHora());
            }
        }
    }

    public void mostrar(BufferedImage imagen, int fila, int hora) {
        setImagen(imagen);
        setFila(fila);
        setHora(hora);
        MaimonidesUtil.ejecutarTask(this, "cargar");
    }

    @Action
    public Task cargar() {
        if (tCarga != null) {
            tCarga.cancel(false);
        }
        tCarga = new CargarTask(org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class));
        return tCarga;
    }

    private class CargarTask extends org.jdesktop.application.Task<Boolean, Void> {

        CargarTask(org.jdesktop.application.Application app) {
            super(app);
            lImagen.setVisible(false);
            lProceso.setProcesando(true);
            lProceso.setVisible(true);
//            if (getImagen() == null) {
//                lProceso.setProcesando(false, false);
//            }
        }

        @Override
        protected Boolean doInBackground() {
           // setImagen(getMensajeActual().cargarImagen());
           // if (getImagen() != null) {
                try {
                    Thread.sleep(300);
                    if (!isCancelled()) {
                        limpiar();
                        DigitalizacionParte dig = null;
                        Image i = null;
                        try {
                            lImagen.setText("");
                            if (getMensajeActual().getParte() != null) {
                                //TODO La página está puesta a pelo, debería ser la del parte
                                i = CacheImagenes.getImagenCasilla(getMensajeActual().getParte(), 1, getFila(), getHora());
                            }
//                            else {
//                                dig = new DigitalizacionParte(getImagen());
//                                dig.setIdParte(getMensajeActual().getParte().getId());
//                                dig.setPagina(1);
//                                dig.prepararImagenExtraccionCasillas();
//                                i = dig.getImagen(((getHora() - 1) * 2), getFila() - 1, 2);
//                            }
                        } catch (Exception ex) {
                            Logger.getLogger(PanelVisorCasilla.class.getName()).log(Level.SEVERE, null, ex);
                        } finally {
                            if (dig != null) {
                                dig.dispose();
                            }
                            dig = null;
                        }
                        ImageIcon ic = null;
                        if (i != null) {
                            ic = new ImageIcon(i);
                        } else {
                            lImagen.setText("No se ha podido cargar la casilla.");
                        }
                        lImagen.setIcon(ic);
                        getMensajeActual().setImagenCasilla(ic);
                        i = null;

                        return true;
                    } else {
                        Logger.getLogger(PanelVisorCasilla.class.getName()).info("Cancelada carga...");
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(PanelVisorCasilla.class.getName()).log(Level.SEVERE, null, ex);
                }
            //}
            return false;
        }

        @Override
        protected void succeeded(Boolean result) {
            lProceso.setProcesando(false, result);
            if (result) {
                lImagen.setVisible(true);
                lProceso.setVisible(false);
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

        lImagen = new javax.swing.JLabel();
        lProceso = new com.codeko.swing.CdkProcesoLabel();

        setName("Form"); // NOI18N

        lImagen.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.codeko.apps.maimonides.MaimonidesApp.class).getContext().getResourceMap(PanelVisorCasilla.class);
        lImagen.setText(resourceMap.getString("lImagen.text")); // NOI18N
        lImagen.setName("lImagen"); // NOI18N

        lProceso.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lProceso.setText(resourceMap.getString("lProceso.text")); // NOI18N
        lProceso.setName("lProceso"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lImagen, javax.swing.GroupLayout.DEFAULT_SIZE, 224, Short.MAX_VALUE)
            .addComponent(lProceso, javax.swing.GroupLayout.DEFAULT_SIZE, 224, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(lImagen, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lProceso, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lImagen;
    private com.codeko.swing.CdkProcesoLabel lProceso;
    // End of variables declaration//GEN-END:variables
}
