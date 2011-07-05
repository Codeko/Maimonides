

package com.codeko.apps.maimonides.mantenimiento.actualizaciones;

import com.codeko.apps.maimonides.mantenimiento.Actualizacion;


public class Actualizacion19 extends Actualizacion {

    @Override
    public String getDescripcion() {
        return "Asignación de usuarios a notificaciones.";
    }

    @Override
    public boolean necesitaConfirmacion() {
        return false;
    }

    @Override
    public boolean ejecutar() {
        boolean ret = true;
        if (!existeCampo("usuario_id", "cartas")) {
            ret = ret & ejecutarSQL("ALTER TABLE `cartas` ADD COLUMN `usuario_id` INTEGER UNSIGNED, ADD CONSTRAINT `fk_cartero_usuario` FOREIGN KEY `fk_cartero_usuario` (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;");
        }
        return ret;
    }
}
