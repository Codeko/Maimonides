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

import com.codeko.apps.maimonides.convivencia.Expulsion;
import com.codeko.apps.maimonides.convivencia.ParteConvivencia;
import com.codeko.apps.maimonides.convivencia.TipoConducta;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.Unidad;
import com.codeko.swing.CdkAutoTabla;
import com.codeko.swing.CdkAutoTablaCol;
import java.util.ArrayList;
import java.util.GregorianCalendar;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
@CdkAutoTabla(editable = 0, mostrarTodos = false)
public class PropuestaExpulsion {

    GregorianCalendar fecha = null;
    @CdkAutoTablaCol(ignorar = false, titulo = "Alumno")
    Alumno alumno = null;
    ArrayList<ParteConvivencia> partes = new ArrayList<ParteConvivencia>();
    ArrayList<ParteConvivencia> partesAfectados = null;
    @CdkAutoTablaCol(ignorar = false, titulo = "Última expulsión")
    Expulsion ultimaExpulsion = null;
    @CdkAutoTablaCol(ignorar = false, titulo = "Expulsión propuesta")
    Expulsion expulsion = null;

    public PropuestaExpulsion(Alumno alumno, GregorianCalendar fecha) {
        setAlumno(alumno);
        setFecha(fecha);
        //Ahora calculamos la última expulsión
        setUltimaExpulsion(Expulsion.getUltimaExpulsionPorFechaDeRegreso(alumno));
    }

    @CdkAutoTablaCol(titulo = "Unidad")
    public Unidad getUnidadAlumno() {
        return getAlumno().getUnidad();
    }

    @CdkAutoTablaCol(titulo = "Partes de convivencia")
    public String getDescripcionPartes() {
        int partesLeves = 0;
        int partesGraves = 0;
        for (ParteConvivencia pc : getPartes()) {
            if (pc.getTipo() == TipoConducta.GRAVEDAD_GRAVE) {
                partesGraves++;
            } else if (pc.getTipo() == TipoConducta.GRAVEDAD_LEVE) {
                partesLeves++;
            }
        }
        StringBuilder sb = new StringBuilder();
        if (partesGraves > 0) {
            if (partesGraves == 1) {
                sb.append("1 parte grave.");
            } else {
                sb.append(partesLeves).append(" partes graves.");
            }
            sb.append(" ");
        }

        if (partesLeves > 0) {
            if (partesLeves == 1) {
                sb.append("1 parte leve.");
            } else {
                sb.append(partesLeves).append(" partes leves.");
            }
        }
        return sb.toString();
    }

    public GregorianCalendar getFecha() {
        return fecha;
    }

    private void setFecha(GregorianCalendar fecha) {
        this.fecha = fecha;
    }

    public Expulsion getExpulsion() {
        if (expulsion == null) {
            generarExpulsion();
        }
        return expulsion;
    }

    private void setExpulsion(Expulsion expulsion) {
        this.expulsion = expulsion;
    }

    private void generarExpulsion() {
        Expulsion e = new Expulsion();
        e.setAlumno(getAlumno());
        e.setFecha(getFecha());
        //Ahora para ver los días necesitamos ver el número de expulsiones que tiene el alumno
        int numExpulsiones = Expulsion.getNumeroExpulsiones(getAlumno(), getFecha());
        //Ahora vemos en la secuencia de expulsiones que valor nos corresponde
        ArrayList<Integer> secuencia = GeneradorExpulsiones.getSecuenciaExpulsiones();
        if (numExpulsiones >= secuencia.size()) {
            numExpulsiones = secuencia.size() - 1;
        }
        e.setDias(secuencia.get(numExpulsiones));
        setExpulsion(e);
    }

    public ArrayList<ParteConvivencia> getPartesAfectados() {
        if (partesAfectados == null) {
            calcularPartesAfectados();
        }
        return partesAfectados;
    }

    public void setPartesAfectados(ArrayList<ParteConvivencia> partesAfectados) {
        this.partesAfectados = partesAfectados;
    }

    private void calcularPartesAfectados() {
        ArrayList<ParteConvivencia> vPartesAfectados = new ArrayList<ParteConvivencia>();
        //Una vez que se acepta la propuesta de expulsión hay que dejar sólo los partes afectados por esa propuesta
        ArrayList<ParteConvivencia> pGs = getPartesGraves();
        if (pGs.size() > 0) {
            vPartesAfectados.add(pGs.get(0));//Los partes se recuperan ordenados por fechas
        } else {
            //Si no hay parte grave tenemos que coger los X leves
            ArrayList<ParteConvivencia> pLs = getPartesLeves();
            for (int i = 0; i < GeneradorExpulsiones.getEquivalenciaExpulsionesLeves(); i++) {
                vPartesAfectados.add(pLs.get(i));
            }
        }
        setPartesAfectados(vPartesAfectados);
    }

    public boolean aceptarPropuestaExpulsion() {
        boolean ret = true;
        //Una vez que tenemos los partes afectados guardamos la expulsión
        ret = getExpulsion().guardar();
        if (ret) {
            //Por cada parte afectado le asignamos la expulsión y le cambiamos el estado a sancionado
            for (ParteConvivencia p : getPartesAfectados()) {
                p.setExpulsionID(getExpulsion().getId());
                p.setEstado(ParteConvivencia.ESTADO_SANCIONADO);
                ret = p.guardar() & ret;
            }
        }
        return ret;
    }

    public void addParteConvivencia(ParteConvivencia parte) {
        getPartes().add(parte);
    }

    public Alumno getAlumno() {
        return alumno;
    }

    private void setAlumno(Alumno alumno) {
        this.alumno = alumno;
    }

    public ArrayList<ParteConvivencia> getPartes() {
        return partes;
    }

    private void setPartes(ArrayList<ParteConvivencia> partes) {
        this.partes = partes;
    }

    public Expulsion getUltimaExpulsion() {
        return ultimaExpulsion;
    }

    private void setUltimaExpulsion(Expulsion ultimaExpulsion) {
        this.ultimaExpulsion = ultimaExpulsion;
    }

    public ArrayList<ParteConvivencia> getPartesGraves() {
        ArrayList<ParteConvivencia> partesGraves = new ArrayList<ParteConvivencia>();
        for (ParteConvivencia pc : getPartes()) {
            if (pc.getTipo() == TipoConducta.GRAVEDAD_GRAVE) {
                partesGraves.add(pc);
            }
        }
        return partesGraves;
    }

    public ArrayList<ParteConvivencia> getPartesLeves() {
        ArrayList<ParteConvivencia> partesLeves = new ArrayList<ParteConvivencia>();
        for (ParteConvivencia pc : getPartes()) {
            if (pc.getTipo() == TipoConducta.GRAVEDAD_LEVE) {
                partesLeves.add(pc);
            }
        }
        return partesLeves;
    }

    public ParteConvivencia getUltimoParteConvivencia() {
        ParteConvivencia parte = null;
        for (ParteConvivencia pc : getPartes()) {
            if (parte == null) {
                parte = pc;
            } else if (pc.getFecha().compareTo(parte.getFecha()) > 0) {
                parte = pc;
            }
        }
        return parte;
    }

    public boolean isExpulsable() {
        //Primero vemos que el alumno no este ya expulsado
        boolean expulsable = false;
        //Si ya está expulsado no hay expulsión que valga
        if (!Expulsion.isAlumnoExpulsado(getAlumno(), getFecha())) {
            //Ahora vemos si el parte más moderno es posterior a la fecha de la última expulsión (si la hay)
            //Esto es para que siempre haya una falta entre expulsión y expulsión
            //TODO Esto debería ser configurable
            if (getUltimaExpulsion() == null) {
                expulsable = true;
            } else {
                ParteConvivencia pUltimo = getUltimoParteConvivencia();
                //Vemos si la fecha del parte es mayor o igual a la de regreso del alumno
                if (pUltimo.getFecha().compareTo(getUltimaExpulsion().getFechaRegreso()) >= 0) {
                    expulsable = true;
                }
            }
            if (expulsable) {
                //Si es expulsable ahora hay que ver si hay expulsión por cuestion de partes
                expulsable = false;
                if (getPartesGraves().size() > 0) {
                    expulsable = true;
                } else if (getPartesLeves().size() >= GeneradorExpulsiones.getEquivalenciaExpulsionesLeves()) {
                    //Si tiene suficientes leves como para que equivalgan a una grave se le expulsa tambien
                    expulsable = true;
                }
            }
        }
        return expulsable;
    }
}
