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
public class Actualizacion14 extends Actualizacion {

    @Override
    public String getDescripcion() {
        return "Nuevo campo de código de faltas.";
    }

    @Override
    public boolean necesitaConfirmacion() {
        return false;
    }

    @Override
    public boolean ejecutar() {
        boolean ret = true;
        if (!existeCampo("codFaltas", "alumnos")) {
            ret = ret & ejecutarSQL("ALTER TABLE `alumnos` ADD COLUMN `codFaltas` VARCHAR(50)  NOT NULL DEFAULT '';");
        }
        return ret;
    }
}

