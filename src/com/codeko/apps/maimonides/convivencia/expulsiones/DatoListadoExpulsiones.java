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
