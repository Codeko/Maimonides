package com.codeko.apps.maimonides.importadores;

/**
 *
 * @author Codeko
 */
public class DatoXML {
    String nombre="";
    String valor="";

    public String getNombre() {
        return nombre;
    }

    public final void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getValor() {
        return valor;
    }

    public final void setValor(String valor) {
        this.valor = valor;
    }

    public DatoXML(String nombre,String valor) {
        setNombre(nombre);
        setValor(valor);
    }   
    
    
}
