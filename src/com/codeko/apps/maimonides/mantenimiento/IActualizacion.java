package com.codeko.apps.maimonides.mantenimiento;

/**
 *
 * @author Codeko
 */
public interface IActualizacion {
    public String getDescripcion();
    public boolean necesitaConfirmacion();
    public boolean ejecutar();
}
