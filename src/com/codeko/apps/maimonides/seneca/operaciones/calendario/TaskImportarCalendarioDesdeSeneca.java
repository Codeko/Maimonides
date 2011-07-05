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


package com.codeko.apps.maimonides.seneca.operaciones.calendario;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.calendario.DiaCalendarioEscolar;
import com.codeko.apps.maimonides.seneca.ClienteSeneca;
import com.codeko.apps.maimonides.seneca.GestorUsuarioClaveSeneca;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import javax.swing.JOptionPane;

/**
 *
 * @author codeko
 */
public class TaskImportarCalendarioDesdeSeneca extends org.jdesktop.application.Task<Integer, Void> {

        boolean borrarPrimero = false;
        ClienteSeneca cli = null;

        public TaskImportarCalendarioDesdeSeneca(org.jdesktop.application.Application app) {
            super(app);
            int op = JOptionPane.showConfirmDialog(MaimonidesApp.getApplication().getMainFrame(), "¿Desea sobreescribir los festivos que se importen y que ya existan?", "Sobreescribir calendario", JOptionPane.YES_NO_CANCEL_OPTION);
            if (op == JOptionPane.CANCEL_OPTION) {
                cancel(false);
            } else {
                borrarPrimero = op == JOptionPane.YES_OPTION;
                if (!GestorUsuarioClaveSeneca.getGestor().pedirUsuarioClave()) {
                    cancel(false);
                } else {
                    cli = new ClienteSeneca(GestorUsuarioClaveSeneca.getGestor().getUsuario(), GestorUsuarioClaveSeneca.getGestor().getClave());
                    cli.setDebugMode(MaimonidesApp.isDebug());
                }
            }
        }

        @Override
        protected Integer doInBackground() {
            if (cli != null) {
                GestorCalendarioSeneca ges = new GestorCalendarioSeneca(cli);
                ges.addPropertyChangeListener(new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent pce) {
                        firePropertyChange(pce.getPropertyName(), pce.getOldValue(), pce.getNewValue());
                    }
                });
                cli.addPropertyChangeListener(new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent pce) {
                        firePropertyChange(pce.getPropertyName(), pce.getOldValue(), pce.getNewValue());
                    }
                });
                ArrayList<DiaCalendarioEscolar> dias = ges.getDiasFestivos();
                if (dias != null && dias.size() > 0) {
                    int dCount = 0;
                    for (DiaCalendarioEscolar d : dias) {
                        setMessage("Guardando " + d);
                        if (borrarPrimero) {
                            d.borrarFechasIguales();
                        }
                        if (d.guardar()) {
                            dCount++;
                        }
                    }
                    return dCount;
                }
            }
            return null;
        }

        @Override
        protected void succeeded(Integer result) {
            if (result != null) {
                setMessage("Se han importado " + result + " días festivos.");
                JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), "Se han importado " + result + " días festivos.", "Importación finalizada", JOptionPane.INFORMATION_MESSAGE);
            } else {
                setMessage("Ha habido algún error importando los datos.");
                JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), "Ha habido algún error importando los datos.", "Importación finalizada", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
