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


package com.codeko.apps.maimonides.partes.cartas;

import com.codeko.apps.maimonides.alumnos.IAlumno;
import com.codeko.apps.maimonides.asistencia.escolaridad.DatoPerdidaEscolaridadPorMaterias;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.Curso;
import com.codeko.apps.maimonides.elementos.Materia;
import com.codeko.apps.maimonides.elementos.Unidad;
import com.codeko.swing.CdkAutoTablaCol;
import java.util.Vector;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class DatoCartaPerdidaEscolaridadPorMaterias implements IAlumno {

    Alumno alumno = null;
    Vector<DatoPerdidaEscolaridadPorMaterias> datos = new Vector<DatoPerdidaEscolaridadPorMaterias>();

    public DatoCartaPerdidaEscolaridadPorMaterias(Alumno alumno) {
        setAlumno(alumno);
    }

    public void addDato(DatoPerdidaEscolaridadPorMaterias dato) {
        datos.add(dato);
    }

    @Override
    public Alumno getAlumno() {
        return alumno;
    }

    public void setAlumno(Alumno alumno) {
        this.alumno = alumno;
    }

    @CdkAutoTablaCol(titulo = "Curso")
    public Curso getCurso() {
        return getAlumno().getObjetoCurso();
    }

    @CdkAutoTablaCol(titulo = "Unidad")
    public Unidad getUnidad() {
        return getAlumno().getUnidad();
    }

    @CdkAutoTablaCol(titulo = "Materias")
    public String getMaterias() {
        StringBuilder sb = new StringBuilder("<html>");
        boolean primero = true;
        for (DatoPerdidaEscolaridadPorMaterias dc : datos) {
            if (!primero) {
                sb.append(" / ");
            }
            if(dc.isNotificado()){
                sb.append("<b>");
            }
            sb.append(dc.getMateria().toString());
            if(dc.isNotificado()){
                sb.append("</b>");
            }
            primero = false;
        }
        return sb.toString();
    }
    @CdkAutoTablaCol(titulo = "Notificado")
    public String getNotificado(){
        boolean haySi=false;
        boolean hayNo=false;
        String not="";
        for (DatoPerdidaEscolaridadPorMaterias dc : datos) {
            if(dc.isNotificado()){
                haySi=true;
            }else{
                hayNo=true;
            }
        }
        if(haySi && !hayNo){
            not="Si";
        }else if(haySi && hayNo){
            not="Parcialmente";
        }else{
            not="No";
        }
        return not;
    }

    public Vector<Materia> getVectorMaterias() {
        Vector<Materia> m = new Vector<Materia>();
        for (DatoPerdidaEscolaridadPorMaterias dc : datos) {
            m.add(dc.getMateria());
        }
        return m;
    }
}
