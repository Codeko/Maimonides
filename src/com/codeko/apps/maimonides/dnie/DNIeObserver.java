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

    private static final byte[] DNIe_ATR = {
        (byte) 0x3B, (byte) 0x7F, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x6A, (byte) 0x44,
        (byte) 0x4E, (byte) 0x49, (byte) 0x65, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x00, (byte) 0x90, (byte) 0x00};
    private static final byte[] DNIe_MASK = {
        (byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
        (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0xFF};
    private boolean daemon = true;

    private boolean isDaemon() {
        return daemon;
    }

    private void setDaemon(boolean daemon) {
        this.daemon = daemon;
    }

    public void start() throws CardException {

        TerminalFactory factory = TerminalFactory.getDefault();
        List<CardTerminal> terminals = factory.terminals().list();
        ThreadGroup tg = new ThreadGroup("CardObservers");
        PropertyChangeListener pcl = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                DNIe dni = (DNIe) pce.getNewValue();
                boolean isDNIe = isDNIe(dni.getCard());
                if (isDNIe) {
                    if ("cardDisconnected".equals(pce.getPropertyName())) {
                        firePropertyChange("dnieDisconnected", null, dni);
                    } else if ("cardConnected".equals(pce.getPropertyName())) {
                        try {
                            dni.loadPublicData();
                            dni.setPin("V8N284dB");
                            firePropertyChange("dnieConnected", null, dni);
                            System.out.println("Autentificado:" + dni.validar());
                        } catch (CardException ex) {
                            Logger.getLogger(DNIeObserver.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        };
        for (CardTerminal ct : terminals) {
            DNIeObserverThread dt = new DNIeObserverThread(ct);
            Thread t = new Thread(tg, dt);
            dt.addPropertyChangeListener(pcl);
            t.setDaemon(isDaemon());
            t.start();
        }
    }

    public static boolean isDNIe(Card card) {
        boolean isDNIe = false;
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
        return isDNIe;
    }

    public static void main(String[] args) {
        DNIeObserver d = new DNIeObserver();
        d.setDaemon(false);
        try {
            d.start();
        } catch (CardException ex) {
            Logger.getLogger(DNIeObserver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
