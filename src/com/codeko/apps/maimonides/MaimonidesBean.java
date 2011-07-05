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
