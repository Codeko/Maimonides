package com.codeko.apps.maimonides.asistencia;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.util.Num;

/**
 *
 * @author codeko
 */
public class ConfiguracionAsistencia {

    public static final String AAE_PARAMETRO = "accion_asistencia_expulsados";
    public static final int AAE_DEJAR_ASISTENCIA = 0;
    public static final int AAE_ADVERTENCIA = 1;
    public static final int AAE_ASIGNAR_EXPULSION = 2;
    public static final int AAE_ASIGNAR_EXPULSION_ADVERTENCIA = 3;

    public static int getAccionAsistenciaEnExpulsados() {
        return Num.getInt(MaimonidesApp.getApplication().getConfiguracion().get(AAE_PARAMETRO, AAE_ASIGNAR_EXPULSION + ""));
    }

    public static boolean isMostrarAdvertenciasAsignacionAsistenciaEnExpulsados() {
        return getAccionAsistenciaEnExpulsados() == AAE_ADVERTENCIA || getAccionAsistenciaEnExpulsados() == AAE_ASIGNAR_EXPULSION_ADVERTENCIA;
    }
}
