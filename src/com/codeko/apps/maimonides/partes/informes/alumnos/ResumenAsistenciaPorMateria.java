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


package com.codeko.apps.maimonides.partes.informes.alumnos;

import com.codeko.apps.maimonides.elementos.Materia;
import com.codeko.swing.CdkAutoTabla;
import com.codeko.swing.CdkAutoTablaCol;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
@CdkAutoTabla(procesarPadre = true)
public class ResumenAsistenciaPorMateria extends ResumenAsistencia {
    @CdkAutoTablaCol(titulo = "Cod.")
    String nombre = "";
    @CdkAutoTablaCol(titulo = "Descripción")
    String descripcion = "";
    @CdkAutoTablaCol(ignorar = true)
    Materia materia = null;

    @CdkAutoTablaCol(titulo="Máximo")
    public int getMaxFaltas(){
        return getMateria().getMaxFaltas();
    }
    
    public Materia getMateria() {
        return materia;
    }

    public void setMateria(Materia materia) {
        this.materia = materia;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    protected void limpiar() {
        super.limpiar();
        setNombre("");
        setDescripcion("");
    }
}
