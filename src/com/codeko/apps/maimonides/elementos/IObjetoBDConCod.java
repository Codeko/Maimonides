package com.codeko.apps.maimonides.elementos;

/**
 *
 * @author codeko
 */
public interface IObjetoBDConCod extends IObjetoBD {
    public int cargarIdDesdeCod();
    public Integer getCodigo();
    public boolean guardar(boolean crearEliminados);
    public boolean recuperarBorrado();
}
