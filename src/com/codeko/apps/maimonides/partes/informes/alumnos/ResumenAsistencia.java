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

import com.codeko.apps.maimonides.MaimonidesBean;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.ParteFaltas;
import com.codeko.swing.CdkAutoTablaCol;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class ResumenAsistencia extends MaimonidesBean {

    @CdkAutoTablaCol(ignorar = true)
    Alumno alumno = null;
    @CdkAutoTablaCol(ignorar = true)
    int indeterminadas = 0;
    @CdkAutoTablaCol(ignorar = true)
    int asistidas = 0;
    @CdkAutoTablaCol(titulo = "J")
    int justificadas = 0;
    @CdkAutoTablaCol(titulo = "I")
    int injustificadas = 0;
    @CdkAutoTablaCol(titulo = "R")
    int retrasos = 0;
    @CdkAutoTablaCol(titulo = "E")
    int expulsadas = 0;
    @CdkAutoTablaCol(ignorar = true)
    int diasDiferentesAsistidas = 0;
    @CdkAutoTablaCol(ignorar = true)
    int diasDiferentesJustificadas = 0;
    @CdkAutoTablaCol(ignorar = true)
    int diasDiferentesInjustificadas = 0;
    @CdkAutoTablaCol(ignorar = true)
    int diasDiferentesRetrasos = 0;
    @CdkAutoTablaCol(ignorar = true)
    int diasDiferentesExpulsiones = 0;
    //TODO Esto debe defibnirse de forma global
    @CdkAutoTablaCol(ignorar = true)
    public static final int HORAS_POR_DIA = 6;

    public Alumno getAlumno() {
        return alumno;
    }

    public void setAlumno(Alumno alumno) {
        Alumno old = this.alumno;
        this.alumno = alumno;
        firePropertyChange("alumno", old, alumno);
    }

    public int getExpulsadas() {
        return expulsadas;
    }

    public int getDiasExpulsados() {
        return getExpulsadas() / HORAS_POR_DIA;
    }

    public void setExpulsadas(int expulsadas) {
        this.expulsadas = expulsadas;
    }

    public int getAsistidas() {
        return asistidas;
    }

    public int getDiasAsistidos() {
        return getAsistidas() / HORAS_POR_DIA;
    }

    public void setAsistidas(int asistidas) {
        this.asistidas = asistidas;
    }

    public int getIndeterminadas() {
        return indeterminadas;
    }

    public int getDiasIndeterminados() {
        return getIndeterminadas() / HORAS_POR_DIA;
    }

    public void setIndeterminadas(int indeterminadas) {
        this.indeterminadas = indeterminadas;
    }

    public int getInjustificadas() {
        return injustificadas;
    }

    public int getDiasInjustificados() {
        return getInjustificadas() / HORAS_POR_DIA;
    }

    public int getJustificadas() {
        return justificadas;
    }

    public int getDiasJustificadosReal() {
        return getJustificadas() / HORAS_POR_DIA;
    }

    public void setInjustificadas(int injustificadas) {
        this.injustificadas = injustificadas;
    }

    public int getDiasJustificados() {
        return getJustificadas() / HORAS_POR_DIA;
    }

    public void setJustificadas(int justificadas) {
        this.justificadas = justificadas;
    }

    public int getRetrasos() {
        return retrasos;
    }

    public int getDiasRetrasos() {
        return getRetrasos() / HORAS_POR_DIA;
    }

    public void setRetrasos(int retrasos) {
        this.retrasos = retrasos;
    }

    @CdkAutoTablaCol(titulo = "Total")
    public int getTotalFaltas() {
        return getJustificadas() + getInjustificadas() + getRetrasos() + getExpulsadas();
    }

    public int getDiasTotalFaltas() {
        return getTotalFaltas() / HORAS_POR_DIA;
    }

    @CdkAutoTablaCol(titulo = "I+R")
    public int getInjustificasRetrasos() {
        return getInjustificadas() + getRetrasos();
    }

    public int getDiasInjustificosRetrasos() {
        return (getInjustificadas() + getRetrasos()) / HORAS_POR_DIA;
    }

    public void setValor(int tipoFalta, int total, int totalDias) {
        switch (tipoFalta) {
            case ParteFaltas.FALTA_ASISTENCIA:
                setAsistidas(total);
                setDiasDiferentesAsistidas(totalDias);
                break;
            case ParteFaltas.FALTA_EXPULSION:
                setExpulsadas(total);
                setDiasDiferentesExpulsiones(totalDias);
                break;
            case ParteFaltas.FALTA_INDETERMINADA:
                setIndeterminadas(total);
                break;
            case ParteFaltas.FALTA_INJUSTIFICADA:
                setInjustificadas(total);
                setDiasDiferentesInjustificadas(totalDias);
                break;
            case ParteFaltas.FALTA_JUSTIFICADA:
                setJustificadas(total);
                setDiasDiferentesJustificadas(totalDias);
                break;
            case ParteFaltas.FALTA_RETRASO:
                setRetrasos(total);
                setDiasDiferentesRetrasos(totalDias);
                break;
        }
    }

    public int getDiasDiferentesAsistidas() {
        return diasDiferentesAsistidas;
    }

    public void setDiasDiferentesAsistidas(int diasDiferentesAsistidas) {
        this.diasDiferentesAsistidas = diasDiferentesAsistidas;
    }

    public int getDiasDiferentesExpulsiones() {
        return diasDiferentesExpulsiones;
    }

    public void setDiasDiferentesExpulsiones(int diasDiferentesExpulsiones) {
        this.diasDiferentesExpulsiones = diasDiferentesExpulsiones;
    }

    public int getDiasDiferentesInjustificadas() {
        return diasDiferentesInjustificadas;
    }

    public void setDiasDiferentesInjustificadas(int diasDiferentesInjustificadas) {
        this.diasDiferentesInjustificadas = diasDiferentesInjustificadas;
    }

    public int getDiasDiferentesJustificadas() {
        return diasDiferentesJustificadas + getDiasDiferentesExpulsiones();
    }

    public void setDiasDiferentesJustificadas(int diasDiferentesJustificadas) {
        this.diasDiferentesJustificadas = diasDiferentesJustificadas;
    }

    public int getDiasDiferentesRetrasos() {
        return diasDiferentesRetrasos;
    }

    public void setDiasDiferentesRetrasos(int diasDiferentesRetrasos) {
        this.diasDiferentesRetrasos = diasDiferentesRetrasos;
    }

    protected void limpiar() {
        setAsistidas(0);
        setIndeterminadas(0);
        setInjustificadas(0);
        setJustificadas(0);
        setRetrasos(0);

        setDiasDiferentesAsistidas(0);
        setDiasDiferentesExpulsiones(0);
        setDiasDiferentesInjustificadas(0);
        setDiasDiferentesJustificadas(0);
        setDiasDiferentesRetrasos(0);
    }

    public int getDiasDiferentesInjustificadasRetrasos() {
        return getDiasDiferentesInjustificadas() + getDiasDiferentesRetrasos();
    }

    public int getTotalDiasDiferentes() {
        return getDiasDiferentesInjustificadasRetrasos() + getDiasDiferentesJustificadas();
    }
}
