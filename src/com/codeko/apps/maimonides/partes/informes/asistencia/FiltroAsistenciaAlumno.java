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
