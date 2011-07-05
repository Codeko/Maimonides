/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codeko.apps.maimonides.mantenimiento.actualizaciones;

import com.codeko.apps.maimonides.mantenimiento.Actualizacion;

/**
 *
 * @author codeko
 */
public class Actualizacion15 extends Actualizacion {

    @Override
    public String getDescripcion() {
        return "Eliminación de campos de opciones en advertencias digitalización.";
    }

    @Override
    public boolean necesitaConfirmacion() {
        return false;
    }

    @Override
    public boolean ejecutar() {
        boolean ret = true;
        if (existeCampo("asistencia1", "partes_advertencias")) {
            ret = ret & ejecutarSQL("ALTER TABLE `partes_advertencias` DROP COLUMN `asistencia1`,DROP COLUMN `asistencia2`,DROP COLUMN `asistencia3`;");
        }
        return ret;
    }
}

