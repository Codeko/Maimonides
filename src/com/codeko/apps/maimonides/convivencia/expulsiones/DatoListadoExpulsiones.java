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


package com.codeko.apps.maimonides.convivencia.expulsiones;

import com.codeko.apps.maimonides.MaimonidesBean;
import com.codeko.apps.maimonides.alumnos.IAlumno;
import com.codeko.apps.maimonides.cartero.Carta;
import com.codeko.apps.maimonides.convivencia.Expulsion;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.INotificado;
import com.codeko.apps.maimonides.elementos.Unidad;
import com.codeko.swing.CdkAutoTablaCol;
import java.util.GregorianCalendar;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class DatoListadoExpulsiones extends MaimonidesBean implements IAlumno,INotificado {

    @CdkAutoTablaCol(ignorar = true)
    Expulsion expulsion = null;
    @CdkAutoTablaCol(ignorar = true)
    Boolean notificado = null;

    public DatoListadoExpulsiones(Expulsion e) {
        setExpulsion(e);
    }

    public Expulsion getExpulsion() {
        return expulsion;
    }

    public final void setExpulsion(Expulsion expulsion) {
        this.expulsion = expulsion;
    }

    @CdkAutoTablaCol(titulo = "Unidad")
    public Unidad getUnidad() {
        return getExpulsion().getAlumno().getUnidad();
    }

    @CdkAutoTablaCol(titulo = "Alumno")
    @Override
    public Alumno getAlumno() {
        return getExpulsion().getAlumno();
    }

    @CdkAutoTablaCol(titulo = "Désde")
    public GregorianCalendar getDesde() {
        return getExpulsion().getFecha();
    }

    @CdkAutoTablaCol(titulo = "Hasta")
    public GregorianCalendar getHasta() {
        return getExpulsion().getFechaRegreso();
    }

    @CdkAutoTablaCol(titulo = "Días")
    public Integer getDias() {
        return getExpulsion().getDias();
    }

    @CdkAutoTablaCol(titulo = "Notificado")
    public Boolean getNotificado() {
        if (notificado == null) {
            notificado = (Carta.isNotificado(getAlumno(), Carta.TIPO_CARTA_EXPULSION, getExpulsion().getId() + ""));
        }
        return notificado;
    }

    @Override
    public Boolean isNotificado() {
        //TODO Fusionar los dos metodos
        return getNotificado();
    }
    
    
}
