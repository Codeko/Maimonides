package com.codeko.apps.maimonides.mantenimiento.actualizaciones;

import com.codeko.apps.maimonides.mantenimiento.*;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class Actualizacion1 extends Actualizacion {

    @Override
    public boolean ejecutar() {
        boolean ret = true;
        return ret;
    }

    @Override
    public String getDescripcion() {
        return "Primera actualzación vacía.";
    }

    @Override
    public boolean necesitaConfirmacion() {
        return false;
    }
}
