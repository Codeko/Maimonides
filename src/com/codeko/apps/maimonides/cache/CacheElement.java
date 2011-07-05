package com.codeko.apps.maimonides.cache;

/**
 * Almacena un elemento y el timestamp de su creación
 * @author codeko
 */
public class CacheElement {

    short duracion = Cache.DURACION_CACHE;
    Object elemento = null;
    long timestamp = 0;

    public CacheElement(Object elemento, short duracion) {
        setElemento(elemento);
        setDuracion(duracion);
    }

    public CacheElement(Object elemento) {
        setElemento(elemento);
    }

    public short getDuracion() {
        return duracion;
    }

    public final void setDuracion(short duracion) {
        this.duracion = duracion;
    }

    public void updateTime() {
        setTimestamp(System.currentTimeMillis());
    }

    public boolean isValid() {
        //El elemento se considera válido durante 10 minutos
        boolean ret = false;
        ret = (System.currentTimeMillis() - getTimestamp()) < (1000 * 60 * getDuracion());
        return ret;
    }

    public Object getElemento() {
        return elemento;
    }

    public final void setElemento(Object elemento) {
        this.elemento = elemento;
        //Actualizamos el tiempo del elemento
        updateTime();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public final void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
