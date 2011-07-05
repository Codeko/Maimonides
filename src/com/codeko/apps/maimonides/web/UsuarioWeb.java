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


package com.codeko.apps.maimonides.web;

import com.codeko.apps.maimonides.elementos.Unidad;
import com.codeko.util.Num;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class UsuarioWeb {

    public static final int TIPO_ALUMNO = 0;
    public static final int TIPO_PROFESOR = 1;
    int id = 0;
    int tipo = 0;
    String nombre = "";
    String apellido1 = "";
    String apellido2 = "";
    String email = "";
    String departamento = "";
    String curso = "";
    String descripcion_curso = "";
    int numero_curso = 0;
    String grupo = "";
    int idGrupo = 0;

    public UsuarioWeb(int tipo, int id, String nombre, String apellido1, String apellido2, String email, String departamento, Unidad u) {
        setTipo(tipo);
        setId(id);
        setNombre(nombre);
        setApellido1(apellido1);
        setApellido2(apellido2);
        setEmail(email);
        setDepartamento(departamento);
        if (u != null) {
            setCurso(u.getCurso().substring(1));
            setDescripcion_curso(u.getDescripcion());
            setNumero_curso(Num.getInt(u.getCurso().substring(0, 1)));
            setGrupo(u.getGrupo());
            setIdGrupo(u.getId());
        }

    }

    public int getIdGrupo() {
        return idGrupo;
    }

    public void setIdGrupo(int idGrupo) {
        this.idGrupo = idGrupo;
    }

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getDescripcion_curso() {
        return descripcion_curso;
    }

    public void setDescripcion_curso(String descripcion_curso) {
        this.descripcion_curso = descripcion_curso;
    }

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }

    public int getNumero_curso() {
        return numero_curso;
    }

    public void setNumero_curso(int numero_curso) {
        this.numero_curso = numero_curso;
    }

    public String getApellido1() {
        return apellido1;
    }

    public void setApellido1(String apellido1) {
        this.apellido1 = apellido1;
    }

    public String getApellido2() {
        return apellido2;
    }

    public void setApellido2(String apellido2) {
        this.apellido2 = apellido2;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getTipo() {
        return tipo;
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }
}
