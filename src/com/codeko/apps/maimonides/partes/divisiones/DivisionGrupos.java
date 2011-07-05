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


package com.codeko.apps.maimonides.partes.divisiones;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesUtil;
import com.codeko.apps.maimonides.elementos.Actividad;

import com.codeko.apps.maimonides.elementos.Horario;
import com.codeko.apps.maimonides.elementos.Materia;
import com.codeko.apps.maimonides.elementos.Unidad;
import com.codeko.util.Obj;
import com.mysql.jdbc.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class DivisionGrupos {

    String curso = null;
    int dia = 0;
    int hora = 0;
    Materia materia = null;
    Actividad actividad = null;
    ArrayList<LineaDivisionGrupo> lineas = null;

    public ArrayList<LineaDivisionGrupo> getLineas() {
        if (lineas == null) {
            lineas = new ArrayList<LineaDivisionGrupo>();
            String sql = "SELECT * FROM horarios_ AS horarios WHERE ano=? AND dia=? AND hora=? AND materia_id=? AND actividad_id=? ORDER BY unidad_id,dicu";
            try {
                PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
                st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
                st.setInt(2, getDia());
                st.setInt(3, getHora());
                if (getMateria() != null) {
                    st.setObject(4, getMateria().getId());
                } else {
                    st.setObject(4, null);
                }
                st.setObject(5, getActividad().getId());
                ResultSet res = st.executeQuery();
                ArrayList<Horario> horarios = new ArrayList<Horario>();
                int ultimaUnidad = -1;
                boolean ultimoDicu = false;
                while (res.next()) {
                    Horario h = new Horario();
                    h.cargarDesdeResultSet(res);
                    if (ultimaUnidad != -1 && (h.getUnidad() != ultimaUnidad || ultimoDicu != h.isDicu())) {
                        Unidad u = Unidad.getUnidad(ultimaUnidad);
                        LineaDivisionGrupo lin = new LineaDivisionGrupo(horarios, u);
                        horarios.clear();
                        lineas.add(lin);
                    }
                    ultimaUnidad = h.getUnidad();
                    ultimoDicu = h.isDicu();
                    horarios.add(h);
                }
                Obj.cerrar(st, res);
                if (horarios.size() > 0) {
                    Unidad u = Unidad.getUnidad(ultimaUnidad);
                    LineaDivisionGrupo lin = new LineaDivisionGrupo(horarios, u);
                    lineas.add(lin);
                }
            } catch (Exception ex) {
                Logger.getLogger(DivisionGrupos.class.getName()).log(Level.SEVERE, "Error recuperando lineas de horarios.", ex);
            }
        }
        return lineas;
    }

    public Actividad getActividad() {
        return actividad;
    }

    public final void setActividad(Actividad actividad) {
        this.actividad = actividad;
    }

    public String getCurso() {
        return curso;
    }

    public final void setCurso(String curso) {
        this.curso = curso;
    }

    public int getDia() {
        return dia;
    }

    public final void setDia(int dia) {
        this.dia = dia;
    }

    public int getHora() {
        return hora;
    }

    public final void setHora(int hora) {
        this.hora = hora;
    }

    public Materia getMateria() {
        return materia;
    }

    public final void setMateria(Materia materia) {
        this.materia = materia;
    }

    public DivisionGrupos(int dia, int hora, int materia_id, int actividad_id, String curso) throws Exception {
        setDia(dia);
        if (materia_id > 0) {
            setMateria(Materia.getMateria(materia_id));
        }
        setActividad(Actividad.getActividad(actividad_id));
        setCurso(curso);
        setHora(hora);
    }

    public String toStringExtendido() {
        StringBuilder sb = new StringBuilder();
        sb.append(MaimonidesUtil.getNombreDiaSemana(getDia(), true));
        sb.append(" ");
        sb.append(getHora());
        sb.append("ª Hora: ");

        if (getMateria() != null) {
            sb.append(getMateria().getDescripcion());
        } else {
            sb.append(getActividad().getDescripcion());
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(MaimonidesUtil.getNombreDiaSemana(getDia(), false));
        sb.append(" ");
        sb.append(getHora());
        sb.append("ª");
        sb.append(" ");
        if (getMateria() != null) {
            sb.append(getMateria().getCodigoMateria());
        } else {
            sb.append(getActividad().getCodigoActividad());
        }
        return sb.toString();
    }

    void resetearLineas() {
        lineas = null;
    }
}
