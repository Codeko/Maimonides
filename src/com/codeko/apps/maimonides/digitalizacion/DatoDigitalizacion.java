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
