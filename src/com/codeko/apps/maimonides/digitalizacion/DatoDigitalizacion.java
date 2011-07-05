package com.codeko.apps.maimonides.digitalizacion;

import java.util.ArrayList;

public class DatoDigitalizacion {

    int fila = 0;
    ArrayList<Integer> columnas = new ArrayList<Integer>();

    public int getFila() {
        return fila;
    }

    protected final void setFila(int fila) {
        this.fila = fila;
    }

    public ArrayList<Integer> getColumnas() {
        return columnas;
    }

    public DatoDigitalizacion(int fila) {
        setFila(fila);
    }

    public void addColumna(int valor) {
        getColumnas().add(valor);
    }
}
