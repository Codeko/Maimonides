package com.codeko.apps.maimonides.mantenimiento.actualizaciones;

import com.codeko.apps.maimonides.mantenimiento.Actualizacion;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class Actualizacion7 extends Actualizacion {

    @Override
    public String getDescripcion() {
        return "Fechas de inicio y fin en Cursos e índices de asistencia.";
    }

    @Override
    public boolean necesitaConfirmacion() {
        return false;
    }

    @Override
    public boolean ejecutar() {

        boolean ret = true;
        if (!existeCampo("fini", "cursos")) {
            ret = ret & ejecutarSQL("ALTER TABLE `cursos` ADD COLUMN `fini` DATE ");
        }
        if (!existeCampo("ffin", "cursos")) {
            ret = ret & ejecutarSQL("ALTER TABLE `cursos` ADD COLUMN `ffin` DATE ");
        }
        ejecutarSQL("ALTER TABLE `partes_alumnos` DROP INDEX `Index_asistencia`;");
        ret = ret & ejecutarSQL("ALTER TABLE `partes_alumnos` ADD INDEX `Index_asistencia`(`asistencia`);");
        ret = ret & ejecutarSQL("DROP VIEW IF EXISTS `asistencia_`;");
        ret = ret & ejecutarSQL("CREATE VIEW `asistencia_` AS select a.id AS alumno_id,a.unidad_id,a.curso_id,a.ano,h.hora,pa.asistencia,p.fecha,h.id AS horario_id FROM alumnos AS a JOIN partes_alumnos AS pa ON pa.alumno_id=a.id JOIN partes AS p ON p.id=pa.parte_id JOIN horarios AS h ON h.id=pa.horario_id WHERE pa.asistencia>1");
        return ret;
    }
}
