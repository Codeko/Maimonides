package com.codeko.apps.maimonides.mantenimiento.actualizaciones;

import com.codeko.apps.maimonides.mantenimiento.Actualizacion;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class Actualizacion6 extends Actualizacion {

    @Override
    public String getDescripcion() {
        return "Mejoras en sistema de horarios";
    }

    @Override
    public boolean necesitaConfirmacion() {
        return false;
    }

    @Override
    public boolean ejecutar() {
        boolean ret = true;
        ret = ret && ejecutarSQL("ALTER TABLE `partes_horarios` MODIFY COLUMN `firmado` TINYINT(1) NOT NULL DEFAULT 1;");
        ret = ret && ejecutarSQL("ALTER TABLE `horarios` ADD COLUMN `falta` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, ADD COLUMN `fborrado` TIMESTAMP ");
        ret = ret && ejecutarSQL("CREATE VIEW `horarios_` AS SELECT * FROM horarios WHERE borrado=0 ");
        //Se borran las asignaciones con horarios que van con unidades que no corresponde
        ret= ret && ejecutarSQL("DELETE alumnos_horarios FROM alumnos_horarios JOIN alumnos AS a ON alumnos_horarios.alumno_id=a.id JOIN horarios AS h ON h.id=alumnos_horarios.horario_id WHERE h.unidad_id!=a.unidad_id ");
        return true;
    }
}
