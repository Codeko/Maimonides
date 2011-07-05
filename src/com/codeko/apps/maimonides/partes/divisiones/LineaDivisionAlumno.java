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
import com.codeko.apps.maimonides.MaimonidesBean;
import com.codeko.apps.maimonides.elementos.Actividad;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.Profesor;
import com.codeko.apps.maimonides.elementos.Unidad;
import com.codeko.swing.IObjetoTabla;
import com.mysql.jdbc.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class LineaDivisionAlumno extends MaimonidesBean implements IObjetoTabla {

    DivisionAlumnosMultimateria division = null;
    ArrayList<HorarioAlumno> horarios = new ArrayList<HorarioAlumno>();
    Alumno alumno = null;
    public static final int OFFSET = 5;

    public LineaDivisionAlumno(ArrayList<HorarioAlumno> horarios, Alumno alumno) {
        if (horarios != null) {
            getHorarios().addAll(horarios);
        }
        if (alumno != null) {
            setAlumno(alumno);
        }
    }

    public DivisionAlumnosMultimateria getDivision() {
        return division;
    }

    public void setDivision(DivisionAlumnosMultimateria division) {
        this.division = division;
    }

    public Alumno getAlumno() {
        return alumno;
    }

    public final void setAlumno(Alumno alumno) {
        this.alumno = alumno;
    }

    public final ArrayList<HorarioAlumno> getHorarios() {
        return horarios;
    }

    @Override
    public int getNumeroDeCampos() {
        return OFFSET + getHorarios().size();
    }

    @Override
    public Object getValueAt(int index) {
        Object val = null;
        switch (index) {
            case 0:
                val = val = this;
                break;
            case 1:
                val = getAlumno().getUnidad();
                break;
            case 2:
                val = getAlumno().isBilingue();
                break;
            case 3:
                val = getAlumno().isRepetidor();
                break;
            case 4:
                val = getAlumno().isDicu();
                break;
            default:
                index = index - OFFSET;
                if (index < getHorarios().size() && getHorarios().get(index)!=null) {
                    val = getHorarios().get(index).isActivoAlumno();
                }
        }
        return val;
    }

    @Override
    public String toString() {
        if(getAlumno()!=null){
            return getAlumno().getNombreFormateado();
        }
        return "";
    }

    @Override
    public String getTitleAt(int index) {
        String val = "-";
        switch (index) {
            case 0:
                val = "Alumno";
                break;
            case 1:
                val = "Unidad";
                break;
            case 2:
                val = "Bilingüe";
                break;
            case 3:
                val = "Repetidor";
                break;
            case 4:
                val = "D.I.C.U.";
                break;
            default:
                index = index - OFFSET;
                if (index < getHorarios().size() && getHorarios().get(index)!=null) {
                    if (getDivision().isMultimateria()) {
                        Actividad a = getHorarios().get(index).getObjetoActividad();
                        if (a != null) {
                            val = a.getCodigoPara(getHorarios().get(index).getObjetoMateria());
                        }
                    } else {
                        Profesor p = getHorarios().get(index).getObjetoProfesor();
                        if (p != null) {
                            val = p.getDescripcionObjeto();
                        }
                    }
                }
        }

        return val;
    }

    @Override
    public Class getClassAt(int index) {
        Class val = null;
        switch (index) {
            case 0:
                val = LineaDivisionAlumno.class;
                break;
            case 1:
                val = Unidad.class;
                break;
            case 2:
                val = Boolean.class;
                break;
            case 3:
                val = Boolean.class;
                break;
            case 4:
                val = Boolean.class;
                break;
            default:
                val = Boolean.class;
        }

        return val;
    }

    @Override
    public boolean setValueAt(int index, Object val) {
        boolean ret = false;
        if (index >= OFFSET) {
            index = index - OFFSET;
            if (index < getHorarios().size()) {
                HorarioAlumno h = getHorarios().get(index);
                h.setActivoAlumno((Boolean) val);
                ret = getHorarios().get(index).guardar();
            } else {
                //Si no lo encontramos podemos coger el de otro alumno
                //TODO Esta técnica no es buena. Inicialmente no debería darse este caso nunca pero se ha dado. Mientra se localiza el problema se hace uso de esta técnica
//                HorarioAlumno ha = null;
//                for (LineaDivisionAlumno la : getDivision().getLineas()) {
//                    if (index < la.getHorarios().size()) {
//                        ha = la.getHorarios().elementAt(index);
//                        break;
//                    }
//                }
//                if (ha != null) {
//                    try {
//                        HorarioAlumno nha = (HorarioAlumno) ha.clone();
//                        nha.setAlumno(getAlumno());
//                        while (getHorarios().size() <= index) {
//                            getHorarios().add(null);
//                        }
//                        getHorarios().setElementAt(nha, index);
//                        getHorarios().elementAt(index).setActivoAlumno((Boolean) val);
//                        ret = getHorarios().elementAt(index).guardar();
//                    } catch (CloneNotSupportedException ex) {
//                        Logger.getLogger(LineaDivisionAlumno.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//
//                }
            }
        }
        return ret;
    }

    public void guardarEnSimilares() {
        String sql = "UPDATE alumnos_horarios AS ah " +
                " JOIN horarios AS h ON h.id=ah.horario_id " +
                " SET ah.activo=? " +
                " WHERE h.materia_id=? AND h.profesor_id=? AND ah.alumno_id=?";
        try {
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
            for (HorarioAlumno h : getHorarios()) {
                st.setBoolean(1, h.isActivoAlumno());
                st.setInt(2, h.getMateria());
                st.setInt(3, h.getProfesor());
                st.setInt(4, h.getAlumno().getId());
                st.addBatch();
            }
            st.executeBatch();
            st.close();
        } catch (SQLException ex) {
            Logger.getLogger(LineaDivisionAlumno.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public boolean esCampoEditable(int index) {
        return index >= OFFSET && (index - OFFSET) < getHorarios().size() && getHorarios().get(index - OFFSET)!=null;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
