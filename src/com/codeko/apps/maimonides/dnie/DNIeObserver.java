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
package com.codeko.apps.maimonides.dnie;

import com.codeko.apps.maimonides.MaimonidesBean;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;

/**
 *
 * @author Codeko <codeko@codeko.com>
 */
public class DNIeObserver extends MaimonidesBean {

    private static DNIeObserver activeDNIeObserver = null;
    private static final byte[] DNIe_ATR = {
        (byte) 0x3B, (byte) 0x7F, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x6A, (byte) 0x44,
        (byte) 0x4E, (byte) 0x49, (byte) 0x65, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x00, (byte) 0x90, (byte) 0x00};
    private static final byte[] DNIe_MASK = {
        (byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
        (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0xFF};
    private boolean daemon = true;
    ThreadGroup tg = new ThreadGroup("CardObservers");
    private boolean started = false;

    private DNIeObserver() {
    }

    private boolean isStarted() {
        return started;
    }

    private void setStarted(boolean started) {
        this.started = started;
    }

    private boolean isDaemon() {
        return daemon;
    }

    private void setDaemon(boolean daemon) {
        this.daemon = daemon;
    }

    private void start() throws CardException {

        TerminalFactory factory = TerminalFactory.getDefault();
        List<CardTerminal> terminals = factory.terminals().list();

        PropertyChangeListener pcl = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                final DNIe dni = (DNIe) pce.getNewValue();
                Logger.getLogger(DNIeObserverThread.class.getName()).log(Level.INFO, "Notificaci\u00f3n. {0}: {1}", new Object[]{pce.getPropertyName(), pce.getNewValue()});
                if ("cardDisconnected".equals(pce.getPropertyName())) {
                    //TODO Verificar que ha estado conectado
                    firePropertyChange("dnieDisconnected", null, dni);
                } else if ("cardConnected".equals(pce.getPropertyName())) {
                    Thread t = new Thread() {
                        @Override
                        public void run() {
                            try {
                                //Por alguna razon si no se hace una pausa no accede bien a la tarjeta
                                Thread.sleep(500);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(DNIeObserver.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            firePropertyChange("message", null, "Nueva tarjeta detectada...");
                            boolean isDNIe = isDNIe(dni.getCardTerminal());
                            firePropertyChange("message", null, "Verificando que sea DNIe...");
                            if (isDNIe) {
                                firePropertyChange("message", null, "Es DNIe. Leyendo datos de tarjeta...");
                                if (dni.loadPublicData()) {
                                    firePropertyChange("message", null, "Nuevo DNIe detectado correctamente.");
                                    firePropertyChange("dnieConnected", null, dni);
                                } else {
                                    firePropertyChange("message", null, "Ha habido algún error accediendo a los datos de la tarjeta.");
                                }
                            } else {
                                firePropertyChange("message", null, "No es DNIe. Ignorando.");
                            }
                        }
                    };
                    t.start();
                } else {
                    firePropertyChange(pce.getPropertyName(), pce.getOldValue(), pce.getNewValue());
                }
            }
        };
        setStarted(true);
        for (CardTerminal ct : terminals) {
            DNIeObserverThread dt = new DNIeObserverThread(ct);
            Thread t = new Thread(tg, dt);
            t.setPriority(Thread.MIN_PRIORITY);
            dt.addPropertyChangeListener(pcl);
            t.setDaemon(isDaemon());
            t.start();
        }
    }

    private void stop() {
        tg.interrupt();
        setStarted(false);
    }

    public static void startObserver() {
        if (activeDNIeObserver == null) {
            activeDNIeObserver = new DNIeObserver();
        }
        if (!activeDNIeObserver.isStarted()) {
            try {
                activeDNIeObserver.start();
            } catch (CardException ex) {
                Logger.getLogger(DNIeObserver.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void stopObserver() {
        if (activeDNIeObserver != null && activeDNIeObserver.isStarted()) {
            activeDNIeObserver.stop();
        }
    }

    public static void addPropertyListener(PropertyChangeListener pcl) {
        startObserver();
        activeDNIeObserver.addPropertyChangeListener(pcl);
    }

    private boolean isDNIe(CardTerminal cardTerminal) {
        boolean isDNIe = false;
        Card card = null;
        try {
            firePropertyChange("message", null, "Conectándose a la tarjeta...");
            card = cardTerminal.connect("T=0");
            firePropertyChange("message", null, "Verificando si es DNIe...");
            byte[] atrCard = card.getATR().getBytes();
            if (atrCard.length == DNIe_ATR.length) {
                isDNIe = true;
                int j = 0;
                while (j < DNIe_ATR.length && isDNIe) {
                    if ((atrCard[j] & DNIe_MASK[j]) != (DNIe_ATR[j] & DNIe_MASK[j])) {
                        isDNIe = false;
                    }
                    j++;
                }
            } else {
                isDNIe = false;
            }

        } catch (CardException ex) {
            Logger.getLogger(DNIeObserver.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (card != null) {
                try {
                    card.disconnect(false);
                } catch (CardException ex) {
                    Logger.getLogger(DNIeObserver.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return isDNIe;
    }

    public static void main(String[] args) {
        DNIeObserver d = new DNIeObserver();
        d.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                System.out.println(pce.getPropertyName() + ": " + pce.getNewValue());
            }
        });
        d.setDaemon(false);
        try {
            d.start();
        } catch (CardException ex) {
            Logger.getLogger(DNIeObserver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
