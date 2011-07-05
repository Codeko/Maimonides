package com.codeko.apps.maimonides.mantenimiento.actualizaciones;

import com.codeko.apps.maimonides.mantenimiento.Actualizacion;

/**
 *
 * @author codeko
 */
public class Actualizacion17 extends Actualizacion {

    @Override
    public String getDescripcion() {
        return "Envío de partes de convivencia a Séneca.";
    }

    @Override
    public boolean necesitaConfirmacion() {
        return false;
    }

    @Override
    public boolean ejecutar() {
        boolean ret = true;
        if (!existeCampo("situacion", "conv_partes")) {
            ret = ret & ejecutarSQL("ALTER TABLE `conv_partes` ADD COLUMN `situacion` INTEGER UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Situación de control' ");
        }
        return ret;
    }

    @Override
    public String getNotificacion(){
        return "<html>Para el correcto funcionamiento de los envíos de partes a Séneca es necesario:<ul><li>Volver a recuperar los datos de medidas y conductas desde Séneca en el panel de configuración pestaña Convivencia/Partes de convivencia</li><li>Para una correcta asignación de las medidas de convivencia tomadas es necesario asociar los periodos de expulsión a las medidas en el panel de configuración pestaña Convivencia/Generador de expulsiones</li></ul>";
    }
}
