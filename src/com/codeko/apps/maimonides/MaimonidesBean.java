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


package com.codeko.apps.maimonides;

import com.codeko.apps.maimonides.elementos.IObjetoBD;
import com.codeko.apps.maimonides.elementos.IObjetoBDConCod;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.application.AbstractBean;

/**
 *
 * @author Codeko
 */
public class MaimonidesBean extends AbstractBean {

    @Override
    public void firePropertyChange(String propiedad, Object valorAntiguio, Object nuevoValor) {
        super.firePropertyChange(propiedad, valorAntiguio, nuevoValor);
    }

    public boolean guardarObjetosBD(Collection<? extends IObjetoBD> objetos) {
        return guardarObjetosBD(objetos, false);
    }

    public boolean guardarObjetosBD(Collection<? extends IObjetoBD> objetos, boolean recrearEliminados) {
        boolean ret = true;
        //Ahora guardamos las unidades
        firePropertyChange("setMinimo", null, 0);
        firePropertyChange("setMaximo", 0, objetos.size());
        int count = 0;
        for (IObjetoBD o : objetos) {
            firePropertyChange("setProgreso", count, ++count);
            firePropertyChange("setMensaje", null, "Procesando " + o.getNombreObjeto() + " " + count + " de " + objetos.size());
            firePropertyChange("setInfoExtra", null, o.getDescripcionObjeto());

            try {
                if (o instanceof IObjetoBDConCod) {
                    if (!((IObjetoBDConCod)o).guardar(recrearEliminados)) {
                        firePropertyChange("setMensaje", null, "Error guardando " + o.getNombreObjeto() + " " + o.getDescripcionObjeto());
                        ret = false;
                    }
                } else if (!o.guardar()) {
                    firePropertyChange("setMensaje", null, "Error guardando " + o.getNombreObjeto() + " " + o.getDescripcionObjeto());
                    ret = false;
                }

            } catch (Exception e) {
                Logger.getLogger(MaimonidesBean.class.getName()).log(Level.WARNING, "Error guardando objeto '" + o + "'", e);
            }

        }
        return ret;
    }
}
