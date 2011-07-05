package com.codeko.apps.maimonides.cache;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Para implementar la cache en un elemento hay que:
 * Implementar el método getElemento(id)
 * Implementar la asignación en cache en el cargarDesdeResultSet
 * Implementar la asignación en cache en el guardar
 * Verificar que el constructor por id es privado
 * @author codeko
 */
public class Cache {

    public static final short DURACION_CACHE = 10;
    public static final int MAX_ELEMENTOS = 10000;
    public static final HashMap<Class, HashMap<Object, CacheElement>> cache = new HashMap<Class, HashMap<Object, CacheElement>>();

    public static void clear() {
        getCache().clear();
    }

     public static void clear(Class clase) {
        getCache().remove(clase);
    }

    public static synchronized Object get(Class clase, Object id) {
        Object ret = null;
        //recuperamos el hashmap de la clase en concreto
        HashMap<Object, CacheElement> hm = getCache().get(clase);
        if (hm != null) {
            //Ahora recuperamos el elemento
            CacheElement c = hm.get(id);
            if (c != null) {
                //Si es válido lo devolvemos
                if (c.isValid()) {
                    ret = c.getElemento();
                    c.updateTime();
                    //Si la clase no corresponde no devolvemos nada
                    if (ret != null && !ret.getClass().equals(clase)) {
                        ret = null;
                        //Y lo eliminamos de la cache
                        hm.remove(id);
                    }
                } else {
                    //Si no es valido lo eliminamos
                    hm.remove(id);
                }
            }
            //Si está vacío lo eliminamos de la cache
            if (hm.isEmpty()) {
                getCache().remove(clase);
            }
        }
        return ret;
    }

    public static synchronized void put(Class clase, Object id, Object elemento) {
        put(clase, id, elemento, DURACION_CACHE);
    }

    public static synchronized void put(Class clase, Object id, Object elemento, int duracion) {
        //Recuperamos el hashmap de la clase
        HashMap<Object, CacheElement> hm = getCache().get(clase);
        //Si no existe lo creamos
        if (hm == null) {
            hm = new HashMap<Object, CacheElement>();
            getCache().put(clase, hm);
        }
        //Ahora recuperamos el elemento
        CacheElement c = hm.get(id);
        if (c == null) {
            //Si no existe lo creamos y lo añadimos a la cache
            c = new CacheElement(elemento, (short) duracion);
            hm.put(id, c);
        } else {
            //Si existe lo actualizamos
            c.setElemento(elemento);
            c.setDuracion((short) duracion);
        }
        //Ahora verificamos si tiene más elementos del máximo
        checkHM(hm);
    }

    /**
     * Verifica que todos los elementos en un mapa de cache sean válidos
     * eliminado aquellos que no lo sean.
     * @param hm
     */
    private static synchronized void checkHM(HashMap<Object, CacheElement> hm) {
        if (hm.size() > MAX_ELEMENTOS) {
            //Si tiene más elementos eliminamos los que ya no sean válidos
            Iterator<Object> it = hm.keySet().iterator();
            while (it.hasNext()) {
                Object key = it.next();
                CacheElement c = hm.get(key);
                if (!c.isValid()) {
                    hm.remove(key);
                }
            }
        }
    }

    private static HashMap<Class, HashMap<Object, CacheElement>> getCache() {
        return cache;
    }
}
