package com.codeko.apps.maimonides.mantenimiento.actualizaciones;

import com.codeko.apps.maimonides.importadores.horarios.ImportadorFicheroHorariosSeneca;
import com.codeko.apps.maimonides.mantenimiento.Actualizacion;

/**
 *
 * @author codeko
 */
public class Actualizacion12 extends Actualizacion {

    @Override
    public String getDescripcion() {
        return "Corrigiendo errores de Howr en fichero de horarios de Séneca";
    }

    @Override
    public boolean necesitaConfirmacion() {
        return false;
    }

    @Override
    public boolean ejecutar() {
        boolean ret = true;
        //ImportadorFicheroHorariosSeneca.corregirErroresHorariosHowr();
        return ret;
    }
}
