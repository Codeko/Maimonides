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


package com.codeko.apps.maimonides.alumnos;

import com.codeko.apps.maimonides.MaimonidesBean;
import com.codeko.swing.IObjetoTabla;

public class ResultadoBusquedaAlumno extends MaimonidesBean implements IObjetoTabla {

    int id = 0;
    String nombre = "";
    String unidad = "";

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    @Override
    public int getNumeroDeCampos() {
        return 2;
    }

    @Override
    public Object getValueAt(int index) {
        Object val = null;
        switch (index) {
            case 0:
                val = getUnidad();
                break;
            case 1:
                val = getNombre();
                break;
        }
        return val;
    }

    @Override
    public String getTitleAt(int index) {
        String val = null;
        switch (index) {
            case 0:
                val = "Unidad";
                break;
            case 1:
                val = "Alumno";
                break;
        }
        return val;
    }

    @Override
    public Class getClassAt(int index) {
        return String.class;
    }

    @Override
    public boolean setValueAt(int index, Object valor) {
        return false;
    }

    @Override
    public boolean esCampoEditable(int index) {
        return false;
    }

    @Override
    public String toString() {
        return getNombre() + " [" + getUnidad() + "]";
    }
}
