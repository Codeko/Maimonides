/**
 *  Maimónides, gestión para centros escolares.
 *  Copyright Codeko and individual contributors
 *  as indicated by the @author tags.
 * 
 *  This is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as
 *  published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 * 
 *  This software is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public
 *  License along with this software; if not, write to the Free
 *  Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *  
 *  For more information:
 *  maimonides@codeko.com
 *  http://codeko.com/maimonides
**/

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
