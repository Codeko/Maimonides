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


package com.codeko.apps.maimonides.horarios;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.elementos.AnoEscolar;
import com.codeko.apps.maimonides.elementos.Materia;
import com.codeko.util.Obj;
import com.mysql.jdbc.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 * Una materia virtual define una materia identificada por su nombre y código.
 * Esta se convertirá en materia real una vez se conozcan las unidades/cursos a la que se aplica.
 */
public class MateriaVirtual {

    String codigo = "";
    String nombre = "";

    public MateriaVirtual(String codigo, String nombre) {
        setCodigo(codigo);
        setNombre(nombre);
    }

    public String getCodigo() {
        return codigo;
    }

    public final void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public final void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return "<html>"+getNombre(); //"<html><b>" + getCodigo() + "</b>: " + getNombre();
    }

    @Override
    public boolean equals(Object obj) {
        boolean ret = false;
        if (obj instanceof MateriaVirtual) {
            MateriaVirtual m = (MateriaVirtual) obj;
            ret = getCodigo().equals(m.getCodigo()) && getNombre().equals(m.getNombre());
        }
        return ret;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + (this.codigo != null ? this.codigo.hashCode() : 0);
        hash = 83 * hash + (this.nombre != null ? this.nombre.hashCode() : 0);
        return hash;
    }

    public Materia getMateria(AnoEscolar ano, Integer cursoId) {
        Materia m = null;
        String sql = "SELECT * FROM materias WHERE ano=? AND codigo_materia=? AND nombre=? AND curso_id=? ";
        PreparedStatement st = null;
        ResultSet res = null;
        try {
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
            st.setInt(1, ano.getId());
            st.setString(2, getCodigo());
            st.setString(3, getNombre());
            st.setInt(4, cursoId);
            res = st.executeQuery();
            if (res.next()) {
                m = new Materia();
                try {
                    m.cargarDesdeResultSet(res);
                } catch (Exception ex) {
                    Logger.getLogger(MateriaVirtual.class.getName()).log(Level.SEVERE, null, ex);
                    m = null;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(MateriaVirtual.class.getName()).log(Level.SEVERE, null, ex);
        }
        Obj.cerrar(st, res);
        return m;
    }
}
