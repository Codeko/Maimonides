package com.codeko.apps.maimonides.elementos;

/**
 *
 * @author Codeko
 */
public interface IObjetoBD {

    public Integer getId();

    public void setId(Integer id);

    public boolean guardar();

    public boolean borrar();

    public String getNombreObjeto();

    public String getDescripcionObjeto();

    public String getTabla();
}
