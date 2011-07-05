package com.codeko.apps.maimonides.mantenimiento.actualizaciones;

import com.codeko.apps.maimonides.mantenimiento.Actualizacion;

/**
 *
 * @author codeko
 */
public class Actualizacion10 extends Actualizacion {

    @Override
    public String getDescripcion() {
        return "Control de las lineas de faltas";
    }

    @Override
    public boolean necesitaConfirmacion() {
        return false;
    }

    @Override
    public boolean ejecutar() {
        boolean ret = true;
        if (!existeCampo("estado", "partes_alumnos")) {
            ret = ret & ejecutarSQL("ALTER TABLE `partes_alumnos` ADD COLUMN `estado` INT(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '0=No hay cambios, 1=Enviado' AFTER `posicion`");
        }
        ret = ret & ejecutarSQL("CREATE TRIGGER actualizar_estado_lineas_faltas BEFORE UPDATE ON partes_alumnos " +
                " FOR EACH ROW BEGIN " +
                " IF OLD.estado = 0 THEN " +
                " SET NEW.estado=1; " +
                " END IF; " +
                " END ");
        return ret;
    }
}
