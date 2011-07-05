package com.codeko.apps.maimonides.partes.informes;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */

public class ParteNoEntregadoPorDescripcion {

    public String curso = "";
    public String descripcion = "";
    public int partes = 0;

    public String getCurso() {
        return curso;
    }

    protected void setCurso(String curso) {
        this.curso = curso;
    }

    public String getDescripcion() {
        return descripcion;
    }

    protected void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getPartes() {
        return partes;
    }

    protected void setPartes(int partes) {
        this.partes = partes;
    }
}