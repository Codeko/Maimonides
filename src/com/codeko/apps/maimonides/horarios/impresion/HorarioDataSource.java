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


package com.codeko.apps.maimonides.horarios.impresion;

import java.util.ArrayList;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

/**
 *
 * @author codeko
 */
public class HorarioDataSource implements JRDataSource{
    ArrayList<HorarioImprimible> horarios=null;
    int pos=-1;
    int posHorarioActual=0;
    HorarioImprimible horarioActual=null;
    public ArrayList<HorarioImprimible> getHorarios() {
        return horarios;
    }

    public void setHorarios(ArrayList<HorarioImprimible> horarios) {
        this.horarios = horarios;
    }

    @Override
    public boolean next() throws JRException {
        //Si no hay horario actual o hemos recorrido todos los bloques del actual avanzamos al siguiente
        if(horarioActual==null || !horarioActual.next()){
            this.pos++;
            if(pos<getHorarios().size()){
                horarioActual=horarios.get(pos);
            }
        }
        //Sobre el horario actual
        return pos<getHorarios().size() && horarioActual!=null;
    }

    @Override
    public Object getFieldValue(JRField jrf) throws JRException {
        Object ret="";
        if(jrf.getName().equals("titulo")){
            ret=horarioActual.getTitulo();
        }else if(jrf.getName().equals("materia")){
            ret=horarioActual.getMateria();
        }else if(jrf.getName().equals("aula")){
            ret=horarioActual.getAula();
        }else if(jrf.getName().equals("hora")){
            ret=horarioActual.getHora();
        }else if(jrf.getName().equals("dia")){
            ret=horarioActual.getDia();
        }else if(jrf.getName().equals("unidadActual")){
            ret=horarioActual.getUnidadActual();
        }else if(jrf.getName().equals("profesorActual")){
            ret=horarioActual.getProfesorActual();
        }else if(jrf.getName().equals("profesor")){
            ret=horarioActual.getProfesor();
        }else if(jrf.getName().equals("alumno")){
            ret=horarioActual.getAlumno();
        }else if(jrf.getName().equals("textoBloque")){
            ret=horarioActual.getTextoBloque();
        }else if(jrf.getName().equals("leyenda")){
            ret=horarioActual.getLeyenda();
        }
        return ret;
    }

}
