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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

/**
 *
 * @author Codeko <codeko@codeko.com>
 */
public final class DNIeObserverThread extends MaimonidesBean implements Runnable {

        private static final int CARD_WAIT = 1000;
        private CardTerminal cardTerminal = null;
        private Card card = null;

        public DNIeObserverThread(CardTerminal ct) {
            setCardTerminal(ct);
        }

        @Override
        public void run() {
            try {
                //Verificamos la tarjeta actual conectada
                checkCard();
                //Esperamos a que se conecte o desconecte la tarjeta
                if (getCardTerminal().isCardPresent()) {
                    while (getCardTerminal().isCardPresent() && !Thread.interrupted()) {
                        getCardTerminal().waitForCardAbsent(CARD_WAIT);
                    }

                } else {
                    while (!getCardTerminal().isCardPresent() && !Thread.interrupted()) {
                        getCardTerminal().waitForCardPresent(CARD_WAIT);
                    }
                }
            } catch (CardException ex) {
                Logger.getLogger(DNIeObserverThread.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (!Thread.interrupted()) {
                //Volvemos a realizar el proceso
                run();
            }
        }

        private void checkCard() throws CardException {
            if (getCardTerminal().isCardPresent()) {
                Card tmpCard = getCardTerminal().connect("T=0");
                if (!tmpCard.equals(getCard())) {
                    if (getCard() != null) {
                        firePropertyChange("cardDisconnected", null,new DNIe(getCardTerminal(),getCard()));
                        setCard(null);
                    }
                    setCard(tmpCard);
                    firePropertyChange("cardConnected", null, new DNIe(getCardTerminal(),getCard()));
                }
            } else {
                if (getCard() != null) {
                    firePropertyChange("cardDisconnected", null,new DNIe(getCardTerminal(),getCard()));
                    setCard(null);
                }
            }
        }

        public CardTerminal getCardTerminal() {
            return cardTerminal;
        }

        private void setCardTerminal(CardTerminal cardTerminal) {
            this.cardTerminal = cardTerminal;
        }

        public Card getCard() {
            return card;
        }

        private void setCard(Card card) {
            this.card = card;
        }
    }
