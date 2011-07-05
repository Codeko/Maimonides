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


package com.codeko.apps.maimonides.partes.informes.asistencia;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class FiltroAsistenciaAlumno {

    public static final int TIPO_FALTAS = 0;
    public static final int TIPO_DIAS_COMPLETOS = 1;
    int tipoFalta = 0;//Usar las
    int tipo = TIPO_FALTAS;
    String operador = "";
    int valor = 0;

    public FiltroAsistenciaAlumno(int tipoFalta, int tipo, String operador, int valor) {
        setTipoFalta(tipoFalta);
        setTipo(tipo);
        setOperador(operador);
        setValor(valor);
    }

    public int getTipoFalta() {
        return tipoFalta;
    }

    public void setTipoFalta(int tipoFalta) {
        this.tipoFalta = tipoFalta;
    }

    public String getOperador() {
        return operador;
    }

    public void setOperador(String operador) {
        this.operador = operador;
    }

    public int getTipo() {
        return tipo;
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }

    public int getValor() {
        return valor;
    }

    public void setValor(int valor) {
        this.valor = valor;
    }
}
